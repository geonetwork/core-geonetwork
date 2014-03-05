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
        <sch:rule context="//gmd:dataQualityInfo/*/gmd:report/*/gmd:result">
            <sch:let name="degree" value="*/gmd:pass/*/text()"/>
            <sch:let name="lang" value="normalize-space(/*/gmd:language)" />
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

            <sch:let name="specification_title" value="*/gmd:specification/*/gmd:title//text()[(string-length(.) > 0) and (../name() = 'gco:CharacterString' or ../@locale = $langCode)]" />
            <sch:let name="has_specification_title" value="$specification_title" />

            <sch:let name="specification_date" value="*/gmd:specification/*/gmd:date/*/gmd:date/*/text()[string-length(.) > 0]"/>
            <sch:let name="specification_dateType" value="normalize-space(*/gmd:specification/*/gmd:date/*/gmd:dateType/*/@codeListValue)"/>
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
            <sch:let name="hasDeTitle" value="$isDeMetadata and $specification_title[lower-case(normalize-space(.)) = $allTitles//ger/text()]"/>

            <sch:let name="isEnMetadata" value="$lang = 'eng'"/>
            <sch:let name="hasEnTitle" value="$isEnMetadata and $specification_title[lower-case(normalize-space(.)) = $allTitles//eng/text()]"/>

            <sch:let name="isFrMetadata" value="$lang = 'fre'"/>
            <sch:let name="hasFrTitle" value="$isFrMetadata and $specification_title[lower-case(normalize-space(.)) = $allTitles//fre/text()]"/>

            <sch:let name="isItMetadata" value="$lang = 'ita'"/>
            <sch:let name="hasItTitle" value="$isItMetadata and $specification_title[lower-case(normalize-space(.)) = $allTitles//ita/text()]"/>

            <sch:let name="isEsMetadata" value="$lang = 'spa'"/>
            <sch:let name="hasEsTitle" value="$isEsMetadata and $specification_title[lower-case(normalize-space(.)) = $allTitles//spa/text()]"/>

            <sch:let name="isFiMetadata" value="$lang = 'fin'"/>
            <sch:let name="hasFiTitle" value="$isFiMetadata and $specification_title[lower-case(normalize-space(.)) = $allTitles//fin/text()]"/>

            <sch:let name="isNlMetadata" value="$lang = 'dut'"/>
            <sch:let name="hasNlTitle" value="$isNlMetadata and $specification_title[lower-case(normalize-space(.)) = $allTitles//dut/text()]"/>

            <sch:let name="correctTitle" value="$allTitles//*[name() = $lang]/text()"/>
            <sch:assert test="$hasDeTitle or $hasEnTitle or $hasFrTitle or $hasItTitle or
                              $hasEsTitle or $hasFiTitle or $hasNlTitle">
                <sch:value-of select="$loc/strings/assert.M44.conformityActual/div"/>

                <sch:value-of select="concat('''', normalize-space($specification_title[1]), '''')"/>

                <sch:value-of select="$loc/strings/assert.M44.conformityExpected/div"/>

                <sch:value-of select="concat('''', $correctTitle, '''')"/>

            </sch:assert>
            <sch:assert test="$has_specification_title">
                <sch:value-of select="$loc/strings/assert.M44.title/div"/>
            </sch:assert>
            <sch:assert test="$specification_date and $specification_dateType">
                <sch:value-of select="$loc/strings/assert.M44.date/div"/>
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
