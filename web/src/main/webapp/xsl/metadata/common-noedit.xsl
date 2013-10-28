<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:geonet="http://www.fao.org/geonetwork">

  <xsl:include href="layout.xsl"/>
  <xsl:include href="tab-utils.xsl"/>

  <xsl:variable name="metadata" select="/root/*[name(.)!='gui' and name(.) != 'request']"/>
  <xsl:variable name="schema" select="$metadata/geonet:info/schema"/>

</xsl:stylesheet>
