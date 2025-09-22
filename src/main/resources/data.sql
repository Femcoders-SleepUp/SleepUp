-- Insert users
INSERT INTO users (username, name, email, password, role) VALUES
('admin', 'Administrator', 'admin@sleepup.com', '$2a$10$HsMF2wIVlZAelTWGNHD/r.lbHJemKWx0.HEfqHKHF91CR8R3fDjX2', 'ADMIN'),
('user1', 'User One', 'user1@sleepup.com', '$2a$10$HsMF2wIVlZAelTWGNHD/r.lbHJemKWx0.HEfqHKHF91CR8R3fDjX2', 'USER'),
('carlos_garcia', 'Carlos García', 'carlos@gmail.com', '$2a$10$HsMF2wIVlZAelTWGNHD/r.lbHJemKWx0.HEfqHKHF91CR8R3fDjX2', 'USER'),
('maria_lopez', 'María López', 'maria@example.com', '$2a$10$HsMF2wIVlZAelTWGNHD/r.lbHJemKWx0.HEfqHKHF91CR8R3fDjX2', 'USER'),
('jose_martinez', 'José Martínez', 'jose@example.com', '$2a$10$HsMF2wIVlZAelTWGNHD/r.lbHJemKWx0.HEfqHKHF91CR8R3fDjX2', 'USER'),
('ana_rodriguez', 'Ana Rodríguez', 'ana@example.com', '$2a$10$HsMF2wIVlZAelTWGNHD/r.lbHJemKWx0.HEfqHKHF91CR8R3fDjX2', 'USER'),
('david_sanchez', 'David Sánchez', 'david@example.com', '$2a$10$HsMF2wIVlZAelTWGNHD/r.lbHJemKWx0.HEfqHKHF91CR8R3fDjX2', 'USER'),
('laura_fernandez', 'Laura Fernández', 'laura@example.com', '$2a$10$HsMF2wIVlZAelTWGNHD/r.lbHJemKWx0.HEfqHKHF91CR8R3fDjX2', 'USER');

-- Insert accommodations
INSERT INTO accommodations (name, price, guest_number, pet_friendly, location, description, image_url, check_in_time, check_out_time, available_from, available_to, managed_by_user_id) VALUES
('Casa Rural El Olivar', 85.00, 6, true, 'Valencia', 'Encantadora casa rural rodeada de olivares con vistas panorámicas. Ideal para familias que buscan tranquilidad.', 'https://example.com/images/casa-olivar.jpg', '15:00:00', '11:00:00', '2026-01-01', '2026-12-31', 3),
('Apartamento Centro Madrid', 120.00, 4, false, 'Madrid', 'Moderno apartamento en el corazón de Madrid, cerca de todas las atracciones turísticas.', 'https://example.com/images/apt-madrid.jpg', '16:00:00', '12:00:00', '2026-01-01', '2026-12-31', 3),
('Villa Costa Brava', 200.00, 8, true, 'Girona', 'Espectacular villa frente al mar con piscina privada y jardín. Perfecta para grupos grandes.', 'https://example.com/images/villa-brava.jpg', '17:00:00', '10:00:00', '2026-03-01', '2026-10-31', 4),
('Chalet Sierra Nevada', 150.00, 6, true, 'Granada', 'Acogedor chalet en la sierra con chimenea y vistas a las montañas. Ideal para esquiadores.', 'https://example.com/images/chalet-sierra.jpg', '14:00:00', '11:00:00', '2026-01-01', '2026-12-31', 4),
('Piso Playa Malagueta', 95.00, 4, false, 'Málaga', 'Apartamento a pie de playa con terraza y vistas al mar Mediterráneo.', 'https://example.com/images/piso-malaga.jpg', '15:00:00', '12:00:00', '2026-04-01', '2026-11-30', 5),
('Casa Asturiana Tradicional', 75.00, 5, true, 'Asturias', 'Casa tradicional asturiana con hórreo y huerto ecológico. Ambiente rural auténtico.', 'https://example.com/images/casa-asturias.jpg', '16:00:00', '11:00:00', '2026-01-01', '2026-12-31', 5),
('Loft Barcelona Eixample', 140.00, 3, false, 'Barcelona', 'Loft de diseño en el Eixample barcelonés, completamente equipado y con wifi de alta velocidad.', 'https://example.com/images/loft-bcn.jpg', '15:30:00', '11:30:00', '2026-02-01', '2026-12-31', 6),
('Cortijo Andaluz', 110.00, 7, true, 'Sevilla', 'Auténtico cortijo andaluz con patio típico, ideal para conocer la cultura del sur.', 'https://example.com/images/cortijo-sevilla.jpg', '14:30:00', '12:00:00', '2026-01-01', '2026-12-31', 6),
('Apartamento Bilbao Centro', 100.00, 4, false, 'Bilbao', 'Céntrico apartamento cerca del Museo Guggenheim y la zona de pintxos.', 'https://example.com/images/apt-bilbao.jpg', '16:00:00', '11:00:00', '2026-01-01', '2026-12-31', 7),
( 'Casa Canaria con Piscina', 130.00, 6, true, 'Las Palmas', 'Casa típica canaria con piscina y barbacoa, a 5 minutos de la playa.', 'https://example.com/images/casa-canarias.jpg', '15:00:00', '12:00:00', '2026-01-01', '2026-12-31', 7),
( 'Estudio Toledo Histórico', 65.00, 2, false, 'Toledo', 'Pequeño pero encantador estudio en el casco histórico de Toledo.', 'https://example.com/images/estudio-toledo.jpg', '17:00:00', '11:00:00', '2026-01-01', '2026-12-31', 8),
( 'Casa Rural Extremeña', 80.00, 8, true, 'Cáceres', 'Amplia casa rural en dehesa extremeña, perfecta para desconectar de la ciudad.', 'https://example.com/images/casa-extremadura.jpg', '14:00:00', '12:00:00', '2026-01-01', '2026-12-31', 8);

