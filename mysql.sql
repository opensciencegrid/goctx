-- phpMyAdmin SQL Dump
-- version 3.5.1
-- http://www.phpmyadmin.net
--
-- Host: localhost
-- Generation Time: Mar 28, 2013 at 06:59 PM
-- Server version: 5.0.95
-- PHP Version: 5.3.3

SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Database: `tx`
--

-- --------------------------------------------------------

--
-- Table structure for table `attachment`
--

CREATE TABLE IF NOT EXISTS `attachment` (
  `tx_id` varchar(32) collate utf8_unicode_ci NOT NULL,
  `source_id` varchar(64) collate utf8_unicode_ci NOT NULL,
  `dest_id` varchar(64) collate utf8_unicode_ci NOT NULL,
  `source_attachment_id` varchar(255) collate utf8_unicode_ci NOT NULL,
  PRIMARY KEY  (`tx_id`,`source_id`,`dest_id`,`source_attachment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `sync`
--

CREATE TABLE IF NOT EXISTS `sync` (
  `source_id` varchar(256) NOT NULL,
  `dest_id` varchar(256) NOT NULL,
  `tx_id` varchar(256) NOT NULL,
  `source_timestamp` timestamp NOT NULL default CURRENT_TIMESTAMP,
  PRIMARY KEY  (`source_id`,`dest_id`,`tx_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
