#!/bin/sh
# An example of how to create the schematrons for geonetwork - you'll need 
# saxon8.jar and some kind of Unix environment for this script to work.

echo "Converting schematron rules to XSL files ..."
java -jar saxon8.jar -s allSchtrnRules.sch -o schematron.xsl schematron-report.xsl
java -jar saxon8.jar -s allSchtrnRules.sch -o schematron_xml.xsl schematron-report-geonetwork-xml.xsl

# After this they need to be copied to the relevant schema directory:
# (NOTE: you may want to back up the ones supplied with this release first!)
echo "Creating backup of previous stylesheet ..."
export SCHEMA_DIR=../web/geonetwork/xml/schemas/iso19139/

cp $SCHEMA_DIR/schematron.xsl $SCHEMA_DIR/schematron.xsl.backup
cp $SCHEMA_DIR/schematron_xml.xsl $SCHEMA_DIR/schematron.xsl.backup

echo "Copying new schematron rules ..."
cp schematron.xsl schematron_xml.xsl $SCHEMA_DIR/.

export SCHEMA_DIR=../web/geonetwork/xml/schemas/iso19139/

cp $SCHEMA_DIR/schematron.xsl $SCHEMA_DIR/schematron.xsl.backup
cp $SCHEMA_DIR/schematron_xml.xsl $SCHEMA_DIR/schematron.xsl.backup

echo "Copying new schematron rules ..."
cp schematron.xsl schematron_xml.xsl $SCHEMA_DIR/.

echo "Done."
# NOTE there appears to be no include mechanism in schematron 1.5 so
# its rather difficult to include these rules in another set of rules for
# a profile - a pain but what can we do except roll on to ISO schematron?
# Simon Pigot - September 2007