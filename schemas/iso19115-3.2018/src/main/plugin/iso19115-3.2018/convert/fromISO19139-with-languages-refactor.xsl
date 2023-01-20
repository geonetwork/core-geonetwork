<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:gcoold="http://www.isotc211.org/2005/gco"
                xmlns:gmi="http://www.isotc211.org/2005/gmi"
                xmlns:gmx="http://www.isotc211.org/2005/gmx"
                xmlns:gsr="http://www.isotc211.org/2005/gsr"
                xmlns:gss="http://www.isotc211.org/2005/gss"
                xmlns:gts="http://www.isotc211.org/2005/gts"
                xmlns:cat="http://standards.iso.org/iso/19115/-3/cat/1.0"
                xmlns:cit="http://standards.iso.org/iso/19115/-3/cit/2.0"
                xmlns:gcx="http://standards.iso.org/iso/19115/-3/gcx/1.0"
                xmlns:gex="http://standards.iso.org/iso/19115/-3/gex/1.0"
                xmlns:lan="http://standards.iso.org/iso/19115/-3/lan/1.0"
                xmlns:srv="http://standards.iso.org/iso/19115/-3/srv/2.1"
                xmlns:mac="http://standards.iso.org/iso/19115/-3/mac/2.0"
                xmlns:mas="http://standards.iso.org/iso/19115/-3/mas/1.0"
                xmlns:mcc="http://standards.iso.org/iso/19115/-3/mcc/1.0"
                xmlns:mco="http://standards.iso.org/iso/19115/-3/mco/1.0"
                xmlns:mda="http://standards.iso.org/iso/19115/-3/mda/1.0"
                xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0"
                xmlns:mdt="http://standards.iso.org/iso/19115/-3/mdt/1.0"
                xmlns:mex="http://standards.iso.org/iso/19115/-3/mex/1.0"
                xmlns:mic="http://standards.iso.org/iso/19115/-3/mic/1.0"
                xmlns:mil="http://standards.iso.org/iso/19115/-3/mil/1.0"
                xmlns:mrl="http://standards.iso.org/iso/19115/-3/mrl/1.0"
                xmlns:mds="http://standards.iso.org/iso/19115/-3/mds/2.0"
                xmlns:mmi="http://standards.iso.org/iso/19115/-3/mmi/1.0"
                xmlns:mpc="http://standards.iso.org/iso/19115/-3/mpc/1.0"
                xmlns:mrc="http://standards.iso.org/iso/19115/-3/mrc/1.0"
                xmlns:mrd="http://standards.iso.org/iso/19115/-3/mrd/1.0"
                xmlns:mri="http://standards.iso.org/iso/19115/-3/mri/1.0"
                xmlns:mrs="http://standards.iso.org/iso/19115/-3/mrs/1.0"
                xmlns:msr="http://standards.iso.org/iso/19115/-3/msr/2.0"
                xmlns:mai="http://standards.iso.org/iso/19115/-3/mai/1.0"
                xmlns:mdq="http://standards.iso.org/iso/19157/-2/mdq/1.0"
                xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0">
  <xsl:include href="ISO19139/fromISO19139.xsl"/>
  <xsl:include href="../process/languages-refactor.xsl"/>


  <xsl:template match="/" priority="999">
    <xsl:variable name="iso19115Record">
      <xsl:for-each select="//(gmd:MD_Metadata|gmi:MI_Metadata)">
        <xsl:variable name="nameSpacePrefix">
          <xsl:call-template name="getNamespacePrefix"/>
        </xsl:variable>

        <xsl:element name="mdb:MD_Metadata">
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
          <xsl:apply-templates select="gmi:acquisitionInformation" mode="from19139to19115-3.2018"/>
        </xsl:element>
      </xsl:for-each>
    </xsl:variable>

    <xsl:apply-templates mode="language-add"
                         select="$iso19115Record"/>
  </xsl:template>
</xsl:stylesheet>
