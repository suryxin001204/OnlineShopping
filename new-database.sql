-- ==========================================================
-- 网上购物系统数据库初始化脚本 (v2.0.0)
-- 包含：表结构、索引、视图、存储过程、触发器、初始化数据
-- 更新时间: 2025-12-23
-- ==========================================================

DROP DATABASE IF EXISTS online_shopping;
CREATE DATABASE online_shopping CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE online_shopping;

-- ----------------------------------------------------------
-- 1. 创建用户表
-- ----------------------------------------------------------
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    username VARCHAR(50) UNIQUE NOT NULL COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    email VARCHAR(100) COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '电话',
    address VARCHAR(255) COMMENT '地址',
    avatar_url VARCHAR(255) COMMENT '头像URL',
    role VARCHAR(20) DEFAULT 'ROLE_USER' COMMENT '角色',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB COMMENT='用户表';

-- 创建索引
CREATE INDEX idx_user_username ON users(username);
CREATE INDEX idx_user_email ON users(email);

-- ----------------------------------------------------------
-- 2. 创建分类表
-- ----------------------------------------------------------
CREATE TABLE categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '分类ID',
    name VARCHAR(100) NOT NULL UNIQUE COMMENT '分类名称',
    description VARCHAR(255) COMMENT '分类描述',
    parent_id BIGINT COMMENT '父分类ID',
    status BOOLEAN DEFAULT TRUE COMMENT '状态',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (parent_id) REFERENCES categories(id) ON DELETE SET NULL
) ENGINE=InnoDB COMMENT='商品分类表';

-- ----------------------------------------------------------
-- 3. 创建商品表
-- ----------------------------------------------------------
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
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL
) ENGINE=InnoDB COMMENT='商品表';

-- 创建索引
CREATE INDEX idx_product_name ON products(name);
CREATE INDEX idx_product_category ON products(category_id);
CREATE INDEX idx_product_price ON products(price);

-- ----------------------------------------------------------
-- 4. 创建购物车表
-- ----------------------------------------------------------
CREATE TABLE cart_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '购物车项ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    quantity INT NOT NULL COMMENT '商品数量',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY unique_user_product (user_id, product_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='购物车表';

-- ----------------------------------------------------------
-- 5. 创建订单表
-- ----------------------------------------------------------
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
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT
) ENGINE=InnoDB COMMENT='订单表';

-- 创建索引
CREATE INDEX idx_order_number ON orders(order_number);
CREATE INDEX idx_order_user ON orders(user_id);
CREATE INDEX idx_order_status ON orders(status);
CREATE INDEX idx_order_create_time ON orders(create_time);

-- ----------------------------------------------------------
-- 6. 创建订单项表
-- ----------------------------------------------------------
CREATE TABLE order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '订单项ID',
    order_id BIGINT NOT NULL COMMENT '订单ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    quantity INT NOT NULL COMMENT '商品数量',
    price DECIMAL(10,2) NOT NULL COMMENT '商品单价',
    subtotal DECIMAL(10,2) NOT NULL COMMENT '小计金额',
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE RESTRICT
) ENGINE=InnoDB COMMENT='订单项表';

-- ----------------------------------------------------------
-- 7. 创建日志表 (用于触发器)
-- ----------------------------------------------------------
CREATE TABLE product_price_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    old_price DECIMAL(10,2),
    new_price DECIMAL(10,2),
    change_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_log_product (product_id)
) COMMENT='商品价格变动日志表';

-- ----------------------------------------------------------
-- 8. 创建视图 (Views)
-- ----------------------------------------------------------

-- 视图1: 订单详情完整视图 (包含用户名、商品名)
CREATE VIEW vw_order_details AS
SELECT 
    o.order_number,
    o.create_time,
    o.status,
    u.username,
    p.name AS product_name,
    oi.quantity,
    oi.price,
    oi.subtotal
FROM orders o
JOIN users u ON o.user_id = u.id
JOIN order_items oi ON o.id = oi.order_id
JOIN products p ON oi.product_id = p.id;

