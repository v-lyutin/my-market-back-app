package com.amit.mymarket.item.usecase;

import com.amit.mymarket.item.api.dto.CreateItemForm;
import com.amit.mymarket.item.api.dto.ItemView;
import com.amit.mymarket.item.api.dto.UpdateItemForm;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Mono;

public interface ItemManagementUseCase {

    Mono<ItemView> createItemAndOptionallyUploadImage(CreateItemForm createItemForm, FilePart file);

    Mono<Void> replaceItemImage(long itemId, FilePart file);

    Mono<Void> updateItemAttributes(long itemId, UpdateItemForm updateItemForm);

    Mono<Void> deleteItem(long itemId);

    Mono<ItemView> getItemById(long itemId);

}
