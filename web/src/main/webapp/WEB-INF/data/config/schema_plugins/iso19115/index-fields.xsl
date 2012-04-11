<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

	<!-- This file defines what parts of the metadata are indexed by Lucene
		Searches can be conducted on indexes defined here. 
		The Field@name attribute defines the name of the search variable.
		If a variable has to be maintained in the user session, it needs to be 
		added to the GeoNetwork constants in the Java source code.
		Please keep indexes consistent among metadata standards if they should
		work accross different metadata resources -->
	<!-- ========================================================================================= -->
	
	<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes" />
	
	<!-- ========================================================================================= -->

	<xsl:template match="/">
		<xsl:variable name="langCode" select="string(/Metadata/mdLang/languageCode/@value)"/>
		<Document locale="{$langCode}">

			<!-- locale information -->	
	        <Field name="_locale" string="{$langCode}" store="true" index="true"/>
            <Field name="_docLocale" string="{$langCode}" store="true" index="true"/>
      			
        	<!-- For multilingual docs it is good to have a title in the default locale.  In this type of metadata we don't have one but in the general case we do so we need to add it to all -->
            <Field name="_defaultTitle" string="{string(/Metadata/dataIdInfo/idCitation/resTitle)}" store="true" index="true"/>
	
			<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->		
			<!-- === Title === -->	
			<xsl:apply-templates select="/Metadata/dataIdInfo/idCitation/resTitle">
				<xsl:with-param name="name"  select="'title'"/>
				<xsl:with-param name="store" select="'true'"/>
			</xsl:apply-templates>
      
        	<!-- not tokenized title for sorting -->
            <Field name="_title" string="{string(/Metadata/dataIdInfo/idCitation/resTitle)}" 
                    store="false" index="true"/>
			<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->		
			<!-- === Abstract === -->	
			<xsl:apply-templates select="/Metadata/dataIdInfo/idAbs">
				<xsl:with-param name="name"  select="'abstract'"/>
				<xsl:with-param name="store" select="'true'"/>
			</xsl:apply-templates>
			
			<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->		
			<!-- === Revision date === -->
			<xsl:for-each select="/Metadata/dataIdInfo/idCitation/resRefDate[dateType/DateTypCd/@value='revision']/refDate">
				<Field name="revisionDate" string="{string(.)}" store="true" index="true"/>
			</xsl:for-each>
	
			<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->		
			<!-- === creation date === -->
			<xsl:for-each select="/Metadata/dataIdInfo/idCitation/resRefDate[dateType/DateTypCd/@value='creation']/refDate">
				<Field name="creationDate" string="{string(.)}" store="true" index="true"/>
			</xsl:for-each>
	
			<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->		
			<!-- === Publication date === -->
			<xsl:for-each select="/Metadata/dataIdInfo/idCitation/resRefDate[dateType/DateTypCd/@value='publication']/refDate">
				<Field name="publicationDate" string="{string(.)}" store="true" index="true"/>
			</xsl:for-each>
	
			<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->		
			<!-- === metadata language === -->
			<xsl:for-each select="/Metadata/mdLang/languageCode/@value">
				<Field name="language" string="{string(.)}" store="true" index="true"/>
			</xsl:for-each>
	
	
			<xsl:for-each select="/Metadata/mdDateSt">
				<Field name="changeDate" string="{string(.)}" store="true" index="true"/>
			</xsl:for-each>
	
			<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->		
			<!-- === Metadata file identifier (GUID in GeoNetwork) === -->
			<xsl:apply-templates select="/Metadata/mdFileID">
				<xsl:with-param name="name"  select="'fileId'"/>
			</xsl:apply-templates>
	
			<xsl:choose>
				<xsl:when test="/Metadata/mdHrLv/ScopeCd/@value">
					<xsl:for-each select="/Metadata/mdHrLv/ScopeCd/@value">
						<Field name="type" string="{string(.)}" store="true" index="true"/>
					</xsl:for-each>
				</xsl:when>
				<xsl:otherwise>
					<Field name="type" string="dataset" store="true" index="true"/>
				</xsl:otherwise>
			</xsl:choose>
	
			<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->		
			<!-- === Parent identifier (should be mapped if available) === -->
	<!--		<xsl:for-each select="/Metadata/parentIdentifier">
				<Field name="parentId" string="{string(.)}" store="true" index="true"/>
			</xsl:for-each> -->
			
			<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->		
			<!-- === Responsible organization === -->		
			<xsl:for-each select="/Metadata/mdContact/rpOrgName">
				<Field name="orgName" string="{string(.)}" store="true" index="true"/>
				<Field name="responsibleParty" string="{concat('pointOfContact|metadata|', ., '|')}" store="true" index="false"/>							
			</xsl:for-each>
	
			<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->		
			<!-- === Security constraints (yes or no) === -->	
			<xsl:choose>
				<xsl:when test="/Metadata/mdConst/SecConsts">
					<Field name="secConstr" string="true" store="true" index="true"/>
				</xsl:when>
				<xsl:otherwise>
					<Field name="secConstr" string="false" store="true" index="true"/>
				</xsl:otherwise>
			</xsl:choose>
	
			<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->		
			<!-- === latlon coordinates + 360, zero-padded, indexed, not stored, not tokenized === -->
			<xsl:apply-templates select="/Metadata/dataIdInfo/geoBox/westBL" mode="latLon"/>
			<xsl:apply-templates select="/Metadata/dataIdInfo/geoBox/eastBL" mode="latLon"/>
			<xsl:apply-templates select="/Metadata/dataIdInfo/geoBox/southBL" mode="latLon"/>
			<xsl:apply-templates select="/Metadata/dataIdInfo/geoBox/northBL" mode="latLon"/>
			<xsl:for-each select="/Metadata/dataIdInfo/geoBox">
				<Field name="geoBox" string="{concat(westBL, '|', 
					southBL, '|', 
					eastBL, '|', 
					northBL
					)}" store="true" index="false"/>
			</xsl:for-each>
			<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->		
			<!-- === keywords === -->
			<xsl:apply-templates select="/Metadata/dataIdInfo/descKeys/keyword">
				<xsl:with-param name="store" select="'true'"/>
			</xsl:apply-templates>
	
			<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->		
			<!-- === Online Resources protocol === -->		
	
			<xsl:for-each select="/Metadata/distInfo/distTranOps">
				<xsl:for-each select="onLineSrc/protocol">
					<!-- this field MUST NOT be tokenized, otherwise search fails -->
					<Field name="protocol" string="{string(.)}" store="false" index="true"/>
				</xsl:for-each>
			</xsl:for-each>
	
			<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->		
			<!-- === Topic category === -->	
			
			<xsl:for-each select="/Metadata/dataIdInfo/tpCat/TopicCatCd/@value">
				<Field name="topicCat" string="{string(.)}" store="false" index="true"/>
			</xsl:for-each>
			
			<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->		
			<!-- === all text === -->
			<Field name="any" store="false" index="true">
				<xsl:attribute name="string">
					<xsl:apply-templates select="/Metadata" mode="allText"/>
				</xsl:attribute>
			</Field>
			
			<Field name="any" store="false" index="true">
				<xsl:attribute name="string">
					<xsl:value-of select="normalize-space(string(/Metadata))"/>
					<xsl:text> </xsl:text>
					<xsl:for-each select="//*/@value">
						<xsl:value-of select="concat(., ' ')"/>
					</xsl:for-each>
				</xsl:attribute>
			</Field>

			<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->		
			<!-- === locally searchable fields === -->
			
			<xsl:if test="/Metadata/dataIdInfo/idCitation/presForm[contains(PresFormCd/@value, 'Digital')]">
				<Field name="digital" string="true" store="false" index="true"/>
			</xsl:if>
			
			<xsl:if test="/Metadata/dataIdInfo/idCitation/presForm[contains(PresFormCd/@value, 'Hardcopy')]">
				<Field name="paper" string="true" store="false" index="true"/>
			</xsl:if>
	
		</Document>
	</xsl:template>
	
	<!-- ========================================================================================= -->
	
	<!-- text element, by default indexed, not stored nor tokenized -->
	<xsl:template match="*">
		<xsl:param name="name"  select="name(.)"/>
		<xsl:param name="store" select="'false'"/>
		<xsl:param name="index" select="'true'"/>
		
		<Field name="{$name}" string="{string(.)}" store="{$store}" index="{$index}"/>
	</xsl:template>
	
	<!-- ========================================================================================= -->

	<!-- codelist element, indexed, not stored nor tokenized -->
	<xsl:template match="*[./*/@value]">
		<xsl:param name="name" select="name(.)"/>
		
		<Field name="{$name}" string="{*/@value}" store="false" index="true"/>
	</xsl:template>
	
	<!-- ========================================================================================= -->

	<!-- latlon coordinates indexed as numeric. -->
	<xsl:template match="*" mode="latLon">
		<xsl:param name="name" select="name(.)"/>
		
		<Field name="{$name}" string="{string(.)}" store="true" index="true"/>
	</xsl:template>
	
</xsl:stylesheet>
