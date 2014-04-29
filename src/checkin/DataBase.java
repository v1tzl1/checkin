package checkin;

import java.awt.Color;
import java.awt.Component;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import au.com.bytecode.opencsv.CSVReader;

public class DataBase {

	public enum Status { Undefined, Invalid, Unused, Used}
	
	private static Connection connection;
	private final static int CSV_CUSTOMER_EMAIL = 8;
	private final static int CSV_CUSTOMER_NAME = 7;
	private final static int CSV_CUSTOMER_PHONE = 9;
	private final static int CSV_MODEL = 5;
	private final static int CSV_OPTIONS = 6;
	private final static int CSV_PRODUCT_NAME = 4;
	// Field indices for CSV dump file
	private final static int CSV_TICKET_NUMBER = 3;
	
	private static String dumpfile = null;
	private static String productname = null;
	private final static int SQL_TIMEOUT = 0;
	private final static String[] STR_STATUS = {"Undefined", "Invalid", "Unused", "Used"};
	private final static Color[] COLOR_STATUS = {Color.GRAY, Color.RED, Color.GREEN, Color.ORANGE};
	
	private static String TABLE_NAME = "";
	
	private static String ticketid = null;
	private static String ticketoptions = null;;
	private static Status ticketstatus = Status.Undefined;
	
