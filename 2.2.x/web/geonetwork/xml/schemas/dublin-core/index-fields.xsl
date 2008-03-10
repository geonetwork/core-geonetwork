<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet version="1.0"
					 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
					 xmlns:dc = "http://purl.org/dc/elements/1.1/"
					 xmlns:dct="http://purl.org/dc/terms/">

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
		<Document>
	
			<!-- This index for "coverage" requires significant expansion to 
				 work well for spatial searches. It now only works for very 
				 strictly formatted content -->
			<xsl:variable name="coverage" select="/simpledc/dc:coverage"/>
			<xsl:variable name="n" select="substring-after($coverage,'North ')"/>
			<xsl:variable name="north" select="substring-before($n,',')"/>
			<xsl:variable name="s" select="substring-after($coverage,'South ')"/>
			<xsl:variable name="south" select="substring-before($s,',')"/>
			<xsl:variable name="e" select="substring-after($coverage,'East ')"/>
			<xsl:variable name="east" select="substring-before($e,',')"/>
			<xsl:variable name="w" select="substring-after($coverage,'West ')"/>
			<xsl:variable name="west" select="substring-before($w,'. ')"/>
			<xsl:variable name="p" select="substring-after($coverage,'(')"/>
			<xsl:variable name="place" select="substring-before($p,')')"/>
			
			<xsl:for-each select="/simpledc/dc:identifier">
				<Field name="identifier" string="{string(.)}" store="true" index="true" token="false"/>
			</xsl:for-each>
	
			<xsl:for-each select="/simpledc/dct:abstract">
				<Field name="abstract" string="{string(.)}" store="true" index="true" token="true"/>
			</xsl:for-each>
			
			<xsl:for-each select="/simpledc/dct:modified">
				<Field name="changeDate" string="{string(.)}" store="true" index="true" token="false"/>
			</xsl:for-each>
	
			<xsl:for-each select="/simpledc/dc:format">
				<Field name="format" string="{string(.)}" store="true" index="true" token="false"/>
			</xsl:for-each>
	
			<xsl:for-each select="/simpledc/dc:type">
				<Field name="type" string="{string(.)}" store="true" index="true" token="false"/>
			</xsl:for-each>
	
			<xsl:for-each select="/simpledc/dc:relation">
				<Field name="relation" string="{string(.)}" store="true" index="true" token="false"/>
			</xsl:for-each>
	
			<xsl:for-each select="/simpledc/dct:spatial">
				<Field name="spatial" string="{string(.)}" store="true" index="true" token="false"/>
			</xsl:for-each>
	
			<!-- This is needed by the CITE test script to look for strings like 'a b*'
				  strings that contain spaces -->
	
			<xsl:for-each select="/simpledc/dc:title">
				<Field name="title" string="{string(.)}" store="false" index="true" token="false"/>
			</xsl:for-each>
	
			<xsl:apply-templates select="/simpledc/dc:title">
				<xsl:with-param name="name" select="'title'"/>
				<xsl:with-param name="token" select="'true'"/>
			</xsl:apply-templates>
	
			<xsl:apply-templates select="/simpledc/dc:description">
				<xsl:with-param name="name" select="'description'"/>
				<xsl:with-param name="token" select="'true'"/>
			</xsl:apply-templates>
			
			<Field name="westBL"  string="{$west  + 360}" store="true" index="true" token="false"/>
			<Field name="eastBL"  string="{$east  + 360}" store="true" index="true" token="false"/>
			<Field name="southBL" string="{$south + 360}" store="true" index="true" token="false"/>
			<Field name="northBL" string="{$north + 360}" store="true" index="true" token="false"/>
			
			<Field name="keyword" string="{$place}" store="true" index="true" token="false"/>
	
			
			<xsl:apply-templates select="/simpledc/dc:subject">
				<xsl:with-param name="name" select="'keyword'"/>
				<xsl:with-param name="store" select="'true'"/>
	
				<!--  the value was 'true' and has been changed to false to pass the CITE scripts 
						Anyway, users should add several dc:subject elements each one containing a
						single word. Hence, it is lecit to not tokenize the subject -->
	
				<xsl:with-param name="token" select="'false'"/> 
			</xsl:apply-templates>
	
			<Field name="any" store="false" index="true" token="true">
				<xsl:attribute name="string">
					<xsl:apply-templates select="/simpledc" mode="allText"/>
				</xsl:attribute>
			</Field>
	
			<!-- locally searchable fields -->
			
			<!-- defaults to true -->
			<Field name="digital" string="true" store="false" index="true" token="false"/>
				
		</Document>
	</xsl:template>
	
	<!-- ========================================================================================= -->
	
	<!-- text element, by default indexed, not stored, tokenized -->
	<xsl:template match="*">
		<xsl:param name="name"  select="name(.)"/>
		<xsl:param name="store" select="'false'"/>
		<xsl:param name="index" select="'true'"/>
		<xsl:param name="token" select="'false'"/>
		
	   <Field name="{$name}" string="{string(.)}" store="{$store}" index="{$index}" token="{$token}"/>
	</xsl:template>
	
	<!-- ========================================================================================= -->
	
	<!--allText -->
	<xsl:template match="*" mode="allText">
		<xsl:for-each select="@*"><xsl:value-of select="concat(string(.),' ')"/></xsl:for-each>
		<xsl:choose>
			<xsl:when test="*"><xsl:apply-templates select="*" mode="allText"/></xsl:when>
			<xsl:otherwise><xsl:value-of select="concat(string(.),' ')"/></xsl:otherwise>
		</xsl:choose>
	</xsl:template>

</xsl:stylesheet>

