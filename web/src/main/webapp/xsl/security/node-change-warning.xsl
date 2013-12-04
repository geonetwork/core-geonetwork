<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:geonet="http://www.fao.org/geonetwork">

    <xsl:import href="../main.xsl"/>

    <xsl:variable name="oldNode" select="/root/request/oldNodeId"/>
    <xsl:variable name="lang" select="/root/gui/language"/>
    <xsl:variable name="baseUrl" select="/root/gui/url"/>
    <xsl:variable name="oldNodeHomeUrl"><xsl:value-of select="$baseUrl"/>/<xsl:value-of select="$oldNode"/>/<xsl:value-of select="$lang"/>/home</xsl:variable>

    <xsl:variable name="redirectedFrom" select="/root/request/redirectedFrom"/>
    <xsl:template mode="script" match="/">
        <script type="text/javascript">
            var logoutAndRedirectTo = function() {
                new Ajax.Request('<xsl:value-of select="$baseUrl"/>/j_spring_security_logout',{
                    onSuccess: function() {
                        window.location.href='<xsl:value-of select="$redirectedFrom"/>';
                    }
                });
            }
        </script>
    </xsl:template>

    <!--
    main page
    -->
    <xsl:template name="content">
        <h1>Node Change Warning</h1>
        Warning: You have are no longer in the same node that you logged into.
        <p>
            If you would like to return to the node you logged into click:
            <a href="{$oldNodeHomeUrl}"><xsl:value-of select="$oldNodeHomeUrl"/></a>
        </p><p>

            If you would like to logout and return to the previous url click:
            <a href="{$redirectedFrom}" onclick="logoutAndRedirectTo()"><xsl:value-of select="$redirectedFrom"/></a>
        </p>
    </xsl:template>

</xsl:stylesheet>
