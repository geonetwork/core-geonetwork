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

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:exslt="http://exslt.org/common"
                xmlns:geonet="http://www.fao.org/geonetwork" xmlns:fo="http://www.w3.org/1999/XSL/Format"
                version="2.0"
                exclude-result-prefixes="exslt">


  <!-- Some colors -->
  <xsl:variable name="background-color">#d6e2f7</xsl:variable>
  <xsl:variable name="background-color-banner">#ffffff</xsl:variable>
  <xsl:variable name="background-color-thumbnail">#f1f2f3</xsl:variable>
  <xsl:variable name="border-color">#b3c6e6</xsl:variable>

  <xsl:variable name="header-border">2pt solid #2e456b</xsl:variable>

  <!-- Some font properties -->
  <xsl:variable name="font-color">#707070</xsl:variable>
  <xsl:variable name="font-size">8pt</xsl:variable>
  <xsl:variable name="font-family">verdana</xsl:variable>
  <xsl:variable name="title-color">#2e456b</xsl:variable>
  <xsl:variable name="title-size">12pt</xsl:variable>
  <xsl:variable name="title-weight">bold</xsl:variable>
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


  <!-- ======================================================
    FOP master configuration A4 with margins
    -->
  <xsl:template name="fop-master">
    <fo:layout-master-set>
      <fo:simple-page-master master-name="simpleA4" page-height="29.7cm" page-width="21cm"
                             margin-top="1.25cm" margin-bottom="1cm" margin-left="2cm"
                             margin-right="2cm">
        <fo:region-body margin-top="1.5cm" margin-bottom="3cm" />
        <fo:region-before extent="0.8cm" />
        <fo:region-after extent="0.8cm" />
      </fo:simple-page-master>

      <fo:simple-page-master master-name="Intro" page-height="29.7cm" page-width="21cm"
                             margin-top="1.25cm" margin-bottom="1cm" margin-left="2cm"
                             margin-right="2cm">
        <fo:region-body margin-top="1.5cm" margin-bottom="3cm" />
        <fo:region-before extent="0.8cm" />
        <fo:region-after extent="0.8cm"/>
      </fo:simple-page-master>

      <fo:page-sequence-master master-name="PSM_Name">
        <fo:single-page-master-reference master-reference="simpleA4"/>
      </fo:page-sequence-master>
    </fo:layout-master-set>
  </xsl:template>


  <!-- ======================================================
        Page header
  -->
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
                    <xsl:with-param name="value" select="$env/metadata/pdfReport/headerLeft" />
                  </xsl:call-template>
                </fo:block>
              </fo:table-cell>

              <fo:table-cell>
                <fo:block padding-top="4pt" padding-right="4pt"
                          padding-left="4pt"
                          text-align="right"
                >
                  <xsl:call-template name="replacePlaceholders">
                    <xsl:with-param name="value" select="$env/metadata/pdfReport/headerRight" />
                  </xsl:call-template>
                </fo:block>
              </fo:table-cell>
            </fo:table-row>

          </fo:table-body>
        </fo:table>
      </fo:block>
    </fo:static-content>
  </xsl:template>


  <!-- ======================================================
        Page footer with node info, date and paging info
  -->
  <xsl:template name="fop-footer">

    <!-- Footer with catalogue name, org name and pagination -->
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
                    <xsl:with-param name="value" select="$env/metadata/pdfReport/footerLeft" />
                  </xsl:call-template>
                </fo:block>
              </fo:table-cell>

              <fo:table-cell>
                <!-- FIXME : align all text on top and capitalize ? -->
                <fo:block padding-top="4pt" padding-right="4pt"
                          padding-left="4pt"
                          text-align="right"
                >
                  <xsl:call-template name="replacePlaceholders">
                    <xsl:with-param name="value" select="$env/metadata/pdfReport/footerRight" />
                  </xsl:call-template>
                  <xsl:if test="string($env/metadata/pdfReport/footerRight)"> | </xsl:if>
                  <fo:page-number/> / <fo:page-number-citation ref-id="terminator"/>
                </fo:block>
              </fo:table-cell>
            </fo:table-row>

          </fo:table-body>
        </fo:table>
      </fo:block>
    </fo:static-content>
  </xsl:template>


  <xsl:template name="replacePlaceholders">
    <xsl:param name="value" />

    <xsl:value-of select="replace(replace($value, '\{date\}', format-dateTime(current-dateTime(),$df)),
                                  '\{siteInfo\}', concat($env/system/site/name, '-', $env/system/site/organization))" />
  </xsl:template>


  <!-- ======================================================
      TOC page
  -->
  <xsl:template name="toc-page">
    <xsl:param name="res"/>

    <fo:block break-after="page">

      <fo:block font-size="{$heading1-text-size}" font-weight="{$heading1-text-weight}" text-align="center" margin-bottom="10pt">
        <xsl:value-of select="/root/gui/strings/pdfReportTocTitle" />
      </fo:block>


      <xsl:for-each select="$res/*[name() != 'summary' and name() != 'from' and name() != 'to']" >
        <xsl:variable name="md">
          <!--<xsl:apply-templates mode="briefPdf" select="."/>-->
          <!-- Using search service with fast=index to retrieve the information directly from the index -->
          <xsl:copy-of select="."/>
        </xsl:variable>

        <xsl:variable name="metadata" select="exslt:node-set($md)/*[1]"/>

        <fo:block text-align-last="justify" font-size="{$toc-text-size}" color="{$title-color}">
          <fo:basic-link internal-destination="section-{$metadata/geonet:info/id}">
            <xsl:value-of select="$metadata/title" />
            <fo:leader leader-pattern="space" />
            <fo:page-number-citation ref-id="section-{$metadata/geonet:info/id}" />
          </fo:basic-link>
        </fo:block>
      </xsl:for-each>

    </fo:block>
  </xsl:template>


  <!--
    gui to show a complex element
  -->
  <xsl:template name="complexElementFop">
    <xsl:param name="title"/>
    <xsl:param name="text"/>
    <xsl:param name="content"/>

    <!-- used as do*ElementAction url anchor to go back to the same position after editing operations -->
    <xsl:param name="anchor">
      <xsl:choose>

        <!-- current node is an element -->
        <xsl:when test="geonet:element/@ref">_
          <xsl:value-of select="geonet:element/@ref"/>
        </xsl:when>

        <!-- current node is a new child: create anchor to parent -->
        <xsl:when test="../geonet:element/@ref">_
          <xsl:value-of select="../geonet:element/@ref"/>
        </xsl:when>

      </xsl:choose>
    </xsl:param>

    <fo:table-row>
      <fo:table-cell>
        <fo:block>
          <fo:table width="100%" table-layout="fixed">
            <fo:table-column column-width="3cm"/>
            <fo:table-column column-width="12cm"/>
            <fo:table-column column-width="1cm"/>
            <fo:table-body>
              <fo:table-row border-top-style="solid" border-right-style="solid"
                            border-left-style="solid" border-top-color="{$background-color}"
                            border-right-color="{$background-color}"
                            border-left-color="{$background-color}">
                <fo:table-cell padding-left="4pt" padding-right="4pt" padding-top="4pt"
                               margin-top="4pt" number-columns-spanned="3">
                  <fo:block border-top="2pt solid black">
                    <fo:inline>
                      <xsl:text>::</xsl:text>
                      <xsl:value-of select="$title"/>
                    </fo:inline>
                  </fo:block>
                </fo:table-cell>
              </fo:table-row>
              <fo:table-row>
                <fo:table-cell>
                  <fo:block/>
                </fo:table-cell>
                <fo:table-cell>
                  <fo:block>
                    <xsl:variable name="n" select="exslt:node-set($content)"/>
                    <xsl:if test="$n/node()">
                      <fo:table table-layout="fixed" width="100%" border-collapse="separate">
                        <fo:table-body>
                          <xsl:copy-of select="$content"/>
                        </fo:table-body>
                      </fo:table>
                    </xsl:if>
                  </fo:block>
                </fo:table-cell>
              </fo:table-row>
            </fo:table-body>
          </fo:table>
        </fo:block>
      </fo:table-cell>
    </fo:table-row>

  </xsl:template>


  <!--
   gui to show a block element
 -->
  <xsl:template name="blockElementFop">
    <xsl:param name="block"/>
    <xsl:param name="label"/>
    <xsl:param name="color">blue</xsl:param>

    <fo:table-row>
      <fo:table-cell padding-left="4pt" padding-right="4pt" padding-top="4pt" margin-top="4pt"
                     number-columns-spanned="2">
        <fo:block>
          <xsl:if test="$block != ''">
            <xsl:if test="$label!=''">
              <fo:inline font-size="{$title-size}" font-weight="{$title-weight}"
                         color="{$title-color}" margin="8pt">
                <xsl:value-of select="$label"/>
              </fo:inline>
            </xsl:if>
            <fo:table width="100%" table-layout="fixed">
              <fo:table-column column-width="5cm"/>
              <fo:table-column column-width="15cm"/>
              <fo:table-body>
                <xsl:copy-of select="$block"/>
              </fo:table-body>
            </fo:table>
          </xsl:if>
        </fo:block>
      </fo:table-cell>
    </fo:table-row>
  </xsl:template>

  <!-- ===========================================
    metadata result to fop
  -->
  <xsl:template name="fo">
    <xsl:param name="res"/>

    <xsl:for-each select="$res/*[name() != 'summary' and name() != 'from' and name() != 'to']">

      <xsl:variable name="md">
        <!--<xsl:apply-templates mode="briefPdf" select="."/>-->
        <!-- Using search service with fast=index to retrieve the information directly from the index -->
        <xsl:copy-of select="."/>
      </xsl:variable>
      <xsl:variable name="metadata" select="exslt:node-set($md)/*[1]"/>
      <xsl:variable name="source" select="string($metadata/geonet:info/source)"/>


      <xsl:if test="$metadata/geonet:info/id != ''">
        <fo:table-row border-top-style="solid" border-right-style="solid" border-left-style="solid"
                      border-top-color="{$background-color}"
                      border-right-color="{$background-color}"
                      border-left-color="{$background-color}"
                      page-break-inside="avoid">
          <fo:table-cell padding-left="4pt" padding-right="4pt" padding-top="4pt" margin-top="4pt">
            <fo:block>
              <fo:external-graphic content-width="35pt">
                <xsl:attribute name="src">url('<xsl:value-of
                  select="concat($baseURL, '/images/logos/', $source , '.gif')"
                />')"
                </xsl:attribute>
              </fo:external-graphic>
            </fo:block>
          </fo:table-cell>
          <fo:table-cell display-align="center">
            <fo:block font-weight="{$title-weight}" font-size="{$title-size}" color="{$title-color}"
                      padding-top="4pt" padding-bottom="4pt" padding-left="4pt" padding-right="4pt"
                      id="section-{$metadata/geonet:info/id}">
              <xsl:value-of select="$metadata/title"/>
            </fo:block>
          </fo:table-cell>
        </fo:table-row>
        <fo:table-row border-bottom-style="solid" border-right-style="solid"
                      border-left-style="solid" border-bottom-color="{$background-color}"
                      border-right-color="{$background-color}"
                      border-left-color="{$background-color}"
                      keep-with-previous.within-page="always"
                      page-break-inside="avoid">
          <fo:table-cell number-columns-spanned="2">
            <fo:block margin-left="2pt" margin-right="4pt" margin-top="4pt" margin-bottom="4pt">
              <fo:table>
                <fo:table-column column-width="11.8cm"/>
                <fo:table-column column-width="4.8cm"/>
                <fo:table-body>
                  <fo:table-row>
                    <fo:table-cell>
                      <fo:block>

                        <!-- Labels and values-->
                        <fo:table>
                          <fo:table-column column-width="2.5cm"/>
                          <fo:table-column column-width="9.3cm"/>

                          <fo:table-body>
                            <xsl:call-template name="info-rows">
                              <xsl:with-param name="label" select="$oldGuiStrings/uuid"/>
                              <xsl:with-param name="value" select="$metadata/geonet:info/uuid"/>
                            </xsl:call-template>


                            <xsl:call-template name="info-rows">
                              <xsl:with-param name="label" select="$oldGuiStrings/abstract"/>
                              <xsl:with-param name="value" select="$metadata/abstract"/>
                            </xsl:call-template>


                            <xsl:call-template name="info-rows">
                              <xsl:with-param name="label" select="$oldGuiStrings/keywords"/>
                              <xsl:with-param name="value"
                                              select="string-join($metadata/keyword, ', ')"/>
                            </xsl:call-template>


                            <xsl:call-template name="info-rows">
                              <xsl:with-param name="label" select="$oldGuiStrings/schema"/>
                              <xsl:with-param name="value" select="$metadata/geonet:info/schema"/>
                            </xsl:call-template>

                            <xsl:call-template name="metadata-resources">
                              <xsl:with-param name="gui" select="$oldGuiStrings"/>
                              <xsl:with-param name="metadata" select="$metadata"/>
                            </xsl:call-template>

                          </fo:table-body>
                        </fo:table>

                      </fo:block>
                    </fo:table-cell>
                    <fo:table-cell background-color="{$background-color-thumbnail}">
                      <xsl:call-template name="metadata-thumbnail-block">
                        <xsl:with-param name="metadata" select="$metadata"/>
                      </xsl:call-template>
                    </fo:table-cell>
                  </fo:table-row>
                </fo:table-body>
              </fo:table>
            </fo:block>
          </fo:table-cell>
        </fo:table-row>
        <fo:table-row height=".3cm">
          <fo:table-cell>
            <fo:block/>
          </fo:table-cell>
        </fo:table-row>
      </xsl:if>
    </xsl:for-each>
  </xsl:template>


  <!-- Metadata thumbnail -->
  <xsl:template name="metadata-thumbnail-block">
    <xsl:param name="metadata"/>

    <fo:block padding-top="4pt" padding-bottom="4pt" padding-right="4pt" padding-left="4pt">
      <!-- Format:
         <image>thumbnail|resources.get?uuid=da165110-88fd-11da-a88f-000d939bc5d8&fname=thumbnail_s.gif&access=public</image>
      -->

      <!-- Thumbnails - Use the first one only -->
      <xsl:if test="$metadata/image">
        <xsl:variable name="image" select="tokenize($metadata/image[1], '\|')[2]"/>

        <xsl:choose>
          <xsl:when test="contains($image ,'://')">
            <fo:external-graphic content-width="4.6cm">
              <xsl:attribute name="src">
                <xsl:text>url('</xsl:text>
                <xsl:value-of select="$image"/>
                <xsl:text>')"</xsl:text>
              </xsl:attribute>
            </fo:external-graphic>
          </xsl:when>
          <xsl:otherwise>
            <fo:external-graphic content-width="4.6cm">
              <xsl:attribute name="src">
                <xsl:text>url('</xsl:text>
                <xsl:value-of
                  select="concat($fullURLForService, '/', $image)"/>
                <xsl:text>')"</xsl:text>
              </xsl:attribute>
            </fo:external-graphic>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:if>
    </fo:block>
  </xsl:template>

  <!-- ====================================
    List of metadata resources based on online source section.
    Metadata must be in brief format
  -->
  <xsl:template name="metadata-resources">
    <xsl:param name="gui"/>
    <xsl:param name="metadata"/>
    <xsl:param name="title" select="true()"/>
    <xsl:param name="remote" select="false()"/>

    <!-- display metadata url but only if its not a remote result -->
    <xsl:call-template name="info-rows">
      <xsl:with-param name="label" select="if ($title) then $oldGuiStrings/resources else ''"/>
      <xsl:with-param name="content">
        <xsl:choose>
          <xsl:when test="$remote=false()">
            <fo:basic-link text-decoration="underline" color="blue">
              <xsl:attribute name="external-destination">url('<xsl:value-of
                select="concat($baseURL, '?uuid=', $metadata/geonet:info/uuid)"
              />')
              </xsl:attribute>
              <xsl:value-of select="$oldGuiStrings/show"/>
            </fo:basic-link>
            <fo:block/>
            <fo:basic-link text-decoration="underline" color="blue">
              <xsl:attribute name="external-destination">url('<xsl:value-of
                select="concat($baseURL, '/srv/', $lang, '/xml.metadata.get?uuid=', $metadata/geonet:info/uuid)"
              />')
              </xsl:attribute>
              <xsl:value-of select="$oldGuiStrings/show"/> (XML)
            </fo:basic-link>
            <fo:block/>
          </xsl:when>
          <xsl:otherwise>
            <fo:block text-align="left" font-style="italic">
              <xsl:text>Z3950: </xsl:text>
              <xsl:value-of select="$metadata/geonet:info/server"/>
              <xsl:text> </xsl:text>
            </fo:block>
          </xsl:otherwise>
        </xsl:choose>

        <xsl:if test="$metadata/geonet:info/download='true'">
          <!-- Format:
          <link>phy.zip|Physiography of North and Central Eurasia Landform (Gif Format)|http://localhost:8080/geonetwork/srv/en/resources.get?uuid=78f93047-74f8-4419-ac3d-fc62e4b0477b&fname=phy.zip&access=private|WWW:DOWNLOAD-1.0-http- -download|application/zip</link>
          -->

          <xsl:for-each select="$metadata/link[contains(., 'WWW:DOWNLOAD-1.0-http--download')]">
            <xsl:variable name="link" select="tokenize(., '\|')[3]"/>

            <fo:basic-link text-decoration="underline" color="blue">
              <xsl:attribute name="external-destination">url('<xsl:value-of select="$link"/>')
              </xsl:attribute>
              <xsl:value-of select="$oldGuiStrings/download"/>
            </fo:basic-link>
            <fo:block/>
          </xsl:for-each>
        </xsl:if>

        <xsl:if test="$metadata/geonet:info/dynamic='true'">
          <!-- Format:
          <link>landform|Physiography of North and Central Eurasia Landform|http://geonetwork3.fao.org/ows/7386_landf|OGC:WMS-1.1.1-http-get-map|application/vnd.ogc.wms_xml</link>
          -->

          <xsl:for-each select="$metadata/link[contains(., 'application/vnd.ogc.wms_xml')]">
            <xsl:variable name="title" select="tokenize(., '\|')[2]"/>
            <xsl:variable name="link" select="tokenize(., '\|')[3]"/>

            <fo:basic-link text-decoration="underline" color="blue">
              <xsl:attribute name="external-destination">url('<xsl:value-of select="$link"/>')
              </xsl:attribute>
              <xsl:value-of select="$oldGuiStrings/visualizationService"/> (<xsl:value-of
              select="$title"/>)
            </fo:basic-link>
            <fo:block/>
          </xsl:for-each>
        </xsl:if>

      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>


  <!--
    main pdf banner
  -->
  <xsl:template name="banner">
    <fo:table table-layout="fixed" width="100%">
      <fo:table-column column-width="20cm"/>
      <!--<fo:table-column column-width="4cm"/>-->
      <fo:table-body>
        <fo:table-row border-bottom-style="solid" border-bottom-color="{$header-color}"
                      border-bottom-width="1pt">
          <fo:table-cell display-align="center" background-color="{$background-color-banner}">
            <!-- FIXME : align all text on top and capitalize ? -->
            <fo:block font-family="{$font-family}" font-size="{$header-size}" color="{$title-color}"
                      font-weight="{$header-weight}" padding-top="4pt" padding-right="4pt"
                      padding-left="4pt">
              <fo:external-graphic padding-right="4pt">
                <xsl:attribute name="src">url('<xsl:value-of
                  select="concat($baseURL, '/images/logos/', $env/system/site/siteId, '.gif')"
                />')"
                </xsl:attribute>
              </fo:external-graphic>
              <xsl:value-of select="upper-case($env/system/site/name)"/> (<xsl:value-of
              select="upper-case($env/system/site/organization)"/>)
            </fo:block>
          </fo:table-cell>
          <!-- <fo:table-cell display-align="right" text-align="top"
            background-color="{$background-color-banner}">
            <fo:block text-align="right" font-family="{$font-family}" font-size="{$header-size}"
              color="{$header-color}" font-weight="{$header-weight}"
              padding-top="4pt" padding-bottom="4pt" padding-right="4pt" padding-left="4pt">
              <xsl:value-of select="upper-case(/root/gui/strings/searchResult)"/>
            </fo:block>
          </fo:table-cell>-->
        </fo:table-row>
      </fo:table-body>
    </fo:table>
  </xsl:template>

  <xsl:template name="info-rows">
    <xsl:param name="label"/>
    <xsl:param name="value"/>
    <xsl:param name="content"/>
    <fo:table-row border-bottom-style="solid" border-top-style="solid"
                  border-top-color="{$title-color}" border-top-width=".1pt"
                  border-bottom-color="{$title-color}"
                  border-bottom-width=".1pt">
      <fo:table-cell background-color="{if ($label != '') then $background-color else ''}"
                     color="{$title-color}" padding-top="4pt" padding-bottom="4pt"
                     padding-right="4pt"
                     padding-left="4pt">
        <fo:block linefeed-treatment="preserve">
          <xsl:value-of select="$label"/>
        </fo:block>
      </fo:table-cell>
      <fo:table-cell color="{$font-color}" padding-top="4pt" padding-bottom="4pt"
                     padding-right="4pt" padding-left="4pt">
        <fo:block linefeed-treatment="preserve">
          <xsl:value-of select="$value"/>
          <xsl:copy-of select="$content"/>
        </fo:block>
      </fo:table-cell>
    </fo:table-row>
  </xsl:template>
</xsl:stylesheet>
