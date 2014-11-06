<?xml version="1.0" encoding="UTF-8"?>
<!--
  Edit metadata embedded processing to add
  a piece of metadata to the editor form
  -->
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:saxon="http://saxon.sf.net/"
    xmlns:gn="http://www.fao.org/geonetwork"
    xmlns:gn-fn-metadata="http://geonetwork-opensource.org/xsl/functions/metadata"
    extension-element-prefixes="saxon"
    exclude-result-prefixes="#all">

    <xsl:output method="html" encoding="UTF-8" indent="yes"/>

    <xsl:include href="../../common/base-variables-metadata-editor.xsl"/>

    <xsl:include href="../../common/functions-metadata.xsl"/>

    <xsl:include href="../../common/profiles-loader.xsl"/>

    <xsl:include href="../form-builder.xsl"/>


    <xsl:template match="/">
        <xsl:for-each
            select="/root/*[name(.)!='gui' and name(.)!='request']//*[@gn:addedObj = 'true']">
            <!-- Dispatch to profile mode -->
            <xsl:variable name="profileTemplate" select="concat('dispatch-', $schema)"/>
            <saxon:call-template name="{$profileTemplate}">
                <xsl:with-param name="base" select="."/>
            </saxon:call-template>
        </xsl:for-each>
    </xsl:template>

</xsl:stylesheet>
