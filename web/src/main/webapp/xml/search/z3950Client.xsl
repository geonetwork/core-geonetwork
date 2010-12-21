<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
xmlns:exslt = "http://exslt.org/common" exclude-result-prefixes="exslt">

<xsl:import href="parser.xsl"/>

<!--
bounding box values
-->
<xsl:variable name="westBL" select="/request/westBL"/>
<xsl:variable name="eastBL" select="/request/eastBL"/>
<xsl:variable name="southBL" select="/request/southBL"/>
<xsl:variable name="northBL" select="/request/northBL"/>

<!--
compiles a request into a Z39.50 query
-->
<xsl:template match="/">
	<xsl:variable name="tree">
		
		<!-- title -->
		<xsl:call-template name="textField">
			<xsl:with-param name="expr" select="/request/title"/>
			<xsl:with-param name="use"  select="4"/> <!-- title -->
		</xsl:call-template>
		
		<!-- abstract -->
		<xsl:call-template name="textField">
			<xsl:with-param name="expr" select="/request/abstract"/>
			<xsl:with-param name="use"  select="62"/> <!-- abstract -->
		</xsl:call-template>
		
		<!-- any -->
		<xsl:call-template name="textField">
			<xsl:with-param name="expr" select="/request/any|/request/or"/>
			<xsl:with-param name="use"  select="1016"/> <!-- any -->
		</xsl:call-template>
		
		<!-- keywords -->
		<xsl:call-template name="textField">
			<xsl:with-param name="expr" select="/request/themekey"/>
			<xsl:with-param name="use"  select="2002"/> <!-- themekey -->
		</xsl:call-template>

		<!-- bounding box -->
		<xsl:if test="$northBL != 'NaN' and $southBL != 'NaN' and $eastBL != 'NaN' and $westBL != 'NaN' and normalize-space(/request/region)!=''">
			<xsl:choose>
				
				<!-- equal -->
				<xsl:when test="string(/request/relation)='equal'">
					<xsl:call-template name="boundingField">
						<xsl:with-param name="relation" select="3"/> <!-- equal -->
					</xsl:call-template>
				</xsl:when>
				
				<!-- overlaps -->
				<xsl:when test="string(/request/relation)='overlaps'">
					<xsl:call-template name="boundingField">
						<xsl:with-param name="relation" select="7"/> <!-- overlaps -->
					</xsl:call-template>
				</xsl:when>

				<!-- fullyOutsideOf -->
				<xsl:when test="string(/request/relation)='fullyOutsideOf'">
					<xsl:call-template name="boundingField">
						<xsl:with-param name="relation" select="10"/> <!-- fullyOutsideOf -->
					</xsl:call-template>
				</xsl:when>

				<!-- encloses -->
				<xsl:when test="string(/request/relation)='encloses'">
					<xsl:call-template name="boundingField">
						<xsl:with-param name="relation" select="9"/> <!-- encloses -->
					</xsl:call-template>
				</xsl:when>

				<!-- fullyEnclosedWithin -->
				<xsl:when test="string(/request/relation)='fullyEnclosedWithin'">
					<xsl:call-template name="boundingField">
						<xsl:with-param name="relation" select="8"/> <!-- fullyEnclosedWithin -->
					</xsl:call-template>
				</xsl:when>

			</xsl:choose>
		</xsl:if>
		
	</xsl:variable>
	
	<xsl:variable name="query">
		<query attrset="{/request/attrset}">
			<xsl:call-template name="binarizeTop">
				<xsl:with-param name="list" select="exslt:node-set($tree)/*"/>
			</xsl:call-template>
		</query>
	</xsl:variable>

	<xsl:copy-of select="$query"/>
	
</xsl:template>

<xsl:template name="binarizeTop">
	<xsl:param name="list"/>
	
	<xsl:choose>
		<xsl:when test="count($list)&lt;2">
			<xsl:copy-of select="$list"/>
		</xsl:when>
		
		<xsl:otherwise>
			<and>
				<xsl:copy-of select="$list[1]"/>
				<xsl:call-template name="binarizeTop">
					<xsl:with-param name="list" select="$list[position()>1]"/>
				</xsl:call-template>
			</and>
		</xsl:otherwise>
	</xsl:choose>
		
