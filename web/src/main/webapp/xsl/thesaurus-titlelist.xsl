<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method='html' encoding='UTF-8' indent='yes'/>

<xsl:template match="/">

<xsl:variable name="lcletters">abcdefghijklmnopqrstuvwxyz</xsl:variable>
<xsl:variable name="ucletters">ABCDEFGHIJKLMNOPQRSTUVWXYZ</xsl:variable>

<xsl:variable name="in" select="translate(/root/request/titleCompleter,$ucletters,$lcletters)"/>

		<ul>
			<xsl:for-each select="/root/response/summary/titles/title[starts-with(@name,$in)=1]">
				<li>
					<xsl:value-of select="@name"/>
				</li>
			</xsl:for-each>
		</ul>
</xsl:template>

	
</xsl:stylesheet>
