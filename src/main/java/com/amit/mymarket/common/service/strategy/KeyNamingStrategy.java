package com.amit.mymarket.common.service.strategy;

import com.amit.mymarket.common.service.util.PathSpecification;

public interface KeyNamingStrategy {

    String buildKey(PathSpecification pathSpecification, String mimeType);

}
