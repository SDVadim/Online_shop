package org.example.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.controller.dto.CategoryRequest;
import org.example.controller.dto.CategoryResponse;
import org.example.mapper.CategoryMapper;
import org.example.repository.CategoryRepository;
import org.example.repository.Entities.CategoryEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
public class CatalogIntegrationTest {
    private final CategoryRequest request1 = new CategoryRequest("test category 1");
    private final CategoryRequest request2 = new CategoryRequest("test category 2");

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgreSQLContainer =
        new PostgreSQLContainer<>("postgres:15-alpine");

    ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoryRepository categoryRepository;

    @BeforeEach
    void setUp() {
        categoryRepository.deleteAll();
    }

    List<Long> loadData() {
        CategoryMapper categoryMapper = new CategoryMapper();
        long id1 = categoryRepository.save(categoryMapper.toEntity(request1)).getId();
        long id2 = categoryRepository.save(categoryMapper.toEntity(request2)).getId();
        return List.of(id1, id2);
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void createCategory() throws Exception {
        RequestBuilder requestBuilder = MockMvcRequestBuilders
            .post("/api/categories/add")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request1))
            .with(csrf());

        String response = mockMvc.perform(requestBuilder)
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        Long id = objectMapper.readValue(response, Long.class);
        Optional<CategoryEntity> saved = categoryRepository.findById(id);
        Assertions.assertTrue(saved.isPresent());
        Assertions.assertEquals("test category 1", saved.get().getName());
        Assertions.assertEquals(1, categoryRepository.count());
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void UpdateCategory() throws Exception {
        Long firstId = loadData().get(0);
        Assertions.assertTrue(categoryRepository.findById(firstId).isPresent());
        Assertions.assertEquals("test category 1", categoryRepository.findById(firstId).get().getName());
        RequestBuilder requestBuilder = MockMvcRequestBuilders.put("/api/categories/{firstId}", firstId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request2))
            .with(csrf());

        String response = mockMvc.perform(requestBuilder)
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        CategoryResponse categoryResponse = objectMapper.readValue(response, CategoryResponse.class);
        Assertions.assertEquals("test category 2", categoryResponse.name());
        Assertions.assertEquals(firstId, categoryResponse.id());

        Optional<CategoryEntity> saved = categoryRepository.findById(categoryResponse.id());
        Assertions.assertTrue(saved.isPresent());

        Assertions.assertEquals("test category 2", saved.get().getName());
        Assertions.assertEquals(2, categoryRepository.count());
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void deleteCategory() throws Exception {
        loadData();
        Assertions.assertTrue(categoryRepository.findById(1L).isPresent());
        Assertions.assertEquals(2, categoryRepository.count());
        RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/api/categories/1")
            .with(csrf());
        mockMvc.perform(requestBuilder).andExpect(status().isNoContent());
        Assertions.assertTrue(categoryRepository.findById(1L).isEmpty());
        Assertions.assertEquals(1, categoryRepository.count());
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void getAllCategories() throws Exception {
        loadData();
        Assertions.assertEquals(2, categoryRepository.count());
        RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/api/categories")
            .with(csrf());
        String body = mockMvc.perform(requestBuilder)
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
        List<CategoryEntity> lists = categoryRepository.findAll();
        Assertions.assertEquals(body, objectMapper.writeValueAsString(lists));
    }
}
