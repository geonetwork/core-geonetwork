<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:csw="http://www.opengis.net/cat/csw/2.0.2"
  xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0"
  xmlns:mri="http://standards.iso.org/iso/19115/-3/mri/1.0"
  xmlns:cit="http://standards.iso.org/iso/19115/-3/cit/2.0"
  xmlns:mcc="http://standards.iso.org/iso/19115/-3/mcc/1.0"
  xmlns:gex="http://standards.iso.org/iso/19115/-3/gex/1.0"
  xmlns:srv2="http://standards.iso.org/iso/19115/-3/srv/2.0"
  xmlns:mco="http://standards.iso.org/iso/19115/-3/mco/1.0"
  xmlns:mrd="http://standards.iso.org/iso/19115/-3/mrd/1.0"
  xmlns:lan="http://standards.iso.org/iso/19115/-3/lan/1.0"
  xmlns:mrl="http://standards.iso.org/iso/19115/-3/mrl/2.0"
  xmlns:mrs="http://standards.iso.org/iso/19115/-3/mrs/1.0"
  xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
  xmlns:gmd="http://www.isotc211.org/2005/gmd"
  xmlns:srv="http://www.isotc211.org/2005/srv"
  xmlns:ows="http://www.opengis.net/ows"
  xmlns:gn="http://www.fao.org/geonetwork"
  exclude-result-prefixes="#all">

  <xsl:import href="../../convert/ISO19139/toISO19139.xsl"/>

  <xsl:param name="displayInfo"/>

  <xsl:template match="/">
    <xsl:for-each select="/*">
      <xsl:variable name="info" select="gn:info"/>
      <xsl:variable name="nameSpacePrefix">
        <xsl:call-template name="getNamespacePrefix"/>
      </xsl:variable>
      <xsl:element name="{concat($nameSpacePrefix,':',local-name(.))}">
        <xsl:call-template name="add-namespaces"/>

        <xsl:apply-templates select="mdb:metadataIdentifier"/>
        <xsl:apply-templates select="mdb:defaultLocale"/>
        <xsl:apply-templates select="mdb:parentMetadata"/>
        <xsl:apply-templates select="mdb:metadataScope"/>
        <xsl:apply-templates select="mdb:dateInfo"/>
        <xsl:apply-templates select="mdb:metadataStandard"/>
        <xsl:apply-templates select="mdb:metadataProfile"/>
        <xsl:apply-templates select="mdb:referenceSystemInfo"/>
        <xsl:apply-templates select="mdb:identificationInfo"/>
        <xsl:apply-templates select="mdb:distributionInfo"/>
        <xsl:apply-templates select="mdb:dataQualityInfo"/>

        <xsl:if test="$displayInfo = 'true'">
          <xsl:copy-of select="$info"/>
        </xsl:if>
      </xsl:element>
    </xsl:for-each>
  </xsl:template>


  <xsl:template match="cit:CI_Citation">
    <xsl:variable name="nameSpacePrefix">
      <xsl:call-template name="getNamespacePrefix"/>
    </xsl:variable>
    <xsl:element name="{concat($nameSpacePrefix,':',local-name(.))}">
      <xsl:apply-templates select="cit:title"/>
      <xsl:apply-templates select="cit:date[cit:CI_Date/cit:dateType/
        cit:CI_DateTypeCode/@codeListValue='revision']"/>
      <xsl:apply-templates select="cit:identifier"/>
      <xsl:apply-templates select="cit:responsibleParty"/>
    </xsl:element>
  </xsl:template>


  <xsl:template match="mrd:MD_Distribution">
    <xsl:variable name="nameSpacePrefix">
      <xsl:call-template name="getNamespacePrefix"/>
    </xsl:variable>
    <xsl:element name="{concat($nameSpacePrefix,':',local-name(.))}">
      <xsl:apply-templates select="mrd:distributionFormat"/>
      <xsl:apply-templates select="mrd:transferOptions"/>
    </xsl:element>
  </xsl:template>


  <xsl:template match="mrd:MD_DigitalTransferOptions">
    <xsl:variable name="nameSpacePrefix">
      <xsl:call-template name="getNamespacePrefix"/>
    </xsl:variable>
    <xsl:element name="{concat($nameSpacePrefix,':',local-name(.))}">
      <xsl:apply-templates select="mrd:onLine"/>
    </xsl:element>
  </xsl:template>


  <xsl:template match="cit:CI_OnlineResource">
    <xsl:variable name="nameSpacePrefix">
      <xsl:call-template name="getNamespacePrefix"/>
    </xsl:variable>
    <xsl:element name="{concat($nameSpacePrefix,':',local-name(.))}">
      <xsl:apply-templates select="cit:linkage"/>
    </xsl:element>
  </xsl:template>


  <xsl:template match="mrd:MD_Format">
    <xsl:variable name="nameSpacePrefix">
      <xsl:call-template name="getNamespacePrefix"/>
    </xsl:variable>
    <xsl:element name="{concat($nameSpacePrefix,':',local-name(.))}">
      <xsl:apply-templates select="mrd:name"/>
      <xsl:apply-templates select="mrd:version"/>
    </xsl:element>
  </xsl:template>


  <xsl:template match="mrl:LI_Lineage">
    <xsl:variable name="nameSpacePrefix">
      <xsl:call-template name="getNamespacePrefix"/>
    </xsl:variable>
    <xsl:element name="{concat($nameSpacePrefix,':',local-name(.))}">
      <xsl:apply-templates select="mrl:statement"/>
      <xsl:apply-templates select="mrl:scope"/>
    </xsl:element>
  </xsl:template>


  <xsl:template match="gex:EX_Extent">
    <xsl:variable name="nameSpacePrefix">
      <xsl:call-template name="getNamespacePrefix"/>
    </xsl:variable>
    <xsl:element name="{concat($nameSpacePrefix,':',local-name(.))}">
      <xsl:apply-templates select="gex:geographicElement[child::gex:EX_GeographicBoundingBox]"/>
    </xsl:element>
  </xsl:template>


  <xsl:template match="gex:EX_GeographicBoundingBox">
    <xsl:variable name="nameSpacePrefix">
      <xsl:call-template name="getNamespacePrefix"/>
    </xsl:variable>
    <xsl:element name="{concat($nameSpacePrefix,':',local-name(.))}">
      <xsl:apply-templates select="gex:westBoundLongitude"/>
      <xsl:apply-templates select="gex:southBoundLatitude"/>
      <xsl:apply-templates select="gex:eastBoundLongitude"/>
      <xsl:apply-templates select="gex:northBoundLatitude"/>
    </xsl:element>
  </xsl:template>


  <xsl:template match="cit:CI_Responsibility[
    cit:role/cit:CI_RoleCode/@codeListValue='originator' or
    cit:role/cit:CI_RoleCode/@codeListValue='author' or
    cit:role/cit:CI_RoleCode/@codeListValue='publisher']">
    <xsl:variable name="nameSpacePrefix">
      <xsl:call-template name="getNamespacePrefix"/>
    </xsl:variable>
    <xsl:element name="{concat($nameSpacePrefix,':',local-name(.))}">
      <xsl:apply-templates select="cit:party/cit:CI_Organisation/cit:name"/>
    </xsl:element>
  </xsl:template>


  <xsl:template match="mco:MD_LegalConstraints">
    <xsl:variable name="nameSpacePrefix">
      <xsl:call-template name="getNamespacePrefix"/>
    </xsl:variable>
    <xsl:element name="{concat($nameSpacePrefix,':',local-name(.))}">
      <xsl:apply-templates select="mco:accessConstraints"/>
    </xsl:element>
  </xsl:template>


  <xsl:template match="mcc:MD_BrowseGraphic">
    <xsl:variable name="nameSpacePrefix">
      <xsl:call-template name="getNamespacePrefix"/>
    </xsl:variable>
    <xsl:element name="{concat($nameSpacePrefix,':',local-name(.))}">
      <xsl:apply-templates select="mcc:fileName"/>
    </xsl:element>
  </xsl:template>


  <xsl:template match="mdb:identificationInfo">
    <gmd:identificationInfo>
      <xsl:copy-of select="@*"/>
      <xsl:for-each select="./*">
        <xsl:variable name="nameSpacePrefix">
          <xsl:call-template name="getNamespacePrefix"/>
        </xsl:variable>

        <xsl:element name="{concat($nameSpacePrefix,':',local-name(.))}">
          <xsl:copy-of select="@*"/>
          <xsl:apply-templates select="mri:citation"/>
          <xsl:apply-templates select="mri:abstract"/>
          <xsl:apply-templates select="mri:graphicOverview"/>
          <xsl:apply-templates select="mri:pointOfContact"/>
          <xsl:apply-templates select="mri:resourceConstraints"/>
          <xsl:apply-templates select="mri:spatialRepresentationType"/>
          <xsl:apply-templates select="mri:spatialResolution"/>
          <xsl:apply-templates select="mri:temporalResolution"/>
          <xsl:apply-templates select="mri:defaultLocale"/>
          <xsl:apply-templates select="mri:topicCategory"/>
          <xsl:apply-templates select="mri:extent[child::gex:EX_Extent[child::gex:geographicElement]]"/>
          <xsl:apply-templates select="srv2:serviceType"/>
          <xsl:apply-templates select="srv2:serviceTypeVersion"/>
          <xsl:apply-templates select="srv2:couplingType"/>
          <xsl:apply-templates select="srv2:containsOperations"/>
        </xsl:element>
      </xsl:for-each>
    </gmd:identificationInfo>
  </xsl:template>


  <xsl:template match="srv2:SV_OperationMetadata">
    <xsl:variable name="nameSpacePrefix">
      <xsl:call-template name="getNamespacePrefix"/>
    </xsl:variable>
    <xsl:element name="{concat($nameSpacePrefix,':',local-name(.))}">
      <xsl:apply-templates select="srv2:operationName"/>
      <xsl:apply-templates select="srv2:DCP"/>
      <xsl:apply-templates select="srv2:connectPoint"/>
    </xsl:element>
  </xsl:template>
</xsl:stylesheet>
