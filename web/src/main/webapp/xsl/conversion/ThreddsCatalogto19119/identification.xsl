<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:srv="http://www.isotc211.org/2005/srv"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:gml="http://www.opengis.net/gml"
                xmlns:math="http://exslt.org/math"
                xmlns:date="http://exslt.org/dates-and-times"
                xmlns:exslt="http://exslt.org/common"
                version="1.0"
                extension-element-prefixes="math exslt gml date">

  <!-- =================================================================== -->

  <xsl:template match="*" mode="SrvDataIdentification">
    <xsl:param name="topic"/>
    <xsl:param name="name"/>
    <xsl:param name="type"/>
    <xsl:param name="desc"/>
    <xsl:param name="props"/>
    <xsl:param name="version"/>
    <xsl:param name="serverops"/>


    <gmd:citation>
      <gmd:CI_Citation>
        <gmd:title>
          <gco:CharacterString>
            <xsl:value-of select="$name"/>
          </gco:CharacterString>
        </gmd:title>
        <gmd:date>
          <gmd:CI_Date>
            <xsl:variable name="df">[Y0001]-[M01]-[D01]T[H01]:[m01]:[s01]</xsl:variable>
            <gmd:date>
              <gco:DateTime>
                <xsl:value-of select="format-dateTime(current-dateTime(),$df)"/>
              </gco:DateTime>
            </gmd:date>
            <gmd:dateType>
              <gmd:CI_DateTypeCode codeList="./resources/codeList.xml#CI_DateTypeCode"
                                   codeListValue="revision"/>
            </gmd:dateType>
          </gmd:CI_Date>
        </gmd:date>
      </gmd:CI_Citation>
    </gmd:citation>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <gmd:abstract>
      <gco:CharacterString>
        <xsl:value-of select="concat('Description: ',$desc,' Properties : ',$props)"/>
      </gco:CharacterString>
    </gmd:abstract>

    <!--idPurp-->

    <gmd:status>
      <gmd:MD_ProgressCode codeList="./resources/codeList.xml#MD_ProgressCode"
                           codeListValue="completed"/>
    </gmd:status>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <xsl:for-each select="//ContactInformation">
      <gmd:pointOfContact>
        <gmd:CI_ResponsibleParty>
          <xsl:apply-templates select="." mode="RespParty"/>
        </gmd:CI_ResponsibleParty>
      </gmd:pointOfContact>
    </xsl:for-each>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
    <srv:serviceType>
      <gco:LocalName codeSpace="www.w3c.org">
        <xsl:value-of select="$type"/>
      </gco:LocalName>
    </srv:serviceType>
    <srv:serviceTypeVersion>
      <gco:CharacterString>
        <xsl:value-of select="$version"/>
      </gco:CharacterString>
    </srv:serviceTypeVersion>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <srv:accessProperties>
      <gmd:MD_StandardOrderProcess>
        <gmd:fees>
          <gco:CharacterString>Free</gco:CharacterString>
        </gmd:fees>
      </gmd:MD_StandardOrderProcess>
    </srv:accessProperties>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <srv:couplingType>
      <srv:SV_CouplingType codeList="#SV_CouplingType" codeListValue="tight">
        tight
      </srv:SV_CouplingType>
    </srv:couplingType>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <srv:containsOperations>
      <srv:SV_OperationMetadata>

        <xsl:choose>
          <xsl:when test="contains($type,'OPENDAP')">
            <xsl:if test="contains($serverops,'das')">
              <xsl:call-template name="opendapOps">
                <xsl:with-param name="op" select="'das'"/>
                <xsl:with-param name="title" select="'dataset attribute structure'"/>
              </xsl:call-template>
            </xsl:if>
            <xsl:if test="contains($serverops,'dds')">
              <xsl:call-template name="opendapOps">
                <xsl:with-param name="op" select="'dds'"/>
                <xsl:with-param name="title" select="'dataset descriptor structure'"/>
              </xsl:call-template>
            </xsl:if>
            <xsl:if test="contains($serverops,'dods')">
              <xsl:call-template name="opendapOps">
                <xsl:with-param name="op" select="'dods'"/>
                <xsl:with-param name="title"
                                select="'dataset descriptor structure populated with data using a constraint'"/>
              </xsl:call-template>
            </xsl:if>
            <xsl:if test="contains($serverops,'ddx')">
              <xsl:call-template name="opendapOps">
                <xsl:with-param name="op" select="'ddx'"/>
                <xsl:with-param name="title"
                                select="'XML version of dataset descriptor structure (dds) and dataset attribute structure (das)'"/>
              </xsl:call-template>
            </xsl:if>
            <xsl:if test="contains($serverops,'blob')">
              <xsl:call-template name="opendapOps">
                <xsl:with-param name="op" select="'blob'"/>
                <xsl:with-param name="title"
                                select="'Serialized binary data content for requested data set, with a constraint expression applied'"/>
              </xsl:call-template>
            </xsl:if>
            <xsl:if test="contains($serverops,'info')">
              <xsl:call-template name="opendapOps">
                <xsl:with-param name="op" select="'info'"/>
                <xsl:with-param name="title"
                                select="'info object (attributes, types and other information)'"/>
              </xsl:call-template>
            </xsl:if>
            <xsl:if test="contains($serverops,'html')">
              <xsl:call-template name="opendapOps">
                <xsl:with-param name="op" select="'html'"/>
                <xsl:with-param name="title" select="'html input form for a dataset'"/>
              </xsl:call-template>
            </xsl:if>
            <xsl:if test="contains($serverops,'ver')">
              <xsl:call-template name="opendapOps">
                <xsl:with-param name="op" select="'ver'"/>
                <xsl:with-param name="title" select="'return the version number of the server'"/>
              </xsl:call-template>
            </xsl:if>
            <xsl:if test="contains($serverops,'help')">
              <xsl:call-template name="opendapOps">
                <xsl:with-param name="op" select="'help'"/>
                <xsl:with-param name="title" select="'return help from server'"/>
              </xsl:call-template>
            </xsl:if>
          </xsl:when>
          <xsl:otherwise>
            <srv:operationName>
              <gco:CharacterString>
                <xsl:value-of select="$name"/>
              </gco:CharacterString>
            </srv:operationName>
            <srv:DCP>
              <srv:DCPList codeList="#DCPList">
                <xsl:attribute name="codeListValue">
                  <xsl:value-of select="http"/>
                </xsl:attribute>
              </srv:DCPList>
            </srv:DCP>
            <srv:connectPoint>
              <gmd:CI_OnlineResource>
                <gmd:linkage>
                  <gmd:URL>
                    <xsl:value-of select="$url"/>
                  </gmd:URL>
                </gmd:linkage>
                <gmd:protocol>
                  <gco:CharacterString>
                    WWW:LINK-1.0-http--link
                  </gco:CharacterString>
                </gmd:protocol>
                <gmd:description>
                  <gco:CharacterString>
                    <xsl:value-of select="$desc"/>
                  </gco:CharacterString>
                </gmd:description>
                <gmd:function>
                  <gmd:CI_OnLineFunctionCode
                    codeList="./resources/codeList.xml#CI_OnLineFunctionCode"
                    codeListValue="information"/>
                </gmd:function>
              </gmd:CI_OnlineResource>
            </srv:connectPoint>
          </xsl:otherwise>
        </xsl:choose>
      </srv:SV_OperationMetadata>
    </srv:containsOperations>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        <srv:operatesOn> is done by harvester after data metadata creation
          -->

  </xsl:template>

  <!-- =================================================================== -->
  <!-- === opendapOps === -->
  <!-- =================================================================== -->

  <xsl:template name="opendapOps">
    <xsl:param name="op"/>
    <xsl:param name="title"/>

    <srv:operationName>
      <gco:CharacterString>
        <xsl:value-of select="concat($op,' - ',$title)"/>
      </gco:CharacterString>
    </srv:operationName>
    <srv:DCP>
      <srv:DCPList codeList="#DCPList">
        <xsl:attribute name="codeListValue">
          <xsl:value-of select="http"/>
        </xsl:attribute>
      </srv:DCPList>
    </srv:DCP>
    <srv:connectPoint>
      <gmd:CI_OnlineResource>
        <gmd:linkage>
          <gmd:URL>
            <xsl:value-of select="concat($url,'/help')"/>
          </gmd:URL>
        </gmd:linkage>
        <gmd:protocol>
          <gco:CharacterString>
            WWW:LINK-1.0-http--link
          </gco:CharacterString>
        </gmd:protocol>
        <gmd:description>
          <gco:CharacterString>
            <xsl:value-of select="concat('Help on ',$op,' - ',$title)"/>
          </gco:CharacterString>
        </gmd:description>
        <gmd:function>
          <gmd:CI_OnLineFunctionCode codeList="./resources/codeList.xml#CI_OnLineFunctionCode"
                                     codeListValue="information"/>
        </gmd:function>
      </gmd:CI_OnlineResource>
    </srv:connectPoint>
  </xsl:template>


  <!-- =================================================================== -->
  <!-- === LayerDataIdentification === -->
  <!-- =================================================================== -->

  <xsl:template match="*" mode="LayerDataIdentification">
    <xsl:param name="Name"/>
    <xsl:param name="topic"/>

    <gmd:citation>
      <gmd:CI_Citation>
        <gmd:title>
          <gco:CharacterString>
          </gco:CharacterString>
        </gmd:title>
        <gmd:date>
          <gmd:CI_Date>
            <xsl:variable name="df">[Y0001]-[M01]-[D01]T[H01]:[m01]:[s01]</xsl:variable>
            <gmd:date>
              <gco:DateTime>
                <xsl:value-of select="format-dateTime(current-dateTime(),$df)"/>
              </gco:DateTime>
            </gmd:date>
            <gmd:dateType>
              <gmd:CI_DateTypeCode codeList="./resources/codeList.xml#CI_DateTypeCode"
                                   codeListValue="revision"/>
            </gmd:dateType>
          </gmd:CI_Date>
        </gmd:date>
      </gmd:CI_Citation>
    </gmd:citation>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <gmd:abstract>
      <gco:CharacterString>
      </gco:CharacterString>
    </gmd:abstract>

    <!--idPurp-->

    <gmd:status>
      <gmd:MD_ProgressCode codeList="./resources/codeList.xml#MD_ProgressCode"
                           codeListValue="completed"/>
    </gmd:status>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <xsl:for-each select="Service/ContactInformation">
      <gmd:pointOfContact>
        <gmd:CI_ResponsibleParty>
          <xsl:apply-templates select="." mode="RespParty"/>
        </gmd:CI_ResponsibleParty>
      </gmd:pointOfContact>
    </xsl:for-each>

    <gmd:descriptiveKeywords>
      <gmd:MD_Keywords>
      </gmd:MD_Keywords>
    </gmd:descriptiveKeywords>

    <gmd:spatialRepresentationType>
      <gmd:MD_SpatialRepresentationTypeCode
        codeList="./resources/codeList.xml#MD_SpatialRepresentationTypeCode" codeListValue="grid"/>
    </gmd:spatialRepresentationType>

    <gmd:language gco:nilReason="missing">
      <gco:CharacterString/>
    </gmd:language>

    <gmd:characterSet>
      <gmd:MD_CharacterSetCode
        codeList="http://www.isotc211.org/2005/resources/codeList.xml#MD_CharacterSetCode"
        codeListValue=""/>
    </gmd:characterSet>

    <gmd:topicCategory>
      <gmd:MD_TopicCategoryCode>
        <xsl:value-of select="$topic"/>
      </gmd:MD_TopicCategoryCode>
    </gmd:topicCategory>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
    <gmd:extent>
      <gmd:EX_Extent>
        <gmd:geographicElement>
          <gmd:EX_GeographicBoundingBox>
            <gmd:westBoundLongitude>
              <gco:Decimal></gco:Decimal>
            </gmd:westBoundLongitude>
            <gmd:eastBoundLongitude>
              <gco:Decimal></gco:Decimal>
            </gmd:eastBoundLongitude>
            <gmd:southBoundLatitude>
              <gco:Decimal></gco:Decimal>
            </gmd:southBoundLatitude>
            <gmd:northBoundLatitude>
              <gco:Decimal></gco:Decimal>
            </gmd:northBoundLatitude>
          </gmd:EX_GeographicBoundingBox>
        </gmd:geographicElement>
      </gmd:EX_Extent>
    </gmd:extent>

  </xsl:template>

  <!-- =================================================================== -->
  <!-- === Keywords === -->
  <!-- =================================================================== -->

  <xsl:template match="*" mode="Keywords">
    <xsl:for-each select="Keyword">
      <gmd:keyword>
        <gco:CharacterString>
          <xsl:value-of select="."/>
        </gco:CharacterString>
      </gmd:keyword>
    </xsl:for-each>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <gmd:type>
      <gmd:MD_KeywordTypeCode codeList="./resources/codeList.xml#MD_KeywordTypeCode"
                              codeListValue="theme"/>
    </gmd:type>

  </xsl:template>

  <!-- ================================================================== -->
  <!-- === Usage === -->
  <!-- ================================================================== -->

  <xsl:template match="*" mode="Usage">

    <gmd:specificUsage>
      <gco:CharacterString>
        <xsl:value-of select="specUsage"/>
      </gco:CharacterString>
    </gmd:specificUsage>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <xsl:for-each select="usageDate">
      <gmd:usageDateTime>
        <gco:DateTime>
          <xsl:value-of select="."/>
        </gco:DateTime>
      </gmd:usageDateTime>
    </xsl:for-each>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <xsl:for-each select="usrDetLim">
      <gmd:userDeterminedLimitations>
        <gco:CharacterString>
          <xsl:value-of select="."/>
        </gco:CharacterString>
      </gmd:userDeterminedLimitations>
    </xsl:for-each>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <xsl:for-each select="usrCntInfo">
      <gmd:userContactInfo>
        <gmd:CI_ResponsibleParty>
          <xsl:apply-templates select="." mode="RespParty"/>
        </gmd:CI_ResponsibleParty>
      </gmd:userContactInfo>
    </xsl:for-each>

  </xsl:template>

  <!-- === Resol === -->

  <xsl:template match="*" mode="Resol">

    <xsl:for-each select="equScale">
      <gmd:equivalentScale>
        <gmd:MD_RepresentativeFraction>
          <gmd:denominator>
            <gco:Integer>
              <xsl:value-of select="rfDenom"/>
            </gco:Integer>
          </gmd:denominator>
        </gmd:MD_RepresentativeFraction>
      </gmd:equivalentScale>
    </xsl:for-each>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <xsl:for-each select="scaleDist">
      <gmd:distance>
        <gco:Distance>
          <xsl:apply-templates select="." mode="Measure"/>
        </gco:Distance>
      </gmd:distance>
    </xsl:for-each>

  </xsl:template>

  <!-- =================================================================== -->

</xsl:stylesheet>
