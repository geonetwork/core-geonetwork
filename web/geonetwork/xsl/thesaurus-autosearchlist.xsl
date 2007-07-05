<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method='html' version='1.0' encoding='UTF-8' indent='yes'/>

<xsl:include href="thesaurus-util.xsl"/>

<xsl:template match="/">
	<ul>
		<xsl:for-each select="/root/response/descKeys/keyword">
			<xsl:call-template name="keywordRow">
				<xsl:with-param name="keyword" select="."/>
				<xsl:with-param name="mode" select="/root/request/pMode"/>
				<xsl:with-param name="thesaurus" select="/root/request/thesaurus"/>
				<xsl:with-param name="url" select="/root/gui/url"/>
				<xsl:with-param name="depth" select="/root/request/level"/>
			</xsl:call-template>

		</xsl:for-each>
	</ul>
</xsl:template>

</xsl:stylesheet>