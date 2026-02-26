import models.CreateUser;
import models.UserLogin;
import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;

public class UserLoginTest extends BaseTest {

    private String randomEmail;

    @Before
    public void prepareUser() {
        randomEmail = "user_" + System.currentTimeMillis() + "@yandex.ru";
        CreateUser user = new CreateUser(randomEmail, DEFAULT_PASSWORD, DEFAULT_NAME);

        Response response = userCreateClient.createUser(user);
        checkStatusCode(response, HttpStatus.SC_OK);
        checkSuccessTrue(response);
        accessToken = response.path("accessToken");
    }

    @Test
    @DisplayName("Успешный логин существующего пользователя")
    @Description("Проверка, что зарегистрированный пользователь может войти в систему")
    public void loginExistingUserSuccess() {
        UserLogin loginData = new UserLogin(randomEmail, DEFAULT_PASSWORD);

        Response response = userLoginClient.loginUser(loginData);
        checkStatusCode(response, HttpStatus.SC_OK);
        checkSuccessTrue(response);
        response.then().body("accessToken", notNullValue());
        response.then().body("refreshToken", notNullValue());
    }

    @Test
    @DisplayName("Логин с неверным email")
    @Description("Проверка, что вход с неправильным email возвращает 401 Unauthorized")
    public void loginWithWrongEmailFails() {
        String wrongEmail = "wrong_" + randomEmail;
        UserLogin loginData = new UserLogin(wrongEmail, DEFAULT_PASSWORD);

        Response response = userLoginClient.loginUser(loginData);
        checkStatusCode(response, HttpStatus.SC_UNAUTHORIZED);
        checkSuccessFalse(response);
        response.then().body("message", equalTo("email or password are incorrect"));
    }

    @Test
    @DisplayName("Логин с неверным паролем")
    @Description("Проверка, что вход с неправильным паролем возвращает 401 Unauthorized")
    public void loginWithWrongPasswordFails() {
        UserLogin loginData = new UserLogin(randomEmail, "wrongPassword");

        Response response = userLoginClient.loginUser(loginData);
        checkStatusCode(response, HttpStatus.SC_UNAUTHORIZED);
        checkSuccessFalse(response);
        response.then().body("message", equalTo("email or password are incorrect"));
    }
}