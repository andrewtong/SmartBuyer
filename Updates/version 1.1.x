Version 1.1.1
2/22/2015
Reorginization of files to for a cleaner and more readable appearance.  Documentation has been thoroughly added to explain what
occurs at every stage for the reader and/or user to grasp a solid understanding of how the software behaves.

Version 1.1.0
2/20/2015
Minimizes search redundancies to prevent checks on item listings that have already been checked.  This is done via tracking the
ID of the first searched item and referencing it against future search checks (e.x. If the program has previously searched items
A through F, then if it does research A, it knows that subsequent searches have already been parsed). A second table on
PostgreSQL stores the information regarding the search ID the software is to lookout for. The same reference table is likely to
be used for other measures to improve performance in future versions.
