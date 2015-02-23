# SmartBuyer v1.1.0
Searches EBay for underpriced listings by looking for pricing patterns among user-input found items.

The intent of this software is to search for underpriced listings using searches sorted by the newest listings avaliable. Using
EBay Finding API, it is possible to look for underpriced items provided that there is a baseline price to reference.  This
program captures pricing information across items and is currently set to do price comparisons across brands as its means
of searching for items of interest.

This software operates on:

JDK 1.8

PostgreSQL v9.4 with JDBC4 drivers (see. https://jdbc.postgresql.org/download.html)

EBay Finding API via Java SDK - Finding Kit (see. https://go.developer.ebay.com/javasdk)

