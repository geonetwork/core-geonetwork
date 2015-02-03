<?xml version="1.0" encoding="UTF-8"?>
<!--  
Stylesheet used to convert Sextant theme from gco:CharacterString to XLink.

The process does not alter the record, if the theme is not found. An output message
is logged in that case.

<gmd:descriptiveKeywords xmlns:srv="http://www.isotc211.org/2005/srv" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:gmx="http://www.isotc211.org/2005/gmx">
	<gmd:MD_Keywords>
		<gmd:keyword>
			<gco:CharacterString>/Milieu physique/Habitats physiques</gco:CharacterString>
		</gmd:keyword>
		<gmd:type>
			<gmd:MD_KeywordTypeCode codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/Codelist/ML_gmxCodelists.xml#MD_KeywordTypeCode" codeListValue="theme"/>
		</gmd:type>
		<gmd:thesaurusName>
			<gmd:CI_Citation>
				<gmd:title>
					<gco:CharacterString>Thèmes Sextant</gco:CharacterString>
				</gmd:title>
				<gmd:date>
					<gmd:CI_Date>
						<gmd:date>
						<gco:Date>2013-08-20</gco:Date>
						</gmd:date>
						<gmd:dateType>
							<gmd:CI_DateTypeCode codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/Codelist/ML_gmxCodelists.xml#CI_DateTypeCode" codeListValue="publication"/>
						</gmd:dateType>
					</gmd:CI_Date>
				</gmd:date>
				<gmd:identifier>
					<gmd:MD_Identifier>
						<gmd:code>
							<gmx:Anchor xlink:href="http://www.ifremer.fr/geonetwork/srv/eng/thesaurus.download?ref=local.theme.sextant-theme">geonetwork.thesaurus.local.theme.sextant-theme</gmx:Anchor>
						</gmd:code>
					</gmd:MD_Identifier>
				</gmd:identifier>
			</gmd:CI_Citation>
		</gmd:thesaurusName>
	</gmd:MD_Keywords>
</gmd:descriptiveKeywords>

to local XLink:
<gmd:descriptiveKeywords>
  <gmd:MD_Keywords 
  	xlink:href="local://xml.keyword.get?thesaurus=local.theme.sextant-theme&amp;amp;id=http://www.ifremer.fr/thesaurus/category%2379&amp;amp;multiple=false" 
  	xlink:show="replace">

to XLing using http protocol (slower than local protocol)
<gmd:descriptiveKeywords>
  <gmd:MD_Keywords 
  	xlink:href="http://localhost:8080/geonetwork/srv/fre/xml.keyword.get?thesaurus=local.theme.sextant-theme&amp;amp;id=http://www.ifremer.fr/thesaurus/category%2379&amp;amp;multiple=false" 
  	xlink:show="replace">


http://localhost:8080/geonetwork/srv/eng/q?_schema=iso19139*
http://localhost:8080/geonetwork/srv/eng/metadata.select?selected=add-all
http://localhost:8080/geonetwork/srv/eng/metadata.batch.processing?process=sextant-theme-toxlink

-->
<xsl:stylesheet version="2.0" xmlns:geonet="http://www.fao.org/geonetwork"
	xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:gco="http://www.isotc211.org/2005/gco"
	xmlns:srv="http://www.isotc211.org/2005/srv" xmlns:xlink="http://www.w3.org/1999/xlink"
	xmlns:gmx="http://www.isotc211.org/2005/gmx" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:param name="url" select="'http://localhost:8080/geonetwork/srv/'"/>

	<!-- Main metadata language -->
	<xsl:param name="mainLanguage" select="/gmd:MD_Metadata/gmd:language/gmd:LanguageCode/@codeListValue"/>
	
	<!-- Sextant Theme -->
	<xsl:param name="sextantThemesThesaurus"
		select="document(concat($url, $mainLanguage, '/xml.search.keywords?pNewSearch=true&amp;pTypeSearch=1&amp;pThesauri=local.theme.sextant-theme&amp;pMode=searchBox&amp;maxResults=200'))"/>
	
	<xsl:template
		match="gmd:descriptiveKeywords[
			not(gmd:MD_Keywords/@xlink:href) and 
			descendant::gmx:Anchor = 'geonetwork.thesaurus.local.theme.sextant-theme']">
		<xsl:variable name="keywordLabel" select="gmd:MD_Keywords/gmd:keyword/gco:CharacterString"/>
		<xsl:variable name="keywordId"
			select="$sextantThemesThesaurus/response/descKeys/keyword[value = $keywordLabel]/uri"/>
		<xsl:message>Replace <xsl:value-of select="$keywordLabel"/> by xlink pointing to <xsl:value-of select="$keywordId"/></xsl:message>
		<xsl:choose>
			<xsl:when test="$keywordId != ''">
				<xsl:copy>
					<!--<gmd:MD_Keywords
						xlink:href="{$url}{$mainLanguage}/xml.keyword.get?thesaurus=local.theme.sextant-theme&amp;amp;id={replace($keywordId[1], '#', '%23')}&amp;amp;multiple=false"
						xlink:show="replace"/> 
					-->
				<gmd:MD_Keywords
					xlink:href="local://{$mainLanguage}/xml.keyword.get?thesaurus=local.theme.sextant-theme&amp;amp;id={replace($keywordId[1], '#', '%23')}&amp;amp;multiple=false"
					xlink:show="replace"/>
				</xsl:copy>
			</xsl:when>
			<xsl:otherwise>
				<xsl:message>### No changes made to record with uuid <xsl:value-of select="/gmd:MD_Metadata/geonet:info/uuid"/></xsl:message>
				<xsl:copy-of select="."/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- Do a copy of every nodes and attributes -->
	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()"/>
		</xsl:copy>
	</xsl:template>

	<!-- Remove geonet:* elements. -->
	<xsl:template match="geonet:*" priority="2"/>
</xsl:stylesheet>
