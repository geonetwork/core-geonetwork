<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:template match="Metadata">
		<thumbnail>
			<xsl:for-each select="dataIdInfo/graphOver">
				<xsl:choose>
					<xsl:when test="bgFileDesc = 'large_thumbnail' and bgFileName != ''">
						<large>
							<xsl:value-of select="bgFileName" />
						</large>
					</xsl:when>
					<xsl:when test="bgFileDesc = 'thumbnail' and bgFileName != ''">
						<small>
							<xsl:value-of select="bgFileName" />
						</small>
					</xsl:when>
				</xsl:choose>
			</xsl:for-each>
		</thumbnail>
	</xsl:template>

</xsl:stylesheet>
