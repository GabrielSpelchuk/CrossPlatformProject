package com.example.shipping;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Path("/shipping")
@Produces(MediaType.APPLICATION_JSON)
public class ShippingResource {

    @Inject
    @RestClient
    OrderServiceClient orderServiceClient;

    @POST
    @Path("/create/{orderId}")
    public Response createShipment(@PathParam("orderId") Long orderId) {
        try {
            OrderDetails order = orderServiceClient.getOrderDetails(orderId);
            String trackingNumber = "TRK-" + order.id + "-" + System.currentTimeMillis() % 1000;

            String message = String.format(
                    "Shipment created for Order #%d (Item: %s, Qty: %d). Tracking: %s",
                    order.id, order.itemId, order.quantity, trackingNumber
            );

            return Response.ok(message).build();
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Order not found or communication error: " + e.getMessage()).build();
        }
    }
}