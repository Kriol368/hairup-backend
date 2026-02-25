-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Servidor: localhost
-- Tiempo de generación: 25-02-2026 a las 19:17:42
-- Versión del servidor: 10.4.28-MariaDB
-- Versión de PHP: 8.2.4

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de datos: `hairup`
--

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `booking`
--

CREATE TABLE `booking` (
  `id` int(11) NOT NULL,
  `date` date NOT NULL,
  `time` time NOT NULL,
  `status` int(11) NOT NULL DEFAULT 0,
  `user_id` int(11) NOT NULL,
  `service_id` int(11) NOT NULL,
  `barber_id` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `booking`
--

INSERT INTO `booking` (`id`, `date`, `time`, `status`, `user_id`, `service_id`, `barber_id`) VALUES
(4, '2026-12-26', '15:00:00', 0, 7, 1, NULL),
(5, '2026-01-14', '16:16:55', 1, 7, 1, NULL),
(6, '2026-02-15', '14:30:00', 0, 7, 1, NULL),
(7, '2026-12-26', '15:00:00', 2, 7, 1, 7),
(8, '2026-12-26', '14:30:00', 3, 7, 1, 7),
(9, '2026-02-24', '17:30:00', 2, 8, 1, 7),
(10, '2026-02-23', '09:00:00', 0, 8, 1, 7),
(11, '2026-02-23', '13:00:00', 2, 8, 1, 7),
(12, '2026-02-23', '15:00:00', 3, 8, 1, 7),
(13, '2026-02-23', '12:30:00', 1, 8, 1, 7),
(14, '2026-02-27', '14:00:00', 0, 14, 4, 7),
(15, '2026-02-26', '16:00:00', 0, 8, 3, 7),
(16, '2026-02-27', '11:30:00', 3, 8, 4, 7);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `category`
--

CREATE TABLE `category` (
  `id` int(11) NOT NULL,
  `name` text NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `category`
--

INSERT INTO `category` (`id`, `name`) VALUES
(1, 'Champus'),
(2, 'Peines');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `level`
--

CREATE TABLE `level` (
  `id` int(11) NOT NULL,
  `name` text NOT NULL,
  `required` int(11) NOT NULL,
  `reward` text NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `level`
--

INSERT INTO `level` (`id`, `name`, `required`, `reward`) VALUES
(1, 'Nivel 1', 0, 'Ninguna'),
(2, 'Nivel 2', 150, '10% descuento');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `product`
--

CREATE TABLE `product` (
  `id` int(11) NOT NULL,
  `name` text NOT NULL,
  `description` text DEFAULT NULL,
  `price` double NOT NULL,
  `image` text DEFAULT NULL,
  `available` tinyint(1) NOT NULL DEFAULT 0,
  `points` int(11) NOT NULL,
  `category_id` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `product`
--

INSERT INTO `product` (`id`, `name`, `description`, `price`, `image`, `available`, `points`, `category_id`) VALUES
(3, 'Peine', 'Un peine', 2.5, 'foto.png', 1, 0, 2),
(5, 'Champú', 'Shampoo para cabello seco', 19.99, 'https://media.istockphoto.com/id/93248129/es/foto/champ%C3%BA.jpg?s=612x612&w=0&k=20&c=ftr_hVRGMxYRb8uZLXrvVO5-gXZqnNrHbUyDPjcBZJ8=', 1, 0, 1),
(6, 'Acondicionador', 'Un acondicionador', 9, NULL, 1, 55, NULL);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `rewards`
--

CREATE TABLE `rewards` (
  `id` int(11) NOT NULL,
  `name` varchar(100) NOT NULL,
  `description` text DEFAULT NULL,
  `points_cost` int(11) NOT NULL,
  `min_level_id` int(11) NOT NULL,
  `available` tinyint(1) DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=hebrew COLLATE=hebrew_bin;

--
-- Volcado de datos para la tabla `rewards`
--

INSERT INTO `rewards` (`id`, `name`, `description`, `points_cost`, `min_level_id`, `available`) VALUES
(13, 'Corte gratis', 'Disfruta de un corte de pelo completamente gratis', 500, 1, 1),
(14, '20% descuento en productos', 'Descuento especial en toda la tienda', 300, 1, 1),
(15, 'Tratamiento capilar gratis', 'Hidrataciu00f3n profunda sin costo', 800, 2, 1),
(16, 'Pack de champu00fa premium', 'Llu00e9vate el set completo de cuidado capilar', 600, 2, 1),
(17, 'Cita VIP sin esperas', 'Prioridad en la reserva de citas', 400, 1, 1),
(18, 'Peine profesional de regalo', 'Peine de alta calidad para tu cuidado diario', 150, 1, 1);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `service`
--

CREATE TABLE `service` (
  `id` int(11) NOT NULL,
  `name` text NOT NULL,
  `description` text DEFAULT NULL,
  `price` double NOT NULL,
  `duration` int(11) NOT NULL,
  `xp` int(11) NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `service`
--

INSERT INTO `service` (`id`, `name`, `description`, `price`, `duration`, `xp`) VALUES
(1, 'Cortar', 'Cortar el pelo', 9, 30, 100),
(3, 'Corte de pelo premium', 'Corte con acabado profesional y productos de calidad', 25, 45, 40),
(4, 'Tinte', 'Tintar el pelo', 25, 90, 200);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `user`
--

CREATE TABLE `user` (
  `id` int(11) NOT NULL,
  `email` text NOT NULL,
  `password` text NOT NULL,
  `name` text NOT NULL,
  `xp` int(11) NOT NULL DEFAULT 0,
  `admin` tinyint(1) NOT NULL DEFAULT 0,
  `phone` text DEFAULT NULL,
  `created` date NOT NULL,
  `level_id` int(11) DEFAULT 1,
  `points` int(11) DEFAULT 0,
  `active` tinyint(1) DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `user`
--

INSERT INTO `user` (`id`, `email`, `password`, `name`, `xp`, `admin`, `phone`, `created`, `level_id`, `points`, `active`) VALUES
(1, 'admin@hairup.com', '$2a$10$/0enS4lytXTARIqg1pj7GeBTr1UgYL6G03wH3pI/5cPn3bAfQuXJO', 'Admin Principal', 0, 1, '1', '2026-02-23', 1, 0, 1),
(7, 'test@testmail.com', '$2a$10$Tr9o0FuCg9ot28QsRKxMMuysW8BjmCSgAOGXBQnY36adnhk6ZX/eO', 'Test', 0, 1, '123456789', '2025-12-17', 1, 0, 1),
(8, 'usu@gmail.com', '$2a$10$KRUbQCx6uVG3/D.09KVCJub3TZ37K8IAdW3gSkwSHtP6hdSpVh/Cu', 'Usuario', 515, 0, '123456789', '2026-02-18', 2, 7709, 0),
(10, 'prueba1@gmail.com', '$2a$10$TtBmyqRiVKzB200c/OKnc.ZMKOGPc2eRk2MwM3N25kAyZFBLoGLnW', 'Prueba', 0, 0, '691118911', '2026-02-24', 1, 0, 1),
(11, 's@s.com', '$2a$10$uooJM5NPBikY.m/3jkjBFOipJ/eYo76d14/FFLRAFoBLedso1eTUS', 's', 0, 0, '12', '2026-02-24', 1, 0, 1),
(12, 'w@w.com', '$2a$10$XW2u0dhVbKo1XgAVB6BWCuMj7RMQ2YWx/tH/YQV7z1DKx8kSDzOvu', 'w', 0, 0, '123', '2026-02-24', 1, 0, 1),
(13, 'b@b.com', '$2a$10$YzGqypx94gXxVsbrzhYEsuaB3IF40VVB8prO.1rU9oK5PrlUzQc6G', 'a', 0, 0, '4', '2026-02-24', 1, 0, 0),
(14, 'prueba@gmail.com', '$2a$10$xuDpPmrzA0yzldmej6niB.Lv9JDsJw3vYWH1KeWaf/0lcPn86GJ/q', 'Prueba', 200, 0, '123456789', '2026-02-24', 2, 200, 1);

--
-- Índices para tablas volcadas
--

--
-- Indices de la tabla `booking`
--
ALTER TABLE `booking`
  ADD PRIMARY KEY (`id`),
  ADD KEY `booking_service_id_fk` (`service_id`),
  ADD KEY `booking_user_id_fk` (`user_id`),
  ADD KEY `fk_booking_barber` (`barber_id`);

--
-- Indices de la tabla `category`
--
ALTER TABLE `category`
  ADD PRIMARY KEY (`id`);

--
-- Indices de la tabla `level`
--
ALTER TABLE `level`
  ADD PRIMARY KEY (`id`);

--
-- Indices de la tabla `product`
--
ALTER TABLE `product`
  ADD PRIMARY KEY (`id`),
  ADD KEY `product_category_id_fk` (`category_id`);

--
-- Indices de la tabla `rewards`
--
ALTER TABLE `rewards`
  ADD PRIMARY KEY (`id`),
  ADD KEY `rewards_level_id_fk` (`min_level_id`);

--
-- Indices de la tabla `service`
--
ALTER TABLE `service`
  ADD PRIMARY KEY (`id`);

--
-- Indices de la tabla `user`
--
ALTER TABLE `user`
  ADD PRIMARY KEY (`id`),
  ADD KEY `user_level_id_fk` (`level_id`);

--
-- AUTO_INCREMENT de las tablas volcadas
--

--
-- AUTO_INCREMENT de la tabla `booking`
--
ALTER TABLE `booking`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=17;

--
-- AUTO_INCREMENT de la tabla `category`
--
ALTER TABLE `category`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT de la tabla `level`
--
ALTER TABLE `level`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT de la tabla `product`
--
ALTER TABLE `product`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9;

--
-- AUTO_INCREMENT de la tabla `rewards`
--
ALTER TABLE `rewards`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=19;

--
-- AUTO_INCREMENT de la tabla `service`
--
ALTER TABLE `service`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT de la tabla `user`
--
ALTER TABLE `user`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=15;

--
-- Restricciones para tablas volcadas
--

--
-- Filtros para la tabla `booking`
--
ALTER TABLE `booking`
  ADD CONSTRAINT `booking_service_id_fk` FOREIGN KEY (`service_id`) REFERENCES `service` (`id`),
  ADD CONSTRAINT `booking_user_id_fk` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  ADD CONSTRAINT `fk_booking_barber` FOREIGN KEY (`barber_id`) REFERENCES `user` (`id`) ON DELETE SET NULL;

--
-- Filtros para la tabla `product`
--
ALTER TABLE `product`
  ADD CONSTRAINT `product_category_id_fk` FOREIGN KEY (`category_id`) REFERENCES `category` (`id`);

--
-- Filtros para la tabla `rewards`
--
ALTER TABLE `rewards`
  ADD CONSTRAINT `rewards_level_id_fk` FOREIGN KEY (`min_level_id`) REFERENCES `level` (`id`);

--
-- Filtros para la tabla `user`
--
ALTER TABLE `user`
  ADD CONSTRAINT `user_level_id_fk` FOREIGN KEY (`level_id`) REFERENCES `level` (`id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
