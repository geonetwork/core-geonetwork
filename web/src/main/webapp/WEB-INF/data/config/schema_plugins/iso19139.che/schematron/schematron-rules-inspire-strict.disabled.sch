<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<sch:schema xmlns:sch="http://purl.oclc.org/dsdl/schematron"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    queryBinding="xslt2">
    <!--
        For certain inspire rules in the schematron-rules-inspire schematron, only a report/warning is issued.  This schematron
        provides assertions for those rules so they will be reported as errors.  The rules are backwards engineered from
        the inspire validator:

        http://inspire-geoportal.ec.europa.eu/validator2/
    -->

    <sch:title xmlns="http://www.w3.org/2001/XMLSchema">INSPIRE Strict Validation rules</sch:title>
    <sch:ns prefix="gml" uri="http://www.opengis.net/gml"/>
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
        <sch:rule context="//gmd:dataQualityInfo/*/gmd:report/*/gmd:result/*">
            <sch:let name="degree" value="gmd:pass/*/text()"/>
            <sch:let name="specification_title" value="gmd:specification/*/gmd:title//text()"/>
            <sch:let name="has_specification_title" value="gmd:specification/*/gmd:title//text()[string-length(.) > 0]"/>
            <sch:let name="specification_date" value="gmd:specification/*/gmd:date/*/gmd:date/*/text()[string-length(.) > 0]"/>
            <sch:let name="specification_dateType" value="normalize-space(gmd:specification/*/gmd:date/*/gmd:dateType/*/@codeListValue)"/>

            <sch:let name="deTitle" value="'verordnung (eg) nr. 1089/2010 der kommission vom 23. november 2010 zur durchführung der richtlinie 2007/2/eg des europäischen parlaments und des rates hinsichtlich der interoperabilität von geodatensätzen und -diensten'"/>
            <sch:let name="enTitle" value="'commission regulation (eu) no 1089/2010 of 23 november 2010 amending regulation (ec) no 976/2009 as regards download services and transformation services'"/>
            <sch:let name="frTitle" value="'règlement (ue) n ° 1088/2010 de la commission du 23 novembre 2010 modifiant le règlement (ce) n ° 976/2009 en ce qui concerne les services de téléchargement et les services de transformation'"/>
            <sch:let name="itTitle" value="'regolamento (ue) n. 1088/2010 della commissione, del 23 novembre 2010 , che modifica il regolamento (ce) n. 976/2009 per quanto riguarda i servizi di scaricamento e di conversione'"/>
            <sch:let name="esTitle" value="'reglamento (ue) n ° 1088/2010 de la comisión, de 23 de noviembre de 2010 , por el que se modifica el reglamento (ce) n ° 976/2009 en lo que se refiere a los servicios de descarga y a los servicios de transformación'"/>
            <sch:let name="fiTitle" value="'komission asetus (eu) n:o 1088/2010, annettu 23 päivänä marraskuuta 2010 , asetuksen (ey) n:o 976/2009 muuttamisesta latauspalvelujen ja muunnospalvelujen osalta'"/>
            <sch:let name="nlTitle" value="'verordening (eu) nr. 1088/2010 van de commissie van 23 november 2010 houdende wijziging van verordening (eg) nr. 976/2009 wat betreft downloaddiensten en transformatiediensten'"/>

            <sch:let name="isDeMetadata" value="normalize-space(/*/gmd:language) = 'ger'"/>
            <sch:let name="hasDeTitle" value="$isDeMetadata and $specification_title[lower-case(normalize-space(.)) = $deTitle]"/>

            <sch:let name="isEnMetadata" value="normalize-space(/*/gmd:language) = 'eng'"/>
            <sch:let name="hasEnTitle" value="$isEnMetadata and $specification_title[lower-case(normalize-space(.)) = $enTitle]"/>

            <sch:let name="isFrMetadata" value="normalize-space(/*/gmd:language) = 'fre'"/>
            <sch:let name="hasFrTitle" value="$isFrMetadata and $specification_title[lower-case(normalize-space(.)) = $frTitle]"/>

            <sch:let name="isItMetadata" value="normalize-space(/*/gmd:language) = 'ita'"/>
            <sch:let name="hasItTitle" value="$isItMetadata and $specification_title[lower-case(normalize-space(.)) = $itTitle]"/>

            <sch:let name="isEsMetadata" value="normalize-space(/*/gmd:language) = 'spa'"/>
            <sch:let name="hasEsTitle" value="$isEsMetadata and $specification_title[lower-case(normalize-space(.)) = $esTitle]"/>

            <sch:let name="isFiMetadata" value="normalize-space(/*/gmd:language) = 'fin'"/>
            <sch:let name="hasFiTitle" value="$isFiMetadata and $specification_title[lower-case(normalize-space(.)) = $fiTitle]"/>

            <sch:let name="isNlMetadata" value="normalize-space(/*/gmd:language) = 'dut'"/>
            <sch:let name="hasNlTitle" value="$isNlMetadata and $specification_title[lower-case(normalize-space(.)) = $nlTitle]"/>

            <sch:assert test="$hasDeTitle or $hasEnTitle or $hasFrTitle or $hasItTitle or
                              $hasEsTitle or $hasFiTitle or $hasNlTitle">
                <sch:value-of select="$specification_title"/>
                <!--<sch:value-of select="$loc/strings/report.M44.spec/div"/>-->
            </sch:assert>
            <sch:assert test="$has_specification_title">
                <sch:value-of select="$loc/strings/report.M44.spec/div"/>
            </sch:assert>
            <sch:assert test="$specification_date and $specification_dateType">
                <sch:value-of select="$loc/strings/report.M44.spec/div"/>
            </sch:assert>
            <sch:report test="$has_specification_title"><sch:value-of select="$loc/strings/report.M44.spec/div"/>
                <sch:value-of select="$has_specification_title"/>, (<sch:value-of select="$specification_date"/>, <sch:value-of select="$specification_dateType"/>)
            </sch:report>
            <sch:report test="$degree">
                <sch:value-of select="$loc/strings/report.M44.degree/div"/>
                <sch:value-of select="$degree"/>
            </sch:report>
        </sch:rule>
    </sch:pattern>

</sch:schema>
