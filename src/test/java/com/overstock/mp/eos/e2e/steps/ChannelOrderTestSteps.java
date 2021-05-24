
package com.overstock.mp.eos.e2e.steps;

        import com.fasterxml.jackson.core.JsonProcessingException;
        import com.fasterxml.jackson.databind.ObjectMapper;
        import com.overstock.arch.moneyutil.MoneyUtil;
        import com.overstock.mp.eos.common.dto.*;
        import com.overstock.mp.eos.common.enums.ChannelOrderStatus;
        import io.restassured.http.ContentType;
        import io.restassured.http.Method;
        import io.restassured.response.Response;
        import net.thucydides.core.annotations.Step;
        import org.apache.http.HttpHeaders;
        import org.hamcrest.Matchers;
        import org.joda.time.DateTime;
        import org.springframework.jdbc.core.JdbcTemplate;
        import org.testcontainers.shaded.com.google.common.collect.Lists;

        import javax.sql.DataSource;
        import java.math.BigDecimal;
        import java.util.List;
        import java.util.Map;

        import static com.overstock.mp.eos.common.constants.TestConstants.*;
        import static com.overstock.mp.eos.integration.common.ITTestConstants.getObjectMapper;
        import static io.restassured.RestAssured.given;
        import static org.hamcrest.CoreMatchers.is;
        import static org.hamcrest.CoreMatchers.notNullValue;
        import static org.hamcrest.MatcherAssert.assertThat;
        import static org.hamcrest.Matchers.containsString;
        import static org.hamcrest.Matchers.greaterThan;

public class ChannelOrderTestSteps {

    private Response response;

    private DataSource dataSource;

    @Step("Get product option ids for channel")
    public List<Map<String, Object>> getProductOptionIds(String channelName, String status, int count) {
        String sql = "select PRI_ID, PRI_PRICE, pri_pro_od From dd_pro_info where pri_pro_od in " +
                "(select pro_id from marketpartner.channel_product where channel_key='" + channelName + "' and status='" + status + "') " +
                "ORDER BY pri_pro_od DESC FETCH FIRST " + count + " ROWS ONLY";

        JdbcTemplate query = new JdbcTemplate(dataSource);

        return query.queryForList(sql);
    }

    @Step("update Inventory for product")
    public int updateInventory(long productOptionId, int quantity) {
        String sql = "update INVENTORY.LOCATION_INVENTORY set qty_on_hand = " + quantity + " where product_option_id in (" + productOptionId + ")";

        JdbcTemplate query = new JdbcTemplate(dataSource);
        return query.update(sql);
    }

