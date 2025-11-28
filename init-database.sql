-- =============================================
-- 网上购物系统完整数据库初始化脚本
-- 包含：数据库创建、表结构、数据清理、初始化数据
-- 创建时间: 2025-11-26
-- =============================================

-- ==================== 第一部分：数据库和表结构 ====================

-- 1. 创建数据库
DROP DATABASE IF EXISTS online_shopping;
CREATE DATABASE online_shopping 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE online_shopping;

-- 2. 创建用户表
CREATE TABLE users (
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
CREATE TABLE categories (
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
CREATE TABLE products (
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
CREATE TABLE cart_items (
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
CREATE TABLE orders (
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
CREATE TABLE order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '订单项ID',
    order_id BIGINT NOT NULL COMMENT '订单ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    quantity INT NOT NULL COMMENT '商品数量',
    price DECIMAL(10,2) NOT NULL COMMENT '商品单价',
    subtotal DECIMAL(10,2) NOT NULL COMMENT '小计金额',
    
    INDEX idx_order_id (order_id),
    INDEX idx_product_id (product_id),
    
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


-- ==================== 第二部分：初始化数据 ====================

-- 临时禁用外键检查
SET FOREIGN_KEY_CHECKS = 0;

-- 8. 插入用户数据（密码为明文，仅用于开发测试）
INSERT INTO users (username, password, email, role) VALUES 
('admin', '123456', 'admin@shop.com', 'ROLE_ADMIN'),
('user1', '123456', 'user1@shop.com', 'ROLE_USER');

-- 9. 插入分类数据
INSERT INTO categories (name, description) VALUES 
('电子产品', '手机、电脑、平板等'),
('服装', '男女服装、鞋帽等'),
('家居', '家具、家居装饰等'),
('图书', '各类图书、杂志等');

-- 10. 插入商品数据（包含原有5个 + 扩展20个）
INSERT INTO products (name, description, price, stock, image_url, category_id, status) VALUES 
-- 原始商品 (5个)
('iPhone 17', '最新款苹果手机', 5999.00, 100, '/images/1.jpg', 1, 1),
('MacBook Pro', '苹果笔记本电脑', 12999.00, 50, '/images/2.jpg', 1, 1),
('牛仔裤', '经典蓝色牛仔裤', 199.00, 200, '/images/3.jpg', 2, 1),
('沙发', '舒适布艺沙发', 2999.00, 20, '/images/4.jpg', 3, 1),
('Java编程思想', '经典编程书籍', 89.00, 150, '/images/5.jpg', 4, 1),

-- 扩展商品 - 电子产品 (5个)
('iPad Pro', 'Apple平板电脑', 6999.00, 80, '/images/6.jpg', 1, 1),
('AirPods Pro', '苹果无线耳机', 1999.00, 200, '/images/7.jpg', 1, 1),
('小米13', '小米旗舰手机', 3999.00, 150, '/images/8.jpg', 1, 1),
('华为MateBook', '华为笔记本电脑', 7999.00, 60, '/images/9.jpg', 1, 1),
('索尼相机', '专业单反相机', 9999.00, 30, '/images/10.jpg', 1, 1),

-- 扩展商品 - 服装 (5个)
('T恤', '纯棉短袖T恤', 89.00, 300, '/images/11.jpg', 2, 1),
('连衣裙', '夏季清新连衣裙', 299.00, 150, '/images/12.jpg', 2, 1),
('运动鞋', '透气跑步鞋', 399.00, 200, '/images/13.jpg', 2, 1),
('羽绒服', '冬季保暖羽绒服', 899.00, 100, '/images/14.jpg', 2, 1),
('帆布包', '文艺帆布单肩包', 159.00, 180, '/images/15.jpg', 2, 1),

-- 扩展商品 - 家居 (5个)
('床垫', '记忆棉床垫', 1999.00, 50, '/images/16.jpg', 3, 1),
('书桌', '简约电脑桌', 599.00, 80, '/images/17.jpg', 3, 1),
('台灯', 'LED护眼台灯', 199.00, 120, '/images/18.jpg', 3, 1),
('收纳箱', '塑料整理箱', 49.00, 500, '/images/19.jpg', 3, 1),
('窗帘', '遮光卧室窗帘', 299.00, 100, '/images/20.jpg', 3, 1),

-- 扩展商品 - 图书 (5个)
('Python编程', 'Python入门教程', 79.00, 200, '/images/21.jpg', 4, 1),
('算法导论', '经典算法书籍', 128.00, 100, '/images/22.jpg', 4, 1),
('设计模式', '软件设计模式', 99.00, 150, '/images/23.jpg', 4, 1),
('人类简史', '历史畅销书', 68.00, 300, '/images/24.jpg', 4, 1),
('三体', '科幻小说', 59.00, 400, '/images/25.jpg', 4, 1);

-- ==================== 第二部分B：添加测试订单数据 ====================

-- 为user1创建一些测试订单
INSERT INTO orders (order_number, user_id, total_amount, status, shipping_address, payment_method) VALUES 
('ORD202511270001', 2, 4398.00, 'DELIVERED', '北京市朝阳区建国门外大街1号', '支付宝'),
('ORD202511270002', 2, 1598.00, 'SHIPPED', '北京市朝阳区建国门外大街1号', '微信支付'),
('ORD202511270003', 2, 299.00, 'PENDING', '北京市朝阳区建国门外大街1号', '支付宝');

-- 为第一个订单添加订单项（iPhone 15 + AirPods）
INSERT INTO order_items (order_id, product_id, quantity, price, subtotal) VALUES 
(1, 1, 1, 3999.00, 3999.00),  -- iPhone 15
(1, 2, 1, 399.00, 399.00);    -- AirPods

-- 为第二个订单添加订单项（MacBook Air）
INSERT INTO order_items (order_id, product_id, quantity, price, subtotal) VALUES 
(2, 3, 1, 1598.00, 1598.00);  -- MacBook Air

-- 为第三个订单添加订单项（台灯）
INSERT INTO order_items (order_id, product_id, quantity, price, subtotal) VALUES 
(3, 18, 1, 299.00, 299.00);   -- 台灯

-- 重新启用外键检查
SET FOREIGN_KEY_CHECKS = 1;


-- ==================== 第三部分：数据验证 ====================

-- 显示创建成功的表
SHOW TABLES;

-- 显示各表的数据统计
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

-- 验证商品数据完整性
SELECT 
    p.id, 
    p.name, 
    p.price,
    p.stock,
    c.name as category_name
FROM products p
JOIN categories c ON p.category_id = c.id
ORDER BY p.category_id, p.id;

-- 验证用户数据
SELECT id, username, email, role FROM users ORDER BY id;

-- 完成提示
SELECT '数据库初始化完成！' as status,
       '2个用户，4个分类，25个商品' as data_summary,
       '管理员: admin/123456' as admin_account,
       '普通用户: user1/123456' as user_account;

ALTER TABLE users ADD COLUMN avatar_url VARCHAR(255) AFTER address;