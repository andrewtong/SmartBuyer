package sqlCommunication;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import sqlCommunication.StoreInfo;


public class StoreInfo {
	private static String database = "listingsdb";
	private static String username = "Username";
	private static String password = "Password";
	private static String url = "jdbc:postgresql://localhost/" + database;
	private static String table = "listings";
	private static String reftable = "comparison";
	
	private static Connection connection = null;
	private static Statement statement = null;
	static ResultSet rset = null;
	boolean databaseListChanged = false;
	int rs = -1;
	
	
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
	
	//Initializes reference table used to make comparisons between search listings and stored data.
	public static void lookupComparisonTable(){
		try {
			statement = connection.createStatement();
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + reftable + " (category varchar(80), itemid bigint)");
			statement.close();
		} catch (SQLException e){
			System.out.println("Table " + reftable + " failed to be created.");
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
	
	//Updates the reference table with the last found Item ID
	//This is used for minimize search redundancies by comparing whether an Item ID has already been searched.
	public static void updateLastFound(long itemid){
		try{
			statement = connection.createStatement();
			statement.executeUpdate("UPDATE " + reftable + " set itemid = " + itemid + " where category = 'lastfound'");
			statement.close();
		} catch (SQLException e){
			System.out.println("Failed to reset table.");
			e.printStackTrace();
		}
	}
	
	//Resets the table values
	public static void resetLastFound(){
		try{
			statement = connection.createStatement();
			statement.executeUpdate("DELETE FROM " + reftable);
			statement.close();
		} catch (SQLException e){
			System.out.println("Failed to reset table.");
			e.printStackTrace();
		}
	}
	
	//Query to insert data into the table.  Currently supports item id, brand, primary category, price, 
	//and listing date
	public static void insertData(long id, String brand, String itemtype, BigDecimal price, String date){
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
	public static void insertReference(long id){
		try {
			statement = connection.createStatement();
			statement.executeUpdate("INSERT INTO " + reftable +" (category, itemid) "
			+ "VALUES ('lastfound', " + id + ")");
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
	public static boolean checkDuplicate(long id){
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
	
	
	//Returns the Item ID stored in the table.  This Item ID is then compared against future searches to ensure
	//that no search redundancies occur.
	public static long checkSearched(){
		try{
			statement = connection.createStatement();
			rset = statement.executeQuery("SELECT itemid FROM " + reftable + " WHERE category = 'lastfound'");			
			if(rset.next()){
				return rset.getLong("itemid");
			}
			statement.close();
		} catch (SQLException e){
			System.out.println("Something went wrong while checking for existing listings.");
			e.printStackTrace();
		}
		return -1;
	}

}