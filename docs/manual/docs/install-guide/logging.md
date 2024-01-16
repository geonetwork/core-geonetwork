# Logging

This section describes how to use the geonetwork log files to find more details on incidents.

## Customising the log file location

Log files are written to the generic log location of the container. It is possible to change the location of the logfiles with an environment parameter ``-Dlog_dir=/var/tomcat/``. The path should end with **`/`** and the log files will be created in **`/var/tomcat/logs/geonetwork.log`**.

Details of some errors, such as xsl transformation errors, are not written to geonetwork.log. They are written to a file called **`catalina.out`**.

## Setting the Loglevel

GeoNetwork by default has 4 log levels: PROD, INDEX, SEARCH, DEV.

-   PROD is the default option, it will only log critical errors.
-   INDEX is similar to PROD, but with extended logging around the indexation process.
-   Search is similar to PROD, but with extended logging around the search process.
-   DEV is the most extended level, all debug messages will be logged.

You can set the log level from the Admin --> Settings page.

![](img/log-setting.png)

## Log4j

GeoNetwork uses [log4j](https://logging.apache.org/log4j) for logging. The log4j configuration file is located at **`/WEB-INF/classes/log4j.xml`**. The configuration file configures for each debug level at what severity messages will be logged.
