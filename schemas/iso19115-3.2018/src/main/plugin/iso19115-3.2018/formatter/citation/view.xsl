<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:cit="http://standards.iso.org/iso/19115/-3/cit/2.0"
                xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
                xmlns:mcc="http://standards.iso.org/iso/19115/-3/mcc/1.0"
                xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0"
                xmlns:mri="http://standards.iso.org/iso/19115/-3/mri/1.0"
                xmlns:mrd="http://standards.iso.org/iso/19115/-3/mrd/1.0"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:gn-fn-iso19115-3.2018="http://geonetwork-opensource.org/xsl/functions/profiles/iso19115-3.2018"
                xmlns:saxon="http://saxon.sf.net/"
                extension-element-prefixes="saxon"
                exclude-result-prefixes="#all">

  <xsl:output omit-xml-declaration="yes"
              method="xml"
              indent="yes"
              saxon:indent-spaces="2"
              encoding="UTF-8"/>

  <xsl:import href="base.xsl"/>
  <xsl:import href="common.xsl"/>

  <xsl:variable name="metadata"
                select="/root/mdb:MD_Metadata"/>

  <xsl:variable name="configuration"
                select="/empty"/>


  <xsl:template match="/">
    <xsl:variable name="citationInfo">
      <xsl:call-template name="get-iso19115-3.2018-citation">
        <xsl:with-param name="metadata" select="$metadata"/>
        <xsl:with-param name="language" select="$language"/>
      </xsl:call-template>
    </xsl:variable>

    <xsl:apply-templates mode="citation" select="$citationInfo"/>
  </xsl:template>
</xsl:stylesheet>
