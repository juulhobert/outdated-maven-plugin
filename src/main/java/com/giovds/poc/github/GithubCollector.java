package com.giovds.poc.github;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.giovds.poc.JsonBodyHandler;
import com.giovds.poc.github.model.extenal.CommitActivity;
import com.giovds.poc.github.model.extenal.ContributorStat;
import com.giovds.poc.github.model.extenal.Repository;
import com.giovds.poc.github.model.internal.*;
import org.apache.maven.plugin.logging.Log;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class GithubCollector implements GithubCollectorInterface {
    private static final ScheduledExecutorService SCHEDULER = Executors.newScheduledThreadPool(1);
    private static final String GITHUB_TOKEN = System.getenv("GITHUB_TOKEN");

    private final String baseUrl;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final Log log;

    public GithubCollector(String baseUrl, HttpClient httpClient, ObjectMapper objectMapper, Log log) {
        this.baseUrl = baseUrl;
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.log = log;

        if (GITHUB_TOKEN == null || GITHUB_TOKEN.isEmpty()) {
            log.warn("No GitHub token provided, rate limits will be enforced. Provide a token by setting the GITHUB_TOKEN environment variable.");
        } else {
            log.info("GitHub token provided");
        }
    }

    @Override
    public Future<Collected> collect(String owner, String repo) throws ExecutionException, InterruptedException {
        var repository = getRepository(owner, repo).get();
        var contributors = getContributors(repository).get();
        var commitActivity = getCommitActivity(repository).get();

        var summary = extractCommits(commitActivity);

        return CompletableFuture.completedFuture(Collected.builder()
                .homepage(repository.getHomepage())
                .starsCount(repository.getStargazersCount())
                .forksCount(repository.getForksCount())
                .subscribersCount(repository.getSubscribersCount())
                .contributors(Arrays.stream(contributors).map(
                        contributor -> Contributor.builder()
                                .username(contributor.getAuthor().getLogin())
                                .commitsCount(contributor.getTotal())
                                .build()
                ).toList().reversed())
                .commits(summary)
                .build());
    }

    private Future<Repository> getRepository(String owner, String repo) {
        return request(String.format("repos/%s/%s", owner, repo), Repository.class);
    }

    private Future<ContributorStat[]> getContributors(Repository repository) {
        return request("%s/stats/contributors".formatted(repository.getUrl()), ContributorStat[].class, true);
    }

    private Future<CommitActivity[]> getCommitActivity(Repository repository) {
        return request("%s/stats/commit_activity".formatted(repository.getUrl()), CommitActivity[].class, true);
    }

    private <R> CompletableFuture<R> request(String path, Class<R> responseType) {
        return request(path, responseType, false);
    }

    private <R> CompletableFuture<R> request(String path, Class<R> responseType, boolean isUri) {
        return sendRequestAsync(path, responseType, isUri).thenApply(res -> {
            if (res.headers().firstValue("X-RateLimit-Remaining").isPresent() && Integer.parseInt(res.headers().firstValue("X-RateLimit-Remaining").get()) == 0) {
                long resetTime = Long.parseLong(res.headers().firstValue("X-RateLimit-Reset").get());
                long delay = Math.max(0, resetTime - System.currentTimeMillis() / 1000);

                log.info("Rate limit exceeded, waiting for %s seconds".formatted(delay));

                CompletableFuture<R> future = new CompletableFuture<>();
                SCHEDULER.schedule(() -> sendRequestAsync(path, responseType).thenAccept(r -> future.complete(r.body())), delay, TimeUnit.SECONDS);
                return future.join();
            }

            return res.body();
        });
    }

    private <R> CompletableFuture<HttpResponse<R>> sendRequestAsync(String pathOrUri, Class<R> responseType) {
        return sendRequestAsync(pathOrUri, responseType, false);
    }

    private <R> CompletableFuture<HttpResponse<R>> sendRequestAsync(String pathOrUri, Class<R> responseType, boolean isUri) {
        String url = isUri ? pathOrUri : String.format("%s/%s", baseUrl, pathOrUri);
        var requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/vnd.github.v3+json")
                .timeout(Duration.ofSeconds(10));

        if (GITHUB_TOKEN != null && !GITHUB_TOKEN.isEmpty()) {
            requestBuilder.header("Authorization", "Bearer %s".formatted(GITHUB_TOKEN));
        }

        var request = requestBuilder.build();
        return httpClient.sendAsync(request, new JsonBodyHandler<>(responseType, objectMapper));
    }

    private static List<RangeSummary> extractCommits(CommitActivity[] commitActivity) {
        List<Point> points = Arrays.stream(commitActivity)
                .map(entry -> Point.builder()
                        .date(Instant.ofEpochSecond(entry.getWeek()).atZone(ZoneOffset.UTC).toOffsetDateTime())
                        .total(entry.getTotal())
                        .build())
                .toList();

        var ranges = pointsToRanges(points, bucketsFromBreakpoints(List.of(7, 30, 90, 180, 365)));

        return ranges.stream()
                .map(range -> {
                    int count = range.getPoints().stream()
                            .mapToInt(Point::getTotal)
                            .sum();

                    return RangeSummary.builder()
                            .start(range.getStart())
                            .end(range.getEnd())
                            .count(count)
                            .build();
                })
                .toList();
    }

    private static List<Range> pointsToRanges(List<Point> points, List<Bucket> buckets) {
        return buckets.stream().map(bucket -> {
            List<Point> filteredPoints = points.stream()
                    .filter(point -> !point.getDate().isBefore(bucket.getStart()) && point.getDate().isBefore(bucket.getEnd()))
                    .collect(Collectors.toList());

            return Range.builder()
                    .start(bucket.getStart())
                    .end(bucket.getEnd())
                    .points(filteredPoints)
                    .build();
        }).collect(Collectors.toList());
    }

    private static List<Bucket> bucketsFromBreakpoints(List<Integer> breakpoints) {
        OffsetDateTime referenceDate = OffsetDateTime.now(ZoneOffset.UTC).toLocalDate().atStartOfDay().atOffset(ZoneOffset.UTC);

        return breakpoints.stream()
                .map(breakpoint -> Bucket.builder()
                        .start(referenceDate.minusDays(breakpoint))
                        .end(referenceDate)
                        .build())
                .collect(Collectors.toList());
    }
}
