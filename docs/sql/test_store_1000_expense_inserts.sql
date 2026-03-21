-- 测试门店与 1000 条支出测试数据脚本
-- 适用库: MySQL 8 / Myapp
-- 说明:
-- 1. 门店编码固定为 TEST_STORE，重复执行不会重复创建门店。
-- 2. 支出数据每次执行都会新增 1000 条，如需重复执行请先自行清理该门店测试数据。
-- 3. 支出日期会均匀分布在 2026-01-01 到执行当天之间。
-- 4. created_by / updated_by 优先取已启用 OWNER 账号；若不存在则退化为首个启用账号；再退化为 1。

-- 显式固定当前会话的连接排序规则，避免与业务表 utf8mb4_general_ci 比较时出现 collation 冲突。
SET NAMES utf8mb4 COLLATE utf8mb4_general_ci;
SET collation_connection = 'utf8mb4_general_ci';

SET @test_store_code := CONVERT('TEST_STORE' USING utf8mb4) COLLATE utf8mb4_general_ci;
SET @test_store_name := CONVERT('测试门店' USING utf8mb4) COLLATE utf8mb4_general_ci;
SET @start_date := DATE('2026-01-01');
SET @end_date := CURDATE();
SET @day_span := GREATEST(DATEDIFF(@end_date, @start_date), 0);

INSERT INTO store (name, code, status)
VALUES (@test_store_name, @test_store_code, 'ENABLED')
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    status = VALUES(status),
    updated_at = CURRENT_TIMESTAMP;

SET @test_store_id := (
    SELECT id
    FROM store
    WHERE code = (@test_store_code COLLATE utf8mb4_general_ci)
    ORDER BY id
    LIMIT 1
);

SET @operator_id := COALESCE(
    (
        SELECT id
        FROM sys_user
        WHERE role = ('OWNER' COLLATE utf8mb4_general_ci)
          AND status = ('ENABLED' COLLATE utf8mb4_general_ci)
        ORDER BY id
        LIMIT 1
    ),
    (
        SELECT id
        FROM sys_user
        WHERE status = ('ENABLED' COLLATE utf8mb4_general_ci)
        ORDER BY id
        LIMIT 1
    ),
    1
);

INSERT INTO expense_record (
    store_id,
    expense_date,
    category_level1_id,
    category_level1_name,
    category_level2_id,
    category_level2_name,
    item_name,
    amount,
    quantity,
    unit,
    remark,
    created_by,
    updated_by,
    deleted,
    version
)
SELECT
    @test_store_id AS store_id,
    DATE_ADD(@start_date, INTERVAL FLOOR(((seq.seq_no - 1) * @day_span) / 999) DAY) AS expense_date,
    seed.category_level1_id,
    seed.category_level1_name,
    seed.category_level2_id,
    seed.category_level2_name,
    seed.item_name,
    ROUND(seed.amount_base + (MOD(seq.seq_no * 37 + seed.seed_no * 53, seed.amount_range * 100) / 100), 2) AS amount,
    CASE
        WHEN seed.quantity_enabled = 1 THEN ROUND(seed.quantity_base + (MOD(seq.seq_no * 17 + seed.seed_no * 11, seed.quantity_range * 1000) / 1000), 3)
        ELSE NULL
    END AS quantity,
    seed.unit,
    CONCAT('自动生成测试数据-测试门店-', DATE_FORMAT(DATE_ADD(@start_date, INTERVAL FLOOR(((seq.seq_no - 1) * @day_span) / 999) DAY), '%Y%m%d'), '-#', LPAD(seq.seq_no, 4, '0')) AS remark,
    @operator_id AS created_by,
    @operator_id AS updated_by,
    0 AS deleted,
    0 AS version
FROM (
    SELECT
        ones.n + tens.n * 10 + hundreds.n * 100 + 1 AS seq_no
    FROM
        (
            SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
            UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9
        ) ones
        CROSS JOIN (
            SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
            UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9
        ) tens
        CROSS JOIN (
            SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
            UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9
        ) hundreds
    ORDER BY seq_no
    LIMIT 1000
) seq
JOIN (
    SELECT 0 AS seed_no, 1 AS category_level1_id, '食材' AS category_level1_name, 2 AS category_level2_id, '牛肉类' AS category_level2_name, '牛腩' AS item_name, '斤' AS unit, 180 AS amount_base, 320 AS amount_range, 8 AS quantity_base, 16 AS quantity_range, 1 AS quantity_enabled
    UNION ALL
    SELECT 1, 1, '食材', 3, '猪肉类', '里脊肉', '斤', 95, 180, 10, 14, 1
    UNION ALL
    SELECT 2, 1, '食材', 6, '面类', '细面', '斤', 60, 120, 18, 22, 1
    UNION ALL
    SELECT 3, 1, '食材', 7, '蔬菜类', '小葱', '斤', 22, 50, 5, 9, 1
    UNION ALL
    SELECT 4, 9, '调料/配料', 10, '日常调味', '豆瓣酱', '袋', 28, 60, 4, 7, 1
    UNION ALL
    SELECT 5, 9, '调料/配料', 11, '香料干货', '花椒', '袋', 35, 80, 3, 6, 1
    UNION ALL
    SELECT 6, 15, '耗材', 16, '打包耗材', '一次性餐盒', '箱', 120, 240, 2, 5, 1
    UNION ALL
    SELECT 7, 15, '耗材', 18, '清洁耗材', '洗洁精', '瓶', 24, 48, 4, 8, 1
    UNION ALL
    SELECT 8, 19, '费用', 20, '固定费用', '房租', '月', 4800, 3600, 0, 0, 0
    UNION ALL
    SELECT 9, 19, '费用', 21, '经营费用', '维修费', '次', 180, 1200, 0, 0, 0
    UNION ALL
    SELECT 10, 22, '其他', 23, '员工相关', '员工餐', '次', 70, 180, 0, 0, 0
    UNION ALL
    SELECT 11, 22, '其他', 24, '其他支出', '价目表制作', '次', 50, 260, 0, 0, 0
) seed
  ON seed.seed_no = MOD(seq.seq_no - 1, 12);

-- 可选校验:
-- SELECT id, code, name, status FROM store WHERE code = 'TEST_STORE';
-- SELECT COUNT(*) AS cnt, MIN(expense_date) AS min_date, MAX(expense_date) AS max_date
-- FROM expense_record
-- WHERE store_id = @test_store_id;
