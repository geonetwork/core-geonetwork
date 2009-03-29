<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:exslt= "http://exslt.org/common" 
	xmlns:str="http://exslt.org/strings" 
	xmlns:geonet="http://www.fao.org/geonetwork"
	exclude-result-prefixes="exslt">

	<xsl:template name="getButtons">
		<xsl:param name="addLink"/>
		<xsl:param name="removeLink"/>
		<xsl:param name="upLink"/>
		<xsl:param name="downLink"/>
		<xsl:param name="validationLink"/>
		<xsl:param name="id"/>



		<!-- add button -->
		<span id="buttons_{$id}">
			<xsl:choose>
				<xsl:when test="normalize-space($addLink)">
					<xsl:variable name="linkTokens" select="str:tokenize($addLink,'!')"/>
					<xsl:text> </xsl:text>
					<xsl:choose>
						<xsl:when test="normalize-space($linkTokens[2])">
							<a id="add_{$id}" style="display:none;cursor:hand;cursor:pointer;"  onclick="if (noDoubleClick()) {$linkTokens[1]}" target="_blank"><img src="{/root/gui/url}/images/plus.gif" alt="{/root/gui/strings/add}"/></a>
						</xsl:when>
						<xsl:otherwise>
							<a id="add_{$id}" style="cursor:hand;cursor:pointer;" onclick="if (noDoubleClick()) {$addLink}" target="_blank"><img src="{/root/gui/url}/images/plus.gif" alt="{/root/gui/strings/add}"/></a>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:when>
				<xsl:otherwise>
					<span id="add_{$id}"/>
				</xsl:otherwise>
			</xsl:choose>
				
			<!-- remove button -->
			<xsl:choose>
				<xsl:when test="normalize-space($removeLink)">
					<xsl:variable name="linkTokens" select="str:tokenize($removeLink,'!')"/>
					<xsl:text> </xsl:text>
					<xsl:choose>
						<xsl:when test="normalize-space($linkTokens[2])">
							<a id="remove_{$id}" style="display:none;cursor:hand;cursor:pointer;"  onclick="if (noDoubleClick()) {$linkTokens[1]}" target="_blank"><img src="{/root/gui/url}/images/del.gif" alt="{/root/gui/strings/del}"/></a>
						</xsl:when>
						<xsl:otherwise>
							<a id="remove_{$id}"  style="cursor:hand;cursor:pointer;" onclick="if (noDoubleClick()) {$removeLink}" target="_blank"><img src="{/root/gui/url}/images/del.gif" alt="{/root/gui/strings/del}"/></a>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:when>
				<xsl:otherwise>
					<span id="remove_{$id}"/>
				</xsl:otherwise>
			</xsl:choose>
	
			<!-- up button -->
			<xsl:choose>
				<xsl:when test="normalize-space($upLink)">
					<xsl:variable name="linkTokens" select="str:tokenize($upLink,'!')"/>
					<xsl:text> </xsl:text>
					<xsl:choose>
						<xsl:when test="normalize-space($linkTokens[2])">
							<a id="up_{$id}" style="display:none;cursor:hand;cursor:pointer;"  onclick="if (noDoubleClick()) {$linkTokens[1]}" target="_blank"><img src="{/root/gui/url}/images/up.gif" alt="{/root/gui/strings/up}"/></a>
						</xsl:when>
						<xsl:otherwise>
							<a id="up_{$id}" style="cursor:hand;cursor:pointer;"  onclick="if (noDoubleClick()) {$upLink}" target="_blank"><img src="{/root/gui/url}/images/up.gif" alt="{/root/gui/strings/up}"/></a>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:when>
				<xsl:otherwise>
					<span id="up_{$id}"/>
				</xsl:otherwise>
			</xsl:choose>
	
			<!-- down button -->
			<xsl:choose>
				<xsl:when test="normalize-space($downLink)">
					<xsl:variable name="linkTokens" select="str:tokenize($downLink,'!')"/>
					<xsl:text> </xsl:text>
					<xsl:choose>
						<xsl:when test="normalize-space($linkTokens[2])">
							<a id="down_{$id}" style="display:none;cursor:hand;cursor:pointer;"  onclick="if (noDoubleClick()) {$linkTokens[1]}" target="_blank"><img src="{/root/gui/url}/images/down.gif" alt="{/root/gui/strings/down}"/></a>
						</xsl:when>
						<xsl:otherwise>
							<a id="down_{$id}" style="cursor:hand;cursor:pointer;" onclick="if (noDoubleClick()) {$downLink}" target="_blank"><img src="{/root/gui/url}/images/down.gif" alt="{/root/gui/strings/down}"/></a>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:when>
				<xsl:otherwise>
					<span id="down_{$id}"/>
				</xsl:otherwise>
			</xsl:choose>
	
			<!-- xsd and schematron validation error button -->
			<xsl:if test="normalize-space($validationLink)">
				<xsl:text> </xsl:text>
				<xsl:variable name="remove">"'</xsl:variable>
				<xsl:variable name="jsString" select="translate($validationLink,$remove,'')"/>
				<a id="validationError{$id}" onclick="setBunload(false);" href='javascript:doEditorAlert("{$jsString}");'><img src="{/root/gui/url}/images/validationError.gif"/></a>
			</xsl:if>
		</span>
	</xsl:template>

</xsl:stylesheet>
