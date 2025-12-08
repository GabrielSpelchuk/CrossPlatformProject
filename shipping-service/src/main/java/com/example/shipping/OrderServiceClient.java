package com.example.shipping;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

// Клас Order (спрощений)
class OrderDetails {
    public Long id;
    public String itemId;
    public int quantity;
    public String status;
}

@Path("/orders")
@RegisterRestClient(configKey = "order-api")
public interface OrderServiceClient {
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    OrderDetails getOrderDetails(@PathParam("id") Long id);
}