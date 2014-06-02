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


                </table>
                <p/>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>

</xsl:stylesheet>