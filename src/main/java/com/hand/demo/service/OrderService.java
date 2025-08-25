package com.hand.demo.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hand.demo.model.dto.CreateShipmentRequest;
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
import com.hand.demo.repository.CartRepository;
import com.hand.demo.repository.OrderRepository;
import com.hand.demo.repository.ProductRepository;
import com.hand.demo.repository.ShipmentRepository;

import lombok.RequiredArgsConstructor;

@Service @RequiredArgsConstructor
public class OrderService {

    private final CartRepository cartRepo;
    private final OrderRepository orderRepo;
    private final ProductRepository productRepo;
    private final ShipmentRepository shipmentRepo;

    /** Checkout لسلة شركة واحدة (زر "اطلب من هذا المتجر") */
    @Transactional
    public Order checkout(Long cartId) {
        Cart cart = cartRepo.findById(cartId).orElseThrow();
        if (cart.isClosed()) throw new IllegalStateException("Cart already closed");
        if (cart.getItems().isEmpty()) throw new IllegalStateException("Cart empty");

        Order order = new Order();
        order.setCustomer(cart.getCustomer());
        order.setCompany(cart.getCompany());
        order.setStatus(OrderStatus.CREATED);

        for (CartItem ci : cart.getItems()) {
            OrderItem oi = new OrderItem();
            oi.setOrder(order);
            oi.setProductId(ci.getProduct().getId());
            oi.setProductName(ci.getProductNameSnapshot());
            oi.setType(ci.getType());
            oi.setUnitPriceBase(ci.getUnitPriceBase());
            oi.setUnitPriceExtra(ci.getUnitPriceExtra());
            oi.setQtyOrdered(ci.getQuantity());

            if (ci.getType() == OrderItemType.STOCK && ci.getProduct() instanceof InStockProduct isp) {
                oi.setAllowBackorder(isp.isAllowBackorder());
            }
            if (ci.getType() == OrderItemType.PRE_ORDER) {
                oi.setPreparationDays(ci.getPreparationDays());
            }

            // selections
            for (CartItemSelection cs : ci.getSelections()) {
                OrderItemSelection os = new OrderItemSelection();
                os.setOrderItem(oi);
                os.setAttributeId(cs.getAttributeId());
                os.setAttributeName(cs.getAttributeName());
                os.setValueId(cs.getValueId());
                os.setValueText(cs.getValueText());
                os.setExtraPrice(cs.getExtraPrice());
                oi.getSelections().add(os);
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
    public Order merchantConfirm(String orderNumber, boolean allowBackorder) {
        Order order = orderRepo.findByOrderNumber(orderNumber).orElseThrow();

        for (OrderItem it : order.getItems()) {
            int need = it.getQtyOrdered() - it.getQtyAllocated() - it.getQtyCanceled();
            if (need <= 0) continue;

            if (it.getType() == OrderItemType.STOCK) {
                InStockProduct isp = (InStockProduct) productRepo.getReferenceById(it.getProductId());
                int available = isp.getQuantityAvailable();

                int alloc = Math.min(available, need);
                if (alloc > 0) {
                    it.setQtyAllocated(it.getQtyAllocated() + alloc);
                    int committed = isp.getQuantityCommitted()==null?0:isp.getQuantityCommitted();
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
            if (shipQty <= 0) continue;

            ShipmentItem si = new ShipmentItem();
            si.setShipment(sh); si.setOrderItem(it); si.setQty(shipQty);
            sh.getItems().add(si);

            it.setQtyShipped(it.getQtyShipped() + shipQty);
            // ملاحظة: عند الشحن النهائي، لو نموذجك يتطلب خصم من totalQuantity افعل ذلك هناك
        }

        return shipmentRepo.save(sh);
    }
}
}
