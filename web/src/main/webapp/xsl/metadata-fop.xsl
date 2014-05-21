<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:exslt="http://exslt.org/common" exclude-result-prefixes="exslt"
  xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:geonet="http://www.fao.org/geonetwork"
  xmlns:fo="http://www.w3.org/1999/XSL/Format">


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

  <!-- Date format for footer information -->
  <xsl:variable name="df">[Y0001]-[M01]-[D01]</xsl:variable>


  <!-- ======================================================
    FOP master configuration A4 with margins
    -->
  <xsl:template name="fop-master">
    <fo:layout-master-set>
      <fo:simple-page-master master-name="simpleA4" page-height="29.7cm" page-width="21cm"
        margin-top=".2cm" margin-bottom=".2cm" margin-left=".6cm" margin-right=".2cm">
        <fo:region-body margin-top="0cm"/>
        <fo:region-after extent=".2cm"/>
      </fo:simple-page-master>

      <fo:page-sequence-master master-name="PSM_Name">
        <fo:single-page-master-reference master-reference="simpleA4"/>
      </fo:page-sequence-master>
    </fo:layout-master-set>
  </xsl:template>

  <!-- ======================================================
        Page footer with node info, date and paging info 
  -->
  <xsl:template name="fop-footer">
    <!-- Footer with catalogue name, org name and pagination -->
    <fo:static-content flow-name="xsl-region-after">
      <fo:block text-align="end" font-family="{$font-family}" font-size="{$note-size}"
        color="{$font-color}">
        <xsl:value-of select="/root/gui/env/site/name"/> - <xsl:value-of
          select="/root/gui/env/site/organization"/> | <!-- TODO : set date format according to locale -->
        <xsl:value-of select="format-dateTime(current-dateTime(),$df)"/> | <fo:page-number/> /
          <fo:page-number-citation ref-id="terminator"/>
      </fo:block>
    </fo:static-content>
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
        <xsl:when test="geonet:element/@ref"> _ <xsl:value-of select="geonet:element/@ref"/>
        </xsl:when>

        <!-- current node is a new child: create anchor to parent -->
        <xsl:when test="../geonet:element/@ref"> _ <xsl:value-of select="../geonet:element/@ref"/>
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
                border-right-color="{$background-color}" border-left-color="{$background-color}">
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
    
      <fo:table-row >
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
    <xsl:param name="server"/>
    <xsl:param name="gui"/>
    <xsl:param name="remote"/>

    <xsl:for-each select="$res/*">

      <xsl:variable name="md">
        <xsl:apply-templates mode="brief" select="."/>
      </xsl:variable>
      <xsl:variable name="metadata" select="exslt:node-set($md)/*[1]"/>
      <xsl:variable name="source" select="string($metadata/geonet:info/source)"/>


      <xsl:if test="$metadata/geonet:info/id != ''">
        <fo:table-row border-top-style="solid" border-right-style="solid" border-left-style="solid"
          border-top-color="{$background-color}" border-right-color="{$background-color}"
          border-left-color="{$background-color}"
          page-break-inside="avoid">
          <fo:table-cell padding-left="4pt" padding-right="4pt" padding-top="4pt" margin-top="4pt">
            <fo:block>
              <fo:external-graphic content-width="35pt">
                <xsl:attribute name="src"> url('<xsl:value-of
                    select="concat($server/protocol, '://', $server/host,':', $server/port, $gui/url, '/images/logos/', $source , '.gif')"
                  />')" </xsl:attribute>
              </fo:external-graphic>
            </fo:block>
          </fo:table-cell>
          <fo:table-cell display-align="center">
            <fo:block font-weight="{$title-weight}" font-size="{$title-size}" color="{$title-color}"
              padding-top="4pt" padding-bottom="4pt" padding-left="4pt" padding-right="4pt">
              <xsl:value-of select="$metadata/title"/>
            </fo:block>
          </fo:table-cell>
        </fo:table-row>
        <fo:table-row border-bottom-style="solid" border-right-style="solid"
          border-left-style="solid" border-bottom-color="{$background-color}"
          border-right-color="{$background-color}" border-left-color="{$background-color}"
          keep-with-previous.within-page="always"
          page-break-inside="avoid">
          <fo:table-cell number-columns-spanned="2">
            <fo:block margin-left="2pt" margin-right="4pt" margin-top="4pt" margin-bottom="4pt">
              <fo:table>
                <fo:table-column column-width="14.8cm"/>
                <fo:table-column column-width="4.8cm"/>
                <fo:table-body>
                  <fo:table-row>
                    <fo:table-cell>
                      <fo:block>

                        <!-- Labels and values-->
                        <fo:table>
                          <fo:table-column column-width="3cm"/>
                          <fo:table-column column-width="11.4cm"/>

                          <fo:table-body>
                            <xsl:call-template name="info-rows">
                              <xsl:with-param name="label" select="$gui/strings/uuid"/>
                              <xsl:with-param name="value" select="$metadata/geonet:info/uuid"/>
                            </xsl:call-template>


                            <xsl:call-template name="info-rows">
                              <xsl:with-param name="label" select="$gui/strings/abstract"/>
                              <xsl:with-param name="value" select="$metadata/abstract"/>
                            </xsl:call-template>


                            <xsl:call-template name="info-rows">
                              <xsl:with-param name="label" select="$gui/strings/keywords"/>
                              <xsl:with-param name="value"
                                select="string-join($metadata/keyword, ', ')"/>
                            </xsl:call-template>


                            <xsl:call-template name="info-rows">
                              <xsl:with-param name="label" select="$gui/strings/schema"/>
                              <xsl:with-param name="value" select="$metadata/geonet:info/schema"/>
                            </xsl:call-template>

                            <xsl:call-template name="metadata-resources">
                              <xsl:with-param name="gui" select="$gui"/>
                              <xsl:with-param name="server" select="$server"/>
                              <xsl:with-param name="metadata" select="$metadata"/>
                            </xsl:call-template>

                          </fo:table-body>
                        </fo:table>

                      </fo:block>
                    </fo:table-cell>
                    <fo:table-cell background-color="{$background-color-thumbnail}">
                      <xsl:call-template name="metadata-thumbnail-block">
                        <xsl:with-param name="metadata" select="$metadata"/>
                        <xsl:with-param name="server" select="$server"/>
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
    <xsl:param name="server"/>

    <fo:block padding-top="4pt" padding-bottom="4pt" padding-right="4pt" padding-left="4pt">
      <!-- Thumbnails - Use the first one only -->
      <xsl:if test="$metadata/image">
        <xsl:choose>
          <xsl:when test="contains($metadata/image[1] ,'://')">
            <fo:external-graphic content-width="4.6cm">
              <xsl:attribute name="src">
                <xsl:text>url('</xsl:text>
                <xsl:value-of select="$metadata/image[1]"/>
                <xsl:text>')"</xsl:text>
              </xsl:attribute>
            </fo:external-graphic>
          </xsl:when>
          <xsl:otherwise>
            <fo:external-graphic content-width="4.6cm">
              <xsl:attribute name="src">
                <xsl:text>url('</xsl:text>
                <xsl:value-of
                  select="concat($server/protocol, '://', $server/host,':', $server/port, $metadata/image[1])"/>
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
    <xsl:param name="server"/>
    <xsl:param name="metadata"/>
    <xsl:param name="title" select="true()"/>
    <xsl:param name="remote" select="false()"/>

    <!-- display metadata url but only if its not a remote result -->
    <xsl:call-template name="info-rows">
      <xsl:with-param name="label" select="if ($title) then $gui/strings/resources else ''"/>
      <xsl:with-param name="content">
        <xsl:choose>
          <xsl:when test="$remote=false()"><fo:basic-link text-decoration="underline" color="blue">
							<xsl:choose>
								<xsl:when test="/root/gui/config/client/@widget='true'">
									<xsl:attribute name="external-destination"> url('<xsl:value-of
										select="concat($server/protocol, '://', $server/host,':', $server/port, /root/gui/url, '/apps/tabsearch/', /root/gui/config/client/@url,'?uuid=', $metadata/geonet:info/uuid , '&amp;hl=', /root/gui/language)"
									/>') </xsl:attribute>
								</xsl:when>
								<xsl:otherwise>
									<xsl:attribute name="external-destination"> url('<xsl:value-of
										select="concat($server/protocol, '://', $server/host,':', $server/port, /root/gui/url, '/srv/',/root/gui/language, '/' ,/root/gui/config/client/@url,'?uuid=', $metadata/geonet:info/uuid)"
									/>') </xsl:attribute>
								</xsl:otherwise>
							</xsl:choose>
              <xsl:value-of select="$gui/strings/show"/>
          </fo:basic-link> | <fo:basic-link text-decoration="underline" color="blue">
            <xsl:attribute name="external-destination"> url('<xsl:value-of
              select="concat($server/protocol, '://', $server/host,':', $server/port, /root/gui/url, '/srv/',/root/gui/language,'/xml.metadata.get?uuid=', $metadata/geonet:info/uuid)"
            />') </xsl:attribute>
            <xsl:value-of select="$gui/strings/show"/> (XML)
          </fo:basic-link> | </xsl:when>
          <xsl:otherwise>
            <fo:block text-align="left" font-style="italic">
              <xsl:text>Z3950: </xsl:text>
              <xsl:value-of select="$metadata/geonet:info/server"/>
              <xsl:text> </xsl:text>
            </fo:block>
          </xsl:otherwise>
        </xsl:choose>

        <xsl:if test="$metadata/geonet:info/download='true'">
          <xsl:for-each select="$metadata/link[@type='download']">
            <fo:basic-link text-decoration="underline" color="blue">
              <xsl:attribute name="external-destination"> url('<xsl:value-of select="."/>') </xsl:attribute>
              <xsl:value-of select="$gui/strings/download"/>
            </fo:basic-link> | </xsl:for-each>
        </xsl:if>

        <xsl:if test="$metadata/geonet:info/dynamic='true'">
          <xsl:for-each select="$metadata/link[@type='application/vnd.ogc.wms_xml']">
            <fo:basic-link text-decoration="underline" color="blue">
              <xsl:attribute name="external-destination"> url('<xsl:value-of select="@href"/>') </xsl:attribute>
              <xsl:value-of select="$gui/strings/visualizationService"/> (<xsl:value-of
                select="@title"/>) </fo:basic-link> | </xsl:for-each>
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
                <xsl:attribute name="src"> url('<xsl:value-of
                    select="concat( /root/gui/env/server/protocol, '://', /root/gui/env/server/host,':', /root/gui/env/server/port, /root/gui/url,'/images/logos/', /root/gui/env/site/siteId,'.gif')"
                  />')" </xsl:attribute>
              </fo:external-graphic>
              <xsl:value-of select="upper-case(/root/gui/env/site/name)"/> (<xsl:value-of
                select="upper-case(/root/gui/env/site/organization)"/>) </fo:block>
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
      border-top-color="{$title-color}" border-top-width=".1pt" border-bottom-color="{$title-color}"
      border-bottom-width=".1pt">
      <fo:table-cell background-color="{if ($label != '') then $background-color else ''}"
        color="{$title-color}" padding-top="4pt" padding-bottom="4pt" padding-right="4pt"
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
