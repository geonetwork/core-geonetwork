<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
xmlns:xalan= "http://xml.apache.org/xalan" exclude-result-prefixes="xalan">

<xsl:import href="parser.xsl"/>
<xsl:import href="lucene-utils.xsl"/>

<xsl:variable name="opView"     select="'_op0'"/>
<xsl:variable name="opDownload" select="'_op1'"/>
<xsl:variable name="opDynamic"  select="'_op5'"/>
<xsl:variable name="opFeatured" select="'_op6'"/>

<xsl:variable name="similarity" select="/request/similarity"/>

<!--
computes bounding box values
-->
<xsl:variable name="region"     select="string(/request/region)"/>
<xsl:variable name="regionData" select="/request/regions/*[string(id)=$region]"/>

<xsl:variable name="westBL">
	<xsl:choose>
		<xsl:when test="$region"><xsl:value-of select="$regionData/west + 360"/></xsl:when>
		<xsl:otherwise><xsl:value-of select="/request/westBL + 360"/></xsl:otherwise>
	</xsl:choose>
</xsl:variable>

<xsl:variable name="eastBL">
	<xsl:choose>
		<xsl:when test="$region"><xsl:value-of select="$regionData/east + 360"/></xsl:when>
		<xsl:otherwise><xsl:value-of select="/request/eastBL + 360"/></xsl:otherwise>
	</xsl:choose>
</xsl:variable>

<xsl:variable name="southBL">
	<xsl:choose>
		<xsl:when test="$region"><xsl:value-of select="$regionData/south + 360"/></xsl:when>
		<xsl:otherwise><xsl:value-of select="/request/southBL + 360"/></xsl:otherwise>
	</xsl:choose>
</xsl:variable>

<xsl:variable name="northBL">
	<xsl:choose>
		<xsl:when test="$region"><xsl:value-of select="$regionData/north + 360"/></xsl:when>
		<xsl:otherwise><xsl:value-of select="/request/northBL + 360"/></xsl:otherwise>
	</xsl:choose>
</xsl:variable>

