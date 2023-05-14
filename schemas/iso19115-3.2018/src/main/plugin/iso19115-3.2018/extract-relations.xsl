<?xml version="1.0" encoding="UTF-8"?>
<!--
  Create a simple XML tree for relation description.
  <relations>
    <relation type="related|services|children">
      + super-brief representation.
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
  xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
  xmlns:gmx="http://standards.iso.org/iso/19115/-3/gmx"
  xmlns:gn="http://www.fao.org/geonetwork"
  xmlns:gn-fn-iso19115-3.2018="http://geonetwork-opensource.org/xsl/functions/profiles/iso19115-3.2018"
  xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0"
  xmlns:lan="http://standards.iso.org/iso/19115/-3/lan/1.0"
  xmlns:mcc="http://standards.iso.org/iso/19115/-3/mcc/1.0"
  xmlns:mrd="http://standards.iso.org/iso/19115/-3/mrd/1.0"
  xmlns:cit="http://standards.iso.org/iso/19115/-3/cit/2.0"
  exclude-result-prefixes="#all" >

  <xsl:import href="index-fields/link-utility.xsl"/>

  <!-- Relation contained in the metadata record has to be returned
       It could be documents (not always in distribution section)
       or thumbnails
  -->
  <xsl:template mode="relation" match="metadata[mdb:MD_Metadata or *[contains(@gco:isoType, 'MD_Metadata')]]" priority="99">

    <thumbnails>
      <xsl:for-each select="*/descendant::*[name(.) = 'mri:graphicOverview']/*">
        <item>
          <id><xsl:value-of select="mcc:fileName/gco:CharacterString"/></id>
          <url>
              <xsl:apply-templates mode="get-iso19115-3.2018-localized-string"
                                    select="mcc:fileName"/>
          </url>
          <title>
              <xsl:apply-templates mode="get-iso19115-3.2018-localized-string"
                                   select="mcc:fileDescription"/>
          </title>
          <type>thumbnail</type>
        </item>
      </xsl:for-each>
    </thumbnails>

    <onlines>
      <xsl:call-template name="collect-distribution-links"/>
      <xsl:call-template name="collect-documents"/>
    </onlines>
  </xsl:template>
</xsl:stylesheet>
