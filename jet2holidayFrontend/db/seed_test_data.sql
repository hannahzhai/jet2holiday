-- =========================================
-- MVP Test Data Seed Script (MySQL)
-- Safe to run multiple times
-- =========================================

START TRANSACTION;

-- 1) Ensure account exists first (FK dependency for portfolio_item)
INSERT INTO account (id, account_name, cash_balance, currency)
VALUES (1, 'Default Portfolio Account', 12000.0000, 'USD')
ON DUPLICATE KEY UPDATE
  account_name = 'Default Portfolio Account',
  cash_balance = 12000.0000,
  currency = 'USD';

-- 2) Seed portfolio items (idempotent via uk_account_symbol: account_id + symbol)
INSERT INTO portfolio_item
(account_id, symbol, company_name, asset_type, shares, cost_basis, currency)
VALUES
(1, 'AAPL',    'Apple Inc.',                           'STOCK',  10.00000000,   180.0000, 'USD'),
(1, 'TSLA',    'Tesla, Inc.',                          'STOCK',   8.00000000,   220.0000, 'USD'),
(1, 'AGG',     'iShares Core U.S. Aggregate Bond ETF', 'BOND',   40.00000000,    98.5000, 'USD'),
(1, 'BTC-USD', 'Bitcoin',                              'CRYPTO',  0.35000000, 42000.0000, 'USD'),
(1, 'MSFT',    'Microsoft Corporation',                'STOCK',  12.00000000,   320.0000, 'USD')
ON DUPLICATE KEY UPDATE
  company_name = VALUES(company_name),
  asset_type   = VALUES(asset_type),
  shares       = VALUES(shares),
  cost_basis   = VALUES(cost_basis),
  currency     = VALUES(currency);

-- 3) Seed price snapshots (idempotent via uk_symbol_snapshot: symbol + snapshot_date)
-- Keep it simple for MVP: 3 days of data
INSERT INTO price_snapshot (symbol, snapshot_date, current_price, currency)
VALUES
('AAPL',    CURDATE() - INTERVAL 2 DAY, 192.3000, 'USD'),
('AAPL',    CURDATE() - INTERVAL 1 DAY, 194.1000, 'USD'),
('AAPL',    CURDATE(),                  195.0000, 'USD'),

('TSLA',    CURDATE() - INTERVAL 2 DAY, 241.2000, 'USD'),
('TSLA',    CURDATE() - INTERVAL 1 DAY, 244.8000, 'USD'),
('TSLA',    CURDATE(),                  246.0000, 'USD'),

('AGG',     CURDATE() - INTERVAL 2 DAY,  99.7000, 'USD'),
('AGG',     CURDATE() - INTERVAL 1 DAY, 100.0000, 'USD'),
('AGG',     CURDATE(),                  100.2000, 'USD'),

('BTC-USD', CURDATE() - INTERVAL 2 DAY, 44880.0000, 'USD'),
('BTC-USD', CURDATE() - INTERVAL 1 DAY, 45220.0000, 'USD'),
('BTC-USD', CURDATE(),                  45550.0000, 'USD'),

('MSFT',    CURDATE() - INTERVAL 2 DAY, 334.2000, 'USD'),
('MSFT',    CURDATE() - INTERVAL 1 DAY, 336.7000, 'USD'),
('MSFT',    CURDATE(),                  338.0000, 'USD')
ON DUPLICATE KEY UPDATE
  current_price = VALUES(current_price),
  currency      = VALUES(currency);

COMMIT;

-- 4) Quick verification
SELECT id, account_name, cash_balance, currency FROM account WHERE id = 1;

SELECT account_id, symbol, company_name, asset_type, shares, cost_basis, currency
FROM portfolio_item
WHERE account_id = 1
ORDER BY symbol;

SELECT symbol, snapshot_date, current_price, currency
FROM price_snapshot
WHERE symbol IN ('AAPL', 'TSLA', 'AGG', 'BTC-USD', 'MSFT')
  AND snapshot_date BETWEEN CURDATE() - INTERVAL 2 DAY AND CURDATE()
ORDER BY symbol, snapshot_date;
