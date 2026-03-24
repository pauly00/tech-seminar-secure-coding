-- Tech Seminar Secure Coding - Schema
-- Auto-run on startup (IF NOT EXISTS prevents duplicate errors)

CREATE DATABASE IF NOT EXISTS `techseminar` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `techseminar`;

CREATE TABLE IF NOT EXISTS `user_tb` (
    `id`            INT          NOT NULL AUTO_INCREMENT,
    `user_id`       VARCHAR(100) DEFAULT NULL,
    `user_password` VARCHAR(100) DEFAULT NULL,
    `user_name`     VARCHAR(45)  DEFAULT NULL,
    `user_gender`   VARCHAR(45)  DEFAULT NULL,
    `user_email`    VARCHAR(45)  DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `user_bbs` (
    `id`          INT          NOT NULL AUTO_INCREMENT,
    `bbs_id`      INT          DEFAULT NULL,
    `bbs_title`   VARCHAR(200) DEFAULT NULL,
    `bbs_userId`  VARCHAR(45)  DEFAULT NULL,
    `bbs_date`    TIMESTAMP    NULL DEFAULT CURRENT_TIMESTAMP,
    `bbs_content` VARCHAR(2000) DEFAULT NULL,
    `is_private`  TINYINT(1)   DEFAULT '0',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `user_bbs_file` (
    `id`           INT         NOT NULL AUTO_INCREMENT,
    `bbs_id`       INT         DEFAULT NULL,
    `filename`     VARCHAR(200) DEFAULT NULL,
    `filerealname` VARCHAR(200) DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `BoxPrices` (
    `box_size` INT            NOT NULL,
    `price`    DECIMAL(10, 2) DEFAULT NULL,
    PRIMARY KEY (`box_size`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `PaymentHistory` (
    `orderID`       INT UNSIGNED NOT NULL AUTO_INCREMENT,
    `paymentMethod` VARCHAR(255) DEFAULT NULL,
    `amount`        DECIMAL(10, 0) DEFAULT NULL,
    `date`          TIMESTAMP    NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`orderID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
