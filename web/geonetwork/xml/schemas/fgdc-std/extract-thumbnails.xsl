<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:template match="metadata">
		<thumbnail>
			<xsl:for-each select="idinfo/browse">
				<xsl:choose>
					<xsl:when test="browset = 'large_thumbnail' and browsen != ''">
						<large>
							<xsl:value-of select="browsen"/>
						</large>
					</xsl:when>
					<xsl:when test="browset = 'thumbnail' and browsen != ''">
						<small>
							<xsl:value-of select="browsen"/>
						</small>
					</xsl:when>
				</xsl:choose>
			</xsl:for-each>
		</thumbnail>
	</xsl:template>

</xsl:stylesheet>
