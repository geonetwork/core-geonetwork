<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:java="java:org.fao.geonet.util.XslUtil"
                exclude-result-prefixes="#all">

    <xsl:include href="main.xsl"/>

    <!-- Use the link parameter to display a custom hyperlink instead of
    a default GeoNetwork Jeeves service URL. -->
    <xsl:template name="addrow">
        <xsl:param name="service"/>
        <xsl:param name="link"/>
        <xsl:param name="args" select="''"/>
        <xsl:param name="displayLink" select="true()"/>
        <xsl:param name="title"/>
        <xsl:param name="desc"/>
        <xsl:param name="icon"/>
        <xsl:param name="content"/>

        <xsl:variable name="modalArg">
            <xsl:choose>
                <xsl:when test="/root/request/modal">
                    <xsl:text>&amp;modal</xsl:text>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:text/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

        <xsl:if test="java:isAccessibleService($service)">
            <xsl:variable name="url">
                <xsl:choose>
                    <xsl:when test="normalize-space($link)!=''">
                        <xsl:value-of select="$link"/>
                    </xsl:when>
                    <xsl:when test="normalize-space($args)='' and normalize-space($modalArg)=''">
                        <xsl:value-of select="concat(/root/gui/locService,'/',$service)"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of
                                select="concat(/root/gui/locService,'/',$service,'?',$args,$modalArg)"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:variable>

            <tr><td class="configOption">
                <xsl:if test="normalize-space($icon)">
                    <img src="../../images/{$icon}" alt="{$desc}" class="configOption"/>
                </xsl:if>
            </td>
                <td class="padded">
                    <xsl:choose>
                        <xsl:when test="not($displayLink)">
                            <xsl:value-of select="$title"/>
                        </xsl:when>
                        <xsl:when test="/root/request/modal">
                            <a onclick="popAdminWindow('{$url}');" href="javascript:void(0);">
                                <xsl:value-of select="$title"/>
                            </a>
                        </xsl:when>
                        <xsl:otherwise>
                            <a href="{$url}">
                                <xsl:value-of select="$title"/>
                            </a>
                        </xsl:otherwise>
                    </xsl:choose>
                </td>
                <td class="padded">
                    <xsl:value-of select="$desc"/>
                    <xsl:if test="normalize-space($content)">
                        <xsl:copy-of select="$content"/>
                    </xsl:if>
                </td>
            </tr>
        </xsl:if>
    </xsl:template>


    <xsl:template name="addTitle">
        <xsl:param name="icon"/>
        <xsl:param name="title"/>
        <xsl:param name="content"/>

        <xsl:if test="normalize-space($content)">
            <tr>
                <td colspan="3" class="configTitle"><img src="../../images/{$icon}" class="configTitle"/>&#160;<b><xsl:value-of
                        select="$title"/></b></td>
            </tr>
            <xsl:copy-of select="$content"/>
            <tr>
                <td class="spacer"/>
            </tr>
        </xsl:if>
    </xsl:template>

    <!--
    page content
    -->
    <xsl:template name="content">
        <xsl:call-template name="formLayout">
            <xsl:with-param name="title" select="/root/gui/strings/admin"/>
            <xsl:with-param name="content">

                <xsl:variable name="readonly" select="/root/gui/env/readonly = 'true'"/>

                <table width="100%" class="text-aligned-left">

                    <!-- metadata services -->
                    <xsl:variable name="mdServices">

                        <xsl:if test="not($readonly)">
                            <xsl:call-template name="addrow">
                                <xsl:with-param name="service" select="'metadata.create.form'"/>
                                <xsl:with-param name="link">
                                    <!-- When client application is the widget redirect to that app
                                    FIXME : hl parameter is only available for GUI widget experimental client.
                                    -->
                                    <xsl:if test="/root/gui/config/client/@widget='true'"><xsl:value-of select="concat(/root/gui/config/client/@url, '?hl=', /root/gui/language, /root/gui/config/client/@createParameter)"/></xsl:if>
                                </xsl:with-param>
                                <xsl:with-param name="title" select="/root/gui/strings/newMetadata"/>
                                <xsl:with-param name="desc" select="/root/gui/strings/newMdDes"/>
                                <xsl:with-param name="icon">page_add.png</xsl:with-param>
                            </xsl:call-template>
                        </xsl:if>

                        <xsl:call-template name="addrow">
                            <xsl:with-param name="service" select="'metadata.searchunused.form'"/>
                            <xsl:with-param name="title"
                                            select="/root/gui/strings/searchUnusedTitle"/>
                            <xsl:with-param name="desc" select="/root/gui/strings/searchUnused"/>
                        </xsl:call-template>

                        <xsl:choose>
                            <xsl:when test="/root/gui/config/client/@widget='true' and /root/gui/config/client/@stateId!=''">

                                <xsl:call-template name="addrow">
                                    <xsl:with-param name="service" select="'metadata.create.form'"/>
                                    <xsl:with-param name="displayLink" select="false()"/>
                                    <xsl:with-param name="title" select="/root/gui/strings/quickSearch"/>
                                    <xsl:with-param name="content">
                                        <ul>
                                            <li>
                                                <a href="{concat(/root/gui/config/client/@url, '?hl=', /root/gui/language, '&amp;s_search&amp;', /root/gui/config/client/@stateId, '_E__owner=', /root/gui/session/userId)}">
                                                    <xsl:value-of select="/root/gui/strings/mymetadata"/>
                                                </a>
                                            </li>
                                            <li>
                                                <a href="{concat(/root/gui/config/client/@url, '?hl=', /root/gui/language, '&amp;s_search&amp;', /root/gui/config/client/@stateId, '_E_siteId=', /root/gui/env/site/siteId)}">
                                                    <xsl:value-of select="/root/gui/strings/catalogueRecords"/>
                                                </a>
                                            </li>
                                            <li>
                                                <a href="{concat(/root/gui/config/client/@url, '?hl=', /root/gui/language, '&amp;s_search&amp;', /root/gui/config/client/@stateId, '_E__isHarvested=y')}">
                                                    <xsl:value-of select="/root/gui/strings/harvestedRecords"/>
                                                </a>
                                            </li>
                                            <li>
                                                <a href="{concat(/root/gui/config/client/@url, '?hl=', /root/gui/language, '&amp;s_search&amp;', /root/gui/config/client/@stateId, '_E_template=y')}">
                                                    <xsl:value-of select="/root/gui/strings/catalogueTemplates"/>
                                                </a>
                                            </li>
                                        </ul>


                                    </xsl:with-param>
                                </xsl:call-template>

                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:call-template name="addrow">
                                    <xsl:with-param name="service" select="'main.search'"/>
                                    <xsl:with-param name="args" select="'hitsPerPage=10&amp;editable=true'"/>

                                    <xsl:with-param name="title"
                                                    select="/root/gui/strings/mymetadata"/>
                                    <xsl:with-param name="desc" select="/root/gui/strings/mymetadata"/>
                                </xsl:call-template>
                            </xsl:otherwise>
                        </xsl:choose>


                        <xsl:if test="not($readonly)">
                            <xsl:call-template name="addrow">
                                <xsl:with-param name="service" select="'transfer.ownership'"/>
                                <xsl:with-param name="title"
                                                select="/root/gui/strings/transferOwnership"/>
                                <xsl:with-param name="desc"
                                                select="/root/gui/strings/transferOwnershipDes"/>
                            </xsl:call-template>
                        </xsl:if>

                        <tr>
                            <td class="spacer"/>
                        </tr>

                        <xsl:if test="not($readonly)">
                            <xsl:call-template name="addrow">
                                <xsl:with-param name="service" select="'metadata.schema.add.form'"/>
                                <xsl:with-param name="title" select="/root/gui/strings/addSchema"/>
                                <xsl:with-param name="desc" select="/root/gui/strings/addSchemaDes"/>
                                <xsl:with-param name="icon">folder_add.png</xsl:with-param>
                            </xsl:call-template>
                            <xsl:if test="count(/root/gui/schemalist/name[@plugin='true'])>0">
                                <xsl:call-template name="addrow">
                                    <xsl:with-param name="service" select="'metadata.schema.update.form'"/>
                                    <xsl:with-param name="title" select="/root/gui/strings/updateSchema"/>
                                    <xsl:with-param name="desc" select="/root/gui/strings/updateSchemaDes"/>
                                </xsl:call-template>

                                <xsl:call-template name="addrow">
                                    <xsl:with-param name="service" select="'metadata.schema.delete.form'"/>
                                    <xsl:with-param name="title" select="/root/gui/strings/deleteSchema"/>
                                    <xsl:with-param name="desc" select="/root/gui/strings/deleteSchemaDes"/>
                                </xsl:call-template>
                            </xsl:if>
                        </xsl:if>
                    </xsl:variable>



                    <!-- Template administration -->
                    <xsl:variable name="mdTemplate">
                        <xsl:call-template name="addrow">
                            <xsl:with-param name="service" select="'metadata.templates.list'"/>
                            <xsl:with-param name="title"
                                            select="/root/gui/strings/metadata-template-order"/>
                            <xsl:with-param name="desc"
                                            select="/root/gui/strings/metadata-template-order-desc"/>
                        </xsl:call-template>
                    </xsl:variable>

                    <!-- Metadata versioning log-->
                    <xsl:variable name="mdVersionLog">
                        <xsl:call-template name="addrow">
                            <xsl:with-param name="service" select="'versioning.log'"/>
                            <xsl:with-param name="title"
                                            select="/root/gui/strings/metadata-versioning-log"/>
                        </xsl:call-template>
                    </xsl:variable>

                    <xsl:call-template name="addTitle">
                        <xsl:with-param name="icon">xml.png</xsl:with-param>
                        <xsl:with-param name="title"
                                        select="concat(/root/gui/strings/metadata, '&#160;&amp;&#160;', /root/gui/strings/template)"/>
                        <xsl:with-param name="content">
                            <xsl:copy-of select="$mdServices"/>
                            <tr>
                                <td class="spacer"/>
                            </tr>
                            <xsl:if test="not($readonly)">
                                <xsl:copy-of select="$mdTemplate"/>
                            </xsl:if>

                            <xsl:if test="not($readonly)">
                                <tr>
                                    <td class="spacer"/>
                                </tr>
                                <xsl:call-template name="addrow">
                                    <xsl:with-param name="service" select="'metadata.templates.add.default'"/>
                                    <xsl:with-param name="displayLink" select="false()"/>
                                    <xsl:with-param name="title" select="/root/gui/strings/metadata-templates-samples-add"/>
                                    <xsl:with-param name="icon">add.png</xsl:with-param>
                                    <xsl:with-param name="content">
                                        <table>
                                            <tr>
                                                <td width="30%">
                                                    <xsl:value-of
                                                            select="/root/gui/strings/selectTemplate"
                                                            /> : <br/>
                                                    <select class="content"
                                                            id="metadata.schemas.select" size="8"
                                                            multiple="true">
                                                        <xsl:for-each select="/root/gui/schemalist/name">
                                                            <xsl:sort select="."/>
                                                            <option value="{string(.)}">
                                                                <xsl:value-of select="string(.)"/>
                                                            </option>
                                                        </xsl:for-each>
                                                    </select>
                                                </td>
                                                <td style="align:center;width:20%;vertical-align:bottom;">
                                                    <div id="addTemplatesSamplesButtons">
                                                        <button class="content"
                                                                onclick="addTemplate('{/root/gui/strings/metadata-schema-select}', '{/root/gui/strings/metadata-template-add-success}');"
                                                                id="tplBtn">
                                                            <xsl:value-of
                                                                    select="/root/gui/strings/metadata-template-add-default"
                                                                    />
                                                        </button>
                                                        <button class="content"
                                                                onclick="addSampleData('{/root/gui/strings/metadata-schema-select}', '{/root/gui/strings/metadata-samples-add-failed}', '{/root/gui/strings/metadata-samples-add-success}');"
                                                                id="tplSamples">
                                                            <xsl:value-of
                                                                    select="/root/gui/strings/metadata-samples-add"/>
                                                        </button>
                                                    </div>
                                                    <img src="{/root/gui/url}/images/loading.gif"
                                                         id="waitLoadingTemplatesSamples" style="display:none;"/>
                                                </td>
                                            </tr>
                                        </table>
                                    </xsl:with-param>
                                </xsl:call-template>
                            </xsl:if>
							<xsl:copy-of select="$mdVersionLog"/>
                        </xsl:with-param>
                    </xsl:call-template>

                    <xsl:variable name="io">

                        <xsl:call-template name="addrow">
                            <xsl:with-param name="service" select="'metadata.xmlinsert.form'"/>
                            <xsl:with-param name="title" select="/root/gui/strings/xmlInsertTitle"/>
                            <xsl:with-param name="desc" select="/root/gui/strings/xmlInsert"/>
                        </xsl:call-template>

                        <xsl:call-template name="addrow">
                            <xsl:with-param name="service" select="'metadata.batchimport.form'"/>
                            <xsl:with-param name="title" select="/root/gui/strings/batchImportTitle"/>
                            <xsl:with-param name="desc" select="/root/gui/strings/batchImport"/>
                        </xsl:call-template>

                        <xsl:call-template name="addrow">
                            <xsl:with-param name="service" select="'notifications.list'"/>
                            <xsl:with-param name="title" select="/root/gui/strings/notifications"/>
                            <xsl:with-param name="desc" select="/root/gui/strings/notificationsDes"
                                    />
                            <xsl:with-param name="icon">bell.png</xsl:with-param>
                        </xsl:call-template>
                    </xsl:variable>

                    <xsl:if test="not($readonly)">
                        <xsl:call-template name="addTitle">
                            <xsl:with-param name="icon">connect.png</xsl:with-param>
                            <xsl:with-param name="title" select="/root/gui/strings/io"/>
                            <xsl:with-param name="content" select="$io"/>
                        </xsl:call-template>
                    </xsl:if>

                    <xsl:variable name="catalogueConfiguration">
                        <xsl:if test="not($readonly)">
                            <xsl:call-template name="addrow">
                                <xsl:with-param name="service" select="'config'"/>
                                <xsl:with-param name="title" select="/root/gui/strings/systemConfig"/>
                                <xsl:with-param name="desc" select="/root/gui/strings/systemConfigDes"/>
                                <xsl:with-param name="icon">exec.png</xsl:with-param>
                            </xsl:call-template>
                        </xsl:if>
                        
                        <xsl:call-template name="addrow">
                            <xsl:with-param name="service" select="'config.info'"/>
                            <xsl:with-param name="title" select="/root/gui/strings/systemInfo"/>
                            <xsl:with-param name="desc" select="/root/gui/strings/systemInfoDes"/>
                        </xsl:call-template>
                        
                        <xsl:if test="not($readonly)">
                            <xsl:call-template name="addrow">
                                <xsl:with-param name="service" select="'csw.config.get'"/>
                                <xsl:with-param name="title" select="/root/gui/strings/cswServer"/>
                                <xsl:with-param name="desc" select="/root/gui/strings/cswServerDes"/>
                            </xsl:call-template>
                        </xsl:if>


                    </xsl:variable>


                    <xsl:call-template name="addTitle">
                        <xsl:with-param name="icon">exec.png</xsl:with-param>
                        <xsl:with-param name="title"
                                        select="/root/gui/strings/catalogueConfiguration"/>
                        <xsl:with-param name="content" select="$catalogueConfiguration"/>
                    </xsl:call-template>


                    <!-- samples and tests services
                    <xsl:variable name="adminServices">
                        <xsl:call-template name="addrow">
                            <xsl:with-param name="service" select="'test.csw'"/>
                            <xsl:with-param name="title" select="/root/gui/strings/cswTest"/>
                            <xsl:with-param name="desc" select="/root/gui/strings/cswTestDesc"/>
                        </xsl:call-template>
                    </xsl:variable>

                    <xsl:call-template name="addTitle">
                        <xsl:with-param name="icon">folder_page.png</xsl:with-param>
                        <xsl:with-param name="title" select="/root/gui/strings/samplesAndTests"/>
                        <xsl:with-param name="content" select="$adminServices"/>
                    </xsl:call-template>
                    </xsl:variable>
                    -->

                    <xsl:variable name="i18n">
                        <xsl:call-template name="addrow">
                            <xsl:with-param name="service" select="'test.i18n'"/>
                            <xsl:with-param name="title" select="/root/gui/strings/i18n"/>
                            <xsl:with-param name="desc" select="/root/gui/strings/i18nDesc"/>
                        </xsl:call-template>
                    </xsl:variable>

                    <xsl:if test="not($readonly)">
                        <xsl:call-template name="addTitle">
                            <xsl:with-param name="icon">comment.png</xsl:with-param>
                            <xsl:with-param name="title" select="/root/gui/strings/localiz"/>
                            <xsl:with-param name="content" select="$i18n"/>
                        </xsl:call-template>
                    </xsl:if>

                </table>
                <p/>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>

</xsl:stylesheet>