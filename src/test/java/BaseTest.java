import client.OrderCreateClient;
import client.UserCreateClient;
import client.UserLoginClient;
import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;

import static org.hamcrest.CoreMatchers.equalTo;

public class BaseTest {

    protected static final String DEFAULT_PASSWORD = "password123";
    protected static final String DEFAULT_NAME = "TestUser";

    protected UserCreateClient userCreateClient;
    protected UserLoginClient userLoginClient;
    protected OrderCreateClient orderCreateClient;
    protected String accessToken;

    @Before
    public void setUp() {
        RestAssured.baseURI = "https://stellarburgers.education-services.ru";
        userCreateClient = new UserCreateClient();
        userLoginClient = new UserLoginClient();
        orderCreateClient = new OrderCreateClient();
    }

    @After
    public void tearDown() {
        if (accessToken != null) {
            deleteUser(accessToken);
        }
    }

    @Step("Проверка кода ответа: {expectedStatusCode}")
    public void checkStatusCode(Response response, int expectedStatusCode) {
        response.then().statusCode(expectedStatusCode);
    }

    @Step("Проверка что success = true")
    public void checkSuccessTrue(Response response) {
        response.then().body("success", equalTo(true));
    }

    @Step("Проверка что success = false")
    public void checkSuccessFalse(Response response) {
        response.then().body("success", equalTo(false));
    }

    @Step("Удаление пользователя")
    public void deleteUser(String token) {
        userCreateClient.deleteUser(token)
                .then()
                .statusCode(202);
    }
}