    @Step("get channel order body")
    public ChannelOrderRequestDto getOrderBody(BigDecimal price, BigDecimal tax, long optionId, String channelKey, String channelRefId, BigDecimal orderTotal) throws JsonProcessingException {

        BigDecimal agentCommission = new BigDecimal(0);
        ChannelOrderLineDto CHANNEL_ORDER_LINE_DTO = ChannelOrderLineDto.builder()
                .price(price)
                .tax(tax)
                .quantity(LINE1_QUANTITY)
                .optionId(optionId)
                .channelLineItemId(LINE1_CHANNEL_LINE_ITEM_ID)
                .invoiceLineId(null)
                .discount(LINE1_DISCOUNT)
                .shippingAmount(LINE1_SHIPPING_AMOUNT)
                .shippingTax(LINE1_SHIPPING_TAX)
                .shippingMethod(LINE1_SHIPPING_METHOD)
                .channelCommission(null)
                .agentCalculatedCommission(null)
                .channelShippingCommission(null)
                .agentCalculatedShippingCommission(agentCommission)
                .agentCalculatedSecondTierCommission(null)
                .channelOrderRefId(null)
                .build();

        ChannelOrderRequestDto channelOrderRequestDto = ChannelOrderRequestDto.builder()
                .channelKey(channelKey)
                .channelReferenceId(channelRefId)
                .customerId(CUSTOMER_ID)
                .channelOrderStatus(null)
                .currencyCode(MoneyUtil.USD.getCurrencyCode())
                .fxRate(BigDecimal.ONE)
                .channelCreationDate(DateTime.now().minusMinutes(10).toDate())
                .customerFirstName(CUSTOMER_FIRST_NAME)
                .customerLastName(CUSTOMER_LAST_NAME)
                .orderTotal(orderTotal)
                .tax(ORDER_TAX)
                .billingAddress(AddressDto.builder().email(CONTACT_EMAIL)
                        .phoneNumber(CONTACT_PHONE)
                        .firstName(CUSTOMER_FIRST_NAME)
                        .lastName(CUSTOMER_LAST_NAME)
                        .line1(ADDRESS_LINE1)
                        .line2(ADDRESS_LINE2)
                        .line3("Suite 201")
                        .city(ADDRESS_CITY)
                        .state(ADDRESS_STATE)
                        .postalCode(ADDRESS_POSTAL_CODE)
                        .countryCode("US").build())
                .freightForwardAddress(AddressDto.builder().email(CONTACT_EMAIL)
                        .phoneNumber(CONTACT_PHONE)
                        .firstName(CUSTOMER_FIRST_NAME)
                        .lastName(CUSTOMER_LAST_NAME)
                        .line1(ADDRESS_LINE1)
                        .line2(ADDRESS_LINE2)
                        .line3("Suite 201")
                        .city(ADDRESS_CITY)
                        .state(ADDRESS_STATE)
                        .postalCode(ADDRESS_POSTAL_CODE)
                        .countryCode("US").build())
                .shipping(ShippingDto.builder()
                        .contact(ShippingContactDto.builder()
                                .phoneNumber(CONTACT_PHONE)
                                .email(CONTACT_EMAIL)
                                .build())
                        .address(ShippingAddressDto.builder()
                                .name(ADDRESS_NAME)
                                .line1(ADDRESS_LINE1)
                                .line2(ADDRESS_LINE2)
                                .city(ADDRESS_CITY)
                                .state(ADDRESS_STATE)
                                .postalCode(ADDRESS_POSTAL_CODE)
                                .build())
                        .build())
                .shippingCost(SHIPPING_COST)
                .lines(Lists.newArrayList(CHANNEL_ORDER_LINE_DTO))
                .build();

        return channelOrderRequestDto;
    }

    @Step("post single order")
    public ChannelOrderResponseDto postSingleOrder(ChannelOrderRequestDto channelOrderRequestDto) throws JsonProcessingException {

        ObjectMapper mapper = getObjectMapper();
        String body = mapper.writeValueAsString(channelOrderRequestDto);

        ChannelOrderResponseDto channelOrderResponseDto = given()
                .header(HttpHeaders.ACCEPT,ContentType.JSON)
                .header(HttpHeaders.CONTENT_TYPE, ContentType.JSON)
                .when().body(body)
                .request(Method.POST, "/v2/channel/order")
                .then()
                .extract()
                .response().as(ChannelOrderResponseDto.class);

        return channelOrderResponseDto;
    }

    @Step("get single order by ChannelRedId")
    public ChannelOrderResponseDto getSingleOrderByChannelOrderRefId(String ChannelKey, String channelOrderRedId) {

        ChannelOrderResponseDto channelOrderResponse = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when()
                .request(Method.GET, "v2/channel/order/" + ChannelKey + "/" + channelOrderRedId)
                .as(ChannelOrderResponseDto.class);

        return channelOrderResponse;
    }

    @Step("post single Restricted order")
    public ChannelOrderResponseDto postSingleRestrictedOrder(ChannelOrderRequestDto channelOrderRequestDto) throws JsonProcessingException {

        ObjectMapper mapper = getObjectMapper();
        String body = mapper.writeValueAsString(channelOrderRequestDto);

        ChannelOrderResponseDto channelOrderResponse = given()
                .header(HttpHeaders.CONTENT_TYPE, ContentType.JSON)
                .header(HttpHeaders.ACCEPT, ContentType.JSON)
                .when().body(body)
                .request(Method.POST, "v2/channel/order").as(ChannelOrderResponseDto.class);

        return channelOrderResponse;
    }

    @Step("verify Asserts of place single order")
    public void verifyAssertPostSingleOrder(ChannelOrderResponseDto channelOrderResponse, String channelKey) {

        assertThat(channelOrderResponse.getInvoiceId(), Matchers.is(greaterThan(0L)));
        assertThat(channelOrderResponse.getChannelOrderStatus(), is(ChannelOrderStatus.BOOKED));
        assertThat(channelOrderResponse.getChannelKey(), containsString(channelKey));
        assertThat(channelOrderResponse.getShippingCost(), Matchers.is(SHIPPING_COST));

    }

