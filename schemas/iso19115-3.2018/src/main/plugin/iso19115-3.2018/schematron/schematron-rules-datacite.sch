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
  <sch:ns prefix="gml" uri="http://www.opengis.net/gml"/>
  <sch:ns prefix="geonet" uri="http://www.fao.org/geonetwork"/>
  <sch:ns prefix="skos" uri="http://www.w3.org/2004/02/skos/core#"/>
  <sch:ns prefix="xlink" uri="http://www.w3.org/1999/xlink"/>
  <sch:ns prefix="srv" uri="http://standards.iso.org/iso/19115/-3/srv/2.0"/>
  <sch:ns prefix="mdb" uri="http://standards.iso.org/iso/19115/-3/mdb/2.0"/>
  <sch:ns prefix="mcc" uri="http://standards.iso.org/iso/19115/-3/mcc/1.0"/>
  <sch:ns prefix="mri" uri="http://standards.iso.org/iso/19115/-3/mri/1.0"/>
  <sch:ns prefix="mrs" uri="http://standards.iso.org/iso/19115/-3/mrs/1.0"/>
  <sch:ns prefix="mrd" uri="http://standards.iso.org/iso/19115/-3/mrd/1.0"/>
  <sch:ns prefix="mco" uri="http://standards.iso.org/iso/19115/-3/mco/1.0"/>
  <sch:ns prefix="msr" uri="http://standards.iso.org/iso/19115/-3/msr/2.0"/>
  <sch:ns prefix="lan" uri="http://standards.iso.org/iso/19115/-3/lan/1.0"/>
  <sch:ns prefix="gcx" uri="http://standards.iso.org/iso/19115/-3/gcx/1.0"/>
  <sch:ns prefix="gex" uri="http://standards.iso.org/iso/19115/-3/gex/1.0"/>
  <sch:ns prefix="dqm" uri="http://standards.iso.org/iso/19157/-2/dqm/1.0"/>
  <sch:ns prefix="cit" uri="http://standards.iso.org/iso/19115/-3/cit/2.0"/>
  <sch:ns prefix="mdq" uri="http://standards.iso.org/iso/19157/-2/mdq/1.0"/>
  <sch:ns prefix="mrl" uri="http://standards.iso.org/iso/19115/-3/mrl/2.0"/>
  <sch:ns prefix="gco" uri="http://standards.iso.org/iso/19115/-3/gco/1.0"/>

  <sch:pattern>
    <sch:title>$loc/strings/datacite.mandatory</sch:title>
    <sch:rule
      context="//mdb:MD_Metadata|//*[@gco:isoType='mdb:MD_Metadata']">

      <sch:let name="title"
               value="mdb:identificationInfo/*/mri:citation/*/cit:title"/>

      <sch:assert test="$title != ''">$loc/strings/datacite.title.missing</sch:assert>
      <sch:report test="$title != ''">
        <sch:value-of select="$loc/strings/datacite.title.present"/>
        <sch:value-of select="$title"/>
      </sch:report>


      <sch:let name="identifier"
               value="string-join(mdb:metadataIdentifier/*/mcc:code/gco:CharacterString, ', ')"/>

      <sch:assert test="$identifier != ''">$loc/strings/datacite.identifier.missing</sch:assert>
      <sch:report test="$identifier != ''">
        <sch:value-of select="$loc/strings/datacite.identifier.present"/>
        "<sch:value-of select="normalize-space($identifier)"/>"
      </sch:report>


      <sch:let name="numberOfCreators"
               value="count(mdb:identificationInfo/*/mri:pointOfContact/*[cit:role/*/@codeListValue = ('pointOfContact', 'custodian')])"/>

      <sch:assert test="$numberOfCreators > 0">$loc/strings/datacite.creator.missing</sch:assert>
      <sch:report test="$numberOfCreators > 0">
        <sch:value-of select="$numberOfCreators"/>
        <sch:value-of select="$loc/strings/datacite.creator.found"/>
      </sch:report>


      <sch:let name="publisher"
               value="(mdb:distributionInfo//mrd:distributorContact)[1]//cit:CI_Organisation/cit:name/(gco:CharacterString|gcx:Anchor)"/>

      <sch:assert test="$publisher != ''">$loc/strings/datacite.publisher.missing</sch:assert>
      <sch:report test="$publisher != ''">
        <sch:value-of select="$loc/strings/datacite.publisher.present"/>
        <sch:value-of select="$publisher"/>
      </sch:report>


      <sch:let name="publicationDate"
               value="string-join(mdb:identificationInfo/*/mri:citation/*/cit:date/*[cit:dateType/*/@codeListValue = 'publication'], ', ')"/>

      <sch:assert test="$publicationDate != ''">$loc/strings/datacite.publicationDate.missing</sch:assert>
      <sch:report test="$publicationDate != ''">
        <sch:value-of select="$loc/strings/datacite.publicationDate.present"/>
        <sch:value-of select="$publicationDate"/>
      </sch:report>

      <sch:let name="type"
               value="string-join(mdb:metadataScope/*/mdb:resourceScope/*/@codeListValue, ', ')"/>

      <sch:assert test="$type != ''">$loc/strings/datacite.type.missing</sch:assert>
      <sch:report test="$type != ''">
        <sch:value-of select="$loc/strings/datacite.type.present"/>
        <sch:value-of select="$type"/>
      </sch:report>

    </sch:rule>
  </sch:pattern>
</sch:schema>
