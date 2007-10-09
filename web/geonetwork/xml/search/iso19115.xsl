<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<xsl:template match="/">
	<Document>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->		
		<!-- === Title === -->	
		<xsl:apply-templates select="/Metadata/dataIdInfo/idCitation/resTitle">
			<xsl:with-param name="name"  select="'title'"/>
			<xsl:with-param name="token" select="'true'"/>
		</xsl:apply-templates>
		
		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->		
		<!-- === Abstract === -->	
		<xsl:apply-templates select="/Metadata/dataIdInfo/idAbs">
			<xsl:with-param name="name"  select="'abstract'"/>
			<xsl:with-param name="token" select="'true'"/>
		</xsl:apply-templates>
		
		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->		
		<!-- === Revision date === -->
		<xsl:for-each select="/Metadata/dataIdInfo/idCitation/resRefDate[dateType/DateTypCd/@value='revision']/refDate">
			<Field name="revisionDate" string="{string(.)}" store="true" index="true" token="false"/>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->		
		<!-- === creation date === -->
		<xsl:for-each select="/Metadata/dataIdInfo/idCitation/resRefDate[dateType/DateTypCd/@value='creation']/refDate">
			<Field name="creationDate" string="{string(.)}" store="true" index="true" token="false"/>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->		
		<!-- === Publication date === -->
		<xsl:for-each select="/Metadata/dataIdInfo/idCitation/resRefDate[dateType/DateTypCd/@value='publication']/refDate">
			<Field name="publicationDate" string="{string(.)}" store="true" index="true" token="false"/>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->		
		<!-- === metadata language === -->
		<xsl:for-each select="/Metadata/mdLang/languageCode/@value">
			<Field name="language" string="{string(.)}" store="true" index="true" token="false"/>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->		
		<!-- === Metadata file identifier (GUID in GeoNetwork) === -->
		<xsl:apply-templates select="/Metadata/mdFileID">
			<xsl:with-param name="name"  select="'fileId'"/>
			<xsl:with-param name="token" select="'false'"/>
		</xsl:apply-templates>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->		
		<!-- === Parent identifier (should be mapped if available) === -->
<!--		<xsl:for-each select="/Metadata/parentIdentifier">
			<Field name="parentId" string="{string(.)}" store="true" index="true" token="false"/>
		</xsl:for-each> -->
		
		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->		
		<!-- === Responsible organization === -->		
		<xsl:for-each select="/Metadata/mdContact/rpOrgName">
			<Field name="orgName" string="{string(.)}" store="true" index="true" token="true"/>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->		
		<!-- === Security constraints (yes or no) === -->	
		<xsl:choose>
			<xsl:when test="/Metadata/mdConst/SecConsts">
				<Field name="secConstr" string="true" store="true" index="true" token="false"/>
			</xsl:when>
			<xsl:otherwise>
				<Field name="secConstr" string="false" store="true" index="true" token="false"/>
			</xsl:otherwise>
		</xsl:choose>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->		
		<!-- === latlon coordinates + 360, zero-padded, indexed, not stored, not tokenized === -->
		<xsl:apply-templates select="/Metadata/dataIdInfo/geoBox/westBL" mode="latLon"/>
		<xsl:apply-templates select="/Metadata/dataIdInfo/geoBox/eastBL" mode="latLon"/>
		<xsl:apply-templates select="/Metadata/dataIdInfo/geoBox/southBL" mode="latLon"/>
		<xsl:apply-templates select="/Metadata/dataIdInfo/geoBox/northBL" mode="latLon"/>
		
		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->		
		<!-- === keywords === -->
		<xsl:apply-templates select="/Metadata/dataIdInfo/descKeys/keyword">
			<xsl:with-param name="store" select="'true'"/>
			<xsl:with-param name="token" select="'false'"/>
		</xsl:apply-templates>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->		
		<!-- === Online Resources protocol === -->		

		<xsl:for-each select="/Metadata/distInfo/distTranOps">
			<xsl:for-each select="onLineSrc/protocol">
				<!-- this field MUST NOT be tokenized, otherwise search fails -->
				<Field name="protocol" string="{string(.)}" store="true" index="true" token="false"/>
			</xsl:for-each>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->		
		<!-- === Topic category === -->	
		
		<xsl:for-each select="/Metadata/dataIdInfo/tpCat/TopicCatCd/@value">
			<Field name="topicCat" string="{string(.)}" store="true" index="true" token="false"/>
		</xsl:for-each>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->		
		<!-- === all text === -->
		<Field name="any" store="false" index="true" token="true">
			<xsl:attribute name="string">
				<xsl:apply-templates select="/Metadata" mode="allText"/>
			</xsl:attribute>
		</Field>

		<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->		
		<!-- === locally searchable fields === -->
		
		<xsl:if test="/Metadata/dataIdInfo/idCitation/presForm[contains(PresFormCd/@value, 'Digital')]">
			<Field name="digital" string="true" store="false" index="true" token="false"/>
		</xsl:if>
		
		<xsl:if test="/Metadata/dataIdInfo/idCitation/presForm[contains(PresFormCd/@value, 'Hardcopy')]">
			<Field name="paper" string="true" store="false" index="true" token="false"/>
		</xsl:if>

	</Document>
</xsl:template>

<!-- text element, by default indexed, not stored nor tokenized -->
<xsl:template match="*">
	<xsl:param name="name"  select="name(.)"/>
	<xsl:param name="store" select="'false'"/>
	<xsl:param name="index" select="'true'"/>
	<xsl:param name="token" select="'false'"/>
	
	<Field name="{$name}" string="{string(.)}" store="{$store}" index="{$index}" token="{$token}"/>
</xsl:template>

<!-- codelist element, indexed, not stored nor tokenized -->
<xsl:template match="*[./*/@value]">
	<xsl:param name="name" select="name(.)"/>
	
	<Field name="{$name}" string="{*/@value}" store="false" index="true" token="false"/>
</xsl:template>

<!-- latlon coordinates + 360, zero-padded, indexed, not stored, not tokenized -->
<xsl:template match="*" mode="latLon">
	<xsl:param name="name" select="name(.)"/>
	
	<Field name="{$name}" string="{string(.) + 360}" store="true" index="true" token="false"/>
</xsl:template>

<!--allText -->
<xsl:template match="*" mode="allText">
	<xsl:for-each select="@*"><xsl:value-of select="concat(string(.),' ')"/></xsl:for-each>
	<xsl:choose>
		<xsl:when test="*"><xsl:apply-templates select="*" mode="allText"/></xsl:when>
		<xsl:otherwise><xsl:value-of select="concat(string(.),' ')"/></xsl:otherwise>
	</xsl:choose>
</xsl:template>

</xsl:stylesheet>
