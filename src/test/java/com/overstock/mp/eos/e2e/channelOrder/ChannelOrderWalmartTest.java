
package com.overstock.mp.eos.e2e.channelOrder;

        import com.fasterxml.jackson.core.JsonProcessingException;
        import com.fasterxml.jackson.databind.ObjectMapper;
        import com.github.javafaker.Faker;
        import com.overstock.mp.eos.common.dto.ChannelOrderRequestDto;
        import com.overstock.mp.eos.common.dto.ChannelOrderResponseDto;
        import com.overstock.mp.eos.common.dto.error.ErrorResponseDto;
        import com.overstock.mp.eos.common.enums.ChannelOrderStatus;
        import com.overstock.mp.eos.e2e.E2EApplication;
        import com.overstock.mp.eos.e2e.common.DbConnection;
        import com.overstock.mp.eos.e2e.steps.ChannelOrderTestSteps;
        import io.restassured.http.ContentType;
        import io.restassured.http.Method;
        import io.restassured.path.json.JsonPath;
        import io.restassured.response.Response;
        import net.serenitybdd.junit5.SerenityTest;
        import net.serenitybdd.rest.SerenityRest;
        import net.thucydides.core.annotations.Steps;
        import org.apache.http.HttpHeaders;
        import org.hamcrest.Matchers;
        import org.junit.jupiter.api.BeforeEach;
        import org.junit.jupiter.api.Test;
        import org.springframework.beans.factory.annotation.Autowired;
        import org.springframework.boot.test.context.SpringBootTest;
        import org.springframework.test.context.ActiveProfiles;

        import java.math.BigDecimal;
        import java.util.List;
        import java.util.Map;

        import static com.overstock.mp.eos.common.constants.TestConstants.SHIPPING_COST;
        import static com.overstock.mp.eos.integration.common.ITTestConstants.getObjectMapper;
        import static com.overstock.mp.eos.integration.common.ITTestConstants.getXmlMapper;
        import static io.restassured.RestAssured.given;
        import static org.hamcrest.CoreMatchers.*;
        import static org.hamcrest.MatcherAssert.assertThat;
        import static org.hamcrest.Matchers.containsString;
        import static org.hamcrest.Matchers.greaterThan;

