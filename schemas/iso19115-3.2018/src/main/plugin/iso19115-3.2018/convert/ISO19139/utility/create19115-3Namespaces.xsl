<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  exclude-result-prefixes="xs"
  version="2.0">
  <xsl:template name="add-iso19115-3.2018-namespaces">
    <!-- new namespaces -->
    <xsl:namespace name="xsi" select="'http://www.w3.org/2001/XMLSchema-instance'"/>
    <!-- Namespaces that include concepts outside of metadata -->
    <!-- Catalog (ISO 19115-3) -->
    <xsl:namespace name="cat" select="'http://standards.iso.org/iso/19115/-3/cat/1.0'"/>
    <xsl:namespace name="gfc" select="'http://standards.iso.org/iso/19110/gfc/1.1'"/>
    <!-- Citation (ISO 19115-3) -->
    <xsl:namespace name="cit" select="'http://standards.iso.org/iso/19115/-3/cit/2.0'"/>
    <!-- Geospatial Common eXtension (ISO 19115-3) -->
    <xsl:namespace name="gcx" select="'http://standards.iso.org/iso/19115/-3/gcx/1.0'"/>
    <!-- Geospatial EXtent (ISO 19115-3) -->
    <xsl:namespace name="gex" select="'http://standards.iso.org/iso/19115/-3/gex/1.0'"/>
    <!-- Language Localization (ISO 19115-3) -->
    <xsl:namespace name="lan" select="'http://standards.iso.org/iso/19115/-3/lan/1.0'"/>
    <!-- Metadata for Services (ISO 19115-3) -->
    <xsl:namespace name="srv" select="'http://standards.iso.org/iso/19115/-3/srv/2.0'"/>
    <!-- Metadata for Application Schema (ISO 19115-3) -->
    <xsl:namespace name="mas" select="'http://standards.iso.org/iso/19115/-3/mas/1.0'"/>
    <!-- Metadata for Common Classes (ISO 19115-3) -->
    <xsl:namespace name="mcc" select="'http://standards.iso.org/iso/19115/-3/mcc/1.0'"/>
    <!-- Metadata for COnstraints (ISO 19115-3) -->
    <xsl:namespace name="mco" select="'http://standards.iso.org/iso/19115/-3/mco/1.0'"/>
    <!-- MetaData Application (ISO 19115-3) -->
    <xsl:namespace name="mda" select="'http://standards.iso.org/iso/19115/-3/mda/1.0'"/>
    <!-- MetaDataBase (ISO 19115-3) -->
    <xsl:namespace name="mdb" select="'http://standards.iso.org/iso/19115/-3/mdb/2.0'"/>
    <!-- Metadata for Data and Services (ISO 19115-3) -->
    <xsl:namespace name="mds" select="'http://standards.iso.org/iso/19115/-3/mds/2.0'"/>
    <!-- Metadata based Data Transfer (ISO 19115-3) -->
    <xsl:namespace name="mdt" select="'http://standards.iso.org/iso/19115/-3/mdt/2.0'"/>
    <!-- Metadata for EXtensions (ISO 19115-3) -->
    <xsl:namespace name="mex" select="'http://standards.iso.org/iso/19115/-3/mex/1.0'"/>
    <!-- Metadata for Maintenance Information (ISO 19115-3) -->
    <xsl:namespace name="mmi" select="'http://standards.iso.org/iso/19115/-3/mmi/1.0'"/>
    <!-- Metadata for Portrayal Catalog (ISO 19115-3) -->
    <xsl:namespace name="mpc" select="'http://standards.iso.org/iso/19115/-3/mpc/1.0'"/>
    <!-- Metadata for Resource Content (ISO 19115-3) -->
    <xsl:namespace name="mrc" select="'http://standards.iso.org/iso/19115/-3/mrc/2.0'"/>
    <!-- Metadata for Resource Distribution (ISO 19115-3) -->
    <xsl:namespace name="mrd" select="'http://standards.iso.org/iso/19115/-3/mrd/1.0'"/>
    <!-- Metadata for Resource Identification (ISO 19115-3) -->
    <xsl:namespace name="mri" select="'http://standards.iso.org/iso/19115/-3/mri/1.0'"/>
    <!-- Metadata for Resource Lineage (ISO 19115-3) -->
    <xsl:namespace name="mrl" select="'http://standards.iso.org/iso/19115/-3/mrl/2.0'"/>
    <!-- Metadata for Reference System (ISO 19115-3) -->
    <xsl:namespace name="mrs" select="'http://standards.iso.org/iso/19115/-3/mrs/1.0'"/>
    <!-- Metadata for Spatial Representation (ISO 19115-3) -->
    <xsl:namespace name="msr" select="'http://standards.iso.org/iso/19115/-3/msr/2.0'"/>
    <!-- Data Quality Measures (ISO 19157-2) -->
    <xsl:namespace name="mdq" select="'http://standards.iso.org/iso/19157/-2/mdq/1.0'"/>
    <!-- Metadata for Acquisition (ISO 19115-2) -->
    <xsl:namespace name="mac" select="'http://standards.iso.org/iso/19115/-3/mac/2.0'"/>
    <!-- other ISO namespaces -->
    <!-- Geospatial COmmon -->
    <xsl:namespace name="gco" select="'http://standards.iso.org/iso/19115/-3/gco/1.0'"/>
    <!-- external namespaces -->
    <xsl:namespace name="gml" select="'http://www.opengis.net/gml/3.2'"/>
    <xsl:namespace name="xlink" select="'http://www.w3.org/1999/xlink'"/>
  </xsl:template>
</xsl:stylesheet>
