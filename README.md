# SmartBuyer v1.2.0
Searches EBay for underpriced listings by looking for pricing patterns among user-input found items.

The intent of this software is to search for underpriced listings using searches sorted by the newest listings avaliable. 
Using the EBay Finding API, it is possible to look for underpriced items provided that there is a baseline price to 
reference.  This program captures pricing information across items and is currently set to do price comparisons across brands
as its means of searching for items of interest.

The main file is located on ParseListings.java while the remainder of the files compliment the software.  Documentation has 
been added throughout the files to emphasize in a simple manner what the function is meant to do such that the process can be
easily understood from a high-level view.

This software operates on:

JDK 1.8

PostgreSQL v9.4 with JDBC4 drivers (see. https://jdbc.postgresql.org/download.html)

EBay Finding API via Java SDK - Finding Kit (see. https://go.developer.ebay.com/javasdk)

Guava 18.0 - https://code.google.com/p/guava-libraries/

Currently the direction of the program is to optimize the software performance by minimizing search redundancies and perform 
quality analytical work on the listings such that the user receives meaningful output to determine whether a listing is 
worth buying.  The dilemna is that there must be a compromise between the insightfulness of the analytics and 
the spectrum of the items this software can accurately cover.  When this software was designed, it had the intent of being 
used to search for low prices across clothes, since clothes can be easily distinguished by brand.  Attempting to apply this 
code to a different item, for example, books, would require a different identifying method, and a whole new set of methods 
to actually determine the value of a given book.  The code would then becomes exponentially more complex when handling
a broader spectrum of items.
