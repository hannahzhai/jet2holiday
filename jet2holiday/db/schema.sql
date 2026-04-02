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
                                CONSTRAINT uk_symbol_snapshot UNIQUE (symbol, snapshot_date)
);

CREATE TABLE market_instrument (
                                   id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                   symbol VARCHAR(32) NOT NULL,
                                   company_name VARCHAR(255) NOT NULL,
                                   asset_type VARCHAR(20) NOT NULL,
                                   market VARCHAR(10) NOT NULL,
                                   currency VARCHAR(10) NOT NULL,
                                   enabled TINYINT(1) NOT NULL DEFAULT 1,
                                   sort_order INT NOT NULL DEFAULT 0,
                                   created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                   updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                   CONSTRAINT uk_market_instrument_symbol UNIQUE (symbol)
);

CREATE INDEX idx_market_instrument_asset_enabled_sort
    ON market_instrument(asset_type, enabled, sort_order, id);

