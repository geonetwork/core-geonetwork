<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<sch:schema xmlns:sch="http://www.ascc.net/xml/schematron">
<!--

This Schematron schema merges three sets of Schematron rules
1. Schematron rules embedded in the GML 3.2 schema
2. Schematron rules implementing the Additional Constraints described in 
   ISO 19139 Table A.1

This script was written by CSIRO for the Australia-New Zealand Land 
Information Council (ANZLIC) as part of a project to develop an XML 
implementation of the ANZLIC ISO Metadata Profile. 

December 2006, March 2007

Port back to good old Schematron-1.5 for use with schematron-report.xsl
and change titles for use as bare bones 19115/19139 schematron checker 
in GN 2.2 onwards.

Simon Pigot, 2007

This work is licensed under the Creative Commons Attribution 2.5 License. 
To view a copy of this license, visit 
    http://creativecommons.org/licenses/by/2.5/au/ 

or send a letter to:

Creative Commons, 
543 Howard Street, 5th Floor, 
San Francisco, California, 94105, 
USA.

-->

	<sch:title xmlns="http://www.w3.org/2001/XMLSchema">Schematron validation for ISO 19115(19139)</sch:title>
	<sch:ns prefix="gml" uri="http://www.opengis.net/gml" />
	<sch:ns prefix="gmd" uri="http://www.isotc211.org/2005/gmd"/>
	<sch:ns prefix="gco" uri="http://www.isotc211.org/2005/gco"/>
	<sch:ns prefix="geonet" uri="http://www.fao.org/geonetwork"/>
	<sch:ns prefix="xlink" uri="http://www.w3.org/1999/xlink"/>

	<!-- Test that every CharacterString element has content or it's parent has a
   		 valid nilReason attribute value - this is not necessary for geonetwork 
			 because update-fixed-info.xsl supplies a gco:nilReason of missing for 
			 all gco:CharacterString elements with no content and removes it if the
			 user fills in a value - this is the same for all gco:nilReason tests 
			 used below - the test for gco:nilReason in 'inapplicable....' etc is
			 "mickey mouse" for that reason. -->
    <sch:pattern name="CharacterString must have content or it's parent must have a valid nilReason attribute.">
      <sch:rule context="*[gco:CharacterString]">
        <sch:report test="(normalize-space(gco:CharacterString) = '') and (not(@gco:nilReason) or not(contains('inapplicable missing template unknown withheld',@gco:nilReason)))">CharacterString must have content or parent\'s nilReason attribute must be legitimate.</sch:report>
        </sch:rule>
    </sch:pattern>

	<sch:pattern name="CRS attributes constraints">
		<!-- UNVERIFIED -->
		<sch:rule id="CRSLabelsPosType" context="//gml:DirectPositionType">
			<sch:report test="not(@srsDimension) or @srsName">The presence of a dimension attribute implies the presence of the srsName attribute.</sch:report>
			<sch:report test="not(@axisLabels) or @srsName">The presence of an axisLabels attribute implies the presence of the srsName attribute.</sch:report>
			<sch:report test="not(@uomLabels) or @srsName">The presence of an uomLabels attribute implies the presence of the srsName attribute.</sch:report>
			<sch:report test="(not(@uomLabels) and not(@axisLabels)) or (@uomLabels and @axisLabels)">The presence of an uomLabels attribute implies the presence of the axisLabels attribute and vice versa.</sch:report>
		</sch:rule>
	</sch:pattern>

	<!--anzlic/trunk/gml/3.2.0/gmd/citation.xsd-->
	<!-- TEST 21 -->
	<sch:pattern name="ISOFTDS19139:2005-TableA1-Row24 - name required">
		<sch:rule context="//gmd:CI_ResponsibleParty">
			<sch:assert test="(count(gmd:individualName) + count(gmd:organisationName) + count(gmd:positionName)) > 0">You must specify one or more of individualName, organisationName or positionName.</sch:assert>
		</sch:rule>
	</sch:pattern>
	<!-- anzlic/trunk/gml/3.2.0/gmd/constraints.xsd-->
	<!-- TEST  4 -->
	<sch:pattern name="ISOFTDS19139:2005-TableA1-Row07 - otherConstraints required if otherRestrictions">
		<sch:rule context="//gmd:MD_LegalConstraints">
			<sch:report test="gmd:accessConstraints/gmd:MD_RestrictionCode/@codeListValue='otherRestrictions' and not(gmd:otherConstraints)">otherConstraints: documented if accessConstraints or useConstraints = \"otherRestrictions\."</sch:report>
			<sch:report test="gmd:useConstraints/gmd:MD_RestrictionCode/@codeListValue='otherRestrictions' and not(gmd:otherConstraints)">otherConstraints: documented if accessConstraints or useConstraints = \"otherRestrictions\."</sch:report>
		</sch:rule>
	</sch:pattern>
	<!-- anzlic/trunk/gml/3.2.0/gmd/content.xsd-->
	<!-- TEST 13 -->
	<sch:pattern name="ISOFTDS19139:2005-TableA1-Row16 - units required for values">
		<sch:rule context="//gmd:MD_Band">
			<sch:report test="(gmd:maxValue or gmd:minValue) and not(gmd:units)">\"units\" is mandatory if \"maxValue\" or \"minValue\" are provided.</sch:report>
		</sch:rule>
	</sch:pattern>
	<!-- anzlic/trunk/gml/3.2.0/gmd/dataQuality.xsd -->
	<!-- TEST 10 -->
	<sch:pattern name="ISOFTDS19139:2005-TableA1-Row13 - description required if no sourceExtent">
		<sch:rule context="//gmd:LI_Source">
			<sch:assert test="gmd:description or gmd:sourceExtent">\"description\" is mandatory if \"sourceExtent\" is not documented.</sch:assert>
		</sch:rule>
	</sch:pattern>
	<!-- TEST 11 -->
	<sch:pattern name="ISOFTDS19139:2005-TableA1-Row14 - sourceExtent required if no description">
		<sch:rule context="//gmd:LI_Source">
			<sch:assert test="gmd:description or gmd:sourceExtent">\"description\" is mandatory if \"sourceExtent\" is not documented.</sch:assert>
		</sch:rule>
	</sch:pattern>
	<!-- TEST  7 -->
	<sch:pattern name="ISOFTDS19139:2005-TableA1-Row10 - content mandatory for dataset or series">
		<sch:rule context="//gmd:DQ_DataQuality">
			<sch:report test="(((count(*/gmd:LI_Lineage/gmd:source) + count(*/gmd:LI_Lineage/gmd:processStep)) = 0) and (gmd:scope/gmd:DQ_Scope/gmd:level/gmd:MD_ScopeCode/@codeListValue='dataset' or gmd:scope/gmd:DQ_Scope/gmd:level/gmd:MD_ScopeCode/@codeListValue='series')) and not(gmd:lineage/gmd:LI_Lineage/gmd:statement) and (gmd:lineage)">If(count(source) + count(processStep) =0) and (DQ_DataQuality.scope.level = \'dataset\' or \'series\') then statement is mandatory.
			</sch:report>
		</sch:rule>
	</sch:pattern>
	<!-- TEST  8 -->
	<sch:pattern name="ISOFTDS19139:2005-TableA1-Row11 - source required if no statement or processStep">
		<sch:rule context="//gmd:LI_Lineage">
			<sch:report test="not(gmd:source) and not(gmd:statement) and not(gmd:processStep)">\"source\" role is mandatory if LI_Lineage.statement and \"processStep\" role are not documented.</sch:report>
		</sch:rule>
	</sch:pattern>
	<!-- TEST  9 -->
	<sch:pattern name="ISOFTDS19139:2005-TableA1-Row12 - processStep required if no statement or source">
		<sch:rule context="//gmd:LI_Lineage">
			<sch:report test="not(gmd:processStep) and not(gmd:statement) and not(gmd:source)">\"processStep\" role is mandatory if LI_Lineage.statement and \"source\" role are not documented.</sch:report>
		</sch:rule>
	</sch:pattern>
	<!-- TEST 5 -->
	<sch:pattern name="ISOFTDS19139:2005-TableA1-Row08 - dataset must have report or lineage">
		<sch:rule context="//gmd:DQ_DataQuality">
			<sch:report test="gmd:scope/gmd:DQ_Scope/gmd:level/gmd:MD_ScopeCode/@codeListValue='dataset' and not(gmd:report) and not(gmd:lineage)">\"report\" or \"lineage\" role is mandatory if scope.DQ_Scope.level = \'dataset\'.</sch:report>
		</sch:rule>
	</sch:pattern>
	<!-- TEST  6 -->
	<sch:pattern name="ISOFTDS19139:2005-TableA1-Row09 - levelDescription needed unless dataset or series">
		<sch:rule context="//gmd:DQ_Scope">
			<sch:assert test="gmd:level/gmd:MD_ScopeCode/@codeListValue='dataset' or gmd:level/gmd:MD_ScopeCode/@codeListValue='series' or (gmd:levelDescription and ((normalize-space(gmd:levelDescription) != '') or (gmd:levelDescription/gmd:MD_ScopeDescription) or (gmd:levelDescription/@gco:nilReason and contains('inapplicable missing template unknown withheld',gmd:levelDescription/@gco:nilReason))))">\"levelDescription\" is mandatory if \"level\" notEqual \'dataset\' or \'series\'.</sch:assert>
		</sch:rule>
	</sch:pattern>
	<!-- anzlic/trunk/gml/3.2.0/gmd/distribution.xsd-->
	<!-- TEST 14 -->
	<sch:pattern name="ISOFTDS19139:2005-TableA1-Row17 - units required for density values">
		<sch:rule context="//gmd:MD_Medium">
			<sch:report test="gmd:density and not(gmd:densityUnits)">\"densityUnits\" is mandatory if \"density\" is provided.</sch:report>
		</sch:rule>
	</sch:pattern>
	<!-- TEST15 -->
	<sch:pattern name="ISOFTDS19139:2005-TableA1-Row18 - MD_Format required">
		<sch:rule context="//gmd:MD_Distribution">
			<sch:assert test="count(gmd:distributionFormat)>0 or count(gmd:distributor/gmd:MD_Distributor/gmd:distributorFormat)>0">count (distributionFormat + distributor/MD_Distributor/distributorFormat) > 0.</sch:assert>
		</sch:rule>
	</sch:pattern>
	<!-- anzlic/trunk/gml/3.2.0/gmd/extent.xsd-->
	<!-- TEST 20 -->
	<sch:pattern name="ISOFTDS19139:2005-TableA1-Row23 - element required">
		<sch:rule context="//gmd:EX_Extent">
			<sch:assert test="count(gmd:description)>0 or count(gmd:geographicElement)>0 or count(gmd:temporalElement)>0 or count(gmd:verticalElement)>0">count(description + geographicElement + temporalElement + verticalElement) &gt; 0.</sch:assert>
		</sch:rule>
	</sch:pattern>
	<!-- TEST  1 -->
	<sch:pattern name="ISOFTDS19139:2005-TableA1-Row04 - dataset must have extent">
		<sch:rule context="//*[gmd:identificationInfo/gmd:MD_DataIdentification]">
			<sch:report test="(not(gmd:hierarchyLevel) or gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue='dataset') and (count(//gmd:MD_DataIdentification/gmd:extent/*/gmd:geographicElement/gmd:EX_GeographicBoundingBox) + count (//gmd:MD_DataIdentification/gmd:extent/*/gmd:geographicElement/gmd:EX_GeographicDescription)) =0 ">MD_Metadata.hierarchyLevel = \"dataset\" (i.e. the default value of this property on the parent) implies count (extent.geographicElement.EX_GeographicBoundingBox) + count (extent.geographicElement.EX_GeographicDescription) &gt;=1.</sch:report>
		</sch:rule>
	</sch:pattern>
	<!-- TEST  2 -->
	<sch:pattern name="ISOFTDS19139:2005-TableA1-Row05 - dataset or series must have topicCategory">
		<sch:rule context="//gmd:MD_DataIdentification">
			<sch:report test="(not(../../gmd:hierarchyLevel) or (../../gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue='dataset') or (../../gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue='series')) and (not(gmd:topicCategory))"> topicCategory is mandatory  if MD_Metadata.hierarchyLevel equal \"dataset\" or \"series\" or doesn\'t exist.</sch:report>
		</sch:rule>
	</sch:pattern>
	<!-- TEST  3 -->
	<sch:pattern name="ISOFTDS19139:2005-TableA1-Row06 - either aggregateDataSetName or aggregateDataSetIdentifier must be documented">
		<sch:rule context="//gmd:MD_AggregateInformation">
			<sch:assert test="gmd:aggregateDataSetName or gmd:aggregateDataSetIdentifier">Either \"aggregateDataSetName\" or \"aggregateDataSetIdentifier\" must be documented.</sch:assert>
		</sch:rule>
	</sch:pattern>
	<!-- anzlic/trunk/gml/3.2.0/gmd/metadataEntity.xsd: -->
	<sch:pattern name="ISOFTDS19139:2005-TableA1-Row01 - language indication">
    <!-- UNVERIFIED -->
    <sch:rule context="//gmd:MD_Metadata|//*[@gco:isoType='gmd:MD_Metadata']">
      <sch:assert test="gmd:language and ((normalize-space(gmd:language) != '')  or (normalize-space(gmd:language/gco:CharacterString) != '') or (gmd:language/gmd:LanguageCode) or (gmd:language/@gco:nilReason and contains('inapplicable missing template unknown withheld',gmd:language/@gco:nilReason)))">language not present.</sch:assert>
      <!-- language: documented if not defined by the encoding standard. 
					 It can't be documented by the encoding because GML doesn't 
					 include xml:language. -->
    </sch:rule>
  </sch:pattern>
	<sch:pattern name="ISOFTDS19139:2005-TableA1-Row02 - character set indication">
    <!-- UNVERIFIED -->
    <sch:rule context="//gmd:MD_Metadata|//*[@gco:isoType='gmd:MD_Metadata']">
      <!-- characterSet: documented if ISO/IEC 10646 not used and not defined by
        the encoding standard. Can't tell if XML declaration has an encoding
        attribute. -->
    </sch:rule>
  </sch:pattern>

	<!-- anzlic/trunk/gml/3.2.0/gmd/metadataExtension.xsd-->
	<!-- TEST 16 -->
	<sch:pattern name="ISOFTDS19139:2005-TableA1-Row19 - detail required unless simple term">
		<sch:rule context="//gmd:MD_ExtendedElementInformation">
			<sch:assert test="(gmd:dataType/gmd:MD_DatatypeCode/@codeListValue='codelist' or gmd:dataType/gmd:MD_DatatypeCode/@codeListValue='enumeration' or gmd:dataType/gmd:MD_DatatypeCode/@codeListValue='codelistElement') or (gmd:obligation and ((normalize-space(gmd:obligation) != '')  or (gmd:obligation/gmd:MD_ObligationCode) or (gmd:obligation/@gco:nilReason and contains('inapplicable missing template unknown withheld',gmd:obligation/@gco:nilReason))))">if \"dataType\" notEqual \'codelist\', \'enumeration\' or \'codelistElement\' then \"obligation\" is mandatory.</sch:assert>
			<sch:assert test="(gmd:dataType/gmd:MD_DatatypeCode/@codeListValue='codelist' or gmd:dataType/gmd:MD_DatatypeCode/@codeListValue='enumeration' or gmd:dataType/gmd:MD_DatatypeCode/@codeListValue='codelistElement') or (gmd:maximumOccurrence and ((normalize-space(gmd:maximumOccurrence) != '')  or (normalize-space(gmd:maximumOccurrence/gco:CharacterString) != '') or (gmd:maximumOccurrence/@gco:nilReason and contains('inapplicable missing template unknown withheld',gmd:maximumOccurrence/@gco:nilReason))))">if \"dataType\" notEqual \'codelist\', \'enumeration\' or \'codelistElement\' then \"maximumOccurence\" is mandatory.</sch:assert>
			<sch:assert test="(gmd:dataType/gmd:MD_DatatypeCode/@codeListValue='codelist' or gmd:dataType/gmd:MD_DatatypeCode/@codeListValue='enumeration' or gmd:dataType/gmd:MD_DatatypeCode/@codeListValue='codelistElement') or (gmd:domainValue and ((normalize-space(gmd:domainValue) != '')  or (normalize-space(gmd:domainValue/gco:CharacterString) != '') or (gmd:domainValue/@gco:nilReason and contains('inapplicable missing template unknown withheld',gmd:domainValue/@gco:nilReason))))">if \"dataType\" notEqual \'codelist\', \'enumeration\' or \'codelistElement\' then \"domainValue\" is mandatory.</sch:assert>
		</sch:rule>
	</sch:pattern>
	<!-- TEST 17 -->
	<sch:pattern name="ISOFTDS19139:2005-TableA1-Row20 - condition">
		<sch:rule context="//gmd:MD_ExtendedElementInformation">
			<sch:report test="gmd:obligation/gmd:MD_ObligationCode='conditional' and not(gmd:condition)">if \"obligation\" = \'conditional\' then \"condition\" is mandatory.</sch:report>
		</sch:rule>
	</sch:pattern>
	<!-- TEST 18 -->
	<sch:pattern name="ISOFTDS19139:2005-TableA1-Row21 - domainCode">
		<sch:rule context="//gmd:MD_ExtendedElementInformation">
			<sch:report test="gmd:dataType/gmd:MD_DatatypeCode/@codeListValue='codelistElement' and not(gmd:domainCode)">if \"dataType\" = \'codelistElement\' then \"domainCode\" is mandatory.</sch:report>
		</sch:rule>
	</sch:pattern>
	<!-- TEST 19 -->
	<sch:pattern name="ISOFTDS19139:2005-TableA1-Row22 - shortName">
		<sch:rule context="//gmd:MD_ExtendedElementInformation">
			<sch:report test="gmd:dataType/gmd:MD_DatatypeCode/@codeListValue!='codelistElement' and not(gmd:shortName)">if \"dataType\" not equal to \'codelistElement\' then \"shortName\" is mandatory.</sch:report>
		</sch:rule>
	</sch:pattern>
	<!-- anzlic/trunk/gml/3.2.0/gmd/spatialRepresentation.xsd-->
	<!-- TEST 12 -->
	<sch:pattern name="ISOFTDS19139:2005-TableA1-Row15 - checkPointDescription required if available">
		<sch:rule context="//gmd:MD_Georectified">
			<sch:report test="(gmd:checkPointAvailability/gco:Boolean='1' or gmd:checkPointAvailability/gco:Boolean='true') and not(gmd:checkPointDescription)">\"checkPointDescription\" is mandatory if \"checkPointAvailability\" = 1 or true.</sch:report>
		</sch:rule>
	</sch:pattern>
</sch:schema>
