package com.hand.demo.model.enums;

public enum OrderStatus {
    CREATED,        // بعد الـ checkout مباشرة (إن لم تربط دفع)
    CONFIRMED,      // التاجر خصّص كميات (ولو جزئيًا)
    PACKING,
    SHIPPED,
    DELIVERED,
    CANCELED
}
