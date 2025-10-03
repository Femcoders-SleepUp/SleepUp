-- Tabla de control para verificar si ya se inicializó
CREATE TABLE IF NOT EXISTS db_initialization (
    id INT PRIMARY KEY DEFAULT 1,
    initialized_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CHECK (id = 1)
);

-- Solo ejecutar si NO existe el flag de inicialización
SET @is_initialized = (SELECT COUNT(*) FROM db_initialization);

-- Insert users (solo si no está inicializado)
INSERT INTO users (username, name, email, password, role)
SELECT * FROM (
    SELECT 'disabled_user' as username, 'Disabled User' as name, 'disabledUser@sleepup.com' as email, '$2a$10$HsMF2wIVlZAelTWGNHD/r.lbHJemKWx0.HEfqHKHF91CR8R3fDjX2' as password, 'USER' as role UNION ALL
    SELECT 'admin', 'Administrator', 'admin@sleepup.com', '$2a$10$HsMF2wIVlZAelTWGNHD/r.lbHJemKWx0.HEfqHKHF91CR8R3fDjX2', 'ADMIN' UNION ALL
    SELECT 'user1', 'User One', 'user1@sleepup.com', '$2a$10$HsMF2wIVlZAelTWGNHD/r.lbHJemKWx0.HEfqHKHF91CR8R3fDjX2', 'USER' UNION ALL
    SELECT 'carlos_garcia', 'Carlos García', 'carlos@gmail.com', '$2a$10$HsMF2wIVlZAelTWGNHD/r.lbHJemKWx0.HEfqHKHF91CR8R3fDjX2', 'USER' UNION ALL
    SELECT 'maria_lopez', 'María López', 'maria@example.com', '$2a$10$HsMF2wIVlZAelTWGNHD/r.lbHJemKWx0.HEfqHKHF91CR8R3fDjX2', 'USER' UNION ALL
    SELECT 'jose_martinez', 'José Martínez', 'jose@example.com', '$2a$10$HsMF2wIVlZAelTWGNHD/r.lbHJemKWx0.HEfqHKHF91CR8R3fDjX2', 'USER' UNION ALL
    SELECT 'ana_rodriguez', 'Ana Rodríguez', 'ana@example.com', '$2a$10$HsMF2wIVlZAelTWGNHD/r.lbHJemKWx0.HEfqHKHF91CR8R3fDjX2', 'USER' UNION ALL
    SELECT 'david_sanchez', 'David Sánchez', 'david@example.com', '$2a$10$HsMF2wIVlZAelTWGNHD/r.lbHJemKWx0.HEfqHKHF91CR8R3fDjX2', 'USER' UNION ALL
    SELECT 'laura_fernandez', 'Laura Fernández', 'laura@example.com', '$2a$10$HsMF2wIVlZAelTWGNHD/r.lbHJemKWx0.HEfqHKHF91CR8R3fDjX2', 'USER'
) AS tmp
WHERE @is_initialized = 0;

