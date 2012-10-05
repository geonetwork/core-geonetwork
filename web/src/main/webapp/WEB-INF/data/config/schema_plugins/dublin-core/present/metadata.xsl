<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:dc = "http://purl.org/dc/elements/1.1/"
	xmlns:dct = "http://purl.org/dc/terms/"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xmlns:ows="http://www.opengis.net/ows"
	xmlns:csw="http://www.opengis.net/cat/csw/2.0.2"
	xmlns:geonet="http://www.fao.org/geonetwork">

  <xsl:include href="metadata-fop.xsl"/>
  
	<!-- main template - the way into processing dublin-core -->
  <xsl:template name="metadata-dublin-core">
		<xsl:param name="schema"/>
		<xsl:param name="edit" select="false()"/>
		<xsl:param name="embedded"/>

    <xsl:apply-templates mode="dublin-core" select="." >
    	<xsl:with-param name="schema" select="$schema"/>
     	<xsl:with-param name="edit"   select="$edit"/>
     	<xsl:with-param name="embedded" select="$embedded" />
    </xsl:apply-templates>
  </xsl:template>

	<!-- CompleteTab template - dc just calls completeTab from 
	     metadata-utils.xsl -->
	<xsl:template name="dublin-coreCompleteTab">
		<xsl:param name="tabLink"/>

		<xsl:call-template name="completeTab">
			<xsl:with-param name="tabLink" select="$tabLink"/>
		</xsl:call-template>
	</xsl:template>

	<!--
	default: in simple mode just a flat list
	-->
	<xsl:template mode="dublin-core" match="*|@*">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>
		
		<xsl:apply-templates mode="element" select=".">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
			<xsl:with-param name="flat"   select="$currTab='simple'"/>
		</xsl:apply-templates>
	</xsl:template>
	
	<!--
	these elements should be boxed
	-->
	<xsl:template mode="dublin-core" match="simpledc|csw:Record">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>

    <xsl:apply-templates mode="elementEP" select="*">
    	<xsl:with-param name="schema" select="$schema"/>
    	<xsl:with-param name="edit"   select="$edit"/>
    </xsl:apply-templates>
	</xsl:template>

	<xsl:template mode="dublin-core" match="dc:anyCHOICE_ELEMENT0">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>

		<xsl:apply-templates mode="elementEP" select="dc:*|geonet:child[string(@prefix)='dc']">
      <xsl:with-param name="schema" select="$schema"/>
      <xsl:with-param name="edit"   select="$edit"/>
    </xsl:apply-templates>

    <xsl:apply-templates mode="elementEP" select="dct:modified|geonet:child[string(@name)='modified']">
      <xsl:with-param name="schema" select="$schema"/>
      <xsl:with-param name="edit"   select="$edit"/>
    </xsl:apply-templates>

    <xsl:apply-templates mode="elementEP" select="dct:*[name(.)!='dct:modified']|geonet:child[string(@prefix)='dct' and name(.)!='modified']">
      <xsl:with-param name="schema" select="$schema"/>
      <xsl:with-param name="edit"   select="$edit"/>
    </xsl:apply-templates>

	</xsl:template>

	<!--
	identifier
	-->
	<xsl:template mode="dublin-core" match="dc:identifier">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>

		<xsl:apply-templates mode="simpleElement" select=".">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
			<xsl:with-param name="text"><xsl:value-of select="."/></xsl:with-param>
		</xsl:apply-templates>
	</xsl:template>
	
	<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
	<!-- dublin-core brief formatting -->
	<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
	
	<xsl:template name="dublin-coreBrief">
		<metadata>
			<xsl:if test="dc:title">
				<title><xsl:value-of select="dc:title"/></title>
			</xsl:if>
			<xsl:if test="dc:description">
				<abstract><xsl:value-of select="dc:description"/></abstract>
			</xsl:if>

			<xsl:for-each select="dc:subject[text()]">
				<keyword><xsl:value-of select="."/></keyword>
			</xsl:for-each>
			<xsl:for-each select="dc:identifier[text()]">
				<link type="url"><xsl:value-of select="."/></link>
			</xsl:for-each>
			<!-- FIXME
			<image>IMAGE</image>
			-->
			<!-- TODO : ows:BoundingBox -->
			<xsl:variable name="coverage" select="dc:coverage"/>
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
			<xsl:if test="$n!=''">
				<geoBox>
					<westBL><xsl:value-of select="$west"/></westBL>
					<eastBL><xsl:value-of select="$east"/></eastBL>
					<southBL><xsl:value-of select="$south"/></southBL>
					<northBL><xsl:value-of select="$north"/></northBL>
				</geoBox>
			</xsl:if>
		
			<xsl:copy-of select="geonet:*"/>
		</metadata>
	</xsl:template>

	<xsl:template name="dublin-core-javascript"/>
</xsl:stylesheet>
