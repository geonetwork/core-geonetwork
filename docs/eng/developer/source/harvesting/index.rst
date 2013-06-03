.. _harvesting:

Harvesting
==========

Structure
---------

The harvesting capability is built around 3 areas: JavaScript code, Java code and
XSL stylesheets (on both the server and client side).

JavaScript code
```````````````

This refers to the web interface. The code is located in the
web/geonetwork/scripts/harvesting folder. Here, there is a subfolder for each
harvesting type plus some classes for the main page. These are:

#.  *harvester.js*: This is an abstract class that must be implemented by
    harvesting types. It defines some information retrieval methods
    (getType, getLabel, etc...) used to handle the harvesting type, plus one
    getUpdateRequest method used to build the XML request to insert or
    update entries.

#.  *harvester-model.js*: Another abstract class that must be implemented
    by harvesting types. When creating the XML request, the only method
    substituteCommon takes care of adding common information like privileges
    and categories taken from the user interface.

#.  *harvester-view.js*: This is an important abstract class that must be
    implemented by harvesting types. It takes care of many common aspects of
    the user interface. It provides methods to add group’s privileges, to
    select categories, to check data for validity and to set and get common
    data from the user interface.

#.  *harvesting.js*: This is the main JavaScript file that takes care of
    everything. It starts all the submodules, loads XML strings from the
    server and displays the main page that lists all harvesting nodes.

#.  *model.js*: Performs all XML requests to the server, handles errors and
    decode responses.

#.  *view.js*: Handles all updates and changes on the main page.

#.  *util.js*: just a couple of utility methods.

Java code
`````````

The harvesting package is located in ``web/src/main/java/org/fao/geonet/kernel/harvest``. Here
too, there is one subfolder for each harvesting type. The most important classes
for the implementor are:

#.  *AbstractHarvester*: This is the main class that a new harvesting type
    must extends. It takes care of all aspects like adding, updating,
    removing, starting, stopping of harvesting nodes. Some abstract methods
    must be implemented to properly tune the behaviour of a particular
    harvesting type.

#.  *AbstractParams*: All harvesting parameters must be enclosed in a class
    that extends this abstract one. Doing so, all common parameters can be
    transparently handled by this abstract class.

All others are small utility classes used by harvesting types.

XSL stylesheets
```````````````

Stylesheets are spread in some folders and are used by both the JavaScript code
and the server. The main folder is located at ``web/src/webapp/xsl/harvesting``.
Here there are some general stylesheets, plus one subfolder for each harvesting
type. The general stylesheets are:

#.  *buttons.xsl*: Defines all button present in the main page
    (*activate*, *deactivate*,
    *run*, *remove*,
    *back*, *add*,
    *refresh*), buttons present in the "add new
    harvesting" page (*back* and
    *add*) and at the bottom of the edit page
    (*back* and *save*).

#.  *client-error-tip.xsl*: This stylesheet is used by the browser to build
    tooltips when an harvesting error occurred. It will show the error class,
    the message and the stacktrace.

#.  *client-node-row.xsl*: This is also used by the browser to add one row
    to the list of harvesting nodes in the main page.

#.  *harvesting.xsl*: This is the main stylesheet. It generates the HTML
    page of the main page and includes all panels from all the harvesting
    nodes.

In each subfolder, there are usually 4 files:

#.  *xxx.xsl*: This is the server stylesheets who builds all panels for
    editing the parameters. XXX is the harvesting type. Usually, it has the
    following panels: site information, search criteria, options, privileges
    and categories.

#.  *client-privil-row.xsl*: This is used by the JavaScript code to add
    rows in the group’s privileges panel.

#.  *client-result-tip.xsl*: This is used by the JavaScript code (which
    inherits from harvester-view.js) to show the tool tip when the harvesting
    has been successful.

#.  *client-search-row.xsl*: Used in some harvesting types to generate the
    HTML for the search criteria panel.

As you may have guessed, all client side stylesheets (those used by JavaScript
code) start with the prefix client-.

Another set of stylesheets are located in ``web/src/webapp/xsl/xml/harvesting``
and are used by the xml.harvesting.get service. This service is used by the
JavaScript code to retrieve all the nodes the system is currently harvesting
from. This implies that a stylesheet (one for each harvesting type) must be
provided to convert from the internal setting structure to an XML structure
suitable to clients.

The last file to take into consideration contains all localised strings and is
located at ``web/src/webapp/loc/XX/xml/harvesting.xml`` (where XX refers to a
language code). This file is used by both JavaScript code and the server.

Data storage
------------

Harvesting nodes are stored inside the Settings table. Further useful information
can be found in the chapter Harvesting.

The SourceNames table is used to keep track of the uuid/name couple when metadata
get migrated to different sites.

Guidelines
----------

To add a new harvesting type, follow these steps:

#.  Add the proper folder in ``web/src/webapp/scripts/harvesting``, maybe copying an already
    existing one.

#.  Edit the harvesting.js file to include the new type (edit both constructor
    and init methods).

#.  Add the proper folder in ``web/src/webapp/xsl/harvesting`` (again, it is easy to copy
    from an already existing one).

#.  Edit the stylesheet ``web/src/webapp/xsl/harvesting/harvesting.xsl`` and add the new type

#.  Add the transformation stylesheet in ``web/src/webapp/xsl/xml/harvesting``. Its name must
    match the string used for the harvesting type.

#.  Add the Java code in a package inside ``org.fao.geonet.kernel.harvest.harvester``.

#.  Add proper strings in ``web/src/webapp/loc/XX/xml/harvesting.xml``.

Here is a list of steps to follow when adding a new harvesting type:

#.  Every harvesting node (not type) must generate its UUID. This UUID is used
    to remove metadata when the harvesting node is removed and to check if a
    metadata (which has another UUID) has been already harvested by another
    node.

#.  If a harvesting type supports multiple searches on a remote site, these
    must be done sequentially and results merged.

#.  Every harvesting type must save in the folder images/logos a GIF image
    whose name is the node’s UUID. This image must be deleted when the
    harvesting node is removed. This is necessary to propagate harvesting
    information to other GeoNetwork nodes.

#.  When a harvesting node is removed, all collected metadata must be removed
    too.

#.  During harvesting, take in mind that a metadata could have been removed
    just after being added to the result list. In this case the metadata should
    be skipped and no exception raised.

#.  The only settable privileges are: view, dynamic, featured. It does not
    make sense to use the others.

#.  If a node raises an exception during harvesting, that node will be
    deactivated.

#.  If a metadata already exists (its UUID exists) but belong to another node,
    it must not be updated even if it has been changed. This way the harvesting
    will not conflict with the other one. As a side effect, this prevent locally
    created metadata from being changed.

#.  The harvesting engine stores results in the database as part of the harvest
    history.

#.  When harvesting parameters are changed, the new harvesting type must
    use them during the next harvesting without requiring server restart.


