.. _monitoring:

System Monitoring
=================

The monitoring system provides automated monitoring of a Geonetwork web application to be able track the health of the system over time.  The monitoring is based on the '''Metrics''' library (http://metrics.codahale.com/) by Yammer and detailed explanation for developers desiring specific monitors can be found there.

The metrics are available via JMX or as JSON with http GET requests.  The same information is available through both APIs.  The web requests provided are:

    - /monitor/metrics?[pretty=(true|false)][class=metric.name] - returns a json response with all of the registered metrics
    - /monitor/threads - returns a text representation of the stack dump at the moment of the call
    - /monitor/healthcheck - runs ALL health checks and returns 200 if all checks pass or 500 Internal Service Error if one fails (and human readable response of the failures)
    - /criticalhealthcheck - runs only the critical (fast) health checks and returns 200 if all checks pass or 500 Internal Service Error if one fails
    - /warninghealthcheck - runs only the non-critical health checks and returns 200 if all checks pass or 500 Internal Service Error if one fails
    - /expensivehealthcheck - runs only the expensive critical health checks and returns 200 if all checks pass or 500 Internal Service Error if one fails
    - /monitor - provide links to pages listed above.

Links to this data is also available in the geonetwork/srv/eng/config.info administration user interface as well.

By default the /monitor/* urls are protected and may only be accessed by an ''administrator'' or ''monitor'', however it is possible in the web.xml to provide a whitelist of URLs or IP addresses of monitoring servers that are permitted to access the monitoring data without needing an administration account.

The monitors available are:

    - Database Health Monitor - checks that the database is accessible
    - Index Health Monitor - checks that the Lucene index is searchable
    - Index Error Health Monitor - checks that there are no index errors in index (documents with _indexError field == 1)
    - CSW GetRecords Health Monitor - Checks that GetRecords? does not return an error for a basic hits search
    - CSW GetCapabilities Health Monitor - Checks that the GetCapabilities is returned and is not an error document
    - Database Access timer - Time taken to access a DBMS instance. This gives and idea of the level of contention over the database connections
    - Database Open Timer - Tracks the length of time a Database access is kept open
    - Database Connection Counter - Counts the number of open Database connections
    - Harvester Error Counter - Tracks errors that are raised during harvesting
    - Service timer - Track the time of service execution
    - Gui Services timer - Track the time of spend executing Gui services
    - XSL output timer - Track the time of output xsl transform
    - Log4j integration - monitors the frequency that logs are made for each log level so (for example) the rate that error are logged can be monitored. See  http://metrics.codahale.com/manual/log4j
    
The monitors that are enabled are in the config-monitoring.xml file and if desired certain monitors can be disabled.

In the source code repository there are configuration files for collectd (and perhaps other monitoring software in the future).