-- 视图2: 商品销售统计视图
CREATE VIEW vw_product_sales_stats AS
SELECT 
    p.id AS product_id,
    p.name,
    c.name AS category_name,
    COUNT(oi.id) AS total_orders,
    SUM(oi.quantity) AS total_sold_count,
    SUM(oi.subtotal) AS total_revenue
FROM products p
LEFT JOIN order_items oi ON p.id = oi.product_id
LEFT JOIN categories c ON p.category_id = c.id
GROUP BY p.id, p.name, c.name;

-- ----------------------------------------------------------
-- 9. 创建存储过程 (Stored Procedures)
-- ----------------------------------------------------------
DELIMITER $$

-- 存储过程: 检查库存
CREATE PROCEDURE proc_check_stock(
    IN p_product_id BIGINT,
    OUT p_stock INT,
    OUT p_status VARCHAR(50)
)
BEGIN
    SELECT stock INTO p_stock FROM products WHERE id = p_product_id;
    
    IF p_stock > 10 THEN
        SET p_status = '充足';
    ELSEIF p_stock > 0 THEN
        SET p_status = '紧张';
    ELSE
        SET p_status = '缺货';
    END IF;
END$$

-- 存储过程: 统计指定日期的销售额
CREATE PROCEDURE proc_daily_sales_report(
    IN p_date DATE,
    OUT p_total_sales DECIMAL(10,2)
)
BEGIN
    SELECT COALESCE(SUM(total_amount), 0) INTO p_total_sales
    FROM orders
    WHERE DATE(create_time) = p_date;
END$$

DELIMITER ;

-- ----------------------------------------------------------
-- 10. 创建触发器 (Triggers)
-- ----------------------------------------------------------
DELIMITER $$

-- 触发器: 记录商品价格变动
CREATE TRIGGER trg_product_price_log
AFTER UPDATE ON products
FOR EACH ROW
BEGIN
    IF OLD.price != NEW.price THEN
        INSERT INTO product_price_logs (product_id, old_price, new_price)
        VALUES (NEW.id, OLD.price, NEW.price);
    END IF;
END$$

DELIMITER ;

-- ----------------------------------------------------------
-- 11. 初始化数据
-- ----------------------------------------------------------

-- 临时禁用外键检查
SET FOREIGN_KEY_CHECKS = 0;

-- 插入用户
INSERT INTO users (username, password, email, role, create_time) VALUES 
('admin', '123456', 'admin@shop.com', 'ROLE_ADMIN', '2025-12-22 10:00:00'),
('user1', '123456', 'user1@shop.com', 'ROLE_USER', '2025-12-22 10:00:00');

-- 插入分类
INSERT INTO categories (name, description, create_time) VALUES 
('电子产品', '手机、电脑、平板等', '2025-12-22 10:00:00'),
('服装', '男女服装、鞋帽等', '2025-12-22 10:00:00'),
('家居', '家具、家居装饰等', '2025-12-22 10:00:00'),
('图书', '各类图书、杂志等', '2025-12-22 10:00:00');

