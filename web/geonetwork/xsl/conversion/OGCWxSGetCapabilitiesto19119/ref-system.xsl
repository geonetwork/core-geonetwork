<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns    ="http://www.isotc211.org/2005/gmd"
										xmlns:gco="http://www.isotc211.org/2005/gco"
										xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
										xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
										>

	<!-- ============================================================================= -->

	<xsl:template match="*" mode="RefSystemTypes">

		<referenceSystemIdentifier>
			<RS_Identifier>
				<code>
					<xsl:choose>
						<xsl:when test="count(//SRS='EPSG:4326')!=0">
           					<gco:CharacterString>4326</gco:CharacterString>
           				</xsl:when>
           				<xsl:otherwise>
           					<gco:CharacterString><xsl:value-of select="//SRS[0]/."/></gco:CharacterString>
           				</xsl:otherwise>
         			</xsl:choose>
           		</code>
			</RS_Identifier>
		</referenceSystemIdentifier>
	
	</xsl:template>

	<!-- ============================================================================= -->

</xsl:stylesheet>
