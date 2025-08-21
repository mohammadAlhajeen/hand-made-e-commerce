package com.hand.demo.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hand.demo.model.Dtos.product_dtos.CreateAttributeDTO;
import com.hand.demo.model.Dtos.product_dtos.CreateInStockProductDto;
import com.hand.demo.model.Dtos.product_dtos.InStockProductForCompanyV1;
import com.hand.demo.model.Dtos.product_dtos.UpdateInStockProductDto;
import com.hand.demo.model.entity.Attribute;
import com.hand.demo.model.entity.AttributeValue;
import com.hand.demo.model.entity.AttributeValueImage;
import com.hand.demo.model.entity.Category;
import com.hand.demo.model.entity.Company;
import com.hand.demo.model.entity.InStockProduct;
import com.hand.demo.model.entity.Product;
import com.hand.demo.model.entity.Tag;
import com.hand.demo.repository.CategoryRepository;
import com.hand.demo.repository.InStockProductRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class InStockProductService {

    private final TagService tagService;

    private final InStockProductRepository productRepo;
    private final CategoryRepository categoryRepo;
    private final ProductImageAssignService mediaService;

    // ##############################
    // ######## Create Product ######
    // ##############################
    @Transactional
    public InStockProduct createInStockProduct(CreateInStockProductDto request, Company company) {
        InStockProduct product = request.DtoToProduct();

        // إعداد العلاقات قبل الحفظ
        if (request.getCategoryIds() != null) {
            List<Category> categories = categoryRepo.findAllById(
                    request.getCategoryIds());
            product.setCategories(categories);
        }

        List<Tag> tags = tagService.getOrCreateTags(request.getTagNames());
        product.setTags(tags);
        product.setCompany(company);

        // حفظ المنتج أولاً للحصول على ID
        InStockProduct savedProduct = productRepo.save(product);

        // الآن يمكن إضافة الصور بأمان
        if (request.getImages() != null) {
            mediaService.assignImagesToProduct(savedProduct, request.getImages());
        }
        if (request.getAttributes() != null) {
            toAttributes(request.getAttributes(), savedProduct);
        }

        savedProduct = productRepo.save(savedProduct);
        return savedProduct;
    }

    public InStockProductForCompanyV1 createInStockProductDto(CreateInStockProductDto request, Company company) {
        InStockProduct product = createInStockProduct(request, company);
        return InStockProductForCompanyV1.fromProduct(product);
    }

    // ##############################
    // ######### Update Product #####
    // ##############################
    public InStockProduct getCompanyProductHelper(Long productId, Long companyId) {
        InStockProduct product = productRepo.findByIdAndCompanyId(productId, companyId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with ID: " + productId));
        return product;
    }

    @Transactional
    public InStockProductForCompanyV1 updateProduct(UpdateInStockProductDto dto, Long productId, Long companyId) {
        if (dto == null) {
            throw new IllegalArgumentException("update dto must not be null");
        }

        InStockProduct p = getCompanyProductHelper(productId, companyId);

        dto.updateProduct(p);

        if (dto.getCategoryIds() != null) {
            List<Long> ids = dto.getCategoryIds();
            p.getCategories().clear();
            List<Category> cats = categoryRepo.findAllById(ids);
            p.setCategories(new ArrayList<>(cats));
        }

        if (dto.getTagNames() != null) {
            p.getTags().clear();
            if (!dto.getTagNames().isEmpty()) {
                List<Tag> tags = tagService.getOrCreateTags(dto.getTagNames());
                p.setTags(tags);
            }
        }
        if (dto.getImages() != null) {
            if (p.getImages() != null) {
                p.getImages().clear();
            }
            if (!dto.getImages().isEmpty()) {
                mediaService.assignImagesToProduct(p, dto.getImages());
            }
        }
        if (dto.getAttributes() != null) {
            p.getAttributes().clear();
            if (!dto.getAttributes().isEmpty()) {
                toAttributes(dto.getAttributes(), p);
            }
        }
        InStockProduct saved = productRepo.save(p);
        return InStockProductForCompanyV1.fromProduct(saved);
    }

    // ###########################
    // #### Attributes Helper ####
    // ###########################
    public List<Attribute> toAttributes(List<CreateAttributeDTO> request, Product product) {
        if (request == null) {
            return new ArrayList<>();
        }
        List<Attribute> att = new ArrayList<>();
        for (CreateAttributeDTO dto : request) {
            att.add(mapToAttribute(dto, product));
        }
        product.setAttributes(att);
        return att;
    }

    private Attribute mapToAttribute(CreateAttributeDTO dto, Product product) {
        Attribute attribute = new Attribute();
        attribute.setName(dto.getName());
        attribute.setType(
                dto.getType() != null ? dto.getType() : Attribute.AttributeType.TEXT);
        attribute.setIsRequired(dto.getIsRequired());
        attribute.setProduct(product);
        List<AttributeValue> values = new ArrayList<>();
        if (dto.getValues() != null) {
            for (CreateAttributeDTO.AttributeValueDTO valDTO : dto.getValues()) {
                values.add(mapToAttributeValue(valDTO, attribute));
            }
        }

        attribute.setAttributeValues(values);
        return attribute;
    }

    private AttributeValue mapToAttributeValue(CreateAttributeDTO.AttributeValueDTO valDTO, Attribute attribute) {
        AttributeValue val = new AttributeValue();
        val.setValue(valDTO.getValue());
        val.setAttribute(attribute);

        List<AttributeValueImage> images = new ArrayList<>();
        var mediaItems = mediaService.getMediaItemsByIds(valDTO.getImage());
        if (mediaItems != null) {
            for (var mediaItem : mediaItems) {
                AttributeValueImage attValImg = new AttributeValueImage();
                attValImg.setMediaItem(mediaItem);
                attValImg.setAttributeValue(val);
                images.add(attValImg);
            }
        }

        val.setAttributeValueImages(images);
        System.out.println(val.getAttributeValueImages().toString());
        return val;
    }

    // #########################
    // ##### Stock Updater #####
    // #########################
    public record InventorySnapshot(int total, int committed, int available, int backordered) {
    }

    // ===== Helpers =====
    private static int nz(Integer v) {
        return v != null ? v : 0;
    }

    private static int availableOf(int total, int committed, boolean allowBackorder) {
        int raw = total - committed;
        return allowBackorder ? Math.max(raw, 0) : raw; // مع OFF نضمن بعقلنا committed<=total
    }

    private static int backorderedOf(int total, int committed) {
        return Math.max(committed - total, 0);
    }

    // ===== RESTOCK: زيادة الإجمالي (دخول بضاعة) =====
    @Transactional
    public InventorySnapshot restock(Long productId, Long companyId, int qty) {
        if (qty <= 0)
            throw new IllegalArgumentException("qty must be > 0");

        var inv = productRepo.lockByIdAndCompanyId(productId, companyId)
                .orElseThrow(() -> new EntityNotFoundException("Inventory not found"));

        int total = nz(inv.getTotalQuantity()) + qty;
        int committed = nz(inv.getQuantityCommitted());
        boolean back = Boolean.TRUE.equals(inv.isAllowBackorder());

        inv.setTotalQuantity(total);
        productRepo.save(inv);

        return new InventorySnapshot(total, committed,
                availableOf(total, committed, back),
                backorderedOf(total, committed));
    }

    // ===== RESERVE: حجز للطلب الأونلاين =====
    @Transactional
    public InventorySnapshot reserve(Long productId, Long companyId, int qty) {
        if (qty <= 0)
            throw new IllegalArgumentException("qty must be > 0");

        var inv = productRepo.lockByIdAndCompanyId(productId, companyId)
                .orElseThrow(() -> new EntityNotFoundException("Inventory not found"));

        // امنع الطلبات الجديدة إذا المنتج غير فعّال
        if (Boolean.FALSE.equals(inv.getIsActive())) {
            throw new IllegalStateException("Product is inactive; ordering is paused.");
        }

        boolean back = Boolean.TRUE.equals(inv.isAllowBackorder());
        int total = inv.getTotalQuantity() == null ? 0 : inv.getTotalQuantity();
        int committed = inv.getQuantityCommitted() == null ? 0 : inv.getQuantityCommitted();
        int available = Math.max(total - committed, 0);

        if (!back && qty > available)
            throw new IllegalStateException("Not enough stock to reserve");

        inv.setQuantityCommitted(committed + qty);
        committed += qty;
        productRepo.save(inv);

        int shownAvailable = back ? Math.max(total - committed, 0) : (total - committed);
        int backordered = Math.max(committed - total, 0);
        return new InventorySnapshot(total, committed, shownAvailable, backordered);
    }

    // ===== RELEASE: فكّ الحجز =====
    @Transactional
    public InventorySnapshot release(Long productId, Long companyId, int qty) {
        if (qty <= 0)
            throw new IllegalArgumentException("qty must be > 0");

        var inv = productRepo.lockByIdAndCompanyId(productId, companyId)
                .orElseThrow(() -> new EntityNotFoundException("Inventory not found"));

        int total = nz(inv.getTotalQuantity());
        int committed = nz(inv.getQuantityCommitted());
        boolean back = Boolean.TRUE.equals(inv.isAllowBackorder());

        if (qty > committed)
            throw new IllegalStateException("Release exceeds committed reservations");

        inv.setQuantityCommitted(committed - qty);
        committed -= qty;

        productRepo.save(inv);
        return new InventorySnapshot(total, committed,
                availableOf(total, committed, back),
                backorderedOf(total, committed));
    }

    // ===== SHIP: شحن الطلب (يُنقص من الإجمالي ومن المحجوز) =====
    @Transactional
    public InventorySnapshot ship(Long productId, Long companyId, int qty) {
        if (qty <= 0)
            throw new IllegalArgumentException("qty must be > 0");

        var inv = productRepo.lockByIdAndCompanyId(productId, companyId)
                .orElseThrow(() -> new EntityNotFoundException("Inventory not found"));

        int total = nz(inv.getTotalQuantity());
        int committed = nz(inv.getQuantityCommitted());
        boolean back = Boolean.TRUE.equals(inv.isAllowBackorder());

        // لازم يكون في بضاعة فعلية على الرف + تكون محجوزة
        if (qty > committed)
            throw new IllegalStateException("Cannot ship more than committed");
        if (qty > total)
            throw new IllegalStateException("Cannot ship more than on-hand total");

        inv.setQuantityCommitted(committed - qty);
        inv.setTotalQuantity(total - qty);

        total -= qty;
        committed -= qty;

        productRepo.save(inv);
        return new InventorySnapshot(total, committed,
                availableOf(total, committed, back),
                backorderedOf(total, committed));
    }

    // ===== SET TOTAL: ضبط الإجمالي مباشرة =====
    @Transactional
    public InventorySnapshot setTotal(Long productId, Long companyId, int newTotal) {
        if (newTotal < 0)
            throw new IllegalArgumentException("newTotal must be >= 0");

        var inv = productRepo.lockByIdAndCompanyId(productId, companyId)
                .orElseThrow(() -> new EntityNotFoundException("Inventory not found"));

        int committed = nz(inv.getQuantityCommitted());
        boolean back = Boolean.TRUE.equals(inv.isAllowBackorder());

        if (!back && newTotal < committed) {
            throw new IllegalStateException("newTotal cannot be less than committed when backorders are disabled");
        }

        inv.setTotalQuantity(newTotal);
        productRepo.save(inv);

        return new InventorySnapshot(newTotal, committed,
                availableOf(newTotal, committed, back),
                backorderedOf(newTotal, committed));
    }

    // (اختياري) تبديل وضع backorder بأمان
    @Transactional
    public ToggleBackorderResult setAllowBackorder(Long productId, Long companyId,
            boolean allow,
            boolean autoDeactivateOnBacklog) {
        var inv = productRepo.lockByIdAndCompanyId(productId, companyId)
                .orElseThrow(() -> new EntityNotFoundException("Inventory not found"));

        int total = inv.getTotalQuantity() != null ? inv.getTotalQuantity() : 0;
        int committed = inv.getQuantityCommitted() != null ? inv.getQuantityCommitted() : 0;
        int backordered = Math.max(committed - total, 0);

        if (allow) {
            inv.setAllowBackorder(true);
            // اختياري: ما بنغيّر isActive هون؛ خليه بيد الأدمن
            productRepo.save(inv);
            return new ToggleBackorderResult(true, Boolean.FALSE.equals(inv.getIsActive()),
                    total, committed, backordered);
        }

        // إطفاء backorder
        if (backordered > 0) {
            if (autoDeactivateOnBacklog) {
                inv.setAllowBackorder(false);
                inv.setIsActive(false); // ← المطلوب: إخفاء المنتج وإيقاف الطلبات الجديدة
                productRepo.save(inv);
                return new ToggleBackorderResult(false, true, total, committed, backordered);
            } else {
                throw new IllegalStateException(
                        "Cannot disable backorders while committed > total (restock or auto-deactivate).");
            }
        } else {
            inv.setAllowBackorder(false);
            productRepo.save(inv);
            return new ToggleBackorderResult(false, Boolean.FALSE.equals(inv.getIsActive()),
                    total, committed, 0);
        }
    }

    public record ToggleBackorderResult(boolean backorderEnabled,
            boolean deactivated,
            int total, int committed, int backordered) {
    }

}
