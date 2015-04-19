package at.smartBuyer.parselistings;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.List;
import com.ebay.services.finding.FindItemsAdvancedResponse;
import at.smartBuyer.detailabstraction.SearchDetails;
import at.smartBuyer.metadata.GenerateResults;
import at.smartBuyer.searchlistings.SearchListings;
import at.smartBuyer.sqlcommunication.StoreInfo;
import at.smartBuyer.sqlcommunication.StoreInfo.ReferenceValues;
import com.ebay.services.finding.SearchItem;

public class ParseListings {
	
	String results;
	
	public static class ItemListingInfo{
		public int marketfactor;
		public int profitfactor;
	}
	
	public static void parseSearchResults(List<SearchItem> items){	
		
		SearchDetails sd = new SearchDetails();
		StoreInfo si = new StoreInfo();
		
		//Variables used to manage information transferred between SQL database and the Java program
		long firstid = 0;
		int loopcount = 0;
		int itemsfound = 0;
		SimpleDateFormat dateformat = new SimpleDateFormat("MM-dd-yyyy");
		
		ReferenceValues lastinfo = new ReferenceValues();
		
		//Prior to running through the results, we initially check for the previously searched ID,
		//as well as the previously known crc value.
		lastinfo = si.checkSearched();
		long lastid = lastinfo.itemid;
		long crcvalue = lastinfo.checksumvalue;
		
		//The crc values are compared and checked to see whether the user file needs to be scanned.
		if(sd.getcrcValue(SearchDetails.sbfile) != crcvalue){
			//Re-scan the user file and add differences into the SQL categorical table
			//This will insert all new entries from the file into the SQL table
			sd.checkFile(SearchDetails.sbfile);
			
			//The last found crc value will also have to be recorded such that it can be used as a record
			//for future runs.
			crcvalue = sd.getcrcValue(SearchDetails.sbfile);
		}
	    
		//Regardless of whether the SQL table is updated, the HashSet that holds the keywords (in this particular 
		//case, brands) needs to be updated from the current table, since it is faster to do a comparison between
		//sets of data within the JVM as opposed to having to constantly compare with a external table.
		SearchDetails.designer = si.retrieveCategorical();
		
		for(SearchItem item : items) {
			//Currently does not support shipping costs due to NPE from the Ebay API.
			
			//Reads brand name of the listing from the listing title
			String brandname = sd.getRegisteredBrand(item.getTitle());
			
			//Retrieves the unique item ID associated with the listing item
			long itemid = Long.parseLong(item.getItemId());
			
			//Retrieves the category the listing item is associated with, is typically similar to 
			//the category assigned in the aspect filters (if applicable)
			String categorytype = item.getPrimaryCategory().getCategoryName();
			
			//Retrieves the listing date that the item was posted on
			String listingdate = dateformat.format(item.getListingInfo().getStartTime().getTime());
			loopcount += 1;
			
			if(loopcount == 1){
				firstid = itemid;
			}

    			//If the item is a brand of interest and has not been registered before, it is then stored into
    			//the SQL database.  (see at.smartBuyer.sqlcommunication.StoreInfo for functions)
    			if(brandname != "none" && !si.checkDuplicate(itemid)){
    				BigDecimal itemprice;
            			if(item.getListingInfo().isBuyItNowAvailable()){
            				itemprice = new BigDecimal(item.getListingInfo().getBuyItNowPrice().getValue());
            			}
            			else{
            				itemprice = new BigDecimal(item.getSellingStatus().getCurrentPrice().getValue());
            			}
            			si.insertData(itemid, brandname, categorytype, itemprice, listingdate);
            			itemsfound++;
        			if(si.isLowPriced(brandname, itemprice)){
        				ItemListingInfo currentitem = new ItemListingInfo();
        				GenerateResults resultstoring = new GenerateResults();
        				resultstoring.addResult(itemid, brandname, itemprice, currentitem.marketfactor, currentitem.profitfactor);
        			}    			
    			}
			
    			//The program then proceeds to check whether the item has been scanned before.  The software stops once 
    			//it realizes it has scanned an item it has seen before, or if the list of search entries
    			//has been processed through.
    			//Case where the algorithm has scanned through all of the new search entries.
    			if(loopcount == SearchListings.searchentries){
    				si.updateLastFound(firstid, crcvalue);
    				System.out.println("Scanned through " + loopcount + " new items.");
    				System.out.println("Added " + itemsfound + " new items to the database.");
    				//This conditional can only occur once per run, so there is no repetition of updating cattable.
    				if(loopcount > 0){
    					si.updateCatTable();
    				}
    			} 
    		
    			//Case where the algorithm searches an item that was of the last search item of the previous run.
    			else if(lastid == itemid){
    				si.updateLastFound(firstid, crcvalue);
    				System.out.println("Scanned through " + (loopcount-1) + " new items.");
    				System.out.println("Added " + itemsfound + " new items to the database.");	
    				if(loopcount-1 > 0){
    					si.updateCatTable();
    				}
    				break;
    			}
		}
	}
	
	public void getResultLog(){
		GenerateResults extractlog = new GenerateResults();
		System.out.println("Search Complete: ");
		System.out.println(extractlog.retrieveResultLog());
	}
	
	public static void main(String[] args) {
		//A result object is created to handle the results output from the EBay API.  
		FindItemsAdvancedResponse result = new FindItemsAdvancedResponse();
		result = SearchListings.searchListings();
		
		StoreInfo search = new StoreInfo();
		
		//A connection to the SQL database then must be made in order to process and registered the incoming data.
		search.establishConnection();
		
		//The listing table stores pricing information across brands of interest.
        	search.lookupListingsTable();
        
        	//The comparison table is used as a lookup to check up to what point can the software stop searching.
        	//For example, if the search has processed entries of id 1100 to 1000 (because it searches by newest), then
        	//searching anything older than or equal to id 1100 is redundant.
        	search.lookupComparisonTable();
		
        	search.lookupCategoricalTable(); 
        
		//The result object contains a list of items pertaining to what the user specified, and can be processed
		//through similarly how a normal list operates.
        	List<SearchItem> items = result.getSearchResult().getItem();
        
        	ParseListings pl = new ParseListings();
        	pl.parseSearchResults(items);
 		pl.getResultLog();

	}
}
