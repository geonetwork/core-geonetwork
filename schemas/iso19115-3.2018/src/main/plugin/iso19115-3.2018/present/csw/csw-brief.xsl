<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:csw="http://www.opengis.net/cat/csw/2.0.2"
  xmlns:dc ="http://purl.org/dc/elements/1.1/"
  xmlns:dct="http://purl.org/dc/terms/"
  xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0"
  xmlns:mri="http://standards.iso.org/iso/19115/-3/mri/1.0"
  xmlns:cit="http://standards.iso.org/iso/19115/-3/cit/2.0"
  xmlns:mcc="http://standards.iso.org/iso/19115/-3/mcc/1.0"
  xmlns:gex="http://standards.iso.org/iso/19115/-3/gex/1.0"
  xmlns:srv="http://standards.iso.org/iso/19115/-3/srv/2.0"
  xmlns:mco="http://standards.iso.org/iso/19115/-3/mco/1.0"
  xmlns:mrd="http://standards.iso.org/iso/19115/-3/mrd/1.0"
  xmlns:mrs="http://standards.iso.org/iso/19115/-3/mrs/1.0"
  xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
  xmlns:ows="http://www.opengis.net/ows"
  xmlns:geonet="http://www.fao.org/geonetwork"
  exclude-result-prefixes="#all">

  <xsl:param name="lang"/>

  <xsl:variable name="metadata"
                select="mdb:MD_Metadata|*[contains(@gco:isoType,'MD_Metadata')]"/>

  <xsl:include href="../../layout/utility-tpl-multilingual.xsl"/>

  <xsl:template match="mdb:MD_Metadata|*[contains(@gco:isoType,'MD_Metadata')]">
    <xsl:variable name="info" select="geonet:info"/>
    <xsl:variable name="langId">
      <xsl:call-template name="getLangId19115-3.2018">
        <xsl:with-param name="langGui" select="$lang"/>
        <xsl:with-param name="md" select="."/>
      </xsl:call-template>
    </xsl:variable>

    <xsl:variable name="identification"
      select="mdb:identificationInfo/mri:MD_DataIdentification|
              mdb:identificationInfo/*[contains(@gco:isoType, 'MD_DataIdentification')]|
              mdb:identificationInfo/srv:SV_ServiceIdentification"
    />

    <csw:BriefRecord>
      <xsl:for-each select="mdb:metadataIdentifier">
        <dc:identifier><xsl:value-of select="mcc:MD_Identifier/mcc:code/gco:CharacterString"/></dc:identifier>
      </xsl:for-each>

      <!-- DataIdentification -->
      <xsl:for-each select="$identification/mri:citation/cit:CI_Citation">
        <xsl:for-each select="cit:title">
          <dc:title>
            <xsl:apply-templates mode="localised" select=".">
              <xsl:with-param name="langId" select="$langId"/>
            </xsl:apply-templates>
          </dc:title>
        </xsl:for-each>
      </xsl:for-each>


      <!-- Type -->
      <xsl:for-each select="mdb:metadataScope/mdb:MD_MetadataScope/
        mdb:resourceScope/mcc:MD_ScopeCode/@codeListValue">
        <dc:type><xsl:value-of select="."/></dc:type>
      </xsl:for-each>

      <!-- bounding box -->
      <xsl:for-each
        select="$identification/mri:extent/gex:EX_Extent/gex:geographicElement/gex:EX_GeographicBoundingBox|
        $identification/srv:extent/gex:EX_Extent/gex:geographicElement/gex:EX_GeographicBoundingBox">
        <ows:BoundingBox crs="urn:ogc:def:crs:EPSG:6.6:4326">
          <ows:LowerCorner>
            <xsl:value-of select="concat(gex:southBoundLatitude/gco:Decimal, ' ', gex:westBoundLongitude/gco:Decimal)"/>
          </ows:LowerCorner>

          <ows:UpperCorner>
            <xsl:value-of select="concat(gex:northBoundLatitude/gco:Decimal, ' ', gex:eastBoundLongitude/gco:Decimal)"/>
          </ows:UpperCorner>
        </ows:BoundingBox>
      </xsl:for-each>
    </csw:BriefRecord>
  </xsl:template>

  <xsl:template match="*">
    <xsl:apply-templates select="*"/>
  </xsl:template>
</xsl:stylesheet>
