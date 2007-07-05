<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
xmlns:xalan= "http://xml.apache.org/xalan" exclude-result-prefixes="xalan">

<!--
equal: coordinates of the target rectangle within 1 degree from corresponding ones of metadata rectangle
-->
<xsl:template name="equal">
	<BooleanClause required="true" prohibited="false">
		<RangeQuery fld="westBL"
						lowerTxt="{$westBL - 1}"
						upperTxt="{$westBL + 1}"
						inclusive="true"/>
	</BooleanClause>
	<BooleanClause required="true" prohibited="false">
		<RangeQuery fld="eastBL"
						lowerTxt="{$eastBL - 1}"
						upperTxt="{$eastBL + 1}"
						inclusive="true"/>
	</BooleanClause>
	<BooleanClause required="true" prohibited="false">
		<RangeQuery fld="southBL"
						lowerTxt="{$southBL - 1}"
						upperTxt="{$southBL + 1}"
						inclusive="true"/>
	</BooleanClause>
	<BooleanClause required="true" prohibited="false">
		<RangeQuery fld="northBL"
						lowerTxt="{$northBL - 1}"
						upperTxt="{$northBL + 1}"
						inclusive="true"/>
	</BooleanClause>
</xsl:template>

<!--
encloses: metadata rectangle encloses target rectangle shrunk by 1 degree
-->
<xsl:template name="encloses">
	<BooleanClause required="true" prohibited="false">
		<RangeQuery fld="westBL"
						lowerTxt="{-180 + 360}"
						upperTxt="{$westBL + 1}"
						inclusive="true"/>
	</BooleanClause>
	<BooleanClause required="true" prohibited="false">
		<RangeQuery fld="eastBL"
						lowerTxt="{$eastBL - 1}"
						upperTxt="{180 + 360}"
						inclusive="true"/>
	</BooleanClause>
	<BooleanClause required="true" prohibited="false">
		<RangeQuery fld="southBL"
						lowerTxt="{-90 + 360}"
						upperTxt="{$southBL + 1}"
						inclusive="true"/>
	</BooleanClause>
	<BooleanClause required="true" prohibited="false">
		<RangeQuery fld="northBL"
						lowerTxt="{$northBL - 1}"
						upperTxt="{90 + 360}"
						inclusive="true"/>
	</BooleanClause>
</xsl:template>

<!--
fullyEnclosedWithin: metadata rectangle fully enclosed within target rectangle augmented by 1 degree
-->
<xsl:template name="fullyEnclosedWithin">
	<BooleanClause required="true" prohibited="false">
		<RangeQuery fld="westBL"
						lowerTxt="{$westBL - 1}"
						upperTxt="{$eastBL + 1}"
						inclusive="true"/>
	</BooleanClause>
	<BooleanClause required="true" prohibited="false">
		<RangeQuery fld="eastBL"
						lowerTxt="{$westBL - 1}"
						upperTxt="{$eastBL + 1}"
						inclusive="true"/>
	</BooleanClause>
	<BooleanClause required="true" prohibited="false">
		<RangeQuery fld="southBL"
						lowerTxt="{$southBL - 1}"
						upperTxt="{$northBL + 1}"
						inclusive="true"/>
	</BooleanClause>
	<BooleanClause required="true" prohibited="false">
		<RangeQuery fld="northBL"
						lowerTxt="{$southBL - 1}"
						upperTxt="{$northBL + 1}"
						inclusive="true"/>
	</BooleanClause>
</xsl:template>

