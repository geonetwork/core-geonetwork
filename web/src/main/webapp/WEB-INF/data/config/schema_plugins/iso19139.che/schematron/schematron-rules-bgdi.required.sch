<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<sch:schema xmlns:sch="http://purl.oclc.org/dsdl/schematron"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
queryBinding="xslt2">
<sch:title xmlns="http://www.w3.org/2001/XMLSchema">BGDI</sch:title>
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
    <sch:title>$loc/strings/translatedTitle</sch:title>
    <!-- Check specification names and status -->
    <sch:rule context="/che:CHE_MD_Metadata/gmd:identificationInfo/*/gmd:citation/gmd:CI_Citation">
        <sch:let name="deTitle" value="normalize-space((gmd:title//gmd:LocalisedCharacterString[@locale='#DE'])[1])"/>
        <sch:let name="frTitle" value="normalize-space((gmd:title//gmd:LocalisedCharacterString[@locale='#FR'])[1])"/>

        <sch:assert test="string-length($deTitle) > 0">
            <sch:value-of select="$loc/strings/deTitleRequired"/>
        </sch:assert>
        <sch:report test="string-length($deTitle) > 0">
            <sch:value-of select="$loc/strings/deTitleReport"/>
            <sch:value-of select="$deTitle"/>
        </sch:report>

        <sch:assert test="string-length($frTitle) > 0">
            <sch:value-of select="$loc/strings/frTitleRequired"/>
        </sch:assert>
        <sch:report test="string-length($frTitle) > 0">
            <sch:value-of select="$loc/strings/frTitleReport"/>
            <sch:value-of select="$frTitle"/>
        </sch:report>
    </sch:rule>
</sch:pattern>


<sch:pattern>
    <sch:title>$loc/strings/translatedAltTitle</sch:title>
    <!-- Check specification names and status -->
    <sch:rule context="/che:CHE_MD_Metadata/gmd:identificationInfo/*/gmd:citation/gmd:CI_Citation">
        <sch:let name="deAltTitle" value="normalize-space((gmd:alternateTitle//gmd:LocalisedCharacterString[@locale='#DE'])[1])"/>
        <sch:let name="frAltTitle" value="normalize-space((gmd:alternateTitle//gmd:LocalisedCharacterString[@locale='#FR'])[1])"/>

        <sch:assert test="string-length($deAltTitle) > 0">
            <sch:value-of select="$loc/strings/deAltTitleRequired"/>
        </sch:assert>
        <sch:report test="string-length($deAltTitle) > 0">
            <sch:value-of select="$loc/strings/deAltTitleReport"/>
            <sch:value-of select="$deAltTitle"/>
        </sch:report>

        <sch:assert test="string-length($deAltTitle) &lt; 31">
            <sch:value-of select="$loc/strings/deAltTitleMaxLength"/>
        </sch:assert>
        <sch:report test="string-length($deAltTitle) &lt; 31 and string-length($deAltTitle) &gt; 0">
            <sch:value-of select="$loc/strings/deAltTitleMaxLengthReport"/>
            <sch:value-of select="string-length($deAltTitle)"/>
        </sch:report>

        <sch:assert test="string-length($frAltTitle) > 0">
            <sch:value-of select="$loc/strings/frAltTitleRequired"/>
        </sch:assert>
        <sch:report test="string-length($frAltTitle) > 0">
            <sch:value-of select="$loc/strings/frAltTitleReport"/>
            <sch:value-of select="$frAltTitle"/>
        </sch:report>


        <sch:assert test="string-length($frAltTitle) &lt; 31">
            <sch:value-of select="$loc/strings/frAltTitleMaxLength"/>
        </sch:assert>
        <sch:report test="string-length($frAltTitle) &lt; 31 and string-length($frAltTitle) &gt; 0">
            <sch:value-of select="$loc/strings/frAltTitleMaxLengthReport"/>
            <sch:value-of select="string-length($frAltTitle)"/>
        </sch:report>
    </sch:rule>
</sch:pattern>

<sch:pattern>
    <sch:title>$loc/strings/translatedAbstract</sch:title>
    <!-- Check specification names and status -->
    <sch:rule context="che:CHE_MD_Metadata/gmd:identificationInfo">
        <sch:let name="deAbstract" value="normalize-space((*/gmd:abstract//gmd:LocalisedCharacterString[@locale='#DE'])[1])"/>
        <sch:let name="frAbstract" value="normalize-space((*/gmd:abstract//gmd:LocalisedCharacterString[@locale='#FR'])[1])"/>

        <sch:assert test="string-length($deAbstract) > 0">
            <sch:value-of select="$loc/strings/deAbstractRequired"/>
        </sch:assert>
        <sch:report test="string-length($deAbstract) > 0">
            <sch:value-of select="$loc/strings/deAbstractReport"/>
            <sch:value-of select="$deAbstract"/>
        </sch:report>

        <sch:assert test="string-length($frAbstract) > 0">
            <sch:value-of select="$loc/strings/frAbstractRequired"/>
        </sch:assert>
        <sch:report test="string-length($frAbstract) > 0">
            <sch:value-of select="$loc/strings/frAbstractReport"/>
            <sch:value-of select="$frAbstract"/>
        </sch:report>
    </sch:rule>
</sch:pattern>



<sch:pattern>
    <sch:title>$loc/strings/pointOfContact</sch:title>
    <!-- Check specification names and status -->
    <sch:rule context="che:CHE_MD_Metadata/gmd:identificationInfo">
        <sch:let name="pointOfContact" value="*/gmd:pointOfContact"/>

        <sch:assert test="$pointOfContact">
            <sch:value-of select="$loc/strings/pointOfContactRequired"/>
        </sch:assert>
        <sch:report test="$pointOfContact">
            <sch:value-of select="$loc/strings/pointOfContactReport"/>
        </sch:report>
    </sch:rule>
</sch:pattern>

</sch:schema>
