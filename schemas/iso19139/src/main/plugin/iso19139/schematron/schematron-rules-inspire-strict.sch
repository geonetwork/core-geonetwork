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
        For certain inspire rules in the schematron-rules-inspire schematron, only a report/warning is issued.  This schematron
        provides assertions for those rules so they will be reported as errors.  The rules are backwards engineered from
        the inspire validator:

        http://inspire-geoportal.ec.europa.eu/validator2/
    -->

    <sch:title xmlns="http://www.w3.org/2001/XMLSchema">INSPIRE Strict rules</sch:title>
    <sch:ns prefix="gml" uri="http://www.opengis.net/gml/3.2"/>
    <sch:ns prefix="gml320" uri="http://www.opengis.net/gml"/>
    <sch:ns prefix="gmd" uri="http://www.isotc211.org/2005/gmd"/>
    <sch:ns prefix="gmx" uri="http://www.isotc211.org/2005/gmx"/>
    <sch:ns prefix="srv" uri="http://www.isotc211.org/2005/srv"/>
    <sch:ns prefix="gco" uri="http://www.isotc211.org/2005/gco"/>
    <sch:ns prefix="geonet" uri="http://www.fao.org/geonetwork"/>
    <sch:ns prefix="skos" uri="http://www.w3.org/2004/02/skos/core#"/>
    <sch:ns prefix="xlink" uri="http://www.w3.org/1999/xlink"/>


    <!-- Make a non blocker conformity check operation - no assertion here -->
    <sch:pattern>
        <sch:title>$loc/strings/conformity</sch:title>
        <!-- Check specification names and status -->
        <sch:rule context="//gmd:dataQualityInfo/*[name() != 'geonet:element']">

            <sch:let name="lang" value="normalize-space(/*/gmd:language/gco:CharacterString|/*/gmd:language/gmd:LanguageCode/@codeListValue)" />
            <sch:let name="langCodeMap">
                <ger>#DE</ger>
                <eng>#EN</eng>
                <fre>#FR</fre>
                <ita>#IT</ita>
                <spa>#ES</spa>
                <fin>#FI</fin>
                <dut>#NL</dut>
            </sch:let>
            <sch:let name="langCode" value="normalize-space($langCodeMap//*[name() = $lang])" />

            <sch:let name="specification_title" value="gmd:report/*/gmd:result/*/gmd:specification/*/gmd:title//text()[(string-length(.) > 0) and (../name() = 'gco:CharacterString' or ../@locale = $langCode)]" />
            <sch:let name="has_specification_title" value="count($specification_title) > 0" />

            <sch:let name="allTitles">
                <titles>
                    <ger>verordnung (eg) nr. 1089/2010 der kommission vom 23. november 2010 zur durchführung der richtlinie 2007/2/eg des europäischen parlaments und des rates hinsichtlich der interoperabilität von geodatensätzen und -diensten</ger>
                    <eng>commission regulation (eu) no 1089/2010 of 23 november 2010 implementing directive 2007/2/ec of the european parliament and of the council as regards interoperability of spatial data sets and services</eng>
                    <fre>règlement (ue) n o 1089/2010 de la commission du 23 novembre 2010 portant modalités d'application de la directive 2007/2/ce du parlement européen et du conseil en ce qui concerne l'interopérabilité des séries et des services de données géographiques</fre>
                    <ita>regolamento (ue) n . 1089/2010 della commissione del 23 novembre 2010 recante attuazione della direttiva 2007/2/ce del parlamento europeo e del consiglio per quanto riguarda l'interoperabilità dei set di dati territoriali e dei servizi di dati territoriali</ita>
                    <spa>reglamento (ue) n o 1089/2010 de la comisión de 23 de noviembre de 2010 por el que se aplica la directiva 2007/2/ce del parlamento europeo y del consejo en lo que se refiere a la interoperabilidad de los conjuntos y los servicios de datos espaciales</spa>
                    <fin>komission asetus (eu) n:o 1089/2010, annettu 23 päivänä marraskuuta 2010 , euroopan parlamentin ja neuvoston direktiivin 2007/2/ey täytäntöönpanosta paikkatietoaineistojen ja -palvelujen yhteentoimivuuden osalta</fin>
                    <dut>verordening (eu) n r. 1089/2010 van de commissie van 23 november 2010 ter uitvoering van richtlijn 2007/2/eg van het europees parlement en de raad betreffende de interoperabiliteit van verzamelingen ruimtelijke gegevens en van diensten met betrekking tot ruimtelijke gegevens</dut>
                </titles>
            </sch:let>
            <sch:let name="isDeMetadata" value="$lang = 'ger'"/>
            <sch:let name="isEnMetadata" value="$lang = 'eng'"/>
            <sch:let name="isFrMetadata" value="$lang = 'fre'"/>
            <sch:let name="isItMetadata" value="$lang = 'ita'"/>
            <sch:let name="isEsMetadata" value="$lang = 'spa'"/>
            <sch:let name="isFiMetadata" value="$lang = 'fin'"/>
            <sch:let name="isNlMetadata" value="$lang = 'dut'"/>

            <sch:let name="specification_inspire" value="gmd:report/*/gmd:result/*/gmd:specification[
                ($isDeMetadata and (lower-case(normalize-space(*/gmd:title//text()[(string-length(.) > 0) and (../name() = 'gco:CharacterString')]))  = $allTitles//ger/text()) or (lower-case(normalize-space(*/gmd:title//text()[(string-length(.) > 0) and (../@locale = $langCode)]))  = $allTitles//ger/text())) or
                ($isEnMetadata and (lower-case(normalize-space(*/gmd:title//text()[(string-length(.) > 0) and (../name() = 'gco:CharacterString')]))  = $allTitles//eng/text()) or (lower-case(normalize-space(*/gmd:title//text()[(string-length(.) > 0) and (../@locale = $langCode)]))  = $allTitles//eng/text())) or
                ($isFrMetadata and (lower-case(normalize-space(*/gmd:title//text()[(string-length(.) > 0) and (../name() = 'gco:CharacterString')]))  = $allTitles//fre/text()) or (lower-case(normalize-space(*/gmd:title//text()[(string-length(.) > 0) and (../@locale = $langCode)]))  = $allTitles//fre/text())) or
                ($isItMetadata and (lower-case(normalize-space(*/gmd:title//text()[(string-length(.) > 0) and (../name() = 'gco:CharacterString')]))  = $allTitles//ita/text()) or (lower-case(normalize-space(*/gmd:title//text()[(string-length(.) > 0) and (../@locale = $langCode)]))  = $allTitles//ita/text())) or
                ($isEsMetadata and (lower-case(normalize-space(*/gmd:title//text()[(string-length(.) > 0) and (../name() = 'gco:CharacterString')]))  = $allTitles//spa/text()) or (lower-case(normalize-space(*/gmd:title//text()[(string-length(.) > 0) and (../@locale = $langCode)]))  = $allTitles//spa/text())) or
                ($isFiMetadata and (lower-case(normalize-space(*/gmd:title//text()[(string-length(.) > 0) and (../name() = 'gco:CharacterString')]))  = $allTitles//fin/text()) or (lower-case(normalize-space(*/gmd:title//text()[(string-length(.) > 0) and (../@locale = $langCode)]))  = $allTitles//fin/text())) or
                ($isNlMetadata and (lower-case(normalize-space(*/gmd:title//text()[(string-length(.) > 0) and (../name() = 'gco:CharacterString')]))  = $allTitles//dut/text()) or (lower-case(normalize-space(*/gmd:title//text()[(string-length(.) > 0) and (../@locale = $langCode)]))  = $allTitles//dut/text()))
                ]" />

            <sch:let name="degree" value="$specification_inspire/../gmd:pass/*/text()"/>

            <sch:let name="correctTitle" value="$allTitles//*[name() = $lang]/text()"/>

            <sch:assert test="count($specification_inspire) > 0">
                <sch:value-of select="$loc/strings/assert.M44.conformityActual/div"/>

                <sch:value-of select="concat('''', normalize-space($specification_title[1]), '''')"/>

                <sch:value-of select="$loc/strings/assert.M44.conformityExpected/div"/>

                <sch:value-of select="concat('''', $correctTitle, '''')"/>

            </sch:assert>
            <sch:assert test="$has_specification_title">
                <sch:value-of select="$loc/strings/assert.M44.title/div"/>
            </sch:assert>

            <sch:let name="specification_date" value="$specification_inspire/*/gmd:date/*/gmd:date/*/text()[string-length(.) > 0]"/>
            <sch:let name="specification_dateType" value="normalize-space($specification_inspire/*/gmd:date/*/gmd:dateType/*/@codeListValue)"/>

            <!-- Ignore specification date checks if no inspire specification -->
            <sch:assert test="not($specification_inspire) or ($specification_date and $specification_dateType)">
                <sch:value-of select="$loc/strings/assert.M44.date/div"/>
            </sch:assert>

            <sch:report test="$has_specification_title">
                <sch:value-of select="$loc/strings/report.M44.spec/div"/>
                <sch:value-of select="$has_specification_title"/>, (<sch:value-of select="$specification_date"/>, <sch:value-of select="$specification_dateType"/>)
            </sch:report>
            <sch:report test="$degree">
                <sch:value-of select="$loc/strings/report.M44.degree/div"/>
                <sch:value-of select="$degree"/>
            </sch:report>
        </sch:rule>
    </sch:pattern>

</sch:schema>
