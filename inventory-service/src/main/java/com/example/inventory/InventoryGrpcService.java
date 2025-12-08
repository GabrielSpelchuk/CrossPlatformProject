package com.example.inventory;

import io.quarkus.grpc.GrpcService;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;

@GrpcService
public class InventoryGrpcService implements Inventory {

    @Inject
    InventoryRepository repository;

    @Override
    public Uni<StockResponse> checkStock(StockRequest request) {
        int currentStock = repository.getStock(request.getItemId());
        boolean available = currentStock >= request.getQuantity();

        StockResponse response = StockResponse.newBuilder()
                .setAvailable(available)
                .setCurrentStock(currentStock)
                .build();

        return Uni.createFrom().item(response);
    }

    @Override
    public Uni<ReduceStockResponse> reduceStock(ReduceStockRequest request) {
        boolean success = repository.reduceStock(request.getItemId(), request.getQuantity());
        String message = success ? "Stock reduced successfully" : "Not enough stock or item not found";

        ReduceStockResponse response = ReduceStockResponse.newBuilder()
                .setSuccess(success)
                .setMessage(message)
                .build();

        return Uni.createFrom().item(response);
    }
}