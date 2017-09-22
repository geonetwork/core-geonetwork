<?xml version="1.0" encoding="UTF-8"?>
<!--
    EMODNET BATHYMETRY migration process
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:geonet="http://www.fao.org/geonetwork"
    xmlns:xlink="http://www.w3.org/1999/xlink"
    xmlns:gco="http://www.isotc211.org/2005/gco"
    xmlns:gmd="http://www.isotc211.org/2005/gmd"
    xmlns:gmx="http://www.isotc211.org/2005/gmx"
    version="2.0"
    exclude-result-prefixes="#all">

    <xsl:output indent="yes"/>

    <!-- File identifier : replace by _ for DOI-->
    <xsl:template match="gmd:fileIdentifier/gco:CharacterString">
      <gco:CharacterString>
        <xsl:value-of select="replace(., ':', '_')"/>
      </gco:CharacterString>
    </xsl:template>

    <!--
        Hierarchylevel is now only a label
         <gmd:hierarchyLevelName>
         <gmx:Anchor xlink:href="http://www.seadatanet.org/urnurl/SDN:L231:6:CPRD">SDN:L231:6:CPRD = Composite Product Record</gmx:Anchor>
         </gmd:hierarchyLevelName>
    -->
    <xsl:template match="gmd:hierarchyLevelName/gmx:Anchor">
        <gco:CharacterString>
            <xsl:value-of select="substring-after(., ' = ')"/>
        </gco:CharacterString>
    </xsl:template>

    <xsl:template match="gmd:metadataStandardName/gco:CharacterString">
        <xsl:copy>
            ISO 19115:2003/19139 - EMODNET - BATHYMETRY
        </xsl:copy>
    </xsl:template>


    <!-- All hardcoded ids are now coming from thesaurus. Remove id prefix-->
    <xsl:template match="gco:CharacterString[
                                starts-with(., 'SDN:') and
                                not(ancestor::gmd:fileIdentifier)]|
                        @uom[starts-with(., 'SDN:')]">
        <xsl:copy>
            <xsl:value-of select="substring-after(., ' = ')"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="@uom" priority="200">
        <xsl:attribute name="uom">
            <xsl:value-of select="substring-after(., ' = ')"/>
        </xsl:attribute>
    </xsl:template>

    <!-- Set thesaurus depending on original list -->
    <xsl:template match="gmd:descriptiveKeywords/*[count(gmd:keyword/*[starts-with(text(), 'SDN:L05')]) > 0]/gmd:thesaurusName">
        <gmd:thesaurusName>
            <gmd:CI_Citation>
                <gmd:title>
                    <gco:CharacterString>Measuring devices</gco:CharacterString>
                </gmd:title>
                <gmd:date>
                    <gmd:CI_Date>
                        <gmd:date>
                            <gco:Date>2017-06-26</gco:Date>
                        </gmd:date>
                        <gmd:dateType>
                            <gmd:CI_DateTypeCode codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/ML_gmxCodelists.xml#CI_DateTypeCode"
                                codeListValue="publication"/>
                        </gmd:dateType>
                    </gmd:CI_Date>
                </gmd:date>
                <gmd:identifier>
                    <gmd:MD_Identifier>
                        <gmd:code>
                            <gmx:Anchor xmlns:gmx="http://www.isotc211.org/2005/gmx"
                                xlink:href="http://sextant.ifremer.fr/geonetwork/srv/eng/thesaurus.download?ref=external.theme.NVS.L05">geonetwork.thesaurus.external.theme.NVS.L05</gmx:Anchor>
                        </gmd:code>
                    </gmd:MD_Identifier>
                </gmd:identifier>
            </gmd:CI_Citation>
        </gmd:thesaurusName>
    </xsl:template>

    <xsl:template match="gmd:descriptiveKeywords/*[count(gmd:keyword/*[starts-with(text(), 'SDN:P021')]) > 0]/gmd:thesaurusName">
        <gmd:thesaurusName>
            <gmd:CI_Citation>
                <gmd:title>
                    <gco:CharacterString>Parameter Discovery Vocabulary (P02)</gco:CharacterString>
                </gmd:title>
                <gmd:date>
                    <gmd:CI_Date>
                        <gmd:date>
                            <gco:Date>2016-09-01</gco:Date>
                        </gmd:date>
                        <gmd:dateType>
                            <gmd:CI_DateTypeCode codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/ML_gmxCodelists.xml#CI_DateTypeCode"
                                codeListValue="publication"/>
                        </gmd:dateType>
                    </gmd:CI_Date>
                </gmd:date>
                <gmd:identifier>
                    <gmd:MD_Identifier>
                        <gmd:code>
                            <gmx:Anchor xmlns:gmx="http://www.isotc211.org/2005/gmx"
                                xlink:href="http://sextant.ifremer.fr/geonetwork/srv/eng/thesaurus.download?ref=external.parameter.NVS.P02">geonetwork.thesaurus.external.parameter.NVS.P02</gmx:Anchor>
                        </gmd:code>
                    </gmd:MD_Identifier>
                </gmd:identifier>
            </gmd:CI_Citation>
        </gmd:thesaurusName>
    </xsl:template>



    <xsl:template match="gmd:descriptiveKeywords/*[normalize-space(gmd:thesaurusName/*/gmd:title/*) = 'external.theme.inspire-theme']/gmd:thesaurusName">
        <gmd:thesaurusName>
            <gmd:CI_Citation>
                <gmd:title>
                    <gco:CharacterString>GEMET - INSPIRE themes, version 1.0</gco:CharacterString>
                </gmd:title>
                <gmd:date>
                    <gmd:CI_Date>
                        <gmd:date>
                            <gco:Date>2009-09-22</gco:Date>
                        </gmd:date>
                        <gmd:dateType>
                            <gmd:CI_DateTypeCode codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/ML_gmxCodelists.xml#CI_DateTypeCode"
                                codeListValue="publication"/>
                        </gmd:dateType>
                    </gmd:CI_Date>
                </gmd:date>
                <gmd:identifier>
                    <gmd:MD_Identifier>
                        <gmd:code>
                            <gmx:Anchor xmlns:gmx="http://www.isotc211.org/2005/gmx"
                                xlink:href="http://sextant.ifremer.fr/geonetwork/srv/eng/thesaurus.download?ref=external.theme.inspire-theme">geonetwork.thesaurus.external.theme.inspire-theme</gmx:Anchor>
                        </gmd:code>
                    </gmd:MD_Identifier>
                </gmd:identifier>
            </gmd:CI_Citation>
        </gmd:thesaurusName>
    </xsl:template>

    <xsl:template match="gmd:descriptiveKeywords/*[normalize-space(gmd:thesaurusName/*/gmd:alternateTitle/*) = 'EDMERP']/gmd:thesaurusName">
     <gmd:thesaurusName>
         <gmd:CI_Citation>
             <gmd:title>
                 <gco:CharacterString>Project name</gco:CharacterString>
             </gmd:title>
             <gmd:date>
                 <gmd:CI_Date>
                     <gmd:date>
                         <gco:Date>2015-01-29</gco:Date>
                     </gmd:date>
                     <gmd:dateType>
                         <gmd:CI_DateTypeCode codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/ML_gmxCodelists.xml#CI_DateTypeCode"
                             codeListValue="publication"/>
                     </gmd:dateType>
                 </gmd:CI_Date>
             </gmd:date>
             <gmd:identifier>
                 <gmd:MD_Identifier>
                     <gmd:code>
                         <gmx:Anchor xmlns:gmx="http://www.isotc211.org/2005/gmx"
                             xlink:href="http://localhost:8080/geonetwork/srv/eng/thesaurus.download?ref=local.theme.emodnet-bathymetry.projectname">geonetwork.thesaurus.local.theme.emodnet-bathymetry.projectname</gmx:Anchor>
                     </gmd:code>
                 </gmd:MD_Identifier>
             </gmd:identifier>
         </gmd:CI_Citation>
     </gmd:thesaurusName>
    </xsl:template>



    <!-- Move old shoal bias -->
    <xsl:template match="gmd:report/gmd:DQ_NonQuantitativeAttributeAccuracy[gmd:nameOfMeasure/* = 'shoal bias']">
        <xsl:variable name="bias"
                      select="gmd:measureDescription/*"/>

        <xsl:copy>
            <gmd:result>
                <gmd:DQ_ConformanceResult>
                    <gmd:specification>
                        <gmd:CI_Citation>
                            <gmd:title>
                                <gco:CharacterString>Shoal bias</gco:CharacterString>
                            </gmd:title>
                            <gmd:date>
                                <gmd:CI_Date>
                                    <gmd:date>
                                        <gco:Date/>
                                    </gmd:date>
                                    <gmd:dateType>
                                        <gmd:CI_DateTypeCode codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/ML_gmxCodelists.xml#CI_DateTypeCode"
                                            codeListValue=""/>
                                    </gmd:dateType>
                                </gmd:CI_Date>
                            </gmd:date>
                        </gmd:CI_Citation>
                    </gmd:specification>
                    <gmd:explanation gco:nilReason="missing">
                        <gco:CharacterString/>
                    </gmd:explanation>
                    <gmd:pass>
                        <gco:Boolean>
                            <xsl:choose>
                                <xsl:when test="$bias = 'yes'">true</xsl:when>
                                <xsl:when test="$bias = 'no'">false</xsl:when>
                                <xsl:otherwise></xsl:otherwise>
                            </xsl:choose>
                        </gco:Boolean>
                    </gmd:pass>
                </gmd:DQ_ConformanceResult>
            </gmd:result>
        </xsl:copy>
    </xsl:template>


    <xsl:template match="gmd:CI_ResponsibleParty[starts-with(gmd:organisationName/gco:CharacterString,
        'SDN:EDMO::EDMO')]">
        <xsl:copy>
            <xsl:attribute name="uuid">
                <xsl:value-of select="substring-after(gmd:organisationName/gco:CharacterString, ' = ')"/>
            </xsl:attribute>
            <xsl:apply-templates select="*"/>
        </xsl:copy>
    </xsl:template>


    <!-- Store language in gmd:LanguageCode instead of gco:CharacterString (INSPIRE requirements). -->
    <xsl:template match="gmd:language[gco:CharacterString]" priority="2">
        <xsl:copy>
            <gmd:LanguageCode codeList="http://www.loc.gov/standards/iso639-2/"
                codeListValue="{gco:CharacterString}"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="gmd:graphicOverview[
            */gmd:fileDescription/gco:CharacterString = 'thumbnail' and
            count(../gmd:graphicOverview[*/gmd:fileDescription/gco:CharacterString = 'large_thumbnail']) > 0]"
                  priority="2"/>

    <xsl:template match="gmd:graphicOverview/*/gmd:fileDescription"
                  priority="2"/>
    <xsl:template match="gmd:graphicOverview/*/gmd:fileType"
                  priority="2"/>

    <!-- Do a copy of every nodes and attributes -->
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <!-- Remove geonet:* elements. -->
    <xsl:template match="geonet:*" priority="2"/>

</xsl:stylesheet>
