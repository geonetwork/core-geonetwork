<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet   xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0" 
  xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
  xmlns:mri="http://standards.iso.org/iso/19115/-3/mri/1.0"
  xmlns:mcc="http://standards.iso.org/iso/19115/-3/mcc/1.0"
  xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0">
  
  <xsl:template match="mdb:MD_Metadata|*[contains(@gco:isoType, 'MD_Metadata')]">
    <thumbnail>
      <xsl:for-each 
        select="mdb:identificationInfo/*/mri:graphicOverview/mcc:MD_BrowseGraphic">
        <xsl:choose>
          <xsl:when
            test="mcc:fileDescription/gco:CharacterString = 'large_thumbnail' and
                  mcc:fileName/gco:CharacterString != ''">
            <large>
              <xsl:value-of select="mcc:fileName/gco:CharacterString"/>
            </large>
          </xsl:when>
          <xsl:when
            test="mcc:fileDescription/gco:CharacterString = 'thumbnail' and
                  mcc:fileName/gco:CharacterString != ''">
            <small>
              <xsl:value-of select="mcc:fileName/gco:CharacterString"/>
            </small>
          </xsl:when>
        </xsl:choose>
      </xsl:for-each>
    </thumbnail>
  </xsl:template>
</xsl:stylesheet>
