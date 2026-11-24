-- MySQL dump 10.13  Distrib 8.0.37, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: techlab
-- ------------------------------------------------------
-- Server version	8.0.37

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `articulo`
--

DROP TABLE IF EXISTS `articulo`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `articulo` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `categoria` varchar(80) NOT NULL,
  `descripcion` varchar(1000) NOT NULL,
  `imagen` varchar(2048) DEFAULT NULL,
  `nombre` varchar(120) NOT NULL,
  `precio` decimal(12,2) NOT NULL,
  `stock` int NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `articulo_chk_1` CHECK ((`stock` >= 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `articulo`
--

LOCK TABLES `articulo` WRITE;
/*!40000 ALTER TABLE `articulo` DISABLE KEYS */;
/*!40000 ALTER TABLE `articulo` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `categorias`
--

DROP TABLE IF EXISTS `categorias`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `categorias` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `nombre` varchar(80) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKqcog8b7hps1hioi9onqwjdt6y` (`nombre`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `categorias`
--

LOCK TABLES `categorias` WRITE;
/*!40000 ALTER TABLE `categorias` DISABLE KEYS */;
INSERT INTO `categorias` VALUES (14,'Adornos'),(15,'Árboles'),(16,'Coronas y guirnaldas'),(17,'Esferas'),(18,'Iluminación'),(19,'Regalos'),(20,'Textiles');
/*!40000 ALTER TABLE `categorias` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `lineas_pedido`
--

DROP TABLE IF EXISTS `lineas_pedido`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `lineas_pedido` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `cantidad` int NOT NULL,
  `precio_unitario` double NOT NULL,
  `subtotal` double NOT NULL,
  `pedido_id` bigint NOT NULL,
  `producto_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKj438esy59j3xndcyvc53w7qf7` (`pedido_id`),
  KEY `FKs293yh6nka5byh793uav2u954` (`producto_id`),
  CONSTRAINT `FKj438esy59j3xndcyvc53w7qf7` FOREIGN KEY (`pedido_id`) REFERENCES `pedidos` (`id`),
  CONSTRAINT `FKs293yh6nka5byh793uav2u954` FOREIGN KEY (`producto_id`) REFERENCES `productos` (`id`),
  CONSTRAINT `lineas_pedido_chk_1` CHECK ((`cantidad` >= 1))
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `lineas_pedido`
--

LOCK TABLES `lineas_pedido` WRITE;
/*!40000 ALTER TABLE `lineas_pedido` DISABLE KEYS */;
INSERT INTO `lineas_pedido` VALUES (1,1,9500,9500,1,3),(2,1,28900,28900,1,4),(3,1,18700,18700,1,2),(4,1,42500,42500,1,6),(5,1,0,0,2,12),(6,1,28900,28900,2,4);
/*!40000 ALTER TABLE `lineas_pedido` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `pedidos`
--

DROP TABLE IF EXISTS `pedidos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pedidos` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `estado` enum('CANCELADO','CONFIRMADO','ENTREGADO','ENVIADO','PENDIENTE') NOT NULL,
  `fecha` datetime(6) NOT NULL,
  `total` double NOT NULL,
  `usuario_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK5g0es69v35nmkmpi8uewbphs2` (`usuario_id`),
  CONSTRAINT `FK5g0es69v35nmkmpi8uewbphs2` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pedidos`
--

LOCK TABLES `pedidos` WRITE;
/*!40000 ALTER TABLE `pedidos` DISABLE KEYS */;
INSERT INTO `pedidos` VALUES (1,'PENDIENTE','2026-11-24 03:07:38.146127',99600,1),(2,'PENDIENTE','2026-11-24 03:08:17.177152',28900,1);
/*!40000 ALTER TABLE `pedidos` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `productos`
--

DROP TABLE IF EXISTS `productos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `productos` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `categoria` varchar(80) NOT NULL,
  `descripcion` varchar(1000) NOT NULL,
  `imagen` varchar(2048) DEFAULT NULL,
  `nombre` varchar(120) NOT NULL,
  `precio` double NOT NULL,
  `stock` int NOT NULL,
  `categoria_id` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `productos_chk_1` CHECK ((`stock` >= 0))
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `productos`
--

LOCK TABLES `productos` WRITE;
/*!40000 ALTER TABLE `productos` DISABLE KEYS */;
INSERT INTO `productos` VALUES (1,'Adornos','Gatito decorativo con gorro rojo, pintado a mano. Un detalle especial para la mesa o el árbol.','assets/navidad/producto-1.png','Gatito Noel de cerámica',12500,1,14),(2,'Esferas','Set de seis esferas combinadas con terminaciones mate y brillantes.','assets/navidad/producto-6.png','Esferas rojas y doradas x6',8900,24,17),(3,'Esferas','Cuatro esferas transparentes con detalles de nieve para una Navidad luminosa.','assets/navidad/producto-7.png','Esferas de cristal nevado x4',10500,30,17),(4,'Coronas y guirnaldas','Corona para la puerta con ramas de pino, frutos rojos y lazo de tela.','assets/navidad/producto-8.png','Corona de pino y frutos rojos',18900,12,16),(5,'Coronas y guirnaldas','Guirnalda verde para decorar escaleras, marcos y mesas de celebración.','assets/navidad/producto-9.png','Guirnalda de pino de 2 metros',14800,21,16),(6,'Árboles','Pino decorativo compacto con base de madera, ideal para espacios pequeños.','assets/navidad/producto-10.png','Árbol de Navidad de mesa',22500,8,15),(7,'Textiles','Bota de tela roja con puño claro y detalles bordados para pequeños regalos.','assets/navidad/producto-11.png','Bota navideña bordada',9500,10,20),(8,'Iluminación','Cadena de luces cálidas para uso interior. Crea un ambiente acogedor en casa.','assets/navidad/producto-12.png','Luces LED cálidas de 5 metros',13400,13,18),(9,'Regalos','Vela en recipiente de vidrio con notas de canela y presentación para regalar.','assets/navidad/producto-13.png','Vela aromática de canela',7900,5,19),(10,'Regalos','Caja reutilizable con lazo y diseño festivo para envolver tus regalos.','assets/navidad/producto-14.png','Caja de regalo navideña',4800,20,19),(11,'Adornos','Adorno de gatito con bufanda y cinta dorada para colgar en el árbol.','assets/navidad/producto-2.png','Gato navideño colgante',6500,1,14),(12,'Adornos','Casita decorativa en tonos crema y rojo para armar un pequeño pueblo navideño.','assets/navidad/producto-3.png','Casita de Navidad',9800,1,14),(13,'Adornos','Figura de madera con terminación artesanal para repisas y centros de mesa.','assets/navidad/producto-4.png','Papá Noel de madera',11500,1,14),(14,'Adornos','Estrella de cinco puntas con acabado dorado para coronar tu árbol.','assets/navidad/producto-5.png','Estrella dorada para el árbol',7200,1,14),(15,'Adornos','Adorno de prueba del catálogo navideño.','assets/navidad/producto-2.png','Adorno navideño de prueba 1',1500,5,14),(16,'Adornos','Adorno de prueba del catálogo navideño.','assets/navidad/producto-2.png','Adorno navideño de prueba 2',1500,5,14),(17,'Adornos','Globo de nieve con casita y pinos en miniatura, sobre una base roja con detalles dorados.','assets/navidad/producto-15.png','Bola de cristal navideña',16900,12,14);
/*!40000 ALTER TABLE `productos` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `usuarios`
--

DROP TABLE IF EXISTS `usuarios`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usuarios` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `email` varchar(160) DEFAULT NULL,
  `nombre` varchar(120) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `usuarios`
--

LOCK TABLES `usuarios` WRITE;
/*!40000 ALTER TABLE `usuarios` DISABLE KEYS */;
INSERT INTO `usuarios` VALUES (1,'cliente@techlab.local','Cliente Demo');
/*!40000 ALTER TABLE `usuarios` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-11-24 10:45:53
