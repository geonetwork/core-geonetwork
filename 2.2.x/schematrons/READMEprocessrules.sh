#!/bin/sh
# An example of how to create the schematrons for geonetwork - you'll need 
# saxon8.jar and some kind of Unix environment for this script to work.
java -jar saxon8.jar -s allSchtrnRules.sch -o schematron.xsl schematron-report.xsl
java -jar saxon8.jar -s allSchtrnRules.sch -o schematron_xml.xsl schematron-report-geonetwork-xml.xsl
# After this they need to be copied to the relevant schema directory:
# (NOTE: you may want to back up the ones supplied with this release first!)
#
# The first xsl runs the standard schematron report for use by the editor
# validation function, the second xsl is run each time a metadata record is
# read and saved by the editor - it is responsible for the orange "i" alert
# icons next to the element names in the editor
#
# If you are testing an xml file (eg. junk.xml) against the rules then you 
# could do the following to get the schematron-report:
# 
# java -jar saxon8.jar -s junk.xml -o schematron-errors.html anzlic.xsl
# java -jar saxon8.jar -s junk.xml -o schematron-out.html verbid.xsl
#
# then open schematron-frame.html in your favourite browser
#
# NOTE there appears to be no include mechanism in schematron 1.5 so
# its rather difficult to include these rules in another set of rules for
# a profile - a pain but what can we do except roll on to ISO schematron?
# Simon Pigot - September 2007
