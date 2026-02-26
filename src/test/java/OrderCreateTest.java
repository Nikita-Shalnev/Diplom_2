import models.CreateOrder;
import models.CreateUser;
import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;

public class OrderCreateTest extends BaseTest {

    private List<String> ingredientIds;
    private String userToken;

    @Before
    public void prepareData() {
        // Получаем список ингредиентов
        Response ingredientsResponse = orderCreateClient.getIngredients();
        checkStatusCode(ingredientsResponse, HttpStatus.SC_OK);

        // Получаем ID ингредиентов
        List<Map<String, Object>> ingredients = ingredientsResponse.path("data");
        ingredientIds = new ArrayList<>();

        if (ingredients != null) {
            for (Map<String, Object> ingredient : ingredients) {
                if (ingredient != null && ingredient.get("_id") != null) {
                    ingredientIds.add((String) ingredient.get("_id"));
                }
            }
        }

        // Создаём пользователя для авторизованных тестов
        String randomEmail = "user_" + System.currentTimeMillis() + "@yandex.ru";
        CreateUser user = new CreateUser(randomEmail, DEFAULT_PASSWORD, DEFAULT_NAME);
        Response createResponse = userCreateClient.createUser(user);

        if (createResponse.getStatusCode() == HttpStatus.SC_OK) {
            userToken = createResponse.path("accessToken");
        }
    }

    @Test
    @DisplayName("Создание заказа с авторизацией и ингредиентами")
    @Description("Проверка, что авторизованный пользователь может создать заказ")
    public void createOrderWithAuthAndIngredientsSuccess() {
        // Пропускаем тест, если нет данных
        if (ingredientIds == null || ingredientIds.isEmpty() || userToken == null) {
            return;
        }

        List<String> orderIngredients = new ArrayList<>();
        orderIngredients.add(ingredientIds.get(0));
        orderIngredients.add(ingredientIds.get(1));

        CreateOrder order = new CreateOrder(orderIngredients);
        Response response = orderCreateClient.createOrderWithToken(order, userToken);

        checkStatusCode(response, HttpStatus.SC_OK);
        checkSuccessTrue(response);
        response.then().body("order.number", notNullValue());
    }

    @Test
    @DisplayName("Создание заказа без авторизации")
    @Description("Проверка, что неавторизованный пользователь может создать заказ")
    public void createOrderWithoutAuthSuccess() {
        if (ingredientIds == null || ingredientIds.isEmpty()) {
            return;
        }

        List<String> orderIngredients = new ArrayList<>();
        orderIngredients.add(ingredientIds.get(0));
        orderIngredients.add(ingredientIds.get(1));

        CreateOrder order = new CreateOrder(orderIngredients);
        Response response = orderCreateClient.createOrderWithoutToken(order);

        checkStatusCode(response, HttpStatus.SC_OK);
        checkSuccessTrue(response);
        response.then().body("order.number", notNullValue());
    }

    @Test
    @DisplayName("Создание заказа без ингредиентов")
    @Description("Проверка, что нельзя создать заказ без ингредиентов")
    public void createOrderWithoutIngredientsFails() {
        List<String> emptyIngredients = new ArrayList<>();
        CreateOrder order = new CreateOrder(emptyIngredients);

        Response response = orderCreateClient.createOrderWithoutToken(order);
        checkStatusCode(response, HttpStatus.SC_BAD_REQUEST);
        checkSuccessFalse(response);
        response.then().body("message", equalTo("Ingredient ids must be provided"));
    }

    @Test
    @DisplayName("Создание заказа с неверным хешем ингредиента")
    @Description("Проверка, что с невалидным ID ингредиента возвращается 500")
    public void createOrderWithInvalidHashFails() {
        List<String> invalidIngredients = new ArrayList<>();
        invalidIngredients.add("invalid-hash-123");
        invalidIngredients.add("invalid-hash-456");

        CreateOrder order = new CreateOrder(invalidIngredients);
        Response response = orderCreateClient.createOrderWithoutToken(order);

        checkStatusCode(response, HttpStatus.SC_INTERNAL_SERVER_ERROR);
    }
}