<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:gnf="http://www.fao.org/geonetwork/functions">

    <!-- Create a label: value widget  -->
    <xsl:function name="gnf:textField">
        <xsl:param name="label"/>
        <xsl:param name="value"/>
        <div>
            <div class="col-xs-3"><xsl:value-of select="$label"/>:</div>
            <div class="offset-xs-3 col-xs-9"><xsl:value-of select="$value"/></div>
        </div>
    </xsl:function>
</xsl:stylesheet>
