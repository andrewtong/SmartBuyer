package detailAbstraction;

import java.util.HashSet;

import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException

public class SearchDetails {
	
	//File directory to store brand name (or any categorizing name) of the item to categorize listings by
	public static File sbfile = new File("Complete Directory Listing e.x. C:\\Users\\Documents\\..etc");
	
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
	
	public void checkFile(File file){
		if(!file.exists()){
			System.out.println("No reference file exists.");
			return;
		}
		try {
			HashCode crc32 = Files.hash(file, Hashing.crc32());
			int cs = crc32.asInt();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		//if(Math.abs(cs) != getCurrentHash()){
		//re-add listings into designer
		//}
		
		try {
            	Scanner input = new Scanner(file);
            	while (input.hasNextLine()) {
                	String line = input.nextLine();
                	designer.add(line);
            	}
            	input.close();
        	} catch (Exception ex) {
            	ex.printStackTrace();
        	}
	}

}
