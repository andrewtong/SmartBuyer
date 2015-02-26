Changelog

v1.2.0
2/26/15
The concept of v1.2.0 is to introduce data driven metrics to predict the marketability of the items the user searches for. This 
is initiallized via first of all storing all categorical words (e.x. brands/models/year/etc) in a separate table as opposed to
having a static list.  The integration of this table will allow for more analytical methods to be applied, such as checking 
for the frequency of listings for a particular category, number of total searches for a given category, as well as the average 
listing price for said category.  The complete table is to be updated prior to v1.3.0, and will be used to return metadata to 
the user to improve search queries.  For my information and to see how this table works, see 'cattable' in StoreInfo.java
as well its associated class ReferenceValues (also in StoreInfo.java).

Categorical identifiers are now stored 'cattable' as opposed to being saved to a HashSet on the java code.  The categorical
table references an external text file to determine whether or not the number of categories needs to be updated.  A crc is
applied to the file during runtime to ensure that the file is only scanned if the contents have been changed.  This is to reduce
search redundancies onto the file since reading from a large file can reduce performance.  Because the file contents must
be stored onto an SQL table, a simple filter is applied to the file contents to ensure that the user file input does not
result in an SQL misinterpretations.

The last-found crc value is stored along with last-found item id in the SQL table 'reftable'.  This purpose of this table is 
to ultimately reduce search redundancies.  The operations of this table have been slightly cleaned up as the table no longer
resets its own values, but simply updates the last-found values to nearing the end of runtime, which is a lot easier for 
external users to understand as well as is better coding practice in general.

A number of static methods have now been altered to non-static.  This is primarily evident within class methods in order to 
follow more of an object-oriented paradigm.  That being said, the main class (see ParseListings.java) still uses static 
methods as the resulting code appears very clean and it is very easy to understand a macro/external view of what is occuring.
However, this may be altered later on.
