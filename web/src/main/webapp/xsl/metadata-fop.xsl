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


  <!--
		gui to show a simple element
	-->
  <xsl:template name="simpleElementFop">
    <xsl:param name="title"/>
    <xsl:param name="text"/>
    <xsl:param name="helpLink"/>
    <xsl:param name="addLink"/>
    <xsl:param name="removeLink"/>
    <xsl:param name="upLink"/>
    <xsl:param name="downLink"/>
    <xsl:param name="schematronLink"/>
    <xsl:param name="schema"/>
    <xsl:param name="edit" select="false()"/>
    
    <!-- used as do*ElementAction url anchor to go back to the same position after editing operations -->
    <xsl:param name="anchor">
      <xsl:choose>

        <!-- current node is an element -->
        <xsl:when test="geonet:element/@ref"> _ <xsl:value-of select="geonet:element/@ref"/>
        </xsl:when>

        <!-- current node is an attribute or a new child: create anchor to parent -->
        <xsl:when test="../geonet:element/@ref"> _ <xsl:value-of select="../geonet:element/@ref"/>
        </xsl:when>

      </xsl:choose>
    </xsl:param>


    <fo:table-row>
      <fo:table-cell>
        <fo:block>
          <fo:inline font-weight="bold">
            <xsl:value-of select="$title"/>
          </fo:inline>
        </fo:block>
      </fo:table-cell>
      <fo:table-cell number-columns-spanned="2">
        <fo:block>
          <xsl:value-of select="$text"/>
        </fo:block>
      </fo:table-cell>
    </fo:table-row>
  </xsl:template>


  <!--
		gui to show a complex element
	-->
  <xsl:template name="complexElementFop">
    <xsl:param name="title"/>
    <xsl:param name="text"/>
    <xsl:param name="content"/>
    <xsl:param name="helpLink"/>
    <xsl:param name="addLink"/>
    <xsl:param name="removeLink"/>
    <xsl:param name="upLink"/>
    <xsl:param name="downLink"/>
    <xsl:param name="schematronLink"/>
    <xsl:param name="schema"/>
    <xsl:param name="edit" select="false()"/>

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
              <fo:table-row>
                <fo:table-cell number-columns-spanned="3">
                  <fo:block border-top="2pt solid black">
                    <fo:inline font-weight="bold">
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
        <fo:table-row border-top-style="solid"
          border-right-style="solid" border-left-style="solid"
          border-top-color="{$background-color}"
          border-right-color="{$background-color}" border-left-color="{$background-color}">
          <fo:table-cell padding-left="4pt" padding-right="4pt" padding-top="4pt" margin-top="4pt">
            <fo:block>
              <fo:external-graphic content-width="35pt">
                <xsl:attribute name="src"> url('<xsl:value-of
                    select="concat('http://', $server/host,':', $server/port, $gui/url, '/images/logos/', $source , '.gif')"
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
        <fo:table-row border-bottom-style="solid" 
          border-right-style="solid" border-left-style="solid" border-bottom-color="{$background-color}"
          border-right-color="{$background-color}" border-left-color="{$background-color}">
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



                            <!-- display metadata url but only if its not a remote result -->
                            <xsl:call-template name="info-rows">
                              <xsl:with-param name="label" select="$gui/strings/resources"/>
                              <xsl:with-param name="content">
                                <xsl:choose>
                                  <xsl:when test="$remote=false()"><fo:basic-link
                                      text-decoration="underline" color="blue">
                                      <xsl:attribute name="external-destination"> url('<xsl:value-of
                                          select="concat('http://', $server/host,':', $server/port, $gui/locService,'/metadata.show?id=', $metadata/geonet:info/id, '&amp;currTab=simple')"
                                        />') </xsl:attribute>
                                      <xsl:value-of select="$gui/strings/show"/>
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
                                      <xsl:attribute name="external-destination"> url('<xsl:value-of
                                          select="."/>') </xsl:attribute>
                                      <xsl:value-of select="$gui/strings/download"/>
                                    </fo:basic-link> | </xsl:for-each>
                                </xsl:if>

                                <xsl:if test="$metadata/geonet:info/dynamic='true'">
                                  <xsl:for-each
                                    select="$metadata/link[@type='application/vnd.ogc.wms_xml']">
                                    <fo:basic-link text-decoration="underline" color="blue">
                                      <xsl:attribute name="external-destination"> url('<xsl:value-of
                                          select="@href"/>') </xsl:attribute>
                                      <xsl:value-of select="$gui/strings/visualizationService"/>
                                      (<xsl:value-of select="@title"/>)
                                    </fo:basic-link> | </xsl:for-each>
                                </xsl:if>

                              </xsl:with-param>
                            </xsl:call-template>

                          </fo:table-body>
                        </fo:table>

                      </fo:block>
                    </fo:table-cell>
                    <fo:table-cell background-color="{$background-color-thumbnail}">
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
                                    select="concat('http://', $server/host,':', $server/port, $metadata/image[1])"/>
                                  <xsl:text>')"</xsl:text>
                                </xsl:attribute>
                              </fo:external-graphic>
                            </xsl:otherwise>
                          </xsl:choose>
                        </xsl:if>
                      </fo:block>
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
          <fo:table-cell display-align="center"
            background-color="{$background-color-banner}">
            <!-- FIXME : align all text on top and capitalize ? -->
            <fo:block font-family="{$font-family}" font-size="{$header-size}"
              color="{$title-color}" font-weight="{$header-weight}" 
              padding-top="4pt" padding-right="4pt" padding-left="4pt">
              <fo:external-graphic padding-right="4pt">
                <xsl:attribute name="src"> url('<xsl:value-of
                    select="concat('http://', //server/host,':', //server/port, /root/gui/url,'/images/logos/', /root/gui/env/site/siteId,'.gif')"
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
    <fo:table-row border-bottom-style="solid" border-top-style="solid" border-top-color="{$title-color}" border-top-width=".1pt"
      border-bottom-color="{$title-color}" border-bottom-width=".1pt">
      <fo:table-cell background-color="{$background-color}" 
        color="{$title-color}"
        padding-top="4pt" padding-bottom="4pt" padding-right="4pt" padding-left="4pt">
        <fo:block>
          <xsl:value-of select="$label"/>
        </fo:block>
      </fo:table-cell>
      <fo:table-cell 
        color="{$font-color}" padding-top="4pt" 
        padding-bottom="4pt" padding-right="4pt" padding-left="4pt">
        <fo:block>
          <xsl:value-of select="$value"/>
          <xsl:copy-of select="$content"/>
        </fo:block>
      </fo:table-cell>
    </fo:table-row>
  </xsl:template>

</xsl:stylesheet>
