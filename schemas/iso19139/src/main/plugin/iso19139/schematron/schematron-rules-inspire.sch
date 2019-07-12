<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
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
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    queryBinding="xslt2">
    <!--

This Schematron define INSPIRE IR on metadata for datasets and services.

    @author Francois Prunayre, 2008-2010
    @author Etienne Taffoureau, 2008-2010


 * 2008-2010 (geosource and geocat.ch sandbox)
  * First release
 * 201004
  * Updates based on INSPIRE metadata validator available here
  http://www.inspire-geoportal.eu/index.cfm/pageid/48.
  * Improve gco:nilReason check
  * Add multilingual INSPIRE theme rule
  * Add service taxonomy rules
  * Don't generate error when conformity is missing. Then confomity is only non-evaluated

NOTE :
 * A record could be "INSPIRE valid" in GeoNetwork
and not in INSPIRE metadata validator if INSPIRE theme
are defined in an other language than english for example (multilingual validation
not available in INSPIRE geoportal for the time being).
 * inspired by the schematron_rules.xml released under EUPL by
Kristian Senkler (conterra), Gianluca Luraschi (JRC), Ioannis Kanellopoulos (JRC)


TODO :
 * Service taxonomy is using a codelist value instead of a label
 (which is not user friendly).
 * In case of multilingual metadata, some rules may be improved ?
 * Do no check if keywords comes from realease X of GEMET as far
 as we check we have one INSPIRE theme.
 * INSPIRE metadata validation engine is not using same ISO19139 schema
 gmd:language is a codelist not a gco:CharacterString.




This work is licensed under the Creative Commons Attribution 2.5 License.
To view a copy of this license, visit
    http://creativecommons.org/licenses/by/2.5/

or send a letter to:

Creative Commons,
543 Howard Street, 5th Floor,
San Francisco, California, 94105,
USA.

