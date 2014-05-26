Introduction
===========
This program generates report files showing Cloudera configurations across all hosts and services, all on one page.
It includes default settings, as well as overrides. The report files are in HTML format, and they use some auxiliary
JavaScript and CSS files, which must be located in the same directory as the HTML files.
To view a report, just open it in a web browser.

This was thrown together fairly quickly based on a customer request, and some of the functionality is specific to
their needs. As such, some shortcuts were taken, some assumptions were made, and code quality was not the highest priority.
The program can/should be easily modified to make it more general purpose and/or make it more applicable to other needs. 

The program is hardcoded to use the first cluster under management of CM. It would need to be extended to add an argument
if there are multiple clusters being managed and you want to target one in particular. It could also be modified to run
the report on multiple clusters and output multiple report files or do in in-memory diff of the configurations.

The "example" directory contains the jar file (which contains all dependencies within it), the js and css files, and
the two report html files from running the program against a test cluster. You can see what the output looks like
by viewing the html files in a web browser. You can also just take the jar/js/css files and run the program against
your own cluster without having to build the project.

---

Usage:
```
    java -jar -D[option]=[value] target/cm-config-report.jar
```

Example:
```
    java -Dhost=hadoop0 -DlogLevel=debug -jar target/cm-config-report.jar
```

---

Options:
```
    hostReportFilename     (default 'host-report.html')    Name of file to save host report in.
    serviceReportFilename  (default 'service-report.html')    Name of file to save service report in.
    showDefaults           (default 'true')    Whether or not to show config default values.
    services               (default 'hdfs1,mapreduce1,hbase1,oozie1,zookeeper1')    Which CM services to include in the services report.
    reports                (default 'service,host')    Comma-separated list of reports to run: service and host are the valid reports.
    logLevel               (default 'info')    The log level to use: debug, info, warn, error.
    host                   (default 'localhost')    The host on which CM is running.
    user                   (default 'admin')    The user with which to connect to CM.
    password               (no default)    The password with which to connect to CM.
```

---

The following files must be in the same directory in which the report HTML files are viewed:
    - cm-config-report.css
    - jquery-2.0.3.min.js
    - jquery.dataTables.min.js

---

To build, install Maven and run the following, which creates a jar file with all dependencies included in it:
```
    mvn clean compile assembly:single
```

Depending on the version of CM you are running against, you might need to change the cloudera-manager-api 
version in the pom.xml file. Visit the following URL to see the available versions:
    - https://repository.cloudera.com/artifactory/cloudera-repos/com/cloudera/api/cloudera-manager-api/
