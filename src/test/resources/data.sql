-- Temporarily disable foreign key checks (MySQL syntax)
SET FOREIGN_KEY_CHECKS = 0;

-- Insert 5 users
INSERT INTO users (id, username, name, email, password, role) VALUES
  (99, 'TestUser', 'nameTest', 'usertnest@test.com', 'password123', 'USER'),
   (6,'disabled_user', 'Disabled User', 'disabledUser@sleepup.com', '$2a$10$HsMF2wIVlZAelTWGNHD/r.lbHJemKWx0.HEfqHKHF91CR8R3fDjX2', 'USER'),
  (1, 'User1', 'Name1', 'user1@example.com', '$2a$10$HsMF2wIVlZAelTWGNHD/r.lbHJemKWx0.HEfqHKHF91CR8R3fDjX2', 'USER'), -- owner of accommodations 1 and reservation 5
  (2, 'User2', 'Name2', 'user2@example.com', '$2a$10$HsMF2wIVlZAelTWGNHD/r.lbHJemKWx0.HEfqHKHF91CR8R3fDjX2', 'USER'), -- owner of accommodations 2,5 and reservation 4,2
  (3, 'User3', 'Name3', 'user3@example.com', '$2a$10$HsMF2wIVlZAelTWGNHD/r.lbHJemKWx0.HEfqHKHF91CR8R3fDjX2', 'USER'), -- owner of accommodation 3 and reservation 2
  (4, 'User4', 'Name4', 'user4@example.com', '$2a$10$HsMF2wIVlZAelTWGNHD/r.lbHJemKWx0.HEfqHKHF91CR8R3fDjX2', 'USER'), -- owner of accommodation 4 and reservation 1
  (5, 'Admin1', 'Name5', 'admin@example.com', '$2a$10$HsMF2wIVlZAelTWGNHD/r.lbHJemKWx0.HEfqHKHF91CR8R3fDjX2', 'ADMIN');

-- Insert 5 accommodations managed by users
INSERT INTO accommodations (
  id, available_from, available_to, check_in_time, check_out_time, guest_number, pet_friendly, price, managed_by_user_id, location, name, description, image_url
) VALUES
  (1, '2025-09-20', '2025-09-25', '15:00:00', '11:00:00', 2, true, 150.0, 1, 'New York', 'Hotel ABC', 'Comfortable hotel with great service', 'http://example.com/images/hotel_abc.jpg'),
  (2, '2025-10-01', '2025-11-08', '14:00:00', '12:00:00', 1, false, 220.0, 2, 'Los Angeles', 'Downtown Studio', 'Modern studio in the heart of the city', 'http://example.com/images/downtown_studio.jpg'),
  (3, '2025-09-15', '2025-09-30', '16:00:00', '10:00:00', 4, true, 300.0, 3, 'Chicago', 'Lakeview Condo', 'Spacious condo with lake view', 'http://example.com/images/lakeview_condo.jpg'),
  (4, '2025-10-05', '2025-10-15', '15:30:00', '11:30:00', 3, false, 180.0, 4, 'Miami', 'Beachside Bungalow', 'Cozy bungalow near the beach', 'http://example.com/images/beachside_bungalow.jpg'),
  (5, '2025-11-01', '2025-11-10', '14:00:00', '12:00:00', 5, true, 200.0, 2, 'San Francisco', 'Mountain View Cabin', 'Quiet cabin with mountain views', 'http://example.com/images/mountain_view_cabin.jpg');

-- Insert 5 reservations linked to users and accommodations
INSERT INTO reservations (
  id, user_id, accommodation_id, check_in_date, check_out_date, booking_status, created_date, email_sent
) VALUES
  (1, 4, 1, '2025-09-21', '2025-09-24', 'CONFIRMED', NOW(), false),
  (2, 2, 1, '2025-10-02', '2025-10-06', 'CONFIRMED', NOW(), false),
  (3, 3, 3, '2025-09-16', '2025-09-20', 'CANCELLED', NOW(), false),
  (4, 2, 4, '2025-10-06', '2025-10-12', 'CONFIRMED', NOW(), false),
  (5, 1, 5, '2025-11-02', '2025-11-08', 'PENDING', NOW(), false);

-- Reset sequences to avoid conflicts with future IDs
ALTER TABLE users AUTO_INCREMENT = 5;
ALTER TABLE accommodations AUTO_INCREMENT = 5;
ALTER TABLE reservations AUTO_INCREMENT = 5;

-- Enable foreign key checks back
SET FOREIGN_KEY_CHECKS = 1;