-->

    <sch:title xmlns="http://www.w3.org/2001/XMLSchema">INSPIRE rules</sch:title>
    <sch:ns prefix="gml" uri="http://www.opengis.net/gml/3.2"/>
    <sch:ns prefix="gml320" uri="http://www.opengis.net/gml"/>
    <sch:ns prefix="gmd" uri="http://www.isotc211.org/2005/gmd"/>
    <sch:ns prefix="gmx" uri="http://www.isotc211.org/2005/gmx"/>
    <sch:ns prefix="srv" uri="http://www.isotc211.org/2005/srv"/>
    <sch:ns prefix="gco" uri="http://www.isotc211.org/2005/gco"/>
    <sch:ns prefix="geonet" uri="http://www.fao.org/geonetwork"/>
    <sch:ns prefix="skos" uri="http://www.w3.org/2004/02/skos/core#"/>
    <sch:ns prefix="xlink" uri="http://www.w3.org/1999/xlink"/>


    <!-- INSPIRE metadata rules / START -->
    <sch:pattern>
        <sch:title>$loc/strings/identification</sch:title>

        <!-- Title -->
        <sch:rule context="//gmd:identificationInfo/*/gmd:citation/gmd:CI_Citation">
            <sch:let name="resourceTitleDefined" value="gmd:title/node()"/>
            <sch:assert test="$resourceTitleDefined" see="geonet:child[@name='title']/@uuid">
                <sch:value-of select="$loc/strings/alert.M35/div"/>
            </sch:assert>

            <sch:let name="noResourceTitle" value="gmd:title/@gco:nilReason='missing'"/>
            <sch:let name="resourceTitle" value="gmd:title/*/text()"/>
            <sch:assert test="not($noResourceTitle)" see="gmd:title/geonet:element/@ref">
                <sch:value-of select="$loc/strings/alert.M35/div"/>
            </sch:assert>
            <sch:report test="not($noResourceTitle)">
                <sch:value-of select="$loc/strings/report.M35/div"/><sch:value-of select="$resourceTitle"/>
            </sch:report>
        </sch:rule>

        <!-- Online resource
            Conditional for spatial dataset and spatial dataset series: Mandatory if a URL is available to obtain
            IR more information on the resources and/or access Obligation / condition related services.
            Conditional for services: Mandatory if linkage to the service is available
        -->
        <sch:rule context="//gmd:distributionInfo/*/gmd:transferOptions/*/gmd:onLine/gmd:CI_OnlineResource">
            <sch:let name="resourceLocatorDefined" value="gmd:linkage/node()"/>
            <sch:assert test="$resourceLocatorDefined" see="geonet:child[@name='linkage']/@uuid">
                <sch:value-of select="$loc/strings/alert.M52/div"/>
            </sch:assert>


            <sch:let name="resourceLocator" value="gmd:linkage/*/text()"/>
            <sch:let name="noResourceLocator" value="normalize-space(gmd:linkage/gmd:URL/text())=''"/>
            <sch:assert test="not($noResourceLocator)" see="gmd:linkage/geonet:element/@ref">
                <sch:value-of select="$loc/strings/alert.M52/div"/>
            </sch:assert>
            <sch:report test="not($noResourceLocator)">
                <sch:value-of select="$loc/strings/report.M52/div"/>
                <sch:value-of select="$resourceLocator"/>
            </sch:report>
        </sch:rule>

        <!-- Resource type -->
        <sch:rule context="/gmd:MD_Metadata">
            <sch:let name="resourceTypeDefined" value="gmd:hierarchyLevel/node()"/>
            <sch:assert test="$resourceTypeDefined" see="geonet:child[@name='hierarchyLevel']/@uuid">
                <sch:value-of select="$loc/strings/alert.M37/div"/>
            </sch:assert>

            <sch:let name="resourceType_present" value="gmd:hierarchyLevel/*/@codeListValue='dataset' or
                                                        gmd:hierarchyLevel/*/@codeListValue='series' or
                                                        gmd:hierarchyLevel/*/@codeListValue='service'"/>
            <sch:let name="resourceType" value="string-join(gmd:hierarchyLevel/*/@codeListValue, ',')"/>
            <sch:assert test="$resourceType_present" see="gmd:hierarchyLevel/geonet:element/@ref">
                <sch:value-of select="$loc/strings/alert.M37/div"/>
            </sch:assert>
            <sch:report test="$resourceType_present">
                <sch:value-of select="$loc/strings/report.M37/div"/>
                <sch:value-of select="$resourceType"/>
            </sch:report>
        </sch:rule>

        <!-- Abstract -->
        <sch:rule context="//gmd:MD_DataIdentification|
			                //*[@gco:isoType='gmd:MD_DataIdentification']|
			                //srv:SV_ServiceIdentification|
			                //*[@gco:isoType='srv:SV_ServiceIdentification']">

            <sch:let name="abstractDefined" value="gmd:abstract/node()"/>
            <sch:assert test="$abstractDefined" see="geonet:child[@name='abstract']/@uuid">
                <sch:value-of select="$loc/strings/alert.M36/div"/>
            </sch:assert>

            <sch:let name="resourceAbstract" value="gmd:abstract/*/text()"/>
            <sch:let name="abstractMissing" value="gmd:abstract/@gco:nilReason='missing'"/>
            <sch:assert test="not($abstractMissing)" see="gmd:abstract/geonet:element/@ref">
                <sch:value-of select="$loc/strings/alert.M36/div"/>
            </sch:assert>
            <sch:report test="not($abstractMissing)">
                <sch:value-of select="$loc/strings/report.M36/div"/>
                <sch:value-of select="$resourceAbstract"/>
            </sch:report>
        </sch:rule>
    </sch:pattern>

    <!-- Dataset and series only -->
    <sch:pattern>
        <sch:title>$loc/strings/dataIdentification</sch:title>

        <sch:rule context="//gmd:MD_DataIdentification[
			                    normalize-space(../../gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue) = 'series' or
			                    normalize-space(../../gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue) = 'dataset' or
			                    normalize-space(../../gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue) = '']|
			               //*[@gco:isoType='gmd:MD_DataIdentification' and (
			                    normalize-space(../../gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue) = 'series' or
			                    normalize-space(../../gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue) = 'dataset' or
			                    normalize-space(../../gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue) = '')]">

            <!-- resource language is only conditional for 'dataset' and 'series'. -->
            <sch:let name="languageDefined" value="gmd:language/node()"/>
            <sch:assert test="$languageDefined" see="geonet:child[@name='language']/@uuid">
                <sch:value-of select="$loc/strings/alert.M55/div"/>
            </sch:assert>

            <sch:let name="resourceLanguage" value="string-join(gmd:language/gco:CharacterString|gmd:language/gmd:LanguageCode/@codeListValue, ', ')"/>
            <sch:let name="euLanguage" value="
				not(gmd:language/@gco:nilReason='missing') and
				geonet:contains-any-of($resourceLanguage,
				('eng', 'fre', 'ger', 'spa', 'dut', 'ita', 'cze', 'lav', 'dan', 'lit', 'mlt',
				'pol', 'est', 'por', 'fin', 'rum', 'slo', 'slv', 'gre', 'bul',
				'hun', 'swe', 'gle'))"/>
            <sch:assert test="$euLanguage" see="gmd:language/geonet:element/@ref">
                <sch:value-of select="$loc/strings/alert.M55/div"/>
            </sch:assert>
            <sch:report test="$euLanguage">
                <sch:value-of select="$loc/strings/report.M55/div"/><sch:value-of select="$resourceLanguage"/>
            </sch:report>

            <!-- Topic category -->
            <sch:let name="topicDefined" value="gmd:topicCategory/node()"/>
            <sch:assert test="$topicDefined" see="geonet:child[@name='topicCategory']/@uuid">
                <sch:value-of select="$loc/strings/alert.M39/div"/>
            </sch:assert>

            <sch:let name="topic" value="gmd:topicCategory/gmd:MD_TopicCategoryCode"/>
            <sch:let name="noTopic" value="gmd:topicCategory/gmd:MD_TopicCategoryCode/text() = ''"/>
            <sch:assert test="not($noTopic)" see="gmd:topicCategory/geonet:element/@ref">
                <sch:value-of select="$loc/strings/alert.M39/div"/>
            </sch:assert>
            <sch:report test="not($noTopic)">
                <sch:value-of select="$loc/strings/report.M39/div"/><sch:value-of select="$topic"/>
            </sch:report>

            <!-- Unique identifier -->
            <sch:let name="idDefined" value="gmd:citation/gmd:CI_Citation/gmd:identifier/node()"/>
            <sch:assert test="$idDefined" see="gmd:citation/gmd:CI_Citation/geonet:child[@name='identifier']/@uuid">
                <sch:value-of select="$loc/strings/alert.M38/div"/>
            </sch:assert>

            <sch:let name="resourceIdentifier_code" value="gmd:citation/gmd:CI_Citation/gmd:identifier/*/gmd:code/*/text()"/>
            <sch:let name="resourceIdentifier" value="gmd:citation/gmd:CI_Citation/gmd:identifier
				and not(gmd:citation/gmd:CI_Citation/gmd:identifier[*/gmd:code/@gco:nilReason='missing'])
				and $resourceIdentifier_code"/>
            <sch:let name="resourceIdentifier_codeSpace" value="gmd:citation/gmd:CI_Citation/gmd:identifier/*/gmd:codeSpace/*/text()"/>
            <sch:assert test="$resourceIdentifier" see="gmd:citation/gmd:CI_Citation/gmd:identifier/geonet:element/@ref">
                <sch:value-of select="$loc/strings/alert.M38/div"/>
            </sch:assert>
            <sch:report test="$resourceIdentifier_code"><sch:value-of select="$loc/strings/report.M38/div"/>
                <sch:value-of select="$resourceIdentifier_code"/>
            </sch:report>
            <sch:report test="$resourceIdentifier_codeSpace"><sch:value-of select="$loc/strings/report.M38.codespace/div"/>
                <sch:value-of select="$resourceIdentifier_codeSpace"/>
            </sch:report>
        </sch:rule>
    </sch:pattern>

    <!-- Service only -->
    <sch:pattern>
        <sch:title>$loc/strings/serviceIdentification</sch:title>

        <sch:rule context="//srv:SV_ServiceIdentification|//*[@gco:isoType='srv:SV_ServiceIdentification']">
            <sch:let name="serviceTypeDefined" value="srv:serviceType/node()"/>
            <sch:assert test="$serviceTypeDefined" see="geonet:child[@name='serviceType']/@uuid">
                <sch:value-of select="$loc/strings/alert.M60/div"/>
            </sch:assert>

            <sch:let name="serviceType" value="srv:serviceType/gco:LocalName"/>
            <sch:let name="serviceTypeWellDefined" value="geonet:contains-any-of(srv:serviceType/gco:LocalName,
				('view', 'discovery', 'download', 'transformation', 'invoke', 'other'))"/>
            <sch:assert test="$serviceTypeWellDefined" see="srv:serviceType/geonet:element/@ref">
                <sch:value-of select="$loc/strings/alert.M60/div"/></sch:assert>
            <sch:report test="$serviceTypeWellDefined">
                <sch:value-of select="$loc/strings/report.M60/div"/><sch:value-of select="$serviceType"/></sch:report>

            <sch:let name="coupledResourceHref" value="string-join(srv:operatesOn/@xlink:href, ', ')"/>
            <sch:let name="coupledResourceUUID" value="string-join(srv:operatesOn/@uuidref, ', ')"/>
            <sch:let name="coupledResource" value="../../gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue='service'
				and //srv:operatesOn"/>
            <!--
              "Conditional to services: Mandatory if linkage to
              datasets on which the service operates are available."
              TODO : maybe check if service couplingType=tight or serviceType=view ?
            <sch:assert test="$coupledResource">
                <sch:value-of select="$loc/strings/alert.M51/div"/>
            </sch:assert>
            -->

            <sch:report test="$coupledResource and $coupledResourceHref!=''">
                <sch:value-of select="$loc/strings/report.M51/div"/><sch:value-of select="$coupledResourceHref"/>
            </sch:report>
            <sch:report test="$coupledResource and $coupledResourceUUID!=''">
                <sch:value-of select="$loc/strings/report.M51/div"/><sch:value-of select="$coupledResourceUUID"/>
            </sch:report>

        </sch:rule>
    </sch:pattern>



    <sch:pattern>
        <sch:title>$loc/strings/theme</sch:title>

        <sch:rule context="//gmd:MD_DataIdentification|
			//*[@gco:isoType='gmd:MD_DataIdentification']">
            <!-- Check that INSPIRE theme are available.
                Use INSPIRE thesaurus available on SVN to check keywords in all EU languages.
            -->
            <sch:let name="thesaurusURL" value="concat('file:///', $thesaurusDir, '/external/thesauri/theme/httpinspireeceuropaeutheme-theme.rdf')"/>
            <sch:let name="inspire-thesaurus" value="document($thesaurusURL)"/>
            <sch:let name="inspire-theme" value="$inspire-thesaurus//skos:Concept"/>

            <!-- Display error if INSPIRE Theme thesaurus is not available. -->
            <sch:assert test="count($inspire-theme) > 0">
                INSPIRE Theme thesaurus not found (at <sch:value-of select="$thesaurusURL" />. Check installation in codelist/external/thesauri/theme.
                Download thesaurus from INSPIRE Registry.
            </sch:assert>


            <sch:let name="thesaurus_name" value="gmd:descriptiveKeywords/*/gmd:thesaurusName/*/gmd:title/*/text()"/>
            <sch:let name="thesaurus_date" value="gmd:descriptiveKeywords/*/gmd:thesaurusName/*/gmd:date/*/gmd:date/*/text()"/>
            <sch:let name="thesaurus_dateType" value="gmd:descriptiveKeywords/*/gmd:thesaurusName/*/gmd:date/*/gmd:dateType/*/@codeListValue/text()"/>
            <sch:let name="keyword"
                value="gmd:descriptiveKeywords/*/gmd:keyword/gco:CharacterString|
				gmd:descriptiveKeywords/*/gmd:keyword/gmx:Anchor"/>
            <sch:let name="inspire-theme-found"
                value="count($inspire-thesaurus//skos:Concept[skos:prefLabel = $keyword])"/>
            <sch:assert test="$inspire-theme-found > 0" see="geonet:child[@name='descriptiveKeywords']/@uuid">
                <sch:value-of select="$loc/strings/alert.M40/div"/>
            </sch:assert>
            <sch:report test="$inspire-theme-found > 0">
                <sch:value-of select="$inspire-theme-found"/> <sch:value-of select="$loc/strings/report.M40/div"/>
            </sch:report>
            <sch:report test="$thesaurus_name">Thesaurus:
                <sch:value-of select="$thesaurus_name"/>, <sch:value-of select="$thesaurus_date"/> (<sch:value-of select="$thesaurus_dateType"/>)
            </sch:report>
            <!-- TODO : We should check GEMET Thesaurus reference and date is set. -->
        </sch:rule>

    </sch:pattern>


    <sch:pattern>
        <sch:title>$loc/strings/serviceTaxonomy</sch:title>

        <sch:rule context="//srv:SV_ServiceIdentification|//*[@gco:isoType='srv:SV_ServiceIdentification']">
            <!-- Check that INSPIRE service taxonomy is available.
                Use INSPIRE thesaurus available on SVN to check keywords in all EU languages.
            -->
            <sch:let name="inspire-thesaurus" value="document(concat('file:///', $thesaurusDir, '/external/thesauri/theme/inspire-service-taxonomy.rdf'))"/>
            <sch:let name="inspire-st" value="$inspire-thesaurus//skos:Concept"/>

            <!-- Display error if INSPIRE thesaurus is not available. -->
            <sch:assert test="count($inspire-st) > 0">
                INSPIRE service taxonomy thesaurus not found. Check installation in codelist/external/thesauri/theme.
                Download thesaurus from https://geonetwork.svn.sourceforge.net/svnroot/geonetwork/utilities/gemet/thesauri/.
            </sch:assert>

            <sch:let name="keyword"
                value="gmd:descriptiveKeywords/*/gmd:keyword/gco:CharacterString | gmd:descriptiveKeywords/*/gmd:keyword//gmd:LocalisedCharacterString |
				gmd:descriptiveKeywords/*/gmd:keyword/gmx:Anchor"/>
            <sch:let name="inspire-theme-found"
                value="count($inspire-thesaurus//skos:Concept[skos:prefLabel = $keyword])"/>
            <sch:assert test="$inspire-theme-found > 0" see="geonet:child[@name='descriptiveKeywords']/@uuid">
                <sch:value-of select="$loc/strings/alert.M58/div"/>
            </sch:assert>
            <sch:report test="$inspire-theme-found > 0">
                <sch:value-of select="$inspire-theme-found"/> <sch:value-of select="$loc/strings/report.M58/div"/>
            </sch:report>

        </sch:rule>
    </sch:pattern>

    <sch:pattern>
        <sch:title>$loc/strings/geo</sch:title>

        <sch:rule context="//gmd:MD_DataIdentification[
			normalize-space(../../gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue) = 'series'
			or normalize-space(../../gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue) = 'dataset'
			or normalize-space(../../gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue) = '']
			/gmd:extent/*/gmd:geographicElement/gmd:EX_GeographicBoundingBox
			|
			//*[@gco:isoType='gmd:MD_DataIdentification' and (
			normalize-space(../../gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue) = 'series'
			or normalize-space(../../gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue) = 'dataset'
			or normalize-space(../../gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue) = '')]
			/gmd:extent/*/gmd:geographicElement/gmd:EX_GeographicBoundingBox
			">

            <sch:let name="west" value="number(gmd:westBoundLongitude/gco:Decimal/text())"/>
            <sch:let name="east" value="number(gmd:eastBoundLongitude/gco:Decimal/text())"/>
            <sch:let name="north" value="number(gmd:northBoundLatitude/gco:Decimal/text())"/>
            <sch:let name="south" value="number(gmd:southBoundLatitude/gco:Decimal/text())"/>

            <!-- assertions and report -->
            <sch:assert test="(-180.00 &lt;= $west) and ( $west &lt;= 180.00)"><sch:value-of select="$loc/strings/alert.M41.W/div"/></sch:assert>
            <sch:report test="(-180.00 &lt;= $west) and ( $west &lt;= 180.00)"><sch:value-of select="$loc/strings/report.M41.W/div"/>
                <sch:value-of select="$west"/>
            </sch:report>
            <sch:assert test="(-180.00 &lt;= $east) and ($east &lt;= 180.00)"><sch:value-of select="$loc/strings/alert.M41.E/div"/></sch:assert>
            <sch:report test="(-180.00 &lt;= $east) and ($east &lt;= 180.00)"><sch:value-of select="$loc/strings/report.M41.E/div"/>
                <sch:value-of select="$east"/>
            </sch:report>
            <sch:assert test="(-90.00 &lt;= $south) and ($south &lt;= $north)"><sch:value-of select="$loc/strings/alert.M41.S/div"/></sch:assert>
            <sch:report test="(-90.00 &lt;= $south) and ($south &lt;= $north)"><sch:value-of select="$loc/strings/report.M41.S/div"/>
                <sch:value-of select="$south"/>
            </sch:report>
            <sch:assert test="($south &lt;= $north) and ($north &lt;= 90.00)"><sch:value-of select="$loc/strings/alert.M41.N/div"/></sch:assert>
            <sch:report test="($south &lt;= $north) and ($north &lt;= 90.00)"><sch:value-of select="$loc/strings/report.M41.N/div"/>
                <sch:value-of select="$north"/>
            </sch:report>
        </sch:rule>

        <sch:rule context="//srv:SV_ServiceIdentification[
			normalize-space(../../gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue) = 'service']
			/srv:extent/*/gmd:geographicElement/gmd:EX_GeographicBoundingBox">
            <sch:let name="west" value="number(gmd:westBoundLongitude/gco:Decimal/text())"/>
            <sch:let name="east" value="number(gmd:eastBoundLongitude/gco:Decimal/text())"/>
            <sch:let name="north" value="number(gmd:northBoundLatitude/gco:Decimal/text())"/>
            <sch:let name="south" value="number(gmd:southBoundLatitude/gco:Decimal/text())"/>
            <!-- report only but we should do assert if outOfBounds ? TODO -->
            <sch:report test="(-180.00 &lt;= $west) and ( $west &lt;= 180.00)"><sch:value-of select="$loc/strings/report.M41.W/div"/>
                <sch:value-of select="$west"/>
            </sch:report>
            <sch:report test="(-180.00 &lt;= $east) and ($east &lt;= 180.00)"><sch:value-of select="$loc/strings/report.M41.E/div"/>
                <sch:value-of select="$east"/>
            </sch:report>
            <sch:report test="(-90.00 &lt;= $south) and ($south &lt;= $north)"><sch:value-of select="$loc/strings/report.M41.S/div"/>
                <sch:value-of select="$south"/>
            </sch:report>
            <sch:report test="($south &lt;= $north) and ($north &lt;= 90.00)"><sch:value-of select="$loc/strings/report.M41.N/div"/>
                <sch:value-of select="$north"/>
            </sch:report>
        </sch:rule>
    </sch:pattern>

    <sch:pattern>
        <sch:title>$loc/strings/temporal</sch:title>
        <sch:rule context="//gmd:MD_DataIdentification|
			//*[@gco:isoType='gmd:MD_DataIdentification']|
			//srv:SV_ServiceIdentification|
			//*[@gco:isoType='srv:SV_ServiceIdentification']">
            <sch:let name="temporalExtentBegin"
                value="gmd:extent/*/gmd:temporalElement/*/gmd:extent/*/(gml:beginPosition|gml320:beginPosition)/text()"/>
            <sch:let name="temporalExtentEnd"
                value="gmd:extent/*/gmd:temporalElement/*/gmd:extent/*/(gml:endPosition|gml320:endPosition)/text()"/>
            <sch:let name="publicationDate"
                value="gmd:citation/*/gmd:date[./*/gmd:dateType/*/@codeListValue='publication']/*/gmd:date/*"/>
            <sch:let name="creationDate"
                value="gmd:citation/*/gmd:date[./*/gmd:dateType/*/@codeListValue='creation']/*/gmd:date/*"/>
            <sch:let name="no_creationDate"
                value="count(gmd:citation/*/gmd:date[./*/gmd:dateType/*/@codeListValue='creation'])"/>
            <sch:let name="revisionDate"
                value="gmd:citation/*/gmd:date[./*/gmd:dateType/*/@codeListValue='revision']/*/gmd:date/*"/>

            <sch:assert test="$no_creationDate &lt;= 1">
                <sch:value-of select="$loc/strings/alert.M42.creation/div"/>
            </sch:assert>

            <sch:assert test="$publicationDate or $creationDate or $revisionDate or $temporalExtentBegin or $temporalExtentEnd">
                <sch:value-of select="$loc/strings/alert.M42/div"/></sch:assert>
            <sch:report test="$temporalExtentBegin">
                <sch:value-of select="$loc/strings/report.M42.begin/div"/>
                <sch:value-of select="$temporalExtentBegin"/>
            </sch:report>
            <sch:report test="$temporalExtentEnd">
                <sch:value-of select="$loc/strings/report.M42.end/div"/>
                <sch:value-of select="$temporalExtentEnd"/>
            </sch:report>
            <sch:report test="$publicationDate">
                <sch:value-of select="$loc/strings/report.M42.publication/div"/>
                <sch:value-of select="$publicationDate"/>
            </sch:report>
            <sch:report test="$revisionDate">
                <sch:value-of select="$loc/strings/report.M42.revision/div"/>
                <sch:value-of select="$revisionDate"/>
            </sch:report>
            <sch:report test="$creationDate">
                <sch:value-of select="$loc/strings/report.M42.creation/div"/>
                <sch:value-of select="$creationDate"/>
            </sch:report>
        </sch:rule>
    </sch:pattern>



    <sch:pattern>
        <sch:title>$loc/strings/quality</sch:title>
        <sch:rule context="//gmd:MD_DataIdentification/gmd:spatialResolution|//*[@gco:isoType='gmd:MD_DataIdentification']/gmd:spatialResolution">
            <sch:let name="distanceDefined" value="*/gmd:distance/node()"/>
            <sch:let name="equivalentScaleDefined" value="*/gmd:equivalentScale/node()"/>

            <sch:assert test="$distanceDefined or $equivalentScaleDefined">
                <sch:value-of select="$loc/strings/alert.M56/div"/>
            </sch:assert>
            <sch:report test="*/gmd:equivalentScale or */gmd:distance">
                <sch:value-of select="$loc/strings/alert.M56/div"/>
            </sch:report>
        </sch:rule>
    </sch:pattern>


    <!-- Make a non blocker conformity check operation - no assertion here -->
    <sch:pattern>
        <sch:title>$loc/strings/conformity</sch:title>
        <!-- Search for on quality report result with status ... We don't really know if it's an INSPIRE conformity report or not. -->
        <sch:rule context="/gmd:MD_Metadata">
            <sch:let name="qualityInfoDefined" value="gmd:dataQualityInfo/node()"/>
            <sch:assert test="$qualityInfoDefined" see="geonet:child[@name='dataQualityInfo']/@uuid">
                <sch:value-of select="$loc/strings/alert.M43/div"/>
            </sch:assert>

            <sch:let name="lineageDefined" value="gmd:dataQualityInfo/*/gmd:lineage/node()"/>
            <sch:assert test="$lineageDefined" see="gmd:dataQualityInfo/gmd:DQ_DataQuality/geonet:child[@name='lineage']/@uuid">
                <sch:value-of select="$loc/strings/alert.M43/div"/>
            </sch:assert>

            <sch:let name="statementDefined" value="gmd:dataQualityInfo/*/gmd:lineage/*/gmd:statement/node()"/>
            <sch:assert test="$statementDefined" see="gmd:dataQualityInfo/*/gmd:lineage/*/geonet:child[@name='statement']/@uuid">
                <sch:value-of select="$loc/strings/alert.M43/div"/>
            </sch:assert>

            <sch:let name="hasAtLeastOneLineage" value="count(gmd:dataQualityInfo/*/gmd:lineage/*/gmd:statement[not(@gco:nilReason)]) > 0"/>
            <sch:assert test="$hasAtLeastOneLineage" see="gmd:dataQualityInfo/*/gmd:lineage/*/gmd:statement/geonet:element/@ref">
                <sch:value-of select="$loc/strings/alert.M43/div"/>
            </sch:assert>
            <sch:report test="$hasAtLeastOneLineage">
                <sch:value-of select="$loc/strings/report.M43/div"/>
            </sch:report>

            <sch:let name="degree" value="count(gmd:dataQualityInfo/*/gmd:report/*/gmd:result/*/gmd:pass)"/>

            <sch:assert test="$qualityInfoDefined" see="geonet:child[@name='dataQualityInfo']/@uuid">
                   <sch:value-of select="$loc/strings/alert.M44/div"/>
            </sch:assert>

            <sch:let name="reportDefined" value="gmd:dataQualityInfo/*/gmd:report/node()"/>
            <sch:assert test="$reportDefined" see="gmd:dataQualityInfo/*/geonet:child[@name='report']/@uuid">
                <sch:value-of select="$loc/strings/alert.M44/div"/>
            </sch:assert>

            <sch:let name="resultDefined" value="gmd:dataQualityInfo/*/gmd:report/*/gmd:result/node()"/>
            <sch:assert test="$resultDefined" see="gmd:dataQualityInfo/*/gmd:report/*/geonet:child[@name='result']/@uuid">
                <sch:value-of select="$loc/strings/alert.M44/div"/>
            </sch:assert>

            <sch:assert test="$degree">
                <sch:value-of select="$loc/strings/alert.M44/div"/><sch:value-of select="$degree>0"/>
            </sch:assert>

            <sch:report test="$degree">
                <sch:value-of select="$loc/strings/report.M44.nonev/div"/>
            </sch:report>
        </sch:rule>

        <!-- Check specification names and status -->
        <sch:rule context="//gmd:dataQualityInfo/*/gmd:report/*/gmd:result/*">
            <sch:let name="degree" value="gmd:pass/*/text()"/>
			<sch:let name="specification_title" value="gmd:specification/*/gmd:title/*/text()"/>
            <sch:let name="specification_date" value="gmd:specification/*/gmd:date/*/gmd:date/*/text()"/>
            <sch:let name="specification_dateType" value="normalize-space(gmd:specification/*/gmd:date/*/gmd:dateType/*/@codeListValue)"/>

            <sch:report test="$specification_title"><sch:value-of select="$loc/strings/report.M44.spec/div"/>
                <sch:value-of select="$specification_title"/>, (<sch:value-of select="$specification_date"/>, <sch:value-of select="$specification_dateType"/>)
            </sch:report>
            <sch:report test="$degree">
                <sch:value-of select="$loc/strings/report.M44.degree/div"/>
                <sch:value-of select="$degree"/>
            </sch:report>
        </sch:rule>
    </sch:pattern>

    <sch:pattern>
        <sch:title>$loc/strings/constraints</sch:title>

        <sch:rule context="//gmd:MD_DataIdentification|
			//*[@gco:isoType='gmd:MD_DataIdentification']|
			//srv:SV_ServiceIdentification|
			//*[@gco:isoType='srv:SV_ServiceIdentification']">

            <sch:assert test="count(gmd:resourceConstraints/*) > 0" see="geonet:child[@name='resourceConstraints']/@uuid">
                <sch:value-of select="$loc/strings/alert.M45.rc/div"/>
            </sch:assert>

            <!-- cardinality of accessconstraints is [1..n] -->
            <sch:let name="accessConstraints_node" value="gmd:resourceConstraints/*/gmd:accessConstraints/node()"/>
            <sch:let name="accessConstraints_count" value="count(gmd:resourceConstraints/*/gmd:accessConstraints[*/@codeListValue != ''])"/>
            <sch:let name="accessConstraints_found" value="$accessConstraints_count > 0"/>

            <!-- If the value of accessConstraints is otherRestrictions
                there shall be instances of otherConstraints expressing
                limitations on public access. This is because the
                limitations on public access required by the INSPIRE
                Directive may need the use of free text, and
                otherConstraints is the only element allowing this data
                type
            -->
            <sch:let name="accessConstraints"
                value="
				count(gmd:resourceConstraints/*/gmd:accessConstraints/gmd:MD_RestrictionCode[@codeListValue='otherRestrictions'])&gt;0
				and (
				not(gmd:resourceConstraints/*/gmd:otherConstraints)
				or gmd:resourceConstraints/*/gmd:otherConstraints[@gco:nilReason='missing']
				)"/>
            <sch:let name="otherConstraints"
                value="
				gmd:resourceConstraints/*/gmd:otherConstraints and
				gmd:resourceConstraints/*/gmd:otherConstraints/gco:CharacterString!='' and
				count(gmd:resourceConstraints/*/gmd:accessConstraints/gmd:MD_RestrictionCode[@codeListValue='otherRestrictions'])=0
				"/>
            <sch:let name="otherConstraintInfo"
                value="gmd:resourceConstraints/*/gmd:otherConstraints/gco:CharacterString"/>

            <sch:assert test="$accessConstraints_node" see="geonet:child[@name='resourceConstraints']/@uuid">
                <sch:value-of select="$loc/strings/alert.M45.ca/div"/>
            </sch:assert>

            <sch:assert test="$accessConstraints_found or not($accessConstraints_node)" see="gmd:resourceConstraints/*/gmd:accessConstraints/geonet:element/@ref">
                <sch:value-of select="$loc/strings/alert.M45.ca/div"/>
            </sch:assert>
            <sch:report test="$accessConstraints_found">
                <sch:value-of select="$accessConstraints_count"/> <sch:value-of select="$loc/strings/report.M45.ca/div"/>
            </sch:report>

            <sch:assert test="not($accessConstraints)" see="gmd:resourceConstraints/*/gmd:accessConstraints/geonet:element/@ref">
                <sch:value-of select="$loc/strings/alert.M45.or/div"/>
            </sch:assert>
            <sch:assert test="not($otherConstraints)" see="gmd:resourceConstraints/*/gmd:accessConstraints/geonet:element/@ref">
                <sch:value-of select="$loc/strings/alert.M45.or/div"/>
            </sch:assert>
            <sch:report test="$otherConstraintInfo!='' and not($accessConstraints) and not($otherConstraints)">
                <sch:value-of select="$loc/strings/report.M45.or/div"/>
                <sch:value-of select="$otherConstraintInfo"/>
            </sch:report>

        </sch:rule>

        <sch:rule context="//gmd:MD_DataIdentification/gmd:resourceConstraints/*|
			//*[@gco:isoType='gmd:MD_DataIdentification']/gmd:resourceConstraints/*|
			//srv:SV_ServiceIdentification/gmd:resourceConstraints/*|
			//*[@gco:isoType='srv:SV_ServiceIdentification']/gmd:resourceConstraints/*">
            <sch:let name="accessConstraints" value="string-join(gmd:accessConstraints/*/@codeListValue, ', ')"/>
            <sch:let name="classification" value="string-join(gmd:classification/*/@codeListValue, ', ')"/>
            <sch:let name="otherConstraints" value="gmd:otherConstraints/gco:CharacterString/text()"/>
            <sch:report test="$accessConstraints!=''">
                <sch:value-of select="$loc/strings/report.M45.ac/div"/>
                <sch:value-of select="$accessConstraints"/>
            </sch:report>
            <sch:report test="$classification!=''">
                <sch:value-of select="$loc/strings/report.M45.class/div"/>
                <sch:value-of select="$classification"/>
            </sch:report>
        </sch:rule>
    </sch:pattern>

    <sch:pattern>
        <sch:title>$loc/strings/constraints</sch:title>
        <sch:rule context="//gmd:MD_DataIdentification|
			//*[@gco:isoType='gmd:MD_DataIdentification']|
			//srv:SV_ServiceIdentification|
			//*[@gco:isoType='srv:SV_ServiceIdentification']">
            <sch:let name="useLimitation" value="gmd:resourceConstraints/*/gmd:useLimitation/*/text()"/>
            <sch:let name="useLimitation_count" value="count(gmd:resourceConstraints/*/gmd:useLimitation/*/text())"/>

           <sch:assert test="count(gmd:resourceConstraints/*) > 0" see="geonet:child[@name='resourceConstraints']/@uuid">
               <sch:value-of select="$loc/strings/alert.M45/div"/>
           </sch:assert>

            <sch:let name="useLimitationDefined" value="gmd:resourceConstraints/*/gmd:useLimitation/node()"/>
            <sch:assert test="$useLimitationDefined" see="gmd:resourceConstraints/*/geonet:child[@name='useLimitation']/@uuid">
                <sch:value-of select="$loc/strings/alert.M45/div"/>
            </sch:assert>

            <sch:assert test="$useLimitation_count or not($useLimitationDefined)" see="gmd:resourceConstraints/*/gmd:useLimitation/geonet:element/@ref">
                <sch:value-of select="$loc/strings/alert.M45/div"/>
            </sch:assert>

            <sch:report test="$useLimitation_count">
                <sch:value-of select="$loc/strings/report.M45/div"/>
                <sch:value-of select="$useLimitation"/>
            </sch:report>
        </sch:rule>
    </sch:pattern>

    <sch:pattern>
        <sch:title>$loc/strings/org</sch:title>

        <sch:rule context="//gmd:identificationInfo">
            <sch:let name="missing" value="not(*/gmd:pointOfContact)"/>
            <sch:assert
                test="not($missing)" see="gmd:MD_DataIdentification/geonet:child[@name='pointOfContact']/@uuid"
                ><sch:value-of select="$loc/strings/alert.M47/div"/></sch:assert>
            <sch:report
                test="not($missing)"
                ><sch:value-of select="$loc/strings/report.M47/div"/>
            </sch:report>
        </sch:rule>

        <sch:rule context="//gmd:identificationInfo/*/gmd:pointOfContact
			|//*[@gco:isoType='gmd:MD_DataIdentification']/gmd:pointOfContact
			|//*[@gco:isoType='srv:SV_ServiceIdentification']/gmd:pointOfContact">
            <sch:let name="missing" value="not(*/gmd:organisationName)
				or (*/gmd:organisationName/@gco:nilReason)
				or not(*/gmd:contactInfo/*/gmd:address/*/gmd:electronicMailAddress)
				or (*/gmd:contactInfo/*/gmd:address/*/gmd:electronicMailAddress/@gco:nilReason)"/>
            <sch:let name="organisationName" value="*/gmd:organisationName/*/text()"/>
            <sch:let name="role" value="normalize-space(*/gmd:role/*/@codeListValue)"/>
            <sch:let name="emptyRole" value="$role=''"/>
            <sch:let name="emailAddress" value="*/gmd:contactInfo/*/gmd:address/*/gmd:electronicMailAddress/*/text()"/>

            <sch:assert
                test="not($missing)"
                ><sch:value-of select="$loc/strings/alert.M47.info/div"/></sch:assert>
            <sch:assert
                test="not($emptyRole)"
                ><sch:value-of select="$loc/strings/alert.M48.role/div"/></sch:assert>
            <sch:report
                test="not($missing)"
                ><sch:value-of select="$loc/strings/report.M47.info/div"/>
                <sch:value-of select="$organisationName"/>
                (<sch:value-of select="$role"/>)
            </sch:report>
        </sch:rule>

    </sch:pattern>


    <sch:pattern>
        <sch:title>$loc/strings/metadata</sch:title>
        <sch:rule context="//gmd:MD_Metadata">
            <!--  Date stamp -->
            <sch:let name="dateStamp" value="gmd:dateStamp/*/text()"/>
            <sch:assert test="$dateStamp">
                <sch:value-of select="$loc/strings/alert.M50/div"/>
            </sch:assert>
            <sch:report test="$dateStamp">
                <sch:value-of select="$loc/strings/report.M50/div"/>
                <sch:value-of select="$dateStamp"/>
            </sch:report>


            <!--  Language -->
            <sch:let name="language" value="gmd:language/gco:CharacterString|gmd:language/gmd:LanguageCode/@codeListValue"/>
            <sch:let name="language_present" value="geonet:contains-any-of($language,
				('eng', 'fre', 'ger', 'spa', 'dut', 'ita', 'cze', 'lav', 'dan', 'lit', 'mlt',
				'pol', 'est', 'por', 'fin', 'rum', 'slo', 'slv', 'gre', 'bul',
				'hun', 'swe', 'gle'))"/>

            <sch:assert test="$language_present" see="gmd:language/geonet:element/@ref">
                <sch:value-of select="$loc/strings/alert.M49/div"/>
            </sch:assert>
            <sch:report test="$language_present">
                <sch:value-of select="$loc/strings/report.M49/div"/>
                <sch:value-of select="normalize-space($language)"/>
            </sch:report>


            <!--  Contact -->
            <sch:let name="missing" value="not(gmd:contact)"/>
            <sch:assert
                test="not($missing)"
                ><sch:value-of select="$loc/strings/alert.M48/div"/></sch:assert>
            <sch:report
                test="not($missing)"
                ><sch:value-of select="$loc/strings/report.M48/div"/>
            </sch:report>
        </sch:rule>

        <sch:rule context="//gmd:MD_Metadata/gmd:contact">
            <sch:let name="missing" value="not(gmd:CI_ResponsibleParty/gmd:organisationName)
				or (gmd:CI_ResponsibleParty/gmd:organisationName/@gco:nilReason)
				or not(gmd:CI_ResponsibleParty/gmd:contactInfo/*/gmd:address/*/gmd:electronicMailAddress)
				or (gmd:CI_ResponsibleParty/gmd:contactInfo/*/gmd:address/*/gmd:electronicMailAddress/@gco:nilReason)"/>
            <sch:let name="organisationName" value="gmd:CI_ResponsibleParty/gmd:organisationName/*/text()"/>
            <!--
                2.11.1 "The role of the responsible party serving as a metadata
                point of contact is out of scope of the INSPIRE
                Implementing Rules, but this property is mandated by ISO
                19115. The default value is pointOfContact."
                JRC schematron 1.0 validate only if role=pointOfContact
            -->
            <sch:let name="role" value="normalize-space(gmd:CI_ResponsibleParty/gmd:role/*/@codeListValue)"/>
            <sch:let name="emptyRole" value="$role=''"/>
            <sch:let name="emailAddress" value="gmd:CI_ResponsibleParty/gmd:contactInfo/*/gmd:address/*/gmd:electronicMailAddress/*/text()"/>

            <sch:assert
                test="not($emptyRole)"
                ><sch:value-of select="$loc/strings/alert.M48.role/div"/></sch:assert>
            <sch:assert
                test="not($missing)"
                ><sch:value-of select="$loc/strings/alert.M48.info/div"/></sch:assert>
            <sch:report
                test="not($missing)"
                ><sch:value-of select="$loc/strings/report.M48.info/div"/>
                <sch:value-of select="$organisationName"/>
                (<sch:value-of select="$role"/>)
            </sch:report>
        </sch:rule>

    </sch:pattern>

    <!-- INSPIRE metadata rules / END -->

</sch:schema>
