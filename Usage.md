The following is a list of options for VoidStats when executing as a jar from the command line:

```
$ java -jar voidstats-0.1-dev.jar 
usage: voidstats
  -cp        run class partitions
  -d <arg>   description of dataset
  -doc       count docs (requires quads)
  -h         print this help message
  -i <arg>   n-triple input file  (sorted by s-p-o; context optional)
  -igz       if input is g-zipped
  -im        use in-memory storage, on-disk storage is used as default
  -o <arg>   n-triple output file
  -ogz       if output should be g-zipped
  -pp        run property partitions
  -s <arg>   pre-sort input file (if not sorted by s-p-o) and output to
            file name given.
  -u <arg>   URI pattern to include in analysis
```

VoidStats requires that the information is sorted by subject-predicate-object.  If it is not then the pre-sort option (-s) must be specified.

Dataset description (-d) is the name that will used within the outputted statistics.

VoidStats uses on-disk storage in the form of temporary files as default.  However, if the dataset is not too large then in-memory (-im) may be specified.  In-memory will be approx. ??? faster and typically can be used on datasets with up to ??? triples, however this will vary depending on how much memory is available on your system and how many class/property partitions need to be created.

The latest jar can be found [here](http://code.google.com/p/voidstats/downloads/list).  Below is an example of a command which analyses a dataset containing 116 million triples.  It took ??? hours in total, ??? hours to sort the data and ??? hours to run the statistics.

```
$ java -Xmx2500M -jar voidstats-0.1-dev.jar -i pubData_unSorted.nq.gz -s out/pubData_sorted.nq.gz -d pubData -o out/pubData_void.log -igz -pp -cp 2> out/pubData.err.log
```
