package com.example.onlineshopping.dto;

import com.example.onlineshopping.entity.Category;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class CategoryDto {
    private Long id;
    private String name;
    private String description;
    private Long parentId;
    private String parentName;
    private List<CategoryDto> children;
    private Boolean status;
    private LocalDateTime createTime;

    public static CategoryDto fromEntity(Category category) {
        CategoryDto dto = new CategoryDto();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        if (category.getParent() != null) {
            dto.setParentId(category.getParent().getId());
            dto.setParentName(category.getParent().getName());
        }
        // 避免在列表查询时触发懒加载异常或无限递归，暂不映射子分类
        // if (category.getChildren() != null) {
        //     dto.setChildren(category.getChildren().stream()
        //             .map(CategoryDto::fromEntity)
        //             .collect(Collectors.toList()));
        // }
        dto.setStatus(category.getStatus());
        dto.setCreateTime(category.getCreateTime());
        return dto;
    }
}