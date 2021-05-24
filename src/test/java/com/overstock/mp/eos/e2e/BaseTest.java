package com.overstock.mp.eos.e2e;

import io.restassured.RestAssured;
import io.restassured.http.Method;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ActiveProfiles;

import javax.annotation.PostConstruct;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;

@ActiveProfiles(profiles = "local")
@SpringBootTest( classes = {E2EApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Component
public class BaseTest {

    @Value("${baseUrl}")
    public String baseUrl;

    @PostConstruct
    public void setDefaultPort() {
        RestAssured.baseURI = baseUrl;
        RestAssured.port = 443;
        RestAssured.useRelaxedHTTPSValidation();
        Response response = healthCheck();
        assertThat("System health check failed", response.getStatusCode() == HttpStatus.SC_OK);
    }

    public static Response healthCheck() {
        return given().when()
                .request(Method.GET, "health");
    }

}
