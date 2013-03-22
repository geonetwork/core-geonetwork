<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method='html' encoding='UTF-8' indent='yes'/>

<xsl:template match="/">
	<ul>
		<xsl:for-each select="/root/response/descKeys/keyword">
		  <li>
		    <xsl:value-of select="value"/>
		  </li>
		</xsl:for-each>
	</ul>
</xsl:template>

</xsl:stylesheet>