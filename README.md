## B-tree algorithm ##
A simple implementation based on the [SQLite 2.8.1](https://www.sqlite.org/src/info/590f963b6599e4e2)'s BTree implementation in C.

Supports **searching**, **inserting** and **deleting** entries in O(log n) time where each entry is composite of an integer **key** and string **data**. 
