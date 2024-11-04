<?xml version="1.0" encoding="UTF-8"?>
<!--
  ~ Copyright (C) 2001-2016 Food and Agriculture Organization of the
  ~ United Nations (FAO-UN), United Nations World Food Programme (WFP)
  ~ and United Nations Environment Programme (UNEP)
  ~
  ~ This program is free software; you can redistribute it and/or modify
  ~ it under the terms of the GNU General Public License as published by
  ~ the Free Software Foundation; either version 2 of the License, or (at
  ~ your option) any later version.
  ~
  ~ This program is distributed in the hope that it will be useful, but
  ~ WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  ~ General Public License for more details.
  ~
  ~ You should have received a copy of the GNU General Public License
  ~ along with this program; if not, write to the Free Software
  ~ Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
  ~
  ~ Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
  ~ Rome - Italy. email: geonetwork@osgeo.org
  -->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:gml="http://www.opengis.net/gml"
                xmlns:srv="http://www.isotc211.org/2005/srv" xmlns:gmx="http://www.isotc211.org/2005/gmx"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:gn-fn-iso19139="http://geonetwork-opensource.org/xsl/functions/profiles/iso19139"
                xmlns:geonet="http://www.fao.org/geonetwork"
                xmlns:java="java:org.fao.geonet.util.XslUtil"
                version="2.0" exclude-result-prefixes="#all">

  <xsl:include href="../iso19139/convert/thesaurus-transformation.xsl"/>


  <!--
   Merges the keywords in the metadata document by thesaurus
   so that all keywords from the same thesaurus are in the same
   keyword block.
  -->
  <xsl:template match="gmd:descriptiveKeywords|srv:keywords" priority="10">
    <xsl:variable name="name" select="name()"/>
    <xsl:variable name="root" select="/"/>
    <xsl:variable name="node" select="."/>

    <!-- On first matching keyword, loop on all and
         dispatch keywords coming from the all thesaurus
         in existing block or in new ones if needed. -->
    <xsl:if test="name(preceding-sibling::*[1]) != $name">

      <!-- Collect the all thesaurus keywords which contains
           the new keyword added by users. -->
      <xsl:variable name="allThesaurusEl"
                    select="(../(gmd:descriptiveKeywords|srv:keywords)[
                                contains(@xlink:href, 'thesaurus=external.none.allThesaurus') or
                                contains(*/gmd:thesaurusName/*/gmd:identifier/*/gmd:code/*,
                                  'external.none.allThesaurus')
                                ])[1]"/>

      <!-- Check if we are in xlink mode or not.
           WARNING: We don't support a mix of keyword in xlink mode
           and others not using xlinks.
      -->
      <xsl:variable name="isAllThesaurusXlinked"
                    select="count($allThesaurusEl/@xlink:href) > 0"/>


      <!-- Collect all XLink parameters from the all thesaurus -->
      <xsl:variable name="hrefPrefix"
                    select="replace($allThesaurusEl/@xlink:href, '(.+\?).*', '$1')"/>
      <xsl:variable name="hrefQuery"
                    select="replace($allThesaurusEl/@xlink:href, '.+\?(.*)', '$1')"/>
      <xsl:variable name="params" as="node()*">
        <xsl:for-each select="tokenize(
                                  $hrefQuery,
                                  '\?|&amp;')">
          <param>
            <key>
              <xsl:value-of select="tokenize(., '=')[1]"/>
            </key>
            <val>
              <xsl:value-of select="java:decodeURLParameter(tokenize(., '=')[2])"/>
            </val>
          </param>
        </xsl:for-each>
      </xsl:variable>

      <!-- Collect all keyword identifiers from the XLink URL in the all thesaurus -->
      <xsl:variable name="keywordIdentifiers" as="node()*">
        <xsl:if test="$params[key = 'id']/val != ''">
          <xsl:for-each select="tokenize(
                                      $params[key = 'id']/val,
                                      ',')">
            <keyword>
              <thes>
                <xsl:value-of
                  select="replace(., 'http://org.fao.geonet.thesaurus.all/(.+)@@@.+', '$1')"/>
              </thes>
              <id>
                <xsl:value-of
                  select="replace(., 'http://org.fao.geonet.thesaurus.all/.+@@@(.+)', '$1')"/>
              </id>
            </keyword>
          </xsl:for-each>
        </xsl:if>
      </xsl:variable>

      <!--<xsl:message>params: <xsl:copy-of select="params"/></xsl:message>
      <xsl:message>$keywordIdentifiers: <xsl:copy-of select="$keywordIdentifiers"/></xsl:message>-->

      <!-- Collect all keyword blocks. -->
      <xsl:variable name="keywords"
                    select=".|following-sibling::*[name() = $name]"/>



      <!-- Inject all new keywords from the all thesaurus
           in existing blocks. -->
      <xsl:for-each
              select="$keywords">

        <!-- Skip the all thesaurus which some kind of virtual block
             only used to collect keywords from multiple sources in the editor. -->
        <xsl:variable name="isNotAllThesaurus"
                      select="not(contains(@xlink:href, 'thesaurus=external.none.allThesaurus')) and
                              not(contains(*/gmd:thesaurusName/*/gmd:identifier/*/gmd:code/*,
                                  'external.none.allThesaurus'))"/>

        <xsl:if test="$isNotAllThesaurus">
          <xsl:choose>
            <xsl:when test="starts-with(@xlink:href, 'local://')">
              <!-- Insert identifiers from the all thesaurus in the existing XLink.
              -->
              <xsl:variable name="currentThesaurus"
                            select="replace(@xlink:href, '.*thesaurus=([^&amp;]+).*', '$1')"/>

              <xsl:variable name="currentIdentifiers"
                            select="replace(@xlink:href, '.*id=([^&amp;]*).*', '$1')"/>

              <xsl:variable name="newIdentifiers"
                            select="string-join(
                                      $keywordIdentifiers[thes = $currentThesaurus]/id,
                                      ',')"/>

              <xsl:variable name="newUrl"
                            select="replace(@xlink:href,
                                      '(.*)id=([^&amp;]*)(.*)',
                                      concat(
                                        '$1id=$2',
                                        if ($newIdentifiers != '' and $currentIdentifiers != '') then '%2C' else '',
                                        $newIdentifiers,
                                        '$3'))"/>

              <!--
              <xsl:message>Appending all thesaurus ids to existing block:
                Thesaurus: <xsl:value-of select="$currentThesaurus"/>
                Current identifiers: <xsl:value-of select="$currentIdentifiers"/>
                Identifiers to append: <xsl:value-of select="$newIdentifiers"/>
                New URL: <xsl:value-of select="$newUrl"/>
              </xsl:message>-->

              <gmd:descriptiveKeywords xlink:href="{$newUrl}"/>
            </xsl:when>
            <xsl:otherwise>


              <xsl:variable name="isFreeTextKeywordBlock"
                            select="not(*/gmd:thesaurusName)"/>
              <xsl:variable name="freeTextKeywordBlockType"
                            select="*/gmd:type/*/@codeListValue"/>
              <xsl:variable name="isFirstFreeTextKeywordBlock"
                            select="count(preceding-sibling::*[
                                    name() = $name
                                    and not(*/gmd:thesaurusName)
                                    and */gmd:type/*/@codeListValue = $freeTextKeywordBlockType]) = 0"/>

              <xsl:choose>
                <xsl:when test="$isFreeTextKeywordBlock
                                and $isFirstFreeTextKeywordBlock = false()">
                  <!-- All free text keywords are combined in same block -->
                </xsl:when>
                <xsl:otherwise>
                  <xsl:copy>
                    <xsl:apply-templates select="@*"/>
                    <gmd:MD_Keywords>
                      <xsl:apply-templates select="*/gmd:keyword"/>

                      <!-- Combine all free text keyword of same type -->
                      <xsl:if test="$isFreeTextKeywordBlock
                                    and $isFirstFreeTextKeywordBlock">
                        <xsl:apply-templates select="following-sibling::*[
                                    name() = $name
                                    and not(*/gmd:thesaurusName)
                                    and */gmd:type/*/@codeListValue = $freeTextKeywordBlockType]/*/gmd:keyword"/>
                      </xsl:if>


                      <!-- Append all keywords added by all thesaurus. -->
                      <xsl:variable name="thesaurusKey"
                                    select="substring-after(
                                      normalize-space(
                                        */gmd:thesaurusName/*/gmd:identifier/*/gmd:code/*),
                                      'geonetwork.thesaurus.')"/>
                      <xsl:for-each select="$allThesaurusEl//gmd:keyword[
                                      @gco:nilReason = concat('thesaurus::', $thesaurusKey)]">
                        <gmd:keyword>
                          <xsl:copy-of select="*"/>
                        </gmd:keyword>
                      </xsl:for-each>


                      <xsl:apply-templates select="*/gmd:type|*/gmd:thesaurusName"/>
                    </gmd:MD_Keywords>
                  </xsl:copy>
                </xsl:otherwise>
              </xsl:choose>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:if>
      </xsl:for-each>



      <!-- Create new descriptive keywords block
           for keyword in the all thesaurus
           which are not in current record. -->
      <xsl:for-each-group select="$allThesaurusEl//gmd:keyword"
                          group-by="@gco:nilReason">
        <xsl:variable name="thesaurusKey"
                      select="substring-after(current-grouping-key(), '::')"/>
        <xsl:variable name="isThesaurusBlockExisting"
                      select="count($node/../*
                                [name() = $name]
                                [contains(@xlink:href,
                                          concat('thesaurus=', $thesaurusKey)) or
                                contains(*/gmd:thesaurusName/*/gmd:identifier/*/gmd:code/*,
                                  $thesaurusKey)]) > 0"/>

        <xsl:if test="$thesaurusKey != '' and not($isThesaurusBlockExisting)">
          <xsl:element name="{$name}">
            <gmd:MD_Keywords>
              <xsl:for-each select="current-group()">
                <gmd:keyword>
                  <xsl:copy-of select="*"/>
                </gmd:keyword>
              </xsl:for-each>

              <xsl:copy-of select="geonet:add-thesaurus-info(
                                              $thesaurusKey,
                                              $mainLanguage,
                                              false(),
                                              true(),
                                              $root/root/env/thesauri,
                                              true())"/>
            </gmd:MD_Keywords>
          </xsl:element>
        </xsl:if>
      </xsl:for-each-group>



      <!-- Create new descriptive keywords block
           for keywords in the all thesaurus xlink
           which are not in current record.

           <gmd:descriptiveKeywords xmlns:gmd="http://www.isotc211.org/2005/gmd"
                         xmlns:xlink="http://www.w3.org/1999/xlink"
                         xlink:href="local://srv/api/registries/vocabularies/keyword?
                           thesaurus=external.none.allThesaurus&amp;
                           id=http://org.fao.geonet.thesaurus.all/external.place.regions@@@
                              http%3A%2F%2Fwww.naturalearthdata.com%2Fne_admin%23Country%2FDependency%2FASM&amp;
                              lang=eng"/>
           -->
      <xsl:if test="$isAllThesaurusXlinked and count($keywordIdentifiers/*) > 0">

        <!-- Build XLink URL common parameters (eg. lang) -->
        <xsl:variable name="uniqueParams"
                      select="distinct-values(
                                  $params/key[. != 'id' and . != 'thesaurus']/text())"/>
        <xsl:variable name="queryString">
          <xsl:for-each select="$uniqueParams">
            <xsl:variable name="p" select="."/>
            <xsl:value-of select="concat('&amp;', $p, '=', $params[key/text() = $p]/val)"/>
          </xsl:for-each>
        </xsl:variable>



        <xsl:for-each-group select="$keywordIdentifiers"
                            group-by="thes">


          <xsl:variable name="isThesaurusBlockExisting"
                        select="count($node/../*[contains(@xlink:href,
                                        concat('thesaurus=', current-grouping-key()))]) > 0"/>
          <xsl:if test="not($isThesaurusBlockExisting)">

            <!--
            <xsl:message>Adding new xlink block
              Thesaurus: <xsl:copy-of select="current-grouping-key()"/>
              Keywords: <xsl:copy-of select="string-join(current-group()/id, ',')"/>
            </xsl:message>-->

            <gmd:descriptiveKeywords
                        xlink:href="{concat(
                                      $hrefPrefix,
                                      'skipdescriptivekeywords=true&amp;thesaurus=', current-grouping-key(),
                                      '&amp;id=',
                                      string-join(
                                        current-group()/id[. != ''],
                                        ','),
                                      $queryString)}"/>
          </xsl:if>
        </xsl:for-each-group>
      </xsl:if>
    </xsl:if>
  </xsl:template>
</xsl:stylesheet>
