<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:gn="http://www.fao.org/geonetwork" xmlns:xs="http://www.w3.org/2001/XMLSchema"
  exclude-result-prefixes="xs" version="2.0">
  <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
  <!-- fgdc-std brief formatting -->
  <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

  <xsl:template name="fgdc-stdBrief">
    <metadata>
      <xsl:copy-of select="idinfo/citation/citeinfo/title"/>
      <xsl:copy-of select="idinfo/descript/abstract"/>


      <xsl:for-each select="idinfo/keywords/theme/themekey[text()]">
        <keyword>
          <xsl:value-of select="."/>
        </keyword>
      </xsl:for-each>
      <xsl:for-each select="idinfo/keywords/place/placekey[text()]">
        <keyword>
          <xsl:value-of select="."/>
        </keyword>
      </xsl:for-each>
      <xsl:for-each select="idinfo/keywords/stratum/stratkey[text()]">
        <keyword>
          <xsl:value-of select="."/>
        </keyword>
      </xsl:for-each>
      <xsl:for-each select="idinfo/keywords/temporal/tempkey[text()]">
        <keyword>
          <xsl:value-of select="."/>
        </keyword>
      </xsl:for-each>
      <xsl:for-each select="idinfo/citation/citeinfo/onlink[text()]">
        <link type="url">
          <xsl:value-of select="."/>
        </link>
      </xsl:for-each>

      <xsl:if test="idinfo/spdom/bounding">
        <geoBox>
          <westBL>
            <xsl:value-of select="idinfo/spdom/bounding/westbc"/>
          </westBL>
          <eastBL>
            <xsl:value-of select="idinfo/spdom/bounding/eastbc"/>
          </eastBL>
          <southBL>
            <xsl:value-of select="idinfo/spdom/bounding/southbc"/>
          </southBL>
          <northBL>
            <xsl:value-of select="idinfo/spdom/bounding/northbc"/>
          </northBL>
        </geoBox>
      </xsl:if>

      <xsl:if test="not(gn:info/server)">
        <xsl:variable name="info" select="gn:info"/>
        <xsl:variable name="id" select="gn:info/id"/>

        <xsl:for-each select="idinfo/browse">
          <xsl:variable name="fileName" select="browsen"/>
          <xsl:if test="$fileName != ''">
            <xsl:variable name="fileDescr" select="browset"/>
            <xsl:choose>

              <!-- the thumbnail is an url -->

              <xsl:when test="contains($fileName ,'://')">
                <image type="unknown">
                  <xsl:value-of select="$fileName"/>
                </image>
              </xsl:when>

              <!-- small thumbnail -->

              <xsl:when test="string($fileDescr)='thumbnail'">
                <xsl:choose>
                  <xsl:when test="$info/isHarvested = 'y'">
                    <xsl:if test="$info/harvestInfo/smallThumbnail">
                      <image type="thumbnail">
                        <xsl:value-of select="concat($info/harvestInfo/smallThumbnail, $fileName)"/>
                      </image>
                    </xsl:if>
                  </xsl:when>

                  <xsl:otherwise>
                    <image type="thumbnail">
                      <xsl:value-of
                        select="concat(/root/gui/locService,'/resources.get?id=',$id,'&amp;fname=',$fileName,'&amp;access=public')"
                      />
                    </image>
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:when>

              <!-- large thumbnail -->

              <xsl:when test="string($fileDescr)='large_thumbnail'">
                <xsl:choose>
                  <xsl:when test="$info/isHarvested = 'y'">
                    <xsl:if test="$info/harvestInfo/largeThumbnail">
                      <image type="overview">
                        <xsl:value-of select="concat($info/harvestInfo/largeThumbnail, $fileName)"/>
                      </image>
                    </xsl:if>
                  </xsl:when>

                  <xsl:otherwise>
                    <image type="overview">
                      <xsl:value-of
                        select="concat(/root/gui/locService,'/graphover.show?id=',$id,'&amp;fname=',$fileName,'&amp;access=public')"
                      />
                    </image>
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:when>

            </xsl:choose>
          </xsl:if>
        </xsl:for-each>
      </xsl:if>
      <xsl:copy-of select="gn:info"/>
    </metadata>
  </xsl:template>
</xsl:stylesheet>
