package at.smartBuyer.sqlcommunication;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;

public class StoreInfo {
	public static String database = "listingsdb";
	public static String username = "Username";
	public static String password = "Password";
	public static String url = "jdbc:postgresql://localhost/" + database;
	public static String table = "listings";
	public static String reftable = "comparison";
	public static String cattable = "categorical";
	public static String schema = "public";
	
	public static Connection connection = null;
	public static Statement statement = null;
	public static ResultSet rset = null;
	public boolean databaseListChanged = false;
	public int rs = -1;
	
	//This class is static since it is called without explicitly knowing the StoreInfo object.  That being said,
	//I am looking for workarounds to make this a non-static class.
	public static class ReferenceValues{
		public long itemid;
		public long checksumvalue;
		//int totalsearches;
		//int low price flags;
		//int searchesperday;
	}

	//Connects to the PostgreSQL database.  By default, a system "user" will result in PSQL attempting to connect
	//to database "user", which will likely be a false match.  Therefore, unless the PSQL default directory is 
	//manually changed, it is recommended that the user of this program initially creates the database 
	//via query CREATEDB "databasename".
	public static void establishConnection(){
		try{
			connection = DriverManager.getConnection(url, username, password);
			System.out.println("Successfully connected to database: " + database);
			
		}catch (SQLException e) {
			System.out.println("Could not establish a connection to database.");
			e.printStackTrace();
		}
	}
	
	public static boolean checkTableExists(String tablename){
		try {
			statement = connection.createStatement();
			rset = statement.executeQuery("SELECT EXISTS(SELECT * FROM " + schema + "." + tablename + ")");
			statement.close();
			if(rset != null)
				return true;

		} catch (SQLException e){
			System.out.println("Table " + tablename + " was not found.  Attempting to initiallize " + tablename + ".");
		}
		return false;
	}
	
