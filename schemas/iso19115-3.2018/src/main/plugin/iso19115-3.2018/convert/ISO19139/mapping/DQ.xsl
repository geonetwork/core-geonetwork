<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:gcoold="http://www.isotc211.org/2005/gco" xmlns:gmi="http://www.isotc211.org/2005/gmi" xmlns:gmx="http://www.isotc211.org/2005/gmx"
  xmlns:gsr="http://www.isotc211.org/2005/gsr" xmlns:gss="http://www.isotc211.org/2005/gss" xmlns:gts="http://www.isotc211.org/2005/gts" xmlns:srvold="http://www.isotc211.org/2005/srv"
  xmlns:gml30="http://www.opengis.net/gml" xmlns:cat="http://standards.iso.org/iso/19115/-3/cat/1.0" xmlns:cit="http://standards.iso.org/iso/19115/-3/cit/2.0"
  xmlns:gcx="http://standards.iso.org/iso/19115/-3/gcx/1.0" xmlns:gex="http://standards.iso.org/iso/19115/-3/gex/1.0" xmlns:lan="http://standards.iso.org/iso/19115/-3/lan/1.0"
  xmlns:srv="http://standards.iso.org/iso/19115/-3/srv/2.0" xmlns:mac="http://standards.iso.org/iso/19115/-3/mac/2.0" xmlns:mas="http://standards.iso.org/iso/19115/-3/mas/1.0"
  xmlns:mcc="http://standards.iso.org/iso/19115/-3/mcc/1.0" xmlns:mco="http://standards.iso.org/iso/19115/-3/mco/1.0" xmlns:mda="http://standards.iso.org/iso/19115/-3/mda/1.0"
  xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0" xmlns:mdt="http://standards.iso.org/iso/19115/-3/mdt/2.0" xmlns:mex="http://standards.iso.org/iso/19115/-3/mex/1.0"
  xmlns:mic="http://standards.iso.org/iso/19115/-3/mic/1.0" xmlns:mil="http://standards.iso.org/iso/19115/-3/mil/1.0" xmlns:mrl="http://standards.iso.org/iso/19115/-3/mrl/2.0"
  xmlns:mds="http://standards.iso.org/iso/19115/-3/mds/2.0" xmlns:mmi="http://standards.iso.org/iso/19115/-3/mmi/1.0" xmlns:mpc="http://standards.iso.org/iso/19115/-3/mpc/1.0"
  xmlns:mrc="http://standards.iso.org/iso/19115/-3/mrc/2.0" xmlns:mrd="http://standards.iso.org/iso/19115/-3/mrd/1.0" xmlns:mri="http://standards.iso.org/iso/19115/-3/mri/1.0"
  xmlns:mrs="http://standards.iso.org/iso/19115/-3/mrs/1.0" xmlns:msr="http://standards.iso.org/iso/19115/-3/msr/2.0" xmlns:mai="http://standards.iso.org/iso/19115/-3/mai/1.0"
  xmlns:mdq="http://standards.iso.org/iso/19157/-2/mdq/1.0" xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0" xmlns:gml="http://www.opengis.net/gml/3.2"
  xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:xd="http://www.oxygenxml.com/ns/doc/xsl" exclude-result-prefixes="#all">

  <xsl:import href="../utility/multiLingualCharacterStrings.xsl"/>

  <xsl:template match="gmd:dataQualityInfo" mode="from19139to19115-3.2018">
    <xsl:if test="gmd:DQ_DataQuality/gmd:report or
                  count(../gmd:distributionInfo//gmd:onLine[
                          */gmd:function/*/@codeListValue = $onlineFunctionMap/entry[@type = 'dq']/@value]) > 0">
      <!-- ISO 19157 -->
      <mdb:dataQualityInfo>
        <mdq:DQ_DataQuality>
          <xsl:if test="gmd:DQ_DataQuality/gmd:scope">
            <mdq:scope>
              <xsl:choose>
                <xsl:when test="gmd:DQ_DataQuality/gmd:scope/@*">
                  <xsl:copy-of select="gmd:DQ_DataQuality/gmd:scope/@*"/>
                </xsl:when>
                <xsl:otherwise>
                  <mcc:MD_Scope>
                    <xsl:apply-templates select="gmd:DQ_DataQuality/gmd:scope/gmd:DQ_Scope/*" mode="from19139to19115-3.2018"/>
                  </mcc:MD_Scope>
                </xsl:otherwise>
              </xsl:choose>
            </mdq:scope>
          </xsl:if>

          <xsl:for-each select="gmd:DQ_DataQuality/gmd:report">
            <xsl:for-each select="*">
              <xsl:element name="mdq:report">
                <!-- DQ_NonQuantitativeAttributeAccuracy changed to DQ_NonQuantitativeAttributeCorrectness -->
                <xsl:variable name="dataQualityReportType"
                                    select="if (local-name()='DQ_NonQuantitativeAttributeAccuracy')
                                                 then 'DQ_NonQuantitativeAttributeCorrectness' else local-name()"/>

                <xsl:element name="{concat('mdq:',$dataQualityReportType)}">
                  <xsl:if test="gmd:nameOfMeasure or gmd:measureIdentification or gmd:measureDescription">
                    <!-- output quality measure information only if gmd:measureIdentification or gmd:measureDescription exist -->
                    <mdq:measure>
                      <mdq:DQ_MeasureReference>
                        <xsl:apply-templates select="gmd:measureIdentification" mode="from19139to19115-3.2018"/>
                        <xsl:call-template name="writeCharacterStringElement">
                          <xsl:with-param name="elementName" select="'mdq:nameOfMeasure'"/>
                          <xsl:with-param name="nodeWithStringToWrite" select="gmd:nameOfMeasure"/>
                        </xsl:call-template>
                        <xsl:call-template name="writeCharacterStringElement">
                          <xsl:with-param name="elementName" select="'mdq:measureDescription'"/>
                          <xsl:with-param name="nodeWithStringToWrite" select="gmd:measureDescription"/>
                        </xsl:call-template>
                      </mdq:DQ_MeasureReference>
                    </mdq:measure>
                  </xsl:if>
                  <xsl:if
                    test="gmd:evaluationMethodDescription or gmd:evaluationProcedure/gmd:CI_Citation
                    or gmd:evaluationMethodType/gmd:DQ_EvaluationMethodTypeCode/@codeListValue">
                    <!-- output quality evaluation method information only if gmd:evaluationMethodDescription
                      or gmd:evaluationProcedure/gmd:CI_Citation
                      or gmd:evaluationMethodType/gmd:DQ_EvaluationMethodTypeCode/@codeListValue exist -->
                    <mdq:evaluationMethod>
                      <mdq:DQ_FullInspection>
                        <xsl:if test="gmd:dateTime/gcoold:DateTime">
                          <mdq:dateTime>
                            <gco:DateTime>
                              <xsl:value-of select="gmd:dateTime/gcoold:DateTime"/>
                            </gco:DateTime>
                          </mdq:dateTime>
                        </xsl:if>
                        <xsl:call-template name="writeCharacterStringElement">
                          <xsl:with-param name="elementName" select="'mdq:evaluationMethodDescription'"/>
                          <xsl:with-param name="nodeWithStringToWrite" select="gmd:evaluationMethodDescription"/>
                        </xsl:call-template>
                        <mdq:evaluationProcedure>
                          <xsl:apply-templates select="gmd:evaluationProcedure/gmd:CI_Citation" mode="from19139to19115-3.2018"/>
                        </mdq:evaluationProcedure>
                        <xsl:call-template name="writeCodelistElement">
                          <xsl:with-param name="elementName" select="'mdq:evaluationMethodType'"/>
                          <xsl:with-param name="codeListName" select="'mdq:DQ_EvaluationMethodTypeCode'"/>
                          <xsl:with-param name="codeListValue" select="gmd:evaluationMethodType/gmd:DQ_EvaluationMethodTypeCode/@codeListValue"/>
                        </xsl:call-template>
                      </mdq:DQ_FullInspection>
                    </mdq:evaluationMethod>
                    <xsl:apply-templates select="gmd:result" mode="from19139to19115-3.2018"/>
                  </xsl:if>
                  <!-- gmd:result uses default templates -->
                  <xsl:apply-templates select="gmd:result" mode="from19139to19115-3.2018"/>
                </xsl:element>
              </xsl:element>
            </xsl:for-each>
          </xsl:for-each>

          <xsl:call-template name="onlineSourceDispatcher">
            <xsl:with-param name="type" select="'reportReference'"/>
          </xsl:call-template>
          <xsl:call-template name="onlineSourceDispatcher">
            <xsl:with-param name="type" select="'specification'"/>
          </xsl:call-template>

        </mdq:DQ_DataQuality>
      </mdb:dataQualityInfo>
    </xsl:if>
    <!--
    gmd:lineage moves directly under MD_Metadata
    -->
    <xsl:for-each select="gmd:DQ_DataQuality/gmd:lineage/gmd:LI_Lineage">
      <!--
      gmd:DataQuality objects without lineage go to ISO 19157
      -->
      <xsl:element name="mdb:resourceLineage">
        <mrl:LI_Lineage>
          <xsl:call-template name="writeCharacterStringElement">
            <xsl:with-param name="elementName" select="'mrl:statement'"/>
            <xsl:with-param name="nodeWithStringToWrite" select="gmd:statement"/>
          </xsl:call-template>
          <xsl:if test="../../../gmd:DQ_DataQuality/gmd:scope">
            <xsl:variable name="dataQualityScopeObject" select="../../../gmd:DQ_DataQuality/gmd:scope/gmd:DQ_Scope"/>
            <mrl:scope>
              <mcc:MD_Scope>
                <xsl:call-template name="writeCodelistElement">
                  <xsl:with-param name="elementName" select="'mcc:level'"/>
                  <xsl:with-param name="codeListName" select="'mcc:MD_ScopeCode'"/>
                  <xsl:with-param name="codeListValue"
                    select="$dataQualityScopeObject//gmd:MD_ScopeCode/@codeListValue|
                                          $dataQualityScopeObject//gmx:MX_ScopeCode/@codeListValue"/>
                </xsl:call-template>
                <xsl:for-each select="$dataQualityScopeObject//gmd:EX_Extent">
                  <mcc:extent>
                    <xsl:apply-templates select="." mode="from19139to19115-3.2018"/>
                  </mcc:extent>
                </xsl:for-each>
                <xsl:if test="$dataQualityScopeObject//gmd:MD_ScopeDescription">
                  <mcc:levelDescription>
                    <mcc:MD_ScopeDescription>
                      <xsl:apply-templates select="$dataQualityScopeObject//gmd:MD_ScopeDescription/*" mode="from19139to19115-3.2018"/>
                      <!--<xsl:call-template name="writeCharacterStringElement">
                      <xsl:with-param name="elementName" select="'cit:other'"/>
                      <xsl:with-param name="stringToWrite" select="gmd:statement"/>
                    </xsl:call-template>-->
                    </mcc:MD_ScopeDescription>
                  </mcc:levelDescription>
                </xsl:if>
              </mcc:MD_Scope>
            </mrl:scope>
          </xsl:if>

          <xsl:call-template name="onlineSourceDispatcher">
            <xsl:with-param name="type" select="'additionalDocumentation'"/>
          </xsl:call-template>

          <xsl:apply-templates select="gmd:source" mode="from19139to19115-3.2018"/>
          <xsl:apply-templates select="gmd:processStep" mode="from19139to19115-3.2018"/>
        </mrl:LI_Lineage>
      </xsl:element>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="gmd:report/*/gmd:result/gmd:DQ_QuantitativeResult" mode="from19139to19115-3.2018">
    <mdq:DQ_QuantitativeResult>
      <xsl:apply-templates select="gmd:value" mode="from19139to19115-3.2018"/>
      <xsl:apply-templates select="gmd:valueUnit" mode="from19139to19115-3.2018"/>
      <xsl:apply-templates select="gmd:valueType" mode="from19139to19115-3.2018"/>
    </mdq:DQ_QuantitativeResult>
  </xsl:template>

  <!-- added to account for element name change from valueType to valueRecordType - 2014-07-29 -->
  <xsl:template match="gmd:result/gmd:DQ_QuantitativeResult/gmd:valueType" mode="from19139to19115-3.2018">
    <xsl:element name="mdq:valueRecordType">
      <xsl:apply-templates mode="from19139to19115-3.2018"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="gmd:report/*/gmd:result/gmd:DQ_ConformanceResult/gmd:specification" mode="from19139to19115-3.2018">
    <mdq:specification>
      <!--<dqm:DQM_SourceReference>
        <dqm:citation>-->
      <xsl:apply-templates mode="from19139to19115-3.2018"/>
      <!--</dqm:citation>
      </dqm:DQM_SourceReference>-->
    </mdq:specification>
  </xsl:template>

  <xsl:template match="gmd:report/*/gmd:result/gmi:QE_CoverageResult/gmi:resultContentDescription/gmi:MI_CoverageDescription" mode="from19139to19115-3.2018">
    <xsl:element name="mrc:MI_CoverageDescription">
      <xsl:element name="mrc:attributeDescription">
        <xsl:element name="gco:RecordType">
          <xsl:apply-templates select="./gmd:attributeDescription/gcoold:RecordType/@*" mode="from19139to19115-3.2018"/>
        </xsl:element>
      </xsl:element>
      <xsl:element name="mrc:attributeGroup">
        <xsl:element name="mrc:MD_AttributeGroup">
          <xsl:apply-templates mode="from19139to19115-3.2018"/>
        </xsl:element>
      </xsl:element>
    </xsl:element>
  </xsl:template>

  <xsl:template match="gmd:LI_ProcessStep/gmd:source" mode="from19139to19115-3.2018">
    <mrl:source>
      <xsl:apply-templates select="@*" mode="from19139to19115-3.2018"/>
      <xsl:if test="*[1]">
        <xsl:variable name="lineageElement" select="concat('mrl:',local-name(*[1]))"/>
        <xsl:element name="{$lineageElement}">
          <xsl:copy-of select="*[1]/@*"/>
          <xsl:call-template name="writeCharacterStringElement">
            <xsl:with-param name="elementName" select="'mrl:description'"/>
            <xsl:with-param name="nodeWithStringToWrite" select="./*/gmd:description"/>
          </xsl:call-template>
          <xsl:apply-templates select="./*/gmd:scaleDenominator" mode="from19139to19115-3.2018"/>
          <xsl:apply-templates select="./*/gmd:sourceReferenceSystem" mode="from19139to19115-3.2018"/>
          <xsl:apply-templates select="./*/gmd:sourceCitation" mode="from19139to19115-3.2018"/>
          <xsl:apply-templates select="./*/gmd:sourceExtent" mode="from19139to19115-3.2018"/>
          <xsl:apply-templates select="./*/gmd:sourceStep" mode="from19139to19115-3.2018"/>
          <xsl:apply-templates select="./*/gmi:processedLevel" mode="from19139to19115-3.2018"/>
          <xsl:apply-templates select="./*/gmi:resolution" mode="from19139to19115-3.2018"/>
        </xsl:element>
      </xsl:if>
    </mrl:source>
  </xsl:template>

  <xsl:template match="gmd:LI_Source/gmd:scaleDenominator | gmi:LE_Source/gmd:scaleDenominator" mode="from19139to19115-3.2018">
    <mrl:sourceSpatialResolution>
      <mri:MD_Resolution>
        <mri:equivalentScale>
          <xsl:apply-templates mode="from19139to19115-3.2018"/>
        </mri:equivalentScale>
      </mri:MD_Resolution>
    </mrl:sourceSpatialResolution>
  </xsl:template>

  <xsl:template match="gmd:LI_Source/gmd:sourceExtent | gmi:LE_Source/gmd:sourceExtent" mode="from19139to19115-3.2018">
    <mrl:scope>
      <mcc:MD_Scope>
        <mcc:level/>
        <mcc:extent>
          <xsl:apply-templates mode="from19139to19115-3.2018"/>
        </mcc:extent>
        <mcc:levelDescription/>
      </mcc:MD_Scope>
    </mrl:scope>
  </xsl:template>

  <xsl:template match="gmi:LE_ProcessStep" mode="from19139to19115-3.2018">
    <xsl:element name="mrl:LE_ProcessStep">
      <xsl:apply-templates select="@*" mode="from19139to19115-3.2018"/>
      <xsl:apply-templates select="gmd:description" mode="from19139to19115-3.2018"/>
      <xsl:apply-templates select="gmd:dateTime" mode="from19139to19115-3.2018"/>
      <xsl:apply-templates select="gmd:processor" mode="from19139to19115-3.2018"/>
      <xsl:apply-templates select="gmd:source" mode="from19139to19115-3.2018"/>
      <xsl:apply-templates select="gmi:processingInformation" mode="from19139to19115-3.2018"/>
      <xsl:apply-templates select="gmi:report" mode="from19139to19115-3.2018"/>
      <xsl:apply-templates select="gmi:output" mode="from19139to19115-3.2018"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="gmd:LI_ProcessStep/gmd:dateTime | gmi:LE_ProcessStep/gmd:dateTime" mode="from19139to19115-3.2018" priority="5">
    <mrl:stepDateTime>
      <gml:TimeInstant>
        <xsl:attribute name="gml:id">
          <xsl:value-of select="generate-id()"/>
        </xsl:attribute>
        <gml:timePosition>
          <xsl:value-of select="./gcoold:DateTime"/>
        </gml:timePosition>
      </gml:TimeInstant>
    </mrl:stepDateTime>
  </xsl:template>

  <xsl:template match="gmi:LE_Processing" mode="from19139to19115-3.2018">
    <xsl:element name="mrl:LE_Processing">
      <xsl:apply-templates select="@*" mode="from19139to19115-3.2018"/>
      <xsl:apply-templates select="gmi:algorithm" mode="from19139to19115-3.2018"/>
      <xsl:apply-templates select="gmi:identifier" mode="from19139to19115-3.2018"/>
      <xsl:apply-templates select="gmi:softwareReference" mode="from19139to19115-3.2018"/>
      <xsl:apply-templates select="gmi:procedureDescription" mode="from19139to19115-3.2018"/>
      <xsl:apply-templates select="gmi:documentation" mode="from19139to19115-3.2018"/>
      <xsl:apply-templates select="gmi:runTimeParameters" mode="from19139to19115-3.2018"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="gmd:report/gmd:DQ_QuantitativeAttributeAccuracy/gmd:result/gmi:QE_CoverageResult//gmd:attributeDescription" priority="5" mode="from19139to19115-3.2018"/>

</xsl:stylesheet>
