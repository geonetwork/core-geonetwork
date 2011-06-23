<xsl:stylesheet version="2.0" xmlns:xsl ="http://www.w3.org/1999/XSL/Transform"
  xmlns:gmd="http://www.isotc211.org/2005/gmd"
  xmlns:gco="http://www.isotc211.org/2005/gco"
  exclude-result-prefixes="gmd gco">
  
  <!-- Subtemplate mode TODO : move to another XSL -->
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
      </xsl:apply-templates> - 
      <xsl:apply-templates mode="localised" select="gmd:individualName">
        <xsl:with-param name="langId" select="$langId"></xsl:with-param>
      </xsl:apply-templates>
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
  <xsl:template mode="iso19139-subtemplate" match="*"/>
  
</xsl:stylesheet>