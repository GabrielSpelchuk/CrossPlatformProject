package com.example.frontend;

public class OrderRequest {
    public String itemId;
    public int quantity;

    public OrderRequest() {
    }

    public OrderRequest(String itemId, int quantity) {
        this.itemId = itemId;
        this.quantity = quantity;
    }
}