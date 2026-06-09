<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:mds="http://standards.iso.org/iso/19115/-3/mds/2.0"
                xmlns:gcx="http://standards.iso.org/iso/19115/-3/gcx/1.0"
                xmlns:mri="http://standards.iso.org/iso/19115/-3/mri/1.0"
                xmlns:mcc="http://standards.iso.org/iso/19115/-3/mcc/1.0"
                xmlns:cit="http://standards.iso.org/iso/19115/-3/cit/2.0"
                xmlns:lan="http://standards.iso.org/iso/19115/-3/lan/1.0"
                xmlns:srv="http://standards.iso.org/iso/19115/-3/srv/2.0"
                xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:gco19115="http://www.isotc211.org/2005/gco"
                xmlns:util="java:org.fao.geonet.util.XslUtil"
                xmlns:geonet="http://www.fao.org/geonetwork"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                exclude-result-prefixes="#all">

  <xsl:import href="ISO19139/mapping/defaults.xsl"/>
  <xsl:import href="ISO19139/mapping/CI_ResponsibleParty.xsl"/>

  <!-- A set of templates use to convert subtemplates to
       iso19115-3 fragments. -->
  <xsl:template name="contact-from-iso19139-to-iso19115-3.2018">
    <xsl:apply-templates select="." mode="from19139to19115-3.2018"/>
  </xsl:template>
</xsl:stylesheet>
