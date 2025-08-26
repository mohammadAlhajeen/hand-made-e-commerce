package com.hand.demo.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hand.demo.model.dto.CreateShipmentRequest;
import com.hand.demo.model.dto.ExtractPrice;
import com.hand.demo.model.dto.PayDepositRequest;
import com.hand.demo.model.entity.Cart;
import com.hand.demo.model.entity.CartItem;
import com.hand.demo.model.entity.CartItemSelection;
import com.hand.demo.model.entity.InStockProduct;
import com.hand.demo.model.entity.Order;
import com.hand.demo.model.entity.OrderItem;
import com.hand.demo.model.entity.OrderItemSelection;
import com.hand.demo.model.entity.Shipment;
import com.hand.demo.model.entity.ShipmentItem;
import com.hand.demo.model.enums.OrderItemType;
import com.hand.demo.model.enums.OrderStatus;
import com.hand.demo.repository.AttributeValueRepository;
import com.hand.demo.repository.CartRepository;
import com.hand.demo.repository.OrderRepository;
import com.hand.demo.repository.ProductRepository;
import com.hand.demo.repository.ShipmentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final CartRepository cartRepo;
    private final OrderRepository orderRepo;
    private final ProductRepository productRepo;
    private final ShipmentRepository shipmentRepo;
    private final AttributeValueRepository valueRepo;
    private final InStockProductService inStockProductService;

    /** Checkout لسلة شركة واحدة (زر "اطلب من هذا المتجر") */
    @Transactional
    public Order checkout(Long cartId) {
        Cart cart = cartRepo.findById(cartId).orElseThrow();
        if (cart.isClosed())
            throw new IllegalStateException("Cart already closed");
        if (cart.getItems().isEmpty())
            throw new IllegalStateException("Cart empty");

        Order order = new Order();
        order.setCustomer(cart.getCustomer());
        order.setCompany(cart.getCompany());
        order.setStatus(OrderStatus.CREATED);

        for (CartItem ci : cart.getItems()) {
            BigDecimal extrasPrice = BigDecimal.ZERO;
            OrderItem oi = new OrderItem();
            var p = ci.getProduct();
            if (p instanceof InStockProduct isp) {
                inStockProductService.reserve(isp.getId(), cart.getCompany().getId(), ci.getQuantity());
                oi.setAllowBackorder(isp.isAllowBackorder());
            }
            var exprice = new ExtractPrice(ci.getProduct(), extrasPrice, ci.getQuantity());
            oi.setOrder(order);
            oi.setProductId(ci.getProduct().getId());
            oi.setProductName(p.getName());
            oi.setType(ci.getType());
            oi.setUnitPriceBase(exprice.getUnitPriceBase());
            oi.setUnitPriceExtra(extrasPrice);
            oi.setDepositAmount(exprice.getDepositAmount());
            oi.setQtyOrdered(ci.getQuantity());

            if (ci.getType() == OrderItemType.PRE_ORDER) {
                oi.setPreparationDays(ci.getPreparationDays());
            }

            // selections
            for (CartItemSelection cs : ci.getSelections()) {
                var extraPrice = valueRepo.findExtraPriceById(cs.getValueId());
                OrderItemSelection os = new OrderItemSelection();
                os.setOrderItem(oi);
                os.setAttributeId(cs.getAttributeId());
                os.setAttributeName(cs.getAttributeName());
                os.setValueId(cs.getValueId());
                os.setValueText(cs.getValueText());
                os.setExtraPrice(extraPrice);
                oi.getSelections().add(os);
                extrasPrice = extrasPrice.add(extraPrice != null ? extraPrice : BigDecimal.ZERO);
            }
            order.addItem(oi);
        }

        order.recomputeTotals();

        cart.setClosed(true);
        cartRepo.save(cart);

        return orderRepo.save(order);
    }

    /**
     * تأكيد التاجر: تخصيص جزئي/كامل حسب التوفر.
     * allowBackorder=true يترك الباقي Backorder، غير ذلك يُلغى النقص (qtyCanceled).
     */
    @Transactional
    public Order merchantConfirm(Order order, boolean allowBackorder) {

        for (OrderItem it : order.getItems()) {
            int need = it.getQtyOrdered() - it.getQtyAllocated() - it.getQtyCanceled();
            if (need <= 0)
                continue;

            if (it.getType() == OrderItemType.STOCK) {
                InStockProduct isp = (InStockProduct) productRepo.getReferenceById(it.getProductId());
                int available = isp.getQuantityAvailable();

                int alloc = Math.min(available, need);
                if (alloc > 0) {
                    it.setQtyAllocated(it.getQtyAllocated() + alloc);
                    int committed = isp.getQuantityCommitted() == null ? 0 : isp.getQuantityCommitted();
                    isp.setQuantityCommitted(committed + alloc);
                }

                int leftover = need - alloc;
                if (leftover > 0 && !allowBackorder && !it.isAllowBackorder()) {
                    it.setQtyCanceled(it.getQtyCanceled() + leftover);
                }
            } else {
                // PRE_ORDER: عادة يُترك للتحضير (Backorder منطقي)
            }
        }

        order.setStatus(OrderStatus.CONFIRMED);
        return orderRepo.save(order);
    }

    /** إنشاء شحنة جزئية من البنود المخصصة ولم تُشحن بعد */
    @Transactional
    public Shipment createShipment(CreateShipmentRequest req) {
        Order order = orderRepo.findByOrderNumber(req.orderNumber()).orElseThrow();
        Shipment sh = new Shipment();
        sh.setOrder(order);

        for (CreateShipmentRequest.Line line : req.items()) {
            OrderItem it = order.getItems().stream()
                    .filter(x -> x.getId().equals(line.orderItemId()))
                    .findFirst().orElseThrow();

            int availableToShip = it.getQtyAllocated() - it.getQtyShipped();
            int shipQty = Math.min(availableToShip, line.qty());
            if (shipQty <= 0)
                continue;

            ShipmentItem si = new ShipmentItem();
            si.setShipment(sh);
            si.setOrderItem(it);
            si.setQty(shipQty);
            sh.getItems().add(si);

            it.setQtyShipped(it.getQtyShipped() + shipQty);
            // ملاحظة: عند الشحن النهائي، لو نموذجك يتطلب خصم من totalQuantity افعل ذلك هناك
        }

        return shipmentRepo.save(sh);
    }

    /** دفع العربون للطلب */
    @Transactional
    public Order payDeposit(PayDepositRequest request) {
        Order order = orderRepo.findByOrderNumber(request.orderNumber()).orElseThrow();

        BigDecimal currentPaid = order.getDepositPaid() != null ? order.getDepositPaid() : BigDecimal.ZERO;
        BigDecimal newTotal = currentPaid.add(request.amount());

        if (newTotal.compareTo(order.getDepositRequired()) > 0) {
            throw new IllegalArgumentException("Amount exceeds required deposit");
        }

        order.setDepositPaid(newTotal);

        // تحديث حالة الطلب إذا تم دفع العربون كاملاً
        if (newTotal.compareTo(order.getDepositRequired()) == 0 && order.getStatus() == OrderStatus.CREATED) {
            order.setStatus(OrderStatus.CONFIRMED);
        }

        return orderRepo.save(order);
    }

    // ===== Customer-specific Order Methods =====

    /**
     * Customer checkout their own cart
     */
    @Transactional
    public Order customerCheckout(Long customerId, Long cartId) {
        Cart cart = cartRepo.findById(cartId).orElseThrow();
        if (!cart.getCustomer().getId().equals(customerId)) {
            throw new IllegalArgumentException("Cart does not belong to customer");
        }
        return checkout(cartId);
    }

    /**
     * Customer pays deposit for their order
     */
    @Transactional
    public Order customerPayDeposit(Long customerId, PayDepositRequest request) {
        Order order = orderRepo.findByOrderNumber(request.orderNumber()).orElseThrow();
        if (!order.getCustomer().getId().equals(customerId)) {
            throw new IllegalArgumentException("Order does not belong to customer");
        }
        return payDeposit(request);
    }

    /**
     * Get customer orders
     */
    public List<Order> getCustomerOrders(Long customerId, OrderStatus status) {
        return orderRepo.findByCustomerIdAndStatusOrderByCreatedAtDesc(customerId, status);
    }

    /**
     * Get customer order by order number
     */
    public Order getCustomerOrder(Long customerId, String orderNumber) {
        Order order = orderRepo.findByOrderNumber(orderNumber).orElseThrow();
        if (!order.getCustomer().getId().equals(customerId)) {
            throw new IllegalArgumentException("Order does not belong to customer");
        }
        return order;
    }

    // ===== Company-specific Order Methods =====

    /**
     * Company confirms order and sets production status
     */
    @Transactional
    public Order companyConfirmOrder(Long companyId, String orderNumber, boolean allowBackorder) {
        Order order = orderRepo.findByOrderNumberAndCompanyId(orderNumber, companyId).orElseThrow(
                () -> new IllegalArgumentException("Order not found"));

        return merchantConfirm(order, allowBackorder);
    }

    /**
     * Company creates shipment for order
     */
    @Transactional
    public Shipment companyCreateShipment(Long companyId, CreateShipmentRequest request) {
        Order order = orderRepo.findByOrderNumber(request.orderNumber()).orElseThrow();
        if (!order.getCompany().getId().equals(companyId)) {
            throw new IllegalArgumentException("Order does not belong to company");
        }
        return createShipment(request);
    }

    /**
     * Get company orders
     */
    public List<Order> getCompanyOrders(Long companyId) {
        return orderRepo.findByCompanyIdOrderByCreatedAtDesc(companyId);
    }

    /**
     * Get company order by order number
     */
    public Order getCompanyOrder(Long companyId, String orderNumber) {
        Order order = orderRepo.findByOrderNumber(orderNumber).orElseThrow();
        if (!order.getCompany().getId().equals(companyId)) {
            throw new IllegalArgumentException("Order does not belong to company");
        }
        return order;
    }

    /**
     * Update order status by company
     */
    @Transactional
    public Order updateOrderStatus(Long companyId, String orderNumber, OrderStatus newStatus) {
        Order order = orderRepo.findByOrderNumber(orderNumber).orElseThrow();
        if (!order.getCompany().getId().equals(companyId)) {
            throw new IllegalArgumentException("Order does not belong to company");
        }
        order.setStatus(newStatus);
        return orderRepo.save(order);
    }
}
