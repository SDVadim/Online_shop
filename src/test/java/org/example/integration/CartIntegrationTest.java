package org.example.integration;

import org.example.controller.dto.CartResponse;
import org.example.repository.CartItemRepository;
import org.example.repository.CategoryRepository;
import org.example.repository.Entities.CartItemEntity;
import org.example.repository.Entities.CategoryEntity;
import org.example.repository.Entities.ProductEntity;
import org.example.repository.Entities.UserEntity;
import org.example.repository.Entities.UserEntity.UsersRole;
import org.example.repository.ProductRepository;
import org.example.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class CartIntegrationTest {
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
        new PostgreSQLContainer<>("postgres:15-alpine");

    @Autowired
    private ObjectMapper objectMapper;

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

    private record UserData(String email, UsersRole role) {
    }

    private List<UserEntity> loadUser(List<UserData> userData) {
        List<UserEntity> entities = new ArrayList<>();
        userData.forEach(user -> {
            UserEntity userEntity = UserEntity.builder()
                .name("testUser")
                .email(user.email)
                .role(user.role)
                .passwordHash("password")
                .build();
            entities.add(userRepository.save(userEntity));
        });
        return entities;
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

    private record CartData(ProductEntity productEntity, UserEntity userEntity,
                            int quantity) {
    }

    private void loadCart(List<CartData> carts) {
        carts.forEach(cartData -> {
            CartItemEntity cartEntity = CartItemEntity.builder()
                .product(cartData.productEntity)
                .user(cartData.userEntity)
                .quantity(cartData.quantity)
                .build();

            cartItemRepository.save(cartEntity);
        });
    }

    @Test
    void addItemInCartTest() throws Exception {
        Long productId = loadProducts().getFirst().getId();
        UserEntity user = loadUser(List.of(new UserData("user1@example.com", UsersRole.USER))).getFirst();

        assertTrue(cartItemRepository.findCartEntitiesByUserIdAndProductId(user.getId(), productId).isEmpty());

        RequestBuilder requestBuilder = MockMvcRequestBuilders
            .post("/api/cart/items/{productId}/increment", productId)
            .with(user("user1@example.com"))
            .with(csrf());


        String response = mockMvc.perform(requestBuilder)
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        CartResponse cartResponse = objectMapper.readValue(response, CartResponse.class);
        assertEquals(1, cartResponse.items().stream().filter(item -> Objects.equals(item.productId(), productId)).findFirst().get().quantity());


        CartItemEntity cartEntity = cartItemRepository.findCartEntitiesByUserIdAndProductId(user.getId(), productId)
            .orElseThrow();
        assertEquals(1, cartEntity.getQuantity());
    }

    @Test
    void incrementProductInCartTest() throws Exception {
        ProductEntity productEntity = loadProducts().getFirst();
        UserEntity user = loadUser(List.of(new UserData("user1@example.com", UsersRole.USER))).getFirst();

        loadCart(List.of(new CartData(productEntity, user, 1)));

        CartItemEntity cartEntityBefore = cartItemRepository.findCartEntitiesByUserIdAndProductId(user.getId(), productEntity.getId())
            .orElseThrow();
        assertEquals(1, cartEntityBefore.getQuantity());

        RequestBuilder requestBuilder = MockMvcRequestBuilders
            .post("/api/cart/items/{productId}/increment", productEntity.getId())
            .with(user("user1@example.com"))
            .with(csrf());

        String response = mockMvc.perform(requestBuilder)
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        CartResponse cartResponse = objectMapper.readValue(response, CartResponse.class);
        assertEquals(2, cartResponse.items().stream().filter(item -> Objects.equals(item.productId(), productEntity.getId())).findFirst().get().quantity());

        CartItemEntity cartEntity = cartItemRepository.findCartEntitiesByUserIdAndProductId(user.getId(), productEntity.getId())
            .orElseThrow();
        assertEquals(2, cartEntity.getQuantity());

        assertEquals(cartEntityBefore.getId(), cartEntity.getId());
    }

    @Test
    void decrementProductInCartTest() throws Exception {
        ProductEntity productEntity = loadProducts().getFirst();
        UserEntity user = loadUser(List.of(new UserData("user1@example.com", UsersRole.USER))).getFirst();
        loadCart(List.of(new CartData(productEntity, user, 2)));

        CartItemEntity cartEntityBefore = cartItemRepository.findCartEntitiesByUserIdAndProductId(user.getId(), productEntity.getId())
            .orElseThrow();
        assertEquals(2, cartEntityBefore.getQuantity());
        assertEquals(1, cartItemRepository.count());

        RequestBuilder requestBuilder = MockMvcRequestBuilders.post(
                "/api/cart/items/{productId}/decrement",
                productEntity.getId()
            )
            .with(user("user1@example.com"))
            .with(csrf());

        String response = mockMvc.perform(requestBuilder)
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        CartResponse cartResponse = objectMapper.readValue(response, CartResponse.class);
        assertEquals(1, cartResponse.items().stream().filter(item -> Objects.equals(item.productId(), productEntity.getId())).findFirst().get().quantity());

        CartItemEntity cartEntity = cartItemRepository.findCartEntitiesByUserIdAndProductId(user.getId(), productEntity.getId())
            .orElseThrow();
        assertEquals(1, cartEntity.getQuantity());

        assertEquals(cartEntityBefore.getId(), cartEntity.getId());
        assertEquals(1, cartItemRepository.count());
    }

    @Test
    void decrementWithRemoveInCartTest() throws Exception {
        ProductEntity productEntity = loadProducts().getFirst();
        UserEntity user = loadUser(List.of(new UserData("user1@example.com", UsersRole.USER))).getFirst();
        loadCart(List.of(new CartData(productEntity, user, 1)));
        int sizeBefore = cartItemRepository.findCartEntitiesByUserId(user.getId()).size();
        CartItemEntity cartEntityBefore = cartItemRepository.findCartEntitiesByUserIdAndProductId(user.getId(), productEntity.getId())
            .orElseThrow();
        assertEquals(1, cartEntityBefore.getQuantity());
        assertEquals(1, cartItemRepository.count());

        RequestBuilder requestBuilder = MockMvcRequestBuilders.post(
                "/api/cart/items/{productId}/decrement",
                productEntity.getId()
            )
            .with(user("user1@example.com"))
            .with(csrf());

        String response = mockMvc.perform(requestBuilder)
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        CartResponse cartResponse = objectMapper.readValue(response, CartResponse.class);

        assertEquals(sizeBefore - 1, cartItemRepository.findCartEntitiesByUserId(user.getId()).size());
        assertEquals(sizeBefore - 1, cartResponse.items().size());

        assertTrue(cartItemRepository.findCartEntitiesByUserIdAndProductId(user.getId(), productEntity.getId()).isEmpty());
    }

    @Test
    void deleteProductTest() throws Exception {
        ProductEntity productEntity = loadProducts().getFirst();
        UserEntity user = loadUser(List.of(new UserData("user1@example.com", UsersRole.USER))).getFirst();
        loadCart(List.of(new CartData(productEntity, user, 14)));
        int sizeBefore = cartItemRepository.findCartEntitiesByUserId(user.getId()).size();
        assertTrue(cartItemRepository.findCartEntitiesByUserIdAndProductId(user.getId(), productEntity.getId()).isPresent());

        RequestBuilder requestBuilder = MockMvcRequestBuilders.delete(
                "/api/cart/items/{productId}",
                productEntity.getId()
            )
            .with(user("user1@example.com"))
            .with(csrf());

        String response = mockMvc.perform(requestBuilder)
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        CartResponse cartResponse = objectMapper.readValue(response, CartResponse.class);

        assertEquals(sizeBefore - 1, cartItemRepository.findCartEntitiesByUserId(user.getId()).size());
        assertEquals(sizeBefore - 1, cartResponse.items().size());
        assertTrue(cartItemRepository.findCartEntitiesByUserIdAndProductId(user.getId(), productEntity.getId()).isEmpty());
    }

    @Test
    void cleanUserCartTest() throws Exception {
        List<ProductEntity> productEntities = loadProducts();
        UserEntity user = loadUser(List.of(new UserData("user1@example.com", UsersRole.USER))).getFirst();
        loadCart(List.of(new CartData(productEntities.getFirst(), user, 1), new CartData(productEntities.getLast(), user, 2)));
        assertTrue(!cartItemRepository.findCartEntitiesByUserId(user.getId()).isEmpty());
        assertTrue(cartItemRepository.findCartEntitiesByUserIdAndProductId(user.getId(), productEntities.getFirst().getId()).isPresent());
        assertTrue(cartItemRepository.findCartEntitiesByUserIdAndProductId(user.getId(), productEntities.getLast().getId()).isPresent());

        RequestBuilder requestBuilder = MockMvcRequestBuilders
            .delete("/api/cart/items")
            .with(user("user1@example.com"))
            .with(csrf());

        String response = mockMvc.perform(requestBuilder)
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        CartResponse cartResponse = objectMapper.readValue(response, CartResponse.class);

        assertEquals(0, cartResponse.items().size());
        assertTrue(cartItemRepository.findCartEntitiesByUserIdAndProductId(user.getId(), productEntities.getFirst().getId()).isEmpty());
        assertTrue(cartItemRepository.findCartEntitiesByUserIdAndProductId(user.getId(), productEntities.getLast().getId()).isEmpty());
        assertEquals(0, cartItemRepository.findCartEntitiesByUserId(user.getId()).size());
    }

    @Test
    @WithUserDetails
    void checkCartPriceTest() {

    }

    @Test
    void clearCartShouldNotAffectAnotherUserCart() throws Exception {
        List<ProductEntity> productEntities = loadProducts();
        List<UserEntity> usersEntities = loadUser(List.of(
            new UserData("user1@example.com", UsersRole.USER),
            new UserData("user2@example.com", UsersRole.USER))
        );

        loadCart(List.of(
            new CartData(productEntities.getFirst(), usersEntities.getFirst(), 1),
            new CartData(productEntities.getLast(), usersEntities.getFirst(), 4),
            new CartData(productEntities.getLast(), usersEntities.getLast(), 12)
        ));

        assertTrue(cartItemRepository.findCartEntitiesByUserIdAndProductId(usersEntities.getFirst().getId(), productEntities.getFirst().getId()).isPresent());
        assertTrue(cartItemRepository.findCartEntitiesByUserIdAndProductId(usersEntities.getFirst().getId(), productEntities.getLast().getId()).isPresent());
        assertTrue(cartItemRepository.findCartEntitiesByUserIdAndProductId(usersEntities.getLast().getId(), productEntities.getLast().getId()).isPresent());

        RequestBuilder requestBuilder1 = MockMvcRequestBuilders
            .delete("/api/cart/items")
            .with(user("user1@example.com"))
            .with(csrf());

        RequestBuilder requestBuilder2 = MockMvcRequestBuilders
            .delete("/api/cart/items")
            .with(user("user1@example.com"))
            .with(csrf());

        mockMvc.perform(requestBuilder1)
            .andExpect(status().isOk());

        assertTrue(cartItemRepository.findCartEntitiesByUserIdAndProductId(usersEntities.getFirst().getId(), productEntities.getFirst().getId()).isEmpty());
        assertTrue(cartItemRepository.findCartEntitiesByUserIdAndProductId(usersEntities.getFirst().getId(), productEntities.getLast().getId()).isEmpty());
        var carEntity = cartItemRepository.findCartEntitiesByUserIdAndProductId(usersEntities.getLast().getId(), productEntities.getLast().getId());
        assertTrue(carEntity.isPresent());
        assertEquals(12, carEntity.get().getQuantity());
    }

    @Test
    void concurrentIncrementsShouldNotLoseUpdates() throws Exception {
        ProductEntity product = loadProducts().getFirst();
        UserEntity userEntity = loadUser(List.of(
            new UserData("user1@example.com", UsersRole.USER)
        )).getFirst();

        loadCart(List.of(new CartData(product, userEntity, 1)));

        Long userId = userEntity.getId();
        Long productId = product.getId();
        String email = userEntity.getEmail();

        Long originalItemId = cartItemRepository
            .findCartEntitiesByUserIdAndProductId(userId, productId)
            .orElseThrow()
            .getId();

        ExecutorService executor = Executors.newFixedThreadPool(2);

        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        List<Future<Void>> futures = new ArrayList<>();

        try {
            for (int i = 0; i < 2; i++) {
                Future<Void> future = executor.submit(() -> {
                    // Сообщаем основному потоку, что задание готово.
                    ready.countDown();

                    // Ждём общего разрешения начать запрос.
                    if (!start.await(10, TimeUnit.SECONDS)) {
                        throw new IllegalStateException(
                            "Не получен сигнал старта"
                        );
                    }

                    mockMvc.perform(MockMvcRequestBuilders
                            .post(
                                "/api/cart/items/{productId}/increment",
                                productId
                            )
                            .with(user(email))
                            .with(csrf()))
                        .andExpect(status().isOk());

                    return null;
                });

                futures.add(future);
            }

            // Дожидаемся, пока оба задания подготовятся.
            assertTrue(
                ready.await(10, TimeUnit.SECONDS),
                "Оба рабочих потока должны быть готовы"
            );

            // Разрешаем обоим заданиям выполнить запрос.
            start.countDown();

            // Проверяем, что оба задания успешно завершились.
            for (Future<Void> future : futures) {
                future.get(15, TimeUnit.SECONDS);
            }
        } finally {
            // При ошибке отменяем оставшуюся работу.
            for (Future<Void> future : futures) {
                future.cancel(true);
            }

            executor.shutdownNow();

            assertTrue(
                executor.awaitTermination(10, TimeUnit.SECONDS),
                "Рабочие потоки должны завершиться"
            );
        }

        List<CartItemEntity> items =
            cartItemRepository.findCartEntitiesByUserId(userId);

        assertEquals(1, items.size());

        CartItemEntity item = cartItemRepository
            .findCartEntitiesByUserIdAndProductId(userId, productId)
            .orElseThrow();

        assertEquals(originalItemId, item.getId());
        assertEquals(3, item.getQuantity());
    }
}
