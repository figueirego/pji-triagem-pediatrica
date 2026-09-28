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

    @org.springframework.beans.factory.annotation.Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbc;

    private static final ParameterizedTypeReference<Map<String, Object>> MAP_RESPONSE = new ParameterizedTypeReference<>() {
    };

    @Test
    void authenticatedFlowPersistsProfileChildAndAssessment() {
        Map<String, Object> health = body(exchange(HttpMethod.GET, "/health", null, null, HttpStatus.OK));
        assertEquals("ok", health.get("status"));
        exchange(HttpMethod.GET, "/actuator/env", null, null, HttpStatus.UNAUTHORIZED);

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

    @Test
    void allNineSeededQuestionnairesApplyRulesPersistSnapshotsAndEnforceOwnership() {
        exchange(HttpMethod.POST, "/usuarios", Map.of("name", "Regras Projeto", "login", "10000000442",
                "email", "10000000442@example.test", "password", "senha123"), null, HttpStatus.CREATED);
        register("10000000523", "Outro Responsável");
        String token = login("10000000442");
        String otherToken = login("10000000523");
        String birthDate = java.time.LocalDate.now().minusYears(2).toString();
        Map<String, Object> child = data(exchange(HttpMethod.POST, "/criancas",
                Map.of("name", "Nome Original", "birthDate", birthDate), token, HttpStatus.CREATED));
        Object childId = child.get("id");
        List<Map<String, Object>> catalog = dataList(exchange(HttpMethod.GET, "/sintomas", null, token, HttpStatus.OK));
        assertEquals(9, catalog.size());
        for (Map<String, Object> symptom : catalog) {
            List<Map<String, Object>> questions = dataList(exchange(HttpMethod.GET,
                    "/api/sintomas/" + symptom.get("id") + "/perguntas", null, token, HttpStatus.OK));
            assertEquals(4, questions.stream().filter(q -> String.valueOf(q.get("code")).startsWith("UNIVERSAL_")).count());
            List<Map<String, Object>> mild = answersFor(questions, Map.of());
            Map<String, Object> result = submit(childId, symptom.get("id"), mild, token, HttpStatus.CREATED);
            assertEquals("LOW", result.get("finalClassification"), String.valueOf(symptom.get("code")));
            assertEquals("Nome Original", result.get("childName"));
            assertNotNull(result.get("createdAt"));
            assertNotNull(result.get("childAgeAtAssessment"));
            assertEquals("projeto-2.2-educacional", result.get("protocolVersion"));
            assertNotNull(result.get("reason"));
            for (String alert : List.of("UNIVERSAL_PROSTRACAO", "UNIVERSAL_RESPIRACAO", "UNIVERSAL_HIDRATACAO", "UNIVERSAL_MANCHAS")) {
                assertEquals("HIGH", submit(childId, symptom.get("id"), answersFor(questions, Map.of(alert, "YES")), token, HttpStatus.CREATED).get("finalClassification"));
            }
            assertEquals("MOD", submit(childId, symptom.get("id"), answersFor(questions, Map.of("UNIVERSAL_RESPIRACAO", "DUNNO")), token, HttpStatus.CREATED).get("finalClassification"));
            submit(childId, symptom.get("id"), mild.subList(1, mild.size()), token, HttpStatus.BAD_REQUEST);
            submit(childId, symptom.get("id"), mild, otherToken, HttpStatus.NOT_FOUND);
            exchange(HttpMethod.GET, "/avaliacao/" + result.get("assessmentId"), null, otherToken, HttpStatus.FORBIDDEN);
            if ("FEBRE".equals(symptom.get("code"))) {
                assertEquals("MOD", submit(childId, symptom.get("id"), answersFor(questions, Map.of("FEBRE_TEMPERATURA", "ACIMA_39")), token, HttpStatus.CREATED).get("finalClassification"));
                Map<String, Object> infant = data(exchange(HttpMethod.POST, "/criancas", Map.of("name", "Bebê", "birthDate", java.time.LocalDate.now().minusMonths(2).toString()), token, HttpStatus.CREATED));
                assertEquals("HIGH", submit(infant.get("id"), symptom.get("id"), mild, token, HttpStatus.CREATED).get("finalClassification"));
            }
            exchange(HttpMethod.PUT, "/criancas/" + childId, Map.of("name", "Nome Alterado", "birthDate", java.time.LocalDate.now().minusYears(3).toString()), token, HttpStatus.OK);
            Map<String, Object> history = data(exchange(HttpMethod.GET, "/avaliacao/" + result.get("assessmentId"), null, token, HttpStatus.OK));
            assertEquals("Nome Original", history.get("childName"));
            assertEquals(result.get("childAgeAtAssessment"), history.get("childAgeAtAssessment"));
            Map<String, Object> historyList = data(exchange(HttpMethod.GET, "/avaliacao?childId=" + childId, null, token, HttpStatus.OK));
            Map<String, Object> savedItem = castList(historyList.get("assessments")).stream()
                    .filter(item -> result.get("assessmentId").equals(item.get("id"))).findFirst().orElseThrow();
            assertEquals("Nome Original", savedItem.get("childName"));
            assertEquals(result.get("childAgeAtAssessment"), savedItem.get("childAgeAtAssessment"));
            assertEquals(result.get("reason"), savedItem.get("reason"));
            assertEquals(result.get("protocolVersion"), savedItem.get("protocolVersion"));
            exchange(HttpMethod.PUT, "/criancas/" + childId, Map.of("name", "Nome Original", "birthDate", birthDate), token, HttpStatus.OK);
        }
        // A child registered earlier can age out: the evaluation must recheck current age.
        jdbc.update("UPDATE crianca SET data_nascimento = ? WHERE id = ?",
                java.sql.Date.valueOf(java.time.LocalDate.now().minusYears(13)), childId);
        Object symptomId = catalog.get(0).get("id");
        List<Map<String, Object>> questions = dataList(exchange(HttpMethod.GET,
                "/api/sintomas/" + symptomId + "/perguntas", null, token, HttpStatus.OK));
        submit(childId, symptomId, answersFor(questions, Map.of()), token, HttpStatus.BAD_REQUEST);
    }

    @Test
    void feverAgeIsDerivedFromBirthDateAndLegacyAnswersCannotOverrideIt() {
        register("10000000604", "Idade Automática");
        String token = login("10000000604");
        Map<String, Object> symptom = dataList(exchange(HttpMethod.GET, "/sintomas", null, token, HttpStatus.OK))
                .stream().filter(item -> "FEBRE".equals(item.get("code"))).findFirst().orElseThrow();
        Object symptomId = symptom.get("id");
        List<Map<String, Object>> questions = dataList(exchange(HttpMethod.GET,
                "/api/sintomas/" + symptomId + "/perguntas", null, token, HttpStatus.OK));
        assertFalse(questions.stream().anyMatch(q -> "FEBRE_BEBE_MENOR_3_MESES".equals(q.get("code"))));
        Long legacyQuestionId = jdbc.queryForObject("SELECT id FROM pergunta WHERE codigo = 'FEBRE_BEBE_MENOR_3_MESES'", Long.class);
        for (java.time.LocalDate dob : List.of(java.time.LocalDate.now().minusMonths(3).plusDays(1),
                java.time.LocalDate.now().minusMonths(3), java.time.LocalDate.now().minusYears(2))) {
            Map<String, Object> child = data(exchange(HttpMethod.POST, "/criancas",
                    Map.of("name", "Criança Idade", "birthDate", dob.toString()), token, HttpStatus.CREATED));
            Object childId = child.get("id");
            boolean infant = dob.plusMonths(3).isAfter(java.time.LocalDate.now());
            String expected = infant ? "HIGH" : "LOW";
            List<Map<String, Object>> answers = answersFor(questions, Map.of());
            Map<String, Object> omittedResult = submit(childId, symptomId, answers, token, HttpStatus.CREATED);
            assertEquals(expected, omittedResult.get("finalClassification"));
            for (String legacyValue : List.of("YES", "NO", "DUNNO")) {
                List<Map<String, Object>> legacyAnswers = java.util.stream.Stream.concat(answers.stream(),
                        java.util.stream.Stream.of(Map.<String,Object>of("questionId", legacyQuestionId, "answer", legacyValue))).toList();
                Map<String, Object> result = submit(childId, symptomId, legacyAnswers, token, HttpStatus.CREATED);
                assertEquals(expected, result.get("finalClassification"));
                assertEquals(omittedResult.get("totalScore"), result.get("totalScore"));
                String savedAnswer = jdbc.queryForObject("SELECT a->>'answer' FROM avaliacao_sintoma av, jsonb_array_elements(av.respostas->'answers') a WHERE av.avaliacao_id = ? AND a->>'questionCode' = 'FEBRE_BEBE_MENOR_3_MESES'", String.class, result.get("assessmentId"));
                assertEquals(infant ? "YES" : "NO", savedAnswer);
            }
            submit(childId, symptomId, answers.subList(1, answers.size()), token, HttpStatus.BAD_REQUEST);
        }
    }

    @Test
    void metadataDerivedQuestionsWorkForAnyCodeAndConfiguredAgeRange() {
        register("10000000795", "Metadados Idade");
        String token = login("10000000795");
        Object symptomId = dataList(exchange(HttpMethod.GET, "/sintomas", null, token, HttpStatus.OK)).stream()
                .filter(item -> "TOSSE".equals(item.get("code"))).findFirst().orElseThrow().get("id");
        // Synthetic configuration verifies the mechanism; it is never part of production clinical seeds.
        Long questionId = jdbc.queryForObject("INSERT INTO pergunta (sintoma_id,codigo,texto,tipo,ordem,idade_derivada,idade_max_valor,idade_max_unidade,idade_max_inclusiva) VALUES (?,'METADATA_FIXTURE','Condição sintética para teste','YESNO',200,TRUE,6,'MONTHS',FALSE) RETURNING id", Long.class, symptomId);
        jdbc.update("INSERT INTO peso_yesno (pergunta_id,score_yes,score_no,score_dunno,red_flag_on_yes,red_flag_on_no) VALUES (?,0,0,3,TRUE,FALSE)", questionId);
        try {
            List<Map<String, Object>> questions = dataList(exchange(HttpMethod.GET,
                    "/api/sintomas/" + symptomId + "/perguntas", null, token, HttpStatus.OK));
            assertFalse(questions.stream().anyMatch(q -> "METADATA_FIXTURE".equals(q.get("code"))));
            List<Map<String, Object>> answers = java.util.stream.Stream.concat(answersFor(questions, Map.of()).stream(),
                    java.util.stream.Stream.of(Map.<String,Object>of("questionId", questionId, "answer", "DUNNO"))).toList();
            assertMetadataRisk(token, symptomId, answers, java.time.LocalDate.now().minusMonths(5), "HIGH");
            assertMetadataRisk(token, symptomId, answers, java.time.LocalDate.now().minusMonths(6), "LOW");
            jdbc.update("UPDATE pergunta SET idade_max_valor=2,idade_max_unidade='YEARS',idade_max_inclusiva=TRUE WHERE id=?", questionId);
            assertMetadataRisk(token, symptomId, answers, java.time.LocalDate.now().minusYears(2), "HIGH");
            assertMetadataRisk(token, symptomId, answers, java.time.LocalDate.now().minusYears(2).minusDays(1), "LOW");
            jdbc.update("UPDATE pergunta SET idade_min_valor=6,idade_min_unidade='MONTHS',idade_min_inclusiva=TRUE,idade_max_inclusiva=FALSE WHERE id=?", questionId);
            assertMetadataRisk(token, symptomId, answers, java.time.LocalDate.now().minusMonths(5), "LOW");
            assertMetadataRisk(token, symptomId, answers, java.time.LocalDate.now().minusMonths(6), "HIGH");
            assertMetadataRisk(token, symptomId, answers, java.time.LocalDate.now().minusYears(2), "LOW");
        } finally {
            jdbc.update("DELETE FROM pergunta WHERE id=?", questionId);
        }
    }

    private void assertMetadataRisk(String token, Object symptomId, List<Map<String,Object>> answers,
                                    java.time.LocalDate birthDate, String expected) {
        Object childId = data(exchange(HttpMethod.POST, "/criancas", Map.of("name", "Teste Metadados",
                "birthDate", birthDate.toString()), token, HttpStatus.CREATED)).get("id");
        assertEquals(expected, submit(childId, symptomId, answers, token, HttpStatus.CREATED).get("finalClassification"));
    }

    private Map<String, Object> submit(Object child, Object symptom, List<Map<String, Object>> answers, String token, HttpStatus expected) {
        var response = exchange(HttpMethod.POST, "/avaliacao", Map.of("childId", child,
                "symptoms", List.of(Map.of("symptomId", symptom, "answers", answers))), token, expected);
        return data(response);
    }

    private List<Map<String, Object>> answersFor(List<Map<String, Object>> questions, Map<String, String> overrides) {
        return questions.stream().map(q -> {
            String code = String.valueOf(q.get("code"));
            String value = overrides.get(code);
            if ("OPTIONS".equals(q.get("type"))) {
                List<Map<String, Object>> options = castList(q.get("options"));
                Object optionId = value == null ? options.get(0).get("id") : options.stream()
                        .filter(o -> value.equals(o.get("code"))).findFirst().orElseThrow().get("id");
                return Map.<String, Object>of("questionId", q.get("id"), "optionId", optionId);
            }
            return Map.<String, Object>of("questionId", q.get("id"), "answer",
                    value == null ? ("FEBRE_BOM_ESTADO".equals(code) ? "YES" : "NO") : value);
        }).toList();
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
