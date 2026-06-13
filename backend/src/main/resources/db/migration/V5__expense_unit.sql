CREATE TABLE IF NOT EXISTS expense_unit (
    id         BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(32)  NOT NULL,
    sort_no    INT          NOT NULL DEFAULT 0,
    status     VARCHAR(16)  NOT NULL DEFAULT 'ENABLED',
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_expense_unit_name (name),
    KEY idx_expense_unit_status_sort (status, sort_no, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

INSERT IGNORE INTO expense_unit (name, sort_no, status)
VALUES
    ('克', 1, 'ENABLED'),
    ('千克', 2, 'ENABLED'),
    ('公斤', 3, 'ENABLED'),
    ('斤', 4, 'ENABLED'),
    ('两', 5, 'ENABLED'),
    ('吨', 6, 'ENABLED'),
    ('毫升', 7, 'ENABLED'),
    ('升', 8, 'ENABLED'),
    ('立方米', 9, 'ENABLED'),
    ('毫米', 10, 'ENABLED'),
    ('厘米', 11, 'ENABLED'),
    ('米', 12, 'ENABLED'),
    ('个', 13, 'ENABLED'),
    ('只', 14, 'ENABLED'),
    ('件', 15, 'ENABLED'),
    ('条', 16, 'ENABLED'),
    ('根', 17, 'ENABLED'),
    ('块', 18, 'ENABLED'),
    ('片', 19, 'ENABLED'),
    ('张', 20, 'ENABLED'),
    ('页', 21, 'ENABLED'),
    ('本', 22, 'ENABLED'),
    ('册', 23, 'ENABLED'),
    ('支', 24, 'ENABLED'),
    ('把', 25, 'ENABLED'),
    ('串', 26, 'ENABLED'),
    ('捆', 27, 'ENABLED'),
    ('束', 28, 'ENABLED'),
    ('双', 29, 'ENABLED'),
    ('对', 30, 'ENABLED'),
    ('套', 31, 'ENABLED'),
    ('台', 32, 'ENABLED'),
    ('台套', 33, 'ENABLED'),
    ('包', 34, 'ENABLED'),
    ('袋', 35, 'ENABLED'),
    ('箱', 36, 'ENABLED'),
    ('盒', 37, 'ENABLED'),
    ('桶', 38, 'ENABLED'),
    ('瓶', 39, 'ENABLED'),
    ('罐', 40, 'ENABLED'),
    ('听', 41, 'ENABLED'),
    ('杯', 42, 'ENABLED'),
    ('筒', 43, 'ENABLED'),
    ('卷', 44, 'ENABLED'),
    ('提', 45, 'ENABLED'),
    ('板', 46, 'ENABLED'),
    ('排', 47, 'ENABLED'),
    ('托', 48, 'ENABLED'),
    ('盘', 49, 'ENABLED'),
    ('次', 50, 'ENABLED'),
    ('小时', 51, 'ENABLED'),
    ('天', 52, 'ENABLED'),
    ('周', 53, 'ENABLED'),
    ('月', 54, 'ENABLED'),
    ('季度', 55, 'ENABLED'),
    ('年', 56, 'ENABLED'),
    ('单', 57, 'ENABLED'),
    ('笔', 58, 'ENABLED'),
    ('项', 59, 'ENABLED');

INSERT IGNORE INTO expense_unit (name, sort_no, status)
SELECT DISTINCT TRIM(default_unit), 999, 'ENABLED'
FROM expense_category
WHERE default_unit IS NOT NULL
  AND TRIM(default_unit) <> '';

INSERT IGNORE INTO expense_unit (name, sort_no, status)
SELECT DISTINCT TRIM(unit), 999, 'ENABLED'
FROM expense_record
WHERE unit IS NOT NULL
  AND TRIM(unit) <> '';