<!--
compiles a request
-->
<xsl:template match="/">

	<BooleanQuery>
		
		<!-- title -->
		<xsl:call-template name="textField">
			<xsl:with-param name="expr" select="/request/title"/>
			<xsl:with-param name="field" select="'title'"/>
		</xsl:call-template>
		
		<!-- abstract -->
		<xsl:call-template name="textField">
			<xsl:with-param name="expr" select="/request/abstract"/>
			<xsl:with-param name="field" select="'abstract'"/>
		</xsl:call-template>
		
		<!-- any -->
		<xsl:call-template name="textField">
			<xsl:with-param name="expr" select="/request/any"/>
			<xsl:with-param name="field" select="'any'"/>
		</xsl:call-template>

		<xsl:if test="string(/request/themekey) != ''">
			<BooleanClause prohibited="false" required="true">
				<BooleanQuery>
					<xsl:for-each select="/request/themekey">
						<xsl:if test="string(.) != '' ">
							<BooleanClause required="false" prohibited="false">
								<xsl:call-template name="compile">
									<xsl:with-param name="expr" select="string(.)"/>
									<xsl:with-param name="field" select="'keyword'"/>
								</xsl:call-template>
							</BooleanClause>
						</xsl:if>
					</xsl:for-each>
				</BooleanQuery>
			</BooleanClause>
		</xsl:if>

		<!-- digital and paper maps -->
		
		<!-- if both are off or both are on then no clauses are added -->
		<xsl:if test="string(/request/digital)='on' and string(/request/paper)=''">
			<BooleanClause required="true" prohibited="false">
				<TermQuery fld="digital" txt="true"/>
			</BooleanClause>
		</xsl:if>
		<xsl:if test="string(/request/paper)='on' and string(/request/digital)=''">
			<BooleanClause required="true" prohibited="false">
				<TermQuery fld="paper" txt="true"/>
			</BooleanClause>
		</xsl:if>
		
		<!-- online and download -->
		<!-- disabled
		<xsl:choose>
			
			<!- - online or download - ->
			<xsl:when test="string(/request/online)='on' and string(/request/download)='on'">
				<BooleanClause required="true" prohibited="false">
					<BooleanQuery>
						<BooleanClause required="false" prohibited="false">
							<BooleanQuery>
								<xsl:call-template name="online"/>
							</BooleanQuery>
						</BooleanClause>
						<BooleanClause required="false" prohibited="false">
							<BooleanQuery>
								<xsl:call-template name="download"/>
							</BooleanQuery>
						</BooleanClause>
					</BooleanQuery>
				</BooleanClause>
			</xsl:when>

			<!- - online - ->
			<xsl:when test="string(/request/online)='on'">
				<xsl:call-template name="online"/>
			</xsl:when>
			
			<!- - download - ->
			<xsl:when test="string(/request/download)='on'">
				<xsl:call-template name="download"/>
			</xsl:when>
			
		</xsl:choose>
		-->
		
		<!-- bounding box -->

		<xsl:if test="$northBL != 'NaN' and $southBL != 'NaN' and $eastBL != 'NaN' and $westBL != 'NaN'">
			<xsl:choose>
				
				<!-- equal -->
				<xsl:when test="string(/request/relation)='equal'">
					<xsl:call-template name="equal"/>
				</xsl:when>
				
				<!-- overlaps -->
				<xsl:when test="string(/request/relation)='overlaps'">
					<xsl:call-template name="overlaps"/>
				</xsl:when>

				<!-- fullyOutsideOf -->
				<xsl:when test="string(/request/relation)='fullyOutsideOf'">
					<xsl:call-template name="fullyOutsideOf"/>
				</xsl:when>

				<!-- encloses -->
				<xsl:when test="string(/request/relation)='encloses'">
					<xsl:call-template name="encloses"/>
				</xsl:when>

				<!-- fullyEnclosedWithin -->
				<xsl:when test="string(/request/relation)='fullyEnclosedWithin'">
					<xsl:call-template name="fullyEnclosedWithin"/>
				</xsl:when>

			</xsl:choose>
		</xsl:if>

		<xsl:choose>
			<!-- featured: just use group "all" for view and featured privilege -->
			<xsl:when test="string(/request/featured)='true'">
				<!-- FIXME: featured privilege is unused for groups different from "all"
				<xsl:call-template name="orFields">
					<xsl:with-param name="expr" select="/request/group"/>
					<xsl:with-param name="field" select="$opFeatured"/>
				</xsl:call-template>
				-->
				<BooleanClause required="true" prohibited="false">
					<TermQuery fld="{$opFeatured}" txt="1"/>
				</BooleanClause>
				<BooleanClause required="true" prohibited="false">
					<TermQuery fld="{$opView}" txt="1"/>
				</BooleanClause>
			</xsl:when>
			
			<!-- use all user's groups for view privileges -->
			<xsl:otherwise>
				<BooleanClause required="true" prohibited="false">
					<BooleanQuery>
						<xsl:for-each select="/request/group">
							<BooleanClause required="false" prohibited="false">
								<TermQuery fld="{$opView}" txt="{string(.)}"/>
							</BooleanClause>
						</xsl:for-each>

						<xsl:if test="/request/isReviewer">
							<xsl:for-each select="/request/group">
								<BooleanClause required="false" prohibited="false">
									<TermQuery fld="_groupOwner" txt="{string(.)}"/>
								</BooleanClause>
							</xsl:for-each>
						</xsl:if>

						<xsl:if test="/request/owner">
							<BooleanClause required="false" prohibited="false">
								<TermQuery fld="_owner" txt="{/request/owner}"/>
							</BooleanClause>
						</xsl:if>

						<xsl:if test="/request/isAdmin">
							<BooleanClause required="false" prohibited="false">
								<TermQuery fld="_dummy" txt="0"/>
							</BooleanClause>
						</xsl:if>

					</BooleanQuery>
				</BooleanClause>
			</xsl:otherwise>
		
		</xsl:choose>
		
		<!-- category -->
		<xsl:if test="string(/request/category) != ''">
			<BooleanClause prohibited="false" required="true">
				<BooleanQuery>
					<xsl:for-each select="/request/category">
						<xsl:if test="string(.) != '' ">
							<BooleanClause required="false" prohibited="false">
								<xsl:call-template name="compile">
									<xsl:with-param name="expr" select="string(.)"/>
									<xsl:with-param name="field" select="'_cat'"/>
								</xsl:call-template>
							</BooleanClause>
						</xsl:if>
					</xsl:for-each>
				</BooleanQuery>
			</BooleanClause>
		</xsl:if>

		<!-- site id -->
		<xsl:if test="string(/request/siteId)!=''">
			<BooleanClause required="true" prohibited="false">
				<TermQuery fld="_source" txt="{/request/siteId}"/>
			</BooleanClause>
		</xsl:if>

		<!-- template -->
		<xsl:choose>
			<xsl:when test="string(/request/template)='y'">
				<BooleanClause required="true" prohibited="false">
					<TermQuery fld="_isTemplate" txt="y"/>
				</BooleanClause>
			</xsl:when>
			<xsl:when test="string(/request/template)='s'">
				<BooleanClause required="true" prohibited="false">
					<TermQuery fld="_isTemplate" txt="s"/>
				</BooleanClause>
			</xsl:when>
			<xsl:otherwise>
				<BooleanClause required="true" prohibited="false">
					<TermQuery fld="_isTemplate" txt="n"/>
				</BooleanClause>
			</xsl:otherwise>
		</xsl:choose>

	</BooleanQuery>
</xsl:template>