-- Insert accommodations (solo si no está inicializado)
INSERT INTO accommodations (name, price, guest_number, pet_friendly, location, description, image_url, check_in_time, check_out_time, available_from, available_to, managed_by_user_id)
SELECT * FROM (
    SELECT 'Casa Rural El Olivar' as name, 85.00 as price, 6 as guest_number, true as pet_friendly, 'Valencia' as location, 'Encantadora casa rural rodeada de olivares con vistas panorámicas. Ideal para familias que buscan tranquilidad.' as description, 'https://res.cloudinary.com/sleepup/image/upload/c_fill,w_800,h_800/v1759479023/casa_elolivar_aqk9ia.jpg' as image_url, '15:00:00' as check_in_time, '11:00:00' as check_out_time, '2026-01-01' as available_from, '2026-12-31' as available_to, 3 as managed_by_user_id UNION ALL
    SELECT 'Apartamento Centro Madrid', 120.00, 4, false, 'Madrid', 'Moderno apartamento en el corazón de Madrid, cerca de todas las atracciones turísticas.', 'https://res.cloudinary.com/sleepup/image/upload/c_crop,w_920,h_920/v1759478978/apartamento_madrid_wwvtdm.jpg', '16:00:00', '12:00:00', '2026-01-01', '2026-12-31', 3 UNION ALL
    SELECT 'Villa Costa Brava', 200.00, 8, true, 'Girona', 'Espectacular villa frente al mar con piscina privada y jardín. Perfecta para grupos grandes.', 'https://res.cloudinary.com/sleepup/image/upload/c_crop,w_920,h_920/v1759478887/villa_costabrava_itvjpd.jpg', '17:00:00', '10:00:00', '2026-03-01', '2026-10-31', 4 UNION ALL
    SELECT 'Chalet Sierra Nevada', 150.00, 6, true, 'Granada', 'Acogedor chalet en la sierra con chimenea y vistas a las montañas. Ideal para esquiadores.', 'https://res.cloudinary.com/sleepup/image/upload/v1759477547/image_1_ucae5w.png', '14:00:00', '11:00:00', '2026-01-01', '2026-12-31', 4 UNION ALL
    SELECT 'Piso Playa Malagueta', 95.00, 4, false, 'Málaga', 'Apartamento a pie de playa con terraza y vistas al mar Mediterráneo.', 'https://res.cloudinary.com/sleepup/image/upload/v1759477726/image_2_lb3ll2_56c8c2.png', '15:00:00', '12:00:00', '2026-04-01', '2026-11-30', 5 UNION ALL
    SELECT 'Casa Asturiana Tradicional', 75.00, 5, true, 'Asturias', 'Casa tradicional asturiana con hórreo y huerto ecológico. Ambiente rural auténtico.', 'https://res.cloudinary.com/sleepup/image/upload/v1759477788/image_3_b3skn4_ee1f8b.png', '16:00:00', '11:00:00', '2026-01-01', '2026-12-31', 5 UNION ALL
    SELECT 'Loft Barcelona Eixample', 140.00, 3, false, 'Barcelona', 'Loft de diseño en el Eixample barcelonés, completamente equipado y con wifi de alta velocidad.', 'https://res.cloudinary.com/sleepup/image/upload/v1759478265/image_4_tig4hq_452022.png', '15:30:00', '11:30:00', '2026-02-01', '2026-12-31', 6 UNION ALL
    SELECT 'Cortijo Andaluz', 110.00, 7, true, 'Sevilla', 'Auténtico cortijo andaluz con patio típico, ideal para conocer la cultura del sur.', 'https://res.cloudinary.com/sleepup/image/upload/v1759478097/image_5_zlroby_682f21.png', '14:30:00', '12:00:00', '2026-01-01', '2026-12-31', 6 UNION ALL
    SELECT 'Apartamento Bilbao Centro', 100.00, 4, false, 'Bilbao', 'Céntrico apartamento cerca del Museo Guggenheim y la zona de pintxos.', 'https://res.cloudinary.com/sleepup/image/upload/c_fill,w_800,h_800/v1759478039/157848997_pkxc0h.jpg', '16:00:00', '11:00:00', '2026-01-01', '2026-12-31', 7 UNION ALL
    SELECT 'Casa Canaria con Piscina', 130.00, 6, true, 'Las Palmas', 'Casa típica canaria con piscina y barbacoa, a 5 minutos de la playa.', 'https://res.cloudinary.com/sleepup/image/upload/c_fill,w_800,h_800/v1759478164/h.488857.445.300.0.ffffff.8e132e69_xi0fto.jpgs', '15:00:00', '12:00:00', '2026-01-01', '2026-12-31', 7 UNION ALL
    SELECT 'Estudio Toledo Histórico', 65.00, 2, false, 'Toledo', 'Pequeño pero encantador estudio en el casco histórico de Toledo.', 'https://res.cloudinary.com/sleepup/image/upload/c_crop,w_920,h_920/v1759478327/salon-clasico-con-muebles-de-diseno-y-carpinteria-blanca_c23c1c51_241115140727_1280x794_xat0mo.webp', '17:00:00', '11:00:00', '2026-01-01', '2026-12-31', 8 UNION ALL
    SELECT 'Casa Rural Extremeña', 80.00, 8, true, 'Cáceres', 'Amplia casa rural en dehesa extremeña, perfecta para desconectar de la ciudad.', 'https://res.cloudinary.com/sleepup/image/upload/c_crop,w_920,h_920/v1759478566/casa_extremena_yamw0h.png', '14:00:00', '12:00:00', '2026-01-01', '2026-12-31', 8
) AS tmp
WHERE @is_initialized = 0;