@ActiveProfiles(profiles = "local")
@SpringBootTest( classes = {E2EApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@SerenityTest
public class ChannelOrderWalmartTest {

    private static final String WALMART = "CA_WALMART";

    @Autowired
    private DbConnection dbConnection;

    @Steps
    private ChannelOrderTestSteps channelOrderTestSteps;

    @BeforeEach
    void beforeEach() {
        channelOrderTestSteps.setDataSource(dbConnection.getDataSource());
    }

    @Test
    public void placeOrderSingleItemTest() throws JsonProcessingException {

        Faker faker = new Faker();
        String status = "UNRESTRICTED";
        String channelOrderRedId = "OB0" + faker.number().numberBetween(1111, 99999);

        List<Map<String, Object>> dbResult = channelOrderTestSteps.getProductOptionIds(WALMART, status, 2);
        Map<String, Object> singleRow = dbResult.get(0);
        BigDecimal price = (BigDecimal) singleRow.get("PRI_PRICE");
        long optionId = ((BigDecimal) singleRow.get("PRI_ID")).longValue();
        BigDecimal tax = BigDecimal.valueOf(0.00);
        BigDecimal orderTotalValue = price;

        int updateInventory = channelOrderTestSteps.updateInventory(optionId, 20);
        assertThat(updateInventory, is(greaterThan(0)));

        ChannelOrderRequestDto channelOrderRequestDto = channelOrderTestSteps.getOrderBody(price, tax, optionId, WALMART, channelOrderRedId, orderTotalValue);
        ChannelOrderResponseDto channelOrderResponseDto = channelOrderTestSteps.postSingleOrder(channelOrderRequestDto);
        channelOrderTestSteps.verifyAssertPostSingleOrder(channelOrderResponseDto, WALMART);

        Map<String, Object> dbData = channelOrderTestSteps.getChannelOrder(channelOrderResponseDto.getInvoiceId());
        channelOrderTestSteps.verifyChannelOrder(dbData, WALMART, orderTotalValue);

    }

    @Test
    public void getOrderByChannelOrderRefIdTest() throws JsonProcessingException {

        Faker faker = new Faker();
        String status = "UNRESTRICTED";
        String channelOrderRedId = "OB0" + faker.number().numberBetween(1111, 99999);

        List<Map<String, Object>> dbResult = channelOrderTestSteps.getProductOptionIds(WALMART, status, 2);
        Map<String, Object> singleRowData = dbResult.get(0);
        BigDecimal price = (BigDecimal) singleRowData.get("PRI_PRICE");
        long optionId = ((BigDecimal) singleRowData.get("PRI_ID")).longValue();
        BigDecimal tax = BigDecimal.valueOf(0.00);
        BigDecimal orderTotalValue = price;

        int updateInventory = channelOrderTestSteps.updateInventory(optionId, 20);
        assertThat(updateInventory, is(greaterThan(0)));

        ChannelOrderRequestDto channelOrderRequestDto = channelOrderTestSteps.getOrderBody(price, tax, optionId, WALMART, channelOrderRedId, orderTotalValue);
        channelOrderTestSteps.postSingleOrder(channelOrderRequestDto);
        ChannelOrderResponseDto channelOrderResponse = channelOrderTestSteps.getSingleOrderByChannelOrderRefId(WALMART, channelOrderRedId);
        channelOrderTestSteps.verifyAssertPostSingleOrder(channelOrderResponse, WALMART);

        Map<String, Object> dbData = channelOrderTestSteps.getChannelOrder(channelOrderResponse.getInvoiceId());
        channelOrderTestSteps.verifyChannelOrder(dbData, WALMART, orderTotalValue);

    }

    @Test
    public void bookRestrictedProductOrderTest() throws JsonProcessingException {

        Faker faker = new Faker();
        String status = "RESTRICTED";
        String channelOrderRedId = "OB0" + faker.number().numberBetween(1111, 99999);

        List<Map<String, Object>> dbResult = channelOrderTestSteps.getProductOptionIds(WALMART, status, 2);
        Map<String, Object> singleRowData = dbResult.get(0);
        BigDecimal price = (BigDecimal) singleRowData.get("PRI_PRICE");
        long optionId = ((BigDecimal) singleRowData.get("PRI_ID")).longValue();
        BigDecimal tax = BigDecimal.valueOf(0.00);
        BigDecimal orderTotalValue = price;

        int updateInventory = channelOrderTestSteps.updateInventory(optionId, 20);
        assertThat(updateInventory, is(greaterThan(0)));

        ChannelOrderRequestDto channelOrderRequestDto = channelOrderTestSteps.getOrderBody(price, tax, optionId, WALMART, channelOrderRedId, orderTotalValue);
        ChannelOrderResponseDto channelOrderResponse = channelOrderTestSteps.postSingleRestrictedOrder(channelOrderRequestDto);

        assertThat(channelOrderResponse.getInvoiceId(), is(nullValue()));
        assertThat(channelOrderResponse.getChannelOrderStatus(),  is(ChannelOrderStatus.CREATED));
        assertThat(channelOrderResponse.getChannelKey(), Matchers.containsString(WALMART));
        assertThat(channelOrderResponse.getOrderErrors().get(0).getErrorCode(), Matchers.is("5001"));
        assertThat(channelOrderResponse.getOrderErrors().get(0).getMessage(), Matchers.containsString("Order not in bookable state"));

    }

    @Test
    public void rebookSingleOrderTest() throws JsonProcessingException {

        Faker faker = new Faker();
        String status = "UNRESTRICTED";
        String channelOrderRedId = "OB0" + faker.number().numberBetween(1111, 99999);

        List<Map<String, Object>> dbResult = channelOrderTestSteps.getProductOptionIds(WALMART, status, 2);
        Map<String, Object> singleRowData = dbResult.get(0);
        BigDecimal price = (BigDecimal) singleRowData.get("PRI_PRICE");
        long optionId = ((BigDecimal) singleRowData.get("PRI_ID")).longValue();
        BigDecimal tax = BigDecimal.valueOf(0.00);
        BigDecimal orderTotalValue = price;

        int updateInventory = channelOrderTestSteps.updateInventory(optionId, 20);
        assertThat(updateInventory, is(greaterThan(0)));

        ChannelOrderRequestDto channelOrderRequestDto = channelOrderTestSteps.getOrderBody(price, tax, optionId, WALMART, channelOrderRedId, orderTotalValue);
        channelOrderTestSteps.postSingleOrder(channelOrderRequestDto);
        ObjectMapper mapper = new ObjectMapper();
        String body = mapper.writeValueAsString(channelOrderRequestDto);

        ErrorResponseDto errorResponse = given()
                .header(HttpHeaders.CONTENT_TYPE, ContentType.JSON)
                .header(HttpHeaders.ACCEPT, ContentType.JSON)
                .when().body(body)
                .request(Method.POST, "/v2/channel/order")
                .then()
                .extract()
                .response().as(ErrorResponseDto.class);

        assertThat(errorResponse.getErrors().get(0).getException(), containsString("ChannelOrderAlreadyExistsException: ChannelOrder already exists for channelKey=" + WALMART + ", channelReferenceId=" + channelOrderRedId));
        assertThat(errorResponse.getErrors().get(0).getMessage(), containsString("ChannelOrder already exists for channelKey=" + WALMART + ", channelReferenceId=" + channelOrderRedId));//

    }

    @Test
    public void placeOrderSingleItemXmlTest() throws JsonProcessingException {

        Faker faker = new Faker();
        String status = "UNRESTRICTED";
        String channelOrderRedId = "OB0" + faker.number().numberBetween(1111, 99999);

        List<Map<String, Object>> dbResult = channelOrderTestSteps.getProductOptionIds(WALMART, status, 2);
        Map<String, Object> singleRowData = dbResult.get(0);
        BigDecimal price = (BigDecimal) singleRowData.get("PRI_PRICE");
        long optionId = ((BigDecimal) singleRowData.get("PRI_ID")).longValue();
        BigDecimal tax = BigDecimal.valueOf(0.00);
        BigDecimal orderTotalValue = price;

        int updateInventory = channelOrderTestSteps.updateInventory(optionId, 20);
        assertThat(updateInventory, is(greaterThan(0)));

        ChannelOrderRequestDto channelOrderRequestDto = channelOrderTestSteps.getOrderBody(price, tax, optionId, WALMART, channelOrderRedId, orderTotalValue);
        ObjectMapper mapper = getObjectMapper();
        String body = mapper.writeValueAsString(channelOrderRequestDto);

        Response response = given()
                .header(HttpHeaders.CONTENT_TYPE, ContentType.JSON)
                .header(HttpHeaders.ACCEPT, ContentType.XML)
                .when().body(body)
                .request(Method.POST, "v2/channel/order");

        ChannelOrderResponseDto channelOrderResponse = getXmlMapper().readValue(response.getBody().asString(), ChannelOrderResponseDto.class);

        assertThat(channelOrderResponse.getInvoiceId(), Matchers.is(notNullValue()));
        assertThat(channelOrderResponse.getChannelOrderStatus(), is(ChannelOrderStatus.BOOKED));
        assertThat(channelOrderResponse.getChannelKey(), containsString(WALMART));
        assertThat(channelOrderResponse.getShippingCost(), Matchers.is(SHIPPING_COST));

        Map<String, Object> dbData = channelOrderTestSteps.getChannelOrder(channelOrderResponse.getInvoiceId());
        channelOrderTestSteps.verifyChannelOrder(dbData, WALMART, orderTotalValue);

    }

    @Test
    public void getOrderByChannelOrderRefIdXmlTest() throws JsonProcessingException {

        Faker faker = new Faker();
        String status = "UNRESTRICTED";
        String channelOrderRedId = "OB0" + faker.number().numberBetween(1111, 99999);

        List<Map<String, Object>> dbResult = channelOrderTestSteps.getProductOptionIds(WALMART, status, 2);
        Map<String, Object> singleRowData = dbResult.get(0);
        BigDecimal price = (BigDecimal) singleRowData.get("PRI_PRICE");
        long optionId = ((BigDecimal) singleRowData.get("PRI_ID")).longValue();
        BigDecimal tax = BigDecimal.valueOf(0.00);
        BigDecimal orderTotalValue = price;

        int updateInventory = channelOrderTestSteps.updateInventory(optionId, 20);
        assertThat(updateInventory, is(greaterThan(0)));

        ChannelOrderRequestDto channelOrderRequestDto = channelOrderTestSteps.getOrderBody(price, tax, optionId, WALMART, channelOrderRedId, orderTotalValue);
        channelOrderTestSteps.postSingleOrder(channelOrderRequestDto);

        Response response = given()
                .contentType(ContentType.XML)
                .accept(ContentType.XML)
                .when()
                .request(Method.GET, "v2/channel/order/" + WALMART + "/" + channelOrderRedId);

        ChannelOrderResponseDto channelOrderResponse = getXmlMapper().readValue(response.getBody().asString(), ChannelOrderResponseDto.class);

        assertThat(channelOrderResponse.getInvoiceId(), is(notNullValue()));
        assertThat(channelOrderResponse.getChannelOrderStatus(), is(ChannelOrderStatus.BOOKED));
        assertThat(channelOrderResponse.getChannelKey(), Matchers.containsString(WALMART));
        assertThat(channelOrderResponse.getChannelReferenceId(), Matchers.containsString(channelOrderRedId));

        Map<String, Object> dbData = channelOrderTestSteps.getChannelOrder(channelOrderResponse.getInvoiceId());
        channelOrderTestSteps.verifyChannelOrder(dbData, WALMART, orderTotalValue);

    }

    @Test
    public void getConfigXmlTest() {

        Response config = SerenityRest
                .given()
                .header(HttpHeaders.ACCEPT, ContentType.XML)
                .when()
                .request(Method.GET, "v2/channel/config/" + WALMART)
                .then()
                .extract()
                .response();

        assertThat(config.body().xmlPath().get("ChannelConfigurationDto.channelKey"), is(WALMART));
        assertThat(config.body().xmlPath().get("ChannelConfigurationDto.paymentType"), is("EP_WALMART"));
        assertThat(config.body().xmlPath().get("ChannelConfigurationDto.pricingStrategy"), is("CHANNEL_PRICE"));
    }

    @Test
    public void getConfigTest() {

        JsonPath configJson = SerenityRest
                .given()
                .header(HttpHeaders.ACCEPT, ContentType.JSON)
                .when()
                .request(Method.GET, "v2/channel/config/" + WALMART)
                .then()
                .extract()
                .jsonPath();

        assertThat(configJson.get("channelKey"), is(WALMART));
        assertThat(configJson.get("paymentType"), is("EP_WALMART"));
        assertThat(configJson.get("pricingStrategy"), is("CHANNEL_PRICE"));
    }

}

