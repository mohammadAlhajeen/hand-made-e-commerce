package com.hand.demo.controller;

import java.util.List;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hand.demo.model.dto.AddToCartRequest;
import com.hand.demo.model.dto.CartViewDTO;
import com.hand.demo.model.dto.CreateShipmentRequest;
import com.hand.demo.model.dto.PayDepositRequest;
import com.hand.demo.model.dto.UpdateCartItemRequest;
import com.hand.demo.model.entity.Cart;
import com.hand.demo.model.entity.Order;
import com.hand.demo.model.entity.Shipment;
import com.hand.demo.service.CartService;
import com.hand.demo.service.OrderService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Validated
public class MultiCartOrderController {

    private final CartService cartService;
    private final OrderService orderService;

    // === Carts ===
    @GetMapping("/carts")
    public List<Cart> listCarts(@RequestParam Long customerId) {
        return cartService.listOpenCarts(customerId);
    }

    @GetMapping("/carts/view")
    public List<CartViewDTO> listCartsAsDTO(@RequestParam Long customerId) {
        return cartService.listOpenCartsAsDTO(customerId);
    }

    @GetMapping("/carts/{cartId}/view")
    public CartViewDTO getCartAsDTO(@PathVariable Long cartId) {
        return cartService.getCartAsDTO(cartId);
    }

    @PostMapping("/carts/items")
    public Cart addItem(@RequestBody @Valid AddToCartRequest req) {
        return cartService.addItem(req); // الشركة تُستنتج من المنتج
    }

    @PutMapping("/carts/items")
    public Cart updateItem(@RequestBody @Valid UpdateCartItemRequest req) {
        return cartService.updateItem(req);
    }

    @DeleteMapping("/carts/{cartId}/items/{itemId}")
    public Cart removeItem(@PathVariable Long cartId, @PathVariable Long itemId) {
        return cartService.removeItem(cartId, itemId);
    }

    // === Orders ===
    @PostMapping("/orders/checkout/{cartId}")
    public Order checkout(@PathVariable Long cartId) {
        return orderService.checkout(cartId);
    }
/* 
    /** تأكيد التاجر (مع خيار السماح بالباك أوردر) 
    @PostMapping("/orders/{orderNumber}/merchant/confirm")
    public Order confirm(@PathVariable String orderNumber,
                         @RequestParam(defaultValue = "true") boolean allowBackorder) {
        return orderService.merchantConfirm(orderNumber, allowBackorder);
    }
*/
    /** دفع العربون للطلب */
    @PostMapping("/orders/pay-deposit")
    public Order payDeposit(@RequestBody @Valid PayDepositRequest request) {
        return orderService.payDeposit(request);
    }

    // === Shipments ===
    @PostMapping("/orders/shipments")
    public Shipment createShipment(@RequestBody @Valid CreateShipmentRequest req) {
        return orderService.createShipment(req);
    }
}
