package app.ecosynergy.api.integrationtests.controller.withjson;

import app.ecosynergy.api.configs.TestConfigs;
import app.ecosynergy.api.integrationtests.testcontainers.AbstractIntegrationTest;
import app.ecosynergy.api.integrationtests.vo.AccountCredentialsVO;
import app.ecosynergy.api.integrationtests.vo.TokenVO;
import app.ecosynergy.api.integrationtests.vo.UserVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test")
public class AuthControllerJsonTest extends AbstractIntegrationTest {
    private static ObjectMapper objectMapper;

    private static TokenVO tokenVO;
    private static UserVO user;

    @BeforeAll
    public static void setUp(){
        objectMapper = new ObjectMapper();
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        objectMapper.registerModule(new JavaTimeModule());

        user = new UserVO();
    }

    @Test
    @Order(1)
    void testSignup() throws JsonProcessingException {
        mockUser();

        RequestSpecification specification = new RequestSpecBuilder()
                .setBasePath("/auth/signup")
                .setPort(TestConfigs.SERVER_PORT)
                .addFilter(new RequestLoggingFilter(LogDetail.ALL))
                .addFilter(new ResponseLoggingFilter(LogDetail.ALL))
                .build();

        String response = given()
                .basePath("/auth/signup")
                .spec(specification)
                .port(TestConfigs.SERVER_PORT)
                .contentType(TestConfigs.CONTENT_TYPE_JSON)
                .body(user)
                .when()
                .post()
                .then()
                .statusCode(200)
                .extract()
                .body()
                .asString();

        UserVO vo = objectMapper.readValue(response, UserVO.class);

        assertNotNull(vo);
        assertNotNull(vo.getId());
        assertNotNull(vo.getUsername());
        assertNotNull(vo.getFullName());
        assertNotNull(vo.getEmail());
        assertNotNull(vo.getGender());
        assertNotNull(vo.getNationality());
        assertTrue(vo.getEnabled());
        assertTrue(vo.getAccountNonExpired());
        assertTrue(vo.getAccountNonLocked());
    }

    @Test
    @Order(2)
    void testSignin() {
        AccountCredentialsVO credentials = new AccountCredentialsVO(user.getUsername(), user.getPassword());
        System.out.println(user.getUsername() + " " + user.getPassword());
        tokenVO = given()
                .basePath("/auth/signin")
                .port(TestConfigs.SERVER_PORT)
                .contentType(TestConfigs.CONTENT_TYPE_JSON)
                .body(credentials)
                .when()
                .post()
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(TokenVO.class);

        assertNotNull(tokenVO.getAccessToken());
        assertNotNull(tokenVO.getRefreshToken());
    }

    @Test
    @Order(3)
    void testRefreshToken() {
        tokenVO = given()
                .basePath("/auth/refresh")
                .port(TestConfigs.SERVER_PORT)
                .contentType(TestConfigs.CONTENT_TYPE_JSON)
                .pathParam("username", tokenVO.getUsername())
                .header(TestConfigs.HEADER_PARAM_AUTHORIZATION, "Bearer " + tokenVO.getRefreshToken())
                .when()
                .put("{username}")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(TokenVO.class);

        assertNotNull(tokenVO.getAccessToken());
        assertNotNull(tokenVO.getRefreshToken());
    }

    private void mockUser(){
        user.setUsername("testejson");
        user.setFullName("Fulano da Silva");
        user.setEmail("testejson@gmail.com");
        user.setPassword("admin123");
        user.setGender("Male");
        user.setNationality("Brazilian");
        user.setTokens(List.of("csTZKYcmRu-BXd261NiIGw:APA91bGGf_WozjsYIKdnjZdRSBjmSR0XCm5UYTHf8J_fxNDIebxR09HUj4mC19hSF3W3G7-AHRGHkIV1UseR__9EcWGEgb-wsif2cDeI6LG3oCFuLbiqnag"));
    }
}
