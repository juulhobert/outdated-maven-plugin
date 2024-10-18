package com.giovds;

import com.giovds.dto.PomResponse;
import com.giovds.poc.evaluator.MaintenanceEvaluator;
import com.giovds.poc.github.GithubCollector;
import com.giovds.poc.github.GithubGuesser;
import com.giovds.poc.github.model.internal.Collected;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@Mojo(name = "unmaintained",
        defaultPhase = LifecyclePhase.TEST_COMPILE,
        requiresOnline = true,
        requiresDependencyResolution = ResolutionScope.TEST)
public class UnmaintainedMojo extends AbstractMojo {

    private final PomClientInterface client;
    private final GithubGuesser githubGuesser;
    private final GithubCollector githubCollector;
    private final MaintenanceEvaluator maintenanceEvaluator;

    @Parameter(readonly = true, required = true, defaultValue = "${project}")
    private MavenProject project;

    /**
     * Required for initialization by Maven
     */
    public UnmaintainedMojo() {
        this(new PomClient(new SystemStreamLog()), new GithubGuesser(), new GithubCollector(new SystemStreamLog()), new MaintenanceEvaluator());
    }

    public UnmaintainedMojo(
            final PomClientInterface client,
            final GithubGuesser githubGuesser,
            final GithubCollector githubCollector,
            final MaintenanceEvaluator maintenanceEvaluator) {
        this.client = client;
        this.githubGuesser = githubGuesser;
        this.githubCollector = githubCollector;
        this.maintenanceEvaluator = maintenanceEvaluator;
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        final List<Dependency> dependencies = project.getDependencies();

        if (dependencies.isEmpty()) {
            // When building a POM without any dependencies there will be nothing to query.
            return;
        }

        final Map<Dependency, PomResponse> pomResponses = dependencies.stream()
                .map(dependency -> {
                    try {
                        PomResponse pomResponse = client.getPom(dependency.getGroupId(), dependency.getArtifactId(), dependency.getVersion());

                        return new DependencyPomResponsePair(dependency, pomResponse);
                    } catch (Exception e) {
                        getLog().error("Failed to fetch POM for %s:%s:%s".formatted(dependency.getGroupId(), dependency.getArtifactId(), dependency.getVersion()), e);
                        return new DependencyPomResponsePair(dependency, PomResponse.empty());
                    }
                })
                .collect(Collectors.toMap(DependencyPomResponsePair::dependency, DependencyPomResponsePair::pomResponse));



        for (Dependency dependency : pomResponses.keySet()) {
            final PomResponse pomResponse = pomResponses.get(dependency);
            final String projectUrl = pomResponse.url();
            final String projectScmUrl = pomResponse.scmUrl();

            // First try to get the Github owner and repo from the url otherwise try to get it from the SCM url
            var guess = projectUrl != null ? githubGuesser.guess(projectUrl) : null;
            if (guess == null && projectScmUrl != null) {
                guess = githubGuesser.guess(projectScmUrl);
            }

            if (guess == null) {
                getLog().warn("Could not guess Github owner and repo for %s".formatted(dependency.getManagementKey()));
                continue;
            }

            Collected collected;
            try {
                collected = githubCollector.collect(guess.owner(), guess.repo()).get(30L, TimeUnit.SECONDS);
            } catch (ExecutionException | InterruptedException | TimeoutException e) {
                throw new MojoFailureException("Failed to collect Github data for %s".formatted(dependency.getManagementKey()), e);
            }

            double score = maintenanceEvaluator.evaluateCommitsFrequency(collected);
            getLog().info("Maintenance score for %s: %f".formatted(dependency.getManagementKey(), score));
        }
    }

    private record DependencyPomResponsePair(Dependency dependency, PomResponse pomResponse) {
    }
}
