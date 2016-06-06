<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet xmlns:sld="http://www.opengis.net/sld" xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:ogc="http://www.opengis.net/ogc"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:math="http://exslt.org/math"
                version="2.0"
                xmlns="http://www.isotc211.org/2005/gmd"
                extension-element-prefixes="math ogc">

  <!-- ============================================================================= -->

  <xsl:key name="prop" match="//ogc:PropertyName" use="."/>

  <!-- ============================================================================= -->

  <xsl:template match="*" mode="DataIdentification">

    <citation>
      <CI_Citation>
        <title>
          <gco:CharacterString>
            <xsl:value-of select="sld:NamedLayer/sld:UserStyle/sld:Title"/>
          </gco:CharacterString>
        </title>
      </CI_Citation>
    </citation>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <abstract>
      <gco:CharacterString>
        <xsl:value-of select="sld:NamedLayer/sld:UserStyle/sld:Abstract"/>
      </gco:CharacterString>
    </abstract>

    <!--idPurp-->
    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <status>
      <MD_ProgressCode codeList="./resources/codeList.xml#MD_ProgressCode"
                       codeListValue="completed"/>
    </status>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <descriptiveKeywords>
      <MD_Keywords>
        <xsl:for-each select="//ogc:PropertyName">
          <xsl:sort select="."/>

          <xsl:variable name="thisNode" select="generate-id(.)"/>
          <xsl:variable name="nodesAtSameLocation" select="key('prop', .)"/>
          <xsl:variable name="firstNodeAtSameLocation"
                        select="generate-id($nodesAtSameLocation[1])"/>

          <xsl:if test="$thisNode = $firstNodeAtSameLocation">
            <keyword>
              <gco:CharacterString>
                <xsl:value-of select="."/>
              </gco:CharacterString>
            </keyword>
          </xsl:if>
        </xsl:for-each>
        <type>
          <MD_KeywordTypeCode codeList="./resources/codeList.xml#MD_KeywordTypeCode"
                              codeListValue="theme"/>
        </type>
      </MD_Keywords>
    </descriptiveKeywords>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <topicCategory>
      <MD_TopicCategoryCode>
        <xsl:value-of select="$topic"/>
      </MD_TopicCategoryCode>
    </topicCategory>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

  </xsl:template>

</xsl:stylesheet>
