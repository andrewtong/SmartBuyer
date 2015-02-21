package smartBuyer;

import java.util.List;


import java.math.BigDecimal;
import java.text.SimpleDateFormat;

import com.ebay.services.client.ClientConfig;
import com.ebay.services.client.FindingServiceClientFactory;
import com.ebay.services.finding.AspectFilter;
import com.ebay.services.finding.FindItemsAdvancedRequest;
import com.ebay.services.finding.FindItemsAdvancedResponse;
import com.ebay.services.finding.FindingServicePortType;
import com.ebay.services.finding.ItemFilter;
import com.ebay.services.finding.ItemFilterType;
import com.ebay.services.finding.PaginationInput;
import com.ebay.services.finding.SearchItem;
import com.ebay.services.finding.SortOrderType;

import detailAbstraction.SearchDetails;
import sqlCommunication.StoreInfo;

public class SearchListings {

    public static void main(String[] args) {
    	
        try {
        	// Setup endpoint configuration
        	ClientConfig config = new ClientConfig();
        	config.setHttpHeaderLoggingEnabled(false);//removes initial collection log
        	config.setSoapMessageLoggingEnabled(false); //removes Soap Message log for all items
        	config.setApplicationId("User Application ID");

        	//Creates the service client
            FindingServicePortType serviceClient = FindingServiceClientFactory.getServiceClient(config);
            
            //Request Object
            FindItemsAdvancedRequest request = new FindItemsAdvancedRequest();
            //
            
            StoreInfo.establishConnection();
            StoreInfo.lookupListingsTable();
            StoreInfo.lookupComparisonTable();
            
            
            //Request Parameters
            //request.setKeywords("Product Keywords");
            //
            
            //Select Category of Item, supports up to 3 categories per input call
            //int of the form "int" to denote category ID
            request.getCategoryId().add("57988"); //Men's Clothing, Coats and Jackets
            request.getCategoryId().add("11484"); //Men's Clothing, Sweaters
            //reference category ID via http://www.isoldwhat.com/getcats/fullcategorytree.php?RootID=11232#11232
            // see https://forums.developer.ebay.com/questions/1208/how-to-limit-any-ebay-api-call-using-date-filter.html for item filters
            
            
            //Detail Selection Using Aspect Filter
            //see http://expertland.net/question/s91qq5u5n2m61hx62asb7m37tmsz8o3/detail.html
            AspectFilter details = new AspectFilter();
            
            //Item Style
            details.setAspectName("Style");
            details.getAspectValueName().add("Basic Jacket");
            details.getAspectValueName().add("Basic Coat");
            
            details.getAspectValueName().add("Cardigan");
            details.getAspectValueName().add("Crewneck");
            //
            
            //Item Size
            details.setAspectName("Size");
            details.getAspectValueName().add("M");
            details.getAspectValueName().add("L");
            
            request.getAspectFilter().add(details);
            //
            
            //Condition: new
            ItemFilter itemconditionfilter = new ItemFilter();
            itemconditionfilter.setName(ItemFilterType.CONDITION);
            List<String> itemcondition = itemconditionfilter.getValue();
            itemcondition.add("1000");
            request.getItemFilter().add(itemconditionfilter);
            //
            
            //Buy It Now: Available, Fixed Price: Available
            ItemFilter itempricingfilter = new ItemFilter();
            itempricingfilter.setName(ItemFilterType.LISTING_TYPE);
            List<String> itempricing = itempricingfilter.getValue();
            itempricing.add("AuctionWithBIN");
            itempricing.add("FixedPrice");
            request.getItemFilter().add(itempricingfilter);
            
            //Remove Duplicate Item Listings from Appearing
            ItemFilter duplicatefilter = new ItemFilter();
            duplicatefilter.setName(ItemFilterType.HIDE_DUPLICATE_ITEMS);
            duplicatefilter.getValue().add("true");
            request.getItemFilter().add(duplicatefilter);
            //Duplicate Removal is not perfect, need to manually sort out most duplicates.
            
            //Sort Items by Newest First
            request.setSortOrder(SortOrderType.START_TIME_NEWEST);
            //
            
            int searchentries = 30;
            //Number of Returns per Search - setEntriesPerPage(int)
            PaginationInput searches = new PaginationInput();
            searches.setEntriesPerPage(searchentries); //Number of items found per search, max 100
            request.setPaginationInput(searches);
            //
            
            //Output Object
            FindItemsAdvancedResponse result = serviceClient.findItemsAdvanced(request);
            System.out.println("Found " + result.getSearchResult().getCount() + " relevant items." );
            //
            
            
            long firstid = 0;
			int loopcount = 0;
            long lastid = StoreInfo.checkSearched();
            System.out.println(lastid);
			StoreInfo.resetLastFound();
			
            SimpleDateFormat dateformat = new SimpleDateFormat("MM-dd-yyyy");
            List<SearchItem> items = result.getSearchResult().getItem();
            
            for(SearchItem item : items) {
            	//Currently does not support shipping costs due to NPE exceptions
        		String brandname = SearchDetails.getRegisteredBrand(item.getTitle());
        		long itemid = Long.parseLong(item.getItemId());
        		String categorytype = item.getPrimaryCategory().getCategoryName();
        		String listingdate = dateformat.format(item.getListingInfo().getStartTime().getTime());
        		loopcount += 1;
        		
        		if(loopcount == 1){
        			firstid = itemid;
        		}
        		
        		if(brandname != "none" && !StoreInfo.checkDuplicate(itemid)){
                	if(item.getListingInfo().isBuyItNowAvailable()){
                		StoreInfo.insertData(itemid, brandname, categorytype, new BigDecimal(item.getListingInfo().getBuyItNowPrice().getValue()), listingdate);
                	}
	            	else{
	            		StoreInfo.insertData(itemid, brandname, categorytype, new BigDecimal(item.getSellingStatus().getCurrentPrice().getValue()), listingdate);
	            	}
        		}
        		
        		if(loopcount == searchentries){
        			StoreInfo.insertReference(firstid);
        			System.out.println("Scanned through " + loopcount + " new items.");
        		} 
        		else if(lastid == itemid){
        			StoreInfo.insertReference(firstid);
        			System.out.println("Scanned through " + loopcount + " new items.");
        			break;
        		}
	
            }
            
        } catch (Exception ex) {
        	// Handles Exceptions
            ex.printStackTrace();
        }
    }
}