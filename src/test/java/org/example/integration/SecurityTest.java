package org.example.integration;

import org.example.controller.dto.CategoryRequest;
import org.example.controller.dto.ProductRequest;
import org.example.repository.CategoryRepository;
import org.example.repository.ProductRepository;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
public class SecurityTest {
    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> postgres =
        new PostgreSQLContainer<>("postgres:15-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    static Stream<Arguments> data() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        ProductRequest productRequest = new ProductRequest("product 1", new BigDecimal(100), 1L);
        CategoryRequest categoryRequest = new CategoryRequest("category 1");

        String productJson = objectMapper.writeValueAsString(productRequest);
        String categoryJson = objectMapper.writeValueAsString(categoryRequest);

        return Stream.of(
            Arguments.of(HttpMethod.POST, "/api/products/add", productJson),
            Arguments.of(HttpMethod.PUT, "/api/products/1", productJson),
            Arguments.of(HttpMethod.DELETE, "/api/products/1", null),
            Arguments.of(HttpMethod.POST, "/api/categories/add", categoryJson),
            Arguments.of(HttpMethod.PUT, "/api/categories/1", categoryJson),
            Arguments.of(HttpMethod.DELETE, "/api/categories/1", null)
        );
    }

    @ParameterizedTest(name = "USER: {0} {1} -> 403")
    @MethodSource("data")
    @WithMockUser(username = "test@test", roles = "USER")
    void userCannotChangeCategory(HttpMethod method, String url, String body) throws Exception {
        long categoryCountBefore = categoryRepository.count();
        long productCountBefore = productRepository.count();

        MockHttpServletRequestBuilder builder = request(method, url).with(csrf());

        if (body != null) {
            builder.contentType(MediaType.APPLICATION_JSON).content(body);
        }

        mockMvc.perform(builder).andExpect(status().isForbidden());
        assertThat(categoryCountBefore).isEqualTo(categoryRepository.count());
        assertThat(productCountBefore).isEqualTo(productRepository.count());
    }

}
