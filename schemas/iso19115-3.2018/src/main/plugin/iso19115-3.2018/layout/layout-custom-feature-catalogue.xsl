<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
  xmlns:gfc="http://standards.iso.org/iso/19110/gfc/1.1"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  exclude-result-prefixes="#all">


  <xsl:template mode="mode-iso19115-3.2018"
                match="gfc:featureType"
                priority="2000">

    <xsl:variable name="isFirstFeatureType"
                  select="preceding-sibling::*[1]/name() != 'gfc:featureType'"
                  as="xs:boolean"/>

    <xsl:if test="$isFirstFeatureType">
      <xsl:variable name="selector" as="node()*">
        <select name="selectFeaturetype"
                icon="fa gn-icon-featureCatalog"
                xpath="/gfc:FC_FeatureCatalogue/gfc:featureType"
                parameter="featureType"
                value="*/gfc:typeName/text()"
                layout="dropdown"/>
      </xsl:variable>
      <xsl:apply-templates mode="form-builder"
                           select="$selector">
        <xsl:with-param name="base" select="ancestor::gfc:FC_FeatureCatalogue"/>
      </xsl:apply-templates>
    </xsl:if>

    <xsl:variable name="isRequestedFeatureType"
                  select="*/gfc:typeName = $request/featureType"/>

    <xsl:variable name="isRequestedFeatureTypeInCurrentRecord"
                  select="count(../gfc:featureType[*/gfc:typeName = $request/featureType]) > 0"/>

    <xsl:if test="$isRequestedFeatureType
                  or (not($isRequestedFeatureTypeInCurrentRecord) and $isFirstFeatureType)
                  or ($service = 'md.format.html')">
      <xsl:call-template name="mode-iso19115-3.2018-fieldset"/>
    </xsl:if>
  </xsl:template>
</xsl:stylesheet>
