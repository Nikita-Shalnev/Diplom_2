package client;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import models.CreateOrder;

import static io.restassured.RestAssured.given;

public class OrderCreateClient {
    private static final String BASE_PATH = "/api/orders";

    @Step("Создание заказа с авторизацией")
    public Response createOrderWithToken(CreateOrder order, String token) {
        return given()
                .header("Content-type", "application/json")
                .header("Authorization", token)
                .body(order)
                .when()
                .post(BASE_PATH);
    }

    @Step("Создание заказа без авторизации")
    public Response createOrderWithoutToken(CreateOrder order) {
        return given()
                .header("Content-type", "application/json")
                .body(order)
                .when()
                .post(BASE_PATH);
    }

    @Step("Получение списка ингредиентов")
    public Response getIngredients() {
        return given()
                .header("Content-type", "application/json")
                .when()
                .get("/api/ingredients");
    }
}