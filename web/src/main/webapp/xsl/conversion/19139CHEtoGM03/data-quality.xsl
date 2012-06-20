<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns="http://www.interlis.ch/INTERLIS2.3"
                xmlns:che="http://www.geocat.ch/2008/che"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:gml="http://www.opengis.net/gml"
                xmlns:gmi="http://www.isotc211.org/2005/gmi"
                xmlns:xalan="http://xml.apache.org/xalan" 
                exclude-result-prefixes="che gco gmd gml xalan gmi">

    <xsl:template mode="DataQuality" match="gmd:DQ_DataQuality">
        <GM03_2Core.Core.DQ_DataQuality TID="x{generate-id(.)}">
            <BACK_REF name="MD_Metadata"/>
            <xsl:apply-templates mode="DataQuality" select="gmd:scope"/>
            <xsl:apply-templates mode="DataQuality" select="gmd:report"/>
            <xsl:apply-templates mode="DataQuality" select="gmd:lineage"/>
        </GM03_2Core.Core.DQ_DataQuality>
    </xsl:template>

    <xsl:template mode="DataQuality" match="gmd:scope|gmd:lineage">
        <xsl:apply-templates mode="DataQuality"/>        
    </xsl:template>

    <xsl:template mode="DataQuality" match="gmd:LI_Source">
        <GM03_2Comprehensive.Comprehensive.sourceLI_Lineage TID="x{generate-id(.)}">
            <source REF="?">
                <GM03_2Comprehensive.Comprehensive.LI_Source  TID="x2{generate-id(.)}">
                 <xsl:apply-templates mode="textGroup" select="gmd:description" />
                 <xsl:apply-templates mode="DataQuality" select="gmd:scaleDenominator" />
                 <xsl:apply-templates mode="DataQuality" select="gmd:sourceCitation" />
                 <xsl:apply-templates mode="DataQuality" select="gmd:sourceReferenceSystem" />
                 
                 