-- Insert reservations (solo si no está inicializado)
INSERT INTO reservations (user_id, guest_number, accommodation_id, check_in_date, check_out_date, booking_status, email_sent, created_date, total_price)
SELECT * FROM (
    SELECT 3 as user_id, 4 as guest_number, 5 as accommodation_id, '2026-07-15' as check_in_date, '2026-07-22' as check_out_date, 'CONFIRMED' as booking_status, true as email_sent, NOW() as created_date, 700.00 as total_price UNION ALL
    SELECT 3, 2, 11, '2026-09-10', '2026-09-13', 'PENDING', false, NOW(), 300.00 UNION ALL
    SELECT 4, 6, 1, '2026-08-05', '2026-08-12', 'CONFIRMED', true, NOW(), 1200.00 UNION ALL
    SELECT 4, 3, 7, '2026-06-20', '2026-06-25', 'CANCELLED', true, NOW(), 500.00 UNION ALL
    SELECT 4, 4, 9, '2026-10-15', '2026-10-20', 'PENDING', false, NOW(), 650.00 UNION ALL
    SELECT 5, 5, 8, '2026-05-10', '2026-05-17', 'CONFIRMED', true, NOW(), 875.00 UNION ALL
    SELECT 5, 2, 11, '2026-11-01', '2026-11-04', 'PENDING', false, NOW(), 350.00 UNION ALL
    SELECT 6, 7, 3, '2026-07-01', '2026-07-08', 'CONFIRMED', true, NOW(), 980.00 UNION ALL
    SELECT 6, 4, 10, '2026-12-20', '2026-12-27', 'PENDING', false, NOW(), 720.00 UNION ALL
    SELECT 6, 3, 7, '2026-04-15', '2026-04-18', 'CONFIRMED', true, NOW(), 400.00 UNION ALL
    SELECT 7, 6, 4, '2026-02-15', '2026-02-22', 'CONFIRMED', true, NOW(), 1140.00 UNION ALL
    SELECT 7, 4, 5, '2026-09-05', '2026-09-10', 'PENDING', false, NOW(), 600.00 UNION ALL
    SELECT 7, 8, 12, '2026-03-20', '2026-03-27', 'CONFIRMED', true, NOW(), 1360.00 UNION ALL
    SELECT 8, 3, 2, '2026-06-01', '2026-06-05', 'CONFIRMED', true, NOW(), 420.00 UNION ALL
    SELECT 8, 5, 6, '2026-08-15', '2026-08-20', 'PENDING', false, NOW(), 700.00 UNION ALL
    SELECT 8, 2, 11, '2026-04-10', '2026-04-13', 'CANCELLED', true, NOW(), 300.00 UNION ALL
    SELECT 8, 6, 10, '2026-11-10', '2026-11-15', 'CONFIRMED', true, NOW(), 1000.00
) AS tmp
WHERE @is_initialized = 0;


-- Marcar como inicializado (solo si no estaba antes)
INSERT INTO db_initialization (id)
SELECT 1 WHERE @is_initialized = 0;

-- Reset sequences solo si se insertaron datos
SET @reset_sequences = (SELECT COUNT(*) FROM db_initialization WHERE id = 1);
ALTER TABLE users AUTO_INCREMENT = 9;
ALTER TABLE accommodations AUTO_INCREMENT = 13;
ALTER TABLE reservations AUTO_INCREMENT = 18;