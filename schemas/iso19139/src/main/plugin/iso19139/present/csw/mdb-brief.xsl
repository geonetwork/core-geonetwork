<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                exclude-result-prefixes="#all">

  <xsl:import href="../../../iso19115-3.2018/convert/ISO19139/fromISO19139.xsl"/>
  <xsl:import href="../../../iso19115-3.2018/present/csw/mdb-brief.xsl"/>

  <xsl:template match="/" priority="99">
    <xsl:variable name="iso19115record">
      <xsl:for-each select="//gmd:MD_Metadata">
        <xsl:variable name="nameSpacePrefix">
          <xsl:call-template name="getNamespacePrefix"/>
        </xsl:variable>

        <xsl:element name="mdb:MD_Metadata">
          <!-- new namespaces -->
          <xsl:call-template name="add-iso19115-3.2018-namespaces"/>

          <xsl:apply-templates select="gmd:fileIdentifier" mode="from19139to19115-3.2018"/>
          <xsl:apply-templates select="gmd:language" mode="from19139to19115-3.2018"/>
          <xsl:apply-templates select="gmd:characterSet" mode="from19139to19115-3.2018"/>
          <xsl:apply-templates select="gmd:parentIdentifier" mode="from19139to19115-3.2018"/>
          <xsl:apply-templates select="gmd:hierarchyLevel" mode="from19139to19115-3.2018"/>
          <xsl:apply-templates select="gmd:contact" mode="from19139to19115-3.2018"/>
          <xsl:apply-templates select="gmd:dateStamp" mode="from19139to19115-3.2018"/>
          <xsl:apply-templates select="gmd:metadataStandardName" mode="from19139to19115-3.2018"/>
          <xsl:apply-templates select="gmd:locale" mode="from19139to19115-3.2018"/>
          <xsl:apply-templates select="gmd:spatialRepresentationInfo" mode="from19139to19115-3.2018"/>
          <xsl:apply-templates select="gmd:referenceSystemInfo" mode="from19139to19115-3.2018"/>
          <xsl:apply-templates select="gmd:metadataExtensionInfo" mode="from19139to19115-3.2018"/>
          <xsl:apply-templates select="gmd:identificationInfo" mode="from19139to19115-3.2018"/>
          <xsl:apply-templates select="gmd:contentInfo" mode="from19139to19115-3.2018"/>
          <xsl:call-template name="onlineSourceDispatcher">
            <xsl:with-param name="type" select="'featureCatalogueCitation'"/>
          </xsl:call-template>

          <xsl:apply-templates select="gmd:distributionInfo" mode="from19139to19115-3.2018"/>
          <xsl:apply-templates select="gmd:dataQualityInfo" mode="from19139to19115-3.2018"/>
          <xsl:apply-templates select="gmd:portrayalCatalogueInfo" mode="from19139to19115-3.2018"/>
          <xsl:call-template name="onlineSourceDispatcher">
            <xsl:with-param name="type" select="'portrayalCatalogueCitation'"/>
          </xsl:call-template>

          <xsl:apply-templates select="gmd:metadataConstraints" mode="from19139to19115-3.2018"/>
          <xsl:apply-templates select="gmd:applicationSchemaInfo" mode="from19139to19115-3.2018"/>
          <xsl:apply-templates select="gmd:metadataMaintenance" mode="from19139to19115-3.2018"/>
        </xsl:element>
      </xsl:for-each>
    </xsl:variable>

    <xsl:for-each select="$iso19115record">
      <xsl:apply-templates select="*"/>
    </xsl:for-each>
  </xsl:template>
</xsl:stylesheet>
