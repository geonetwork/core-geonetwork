<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:output method="xml" encoding="UTF-8" indent="yes"/>

	<xsl:template match="/">

		<xsl:variable name="elementName" select="/root/request/element"/>
		<xsl:variable name="schema" select="/root/request/schema"/>
		<xsl:variable name="registeredTheasurus"
			select="if (/root/request/element) then /root/gui/schemalist/*[text()=$schema]/
			associations/registerAssociation[@element = $elementName] else ''"/>
		
		<response>
			<thesauri>
				<!-- Filtering thesaurus list according to element type -->
				<xsl:choose>
					<xsl:when test="$registeredTheasurus">
						
						<xsl:for-each select="$registeredTheasurus">
							<xsl:variable name="registerId"
								select="substring-after(@registerId, 'local://thesaurus/')"/>
							<xsl:copy-of
								select="/root/response/thesauri/thesaurus[key = $registerId]"/>

						</xsl:for-each>
					</xsl:when>
					<xsl:otherwise>
						<xsl:copy-of select="/root/response/thesauri/thesaurus"/>
					</xsl:otherwise>
				</xsl:choose>
			</thesauri>
		</response>
	</xsl:template>
</xsl:stylesheet>
