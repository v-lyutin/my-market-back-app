package com.amit.storage.strategy;

import com.amit.storage.util.PathSpecification;

public interface KeyNamingStrategy {

    String buildKey(PathSpecification pathSpecification, String mimeType);

}
