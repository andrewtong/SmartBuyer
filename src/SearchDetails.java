package at.smartBuyer.detailabstraction;

import java.io.File;

import java.math.BigDecimal;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;

import at.smartBuyer.sqlcommunication.StoreInfo;


public class SearchDetails {
	
	//sbfile refers to the file directory of where the categorical identifiers are all listed.  The table
	//spaces entries by new lines, and the file will only be scanned again if the contents have changed.  Categorical
	//identifiers are the means of identifying across listings.  In this default example, the brand of the item is 
	//used to categorize listings, and therefore the file would include all the brands the user is interested
	//in searching for.
	public static File sbfile = new File("C:\\Users\\Andrew\\Documents\\listings.txt");
	
	//This table contains the variables of interest.  For this particular project, it is focused on 
	//designer brands.  The program will attempt to match the brand to the listing title and find a match.
	public static HashSet<String> designer;
	
	//Currently the program only supports string matches where letters are in the correct order
	public String getRegisteredBrand(String s){
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
	
	//This method retrieves the checksum value associated with the file sbfile (see File sbfile at the top for info)
	//The crcvalue is used as a means of determining whether the file has been changed or not.  If so, the 
	//software will re-scan the file to check whether any new categorical identifiers need to be accounted for.
	//Note that removing categories from the file does not stop the software from searching the categories, as the
	//master list is located in an SQL table.  That being said, the categories can be altered manually with a tool
	//such as PgAdmin or using command line prompts.
	public int getcrcValue(File file){
		if(!file.exists()){
			System.out.println("No reference file exists or the directory is incorrect.");
			return -1;
		}
		try {
			HashCode crc32 = Files.hash(file, Hashing.crc32());
			int cs = crc32.asInt();
			return cs;

		} catch (IOException e) {
			e.printStackTrace();
		}
		return -1;
	}

	//checkFile will occur when the crc value differs from the previously noted crc value.  This will indicate
	//that the file contents have likely changed and a new scan will be ran through the file looking for unique
	//categorical values to be added to the database of words to search for.  
	public void checkFile(File file){
		StoreInfo si = new StoreInfo();
		if(!file.exists()){
			System.out.println("No reference file exists or the directory is incorrect.");
			return;
		}
        	try {
        		Scanner input = new Scanner(file);
        		
        		while (input.hasNextLine()) {
				String line = input.nextLine();
				//Because the text file may potentially contain anything, it is important that all ' marks are 
				//removed to ensure that the SQL database does not misinterpret the meaning.
				line = line.replace("'", "");
				if(!si.checkCategoricalDuplicate(line)){
					si.initiateCategorical(line, 0, new BigDecimal(0), 0, 0);
				}
        		}
			input.close();
			
        	} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
