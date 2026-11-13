package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import org.example.controller.dto.CategoryRequest;
import org.example.controller.dto.CategoryResponse;
import org.example.service.CategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {
    private CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Operation(
        summary = "Get all categories",
        description = "Get all existing categories"
    ) //todo: для всего остального
    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        var allCategories = categoryService.getAll();
        return ResponseEntity.status(HttpStatus.OK).body(allCategories);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(
        @Parameter(description = "Идентификатор товара", example = "1")
        @PathVariable Long id
    ) {
        var category = categoryService.get(id);
        return ResponseEntity.status(HttpStatus.OK).body(category);
    }

    @PostMapping("/add")
    public ResponseEntity<Long> addCategory(
        @Valid @RequestBody CategoryRequest categoryRequest
    ) {
        Long id = categoryService.create(categoryRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(
        @PathVariable Long id,
        @Valid @RequestBody CategoryRequest categoryRequest
    ) {
        CategoryResponse response = categoryService.update(id, categoryRequest);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(
        @PathVariable Long id
    ) {
        categoryService.delete(id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
