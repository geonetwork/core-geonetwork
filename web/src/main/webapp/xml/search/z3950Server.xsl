<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
xmlns:exslt= "http://exslt.org/common" exclude-result-prefixes="exslt">

<xsl:import href="parser.xsl"/>
<xsl:import href="lucene-utils.xsl"/>

<xsl:variable name="opView"     select="'_op0'"/>
<xsl:variable name="category"     select="'_cat'"/>

<!--
computes bounding box values - don't add 360 to them!
-->
<xsl:variable name="boundingBox" select="/request/query//term[@use='2060']/text()"/>
<xsl:variable name="northBL"     select="substring-before($boundingBox,' ')"/>
<xsl:variable name="rest1"       select="substring-after($boundingBox,' ')"/>
<xsl:variable name="westBL"      select="substring-before($rest1,' ')"/>
<xsl:variable name="rest2"       select="substring-after($rest1,' ')"/>
<xsl:variable name="southBL"     select="substring-before($rest2,' ')"/>
<xsl:variable name="eastBL"      select="substring-after($rest2,' ')"/>

<!--
compiles a request
-->
<xsl:template match="/">

	<BooleanQuery>

		<BooleanClause required="true" prohibited="false">
			<xsl:call-template name="compile">
				<xsl:with-param name="expr" select="/request/query"/>
			</xsl:call-template>
		</BooleanClause>

		<!-- view privileges -->
		<xsl:call-template name="orFields">
			<xsl:with-param name="expr" select="/request/mygroups"/>
			<xsl:with-param name="field" select="$opView"/>
		</xsl:call-template>

		<!-- collection in z query is passed through to category search on local
		     z server -->
		<xsl:call-template name="orFields">
			<xsl:with-param name="expr" select="/request/category"/>
			<xsl:with-param name="field" select="$category"/>
		</xsl:call-template>

		<BooleanClause required="true" prohibited="false">
			<TermQuery fld="_isTemplate" txt="n"/>
		</BooleanClause>

	</BooleanQuery>
</xsl:template>

<!--
compiles a parse tree into a class tree
-->
<xsl:template name="compile">
	<xsl:param name="expr"/>

	<xsl:call-template name="doCompile">
		<xsl:with-param name="expr" select="exslt:node-set($expr)"/>
	</xsl:call-template>
</xsl:template>

