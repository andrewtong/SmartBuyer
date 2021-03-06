package at.smartBuyer.sqlcommunication;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;

import at.smartBuyer.metadata.GenerateResults;
import at.smartBuyer.parselistings.ParseListings.ItemListingInfo;

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
	public void lookupListingsTable(){
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
	public void lookupComparisonTable(){
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
	
	public void lookupCategoricalTable(){
		
		if(!StoreInfo.checkTableExists(cattable)){
			try {
				statement = connection.createStatement();
				statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + cattable + " (name varchar(50) PRIMARY KEY, frequency int, "
						+ "lastseen date, averageprice numeric(7,2), marketability int, profitability int)");
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
			//Currently blocked, there is a rare bug which results in this function being called twice.  I believe 
			//it may originate from the end of the EBay API or is some concurrency issue.  The bug is not harmful 
			//in any way, aside from the fact that it attempts to introduce an item into the database twice resulting
			//in a primary key error since the listing already exists.
			//System.out.println("Failed to insert a data element into table: " + table);
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
	
	public void insertCategorical(String name, int freq, String date, BigDecimal price, int marketvalue, int profitvalue){
		try {
			statement = connection.createStatement();
			statement.executeUpdate("UPDATE " + cattable + " SET frequency = " + freq + ", lastseen = '" + date + "', averageprice = " + price 
					+ ", marketability = " + marketvalue + ", profitability = " + profitvalue + " WHERE name = '" 
					+ name + "'");
		
			statement.close();
		} catch (SQLException e){
			System.out.println("Failed to insert data elements into table: " + cattable);
			e.printStackTrace();
		}
	}
	
	public void initiateCategorical(String name, int freq, BigDecimal price, int marketvalue, int profitvalue){
		try {
			statement = connection.createStatement();
			statement.executeUpdate("INSERT INTO " + cattable + 
					" (name, frequency, averageprice, marketability, profitability) "
					+ "VALUES ('" + name + "'," + freq + "," + price + "," + marketvalue + "," + profitvalue + ")");
			statement.close();
		} catch (SQLException e){
			System.out.println("Failed to insert data elements into table: " + cattable);
			e.printStackTrace();
		}
	}
	
	//updateCatTable is performed after a search has been performed AND there have been a new amount of listings
	//added to the database.  updateCatTable will update pricing information across brands, as well as attempt
	//to predict how worthwhile a purchase is making.  Specifically, this function currently looks for the average
	//price of the branded product, the date said product has been seen on EBay, and the total number of said products
	//that have been registered by the database.  Using these elements, the algorithm will then attempt to predict
	//how profitable and how easy it would be to sell an item of said brand.
	public void updateCatTable(){
		try{
			statement = connection.createStatement();
			rset = statement.executeQuery("SELECT name FROM " + cattable);
			while(rset.next()){
				Statement checkentries = connection.createStatement();
				ResultSet total = checkentries.executeQuery("SELECT COUNT (*) FROM " + table + 
						" WHERE itembrand = '" + rset.getString("name") + "'");
				if(total.next()){
					int totalentries = total.getInt("COUNT");
					Statement gatherexistingentries = connection.createStatement();
					ResultSet exists = gatherexistingentries.executeQuery("SELECT frequency FROM " + cattable + 
							" WHERE name = '" + rset.getString("name") + "'");
					if(exists.next()){
						int existingentries = exists.getInt("frequency");
						if(totalentries != existingentries){
							
							//Parameters that are retrievied
							String lastseen = "";
							BigDecimal avgprice = new BigDecimal(0);
							int marketability;
							int profitability;
							
							Statement getdateinfo = connection.createStatement();
							
							//Gets the Date of Last Appearance
							ResultSet entryinfo = getdateinfo.executeQuery("SELECT max(listingdate) AS lastseen FROM " + table + 
									" WHERE itembrand = '" + rset.getString("name") + "'");
							while(entryinfo.next()){
								lastseen = entryinfo.getString("lastseen");
							}
							
							Statement getpriceinfo = connection.createStatement();

							//Get the Average Price
							ResultSet priceinfo = getpriceinfo.executeQuery("SELECT avg(price) AS avgprice FROM " + table + 
									" WHERE itembrand = '" + rset.getString("name") + "'");
							while(priceinfo.next()){
								avgprice = priceinfo.getBigDecimal("avgprice");
								avgprice = avgprice.setScale(2, RoundingMode.CEILING);
							}
							
							//Get the Number of Search Results in the Past Week
							GenerateResults md = new GenerateResults();
							
							DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
							Calendar cal = Calendar.getInstance();
							cal.add(Calendar.DATE, -7);
							String lastdate = df.format(cal.getTime()).toString();
							int oneweekquantity = 0;
							
							Statement marketinfo = connection.createStatement();
						
							//Get the Marketability Value by determining number of listings within past week
							ResultSet marketvalue = marketinfo.executeQuery("SELECT COUNT(*) AS marketvalue FROM " + table + 
									" WHERE itembrand = '" + rset.getString("name") + "' AND listingdate > '" + lastdate + "'");
							while(marketvalue.next()){
								oneweekquantity += marketvalue.getInt("marketvalue");
							}
							
							marketability = md.calculateMarketability(oneweekquantity);
							
							
							//Get the Total Number of Prices Found in the Last Month to determine Profitability
							//Find the date that was 31 days back back
							Calendar calen = Calendar.getInstance();
							calen.add(Calendar.DATE, -31);
							String onemonthback = df.format(cal.getTime()).toString();
							List<BigDecimal> onemonthofprices = new ArrayList<BigDecimal>();
							
							Statement profitinfo = connection.createStatement();
							
							ResultSet profitvalue = profitinfo.executeQuery("SELECT price AS profitvalue FROM " + table + 
									" WHERE itembrand = '" + rset.getString("name") + "' AND listingdate > '" + onemonthback + "'");
							while(profitvalue.next()){
								onemonthofprices.add(profitvalue.getBigDecimal("profitvalue"));
							}
							
							profitability = md.calculateProfitability(onemonthofprices);
							StoreInfo unit = new StoreInfo();
							unit.insertCategorical(rset.getString("name"), totalentries, lastseen, avgprice, marketability, profitability);
						}
						
					}
					
					gatherexistingentries.close();
				}
				checkentries.close();	
			}
			statement.close();	
			System.out.println("The categorical table has been updated with the addition of new entries.");
		} catch (SQLException e){
			System.out.println("Error occured while updating values of table: " + cattable);
			e.printStackTrace();
		}
	}

	//StoreInfo.hardResetCatTable is used to catch version v1.2 users up to date with the v2.0 patch. The 2.0
	//version officially introduces the usage of metadata to give the user feedback with how the searches
	//are proceeding and how marketable as well as how profitable an item may be.  For v1.2 users, running
	//StoreInfo.hardResetCatTable once under the main function is all that will be required to be able to use
	//the next patch.  A patch check will likely be added into the future to prevent users from having
	//to apply this function manually.
	public static void hardResetCatTable(){
		try{
			statement = connection.createStatement();
			
			//The second statement is required since the first one closes prior to/during the execution of the second.
			Statement resetct = connection.createStatement();
			//
			
			rset = statement.executeQuery("SELECT name FROM " + cattable);
			while(rset.next()){
				resetct.executeUpdate("UPDATE " + cattable + " SET frequency = " + 0 + ", averageprice = " + 0 + ", marketability = " + 
						0 + ", profitability = " + 0 + " WHERE name = '" + rset.getString("name") + "'");
			}
			statement.close();
			resetct.close();
		} catch (SQLException e){
			System.out.println("Error occured while resetting values of table: " + cattable);
			e.printStackTrace();
		}
	}
	
	//Searches for underpriced items below a threshold value of 30% below the average price of individual brands.  
	//searchLowPricesIndividualBrands is primarily used when the number of listings in the PSQL table is sufficient
	//and the price variation across all number of brands is very high.
	public boolean isLowPriced(String brandname, BigDecimal itemprice){
		try{
			statement = connection.createStatement();
			rset = statement.executeQuery("SELECT frequency, averageprice FROM " + cattable + " WHERE name = '" 
					+ brandname + "'");
			while(rset.next()){
				int numberoflistings = rset.getInt("frequency");
				int greaterthan = rset.getBigDecimal("averageprice").compareTo(itemprice.multiply(new BigDecimal(0.3)));
				if(numberoflistings > 1){
					if(greaterthan != 1){
						return true;
					}
				}
			}	
			statement.close();
		} catch (SQLException e){
			System.out.println("Error while searching for low price offers across individual brands.");
			e.printStackTrace();
		}
		return false;
	}
	
	public boolean isVariableLowPriced(String brandname, BigDecimal itemprice, Double threshold){
		BigDecimal bdthresh = new BigDecimal(threshold);
		try{
			statement = connection.createStatement();
			rset = statement.executeQuery("SELECT frequency, averageprice FROM " + cattable + " WHERE name = '" 
					+ brandname + "'");
			while(rset.next()){
				int numberoflistings = rset.getInt("frequency");
				int greaterthan = rset.getBigDecimal("averageprice").compareTo(itemprice.multiply(bdthresh));
				if(numberoflistings > 1){
					if(greaterthan != 1){
						return true;
					}
				}
			}	
			statement.close();
		} catch (SQLException e){
			System.out.println("Error while searching for low price offers across individual brands.");
			e.printStackTrace();
		}
		return false;
	}	
	
	//Need to retrieve profitability and marketability from the cattable
	//ParseListings already has brand, itemid, listingdate, and price
	public static ItemListingInfo getNewItemInfo(String brandname){
		int marketabilityf = 0;
		int profitabilityf = 0;
		try{
			statement = connection.createStatement();
			connection.setAutoCommit(false);
			statement.setFetchSize(0);
			rset = statement.executeQuery("SELECT marketability, profitability FROM " + cattable 
					+ " WHERE name = '" + brandname + "'");
			while(rset.next()){
				marketabilityf = rset.getInt("marketability");
				profitabilityf = rset.getInt("profitability");
			}
		} catch (SQLException e){
			System.out.println("Error occured while checking for generalized item data.");
			e.printStackTrace();
		}
		
		ItemListingInfo iteminfo = new ItemListingInfo();
		iteminfo.profitfactor = profitabilityf;
		iteminfo.marketfactor = marketabilityf;
		return iteminfo;
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

}

