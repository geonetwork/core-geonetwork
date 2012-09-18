<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:geonet="http://www.fao.org/geonetwork"
                xmlns:exslt="http://exslt.org/common"
                exclude-result-prefixes="exslt geonet"
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

        <script type="text/javascript" src="{/root/gui/url}/scripts/ext/locale/ext-lang-{/root/gui/language}.js"/>
        <script type="text/javascript" src="{/root/gui/url}/scripts/core/kernel/kernel.js"/>
        <script type="text/javascript" src="{/root/gui/url}/scripts/webtoolkit.aim.js"/>
        <script type="text/javascript">
            <xsl:if test="/root/gui/position!='-1'">
                function scrollIt()
                {
                window.scroll(0,<xsl:value-of select="/root/gui/position"/>);
                document.mainForm.position.value = -1; // reset
                }
                timeId = setTimeout('scrollIt()',1000);
            </xsl:if>
            function removeJustCreated() {
            var justCreated = $('just-created');
            if(justCreated != null) {
            justCreated.parentNode.removeChild(justCreated);
            }
            }
        </script>

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

        <table  width="100%" height="100%">
            <!-- <xsl:for-each select="/root/*[name(.)!='gui' and name(.)!='request']"> < ! - - just one -->
            <tr height="100%">

                <!-- source (just 1) -->
                <xsl:for-each select="/root/response/source/*">

                    <td class="content" valign="top" width="600">

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

                <!-- target (just 1) -->
                <xsl:for-each select="/root/response/target/*">

                    <td class="content" valign="top" width="600">

                        <div id="target-container" style="position:relative;margin-left:5px;overflow:auto;heigth:800px;max-height:800px;max-width:800px;">

                            <xsl:variable name="md">
                                <!--<xsl:apply-templates mode="brief" select="."/>-->
                                <xsl:apply-templates mode="elementEP" select=".">
                                    <xsl:with-param name="edit" select="true()"/>
                                </xsl:apply-templates>
                            </xsl:variable>
                            <xsl:variable name="metadata" select="exslt:node-set($md)/*[1]"/>
                            <xsl:variable name="mdURL" select="normalize-space(concat($baseURL, '?uuid=', geonet:info/uuid))"/>

                            <xsl:call-template name="socialBookmarks">
                                <xsl:with-param name="baseURL" select="$baseURL" /> <!-- The base URL of the local GeoNetwork site -->
                                <xsl:with-param name="mdURL" select="$mdURL" /> <!-- The URL of the metadata using the UUID -->
                                <xsl:with-param name="title" select="$metadata/title" />
                                <xsl:with-param name="abstract" select="$metadata/abstract" />
                            </xsl:call-template>

                            <table width="100%" style="max-width:800px;">

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
                                        <table style="max-width:800px;">
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
                                    <table class="md" style="max-width:800px;">
                                        <img id="editorBusy" src="{/root/gui/url}/images/spinner.gif" alt="busy" style="display:none"/>
                                        <table id="editFormTable" style="max-width:800px;">
                                            <tr>
                                                <td class="content" valign="top">
                                                    <form id="editForm" name="mainForm" accept-charset="UTF-8" method="POST" action="{/root/gui/locService}/metadata.update">
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
                                                        <div id="hiddenFormElements" style="display:none;"/>

                                                        <table style="max-width:800px;">
                                                            <tr><td class="padded-content" height="100%" align="center" valign="top">
                                                                <xsl:call-template name="editButtons"/>
                                                            </td></tr>
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
                                                            <tr><td class="padded-content" height="100%" align="center" valign="top">
                                                                <xsl:call-template name="editButtons">
                                                                    <xsl:with-param name="top" select="false()"/>
                                                                </xsl:call-template>
                                                            </td></tr>
                                                        </table>
                                                    </form>

                                                    <div id="validationReport" class="content" style="display:none;"/>
                                                    <div id="shortcutHelp" class="content" style="display:none;">
                                                        <xsl:copy-of select="/root/gui/strings/helpShortcutsEditor"/>
                                                    </div>

                                                </td>
                                            </tr>
                                        </table>
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
        </table>
    </xsl:template>




    <xsl:template name="editButtons">
        <xsl:param name="top" select="true()"/>

        <!-- reset button -->
        <button class="content" id="btnReset" onclick="doSaveAction('metadata.update.forget')" type="button"><xsl:value-of select="/root/gui/strings/reset"/></button>

        <!-- save button -->
        &#160;
        <button class="content" id="btnSave" onclick="removeJustCreated();doSaveAction('metadata.update')" type="button">
            <xsl:value-of select="/root/gui/strings/save"/>
        </button>

        <!-- save and close button -->
        &#160;
        <button class="content" id="btnSaveAndClose" onclick="removeJustCreated();doSaveAction('metadata.update.finish')" type="button">
            <xsl:value-of select="/root/gui/strings/saveAndClose"/>
        </button>

        <!-- save and validate button -->
        &#160;
        <button class="content" id="btnValidate" onclick="removeJustCreated();doSaveAction('metadata.update','metadata.validate');return false;" type="button">
            <xsl:value-of select="/root/gui/strings/saveAndValidate"/>
        </button>

        &#160;
        <!-- =========================  -->
        <!-- Add other actions list     -->
        <a id="oAc{geonet:info/id}{$top}" name="oAc{geonet:info/id}{$top}" class="button content" onclick="oActions('oAc','{geonet:info/id}{$top}');" title="{/root/gui/strings/otherActions}">
            <img id="oAcImg{geonet:info/id}{$top}" name="oAcImg{geonet:info/id}{$top}" src="{/root/gui/url}/images/plus.gif" style="padding-right:3px;"/>
            <xsl:value-of select="/root/gui/strings/otherActions"/>
        </a>
        <div id="oAcEle{geonet:info/id}{$top}" class="oAcEle" style="display:none;width:350px" onClick="oActions('oAc','{geonet:info/id}{$top}');">

            <!-- thumbnails -->
            <xsl:if test="string(geonet:info/schema)='fgdc-std' or string(geonet:info/schema)='iso19115' or starts-with(string(geonet:info/schema),'iso19139')"> <!-- FIXME: should be more general -->
                <button class="content" id="btnThumbnails" onclick="doAction('{/root/gui/locService}/metadata.thumbnail.form')" type="button">
                    <img class="icon" src="../../images/photo.png"/><xsl:value-of select="/root/gui/strings/thumbnails"/>
                </button>
            </xsl:if>

            <xsl:if test="starts-with(string(geonet:info/schema),'iso19139') and /root/gui/config/editor-actions/compute-extent-from-keyword">
                <!-- TODO : check that keywords are available in current metadata -->
                <button class="content" id="btnComputeExtent" onclick="computeExtentFromKeywords(1);" type="button"
                        alt="{/root/gui/strings/computeExtentFromKeywordsHelp}"
                        title="{/root/gui/strings/computeExtentFromKeywordsHelp}">
                    <img class="icon" src="../../images/extent.png"/><xsl:value-of select="/root/gui/strings/computeExtentFromKeywordsReplace"/>
                </button>
                <button class="content" id="btnComputeExtentAddMode" onclick="computeExtentFromKeywords(0);" type="button"
                        title="{/root/gui/strings/computeExtentFromKeywordsHelp}"
                        alt="{/root/gui/strings/computeExtentFromKeywordsHelp}">
                    <img class="icon" src="../../images/extent-add.png"/><xsl:value-of select="/root/gui/strings/computeExtentFromKeywords"/>
                </button>
            </xsl:if>
        </div>

        <!-- cancel button -->
        &#160;
        <xsl:choose>
            <xsl:when test="//JUSTCREATED">
                <button class="content" id="btnCancelCreation" onclick="cancelCreation('{/root/gui/locService}/metadata.quiet.delete?id={geonet:info/id}','{/root/gui/strings/confirmCancelCreate}');" type="button">
                    <xsl:value-of select="/root/gui/strings/cancel"/>
                </button>
            </xsl:when>
            <xsl:otherwise>
                <button class="content" id="btnCancelEdit" onclick="doCancelAction('metadata.update.forgetandfinish','{/root/gui/strings/confirmCancel}',this.id)" type="button">
                    <xsl:value-of select="/root/gui/strings/cancel"/>
                </button>
            </xsl:otherwise>
        </xsl:choose>
        <!-- minor edit button -->
        <input type="checkbox" id="minorEdit" name="minorEdit" onchange="$('minor').value = this.checked">
            <xsl:if test="/root/request/minor='true'">
                <xsl:attribute name="checked">checked</xsl:attribute>
            </xsl:if>
        </input>
        <label for="minorEdit"><xsl:value-of select="/root/gui/strings/minor"/></label>


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
        <!--<xsl:text>&#160;</xsl:text>
                <xsl:value-of select="/root/gui/strings/subtemplateTitle"/>
                <xsl:text>&#160;</xsl:text>
                <input class="content" type="text" name="title" value="{geonet:info/title}"/>
        -->

    </xsl:template>



</xsl:stylesheet>