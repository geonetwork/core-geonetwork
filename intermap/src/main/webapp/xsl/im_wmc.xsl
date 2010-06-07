<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
        xmlns:wmc="http://www.opengis.net/context">

	<xsl:output method="xml" indent="yes" encoding="UTF-8"/>
	<xsl:strip-space elements="*"/>
	<xsl:preserve-space elements="xsl:text"/>

	<!--xsl:include href="prettyprint.xsl"/-->

	<xsl:template match="/" >
		<xsl:copy-of  select="//wmc:ViewContext"/> <!--  will use prettyprint templates -->
	</xsl:template>

</xsl:stylesheet>