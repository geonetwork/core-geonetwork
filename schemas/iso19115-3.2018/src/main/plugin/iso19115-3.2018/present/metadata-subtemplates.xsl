<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
  xmlns:mds="http://standards.iso.org/iso/19115/-3/mds/2.0"
  xmlns:mri="http://standards.iso.org/iso/19115/-3/mri/1.0"
  xmlns:cit="http://standards.iso.org/iso/19115/-3/cit/2.0"
  xmlns:mrd="http://standards.iso.org/iso/19115/-3/mrd/1.0"
  xmlns:mco="http://standards.iso.org/iso/19115/-3/mco/1.0"
  xmlns:gex="http://standards.iso.org/iso/19115/-3/gex/1.0"
  xmlns:geonet="http://www.fao.org/geonetwork" 
  exclude-result-prefixes="mds mri cit mrd mco gex gco geonet">
  
  <!-- Compute title for all type of subtemplates. If none defined, 
  the title from the metadata title column is used. -->
  <xsl:template name="iso19115-3.2018-subtemplate">
    
    <xsl:variable name="subTemplateTitle">
      <xsl:apply-templates mode="iso19115-3.2018-subtemplate" select="."/>
    </xsl:variable>
    
    <title>
      <xsl:choose>
        <xsl:when test="normalize-space($subTemplateTitle)!=''">
          <xsl:value-of select="$subTemplateTitle"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="geonet:info/title"/>
        </xsl:otherwise>
      </xsl:choose>
    </title>
  </xsl:template>
  
  <!-- Subtemplate mode -->
  <xsl:template mode="iso19115-3.2018-subtemplate" match="cit:CI_Responsibility">
    <!-- TODO : multilingual subtemplate are not supported. There is
      no locale element -->
    <xsl:variable name="langId">
      <xsl:call-template name="getLangId">
        <xsl:with-param name="langGui" select="/root/gui/language"/>
        <xsl:with-param name="md" select="."/>
      </xsl:call-template>
    </xsl:variable>
    
    <xsl:apply-templates mode="localised" select="cit:name[ancestor::cit:CI_Organisation]">
      <xsl:with-param name="langId" select="$langId"/>
    </xsl:apply-templates>
    
    <!-- Concatenate email or name -->
    <xsl:choose>
      <xsl:when test="count(cit:party/*/cit:contactInfo/cit:CI_Contact/cit:address/cit:CI_Address/cit:electronicMailAddress/gco:CharacterString[normalize-space(.)!='']) > 0">
        <xsl:text> > </xsl:text>
        <xsl:value-of select="string-join(cit:party/*/cit:contactInfo/cit:CI_Contact/cit:address/cit:CI_Address/cit:electronicMailAddress/gco:CharacterString, ',')"/>
      </xsl:when>
      <xsl:when test="normalize-space(cit:party/*/cit:name/gco:CharacterString)!=''">
        <xsl:text> > </xsl:text>
        <xsl:apply-templates mode="localised" select="cit:party/*/cit:name">
          <xsl:with-param name="langId" select="$langId"/>
        </xsl:apply-templates>
      </xsl:when>
      <xsl:when test="normalize-space(cit:party/cit:CI_Individual/cit:positionName/gco:CharacterString)!=''">
        <xsl:text> > </xsl:text>
        <xsl:apply-templates mode="localised" select="cit:party/cit:CI_Individual/cit:positionName">
          <xsl:with-param name="langId" select="$langId"/>
        </xsl:apply-templates>
      </xsl:when>
      <xsl:otherwise>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template mode="iso19115-3.2018-subtemplate" match="gex:EX_Extent">
    <!-- TODO : multilingual subtemplate are not supported. There is
      no gmd:language element or gmd:locales -->
    <xsl:variable name="langId">
      <xsl:call-template name="getLangId">
        <xsl:with-param name="langGui" select="/root/gui/language"/>
        <xsl:with-param name="md" select="."/>
      </xsl:call-template>
    </xsl:variable>
    
    <xsl:apply-templates mode="localised" select="gex:description">
      <xsl:with-param name="langId" select="$langId"/>
    </xsl:apply-templates>
  </xsl:template>
  
  <xsl:template mode="iso19115-3.2018-subtemplate" match="mri:MD_Keywords">
    <xsl:variable name="langId">
      <xsl:call-template name="getLangId">
        <xsl:with-param name="langGui" select="/root/gui/language"/>
        <xsl:with-param name="md" select="."/>
      </xsl:call-template>
    </xsl:variable>
    
    <xsl:for-each select="mri:keyword">
      <xsl:apply-templates mode="localised" select=".">
        <xsl:with-param name="langId" select="$langId"/>
      </xsl:apply-templates>
      <xsl:if test="position() != last()">
        <xsl:text>, </xsl:text>
      </xsl:if>
    </xsl:for-each>
  </xsl:template>
  
  <xsl:template mode="iso19115-3.2018-subtemplate" match="mrd:MD_Distribution">
    <xsl:value-of
      select="string-join(mrd:transferOptions/mrd:MD_DigitalTransferOptions/mrd:onLine/cit:CI_OnlineResource/cit:linkage/*, ' ,')"
    />
  </xsl:template>
  
  <xsl:template mode="iso19115-3.2018-subtemplate" match="mco:MD_LegalConstraints">
    <xsl:value-of
      select="if (mco:useLimitation) then mco:useLimitation/* else mco:otherConstraints/*"
    />
  </xsl:template>
  
  
  <xsl:template mode="iso19115-3.2018-subtemplate" match="*"/>
  
</xsl:stylesheet>
