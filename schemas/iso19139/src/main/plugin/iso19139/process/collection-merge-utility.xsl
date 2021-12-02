<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:srv="http://www.isotc211.org/2005/srv"
                xmlns:gmx="http://www.isotc211.org/2005/gmx"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:saxon="http://saxon.sf.net/"
                extension-element-prefixes="saxon"
                exclude-result-prefixes="#all">


  <xsl:template match="*[name() = $elements/@name]" mode="merge" priority="2">
    <xsl:variable name="name"
                  select="name()"/>

    <!-- Do the work once on the first element, others are ignored. -->
    <xsl:if test="count(preceding-sibling::*[name() = $name]) = 0">
      <xsl:variable name="current"
                    select="$elements[@name = $name]"/>
      <xsl:choose>
        <xsl:when test="$updateAllFromMembers">

          <xsl:variable name="elementName"
                        select="$current/@name"/>
          <xsl:variable name="match"
                        select="$existingMembers//*[name() = $current/@name]"/>
          <xsl:variable name="groupBy"
                        select="$current/@groupBy"/>
          <xsl:variable name="groupKey"
                        select="saxon:evaluate(concat('$p1/', $groupBy), $match)"/>
          <xsl:variable name="elementName"
                        select="$current/@name"/>
          <xsl:variable name="merge"
                        select="$current/@merge"/>

          <xsl:message>Element:
            <xsl:copy-of select="$elementName"/>
          </xsl:message>
          <xsl:message>Match:
            <xsl:copy-of select="$match"/>
          </xsl:message>
          <xsl:message>Groups:
            <xsl:copy-of select="string-join(distinct-values($groupKey), ' ,')"/>
          </xsl:message>

          <xsl:for-each select="distinct-values($groupKey)">
            <xsl:variable name="groupKey"
                          select="current()"/>
            <xsl:variable name="emptyKey"
                          select=". = ''"/>
            <xsl:variable name="groupValues"
                          select="saxon:evaluate(concat('$p1[', $groupBy, ' = ''', $groupKey ,''']'), $match)"/>
            <!--
            <xsl:message>empty: <xsl:value-of select="$emptyKey"/> </xsl:message>
            <xsl:message> Groups: <xsl:copy-of select="$groupValues"/></xsl:message>-->

            <!-- Copy the first instance -->
            <xsl:for-each select="$groupValues[1]">
              <xsl:copy>
                <xsl:attribute name="xlink:href"
                               select="concat('copy://api/registries/entries/', $elementName, '/', $groupKey, '?from=', $existingMemberUuids)"/>
                <xsl:copy-of select="@*"/>
                <xsl:message>empty:
                  <xsl:copy-of select="count(*/*)"/>
                </xsl:message>

                <!-- Copy class name. Can be a one child element
                eg. gmd:topicCategory
                or a complex element with children. -->
                <xsl:choose>
                  <xsl:when test="count(*/*) > 0">
                    <xsl:for-each select="*">
                      <xsl:copy>
                        <!-- In order, Group or copy elements -->
                        <xsl:variable name="childNames"
                                      select="distinct-values(*/name())"/>

                        <xsl:for-each select="$childNames">
                          <xsl:message>Copy:
                            <xsl:value-of select="."/>
                          </xsl:message>
                          <xsl:choose>
                            <!-- Merge
                            eg. gmd:keyword -->
                            <xsl:when test="current() = $merge">
                              <xsl:variable name="elementsToMerge"
                                            select="$groupValues/*/*[name() = current()]"/>
                              <xsl:variable name="isTextElement"
                                            select="count($elementsToMerge[0]/*/text()) > 0"/>
                              <xsl:choose>
                                <xsl:when test="$isTextElement">
                                  <xsl:for-each-group select="$elementsToMerge"
                                                      group-by="*/text()">
                                    <xsl:copy-of select="."/>
                                  </xsl:for-each-group>
                                </xsl:when>
                                <xsl:otherwise>
                                  <xsl:for-each-group select="$elementsToMerge"
                                                      group-by="normalize-space(*)">
                                    <xsl:copy-of select="."/>
                                  </xsl:for-each-group>
                                </xsl:otherwise>
                              </xsl:choose>
                            </xsl:when>
                            <!-- Nothing to merge here, so preserve all other children -->
                            <xsl:when test="$emptyKey">
                              <xsl:copy-of select="$groupValues/*/*[name() = current()]"/>
                            </xsl:when>
                            <!-- Copy the siblings next to the one to merge
                             from the first element in the group.
                             Others are ignored.
                            eg. gmd:type, then gmd:thesaurusName -->
                            <xsl:otherwise>
                              <xsl:copy-of select="$groupValues[1]/*/*[name() = current()]"/>
                            </xsl:otherwise>
                          </xsl:choose>
                        </xsl:for-each>
                      </xsl:copy>
                    </xsl:for-each>
                  </xsl:when>
                  <xsl:otherwise>
                    <!-- Merge by selecting the first element matching the group
                    by codeListValue attribute or text().
                        eg. gmd:topicCategory -->
                    <xsl:for-each select="($groupValues/*[
                                              @codeListValue = $groupKey
                                              or text() = $groupKey])[1]">
                      <xsl:copy-of select="."/>
                    </xsl:for-each>
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:copy>
            </xsl:for-each>
          </xsl:for-each>
        </xsl:when>
        <xsl:otherwise>
          <xsl:copy-of select="."/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:if>
  </xsl:template>
</xsl:stylesheet>
