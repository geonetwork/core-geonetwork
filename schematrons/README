= Schematron =

== Introduction ==
Schematron validation is made in 2 steps:
 * on editing based on schematron_xml.xsl which produce (i) icon on error in
the user interface and popup alert when clicking on it.
 * on validation (after XSD validation) based on schematron.xsl. 

The first xsl runs the standard schematron report for use by the editor
validation function, the second xsl is run each time a metadata record is
read and saved by the editor - it is responsible for the orange "i" alert
icons next to the element names in the editor

== Location ==
The schematron validation is located on the xml/schemas/{name of the schema}. 

Schematron.xsl and schematron_xml.xsl are produce by translating schematron 
rules (allSchtrnRules.sch) running processrules.sh. The script is located in 
the schematron direcory. 
 
== i18n ==
Schematron language is based on the user interface. Updating the schematron 
translation is made using the xml/schemas/{name of the schema}/loc/{language 
of the user interface}/schematron.xml.


== Testing schematron ==
If you are testing an xml file (eg. junk.xml) against the rules then you 
could do the following to get the schematron-report:

java -jar saxon8.jar -s junk.xml -o schematron-errors.html anzlic.xsl
java -jar saxon8.jar -s junk.xml -o schematron-out.html verbid.xsl
then open schematron-frame.html in your favourite browser

== Note ==
NOTE there appears to be no include mechanism in schematron 1.5 so
its rather difficult to include these rules in another set of rules for
a profile - a pain but what can we do except roll on to ISO schematron?

@author: Simon Pigot - September 2007
@author: Francois - 2008 - Added i18n support
