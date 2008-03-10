<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method='html' version='1.0' encoding='UTF-8' indent='yes'/>

<xsl:include href="thesaurus-util.xsl"/>


<xsl:template match="/">
	<xsl:variable name="mode" select="/root/request/pMode"/>

	<table width="100%"><tr><td>
		<xsl:variable name="nb" select="count(/root/response/descKeys/child::*)"/>
		<xsl:choose>
			<xsl:when test="/root/request/nbResults = $nb">
				<xsl:value-of select="/root/gui/strings/thesaurus/foundKeyWordsLimit"/> (<xsl:value-of select="/root/gui/strings/thesaurus/in"/>&#160;<xsl:value-of select="/root/gui/strings/child::*[name() = /root/gui/language]"/>):
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="/root/gui/strings/thesaurus/foundKeyWords"/> (<xsl:value-of select="/root/gui/strings/thesaurus/in"/>&#160;<xsl:value-of select="/root/gui/strings/child::*[name() = /root/gui/language]"/>):
			</xsl:otherwise>
		</xsl:choose>
		<xsl:value-of select="$nb"/>
	</td><td>
	</td></tr></table>


	<xsl:if test="count(/root/response/descKeys/child::*) != '0'">
	
	<table align="center" border="0" width="80%">
		<colgroup>
			<col width="5%"/>
			<col width="30%"/>
			<col width="40%"/>
			<col width="20%"/>
		</colgroup>
		<tr><td colspan="4">
				<xsl:value-of select="/root/gui/strings/label"/>
		</td></tr>
		<tr><td colspan="4">
			<div class="keywordResults">
			<ul id="keywordResults" name="keywordResults">
			<xsl:for-each select="/root/response/descKeys/keyword">
				<xsl:call-template name="keywordRow">
					<xsl:with-param name="keyword" select="."/>
					<xsl:with-param name="mode" select="$mode"/>
					<xsl:with-param name="thesaurus" select="/root/request/pThesauri"/>
					<xsl:with-param name="url" select="/root/gui/url"/>
				</xsl:call-template>
			</xsl:for-each>
			</ul>
			</div>
		</td></tr>
		<xsl:if test="$mode='edit'">	
		<tr><td colspan="2">
			<button id="del" name="del" onclick="javascript:removeKeyword();" class="content" disabled="disabled">
				<xsl:value-of select="/root/gui/strings/delete"/>
			</button>
			&#160;
			<xsl:call-template name="buttonAdd">
				<xsl:with-param name="mode" select="$mode"/>
				<xsl:with-param name="thesaurus" select="/root/request/pThesauri"/>
			</xsl:call-template>
			</td>
			<td></td><td></td>
		</tr>
		</xsl:if>
		</table>
	</xsl:if>
</xsl:template>

	
</xsl:stylesheet>