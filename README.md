# checkin
Checkin is meant to be used at the entrance of an event to check whether a ticket number was already allowed entrance or not.

### Data import
The data for an event is imported from a CSV file with the following header

````
"Select","Order Id","*Ticket Name","*Ticket Number","*Product","*Model","*Option's","Customer","Email","Telephone"
````

The fields marked with an asterisk are read by the software. The absolute and relative of the fields need to be the same as above, additional fields could be added at the end. The values for "Ticket Number" need to be numeric and unique.

### MySQL
To store the data a MySQL table is used that needs to be initialised before the software can run.

#### Table
The table can be created with the following SQL snippet. The database and table name can be chosen freely.

````SQL
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
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
````

#### User permissions
To perform the checkin within the software the MySQL user needs the rights for *SELECT* and *UPDATE* on the table and *LOCK TABLES* on the database.

For the data import the user additionally needs the rights for *DELETE* and *INSERT* on the table.

#### Configuration
The MySQL settings for checkin are controlled by an ASCII coded text file with the name ".checkinconf" that has to be located in the current directory or in the home folder of the current user. The file in the current directory takes precedence.

The file takes one configuration per line started by a keyword followed by a space, followed by the configuration string until the newline character. Lines starting with _#_ are ignored.

An example configuration looks like this:
````
server localhost
port 3306
user checkin
password supersecret
db checkin
table tickets
````

### Feedback
I am looking forward to feedback (preferably over GitHub), be it messages, forks or pull requests.
