package com.example.onlineshopping.entity.converter;

import com.example.onlineshopping.entity.User;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class UserRoleConverter implements AttributeConverter<User.UserRole, String> {

    @Override
    public String convertToDatabaseColumn(User.UserRole attribute) {
        if (attribute == null) return null;
        return attribute.name();
    }

    @Override
    public User.UserRole convertToEntityAttribute(String dbData) {
        if (dbData == null) return null; // 交给实体默认值处理
        String upper = dbData.trim().toUpperCase();
        if (upper.isEmpty()) {
            return User.UserRole.ROLE_USER;
        }

        // 兼容数据库中既可能存储 ROLE_USER，也可能存储 USER 的情况
        if (!upper.startsWith("ROLE_")) {
            upper = "ROLE_" + upper;
        }

        try {
            return User.UserRole.valueOf(upper);
        } catch (IllegalArgumentException ex) {
            return User.UserRole.ROLE_USER; // 兜底
        }
    }
}
