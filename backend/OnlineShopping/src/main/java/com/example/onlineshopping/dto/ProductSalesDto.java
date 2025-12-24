package com.example.onlineshopping.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProductSalesDto {
    private String productName;
    private Long totalSold;
}
