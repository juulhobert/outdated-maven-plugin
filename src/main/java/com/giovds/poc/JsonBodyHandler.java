package com.giovds.poc;

import com.fasterxml.jackson.jr.ob.JSON;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class JsonBodyHandler<R> implements HttpResponse.BodyHandler<R> {
    private final Class<R> resultClass;

    public JsonBodyHandler(Class<R> resultClass) {
        this.resultClass = resultClass;
    }

    @Override
    public HttpResponse.BodySubscriber<R> apply(HttpResponse.ResponseInfo responseInfo) {
        // Print rate limit information
        if (responseInfo.headers().firstValue("X-RateLimit-Remaining").isPresent()) {
            System.out.println("Rate limit: " + responseInfo.headers().firstValue("X-RateLimit-Remaining").get());
        }


        if (responseInfo.headers().firstValue("X-RateLimit-Remaining").isPresent() && Integer.parseInt(responseInfo.headers().firstValue("X-RateLimit-Remaining").get()) == 0) {
            return HttpResponse.BodySubscribers.replacing(null);
        }

        return asJSON(resultClass);
    }

    public static <R> HttpResponse.BodySubscriber<R> asJSON(Class<R> targetType) {
        HttpResponse.BodySubscriber<String> upstream = HttpResponse.BodySubscribers.ofString(StandardCharsets.UTF_8);
        return HttpResponse.BodySubscribers.mapping(
                upstream,
                (String body) -> {
                    try {
                        return JSON.std.beanFrom(targetType, body);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }
}