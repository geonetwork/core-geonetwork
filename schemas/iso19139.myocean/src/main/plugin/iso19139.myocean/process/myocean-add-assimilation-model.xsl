<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gmx="http://www.isotc211.org/2005/gmx"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:geonet="http://www.fao.org/geonetwork"
                exclude-result-prefixes="#all">

  <xsl:output indent="yes"/>

  <!-- This process add a model assimilation keyword block
  after the last descriptiveKeywords element
  if no model assimilation already defined in the document. -->
  <xsl:template match="//gmd:descriptiveKeywords[position() = last() and
                            count(//gmd:thesaurusName[*/gmd:identifier/*/
                                    gmd:code/*/text() =
                                    'geonetwork.thesaurus.external.theme.myocean.model-assimilation']) = 0]">
    <xsl:copy-of select="."/>
    <gmd:descriptiveKeywords>
      <gmd:MD_Keywords>
        <gmd:type>
          <gmd:MD_KeywordTypeCode codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/ML_gmxCodelists.xml#MD_KeywordTypeCode" codeListValue="theme"/>
        </gmd:type>
        <gmd:thesaurusName>
          <gmd:CI_Citation>
            <gmd:title>
              <gco:CharacterString>Model assimilation</gco:CharacterString>
            </gmd:title>
            <gmd:date>
              <gmd:CI_Date>
                <gmd:date>
                  <gco:Date>2016-02-03</gco:Date>
                </gmd:date>
                <gmd:dateType>
                  <gmd:CI_DateTypeCode codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/ML_gmxCodelists.xml#CI_DateTypeCode" codeListValue="publication"/>
                </gmd:dateType>
              </gmd:CI_Date>
            </gmd:date>
            <gmd:identifier>
              <gmd:MD_Identifier>
                <gmd:code>
                  <gmx:Anchor xlink:href="http://sextant.ifremer.fr/geonetwork/srv/eng/thesaurus.download?ref=external.theme.myocean.model-assimilation">geonetwork.thesaurus.external.theme.myocean.model-assimilation</gmx:Anchor>
                </gmd:code>
              </gmd:MD_Identifier>
            </gmd:identifier>
          </gmd:CI_Citation>
        </gmd:thesaurusName>
      </gmd:MD_Keywords>
    </gmd:descriptiveKeywords>
  </xsl:template>

  <!-- Do a copy of every nodes and attributes recursively -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <!-- Remove geonet:* elements. -->
  <xsl:template match="geonet:*" priority="2"/>
</xsl:stylesheet>