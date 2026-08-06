package pl.propertyrentalmanager;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import pl.propertyrentalmanager.common.web.RequestIdFilter;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class RequestIdFilterTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Should generate X-Request-ID header when missing in request")
    void shouldGenerateRequestIdHeaderWhenMissing() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(header().exists(RequestIdFilter.HEADER_NAME))
                .andExpect(header().string(RequestIdFilter.HEADER_NAME, notNullValue()));
    }

    @Test
    @DisplayName("Should preserve custom valid X-Request-ID header from request")
    void shouldPreserveCustomValidRequestId() throws Exception {
        String customId = "custom-client-id-12345";

        mockMvc.perform(get("/actuator/health")
                        .header(RequestIdFilter.HEADER_NAME, customId))
                .andExpect(status().isOk())
                .andExpect(header().string(RequestIdFilter.HEADER_NAME, is(customId)));
    }
}
