package com.hand.demo.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hand.demo.model.dto.AddToCartRequest;
import com.hand.demo.model.dto.CartViewDTO;
import com.hand.demo.model.dto.ExtractPrice;
import com.hand.demo.model.dto.UpdateCartItemRequest;
import com.hand.demo.model.entity.AppUser;
import com.hand.demo.model.entity.Attribute;
import com.hand.demo.model.entity.AttributeValue;
import com.hand.demo.model.entity.Cart;
import com.hand.demo.model.entity.CartItem;
import com.hand.demo.model.entity.CartItemSelection;
import com.hand.demo.model.entity.Company;
import com.hand.demo.model.entity.InStockProduct;
import com.hand.demo.model.entity.PreOrderProduct;
import com.hand.demo.model.entity.Product;
import com.hand.demo.model.enums.OrderItemType;
import com.hand.demo.repository.AttributeRepository;
import com.hand.demo.repository.AttributeValueRepository;
import com.hand.demo.repository.CartRepository;
import com.hand.demo.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class CartService {

    private final CartRepository cartRepo;
    private final ProductRepository productRepo;
    private final AttributeRepository attributeRepo;
    private final AttributeValueRepository valueRepo;

    @Transactional(readOnly = true)
    public List<Cart> listOpenCarts(Long customerId) {
        return cartRepo.findByCustomerIdAndClosedFalse(customerId);
    }

    @Transactional(readOnly = true)
    public List<CartViewDTO> listOpenCartsAsDTO(Long customerId) {
        List<Cart> carts = cartRepo.findByCustomerIdAndClosedFalse(customerId);
        return carts.stream().map(this::toCartViewDTO).toList();
    }

    @Transactional(readOnly = true)
    public CartViewDTO getCartAsDTO(Long cartId) {
        Cart cart = cartRepo.findById(cartId).orElseThrow();
        return toCartViewDTO(cart);
    }

    @Transactional
    public Cart addItem(AddToCartRequest req) {
        if (req.quantity() <= 0)
            throw new IllegalArgumentException("Quantity must be >= 1");

        Product p = productRepo.getReferenceById(req.productId());
        Long companyId = p.getCompany().getId();

        Cart cart = cartRepo.findByCustomerIdAndCompanyIdAndClosedFalse(req.customerId(), companyId)
                .orElseGet(() -> {
                    Cart c = new Cart();
                    var cu = new AppUser();
                    cu.setId(req.customerId());
                    c.setCustomer(cu);
                    c.setCompany(p.getCompany()); // الشركة من المنتج
                    return cartRepo.save(c);
                });

        CartItem newItem = buildItem(p, req.quantity(), req.selections());

        Optional<CartItem> same = cart.getItems().stream()
                .filter(i -> i.getProduct().getId().equals(p.getId()) && i.sameSelections(newItem))
                .findFirst();

        if (same.isPresent()) {
            CartItem it = same.get();
            int newQty = it.getQuantity() + req.quantity();
            validateStockIfNeeded(p, newQty);
            it.setQuantity(newQty);
        } else {
            validateStockIfNeeded(p, req.quantity());
            newItem.setCart(cart);
            cart.getItems().add(newItem);
        }
        return cartRepo.save(cart);
    }

    @Transactional
    public Cart updateItem(UpdateCartItemRequest req) {
        if (req.quantity() <= 0)
            throw new IllegalArgumentException("Quantity must be >= 1");

        Cart cart = cartRepo.findById(req.cartId()).orElseThrow();
        CartItem it = cart.getItems().stream()
                .filter(i -> i.getId().equals(req.cartItemId()))
                .findFirst().orElseThrow();

        Product p = it.getProduct();

        if (req.selections() != null) {
            rebuildSelections(it, p, req.selections());

        }

        validateStockIfNeeded(p, req.quantity());
        it.setQuantity(req.quantity());

        // دمج مع سطر آخر صار مطابق
        Optional<CartItem> merge = cart.getItems().stream()
                .filter(i -> !i.getId().equals(it.getId()))
                .filter(i -> i.getProduct().getId().equals(p.getId()) && i.sameSelections(it))
                .findFirst();
        if (merge.isPresent()) {
            CartItem o = merge.get();
            it.setQuantity(it.getQuantity() + o.getQuantity());
            cart.getItems().remove(o);
        }
        return cartRepo.save(cart);
    }

    @Transactional
    public Cart removeItem(Long cartId, Long cartItemId) {
        Cart cart = cartRepo.findById(cartId).orElseThrow();
        cart.getItems().removeIf(i -> i.getId().equals(cartItemId));
        return cartRepo.save(cart);
    }

    // ===== Customer Cart Methods =====
    
    @Transactional(readOnly = true)
    public Cart getCustomerCart(Long customerId, Long companyId) {
        return cartRepo.findByCustomerIdAndCompanyIdAndClosedFalse(customerId, companyId)
                .orElse(null);
    }
    
    @Transactional(readOnly = true)
    public CartViewDTO getCustomerCartView(Long customerId, Long companyId) {
        return listOpenCartsAsDTO(customerId).stream()
                .filter(dto -> dto.companyId().equals(companyId))
                .findFirst()
                .orElse(null);
    }
    
    public Cart addItemToCustomerCart(Long customerId, Long companyId, AddToCartRequest request) {
        // Validate that the request has the correct customer ID
        if (!request.customerId().equals(customerId)) {
            throw new IllegalArgumentException("Customer ID mismatch");
        }
        
        // Use existing addItem logic which handles cart creation
        return addItem(request);
    }
    
    @Transactional
    public Cart updateCustomerCartItem(Long customerId, Long companyId, UpdateCartItemRequest request) {
        Cart cart = cartRepo.findByCustomerIdAndCompanyIdAndClosedFalse(customerId, companyId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));
        
        return updateItem(new UpdateCartItemRequest(cart.getId(), request.cartItemId(), 
                request.quantity(), request.selections()));
    }
    
    @Transactional
    public Cart removeItemFromCustomerCart(Long customerId, Long companyId, Long cartItemId) {
        Cart cart = cartRepo.findByCustomerIdAndCompanyIdAndClosedFalse(customerId, companyId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));
        
        return removeItem(cart.getId(), cartItemId);
    }
    
    @Transactional
    public void clearCustomerCart(Long customerId, Long companyId) {
        Cart cart = cartRepo.findByCustomerIdAndCompanyIdAndClosedFalse(customerId, companyId)
                .orElse(null);
        if (cart != null) {
            cart.getItems().clear();
            cartRepo.save(cart);
        }
    }
    
    private Cart createNewCart(Long customerId, Long companyId) {
        Cart cart = new Cart();
        AppUser customer = new AppUser();
        customer.setId(customerId);
        cart.setCustomer(customer);
        
        Company company = new Company();
        company.setId(companyId);
        cart.setCompany(company);
        
        cart.setClosed(false);
        return cartRepo.save(cart);
    }

    // ===== Helpers =====

    private CartItem buildItem(Product p, int qty, List<AddToCartRequest.Selection> selsReq) {
        CartItem item = new CartItem();


        item.setQuantity(qty);

        if (p instanceof InStockProduct)
            item.setType(OrderItemType.STOCK);
        else if (p instanceof PreOrderProduct prep) {
            item.setType(OrderItemType.PRE_ORDER);
            item.setPreparationDays(prep.getPreparationDays());
            // تعيين مبلغ العربون للمنتجات المسبقة الطلب

        } else
            throw new IllegalStateException("Unknown product type");

        List<CartItemSelection> sels = buildSelectionsForProduct(p, selsReq);
        item.setSelections(sels);
        return item;
    }

    private List<CartItemSelection> buildSelectionsForProduct(Product p, List<AddToCartRequest.Selection> reqSels) {
        List<Attribute> attrs = attributeRepo.findByProductIdOrderBySortOrderAsc(p.getId());
        Map<Long, Attribute> attrById = attrs.stream().collect(Collectors.toMap(Attribute::getId, Function.identity()));
        var required = attrs.stream().filter(a -> Boolean.TRUE.equals(a.getIsRequired())).toList();

        @SuppressWarnings("unused")
        Map<Long, Long> provided = (reqSels == null ? List.<AddToCartRequest.Selection>of() : reqSels)
                .stream().collect(Collectors.toMap(AddToCartRequest.Selection::attributeId,
                        AddToCartRequest.Selection::valueId, (existing, duplicate) -> existing));

        for (Attribute r : required)
            if (!provided.containsKey(r.getId()))
                throw new IllegalArgumentException("Missing required attribute: " + r.getName());

        List<CartItemSelection> out = new ArrayList<>();
        for (var e : provided.entrySet()) {
            Long attId = e.getKey();
            Long valId = e.getValue();
            Attribute att = attrById.get(attId);
            if (att == null)
                throw new IllegalArgumentException("Attribute not found for this product: " + attId);

            AttributeValue val = valueRepo.findById(valId)
                    .orElseThrow(() -> new IllegalArgumentException("Attribute value not found: " + valId));
            if (!val.getAttribute().getId().equals(attId))
                throw new IllegalArgumentException("Value does not belong to attribute");

            CartItemSelection s = new CartItemSelection();
            s.setAttributeId(att.getId());
            s.setAttributeName(att.getName());
            s.setValueId(val.getId());
            s.setValueText(val.getValue());
            out.add(s);
        }
        return out;
    }

    private void rebuildSelections(CartItem item, Product p, List<UpdateCartItemRequest.Selection> reqSels) {
        List<AddToCartRequest.Selection> tmp = (reqSels == null ? List.of()
                : reqSels.stream().map(s -> new AddToCartRequest.Selection(s.attributeId(), s.valueId())).toList());
        List<CartItemSelection> sels = buildSelectionsForProduct(p, tmp);
        item.getSelections().clear();
        for (CartItemSelection s : sels) {
            s.setCartItem(item);
            item.getSelections().add(s);
        }
    }

    private void validateStockIfNeeded(Product p, int qty) {
        if (p instanceof InStockProduct isp) {
            int available = isp.getQuantityAvailable();
            if (!isp.isAllowBackorder() && qty > available)
                throw new IllegalStateException("Not enough stock");
        }
    }

    // ===== DTO Conversion =====

    private CartViewDTO toCartViewDTO(Cart cart) {
        List<CartViewDTO.CartItemViewDTO> itemsDTO = cart.getItems().stream()
                .map(this::toCartItemViewDTO)
                .toList();

        BigDecimal subtotal = itemsDTO.stream()
                .map(CartViewDTO.CartItemViewDTO::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalDepositRequired = itemsDTO.stream()
                .map(CartViewDTO.CartItemViewDTO::totalDeposit)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalRemainingAmount = itemsDTO.stream()
                .map(CartViewDTO.CartItemViewDTO::remainingAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartViewDTO(
                cart.getId(),
                cart.getCustomer().getId(),
                cart.getCustomer().getName(),
                cart.getCompany().getId(),
                cart.getCompany().getName(),
                itemsDTO,
                subtotal,
                totalDepositRequired,
                totalRemainingAmount,
                cart.getCreatedAt(),
                cart.getUpdatedAt());
    }

    private CartViewDTO.CartItemViewDTO toCartItemViewDTO(CartItem item) {
        List<CartViewDTO.SelectionViewDTO> selectionsDTO = item.getSelections().stream()
                .map(this::toSelectionViewDTO)
                .toList();
        var p = item.getProduct();

        var sum = selectionsDTO.stream()
                .map(s -> s.extraPrice())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        var extPrices = new ExtractPrice(p, sum, item.getQuantity());
        return new CartViewDTO.CartItemViewDTO(
                item.getId(),
                item.getProduct().getId(),
                p.getName(),
                item.getType().toString(),
                extPrices.getUnitPriceBase(),
                sum,
                extPrices.getUnitPriceTotal(),
                extPrices.getDepositAmount(),
                extPrices.getTotalDeposit(),
                item.getQuantity(),
                extPrices.getLineTotal(),
                extPrices.getRemainingAmount(),
                item.getPreparationDays(),
                selectionsDTO);
    }

    private CartViewDTO.SelectionViewDTO toSelectionViewDTO(CartItemSelection selection) {
        var extraPrice = valueRepo.findExtraPriceById(selection.getValueId());
        return new CartViewDTO.SelectionViewDTO(
                selection.getAttributeId(),
                selection.getAttributeName(),
                selection.getValueId(),
                selection.getValueText(),
                extraPrice);
    }
}
