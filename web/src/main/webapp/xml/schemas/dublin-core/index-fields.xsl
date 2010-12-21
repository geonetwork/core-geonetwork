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
				<Field name="identifier" string="{string(.)}" store="true" index="true"/>
			</xsl:for-each>
	
			<xsl:for-each select="/simpledc/dct:abstract">
				<Field name="abstract" string="{string(.)}" store="true" index="true"/>
			</xsl:for-each>

			<xsl:for-each select="/simpledc/dc:date">
			  <Field name="createDate" string="{string(.)}" store="true" index="true"/>
			</xsl:for-each>

			
			<xsl:for-each select="/simpledc/dct:modified">
				<Field name="changeDate" string="{string(.)}" store="true" index="true"/>
			</xsl:for-each>
	
			<xsl:for-each select="/simpledc/dc:format">
				<Field name="format" string="{string(.)}" store="true" index="true"/>
			</xsl:for-each>
	
			<xsl:for-each select="/simpledc/dc:type">
				<Field name="type" string="{string(.)}" store="true" index="true"/>
			</xsl:for-each>
	
			<xsl:for-each select="/simpledc/dc:relation">
				<Field name="relation" string="{string(.)}" store="true" index="true"/>
			</xsl:for-each>
	
			<xsl:for-each select="/simpledc/dct:spatial">
				<Field name="spatial" string="{string(.)}" store="true" index="true"/>
			</xsl:for-each>
	
			<!-- This is needed by the CITE test script to look for strings like 'a b*'
				  strings that contain spaces -->
	
			<xsl:for-each select="/simpledc/dc:title">
				<Field name="title" string="{string(.)}" store="false" index="true"/>
                <!-- not tokenized title for sorting -->
                <Field name="_title" string="{string(.)}" store="true" index="true"/>
			</xsl:for-each>
	
			<xsl:apply-templates select="/simpledc/dc:title">
				<xsl:with-param name="name" select="'title'"/>
			</xsl:apply-templates>
	
			<xsl:apply-templates select="/simpledc/dc:description">
				<xsl:with-param name="name" select="'description'"/>
			</xsl:apply-templates>
			
			<Field name="westBL"  string="{$west}" store="true" index="true"/>
			<Field name="eastBL"  string="{$east}" store="true" index="true"/>
			<Field name="southBL" string="{$south}" store="true" index="true"/>
			<Field name="northBL" string="{$north}" store="true" index="true"/>
			
			<Field name="keyword" string="{$place}" store="true" index="true"/>
	
			
			<xsl:apply-templates select="/simpledc/dc:subject">
				<xsl:with-param name="name" select="'keyword'"/>
				<xsl:with-param name="store" select="'true'"/> 
			</xsl:apply-templates>
			
			<Field name="any" store="false" index="true">
				<xsl:attribute name="string">
					<xsl:value-of select="normalize-space(string(/simpledc))"/>
					<xsl:text> </xsl:text>
					<xsl:for-each select="//*/@*">
						<xsl:value-of select="concat(., ' ')"/>
					</xsl:for-each>
				</xsl:attribute>
			</Field>
			
			<!-- locally searchable fields -->
			
			<!-- defaults to true -->
			<Field name="digital" string="true" store="false" index="true"/>
				
		</Document>
	</xsl:template>
	
	<!-- ========================================================================================= -->
	
	<!-- text element, by default indexed, not stored, tokenized -->
	<xsl:template match="*">
		<xsl:param name="name"  select="name(.)"/>
		<xsl:param name="store" select="'false'"/>
		<xsl:param name="index" select="'true'"/>
		
	   <Field name="{$name}" string="{string(.)}" store="{$store}" index="{$index}"/>
	</xsl:template>

</xsl:stylesheet>