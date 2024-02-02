<xsl:stylesheet version="2.0"
                xmlns:mrs="http://standards.iso.org/iso/19115/-3/mrs/1.0"
                xmlns:rbc="https://schemas.isotc211.org/19111/-/rbc/3.1"
                xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xsi:schemaLocation="https://schemas.isotc211.org/19103/-/gco/1.2 https://schemas.isotc211.org/19103/-/gco/1.2.0/gco.xsd"
                exclude-result-prefixes="#all">

  <!--
 Create template to translate crs Sub-templates from iso 19115-3:2018 crs
 To new ISO10115-3:2023 by inserting coordinateEpoch element
     -->

  <xsl:template match="mrs:MD_ReferenceSystem" mode="fromIso19115-3.2018toAmd2">
    <xsl:element name="mrs:MD_ReferenceSystem">
      <xsl:copy-of select="mrs:referenceSystemIdentifier"/>
      <mrs:coordinateEpoch>
        <rbc:RS_DataEpoch>
          <rbc:coordinateEpoch>
            <gco:Measure uom="decimalYear">0000.0000</gco:Measure>
          </rbc:coordinateEpoch>
        </rbc:RS_DataEpoch>
      </mrs:coordinateEpoch>
      <xsl:copy-of select="mrs:referenceSystemType"/>
    </xsl:element>
  </xsl:template>
</xsl:stylesheet>
