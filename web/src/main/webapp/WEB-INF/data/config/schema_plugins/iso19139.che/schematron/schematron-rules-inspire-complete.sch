<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<sch:schema xmlns:sch="http://purl.oclc.org/dsdl/schematron"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	queryBinding="xslt2">

	<sch:title xmlns="http://www.w3.org/2001/XMLSchema">INSPIRE metadata implementing rule validation</sch:title>
	<sch:ns prefix="gml" uri="http://www.opengis.net/gml"/>
	<sch:ns prefix="gmd" uri="http://www.isotc211.org/2005/gmd"/>
    <sch:ns prefix="gmx" uri="http://www.isotc211.org/2005/gmx"/>
	<sch:ns prefix="srv" uri="http://www.isotc211.org/2005/srv"/>
	<sch:ns prefix="gco" uri="http://www.isotc211.org/2005/gco"/>
    <sch:ns prefix="che" uri="http://www.geocat.ch/2008/che"/>
	<sch:ns prefix="geonet" uri="http://www.fao.org/geonetwork"/>
	<sch:ns prefix="skos" uri="http://www.w3.org/2004/02/skos/core#"/>
	<sch:ns prefix="xlink" uri="http://www.w3.org/1999/xlink"/>
	

	<!-- INSPIRE metadata rules / START -->
	<sch:pattern>
		<sch:title>$loc/strings/title.M8.1.Access</sch:title>
		
		<sch:rule context="//gmd:identificationInfo/gmd:MD_Metadata|//*[@gco:isoType='gmd:MD_Metadata']">
			<!-- Title -->
			<sch:let name="noAccessConstraints"
                     value="gmd:resourceConstraints/*/gmd:accessConstraints/gmd:MD_RestrictionCode[codeList='http://www.isotc211.org/2005/resources/codeList.xml#MD_RestrictionCode']/@codeListValue"/>

			<sch:assert test="not($noAccessConstraints)"><sch:value-of select="$loc/strings/alert.M8.1.Access"/></sch:assert>
			<sch:report test="not($noAccessConstraints)">
				<sch:value-of select="$loc/strings/report.M8.1.Access"/>
			</sch:report>
		</sch:rule>
    </sch:pattern>
	<sch:pattern>
		<sch:title>$loc/strings/conditionsForUse</sch:title>

		<sch:rule context="//gmd:identificationInfo/gmd:MD_Metadata|//*[@gco:isoType='gmd:MD_Metadata']">
			<!-- Title -->
			<sch:let name="noAccessConstraints"
                     value="string-length(normalize-space(gmd:resourceConstraints/gmd:MD_Constraints/gmd:useLimitation/gmd:PT_FreeText))"/>

			<sch:assert test="$noAccessConstraints > 0">
                <sch:value-of select="$loc/strings/alert.M8.1.Use"/>
            </sch:assert>
			<sch:report test="$noAccessConstraints > 0">
				<sch:value-of select="$loc/strings/report.M8.1.Use"/>
			</sch:report>
		</sch:rule>
    </sch:pattern>
</sch:schema>
