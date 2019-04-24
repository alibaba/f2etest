create database `f2etest` default character set utf8 collate utf8_general_ci;

use f2etest;

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for appLogs
-- ----------------------------
CREATE TABLE `appLogs` (
  `logId` bigint(20) NOT NULL AUTO_INCREMENT,
  `UserId` varchar(100) DEFAULT '',
  `appId` varchar(50) DEFAULT '',
  `isWeb` tinyint(4) DEFAULT '1',
  `LogTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`logId`),
  KEY `UserId` (`LogTime`,`UserId`) USING BTREE,
  KEY `appOrder` (`LogTime`,`appId`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=97472 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for appUsers
-- ----------------------------
CREATE TABLE `appUsers` (
  `UserId` varchar(100) NOT NULL,
  `RemotePassword` varchar(64) DEFAULT NULL,
  `ApiKey` varchar(64) DEFAULT NULL,
  `ActiveTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `LastTime` datetime DEFAULT NULL,
  `LastIp` varchar(50) DEFAULT NULL,
  `Name` varchar(255) DEFAULT NULL,
  `NickName` varchar(255) DEFAULT NULL,
  `Gender` varchar(10) DEFAULT NULL,
  `Job` varchar(255) DEFAULT NULL,
  `Company` varchar(255) DEFAULT NULL,
  `Department` varchar(255) DEFAULT NULL,
  `Location` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`UserId`),
  KEY `ActiveTime` (`ActiveTime`) USING BTREE,
  KEY `Company` (`Company`),
  KEY `Location` (`Location`),
  KEY `Department` (`Department`),
  KEY `Job` (`Job`),
  KEY `Gender` (`Gender`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for wd_jsunit
-- ----------------------------
CREATE TABLE `wd_jsunit` (
  `task_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `userid` varchar(255) DEFAULT NULL,
  `client_ip` varchar(50) CHARACTER SET latin1 DEFAULT NULL,
  `url` varchar(2000) DEFAULT NULL,
  `browser_name` varchar(20) CHARACTER SET latin1 DEFAULT NULL,
  `browser_version` varchar(10) CHARACTER SET latin1 DEFAULT NULL,
  `coverage_include` varchar(2000) CHARACTER SET latin1 DEFAULT NULL,
  `coverage_exclude` varchar(2000) CHARACTER SET latin1 DEFAULT NULL,
  `coverage_beautify` tinyint(10) DEFAULT NULL,
  `hosts` longtext,
  `browser_id` int(11) DEFAULT NULL,
  `add_time` datetime DEFAULT NULL,
  `run_status` tinyint(4) DEFAULT '0',
  `end_time` datetime DEFAULT NULL,
  `actual_browser_name` varchar(50) CHARACTER SET latin1 DEFAULT NULL,
  `actual_browser_version` varchar(50) CHARACTER SET latin1 DEFAULT NULL,
  `test_type` varchar(20) CHARACTER SET latin1 DEFAULT NULL,
  `test_success` tinyint(4) DEFAULT '0',
  `test_all_count` int(11) DEFAULT '0',
  `test_failed_count` int(11) DEFAULT '0',
  `test_passed_count` int(11) DEFAULT '0',
  `test_ratio` decimal(5,2) DEFAULT NULL,
  `line_cover` decimal(5,2) DEFAULT '0.00',
  `branch_cover` decimal(5,2) DEFAULT '0.00',
  `function_cover` decimal(5,2) DEFAULT '0.00',
  `test_result_data` longtext,
  PRIMARY KEY (`task_id`)
) ENGINE=MyISAM AUTO_INCREMENT=1120 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for wd_logs
-- ----------------------------
CREATE TABLE `wd_logs` (
  `log_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `type` varchar(10) CHARACTER SET latin1 DEFAULT NULL,
  `userid` varchar(255) CHARACTER SET latin1 DEFAULT NULL,
  `data` varchar(1000) DEFAULT NULL,
  `log_time` datetime DEFAULT NULL,
  PRIMARY KEY (`log_id`)
) ENGINE=MyISAM AUTO_INCREMENT=2535 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for wd_browsers
-- ----------------------------
CREATE TABLE `wd_browsers` (
  `browser_id` int(11) NOT NULL AUTO_INCREMENT,
  `browser_name` varchar(20) DEFAULT '',
  `browser_version` varchar(10) DEFAULT '',
  `node_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`browser_id`),
  UNIQUE KEY `node_id` (`node_id`,`browser_name`,`browser_version`)
) ENGINE=MyISAM AUTO_INCREMENT=89 DEFAULT CHARSET=latin1;

-- ----------------------------
-- Table structure for wd_nodes
-- ----------------------------
CREATE TABLE `wd_nodes` (
  `node_id` int(11) NOT NULL AUTO_INCREMENT,
  `work_status` tinyint(4) DEFAULT '0',
  `node_ip` varchar(20) DEFAULT NULL,
  `node_name` varchar(2) DEFAULT NULL,
  `rdp_support` tinyint(4) DEFAULT '1',
  `last_report_time` datetime DEFAULT NULL,
  `last_apply_userid` varchar(255) DEFAULT NULL,
  `last_apply_time` datetime DEFAULT NULL,
  PRIMARY KEY (`node_id`),
  UNIQUE KEY `node_ip` (`node_ip`,`node_name`)
) ENGINE=MyISAM AUTO_INCREMENT=73 DEFAULT CHARSET=latin1;