package com.example.order;

// Імпорти з proto-definitions
import com.example.inventory.Inventory;
import com.example.inventory.InventoryGrpc;
import com.example.inventory.StockRequest;
import com.example.inventory.StockResponse;
import com.example.inventory.ReduceStockRequest;
import com.example.inventory.ReduceStockResponse;

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
    InventoryGrpc.InventoryBlockingStub inventoryClient;

    @POST
    public Response createOrder(Order order) {
        StockResponse stockResponse = inventoryClient.checkStock(
                StockRequest.newBuilder()
                        .setItemId(order.itemId)
                        .setQuantity(order.quantity)
                        .build()
        );

        if (!stockResponse.getAvailable()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Not enough stock. Available: " + stockResponse.getCurrentStock())
                    .build();
        }

        ReduceStockResponse reduceResponse = inventoryClient.reduceStock(
                ReduceStockRequest.newBuilder()
                        .setItemId(order.itemId)
                        .setQuantity(order.quantity)
                        .build()
        );

        if (!reduceResponse.getSuccess()) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Failed to reduce stock: " + reduceResponse.getMessage())
                    .build();
        }

        order.status = "CREATED";
        Order savedOrder = repository.save(order);
        return Response.status(Response.Status.CREATED).entity(savedOrder).build();
    }


    @GET
    @Path("/{id}")
    public Response getOrder(@PathParam("id") Long id) {
        return repository.findById(id)
                .map(order -> Response.ok(order).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }
}