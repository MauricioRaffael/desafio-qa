package api;

import io.restassured.http.ContentType;
import io.restassured.response.Response;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;

public class ApiClient {
        private final String baseUrl;
        public ApiClient(String baseUrl) { this.baseUrl = baseUrl; }

        public Response createUser(String user, String pass) {
                return given().baseUri(baseUrl)
                        .contentType(ContentType.JSON)
                        .body(Map.of("userName", user, "password", pass))
                        .post("/Account/v1/User");
        }

        public Response generateToken(String user, String pass) {
                return given().baseUri(baseUrl)
                        .contentType(ContentType.JSON)
                        .body(Map.of("userName", user, "password", pass))
                        .post("/Account/v1/GenerateToken");
        }

        public Response authorize(String user, String pass) {
                return given().baseUri(baseUrl)
                        .contentType(ContentType.JSON)
                        .body(Map.of("userName", user, "password", pass))
                        .post("/Account/v1/Authorized");
        }

        public Response listBooks() {
                return given().baseUri(baseUrl).get("/BookStore/v1/Books");
        }

        public Response addBooks(String token, String userId, List<String> isbns) {
                var payload = Map.of(
                        "userId", userId,
                        "collectionOfIsbns", isbns.stream().map(i -> Map.of("isbn", i)).toList()
                );
                return given().baseUri(baseUrl)
                        .contentType(ContentType.JSON)
                        .header("Authorization", "Bearer " + token)
                        .body(payload)
                        .post("/BookStore/v1/Books");
        }

        public Response getUser(String token, String userId) {
                return given().baseUri(baseUrl)
                        .header("Authorization", "Bearer " + token)
                        .get("/Account/v1/User/" + userId);
        }
}
