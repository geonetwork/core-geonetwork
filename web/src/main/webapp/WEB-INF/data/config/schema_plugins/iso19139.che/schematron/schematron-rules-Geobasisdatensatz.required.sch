<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<sch:schema xmlns:sch="http://purl.oclc.org/dsdl/schematron"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    queryBinding="xslt2">

    <sch:title xmlns="http://www.w3.org/2001/XMLSchema">Geobasisdatensatz</sch:title>
    <sch:ns prefix="gml" uri="http://www.opengis.net/gml"/>
    <sch:ns prefix="gmd" uri="http://www.isotc211.org/2005/gmd"/>
    <sch:ns prefix="gmx" uri="http://www.isotc211.org/2005/gmx"/>
    <sch:ns prefix="srv" uri="http://www.isotc211.org/2005/srv"/>
    <sch:ns prefix="gco" uri="http://www.isotc211.org/2005/gco"/>
    <sch:ns prefix="che" uri="http://www.geocat.ch/2008/che"/>
    <sch:ns prefix="geonet" uri="http://www.fao.org/geonetwork"/>
    <sch:ns prefix="skos" uri="http://www.w3.org/2004/02/skos/core#"/>
    <sch:ns prefix="xlink" uri="http://www.w3.org/1999/xlink"/>


    <sch:pattern>
        <sch:title>$loc/strings/collectivetitle</sch:title>
        <!-- Check specification names and status -->
        <sch:rule context="che:CHE_MD_Metadata/gmd:identificationInfo/*/gmd:citation">
            <sch:let name="collectiveTitle" value="normalize-space(*/gmd:collectiveTitle)"/>

            <sch:assert test="string-length($collectiveTitle) > 0">
                <sch:value-of select="$loc/strings/collectiveTitleRequired"/>
            </sch:assert>
            <sch:report test="string-length($collectiveTitle) > 0">
                <sch:value-of select="$loc/strings/collectiveTitleReport"/>
                <sch:value-of select="$collectiveTitle"/>
            </sch:report>
        </sch:rule>
    </sch:pattern>

    <sch:pattern>
        <sch:title>$loc/strings/basicGeoDataInfo</sch:title>
        <!-- Check specification names and status -->
        <sch:rule context="che:CHE_MD_Metadata/gmd:identificationInfo">
            <sch:let name="geodataId" value="normalize-space(*/che:basicGeodataID/gco:CharacterString)"/>
            <sch:let name="geodataType" value="normalize-space(*/che:basicGeodataIDType/che:basicGeodataIDTypeCode/@codeListValue)"/>

            <sch:assert test="string-length($geodataId) > 0">
                <sch:value-of select="$loc/strings/geodataIdRequired"/>
            </sch:assert>
            <sch:report test="string-length($geodataId) > 0">
                <sch:value-of select="$loc/strings/geodataIdReport"/>
                <sch:value-of select="$geodataId"/>
            </sch:report>

            <sch:assert test="string-length($geodataType) > 0">
                <sch:value-of select="$loc/strings/geodataTypeRequired"/>
            </sch:assert>
            <sch:report test="string-length($geodataType) > 0">
                <sch:value-of select="$loc/strings/geodataTypeReport"/>
                <sch:value-of select="$geodataType"/>
            </sch:report>
        </sch:rule>
    </sch:pattern>

    <sch:pattern>
        <sch:title>$loc/strings/legislativeInformation</sch:title>
        <!-- Check specification names and status -->
        <sch:rule context="che:CHE_MD_Metadata">
            <sch:let name="legislativeInformation" value="che:legislationInformation"/>

            <sch:assert test="$legislativeInformation">
                <sch:value-of select="$loc/strings/legislativeInformationRequired"/>
            </sch:assert>
            <sch:report test="$legislativeInformation">
                <sch:value-of select="$loc/strings/legislativeInformationReport"/>
            </sch:report>
        </sch:rule>
    </sch:pattern>

</sch:schema>
