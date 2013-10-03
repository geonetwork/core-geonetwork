<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl ="http://www.w3.org/1999/XSL/Transform"
	xmlns:gmd="http://www.isotc211.org/2005/gmd"
	xmlns:gts="http://www.isotc211.org/2005/gts"
	xmlns:gco="http://www.isotc211.org/2005/gco"
	xmlns:gmx="http://www.isotc211.org/2005/gmx"
	xmlns:srv="http://www.isotc211.org/2005/srv"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xmlns:gml="http://www.opengis.net/gml"
	xmlns:xlink="http://www.w3.org/1999/xlink"
	xmlns:gn="http://www.fao.org/geonetwork"
	xmlns:gn-fn-metadata="http://geonetwork-opensource.org/xsl/functions/metadata"
	xmlns:gn-fn-iso19139="http://geonetwork-opensource.org/xsl/functions/profiles/iso19139"
	xmlns:exslt="http://exslt.org/common"
	exclude-result-prefixes="#all">
	
	<xsl:include href="mapping.xsl"/>
	
	
	<!-- Could we define a tab configuration ? -->
	<xsl:variable name="tabConfig">
		<tab id="default">
			<section match="gmd:identificationInfo"/>
			<section match="gmd:distributionInfo"/>
			<section match="gmd:dataQualityInfo"/>
			<section match="gmd:MD_Metadata"/>
		</tab>
	    <tab id="inspire">
	        <section name="resource">
	            <field xpath="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:title"/>
	            <field xpath="gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:citation/gmd:CI_Citation/gmd:title"/>
	            <field xpath="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:abstract"/>
	            <field xpath="gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:abstract"/>
	            <field xpath="gmd:hierarchyLevel"/>
	            <field xpath="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:descriptiveKeywords/gmd:MD_Keywords/gmd:keyword"
	                    eval=""/>
	            <fieldset xpath="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:descriptiveKeywords"
	                eval="contains(gmd:thesaurusName/gmd:CI_Citation/gmd:title/gco:CharacterString, 'GEMET - INSPIRE')">
	                <!-- The default XML snippet to insert if empty -->
	                <template>
	                    
	                </template>
	            </fieldset>
	            <field xpath="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:date"
	                eval="gmd:CI_Date/gmd:dateType/gmd:CI_DateTypeCode/@codeListValue = 'revision'">
	                <template>
	                    <gmd:date>
	                        <gmd:CI_Date>
	                            <gmd:date>
	                                <gco:DateTime>${date}</gco:DateTime>
	                            </gmd:date>
	                            <gmd:dateType>
	                                <gmd:CI_DateTypeCode codeList="http://www.isotc211.org/2005/resources/codeList.xml#CI_DateTypeCode" codeListValue="revision"/>
	                            </gmd:dateType>
	                        </gmd:CI_Date>
	                    </gmd:date>
	                </template>
	            </field>
	            <fieldset xpath="quality/spec">
	                
	            </fieldset>
	        </section>
	        <section name="metadata">
	            <field xpath="gmd:dateStamp"/>
	        </section>
	    </tab>
	</xsl:variable>
	
	
	<!-- Dispatching to the profile mode according to the tab -->
	<xsl:template name="render-iso19139">
		<xsl:param name="base" as="node()"/>
		
		<xsl:variable name="theTabConfiguration" select="$tabConfig/tab[@id = $tab]/section"/>
		
		<xsl:choose>
			<xsl:when test="$theTabConfiguration">
				<xsl:for-each select="$theTabConfiguration">
					<xsl:variable name="matchingElement" select="@match"/>
				    
				    <!-- Apply tab mode first and if empty, fallback to default. -->
					<xsl:apply-templates mode="mode-iso19139" select="$base/*[name() = $matchingElement]"/>
				</xsl:for-each>
			</xsl:when>
			<xsl:when test="$tab = 'xml'">
				<xsl:apply-templates mode="render-xml" select="$base"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:apply-templates mode="mode-iso19139" select="$base"/>
			</xsl:otherwise>
		</xsl:choose>
		
	</xsl:template>
	
	
</xsl:stylesheet>
