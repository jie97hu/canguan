CREATE TABLE IF NOT EXISTS expense_record (
    id                    BIGINT         NOT NULL AUTO_INCREMENT PRIMARY KEY,
    store_id              BIGINT         NOT NULL,
    expense_date          DATE           NOT NULL,
    category_level1_id    BIGINT         NOT NULL,
    category_level1_name  VARCHAR(128)   NOT NULL,
    category_level2_id    BIGINT         NOT NULL,
    category_level2_name  VARCHAR(128)   NOT NULL,
    item_name             VARCHAR(128)   NOT NULL,
    amount                DECIMAL(12, 2) NOT NULL,
    quantity              DECIMAL(12, 3) NULL,
    unit                  VARCHAR(32)    NULL,
    remark                VARCHAR(255)   NULL,
    created_by            BIGINT         NOT NULL,
    updated_by            BIGINT         NOT NULL,
    deleted               TINYINT(1)     NOT NULL DEFAULT 0,
    version               BIGINT         NULL DEFAULT 0,
    created_at            DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at            DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_expense_record_store_date (store_id, expense_date),
    KEY idx_expense_record_date (expense_date),
    KEY idx_expense_record_category2_date (category_level2_id, expense_date),
    KEY idx_expense_record_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS expense_record_history (
    id                BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    expense_record_id BIGINT       NOT NULL,
    action            VARCHAR(16)  NOT NULL,
    before_snapshot   LONGTEXT     NULL,
    after_snapshot    LONGTEXT     NULL,
    operator_id       BIGINT       NOT NULL,
    operator_name     VARCHAR(128) NOT NULL,
    operate_time      DATETIME     NOT NULL,
    KEY idx_expense_history_record_time (expense_record_id, operate_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
