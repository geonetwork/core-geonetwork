<?xml version="1.0" encoding="UTF-8"?>
<!--
  ~ Copyright (C) 2001-2016 Food and Agriculture Organization of the
  ~ United Nations (FAO-UN), United Nations World Food Programme (WFP)
  ~ and United Nations Environment Programme (UNEP)
  ~
  ~ This program is free software; you can redistribute it and/or modify
  ~ it under the terms of the GNU General Public License as published by
  ~ the Free Software Foundation; either version 2 of the License, or (at
  ~ your option) any later version.
  ~
  ~ This program is distributed in the hope that it will be useful, but
  ~ WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  ~ General Public License for more details.
  ~
  ~ You should have received a copy of the GNU General Public License
  ~ along with this program; if not, write to the Free Software
  ~ Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
  ~
  ~ Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
  ~ Rome - Italy. email: geonetwork@osgeo.org
  -->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:geonet="http://www.fao.org/geonetwork"
                xmlns:fo="http://www.w3.org/1999/XSL/Format"
                version="2.0" xmlns:xslk="http://www.w3.org/1999/XSL/Transform"
                exclude-result-prefixes="#all">


  <!-- Some colors -->
  <xsl:variable name="background-color">#DDDDDD</xsl:variable>
  <xsl:variable name="background-color-banner">#333333</xsl:variable>
  <xsl:variable name="background-color-thumbnail">#FFFFFF</xsl:variable>
  <xsl:variable name="border-color">#333333</xsl:variable>

  <xsl:variable name="header-border">2pt solid #2e456b</xsl:variable>

  <!-- Some font properties -->
  <xsl:variable name="link-color">#428BCA</xsl:variable>
  <xsl:variable name="font-color">#333333</xsl:variable>
  <xsl:variable name="font-size">8pt</xsl:variable>
  <xsl:variable name="font-family">Helvetica</xsl:variable>
  <xsl:variable name="title-color">#FFFFFF</xsl:variable>
  <xsl:variable name="title-size">12pt</xsl:variable>
  <xsl:variable name="title-weight">bold</xsl:variable>
  <xsl:variable name="label-weight">bold</xsl:variable>
  <xsl:variable name="header-color">#2e456b</xsl:variable>
  <xsl:variable name="header-size">16pt</xsl:variable>
  <xsl:variable name="header-weight">bold</xsl:variable>
  <xsl:variable name="note-size">6pt</xsl:variable>

  <xsl:variable name="heading1-text-size">16pt</xsl:variable>
  <xsl:variable name="heading1-text-weight">bold</xsl:variable>

  <xsl:variable name="heading2-text-size">12pt</xsl:variable>
  <xsl:variable name="heading2-text-weight">bold</xsl:variable>

  <xsl:variable name="toc-text-size">10pt</xsl:variable>

  <xsl:variable name="header-text-size">9pt</xsl:variable>
  <xsl:variable name="footer-text-size">11pt</xsl:variable>

  <!-- Date format for footer information -->
  <xsl:variable name="df">[Y0001]-[M01]-[D01]</xsl:variable>


  <!-- FOP master configuration A4 with margins -->
  <xsl:template name="fop-master">
    <fo:layout-master-set>
      <fo:simple-page-master master-name="simpleA4" page-height="29.7cm" page-width="21cm"
                             margin-top=".5cm" margin-bottom=".5cm" margin-left="2cm"
                             margin-right="2cm">
        <fo:region-body margin-top="1.4cm" margin-bottom=".4cm"/>
        <fo:region-before extent=".4cm"/>
        <fo:region-after extent=".4cm"/>
      </fo:simple-page-master>

      <fo:simple-page-master master-name="Intro" page-height="29.7cm" page-width="21cm"
                             margin-top=".5cm" margin-bottom=".5cm" margin-left="2cm"
                             margin-right="2cm">
        <fo:region-body margin-top="1.4cm" margin-bottom=".4cm"/>
        <fo:region-before extent=".4cm"/>
        <fo:region-after extent=".4cm"/>
      </fo:simple-page-master>

      <fo:page-sequence-master master-name="PSM_Name">
        <fo:single-page-master-reference master-reference="simpleA4"/>
      </fo:page-sequence-master>
    </fo:layout-master-set>
  </xsl:template>


  <xsl:template name="fop-header">
    <fo:static-content flow-name="xsl-region-before">
      <fo:block font-size="{$header-text-size}" color="{$font-color}">

        <fo:table width="100%" table-layout="fixed">
          <fo:table-body>
            <fo:table-row height="8mm">
              <fo:table-cell>
                <fo:block padding-top="4pt" padding-right="4pt"
                          padding-left="4pt"
                          text-align="left">
                  <xsl:call-template name="replacePlaceholders">
                    <xsl:with-param name="value" select="$env/system/metadata/pdfReport/headerLeft"/>
                  </xsl:call-template>
                </fo:block>
              </fo:table-cell>

              <fo:table-cell>
                <fo:block padding-top="4pt" padding-right="4pt"
                          padding-left="4pt"
                          text-align="right">
                  <xsl:call-template name="replacePlaceholders">
                    <xsl:with-param name="value" select="$env/system/metadata/pdfReport/headerRight"/>
                  </xsl:call-template>
                </fo:block>
              </fo:table-cell>
            </fo:table-row>
          </fo:table-body>
        </fo:table>
      </fo:block>
    </fo:static-content>
  </xsl:template>


  <!-- Footer with catalogue name, org name and pagination -->
  <xsl:template name="fop-footer">
    <fo:static-content flow-name="xsl-region-after">
      <fo:block font-size="{$header-text-size}" color="{$font-color}">
        <fo:table width="100%" table-layout="fixed">
          <fo:table-body>
            <fo:table-row height="8mm">
              <fo:table-cell>
                <fo:block padding-top="4pt" padding-right="4pt"
                          padding-left="4pt"
                          text-align="left">
                  <xsl:call-template name="replacePlaceholders">
                    <xsl:with-param name="value" select="$env/system/metadata/pdfReport/footerLeft"/>
                  </xsl:call-template>
                </fo:block>
              </fo:table-cell>

              <fo:table-cell>
                <!-- FIXME : align all text on top and capitalize ? -->
                <fo:block padding-top="4pt" padding-right="4pt"
                          padding-left="4pt"
                          text-align="right">
                  <xsl:call-template name="replacePlaceholders">
                    <xsl:with-param name="value" select="$env/system/metadata/pdfReport/footerRight"/>
                  </xsl:call-template>
                  <xsl:if test="string($env/system/metadata/pdfReport/footerRight)"> | </xsl:if>
                  <fo:page-number/>/<fo:page-number-citation ref-id="terminator"/>
                </fo:block>
              </fo:table-cell>
            </fo:table-row>
          </fo:table-body>
        </fo:table>
      </fo:block>
    </fo:static-content>
  </xsl:template>


  <xsl:template name="replacePlaceholders">
    <xsl:param name="value"/>

    <xsl:value-of select="replace(replace(
                                $value, '\{date\}',
                                format-dateTime(current-dateTime(),$df)),
                                  '\{siteInfo\}',
                                  concat($env/system/system/site/name, '-',
                                         $env/system/system/site/organization))"/>
  </xsl:template>


  <xsl:template name="toc-page">
    <xsl:param name="res"/>

    <fo:block break-after="page">
      <fo:block font-size="{$heading1-text-size}"
                font-weight="{$heading1-text-weight}"
                text-align="center"
                margin-bottom="10pt">
        <xsl:value-of select="$translations/pdfReportTocTitle"/>
      </fo:block>


      <xsl:for-each select="$res">
        <fo:block text-align-last="justify"
                  font-size="{$toc-text-size}"
                  font-weight="{$label-weight}"
                  color="{$font-color}">
          <fo:basic-link internal-destination="section-{uuid}">
            <xsl:value-of select="resourceTitleObject"/>
            <fo:leader leader-pattern="space"/>
            <fo:page-number-citation ref-id="section-{uuid}"/>
          </fo:basic-link>
        </fo:block>
      </xsl:for-each>

    </fo:block>
  </xsl:template>


  <!--
    Print search results as PDF
  -->
  <xsl:template name="fo">
    <xsl:param name="res"/>

    <fo:table>
      <fo:table-column column-width="17cm"/>
      <fo:table-body>
        <xsl:for-each select="$res">
          <xsl:variable name="source" select="string(sourceCatalogue)"/>

          <xsl:if test="id != ''">
            <fo:table-row border-top-style="solid"
                          border-right-style="solid"
                          border-left-style="solid"
                          border-top-color="{$background-color}"
                          border-right-color="{$background-color}"
                          border-left-color="{$background-color}"
                          page-break-inside="avoid">
              <fo:table-cell display-align="center"
                             background-color="{$background-color-banner}">
                <fo:block text-align="center"
                          font-weight="{$title-weight}"
                          font-size="{$title-size}" color="{$title-color}"
                          padding-top="4pt" padding-bottom="4pt"
                          padding-left="4pt" padding-right="4pt"
                          id="section-{uuid}">
                  <xsl:value-of select="resourceTitleObject"/>
                </fo:block>
              </fo:table-cell>
            </fo:table-row>
            <fo:table-row keep-with-previous.within-page="always"
                          page-break-inside="avoid">
              <fo:table-cell>
                <fo:block margin-left="0pt" margin-right="0pt"
                          margin-top="4pt" margin-bottom="4pt">

                  <fo:table>
                    <fo:table-column column-width="3cm"/>
                    <fo:table-column column-width="13cm"/>
                    <fo:table-body>

                      <xsl:call-template name="info-rows">
                        <xsl:with-param name="label" select="$translations/abstract"/>
                        <xsl:with-param name="value" select="resourceAbstractObject"/>
                      </xsl:call-template>

                      <xsl:call-template name="info-rows">
                        <xsl:with-param name="label" select="$translations/extent"/>
                        <xsl:with-param name="content">
                          <xsl:if test="geom[. != '']">
                            <xsl:variable name="url"
                                          select="concat($nodeUrl, 'api/records/', uuid, '/extents.png')"/>

                            <fo:basic-link text-decoration="underline"
                                           color="{$link-color}"
                                           external-destination="{$url}">
                              <fo:external-graphic padding-left="4pt"
                                                   content-width="100%">
                                <xsl:attribute name="src">url('<xsl:value-of
                                  select="$url"
                                />')"
                                </xsl:attribute>
                              </fo:external-graphic>
                            </fo:basic-link>
                          </xsl:if>
                        </xsl:with-param>
                      </xsl:call-template>


                      <xsl:call-template name="info-rows">
                        <xsl:with-param name="label" select="$translations/Keywords"/>
                        <xsl:with-param name="value"
                                        select="string-join(tag, ', ')"/>
                      </xsl:call-template>

                      <xsl:if test="legalConstraints">
                        <xsl:call-template name="info-rows">
                          <xsl:with-param name="label" select="$oldGuiStrings/constraints"/>
                          <xsl:with-param name="value">
                            <xsl:for-each select="legalConstraints">- <xsl:value-of select="."/><xsl:text>
                            </xsl:text>
                            </xsl:for-each>
                          </xsl:with-param>
                        </xsl:call-template>
                      </xsl:if>

                      <xsl:if test="codelist_spatialRepresentationType_text">
                        <xsl:call-template name="info-rows">
                          <xsl:with-param name="label" select="$translations/spatialRepresentationTypes"/>
                          <xsl:with-param name="value"
                                          select="string-join(codelist_spatialRepresentationType_text, ', ')"/>
                        </xsl:call-template>
                      </xsl:if>

                      <xsl:if test="format">
                        <xsl:call-template name="info-rows">
                          <xsl:with-param name="label" select="$translations/format"/>
                          <xsl:with-param name="value"
                                          select="string-join(format, ', ')"/>
                        </xsl:call-template>
                      </xsl:if>

                      <xsl:if test="codelist_maintenanceAndUpdateFrequency_text">
                        <xsl:call-template name="info-rows">
                          <xsl:with-param name="label" select="$translations/updateFrequency"/>
                          <xsl:with-param name="value"
                                          select="string-join(codelist_maintenanceAndUpdateFrequency_text, ', ')"/>
                        </xsl:call-template>
                      </xsl:if>

                      <xsl:call-template name="info-rows">
                        <xsl:with-param name="label" select="$translations/uuid"/>
                        <xsl:with-param name="value" select="uuid"/>
                        <xsl:with-param name="content">
                          <fo:external-graphic padding-left="4pt"
                                               content-width="9pt">
                            <xsl:attribute name="src">url('<xsl:value-of
                              select="concat($baseURL, 'images/logos/', $source , '.png')"
                            />')"
                            </xsl:attribute>
                          </fo:external-graphic>
                        </xsl:with-param>
                      </xsl:call-template>

                      <xsl:call-template name="info-rows">
                        <xsl:with-param name="label" select="$translations/updatedOn"/>
                        <xsl:with-param name="value"
                                        select="changeDate"/>
                      </xsl:call-template>

                      <xsl:call-template name="metadata-resources"/>

                      <xsl:call-template name="info-rows">
                        <xsl:with-param name="label" select="$translations/onTheWeb"/>
                        <xsl:with-param name="content">
                          <fo:inline>
                            <xsl:value-of select="$translations/show"/>
                            <xsl:text> </xsl:text>
                            <fo:basic-link text-decoration="underline" color="{$link-color}">
                              <xsl:attribute name="external-destination">url('<xsl:value-of
                                select="concat($nodeUrl, 'api/records/', uuid)"
                              />')
                              </xsl:attribute>HTML
                            </fo:basic-link>
                            <fo:basic-link text-decoration="underline" color="{$link-color}">
                              <xsl:attribute name="external-destination">url('<xsl:value-of
                                select="concat($nodeUrl, 'api/records/', uuid, '/formatters/xsl-view?view=advanced&amp;output=pdf')"/>')
                              </xsl:attribute>PDF
                            </fo:basic-link>
                            <fo:basic-link text-decoration="underline" color="{$link-color}">
                              <xsl:attribute name="external-destination">url('<xsl:value-of
                                select="concat($nodeUrl, 'api/records/', uuid, '/formatters/xml')"
                              />')
                              </xsl:attribute>XML (<xsl:value-of select="if (standardName != '') then standardName else schema"/>)
                            </fo:basic-link>
                          </fo:inline>

                        </xsl:with-param>
                      </xsl:call-template>
                    </fo:table-body>
                  </fo:table>
                </fo:block>
              </fo:table-cell>
            </fo:table-row>
            <fo:table-row>
              <fo:table-cell background-color="{$background-color-thumbnail}">
                <xsl:call-template name="metadata-thumbnail-block"/>
              </fo:table-cell>
            </fo:table-row>
          </xsl:if>
        </xsl:for-each>
      </fo:table-body>
    </fo:table>
  </xsl:template>

  <xsl:template name="metadata-thumbnail-block">
    <fo:block display-align="center"
              padding-top="4pt" padding-bottom="4pt" padding-right="4pt" padding-left="4pt">
      <xsl:for-each select="overview">
        <xsl:variable name="image" select="tokenize(., '\|')[1]"/>

        <xsl:choose>
          <xsl:when test="contains($image ,'://')">
            <fo:external-graphic content-width="15cm">
              <xsl:attribute name="src">
                <xsl:text>url('</xsl:text>
                <xsl:value-of select="$image"/>
                <xsl:text>')"</xsl:text>
              </xsl:attribute>
            </fo:external-graphic>
          </xsl:when>
          <xsl:otherwise>
            <fo:external-graphic content-width="15cm">
              <xsl:attribute name="src">
                <xsl:text>url('</xsl:text>
                <xsl:value-of
                  select="concat($fullURLForService, '/', $image)"/>
                <xsl:text>')"</xsl:text>
              </xsl:attribute>
            </fo:external-graphic>
          </xsl:otherwise>
        </xsl:choose>
        <fo:block/>
      </xsl:for-each>
    </fo:block>
  </xsl:template>



  <xsl:template name="metadata-resources">
    <!-- display metadata url but only if its not a remote result -->
    <xsl:call-template name="info-rows">
      <xsl:with-param name="label" select="$translations/downloadsAndResources"/>
      <xsl:with-param name="content">
          <xsl:for-each select="link">
            <xsl:variable name="link" select="tokenize(., '\|')"/>

            <fo:basic-link text-decoration="underline" color="{$link-color}">
              <xsl:attribute name="external-destination">url('<xsl:value-of select="$link[3]"/>')
              </xsl:attribute>
              <xsl:value-of select="$link[1]"/>
              (<xsl:value-of select="$link[4]"/>)
            </fo:basic-link>
            <fo:block/>
          </xsl:for-each>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>


  <xsl:template name="info-rows">
    <xsl:param name="label"/>
    <xsl:param name="value"/>
    <xsl:param name="content"/>
    <fo:table-row>
      <fo:table-cell background-color="{if ($label != '') then $background-color else '#DDDDDD'}"
                     border-top-style="solid"
                     border-top-color="{$title-color}" border-top-width=".1pt"
                     color="{$font-color}"
                     padding-top="4pt" padding-bottom="4pt"
                     padding-right="4pt" padding-left="4pt">
        <fo:block linefeed-treatment="preserve"
                  font-weight="{$label-weight}"
                  font-family="{$font-family}" >
          <xsl:value-of select="$label"/>
        </fo:block>
      </fo:table-cell>
      <fo:table-cell color="{$font-color}"
                     border-top-style="solid"
                     border-top-color="{$font-color}" border-top-width=".1pt"
                     padding-top="4pt" padding-bottom="4pt"
                     padding-right="4pt" padding-left="4pt">
        <fo:block linefeed-treatment="preserve">
          <xsl:value-of select="$value"/>
          <xsl:copy-of select="$content"/>
        </fo:block>
      </fo:table-cell>
    </fo:table-row>
  </xsl:template>
</xsl:stylesheet>
