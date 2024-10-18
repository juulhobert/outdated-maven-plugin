package com.giovds;

import com.giovds.dto.PomResponse;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import java.util.List;

@Mojo(name = "score",
        defaultPhase = LifecyclePhase.TEST_COMPILE,
        requiresOnline = true,
        requiresDependencyResolution = ResolutionScope.TEST)
public class ScoreMojo extends AbstractMojo {

    private final PomClientInterface client;

    @Parameter(readonly = true, required = true, defaultValue = "${project}")
    private MavenProject project;

    /**
     * Required for initialization by Maven
     */
    public ScoreMojo() {
        this(new PomClient());
    }

    public ScoreMojo(final PomClientInterface client) {
        this.client = client;
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        final List<Dependency> dependencies = project.getDependencies();

        if (dependencies.isEmpty()) {
            // When building a POM without any dependencies there will be nothing to query.
            return;
        }

        final List<PomResponse> pomResponses = dependencies.stream()
                .map(dependency -> {
                    try {
                        return client.getPom(dependency.getGroupId(), dependency.getArtifactId(), dependency.getVersion());
                    } catch (Exception e) {
                        getLog().error("Failed to fetch POM for %s:%s:%s".formatted(dependency.getGroupId(), dependency.getArtifactId(), dependency.getVersion()), e);
                        return null;
                    }
                })
                .toList();

        for (PomResponse pomResponse : pomResponses) {
            getLog().info("URL: %s".formatted(pomResponse.url()));
        }
    }
}
