<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:geonet="http://www.fao.org/geonetwork">

    <xsl:import href="../base-layout.xsl"/>

    <xsl:variable name="oldNode" select="/root/request/oldNodeId"/>
    <xsl:variable name="lang" select="/root/gui/language"/>
    <xsl:variable name="baseUrl" select="/root/gui/url"/>
    <xsl:variable name="oldNodeHomeUrl"><xsl:value-of select="$baseUrl"/>/<xsl:value-of
            select="$oldNode"/>/<xsl:value-of select="$lang"/>/home</xsl:variable>
    <xsl:variable name="redirectedFrom" select="/root/request/redirectedFrom"/>


    <xsl:template mode="content" match="/">
        <h1>
            <xsl:value-of select="$i18n/nodeChangeWarning"/>
        </h1>
        <div class="alert alert-danger" data-ng-controller="GnLoginController">
            <p>
                <strong><xsl:value-of select="$i18n/nodeChangeInfo"/></strong>
            </p>

            <p>
                <xsl:value-of select="$i18n/nodeChangeBack"/>
                <a href="{$oldNodeHomeUrl}" class="btn btn-link">
                    <xsl:value-of select="$oldNodeHomeUrl"/>
                </a>
            </p>
            <p>

                <xsl:value-of select="$i18n/nodeChangeForward"/>
                <a href="" data-ng-click="nodeChangeRedirect('{$redirectedFrom}')" class="btn btn-link">
                    <xsl:value-of select="$redirectedFrom"/>
                </a>
            </p>
        </div>
    </xsl:template>

</xsl:stylesheet>