    @Step("get channel order by invoice id")
    public Map<String, Object> getChannelOrder(long invoiceId) {

        String sql = "Select * from MARKETPARTNER.channel_order where invoice_id=" + invoiceId + "";

        JdbcTemplate query = new JdbcTemplate(dataSource);
        List<Map<String, Object>> result = query.queryForList(sql);
        assertThat(result.size(), is(greaterThan(0)));

        return !result.isEmpty() ? result.get(0) : null;
    }

    @Step("verify channel order")
    public void verifyChannelOrder(Map<String, Object> order, String channelKey, Object orderTotalValue) {

        assertThat(order, notNullValue());
        assertThat(order.get("CHANNEL_KEY"), Matchers.is(channelKey));
        assertThat(order.get("STATUS"), Matchers.is("BOOKED"));
        assertThat(order.get("ORDER_TOTAL"), Matchers.is(orderTotalValue));

    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Step("get channel order body")
    public ChannelOrderRequestDto getOrderBodyGlobal(BigDecimal price, BigDecimal tax,
                                                     long optionId, String channelKey,
                                                     String channelRefId,
                                                     BigDecimal orderTotal, long invoiceId) throws JsonProcessingException {

        BigDecimal agentCommission = new BigDecimal(0);
        ChannelOrderLineDto CHANNEL_ORDER_LINE_DTO = ChannelOrderLineDto.builder()
                .price(price)
                .tax(tax)
                .quantity(LINE1_QUANTITY)
                .optionId(optionId)
                .channelLineItemId(LINE1_CHANNEL_LINE_ITEM_ID)
                .invoiceLineId(null)
                .discount(LINE1_DISCOUNT)
                .shippingAmount(LINE1_SHIPPING_AMOUNT)
                .shippingTax(LINE1_SHIPPING_TAX)
                .shippingMethod(LINE1_SHIPPING_METHOD)
                .channelCommission(null)
                .agentCalculatedCommission(null)
                .channelShippingCommission(null)
                .agentCalculatedShippingCommission(agentCommission)
                .agentCalculatedSecondTierCommission(null)
                .channelOrderRefId(null)
                .build();

        ChannelOrderRequestDto channelOrderRequestDto = ChannelOrderRequestDto.builder()
                .channelKey(channelKey)
                .channelReferenceId(channelRefId)
                .invoiceId(invoiceId)
                .customerId(169898330L)
                .channelOrderStatus(null)
                .currencyCode("CAD")
                .fxRate(new BigDecimal(2))
                .channelCreationDate(DateTime.now().minusMinutes(10).toDate())
                .customerFirstName(CUSTOMER_FIRST_NAME)
                .customerLastName(CUSTOMER_LAST_NAME)
                .orderTotal(orderTotal)
                .tax(ORDER_TAX)
                .billingAddress(AddressDto.builder().email(CONTACT_EMAIL)
                        .phoneNumber(CONTACT_PHONE)
                        .firstName(CUSTOMER_FIRST_NAME)
                        .lastName(CUSTOMER_LAST_NAME)
                        .line1("1 Main St")
                        .line2("Building A")
                        .line3("Suite 201")
                        .city("Cerritos")
                        .state("CA")
                        .postalCode("90001")
                        .countryCode("US").build())
                .freightForwardAddress(AddressDto.builder().email(CONTACT_EMAIL)
                        .phoneNumber(CONTACT_PHONE)
                        .firstName(CUSTOMER_FIRST_NAME)
                        .lastName(CUSTOMER_LAST_NAME)
                        .line1("2 Main St")
                        .line2("Building B")
                        .line3("Suite 201")
                        .city("New Westminster")
                        .state("BC")
                        .postalCode("V5M2G7")
                        .countryCode("CA").build())
                .shipping(ShippingDto.builder()
                        .contact(ShippingContactDto.builder()
                                .phoneNumber(CONTACT_PHONE)
                                .email(CONTACT_EMAIL)
                                .build())
                        .address(ShippingAddressDto.builder()
                                .name(ADDRESS_NAME)
                                .line1(ADDRESS_LINE1)
                                .line2(ADDRESS_LINE2)
                                .city("New Westminister")
                                .state("BC")
                                .postalCode("V5M2G7")
                                .countryCode("CA")
                                .build())
                        .build())
                .shippingCost(SHIPPING_COST)
                .lines(Lists.newArrayList(CHANNEL_ORDER_LINE_DTO))
                .build();

        return channelOrderRequestDto;
    }


}
