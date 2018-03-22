<?xml version="1.0" encoding="UTF-8"?>
<!--
    EMODNET SDN & CHEMISTRY migration process
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:geonet="http://www.fao.org/geonetwork"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:gmx="http://www.isotc211.org/2005/gmx"
                xmlns:gml="http://www.opengis.net/gml"
                version="2.0"
                exclude-result-prefixes="#all">

  <xsl:output indent="yes"/>

  <!-- Replace standard name.

  Old value was "ISO 19115:2003/19139, SeadataNet product profile"
  or "ISO 19115:2003/19139 - EMODNET - CHEMISTRY".
  -->
  <xsl:template match="gmd:metadataStandardName/gco:CharacterString">
    <xsl:copy>
      ISO 19115:2003/19139 - EMODNET - SDN
    </xsl:copy>
  </xsl:template>


  <!-- Add an empty sextant theme block if not set already
  after the last keyword block. -->
  <xsl:variable name="hasSextantTheme"
                select="count(//gmd:descriptiveKeywords[
                                .//gmd:code/*/text() = 'geonetwork.thesaurus.local.theme.sextant-theme'
                                ]) > 0"/>

  <xsl:template
    match="gmd:descriptiveKeywords[$hasSextantTheme = false() and name(following-sibling::*[1]) != 'gmd:descriptiveKeywords']">
    <xsl:copy-of select="."/>

    <gmd:descriptiveKeywords>
      <gmd:MD_Keywords>
        <gmd:keyword gco:nilReason="missing">
          <gco:CharacterString/>
        </gmd:keyword>
        <gmd:type>
          <gmd:MD_KeywordTypeCode
            codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/ML_gmxCodelists.xml#MD_KeywordTypeCode"
            codeListValue="theme"/>
        </gmd:type>
        <gmd:thesaurusName>
          <gmd:CI_Citation>
            <gmd:title>
              <gco:CharacterString>Thèmes Sextant</gco:CharacterString>
            </gmd:title>
            <gmd:date>
              <gmd:CI_Date>
                <gmd:date>
                  <gco:Date>2017-01-23</gco:Date>
                </gmd:date>
                <gmd:dateType>
                  <gmd:CI_DateTypeCode
                    codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/ML_gmxCodelists.xml#CI_DateTypeCode"
                    codeListValue="publication"/>
                </gmd:dateType>
              </gmd:CI_Date>
            </gmd:date>
            <gmd:identifier>
              <gmd:MD_Identifier>
                <gmd:code>
                  <gmx:Anchor
                    xlink:href="http://sextant-test.ifremer.fr/geonetwork/srv/eng/thesaurus.download?ref=local.theme.sextant-theme">
                    geonetwork.thesaurus.local.theme.sextant-theme
                  </gmx:Anchor>
                </gmd:code>
              </gmd:MD_Identifier>
            </gmd:identifier>
          </gmd:CI_Citation>
        </gmd:thesaurusName>
      </gmd:MD_Keywords>
    </gmd:descriptiveKeywords>
  </xsl:template>


  <!-- Add useLimitation and use constraints if needed -->
  <xsl:template match="gmd:MD_LegalConstraints">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:choose>
        <xsl:when test="gmd:useLimitation">
          <xsl:apply-templates select="gmd:useLimitation"/>
        </xsl:when>
        <xsl:otherwise>
          <gmd:useLimitation gco:nilReason="missing">
            <gco:CharacterString/>
          </gmd:useLimitation>
        </xsl:otherwise>
      </xsl:choose>

      <xsl:apply-templates select="gmd:accessConstraints"/>
      <xsl:choose>
        <xsl:when test="gmd:useConstraints">
          <xsl:apply-templates select="gmd:useConstraints"/>
        </xsl:when>
        <xsl:otherwise>
          <gmd:useConstraints>
            <gmd:MD_RestrictionCode
              codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/ML_gmxCodelists.xml#MD_RestrictionCode"
              codeListValue=""/>
          </gmd:useConstraints>
        </xsl:otherwise>
      </xsl:choose>

      <xsl:apply-templates select="gmd:otherConstraints"/>
    </xsl:copy>
  </xsl:template>


  <!-- Cleanup some empty elements -->
  <xsl:template match="gmd:distributorContact[not(*)]|gmd:distributorFormat[not(*)]"/>


  <!-- Store language in gmd:LanguageCode instead of gco:CharacterString (INSPIRE requirements). -->
  <xsl:template match="gmd:language[gco:CharacterString]" priority="2">
    <xsl:copy>
      <gmd:LanguageCode codeList="http://www.loc.gov/standards/iso639-2/"
                        codeListValue="{gco:CharacterString}"/>
    </xsl:copy>
  </xsl:template>


  <!-- Remove small thumbnail if a large one exist and cleanup thumbnail description and type which are not used. -->
  <xsl:template match="gmd:graphicOverview[
            */gmd:fileDescription/gco:CharacterString = 'thumbnail' and
            count(../gmd:graphicOverview[*/gmd:fileDescription/gco:CharacterString = 'large_thumbnail']) > 0]"
                priority="2"/>

  <xsl:template match="gmd:graphicOverview/*/gmd:fileDescription"
                priority="2"/>
  <xsl:template match="gmd:graphicOverview/*/gmd:fileType"
                priority="2"/>


  <!-- Do a copy of every nodes and attributes -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <!-- Remove geonet:* elements. -->
  <xsl:template match="geonet:*" priority="2"/>

</xsl:stylesheet>
