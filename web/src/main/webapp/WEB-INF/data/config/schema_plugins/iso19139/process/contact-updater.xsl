<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:saxon="http://saxon.sf.net/"
  xmlns:geonet="http://www.fao.org/geonetwork" xmlns:gco="http://www.isotc211.org/2005/gco"
  xmlns:gmd="http://www.isotc211.org/2005/gmd" version="2.0" extension-element-prefixes="saxon"
  exclude-result-prefixes="#all">


  <!-- Search for all contact avec a defined email and 
    replace the CI_ResponsibleParty by the provided XML. -->

  <!-- The contact email to search for -->
  <xsl:param name="emailToSearch"/>

  <!-- The contact snippet to insert -->
  <xsl:param name="contactAsXML"/>

  <!-- Do a copy of every nodes and attributes -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <!-- Remove geonet:* elements. -->
  <xsl:template match="geonet:*" priority="2"/>


  <xsl:template
    match="gmd:CI_ResponsibleParty[
                          gmd:contactInfo/gmd:CI_Contact/gmd:address/
                            gmd:CI_Address/gmd:electronicMailAddress/
                              gco:CharacterString = $emailToSearch and $emailToSearch != '']"
    priority="2">
    <xsl:copy-of select="saxon:parse($contactAsXML)"/>
  </xsl:template>

</xsl:stylesheet>