</xsl:template>

<!--
compiles a text field with default structure=6 (word list) and relation=3 (equal)
-->
<xsl:template name="textField">
	<xsl:param name="expr"/>
	<xsl:param name="use"/>
	<xsl:param name="structure" select="6"/> <!-- word list -->
	<xsl:param name="relation"  select="3"/> <!-- equal -->
	
	<xsl:if test="$expr!=''">
		<xsl:call-template name="compile">
			<xsl:with-param name="expr" select="$expr"/>
			<xsl:with-param name="use"       select="$use"/>
			<xsl:with-param name="structure" select="$structure"/>
			<xsl:with-param name="relation"  select="$relation"/>
		</xsl:call-template>
	</xsl:if>
</xsl:template>

<!--
compiles a bounding coordinates field (2060) with structure=201 (coordinate String)
-->
<xsl:template name="boundingField">
	<xsl:param name="relation"/>
	
	<term use="2060" structure="201" relation="{$relation}">
		<xsl:value-of select="concat($northBL, ' ', $westBL, ' ', $southBL, ' ', $eastBL)"/>
	</term>
</xsl:template>

<!--
compiles a parse tree into a class tree
-->
<xsl:template name="compile">
	<xsl:param name="expr"/>
	<xsl:param name="use"/>
	<xsl:param name="structure"/>
	<xsl:param name="relation"/>
	
	<xsl:variable name="tree">
		<xsl:call-template name="parse">
			<xsl:with-param name="expr" select="$expr"/>
		</xsl:call-template>
	</xsl:variable>
	
	<xsl:variable name="btree">
		<xsl:call-template name="binarize">
			<xsl:with-param name="expr" select="$tree"/>
		</xsl:call-template>
	</xsl:variable>
	
	<xsl:call-template name="doCompile">
		<xsl:with-param name="expr"      select="exslt:node-set($btree)/*"/>
		<xsl:with-param name="use"       select="$use"/>
		<xsl:with-param name="structure" select="$structure"/>
		<xsl:with-param name="relation"  select="$relation"/>
	</xsl:call-template>
</xsl:template>

<!--
recursive compiler
-->
<xsl:template name="doCompile">
	<xsl:param name="expr"/>
	<xsl:param name="use"/>
	<xsl:param name="structure"/>
	<xsl:param name="relation"/>
	
	<xsl:choose>
		<!-- tree: build a boolean query -->
		<xsl:when test="name($expr)='tree'">
			<xsl:element name="{$expr/@type}">
				<xsl:call-template name="doCompile">
					<xsl:with-param name="expr" select="$expr/*[1]"/>
					<xsl:with-param name="use"       select="$use"/>
					<xsl:with-param name="structure" select="$structure"/>
					<xsl:with-param name="relation"  select="$relation"/>
				</xsl:call-template>
				<xsl:call-template name="doCompile">
					<xsl:with-param name="expr" select="$expr/*[2]"/>
					<xsl:with-param name="use"       select="$use"/>
					<xsl:with-param name="structure" select="$structure"/>
					<xsl:with-param name="relation"  select="$relation"/>
				</xsl:call-template>
			</xsl:element>
		</xsl:when>
		
		<!-- quoted string: build a phrase query -->
		<xsl:when test="$expr/@type='qstring'">
			<term use="{$use}" structure="{$structure}" relation="{$relation}">
				<xsl:text>"</xsl:text>
				<xsl:value-of select="$expr/@text"/>
				<xsl:text>"</xsl:text>
			</term>
		</xsl:when>
			
		<!-- prefix string: not supported, handle like a simple string -->
		<xsl:when test="$expr/@type='pstring'">
			<term use="{$use}" structure="{$structure}" relation="{$relation}">
				<xsl:value-of select="$expr/@text"/>
				<xsl:text>*</xsl:text>
			</term>
		</xsl:when>
		
		<!-- simple string -->
		<xsl:when test="$expr/@type='string'">
			<term use="{$use}" structure="{$structure}" relation="{$relation}">
				<xsl:value-of select="$expr/@text"/>
			</term>
		</xsl:when>
	</xsl:choose>
</xsl:template>
	
</xsl:stylesheet>
