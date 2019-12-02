# Software development

As the title of this document says, this manual is for software developers that want to 
customize or develop GeoNetwork themselves. If you just want to use the software and are 
looking for instructions on how to do that, there is a lot of documentation for users, 
administrators, metadata editors and application maintainers at 
http://geonetwork-opensource.org/manuals/trunk/eng/users/index.html

## Build the application

See https://geonetwork-opensource.org/manuals/3.8.x/en/maintainer-guide/installing/installing-from-source-code.html

## Build the documentation

See https://geonetwork-opensource.org/manuals/3.8.x/en/contributing/writing-documentation.html#building-the-docs

## Making a release

See https://geonetwork-opensource.org/manuals/3.8.x/en/contributing/doing-a-release.html

# Eclipse setup

The easiest way to develop GeoNetwork within Eclipse is with [m2e plugin](http://eclipse.org/m2e/),
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

An example of the configuration file for JRebel may be the following:

```xml
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
```

* Tomcat Server :

Create a new Tomcat Server on Eclipse and add the geonetwork-main project as a web project.

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

```bash
mvn install -DskipTests
```

Or if you want to run the tests but skip static analysis:

```bash
mvn install -P-run-static-analysis
```

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

### Forks, Pull requests and branches

If you want to contribute back to GeoNetwork you create a Github account, fork the GeoNetwork repository and work on your fork. This is a huge benefit because you can push your changes to your repository as much as you want and when a feature is complete you can make a 'Pull Request'.  Pull requests are the recommended method of contributing back to GeoNetwork because Github has code review tools and merges are much easier than trying to apply a patch attached to a ticket.

The GeoNetwork Repository is at: https://github.com/geonetwork/core-geonetwork.

Follow the instructions on the Github website to get started (make accounts, how to fork etc...) http://help.github.com/

If you cloned the GeoNetwork Repository earlier, you set you can now set your fork up as a remote and begin to work.

Rename the GeoNetwork repository as ``upstream``:

     git remote rename origin upstream

Add your fork as origin (the URL provided by GitHub CLONE or DOWNLOAD button):

     git remote add origin https://github.com/USERNAME/core-geonetwork.git

List remotes showing ``origin`` and ``upstream``:

     git remote -v
     
To checkout a branch from upstream::

     git checkout -t upstream/3.6.x

### Pull request

See https://geonetwork-opensource.org/manuals/3.8.x/en/contributing/making-a-pull-request.html

