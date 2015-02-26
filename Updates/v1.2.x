Changelog

v1.2.0
In Progress
The concept of v1.2.0 is to introduce data driven metrics to predict the marketability of the items the user searches for. This 
is initiallized via first of all storing all categorical words (e.x. brands/models/year/etc) in a separate table as opposed to
having a static list.  The integration of this table will allow for more analytical methods to be applied, such as checking 
for the frequency of listings for a particular category as well as checks to see if an "lower price" flagged item
was sold.

With regards to categorical words, a separate file will be used and read off of for the software to know what listings to 
search for.  A simple crc is applied to the text file to check whether changes have been made to reduce search redundancies
on the file.  However, the issue that may arise (and actually already persists) from this is that there is no control over user
input duplicates. Some sort of duplicate removal feature can be applied to the table but it may be costly 
in terms of performance.

v1.1.1
2/22/2015
Reorginization of files to for a cleaner and more readable appearance.  Documentation has been thoroughly added to explain what
occurs at every stage for the reader and/or user to grasp a solid understanding of how the software behaves.

v1.1.0
2/20/2015
Minimizes search redundancies to prevent checks on item listings that have already been checked.  This is done via tracking the
ID of the first searched item and referencing it against future search checks (e.x. If the program has previously searched items
A through F, then if it does research A, it knows that subsequent searches have already been parsed). A second table on
PostgreSQL stores the information regarding the search ID the software is to lookout for. The same reference table is likely to
be used for other measures to improve performance in future versions.

