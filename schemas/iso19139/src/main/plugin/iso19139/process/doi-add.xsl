<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet   xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                  xmlns:gco="http://www.isotc211.org/2005/gco"
                  xmlns:gmd="http://www.isotc211.org/2005/gmd"
                  xmlns:geonet="http://www.fao.org/geonetwork"
                  exclude-result-prefixes="#all">

  <!-- Insert a DOI in the metadata record.

  The default mode here is to add the DOI in the distribution info section
  with a "DOI" protocol.
  -->
  <xsl:output method="xml" indent="yes"/>

  <xsl:param name="doi"
             select="''"/>
  <xsl:param name="doiProtocolRegex"
             select="'(DOI|WWW:LINK-1.0-http--metadata-URL)'"/>

  <xsl:variable name="doiProtocol"
                select="'WWW:LINK-1.0-http--metadata-URL'"/>
  <xsl:variable name="doiName"
                select="'Digital Object Identifier (DOI)'"/>


  <xsl:variable name="isDoiAlreadSet"
                select="count(//gmd:distributionInfo//gmd:onLine/*[matches(gmd:protocol/gco:CharacterString, $doiProtocolRegex)]/gmd:linkage/gmd:URL[. != '']) > 0"/>

  <!-- Insert the DOI in the first distributionInfo section
  (a distribution format is mandatory in ISO
  and a publisher (distributor) is required for a DOI
  so it should always exist).

  Adding a new transfer option block. -->
  <xsl:template match="gmd:distributionInfo[not($isDoiAlreadSet) and position() = 1]"
                priority="2">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <gmd:MD_Distribution>
        <xsl:apply-templates select="*/@*"/>
        <xsl:apply-templates select="*/gmd:distributionFormat"/>
        <xsl:apply-templates select="*/gmd:distributor"/>
        <xsl:apply-templates select="*/gmd:transferOptions"/>
        <gmd:transferOptions>
          <gmd:MD_DigitalTransferOptions>
            <gmd:onLine>
              <gmd:CI_OnlineResource>
                <gmd:linkage>
                  <gmd:URL><xsl:value-of select="$doi"/></gmd:URL>
                </gmd:linkage>
                <gmd:protocol>
                  <gco:CharacterString><xsl:value-of select="$doiProtocol"/></gco:CharacterString>
                </gmd:protocol>
                <gmd:name>
                  <gco:CharacterString><xsl:value-of select="$doiName"/></gco:CharacterString>
                </gmd:name>
              </gmd:CI_OnlineResource>
            </gmd:onLine>
          </gmd:MD_DigitalTransferOptions>
        </gmd:transferOptions>
      </gmd:MD_Distribution>
    </xsl:copy>
  </xsl:template>

  <!-- Do a copy of every nodes and attributes -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <!-- Remove geonet:* elements. -->
  <xsl:template match="geonet:*" priority="2"/>

</xsl:stylesheet>
