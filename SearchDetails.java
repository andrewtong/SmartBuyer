package detailAbstraction;

import java.util.HashSet;

import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SearchDetails {
	//This table contains the variables of interest.  For this particular project, it is focused on 
	//designer brands.  The program will attempt to match the brand to the listing title and find a match.
	private static final HashSet<String> designer = new HashSet<String>() {{
    	//Brands of Interest are included here
    	//ex. add("Brand Name");
	}};
	
	//Currently the program only supports string matches where letters are in the correct order
	public static final String getRegisteredBrand(String s){
		String line = s.replaceAll("[^A-Za-z]", ""); //Removes everything but letters
		Iterator<String> it = designer.iterator();
		while(it.hasNext()){
			String pattern = it.next();
			String primpattern = pattern.replaceAll("[^A-Za-z]", "");
			Pattern p = Pattern.compile(Pattern.quote(primpattern),Pattern.CASE_INSENSITIVE);
			Matcher m = p.matcher(line);
			if(m.find())
				return pattern;
		}
		return "none";
		
	}

}