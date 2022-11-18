# apache-camel-read-grouped-file

This example shows how to read a group of files (grouped by a unique number) from a FTP server with Apache Camel. It uses 
Quarkus as infrastructure.

The FTP component (consumer) only reads one file during a poll execution and starts a new exchange in Camel based on that file. 
However, when you would like to read a group of files that have to be processed in one step this can only be achieved by 
using a custom GenericFileProcessStrategy (GroupFilesProcessingStrategy).

**Example:** group of files grouped by 4711 as key

- 4711.xml (trigger-file, can contain custom metadata)
- 4711_test1.pdf
- 4711_test2.pdf

The GenericFileProcessStrategy is used to get the names of all files belonging to the trigger file, based on the name of 
the trigger file. The file names will be stored in a header variable in the exchange from Camel. In the Camel route
(ReadGroupedFileRoute) the header variable will be used to iterate over the list of files and use a enrichPull component
in Camel to download each file.

## Build

```
./mvnw clean install
``` 

## Run in dev mode

```
./mvnw quarkus:dev
```

This project uses Quarkus, the Supersonic Subatomic Java Framework.
If you want to learn more about Quarkus, please visit its website: https://quarkus.io/ .