	public static synchronized boolean checkInReady(Component parent) {
		boolean ready = false;
		try {
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(SQL_TIMEOUT);
			
			// lock table
			statement.executeUpdate("LOCK TABLES "+TABLE_NAME+" WRITE");
			
			// check that there is at lease one row in the ticket table
			ResultSet result = statement.executeQuery("SELECT COUNT(*) FROM "+TABLE_NAME);
			result.next();
			ready = (result.getInt(1) > 0);
						
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(parent, e.getMessage(), "MySQL Exception", JOptionPane.ERROR_MESSAGE);
		}

		try{
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(SQL_TIMEOUT);
			
			// release lock
			statement.executeUpdate("UNLOCK TABLES");

			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, e.getMessage(), "MySQL Exception", JOptionPane.ERROR_MESSAGE);
		}
		return ready;
	}
	
	public static synchronized void checkInTicket(String id, Component parent) {
		
		// First parse to int, then back to String to make sure it just contains numeric characters
		try {
			ticketid = Integer.toString(Integer.parseInt(id));
		} catch (NumberFormatException e) {
			//JOptionPane.showMessageDialog(parent, "'"+id+"' is not a valid number", "Wrong ticket number", JOptionPane.ERROR_MESSAGE);
			ticketstatus = Status.Invalid;
			ticketoptions = "TicketID is not a valid number";
			productname = "";
			return;
		}
		
		try {
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(SQL_TIMEOUT);
			
			// lock table
			statement.executeUpdate("LOCK TABLES "+TABLE_NAME+" WRITE");
			
			final String CONSTR = "WHERE ticketid = "+Integer.parseInt(ticketid);
			ResultSet result = statement.executeQuery("SELECT COUNT(*) FROM "+TABLE_NAME+" "+CONSTR);
			
			result.next();
			final int num_rows = result.getInt(1);
			if(num_rows == 0) {
				ticketstatus = Status.Invalid;
				ticketoptions = "TicketID was not found in Database";
				productname = "";
				return;
			} else if(num_rows != 1) {
				JOptionPane.showMessageDialog(parent, "Ticket with ID "+ticketid+" appears more than once in database. Exit.", "Ticket Duplicate", JOptionPane.ERROR_MESSAGE);
				System.err.println("Ticket with ID "+ticketid+" has "+num_rows+" entry in database, although it should be unique.");
				System.err.println("Exit, because I have no clue what happend here");
				System.exit(1);
			}
			
			// Only one data set found, everything is fine
			result = statement.executeQuery("SELECT * FROM "+TABLE_NAME+" "+CONSTR);
			
			result.next();
			int count = result.getInt("use_counter");
			if(result.getInt("valid") == 0) {
				// Invalid
				ticketstatus = Status.Invalid;
				ticketoptions = "TicketID was set to invalid in database";
			} else if(count == 0) {
				ticketstatus = Status.Unused;
			} else {
				ticketstatus = Status.Used;
			}

			ticketoptions = result.getString("options_str");
			productname = result.getString("product_str");
			
			// Update counter
			count = Math.min(255,count+1); // Cap at 255, because database counter is only one byte
			statement.executeUpdate("UPDATE "+TABLE_NAME+" SET use_counter = "+count+" "+CONSTR);
			
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, e.getMessage(), "MySQL Exception", JOptionPane.ERROR_MESSAGE);
			
			ticketid = "";
			ticketoptions = "Lookup error";
			ticketstatus = Status.Invalid;
			productname = "";
			
		} finally {

			try{
				Statement statement = connection.createStatement();
				statement.setQueryTimeout(SQL_TIMEOUT);

				// release lock
				statement.executeUpdate("UNLOCK TABLES");

				statement.close();
			} catch (SQLException e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(null, e.getMessage(), "MySQL Exception", JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
	public static synchronized String[][] getEvents(Component parent) {
		ArrayList<String> models = new ArrayList<String>();
		ArrayList<String> names = new ArrayList<String>();
		List<String[]> myEntries;
		
		if(dumpfile == null) {
			JOptionPane.showMessageDialog(parent, "No data file selected", "IO Exception", JOptionPane.WARNING_MESSAGE);
			
		}
		
		// read file
		try {
	    	CSVReader reader = new CSVReader(new FileReader(dumpfile), ',', '"', 1);
			myEntries = reader.readAll();
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(parent, e.getMessage(), "IO Exception", JOptionPane.WARNING_MESSAGE);
			
			// return empty list
			return new String[0][0];
		}
		
		// iterate over lines
		for(String[] line : myEntries) {
			String model = line[CSV_MODEL];
			
			// if model not yet in list, add it
			if(!models.contains(model)) {
				models.add(model);
				names.add(line[CSV_PRODUCT_NAME]);
			}
		}
		
		String[][] ret = new String[models.size()][2];
		for(int i=0;i<models.size();i++) {
			ret[i][0] = models.get(i);
			ret[i][1] = names.get(i);
		}
		return ret;
	}
	
	public static synchronized String getNumInvalid() {
		int ret = -1;
		try {
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(SQL_TIMEOUT);
			
			ResultSet result = statement.executeQuery("SELECT COUNT(*) FROM "+TABLE_NAME+" WHERE valid = 0");
			result.next();
			ret = result.getInt(1);
			
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return Integer.toString(ret);
	}
	
	
	public static synchronized String getNumTotal() {
		int ret = -1;
		try {
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(SQL_TIMEOUT);
			
			ResultSet result = statement.executeQuery("SELECT COUNT(*) FROM "+TABLE_NAME);
			result.next();
			ret = result.getInt(1);
			
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return Integer.toString(ret);
	}
	
	public static synchronized String getNumUnused() {
		int ret = -1;
		try {
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(SQL_TIMEOUT);
			
			ResultSet result = statement.executeQuery("SELECT COUNT(*) FROM "+TABLE_NAME+" WHERE use_counter = 0 AND valid = 1");
			result.next();
			ret = result.getInt(1);
			
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		if(ret == 0) {
			if(!getNumTotal().equals("0")) {
				JOptionPane.showMessageDialog(null, "Get on your couch!", "Feierabend", JOptionPane.INFORMATION_MESSAGE);
			}
		}
		return Integer.toString(ret);
	}
	public static synchronized String getNumUsed() {
		int ret = -1;
		try {
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(SQL_TIMEOUT);
			
			ResultSet result = statement.executeQuery("SELECT COUNT(*) FROM "+TABLE_NAME+" WHERE use_counter > 0 AND valid = 1");
			result.next();
			ret = result.getInt(1);
			
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return Integer.toString(ret);
	}

	public static synchronized String getProductName() {
		return productname;
	}
	
	public static synchronized String getTicketID() {
			return ticketid;
	}
	public static synchronized String getTicketName() {
		return "not implemented"; // TODO implement ticket name
	}

	public static synchronized String getTicketOptions() {
		return ticketoptions;
	}
	public static synchronized Color getTicketStatusColor() {
		return COLOR_STATUS[ticketstatus.ordinal()];
	}
	public static synchronized String getTicketStatusString() {
		return STR_STATUS[ticketstatus.ordinal()];
	}

	public static synchronized void init() {
		
		// Search config file
		
		// try ./.checkinconf
		File config = new File(".checkinconf");
		
		System.out.println("Looking for config file in");
		System.out.print("  ");
		System.out.print(config.toPath());
		
		// try ~/.checkinconf
		if(!config.isFile()) {
			config = new File(System.getProperty("user.home"), ".checkinconf");
			System.out.println();
			System.out.print("  ");
			System.out.print(config.toPath());
		}
		
		// give up if no file is found
		if(!config.isFile()) {
			System.out.println();
			System.err.println("No config file found");
			JOptionPane.showMessageDialog(null, "Unable to find database configuration file", "Config not found", JOptionPane.ERROR_MESSAGE);
			
			// exit
			System.exit(1);
		}
		
		// config file is found
		System.out.println("  - found");

		if(!config.canRead()) {
			JOptionPane.showMessageDialog(null, "Insufficient permission to read "+config.getPath(), "Config not readable", JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}
		
		String hostname = "localhost";
		int port = 3306; 
		String user = "checkin";
		String pw = "";
		String db = "";
		
		System.out.println("reading config file");
		try {
			for(String line : Files.readAllLines(config.toPath(), Charset.defaultCharset())) {
				
				if(line.startsWith("#")) {
					continue;
				} else if(line.startsWith("server ")) {
					hostname = line.substring(7);
				} else if(line.startsWith("port ")) {
					port = Integer.parseInt(line.substring(5));
				} else if(line.startsWith("user ")) {
					user = line.substring(5);
				} else if(line.startsWith("password ")) {
					pw = line.substring(9);
				} else if(line.startsWith("db ")) {
					db = line.substring(3);
				} else if(line.startsWith("table ")) {
					TABLE_NAME = line.substring(6);
				} else {
					System.err.println("Ignoring line: "+line);
				}
			}
			
		} catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "IOException while reading config file", "IO Exception", JOptionPane.ERROR_MESSAGE);
			
		}
		
		System.out.println("Using the following database parameters:");
		System.out.println("  hostname: \t'"+hostname+"'");
		System.out.println("  port: \t'"+port+"'");
		System.out.println("  database: \t'"+db+"'");
		System.out.println("  table: \t'"+TABLE_NAME+"'");
		System.out.println("  user: \t'"+user+"'");
		System.out.println("  password: \t"+pw.length()+" characters");
		//System.out.println("  password: \t'"+pw+"'");
		
		// load MySQL driver
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Unable to find MySQL driver", "ClassNotFound Exception", JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}
		
		// open connection
		try {
			connection = DriverManager
			          .getConnection("jdbc:mysql://"+hostname+":"+port+"/"+db,user,pw);
			
		} catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, e.getMessage(), "MySQL Exception", JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}
	}

	public static synchronized boolean isInvalid() {
		return ticketstatus == Status.Invalid;
	}
	
	public static synchronized boolean isUnused() {
		return ticketstatus == Status.Unused;
	}
	
	public static synchronized boolean isUsed() {
		return ticketstatus == Status.Used;
	}
	
	public static synchronized void setDumpFile(File file) {
		dumpfile = file.getAbsolutePath();
	}
	
	public static synchronized void setTicketStatus(Status st, Component parent) {
		try {
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(SQL_TIMEOUT);
			statement.executeUpdate("LOCK TABLES "+TABLE_NAME+" WRITE");
			
			final String CONSTR = "WHERE ticketid = "+Integer.parseInt(ticketid);
			ResultSet result = statement.executeQuery("SELECT COUNT(*) FROM "+TABLE_NAME+" "+CONSTR);
			
			result.next();
			final int num_rows = result.getInt(1);
			if(num_rows == 0) {
				JOptionPane.showMessageDialog(parent, "Ticket with ID "+ticketid+" does not appear in database. Status not changed", "Ticket Duplicate", JOptionPane.WARNING_MESSAGE);
				
			} else if(num_rows != 1) {
				JOptionPane.showMessageDialog(parent, "Ticket with ID "+ticketid+" appears more than once in database. Exit.", "Ticket Duplicate", JOptionPane.ERROR_MESSAGE);
				System.err.println("Ticket with ID "+ticketid+" has "+num_rows+" entry in database, although it should be unique.");
				System.err.println("Exit, because I have no clue what happend here");
				System.exit(1);
			}
			
			if(st == Status.Invalid) {
				statement.executeUpdate("UPDATE "+TABLE_NAME+" SET valid = 0 "+CONSTR);
			} else if(st == Status.Unused) {
				statement.executeUpdate("UPDATE "+TABLE_NAME+" SET use_counter = 0, valid=1 "+CONSTR);
			} else if(st == Status.Used){
				// Only one data set found, everything is fine
				result = statement.executeQuery("SELECT * FROM "+TABLE_NAME+" "+CONSTR);
				
				result.next();
				int count = result.getInt("use_counter");
				
				// Update counter
				count = Math.min(255,count+1); // Cap at 255, because database counter is only one byte
				statement.executeUpdate("UPDATE "+TABLE_NAME+" SET use_counter = "+count+", valid=1 "+CONSTR);
			}
			
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, e.getMessage(), "MySQL Exception", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		try{
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(SQL_TIMEOUT);
			
			// release lock
			statement.executeUpdate("UNLOCK TABLES");

			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, e.getMessage(), "MySQL Exception", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		// if succesfull, update status
		ticketstatus = st;
	}
	
	public static synchronized void updateDB(List<String> events, Component parent) {
		if(dumpfile == null) {
			JOptionPane.showMessageDialog(parent, "No data file selected", "IO Exception", JOptionPane.WARNING_MESSAGE);
			
		}
		
		List<String[]> myEntries;
		// read file
		try {
	    	CSVReader reader = new CSVReader(new FileReader(dumpfile), ',', '"', 1);
			myEntries = reader.readAll();
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(parent, "Data file could not be read, database NOT updated.", "IO Exception", JOptionPane.WARNING_MESSAGE);
			
			// return early
			return;
		}
		
		try {
			// clear table
			Statement delstatement = connection.createStatement();
			delstatement.setQueryTimeout(SQL_TIMEOUT);
			delstatement.executeUpdate("DELETE FROM "+TABLE_NAME);
			delstatement.close();
		} catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(parent, e.getMessage(), "MySQL Exception", JOptionPane.ERROR_MESSAGE);
			
		}
		
		try {
			// clear table
			PreparedStatement statement = connection.prepareStatement("INSERT INTO "+TABLE_NAME+" (ticketid, product_str, options_str, customer_str, customer_email, customer_phone, valid, use_counter, use_lastdate) VALUES (?, ?, ?, ?, ?, ?, 1, 0, NOW())");
			for(String line[] : myEntries) {
				String model = line[CSV_MODEL];
				
				// Skip events that are not selected
				if(!events.contains(model)) {
					continue;
				}
				
				// Read parameters from file and add them to the query
				statement.setInt(1, Integer.parseInt(line[CSV_TICKET_NUMBER]));
				statement.setString(2, line[CSV_PRODUCT_NAME]);
				statement.setString(3, line[CSV_OPTIONS]);
				statement.setString(4, line[CSV_CUSTOMER_NAME]);
				statement.setString(5, line[CSV_CUSTOMER_EMAIL]);
				statement.setString(6, line[CSV_CUSTOMER_PHONE]);
				
				// run the query
				statement.executeUpdate();
			}
			
			// close statement
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(parent, e.getMessage(), "MySQL Exception", JOptionPane.ERROR_MESSAGE);
			
		}
		
		return;
	}

}
