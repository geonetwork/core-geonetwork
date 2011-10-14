<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
							xmlns:ogc="http://www.opengis.net/ogc"
   							xmlns:gml="http://www.opengis.net/gml"
							exclude-result-prefixes="ogc gml">

	<!-- ========================================================================== -->
	<!-- === Property operators : =, <>, <, <=, >, >=, Like, Between === -->
	<!-- ========================================================================== -->

	<xsl:template match="ogc:PropertyIsEqualTo">
		<xsl:variable name="sibling" select="preceding-sibling::ogc:PropertyIsEqualTo[ogc:PropertyName='similarity']" />

        <xsl:variable name="currentSimilarity">
            <xsl:choose>
                <xsl:when test="count($sibling) = 0">
	                <xsl:value-of select="1"/><!-- Change this value to enable default FuzzySearch (eg. 0.8). -->
                </xsl:when>
                <xsl:otherwise>
     	            <xsl:value-of
                    select="$sibling[count($sibling)]/ogc:Literal"/>
                </xsl:otherwise>
             </xsl:choose>
        </xsl:variable>
         
        <xsl:choose>
			<!-- we cannot check ogc:PropertyName because it can be null to search for
              any property -->
			<xsl:when test="ogc:Literal and $currentSimilarity &gt;= 1.0">
				<TermQuery fld="{ogc:PropertyName}" txt="{ogc:Literal}"/>
			</xsl:when>
			<xsl:when test="ogc:Literal">
				<FuzzyQuery fld="{ogc:PropertyName}" txt="{ogc:Literal}" sim="{$currentSimilarity}"/>
			</xsl:when>
			<xsl:otherwise>
				<error type="Unknown content of expression">
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
						<!--WildcardQuery fld="any" txt="*"/-->
                        <MatchAllDocsQuery required="true" prohibited="false"/>
					</BooleanClause>
					<BooleanClause required="false" prohibited="true">
						<TermQuery fld="{ogc:PropertyName}" txt="{ogc:Literal}"/>
					</BooleanClause>
				</BooleanQuery>
			</xsl:when>
			<xsl:otherwise>
				<error type="Unknown content of expression">
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
				<error type="Unknown content of expression">
					<xsl:copy-of select="."/>
				</error>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- ========================================================================== -->

	<!-- Filter spec says ogc:PropertyIsLessThanOrEqualTo, OGC CSW schema and 
	     GetCapabilties says ogc:PropertyIsLessThanEqualTo so we'd better 
			 support both -->
	<xsl:template match="ogc:PropertyIsLessThanOrEqualTo|ogc:PropertyIsLessThanEqualTo">
		<xsl:choose>
			<xsl:when test="ogc:PropertyName and ogc:Literal">
				<RangeQuery fld="{ogc:PropertyName}" upperTxt="{ogc:Literal}" inclusive="true"/>
			</xsl:when>
			<xsl:otherwise>
				<error type="Unknown content of expression">
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
				<error type="Unknown content of expression">
					<xsl:copy-of select="."/>
				</error>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- ========================================================================== -->

	<!-- Filter spec says ogc:PropertyIsGreaterThanOrEqualTo, OGC CSW schema and 
	     GetCapabilties says ogc:PropertyIsGreaterThanEqualTo so we'd better 
			 support both -->
	<xsl:template match="ogc:PropertyIsGreaterThanOrEqualTo|ogc:PropertyIsGreaterThanEqualTo">
		<xsl:choose>
			<xsl:when test="ogc:PropertyName and ogc:Literal">
				<RangeQuery fld="{ogc:PropertyName}" lowerTxt="{ogc:Literal}" inclusive="true"/>
			</xsl:when>
			<xsl:otherwise>
				<error type="Unknown content of expression">
					<xsl:copy-of select="."/>
				</error>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- ========================================================================== -->

	<xsl:template match="ogc:PropertyIsLike">
		<xsl:choose>
			<!-- If search for all, nothing (empty query) is faster than WildcardQuery or MatchAllDocsQuery. -->
			<xsl:when test="ogc:PropertyName and ogc:Literal=@wildCard">
			</xsl:when>
            
			<!-- Lucene supports single and multiple character wildcard searches within
                 single terms (not within phrase queries). -->
            <xsl:when test="ogc:PropertyName and ogc:Literal">
                <xsl:variable name="pn" select="ogc:PropertyName" />
                <xsl:variable name="wc" select="@wildCard" />
                <xsl:variable name="sc" select="@singleChar" />

                <BooleanQuery>
                    <xsl:for-each select="tokenize(ogc:Literal, ' ')">
                        <xsl:variable name="token" select="." />
                        <xsl:variable name="ol" select="translate(translate($token, $wc, '*'), $sc, '?')" />
                        <BooleanClause required="true" prohibited="false">
                            <WildcardQuery fld="{$pn}" txt="{$ol}"/>
                        </BooleanClause>
                    </xsl:for-each>
                </BooleanQuery>
            </xsl:when>
            
			<xsl:otherwise>
				<error type="Unknown content of expression">
					<xsl:copy-of select="."/>
				</error>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- ========================================================================== -->

	<xsl:template match="ogc:PropertyIsBetween">
		<xsl:choose>
			<xsl:when
				test="ogc:PropertyName and ogc:LowerBoundary/ogc:Literal and ogc:UpperBoundary/ogc:Literal">
				<RangeQuery fld="{ogc:PropertyName}" lowerTxt="{ogc:LowerBoundary/ogc:Literal}"
					upperTxt="{ogc:UpperBoundary/ogc:Literal}" inclusive="true"/>
			</xsl:when>
			<xsl:otherwise>
				<error type="Unknown content of expression">
					<xsl:copy-of select="."/>
				</error>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- ========================================================================== -->

	<xsl:template match="ogc:PropertyIsNull">
        <BooleanQuery>
          <BooleanClause required="true" prohibited="false">
            <MatchAllDocsQuery required="true" prohibited="false"/>
          </BooleanClause>

          <BooleanClause required="false" prohibited="true">
            <WildcardQuery fld="{ogc:PropertyName}" txt="*"/>
          </BooleanClause>
        </BooleanQuery>
    </xsl:template>

	<!-- ========================================================================== -->
	<!-- === Logic operators : AND, OR, NOT === -->
	<!-- ========================================================================== -->

	<xsl:template match="ogc:And">
		<BooleanQuery>
			<xsl:for-each
				select="*[not(ogc:PropertyName)]|*[ogc:PropertyName!='similarity' and 
				ogc:PropertyName!='group']">
				<BooleanClause required="true" prohibited="false">
					<xsl:apply-templates select="."/>
				</BooleanClause>
			</xsl:for-each>
		</BooleanQuery>
	</xsl:template>

	<!-- ========================================================================== -->

	<xsl:template match="ogc:Or">
		<BooleanQuery>
			<xsl:for-each
				select="*[not(ogc:PropertyName)]|*[ogc:PropertyName!='similarity' and 
				ogc:PropertyName!='group']">
				<BooleanClause required="false" prohibited="false">
					<xsl:apply-templates select="."/>
				</BooleanClause>
			</xsl:for-each>
		</BooleanQuery>
	</xsl:template>

	<!-- ========================================================================== -->

	<xsl:template match="ogc:Not[not(ogc:Disjoint|ogc:Within|ogc:Equals|ogc:Touches|ogc:Overlaps|ogc:Crosses|ogc:Intersects|ogc:Contains|ogc:DWithin|ogc:Beyond|ogc:BBOX)]">
		<BooleanQuery>
			<BooleanClause required="true" prohibited="false">
				<!--WildcardQuery fld="any" txt="*"/-->
				<MatchAllDocsQuery required="true" prohibited="false"/>
			</BooleanClause>

			<xsl:for-each
				select="*[not(ogc:PropertyName)]|*[ogc:PropertyName!='similarity' and 
				ogc:PropertyName!='group']">
				<BooleanClause required="false" prohibited="true">
					<xsl:apply-templates select="."/>
				</BooleanClause>
			</xsl:for-each>
		</BooleanQuery>
	</xsl:template>

	<!-- ========================================================================== -->
	<!-- === Spatial operators : BBOX === -->
	<!-- ========================================================================== -->
	<!--

	<xsl:template match="ogc:BBOX">
		<xsl:variable name="lower" select="gml:Envelope/gml:lowerCorner"/>
		<xsl:variable name="upper" select="gml:Envelope/gml:upperCorner"/>

		<xsl:variable name="northBL" select="substring-before($upper, ' ') + 360"/>
		<xsl:variable name="eastBL"  select="substring-after($upper,  ' ') + 360"/>
		<xsl:variable name="southBL" select="substring-before($lower, ' ') + 360"/>
		<xsl:variable name="westBL"  select="substring-after($lower,  ' ') + 360"/>

		<xsl:choose>
			 A better test should be done by java code
			<xsl:when test="not (contains(ogc:PropertyName, 'ows:BoundingBox'))">
				<error type="The queried property is not spatial">
					<xsl:copy-of select="."/>
				</error>
			</xsl:when>

			<xsl:otherwise>
				 overlaps test : BBOX = not disjoint
		
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

	-->
	<!-- ========================================================================== -->

	<xsl:template
		match="ogc:BBOX|ogc:Equals|ogc:Disjoint|ogc:Touches|ogc:Within|ogc:Overlaps|ogc:Crosses|ogc:Intersects|ogc:Contains|ogc:DWithin|ogc:Beyond">
		<MatchAllDocsQuery/>
		<!--<error type="Operator not implemented">
			<xsl:copy-of select="."/>
		</error>-->
	</xsl:template>

	<!-- ========================================================================== -->
	<!-- Template based on group and privileges to search for 
			* all records visible for one group (group)
			* all records created in one group (_groupOwner)
			* all records created by one user (_owner)
	-->
	<!-- Privilege variables stored in the index use for group access -->
	<xsl:variable name="opView" select="'_op0'"/>
	<xsl:variable name="opDownload" select="'_op1'"/>
	<xsl:variable name="opDynamic" select="'_op5'"/>
	<xsl:variable name="opFeatured" select="'_op6'"/>

	<xsl:variable name="internetGp" select="'1'"/>


	<xsl:template match="ogc:PropertyIsEqualTo[ogc:PropertyName='group']" priority="2">
		<TermQuery fld="{$opView}" txt="{ogc:Literal}"/>
	</xsl:template>

	<!-- Do not allow FuzzyQuery for those fields -->
	<xsl:template
		match="ogc:PropertyIsEqualTo[ogc:PropertyName='_groupOwner' or
									ogc:PropertyName='_owner' or
									ogc:PropertyName='_validsch' or
									ogc:PropertyName='_validxsd' or
									ogc:PropertyName='_isTemplate' or
									ogc:PropertyName='_isHarvested' or
									ogc:PropertyName='_valid' or
									ogc:PropertyName='_visibleForOwnerOnly']"
		priority="2">
		<TermQuery fld="{ogc:PropertyName}" txt="{ogc:Literal}"/>
	</xsl:template>

	<!-- An empty filter means return all -->
	<xsl:template match="ogc:Filter[count(*)=0]">
		<TermQuery fld="_isTemplate" txt="n"/>
	</xsl:template>
	
	<!-- ========================================================================== -->

	<xsl:template match="*">
		<xsl:choose>
			<!-- Applied default criteria to exclude template from results -->
			<xsl:when test="not(//*[ogc:PropertyName='_isTemplate'])">
				<xsl:call-template name="filterTemplate"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:apply-templates select="*"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- ========================================================================== -->
	<!-- === Filter on isTemplate field, allows CSW search on that criteria === -->
	<!-- ========================================================================== -->
	<!-- -->
	<xsl:template name="filterTemplate">
		<BooleanQuery>
			<BooleanClause required="true" prohibited="false">
				<xsl:apply-templates select="*"/>
			</BooleanClause>
			<BooleanClause required="true" prohibited="false">
				<TermQuery fld="_isTemplate" txt="n"/>
			</BooleanClause>
		</BooleanQuery>
	</xsl:template>

	<!-- ============================================================================= -->

</xsl:stylesheet>