<!--
online
-->
<xsl:template name="online">
	<BooleanClause required="true" prohibited="false">
		<BooleanQuery>
			<BooleanClause required="false" prohibited="false">
				<TermQuery fld="protocol" txt="esri:aims-3.1-http-get-image"/>
			</BooleanClause>
			<BooleanClause required="false" prohibited="false">
				<TermQuery fld="protocol" txt="esri:aims-4.0-http-get-image"/>
			</BooleanClause>
			<BooleanClause required="false" prohibited="false">
				<TermQuery fld="protocol" txt="ogc:wms-1.0.0-http-get-capabilities"/>
			</BooleanClause>
			<BooleanClause required="false" prohibited="false">
				<TermQuery fld="protocol" txt="ogc:wms-1.0.0-http-get-map"/>
			</BooleanClause>
		</BooleanQuery>
	</BooleanClause>
	
	<!-- online privileges -->
	<xsl:call-template name="orFields">
		<xsl:with-param name="expr" select="/request/group"/>
		<xsl:with-param name="field" select="$opDynamic"/>
	</xsl:call-template>
</xsl:template>

<!--
download
-->
<xsl:template name="download">
	<BooleanClause required="true" prohibited="false">
		<TermQuery fld="protocol" txt="www:download-1.0-http--download"/>
	</BooleanClause>

	<!-- download privileges -->
	<xsl:call-template name="orFields">
		<xsl:with-param name="expr" select="/request/group"/>
		<xsl:with-param name="field" select="$opDownload"/>
	</xsl:call-template>
</xsl:template>

<xsl:template name="textField">
	<xsl:param name="expr"/>
	<xsl:param name="field"/>
	
	<xsl:if test="$expr!=''">
		<BooleanClause required="true" prohibited="false">
			<xsl:call-template name="compile">
				<xsl:with-param name="expr" select="$expr"/>
				<xsl:with-param name="field" select="$field"/>
			</xsl:call-template>
		</BooleanClause>
	</xsl:if>
</xsl:template>

<!--
compiles a parse tree into a class tree
-->
<xsl:template name="compile">
	<xsl:param name="expr"/>
	<xsl:param name="field"/>
	
	<xsl:variable name="tree">
		<xsl:call-template name="parse">
			<xsl:with-param name="expr" select="$expr"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:call-template name="doCompile">
		<xsl:with-param name="expr" select="xalan:nodeset($tree)/*"/>
		<xsl:with-param name="field" select="$field"/>
	</xsl:call-template>
</xsl:template>

<!--
recursive compiler
-->
<xsl:template name="doCompile">
	<xsl:param name="expr"/>
	<xsl:param name="field"/>
	
	<xsl:choose>
		<!-- tree: build a boolean query -->
		<xsl:when test="name($expr)='tree'">
			<xsl:variable name="required" select="$expr/@type='and'"/>
			<xsl:variable name="prohibited" select="$expr/@type='not'"/>
			
			<BooleanQuery>
				<xsl:choose>
					<xsl:when test="$prohibited">
						<BooleanClause required="true" prohibited="false">
							<!-- first clause is positive -->
							<xsl:call-template name="doCompile">
								<xsl:with-param name="expr" select="$expr/*[1]"/>
								<xsl:with-param name="field" select="$field"/>
							</xsl:call-template>
						</BooleanClause>
						<!-- other clauses are negative -->
						<xsl:for-each select="$expr/*[position()>1]">
							<BooleanClause required="false" prohibited="true">
								<xsl:call-template name="doCompile">
									<xsl:with-param name="expr" select="."/>
									<xsl:with-param name="field" select="$field"/>
								</xsl:call-template>
							</BooleanClause>
						</xsl:for-each>
					</xsl:when>
					<xsl:otherwise>
						<xsl:for-each select="$expr/*">
							<BooleanClause required="{$required}" prohibited="false">
								<xsl:call-template name="doCompile">
									<xsl:with-param name="expr" select="."/>
									<xsl:with-param name="field" select="$field"/>
								</xsl:call-template>
							</BooleanClause>
						</xsl:for-each>
					</xsl:otherwise>
				</xsl:choose>
			</BooleanQuery>
		</xsl:when>
		
		<!-- Keyword -->
		<xsl:when test="$field='keyword' and $expr/@type='qstring'">
			<TermQuery fld="{$field}" txt="{$expr/@text}"/>
		</xsl:when>
		
		<!-- quoted string: build a phrase query -->
		<xsl:when test="$expr/@type='qstring'">
			<PhraseQuery>
				<xsl:call-template name="phraseQueryArgs">
					<xsl:with-param name="expr" select="$expr/@text"/>
					<xsl:with-param name="field" select="$field"/>
				</xsl:call-template>
			</PhraseQuery>
		</xsl:when>

		<!-- prefix string: build a prefix query -->
		<xsl:when test="$expr/@type='pstring'">
			<PrefixQuery fld="{$field}" txt="{$expr/@text}"/>
		</xsl:when>
		
		<!-- simple string -->
		<xsl:otherwise>
			<xsl:choose>
				<xsl:when test="$similarity!=1"><!-- if similarity = 1 just use TermQuery -->
					<FuzzyQuery fld="{$field}" txt="{$expr/@text}" sim="{$similarity}"/>
				</xsl:when>
				<xsl:otherwise>
					<TermQuery fld="{$field}" txt="{$expr/@text}"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:otherwise>
	</xsl:choose>
</xsl:template>

</xsl:stylesheet>
