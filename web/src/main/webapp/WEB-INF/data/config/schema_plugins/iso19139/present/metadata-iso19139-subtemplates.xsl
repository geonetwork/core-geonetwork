<xsl:stylesheet version="2.0" xmlns:xsl ="http://www.w3.org/1999/XSL/Transform"
  xmlns:gmd="http://www.isotc211.org/2005/gmd"
  xmlns:gco="http://www.isotc211.org/2005/gco"
	xmlns:geonet="http://www.fao.org/geonetwork"
  exclude-result-prefixes="gmd gco geonet">

	<xsl:template name="iso19139-subtemplate">
		<xsl:choose>
			<xsl:when test="normalize-space(geonet:info/title)!=''">
				<title><xsl:value-of select="geonet:info/title"/></title>
			</xsl:when>
			<xsl:otherwise>
				<xsl:apply-templates mode="iso19139-subtemplate" select="."/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
  
  <!-- Subtemplate mode -->
  <xsl:template mode="iso19139-subtemplate" match="gmd:CI_ResponsibleParty">
    <!-- TODO : multilingual subtemplate are not supported. There is
      no gmd:language element or gmd:locales -->
    <xsl:variable name="langId">
      <xsl:call-template name="getLangId">
        <xsl:with-param name="langGui" select="/root/gui/language"/>
        <xsl:with-param name="md" select="."/>
      </xsl:call-template>
    </xsl:variable>
    <title>
      <xsl:apply-templates mode="localised" select="gmd:organisationName">
        <xsl:with-param name="langId" select="$langId"></xsl:with-param>
      </xsl:apply-templates>
      -
      <xsl:choose>
        <xsl:when test="normalize-space(gmd:individualName/gco:CharacterString)!=''">
          <xsl:apply-templates mode="localised" select="gmd:individualName">
            <xsl:with-param name="langId" select="$langId"></xsl:with-param>
          </xsl:apply-templates>
        </xsl:when>
        <xsl:otherwise>
          <xsl:apply-templates mode="localised" select="gmd:positionName">
            <xsl:with-param name="langId" select="$langId"></xsl:with-param>
          </xsl:apply-templates>
        </xsl:otherwise>
      </xsl:choose>
    </title>
  </xsl:template>
  <xsl:template mode="iso19139-subtemplate" match="gmd:EX_Extent">
    <!-- TODO : multilingual subtemplate are not supported. There is
      no gmd:language element or gmd:locales -->
    <xsl:variable name="langId">
      <xsl:call-template name="getLangId">
        <xsl:with-param name="langGui" select="/root/gui/language"/>
        <xsl:with-param name="md" select="."/>
      </xsl:call-template>
    </xsl:variable>
    <title>
      <xsl:apply-templates mode="localised" select="gmd:description">
        <xsl:with-param name="langId" select="$langId"></xsl:with-param>
      </xsl:apply-templates>
    </title>
  </xsl:template>
  <xsl:template mode="iso19139-subtemplate" match="gmd:MD_Keywords">
    <xsl:variable name="langId">
      <xsl:call-template name="getLangId">
        <xsl:with-param name="langGui" select="/root/gui/language"/>
        <xsl:with-param name="md" select="."/>
      </xsl:call-template>
    </xsl:variable>
    <title>
      <xsl:for-each select="gmd:keyword">
        <xsl:apply-templates mode="localised" select=".">
          <xsl:with-param name="langId" select="$langId"></xsl:with-param>
        </xsl:apply-templates>
        <xsl:if test="position() != last()"><xsl:text>, </xsl:text></xsl:if>
      </xsl:for-each>
    </title>
  </xsl:template>
  <xsl:template mode="iso19139-subtemplate" match="gmd:MD_Distribution">
    <title>
      <xsl:value-of select="string-join(gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource/gmd:linkage/gmd:URL, ' ,')"/>
    </title>
  </xsl:template>
  <xsl:template mode="iso19139-subtemplate" match="*"/>
  
</xsl:stylesheet>
