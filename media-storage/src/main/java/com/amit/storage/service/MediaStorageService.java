package com.amit.storage.service;

import com.amit.storage.util.PathSpecification;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Mono;

public interface MediaStorageService {

    /**
     * Stores the given file and returns the storage key.
     * Validation (type/size) is handled by the implementation.
     */
    Mono<String> saveMediaFile(FilePart file, PathSpecification pathSpecification);

    /**
     * Deletes an object by its storage key.
     * Implementations may choose to ignore missing objects.
     */
    Mono<Void> deleteMediaFile(String key);

}
