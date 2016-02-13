<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:exslt="http://exslt.org/common"
                xmlns:geonet="http://www.fao.org/geonetwork"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:gmx="http://www.isotc211.org/2005/gmx"
                xmlns:srv="http://www.isotc211.org/2005/srv"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:gn-fn-iso19139="http://geonetwork-opensource.org/xsl/functions/profiles/iso19139"
                xmlns:util="java:org.fao.geonet.util.XslUtil"
                version="2.0"
                exclude-result-prefixes="#all">

    <xsl:import href="../../iso19139/process/process-utility.xsl"/>

    <xsl:variable name="isService" select="boolean(/gmd:MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification)"/>

    <!-- CC2 4.5.2 Quality of Service -->
    <!-- Add gmd:DQ_ConceptualConsistency elements to express Quality of service in term of:
           * Availability
           * Performance
           * Capacity
    -->
        <xsl:template match="/gmd:MD_Metadata/gmd:dataQualityInfo/gmd:DQ_DataQuality">
            <xsl:variable name="qualityofservice">
                <gmd:report>
                    <gmd:DQ_ConceptualConsistency>
                        <gmd:nameOfMeasure>
                            <gmx:Anchor xlink:href="http://inspire.ec.europa.eu/metadata-codelist/Criteria/availability">availability</gmx:Anchor>
                        </gmd:nameOfMeasure>
                        <gmd:measureIdentification>
                            <gmd:MD_Identifier>
                                <gmd:code>
                                    <gco:CharacterString>INSPIRE_service_availability</gco:CharacterString>
                                </gmd:code>
                            </gmd:MD_Identifier>
                        </gmd:measureIdentification>
                        <gmd:result>
                            <gmd:DQ_QuantitativeResult>
                                <gmd:valueUnit xlink:href="http://www.opengis.net/def/uom/OGC/1.0/unity"/>
                                <gmd:value>
                                    <gco:Record>90</gco:Record>
                                </gmd:value>
                            </gmd:DQ_QuantitativeResult>
                        </gmd:result>
                    </gmd:DQ_ConceptualConsistency>
                </gmd:report>
                <gmd:report>
                    <gmd:DQ_ConceptualConsistency>
                        <gmd:nameOfMeasure>
                            <gmx:Anchor xlink:href="http://inspire.ec.europa.eu/metadata-codelist/Criteria/performance">performance</gmx:Anchor>
                        </gmd:nameOfMeasure>
                        <gmd:measureIdentification>
                            <gmd:MD_Identifier>
                                <gmd:code>
                                    <gco:CharacterString> INSPIRE_service_performance</gco:CharacterString>
                                </gmd:code>
                            </gmd:MD_Identifier>
                        </gmd:measureIdentification>
                        <gmd:result>
                            <gmd:DQ_QuantitativeResult>
                                <gmd:valueUnit xlink:href=" http://www.opengis.net/def/uom/SI/second"/>
                                <gmd:value>
                                    <gco:Record>0.5</gco:Record>
                                </gmd:value>
                            </gmd:DQ_QuantitativeResult>
                        </gmd:result>
                    </gmd:DQ_ConceptualConsistency>
                </gmd:report>
                <gmd:report>
                    <gmd:DQ_ConceptualConsistency>
                        <gmd:nameOfMeasure>
                            <gmx:Anchor xlink:href="http://inspire.ec.europa.eu/metadata-codelist/Criteria/capacity">capacity</gmx:Anchor>
                        </gmd:nameOfMeasure>
                        <gmd:measureIdentification>
                            <gmd:MD_Identifier>
                                <gmd:code>
                                    <gco:CharacterString>INSPIRE_service_capacity </gco:CharacterString>
                                </gmd:code>
                            </gmd:MD_Identifier>
                        </gmd:measureIdentification>
                        <gmd:result>
                            <gmd:DQ_QuantitativeResult>
                                <gmd:valueUnit gco:nilReason="inapplicable"/>
                                <gmd:value>
                                    <gco:Record>50</gco:Record>
                                </gmd:value>
                            </gmd:DQ_QuantitativeResult>
                        </gmd:result>
                    </gmd:DQ_ConceptualConsistency>
                </gmd:report>
            </xsl:variable>
            <xsl:variable name="report">
                <gmd:report><gmd:DQ_ConceptualConsistency /></gmd:report>
            </xsl:variable>
            <!-- xsl:message>0) <xsl:value-of select="count(gmd:scope/gmd:DQ_Scope/gmd:level/gmd:MD_ScopeCode/@codeListValue='service')"/></xsl:message>
            <xsl:message>1) <xsl:value-of select="count(/gmd:MD_Metadata/gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:report/gmd:DQ_ConceptualConsistency/gmd:nameOfMeasure/gmx:Anchor[text()='availability'])=0"/></xsl:message>
            <xsl:message>2) <xsl:value-of select="count(/gmd:MD_Metadata/gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:report/gmd:DQ_ConceptualConsistency/gmd:nameOfMeasure/gmx:Anchor[text()='performance'])"/></xsl:message>
            <xsl:message>3) <xsl:value-of select="count(/gmd:MD_Metadata/gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:report/gmd:DQ_ConceptualConsistency/gmd:nameOfMeasure/gmx:Anchor[text()='capacity'])"/></xsl:message -->
            <xsl:choose>
                <xsl:when test="count(gmd:scope/gmd:DQ_Scope/gmd:level/gmd:MD_ScopeCode/@codeListValue='service')=1 and
                              count(/gmd:MD_Metadata/gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:report/gmd:DQ_ConceptualConsistency/gmd:nameOfMeasure/gmx:Anchor[text()='availability'])=0 and
                              count(/gmd:MD_Metadata/gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:report/gmd:DQ_ConceptualConsistency/gmd:nameOfMeasure/gmx:Anchor[text()='performance'])=0 and
                              count(/gmd:MD_Metadata/gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:report/gmd:DQ_ConceptualConsistency/gmd:nameOfMeasure/gmx:Anchor[text()='capacity'])=0">
                    <xsl:copy>
                        <xsl:copy-of select="@*|node()"/>
                        <xsl:copy-of select="$qualityofservice" />
                    </xsl:copy>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:copy>
                        <xsl:copy-of select="@*|node()"/>
                    </xsl:copy>
                </xsl:otherwise>
            </xsl:choose>
            
    </xsl:template>

    <!-- Do a copy of every nodes and attributes -->
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
    
    <!-- Remove geonet:* elements. -->
    <xsl:template match="geonet:*" priority="2"/>

</xsl:stylesheet>
