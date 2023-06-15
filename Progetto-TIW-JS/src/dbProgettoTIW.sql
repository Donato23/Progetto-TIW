CREATE DATABASE  IF NOT EXISTS `dbprogettotiw` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `dbprogettotiw`;
-- MySQL dump 10.13  Distrib 8.0.32, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: dbprogettotiw
-- ------------------------------------------------------
-- Server version	8.0.32

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `appello`
--

DROP TABLE IF EXISTS `appello`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `appello` (
  `idcorso` int NOT NULL,
  `data` date NOT NULL,
  PRIMARY KEY (`idcorso`,`data`),
  KEY `appellodata` (`data`) /*!80000 INVISIBLE */,
  CONSTRAINT `id` FOREIGN KEY (`idcorso`) REFERENCES `corso` (`id`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `appello`
--

LOCK TABLES `appello` WRITE;
/*!40000 ALTER TABLE `appello` DISABLE KEYS */;
INSERT INTO `appello` VALUES (1,'2023-06-23'),(1,'2023-07-12'),(1,'2023-09-03'),(2,'2023-06-10'),(2,'2023-07-20'),(3,'2023-06-25'),(3,'2023-07-03'),(4,'2023-07-05'),(5,'2023-06-19'),(5,'2023-07-08');
/*!40000 ALTER TABLE `appello` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `corso`
--

DROP TABLE IF EXISTS `corso`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `corso` (
  `id` int NOT NULL,
  `nome` varchar(45) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `corso`
--

LOCK TABLES `corso` WRITE;
/*!40000 ALTER TABLE `corso` DISABLE KEYS */;
INSERT INTO `corso` VALUES (1,'Analisi'),(2,'Fisica'),(3,'Meccanica'),(4,'TIW'),(5,'Geometria');
/*!40000 ALTER TABLE `corso` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `frequenta`
--

DROP TABLE IF EXISTS `frequenta`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `frequenta` (
  `matricolastudente` int NOT NULL,
  `idcorso` int NOT NULL,
  PRIMARY KEY (`matricolastudente`,`idcorso`),
  KEY `idcorso_idx` (`idcorso`),
  CONSTRAINT `idcorso2` FOREIGN KEY (`idcorso`) REFERENCES `corso` (`id`) ON UPDATE CASCADE,
  CONSTRAINT `matricolastudente` FOREIGN KEY (`matricolastudente`) REFERENCES `utente` (`matricola`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `frequenta`
--

LOCK TABLES `frequenta` WRITE;
/*!40000 ALTER TABLE `frequenta` DISABLE KEYS */;
INSERT INTO `frequenta` VALUES (9899,1),(9236,2),(9899,3),(9236,4),(9876,4),(9899,4),(9999,4),(9236,5);
/*!40000 ALTER TABLE `frequenta` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `insegna`
--

DROP TABLE IF EXISTS `insegna`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `insegna` (
  `matricoladocente` int NOT NULL,
  `idcorso` int NOT NULL,
  PRIMARY KEY (`matricoladocente`,`idcorso`),
  KEY `idcorso_idx` (`idcorso`),
  CONSTRAINT `idcorso` FOREIGN KEY (`idcorso`) REFERENCES `corso` (`id`) ON UPDATE CASCADE,
  CONSTRAINT `matricola` FOREIGN KEY (`matricoladocente`) REFERENCES `utente` (`matricola`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `insegna`
--

LOCK TABLES `insegna` WRITE;
/*!40000 ALTER TABLE `insegna` DISABLE KEYS */;
INSERT INTO `insegna` VALUES (9572,1),(9572,2),(9333,3),(9333,4),(9333,5);
/*!40000 ALTER TABLE `insegna` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `iscritto`
--

DROP TABLE IF EXISTS `iscritto`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `iscritto` (
  `matricolastudente` int NOT NULL,
  `corso` int NOT NULL,
  `appello` date NOT NULL,
  `verbale` int DEFAULT NULL,
  `voto` enum('ASSENTE','RIMANDATO','RIPROVATO','18','19','20','21','22','23','24','25','26','27','28','29','30','30L') DEFAULT NULL,
  `statovalutazione` enum('NON_INSERITO','INSERITO','PUBBLICATO','RIFIUTATO','VERBALIZZATO') NOT NULL,
  PRIMARY KEY (`matricolastudente`,`corso`,`appello`),
  KEY `appellocorso_idx` (`corso`),
  KEY `appellodata_idx` (`appello`),
  KEY `verbale_idx` (`verbale`),
  CONSTRAINT `appellocorso` FOREIGN KEY (`corso`) REFERENCES `appello` (`idcorso`) ON UPDATE CASCADE,
  CONSTRAINT `appellodata` FOREIGN KEY (`appello`) REFERENCES `appello` (`data`) ON UPDATE CASCADE,
  CONSTRAINT `studente` FOREIGN KEY (`matricolastudente`) REFERENCES `utente` (`matricola`) ON UPDATE CASCADE,
  CONSTRAINT `verbale` FOREIGN KEY (`verbale`) REFERENCES `verbale` (`codice`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `iscritto`
--

LOCK TABLES `iscritto` WRITE;
/*!40000 ALTER TABLE `iscritto` DISABLE KEYS */;
INSERT INTO `iscritto` VALUES (9123,4,'2023-07-05',NULL,NULL,'NON_INSERITO'),(9236,2,'2023-06-10',NULL,'19','PUBBLICATO'),(9236,2,'2023-07-20',NULL,NULL,'NON_INSERITO'),(9236,4,'2023-07-05',NULL,NULL,'NON_INSERITO'),(9236,5,'2023-06-19',1,'RIMANDATO','VERBALIZZATO'),(9876,4,'2023-07-05',NULL,'RIPROVATO','PUBBLICATO'),(9899,1,'2023-06-23',NULL,'18','RIFIUTATO'),(9899,1,'2023-07-12',NULL,NULL,'NON_INSERITO'),(9899,3,'2023-06-25',NULL,'24','INSERITO'),(9899,4,'2023-07-05',2,'30L','VERBALIZZATO'),(9999,4,'2023-07-05',NULL,'29','PUBBLICATO');
/*!40000 ALTER TABLE `iscritto` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `utente`
--

DROP TABLE IF EXISTS `utente`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `utente` (
  `matricola` int NOT NULL,
  `nome` varchar(45) NOT NULL,
  `cognome` varchar(45) NOT NULL,
  `password` varchar(45) NOT NULL,
  `ruolo` enum('studente','docente') NOT NULL,
  `email` varchar(45) DEFAULT NULL,
  `corsodilaurea` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`matricola`),
  UNIQUE KEY `matricola_UNIQUE` (`matricola`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `utente`
--

LOCK TABLES `utente` WRITE;
/*!40000 ALTER TABLE `utente` DISABLE KEYS */;
INSERT INTO `utente` VALUES (9123,'Manuela','Tommasi','tommasi','studente','manu.tom@caso.it','informatica'),(9236,'Luca','Rossi','rossi','studente','luca.rossi@caso.it','informatica'),(9333,'Giovanna','Lucia','lucia','docente',NULL,NULL),(9572,'Francesco','Lepore','password','docente',NULL,NULL),(9876,'Carlo','Mancini','mancio','studente','mancio@prova.com','informatica'),(9899,'Marco','Ciufalo','ciufalo','studente','marchino@prova.com','matematica'),(9999,'Sara','Nunziante','nunziante','studente','saretta@prova.com','matematica');
/*!40000 ALTER TABLE `utente` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `verbale`
--

DROP TABLE IF EXISTS `verbale`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `verbale` (
  `codice` int NOT NULL,
  `data` date NOT NULL,
  `ora` time NOT NULL,
  PRIMARY KEY (`codice`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `verbale`
--

LOCK TABLES `verbale` WRITE;
/*!40000 ALTER TABLE `verbale` DISABLE KEYS */;
INSERT INTO `verbale` VALUES (1,'2023-06-30','22:15:00'),(2,'2023-07-15','17:37:00');
/*!40000 ALTER TABLE `verbale` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2023-06-15 19:09:04
