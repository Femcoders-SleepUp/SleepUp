SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS reservations;
DROP TABLE IF EXISTS accommodations;
DROP TABLE IF EXISTS users;

SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(50) NOT NULL,
  name VARCHAR(100) NOT NULL,
  email VARCHAR(255) NOT NULL,
  role ENUM('USER', 'ADMIN') NOT NULL,
  password VARCHAR(255) NOT NULL,
  UNIQUE (username),
  UNIQUE (email)
);

CREATE TABLE accommodations (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  available_from DATE NOT NULL,
  available_to DATE NOT NULL,
  check_in_time TIME,
  check_out_time TIME,
  guest_number INT NOT NULL,
  pet_friendly BOOLEAN NOT NULL,
  price DOUBLE NOT NULL,
  managed_by_user_id BIGINT,
  location VARCHAR(50) NOT NULL,
  name VARCHAR(100) NOT NULL,
  description VARCHAR(255) NOT NULL,
  image_url VARCHAR(255) NOT NULL,
  CONSTRAINT FK_accommodation_user FOREIGN KEY (managed_by_user_id) REFERENCES users(id)
);

CREATE TABLE reservations (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  booking_status ENUM('CANCELLED','CONFIRMED','PENDING') NOT NULL,
  check_in_date DATE NOT NULL,
  check_out_date DATE NOT NULL,
  created_date DATETIME NOT NULL,
  email_sent BOOLEAN NOT NULL,
  guest_number INT DEFAULT NULL,
  accommodation_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  KEY FK_accommodation_id (accommodation_id),
  KEY FK_user_id (user_id),
  CONSTRAINT FK_accommodation FOREIGN KEY (accommodation_id) REFERENCES accommodations(id),
  CONSTRAINT FK_user FOREIGN KEY (user_id) REFERENCES users(id)
);
