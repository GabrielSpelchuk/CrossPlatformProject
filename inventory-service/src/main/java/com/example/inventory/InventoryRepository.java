package com.example.inventory;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class InventoryRepository {
    private final Map<String, Integer> stock = new HashMap<>();

    public InventoryRepository() {
        stock.put("ITEM-001", 100);
        stock.put("ITEM-002", 50);
    }

    public int getStock(String itemId) {
        return stock.getOrDefault(itemId, 0);
    }

    public boolean reduceStock(String itemId, int quantity) {
        if (stock.containsKey(itemId) && stock.get(itemId) >= quantity) {
            stock.put(itemId, stock.get(itemId) - quantity);
            return true;
        }
        return false;
    }
}