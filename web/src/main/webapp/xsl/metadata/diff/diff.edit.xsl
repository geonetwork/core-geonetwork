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


                    <div id="target-container" style="position:relative;overflow:auto;heigth:800px;max-height:800px;border-left:2px solid #ccf">
                        <xsl:variable name="schemaTemplate" select="concat('view-with-header-',$schema)"/>

                        <saxon:call-template name="{$schemaTemplate}">
                            <xsl:with-param name="tabs">
                                <form id="editForm" name="diffEditForm" accept-charset="UTF-8" method="POST" action="{/root/gui/locService}/metadata.update">
                                    <input class="md" type="hidden" name="id" value="{geonet:info/id}"/>
                                    <input class="md" type="hidden" name="version" value="{geonet:info/version}"/>
                                    <input class="md" type="hidden" name="ref"/>
                                    <input class="md" type="hidden" name="name"/>
                                    <input class="md" type="hidden" name="licenseurl"/>
                                    <input class="md" type="hidden" name="type"/>
                                    <input class="md" type="hidden" name="editing" value="{geonet:info/id}"/>
                                    <input class="md" type="hidden" name="minor" id="minor" value="{/root/request/minor}"/>
                                    <input class="md" type="hidden" name="child"/>
                                    <input class="md" type="hidden" name="fname"/>
                                    <input class="md" type="hidden" name="access"/>
                                    <xsl:if test="//JUSTCREATED">
                                        <input id="just-created" class="md" type="hidden" name="just-created" value="true"/>
                                    </xsl:if>
                                    <input class="md" type="hidden" name="position" value="-1"/>
                                    <!-- showvalidationerrors is only set to true when 'Check' is
                               pressed - default is false -->
                                    <input class="md" type="hidden" name="showvalidationerrors" value="{/root/request/showvalidationerrors}"/>
                                    <input class="md" type="hidden" name="currTab" value="{/root/gui/currTab}"/>

                                    <!-- Hidden div to contains extra elements like when posting multiple keywords. -->
                                    <div id="hiddenFormElements" style=""></div>

                                    <table style="max-width:800px;">
                                        <tr><td class="padded-content">
                                            <table class="md" style="max-width:800px;">
                                                <xsl:choose>
                                                    <xsl:when test="$currTab='xml'">
                                                        <xsl:apply-templates mode="xmlDocument" select=".">
                                                            <xsl:with-param name="edit" select="true()"/>
                                                        </xsl:apply-templates>
                                                    </xsl:when>
                                                    <xsl:otherwise>
                                                        <xsl:apply-templates mode="elementEP" select=".">
                                                            <xsl:with-param name="edit" select="true()"/>
                                                        </xsl:apply-templates>
                                                    </xsl:otherwise>
                                                </xsl:choose>
                                            </table>
                                        </td></tr>
                                        <tr><td class="padded-content" height="100%" align="center" valign="top">
                                            <xsl:call-template name="templateChoice"/>
                                        </td></tr>
                                    </table>
                                </form>

                                <div id="validationReport" class="content" style="display:none;"/>
                                <div id="shortcutHelp" class="content" style="display:none;">
                                    <xsl:copy-of select="/root/gui/strings/helpShortcutsEditor"/>
                                </div>
                            </xsl:with-param>
                        </saxon:call-template>
                    </div>
                </div>
            </xsl:for-each>
        </div>
    </xsl:template>


    <xsl:template name="editButtons">
        <xsl:param name="top" select="true()"/>

    </xsl:template>

    <xsl:template name="templateChoice">

        <b><xsl:value-of select="/root/gui/strings/type"/></b>
        <xsl:text>&#160;</xsl:text>
        <select class="content" name="template" size="1">
            <option value="n">
                <xsl:if test="string(geonet:info/isTemplate)='n'">
                    <xsl:attribute name="selected">true</xsl:attribute>
                </xsl:if>
                <xsl:value-of select="/root/gui/strings/metadata"/>
            </option>
            <option value="y">
                <xsl:if test="string(geonet:info/isTemplate)='y'">
                    <xsl:attribute name="selected">true</xsl:attribute>
                </xsl:if>
                <xsl:value-of select="/root/gui/strings/template"/>
            </option>

            <!-- subtemplates are disabled for the moment
               <option value="s">
                   <xsl:if test="string(geonet:info/isTemplate)='s'">
                       <xsl:attribute name="selected">true</xsl:attribute>
                   </xsl:if>
                   <xsl:value-of select="/root/gui/strings/subtemplate"/>
               </option> -->
        </select>

    </xsl:template>


</xsl:stylesheet>