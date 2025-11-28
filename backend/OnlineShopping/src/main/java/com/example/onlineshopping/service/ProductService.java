package com.example.onlineshopping.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.onlineshopping.entity.Product;
import com.example.onlineshopping.repository.ProductRepository;

@Service
public class ProductService {
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> findAll() {
        return productRepository.findByStatusTrue();
    }

    public Optional<Product> findById(Long id) {
        return productRepository.findById(id).filter(Product::getStatus);
    }

    public Product save(Product product) {
        return productRepository.save(product);
    }

    public Product update(Long id, Product product) {
        return productRepository.findById(id)
                .map(existingProduct -> {
                    existingProduct.setName(product.getName());
                    existingProduct.setDescription(product.getDescription());
                    existingProduct.setPrice(product.getPrice());
                    existingProduct.setStock(product.getStock());
                    existingProduct.setImageUrl(product.getImageUrl());
                    existingProduct.setCategory(product.getCategory());
                    return productRepository.save(existingProduct);
                })
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }

    public void delete(Long id) {
        productRepository.findById(id).ifPresent(product -> {
            product.setStatus(false);
            productRepository.save(product);
        });
    }

    public List<Product> findByCategoryId(Long categoryId) {
        return productRepository.findByCategoryIdAndStatusTrue(categoryId);
    }

    public List<Product> searchProducts(String keyword) {
        return productRepository.searchProducts(keyword);
    }
}