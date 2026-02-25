import client.UserCreateClient;
import models.CreateUser;
import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;

public class UserCreateTest extends BaseTest {

    @Test
    @DisplayName("Создание уникального пользователя")
    @Description("Проверка успешной регистрации нового пользователя")
    public void createUniqueUserSuccess() {
        String randomEmail = "user_" + System.currentTimeMillis() + "@yandex.ru";
        CreateUser user = new CreateUser(randomEmail, DEFAULT_PASSWORD, DEFAULT_NAME);

        Response response = userCreateClient.createUser(user);
        checkStatusCode(response, 200);
        checkSuccessTrue(response);
        response.then().body("accessToken", notNullValue());
        response.then().body("refreshToken", notNullValue());

        accessToken = response.path("accessToken");
    }

    @Test
    @DisplayName("Создание уже существующего пользователя")
    @Description("Проверка, что нельзя создать пользователя с уже занятыми данными")
    public void createDuplicateUserFails() {
        String randomEmail = "user_" + System.currentTimeMillis() + "@yandex.ru";
        CreateUser user = new CreateUser(randomEmail, DEFAULT_PASSWORD, DEFAULT_NAME);

        Response response = userCreateClient.createUser(user);
        checkStatusCode(response, 200);
        accessToken = response.path("accessToken");

        Response duplicateResponse = userCreateClient.createUser(user);
        checkStatusCode(duplicateResponse, 403);
        checkSuccessFalse(duplicateResponse);
        duplicateResponse.then().body("message", equalTo("User already exists"));
    }

    @Test
    @DisplayName("Создание пользователя без email")
    @Description("Проверка, что нельзя создать пользователя без email")
    public void createUserWithoutEmailFails() {
        CreateUser user = new CreateUser("", DEFAULT_PASSWORD, DEFAULT_NAME);

        Response response = userCreateClient.createUser(user);
        checkStatusCode(response, 403);
        checkSuccessFalse(response);
        response.then().body("message", equalTo("Email, password and name are required fields"));
    }

    @Test
    @DisplayName("Создание пользователя без пароля")
    @Description("Проверка, что нельзя создать пользователя без password")
    public void createUserWithoutPasswordFails() {
        String randomEmail = "user_" + System.currentTimeMillis() + "@yandex.ru";
        CreateUser user = new CreateUser(randomEmail, "", DEFAULT_NAME);

        Response response = userCreateClient.createUser(user);
        checkStatusCode(response, 403);
        checkSuccessFalse(response);
        response.then().body("message", equalTo("Email, password and name are required fields"));
    }

    @Test
    @DisplayName("Создание пользователя без имени")
    @Description("Проверка, что нельзя создать пользователя без name")
    public void createUserWithoutNameFails() {
        String randomEmail = "user_" + System.currentTimeMillis() + "@yandex.ru";
        CreateUser user = new CreateUser(randomEmail, DEFAULT_PASSWORD, "");

        Response response = userCreateClient.createUser(user);
        checkStatusCode(response, 403);
        checkSuccessFalse(response);
        response.then().body("message", equalTo("Email, password and name are required fields"));
    }
}