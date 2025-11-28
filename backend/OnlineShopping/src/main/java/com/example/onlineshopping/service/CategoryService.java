package com.example.onlineshopping.service;

import com.example.onlineshopping.entity.Category;
import com.example.onlineshopping.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;

    /**
     * 获取所有分类
     */
    public List<Category> findAll() {
        return categoryRepository.findByStatusTrue();
    }

    /**
     * 获取根分类（没有父分类的分类）
     */
    public List<Category> findRootCategories() {
        return categoryRepository.findByParentIsNullAndStatusTrue();
    }

    /**
     * 根据父分类ID获取子分类
     */
    public List<Category> findByParentId(Long parentId) {
        return categoryRepository.findByParentIdAndStatusTrue(parentId);
    }

    /**
     * 根据ID获取分类
     */
    public Optional<Category> findById(Long id) {
        return categoryRepository.findById(id).filter(Category::getStatus);
    }

    /**
     * 创建分类
     */
    @Transactional
    public Category save(Category category) {
        // 检查分类名称是否已存在
        if (categoryRepository.findByName(category.getName()).isPresent()) {
            throw new RuntimeException("分类名称已存在: " + category.getName());
        }

        category.setStatus(true);
        category.setCreateTime(LocalDateTime.now());

        // 如果设置了父分类，验证父分类是否存在
        if (category.getParent() != null && category.getParent().getId() != null) {
            Category parent = categoryRepository.findById(category.getParent().getId())
                    .orElseThrow(() -> new RuntimeException("父分类不存在"));
            category.setParent(parent);
        }

        return categoryRepository.save(category);
    }

    /**
     * 更新分类
     */
    @Transactional
    public Category update(Long id, Category category) {
        return categoryRepository.findById(id)
                .map(existingCategory -> {
                    // 检查分类名称是否与其他分类重复
                    if (!existingCategory.getName().equals(category.getName())) {
                        categoryRepository.findByName(category.getName())
                                .ifPresent(c -> {
                                    if (!c.getId().equals(id)) {
                                        throw new RuntimeException("分类名称已存在: " + category.getName());
                                    }
                                });
                    }

                    existingCategory.setName(category.getName());
                    existingCategory.setDescription(category.getDescription());

                    // 更新父分类
                    if (category.getParent() != null && category.getParent().getId() != null) {
                        Category parent = categoryRepository.findById(category.getParent().getId())
                                .orElseThrow(() -> new RuntimeException("父分类不存在"));
                        // 防止循环引用
                        if (isCircularReference(id, parent.getId())) {
                            throw new RuntimeException("不能将分类设置为其子分类的父分类");
                        }
                        existingCategory.setParent(parent);
                    } else {
                        existingCategory.setParent(null);
                    }

                    return categoryRepository.save(existingCategory);
                })
                .orElseThrow(() -> new RuntimeException("分类不存在"));
    }

    /**
     * 删除分类（软删除）
     */
    @Transactional
    public void delete(Long id) {
        categoryRepository.findById(id).ifPresent(category -> {
            // 检查是否有子分类
            List<Category> children = categoryRepository.findByParentIdAndStatusTrue(id);
            if (!children.isEmpty()) {
                throw new RuntimeException("该分类下存在子分类，无法删除");
            }

            // 检查是否有商品使用该分类
            // 这里需要根据实际情况实现商品关联检查

            category.setStatus(false);
            categoryRepository.save(category);
        });
    }

    /**
     * 检查分类名称是否存在
     */
    public boolean existsByName(String name) {
        return categoryRepository.findByName(name).isPresent();
    }

    /**
     * 搜索分类
     */
    public List<Category> searchCategories(String keyword) {
        return categoryRepository.findAll().stream()
                .filter(category -> category.getStatus() &&
                        (category.getName().toLowerCase().contains(keyword.toLowerCase()) ||
                                (category.getDescription() != null &&
                                        category.getDescription().toLowerCase().contains(keyword.toLowerCase()))))
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 获取分类树
     */
    public List<Category> getCategoryTree() {
        List<Category> rootCategories = findRootCategories();
        rootCategories.forEach(this::loadChildren);
        return rootCategories;
    }

    /**
     * 递归加载子分类
     */
    private void loadChildren(Category category) {
        List<Category> children = findByParentId(category.getId());
        category.setChildren(children);
        children.forEach(this::loadChildren);
    }

    /**
     * 检查循环引用
     */
    private boolean isCircularReference(Long categoryId, Long parentId) {
        if (categoryId.equals(parentId)) {
            return true;
        }

        Category parent = categoryRepository.findById(parentId).orElse(null);
        while (parent != null && parent.getParent() != null) {
            if (parent.getParent().getId().equals(categoryId)) {
                return true;
            }
            parent = parent.getParent();
        }

        return false;
    }

    /**
     * 获取分类路径
     */
    public String getCategoryPath(Long categoryId) {
        StringBuilder path = new StringBuilder();
        buildCategoryPath(categoryId, path);
        return path.toString();
    }

    private void buildCategoryPath(Long categoryId, StringBuilder path) {
        Category category = categoryRepository.findById(categoryId).orElse(null);
        if (category != null) {
            if (category.getParent() != null) {
                buildCategoryPath(category.getParent().getId(), path);
                path.append(" > ");
            }
            path.append(category.getName());
        }
    }

    /**
     * 获取所有分类（包括禁用的）
     */
    public List<Category> findAllWithDisabled() {
        return categoryRepository.findAll();
    }
}