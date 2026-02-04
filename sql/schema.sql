-- Pay My Buddy - Schema (MySQL) - DB-first

CREATE DATABASE IF NOT EXISTS pay_my_buddy;
USE pay_my_buddy;

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS user_contacts;
DROP TABLE IF EXISTS transactions;
DROP TABLE IF EXISTS accounts;
DROP TABLE IF EXISTS users;

SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE users (
                       id        BIGINT AUTO_INCREMENT PRIMARY KEY,
                       username VARCHAR(100) NOT NULL,
                       email     VARCHAR(255) NOT NULL UNIQUE,
                       password  VARCHAR(255) NOT NULL
);

CREATE TABLE accounts (
                          id      BIGINT AUTO_INCREMENT PRIMARY KEY,
                          user_id BIGINT NOT NULL UNIQUE,
                          balance DECIMAL(19,2) NOT NULL DEFAULT 0.00,
                          FOREIGN KEY (user_id) REFERENCES users(id)
                              ON DELETE RESTRICT
                              ON UPDATE CASCADE
);

CREATE TABLE transactions (
                              id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
                              sender_account_id   BIGINT NOT NULL,
                              receiver_account_id BIGINT NOT NULL,
                              description         VARCHAR(255),
                              amount              DECIMAL(19,2) NOT NULL,
                              fee                 DECIMAL(19,2) NOT NULL,
                              date                DATETIME NOT NULL,
                              CHECK (amount > 0),
                              FOREIGN KEY (sender_account_id) REFERENCES accounts(id)
                                  ON DELETE RESTRICT
                                  ON UPDATE CASCADE,
                              FOREIGN KEY (receiver_account_id) REFERENCES accounts(id)
                                  ON DELETE RESTRICT
                                  ON UPDATE CASCADE
);

CREATE INDEX idx_transactions_sender   ON transactions (sender_account_id);
CREATE INDEX idx_transactions_receiver ON transactions (receiver_account_id);
CREATE INDEX idx_transactions_date     ON transactions (date);

CREATE TABLE user_contacts (
                               id         BIGINT AUTO_INCREMENT PRIMARY KEY,
                               user_id    BIGINT NOT NULL,
                               contact_id BIGINT NOT NULL,
                               UNIQUE (user_id, contact_id),
                               FOREIGN KEY (user_id) REFERENCES users(id)
                                   ON DELETE CASCADE
                                   ON UPDATE CASCADE,
                               FOREIGN KEY (contact_id) REFERENCES users(id)
                                   ON DELETE CASCADE
                                   ON UPDATE CASCADE
);

CREATE INDEX idx_user_contacts_user_id    ON user_contacts (user_id);
CREATE INDEX idx_user_contacts_contact_id ON user_contacts (contact_id);
