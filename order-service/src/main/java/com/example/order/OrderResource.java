package com.example.order;

// Згенеровані класи з .proto файлу
import com.example.inventory.InventoryGrpc;
import com.example.inventory.InventoryGrpc.InventoryMutinyClient; // <--- ВИПРАВЛЕННЯ: використовуємо MutinyClient
import com.example.inventory.ReduceStockRequest;
import com.example.inventory.StockRequest;

import io.quarkus.grpc.GrpcClient;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/orders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OrderResource {

    @Inject
    OrderRepository repository;

    @GrpcClient("inventory")
    InventoryMutinyClient inventoryClient; // <--- ВИПРАВЛЕННЯ: інжектуємо MutinyClient

    @POST
    public Uni<Response> createOrder(Order order) {
        // 1. Перевірка наявності товару (gRPC Mutiny клієнт)
        return inventoryClient.checkStock(
                        StockRequest.newBuilder()
                                .setItemId(order.itemId)
                                .setQuantity(order.quantity)
                                .build()
                )
                .onItem().transformToUni(stockResponse -> {
                    if (!stockResponse.getAvailable()) {
                        return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                                .entity("Not enough stock. Available: " + stockResponse.getCurrentStock()).build());
                    }

                    // 2. Зменшення запасу (gRPC Mutiny клієнт)
                    return inventoryClient.reduceStock(
                                    ReduceStockRequest.newBuilder()
                                            .setItemId(order.itemId)
                                            .setQuantity(order.quantity)
                                            .build()
                            )
                            .onItem().transform(reduceResponse -> {
                                if (reduceResponse.getSuccess()) {
                                    // 3. Збереження замовлення
                                    order.status = "CREATED";
                                    Order savedOrder = repository.save(order);

                                    // У реальному житті тут може бути асинхронний виклик Shipping
                                    // У вашому випадку це місце, де ви викликатимете ShippingService (REST)
                                    return Response.status(Response.Status.CREATED).entity(savedOrder).build();
                                }
                                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                        .entity("Failed to reduce stock: " + reduceResponse.getMessage()).build();
                            });
                });
    }

    @GET
    @Path("/{id}")
    public Response getOrder(@PathParam("id") Long id) {
        return repository.findById(id)
                .map(order -> Response.ok(order).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }
}