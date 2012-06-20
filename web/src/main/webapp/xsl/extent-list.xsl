<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink">

	<xsl:include href="extent-util.xsl" />

	<xsl:output method='html' encoding='UTF-8' indent='yes' />

	<xsl:template match="wfs">
		<ul>
			<xsl:for-each select="/root/response/wfs/featureType/feature">
				<xsl:call-template name="featureRow">
					<xsl:with-param name="feature" select="." />
					<xsl:with-param name="mode" select="'search'" />
				</xsl:call-template>
			</xsl:for-each>
		</ul>
	</xsl:template>


	<xsl:template match="text()" />

</xsl:stylesheet>