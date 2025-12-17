package com.example.frontend;

public class OrderResponse {
    public Long id;
    public String itemId;
    public int quantity;
    public String status;

    public OrderResponse() {
    }

    public OrderResponse(Long id, String itemId, int quantity, String status) {
        this.id = id;
        this.itemId = itemId;
        this.quantity = quantity;
        this.status = status;
    }
}