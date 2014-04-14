<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:include href="../main.xsl"/>
    <xsl:template mode="script" match="/">
        <script type="text/javascript" src="{/root/gui/url}/static/kernel.js"/>
        <script type="text/javascript" src="{/root/gui/url}/scripts/core/gui/gui.js"/>
        <link rel="stylesheet" type="text/css" href="{/root/gui/url}/scripts/ext/resources/css/ext-all.css" />
        <script type="text/javascript" src="{/root/gui/url}/scripts/ext/adapter/ext/ext-base.js"></script>
        <script type="text/javascript" src="{/root/gui/url}/scripts/ext/ext-all.js"></script>
        <script type="text/javascript" src="{/root/gui/url}/scripts/versionlog.js"></script>
    </xsl:template>
    <xsl:template name="content">
        <xsl:call-template name="formLayout">
            <xsl:with-param name="title" select="/root/gui/strings/metadata-versioning-log"/>

            <xsl:with-param name="content">
                <xsl:call-template name="logsList"/>
            </xsl:with-param>

            <xsl:with-param name="buttons">
                <xsl:call-template name="buttons"/>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template name="logsList">

        <div id="topic-grid"></div>

    </xsl:template>
    <xsl:template name="buttons">
        <button class="content" onclick="load('{/root/gui/locService}/admin')">
            <xsl:value-of select="/root/gui/strings/back"/>
        </button>
    </xsl:template>
</xsl:stylesheet>
