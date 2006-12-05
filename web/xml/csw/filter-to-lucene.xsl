<?xml version="1.0" encoding="UTF-8" ?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
										xmlns:ogc="http://www.opengis.net/ogc"
   									xmlns:gml="http://www.opengis.net/gml"
										exclude-result-prefixes="ogc gml">

	<!-- ========================================================================== -->
	<!-- === Property operators : =, <>, <, <=, >, >=, Like, Between === -->
	<!-- ========================================================================== -->

	<xsl:template match="ogc:PropertyIsEqualTo">
		<xsl:choose>
			<xsl:when test="ogc:PropertyName and ogc:Literal">
				<TermQuery fld="{ogc:PropertyName}" txt="{ogc:Literal}"/>
			</xsl:when>
			<xsl:otherwise>
				<error type="Unkown content of expression">
					<xsl:copy-of select="."/>
				</error>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- ========================================================================== -->

	<xsl:template match="ogc:PropertyIsNotEqualTo">
		<xsl:choose>
			<xsl:when test="ogc:PropertyName and ogc:Literal">
				<BooleanQuery>
					<BooleanClause required="true" prohibited="false">
						<WildcardQuery fld="any" txt="*"/>
					</BooleanClause>
					<BooleanClause required="false" prohibited="true">
						<TermQuery fld="{ogc:PropertyName}" txt="{ogc:Literal}"/>
					</BooleanClause>
				</BooleanQuery>
			</xsl:when>
			<xsl:otherwise>
				<error type="Unkown content of expression">
					<xsl:copy-of select="."/>
				</error>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- ========================================================================== -->

	<xsl:template match="ogc:PropertyIsLessThan">
		<xsl:choose>
			<xsl:when test="ogc:PropertyName and ogc:Literal">
				<RangeQuery fld="{ogc:PropertyName}" upperTxt="{ogc:Literal}" inclusive="false"/>
			</xsl:when>
			<xsl:otherwise>
				<error type="Unkown content of expression">
					<xsl:copy-of select="."/>
				</error>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- ========================================================================== -->

	<xsl:template match="ogc:PropertyIsLessThanOrEqualTo">
		<xsl:choose>
			<xsl:when test="ogc:PropertyName and ogc:Literal">
				<RangeQuery fld="{ogc:PropertyName}" upperTxt="{ogc:Literal}" inclusive="true"/>
			</xsl:when>
			<xsl:otherwise>
				<error type="Unkown content of expression">
					<xsl:copy-of select="."/>
				</error>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- ========================================================================== -->

	<xsl:template match="ogc:PropertyIsGreaterThan">
		<xsl:choose>
			<xsl:when test="ogc:PropertyName and ogc:Literal">
				<RangeQuery fld="{ogc:PropertyName}" lowerTxt="{ogc:Literal}" inclusive="false"/>
			</xsl:when>
			<xsl:otherwise>
				<error type="Unkown content of expression">
					<xsl:copy-of select="."/>
				</error>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- ========================================================================== -->

	<xsl:template match="ogc:PropertyIsGreaterThanOrEqualTo">
		<xsl:choose>
			<xsl:when test="ogc:PropertyName and ogc:Literal">
				<RangeQuery fld="{ogc:PropertyName}" lowerTxt="{ogc:Literal}" inclusive="true"/>
			</xsl:when>
			<xsl:otherwise>
				<error type="Unkown content of expression">
					<xsl:copy-of select="."/>
				</error>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- ========================================================================== -->

	<xsl:template match="ogc:PropertyIsLike">
		<xsl:choose>
			<xsl:when test="ogc:PropertyName and ogc:Literal">
				<WildcardQuery fld="{ogc:PropertyName}" txt="{translate(translate(ogc:Literal, @wildCard, '*'), @singleChar, '?')}"/>
			</xsl:when>
			<xsl:otherwise>
				<error type="Unkown content of expression">
					<xsl:copy-of select="."/>
				</error>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- ========================================================================== -->

	<xsl:template match="ogc:PropertyIsBetween">
		<xsl:choose>
			<xsl:when test="ogc:PropertyName and ogc:LowerBoundary/ogc:Literal and ogc:UpperBoundary/ogc:Literal">
				<RangeQuery fld="{ogc:PropertyName}" lowerTxt="{ogc:LowerBoundary/ogc:Literal}" upperTxt="{ogc:UpperBoundary/ogc:Literal}" inclusive="true"/>
			</xsl:when>
			<xsl:otherwise>
				<error type="Unkown content of expression">
					<xsl:copy-of select="."/>
				</error>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- ========================================================================== -->

	<xsl:template match="ogc:PropertyIsNull">
		<error type="Operator not implemented">
			<xsl:copy-of select="."/>
		</error>
	</xsl:template>

	<!-- ========================================================================== -->
	<!-- === Logic operators : AND, OR, NOT === -->
	<!-- ========================================================================== -->

	<xsl:template match="ogc:And">
		<BooleanQuery>
			<xsl:for-each select="*">
				<BooleanClause required="true" prohibited="false">
					<xsl:apply-templates select="." />
				</BooleanClause>
			</xsl:for-each>
		</BooleanQuery>
	</xsl:template>

	<!-- ========================================================================== -->

	<xsl:template match="ogc:Or">
		<BooleanQuery>
			<xsl:for-each select="*">
				<BooleanClause required="false" prohibited="false">
					<xsl:apply-templates select="." />
				</BooleanClause>
			</xsl:for-each>
		</BooleanQuery>
	</xsl:template>

	<!-- ========================================================================== -->

	<xsl:template match="ogc:Not">
		<BooleanQuery>
			<BooleanClause required="true" prohibited="false">
				<WildcardQuery fld="any" txt="*"/>
			</BooleanClause>

			<xsl:for-each select="*">
				<BooleanClause required="false" prohibited="true">
					<xsl:apply-templates select="." />
				</BooleanClause>
			</xsl:for-each>
		</BooleanQuery>
	</xsl:template>

	<!-- ========================================================================== -->
	<!-- === Spatial operators : BBOX === -->
	<!-- ========================================================================== -->

	<xsl:template match="ogc:BBOX">
		<xsl:variable name="lower" select="gml:Envelope/gml:lowerCorner"/>
		<xsl:variable name="upper" select="gml:Envelope/gml:upperCorner"/>

		<xsl:variable name="northBL" select="substring-after($upper, ' ')  + 360"/>
		<xsl:variable name="southBL" select="substring-after($lower, ' ')  + 360"/>
		<xsl:variable name="eastBL"  select="substring-before($upper, ' ') + 360"/>
		<xsl:variable name="westBL"  select="substring-before($lower, ' ') + 360"/>

		<xsl:choose>
			<!-- A better test should be done by java code -->
			<xsl:when test="not (contains(ogc:PropertyName, 'ows:BoundingBox'))">
				<error type="The queried property is not spatial">
					<xsl:copy-of select="."/>
				</error>
			</xsl:when>

			<xsl:otherwise>
				<!-- overlaps test : BBOX = not disjoint -->
		
				<BooleanQuery>
					<BooleanClause required="true" prohibited="false">
						<RangeQuery fld="eastBL" lowerTxt="{$westBL + 1}" upperTxt="{180 + 360}" inclusive="true"/>
					</BooleanClause>
		
					<BooleanClause required="true" prohibited="false">
						<RangeQuery fld="westBL" lowerTxt="{-180 + 360}" upperTxt="{$eastBL - 1}" inclusive="true"/>
					</BooleanClause>
		
					<BooleanClause required="true" prohibited="false">
						<RangeQuery fld="northBL" lowerTxt="{$southBL + 1}" upperTxt="{90 + 360}" inclusive="true"/>
					</BooleanClause>
		
					<BooleanClause required="true" prohibited="false">
						<RangeQuery fld="southBL" lowerTxt="{-90 + 360}" upperTxt="{$northBL - 1}" inclusive="true"/>
					</BooleanClause>
				</BooleanQuery>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- ========================================================================== -->

	<xsl:template match="ogc:Equals|ogc:Disjoint|ogc:Touches|ogc:Within|ogc:Overlaps|ogc:Crosses|ogc:Intersects|ogc:Contains|ogc:DWithin|ogc:Beyond">
		<error type="Operator not implemented">
			<xsl:copy-of select="."/>
		</error>
	</xsl:template>

	<!-- ========================================================================== -->

	<xsl:template match="*">
		<xsl:apply-templates select="*"/>
	</xsl:template>

	<!-- ============================================================================= -->

</xsl:stylesheet>
