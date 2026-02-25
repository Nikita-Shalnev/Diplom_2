package client;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import models.UserLogin;

import static io.restassured.RestAssured.given;

public class UserLoginClient {
    private static final String BASE_PATH = "/api/auth/login";

    @Step("Авторизация пользователя: {userLogin}")
    public Response loginUser(UserLogin userLogin) {
        return given()
                .header("Content-type", "application/json")
                .body(userLogin)
                .when()
                .post(BASE_PATH);
    }
}