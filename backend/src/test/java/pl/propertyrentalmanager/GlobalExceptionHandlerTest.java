package pl.propertyrentalmanager;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Should handle DTO validation errors and return 400 Bad Request with field errors")
    void shouldReturnBadRequestOnValidationError() throws Exception {
        String invalidJson = "{\"email\":\"invalid-email\",\"name\":\"\"}";

        mockMvc.perform(post("/api/test-fixture/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("VALIDATION_ERROR")))
                .andExpect(jsonPath("$.fieldErrors", hasSize(2)))
                .andExpect(jsonPath("$.requestId", notNullValue()));
    }

    @Test
    @DisplayName("Should handle ResourceNotFoundException and return 404 Not Found")
    void shouldReturnNotFoundResponse() throws Exception {
        mockMvc.perform(get("/api/test-fixture/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", is("RESOURCE_NOT_FOUND")))
                .andExpect(jsonPath("$.message", containsString("Test resource was not found")));
    }

    @Test
    @DisplayName("Should handle ResourceConflictException and return 409 Conflict")
    void shouldReturnConflictResponse() throws Exception {
        mockMvc.perform(get("/api/test-fixture/conflict"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code", is("CONFLICT")));
    }

    @Test
    @DisplayName("Should handle malformed JSON and return 400 Bad Request")
    void shouldReturnBadRequestOnMalformedJson() throws Exception {
        mockMvc.perform(post("/api/test-fixture/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{broken-json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("MALFORMED_REQUEST")));
    }

    @Test
    @DisplayName("Should handle unexpected exceptions and return 500 without stack trace leak")
    void shouldReturnInternalErrorResponse() throws Exception {
        mockMvc.perform(get("/api/test-fixture/unexpected"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code", is("INTERNAL_ERROR")))
                .andExpect(jsonPath("$.message", is("An unexpected internal server error occurred")));
    }
}
