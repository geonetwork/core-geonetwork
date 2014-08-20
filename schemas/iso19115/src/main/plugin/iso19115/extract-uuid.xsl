<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:template match="Metadata">
		 <uuid><xsl:value-of select="mdFileID"/></uuid>
	</xsl:template>

</xsl:stylesheet>
