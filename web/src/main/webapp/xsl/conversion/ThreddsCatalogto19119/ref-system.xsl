<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:gmd="http://www.isotc211.org/2005/gmd"
										xmlns:gco="http://www.isotc211.org/2005/gco"
										xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
										xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
										>

	<!-- ============================================================================= -->

	<xsl:template match="*" mode="RefSystemTypes">

		<gmd:referenceSystemIdentifier>
			<gmd:RS_Identifier>
				<gmd:code>
					<gco:CharacterString>4326</gco:CharacterString>
				</gmd:code>
			</gmd:RS_Identifier>
		</gmd:referenceSystemIdentifier>
	
	</xsl:template>

	<!-- ============================================================================= -->

</xsl:stylesheet>
