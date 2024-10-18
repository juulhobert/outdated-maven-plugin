package com.giovds.poc.github;

import com.giovds.poc.github.model.internal.Collected;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public interface GithubCollectorInterface {
    Future<Collected> collect(String owner, String repo) throws ExecutionException, InterruptedException;
}
