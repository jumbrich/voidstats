# Welcome to VoidStats #

The Vocabulary of Interlinked Datasets (VoID) is concerned with metadata about linked data. It is an RDF Schema vocabulary that provides terms and patterns for describing RDF datasets, and is intended as a bridge between the publishers and users of RDF data.

VoidStats produces a summary of an N-triple dataset based on VoID descriptions.

For example it will provide the following VoID statistics:
  * # triples/quads
  * # distinct subjects
  * # distinct objects
  * # entities
  * # classes
  * # properties
  * # vocabularies

It will also partition the dataset based on it's own classes and properties and produce VoID statistics for each partition.


VoidStats uses the [NxParser](http://code.google.com/p/nxparser) to parse the dataset.