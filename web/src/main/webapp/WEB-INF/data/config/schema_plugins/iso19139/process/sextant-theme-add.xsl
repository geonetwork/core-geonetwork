<?xml version="1.0" encoding="UTF-8"?>
<!--  
Stylesheet used to transfert Sextant theme from category table to
keyword in the metadata record.


http://localhost:8080/geonetwork/srv/eng/q?fast=index&_id=98630
http://localhost:8080/geonetwork/srv/eng/metadata.select?selected=add-all
http://localhost:8080/geonetwork/srv/eng/metadata.batch.processing?process=sextant-theme-add

-->
<xsl:stylesheet version="2.0" xmlns:geonet="http://www.fao.org/geonetwork"
	xmlns:gmd="http://www.isotc211.org/2005/gmd" 
	xmlns:gco="http://www.isotc211.org/2005/gco"
	xmlns:srv="http://www.isotc211.org/2005/srv"
	xmlns:xlink="http://www.w3.org/1999/xlink"
	xmlns:gmx="http://www.isotc211.org/2005/gmx"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	<!-- The uuid of the record -->
	<xsl:param name="id" select="//geonet:info/id"/>
	<xsl:param name="withTranslation" select="'false'"/>
	<xsl:param name="mainLanguage" select="/gmd:MD_Metadata/gmd:language/gmd:LanguageCode/@codeListValue"/>
	<xsl:param name="url" select="'http://localhost:8080/geonetwork/srv/eng/'"/>
	
	<!-- The metadata catagories -->
	<xsl:param name="categories" select="document(concat($url, 'xml.metadata.category?id=', $id))"/>
	
	
	<xsl:template match="gmd:MD_DataIdentification|*[@gco:isoType='gmd:MD_DataIdentification']">
		
		<xsl:message>## Adding categories for record: <xsl:value-of select="$id"/></xsl:message>
		<xsl:message>  Main language: <xsl:value-of select="$mainLanguage"/></xsl:message>
		<xsl:message>  Others: <xsl:value-of select="string-join(/gmd:MD_Metadata/gmd:locale/gmd:PT_Locale/
			gmd:languageCode/gmd:LanguageCode/@codeListValue, ', ')"/></xsl:message>
		<xsl:message>  Found categories: <xsl:copy-of select="$categories//category[on]"/></xsl:message>
		
		<xsl:copy>
			<xsl:copy-of select="@*"/>
			<xsl:apply-templates select="gmd:citation"/>
			<xsl:apply-templates select="gmd:abstract"/>
			<xsl:apply-templates select="gmd:purpose"/>
			<xsl:apply-templates select="gmd:credit"/>
			<xsl:apply-templates select="gmd:status"/>
			<xsl:apply-templates select="gmd:pointOfContact"/>
			<xsl:apply-templates select="gmd:resourceMaintenance"/>
			<xsl:apply-templates select="gmd:graphicOverview"/>
			<xsl:apply-templates select="gmd:resourceFormat"/>
			
			<xsl:call-template name="add-categories"/>
			<xsl:apply-templates select="gmd:descriptiveKeywords"/>
			
			<xsl:apply-templates select="gmd:resourceSpecificUsage"/>
			<xsl:apply-templates select="gmd:resourceConstraints"/>
			<xsl:apply-templates select="gmd:aggregationInfo"/>
			<xsl:apply-templates select="gmd:spatialRepresentationType"/>
			<xsl:apply-templates select="gmd:spatialResolution"/>
			<xsl:apply-templates select="gmd:language"/>
			<xsl:apply-templates select="gmd:characterSet"/>
			<xsl:apply-templates select="gmd:topicCategory"/>
			<xsl:apply-templates select="gmd:environmentDescription"/>
			<xsl:apply-templates select="gmd:extent"/>
			<xsl:apply-templates select="gmd:supplementalInformation"/>
		</xsl:copy>
	</xsl:template>
	
	<xsl:template name="add-categories">
		<xsl:variable name="metadata-categories" select="$categories//category[on]"/>
		<xsl:variable name="otherLanguages" select="/gmd:MD_Metadata/gmd:locale/gmd:PT_Locale/
			gmd:languageCode/gmd:LanguageCode"/>

		<xsl:message>  Look for existing sextant theme: <xsl:value-of select="count(//gmd:thesaurusName[gmd:CI_Citation/gmd:title/gco:CharacterString='Thèmes Sextant'])"/></xsl:message>
		
		<xsl:if test="$metadata-categories and 
			count(//gmd:thesaurusName[gmd:CI_Citation/gmd:title/gco:CharacterString='Thèmes Sextant']) = 0">
			<gmd:descriptiveKeywords>
				<gmd:MD_Keywords>
					<xsl:for-each select="$metadata-categories">
						<xsl:variable name="cat" select="."/>
						<gmd:keyword>
							<gco:CharacterString><xsl:value-of select="label/*[name(.) = $mainLanguage]"/></gco:CharacterString>
							
							<!-- Add translation if requuested -->
							<xsl:if test="$withTranslation = 'true'">
								<xsl:for-each select="$otherLanguages">
									<xsl:variable name="language" select="@codeListValue"/>
									<xsl:variable name="translation" select="$cat/label/*[name(.) = $language]"/>
									
									<xsl:if test="normalize-space($translation) != ''">
										<xsl:message>  Adding translation <xsl:value-of select="$translation"/> (<xsl:value-of select="$language"/>)</xsl:message>
										<gmd:PT_FreeText>
											<gmd:textGroup>
												<gmd:LocalisedCharacterString locale="#{upper-case($language)}"><xsl:value-of select="$translation"/></gmd:LocalisedCharacterString>
											</gmd:textGroup>
										</gmd:PT_FreeText>
									</xsl:if>
								</xsl:for-each>
							</xsl:if>
						</gmd:keyword>
					</xsl:for-each>
					<gmd:type>
						<gmd:MD_KeywordTypeCode codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/Codelist/ML_gmxCodelists.xml#MD_KeywordTypeCode"
							codeListValue="theme"/>
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
										<gmd:CI_DateTypeCode codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/Codelist/ML_gmxCodelists.xml#CI_DateTypeCode"
											codeListValue="publication"/>
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
		</xsl:if>
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
