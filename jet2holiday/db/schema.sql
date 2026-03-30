CREATE TABLE account (
                         id BIGINT PRIMARY KEY AUTO_INCREMENT,
                         account_name VARCHAR(100) NOT NULL DEFAULT 'Default Portfolio Account',
                         cash_balance DECIMAL(19,4) NOT NULL DEFAULT 0.0000,
                         currency VARCHAR(10) DEFAULT 'USD',
                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);


CREATE TABLE portfolio_item (
                                id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                account_id BIGINT NOT NULL,
                                symbol VARCHAR(20) NOT NULL,
                                company_name VARCHAR(200) NOT NULL,
                                asset_type VARCHAR(20) NOT NULL,
                                shares DECIMAL(19,8) NOT NULL,
                                cost_basis DECIMAL(19,4) NOT NULL,
                                currency VARCHAR(10) DEFAULT 'USD',
                                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                CONSTRAINT fk_portfolio_item_account
                                    FOREIGN KEY (account_id) REFERENCES account(id),
                                CONSTRAINT uk_account_symbol UNIQUE (account_id, symbol)
);

CREATE TABLE price_snapshot (
                                id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                symbol VARCHAR(20) NOT NULL,
                                snapshot_date DATE NOT NULL,
                                current_price DECIMAL(19,4) NOT NULL,
                                currency VARCHAR(10) DEFAULT 'USD',
                                source VARCHAR(50) NOT NULL DEFAULT 'YAHOO_FINANCE',
                                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                CONSTRAINT uk_symbol_snapshot UNIQUE (symbol, snapshot_date)
);

