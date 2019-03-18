CREATE DATABASE IF NOT EXISTS kotlin_elastic_db;
USE kotlin_elastic_db;

CREATE TABLE IF NOT EXISTS users(
  id           INT(10),
  last_name     VARCHAR(128),
  first_name     VARCHAR(128),
  gender   CHAR(1),
  phone_number  VARCHAR(128),
  email    VARCHAR(128),
  birth_date DATETIME,
  password VARCHAR(128)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

LOAD DATA LOCAL INFILE '/docker-entrypoint-initdb.d/data.csv' INTO TABLE users FIELDS TERMINATED BY ',' ENCLOSED BY '"'