-- Insert reservations (for all users EXCEPT admin (id=1) and user1 (id=2))
-- Reservations for carlos_garcia (id=3)
INSERT INTO reservations (user_id, guest_number, accommodation_id, check_in_date, check_out_date, booking_status, email_sent, created_date) VALUES
(3, 4, 5, '2026-07-15', '2026-07-22', 'CONFIRMED', true, NOW()),
(3, 2, 11, '2026-09-10', '2026-09-13', 'PENDING', false, NOW());

-- Reservations for maria_lopez (id=4)
INSERT INTO reservations (user_id, guest_number, accommodation_id, check_in_date, check_out_date, booking_status, email_sent, created_date) VALUES
(4, 6, 1, '2026-08-05', '2026-08-12', 'CONFIRMED', true, NOW()),
(4, 3, 7, '2026-06-20', '2026-06-25', 'CANCELLED', true, NOW()),
(4, 4, 9, '2026-10-15', '2026-10-20', 'PENDING', false, NOW());

-- Reservations for jose_martinez (id=5)
INSERT INTO reservations (user_id, guest_number, accommodation_id, check_in_date, check_out_date, booking_status, email_sent, created_date) VALUES
(5, 5, 8, '2026-05-10', '2026-05-17', 'CONFIRMED', true, NOW()),
(5, 2, 11, '2026-11-01', '2026-11-04', 'PENDING', false, NOW());

-- Reservations for ana_rodriguez (id=6)
INSERT INTO reservations (user_id, guest_number, accommodation_id, check_in_date, check_out_date, booking_status, email_sent, created_date) VALUES
(6, 7, 3, '2026-07-01', '2026-07-08', 'CONFIRMED', true, NOW()),
(6, 4, 10, '2026-12-20', '2026-12-27', 'PENDING', false, NOW()),
(6, 3, 7, '2026-04-15', '2026-04-18', 'CONFIRMED', true, NOW());

-- Reservations for david_sanchez (id=7)
INSERT INTO reservations (user_id, guest_number, accommodation_id, check_in_date, check_out_date, booking_status, email_sent, created_date) VALUES
(7, 6, 4, '2026-02-15', '2026-02-22', 'CONFIRMED', true, NOW()),
(7, 4, 5, '2026-09-05', '2026-09-10', 'PENDING', false, NOW()),
(7, 8, 12, '2026-03-20', '2026-03-27', 'CONFIRMED', true, NOW());

-- Reservations for laura_fernandez (id=8)
INSERT INTO reservations (user_id, guest_number, accommodation_id, check_in_date, check_out_date, booking_status, email_sent, created_date) VALUES
(8, 3, 2, '2026-06-01', '2026-06-05', 'CONFIRMED', true, NOW()),
(8, 5, 6, '2026-08-15', '2026-08-20', 'PENDING', false, NOW()),
(8, 2, 11, '2026-04-10', '2026-04-13', 'CANCELLED', true, NOW()),
(8, 6, 10, '2026-11-10', '2026-11-15', 'CONFIRMED', true, NOW());

-- Reset sequences to avoid conflicts with future IDs
ALTER TABLE users AUTO_INCREMENT = 9;
ALTER TABLE accommodations AUTO_INCREMENT = 13;
ALTER TABLE reservations AUTO_INCREMENT = 18;