-- 插入商品
INSERT INTO products (name, description, price, stock, image_url, category_id, status, create_time) VALUES 
-- 原始商品
('iPhone 17', '最新款苹果手机', 5999.00, 100, '/images/1.jpg', 1, 1, '2025-12-22 10:00:00'),
('MacBook Pro', '苹果笔记本电脑', 12999.00, 50, '/images/2.jpg', 1, 1, '2025-12-22 10:00:00'),
('牛仔裤', '经典蓝色牛仔裤', 199.00, 200, '/images/3.jpg', 2, 1, '2025-12-22 10:00:00'),
('沙发', '舒适布艺沙发', 2999.00, 20, '/images/4.jpg', 3, 1, '2025-12-22 10:00:00'),
('Java编程思想', '经典编程书籍', 89.00, 150, '/images/5.jpg', 4, 1, '2025-12-22 10:00:00'),
-- 扩展商品
('iPad Pro', 'Apple平板电脑', 6999.00, 80, '/images/6.jpg', 1, 1, '2025-12-22 10:00:00'),
('AirPods Pro', '苹果无线耳机', 1999.00, 200, '/images/7.jpg', 1, 1, '2025-12-22 10:00:00'),
('小米13', '小米旗舰手机', 3999.00, 150, '/images/8.jpg', 1, 1, '2025-12-22 10:00:00'),
('华为MateBook', '华为笔记本电脑', 7999.00, 60, '/images/9.jpg', 1, 1, '2025-12-22 10:00:00'),
('索尼相机', '专业单反相机', 9999.00, 30, '/images/10.jpg', 1, 1, '2025-12-22 10:00:00'),
('T恤', '纯棉短袖T恤', 89.00, 300, '/images/11.jpg', 2, 1, '2025-12-22 10:00:00'),
('连衣裙', '夏季清新连衣裙', 299.00, 150, '/images/12.jpg', 2, 1, '2025-12-22 10:00:00'),
('运动鞋', '透气跑步鞋', 399.00, 200, '/images/13.jpg', 2, 1, '2025-12-22 10:00:00'),
('羽绒服', '冬季保暖羽绒服', 899.00, 100, '/images/14.jpg', 2, 1, '2025-12-22 10:00:00'),
('帆布包', '文艺帆布单肩包', 159.00, 180, '/images/15.jpg', 2, 1, '2025-12-22 10:00:00'),
('床垫', '记忆棉床垫', 1999.00, 50, '/images/16.jpg', 3, 1, '2025-12-22 10:00:00'),
('书桌', '简约电脑桌', 599.00, 80, '/images/17.jpg', 3, 1, '2025-12-22 10:00:00'),
('台灯', 'LED护眼台灯', 199.00, 120, '/images/18.jpg', 3, 1, '2025-12-22 10:00:00'),
('收纳箱', '塑料整理箱', 49.00, 500, '/images/19.jpg', 3, 1, '2025-12-22 10:00:00'),
('窗帘', '遮光卧室窗帘', 299.00, 100, '/images/20.jpg', 3, 1, '2025-12-22 10:00:00'),
('Python编程', 'Python入门教程', 79.00, 200, '/images/21.jpg', 4, 1, '2025-12-22 10:00:00'),
('算法导论', '经典算法书籍', 128.00, 100, '/images/22.jpg', 4, 1, '2025-12-22 10:00:00'),
('设计模式', '软件设计模式', 99.00, 150, '/images/23.jpg', 4, 1, '2025-12-22 10:00:00'),
('人类简史', '历史畅销书', 68.00, 300, '/images/24.jpg', 4, 1, '2025-12-22 10:00:00'),
('三体', '科幻小说', 59.00, 400, '/images/25.jpg', 4, 1, '2025-12-22 10:00:00');

-- 插入订单 (时间更新为 12-22 和 12-23)
INSERT INTO orders (order_number, user_id, total_amount, status, shipping_address, payment_method, create_time) VALUES 
('ORD202512220001', 2, 4398.00, 'DELIVERED', '北京市朝阳区建国门外大街1号', '支付宝', '2025-12-22 14:30:00'),
('ORD202512230001', 2, 1999.00, 'SHIPPED', '北京市朝阳区建国门外大街1号', '微信支付', '2025-12-23 09:15:00'),
('ORD202512230002', 2, 299.00, 'PENDING', '北京市朝阳区建国门外大街1号', '支付宝', '2025-12-23 16:45:00');

-- 插入订单项
INSERT INTO order_items (order_id, product_id, quantity, price, subtotal) VALUES 
(1, 8, 1, 3999.00, 3999.00),  -- 小米13
(1, 13, 1, 399.00, 399.00),   -- 运动鞋
(2, 7, 1, 1999.00, 1999.00),  -- AirPods Pro
(3, 20, 1, 299.00, 299.00);   -- 窗帘

-- 恢复外键检查
SET FOREIGN_KEY_CHECKS = 1;

-- ----------------------------------------------------------
-- 12. 完成验证
-- ----------------------------------------------------------
SELECT '数据库初始化完成 (v2.0.0)' as status;
