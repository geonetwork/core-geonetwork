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