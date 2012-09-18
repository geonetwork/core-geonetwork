<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:geonet="http://www.fao.org/geonetwork"
                xmlns:exslt="http://exslt.org/common"
                xmlns:dc = "http://purl.org/dc/elements/1.1/"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:saxon="http://saxon.sf.net/"
                extension-element-prefixes="saxon"
                exclude-result-prefixes="gco gmd dc exslt geonet saxon"
        >

    <xsl:include href="../common.xsl"/>

    <xsl:variable name="mode">view-simple</xsl:variable>

    <!--
     show metadata form
     -->

    <xsl:template match="/">

        <!--<html>
            <body>
                <xsl:call-template name="content"/>
            </body>
        </html> -->
        <div style="float: left; width: 50%">
            <xsl:for-each select="/root/response/source/*">

                <xsl:variable name="metadata" select="."/>
                <xsl:variable name="schema" select="$metadata/geonet:info/schema"/>

                <div class="metadata">
                    <div class="x-toolbar x-small-editor x-toolbar-layout-ct" style="padding:5px;">
                        <xsl:choose>
                            <xsl:when test="string(geonet:info/workspace)='true'">
                                <xsl:value-of select="/root/gui/strings/workspaceview"/>
                            </xsl:when>
                            <xsl:otherwise>
                                Original
                            </xsl:otherwise>
                        </xsl:choose>
                    </div>


                    <div id="source-container" style="position:relative;overflow:auto;height:100%;max-height:800px;">
                        <xsl:variable name="schemaTemplate" select="concat('view-with-header-',$schema)"/>

                        <saxon:call-template name="{$schemaTemplate}">
                            <xsl:with-param name="tabs">
                                <xsl:apply-templates mode="elementEP" select=".">
                                    <xsl:with-param name="edit" select="false()"/>
                                </xsl:apply-templates>
                            </xsl:with-param>
                        </saxon:call-template>
                    </div>
                </div>
            </xsl:for-each>
        </div>

        <div style="float: left; width: 50%">
            <xsl:for-each select="/root/response/target/*">

                <xsl:variable name="metadata" select="."/>
                <xsl:variable name="schema" select="$metadata/geonet:info/schema"/>

                <div class="metadata">
                    <div class="x-toolbar x-small-editor x-toolbar-layout-ct" style="padding:5px;">
                        <xsl:choose>
                            <xsl:when test="string(geonet:info/workspace)='true'">
                                <xsl:value-of select="/root/gui/strings/workspaceview"/>
                            </xsl:when>
                            <xsl:otherwise>
                                Modified version
                            </xsl:otherwise>
                        </xsl:choose>
                    </div>


                    <div id="target-container" style="position:relative;overflow:auto;heigth:800px;max-height:800px;border-left:2px solid #ccc">
                        <xsl:variable name="schemaTemplate" select="concat('view-with-header-',$schema)"/>

                        <saxon:call-template name="{$schemaTemplate}">
                            <xsl:with-param name="tabs">
                                <xsl:apply-templates mode="elementEP" select=".">
                                    <xsl:with-param name="edit" select="false()"/>
                                </xsl:apply-templates>
                            </xsl:with-param>
                        </saxon:call-template>
                    </div>
                </div>
            </xsl:for-each>
        </div>

    </xsl:template>

</xsl:stylesheet>