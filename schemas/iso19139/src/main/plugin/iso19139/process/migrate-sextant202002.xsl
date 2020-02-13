<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:geonet="http://www.fao.org/geonetwork"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:gmx="http://www.isotc211.org/2005/gmx"
                version="2.0"
                exclude-result-prefixes="#all">

  <!-- Set title, date and use Anchor ?
  * GEMET thesaurus name
  <gmd:thesaurusName>
    <gmd:CI_Citation>
      <gmd:title>
      <gco:CharacterString>external.theme.gemet-theme</gco:CharacterString>
      </gmd:title>
      <gmd:date gco:nilReason="unknown"/>
    </gmd:CI_Citation>
  </gmd:thesaurusName>
  -->
  <xsl:template match="gmd:thesaurusName[*/gmd:title/*/text() = 'external.theme.gemet-theme']">
    <gmd:thesaurusName>
      <gmd:CI_Citation>
        <gmd:title>
          <gco:CharacterString>GEMET - Concepts, version 2.4</gco:CharacterString>
        </gmd:title>
        <gmd:date>
          <gmd:CI_Date>
            <gmd:date>
              <gco:Date>2010-01-13</gco:Date>
            </gmd:date>
            <gmd:dateType>
              <gmd:CI_DateTypeCode
                codeList="http://standards.iso.org/iso/19139/resources/gmxCodelists.xml#CI_DateTypeCode"
                codeListValue="publication"/>
            </gmd:dateType>
          </gmd:CI_Date>
        </gmd:date>
        <gmd:identifier>
          <gmd:MD_Identifier>
            <gmd:code>
              <gmx:Anchor
                xlink:href="http://localhost:8080/geonetwork/srv/eng/thesaurus.download?ref=external.theme.gemet">
                geonetwork.thesaurus.external.theme.gemet
              </gmx:Anchor>
            </gmd:code>
          </gmd:MD_Identifier>
        </gmd:identifier>
      </gmd:CI_Citation>
    </gmd:thesaurusName>
  </xsl:template>

  <!--
  * INSPIRE Theme thesaurus name
  <gco:CharacterString>external.theme.httpinspireeceuropaeutheme-theme</gco:CharacterString>
  -->
  <xsl:template match="gmd:thesaurusName[*/gmd:title/*/text() = 'external.theme.httpinspireeceuropaeutheme-theme']">
    <gmd:thesaurusName>
      <gmd:CI_Citation>
        <gmd:title>
          <gco:CharacterString>GEMET - INSPIRE themes, version 1.0</gco:CharacterString>
        </gmd:title>
        <gmd:date>
          <gmd:CI_Date>
            <gmd:date>
              <gco:Date>2018-07-27</gco:Date>
            </gmd:date>
            <gmd:dateType>
              <gmd:CI_DateTypeCode
                codeList="http://standards.iso.org/iso/19139/resources/gmxCodelists.xml#CI_DateTypeCode"
                codeListValue="publication"/>
            </gmd:dateType>
          </gmd:CI_Date>
        </gmd:date>
        <gmd:identifier>
          <gmd:MD_Identifier>
            <gmd:code>
              <gmx:Anchor
                xlink:href="https://sextant.ifremer.fr/geonetwork/srv/eng/thesaurus.download?ref=external.theme.httpinspireeceuropaeutheme-theme">
                geonetwork.thesaurus.external.theme.httpinspireeceuropaeutheme-theme
              </gmx:Anchor>
            </gmd:code>
          </gmd:MD_Identifier>
        </gmd:identifier>
      </gmd:CI_Citation>
    </gmd:thesaurusName>
  </xsl:template>


  <!--
  * INSPIRE <gco:CharacterString>Habitats et biotopes</gco:CharacterString>
  -->


  <!-- Graphic overview, remove old small thumbnails if not the only one. -->
  <xsl:template match="gmd:graphicOverview[
                                */gmd:fileDescription/*/text() = 'thumbnail'
                                and ends-with(*/gmd:fileName/*/text(), '_s.png')
                                and count(../gmd:graphicOverview) > 1]">
    <xsl:message>Removing small thumbnail <xsl:value-of select="*/gmd:fileName/*/text()"/>. </xsl:message>
  </xsl:template>

  <!-- Graphic overview, remove use file description and file type. -->
  <xsl:template match="gmd:graphicOverview/*/gmd:fileDescription[gco:CharacterString = ('thumbnail', 'large_thumbnail')]"/>
  <xsl:template match="gmd:graphicOverview/*/gmd:fileType[gco:CharacterString = ('png', 'jpg')]"/>

  <!--
  * graphicOverview
  <gmd:graphicOverview>
    <gmd:MD_BrowseGraphic>
      <gmd:fileName>
        <gco:CharacterString>
          https://sextant.ifremer.fr/geonetwork/srv/fr/resources.get?uuid=01051510-a178-11dc-8c36-000086f6a62e&fname=ifr_peupl_ChasseGlemarec_GolfeGascogne_Imagette_s.png
        </gco:CharacterString>
      </gmd:fileName>
      <gmd:fileDescription>
        <gco:CharacterString>thumbnail</gco:CharacterString>
      </gmd:fileDescription>
      <gmd:fileType>
        <gco:CharacterString>png</gco:CharacterString>
      </gmd:fileType>
    </gmd:MD_BrowseGraphic>
    </gmd:graphicOverview>
    <gmd:graphicOverview>
    <gmd:MD_BrowseGraphic>
      <gmd:fileName>
        <gco:CharacterString>
          https://sextant.ifremer.fr/geonetwork/srv/fr/resources.get?uuid=01051510-a178-11dc-8c36-000086f6a62e&fname=ifr_peupl_ChasseGlemarec_GolfeGascogne_Imagette.jpg
        </gco:CharacterString>
      </gmd:fileName>
      <gmd:fileDescription>
        <gco:CharacterString>large_thumbnail</gco:CharacterString>
      </gmd:fileDescription>
      <gmd:fileType>
        <gco:CharacterString>jpg</gco:CharacterString>
      </gmd:fileType>
    </gmd:MD_BrowseGraphic>
    </gmd:graphicOverview>
  -->

  <!-- Do a copy of every nodes and attributes -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <!-- Remove geonet:* elements. -->
  <xsl:template match="geonet:*" priority="2"/>
</xsl:stylesheet>
