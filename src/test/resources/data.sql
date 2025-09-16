INSERT INTO users (username, name, email, password, role)
VALUES ('TestUser', 'nameTest', 'usertnest@test.com', 'password123', 'USER');

INSERT INTO users (username, name, email, password, role)
VALUES ('TestUser2', 'nameTest2', 'user2tnest@test.com', 'password123', 'USER');

INSERT INTO accommodations (
  available_from, available_to, check_in_time, check_out_time, guest_number, pet_friendly, price, managed_by_user_id, location, name, description, image_url
) VALUES (
  DATE '2025-09-20', DATE '2025-09-25', TIME '15:00:00', TIME '11:00:00', 2, true,
  150.0, 2, 'New York', 'Hotel ABC', 'Comfortable hotel with great service', 'http://example.com/images/hotel_abc.jpg'
);

