<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:gmd="http://www.isotc211.org/2005/gmd"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:output method="xml" />
	<xsl:template match="/">
		<xsl:apply-templates select="root/crsList/crs"/>
	</xsl:template>

	<xsl:template match="crs">
		<!-- Return only one element in iso19139 format -->
		<gmd:MD_ReferenceSystem>
			<gmd:referenceSystemIdentifier>
				<gmd:RS_Identifier>
					<gmd:code>
						<gco:CharacterString>
							<xsl:value-of select="description" />
						</gco:CharacterString>
					</gmd:code>
					<gmd:codeSpace>
						<gco:CharacterString>
							<xsl:value-of select="codeSpace" />
						</gco:CharacterString>
					</gmd:codeSpace>
					<gmd:version>
						<gco:CharacterString>
							<xsl:value-of select="version" />
						</gco:CharacterString>
					</gmd:version>
				</gmd:RS_Identifier>
			</gmd:referenceSystemIdentifier>
		</gmd:MD_ReferenceSystem>
	</xsl:template>
</xsl:stylesheet>
