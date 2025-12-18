package com.example.frontend;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Path("/")
@Authenticated
public class FrontendResource {

    @Inject
    SecurityIdentity identity;

    @Inject
    Template index;

    @Inject
    Template orders;

    @Inject
    Template orderResult;

    @Inject
    Template orderList;

    @RestClient
    OrderServiceClient orderClient;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance home() {
        String username = identity.getPrincipal().getName();
        String roles = String.join(", ", identity.getRoles());
        return index
                .data("username", username)
                .data("roles", roles);
    }

    // Сторінка створення замовлення
    @GET
    @Path("/orders/create")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance ordersPage() {
        return orders.data(
                "username", identity.getPrincipal().getName());
    }

    // Сторінка списку/пошуку замовлень
    @GET
    @Path("/orders/list")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance orderListPage() {
        return orderList.data(
                "username", identity.getPrincipal().getName());
    }

    // Створення нового замовлення
    @POST
    @Path("/orders/create")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance createOrder(
            @FormParam("itemId") String itemId,
            @FormParam("quantity") int quantity) {
        try {
            OrderRequest request = new OrderRequest();
            request.itemId = itemId;
            request.quantity = quantity;

            OrderResponse order = orderClient.createOrder(request);

            return orderResult
                    .data("success", true)
                    .data("order", order)
                    .data("username", identity.getPrincipal().getName());
        } catch (Exception e) {
            return orderResult
                    .data("success", false)
                    .data("error", e.getMessage())
                    .data("username", identity.getPrincipal().getName());
        }
    }

    // Перегляд конкретного замовлення (через GET параметр)
    @GET
    @Path("/orders/view")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance viewOrderByParam(@QueryParam("id") Long id) {
        if (id == null) {
            return orderResult
                    .data("success", false)
                    .data("error", "Не вказано номер замовлення")
                    .data("username", identity.getPrincipal().getName());
        }

        try {
            OrderResponse order = orderClient.getOrder(id);
            return orderResult
                    .data("success", true)
                    .data("order", order)
                    .data("username", identity.getPrincipal().getName());
        } catch (Exception e) {
            return orderResult
                    .data("success", false)
                    .data("error", "Замовлення #" + id + " не знайдено")
                    .data("username", identity.getPrincipal().getName());
        }
    }

    // Перегляд конкретного замовлення (через path параметр)
    @GET
    @Path("/orders/{id}")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance viewOrder(@PathParam("id") Long id) {
        try {
            OrderResponse order = orderClient.getOrder(id);
            return orderResult
                    .data("success", true)
                    .data("order", order)
                    .data("username", identity.getPrincipal().getName());
        } catch (Exception e) {
            return orderResult
                    .data("success", false)
                    .data("error", "Замовлення не знайдено")
                    .data("username", identity.getPrincipal().getName());
        }
    }
}