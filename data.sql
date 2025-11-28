-- 网上购物系统数据库脚本
-- 创建时间: 2024-10-12

-- 1. 创建数据库
CREATE DATABASE IF NOT EXISTS online_shopping 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE online_shopping;

-- 2. 创建用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    username VARCHAR(50) UNIQUE NOT NULL COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    email VARCHAR(100) COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '电话',
    address VARCHAR(255) COMMENT '地址',
    role VARCHAR(20) DEFAULT 'USER' COMMENT '角色',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_create_time (create_time),
    INDEX idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 3. 创建分类表
CREATE TABLE IF NOT EXISTS categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '分类ID',
    name VARCHAR(100) NOT NULL COMMENT '分类名称',
    description VARCHAR(255) COMMENT '分类描述',
    parent_id BIGINT COMMENT '父分类ID',
    status BOOLEAN DEFAULT TRUE COMMENT '状态',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    UNIQUE KEY uk_name (name),
    INDEX idx_parent_id (parent_id),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time),
    
    CONSTRAINT fk_category_parent
        FOREIGN KEY (parent_id)
        REFERENCES categories(id)
        ON DELETE SET NULL
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品分类表';

-- 4. 创建商品表
CREATE TABLE IF NOT EXISTS products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '商品ID',
    name VARCHAR(255) NOT NULL COMMENT '商品名称',
    description TEXT COMMENT '商品描述',
    price DECIMAL(10,2) NOT NULL COMMENT '商品价格',
    stock INT DEFAULT 0 COMMENT '库存数量',
    image_url VARCHAR(500) COMMENT '商品图片',
    category_id BIGINT COMMENT '分类ID',
    status BOOLEAN DEFAULT TRUE COMMENT '商品状态',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    INDEX idx_name (name),
    INDEX idx_category_id (category_id),
    INDEX idx_price (price),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time),
    INDEX idx_stock (stock),
    
    CONSTRAINT fk_product_category
        FOREIGN KEY (category_id)
        REFERENCES categories(id)
        ON DELETE SET NULL
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品表';

-- 5. 创建购物车表
CREATE TABLE IF NOT EXISTS cart_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '购物车项ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    quantity INT NOT NULL COMMENT '商品数量',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_user_id (user_id),
    INDEX idx_product_id (product_id),
    INDEX idx_create_time (create_time),
    
    UNIQUE KEY unique_user_product (user_id, product_id),
    
    CONSTRAINT fk_cart_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
        
    CONSTRAINT fk_cart_product
        FOREIGN KEY (product_id)
        REFERENCES products(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='购物车表';

-- 6. 创建订单表
CREATE TABLE IF NOT EXISTS orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '订单ID',
    order_number VARCHAR(50) UNIQUE NOT NULL COMMENT '订单编号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    total_amount DECIMAL(10,2) NOT NULL COMMENT '订单总金额',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '订单状态',
    shipping_address VARCHAR(500) COMMENT '收货地址',
    payment_method VARCHAR(50) COMMENT '支付方式',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_order_number (order_number),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time),
    
    CONSTRAINT fk_order_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

-- 7. 创建订单项表
CREATE TABLE IF NOT EXISTS order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '订单项ID',
    order_id BIGINT NOT NULL COMMENT '订单ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    quantity INT NOT NULL COMMENT '商品数量',
    price DECIMAL(10,2) NOT NULL COMMENT '商品单价',
    subtotal DECIMAL(10,2) NOT NULL COMMENT '小计金额',
    
    INDEX idx_order_id (order_id),
    INDEX idx_product_id (product_i    spring:
      datasource:
        url: jdbc:mysql://localhost:3306/online_shopping?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8
        username: root
        password: 123456
        driver-class-name: com.mysql.cj.jdbc.Driverd),
    
    CONSTRAINT fk_order_item_order
        FOREIGN KEY (order_id)
        REFERENCES orders(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
        
    CONSTRAINT fk_order_item_product
        FOREIGN KEY (product_id)
        REFERENCES products(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单项表';


INSERT INTO users (username, password, email, role) VALUES 
('admin', '123456', 'admin@shop.com', 'ROLE_ADMIN'),
('user1', '123456', 'user1@shop.com', 'ROLE_USER');

INSERT INTO categories (name, description) VALUES 
('电子产品', '手机、电脑、平板等'),
('服装', '男女服装、鞋帽等'),
('家居', '家具、家居装饰等'),
('图书', '各类图书、杂志等');

-- 插入商品数据
INSERT INTO products (name, description, price, stock, image_url, category_id) VALUES 
('iPhone 14', '最新款苹果手机', 5999.00, 100, '/images/iphone14.jpg', 1),
('MacBook Pro', '苹果笔记本电脑', 12999.00, 50, '/images/macbook.jpg', 1),
('牛仔裤', '经典蓝色牛仔裤', 199.00, 200, '/images/jeans.jpg', 2),
('沙发', '舒适布艺沙发', 2999.00, 20, '/images/sofa.jpg', 3),
('Java编程思想', '经典编程书籍', 89.00, 150, '/images/java-book.jpg', 4);

-- 9. 显示创建成功的表
SHOW TABLES;

-- 10. 显示各表的数据统计
SELECT 
    'users' as table_name, 
    COUNT(*) as record_count 
FROM users
UNION ALL
SELECT 
    'categories', 
    COUNT(*) 
FROM categories
UNION ALL
SELECT 
    'products', 
    COUNT(*) 
FROM products;

-- 11. 验证数据完整性
SELECT 
    p.id, 
    p.name, 
    p.price,
    c.name as category_name
FROM products p
JOIN categories c ON p.category_id = c.id
ORDER BY p.id;