<!--
recursive compiler
-->
<xsl:template name="doCompile">
	<xsl:param name="expr"/>

	<xsl:choose>
		<!-- query: recurse -->
		<xsl:when test="name($expr)='query'">
			<xsl:call-template name="doCompile">
				<xsl:with-param name="expr" select="$expr/*"/>
			</xsl:call-template>
		</xsl:when>

		<!-- and: build a boolean query -->
		<xsl:when test="name($expr)='and'">
			<BooleanQuery>
				<xsl:for-each select="$expr/*">
					<BooleanClause required="true" prohibited="false">
						<xsl:call-template name="doCompile">
							<xsl:with-param name="expr" select="."/>
						</xsl:call-template>
					</BooleanClause>
				</xsl:for-each>
			</BooleanQuery>
		</xsl:when>

		<!-- or: build a boolean query -->
		<xsl:when test="name($expr)='or'">
			<BooleanQuery>
				<xsl:for-each select="$expr/*">
					<BooleanClause required="false" prohibited="false">
						<xsl:call-template name="doCompile">
							<xsl:with-param name="expr" select="."/>
						</xsl:call-template>
					</BooleanClause>
				</xsl:for-each>
			</BooleanQuery>
		</xsl:when>

		<!-- not: build a boolean query -->
		<xsl:when test="name($expr)='not'">
			<BooleanQuery>
				<BooleanClause required="true" prohibited="false">
					<!-- first clause is positive -->
					<xsl:call-template name="doCompile">
						<xsl:with-param name="expr" select="$expr/*[1]"/>
					</xsl:call-template>
				</BooleanClause>
				<!-- other clauses are negative -->
				<xsl:for-each select="$expr/*[position()>1]">
					<BooleanClause required="false" prohibited="true">
						<xsl:call-template name="doCompile">
							<xsl:with-param name="expr" select="."/>
						</xsl:call-template>
					</BooleanClause>
				</xsl:for-each>
			</BooleanQuery>
		</xsl:when>

		<xsl:when test="name($expr)='term'">

   		<!-- title -->
			<xsl:if test="$expr/@use='4'">
				<xsl:call-template name="Multi2WordListTerm">
					<xsl:with-param name="expr" select="$expr/text()"/>
					<xsl:with-param name="field1" select="'altTitle'"/>
					<xsl:with-param name="field2" select="'title'"/>
				</xsl:call-template>
			</xsl:if>

			<!-- abstract -->
			<xsl:if test="$expr/@use='62'">
				<xsl:call-template name="wordListTerm">
					<xsl:with-param name="expr" select="$expr/text()"/>
					<xsl:with-param name="field" select="'abstract'"/>
				</xsl:call-template>
			</xsl:if>

			<!-- description (same Z3950 attribute, different Lucene index) -->
			<xsl:if test="$expr/@use='62' or $expr/@use='3102'">
				<xsl:call-template name="wordListTerm">
					<xsl:with-param name="expr" select="$expr/text()"/>
					<xsl:with-param name="field" select="'description'"/>
				</xsl:call-template>
			</xsl:if>

			<!-- changeDate -->
			<xsl:if test="$expr/@use='1012'">
				<xsl:call-template name="dateTerm">
					<xsl:with-param name="date" select="$expr/text()"/>
					<xsl:with-param name="field" select="'_changeDate'"/>
					<xsl:with-param name="relation" select="$expr/@relation"/>
					<xsl:with-param name="structure" select="$expr/@structure"/>
				</xsl:call-template>
			</xsl:if>

			<!-- createDate -->
			<xsl:if test="$expr/@use='30'">
				<xsl:call-template name="dateTerm">
					<xsl:with-param name="date" select="$expr/text()"/>
					<xsl:with-param name="field" select="'_createDate'"/>
					<xsl:with-param name="relation" select="$expr/@relation"/>
					<xsl:with-param name="structure" select="$expr/@structure"/>
				</xsl:call-template>
			</xsl:if>

			<!-- publicationDate -->
			<xsl:if test="$expr/@use='31'">
				<xsl:call-template name="dateTerm">
					<xsl:with-param name="date" select="$expr/text()"/>
					<xsl:with-param name="field" select="'publicationDate'"/>
					<xsl:with-param name="relation" select="$expr/@relation"/>
					<xsl:with-param name="structure" select="$expr/@structure"/>
				</xsl:call-template>
			</xsl:if>
                       
			<!-- beginDate-->
			<xsl:if test="$expr/@use='2072'">
				<xsl:call-template name="dateTerm">
					<xsl:with-param name="date" select="$expr/text()"/>
					<xsl:with-param name="field" select="'tempExtentBegin'"/>
					<xsl:with-param name="relation" select="$expr/@relation"/>
					<xsl:with-param name="structure" select="$expr/@structure"/>
				</xsl:call-template>
			</xsl:if>
                       
			<!-- endingDate-->
			<xsl:if test="$expr/@use='2073'">
				<xsl:call-template name="dateTerm">
					<xsl:with-param name="date" select="$expr/text()"/>
					<xsl:with-param name="field" select="'tempExtentEnd'"/>
					<xsl:with-param name="relation" select="$expr/@relation"/>
					<xsl:with-param name="structure" select="$expr/@structure"/>
				</xsl:call-template>
			</xsl:if>

			<!-- spatialDomain, spatialreferencemethod -->
			<xsl:if test="$expr/@use='2059' or $expr/@use='3302'">
				<xsl:call-template name="wordListTerm">
					<xsl:with-param name="expr" select="$expr/text()"/>
					<xsl:with-param name="field" select="'crs'"/>
				</xsl:call-template>
			</xsl:if>

			<!-- placeKeyword, place-->
			<xsl:if test="$expr/@use='2042' or $expr/@use='2061'">
				<xsl:call-template name="wordListTerm">
					<xsl:with-param name="expr" select="$expr/text()"/>
					<xsl:with-param name="field" select="'geoDescCode'"/>
				</xsl:call-template>
			</xsl:if>
                       
			<!-- Type, ResourceType -->
			<xsl:if test="$expr/@use='1031'">
				<xsl:call-template name="wordListTerm">
					<xsl:with-param name="expr" select="$expr/text()"/>
					<xsl:with-param name="field" select="'keywordType'"/>
				</xsl:call-template>
			</xsl:if>
                       
			<!-- Author -->
			<xsl:if test="$expr/@use='1003'">
				<xsl:call-template name="wordListTerm">
					<xsl:with-param name="expr" select="$expr/text()"/>
					<xsl:with-param name="field" select="'orgName'"/>
				</xsl:call-template>
			</xsl:if>

			<!-- format-->
			<xsl:if test="$expr/@use='1034'">
				<xsl:call-template name="wordListTerm">
					<xsl:with-param name="expr" select="$expr/text()"/>
					<xsl:with-param name="field" select="'format'"/>
				</xsl:call-template>
			</xsl:if>

			<!-- fileId -->
			<xsl:if test="$expr/@use='2012'">
				<xsl:call-template name="wordListTerm">
					<xsl:with-param name="expr" select="$expr/text()"/>
					<xsl:with-param name="field" select="'fileId'"/>
				</xsl:call-template>
			</xsl:if>

			<!-- identifier -->
			<xsl:if test="$expr/@use='12'">
				<xsl:call-template name="wordListTerm">
					<xsl:with-param name="expr" select="$expr/text()"/>
					<xsl:with-param name="field" select="'identifier'"/>
				</xsl:call-template>
			</xsl:if>

			<!-- keyword (three equivalent Z3950 attributes, three different Lucene indexes) -->
			<!-- subject (three equivalent Z3950 attributes, three different Lucene indexes) -->
			<!-- topicCat (three equivalent Z3950 attributes, three different Lucene indexes) -->
			<xsl:if test="$expr/@use='21' or $expr/@use='29' or $expr/@use='2002' or $expr/@use='3121' or $expr/@use='3122'">
				<xsl:call-template name="Multi3WordListTerm">
					<xsl:with-param name="expr" select="$expr/text()"/>
					<xsl:with-param name="field1" select="'keyword'"/>
					<xsl:with-param name="field2" select="'subject'"/>
					<xsl:with-param name="field3" select="'topicCat'"/>
				</xsl:call-template>
			</xsl:if>

			<!-- any -->
			<xsl:if test="$expr/@use='1016'">
				<xsl:call-template name="wordListTerm">
					<xsl:with-param name="expr" select="$expr/text()"/>
					<xsl:with-param name="field" select="'any'"/>
				</xsl:call-template>
			</xsl:if>
       
			<!-- bounding box -->
			<xsl:if test="$expr/@use='2060' and $boundingBox">
				<xsl:choose>

					<!-- equal -->
					<xsl:when test="$expr/@relation=3">
						<BooleanQuery>
							<xsl:call-template name="equal"/>
						</BooleanQuery>
					</xsl:when>

					<!-- overlaps -->
					<xsl:when test="$expr/@relation=7">
						<BooleanQuery>
							<xsl:call-template name="overlaps"/>
						</BooleanQuery>
					</xsl:when>

					<!-- fullyOutsideOf -->
					<xsl:when test="$expr/@relation=10">
						<BooleanQuery>
							<xsl:call-template name="fullyOutsideOf"/>
						</BooleanQuery>
					</xsl:when>

					<!-- encloses -->
					<xsl:when test="$expr/@relation=9">
						<BooleanQuery>
							<xsl:call-template name="encloses"/>
						</BooleanQuery>
					</xsl:when>

					<!-- fullyEnclosedWithin -->
					<xsl:when test="$expr/@relation=8">
						<BooleanQuery>
							<xsl:call-template name="fullyEnclosedWithin"/>
						</BooleanQuery>
					</xsl:when>

				</xsl:choose>
			</xsl:if>

		</xsl:when>

	</xsl:choose>
