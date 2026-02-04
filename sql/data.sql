-- =========================================================
-- IMPORTANT (MySQL Workbench): make sure you run the script in the correct schema
USE pay_my_buddy;

-- Optional but helpful when re-running the seed multiple times
SET FOREIGN_KEY_CHECKS = 0;

-- Clean (MySQL Workbench safe-updates friendly)
-- TRUNCATE avoids Error 1175 (safe update mode) and resets AUTO_INCREMENT.
TRUNCATE TABLE user_contacts;
TRUNCATE TABLE transactions;
TRUNCATE TABLE accounts;
TRUNCATE TABLE users;

SET FOREIGN_KEY_CHECKS = 1;

-- ---------------------------------------------------------
-- USERS
-- Password hash below is a BCrypt hash for the clear password: "password"
-- ---------------------------------------------------------
INSERT INTO users (id, user_name, email, password) VALUES
                                                       (1, 'Alice Martin',    'alice@paymybuddy.local',   '$2a$10$24U/YAoh6vzt0vV4H8KLn.HZmS68P9ftCsNU8SC8SSK43PuNG/4wK'),
                                                       (2, 'Bob Dupont',      'bob@paymybuddy.local',     '$2a$10$24U/YAoh6vzt0vV4H8KLn.HZmS68P9ftCsNU8SC8SSK43PuNG/4wK'),
                                                       (3, 'Chloé Bernard',   'chloe@paymybuddy.local',   '$2a$10$24U/YAoh6vzt0vV4H8KLn.HZmS68P9ftCsNU8SC8SSK43PuNG/4wK'),
                                                       (4, 'David Leroy',     'david@paymybuddy.local',   '$2a$10$24U/YAoh6vzt0vV4H8KLn.HZmS68P9ftCsNU8SC8SSK43PuNG/4wK'),
                                                       (5, 'Emma Rousseau',   'emma@paymybuddy.local',    '$2a$10$24U/YAoh6vzt0vV4H8KLn.HZmS68P9ftCsNU8SC8SSK43PuNG/4wK'),
                                                       (6, 'Farid Benali',    'farid@paymybuddy.local',   '$2a$10$24U/YAoh6vzt0vV4H8KLn.HZmS68P9ftCsNU8SC8SSK43PuNG/4wK'),
                                                       (7, 'Giulia Conti',    'giulia@paymybuddy.local',  '$2a$10$24U/YAoh6vzt0vV4H8KLn.HZmS68P9ftCsNU8SC8SSK43PuNG/4wK'),
                                                       (8, 'Hugo Petit',      'hugo@paymybuddy.local',    '$2a$10$24U/YAoh6vzt0vV4H8KLn.HZmS68P9ftCsNU8SC8SSK43PuNG/4wK');

-- ---------------------------------------------------------
-- ACCOUNTS (1 per user, unique user_id)
-- ---------------------------------------------------------
INSERT INTO accounts (id, user_id, balance) VALUES
                                                (1, 1, 325.40),
                                                (2, 2,  95.00),
                                                (3, 3, 512.10),
                                                (4, 4,  18.75),
                                                (5, 5, 240.00),
                                                (6, 6,  60.50),
                                                (7, 7, 1100.00),
                                                (8, 8,   5.00);

-- ---------------------------------------------------------
-- CONTACTS (network)
-- Unique constraint: (user_id, contact_id)
-- ---------------------------------------------------------
INSERT INTO user_contacts (id, user_id, contact_id) VALUES
                                                        (1,  1, 2),
                                                        (2,  1, 3),
                                                        (3,  1, 5),
                                                        (4,  2, 1),
                                                        (5,  2, 4),
                                                        (6,  2, 6),
                                                        (7,  3, 1),
                                                        (8,  3, 7),
                                                        (9,  4, 2),
                                                        (10, 4, 8),
                                                        (11, 5, 1),
                                                        (12, 5, 6),
                                                        (13, 6, 2),
                                                        (14, 6, 5),
                                                        (15, 7, 3),
                                                        (16, 7, 1),
                                                        (17, 8, 4);

-- ---------------------------------------------------------
-- TRANSACTIONS
-- fee ~ 0.5% of amount, rounded to 2 decimals (example)
-- amount must be > 0 (check constraint)
-- ---------------------------------------------------------
INSERT INTO transactions
(id, sender_account_id, receiver_account_id, description, amount, fee, date)
VALUES
    (1,  1, 2, 'Remboursement resto',         25.00, 0.13, '2026-01-10 12:30:00'),
    (2,  2, 1, 'Partage essence',             15.00, 0.08, '2026-01-12 18:10:00'),
    (3,  3, 1, 'Cadeau commun',               40.00, 0.20, '2026-01-15 09:15:00'),
    (4,  1, 3, 'Billets concert',             30.00, 0.15, '2026-01-18 20:45:00'),
    (5,  2, 4, 'Déjeuner',                    12.00, 0.06, '2026-01-20 13:05:00'),

    (6,  5, 1, 'Courses partagées',           48.90, 0.24, '2026-01-22 19:05:00'),
    (7,  1, 5, 'Remboursement courses',       18.50, 0.09, '2026-01-23 08:40:00'),
    (8,  6, 2, 'Covoiturage',                  9.80, 0.05, '2026-01-24 07:55:00'),
    (9,  2, 6, 'Café',                         3.60, 0.02, '2026-01-24 16:20:00'),
    (10, 7, 3, 'Participation Airbnb',       120.00, 0.60, '2026-01-26 21:10:00'),

    (11, 3, 7, 'Remboursement voyage',        75.00, 0.38, '2026-01-28 10:10:00'),
    (12, 4, 2, 'Cinéma',                      14.20, 0.07, '2026-01-30 22:30:00'),
    (13, 2, 1, 'Apéro',                        8.00, 0.04, '2026-02-01 19:45:00'),
    (14, 1, 2, 'Tickets métro',               11.30, 0.06, '2026-02-02 09:05:00'),
    (15, 5, 6, 'Matériel',                    34.99, 0.17, '2026-02-02 14:12:00'),

    (16, 6, 5, 'Remboursement matériel',      10.00, 0.05, '2026-02-02 18:35:00'),
    (17, 7, 1, 'Dépanne',                    200.00, 1.00, '2026-02-03 08:00:00'),
    (18, 3, 1, 'Participation anniversaire',  22.00, 0.11, '2026-02-03 12:05:00'),
    (19, 1, 3, 'Remboursement anniversaire',  16.00, 0.08, '2026-02-03 17:40:00'),
    (20, 8, 4, 'Dépannage petite somme',       4.50, 0.02, '2026-02-03 23:10:00'),

    (21, 4, 8, 'Retour dépannage',             2.00, 0.01, '2026-02-04 09:30:00'),
    (22, 2, 4, 'Snack',                        6.80, 0.03, '2026-02-04 12:25:00'),
    (23, 1, 5, 'Partage facture internet',    19.99, 0.10, '2026-02-04 18:55:00'),
    (24, 5, 1, 'Remboursement internet',       19.99, 0.10, '2026-02-05 08:15:00'),
    (25, 7, 3, 'Participation resto (groupe)',55.00, 0.28, '2026-02-05 21:20:00');
