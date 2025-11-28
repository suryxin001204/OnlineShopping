-- -- Initial data for MySQL (idempotent via INSERT IGNORE / subselects)

-- -- Users (BCrypt hash from provided script)
-- INSERT IGNORE INTO users (username, password, email, role)
-- VALUES 
--   ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTV6UiC', 'admin@shop.com', 'ROLE_ADMIN'),
--   ('user1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTV6UiC', 'user1@shop.com', 'ROLE_USER');

-- -- Categories
-- INSERT IGNORE INTO categories (name, description)
-- VALUES 
--   ('电子产品', '手机、电脑、平板等'),
--   ('服装', '男女服装、鞋帽等'),
--   ('家居', '家具、家居装饰等'),
--   ('图书', '各类图书、杂志等');

-- -- Products (use subselect to resolve category_id by name)
-- INSERT IGNORE INTO products (name, description, price, stock, image_url, category_id, status)
-- SELECT 'iPhone 14', '最新款苹果手机', 5999.00, 100, '/images/iphone14.jpg', c.id, TRUE FROM categories c WHERE c.name='电子产品' LIMIT 1;
-- INSERT IGNORE INTO products (name, description, price, stock, image_url, category_id, status)
-- SELECT 'MacBook Pro', '苹果笔记本电脑', 12999.00, 50, '/images/macbook.jpg', c.id, TRUE FROM categories c WHERE c.name='电子产品' LIMIT 1;
-- INSERT IGNORE INTO products (name, description, price, stock, image_url, category_id, status)
-- SELECT '牛仔裤', '经典蓝色牛仔裤', 199.00, 200, '/images/jeans.jpg', c.id, TRUE FROM categories c WHERE c.name='服装' LIMIT 1;
-- INSERT IGNORE INTO products (name, description, price, stock, image_url, category_id, status)
-- SELECT '沙发', '舒适布艺沙发', 2999.00, 20, '/images/sofa.jpg', c.id, TRUE FROM categories c WHERE c.name='家居' LIMIT 1;
-- INSERT IGNORE INTO products (name, description, price, stock, image_url, category_id, status)
-- SELECT 'Java编程思想', '经典编程书籍', 89.00, 150, '/images/java-book.jpg', c.id, TRUE FROM categories c WHERE c.name='图书' LIMIT 1;
