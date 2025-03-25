package org.itmo.testing.lab2.integration;

import io.javalin.Javalin;
import io.restassured.RestAssured;
import org.itmo.testing.lab2.controller.UserAnalyticsController;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserAnalyticsIntegrationTest {

    private Javalin app;
    private int port = 7000;

    @BeforeAll
    void setUp() {
        app = UserAnalyticsController.createApp();
        app.start(port);
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }

    @AfterAll
    void tearDown() {
        app.stop();
    }

    @ParameterizedTest
    @CsvSource({
            "user1, Alice, 200, 'User registered: true', ''",
            "user2, , 400, '', 'Missing parameters'"
    })
    @Order(1)
    @DisplayName("Параметризованный тест регистрации пользователя")
    void testUserRegistration(String userId, String userName, int expectedStatus, String expectedBody, String expectedEqualBody) {
        var request = given().queryParam("userId", userId);
        if (userName != null && !userName.isEmpty()) {
            request.queryParam("userName", userName);
        }

        var response = request.when().post("/register");
        response.then().statusCode(expectedStatus);

        if (!expectedEqualBody.isEmpty()) {
            response.then().body(equalTo(expectedEqualBody));
        } else {
            response.then().body(equalTo(expectedBody));
        }
    }

    @ParameterizedTest
    @CsvSource({
            "user1, 'invalid-date', 2024-03-01T12:00:00, 400, 'Invalid data'",
            "user1, 2024-03-01T10:00:00, 2024-03-01T12:00:00, 200, 'Session recorded'"
    })
    @Order(2)
    @DisplayName("Параметризованный тест записи сессии")
    void testRecordSession(String userId, String loginTime, String logoutTime, int expectedStatus, String expectedBody) {
        given()
                .queryParam("userId", userId)
                .queryParam("loginTime", loginTime)
                .queryParam("logoutTime", logoutTime)
                .when()
                .post("/recordSession")
                .then()
                .statusCode(expectedStatus)
                .body(containsString(expectedBody));
    }

    @ParameterizedTest
    @CsvSource({
            "user1, 200, 'Total activity:', 'minutes', ''",
            ", 400, 'Missing userId', '', ''"
    })
    @Order(3)
    @DisplayName("Параметризованный тест получения общего времени активности")
    void testGetTotalActivity(String userId, int expectedStatus, String expectedBody1, String expectedBody2, String expectedEqualBody) {
        var request = given();
        if (userId != null && !userId.isEmpty()) {
            request.queryParam("userId", userId);
        }

        var response = request.when().get("/totalActivity");
        response.then().statusCode(expectedStatus);

        if (!expectedEqualBody.isEmpty()) {
            response.then().body(equalTo(expectedEqualBody));
        } else {
            response.then().body(containsString(expectedBody1));
            if (!expectedBody2.isEmpty()) {
                response.then().body(containsString(expectedBody2));
            }
        }
    }

    @ParameterizedTest
    @CsvSource({
            "30, 200, application/json, ''",
            "invalid, 400, '', 'Invalid number format for days'"
    })
    @Order(4)
    @DisplayName("Параметризованный тест получения неактивных пользователей")
    void testGetInactiveUsers(String days, int expectedStatus, String expectedContentType, String expectedBody) {
        var request = given().queryParam("days", days);

        var response = request.when().get("/inactiveUsers");
        response.then().statusCode(expectedStatus);

        if (!expectedContentType.isEmpty()) {
            response.then().contentType(expectedContentType);
        }

        if (!expectedBody.isEmpty()) {
            response.then().body(equalTo(expectedBody));
        }
    }

    @ParameterizedTest
    @CsvSource({
            "user1, 2024-03, 200, application/json, ''",
            "user1, invalid-month, 400, '', 'Invalid data'"
    })
    @Order(5)
    @DisplayName("Параметризованный тест получения активности за месяц")
    void testGetMonthlyActivity(String userId, String month, int expectedStatus, String expectedContentType, String expectedBody) {
        var response = given()
                .queryParam("userId", userId)
                .queryParam("month", month)
                .when()
                .get("/monthlyActivity");

        response.then().statusCode(expectedStatus);

        if (!expectedContentType.isEmpty()) {
            response.then().contentType(expectedContentType);
        }
        if (!expectedBody.isEmpty()) {
            response.then().body(containsString(expectedBody));
        }
    }
}
