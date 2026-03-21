# 面馆多分店支出记账系统 API 契约

## 1. 通用约定

### 1.1 Base URL

- 后端统一前缀：`/api`

### 1.2 统一响应

```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": {}
}
```

### 1.3 分页响应

```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": {
    "list": [],
    "total": 0,
    "pageNo": 1,
    "pageSize": 10,
    "extra": {}
  }
}
```

### 1.4 统一错误码

- `SUCCESS`
- `UNAUTHORIZED`
- `FORBIDDEN`
- `VALIDATION_ERROR`
- `DATA_NOT_FOUND`
- `STORE_DISABLED`
- `USER_DISABLED`
- `CATEGORY_DISABLED`
- `EXPENSE_ALREADY_DELETED`
- `TOKEN_INVALID`
- `TOKEN_EXPIRED`
- `REFRESH_TOKEN_INVALID`
- `SYSTEM_ERROR`

### 1.5 鉴权头

- `Authorization: Bearer <accessToken>`

### 1.6 时间与金额

- 时区固定：`Asia/Shanghai`
- 日期字段格式：`yyyy-MM-dd`
- 日期时间字段格式：`yyyy-MM-dd HH:mm:ss`
- 金额：保留 2 位小数
- 数量：保留 3 位小数

## 2. 角色与权限

- `OWNER`：老板
- `CLERK`：录入员

规则：

- `CLERK` 必须绑定一个有效分店。
- `CLERK` 新增支出时，后端忽略前端传入的 `storeId`，强制使用绑定分店。
- `CLERK` 查询、编辑、删除支出时，只能访问绑定分店的数据。
- `OWNER` 可跨分店查看与维护数据。

## 3. 鉴权接口

### 3.1 登录

- `POST /api/auth/login`

请求：

```json
{
  "username": "owner",
  "password": "123456"
}
```

响应：

```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": {
    "accessToken": "jwt-access-token",
    "refreshToken": "jwt-refresh-token",
    "expiresIn": 7200,
    "userInfo": {
      "id": 1,
      "username": "owner",
      "displayName": "老板",
      "role": "OWNER",
      "storeId": null,
      "storeName": null,
      "status": "ENABLED"
    }
  }
}
```

### 3.2 刷新 Token

- `POST /api/auth/refresh`

请求：

```json
{
  "refreshToken": "jwt-refresh-token"
}
```

响应结构同登录。

### 3.3 退出登录

- `POST /api/auth/logout`

请求：

```json
{
  "refreshToken": "jwt-refresh-token"
}
```

### 3.4 当前登录人

- `GET /api/auth/me`

响应：

```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": {
    "id": 1,
    "username": "owner",
    "displayName": "老板",
    "role": "OWNER",
    "storeId": null,
    "storeName": null,
    "status": "ENABLED"
  }
}
```

## 4. 分店接口

### 4.1 分店列表

- `GET /api/stores`

查询参数：

- `keyword`
- `status`
- `pageNo`
- `pageSize`

列表项：

```json
{
  "id": 1,
  "name": "总店",
  "code": "STORE001",
  "status": "ENABLED",
  "createdAt": "2026-03-21 12:00:00",
  "updatedAt": "2026-03-21 12:00:00"
}
```

### 4.2 新增分店

- `POST /api/stores`

```json
{
  "name": "南坪店",
  "code": "NP001",
  "status": "ENABLED"
}
```

### 4.3 编辑分店

- `PUT /api/stores/{id}`

### 4.4 停用/启用分店

- `PATCH /api/stores/{id}/status`

```json
{
  "status": "DISABLED"
}
```

### 4.5 可用分店下拉

- `GET /api/stores/options`

## 5. 账号接口

### 5.1 账号列表

- `GET /api/users`

查询参数：

- `keyword`
- `role`
- `status`
- `storeId`
- `pageNo`
- `pageSize`

列表项：

```json
{
  "id": 1,
  "username": "clerk01",
  "displayName": "张三",
  "role": "CLERK",
  "storeId": 1,
  "storeName": "总店",
  "status": "ENABLED",
  "createdAt": "2026-03-21 12:00:00",
  "updatedAt": "2026-03-21 12:00:00"
}
```

### 5.2 新增账号

- `POST /api/users`

```json
{
  "username": "clerk01",
  "password": "123456",
  "displayName": "张三",
  "role": "CLERK",
  "storeId": 1,
  "status": "ENABLED"
}
```

### 5.3 编辑账号

- `PUT /api/users/{id}`

### 5.4 修改账号状态

- `PATCH /api/users/{id}/status`

### 5.5 重置密码

- `PATCH /api/users/{id}/password`

```json
{
  "password": "123456"
}
```

## 6. 分类接口

### 6.1 分类树

- `GET /api/categories/tree`

响应：

```json
[
  {
    "id": 1,
    "parentId": 0,
    "level": 1,
    "name": "食材",
    "code": "FOOD",
    "defaultUnit": null,
    "sortNo": 1,
    "status": "ENABLED",
    "children": [
      {
        "id": 2,
        "parentId": 1,
        "level": 2,
        "name": "牛肉类",
        "code": "BEEF",
        "defaultUnit": "斤",
        "sortNo": 1,
        "status": "ENABLED",
        "children": []
      }
    ]
  }
]
```

### 6.2 分类列表

- `GET /api/categories`

### 6.3 新增分类

- `POST /api/categories`

```json
{
  "parentId": 1,
  "level": 2,
  "name": "猪肉类",
  "code": "PORK",
  "defaultUnit": "斤",
  "sortNo": 2,
  "status": "ENABLED"
}
```

### 6.4 编辑分类

- `PUT /api/categories/{id}`

### 6.5 修改分类状态

