<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
							  xmlns:math="http://exslt.org/math">
    <xsl:output method="html"/>

	<!-- A template to add a new line \n with no extra space. -->	
	<xsl:template name="newLine">
<xsl:text>
</xsl:text>		
	</xsl:template>

    <xsl:template match="/">
    <!--  generates the text for tagcloud, after having normalized the tag weights -->
    <xsl:variable name="maxCount" select="math:max(root/gui/tagcloud/record/tagcount)"/>
    <xsl:variable name="limit" select="root/gui/tagcloud/limit"/>
    <xsl:for-each select="root/gui/tagcloud/record[position() &lt; $limit]">
        <xsl:variable name="weight" select="round((tagcount div $maxCount) * 100)"/>
        <xsl:value-of select="$weight"/>,<xsl:value-of select="termtext"/>;
    </xsl:for-each>
    </xsl:template>
</xsl:stylesheet>
