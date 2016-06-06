<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:exslt="http://exslt.org/common"
                version="1.0" exclude-result-prefixes="exslt">

  <xsl:import href="parser.xsl"/>
  <xsl:import href="lucene-utils.xsl"/>

  <xsl:variable name="opView" select="'_op0'"/>
  <xsl:variable name="opDownload" select="'_op1'"/>
  <xsl:variable name="opDynamic" select="'_op5'"/>
  <xsl:variable name="opFeatured" select="'_op6'"/>

  <xsl:variable name="similarity" select="/request/similarity"/>

  <!--
  computes bounding box values
  -->
  <xsl:variable name="region" select="string(/request/region)"/>
  <xsl:variable name="regionData" select="/request/regions/*[string(id)=$region]"/>

  <xsl:variable name="westBL">
    <xsl:choose>
      <xsl:when test="$region">
        <xsl:value-of select="$regionData/west"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="/request/westBL"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <xsl:variable name="eastBL">
    <xsl:choose>
      <xsl:when test="$region">
        <xsl:value-of select="$regionData/east"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="/request/eastBL"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <xsl:variable name="southBL">
    <xsl:choose>
      <xsl:when test="$region">
        <xsl:value-of select="$regionData/south"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="/request/southBL"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <xsl:variable name="northBL">
    <xsl:choose>
      <xsl:when test="$region">
        <xsl:value-of select="$regionData/north"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="/request/northBL"/>
      </xsl:otherwise>
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

      <!-- type -->
      <xsl:call-template name="textField">
        <xsl:with-param name="expr" select="/request/type"/>
        <xsl:with-param name="field" select="'type'"/>
      </xsl:call-template>

      <!-- schema -->
      <xsl:call-template name="textField">
        <xsl:with-param name="expr" select="/request/_schema"/>
        <xsl:with-param name="field" select="'_schema'"/>
      </xsl:call-template>

      <!-- operates On -->
      <xsl:call-template name="textField">
        <xsl:with-param name="expr" select="/request/operatesOn"/>
        <xsl:with-param name="field" select="'operatesOn'"/>
      </xsl:call-template>

      <!-- parent UUID -->
      <xsl:call-template name="textField">
        <xsl:with-param name="expr" select="/request/parentUuid"/>
        <xsl:with-param name="field" select="'parentUuid'"/>
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

      <!-- phrase  -->
      <xsl:if test="string(/request/phrase) != ''">
        <xsl:if test="string(.) != '' ">
          <BooleanClause prohibited="false" required="true">
            <xsl:call-template name="phraseQuery">
              <xsl:with-param name="expr" select="/request/phrase"/>
              <xsl:with-param name="field" select="'any'"/>
            </xsl:call-template>
          </BooleanClause>
        </xsl:if>
      </xsl:if>

      <!-- or -->
      <xsl:call-template name="notRequiredTextField">
        <xsl:with-param name="expr" select="/request/or"/>
        <xsl:with-param name="field" select="'any'"/>
      </xsl:call-template>

      <!-- without -->
      <xsl:call-template name="notAllowedTextField">
        <xsl:with-param name="expr" select="/request/without"/>
        <xsl:with-param name="field" select="'any'"/>
      </xsl:call-template>

      <!-- download - - - - - - - - - - - - - - - - - - - - - - - - - -->

      <xsl:if test="string(/request/download)='on'">
        <BooleanClause required="true" prohibited="false">
          <BooleanQuery>
            <BooleanClause required="false" prohibited="false">
              <WildcardQuery fld="protocol" txt="WWW:DOWNLOAD-*--download"/>
            </BooleanClause>
          </BooleanQuery>
        </BooleanClause>
      </xsl:if>

      <!-- dynamic - - - - - - - - - - - - - - - - - - - - - - - - - -->

      <xsl:if test="string(/request/dynamic)='on'">
        <BooleanClause required="true" prohibited="false">
          <BooleanQuery>
            <BooleanClause required="false" prohibited="false">
              <WildcardQuery fld="protocol" txt="OGC:WMS-*-get-map"/>
            </BooleanClause>

            <BooleanClause required="false" prohibited="false">
              <WildcardQuery fld="protocol" txt="OGC:WMS-*-get-capabilities"/>
            </BooleanClause>

            <BooleanClause required="false" prohibited="false">
              <WildcardQuery fld="protocol" txt="ESRI:AIMS-*-get-image"/>
            </BooleanClause>
          </BooleanQuery>
        </BooleanClause>
      </xsl:if>

      <!-- generic protocol searching - - - - - - - - - - - - - - - - - -->

      <xsl:if test="string(/request/protocol) !=''">
        <BooleanClause required="true" prohibited="false">
          <TermQuery fld="protocol" txt="{/request/protocol}"/>
        </BooleanClause>
      </xsl:if>

      <!-- bounding box - - - - - - - - - - - - - - - - - - - - - - - - -->

      <xsl:if
        test="$northBL != 'NaN' and $southBL != 'NaN' and $eastBL != 'NaN' and $westBL != 'NaN'">
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

      <!-- topic category / only iso records -->
      <xsl:call-template name="textField">
        <xsl:with-param name="expr" select="/request/topicCat"/>
        <xsl:with-param name="field" select="'topicCat'"/>
      </xsl:call-template>


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

      <!-- uuid -->
      <xsl:call-template name="textField">
        <xsl:with-param name="expr" select="/request/uuid"/>
        <xsl:with-param name="field" select="'_uuid'"/>
      </xsl:call-template>

      <!-- parent uuid -->
      <xsl:call-template name="textField">
        <xsl:with-param name="expr" select="/request/parentUuid"/>
        <xsl:with-param name="field" select="'parentUuid'"/>
      </xsl:call-template>

      <!-- operates on (identify datasets link to services) -->
      <xsl:call-template name="textField">
        <xsl:with-param name="expr" select="/request/operatesOn"/>
        <xsl:with-param name="field" select="'operatesOn'"/>
      </xsl:call-template>

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

      <!-- date range search -->

      <xsl:if test="/request/dateFrom or /request/dateTo">
        <BooleanClause required="true" prohibited="false">
          <RangeQuery fld="_changeDate" inclusive="true">
            <xsl:if test="/request/dateFrom">
              <xsl:attribute name="lowerTxt">
                <xsl:value-of select="/request/dateFrom"/>
              </xsl:attribute>
            </xsl:if>

            <xsl:if test="/request/dateTo">
              <xsl:attribute name="upperTxt">
                <xsl:value-of select="/request/dateTo"/>

                <!-- while the 'from' parameter can be short (like yyyy-mm-dd)
                                    the 'until' parameter must be long to match -->

                <xsl:if test="string-length(/request/dateTo) = 10">
                  <xsl:text>T23:59:59</xsl:text>
                </xsl:if>
              </xsl:attribute>
            </xsl:if>
          </RangeQuery>
        </BooleanClause>
      </xsl:if>

      <!-- date range search - finds records where temporal extent overlaps
               the search extent -->

      <xsl:if test="(/request/extFrom or /request/extTo)">
        <BooleanClause required="true" prohibited="false">
          <BooleanQuery>
            <!-- temporal extent start is within search extent -->
            <BooleanClause required="false" prohibited="false">
              <RangeQuery fld="tempExtentBegin" inclusive="true">
                <xsl:if test="/request/extFrom">
                  <xsl:attribute name="lowerTxt">
                    <xsl:value-of select="/request/extFrom"/>
                  </xsl:attribute>
                </xsl:if>
                <xsl:if test="/request/extTo">
                  <xsl:attribute name="upperTxt">
                    <xsl:value-of select="/request/extTo"/>
                  </xsl:attribute>
                </xsl:if>
              </RangeQuery>
            </BooleanClause>
            <!-- or temporal extent end is within search extent -->
            <BooleanClause required="false" prohibited="false">
              <RangeQuery fld="tempExtentEnd" inclusive="true">
                <xsl:if test="/request/extFrom">
                  <xsl:attribute name="lowerTxt">
                    <xsl:value-of select="/request/extFrom"/>
                  </xsl:attribute>
                </xsl:if>
                <xsl:if test="/request/extTo">
                  <xsl:attribute name="upperTxt">
                    <xsl:value-of select="/request/extTo"/>
                  </xsl:attribute>
                </xsl:if>
              </RangeQuery>
            </BooleanClause>
            <!-- or temporal extent contains search extent -->
            <xsl:if test="/request/extTo and /request/extFrom">
              <BooleanClause required="false" prohibited="false">
                <BooleanQuery>
                  <BooleanClause required="true" prohibited="false">
                    <RangeQuery fld="tempExtentEnd" inclusive="true">
                      <xsl:attribute name="lowerTxt">
                        <xsl:value-of select="/request/extTo"/>
                      </xsl:attribute>
                    </RangeQuery>
                  </BooleanClause>
                  <BooleanClause required="true" prohibited="false">
                    <RangeQuery fld="tempExtentBegin" inclusive="true">
                      <xsl:attribute name="upperTxt">
                        <xsl:value-of select="/request/extFrom"/>
                      </xsl:attribute>
                    </RangeQuery>
                  </BooleanClause>
                </BooleanQuery>
              </BooleanClause>
            </xsl:if>
          </BooleanQuery>
        </BooleanClause>
      </xsl:if>

    </BooleanQuery>
  </xsl:template>

  <!-- ================================================================================ -->

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

  <xsl:template name="phraseQuery">
    <xsl:param name="expr"/>
    <xsl:param name="field"/>
    <PhraseQuery>
      <xsl:call-template name="phraseQueryArgs">
        <xsl:with-param name="expr" select="$expr"/>
        <xsl:with-param name="field" select="$field"/>
      </xsl:call-template>
    </PhraseQuery>
  </xsl:template>


  <!-- ================================================================================ -->
  <xsl:template name="notRequiredTextFieldArgs">
    <xsl:param name="expr"/>
    <xsl:param name="field"/>
    <xsl:variable name="nExpr" select="normalize-space($expr)"/>
    <xsl:variable name="first" select="substring-before($nExpr,' ')"/>
    <xsl:choose>
      <xsl:when test="$first">
        <BooleanClause required="false" prohibited="false">
          <TermQuery fld="{$field}" txt="{$first}"/>
        </BooleanClause>
        <xsl:call-template name="notRequiredTextFieldArgs">
          <xsl:with-param name="expr" select="substring-after($nExpr,' ')"/>
          <xsl:with-param name="field" select="$field"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="$expr">
        <BooleanClause required="false" prohibited="false">
          <TermQuery fld="{$field}" txt="{$nExpr}"/>
        </BooleanClause>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="notRequiredTextField">
    <xsl:param name="expr"/>
    <xsl:param name="field"/>

    <xsl:if test="$expr!=''">
      <BooleanClause required="true" prohibited="false">
        <BooleanQuery>
          <xsl:call-template name="notRequiredTextFieldArgs">
            <xsl:with-param name="expr" select="$expr"/>
            <xsl:with-param name="field" select="$field"/>
          </xsl:call-template>
        </BooleanQuery>
      </BooleanClause>
    </xsl:if>
  </xsl:template>
  <xsl:template name="notAllowedTextFieldArgs">
    <xsl:param name="expr"/>
    <xsl:param name="field"/>
    <xsl:variable name="nExpr" select="normalize-space($expr)"/>
    <xsl:variable name="first" select="substring-before($nExpr,' ')"/>
    <xsl:choose>
      <xsl:when test="$first">
        <BooleanClause required="false" prohibited="true">
          <TermQuery fld="{$field}" txt="{$first}"/>
        </BooleanClause>
        <xsl:call-template name="notAllowedTextFieldArgs">
          <xsl:with-param name="expr" select="substring-after($nExpr,' ')"/>
          <xsl:with-param name="field" select="$field"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="$expr">
        <BooleanClause required="false" prohibited="true">
          <TermQuery fld="{$field}" txt="{$nExpr}"/>
        </BooleanClause>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="notAllowedTextField">
    <xsl:param name="expr"/>
    <xsl:param name="field"/>

    <xsl:if test="$expr!=''">
      <BooleanClause required="true" prohibited="false">
        <BooleanQuery>
          <BooleanClause required="true" prohibited="false">
            <MatchAllDocsQuery required="true" prohibited="false">
            </MatchAllDocsQuery>
          </BooleanClause>
          <xsl:call-template name="notAllowedTextFieldArgs">
            <xsl:with-param name="expr" select="$expr"/>
            <xsl:with-param name="field" select="$field"/>
          </xsl:call-template>

        </BooleanQuery>
      </BooleanClause>
    </xsl:if>
  </xsl:template>
  <!-- ================================================================================ -->
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
      <xsl:with-param name="expr" select="exslt:node-set($tree)/*"/>
      <xsl:with-param name="field" select="$field"/>
    </xsl:call-template>
  </xsl:template>

  <!-- ================================================================================ -->
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
