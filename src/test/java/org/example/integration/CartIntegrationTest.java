package org.example.integration;

import org.example.controller.dto.CartResponse;
import org.example.repository.CartItemRepository;
import org.example.repository.CategoryRepository;
import org.example.repository.Entities.CartEntity;
import org.example.repository.Entities.CategoryEntity;
import org.example.repository.Entities.ProductEntity;
import org.example.repository.Entities.UserEntity;
import org.example.repository.Entities.UserEntity.UsersRole;
import org.example.repository.ProductRepository;
import org.example.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class CartIntegrationTest {
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
        new PostgreSQLContainer<>("postgres:15-alpine");

    ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        cartItemRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();
    }

    private UserEntity loadUser(String name, UsersRole role) {
        UserEntity user = UserEntity.builder()
            .name("testUser")
            .email(name)
            .role(role)
            .passwordHash("password")
            .build();
        return userRepository.save(user);
    }

    private List<ProductEntity> loadProducts() {
        CategoryEntity category = categoryRepository.save(CategoryEntity
            .builder()
            .name("Test category")
            .build()
        );
        ProductEntity product1 = ProductEntity.builder()
            .name("Test product 1")
            .price(new BigDecimal(100))
            .category(category)
            .build();
        ProductEntity product2 = ProductEntity.builder()
            .name("Test product 2")
            .price(new BigDecimal(100))
            .category(category)
            .build();
        return List.of(productRepository.save(product1), productRepository.save(product2));
    }

    private void loadCart(List<ProductEntity> products, UserEntity user, int quantity) {
        products.forEach(product -> {
            CartEntity cartEntity = CartEntity.builder()
                .product(product)
                .user(user)
                .quantity(quantity)
                .build();

            cartItemRepository.save(cartEntity);
        });
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    void addItemInCartTest() throws Exception {
        Long productId = loadProducts().getFirst().getId();
        UserEntity user = loadUser("user@example.com", UsersRole.USER);

        assertTrue(cartItemRepository.findCartEntitiesByUserIdAndProductId(user.getId(), productId).isEmpty());

        RequestBuilder requestBuilder = MockMvcRequestBuilders
            .post("/api/cart/items/{productId}/increment", productId)
            .with(csrf());


        String response = mockMvc.perform(requestBuilder)
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        CartResponse cartResponse = objectMapper.readValue(response, CartResponse.class);
        assertEquals(productId, cartResponse.productId());
        assertEquals(1, cartResponse.quantity());

        CartEntity cartEntity = cartItemRepository.findCartEntitiesByUserIdAndProductId(user.getId(), productId)
            .orElseThrow();
        assertEquals(1, cartEntity.getQuantity());
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    void incrementProductInCartTest() throws Exception {
        ProductEntity productEntity = loadProducts().getFirst();
        UserEntity user = loadUser("user@example.com", UsersRole.USER);

        loadCart(List.of(productEntity), user, 1);

        CartEntity cartEntityBefore = cartItemRepository.findCartEntitiesByUserIdAndProductId(user.getId(), productEntity.getId())
            .orElseThrow();
        assertEquals(1, cartEntityBefore.getQuantity());

        RequestBuilder requestBuilder = MockMvcRequestBuilders
            .post("/api/cart/items/{productId}/increment", productEntity.getId())
            .with(csrf());

        String response = mockMvc.perform(requestBuilder)
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        CartResponse cartResponse = objectMapper.readValue(response, CartResponse.class);
        assertEquals(productEntity.getId(), cartResponse.productId());
        assertEquals(2, cartResponse.quantity());

        CartEntity cartEntity = cartItemRepository.findCartEntitiesByUserIdAndProductId(user.getId(), productEntity.getId())
            .orElseThrow();
        assertEquals(2, cartEntity.getQuantity());

        assertEquals(cartEntityBefore.getId(), cartEntity.getId());
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    void decrementProductInCartTest() throws Exception {
        ProductEntity productEntity = loadProducts().getFirst();
        UserEntity user = loadUser("user@example.com", UsersRole.USER);
        loadCart(List.of(productEntity), user, 2);

        CartEntity cartEntityBefore = cartItemRepository.findCartEntitiesByUserIdAndProductId(user.getId(), productEntity.getId())
            .orElseThrow();
        assertEquals(2, cartEntityBefore.getQuantity());
        assertEquals(1, cartItemRepository.count());

        RequestBuilder requestBuilder = MockMvcRequestBuilders.post(
            "/api/cart/items/{productId}/decrement",
                productEntity.getId()
            )
            .with(csrf());

        String response = mockMvc.perform(requestBuilder)
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        CartResponse cartResponse = objectMapper.readValue(response, CartResponse.class);
        assertEquals(productEntity.getId(), cartResponse.productId());
        assertEquals(1, cartResponse.quantity());

        CartEntity cartEntity = cartItemRepository.findCartEntitiesByUserIdAndProductId(user.getId(), productEntity.getId())
            .orElseThrow();
        assertEquals(1, cartEntity.getQuantity());

        assertEquals(cartEntityBefore.getId(), cartEntity.getId());
        assertEquals(1, cartItemRepository.count());
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    void decrementWithRemoveInCartTest() throws Exception {
        ProductEntity productEntity = loadProducts().getFirst();
        UserEntity user = loadUser("user@example.com", UsersRole.USER);
        loadCart(List.of(productEntity), user, 1);

        CartEntity cartEntityBefore = cartItemRepository.findCartEntitiesByUserIdAndProductId(user.getId(), productEntity.getId())
            .orElseThrow();
        assertEquals(1, cartEntityBefore.getQuantity());
        assertEquals(1, cartItemRepository.count());

        RequestBuilder requestBuilder = MockMvcRequestBuilders.post(
                "/api/cart/items/{productId}/decrement",
                productEntity.getId()
            )
            .with(csrf());

        mockMvc.perform(requestBuilder)
            .andExpect(status().isNoContent())
            .andExpect(content().string(""));

        assertTrue(cartItemRepository.findCartEntitiesByUserIdAndProductId(user.getId(), productEntity.getId()).isEmpty());

        assertEquals(0, cartItemRepository.count());
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    void deleteProductTest() throws Exception {
        ProductEntity productEntity = loadProducts().getFirst();
        UserEntity user = loadUser("user@example.com", UsersRole.USER);
        loadCart(List.of(productEntity), user, 14);

        assertTrue(cartItemRepository.findCartEntitiesByUserIdAndProductId(user.getId(), productEntity.getId()).isPresent());

        RequestBuilder requestBuilder = MockMvcRequestBuilders.delete(
            "/api/cart/items/{productId}",
                productEntity.getId()
            )
            .with(csrf());

        mockMvc.perform(requestBuilder)
            .andExpect(status().isNoContent());

        assertTrue(cartItemRepository.findCartEntitiesByUserIdAndProductId(user.getId(), productEntity.getId()).isEmpty());
        assertEquals(0, cartItemRepository.count());
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    void cleanUserCartTest() throws Exception {
        List<ProductEntity> productEntities = loadProducts();
        UserEntity user = loadUser("user@example.com", UsersRole.USER);
        loadCart(productEntities, user, 1);

        assertTrue(cartItemRepository.findCartEntitiesByUserIdAndProductId(user.getId(), productEntities.getFirst().getId()).isPresent());
        assertTrue(cartItemRepository.findCartEntitiesByUserIdAndProductId(user.getId(), productEntities.getLast().getId()).isPresent());

        RequestBuilder requestBuilder = MockMvcRequestBuilders
            .delete("/api/cart/items")
            .with(csrf());

        mockMvc.perform(requestBuilder)
            .andExpect(status().isNoContent());

        assertTrue(cartItemRepository.findCartEntitiesByUserIdAndProductId(user.getId(), productEntities.getFirst().getId()).isEmpty());
        assertTrue(cartItemRepository.findCartEntitiesByUserIdAndProductId(user.getId(), productEntities.getLast().getId()).isEmpty());

    }

}
