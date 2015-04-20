# SmartBuyer v2.0

**About**

SmartBuyer searches EBay listings for underpriced items by looking for pricing patterns among user-input found items.  The
items searched are with accordance to what the user wants to find.  The search can be altered by refining the search 
variables located in SearchListings.java.

The intent of this software is to search for underpriced listings using searches sorted by the newest listings avaliable. 
Using the EBay Finding API, it is possible to look for underpriced items provided that there is a baseline price to 
reference.  This program captures pricing information across items and is currently set to do price comparisons across brands
as its means of searching for items of interest.  

This software utilizes several libraries to perform its computations.  It is recommend to use the listed or later versions
when using SmartBuyer.

+ JDK 1.8
+ PostgreSQL v9.4 with JDBC4 drivers (see. https://jdbc.postgresql.org/download.html)
+ EBay Finding API via Java SDK - Finding Kit (see. https://go.developer.ebay.com/javasdk)
+ Guava 18.0 - https://code.google.com/p/guava-libraries/
+ Apache Commons Math 3.5 - http://commons.apache.org/proper/commons-math/download_math.cgi

**How Does SmartBuyer Work?**

The initial step of SmartBuyer is to determine what you would like the algorithm to search for.  For example, if I have
a list of brands, then I would write them to a file, and a checksum feature is used to determine whether the file has
been updated or not.  If so, the database tables will be updated prior to the search such that the algorithm will know 
what items to search for.  The absolute file directory can be found in SearchDetail.java.

The software then uses the EBay API to find the newest 100 items.  The API has a maximum of 100 listings along with their 
respective listing details that can be returned for a given search.  SmartBuyer also stores the last-searched item id from 
the previous run to prevent duplicate searches.  Because of this, the results from using SmartBuyer is highly contingent 
on how frequent searches are performed.

For each scanned listing, if the algorithm detects that the listing is an item of interest *and* has not been previously 
registered, it stores the listing along with its associated pricing information in a database, which in this project 
is referred to as the listings table. A second check is performed against the existing pricing information for the type of
information, which is stored in the categorical table.  If the scanned item meets a particular pricing threshold, it will
be flagged as a low price item and the user is notified once the search concludes.  Below is an example of what the listing
table stores.  By default, the searches are configured towards designer coats and jackets.

![listingtable](https://cloud.githubusercontent.com/assets/10404525/7218674/40fa2548-e630-11e4-9cfb-447d92ed75eb.PNG)

Once all unique new listings have been parsed, the categorical table is updated if new listings have been added to 
listings table.  The categorical table provides a broader overview of detail for a particular type of item.  Data gathered
from a spectrum of listings can be further used to predict how  profitable an item is in terms of reselling, as well as 
how easy it would be to sell said item.  The following is an example of the data stored within the categorical table.

![cattable](https://cloud.githubusercontent.com/assets/10404525/7220986/1674eb30-e691-11e4-8642-0728f09b53a5.PNG)

**Results**

For every scan, SmartBuyer prints out a log indicating the results of the run.  Currently the result logs are printed through
the console.  A sample output log is shown below.  In the case that the algorithm detects a low priced item, the item details
will be printed out onto the log.

![sampleoutput](https://cloud.githubusercontent.com/assets/10404525/7218673/3eeefde6-e630-11e4-991f-fba3685bfe00.PNG)
