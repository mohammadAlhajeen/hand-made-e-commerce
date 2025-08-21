package com.hand.demo.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hand.demo.model.Dtos.image_dtos.CreateImageDto;
import com.hand.demo.model.entity.MediaItem;
import com.hand.demo.model.entity.Product;
import com.hand.demo.model.entity.ProductImage;
import com.hand.demo.repository.MediaRepository;
import com.hand.demo.repository.ProductImageRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductImageAssignService {

    private final ProductImageRepository productImageRepo;
    private final MediaRepository mediaRepository;

    private void checkMainImage(List<CreateImageDto> dto, Product product) {
        if (dto != null) {
            List<ProductImage> newImages = new java.util.ArrayList<>();
            boolean mainPicked = false;
            if (product.getImages() != null) {
                product.getImages().clear();
            }
            for (CreateImageDto imgDto : dto) {
                MediaItem item = mediaRepository.findById(imgDto.imageId())
                        .orElseThrow(() -> new IllegalArgumentException("Invalid image ID: " + imgDto.imageId()));
                boolean isMain = imgDto.main() && !mainPicked;
                if (isMain) {
                    mainPicked = true;
                }

                newImages.add(new ProductImage(product, item, isMain, imgDto.sortOrder()));
            }
            // ensure single main
            if (!mainPicked && !newImages.isEmpty()) {
                newImages.get(0).setMain(true);
            }
            if (product.getImages() != null) {
                product.getImages().clear();

                product.getImages().addAll(newImages);
                return;
            }

            product.setImages(newImages);

        }
    }

    public List<MediaItem> getMediaItemsByIds(List<UUID> imgIds) {
        if (imgIds == null || imgIds.isEmpty()) {
            return List.of();
        }
        var medItm = mediaRepository.findAllById(imgIds);

        return medItm;
    }

    @Transactional
    public void 
    assignImagesToProduct(Product product, List<CreateImageDto> imageDtos) {
        // التأكد من أن المنتج له ID (تم حفظه مسبقاً)
        if (product.getId() == null) {
            throw new IllegalStateException("Product must be saved before assigning images");
        }

        checkMainImage(imageDtos, product);

        // حفظ الصور إذا كانت موجودة
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            productImageRepo.saveAll(product.getImages());
        }
    }

}
