-- Seed Insurance Products
INSERT INTO insurance_products (product_code, product_name, line_of_business, description,
    min_coverage_limit, max_coverage_limit, min_deductible, max_deductible)
VALUES
    ('AUTO-STD-001', 'Standard Auto Insurance',    'AUTO',       'Comprehensive auto coverage for personal vehicles',        25000, 500000,  250, 5000),
    ('AUTO-COMP-002','Comprehensive Auto Plus',     'AUTO',       'Full coverage including collision and comprehensive',       50000, 1000000, 500, 10000),
    ('HOME-STD-001', 'Standard Homeowners',         'HOME',       'Standard homeowners coverage for single-family dwellings', 100000, 2000000, 500, 25000),
    ('HOME-PREM-002','Premium Home Protection',     'HOME',       'Enhanced home coverage with umbrella endorsement options', 200000, 5000000, 1000, 50000),
    ('COMM-BOP-001', 'Business Owners Policy',      'COMMERCIAL', 'Combined property and liability for small businesses',     100000, 5000000, 1000, 100000)
ON CONFLICT (product_code) DO NOTHING;

-- Seed State Rules (AUTO)
INSERT INTO state_rules (state_code, state_name, line_of_business, min_liability_limit, mandatory_coverage_types, regulatory_surcharge_rate, no_fault_state)
VALUES
    ('CA', 'California',   'AUTO', 15000,  '["LIABILITY","UNINSURED_MOTORIST"]',           0.0150, FALSE),
    ('NY', 'New York',     'AUTO', 25000,  '["LIABILITY","UNINSURED_MOTORIST","PIP"]',      0.0200, TRUE),
    ('TX', 'Texas',        'AUTO', 30000,  '["LIABILITY","UNINSURED_MOTORIST"]',            0.0100, FALSE),
    ('FL', 'Florida',      'AUTO', 10000,  '["PIP","PROPERTY_DAMAGE"]',                    0.0250, TRUE),
    ('IL', 'Illinois',     'AUTO', 25000,  '["LIABILITY","UNINSURED_MOTORIST"]',            0.0120, FALSE),
    ('WA', 'Washington',   'AUTO', 25000,  '["LIABILITY","UNINSURED_MOTORIST"]',            0.0130, FALSE),
    ('GA', 'Georgia',      'AUTO', 25000,  '["LIABILITY","UNINSURED_MOTORIST"]',            0.0110, FALSE),
    ('OH', 'Ohio',         'AUTO', 25000,  '["LIABILITY","UNINSURED_MOTORIST"]',            0.0090, FALSE)
ON CONFLICT (state_code, line_of_business) DO NOTHING;

-- Seed State Rules (HOME)
INSERT INTO state_rules (state_code, state_name, line_of_business, min_liability_limit, mandatory_coverage_types, regulatory_surcharge_rate, no_fault_state)
VALUES
    ('CA', 'California',   'HOME', 100000, '["DWELLING","LIABILITY","PERSONAL_PROPERTY"]', 0.0100, FALSE),
    ('NY', 'New York',     'HOME', 100000, '["DWELLING","LIABILITY","PERSONAL_PROPERTY"]', 0.0150, FALSE),
    ('TX', 'Texas',        'HOME', 100000, '["DWELLING","LIABILITY"]',                     0.0080, FALSE),
    ('FL', 'Florida',      'HOME', 100000, '["DWELLING","LIABILITY","HURRICANE"]',         0.0300, FALSE)
ON CONFLICT (state_code, line_of_business) DO NOTHING;
