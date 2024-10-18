package com.giovds;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.giovds.dto.PomResponse;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class PomClient implements PomClientInterface {

    private final String basePath;
    private final String pomPathTemplate;

    private final HttpClient client = HttpClient.newBuilder()
//            .version(HttpClient.Version.HTTP_2)
            .build();

    public PomClient() {
        this("https://repo1.maven.org", "/maven2/%s/%s/%s/%s-%s.pom");
    }

    public PomClient(String basePath, String pomPathTemplate) {
        this.basePath = basePath;
        this.pomPathTemplate = pomPathTemplate;
    }

    public PomResponse getPom(String group, String artifact, String version) throws IOException, InterruptedException {
        final String path = String.format(pomPathTemplate, group.replace(".", "/"), artifact, version, artifact, version);
        final HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(basePath + path))
                .build();

        System.out.println("Fetching POM from: " + request.uri());

        return client.send(request, new PomResponseBodyHandler()).body();
    }

    private class PomResponseBodyHandler implements HttpResponse.BodyHandler<PomResponse> {

        @Override
        public HttpResponse.BodySubscriber<PomResponse> apply(final HttpResponse.ResponseInfo responseInfo) {
            int statusCode = responseInfo.statusCode();

            if (statusCode < 200 || statusCode >= 300) {
                return HttpResponse.BodySubscribers.mapping(HttpResponse.BodySubscribers.ofString(StandardCharsets.UTF_8), s -> {
                    throw new RuntimeException("Search failed: status: %d body: %s".formatted(responseInfo.statusCode(), s));
                });
            }

            HttpResponse.BodySubscriber<InputStream> stream = HttpResponse.BodySubscribers.ofInputStream();

            return HttpResponse.BodySubscribers.mapping(stream, this::toPomResponse);
        }

        private PomResponse toPomResponse(final InputStream inputStream) {
            try (final InputStream input = inputStream) {
                var blaat = new String(input.readAllBytes());
                System.out.println(blaat);

                final XmlMapper xmlMapper = new XmlMapper();

                return xmlMapper.readValue(blaat, PomResponse.class);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
