-- Seed rate tables for AUTO and HOME for major states

INSERT INTO rate_tables (table_code, line_of_business, state_code, effective_date, base_rate, rate_factors, is_active) VALUES
-- AUTO rate tables
('RT-AUTO-CA-2024', 'AUTO', 'CA', '2024-01-01', 0.025000,
 '{"territoryFactor_urban":1.25,"territoryFactor_suburban":1.05,"territoryFactor_rural":0.90,"vehicleAge_new":1.10,"vehicleAge_5yr":0.95,"vehicleAge_10yr":0.85}',
 TRUE),
('RT-AUTO-NY-2024', 'AUTO', 'NY', '2024-01-01', 0.028000,
 '{"territoryFactor_urban":1.35,"territoryFactor_suburban":1.10,"territoryFactor_rural":0.88}',
 TRUE),
('RT-AUTO-TX-2024', 'AUTO', 'TX', '2024-01-01', 0.022000,
 '{"territoryFactor_urban":1.15,"territoryFactor_suburban":1.00,"territoryFactor_rural":0.85}',
 TRUE),
('RT-AUTO-FL-2024', 'AUTO', 'FL', '2024-01-01', 0.030000,
 '{"territoryFactor_urban":1.30,"territoryFactor_coastal":1.40,"territoryFactor_inland":1.05}',
 TRUE),

-- HOME rate tables
('RT-HOME-CA-2024', 'HOME', 'CA', '2024-01-01', 0.006500,
 '{"firePremium_hillside":1.50,"constructionType_frame":1.00,"constructionType_masonry":0.90,"roofAge_new":0.95}',
 TRUE),
('RT-HOME-NY-2024', 'HOME', 'NY', '2024-01-01', 0.005800,
 '{"constructionType_frame":1.00,"constructionType_masonry":0.88,"buildingAge_new":0.90}',
 TRUE),
('RT-HOME-TX-2024', 'HOME', 'TX', '2024-01-01', 0.007200,
 '{"hailPremium_high":1.25,"windPremium":1.15,"constructionType_frame":1.00}',
 TRUE),
('RT-HOME-FL-2024', 'HOME', 'FL', '2024-01-01', 0.009500,
 '{"hurricanePremium_coastal":1.60,"floodZone_a":1.35,"constructionType_frame":1.00}',
 TRUE),

-- 2025 updated tables
('RT-AUTO-CA-2025', 'AUTO', 'CA', '2025-01-01', 0.026500,
 '{"territoryFactor_urban":1.28,"territoryFactor_suburban":1.08,"territoryFactor_rural":0.92}',
 TRUE),
('RT-HOME-CA-2025', 'HOME', 'CA', '2025-01-01', 0.007000,
 '{"firePremium_hillside":1.55,"constructionType_frame":1.00,"constructionType_masonry":0.88}',
 TRUE);
