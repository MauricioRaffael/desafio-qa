package api;

import io.cucumber.java.en.*;
import io.restassured.response.Response;

import java.util.*;
import static org.assertj.core.api.Assertions.assertThat;

public class ApiSteps {

    private ApiClient client;
    private String userName, password, userId, token;
    private List<String> allIsbns;

    @Given("que a baseUrl da API é {string}")
    public void setBase(String base) {
        client = new ApiClient(base);
    }

    @When("eu crio um usuário aleatório válido")
    public void createUser() {
        userName = "user_" + System.currentTimeMillis();
        password = "P@ssw0rd" + new Random().nextInt(9999);

        Response r = client.createUser(userName, password);
        assertThat(r.statusCode()).isBetween(200, 201);
        userId = r.jsonPath().getString("userID");
        assertThat(userId).isNotBlank();

        System.out.println(">>> DEMOQA USERNAME: " + userName);
        System.out.println(">>> DEMOQA PASSWORD: " + password);
    }

    @When("eu gero um token para esse usuário")
    public void generateToken() {
        int maxAttempts = 4;
        long sleepMs = 800;
        Response r = null;
        for (int i = 1; i <= maxAttempts; i++) {
            r = client.generateToken(userName, password);
            int code = r.statusCode();
            if (code == 200) break;

            System.out.printf(">>> GenerateToken tentativa %d falhou com %d. Body: %s%n",
                    i, code, r.getBody().asString());

            if (code == 502 || code == 503 || code == 504 || code == 429 || code == 408) {
                try { Thread.sleep(sleepMs); } catch (InterruptedException ignored) {}
                sleepMs *= 2;
                continue;
            }
            break;
        }

        org.assertj.core.api.Assertions.assertThat(r.statusCode())
                .withFailMessage("GenerateToken não retornou 200. Último status: %s | Body: %s",
                        r.statusCode(), r.getBody().asString())
                .isEqualTo(200);

        token = r.jsonPath().getString("token");
        org.assertj.core.api.Assertions.assertThat(token).isNotBlank();
        System.out.println(">>> DEMOQA TOKEN: " + token);
    }

    @When("eu verifico que o usuário está autorizado")
    public void authorize() {
        Response r = client.authorize(userName, password);
        assertThat(r.statusCode()).isEqualTo(200);
        assertThat(r.getBody().asString()).contains("true");
    }

    @When("eu listo os livros disponíveis")
    public void listBooks() {
        Response r = client.listBooks();
        assertThat(r.statusCode()).isEqualTo(200);
        allIsbns = r.jsonPath().getList("books.isbn");
        assertThat(allIsbns).isNotEmpty();
    }

    @When("eu adiciono {int} livros ao usuário")
    public void addBooks(int qtd) {
        var pick = allIsbns.subList(0, Math.min(qtd, allIsbns.size()));
        Response r = client.addBooks(token, userId, pick);
        assertThat(r.statusCode()).isIn(200, 201);
    }

    @Then("eu vejo o usuário com exatamente {int} livros")
    public void validateBooks(int qtd) {
        Response r = client.getUser(token, userId);
        assertThat(r.statusCode()).isEqualTo(200);
        var userIsbns = r.jsonPath().getList("books.isbn");
        assertThat(userIsbns).hasSize(qtd);
    }
}
