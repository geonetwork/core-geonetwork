<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" 
	xmlns:gfc="http://www.isotc211.org/2005/gfc" xmlns:gmd="http://www.isotc211.org/2005/gmd" 
	xmlns:gmx="http://www.isotc211.org/2005/gmx" xmlns:gco="http://www.isotc211.org/2005/gco">

	<!-- This file defines what parts of the metadata are indexed by Lucene
		Searches can be conducted on indexes defined here. 
		The Field@name attribute defines the name of the search variable.
		If a variable has to be maintained in the user session, it needs to be 
		added to the GeoNetwork constants in the Java source code.
		Please keep indexes consistent among metadata standards if they should
		work accross different metadata resources -->
	<!-- ========================================================================================= -->

	<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>

	<!-- ========================================================================================= -->

	<xsl:template match="/">
		<Document>

			<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
			<!-- === Title === -->
			<xsl:apply-templates select="/gfc:FC_FeatureCatalogue/gfc:name/gco:CharacterString">
				<xsl:with-param name="name" select="'title'"/>
				<xsl:with-param name="store" select="'true'"/>
			</xsl:apply-templates>

			<!-- not tokenized title for sorting -->
			<Field name="_title" string="{string(/gfc:FC_FeatureCatalogue/gfc:name/gco:CharacterString)}" store="false" index="true"/>


			<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
			<!-- === Abstract === -->
			<xsl:apply-templates select="/gfc:FC_FeatureCatalogue/gfc:scope/gco:CharacterString">
			  <xsl:with-param name="name" select="'abstract'"/>
			  <xsl:with-param name="store" select="'true'"/>
			</xsl:apply-templates>

			<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
			<!-- === Revision date === -->
			<xsl:for-each select="/gfc:FC_FeatureCatalogue/gfc:versionDate/gco:Date">
				<Field name="revisionDate" string="{string(.)}" store="true" index="true"/>
			</xsl:for-each>

			<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
			<!-- === metadata language === -->
			<xsl:for-each select="/gfc:FC_FeatureCatalogue/gfc:language/gmd:LanguageCode">
				<Field name="language" string="{string(.)}" store="true" index="true"/>
			</xsl:for-each>

			<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
			<!-- === Metadata file identifier (GUID in GeoNetwork) === -->
			<xsl:apply-templates select="/gfc:FC_FeatureCatalogue/gfc:name/gco:CharacterString">
				<xsl:with-param name="name" select="'fileId'"/>
			</xsl:apply-templates>

			<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
			<!-- === Responsible organization === -->
			<xsl:for-each select="/gfc:FC_FeatureCatalogue/gfc:producer/gmd:CI_ResponsibleParty/gmd:organisationName/gco:CharacterString">				
				<xsl:variable name="role" select="../../gmd:role/*/@codeListValue"/>
				<xsl:variable name="logo" select="descendant::*/gmx:FileName/@src"/>
				
				<!-- TODO : Add complete xPath -->
				<Field name="orgName" string="{string(.)}" store="false" index="true"/>
				<Field name="responsibleParty" string="{concat($role, '|metadata|', ., '|', $logo)}" store="true" index="false"/>							
			</xsl:for-each>
			
			<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
			<!-- === all text === -->
			<Field name="any" store="false" index="true">
				<xsl:attribute name="string">
					<xsl:value-of select="normalize-space(string(/gfc:FC_FeatureCatalogue))"/>
					<xsl:text> </xsl:text>
					<xsl:for-each select="//@codeListValue">
						<xsl:value-of select="concat(., ' ')"/>
					</xsl:for-each>
				</xsl:attribute>
			</Field>
			
			<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
			<!-- === Metadata type (iso19110 is used to describe attribute datasets) === -->
			<Field name="type" string="model" store="true" index="true"/>

		</Document>
	</xsl:template>

	<!-- ========================================================================================= -->

	<!-- text element, by default indexed, not stored nor tokenized -->
	<xsl:template match="*">
		<xsl:param name="name" select="name(.)"/>
		<xsl:param name="store" select="'false'"/>
		<xsl:param name="index" select="'true'"/>

		<Field name="{$name}" string="{string(.)}" store="{$store}" index="{$index}"/>
	</xsl:template>

</xsl:stylesheet>
