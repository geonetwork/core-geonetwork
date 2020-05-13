<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<sch:schema
    xmlns:sch="http://purl.oclc.org/dsdl/schematron"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    queryBinding="xslt2">
<!--

This Schematron define INSPIRE IR on metadata for Spatial Data Service (SDS)

    @author Emanuele Tajariol - GeoSolutions, 2016


This work is licensed under the Creative Commons Attribution 2.5 License. 
To view a copy of this license, visit 
    http://creativecommons.org/licenses/by/2.5/ 

or send a letter to:

Creative Commons, 
543 Howard Street, 5th Floor, 
San Francisco, California, 94105, 
USA.

    -->

    <sch:title xmlns="http://www.w3.org/2001/XMLSchema">INSPIRE SDS rules</sch:title>

    <sch:ns prefix="gml" uri="http://www.opengis.net/gml/3.2"/>
    <sch:ns prefix="gml320" uri="http://www.opengis.net/gml"/>
    <sch:ns prefix="gmd" uri="http://www.isotc211.org/2005/gmd"/>
    <sch:ns prefix="gmx" uri="http://www.isotc211.org/2005/gmx"/>
    <sch:ns prefix="srv" uri="http://www.isotc211.org/2005/srv"/>
    <sch:ns prefix="gco" uri="http://www.isotc211.org/2005/gco"/>
    <sch:ns prefix="geonet" uri="http://www.fao.org/geonetwork"/>
    <sch:ns prefix="skos" uri="http://www.w3.org/2004/02/skos/core#"/>
    <sch:ns prefix="xlink" uri="http://www.w3.org/1999/xlink"/>

    <!-- INSPIRE SDS metadata rules / START -->

    <sch:pattern>
        <sch:title>$loc/strings/precondition</sch:title>

        <sch:rule context="//gmd:MD_Metadata">

            <sch:report test="count(gmd:identificationInfo/srv:SV_ServiceIdentification)=0">
                <sch:value-of select="$loc/strings/precondition.noservice"/>
            </sch:report>

            <sch:report test="count(gmd:identificationInfo/srv:SV_ServiceIdentification)=1">
                <sch:value-of select="$loc/strings/precondition.service"/>
            </sch:report>

        </sch:rule>
    </sch:pattern>

    <!-- TG Requirement 4
    For each spatial data service, there shall be one and only one set of quality information (dataQualityInfo element) scoped to “service”. -->

    <sch:pattern>
        <sch:title>$loc/strings/tg4</sch:title>

        <sch:rule context="//gmd:MD_Metadata[gmd:identificationInfo/srv:SV_ServiceIdentification]">

            <sch:assert test="count(gmd:dataQualityInfo/gmd:DQ_DataQuality[gmd:scope/gmd:DQ_Scope/gmd:level/gmd:MD_ScopeCode/@codeListValue='service'])>0">
                <sch:value-of select="$loc/strings/tg4.missing"/>
            </sch:assert>
            <sch:assert test="count(gmd:dataQualityInfo/gmd:DQ_DataQuality[gmd:scope/gmd:DQ_Scope/gmd:level/gmd:MD_ScopeCode/@codeListValue='service'])&lt;2">
                <sch:value-of select="$loc/strings/tg4.toomany"/>
            </sch:assert>

            <sch:report test="count(gmd:dataQualityInfo/gmd:DQ_DataQuality[gmd:scope/gmd:DQ_Scope/gmd:level/gmd:MD_ScopeCode/@codeListValue='service'])=1">
                <sch:value-of select="$loc/strings/tg4.ok"/>
            </sch:report>

        </sch:rule>
    </sch:pattern>


    <!-- TG Requirement 5
         Category.
    -->

    <sch:pattern>
        <sch:title>$loc/strings/tg5.category</sch:title>

        <sch:rule context="//gmd:MD_Metadata[gmd:identificationInfo/srv:SV_ServiceIdentification]/gmd:dataQualityInfo/gmd:DQ_DataQuality[gmd:scope/gmd:DQ_Scope/gmd:level/gmd:MD_ScopeCode/@codeListValue='service']">

            <sch:let name="anchorExists" value="count(gmd:report/gmd:DQ_DomainConsistency/gmd:result/gmd:DQ_ConformanceResult/gmd:specification/gmd:CI_Citation/gmd:title/gmx:Anchor)>0"/>
            <sch:let name="anchorHref"  value="string(gmd:report/gmd:DQ_DomainConsistency/gmd:result/gmd:DQ_ConformanceResult/gmd:specification/gmd:CI_Citation/gmd:title/gmx:Anchor/@xlink:href)"/>
            <sch:let name="category"    value="lower-case(substring-after($anchorHref, 'http://inspire.ec.europa.eu/metadata-codelist/Category/' ))"/>
            <sch:let name="isValid"     value="contains('invocable|interoperable|harmonised', $category)"/>

            <sch:assert test="$anchorExists">
                <sch:value-of select="$loc/strings/tg5.category.noanchor"/>
            </sch:assert>
            <sch:report test="$anchorExists">
                <sch:value-of select="$loc/strings/tg5.category.anchorfound"/>
            </sch:report>

            <sch:assert test="not($anchorExists) or ($anchorExists and $isValid)">
                <sch:value-of select="$loc/strings/tg5.category.invalid"/>: <sch:value-of select="$category"/>
            </sch:assert>
            <sch:report test="$isValid">
                <sch:value-of select="$loc/strings/tg5.category.valid"/>: <sch:value-of select="$category"/>
            </sch:report>

        </sch:rule>
    </sch:pattern>

    <!-- TG Requirement 6
         The value of the pass element shall be set to true.
    -->

    <sch:pattern>
        <sch:title>$loc/strings/tg6</sch:title>

        <sch:rule context="//gmd:MD_Metadata[gmd:identificationInfo/srv:SV_ServiceIdentification]/gmd:dataQualityInfo/gmd:DQ_DataQuality[gmd:scope/gmd:DQ_Scope/gmd:level/gmd:MD_ScopeCode/@codeListValue='service']">

            <sch:let name="passExists" value="count(gmd:report/gmd:DQ_DomainConsistency/gmd:result/gmd:DQ_ConformanceResult/gmd:pass)>0"/>
            <sch:let name="boolExists" value="count(gmd:report/gmd:DQ_DomainConsistency/gmd:result/gmd:DQ_ConformanceResult/gmd:pass/gco:Boolean)>0"/>
            <sch:let name="pass"  value="gmd:report/gmd:DQ_DomainConsistency/gmd:result/gmd:DQ_ConformanceResult/gmd:pass/gco:Boolean/text()='true'"/>

            <sch:assert test="$passExists">
                <sch:value-of select="$loc/strings/tg6.nopass"/>
            </sch:assert>
            <sch:report test="$passExists">
                <sch:value-of select="$loc/strings/tg6.passfound"/>
            </sch:report>

            <sch:assert test="not($passExists) or ($passExists and $boolExists)">
                <sch:value-of select="$loc/strings/tg6.nobool"/>
            </sch:assert>
            <sch:report test="$boolExists">
                <sch:value-of select="$loc/strings/tg6.boolfound"/>
            </sch:report>

            <sch:assert test="not($boolExists) or ($boolExists and $pass)">
                <sch:value-of select="$loc/strings/tg6.badbool"/>
            </sch:assert>
            <sch:report test="$pass">
                <sch:value-of select="$loc/strings/tg6.passok"/>
            </sch:report>

        </sch:rule>
    </sch:pattern>


    <!-- TG Requirement 7
         Resource locator.
    -->

    <sch:pattern>
        <sch:title>$loc/strings/tg7.exists</sch:title>

        <sch:rule context="//gmd:MD_Metadata[gmd:identificationInfo/srv:SV_ServiceIdentification]">

            <sch:let name="anchorExists" 
                     value="count(gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource/gmd:description/gmx:Anchor)>0"/>
            <sch:let name="accessPointExists"
                     value="count(gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource/gmd:description/gmx:Anchor
                                [@xlink:href='http://inspire.ec.europa.eu/registry/metadata-codelist/ResourceLocatorDescription/accessPoint'])>0"/>

            <sch:assert test="$anchorExists">
                <sch:value-of select="$loc/strings/tg7.exists.noanchor"/>
            </sch:assert>
            <sch:report test="$anchorExists">
                <sch:value-of select="$loc/strings/tg7.exists.anchorfound"/>
            </sch:report>

            <sch:assert test="not($anchorExists) or ($anchorExists and $accessPointExists)">
                <sch:value-of select="$loc/strings/tg7.exists.noaccesspoint"/>
            </sch:assert>
            <sch:report test="$accessPointExists">
                <sch:value-of select="$loc/strings/tg7.exists.accesspointfound"/>
            </sch:report>

        </sch:rule>
    </sch:pattern>

    <sch:pattern>
        <sch:title>$loc/strings/tg7.check</sch:title>

        <sch:rule context="//gmd:MD_Metadata[gmd:identificationInfo/srv:SV_ServiceIdentification]/gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource/gmd:description/gmx:Anchor">

            <sch:let name="anchorHref"  value="string(@xlink:href)"/>
            <sch:let name="description" value="lower-case(substring-after($anchorHref, 'http://inspire.ec.europa.eu/registry/metadata-codelist/ResourceLocatorDescription/' ))"/>
            <sch:let name="isValid"     value="contains('accesspoint|endpoint', $description)"/>

            <sch:assert test="$isValid">
                <sch:value-of select="$loc/strings/tg7.check.invalid"/>: <sch:value-of select="$description"/>
            </sch:assert>
            <sch:report test="$isValid">
                <sch:value-of select="$loc/strings/tg7.check.valid"/>: <sch:value-of select="$description"/>
            </sch:report>

        </sch:rule>
    </sch:pattern>

    <!-- TG Requirement 8
         Technical specification.
    -->
    <sch:pattern>
        <sch:title>$loc/strings/tg8.exists</sch:title>

        <sch:rule context="//gmd:MD_Metadata[gmd:identificationInfo/srv:SV_ServiceIdentification]">

            <sch:let name="anchorExists"
                     value="count(gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:report/gmd:DQ_FormatConsistency/gmd:result/gmd:DQ_ConformanceResult/gmd:specification/gmd:CI_Citation/gmd:title/gmx:Anchor)>0"/>

            <sch:assert test="$anchorExists">
                <sch:value-of select="$loc/strings/tg8.exists.noanchor"/>
            </sch:assert>
            <sch:report test="$anchorExists">
                <sch:value-of select="$loc/strings/tg8.exists.anchorfound"/>
            </sch:report>

        </sch:rule>
    </sch:pattern>

    <!-- TG Requirement 10
         Coordinate Reference Systems Identifier
    -->
    <sch:pattern>
        <sch:title>$loc/strings/tg10.exists</sch:title>

        <sch:rule context="//gmd:MD_Metadata[gmd:identificationInfo/srv:SV_ServiceIdentification]">

            <sch:let name="anchorExists"
                     value="count(gmd:referenceSystemInfo/gmd:MD_ReferenceSystem/gmd:referenceSystemIdentifier/gmd:RS_Identifier/gmd:code/gmx:Anchor)>0"/>

            <sch:assert test="$anchorExists">
                <sch:value-of select="$loc/strings/tg10.exists.noanchor"/>
            </sch:assert>
            <sch:report test="$anchorExists">
                <sch:value-of select="$loc/strings/tg10.exists.anchorfound"/>
            </sch:report>

        </sch:rule>
    </sch:pattern>

    <!-- TG Requirement 12
         Quality of Service
    -->
    <sch:pattern>
        <sch:title>$loc/strings/tg12</sch:title>

        <sch:rule context="//gmd:MD_Metadata[gmd:identificationInfo/srv:SV_ServiceIdentification]">

            <sch:let name="availExists"
                     value="count(gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:report/gmd:DQ_ConceptualConsistency/gmd:nameOfMeasure/gmx:Anchor
                              [@xlink:href='http://inspire.ec.europa.eu/metadata-codelist/Criteria/availability'])>0"/>
            <sch:let name="perfExists"
                     value="count(gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:report/gmd:DQ_ConceptualConsistency/gmd:nameOfMeasure/gmx:Anchor
                              [@xlink:href='http://inspire.ec.europa.eu/metadata-codelist/Criteria/performance'])>0"/>
            <sch:let name="capaExists"
                     value="count(gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:report/gmd:DQ_ConceptualConsistency/gmd:nameOfMeasure/gmx:Anchor
                              [@xlink:href='http://inspire.ec.europa.eu/metadata-codelist/Criteria/capacity'])>0"/>


            <sch:assert test="$availExists"><sch:value-of select="$loc/strings/tg12.noavail"/></sch:assert>
            <sch:report test="$availExists"><sch:value-of select="$loc/strings/tg12.availfound"/></sch:report>

            <sch:assert test="$perfExists"><sch:value-of select="$loc/strings/tg12.noperf"/></sch:assert>
            <sch:report test="$perfExists"><sch:value-of select="$loc/strings/tg12.perffound"/></sch:report>

            <sch:assert test="$capaExists"><sch:value-of select="$loc/strings/tg12.nocapa"/></sch:assert>
            <sch:report test="$capaExists"><sch:value-of select="$loc/strings/tg12.capafound"/></sch:report>

        </sch:rule>
    </sch:pattern>

    <!-- TG Requirement 13
         Conditions applying to access and use
    -->

    <sch:pattern>
        <sch:title>$loc/strings/tg13.exists</sch:title>

        <sch:rule context="//gmd:MD_Metadata[gmd:identificationInfo/srv:SV_ServiceIdentification]">

            <sch:let name="exists"
                     value="count(.//gmd:resourceConstraints/gmd:MD_LegalConstraints)>0"/>

            <sch:assert test="$exists">
                <sch:value-of select="$loc/strings/tg13.exists.notfound"/>
            </sch:assert>
            <sch:report test="$exists">
                <sch:value-of select="$loc/strings/tg13.exists.found"/>
            </sch:report>

        </sch:rule>
    </sch:pattern>


    <sch:pattern>
        <sch:title>$loc/strings/tg13</sch:title>
        <sch:rule context="//gmd:MD_Metadata[gmd:identificationInfo/srv:SV_ServiceIdentification]/gmd:resourceConstraints/gmd:MD_LegalConstraints">

            <sch:let name="access" value="count(gmd:accessConstraints)>0"/>
            <sch:let name="use"  value="count(gmd:useConstraints)>0"/>
            <sch:let name="other"  value="count(gmd:otherConstraints)>0"/>

            <sch:let name="onlyone"  value="($access and not($use)) or (not($access) and $use)"/>

            <sch:let name="accesscode" value="count(gmd:accessConstraints/gmd:MD_RestrictionCode[@codeListValue='otherRestrictions'] )>0"/>
            <sch:let name="usecode" value="count(gmd:useConstraints/gmd:MD_RestrictionCode[@codeListValue='otherRestrictions'] )>0"/>

            <sch:assert test="$other"><sch:value-of select="$loc/strings/tg13.othermissing"/></sch:assert>
            <sch:assert test="$onlyone"><sch:value-of select="$loc/strings/tg13.both"/></sch:assert>

            <!--<sch:assert test="not($onlyone) or ($onlyone and $access and $accesscode) or ($onlyone and $use and $usecode)"><sch:value-of select="$loc/strings/tg13.badcodelist"/></sch:assert>-->
            <sch:assert test="not($onlyone) or ($onlyone and ($accesscode or $usecode))"><sch:value-of select="$loc/strings/tg13.badcodelist"/></sch:assert>

            <sch:report test="$onlyone and $access and $other"><sch:value-of select="$loc/strings/tg13.accessfound"/></sch:report>
            <sch:report test="$onlyone and $use and $other"><sch:value-of select="$loc/strings/tg13.usefound"/></sch:report>

            <sch:report test="$onlyone and $access and $other and $accesscode"><sch:value-of select="$loc/strings/tg13.okaccess"/></sch:report>
            <sch:report test="$onlyone and $use and $other and $usecode"><sch:value-of select="$loc/strings/tg13.okuse"/></sch:report>

        </sch:rule>
    </sch:pattern>


    <!-- TG Requirement 15
         Conditions applying to access and use
    -->

    <sch:pattern>
        <sch:title>$loc/strings/tg15</sch:title>

        <sch:rule context="//gmd:MD_Metadata[gmd:identificationInfo/srv:SV_ServiceIdentification]">

            <sch:let name="existspoc"       value="count(gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:pointOfContact/gmd:CI_ResponsibleParty)>0"/>
            <sch:let name="existscustodian" value="count(gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:pointOfContact/gmd:CI_ResponsibleParty/gmd:role/gmd:CI_RoleCode[@codeListValue='custodian'])>0"/>

            <sch:assert test="$existspoc"><sch:value-of select="$loc/strings/tg15.nopoc"/></sch:assert>
            <sch:assert test="not($existspoc) or ($existspoc and $existscustodian)"><sch:value-of select="$loc/strings/tg15.nocustodian"/></sch:assert>

            <sch:report test="$existscustodian"><sch:value-of select="$loc/strings/tg15.okcustodian"/></sch:report>
            
        </sch:rule>
    </sch:pattern>


    <!-- INSPIRE SDS metadata rules / END -->

</sch:schema>
