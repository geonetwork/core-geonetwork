<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:cit="http://standards.iso.org/iso/19115/-3/cit/2.0"
                xmlns:mcc="http://standards.iso.org/iso/19115/-3/mcc/1.0"
                xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0"
                xmlns:gcx="http://standards.iso.org/iso/19115/-3/gcx/1.0"
                xmlns:mrd="http://standards.iso.org/iso/19115/-3/mrd/1.0"
                xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
                xmlns:mri="http://standards.iso.org/iso/19115/-3/mri/1.0"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:geonet="http://www.fao.org/geonetwork"
                exclude-result-prefixes="#all">

  <!-- Insert a DOI in the metadata record as a resource identifier. -->
  <xsl:output method="xml" indent="yes"/>

  <xsl:param name="doi"
             select="''"/>
  <xsl:param name="doiProxy"
             select="'https://www.doi.org/'"/>
  <xsl:variable name="doiProtocol"
                select="'DOI'"/>
  <xsl:variable name="doiName"
                select="'Digital Object Identifier (DOI)'"/>
  <xsl:param name="doiProtocolRegex"
             select="'(DOI|WWW:LINK-1.0-http--metadata-URL)'"/>

  <xsl:variable name="isDoiAlreadySet"
                select="count(//mdb:identificationInfo/*/mri:citation/*/
                              cit:identifier/*/mcc:code[
                                contains(*/text(), 'datacite.org/doi/')
                                or contains(*/text(), 'doi.org')
                                or contains(*/@xlink:href, 'doi.org')]) > 0"/>

<!--  <xsl:variable name="isDoiAlreadySet"-->
<!--                select="count(//mdb:distributionInfo//mrd:onLine/*[matches(cit:protocol/gco:CharacterString, $doiProtocolRegex)]/cit:linkage/gco:CharacterString[. != '']) > 0"/>-->


  <xsl:template match="mdb:identificationInfo[1]/*/mri:citation/*[not($isDoiAlreadySet) and string($doi)]"
                priority="2">
    <xsl:copy>
      <xsl:copy-of select="@*"/>

      <xsl:copy-of select="cit:title
                           |cit:alternateTitle
                           |cit:date
                           |cit:edition
                           |cit:editionDate
                           |cit:identifier
                          "/>
      <cit:identifier>
        <mcc:MD_Identifier>
          <mcc:code>
            <gcx:Anchor xlink:href="{concat($doiProxy, $doi)}">
              <xsl:value-of select="$doi"/>
            </gcx:Anchor>
          </mcc:code>
          <mcc:codeSpace>
            <gco:CharacterString>doi.org</gco:CharacterString>
          </mcc:codeSpace>
          <mcc:description>
            <gco:CharacterString>Digital Object Identifier (DOI)</gco:CharacterString>
          </mcc:description>
        </mcc:MD_Identifier>
      </cit:identifier>

      <xsl:copy-of select="cit:citedResponsibleParty
                           |cit:presentationForm
                           |cit:series
                           |cit:otherCitationDetails
                           |cit:collectiveTitle
                           |cit:ISBN
                           |cit:ISSN
                           |cit:onlineResource
                           |cit:graphic
                          "/>
    </xsl:copy>
  </xsl:template>


  <xsl:template match="mdb:distributionInfo[not($isDoiAlreadySet) and string($doi) and position() = 1]">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <mrd:MD_Distribution>
        <xsl:apply-templates select="*/@*"/>
        <xsl:apply-templates select="*/mrd:distributionFormat"/>
        <xsl:apply-templates select="*/mrd:distributor"/>
        <xsl:choose>
          <xsl:when test="*/mrd:transferOptions">
            <xsl:apply-templates select="*/mrd:transferOptions[1]" mode="doi-add"/>
            <xsl:apply-templates select="*/mrd:transferOptions[position() > 1]"/>
          </xsl:when>
          <xsl:otherwise>
            <mrd:transferOptions>
              <mrd:MD_DigitalTransferOptions>
                <xsl:call-template name="add-doi-resource">
                  <xsl:with-param name="linkage" select="concat($doiProxy, $doi)" />
                  <xsl:with-param name="protocol" select="$doiProtocol" />
                  <xsl:with-param name="name" select="$doiName" />
                </xsl:call-template>
              </mrd:MD_DigitalTransferOptions>
            </mrd:transferOptions>
          </xsl:otherwise>
        </xsl:choose>
      </mrd:MD_Distribution>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="mrd:transferOptions" mode="doi-add">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <mrd:MD_DigitalTransferOptions>
        <xsl:apply-templates select="mrd:MD_DigitalTransferOptions/@*|mrd:MD_DigitalTransferOptions/(mrd:unitsOfDistribution|mrd:transferSize|mrd:onLine)"/>

        <xsl:call-template name="add-doi-resource">
          <xsl:with-param name="linkage" select="concat($doiProxy, $doi)" />
          <xsl:with-param name="protocol" select="$doiProtocol" />
          <xsl:with-param name="name" select="$doiName" />
        </xsl:call-template>

        <xsl:apply-templates select="mrd:MD_DigitalTransferOptions/(mrd:offLine|mrd:transferFrequency|mrd:distributionFormat)"/>
      </mrd:MD_DigitalTransferOptions>
    </xsl:copy>
  </xsl:template>

  <xsl:template name="add-doi-resource">
    <xsl:param name="linkage" as="xs:string" />
    <xsl:param name="protocol" as="xs:string" />
    <xsl:param name="name" as="xs:string" />

    <mrd:onLine>
      <cit:CI_OnlineResource>
        <cit:linkage>
          <gco:CharacterString><xsl:value-of select="$linkage"/></gco:CharacterString>
        </cit:linkage>
        <cit:protocol>
          <gco:CharacterString><xsl:value-of select="$protocol"/></gco:CharacterString>
        </cit:protocol>
        <cit:name>
          <gco:CharacterString><xsl:value-of select="$name"/></gco:CharacterString>
        </cit:name>
      </cit:CI_OnlineResource>
    </mrd:onLine>
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
