<?xml version="1.0" encoding="UTF-8"?>
<!--
  ~ Copyright (C) 2001-2016 Food and Agriculture Organization of the
  ~ United Nations (FAO-UN), United Nations World Food Programme (WFP)
  ~ and United Nations Environment Programme (UNEP)
  ~
  ~ This program is free software; you can redistribute it and/or modify
  ~ it under the terms of the GNU General Public License as published by
  ~ the Free Software Foundation; either version 2 of the License, or (at
  ~ your option) any later version.
  ~
  ~ This program is distributed in the hope that it will be useful, but
  ~ WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  ~ General Public License for more details.
  ~
  ~ You should have received a copy of the GNU General Public License
  ~ along with this program; if not, write to the Free Software
  ~ Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
  ~
  ~ Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
  ~ Rome - Italy. email: geonetwork@osgeo.org
  -->

<sch:schema xmlns:sch="http://purl.oclc.org/dsdl/schematron"
>

  <sch:title xmlns="http://www.w3.org/2001/XMLSchema">Datacite (DOI)</sch:title>
  <sch:ns prefix="gmd" uri="http://www.isotc211.org/2005/gmd"/>
  <sch:ns prefix="gmx" uri="http://www.isotc211.org/2005/gmx"/>
  <sch:ns prefix="srv" uri="http://www.isotc211.org/2005/srv"/>
  <sch:ns prefix="gco" uri="http://www.isotc211.org/2005/gco"/>
  <sch:ns prefix="geonet" uri="http://www.fao.org/geonetwork"/>
  <sch:ns prefix="xlink" uri="http://www.w3.org/1999/xlink"/>

  <sch:pattern>
    <sch:title>$loc/strings/datacite.mandatory</sch:title>
    <sch:rule
      context="//gmd:MD_Metadata|//*[@gco:isoType='gmd:MD_Metadata']">

      <sch:let name="title"
               value="gmd:identificationInfo/*/gmd:citation/*/gmd:title"/>

      <sch:assert test="$title != ''">$loc/strings/datacite.title.missing</sch:assert>
      <sch:report test="$title != ''">
        <sch:value-of select="$loc/strings/datacite.title.present"/>
        <sch:value-of select="$title"/>
      </sch:report>


      <sch:let name="identifier"
               value="gmd:fileIdentifier/gco:CharacterString"/>

      <sch:assert test="$identifier != ''">$loc/strings/datacite.identifier.missing</sch:assert>
      <sch:report test="$identifier != ''">
        <sch:value-of select="$loc/strings/datacite.identifier.present"/>
        "<sch:value-of select="normalize-space($identifier)"/>"
      </sch:report>


      <sch:let name="numberOfCreators"
               value="count(gmd:identificationInfo/*/gmd:pointOfContact/*[gmd:role/*/@codeListValue = ('pointOfContact', 'custodian')])"/>

      <sch:assert test="$numberOfCreators > 0">$loc/strings/datacite.creator.missing</sch:assert>
      <sch:report test="$numberOfCreators > 0">
        <sch:value-of select="$numberOfCreators"/>
        <sch:value-of select="$loc/strings/datacite.creator.found"/>
      </sch:report>


      <sch:let name="publisher"
               value="(gmd:distributionInfo//gmd:distributorContact)[1]/*/gmd:organisationName/(gco:CharacterString|gmx:Anchor)"/>

      <sch:assert test="$publisher != ''">$loc/strings/datacite.publisher.missing</sch:assert>
      <sch:report test="$publisher != ''">
        <sch:value-of select="$loc/strings/datacite.publisher.present"/>
        <sch:value-of select="$publisher"/>
      </sch:report>


      <sch:let name="publicationDate"
               value="string-join(gmd:identificationInfo/*/gmd:citation/*/gmd:date/*[gmd:dateType/*/@codeListValue = 'publication'], ', ')"/>

      <sch:assert test="$publicationDate != ''">$loc/strings/datacite.publicationDate.missing</sch:assert>
      <sch:report test="$publicationDate != ''">
        <sch:value-of select="$loc/strings/datacite.publicationDate.present"/>
        <sch:value-of select="$publicationDate"/>
      </sch:report>

      <sch:let name="type"
               value="string-join(gmd:hierarchyLevel/*/@codeListValue, ', ')"/>

      <sch:assert test="$type != ''">$loc/strings/datacite.type.missing</sch:assert>
      <sch:report test="$type != ''">
        <sch:value-of select="$loc/strings/datacite.type.present"/>
        <sch:value-of select="$type"/>
      </sch:report>

    </sch:rule>
  </sch:pattern>
</sch:schema>
