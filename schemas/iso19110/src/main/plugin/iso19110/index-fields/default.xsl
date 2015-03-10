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
		<!-- TODO I don't know what tag the language is in so this needs to be done -->
		<xsl:variable name="langCode" select="''"/>
		<Document locale="{$langCode}">

			<!-- locale information -->
			<Field name="_locale" string="{$langCode}" store="true" index="true"/>
			<Field name="_docLocale" string="{$langCode}" store="true" index="true"/>

			<!-- For multilingual docs it is good to have a title in the default locale.  In this type of metadata we don't have one but in the general case we do so we need to add it to all -->
			<Field name="_defaultTitle"
				string="{/gfc:FC_FeatureCatalogue/gmx:name/gco:CharacterString|
				/gfc:FC_FeatureCatalogue/gfc:name/gco:CharacterString|
				/gfc:FC_FeatureType/gfc:typeName/gco:LocalName}"
				store="true" index="true"/>

			<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
			<!-- === Title === -->
			<xsl:apply-templates
				select="/gfc:FC_FeatureCatalogue/gmx:name/gco:CharacterString|
				/gfc:FC_FeatureCatalogue/gfc:name/gco:CharacterString|
				/gfc:FC_FeatureType/gfc:typeName/gco:LocalName">
				<xsl:with-param name="name" select="'title'"/>
				<xsl:with-param name="store" select="'true'"/>
			</xsl:apply-templates>

			<!-- not tokenized title for sorting -->
			<Field name="_title"
				string="{string(/gfc:FC_FeatureCatalogue/gmx:name/gco:CharacterString|
				/gfc:FC_FeatureCatalogue/gfc:name/gco:CharacterString|
				/gfc:FC_FeatureType/gfc:typeName/gco:LocalName)}"
				store="false" index="true"/>


			<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
			<!-- === Abstract === -->
			<xsl:apply-templates
				select="/gfc:FC_FeatureCatalogue/gmx:scope/gco:CharacterString|
				/gfc:FC_FeatureCatalogue/gfc:scope/gco:CharacterString|
				/gfc:FC_FeatureType/gfc:definition/gco:CharacterString">
				<xsl:with-param name="name" select="'abstract'"/>
				<xsl:with-param name="store" select="'true'"/>
			</xsl:apply-templates>

			<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
			<!-- === Revision date === -->
			<xsl:for-each select="/gfc:FC_FeatureCatalogue/gmx:versionDate/gco:Date|
				/gfc:FC_FeatureCatalogue/gfc:versionDate/gco:Date">
				<Field name="revisionDate" string="{string(.)}" store="true" index="true"/>
			</xsl:for-each>

			<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
			<!-- === Metadata file identifier (GUID in GeoNetwork) === -->
			<xsl:apply-templates select="/gfc:FC_FeatureCatalogue/@uuid|/gfc:FC_FeatureType/@uuid">
				<xsl:with-param name="name" select="'fileId'"/>
			</xsl:apply-templates>

      <!-- Index attribute table as JSON object -->
      <xsl:variable name="attributes"
                    select=".//gfc:carrierOfCharacteristics"/>
      <xsl:if test="count($attributes) > 0">
        <xsl:variable name="jsonAttributeTable">
          [<xsl:for-each select="$attributes">
          {"name": "<xsl:value-of select="*/gfc:memberName/*/text()"/>",
            "definition": "<xsl:value-of select="*/gfc:definition/*/text()"/>",
            "type": "<xsl:value-of select="*/gfc:valueType/gco:TypeName/gco:aName/*/text()"/>"
            <xsl:if test="*/gfc:listedValue">
              ,"values": [<xsl:for-each select="*/gfc:listedValue">{
                "label": "<xsl:value-of select="*/gfc:label/*/text()"/>",
                "code": "<xsl:value-of select="*/gfc:code/*/text()"/>",
                "definition": "<xsl:value-of select="*/gfc:definition/*/text()"/>"}
              <xsl:if test="position() != last()">,</xsl:if>
              </xsl:for-each>]
            </xsl:if>}
            <xsl:if test="position() != last()">,</xsl:if>
          </xsl:for-each>]
        </xsl:variable>
        <Field name="attributeTable" index="true" store="true"
               string="{$jsonAttributeTable}"/>
      </xsl:if>

			<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
			<!-- === Responsible organization === -->
			<xsl:for-each
				select="/gfc:FC_FeatureCatalogue/gfc:producer/gmd:CI_ResponsibleParty/gmd:organisationName/gco:CharacterString">
				<xsl:variable name="role" select="../../gmd:role/*/@codeListValue"/>
				<xsl:variable name="logo" select="descendant::*/gmx:FileName/@src"/>

				<Field name="orgName" string="{string(.)}" store="false" index="true"/>
				<Field name="responsibleParty" string="{concat($role, '|metadata|', ., '|', $logo)}"
					store="true" index="false"/>
			</xsl:for-each>

			<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
			<!-- === all text === -->
			<Field name="any" store="false" index="true">
				<xsl:attribute name="string">
					<xsl:value-of
						select="normalize-space(string(/gfc:FC_FeatureCatalogue|/gfc:FC_FeatureType))"/>
					<xsl:text> </xsl:text>
					<xsl:for-each select="//@codeListValue">
						<xsl:value-of select="concat(., ' ')"/>
					</xsl:for-each>
				</xsl:attribute>
			</Field>

			<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
			<!-- === Metadata type (iso19110 is used to describe attribute datasets) === -->
			<Field name="type" string="featureCatalog" store="true" index="true"/>

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
