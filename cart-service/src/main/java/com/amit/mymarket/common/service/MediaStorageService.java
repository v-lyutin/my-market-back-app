package com.amit.mymarket.common.service;

import com.amit.mymarket.common.service.util.PathSpecification;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.multipart.MultipartFile;
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