</xsl:template>

<xsl:template name="dateTerm">
	<xsl:param name="date"/>
	<xsl:param name="field"/>
	<xsl:param name="relation"/>
	<xsl:param name="structure"/>

	<!-- trim date because we dont want time resolution till the uppercase / lowercase indexing is fixed in GN-->
	<xsl:variable name="trimdate" select="substring($date,0,11)"/>

	<xsl:variable name="future" select="'9999-99-99T99:99:99'"/>
	<xsl:variable name="past" select="'0001-01-01T00:00:00'"/>

		<xsl:choose>
			<xsl:when test="$relation=1"> <!-- less than -->
				<RangeQuery fld="{$field}" upperTxt="{$trimdate}" lowerTxt="{$past}" inclusive="false"/>
			</xsl:when>
			<xsl:when test="$relation=2"> <!-- less than or equal to -->
				<RangeQuery fld="{$field}" upperTxt="{$trimdate}" lowerTxt="{$past}" inclusive="true"/>
			</xsl:when>
			<xsl:when test="$relation=3"> <!-- equal to, we use no term query because it is case sensitive in GN -->
				<RangeQuery fld="{$field}" upperTxt="{$trimdate}" lowerTxt="{$trimdate}" inclusive="true"/>
			</xsl:when>
			<xsl:when test="$relation=4"> <!-- equal to or greater than -->
				<RangeQuery fld="{$field}" upperTxt="{$future}" lowerTxt="{$trimdate}"  inclusive="true"/>
			</xsl:when>
			<xsl:when test="$relation=5"> <!-- greater than -->
				<RangeQuery fld="{$field}" upperTxt="{$future}" lowerTxt="{$trimdate}" inclusive="false"/>
			</xsl:when>
		</xsl:choose>
