<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns    ="http://www.isotc211.org/2005/gmd"
										xmlns:gco="http://www.isotc211.org/2005/gco"
										xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
										xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
										>

	<!-- ============================================================================= -->

	<xsl:template name="RefSystemTypes">
		<xsl:param name="srs"/>
		<referenceSystemIdentifier>
			<RS_Identifier>
				<code>
                    <gco:CharacterString><xsl:value-of select="$srs"/></gco:CharacterString>
           		</code>
			</RS_Identifier>
		</referenceSystemIdentifier>	
	</xsl:template>



	<!-- ============================================================================= -->

</xsl:stylesheet>
