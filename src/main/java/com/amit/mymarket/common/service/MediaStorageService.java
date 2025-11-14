package com.amit.mymarket.common.service;

import com.amit.mymarket.common.service.util.PathSpecification;
import org.springframework.web.multipart.MultipartFile;

public interface MediaStorageService {

    /**
     * Stores the given file and returns the storage key.
     * Validation (type/size) is handled by the implementation.
     */
    String saveMediaFile(MultipartFile file, PathSpecification pathSpecification);

    /**
     * Deletes an object by its storage key.
     * Implementations may choose to ignore missing objects.
     */
    void deleteMediaFile(String key);

}
