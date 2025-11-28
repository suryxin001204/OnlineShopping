package com.example.onlineshopping.controller;

import com.example.onlineshopping.dto.CategoryDto;
import com.example.onlineshopping.entity.Category;
import com.example.onlineshopping.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<CategoryDto>> getAllCategories() {
        List<Category> categories = categoryService.findAll();
        List<CategoryDto> categoryDtos = categories.stream()
                .map(CategoryDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(categoryDtos);
    }

    @GetMapping("/roots")
    public ResponseEntity<List<CategoryDto>> getRootCategories() {
        List<Category> categories = categoryService.findRootCategories();
        List<CategoryDto> categoryDtos = categories.stream()
                .map(CategoryDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(categoryDtos);
    }

    @GetMapping("/{id}/children")
    public ResponseEntity<List<CategoryDto>> getChildCategories(@PathVariable Long id) {
        List<Category> categories = categoryService.findByParentId(id);
        List<CategoryDto> categoryDtos = categories.stream()
                .map(CategoryDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(categoryDtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryDto> getCategoryById(@PathVariable Long id) {
        return categoryService.findById(id)
                .map(CategoryDto::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<CategoryDto> createCategory(@RequestBody Category category) {
        Category savedCategory = categoryService.save(category);
        return ResponseEntity.ok(CategoryDto.fromEntity(savedCategory));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryDto> updateCategory(@PathVariable Long id, @RequestBody Category category) {
        Category updatedCategory = categoryService.update(id, category);
        return ResponseEntity.ok(CategoryDto.fromEntity(updatedCategory));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.delete(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/all")
    public ResponseEntity<List<CategoryDto>> getAllCategoriesWithDisabled() {
        List<Category> categories = categoryService.findAllWithDisabled();
        List<CategoryDto> categoryDtos = categories.stream()
                .map(CategoryDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(categoryDtos);
    }
}