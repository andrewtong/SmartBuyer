package at.smartBuyer.searchlistings;

import java.util.List;

import com.ebay.services.client.ClientConfig;
import com.ebay.services.client.FindingServiceClientFactory;
import com.ebay.services.finding.AspectFilter;
import com.ebay.services.finding.FindItemsAdvancedRequest;
import com.ebay.services.finding.FindItemsAdvancedResponse;
import com.ebay.services.finding.FindingServicePortType;
import com.ebay.services.finding.ItemFilter;
import com.ebay.services.finding.ItemFilterType;
import com.ebay.services.finding.PaginationInput;
import com.ebay.services.finding.SortOrderType;

public class SearchListings {
	
	public static final int searchentries = 100;
	public static final String appid = "User App ID";
	
	public static FindItemsAdvancedResponse searchListings(){
		try {
        	// Setup Endpoint Configuration
        	ClientConfig config = new ClientConfig();
        	
        	//Setting this value as false suppresses the console SOAP messages and the XML input/output associated
        	//with the user search.
        	config.setHttpHeaderLoggingEnabled(false);
        	config.setSoapMessageLoggingEnabled(false);
        	//An application id is obtained by signing up for the EBay developer's program
        	config.setApplicationId(appid);
        	//

        	//Creates a Service Client
        	//A service client accepts a user request and returns a result based off user input
		FindingServicePortType serviceClient = FindingServiceClientFactory.getServiceClient(config);
		//
		
		//Request Object
		//A number of parameters can be added to the request object to specify the user desired search listings
		FindItemsAdvancedRequest request = new FindItemsAdvancedRequest();
		
		//Request Parameters
		
		//Keywords - product keywords may be added using //request.setKeywords("Product Keywords");
		
		//Category - a call to the ebay servers supports up to three unique categories
		//Categories divided by integers, but are registered in the form of Strings
		//reference category ID via http://www.isoldwhat.com/getcats/fullcategorytree.php?RootID=11232#11232
		request.getCategoryId().add("57988"); //Men's Clothing, Coats and Jackets
		request.getCategoryId().add("11484"); //Men's Clothing, Sweaters
		
		//Detail Selection Using Aspect Filter
		//Ebay's Aspect Filter describes the characteristics of an item that vary from category to category.
		//For example, pants may be filtered by leg length and waist size, so it may have an aspect filter of
		//leg length and waist size.  On the other hand, a book may be filtered by language or subject.  Therefore, 
		//aspect filters the language of the book, as well as the subject of the book (e.x. action/mystery)
		//see http://expertland.net/question/s91qq5u5n2m61hx62asb7m37tmsz8o3/detail.html
            
		//A general aspect filter class must be created, which is then added to the request object.
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
            
		//Item Condition
		//EBay divides item condition typically via increments of 500 or 1000, with 1000 indicating that the 
		//object is in the newest form, while subsequent numbers indicate incremental usage of the item.
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
		//Duplicate Removal is not perfect, will need to manually sort out most duplicates.
            
		//Sort Items by Newest First
		request.setSortOrder(SortOrderType.START_TIME_NEWEST);
		//
            
		//Number of Returns per Search - the maximum limit of searches that can be returned is 100
		PaginationInput searches = new PaginationInput();
		searches.setEntriesPerPage(searchentries);
		//Extra:  //see https://forums.developer.ebay.com/questions/1208/how-to-limit-any-ebay-api-call-using-date-filter.html for item filters
		//
            
		//Output Object
		FindItemsAdvancedResponse result = serviceClient.findItemsAdvanced(request);
		System.out.println("Found " + result.getSearchResult().getCount() + " relevant items." );
		return result;
		
        	} catch (Exception ex) {
        	// Handles Exceptions
		ex.printStackTrace();
        	}
		System.out.println("Something went horribly wrong!");
		return null;
	}

}
