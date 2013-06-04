.. _gast:

GeoNetwork’s Administrator Survival Tool - GAST
###############################################

What is GAST?
=============

GAST used to stand for the GeoNetwork’s Administrator Survival Tool. However, most of the functions that used to be in GAST have moved into the 'Administration' page of the GeoNetwork web application. One function that is left to GAST is to provide a graphical user interface for configuring the basic JDBC database settings (see :ref:`basic_database_config`) used by GeoNetwork.

Starting GAST
=============

GAST is an optional component that can be chosen for installation in the GeoNetwork installer. See :ref:`installing`.

On Windows computers, simply select the Start GAST option under the GeoNetwork opensource program group under :menuselection:`Start --> Programs --> GeoNetwork opensource`

Other options to start GAST are either to use a Java command **from a terminal window** or just click on the GAST jar icon. To issue the Java command you have to:

#. change directory to the GeoNetwork installation folder

#. issue the command ``java -jar gast/gast.jar``

GAST will be in current system language if any translation is available. If you want to force GAST GUI language, you could start GAST using the -Duser.language option (e.g. ``./gast.sh -Duser.language=fr or java -Duser.language=fr -jar gast/gast.jar``).

You can also open the GeoNetwork installation folder, go to the gast folder and double click on the gast.jar file. If you have Java installed, GAST should start in a few seconds.

To run, GAST requires Java 1.5 or 1.6. It will not work on Java 1.4.
