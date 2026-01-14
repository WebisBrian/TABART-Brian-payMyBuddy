-- =========================================================
-- Pay My Buddy - Schema (DDL) - MySQL
-- Compatible with Spring Boot schema.sql execution
-- =========================================================

DROP TABLE IF EXISTS user_contacts;
DROP TABLE IF EXISTS transactions;
DROP TABLE IF EXISTS accounts;
DROP TABLE IF EXISTS users;

-- =========================================================
-- Order matters because of foreign keys:
-- users -> accounts -> transactions -> user_contacts
-- =========================================================

CREATE TABLE users (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       user_name VARCHAR(100) NOT NULL,
                       email VARCHAR(255) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL
);

CREATE TABLE accounts (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          user_id BIGINT NOT NULL UNIQUE,
                          balance DECIMAL(19, 2) NOT NULL DEFAULT 0.00,

                          CONSTRAINT fk_accounts_user
                              FOREIGN KEY (user_id)
                                  REFERENCES users(id)
                                  ON DELETE RESTRICT
                                  ON UPDATE CASCADE
);

CREATE TABLE transactions (
                              id BIGINT AUTO_INCREMENT PRIMARY KEY,
                              sender_account_id BIGINT NOT NULL,
                              receiver_account_id BIGINT NOT NULL,

                              description VARCHAR(255) NULL,
                              amount DECIMAL(19, 2) NOT NULL,
                              fee DECIMAL(19, 2) NOT NULL,
                              date DATETIME NOT NULL,

                              CONSTRAINT fk_transactions_sender
                                  FOREIGN KEY (sender_account_id)
                                      REFERENCES accounts(id)
                                      ON DELETE RESTRICT
                                      ON UPDATE CASCADE,

                              CONSTRAINT fk_transactions_receiver
                                  FOREIGN KEY (receiver_account_id)
                                      REFERENCES accounts(id)
                                      ON DELETE RESTRICT
                                      ON UPDATE CASCADE,

                              CONSTRAINT chk_transactions_amount_positive CHECK (amount > 0)
);

CREATE INDEX idx_transactions_sender ON transactions(sender_account_id);
CREATE INDEX idx_transactions_receiver ON transactions(receiver_account_id);
CREATE INDEX idx_transactions_date ON transactions(date);

CREATE TABLE user_contacts (
                               user_id BIGINT NOT NULL,
                               contact_id BIGINT NOT NULL,

                               PRIMARY KEY (user_id, contact_id),

                               CONSTRAINT fk_user_contacts_user
                                   FOREIGN KEY (user_id)
                                       REFERENCES users(id)
                                       ON DELETE CASCADE
                                       ON UPDATE CASCADE,

                               CONSTRAINT fk_user_contacts_contact
                                   FOREIGN KEY (contact_id)
                                       REFERENCES users(id)
                                       ON DELETE CASCADE
                                       ON UPDATE CASCADE
);

CREATE INDEX idx_user_contacts_contact_id ON user_contacts(contact_id);
