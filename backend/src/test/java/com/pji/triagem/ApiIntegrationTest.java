package com.pji.triagem;

import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiIntegrationTest extends AbstractIntegrationTest {

    private static final ParameterizedTypeReference<Map<String, Object>> MAP_RESPONSE = new ParameterizedTypeReference<>() {
    };

    @Test
    void authenticatedFlowPersistsProfileChildAndAssessment() {
        Map<String, Object> health = body(exchange(HttpMethod.GET, "/health", null, null, HttpStatus.OK));
        assertEquals("ok", health.get("status"));

        register("52998224725", "Camila Integra");
        String token = login("52998224725");

        Map<String, Object> me = data(exchange(HttpMethod.GET, "/auth/me", null, token, HttpStatus.OK));
        assertEquals("52998224725", me.get("login"));

        Map<String, Object> updatedProfile = data(exchange(
                HttpMethod.PATCH,
                "/api/usuarios/me",
                Map.of("name", "Camila Atualizada", "email", "camila.atualizada@example.test"),
                token,
                HttpStatus.OK
        ));
        assertEquals("Camila Atualizada", updatedProfile.get("name"));
        assertEquals("camila.atualizada@example.test", updatedProfile.get("email"));

        Map<String, Object> child = data(exchange(
                HttpMethod.POST,
                "/api/criancas",
                Map.of(
                        "avatarEmoji", "🌸",
                        "birthDate", "2021-05-10",
                        "cpf", "",
                        "name", "Maria Integra",
                        "weightKg", BigDecimal.valueOf(17.5)
                ),
                token,
                HttpStatus.CREATED
        ));
        assertNotNull(child.get("id"));

        List<Map<String, Object>> symptoms = dataList(exchange(HttpMethod.GET, "/api/sintomas", null, token, HttpStatus.OK));
        assertFalse(symptoms.isEmpty());
        Map<String, Object> symptom = symptoms.get(0);

        List<Map<String, Object>> questions = dataList(exchange(
                HttpMethod.GET,
                "/api/sintomas/" + symptom.get("id") + "/perguntas",
                null,
                token,
                HttpStatus.OK
        ));
        assertFalse(questions.isEmpty());

        List<Map<String, Object>> answers = questions.stream()
                .map(question -> buildAnswer(question))
                .toList();

        Map<String, Object> assessment = data(exchange(
                HttpMethod.POST,
                "/api/avaliacoes",
                Map.of(
                        "childId", child.get("id"),
                        "symptoms", List.of(Map.of("symptomId", symptom.get("id"), "answers", answers))
                ),
                token,
                HttpStatus.CREATED
        ));
        assertNotNull(assessment.get("assessmentId"));

        Map<String, Object> stats = data(exchange(HttpMethod.GET, "/api/avaliacoes/stats", null, token, HttpStatus.OK));
        assertEquals(1, ((Number) stats.get("totalAssessments")).intValue());
        assertEquals(1, ((Number) stats.get("total")).intValue());
    }

    @Test
    void registerReturnsTokenAndRejectsDuplicateEmail() {
        Map<String, Object> created = data(exchange(
                HttpMethod.POST,
                "/auth/register",
                Map.of("email", "duplicado@example.test", "login", "10000000019", "name", "Usuário Um", "password", "senha123"),
                null,
                HttpStatus.CREATED
        ));
        Map<String, Object> token = castMap(created.get("token"));
        Map<String, Object> user = castMap(created.get("usuario"));
        assertNotNull(token.get("accessToken"));
        assertEquals("duplicado@example.test", user.get("email"));

        Map<String, Object> duplicate = body(exchange(
                HttpMethod.POST,
                "/auth/register",
                Map.of("email", "duplicado@example.test", "login", "10000000108", "name", "Usuário Dois", "password", "senha123"),
                null,
                HttpStatus.CONFLICT
        ));
        assertEquals(409, ((Number) duplicate.get("status")).intValue());
        assertTrue(String.valueOf(duplicate.get("mensagem")).contains("email"));
    }

    @Test
    void authAndChildEndpointsValidateUnauthorizedCrudIsolationAndAvatar() {
        register("10000000280", "Cuidador A", "cuidador.a@example.test");
        register("10000000361", "Cuidador B", "cuidador.b@example.test");
        String tokenA = login("10000000280");
        String tokenB = login("10000000361");

        exchange(HttpMethod.POST, "/auth/login", Map.of("login", "10000000280", "password", "errada"), null, HttpStatus.UNAUTHORIZED);
        exchange(HttpMethod.GET, "/api/criancas", null, null, HttpStatus.UNAUTHORIZED);
        Map<String, Object> invalidToken = body(exchange(HttpMethod.GET, "/api/criancas", null, "token-invalido", HttpStatus.UNAUTHORIZED));
        assertEquals(401, ((Number) invalidToken.get("status")).intValue());
        assertEquals("Token inválido ou expirado", invalidToken.get("mensagem"));

        exchange(
                HttpMethod.POST,
                "/api/criancas",
                Map.of("avatarEmoji", "ZZ", "birthDate", "2021-05-10", "cpf", "", "name", "Avatar Inválido"),
                tokenA,
                HttpStatus.BAD_REQUEST
        );

        Map<String, Object> created = data(exchange(
                HttpMethod.POST,
                "/api/criancas",
                Map.of("avatarEmoji", "🐻", "birthDate", "2021-05-10", "cpf", "", "name", "Ana CRUD", "weightKg", 18),
                tokenA,
                HttpStatus.CREATED
        ));
        Long childId = ((Number) created.get("id")).longValue();

        List<Map<String, Object>> children = dataList(exchange(HttpMethod.GET, "/api/criancas", null, tokenA, HttpStatus.OK));
        assertTrue(children.stream().anyMatch(child -> childId.equals(((Number) child.get("id")).longValue())));

        Map<String, Object> updated = data(exchange(
                HttpMethod.PUT,
                "/api/criancas/" + childId,
                Map.of("avatarEmoji", "🦊", "birthDate", "2020-05-10", "cpf", "", "name", "Ana Atualizada", "weightKg", 19),
                tokenA,
                HttpStatus.OK
        ));
        assertEquals("Ana Atualizada", updated.get("name"));
        assertEquals("🦊", updated.get("avatarEmoji"));

        exchange(HttpMethod.GET, "/api/criancas/" + childId, null, tokenB, HttpStatus.NOT_FOUND);
        exchange(HttpMethod.DELETE, "/api/criancas/" + childId, null, tokenA, HttpStatus.OK);
        exchange(HttpMethod.GET, "/api/criancas/" + childId, null, tokenA, HttpStatus.NOT_FOUND);
    }

    private void register(String login, String name) {
        register(login, name, login + "@example.test");
    }

    private void register(String login, String name, String email) {
        exchange(
                HttpMethod.POST,
                "/auth/register/user",
                Map.of("email", email, "login", login, "name", name, "password", "senha123"),
                null,
                HttpStatus.CREATED
        );
    }

    private String login(String login) {
        Map<String, Object> token = data(exchange(
                HttpMethod.POST,
                "/auth/login",
                Map.of("login", login, "password", "senha123"),
                null,
                HttpStatus.OK
        ));
        return String.valueOf(token.get("accessToken"));
    }

    private ResponseEntity<Map<String, Object>> exchange(
            HttpMethod method,
            String path,
            Object body,
            String token,
            HttpStatus expectedStatus
    ) {
        HttpHeaders headers = new HttpHeaders();
        if (token != null) {
            headers.setBearerAuth(token);
        }
        ResponseEntity<Map<String, Object>> response = rest.exchange(
                "http://localhost:" + port + path,
                method,
                new HttpEntity<>(body, headers),
                MAP_RESPONSE
        );
        assertEquals(expectedStatus, response.getStatusCode(), () -> method + " " + path + " returned " + response.getBody());
        return response;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> data(ResponseEntity<Map<String, Object>> response) {
        return (Map<String, Object>) response.getBody().get("data");
    }

    private Map<String, Object> body(ResponseEntity<Map<String, Object>> response) {
        return response.getBody();
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> dataList(ResponseEntity<Map<String, Object>> response) {
        return (List<Map<String, Object>>) response.getBody().get("data");
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> castList(Object value) {
        return (List<Map<String, Object>>) value;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> castMap(Object value) {
        return (Map<String, Object>) value;
    }

    private Map<String, Object> buildAnswer(Map<String, Object> question) {
        if ("OPTIONS".equals(question.get("type"))) {
            List<Map<String, Object>> options = castList(question.get("options"));
            return Map.of("questionId", question.get("id"), "optionId", options.get(0).get("id"));
        }
        return Map.of("questionId", question.get("id"), "answer", "NO");
    }
}
