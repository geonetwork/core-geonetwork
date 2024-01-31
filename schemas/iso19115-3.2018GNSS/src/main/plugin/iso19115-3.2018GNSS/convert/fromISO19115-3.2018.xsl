<xsl:stylesheet version="2.0"
                xmlns:mrs="http://standards.iso.org/iso/19115/-3/mrs/1.0"
                xmlns:rbc="https://schemas.isotc211.org/19111/-/rbc/3.1"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                exclude-result-prefixes="#all">
  <xsl:import href="ISO19139/fromISO19115-3.2014.xsl"/>

  <!--
 Create template to translate from iso 19115-3:2018 crs

 <mrs:MD_ReferenceSystem>
        <mrs:referenceSystemIdentifier>
           <mcc:MD_Identifier>
              <mcc:code>
                 <gcx:Anchor xlink:href="http://www.opengis.net/def/crs/EPSG/0/9308">EPSG:9308</gcx:Anchor>
              </mcc:code>
              <mcc:codeSpace>
                 <gco:CharacterString>EPSG</gco:CharacterString>
              </mcc:codeSpace>
              <mcc:description>
                 <gco:CharacterString>ATRF2014 - Geographic 3D</gco:CharacterString>
              </mcc:description>
           </mcc:MD_Identifier>
        </mrs:referenceSystemIdentifier>
        <mrs:referenceSystemType>
           <mrs:MD_ReferenceSystemTypeCode codeList="http://standards.iso.org/iso/19115/resources/Codelists/cat/codelists.xml#MD_ReferenceSystemTypeCode"
                                           codeListValue="geodeticGeographic3D"/>
        </mrs:referenceSystemType>
     </mrs:MD_ReferenceSystem>

 To new ISO10115-3:2023

 <mrs:MD_ReferenceSystem>
        <mrs:referenceSystemIdentifier>
           <mcc:MD_Identifier>
              <mcc:code>
                 <gcx:Anchor xlink:href="http://www.opengis.net/def/crs/EPSG/0/9308">EPSG:9308</gcx:Anchor>
              </mcc:code>
              <mcc:codeSpace>
                 <gco:CharacterString>EPSG</gco:CharacterString>
              </mcc:codeSpace>
              <mcc:description>
                 <gco:CharacterString>ATRF2014 - Geographic 3D</gco:CharacterString>
              </mcc:description>
           </mcc:MD_Identifier>
        </mrs:referenceSystemIdentifier>
        <mrs:coordinateEpoch>
           <rbc:RS_DataEpoch xmlns:rbc="https://schemas.isotc211.org/19111/-/rbc/3.1"/>
        </mrs:coordinateEpoch>
        <mrs:referenceSystemType>
           <mrs:MD_ReferenceSystemTypeCode codeList="http://standards.iso.org/iso/19115/resources/Codelists/cat/codelists.xml#MD_ReferenceSystemTypeCode"
                                           codeListValue="geodeticGeographic3D"/>
        </mrs:referenceSystemType>
     </mrs:MD_ReferenceSystem>
     -->
  <xsl:template match="mrs:MD_ReferenceSystem" mode="fromIso19115-3.2018toAmd2">
    <xsl:element name="mrs:MD_ReferenceSystem">
      <xsl:copy-of select="mrs:referenceSystemIdentifier"/>
      <mrs:coordinateEpoch>
        <rbc:RS_DataEpoch xmlns:rbc="https://schemas.isotc211.org/19111/-/rbc/3.1"/>
      </mrs:coordinateEpoch>
      <xsl:copy-of select="mrs:referenceSystemType"/>
    </xsl:element>
  </xsl:template>
</xsl:stylesheet>
