package com.example.frontend;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "order-service")
public interface OrderServiceClient {

    @POST
    @Path("/orders")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    OrderResponse createOrder(OrderRequest order);

    @GET
    @Path("/orders/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    OrderResponse getOrder(@PathParam("id") Long id);
}