<!--
overlaps: not fullyOutsideOf
-->
<xsl:template name="overlaps">

	<!--
	new implementation: uses the equivalence
	
	-(a + b + c + d) = -a * -b * -c * -d
	-->
	<BooleanClause required="true" prohibited="false">
		<RangeQuery fld="eastBL"
						lowerTxt="{$westBL + 1}"
						upperTxt="{180 + 360}"
						inclusive="true"/>
	</BooleanClause>
	<BooleanClause required="true" prohibited="false">
		<RangeQuery fld="westBL"
						lowerTxt="{-180 + 360}"
						upperTxt="{$eastBL - 1}"
						inclusive="true"/>
	</BooleanClause>
	<BooleanClause required="true" prohibited="false">
		<RangeQuery fld="northBL"
						lowerTxt="{$southBL + 1}"
						upperTxt="{90 + 360}"
						inclusive="true"/>
	</BooleanClause>
	<BooleanClause required="true" prohibited="false">
		<RangeQuery fld="southBL"
						lowerTxt="{-90 + 360}"
						upperTxt="{$northBL - 1}"
						inclusive="true"/>
	</BooleanClause>
	
	<!--
	old implementation
	
	<BooleanClause required="false" prohibited="true">
		<BooleanQuery>
			<BooleanClause required="false" prohibited="false">
				<RangeQuery fld="eastBL"
								lowerTxt="{-180 + 360}"
								upperTxt="{$westBL + 1}"
								inclusive="true"/>
			</BooleanClause>
			<BooleanClause required="false" prohibited="false">
				<RangeQuery fld="westBL"
								lowerTxt="{$eastBL - 1}"
								upperTxt="{180 + 360}"
								inclusive="true"/>
			</BooleanClause>
			<BooleanClause required="false" prohibited="false">
				<RangeQuery fld="northBL"
								lowerTxt="{-90 + 360}"
								upperTxt="{$southBL + 1}"
								inclusive="true"/>
			</BooleanClause>
			<BooleanClause required="false" prohibited="false">
				<RangeQuery fld="southBL"
								lowerTxt="{$northBL - 1}"
								upperTxt="{90 + 360}"
								inclusive="true"/>
			</BooleanClause>
		</BooleanQuery>
	</BooleanClause>
	-->
</xsl:template>

<!--
fullyOutsideOf: one or more of the 4 forbidden halfplanes contains the metadata rectangle,
that is, not true that all the 4 forbidden halfplanes do not contain the metadata rectangle
-->
<xsl:template name="fullyOutsideOf">
	<BooleanClause required="true" prohibited="false">
		<BooleanQuery>
			<BooleanClause required="false" prohibited="false">
				<RangeQuery fld="eastBL"
								lowerTxt="{-180 + 360}"
								upperTxt="{$westBL + 1}"
								inclusive="true"/>
			</BooleanClause>
			<BooleanClause required="false" prohibited="false">
				<RangeQuery fld="westBL"
								lowerTxt="{$eastBL - 1}"
								upperTxt="{180 + 360}"
								inclusive="true"/>
			</BooleanClause>
			<BooleanClause required="false" prohibited="false">
				<RangeQuery fld="northBL"
								lowerTxt="{-90 + 360}"
								upperTxt="{$southBL + 1}"
								inclusive="true"/>
			</BooleanClause>
			<BooleanClause required="false" prohibited="false">
				<RangeQuery fld="southBL"
								lowerTxt="{$northBL - 1}"
								upperTxt="{90 + 360}"
								inclusive="true"/>
			</BooleanClause>
		</BooleanQuery>
	</BooleanClause>
</xsl:template>

<xsl:template name="orFields">
	<xsl:param name="expr"/>
	<xsl:param name="field"/>
	
	<xsl:if test="$expr!=''">
		<BooleanClause required="true" prohibited="false">
			<BooleanQuery>
				<xsl:for-each select="$expr">
					<BooleanClause required="false" prohibited="false">
						<TermQuery fld="{$field}" txt="{string(.)}"/>
					</BooleanClause>
				</xsl:for-each>
			</BooleanQuery>
		</BooleanClause>
	</xsl:if>
</xsl:template>

<!--
compiles a quoted string token into a phrase query
-->
<xsl:template name="phraseQueryArgs">
	<xsl:param name="expr"/>
	<xsl:param name="field"/>

	<xsl:variable name="nExpr" select="normalize-space($expr)"/>
	<xsl:variable name="first" select="substring-before($nExpr,' ')"/>
	<xsl:choose>
		<xsl:when test="$first">
			<Term fld="{$field}" txt="{$first}"/>
			<xsl:call-template name="phraseQueryArgs">
				<xsl:with-param name="expr" select="substring-after($nExpr,' ')"/>
				<xsl:with-param name="field" select="$field"/>
			</xsl:call-template>
		</xsl:when>
		<xsl:when test="$expr">
			<Term fld="{$field}" txt="{$nExpr}"/>
		</xsl:when>
	</xsl:choose>
</xsl:template>

</xsl:stylesheet>
