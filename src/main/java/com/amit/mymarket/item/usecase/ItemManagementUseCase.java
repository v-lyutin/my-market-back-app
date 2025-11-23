package com.amit.mymarket.item.usecase;

import com.amit.mymarket.item.api.dto.CreateItemForm;
import com.amit.mymarket.item.api.dto.ItemView;
import com.amit.mymarket.item.api.dto.UpdateItemForm;
import org.springframework.web.multipart.MultipartFile;

public interface ItemManagementUseCase {

    ItemView createItemAndOptionallyUploadImage(CreateItemForm createItemForm, MultipartFile file);

    void replacePrimaryItemImage(long itemId, MultipartFile file);

    void updateItemAttributes(long itemId, UpdateItemForm updateItemForm);

    void deleteItemCompletely(long itemId);

    ItemView fetchItemById(long itemId);

}
