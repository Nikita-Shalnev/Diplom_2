import client.OrderCreateClient;
import client.UserCreateClient;
import models.CreateOrder;
import models.CreateUser;
import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;

public class OrderCreateTest extends BaseTest {

    private List<String> ingredientIds;
    private String userToken;

    @Before
    public void prepareData() {
        // Получаем список ингредиентов
        Response ingredientsResponse = orderCreateClient.getIngredients();
        checkStatusCode(ingredientsResponse, 200);
        ingredientIds = ingredientsResponse.path("data._id");

        // Создаём пользователя для авторизованных тестов
        String randomEmail = "user_" + System.currentTimeMillis() + "@yandex.ru";
        CreateUser user = new CreateUser(randomEmail, DEFAULT_PASSWORD, DEFAULT_NAME);
        Response createResponse = userCreateClient.createUser(user);
        checkStatusCode(createResponse, 200);
        userToken = createResponse.path("accessToken");
    }

    @Test
    @DisplayName("Создание заказа с авторизацией и ингредиентами")
    @Description("Проверка, что авторизованный пользователь может создать заказ")
    public void createOrderWithAuthAndIngredientsSuccess() {
        List<String> orderIngredients = List.of(ingredientIds.get(0), ingredientIds.get(1));
        CreateOrder order = new CreateOrder(orderIngredients);

        Response response = orderCreateClient.createOrderWithToken(order, userToken);
        checkStatusCode(response, 200);
        checkSuccessTrue(response);
        response.then().body("order.number", notNullValue());
    }

    @Test
    @DisplayName("Создание заказа без авторизации")
    @Description("Проверка, что неавторизованный пользователь может создать заказ (по документации)")
    public void createOrderWithoutAuthSuccess() {
        List<String> orderIngredients = List.of(ingredientIds.get(0), ingredientIds.get(1));
        CreateOrder order = new CreateOrder(orderIngredients);

        Response response = orderCreateClient.createOrderWithoutToken(order);
        checkStatusCode(response, 200);
        checkSuccessTrue(response);
        response.then().body("order.number", notNullValue());
    }

    @Test
    @DisplayName("Создание заказа без ингредиентов")
    @Description("Проверка, что нельзя создать заказ без ингредиентов, возвращается 400")
    public void createOrderWithoutIngredientsFails() {
        List<String> emptyIngredients = List.of();
        CreateOrder order = new CreateOrder(emptyIngredients);

        Response response = orderCreateClient.createOrderWithoutToken(order);
        checkStatusCode(response, 400);
        checkSuccessFalse(response);
        response.then().body("message", equalTo("Ingredient ids must be provided"));
    }

    @Test
    @DisplayName("Создание заказа с неверным хешем ингредиента")
    @Description("Проверка, что с невалидным ID ингредиента возвращается 500 Internal Server Error")
    public void createOrderWithInvalidHashFails() {
        List<String> invalidIngredients = List.of("invalid-hash-123", "invalid-hash-456");
        CreateOrder order = new CreateOrder(invalidIngredients);

        Response response = orderCreateClient.createOrderWithoutToken(order);

        checkStatusCode(response, 500);
    }
}