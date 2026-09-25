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

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:gn-fn-core="http://geonetwork-opensource.org/xsl/functions/core"
                xmlns:util="java:org.fao.geonet.util.XslUtil"
                version="2.0"
                exclude-result-prefixes="#all">
  <!--
  Basic search interface which does not require JS.
  -->
  <xsl:import href="../base-layout-nojs.xsl"/>
  <xsl:import href="../common/functions-core.xsl"/>

  <xsl:variable name="count"
                select="/root/search/response[1]/summary[1]/@count"/>

  <!-- $parameters itself is declared in skin.xsl (every page includes it, and it's needed
  there too); this stylesheet only adds the query-string helper built from it. -->
  <xsl:variable name="otherParamsQueryString">
    <xsl:call-template name="other-params-query-string"/>
  </xsl:variable>

  <!-- Joins $parameters (excluding $excludeName) into a "&amp;name=value..." query suffix, to
  carry forward the other active filters on a link. -->
  <xsl:template name="other-params-query-string">
    <xsl:param name="excludeName" select="''"/>
    <xsl:for-each select="$parameters[name(.) != $excludeName]">
      <xsl:value-of select="concat('&amp;', name(.), '=', encode-for-uri(.))"/>
    </xsl:for-each>
  </xsl:template>

  <!-- A filter's param name isn't always its own translation key: "cat" collides with the
  Catalan language name ($t/cat = "Català"); "resourceType" has no key of its own but means the
  same thing as "type" (the classic-search alias SearchApi.remapFieldName treats identically);
  "_groupPublished" and "_source" have no keys of their own either - the real keys are
  "groupPublished" and "sourceCatalog", without the leading underscore SearchApi's own request
  param name carries. -->
  <xsl:template name="param-label-key">
    <xsl:param name="paramName" select="''"/>
    <xsl:choose>
      <xsl:when test="$paramName = 'cat'">category</xsl:when>
      <xsl:when test="$paramName = 'resourceType'">type</xsl:when>
      <xsl:when test="$paramName = '_groupPublished'">groupPublished</xsl:when>
      <xsl:when test="$paramName = '_source'">sourceCatalog</xsl:when>
      <xsl:otherwise><xsl:value-of select="$paramName"/></xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- From/to/count plus Previous/Next; called both above and below the result list. -->
  <xsl:template name="pagination-nav">
    <xsl:param name="response" as="element()"/>
    <div class="row gn-pages">
      <div class="col-xs-12">
        <xsl:value-of select="gn-fn-core:translate('from', $t)"/>
        <b>
          <xsl:value-of select="$response/@from"/>
        </b>
        -
        <b>
          <xsl:value-of select="$response/@to"/>
        </b>
        /
        <b>
          <xsl:value-of select="$count"/>
        </b>
      </div>
      <div class="col-xs-12">
        <xsl:if test="number($response/@from) > 1">
          <a href="{$nodeUrl}search?from={max((1, number($response/@from) - number($response/@hitsPerPage)))}{$otherParamsQueryString}">
            <xsl:value-of select="gn-fn-core:translate('previous', $t)"/>
          </a>
        </xsl:if>
        <xsl:if test="number($response/@to) &lt; $count">
          <a href="{$nodeUrl}search?from={number($response/@to) + 1}{$otherParamsQueryString}"
             class="pull-right">
            <xsl:value-of select="gn-fn-core:translate('next', $t)"/>
          </a>
        </xsl:if>
      </div>
    </div>
  </xsl:template>

  <xsl:template mode="content" match="/">

    <div class="row gn-search-page"
      id="{/root/gui/systemConfig/system/site/siteId}"
      itemscope="itemscope"
      itemtype="http://schema.org/DataCatalog">
      <!-- Only h1 on the page; result titles below are h2. -->
      <h1><xsl:value-of select="gn-fn-core:translate('search', $t)"/></h1>
      <meta itemprop="name" content="{/root/gui/systemConfig/system/site/name}"></meta>
      <span itemprop="publisher" itemscope="itemscope" itemtype="http://schema.org/Organization">
        <meta itemprop="name" content="{/root/gui/systemConfig/system/site/organization}"></meta>
        <meta itemprop="email" content="{/root/gui/systemConfig/system/feedback/email}"></meta>
      </span>
      <meta itemprop="url" content="{$nodeUrl}search"></meta>

      <div class="col-md-3 gn-facet">
        <div>
          <!-- One removable chip per active filter - same treatment regardless of how many
          are active, rather than a one-off icon+heading layout for exactly one filter that
          just repeated what its own chip already says. -->
          <xsl:if test="count($parameters) > 0">
            <div class="gn-margin-bottom">
              <xsl:for-each select="$parameters">
                <xsl:variable name="paramName" select="name(.)"/>
                <xsl:variable name="paramValue" select="text()"/>
                <xsl:variable name="paramLabelKey">
                  <xsl:call-template name="param-label-key">
                    <xsl:with-param name="paramName" select="$paramName"/>
                  </xsl:call-template>
                </xsl:variable>
                <xsl:variable name="paramLabel" select="gn-fn-core:translate(string($paramLabelKey), $t)"/>
                <xsl:variable name="removeThisParamQueryString">
                  <xsl:call-template name="other-params-query-string">
                    <xsl:with-param name="excludeName" select="$paramName"/>
                  </xsl:call-template>
                </xsl:variable>
                <span class="label label-default">
                  <xsl:value-of select="concat($paramLabel, ': ', $paramValue)"/>
                  <xsl:text> </xsl:text>
                  <!-- Explicit size/spacing: .label's badge font-size is too small to click. -->
                  <a href="{$nodeUrl}search?from=1{$removeThisParamQueryString}"
                     title="{concat(gn-fn-core:translate('remove', $t), ' ', $paramLabel)}"
                     style="display:inline-block; margin-left:6px; font-size:16px; line-height:1; font-weight:bold; color:inherit;">&#215;</a>
                </span>
                <xsl:text> </xsl:text>
              </xsl:for-each>
            </div>
          </xsl:if>
          &#160;
        </div>

        <xsl:if test="$count > 0">
          <xsl:for-each select="/root/search/response[1]/summary">
            <xsl:for-each select="dimension[category]">
            <details open="open">
              <xsl:variable name="dimensionLabelKey">
                <xsl:call-template name="param-label-key">
                  <xsl:with-param name="paramName" select="@label"/>
                </xsl:call-template>
              </xsl:variable>
              <summary><xsl:value-of select="gn-fn-core:translate(string($dimensionLabelKey), $t)"/></summary>

              <xsl:variable name="field" select="@name"/>
              <ul>
                <xsl:for-each select="category">
                  <li>
                    <label>
                      <!-- $field is a display name (e.g. "topicCat"); SearchApi.remapFieldName
                      does the real-field mapping, and searchCriteria echoes it back under this
                      same name - using the real field here instead would let both spellings
                      reach buildMustClauses at once, ANDing two clauses on one field. -->
                      <!-- Excludes $field itself so this replaces rather than duplicates. -->
                      <xsl:variable name="otherParamsExcludingThisField">
                        <xsl:call-template name="other-params-query-string">
                          <xsl:with-param name="excludeName" select="$field"/>
                        </xsl:call-template>
                      </xsl:variable>
                      <a href="{$nodeUrl}search?{$field}={encode-for-uri(@value)}{$otherParamsExcludingThisField}">
                        <span class="gn-facet-label">
                        <xsl:value-of select="gn-fn-core:translate(@label, $t)"/>
                        </span>
                        <span class="gn-facet-count">
                        (<xsl:value-of select="@count"/>)
                        </span>
                      </a>
                    </label>
                  </li>
                </xsl:for-each>
              </ul></details>
            </xsl:for-each>

          </xsl:for-each>
        </xsl:if>
      </div>
      <div class="col-md-9">
        <xsl:choose>
          <!-- Whether this page has hits, not just whether the search matched anything. -->
          <xsl:when test="/root/search/response[1]/metadata">
          <xsl:for-each select="/root/search/response[@from]">

            <xsl:call-template name="pagination-nav">
              <xsl:with-param name="response" select="."/>
            </xsl:call-template>

            <ul class="list-group gn-resultview gn-resultview-sumup">
              <xsl:for-each select="metadata">
               <li class="list-group-item gn-grid"
                   id="{*[name()='geonet:info']/uuid}"
                   itemprop="dataset"
                   itemscope="itemscope"
                   itemtype="{gn-fn-core:get-schema-org-class(string(type[1]))}">
                 <meta itemprop="url" content="{$nodeUrl}api/records/{*[name()='geonet:info']/uuid}"></meta>
                 <div class="row">
                   <xsl:if test="count(category) > 0">
                     <div class="gn-md-category">
                       <span><xsl:value-of select="gn-fn-core:translate('categories', $t)"/></span>
                       <xsl:for-each select="category">
                         <!-- "cat" is the real, queryable field for a record's category. -->
                         <xsl:variable name="otherParamsExcludingCat">
                           <xsl:call-template name="other-params-query-string">
                             <xsl:with-param name="excludeName" select="'cat'"/>
                           </xsl:call-template>
                         </xsl:variable>
                         <a title="{.}"
                            href="{$nodeUrl}search?cat={encode-for-uri(.)}{$otherParamsExcludingCat}">
                           <i class="fa">
                             <span class="fa gn-icon-{.}">&#160;</span>
                           </i>
                         </a>
                       </xsl:for-each>
                     </div>
                   </xsl:if>
                 </div>

                 <div class="gn-md-title">
                   <h2 itemprop="name">
                     <a href="{$nodeUrl}api/records/{*[name()='geonet:info']/uuid}"
                        itemprop="url">
                       <xsl:choose>
                         <xsl:when test="title != ''">
                           <xsl:value-of select="title"/>
                         </xsl:when>
                         <xsl:otherwise>
                           <xsl:value-of select="defaultTitle"/>
                         </xsl:otherwise>
                       </xsl:choose>
                     </a>
                   </h2>
                 </div>

                  <div class="clearfix">
                    <xsl:for-each select="image[1]">
                      <div class="gn-md-thumbnail pull-left">
                        <img class="gn-img-thumbnail"
                             itemprop="thumbnailUrl"
                             alt="{@alt}"
                             src="{@url}" />
                      </div>
                    </xsl:for-each>
                    <p itemprop="description">
                      <xsl:value-of select="abstract"/>
                    </p>
                  </div>
                </li>
              </xsl:for-each>
            </ul>

            <xsl:call-template name="pagination-nav">
              <xsl:with-param name="response" select="."/>
            </xsl:call-template>
          </xsl:for-each>
          </xsl:when>
          <xsl:otherwise>
            <p class="gn-no-results">
              <xsl:value-of select="gn-fn-core:translate('noRecordFound', $t)"/>
            </p>
          </xsl:otherwise>
        </xsl:choose>
      </div>
    </div>
  </xsl:template>

</xsl:stylesheet>
