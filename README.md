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
The table can be created with the following SQL snippet. The database ($db) and table ($table) name can be chosen freely and need to match the entry in the checkin configuration file described below.

````SQL
USE $db;
CREATE TABLE `$table` (
  `ticketid` int(10) unsigned NOT NULL,
  `ticket_str` text,
  `product_str` text,
  `options_str` text,
  `valid` tinyint(1) unsigned DEFAULT NULL,
  `use_counter` tinyint(3) unsigned DEFAULT '0',
  PRIMARY KEY (`ticketid`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
````

#### User permissions
To perform the checkin within the software the MySQL user needs the rights for *SELECT* and *UPDATE* on the table and *LOCK TABLES* on the database. These can be set by running the following snippet.

````SQL
GRANT LOCK TABLES ON $db.* TO $user IDENTIFIED BY '$pw';
GRANT SELECT, UPDATE ON $db.$table TO $user IDENTIFIED BY '$pw';
````

For the data import the user additionally needs the rights for *DELETE* and *INSERT* on the table, which can be set with the following snippet.

````SQL
GRANT LOCK TABLES ON $db.* TO $user IDENTIFIED BY '$pw';
GRANT SELECT, UPDATE, DELETE, INSERT ON $db.$table TO $user IDENTIFIED BY '$pw';
````

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
