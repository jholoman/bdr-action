Introduction
===========
This program allows for programmatic execution of previously scheduled BDR jobs. It has three basic functions:
```
1) List BDR job schedule (and history). This is primarily in place to determine the schedule ID of the BDR job.
2) Run BDR job schedules. 
3) Generate an encrypted properties file that contains the CM instance details and password. 
It is of course not 100% secure, but better than storing in clear text. 
```
Additionally, the utility operates in either local or distributed mode, specified via system property. The default mode is distributed. In distributed mode, every entry point must contain the path as part of the arguments list. The path refers to the HDFS location of the properties file, "bdr.properties". TODO work includes implementation of writing the properties file directly to HDFS. Currently this must be done manually.

. 

---

Usage:
```
    java -jar -D[option]=[value] target/bdr-action.jar <CMD> <args>
```

Example:
```
    java -DlogLevel=debug -Dmode=local -jar target/bdr-action.jar List 
    java -DlogLevel=debug -Dmode=local -jar target/bdr-action.jar GenerateCredentials /path/to/credentials_file/
    
    hadoop jar bdr-action.jar Run 9 true /user/cloudera/
```

---

Options:
```
    logLevel               (default 'info')    The log level to use: debug, info, warn, error.
    mode                   (default 'distributed')    The mode local or distributed.
    filename               (default 'bdr.properties')    The filename to use.
    
```
Command Arguments:
```
***In local mode the path argument is optional for all commands, and defaults to $PWD***

   Run scheduleId dryRun /path/to/credentials_file/
   GenerateCredentials /path/to/credentials_file/
   List /path/to/credentials/
```
Example job.properties and workflow.xml are in the workflow directory. 

To build, install Maven and run the following, which creates a jar file with all dependencies included in it:
```
    mvn clean compile assembly:single
```

Depending on the version of CM you are running against, you might need to change the cloudera-manager-api 
version in the pom.xml file. Visit the following URL to see the available versions:
    - https://repository.cloudera.com/artifactory/cloudera-repos/com/cloudera/api/cloudera-manager-api/
You may also need to change the hadoop-client version.

Additional Note:
The encryption key is embedded in the AESEncryption class. This isn't a great practice and you should change this for your implementation.
