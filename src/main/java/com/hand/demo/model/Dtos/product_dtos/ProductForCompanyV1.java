package com.hand.demo.model.Dtos.product_dtos;
// الواجهة المشتركة
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "kind")
@JsonSubTypes({
    @JsonSubTypes.Type(value = InStockProductForCompanyV1.class,  name = "STOCK"),
    @JsonSubTypes.Type(value = PreOrderProductForCompanyV1.class, name = "PREORDER")
})
public sealed interface ProductForCompanyV1
        permits InStockProductForCompanyV1, PreOrderProductForCompanyV1 {}
