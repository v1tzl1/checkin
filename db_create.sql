CREATE TABLE `tickets` (
  `ticketid` int(10) unsigned NOT NULL,
  `product_str` text,
  `options_str` text,
  `customer_str` tinytext,
  `customer_email` tinytext,
  `customer_phone` tinytext,
  `valid` tinyint(1) unsigned DEFAULT NULL,
  `use_counter` tinyint(3) unsigned DEFAULT '0',
  `use_lastdate` datetime DEFAULT NULL,
  PRIMARY KEY (`ticketid`),
  UNIQUE KEY `ticketid_UNIQUE` (`ticketid`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8