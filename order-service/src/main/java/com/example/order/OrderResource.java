package com.example.order;

import com.example.inventory.InventoryGrpc;
import com.example.inventory.StockRequest;
import com.example.inventory.StockResponse;
import com.example.inventory.ReduceStockRequest;
import com.example.inventory.ReduceStockResponse;

import io.quarkus.grpc.GrpcClient;
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
    InventoryGrpc.InventoryBlockingStub inventoryClient;

    @POST
    public Response createOrder(Order order) {
        try {
            // Перевірка stock
            StockResponse stockResponse = inventoryClient.checkStock(
                    StockRequest.newBuilder()
                            .setItemId(order.itemId)
                            .setQuantity(order.quantity)
                            .build()
            );

            if (!stockResponse.getAvailable()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\": \"Not enough stock. Available: " + stockResponse.getCurrentStock() + "\"}")
                        .build();
            }

            // Зменшення stock
            ReduceStockResponse reduceResponse = inventoryClient.reduceStock(
                    ReduceStockRequest.newBuilder()
                            .setItemId(order.itemId)
                            .setQuantity(order.quantity)
                            .build()
            );

            if (!reduceResponse.getSuccess()) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("{\"error\": \"Failed to reduce stock: " + reduceResponse.getMessage() + "\"}")
                        .build();
            }

            // Створення замовлення
            order.status = "CREATED";
            Order savedOrder = repository.save(order);
            return Response.status(Response.Status.CREATED).entity(savedOrder).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Internal server error: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    @GET
    @Path("/{id}")
    public Response getOrder(@PathParam("id") Long id) {
        return repository.findById(id)
                .map(order -> Response.ok(order).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }
}