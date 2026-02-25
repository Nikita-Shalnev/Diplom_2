package client;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import models.CreateUser;

import static io.restassured.RestAssured.given;

public class UserCreateClient {
    private static final String BASE_PATH = "/api/auth/";

    @Step("Создание пользователя: {user}")
    public Response createUser(CreateUser user) {
        return given()
                .header("Content-type", "application/json")
                .body(user)
                .when()
                .post(BASE_PATH + "register");
    }

    @Step("Удаление пользователя")
    public Response deleteUser(String token) {
        return given()
                .header("Content-type", "application/json")
                .header("Authorization", token)
                .when()
                .delete(BASE_PATH + "user");
    }
}