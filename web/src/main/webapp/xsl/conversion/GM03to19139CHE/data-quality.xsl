<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:che="http://www.geocat.ch/2008/che"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gmi="http://www.isotc211.org/2005/gmi"
                xmlns:gml="http://www.opengis.net/gml"
                xmlns:java="http://xml.apache.org/xslt/java"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                exclude-result-prefixes="java"
                >

    <xsl:template match="GM03Core.Core.DQ_DataQuality" mode="DataQuality">
    <!--  if DataQuality is empty then don't add it as it makes the document invalid -->
        <xsl:if test="count(./node())>0">
            <gmd:dataQualityInfo>
                <gmd:DQ_DataQuality>
                    <xsl:apply-templates mode="DataQuality" select="GM03Comprehensive.Comprehensive.DQ_Scope|GM03Core.Core.DQ_Scope"/>
                    <xsl:apply-templates mode="DataQuality" select="GM03Comprehensive.Comprehensive.reportDQ_DataQuality"/>
                    <xsl:apply-templates mode="DataQuality" select="GM03Core.Core.LI_Lineage"/>
                </gmd:DQ_DataQuality>
            </gmd:dataQualityInfo>
        </xsl:if>
    </xsl:template>

    <xsl:template mode="DataQuality" match="GM03Comprehensive.Comprehensive.reportDQ_DataQuality">
        <xsl:apply-templates mode="DataQuality"/>
    </xsl:template>

    <xsl:template mode="DataQuality" match="report">
        <xsl:apply-templates mode="DataQuality"/>
    </xsl:template>


    <xsl:template mode="DataQuality" match="GM03Comprehensive.Comprehensive.DQ_TemporalValidity">
        <xsl:apply-templates mode="DQ_Element" select=".">
            <xsl:with-param name="reportName">DQ_TemporalValidity</xsl:with-param>
        </xsl:apply-templates>
    </xsl:template>

    <xsl:template mode="DataQuality" match="GM03Comprehensive.Comprehensive.DQ_TemporalConsistency">
        <xsl:apply-templates mode="DQ_Element" select=".">
            <xsl:with-param name="reportName">DQ_TemporalConsistency</xsl:with-param>
        </xsl:apply-templates>
    </xsl:template>

    <xsl:template mode="DataQuality" match="GM03Comprehensive.Comprehensive.DQ_AccuracyOfATimeMeasurement">
        <xsl:apply-templates mode="DQ_Element" select=".">
            <xsl:with-param name="reportName">DQ_AccuracyOfATimeMeasurement</xsl:with-param>
        </xsl:apply-templates>
    </xsl:template>

    <xsl:template mode="DataQuality" match="GM03Comprehensive.Comprehensive.DQ_QuantitativeAttributeAccuracy">
        <xsl:apply-templates mode="DQ_Element" select=".">
            <xsl:with-param name="reportName">DQ_QuantitativeAttributeAccuracy</xsl:with-param>
        </xsl:apply-templates>
    </xsl:template>

    <xsl:template mode="DataQuality" match="GM03Comprehensive.Comprehensive.DQ_NonQuantitativeAttributeAccuracy">
        <xsl:apply-templates mode="DQ_Element" select=".">
            <xsl:with-param name="reportName">DQ_NonQuantitativeAttributeAccuracy</xsl:with-param>
        </xsl:apply-templates>
    </xsl:template>

    <xsl:template mode="DataQuality" match="GM03Comprehensive.Comprehensive.DQ_ThematicClassificationCorrectness">
        <xsl:apply-templates mode="DQ_Element" select=".">
            <xsl:with-param name="reportName">DQ_ThematicClassificationCorrectness</xsl:with-param>
        </xsl:apply-templates>
    </xsl:template>

    <xsl:template mode="DataQuality" match="GM03Comprehensive.Comprehensive.DQ_RelativeInternalPositionalAccuracy">
        <xsl:apply-templates mode="DQ_Element" select=".">
            <xsl:with-param name="reportName">DQ_RelativeInternalPositionalAccuracy</xsl:with-param>
        </xsl:apply-templates>
    </xsl:template>

    <xsl:template mode="DataQuality" match="GM03Comprehensive.Comprehensive.DQ_GriddedDataPositionalAccuracy">
        <xsl:apply-templates mode="DQ_Element" select=".">
            <xsl:with-param name="reportName">DQ_GriddedDataPositionalAccuracy</xsl:with-param>
        </xsl:apply-templates>
    </xsl:template>

    <xsl:template mode="DataQuality" match="GM03Comprehensive.Comprehensive.DQ_AbsoluteExternalPositionalAccuracy">
        <xsl:apply-templates mode="DQ_Element" select=".">
            <xsl:with-param name="reportName">DQ_AbsoluteExternalPositionalAccuracy</xsl:with-param>
        </xsl:apply-templates>
    </xsl:template>

    <xsl:template mode="DataQuality" match="GM03Comprehensive.Comprehensive.DQ_TopologicalConsistency">
        <xsl:apply-templates mode="DQ_Element" select=".">
            <xsl:with-param name="reportName">DQ_TopologicalConsistency</xsl:with-param>
        </xsl:apply-templates>
    </xsl:template>

    <xsl:template mode="DataQuality" match="GM03Comprehensive.Comprehensive.DQ_FormatConsistency">
        <xsl:apply-templates mode="DQ_Element" select=".">
            <xsl:with-param name="reportName">DQ_FormatConsistency</xsl:with-param>
        </xsl:apply-templates>
    </xsl:template>

    <xsl:template mode="DataQuality" match="GM03Comprehensive.Comprehensive.DQ_DomainConsistency">
        <xsl:apply-templates mode="DQ_Element" select=".">
            <xsl:with-param name="reportName">DQ_DomainConsistency</xsl:with-param>
        </xsl:apply-templates>
    </xsl:template>

    <xsl:template mode="DataQuality" match="GM03Comprehensive.Comprehensive.DQ_ConceptualConsistency">
        <xsl:apply-templates mode="DQ_Element" select=".">
            <xsl:with-param name="reportName">DQ_ConceptualConsistency</xsl:with-param>
        </xsl:apply-templates>
    </xsl:template>

    <xsl:template mode="DataQuality" match="GM03Comprehensive.Comprehensive.DQ_CompletenessOmission">
        <xsl:apply-templates mode="DQ_Element" select=".">
            <xsl:with-param name="reportName">DQ_CompletenessOmission</xsl:with-param>
        </xsl:apply-templates>
    </xsl:template>

    <xsl:template mode="DataQuality" match="GM03Comprehensive.Comprehensive.DQ_CompletenessCommission">
        <xsl:apply-templates mode="DQ_Element" select=".">
            <xsl:with-param name="reportName">DQ_CompletenessCommission</xsl:with-param>
        </xsl:apply-templates>
    </xsl:template>

    <xsl:template mode="DQ_Element" match="*">
        <xsl:param name="reportName">unknown</xsl:param>
        <gmd:report>
            <xsl:element name="gmd:{string($reportName)}"
                         namespace="http://www.isotc211.org/2005/gmd">
                <xsl:apply-templates mode="text" select="nameOfMeasure"/>
                <xsl:apply-templates mode="DataQualityReport" select="measureIdentification"/>
                <xsl:apply-templates mode="text" select="measureDescription"/>
                <xsl:apply-templates mode="DataQualityReport" select="evaluationMethodType"/>
                <xsl:apply-templates mode="text" select="evaluationMethodDescription"/>
                <xsl:apply-templates mode="DataQualityReport" select="evaluationProcedure"/>
                <xsl:apply-templates mode="DataQualityReport" select="dateTime"/>
                <xsl:apply-templates mode="DataQualityReport" select="GM03Comprehensive.Comprehensive.DQ_ConformanceResult"/>
                <xsl:apply-templates mode="DataQualityReport" select="GM03Comprehensive.Comprehensive.DQ_QuantitativeResult"/>
            </xsl:element>
        </gmd:report>
    </xsl:template>

    <xsl:template mode="DataQualityReport" match="measureIdentification">
        <gmd:measureIdentification>
            <gmd:MD_Identifier>
                <gmd:code xsi:type="gmd:PT_FreeText_PropertyType">
                    <xsl:attribute name="xsi:type">gmd:PT_FreeText_PropertyType</xsl:attribute>
                    <xsl:apply-templates mode="language" select="."/>
                </gmd:code>
            </gmd:MD_Identifier>
        </gmd:measureIdentification>
    </xsl:template>

    <xsl:template mode="DataQualityReport" match="dateTime">
        <gmd:dateTime>
            <xsl:apply-templates mode="dateTime"/>
        </gmd:dateTime>
    </xsl:template>

    <xsl:template mode="DataQualityReport" match="evaluationMethodType">
        <gmd:evaluationMethodType>
            <gmd:DQ_EvaluationMethodTypeCode codeList="http://www.isotc211.org/2005/resources/codeList.xml#DQ_EvaluationMethodTypeCode" codeListValue="{normalize-space(.)}"/>
        </gmd:evaluationMethodType>
    </xsl:template>

    <xsl:template mode="DataQualityReport" match="evaluationProcedure">
        <gmd:TODO>evaluationProcedure</gmd:TODO>
    </xsl:template>

    <xsl:template mode="DataQualityReport" match="GM03Comprehensive.Comprehensive.DQ_ConformanceResult">
        <gmd:result>
            <gmd:DQ_ConformanceResult>
                <xsl:choose>
                    <xsl:when test="not(GM03Comprehensive.Comprehensive.CI_Citation)">
                        <xsl:call-template name="specificationStub" />
                    </xsl:when>
                    <xsl:otherwise>
                        <gmd:specification>
                            <xsl:apply-templates mode="Citation" select="GM03Comprehensive.Comprehensive.CI_Citation" />
                        </gmd:specification>
                    </xsl:otherwise>
                </xsl:choose>
                <xsl:apply-templates mode="text" select="explanation"/>
                <xsl:apply-templates mode="boolean" select="pass"/>
            </gmd:DQ_ConformanceResult>
        </gmd:result>
    </xsl:template>

    <xsl:template name="specificationStub">
        <xsl:variable name="date"
            select="java:format(java:java.text.SimpleDateFormat.new('yyyy-MM-dd'),
                                java:java.util.Date.new())"></xsl:variable>
        <gmd:specification>
            <gmd:CI_Citation>
                <gmd:title xsi:type="PT_FreeText_PropertyType" gco:nilReason="missing">
                    <gco:CharacterString></gco:CharacterString>
                </gmd:title>
                <gmd:date>
                    <gmd:CI_Date>
                        <gmd:date>
                            <gco:Date><xsl:value-of select="$date"/></gco:Date>
                        </gmd:date>
                        <gmd:dateType>
                            <gmd:CI_DateTypeCode codeList="http://www.isotc211.org/2005/resources/codeList.xml#CI_DateTypeCode" codeListValue="publication"/>
                        </gmd:dateType>
                    </gmd:CI_Date>
                </gmd:date>
            </gmd:CI_Citation>
        </gmd:specification>
    </xsl:template>

    <xsl:template mode="DataQualityReport" match="GM03Comprehensive.Comprehensive.DQ_QuantitativeResult">
        <gmd:result>
            <gmd:DQ_QuantitativeResult>
                <xsl:apply-templates mode="DataQualityReport" select="valueType"/>
                <xsl:apply-templates mode="DataQualityReport" select="valueUnit"/>
                <xsl:apply-templates mode="text" select="errorStatistic"/>
                <xsl:apply-templates mode="DataQualityReport" select="value"/>
            </gmd:DQ_QuantitativeResult>
        </gmd:result>
    </xsl:template>

    <xsl:template mode="DataQualityReport" match="valueType">
        <gmd:valueType>
            <gco:RecordType>
                <xsl:value-of select="."/>
            </gco:RecordType>
        </gmd:valueType>
    </xsl:template>

    <xsl:template mode="DataQualityReport" match="valueUnit">
        <gmd:valueUnit>
            <gml:BaseUnit gml:id="m">
              <gml:identifier codeSpace="http://www.bipm.org/en/si/base_units/l">metre</gml:identifier>
              <gml:name codeSpace="http://www.bipm.org/en/si/base_units/l">metre</gml:name>
              <gml:name>meter</gml:name>
              <gml:quantityType>length</gml:quantityType>
              <gml:catalogSymbol codeSpace="http://www.bipm.org/en/si/base_units/">m</gml:catalogSymbol>
              <gml:unitsSystem xmlns:xlink="http://www.w3.org/1999/xlink" xlink:href="http://www.bipm.fr/en/si"/>
            </gml:BaseUnit>
        </gmd:valueUnit>
    </xsl:template>


    <xsl:template mode="DataQualityReport" match="value">
        <gmd:value>
            <gco:Record>
                <xsl:choose>
                    <xsl:when test=".//XMLBLBOX">
                        <xsl:copy-of select=".//XMLBLBOX/*"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select=".//value"/>
                    </xsl:otherwise>
                </xsl:choose>
            </gco:Record>
        </gmd:value>
    </xsl:template>

    <xsl:template mode="DataQuality" match="GM03Core.Core.LI_Lineage">
        <gmd:lineage>
            <gmd:LI_Lineage>
                <xsl:apply-templates mode="DataQuality"/>
            </gmd:LI_Lineage>
        </gmd:lineage>
    </xsl:template>

    <xsl:template mode="DataQuality" match="GM03Comprehensive.Comprehensive.LI_ProcessStep">
        <gmd:processStep>
            <gmd:LI_ProcessStep>
                <xsl:apply-templates mode="text" select="description"/>
                <xsl:apply-templates mode="text" select="rationale"/>
                <xsl:if test="normalize-space(dateTime) != ''">
                <gmd:dateTime>
                     <xsl:apply-templates mode="dateTime" select="dateTime"/>
                </gmd:dateTime>
                </xsl:if>
                <xsl:apply-templates mode="DataQuality" select="processor"/>
            </gmd:LI_ProcessStep>
        </gmd:processStep>
    </xsl:template>

    <xsl:template mode="DataQuality" match="source">
        <gmd:source>
         <xsl:apply-templates mode="DataQuality" />
        </gmd:source>
    </xsl:template>

    <xsl:template mode="DataQuality" match="GM03Comprehensive.Comprehensive.LI_Source">
        <gmd:LI_Source>
            <xsl:apply-templates mode="text" select="description"/>
            <xsl:apply-templates mode="DataQuality" select="scaleDenominator"/>
            <xsl:apply-templates mode="DataQuality" select="sourceReferenceSystem"/>
            <xsl:apply-templates mode="DataQuality" select="sourceCitation"/>
            <xsl:apply-templates mode="DataQuality" select="GM03Core.Core.EX_Extent"/>
            <xsl:apply-templates mode="DataQuality" select="sourceStep"/>
        </gmd:LI_Source>
    </xsl:template>

    <xsl:template mode="DataQuality" match="sourceReferenceSystem">
        <gmd:sourceReferenceSystem>
            <xsl:apply-templates mode="RefSystem"/>
        </gmd:sourceReferenceSystem>
    </xsl:template>
    <xsl:template mode="DataQuality" match="sourceCitation">
        <gmd:sourceCitation>
            <xsl:apply-templates mode="Citation"/>
        </gmd:sourceCitation>
    </xsl:template>
    <xsl:template mode="DataQuality" match="GM03Core.Core.EX_Extent">
        <gmd:sourceExtent>
            <gmd:EX_Extent>
                <xsl:apply-templates mode="Extent"/>
            </gmd:EX_Extent>
        </gmd:sourceExtent>
    </xsl:template>
    <xsl:template mode="DataQuality" match="scaleDenominator">
        <gmd:scaleDenominator>
            <xsl:apply-templates mode="DataIdentification"/>
        </gmd:scaleDenominator>
    </xsl:template>

    <xsl:template mode="DataQuality" match="GM03Comprehensive.Comprehensive.DQ_Scope|GM03Core.Core.DQ_Scope">
        <gmd:scope>
            <gmd:DQ_Scope>
                <xsl:apply-templates mode="DataQuality" select="level"/>
                <xsl:apply-templates mode="DataQuality" select="extent"/>
                <xsl:if test="normalize-space(description) != ''">
                    <gmd:levelDescription>
                        <gmd:MD_ScopeDescription>
                            <gmd:other>
                                <xsl:apply-templates mode="text" select="description"/>
                            </gmd:other>
                        </gmd:MD_ScopeDescription>
                    </gmd:levelDescription>
                </xsl:if>
                <xsl:apply-templates mode="DataQuality" select="GM03Comprehensive.Comprehensive.DQ_Scopeextent/*"/>
                <xsl:apply-templates mode="DataQuality" select="GM03Core.Core.MD_ScopeDescription|GM03Comprehensive.Comprehensive.MD_ScopeDescription"/>
            </gmd:DQ_Scope>
        </gmd:scope>
    </xsl:template>

    <xsl:template mode="DataQuality" match="extent">
        <xsl:apply-templates mode="Extent"/>
    </xsl:template>

    <xsl:template mode="DataQuality" match="GM03Core.Core.MD_ScopeDescription|GM03Comprehensive.Comprehensive.MD_ScopeDescription">
        <gmd:levelDescription>
            <gmd:MD_ScopeDescription>
                <xsl:apply-templates mode="DataQuality" select="attributes"/>
                <xsl:apply-templates mode="DataQuality" select="features"/>
                <xsl:apply-templates mode="DataQuality" select="featureInstances"/>
                <xsl:apply-templates mode="DataQuality" select="attributeInstances"/>
                <xsl:apply-templates mode="text" select="dataset"/>
                <xsl:apply-templates mode="text" select="other"/>
            </gmd:MD_ScopeDescription>
        </gmd:levelDescription>
    </xsl:template>

    <xsl:template mode="DataQuality" match="attributes|features|featureInstances|attributeInstances">
        <!-- TODO: I don't know what to put -->
        <!--<xsl:element name="{local-name(.)}">-->
            <!--<LocalisedURL>-->
                <!--<xsl:value-of select="."/>-->
            <!--</LocalisedURL>-->
        <!--</xsl:element>-->
    </xsl:template>

    <!-- ============================================================================== -->

    <xsl:template match="statement" mode="DataQuality">
        <gmd:statement>
            <xsl:apply-templates mode="language"/>
        </gmd:statement>
    </xsl:template>

    <!-- ============================================================================== -->

    <xsl:template match="level" mode="DataQuality">
        <gmd:level>
            <gmd:MD_ScopeCode codeList="./resources/codeList.xml#MD_ScopeCode" codeListValue="{normalize-space(.)}"/>
        </gmd:level>
    </xsl:template>

    <xsl:template match="DQ_DataQuality" mode="DataQuality"/>

    <!-- ============================================================================== -->

    <xsl:template mode="DataQualityReport" match="text()">
        <xsl:call-template name="UnMatchedText">
            <xsl:with-param name="mode">DataQuality</xsl:with-param>
        </xsl:call-template>
    </xsl:template>

    <xsl:template mode="DQ_Element" match="text()">
        <xsl:call-template name="UnMatchedText">
            <xsl:with-param name="mode">DataQuality</xsl:with-param>
        </xsl:call-template>
    </xsl:template>

    <xsl:template mode="DataQuality" match="text()">
        <xsl:call-template name="UnMatchedText">
            <xsl:with-param name="mode">DataQualityReport</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
</xsl:stylesheet>
