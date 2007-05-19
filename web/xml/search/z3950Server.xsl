<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
xmlns:xalan= "http://xml.apache.org/xalan" exclude-result-prefixes="xalan">

<xsl:import href="parser.xsl"/>
<xsl:import href="lucene-utils.xsl"/>

<xsl:variable name="opView"     select="'_op0'"/>

<!--
computes bounding box values
-->
<xsl:variable name="boundingBox" select="/request/query//term[@use='2060']/text()"/>
<xsl:variable name="northBL"     select="substring-before($boundingBox,' ') + 360"/>
<xsl:variable name="rest1"       select="substring-after($boundingBox,' ')"/>
<xsl:variable name="westBL"      select="substring-before($rest1,' ')       + 360"/>
<xsl:variable name="rest2"       select="substring-after($rest1,' ')"/>
<xsl:variable name="southBL"     select="substring-before($rest2,' ')       + 360"/>
<xsl:variable name="eastBL"      select="substring-after($rest2,' ')        + 360"/>

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
			<xsl:with-param name="expr" select="/request/group"/>
			<xsl:with-param name="field" select="$opView"/>
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
		<xsl:with-param name="expr" select="xalan:nodeset($expr)"/>
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

		<!-- title -->
		<xsl:when test="name($expr)='term' and $expr/@use='4'">
			<xsl:call-template name="wordListTerm">
				<xsl:with-param name="expr" select="$expr/text()"/>
				<xsl:with-param name="field" select="'title'"/>
			</xsl:call-template>
		</xsl:when>

		<!-- abstract -->
		<xsl:when test="name($expr)='term' and $expr/@use='62'">
			<xsl:call-template name="wordListTerm">
				<xsl:with-param name="expr" select="$expr/text()"/>
				<xsl:with-param name="field" select="'abstract'"/>
			</xsl:call-template>
		</xsl:when>

		<!-- any -->
		<xsl:when test="name($expr)='term' and $expr/@use='1016'">
			<xsl:call-template name="wordListTerm">
				<xsl:with-param name="expr" select="$expr/text()"/>
				<xsl:with-param name="field" select="'any'"/>
			</xsl:call-template>
		</xsl:when>

		<!-- keywords -->
		<xsl:when test="name($expr)='term' and $expr/@use='2002'">
			<TermQuery fld="keyword" txt="{$expr/text()}"/>
		</xsl:when>

		<!-- bounding box -->
		<xsl:when test="name($expr)='term' and $expr/@use='2060'">

			<!-- bounding box -->
			<xsl:if test="$boundingBox">
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
