
# Software development

As the title of this document says, this manual is for software developers that want to 
customize or develop GeoNetwork themselves. If you just want to use the software and are 
looking for instructions on how to do that, there is a lot of documentation for users, 
administrators, metadata editors and application maintainers at 
http://geonetwork-opensource.org/manuals/trunk/eng/users/index.html

## System Requirements

GeoNetwork is a Java application that runs as a servlet so the Java Runtime
Environment (JRE) must be installed in order to run it.
You can get the JRE from http://openjdk.java.net/ or http://www.oracle.com/technetwork/java/javase/downloads and
you have to download Java 7 or 8. GeoNetwork won’t run with Java 1.4, 1.5, 1.6.


Being written in Java, GeoNetwork can run on any
platform that supports Java, so it can run on Windows, Linux and Mac OSX.


Next, you need a servlet container. GeoNetwork comes with an embedded container (Jetty)
which is fast and well suited for most applications. If you need a stronger one, you
can install Tomcat from the Apache Software Foundation (http://tomcat.apache.org).
It provides load balancing, fault tolerance and other production features. If you
work for an organisation, it is probable that you already use Tomcat.
The tested version is 7.x or 8.x.


Regarding storage, you need a Database Management System (DBMS) like Oracle,
MySQL, Postgresql etc. GeoNetwork comes with an embedded DBMS (H2) which is
used by default during installation. This DBMS can be used for small or desktop
installations of no more than a few thousand metadata records with one or
two users. If you have heavier demands then you should use a professional, stand
alone DBMS.


GeoNetwork does not require a powerful machine. Good performance can be
obtained even with 1GB of RAM. The suggested amount is 2GB. For hard disk
space, you have to consider the space required for the application itself
(about 350 MB) and the space required for data, which can require 50 GB or
more. A simple disk of 250 GB should be OK.  You also need some space
for the search index which is located in ``GEONETWORK_DATA_DIR/index`` (by default GEONETWORK_DATA_DIR is ``INSTALL_DIR/web/geonetwork/WEB_INF/data``. However, even with a few thousand metadata records, the index is small so usually 500 MB of space is more than enough.

The software is run in different ways depending on the servlet container you are
using:

* *Tomcat* - GeoNetwork is available as a WAR file which you can put into the Tomcat webapps directory. Tomcat will deploy the WAR file when it is started. You can then use the Tomcat manager web application to stop/start GeoNetwork. You can also use the startup.* and shutdown.* scripts located in the Tomcat bin directory (.* means .sh or .bat depending on your OS) but if you have other web applications in the tomcat container, then they will also be affected.
* *Jetty* - If you use the provided container you can use the scripts in GeoNetwork’s bin directory. The scripts are startup.* and shutdown.* and you must be inside the bin directory to run them. You can use these scripts just after installation.

## Tools

The following tools are required to be installed to setup a development environment for GeoNetwork:

* **Java** - Developing with GeoNetwork requires Java Development Kit (JDK) 1.7 or greater.
* **Maven** 3.1.0+ - GeoNetwork uses [Maven](http://maven.apache.org/) to manage the build process and the dependencies. Once is installed, you should have the mvn command in your path (on Windows systems, you have to open a shell to check).
* **Git** - GeoNetwork source code is stored and versioned in [a Git repository on Github](https://github.com/geonetwork/core-geonetwork). Depending on your operating system a variety of git clients are avalaible. Check in http://git-scm.com/downloads/guis for some alternatives.  Good documentation can be found on the git website: http://git-scm.com/documentation and on the Github website https://help.github.com/.
* **Ant** - GeoNetwork uses [Ant](http://ant.apache.org/) to build the installer.  Version 1.6.5 works but any other recent version should be OK. Once installed, you should have the ant command in your path (on Windows systems, you have to open a shell to check).
* **Sphinx** - To create the GeoNetwork documentation in a nice format [Sphinx](http://sphinx.pocoo.org/)  is used.
* (Optional) **Python and closure** - See [web-ui module documentation](/web-ui/)
_

# The quick way

Get GeoNetwork running - the short path:

```
git clone --recursive https://github.com/geonetwork/core-geonetwork.git
cd core-geonetwork
mvn clean install -DskipTests
cd web
mvn jetty:run
```
Open your browser and check http://localhost:8080/geonetwork


# How-to build ?
## Check out source code

If you just want to quickly get the code the fastest way is to download the zip bundle: https://github.com/geonetwork/core-geonetwork/zipball/master
or to clone the repository and build:

```
git clone --recursive https://github.com/geonetwork/core-geonetwork.git
cd core-geonetwork
mvn clean install -DskipTests
```

### Submodules

GeoNetwork use submodules. To properly init them use the ``--recursive`` option when cloning the repository or run the following:

```
cd core-geonetwork
git submodule init
git submodule update
```

Then build the application.


### Pull requests and branches

However, it is recommended that if you want to contribute back to GeoNetwork you create a Github account, fork the GeoNetwork repository and work on your fork. This is a huge benefit because you can push your changes to your repository as much as you want and when a feature is complete you can make a 'Pull Request'.  Pull requests are the recommended method of contributing back to GeoNetwork because Github has code review tools and merges are much easier than trying to apply a patch attached to a ticket.

The GeoNetwork Repository is at: https://github.com/geonetwork/core-geonetwork.

Follow the instructions on the Github website to get started (make accounts, how to fork etc...) http://help.github.com/

Once you have the repository forked or cloned locally you can begin to work.

A clone contains all branches so you can list the branches with::

     $ git branch -a

Just look at last section (ignoring remotes/origin/).  To checkout a branch just::

     $ git checkout 2.8.x

Typically work is done on branches and merged back so when developing normally you will go change to the branch you want to work on, create a branch from there, work and then merge the changes back (or make a Pull Request on Github).  There are many great guides (See the links above) but here is a quick sequence illustrating how to make a change and commit the change.

     $ git checkout master
        # master is the 'trunk' and main development branch
        # the checkout command "checks out" the requested branch
     $ git checkout -b myfeature
        # the -b requests that the branch be created
        # ``git branch`` will list all the branches you have checked out locally at some point
        # ``git branch -a`` will list all branches in repository (checked out or not)
     # work work work
     $ git status
        # See what files have been modified or added
     $ git add <new or modified files>
        # Add all files to be committed ``git add -u`` will add all modified (but not untracked)
     $ git commit
        # Commit often.  it is VERY fast to commit
        # NOTE: doing a commit is a local operation.  It does not push the change to Github
     # more work
     # another commit
     $ git push origin myfeature
        # this pushed your new branch to Github now you are ready to make a Pull Request to get the new feature added to GeoNetwork

GeoNetwork uses git submodules in order to keep track of externals dependencies. It is necessary to init and update them after a repository clone or a branch change::

     $ git submodule update --init

### Build GeoNetwork

Once you checked out the code from Github repository, go inside the GeoNetwork’s root folder and execute the maven build command::

  $ mvn clean install


If the build is successful you'll get an output like::
        
        [INFO] 
        [INFO] ------------------------------------------------------------------------
        [INFO] Reactor Summary:
        [INFO] ------------------------------------------------------------------------
        [INFO] GeoNetwork opensource ................................. SUCCESS [1.345s]
        [INFO] Caching xslt module ................................... SUCCESS [1.126s]
        [INFO] Jeeves modules ........................................ SUCCESS [3.970s]
        [INFO] ArcSDE module (dummy-api) ............................. SUCCESS [0.566s]
        [INFO] GeoNetwork web client module .......................... SUCCESS [23.084s]
        [INFO] GeoNetwork user interface module ...................... SUCCESS [15.940s]
        [INFO] Oaipmh modules ........................................ SUCCESS [1.029s]
        [INFO] GeoNetwork domain ..................................... SUCCESS [0.808s]
        [INFO] GeoNetwork core ....................................... SUCCESS [6.426s]
        [INFO] GeoNetwork CSW server ................................. SUCCESS [2.050s]
        [INFO] GeoNetwork health monitor ............................. SUCCESS [1.014s]
        [INFO] GeoNetwork harvesters ................................. SUCCESS [2.583s]
        [INFO] GeoNetwork services ................................... SUCCESS [3.178s]
        [INFO] GeoNetwork Web module ................................. SUCCESS [2:31.387s]
        [INFO] ------------------------------------------------------------------------
        [INFO] ------------------------------------------------------------------------
        [INFO] BUILD SUCCESSFUL
        [INFO] ------------------------------------------------------------------------
        [INFO] Total time: 3 minutes 35 seconds
        [INFO] Finished at: Sun Oct 27 16:21:46 CET 2013



and your local maven repository should contain the GeoNetwork artifacts created (``$HOME/.m2/repository/org/geonetwork-opensource``).

*Note:* Many Maven build options are available. Please refer to the maven documentation for any other options, [Maven: The Complete Reference](http://www.sonatype.com/books/mvnref-book/reference/public-book.html).

For instance, you might like to use following options :

    -- Skip test
    $ mvn install -Dmaven.test.skip=true

    -- Offline use
    $ mvn install -o

Please refer to the maven documentation for any other options, [Maven: The Complete Reference](http://www.sonatype.com/books/mvnref-book/reference/public-book.html)

*Note 2:* There's ongoing work to fix failing tests, so for now (current [develop](https://github.com/geonetwork/core-geonetwork/commit/ba44ebab86119b34bf1d052f54bc3bb1aa9e0913) and 3.0.x branch) you should execute maven with `-DskipTests`

### Run embedded Jetty server

Maven comes with built-in support for Jetty via a [plug-in](http://docs.codehaus.org/display/JETTY/Maven+Jetty+Plugin)

To run GeoNetwork with the embedded Jetty server you have to change directory to the root of the **web** module,
and then execute the following maven command::

    $ mvn jetty:run -Penv-dev

After a moment, GeoNetwork should be accessible at: http://localhost:8080/geonetwork

For changes related to the user interface in the `web-ui` module or the metadata schemas in the `schemas` module, can be deployed in jetty executing the following maven command in the **web** module::

    $ mvn process-resources


### Source code documentation

The GeoNetwork Java source code is based on Javadoc. Javadoc is a tool for
generating API documentation in HTML format from doc comments in source code. To
see documentation generated by the Javadoc tool, go to:

* [GeoNetwork opensource
  Javadoc](../../../javadoc/geonetwork/index.html)


### Build the documentation

*Note:* Building the GeoNetwork documentation requires the following be installed:

        * [Sphinx](http://sphinx.pocoo.org/) version 0.6 or greater (sphinx-doc on ubuntu/debian)
     
    easy_install Sphinx


In order to build the documentation::

  mvn clean install -Pwith-doc



## Creating the installer

To run the build script that creates the installer you need the Ant tool. You can generate an installer by running the ant command inside the **installer** directory::

    $ ant

    Buildfile: build.xml
    setProperties:
    ...
    BUILD SUCCESSFUL
    Total time: 31 seconds

Both platform independent and Windows specific installers are generated by
default.

Make sure you update version number and other relevant properties in the
``installer/build.xml`` file


## Packaging GeoNetwork using Maven

Using Maven, you have the ability to package GeoNetwork in two different ways :

* WAR files (geonetwork.war, geoserver.war)
* Binary ZIP package (with Jetty embedded)

The [Assembly Plugin](http://maven.apache.org/plugins/maven-assembly-plugin/)
is used to create the packages using ::

    $ mvn package assembly:assembly

The Assembly Plugin configuration is in the release module (See bin.xml and zip-war.xml).


# Eclipse setup

The easiest way to develop GeoNetwork within Eclipse is with the [m2e plugin](http://eclipse.org/m2e/),
which comes by default on many Eclipse installations.

## Import source code

In order to import the source code, follow instructions below :

* Press **File**> **Import** Menu item
* In new dialog Select **Maven**> **Existing Maven Projects**
* Press Next

![Import existing projects into Eclipse](../eclipse-import-existing-projects.png)

* In **Select root directory** field enter where your code is:
 * example: C:\dev\geonetwork\trunk
* Select All projects and Press **Finish** button. If another window appears, just continue without changing any option.

It will take some minutes while the m2e plugin downloads all the Maven dependencies.

## Debugging inside Eclipse

* JRebel Plugin :

Using the [JRebel plugin](http://zeroturnaround.com/software/jrebel/) is very useful for debugging in Eclipse.

An example of the configuration file for JRebel may be the following::

     <?xml version="1.0" encoding="UTF-8"?>
     <application xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns="http://www.zeroturnaround.com" xsi:schemaLocation="http://www.zeroturnaround.com http://www.zeroturnaround.com/alderaan/rebel-2_0.xsd">

          <classpath>
     		<dir name="------/web/target/classes"/>
     	</classpath>

     	<web>
     		<link target="/">
     			<dir name="--------/web/src/main/webapp">
     			</dir>
     		</link>
             <link target="/">
                 <dir name="-------/web/target/webapp">
                 </dir>
             </link>
             <link target="/">
                 <dir name="--------/web/target/geonetwork">
                 </dir>
             </link>
     	</web>

     </application>


* Tomcat Server :

Create a new Tomcat Server (6) on Eclipse and add the geonetwork-main project as a web project.

* Remote Debugging :

 * [How do I configure Tomcat to support remote debugging?](http://wiki.apache.org/tomcat/FAQ/Developing#Q1)
 * [How do I remotely debug Tomcat using Eclipse?](http://wiki.apache.org/tomcat/FAQ/Developing#Q2)

## Code Quality Tools in Eclipse

In order to see the same code quality warnings in Eclipse as Maven will detect, Find Bugs and Checkstyle need to be installed in your Eclipse install and configured as follows::

* Start Eclipse
* Go to **Help > Eclipse Marketplace**
 * Install **findbugs**
  * Don't Restart
 * Install **checkstyle**
  * Now Restart
* Open preferences **Window > Preferences**
 *  Select *Java > Code Style > Code Templates*
  *  Select both Comments and Code elements
  *  Click **Import** and import **code_quality/codetemplates.xml**
 *  Select **Java > Code Style > Formatter**
  *  Click **Import** and import **code_quality/formatter.xml**
 *  Select **Java > Code Style > Clean Up**
  *  Click **Import** and import **code_quality/cleanup.xml**
 *  Select **Checkstyle**
  * Click **New**
  * Select **External Configuration**
  * Enter any name (IE GeoNetwork)
  * For **location** choose **code_quality/checkstyle_checks.xml**
  * Press *OK*
  * Select New configuration
  * Press *Set as Default*
 * Select **Java > FindBugs**
  * Set **analysis effort** to **Maximum**
  * Set **Minimum rank to report** to **2**
  * Set **Minimum confidence to report** to **Medium**
  * Check(enable) all bug categories
  * Set all **Mark bugs with ... rank as** to **Warning**
  * Change to _Filter files_ tab
   * Add **code_quality/findbugs-excludes.xml** file to the **Exclude filter files**
 * Close Preferences
 * Right click on project in **Projects View** select **Checkstyle > Activate Checkstyle**
 * Rebuild full project ( **Project > Clean...** )
  * Checkstyle violations will show up as warnings
 * Right click on project in **Projects View** select **Find Bugs > Find Bugs**
   * FindBugs violations will show up as warnings

## Code Quality Tools and Maven

During the build process FindBugs and Checkstyle are executed. If a violation is found then the build will fail. Usually the easiest way of resolving violations are to use Eclipse and run Checkstyle or FindBugs on the class or project with the failure. 
Usually a detailed report will be provided in Eclipse along with suggested fixes. If the violation is determined to be an intentional violation the **code_quality/findbugs-excludes.xml** or **code_quality/checkstyle_suppressions.xml** should be updated to suppress the reporting of the violation. (See FindBugs and Checkstyle sections for more details.)

Since the FindBugs and Checkstyle processes can be quite time consuming, adding -DskipTests to the maven commandline will skip those processes as well as tests.
For example:

    mvn install -DskipTests

Or if you want to run the tests but skip static analysis:

    mvn install -P-run-static-analysis

That disables the profile that executes the static analysis tasks.

### FindBugs

FindBugs is a tool that statically analyzes Java class files and searches for potential bugs. It excels at finding issues like unclosed reasources, inconsistent locking of resources, refering null known null-values. It also checks for bad practices like using default platform charset instead of an explicit charset.

Because bad practices are checked for, sometimes FindBugs detects issues that are intentional. In order to account for these intentional violations FindBugs has exclude filter files which contain rules for violations that should be ignored.
In GeoNetwork the excludes filter file can be found at **<root>/code_quality/findbugs-excludes.xml**.

For complete details of how to specify matches in the excludes file see http://findbugs.sourceforge.net/manual/filter.html and look at the existing examples in the file.

The Maven build will fail if any violations are detected so it is important to run FindBugs on each project and fix or exclude each violation that is reported.

## FindBugs Annotations (JSR 305)

In order to get the maximum benefit from the FindBugs (and Eclipse) analysis the javax.annotation annotations can be used to add metadata to methods, fields and parameters. The most commonly used annotations are @CheckForNull and @Nonnull. These
can be used on a parameter or return value to indicate that the parameter or return value must not be null or may be null. The FindBugs process will enforce these conditions and statically check that null is only ever correctly returned (in the case of return values) or passed to a method (in the case of parameters).

Some resources for these annotations are:

* http://vard-lokkur.blogspot.ch/2012/03/findbugs-and-jsr-305.html
* http://www.infoq.com/news/2008/06/jsr-305-update
* http://www.klocwork.com/blog/static-analysis/jsr-305-a-silver-bullet-or-not-a-bullet-at-all/
* http://minds.coremedia.com/2012/10/31/jsr-305-nonnull-and-guava-preconditions/
* http://findbugs.sourceforge.net/manual/annotations.html (the package names are out of date and should be java.annotation instead of edu.umd.cs.findbugs.annotation but the descriptions are accurate)
