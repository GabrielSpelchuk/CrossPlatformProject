package com.example.order;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.Optional;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class OrderRepository {
    private final Map<Long, Order> orders = new ConcurrentHashMap<>();
    private Long nextId = 1L;

    public Order save(Order order) {
        order.id = nextId++;
        orders.put(order.id, order);
        return order;
    }

    public Optional<Order> findById(Long id) {
        return Optional.ofNullable(orders.get(id));
    }
}