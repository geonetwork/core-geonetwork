<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<sch:schema xmlns:sch="http://www.ascc.net/xml/schematron"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<!--

This Schematron define INSPIRE IR on metadata for datasets and services.

Francois Prunayre, 2008
Etiennet Taffoureau, 2008

This work is licensed under the Creative Commons Attribution 2.5 License. 
To view a copy of this license, visit 
    http://creativecommons.org/licenses/by/2.5/ 

or send a letter to:

Creative Commons, 
543 Howard Street, 5th Floor, 
San Francisco, California, 94105, 
USA.

-->

	<sch:title xmlns="http://www.w3.org/2001/XMLSchema">INSPIRE metadata implementing rule validation</sch:title>
	<sch:ns prefix="gml" uri="http://www.opengis.net/gml"/>
	<sch:ns prefix="gmd" uri="http://www.isotc211.org/2005/gmd"/>
	<sch:ns prefix="srv" uri="http://www.isotc211.org/2005/srv"/>
	<sch:ns prefix="gco" uri="http://www.isotc211.org/2005/gco"/>
	<sch:ns prefix="geonet" uri="http://www.fao.org/geonetwork"/>
	<sch:ns prefix="xlink" uri="http://www.w3.org/1999/xlink"/>
	


	<!-- INSPIRE metadata rules / START -->
	<sch:pattern name="$loc/strings/M35">
		<sch:rule context="//gmd:citation">
			<sch:report test="not(*/gmd:title) or (*/gmd:title/@gco:nilReason='missing')">$loc/strings/alert.M35/div</sch:report>
		</sch:rule>
	</sch:pattern>
	<sch:pattern name="$loc/strings/M36">
		<sch:rule
			context="//gmd:MD_DataIdentification|//*[@gco:isoType='gmd:MD_DataIdentification']">
			<sch:report test="not(gmd:abstract) or (gmd:abstract/@gco:nilReason='missing')">$loc/strings/alert.M36/div</sch:report>
		</sch:rule>
	</sch:pattern>
	<sch:pattern name="$loc/strings/M37">
		<sch:rule context="//gmd:MD_Metadata|//*[@gco:isoType='gmd:MD_Metadata']">
			<sch:report test="not(gmd:hierarchyLevel)">$loc/strings/alert.M37/div</sch:report>
		</sch:rule>
	</sch:pattern>
	<sch:pattern name="$loc/strings/M38">
		<sch:rule context="//gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation|
			//*[@gco:isoType='gmd:MD_DataIdentification']/gmd:citation/gmd:CI_Citation
			">
			<sch:report test="not(gmd:identifier) or gmd:identifier[*/gmd:code/@gco:nilReason='missing']">$loc/strings/alert.M38/div</sch:report>
		</sch:rule>
	</sch:pattern>
	<sch:pattern name="$loc/strings/M39">
		<sch:rule
			context="//gmd:MD_DataIdentification|//*[@gco:isoType='gmd:MD_DataIdentification']">
			<sch:report test="not(gmd:topicCategory)  or (normalize-space(gmd:topicCategory/gmd:MD_TopicCategoryCode) = '')">$loc/strings/alert.M39/div</sch:report>
		</sch:rule>
	</sch:pattern>
	<sch:pattern name="$loc/strings/M40">
		<sch:rule
			context="//gmd:MD_DataIdentification|//*[@gco:isoType='gmd:MD_DataIdentification']">
			<sch:report test="not(gmd:descriptiveKeywords) or (gmd:descriptiveKeywords/*/gmd:keyword/@gco:nilReason) or (normalize-space(gmd:descriptiveKeywords/*/gmd:keyword/gco:CharacterString)='')">$loc/strings/alert.M40/div</sch:report>
		</sch:rule>
	</sch:pattern>
	<sch:pattern name="$loc/strings/M41">
		<sch:rule
			context="//gmd:MD_DataIdentification|//*[@gco:isoType='gmd:MD_DataIdentification']">
			<sch:report
				test="not((gmd:extent/*/gmd:geographicElement/*/gmd:westBoundLongitude) and (gmd:extent/*/gmd:geographicElement/*/gmd:eastBoundLongitude) 
				and (gmd:extent/*/gmd:geographicElement/*/gmd:southBoundLatitude) and (gmd:extent/*/gmd:geographicElement/*/gmd:northBoundLatitude)) 
				or not(normalize-space(gmd:extent/*/gmd:geographicElement/*/gmd:westBoundLongitude/gco:Decimal)) or not(normalize-space(gmd:extent/*/gmd:geographicElement/*/gmd:northBoundLatitude/gco:Decimal)) 
				or not(normalize-space(gmd:extent/*/gmd:geographicElement/*/gmd:eastBoundLongitude/gco:Decimal)) or not(normalize-space(gmd:extent/*/gmd:geographicElement/*/gmd:southBoundLatitude/gco:Decimal))"
				>$loc/strings/alert.M41</sch:report>
		</sch:rule>
	</sch:pattern>
	<sch:pattern name="$loc/strings/M42">
		<sch:rule
			context="//gmd:MD_DataIdentification|//*[@gco:isoType='gmd:MD_DataIdentification']">
			<sch:report
				test="not(normalize-space(gmd:citation//gmd:date//gmd:date/gco:DateTime)) and not(gmd:extent//gmd:temporalElement//gmd:extent/gml:TimePeriod) and not(gmd:extent//gmd:temporalElement//gmd:extent/gml:TimeInstant)"
				>$loc/strings/alert.M42/div</sch:report>
		</sch:rule>
	</sch:pattern>
	<sch:pattern name="$loc/strings/M43">
		<sch:rule context="//gmd:DQ_DataQuality">
			<sch:report test="not(gmd:lineage/gmd:LI_Lineage/gmd:statement) or (gmd:lineage//gmd:statement/@gco:nilReason)"
				>$loc/strings/alert.M43/div</sch:report>
		</sch:rule>
	</sch:pattern>
	<sch:pattern name="$loc/strings/M44">
		<sch:rule context="//gmd:dataQualityInfo/gmd:DQ_DataQuality">
			<sch:report
				test="not(gmd:report//gmd:result) or (gmd:report//gmd:result//gmd:specification//gmd:title/@gco:nilReason)"
				>$loc/strings/alert.M44/div</sch:report>
		</sch:rule>
	</sch:pattern>
	<sch:pattern name="$loc/strings/M45">
		<sch:rule context="//gmd:MD_DataIdentification|//*[@gco:isoType='gmd:MD_DataIdentification']">
			<sch:report test="not(gmd:resourceConstraints/*/gmd:useLimitation) or (gmd:resourceConstraints/*/gmd:useLimitation/@gco:nilReason)"
				>$loc/strings/alert.M45</sch:report>
		</sch:rule>
	</sch:pattern>
	<sch:pattern name="$loc/strings/M46">
		<sch:rule context="//gmd:MD_DataIdentification|//*[@gco:isoType='gmd:MD_DataIdentification']">
			<sch:report test="(not(gmd:resourceConstraints/*/gmd:accessConstraints) or (gmd:resourceConstraints/*/gmd:accessConstraints/gmd:MD_RestrictionCode/@codeListValue='')) and (not(gmd:resourceConstraints/*/gmd:classification) or (gmd:resourceConstraints/*/gmd:classification/gmd:MD_ClassificationCode/@codeListValue='')) and (not(gmd:resourceConstraints/*/gmd:otherConstraints) or (gmd:resourceConstraints/*/gmd:otherConstraints/@gco:nilReason))">$loc/strings/alert.M46</sch:report>
		</sch:rule>
	</sch:pattern>
	<sch:pattern name="$loc/strings/M47">
		<sch:rule context="//gmd:MD_DataIdentification|//*[@gco:isoType='gmd:MD_DataIdentification']">
			<sch:report
				test="not(gmd:pointOfContact/*/gmd:organisationName) or (gmd:pointOfContact/*/gmd:organisationName/@gco:nilReason) or not(gmd:pointOfContact/*/gmd:contactInfo/*/gmd:address/*/gmd:electronicMailAddress) or (gmd:pointOfContact/*/gmd:contactInfo/*/gmd:address/*/gmd:electronicMailAddress/@gco:nilReason)"
				>$loc/strings/alert.M47</sch:report>
		</sch:rule>
	</sch:pattern>
	<sch:pattern name="$loc/strings/M48">
		<sch:rule context="//gmd:MD_Metadata/gmd:contact">
			<sch:report
				test="not(gmd:CI_ResponsibleParty/gmd:organisationName) or (gmd:CI_ResponsibleParty/gmd:organisationName/@gco:nilReason) or not(gmd:CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:electronicMailAddress) or (gmd:CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:electronicMailAddress/@gco:nilReason)"
				>$loc/strings/alert.M48/div</sch:report>
		</sch:rule>
	</sch:pattern>
	<sch:pattern name="$loc/strings/M49">
		<sch:rule context="//gmd:MD_Metadata">
			<sch:report test="(not(gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue='service') and (not(gmd:language) or (gmd:language/@gco:nilReason)))">$loc/strings/alert.M49/div</sch:report>
		</sch:rule>
	</sch:pattern>
	<sch:pattern name="$loc/strings/M50">
		<sch:rule context="//gmd:MD_Metadata|//*[@gco:isoType='gmd:MD_Metadata']">
			<sch:report test="not(gmd:dateStamp)">$loc/strings/alert.M50/div</sch:report>
		</sch:rule>
	</sch:pattern>
	<sch:pattern name="$loc/strings/M51">
		<sch:rule context="//gmd:identificationInfo">
			<sch:report
				test="(../../gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue='service') and not(*/srv:operatesOn)"
				>$loc/strings/alert.M51/div</sch:report>
		</sch:rule>
	</sch:pattern>
	<sch:pattern name="$loc/strings/M52">
		<sch:rule context="//gmd:distributionInfo">
			<sch:report test="not(*/gmd:transferOptions/*/gmd:onLine/*/gmd:linkage)  or normalize-space(*/gmd:transferOptions/*/gmd:onLine/*/gmd:linkage/gmd:URL)=''"
				>$loc/strings/alert.M52/div</sch:report>
		</sch:rule>
	</sch:pattern>
	<sch:pattern name="$loc/strings/M53">
		<sch:rule context="//gmd:identificationInfo">
			<sch:report
				test="count(*/gmd:resourceConstraints/gmd:MD_Constraints/gmd:useLimitation) >1 or 
				count(*/gmd:resourceConstraints/gmd:MD_LegalConstraints/gmd:useLimitation) >1 or 
				count(*/gmd:resourceConstraints/gmd:MD_SecurityConstraints/gmd:useLimitation) >1 or 
				count(*/gmd:resourceConstraints/*[@gco:isoType='gmd:MD_Constraints']/gmd:useLimitation) >1 or 
				count(*/gmd:resourceConstraints/*[@gco:isoType='gmd:MD_LegalConstraints']/gmd:useLimitation) >1 or 
				count(*/gmd:resourceConstraints/*[@gco:isoType='gmd:MD_SecurityConstraints']/gmd:useLimitation) >1"
				>$loc/strings/alert.M53/div</sch:report>
		</sch:rule>
	</sch:pattern>
	<sch:pattern name="$loc/strings/M54">
		<sch:rule context="//gmd:identificationInfo">
			<sch:report
				test="(*/gmd:resourceConstraints/*/gmd:accessConstraints/gmd:MD_RestrictionCode/@codeListValue='otherRestrictions') and 
				not(*/gmd:resourceConstraints/*/gmd:otherConstraints)"
				>$loc/strings/alert.M54/div</sch:report>
		</sch:rule>
	</sch:pattern>
	<sch:pattern name="$loc/strings/M55">
		<sch:rule context="//gmd:MD_DataIdentification|//*[@gco:isoType='gmd:MD_DataIdentification']">
			<sch:report
				test="not(../../gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue='service') and (not(gmd:language) or (gmd:language/@gco:nilReason))"
				>$loc/strings/alert.M55/div</sch:report>
		</sch:rule>
	</sch:pattern>
	<sch:pattern name="$loc/strings/M56">
		<sch:rule context="//gmd:MD_DataIdentification/gmd:spatialResolution|//*[@gco:isoType='gmd:MD_DataIdentification']/gmd:spatialResolution">
			<sch:report
				test="not(*/gmd:equivalentScale) and not(*/gmd:distance)"
				>$loc/strings/alert.M56/div</sch:report>
		</sch:rule>
	</sch:pattern>
	<sch:pattern name="$loc/strings/M57">
		<sch:rule context="//srv:SV_ServiceIdentification|//*[@gco:isoType='srv:SV_ServiceIdentification']">
			<sch:report
				test="not(srv:containsOperations/srv:SV_OperationMetadata/srv:operationName)"
				>$loc/strings/alert.M57/div</sch:report>
		</sch:rule>
	</sch:pattern>
	<sch:pattern name="$loc/strings/M58">
		<sch:rule context="//srv:SV_ServiceIdentification|//*[@gco:isoType='srv:SV_ServiceIdentification']">
			<sch:report test="not(srv:containsOperations/srv:SV_OperationMetadata/srv:DCP)"
				>$loc/strings/alert.M58/div</sch:report>
		</sch:rule>
	</sch:pattern>
	<sch:pattern name="$loc/strings/M59">
		<sch:rule context="//srv:SV_ServiceIdentification|//*[@gco:isoType='srv:SV_ServiceIdentification']">
			<sch:report test="not(srv:containsOperations/srv:SV_OperationMetadata/srv:connectPoint)"
				>$loc/strings/alert.M59/div</sch:report>
		</sch:rule>
	</sch:pattern>
	<sch:pattern name="$loc/strings/M60">
		<sch:rule context="//srv:SV_ServiceIdentification|//*[@gco:isoType='srv:SV_ServiceIdentification']">
			<sch:report test="not(srv:serviceType)">$loc/strings/alert.M60/div</sch:report>
		</sch:rule>
	</sch:pattern>
	<!-- INSPIRE metadata rules / END -->
</sch:schema>