	//Checks if the table that stores pricing information currently exists.  If not, the table will be created.
	//By default, the table will be named "listings".
	public static void lookupListingsTable(){
		try {
			statement = connection.createStatement();
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + table + " (itemid bigint PRIMARY KEY, itembrand varchar(20), "
					+ "itemtype varchar(50), price numeric(7,2), listingdate date, seller varchar(30))");
			
			statement.close();
			System.out.println("Accessing table: " + table);
		} catch (SQLException e) {
			System.out.println("Failed to access table: " + table);
			e.printStackTrace();
		}
		
	}
	
	//Deletes the table "listings", can be used as a reset.  
	public static void deleteListingsTable(){
		try {
			statement = connection.createStatement();
			statement.executeUpdate("DROP TABLE " + table);
			statement.close();
			
		} catch (SQLException e) {
			System.out.println("Failed to delete table.");
			e.printStackTrace();
		}
	}
	
	//Initializes reference table used to make comparisons between search listings and stored data.  If this is the
	//first time run through, it will set the initial crc and last checked item id to both be 0.
	public static void lookupComparisonTable(){
		if(!StoreInfo.checkTableExists(reftable)){
			try {
				statement = connection.createStatement();
				statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + reftable + " (category varchar(80), numerical bigint)");
				statement.close();
			} catch (SQLException e){
				System.out.println("Table " + reftable + " could not be created.");
			}
			
			StoreInfo.insertReference(0, 0);
		}

	}
	

	
	//Deletes the reference table if needed.
	public static void deleteComparisonTable(){
		try {
			statement = connection.createStatement();
			statement.executeUpdate("DROP TABLE " + reftable);
			statement.close();
		} catch (SQLException e) {
			System.out.println("Failed to delete table: " + reftable);
			e.printStackTrace();
		}
	}
	
	public static void lookupCategoricalTable(){
		
		if(!StoreInfo.checkTableExists(cattable)){
			try {
				statement = connection.createStatement();
				statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + cattable + " (name varchar(50) PRIMARY KEY, frequency int, "
						+ "lastseen date, appearancerate numeric(5,2), averageprice numeric(7,2))");
				statement.close();
				System.out.println("Accessing table: " + table);
			} catch (SQLException e) {
				System.out.println("Failed to access table: " + table);
				e.printStackTrace();
			}
		}
	}
	
	//Updates the reference table with the last found Item ID
	//This is used for minimize search redundancies by comparing whether an Item ID has already been searched.
	public void updateLastFound(long itemid, long crc){
		try{
			statement = connection.createStatement();
			statement.executeUpdate("UPDATE " + reftable + " SET numerical = " + itemid + " WHERE category = 'lastfound'");
			statement.executeUpdate("UPDATE " + reftable + " SET numerical = " + crc + " WHERE category = 'checksum'");
			statement.close();
		} catch (SQLException e){
			System.out.println("Failed to reset table.");
			e.printStackTrace();
		}
	}
	
	//Resets the table values.
	public static void resetLastFound(){
		try{
			statement = connection.createStatement();
			statement.executeUpdate("UPDATE " + reftable + " SET numerical = 0 WHERE category = 'lastfound'");
			statement.executeUpdate("UPDATE " + reftable + " SET numerical = 0 WHERE category = 'checksum'");
			statement.close();
		} catch (SQLException e){
			System.out.println("Failed to reset table.");
			e.printStackTrace();
		}
	}
	
	//Query to insert data into the table.  Currently supports item id, brand, primary category, price, 
	//and listing date
	public void insertData(long id, String brand, String itemtype, BigDecimal price, String date){
		try {
			statement = connection.createStatement();
			statement.executeUpdate("INSERT INTO " + table +" (itemid, itembrand, itemtype, price, listingdate) "
			+ "VALUES (" + id + ", '" + brand + "', '" + itemtype + "', " + price + ", '" + date + "')");
			statement.close();
		} catch (SQLException e){
			System.out.println("Failed to insert data elements into table: " + table);
			e.printStackTrace();
		}
	}
	
	
	//Inserts reference ID into the table
	public static void insertReference(long id, long crc){
		try {
			statement = connection.createStatement();
			statement.executeUpdate("INSERT INTO " + reftable +" (category, numerical) "
			+ "VALUES ('lastfound', " + id + ")");
			statement.executeUpdate("INSERT INTO " + reftable +" (category, numerical) "
			+ "VALUES ('checksum', " + crc + ")");
			statement.close();
		} catch (SQLException e){
			System.out.println("Failed to insert data elements into table: " + reftable);
			e.printStackTrace();
		}
	}
	
	public void insertCategorical(String name){
		try {
			statement = connection.createStatement();
			statement.executeUpdate("INSERT INTO " + cattable +" (name) "
			+ "VALUES ('" + name + "')");
			statement.close();
		} catch (SQLException e){
			System.out.println("Failed to insert data elements into table: " + reftable);
			e.printStackTrace();
		}
	}
	
	//Searches for underpriced items below a threshold value.  searchLowPricesAllBrands is primarily used
	//when the number of listings in the PSQL table is still very minimal, and where there is insufficient
	//data to search by individual brands.
	public static void searchLowPricesAllBrands(BigDecimal threshold){
		try{
			statement = connection.createStatement();
			connection.setAutoCommit(false);
			statement.setFetchSize(0);
			rset = statement.executeQuery("SELECT itemid, itembrand, itemtype, price, listingdate FROM " + table
					+ "WHERE price < " + threshold + "*(SELECT avg(price) FROM listings) ORDER BY price ASC");
			while(rset.next()){
				System.out.println("Item ID: " + rset.getLong("itemid") + " Brand: " + rset.getString("itembrand") + 
						" Category: " + rset.getString("itemtype") + " Price: " + rset.getBigDecimal("price")
						+ " Listing Date: " + rset.getString("listingdate"));
			}
			statement.close();
		} catch (SQLException e){
			System.out.println("Error while searching for low price offers across all brands.");
			e.printStackTrace();
		}
	}
	
	//Searches for underpriced items below a threshold value of 30% below the average price of all brands.  
	//searchLowPricesAllBrands is primarily used when the number of listings in the PSQL table is still 
	//very minimal, and where there is insufficient data to search by individual brands.
	public static void searchLowPricesAllBrands(){
		try{
			statement = connection.createStatement();
			connection.setAutoCommit(false);
			statement.setFetchSize(0);
			rset = statement.executeQuery("SELECT itemid, itembrand, itemtype, price, listingdate FROM " + table
					+ "WHERE price < 0.3*(SELECT avg(price) FROM listings) ORDER BY price ASC");
			while(rset.next()){
				System.out.println("Item ID: " + rset.getInt("itemid") + " Brand: " + rset.getString("itembrand") + 
						" Category: " + rset.getString("itemtype") + " Price: " + rset.getBigDecimal("price")
						+ " Listing Date: " + rset.getString("listingdate"));
			}
			statement.close();
		} catch (SQLException e){
			System.out.println("Error while searching for low price offers across all brands.");
			e.printStackTrace();
		}
	}
	
	//CURRENTLY DOES NOT WORK
	//Searches for underpriced items below a threshold value of 30% below the average price of individual brands.  
	//searchLowPricesIndividualBrands is primarily used when the number of listings in the PSQL table is sufficient
	//and the price variation across all number of brands is very high.
	public static void searchLowPricesIndividualBrands(){
		try{
			statement = connection.createStatement();
			connection.setAutoCommit(false);
			statement.setFetchSize(0);
			rset = statement.executeQuery("SELECT itemid, itembrand, itemtype, price, listingdate FROM " + table
					+ "WHERE (SELECT price OVER (PARTITION BY itembrand) FROM listings) < 0.3*(SELECT avg(price) OVER "
					+ "(PARTITION BY itembrand) FROM listings) ORDER BY price ASC");
			while(rset.next()){
				System.out.println("Item ID: " + rset.getInt("itemid") + " Brand: " + rset.getString("itembrand") + 
						" Category: " + rset.getString("itemtype") + " Price: " + rset.getBigDecimal("price")
						+ " Listing Date: " + rset.getString("listingdate"));
			}
			statement.close();
		} catch (SQLException e){
			System.out.println("Error while searching for low price offers across individual brands.");
			e.printStackTrace();
		}
	}
	
	//CURRENTLY DOES NOT WORK
	//Searches for underpriced items below a user input threshold value below the average price of individual brands.  
	//searchLowPricesIndividualBrands is primarily used when the number of listings in the PSQL table is sufficient
	//and the price variation across all number of brands is very high.
	public static void searchLowPricesIndividualBrands(BigDecimal threshold){
		//Work in Progress
	}
	
	//Used to check for duplicate listings.  EBay has a unique item id for all listings, and the item id is checked
	//against the database table to prevent the same listing from being stored multiple times.
	public boolean checkDuplicate(long id){
		try{
			statement = connection.createStatement();
			rset = statement.executeQuery("SELECT itemid FROM " + table + " WHERE itemid IN (" + id + ")");			
			if(rset.next()){
				return true;
			}
			statement.close();
		} catch (SQLException e){
			System.out.println("Something went wrong while checking for existing listings.");
			e.printStackTrace();
		}
		return false;
	}
	
	public boolean checkCategoricalDuplicate(String name){
		try{
			statement = connection.createStatement();
			rset = statement.executeQuery("SELECT name FROM " + cattable + " WHERE name IN ('" + name + "')");			
			if(rset.next()){
				return true;
			}
			statement.close();
		} catch (SQLException e){
			System.out.println("Something went wrong while checking for existing categories.");
			e.printStackTrace();
		}
		return false;
	}
	
	//Returns the Item ID stored in the table.  This Item ID is then compared against future searches to ensure
	//that no search redundancies occur.
	public ReferenceValues checkSearched(){
		ReferenceValues meta = new ReferenceValues();
		meta.itemid = -1;
		meta.checksumvalue = -1;
		try{
			statement = connection.createStatement();
			rset = statement.executeQuery("SELECT numerical FROM " + reftable + " WHERE category = 'lastfound'");			
			if(rset.next())
				meta.itemid = rset.getLong("numerical");
			rset = statement.executeQuery("SELECT numerical FROM " + reftable + " WHERE category = 'checksum'");	
			if(rset.next())
				meta.checksumvalue = rset.getLong("numerical");
			statement.close();
		} catch (SQLException e){
			System.out.println("Error accessing metadata table.  Try resetting reftable or ensure it refers to the "
					+ "correct table.");
			e.printStackTrace();
		}

		return meta;
	}
	

	
	public HashSet<String> retrieveCategorical(){
		HashSet<String> hs = new HashSet<String>();
		try{
			statement = connection.createStatement();
			rset = statement.executeQuery("SELECT name FROM " + cattable);
			while(rset.next()){
				hs.add(rset.getString("name"));
			}
			statement.close();
		} catch (SQLException e){
			System.out.println("Something went wrong while checking for the current checksum.");
			e.printStackTrace();
		}
		return hs;
	}
	
	
	public static void main(String[] args){

	}
}

