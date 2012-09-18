<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:geonet="http://www.fao.org/geonetwork"
                xmlns:exslt="http://exslt.org/common"
                xmlns:dc = "http://purl.org/dc/elements/1.1/"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                exclude-result-prefixes="gco gmd dc exslt geonet"
        >

    <!--
     show metadata form
     -->

    <xsl:include href="main.xsl"/>
    <xsl:include href="metadata.xsl"/>

    <xsl:variable name="protocol" select="/root/gui/env/server/protocol" />
    <xsl:variable name="host" select="/root/gui/env/server/host" />
    <xsl:variable name="port" select="/root/gui/env/server/port" />
    <xsl:variable name="baseURL" select="concat($protocol,'://',$host,':',$port,/root/gui/url)" />
    <xsl:variable name="serverUrl" select="concat($protocol,'://',$host,':',$port,/root/gui/locService)" />

    <xsl:template mode="css" match="/">
        <xsl:if test="$currTab!='xml'">
            <xsl:call-template name="geoCssHeader"/>
            <xsl:call-template name="ext-ux-css"/>
        </xsl:if>
    </xsl:template>

    <!--
     additional scripts
     -->
    <xsl:template mode="script" match="/">
        <script type="text/javascript" src="{/root/gui/url}/scripts/core/kernel/kernel.js"/>
        <xsl:call-template name="geoHeader"/>
        <xsl:call-template name="jsHeader">
            <xsl:with-param name="small" select="false()"/>
        </xsl:call-template>

        <xsl:choose>
            <xsl:when test="/root/request/debug">
                <script type="text/javascript" src="{/root/gui/url}/scripts/editor/metadata-show.js"></script>
                <script type="text/javascript" src="{/root/gui/url}/scripts/editor/metadata-editor.js"></script>
                <script type="text/javascript" src="{/root/gui/url}/scripts/editor/simpletooltip.js"></script>
            </xsl:when>
            <xsl:otherwise>
                <script type="text/javascript" src="{/root/gui/url}/scripts/lib/gn.editor.js"></script>
            </xsl:otherwise>
        </xsl:choose>

        <script type="text/javascript">

        </script>
    </xsl:template>

    <!--
     page content
     -->
    <xsl:template name="content">
        <xsl:param name="schema">
            <xsl:apply-templates mode="schema" select="."/>
        </xsl:param>

        <xsl:if test="/root/response/source//geonet:info/id != /root/response/target//geonet:info/id">
            <div id="switch-div" style="text-align:center;cursor:pointer;" onclick="load('{/root/gui/locService}/metadata.diff?first={/root/response/target//geonet:info/id}&amp;second={/root/response/source//geonet:info/id}');">
                <img src="{/root/gui/url}/images/switch.png" title="{/root/gui/strings/switchdiff}" alt="{/root/gui/strings/switchdiff}" style="vertical-align:middle;"/>
                <span><xsl:value-of select="/root/gui/strings/switchdiff"/></span>
            </div>
        </xsl:if>

        <table  width="100%" height="100%">
            <!-- <xsl:for-each select="/root/*[name(.)!='gui' and name(.)!='request']"> < ! - - just one -->

            <tr height="100%">

                <!-- source -->
                <xsl:for-each select="/root/response/source/*">

                    <!-- left menu
                         <td class="blue-content" width="150" valign="top">
                             <xsl:call-template name="tab">
                                 <xsl:with-param name="tabLink" select="concat(/root/gui/locService,'/metadata.show')"/>
                             </xsl:call-template>
                         </td> -->
                    <td class="content" valign="top">

                        <div id="source-container" style="position:relative;overflow:auto;heigth:800px;max-height:800px;">

                            <xsl:variable name="md">
                                <xsl:apply-templates mode="brief" select="."/>
                            </xsl:variable>
                            <xsl:variable name="metadata" select="exslt:node-set($md)/*[1]"/>
                            <xsl:variable name="mdURL" select="normalize-space(concat($baseURL, '?uuid=', geonet:info/uuid))"/>

                            <xsl:call-template name="socialBookmarks">
                                <xsl:with-param name="baseURL" select="$baseURL" /> <!-- The base URL of the local GeoNetwork site -->
                                <xsl:with-param name="mdURL" select="$mdURL" /> <!-- The URL of the metadata using the UUID -->
                                <xsl:with-param name="title" select="$metadata/title" />
                                <xsl:with-param name="abstract" select="$metadata/abstract" />
                            </xsl:call-template>

                            <table width="100%">

                                <tr>
                                    <td valign="top" height="100%" align="center" class="padded-content">
                                        <div style="font-size:x-large;">
                                            <xsl:choose>
                                                <xsl:when test="(string(geonet:info/workspace)='true')">
                                                    <xsl:value-of select="/root/gui/strings/workspaceview"/>
                                                </xsl:when>
                                                <xsl:otherwise>
                                                    <xsl:value-of select="/root/gui/strings/metadataview"/>
                                                </xsl:otherwise>
                                            </xsl:choose>
                                        </div>
                                    </td>
                                </tr>

                                <xsl:variable name="buttons">
                                    <tr><td class="padded-content" height="100%" align="center" valign="top">
                                        <xsl:call-template name="buttons">
                                            <xsl:with-param name="metadata" select="$metadata"/>
                                        </xsl:call-template>
                                    </td></tr>
                                </xsl:variable>
                                <xsl:if test="$buttons!=''">
                                    <xsl:copy-of select="$buttons"/>
                                </xsl:if>
                                <tr>
                                    <td align="center" valign="left" class="padded-content">
                                        <table width="100%">
                                            <tr>
                                                <td align="left" valign="middle" class="padded-content" height="40">
                                                    <xsl:variable name="source" select="string(geonet:info/source)"/>
                                                    <xsl:choose>
                                                        <!-- //FIXME does not point to baseURL yet-->
                                                        <xsl:when test="/root/gui/sources/record[string(siteid)=$source]">
                                                            <a href="{/root/gui/sources/record[string(siteid)=$source]/baseURL}" target="_blank">
                                                                <img src="{/root/gui/url}/images/logos/{$source}.gif" width="40"/>
                                                            </a>
                                                        </xsl:when>
                                                        <xsl:otherwise>
                                                            <img src="{/root/gui/url}/images/logos/{$source}.gif" width="40"/>
                                                        </xsl:otherwise>
                                                    </xsl:choose>
                                                </td>
                                                <td class="padded" width="90%">
                                                    <h1 align="left">
                                                        <xsl:value-of select="$metadata/title"/>
                                                    </h1>
                                                </td>

                                                <!-- Export links (XML, PDF, ...) -->
                                                <xsl:if test="(string(geonet:info/isTemplate)!='s')">
                                                    <td align="right" class="padded-content" height="16" nowrap="nowrap">
                                                        <xsl:call-template name="showMetadataExportIcons"/>
                                                    </td>
                                                </xsl:if>

                                            </tr>
                                        </table>
                                    </td>
                                </tr>
                                <!-- subtemplate title button -->
                                <xsl:if test="(string(geonet:info/isTemplate)='s')">
                                    <tr><td class="padded-content" height="100%" align="center" valign="top">
                                        <b><xsl:value-of select="geonet:info/title"/></b>
                                    </td></tr>
                                </xsl:if>

                                <tr><td class="padded-content">
                                    <table class="md" width="100%">
                                        <form name="mainForm" accept-charset="UTF-8" method="POST" action="{/root/gui/locService}/metadata.edit">
                                            <input type="hidden" name="id" value="{geonet:info/id}"/>
                                            <input type="hidden" name="currTab" value="{/root/gui/currTab}"/>

                                            <xsl:choose>
                                                <xsl:when test="$currTab='xml'">
                                                    <xsl:apply-templates mode="xmlDocument" select="."/>
                                                </xsl:when>
                                                <xsl:otherwise>
                                                    <xsl:apply-templates mode="elementEP" select="."/>
                                                </xsl:otherwise>
                                            </xsl:choose>

                                        </form>
                                    </table>
                                </td></tr>

                                <xsl:if test="$buttons!=''">
                                    <xsl:copy-of select="$buttons"/>
                                </xsl:if>

                            </table>

                        </div>

                    </td>

                </xsl:for-each>

                <!-- target -->
                <xsl:for-each select="/root/response/target/*">

                    <td class="content" valign="top">

                        <div id="target-container" style="position:relative;margin-left:5px;overflow:auto;heigth:800px;max-height:800px;">

                            <xsl:variable name="md">
                                <xsl:apply-templates mode="brief" select="."/>
                            </xsl:variable>
                            <xsl:variable name="metadata" select="exslt:node-set($md)/*[1]"/>
                            <xsl:variable name="mdURL" select="normalize-space(concat($baseURL, '?uuid=', geonet:info/uuid))"/>

                            <xsl:call-template name="socialBookmarks">
                                <xsl:with-param name="baseURL" select="$baseURL" /> <!-- The base URL of the local GeoNetwork site -->
                                <xsl:with-param name="mdURL" select="$mdURL" /> <!-- The URL of the metadata using the UUID -->
                                <xsl:with-param name="title" select="$metadata/title" />
                                <xsl:with-param name="abstract" select="$metadata/abstract" />
                            </xsl:call-template>

                            <table width="100%">

                                <xsl:if test="(string(geonet:info/workspace)='true')">
                                    <tr>
                                        <td valign="top" height="100%" align="center" class="padded-content">
                                            <div style="font-size:x-large;">
                                                <xsl:value-of select="/root/gui/strings/workspaceview"/>
                                            </div>
                                        </td>
                                    </tr>
                                </xsl:if>

                                <xsl:variable name="buttons">
                                    <tr><td class="padded-content" height="100%" align="center" valign="top">
                                        <xsl:call-template name="buttons">
                                            <xsl:with-param name="metadata" select="$metadata"/>
                                        </xsl:call-template>
                                    </td></tr>
                                </xsl:variable>
                                <xsl:if test="$buttons!=''">
                                    <xsl:copy-of select="$buttons"/>
                                </xsl:if>
                                <tr>
                                    <td align="center" valign="left" class="padded-content">
                                        <table width="100%">
                                            <tr>
                                                <td align="left" valign="middle" class="padded-content" height="40">
                                                    <xsl:variable name="source" select="string(geonet:info/source)"/>
                                                    <xsl:choose>
                                                        <!-- //FIXME does not point to baseURL yet-->
                                                        <xsl:when test="/root/gui/sources/record[string(siteid)=$source]">
                                                            <a href="{/root/gui/sources/record[string(siteid)=$source]/baseURL}" target="_blank">
                                                                <img src="{/root/gui/url}/images/logos/{$source}.gif" width="40"/>
                                                            </a>
                                                        </xsl:when>
                                                        <xsl:otherwise>
                                                            <img src="{/root/gui/url}/images/logos/{$source}.gif" width="40"/>
                                                        </xsl:otherwise>
                                                    </xsl:choose>
                                                </td>
                                                <td class="padded" width="90%">
                                                    <h1 align="left">
                                                        <xsl:value-of select="$metadata/title"/>
                                                    </h1>
                                                </td>

                                                <!-- Export links (XML, PDF, ...) -->
                                                <xsl:if test="(string(geonet:info/isTemplate)!='s')">
                                                    <td align="right" class="padded-content" height="16" nowrap="nowrap">
                                                        <xsl:call-template name="showMetadataExportIcons"/>
                                                    </td>
                                                </xsl:if>

                                            </tr>
                                        </table>
                                    </td>
                                </tr>
                                <!-- subtemplate title button -->
                                <xsl:if test="(string(geonet:info/isTemplate)='s')">
                                    <tr><td class="padded-content" height="100%" align="center" valign="top">
                                        <b><xsl:value-of select="geonet:info/title"/></b>
                                    </td></tr>
                                </xsl:if>

                                <tr><td class="padded-content">
                                    <table class="md" width="100%">
                                        <form name="mainForm" accept-charset="UTF-8" method="POST" action="{/root/gui/locService}/metadata.edit">
                                            <input type="hidden" name="id" value="{geonet:info/id}"/>
                                            <input type="hidden" name="currTab" value="{/root/gui/currTab}"/>

                                            <xsl:choose>
                                                <xsl:when test="$currTab='xml'">
                                                    <xsl:apply-templates mode="xmlDocument" select="."/>
                                                </xsl:when>
                                                <xsl:otherwise>
                                                    <xsl:apply-templates mode="elementEP" select="."/>
                                                </xsl:otherwise>
                                            </xsl:choose>

                                        </form>
                                    </table>
                                </td></tr>

                                <xsl:if test="$buttons!=''">
                                    <xsl:copy-of select="$buttons"/>
                                </xsl:if>

                            </table>

                        </div>

                    </td>

                </xsl:for-each>

            </tr>

            <tr><td class="blue-content" colspan="3"/></tr>
        </table>
    </xsl:template>


</xsl:stylesheet>