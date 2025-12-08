package com.amit.storage.service;

public interface MediaUrlResolver {

    /**
     * Builds a public URL for the given key, if direct linking is needed.
     * Implementations may return the key as-is when a CDN/static prefix is applied elsewhere.
     */
    String buildPublicUrl(String key);

}