- `PATCH /api/categories/{id}/status`

## 7. 支出接口

### 7.1 支出列表

- `GET /api/expenses`

查询参数：

- `storeId`
- `dateStart`
- `dateEnd`
- `categoryLevel1Id`
- `categoryLevel2Id`
- `itemName`
- `pageNo`
- `pageSize`

响应：

```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": {
    "list": [
      {
        "id": 1,
        "storeId": 1,
        "storeName": "总店",
        "expenseDate": "2026-03-21",
        "categoryLevel1Id": 1,
        "categoryLevel1Name": "食材",
        "categoryLevel2Id": 2,
        "categoryLevel2Name": "牛肉类",
        "itemName": "牛腩",
        "amount": 236.50,
        "quantity": 20.000,
        "unit": "斤",
        "remark": "早市采购",
        "createdBy": 1,
        "createdByName": "老板",
        "updatedBy": 1,
        "updatedByName": "老板",
        "createdAt": "2026-03-21 12:00:00",
        "updatedAt": "2026-03-21 12:00:00"
      }
    ],
    "total": 1,
    "pageNo": 1,
    "pageSize": 10,
    "extra": {
      "totalAmount": 236.50
    }
  }
}
```

### 7.2 支出详情

- `GET /api/expenses/{id}`

### 7.3 新增支出

- `POST /api/expenses`

```json
{
  "storeId": 1,
  "expenseDate": "2026-03-21",
  "categoryLevel1Id": 1,
  "categoryLevel2Id": 2,
  "itemName": "牛腩",
  "amount": 236.50,
  "quantity": 20.000,
  "unit": "斤",
  "remark": "早市采购"
}
```

### 7.4 编辑支出

- `PUT /api/expenses/{id}`

请求结构同新增。

### 7.5 删除支出

- `DELETE /api/expenses/{id}`

### 7.6 支出修改历史

- `GET /api/expenses/{id}/history`

响应项：

```json
{
  "id": 1,
  "expenseRecordId": 1,
  "action": "UPDATE",
  "beforeSnapshot": {},
  "afterSnapshot": {},
  "operatorId": 1,
  "operatorName": "老板",
  "operateTime": "2026-03-21 12:00:00"
}
```

## 8. 报表接口

### 8.1 老板首页总览

- `GET /api/reports/overview`

查询参数：

- `storeId`
- `dateStart`
- `dateEnd`

响应：

```json
{
  "todayAmount": 1000.00,
  "monthAmount": 30000.00,
  "rangeAmount": 5000.00,
  "storeCount": 3,
  "topStoreName": "总店",
  "topStoreAmount": 2600.00
}
```

### 8.2 趋势图

- `GET /api/reports/trend`

查询参数：

- `storeId`
- `dateStart`
- `dateEnd`
- `categoryLevel1Id`
- `categoryLevel2Id`

响应：

```json
[
  {
    "label": "2026-03-21",
    "amount": 1000.00
  }
]
```

### 8.3 分类占比

- `GET /api/reports/category-breakdown`

查询参数：

- `storeId`
- `dateStart`
- `dateEnd`
- `categoryLevel`

响应：

```json
[
  {
    "categoryId": 1,
    "categoryName": "食材",
    "amount": 3000.00,
    "ratio": 0.60
  }
]
```

### 8.4 品项排行

- `GET /api/reports/item-ranking`

查询参数：

- `storeId`
- `dateStart`
- `dateEnd`
- `categoryLevel1Id`
- `categoryLevel2Id`
- `topN`

响应：

```json
[
  {
    "itemName": "牛腩",
    "amount": 1500.00,
    "rankNo": 1
  }
]
```

### 8.5 分店对比

- `GET /api/reports/store-comparison`

查询参数：

- `dateStart`
- `dateEnd`
- `categoryLevel1Id`
- `categoryLevel2Id`

响应：

```json
[
  {
    "storeId": 1,
    "storeName": "总店",
    "amount": 3000.00,
    "rankNo": 1
  }
]
```

## 9. 落库模型约束

### 9.1 store

- `id`
- `name`
- `code`
- `status`
- `created_at`
- `updated_at`

### 9.2 sys_user

- `id`
- `username`
- `password_hash`
- `display_name`
- `role`
- `store_id`
- `status`
- `token_version`
- `created_at`
- `updated_at`

### 9.3 expense_category

- `id`
- `parent_id`
- `level`
- `name`
- `code`
- `default_unit`
- `sort_no`
- `status`
- `created_at`
- `updated_at`

### 9.4 expense_record

- `id`
- `store_id`
- `expense_date`
- `category_level1_id`
- `category_level1_name`
- `category_level2_id`
- `category_level2_name`
- `item_name`
- `amount`
- `quantity`
- `unit`
- `remark`
- `created_by`
- `updated_by`
- `deleted`
- `version`
- `created_at`
- `updated_at`

### 9.5 expense_record_history

- `id`
- `expense_record_id`
- `action`
- `before_snapshot`
- `after_snapshot`
- `operator_id`
- `operator_name`
- `operate_time`

### 9.6 auth_refresh_token

- `id`
- `user_id`
- `token_hash`
- `expires_at`
- `revoked`
- `created_at`
- `updated_at`

## 10. 初始化分类

初始化一级分类：

- 食材
- 调料/配料
- 耗材
- 费用
- 其他

初始化二级分类至少覆盖：

- 牛肉类
- 猪肉类
- 鸡鸭蛋类
- 大骨类
- 面类
- 蔬菜类
- 米油类
- 日常调味
- 香料干货
- 干货类
- 腌制配菜
- 豆制品类
- 打包耗材
- 办公耗材
- 清洁耗材
- 固定费用
- 经营费用
- 员工相关
- 其他支出
