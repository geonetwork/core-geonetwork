<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet version="2.0"
					 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
					 xmlns:dc = "http://purl.org/dc/elements/1.1/"
					 xmlns:java="java:org.fao.geonet.util.XslUtil"
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

    <xsl:variable name="langCode"
                  select="if (normalize-space(dc:language) != '')
                          then string(dc:language) else 'eng'"/>

		<Document locale="{$langCode}">

			<!-- locale information -->	
	        <Field name="_locale" string="{$langCode}" store="true" index="true"/>
            <Field name="_docLocale" string="{$langCode}" store="true" index="true"/>
      			
        	<!-- For multilingual docs it is good to have a title in the default locale.  In this type of metadata we don't have one but in the general case we do so we need to add it to all -->
            <Field name="_defaultTitle" string="{string(/simpledc/dc:title)}" store="true" index="true"/>
	

			<xsl:for-each select="/simpledc/dc:identifier">
				<Field name="identifier" string="{string(.)}" store="false" index="true"/>
			</xsl:for-each>
	
			<xsl:for-each select="/simpledc/dct:abstract|/simpledc/dc:description">
				<Field name="abstract" string="{string(.)}" store="true" index="true"/>
			</xsl:for-each>

			<xsl:for-each select="/simpledc/dc:date">
			  <Field name="createDate" string="{string(.)}" store="true" index="true"/>
        <Field name="createDateYear" string="{substring(., 0, 5)}" store="true" index="true"/>
			</xsl:for-each>

			
			<xsl:for-each select="/simpledc/dct:modified">
				<Field name="changeDate" string="{string(.)}" store="true" index="true"/>
        <!--<Field name="createDateYear" string="{substring(., 0, 5)}" store="true" index="true"/>-->
			</xsl:for-each>
	
			<xsl:for-each select="/simpledc/dc:format">
				<Field name="format" string="{string(.)}" store="true" index="true"/>
			</xsl:for-each>
	
			<xsl:for-each select="/simpledc/dc:type">
				<Field name="type" string="{string(.)}" store="true" index="true"/>
			</xsl:for-each>
	
			<xsl:for-each select="/simpledc/dc:relation">
				<Field name="relation" string="{string(.)}" store="false" index="true"/>
			</xsl:for-each>
	
			<xsl:for-each select="/simpledc/dct:spatial">
				<Field name="spatial" string="{string(.)}" store="false" index="true"/>
			</xsl:for-each>
	
			<!-- This is needed by the CITE test script to look for strings like 'a b*'
				  strings that contain spaces -->
	
			<xsl:for-each select="/simpledc/dc:title">
				<Field name="title" string="{string(.)}" store="true" index="true"/>
                <!-- not tokenized title for sorting -->
                <Field name="_title" string="{string(.)}" store="false" index="true"/>
			</xsl:for-each>


      <xsl:for-each select="/simpledc/descendant::*
                        [name(.) = 'dct:references' or
                              name(.) = 'dc:relation']
                        [starts-with(., 'http') or
                              contains(. , 'resources.get') or
                              contains(., 'file.disclaimer')]">
        <xsl:variable name="name" select="tokenize(., '/')[last()]"/>
        <!-- Index link where last token after the last / is the link name. -->
        <Field name="link"
               string="{concat($name, '|', $name, '|', ., '|WWW-LINK|WWW:LINK|0')}"
               store="true"
               index="false"/>
      </xsl:for-each>


      <!-- This index for "coverage" requires significant expansion to
         work well for spatial searches. It now only works for very
         strictly formatted content -->
      <xsl:for-each select="/simpledc/dc:coverage">
        <xsl:variable name="coverage" select="."/>

        <!-- North 46.3, South 42.51, East 3.88, West -1.84 -->
        <xsl:choose>
          <xsl:when test="starts-with(., 'North')">
            <xsl:variable name="n" select="substring-after($coverage,'North ')"/>
            <xsl:variable name="north" select="substring-before($n, ',')"/>
            <xsl:variable name="s" select="substring-after($coverage,'South ')"/>
            <xsl:variable name="south" select="substring-before($s, ',')"/>
            <xsl:variable name="e" select="substring-after($coverage,'East ')"/>
            <xsl:variable name="east" select="substring-before($e, ',')"/>
            <xsl:variable name="w" select="substring-after($coverage,'West ')"/>
            <xsl:variable name="west" select="if (contains($w, '. ')) then substring-before($w, '. ') else $w"/>
            <xsl:variable name="p" select="substring-after($coverage,'(')"/>
            <xsl:variable name="place" select="substring-before($p,')')"/>

            <Field name="westBL"  string="{$west}" store="false" index="true"/>
            <Field name="eastBL"  string="{$east}" store="false" index="true"/>
            <Field name="southBL" string="{$south}" store="false" index="true"/>
            <Field name="northBL" string="{$north}" store="false" index="true"/>
            <Field name="geoBox" string="{concat($west, '|',
                                                  $south, '|',
                                                  $east, '|',
                                                  $north
                                                  )}" store="true" index="false"/>

            <Field name="keyword" string="{$place}" store="true" index="true"/>
          </xsl:when>
          <xsl:otherwise>
            <Field name="keyword" string="{.}" store="true" index="true"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:for-each>


			
			<xsl:apply-templates select="/simpledc/dc:subject">
				<xsl:with-param name="name" select="'keyword'"/>
				<xsl:with-param name="store" select="'true'"/> 
			</xsl:apply-templates>

      <xsl:for-each select="/simpledc/dct:isPartOf">
        <Field name="parentUuid" string="{string(.)}" store="true" index="true"/>
      </xsl:for-each>

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

      <xsl:for-each select="/simpledc/dc:creator">
			  <Field name="responsibleParty" string="{concat('creator', '|metadata|', ., '|')}" store="true" index="false"/>
      </xsl:for-each>

      <xsl:choose>
        <xsl:when test="/simpledc/dct:accrualPeriodicity">
          <xsl:for-each select="/simpledc/dct:accrualPeriodicity">
            <Field name="updateFrequency" string="{string(.)}" store="true" index="true"/>
            <Field name="cl_maintenanceAndUpdateFrequency_text"
                   string="{java:getCodelistTranslation('gmd:MD_MaintenanceFrequencyCode',
                                                 string(.),
                                                 string($langCode))}"
                   store="true" index="true"/>
          </xsl:for-each>
        </xsl:when>
        <xsl:otherwise>
          <Field name="updateFrequency" string="other" store="true" index="true"/>
        </xsl:otherwise>
      </xsl:choose>
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
