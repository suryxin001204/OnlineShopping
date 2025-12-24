https://mermaid.live/
```mermaid
erDiagram
    %% 核心实体
    users {
        int id PK "用户ID"
        varchar username "用户名"
        varchar password "密码"
        varchar role "角色"
    }
    
    products {
        int id PK "商品ID"
        varchar name "商品名称"
        int price "价格(分)"
        int stock "库存"
        int category_id FK "分类ID"
    }

    orders {
        int id PK "订单ID"
        varchar order_number "订单号"
        int user_id FK "用户ID"
        int total_amount "总金额(分)"
        varchar status "状态"
    }

    %% 关联实体
    categories {
        int id PK "分类ID"
        varchar name "分类名称"
        int parent_id FK "父分类ID"
    }

    cart_items {
        int id PK "购物车ID"
        int user_id FK "用户ID"
        int product_id FK "商品ID"
        int quantity "数量"
    }

    order_items {
        int id PK "详情ID"
        int order_id FK "订单ID"
        int product_id FK "商品ID"
        int quantity "数量"
        int price "单价(分)"
    }

    %% 关系定义
    users ||--o{ orders : places
    users ||--o{ cart_items : owns

    categories ||--o{ products : contains
    categories ||--o{ categories : parent
    
    products ||--o{ cart_items : in
    products ||--o{ order_items : in
    orders ||--|{ order_items : has
```





时序图
sequenceDiagram
    participant User as 用户 (Client)
    participant Controller as OrderController
    participant Service as OrderService
    participant ProductService as ProductService
    participant Repo as OrderRepository
    participant DB as Database

    User->>Controller: POST /api/orders (下单请求)
    activate Controller
    Controller->>Service: createOrder(userId, address)
    activate Service
    
    Note over Service: @Transactional 开启事务

    Service->>DB: 查询用户购物车项 (cartItems)
    
    loop 遍历购物车每一项
        Service->>ProductService: decreaseStock(productId, quantity)
        activate ProductService
        ProductService->>DB: 检查库存并扣减
        alt 库存不足
            ProductService-->>Service: 抛出异常
            Service-->>Controller: 返回错误信息
            Controller-->>User: 提示"库存不足"
        else 库存充足
            ProductService-->>Service: 扣减成功
        end
        deactivate ProductService
    end

    Service->>Service: 计算订单总价
    Service->>Repo: save(order) (保存订单主表)
    activate Repo
    Repo->>DB: INSERT INTO orders
    Repo-->>Service: 返回持久化对象
    deactivate Repo

    Service->>DB: saveAll(orderItems) (保存订单详情)
    Service->>DB: deleteCartItems(userId) (清空购物车)

    Service-->>Controller: 返回 OrderDTO
    deactivate Service
    
    Controller-->>User: 200 OK (下单成功)
    deactivate Controller





    
