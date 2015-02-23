<<<<<<< HEAD
package at.smartBuyer.parselistings;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.List;
import com.ebay.services.finding.FindItemsAdvancedResponse;
import at.smartBuyer.detailabstraction.SearchDetails;
import at.smartBuyer.searchlistings.SearchListings;
import at.smartBuyer.sqlcommunication.StoreInfo;
import com.ebay.services.finding.SearchItem;

public class ParseListings {
	
	public static void parseSearchResults(List<SearchItem> items){	
		
		//Variables used to manage information transferred between SQL database and the Java program
        long firstid = 0;
		int loopcount = 0;
		SimpleDateFormat dateformat = new SimpleDateFormat("MM-dd-yyyy");
	    long lastid = StoreInfo.checkSearched();
	    
	    //Here we need to reset the last found ID because a new search is occuring.
	    StoreInfo.resetLastFound();
		
		for(SearchItem item : items) {
        	//Currently does not support shipping costs due to NPE exceptions
			
			//Reads brand name of the listing from the listing title
    		String brandname = SearchDetails.getRegisteredBrand(item.getTitle());
    		
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
    		if(brandname != "none" && !StoreInfo.checkDuplicate(itemid)){
            	if(item.getListingInfo().isBuyItNowAvailable()){
            		StoreInfo.insertData(itemid, brandname, categorytype, new BigDecimal(item.getListingInfo().getBuyItNowPrice().getValue()), listingdate);
            	}
            	else{
            		StoreInfo.insertData(itemid, brandname, categorytype, new BigDecimal(item.getSellingStatus().getCurrentPrice().getValue()), listingdate);
            	}
    		}
    		
    		//The program then proceeds to check whether the item has been scanned before.  The software stops once 
    		//it realizes it has scanned an item it has seen before, or if the list of search entries
    		//has been processed through.
    		if(loopcount == SearchListings.searchentries){
    			StoreInfo.insertReference(firstid);
    			System.out.println("Scanned through " + loopcount + " new items.");
    		} 
    		else if(lastid == itemid){
    			StoreInfo.insertReference(firstid);
    			System.out.println("Scanned through " + (loopcount-1) + " new items.");
    			break;
    		}

        }
	}
	
	public static void main(String[] args) {
		//A result object is created to handle the results output from the EBay API.  
		FindItemsAdvancedResponse result = new FindItemsAdvancedResponse();
		result = SearchListings.searchListings();
		
		//A connection to the SQL database then must be made in order to process and registered the incoming data.
		StoreInfo.establishConnection();
		
		//The listing table stores pricing information across brands of interest.
        StoreInfo.lookupListingsTable();
        
        //The comparison table is used as a lookup to check up to what point can the software stop searching.
        //For example, if the search has processed entries of id 1100 to 1000 (because it searches by newest), then
        //searching anything older than or equal to id 1100 is redundant.
        StoreInfo.lookupComparisonTable();
		
		//The result object contains a list of items pertaining to what the user specified, and can be processed
		//through similarly how a normal list operates.
        List<SearchItem> items = result.getSearchResult().getItem();
        parseSearchResults(items);

	}

}
=======

>>>>>>> 1573382233e8d395b7b5b201ffbe7e0fe0ee645c
