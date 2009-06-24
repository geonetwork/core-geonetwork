Author: Just van den Broecke
Email: just@justobjects.nl

This is the home of GeoNetwork service unit tests. Extended documentation should be forecoming.
Although they are called unit tests a better name would be blackbox/protocol/webservice tests since
we test the server protocol api similar to requests issued by an external client.

The core idea is that we fire an XML message (request) to a Jeeves server, receive a response
and calculate the test outcome by comparing the actual response with an expected response. So basically
we are dealing with XML as our test specification data.

WHAT
Each test dispatches one or more
requests to a (locally) running Jeeves instance/engine. Responses are matched with an expected responses.
In order to facilitate writing test scripts, requests and expected responses
are specified in XML within the package dir of the service. See for example
src/org/fao/geonet/services/harvesting

Furthermore:
- JUnit is used for test execution and XMLUnit for result evaluation
- variable content in result (like dates/id's etc) is catered for using symbolic vars
- symbolic vars can be reused e.g. a returned "id" with "add"-service can be used in "remove"-service
- symbolic variables can also be set e.g. setVariable("latNorth", "48");

WHY
Basically to have a quick way to create tests and shorten the edit/test/debug cycle-time.

- tests can be added quickly using XML files without programming
- a local instance of Jeeves is run (once), i.e. no J2EE engine like Jetty is required
- tests can be directly run within the IDE: this greatly facilitates debugging
- and offcourse Unit testing is a must in every project !!

WHERE
Best is to look at the tests under
src/org/fao/geonet/services

The test-framework can be found under src/org/fao/geonet/test
The local Jeeves engine can be found under jeeves/src/jeeves/server/local

HOWTO
0) Important when running tests is that have the top of the GeoNetwork
svn dir (e.g. trunk) as working directory by:
- a system property GN_HOME points to that directory! See e.g. build.xml run task (sysproperty).
- user.dir is assumed to be the working dir.

1) create a package with the same name as the service, e.g.  org/fao/geonet/services/category
(just for consistency)

2) within your package create a .java file and your xml-test files
for example the CGP server tests look like

public class HarvestTest extends ProtocolTestCase
{
    public static final String URL_FAO="www.fao.org";

    @Test
    public void testGNHarvester() throws Exception
    {
        setVariable("name", "gn.fao");
        setVariable("host", URL_FAO);
        doTest("gn-harvester-add.xml");
        doTest("gn-harvester-get-inactive.xml");
        doTest("harvester-start.xml");
        doTest("gn-harvester-get-active.xml");

   .
   .
}

example xml test file gn-harvester-add.xml:

<test>
    <request url="/eng/xml.harvesting.add">
        <params type="geonetwork">
            <site>
                <name>${name}</name>
                <host>${host}</host>
            </site>
        </params>
    </request>

    <response>
        <node type="geonetwork" id="${id}">
            <site>
                <name>${name}</name>
                <uuid>${uuid}</uuid>
                <account>
                    <use>false</use>
                    <username/>
                    <password/>
                </account>
                <host>${host}</host>
                <port>80</port>
                <servlet>${servlet}</servlet>
            </site>
                .
                 .
        </node>
    </response>
</test>

3) execute using one of 3 methods
- JUnit (e.g. in your IDE)
- running AllTests main() by adding your class to AllTests.java
- using Ant (currently only for Alltests.java)

In the first two cases don't forget to have the top dir as working dir !