<!--             Doesn't seem to be part of the gm03 schema    -->
<!--                 <xsl:apply-templates mode="DataQuality" select="gmd:sourceExtent" />-->
<!--                 <xsl:apply-templates mode="DataQuality" select="gmd:sourceStep" />-->
               </GM03_2Comprehensive.Comprehensive.LI_Source>
            </source>
            <BACK_REF name="LI_Lineage" />
        </GM03_2Comprehensive.Comprehensive.sourceLI_Lineage>
    </xsl:template>
    
    <xsl:template mode="DataQuality" match="gmd:scaleDenominator">
        <scaleDenominator REF="?">
            <xsl:apply-templates mode="DataIdentification" />
        </scaleDenominator>
    </xsl:template>
    
    <xsl:template mode="DataQuality" match="gmd:sourceReferenceSystem">
        <sourceReferenceSystem REF="?">
            <xsl:apply-templates mode="RefSystem" />
        </sourceReferenceSystem>
    </xsl:template>
    
    
    <xsl:template mode="DataQuality" match="gmd:sourceCitation">
        <sourceCitation REF="?">
            <xsl:apply-templates mode="DataIdentification" />
        </sourceCitation>
    </xsl:template>
    
    <xsl:template mode="DataQuality" match="gmd:report">
        <xsl:variable name="report">
            <xsl:apply-templates mode="DataQuality"/>
        </xsl:variable>
        <xsl:if test="count($report/*)>0">
            <GM03_2Comprehensive.Comprehensive.reportDQ_DataQuality TID="x{generate-id(.)}">
                <report REF="?">
                    <xsl:copy-of select="$report"/>
                </report>
                <BACK_REF name="DQ_Qualitiy" />
            </GM03_2Comprehensive.Comprehensive.reportDQ_DataQuality>
        </xsl:if>
    </xsl:template>

    
    <xsl:template mode="DataQuality" match="gmd:extent">
        <GM03_2Comprehensive.Comprehensive.DQ_Scopeextent TID="x{generate-id(.)}">
	        <extent REF="?">
	            <xsl:apply-templates mode="DataQuality" /> 
	        </extent>
            <BACK_REF name="DQ_Scope"/>
        </GM03_2Comprehensive.Comprehensive.DQ_Scopeextent>
    </xsl:template>

    <xsl:template mode="DataQuality" match="gmd:EX_Extent">
        <GM03_2Core.Core.EX_Extent TID="x{generate-id(.)}">
            <xsl:apply-templates mode="textGroup" select="gmd:description"/>
            <xsl:apply-templates mode="Extent" select="gmd:geographicElement"/>
            <xsl:apply-templates mode="Extent" select="gmd:temporalElement"/>
            <xsl:apply-templates mode="Extent" select="gmd:verticalElement"/>
        </GM03_2Core.Core.EX_Extent>
    </xsl:template>

    <xsl:template mode="DataQuality" match="gmd:DQ_Scope">
        <GM03_2Core.Core.DQ_Scope TID="x{generate-id(.)}">
            <xsl:apply-templates mode="text" select="gmd:level"/>
            <BACK_REF name="DQ_DataQuality"/>
            <xsl:apply-templates mode="DataQuality" select="gmd:extent"/>
            <xsl:apply-templates mode="DataQuality" select="gmd:levelDescription/gmd:MD_ScopeDescription"/>
        </GM03_2Core.Core.DQ_Scope>
    </xsl:template>

    <xsl:template mode="DataQuality" match="gmd:MD_ScopeDescription">
        <GM03_2Core.Core.MD_ScopeDescription TID="x{generate-id(.)}">
           <xsl:apply-templates mode="text" select="gmd:attributes"/>
           <xsl:apply-templates mode="text" select="gmd:features"/>
           <xsl:apply-templates mode="text" select="gmd:featureInstances"/>
           <xsl:apply-templates mode="text" select="gmd:attributeInstances"/>
            <xsl:apply-templates mode="text" select="gmd:dataset"/>
            <xsl:apply-templates mode="text" select="gmd:other"/>
            <BACK_REF name="DQ_Scope"/>
        </GM03_2Core.Core.MD_ScopeDescription>
    </xsl:template>


    <xsl:template mode="DataQuality" match="gmd:DQ_TemporalValidity">
        <GM03_2Comprehensive.Comprehensive.DQ_TemporalValidity TID="x{generate-id(.)}">
            <xsl:apply-templates mode="DQ_Element" select=".">
                <xsl:with-param name="backRef">DQ_TemporalValidity</xsl:with-param>
            </xsl:apply-templates>
        </GM03_2Comprehensive.Comprehensive.DQ_TemporalValidity>
    </xsl:template>

    <xsl:template mode="DataQuality" match="gmd:DQ_TemporalConsistency">
        <GM03_2Comprehensive.Comprehensive.DQ_TemporalConsistency TID="x{generate-id(.)}">
            <xsl:apply-templates mode="DQ_Element" select=".">
                <xsl:with-param name="backRef">DQ_TemporalConsistency</xsl:with-param>
            </xsl:apply-templates>
        </GM03_2Comprehensive.Comprehensive.DQ_TemporalConsistency>
    </xsl:template>

    <xsl:template mode="DataQuality" match="gmd:DQ_AccuracyOfATimeMeasurement">
        <GM03_2Comprehensive.Comprehensive.DQ_AccuracyOfATimeMeasurement TID="x{generate-id(.)}">
            <xsl:apply-templates mode="DQ_Element" select=".">
                <xsl:with-param name="backRef">DQ_AccuracyOfATimeMeasurement</xsl:with-param>
            </xsl:apply-templates>
        </GM03_2Comprehensive.Comprehensive.DQ_AccuracyOfATimeMeasurement>
    </xsl:template>

    <xsl:template mode="DataQuality" match="gmd:DQ_QuantitativeAttributeAccuracy">
        <GM03_2Comprehensive.Comprehensive.DQ_QuantitativeAttributeAccuracy TID="x{generate-id(.)}">
            <xsl:apply-templates mode="DQ_Element" select=".">
                <xsl:with-param name="backRef">DQ_QuantitativeAttributeAccuracy</xsl:with-param>
            </xsl:apply-templates>
        </GM03_2Comprehensive.Comprehensive.DQ_QuantitativeAttributeAccuracy>
    </xsl:template>

    <xsl:template mode="DataQuality" match="gmd:DQ_NonQuantitativeAttributeAccuracy">
        <GM03_2Comprehensive.Comprehensive.DQ_NonQuantitativeAttributeAccuracy TID="x{generate-id(.)}">
            <xsl:apply-templates mode="DQ_Element" select=".">
                <xsl:with-param name="backRef">DQ_NonQuantitativeAttributeAccuracy</xsl:with-param>
            </xsl:apply-templates>
        </GM03_2Comprehensive.Comprehensive.DQ_NonQuantitativeAttributeAccuracy>
    </xsl:template>

    <xsl:template mode="DataQuality" match="gmd:DQ_ThematicClassificationCorrectness">
        <GM03_2Comprehensive.Comprehensive.DQ_ThematicClassificationCorrectness TID="x{generate-id(.)}">
            <xsl:apply-templates mode="DQ_Element" select=".">
                <xsl:with-param name="backRef">DQ_ThematicClassificationCorrectness</xsl:with-param>
            </xsl:apply-templates>
        </GM03_2Comprehensive.Comprehensive.DQ_ThematicClassificationCorrectness>
    </xsl:template>

    <xsl:template mode="DataQuality" match="gmd:DQ_RelativeInternalPositionalAccuracy">
        <GM03_2Comprehensive.Comprehensive.DQ_RelativeInternalPositionalAccuracy TID="x{generate-id(.)}">
            <xsl:apply-templates mode="DQ_Element" select=".">
                <xsl:with-param name="backRef">DQ_RelativeInternalPositionalAccuracy</xsl:with-param>
            </xsl:apply-templates>
        </GM03_2Comprehensive.Comprehensive.DQ_RelativeInternalPositionalAccuracy>
    </xsl:template>

    <xsl:template mode="DataQuality" match="gmd:DQ_GriddedDataPositionalAccuracy">
        <GM03_2Comprehensive.Comprehensive.DQ_GriddedDataPositionalAccuracy TID="x{generate-id(.)}">
            <xsl:apply-templates mode="DQ_Element" select=".">
                <xsl:with-param name="backRef">DQ_GriddedDataPositionalAccuracy</xsl:with-param>
            </xsl:apply-templates>
        </GM03_2Comprehensive.Comprehensive.DQ_GriddedDataPositionalAccuracy>
    </xsl:template>

    <xsl:template mode="DataQuality" match="gmd:DQ_AbsoluteExternalPositionalAccuracy">
        <GM03_2Comprehensive.Comprehensive.DQ_AbsoluteExternalPositionalAccuracy TID="x{generate-id(.)}">
            <xsl:apply-templates mode="DQ_Element" select=".">
                <xsl:with-param name="backRef">DQ_AbsoluteExternalPositionalAccuracy</xsl:with-param>
            </xsl:apply-templates>
        </GM03_2Comprehensive.Comprehensive.DQ_AbsoluteExternalPositionalAccuracy>
    </xsl:template>

    <xsl:template mode="DataQuality" match="gmd:DQ_TopologicalConsistency">
        <GM03_2Comprehensive.Comprehensive.DQ_TopologicalConsistency TID="x{generate-id(.)}">
            <xsl:apply-templates mode="DQ_Element" select=".">
                <xsl:with-param name="backRef">DQ_TopologicalConsistency</xsl:with-param>
            </xsl:apply-templates>
        </GM03_2Comprehensive.Comprehensive.DQ_TopologicalConsistency>
    </xsl:template>

    <xsl:template mode="DataQuality" match="gmd:DQ_FormatConsistency">
        <GM03_2Comprehensive.Comprehensive.DQ_FormatConsistency TID="x{generate-id(.)}">
            <xsl:apply-templates mode="DQ_Element" select=".">
                <xsl:with-param name="backRef">DQ_FormatConsistency</xsl:with-param>
            </xsl:apply-templates>
        </GM03_2Comprehensive.Comprehensive.DQ_FormatConsistency>
    </xsl:template>

    <xsl:template mode="DataQuality" match="gmd:DQ_DomainConsistency">
        <GM03_2Comprehensive.Comprehensive.DQ_DomainConsistency TID="x{generate-id(.)}">
            <xsl:apply-templates mode="DQ_Element" select=".">
                <xsl:with-param name="backRef">DQ_DomainConsistency</xsl:with-param>
            </xsl:apply-templates>
        </GM03_2Comprehensive.Comprehensive.DQ_DomainConsistency>
    </xsl:template>

    <xsl:template mode="DataQuality" match="gmd:DQ_ConceptualConsistency">
        <GM03_2Comprehensive.Comprehensive.DQ_ConceptualConsistency TID="x{generate-id(.)}">
            <xsl:apply-templates mode="DQ_Element" select=".">
                <xsl:with-param name="backRef">DQ_ConceptualConsistency</xsl:with-param>
            </xsl:apply-templates>
        </GM03_2Comprehensive.Comprehensive.DQ_ConceptualConsistency>
    </xsl:template>

    <xsl:template mode="DataQuality" match="gmd:DQ_CompletenessOmission">
        <GM03_2Comprehensive.Comprehensive.DQ_CompletenessOmission TID="x{generate-id(.)}">
            <xsl:apply-templates mode="DQ_Element" select=".">
                <xsl:with-param name="backRef">DQ_CompletenessOmission</xsl:with-param>
            </xsl:apply-templates>
        </GM03_2Comprehensive.Comprehensive.DQ_CompletenessOmission>
    </xsl:template>

    <xsl:template mode="DataQuality" match="gmd:DQ_CompletenessCommission">
        <GM03_2Comprehensive.Comprehensive.DQ_CompletenessCommission TID="x{generate-id(.)}">
            <xsl:apply-templates mode="DQ_Element" select=".">
                <xsl:with-param name="backRef">DQ_CompletenessCommission</xsl:with-param>
            </xsl:apply-templates>
        </GM03_2Comprehensive.Comprehensive.DQ_CompletenessCommission>
    </xsl:template>
    
    <xsl:template mode="DataQuality" match="gmi:QE_Usability">
        <!-- XXX do nothing for now gm03 does not support this report -->
    </xsl:template>


    <xsl:template mode="DQ_Element" match="*">
           <xsl:param name="backRef"/>
           <xsl:apply-templates mode="DataQuality" select="gmd:nameOfMeasure"/>
           <xsl:apply-templates mode="text" select="gmd:measureDescription"/>
           <xsl:apply-templates mode="text" select="gmd:evaluationMethodType"/>
           <xsl:apply-templates mode="text" select="gmd:evaluationMethodDescription"/>
           <xsl:if test="normalize-space(gmd:dateTime) != ''">
               <xsl:for-each select="gmd:dateTime[normalize-space(.) != '']">
                   <dateTime>
                      <GM03_2Core.Core.DateTime_>
                        <value><xsl:value-of select="gmd:dateTime"/></value>
                      </GM03_2Core.Core.DateTime_>
                   </dateTime>
               </xsl:for-each>
           </xsl:if>
           <xsl:apply-templates mode="DataQuality" select="gmd:measureIdentification"/>
           <xsl:apply-templates mode="DataQuality" select="gmd:result">
                <xsl:with-param name="backRef" select="$backRef"/>
           </xsl:apply-templates>
    </xsl:template>

    <xsl:template mode="DataQuality" match="gmd:result">
        <xsl:param name="backRef"/>
        
        <xsl:apply-templates mode="DataQualityResult">
            <xsl:with-param name="backRef" select="$backRef"/>
        </xsl:apply-templates>
    </xsl:template>
        
    <xsl:template mode="DataQualityResult" match="gmi:QE_CoverageResult">
        <!-- XXX Ignore for now -->
    </xsl:template>
    
    <xsl:template mode="DataQualityResult" match="gmd:DQ_ConformanceResult">
        <xsl:param name="backRef"/>
          <GM03_2Comprehensive.Comprehensive.DQ_ConformanceResult TID="x{generate-id(.)}">
                <BACK_REF name="DQ_Element" />
                <xsl:apply-templates mode="characterString" select="gmd:explanation"/>
                <xsl:apply-templates mode="text" select="gmd:pass"/>
                <xsl:if test="gmd:specification/gmd:CI_Citation">
                    <GM03_2Comprehensive.Comprehensive.CI_Citation TID="xCI{generate-id(.)}">
                        <xsl:apply-templates mode="RefSystem" select="gmd:specification/gmd:CI_Citation">
                            <xsl:with-param name="backRef">DQ_ConformanceResult</xsl:with-param>
                        </xsl:apply-templates>
                    </GM03_2Comprehensive.Comprehensive.CI_Citation>
                </xsl:if>
          </GM03_2Comprehensive.Comprehensive.DQ_ConformanceResult>
    </xsl:template>
    
    <xsl:template mode="DataQualityResult" match="gmd:DQ_QuantitativeResult">
        <xsl:param name="backRef"/>
          <GM03_2Comprehensive.Comprehensive.DQ_QuantitativeResult TID="x{generate-id(.)}">
                <BACK_REF name="DQ_Element" />
                <xsl:if test="normalize-space(gmd:valueType/gco:RecordType) != ''">
                    <valueType><xsl:value-of select="gmd:valueType/gco:RecordType"/></valueType>
                </xsl:if>

                <xsl:apply-templates mode="DataQualityResult" select="gmd:valueUnit"/>
                <xsl:apply-templates mode="text" select="gmd:errorStatistic"/>
                <xsl:apply-templates mode="DataQualityResult" select="gmd:value"/>
                <xsl:apply-templates mode="RefSystem" select="gmd:specification/gmd:CI_Citation">
                    <xsl:with-param name="backRef">DQ_QuantitativeResult</xsl:with-param>
                </xsl:apply-templates>
          </GM03_2Comprehensive.Comprehensive.DQ_QuantitativeResult>
    </xsl:template>
    
    <xsl:template mode="DataQualityResult" match="gmd:valueUnit">
          <valueUnit>m</valueUnit>
    </xsl:template>
    
    <xsl:template mode="DataQualityResult" match="gmd:value">
        <xsl:choose>
            <xsl:when test="gco:Record/text()">
                <value>
                  <GM03_2Comprehensive.Comprehensive.Record_>
                    <value><xsl:value-of select="gco:Record"/></value>
                  </GM03_2Comprehensive.Comprehensive.Record_>
                </value>
            </xsl:when>
            <xsl:when test="gco:Record/node()">
                <value>
                  <GM03_2Comprehensive.Comprehensive.Record_>
                    <value REF="?">
                         <XMLBLBOX TID="x{generate-id(.)}">
                            <xsl:copy-of select="gco:Record/*"/>
                         </XMLBLBOX>
                     </value>
                  </GM03_2Comprehensive.Comprehensive.Record_>
                </value>
            </xsl:when>
            <xsl:otherwise>
            <!-- Do nothing -->
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <xsl:template mode="DataQuality" match="gmd:nameOfMeasure">
          <nameOfMeasure>
             <GM03_2Core.Core.CharacterString_>
                 <value><xsl:value-of select="."/></value>
             </GM03_2Core.Core.CharacterString_>
          </nameOfMeasure>
    </xsl:template>

    <xsl:template mode="DataQuality" match="gmd:measureIdentification">
        <xsl:if test="normalize-space(text()) != ''">
            <measureIdentification REF="?">
                <xsl:apply-templates mode="RefSystem" select="gmd:RS_Identifier"/>
            </measureIdentification>
       </xsl:if>
    </xsl:template>

    <xsl:template mode="DataQuality" match="gmd:LI_Lineage">
        <GM03_2Core.Core.LI_Lineage TID="x{generate-id(.)}">
            <xsl:apply-templates mode="textGroup" select="gmd:statement"/>
            <BACK_REF name="DQ_DataQuality"/>
            <xsl:apply-templates mode="DataQuality" select="gmd:processStep"/>
            <xsl:apply-templates mode="DataQuality" select="gmd:source/gmd:LI_Source"/>    
        </GM03_2Core.Core.LI_Lineage>
    </xsl:template>
    
     <xsl:template mode="DataQuality" match="gmd:processStep">
            <xsl:apply-templates mode="DataQuality" select="gmd:LI_ProcessStep"/>    
     </xsl:template>
     <xsl:template mode="DataQuality" match="gmd:LI_ProcessStep">
        <GM03_2Comprehensive.Comprehensive.LI_ProcessStep TID="x{generate-id(.)}">
            <xsl:apply-templates mode="text" select="gmd:description"/>
            <xsl:apply-templates mode="text" select="gmd:dateTime"/>
            <xsl:apply-templates mode="text_" select="gmd:rationale"/>
            <xsl:apply-templates mode="DataQuality" select="processor"/>
            <BACK_REF name="LI_Lineage"/>
        </GM03_2Comprehensive.Comprehensive.LI_ProcessStep>
    </xsl:template>
    
    <xsl:template mode="DQ_Element" match="*" priority="-100">
        <ERROR>Unknown DataQualityResult element <xsl:value-of select="local-name(.)"/></ERROR>
    </xsl:template>
    
    <xsl:template mode="DataQualityResult" match="*" priority="-100">
        <ERROR>Unknown DataQualityResult element <xsl:value-of select="local-name(.)"/></ERROR>
    </xsl:template>
        
    <xsl:template mode="DataQuality" match="*" priority="-100">
        <ERROR>Unknown DataQuality element <xsl:value-of select="local-name(.)"/></ERROR>
    </xsl:template>
</xsl:stylesheet>

