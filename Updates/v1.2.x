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