</xsl:template>


<!-- we want XSLT 2.0 and tokenize functions -->
<xsl:template name="Multi2WordListTerm">
	<xsl:param name="expr"/>
	<xsl:param name="field1"/>
	<xsl:param name="field2"/>


	<BooleanQuery>

	<BooleanClause required="false" prohibited="false">
		<PhraseQuery>
		<xsl:call-template name="phraseQueryArgs">
			<xsl:with-param name="expr" select="$expr"/>
			<xsl:with-param name="field" select="$field1"/>
		</xsl:call-template>
		</PhraseQuery>
	</BooleanClause>

	<BooleanClause required="false" prohibited="false">
		<PhraseQuery>
		<xsl:call-template name="phraseQueryArgs">
			<xsl:with-param name="expr" select="$expr"/>
			<xsl:with-param name="field" select="$field2"/>
		</xsl:call-template>
		</PhraseQuery>
	</BooleanClause>

	</BooleanQuery>

</xsl:template>

<!-- we want XSLT 2.0 and tokenize functions -->
<xsl:template name="Multi3WordListTerm">
	<xsl:param name="expr"/>
	<xsl:param name="field1"/>
	<xsl:param name="field2"/>
	<xsl:param name="field3"/>

	<BooleanQuery>

	<BooleanClause required="false" prohibited="false">
		<PhraseQuery>
		<xsl:call-template name="phraseQueryArgs">
			<xsl:with-param name="expr" select="$expr"/>
			<xsl:with-param name="field" select="$field1"/>
		</xsl:call-template>
		</PhraseQuery>
	</BooleanClause>

	<BooleanClause required="false" prohibited="false">
		<PhraseQuery>
		<xsl:call-template name="phraseQueryArgs">
			<xsl:with-param name="expr" select="$expr"/>
			<xsl:with-param name="field" select="$field2"/>
		</xsl:call-template>
		</PhraseQuery>
	</BooleanClause>

	<BooleanClause required="false" prohibited="false">
		<PhraseQuery>
		<xsl:call-template name="phraseQueryArgs">
			<xsl:with-param name="expr" select="$expr"/>
			<xsl:with-param name="field" select="$field3"/>
		</xsl:call-template>
		</PhraseQuery>
	</BooleanClause>
	</BooleanQuery>

</xsl:template>

<xsl:template name="wordListTerm">
	<xsl:param name="expr"/>
	<xsl:param name="field"/>

	<PhraseQuery>
		<xsl:call-template name="phraseQueryArgs">
			<xsl:with-param name="expr" select="$expr"/>
			<xsl:with-param name="field" select="$field"/>
		</xsl:call-template>
	</PhraseQuery>
</xsl:template>

</xsl:stylesheet>
