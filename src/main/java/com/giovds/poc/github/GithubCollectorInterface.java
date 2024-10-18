package com.giovds.poc.github;

import com.giovds.poc.github.model.internal.Collected;

import java.util.concurrent.ExecutionException;

public interface GithubCollectorInterface {
    Collected collect(String owner, String repo) throws ExecutionException, InterruptedException;
}
