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

  <xsl:template mode="content" match="/">

    <xsl:variable name="count"
                  select="/root/search/response[1]/summary[1]/@count"/>



    <div class="row">
      <div class="col-md-3 gn-facet">

        <div>
          <xsl:variable name="parameters"
                        select="/root/search/params/*[
                                  name(.) != 'fast' and name(.) != 'resultType'
                                ]"/>

          <!-- If only one parameter set, the page provides a summary
          for this criteria. For contact, source catalog, an extra panel
          is added with some details about this resource. -->
          <xsl:if test="count($parameters) = 1">
            <div class="thumbnail text-center">
              <xsl:variable name="parameterName"
                            select="$parameters[1]/name()"/>
              <xsl:variable name="parameterLabel"
                            select="gn-fn-core:translate($parameterName, $t)"/>

              <xsl:variable name="parameterValue"
                            select="$parameters[1]/text()"/>
              <h4>
                <xsl:value-of select="$parameterLabel"/>
              </h4>
              <!-- Illustration -->
              <xsl:choose>
                <xsl:when test="$parameterName = '_groupPublished'">
                  <img  class="gn-logo-lg"
                        src="{nodeUrl}../images/harvesting/{$parameterValue}.png"/>
                </xsl:when>
                <xsl:when test="$parameterName = '_source'">
                  <img  class="gn-logo-lg"
                        src="{nodeUrl}../images/logos/{$parameterValue}.png"/>
                </xsl:when>
                <xsl:when test="$parameterName = 'responsiblePartyEmail'">
                  <img src="//gravatar.com/avatar/{util:md5Hex($parameterValue)}?s=200"/>
                  <h2>
                    <xsl:value-of select="$parameterValue"/>
                  </h2>
                </xsl:when>
                <xsl:when test="$parameterName = 'topicCat' or $parameterName = 'type'">
                  <span class="">
                    <i class="fa fa-3x gn-icon gn-icon-{$parameterValue}">&#160;</i>
                  </span>
                  <h2>
                    <xsl:value-of select="$parameterValue"/>
                  </h2>
                </xsl:when>
                <xsl:otherwise>
                  <h2>
                    <xsl:value-of select="$parameterValue"/>
                  </h2>
                </xsl:otherwise>
              </xsl:choose>
              <h4>
                <xsl:value-of select="concat($count, ' ', $t/records)"/>
              </h4>
            </div>
          </xsl:if>
          &#160;
        </div>

        <xsl:if test="$count > 0">
          <xsl:for-each select="/root/search/response[1]/summary">
            <xsl:for-each select="dimension[category]">
              <h4><xsl:value-of select="gn-fn-core:translate(@label, $t)"/></h4>

              <xsl:variable name="field" select="@name"/>
              <ul>
                <xsl:for-each select="category">
                  <li>
                    <label>
                      <xsl:variable name="luceneField"
                                    select="if ($field = 'sourceCatalog')
                                            then '_source'
                                            else if ($field = 'maintenanceAndUpdateFrequency')
                                            then 'cl_maintenanceAndUpdateFrequency'
                                            else $field"/>
                      <a href="{$nodeUrl}search?{$luceneField}={@value}">
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
              </ul>
            </xsl:for-each>
          </xsl:for-each>
        </xsl:if>
      </div>
      <xsl:if test="$count > 0">
        <div class="col-md-9">
          <xsl:for-each select="/root/search/response[@from]">

            <div class="row" style="padding-bottom:20px">
              <div class="col-xs-12">
                <xsl:value-of select="$t/from"/>
                <b>
                  <xsl:value-of select="@from"/>
                </b>
                -
                <b>
                  <xsl:value-of select="@to"/>
                </b>
                /
                <b>
                  <xsl:value-of select="$count"/>
                </b>
              </div>
            </div>

            <ul class="list-group gn-resultview gn-resultview-sumup">
              <xsl:for-each select="metadata">
               <li class="list-group-item gn-grid"
                   itemscope="itemscope"
                   itemtype="{gn-fn-core:get-schema-org-class(type[1])}">
                 <div class="row">
                   <xsl:if test="count(category) > 0">
                     <div class="gn-md-category">
                       <span><xsl:value-of select="$t/categories"/></span>
                       <xsl:for-each select="category">
                         <a title="{.}"
                            href="{$nodeUrl}search?_cat=maps">
                           <i class="fa">
                             <span class="fa gn-icon-{.}">&#160;</span>
                           </i>
                         </a>
                       </xsl:for-each>
                     </div>
                   </xsl:if>
                 </div>

                 <div class="row gn-md-title">
                   <h3 itemprop="name">
                     <a href="{$nodeUrl}api/records/{*[name()='geonet:info']/uuid}"
                        itemprop="url">
                       <i class="fa gn-icon-{type}" title="{type}">&#160;</i>
                       <xsl:choose>
                         <xsl:when test="title != ''">
                           <xsl:value-of select="title"/>
                         </xsl:when>
                         <xsl:otherwise>
                           <xsl:value-of select="defaultTitle"/>
                         </xsl:otherwise>
                       </xsl:choose>
                     </a>
                   </h3>
                 </div>

                 <div>
                   <div class="gn-md-thumbnail">
                     <xsl:for-each select="image[1]">
                       <img class="gn-img-thumbnail"
                            src="{tokenize(., '\|')[2]}"></img>
                     </xsl:for-each>
                   </div>
                   <div style="float:left; display:block; width: calc(100% - 162px)">

                     <div class="text-justify gn-md-abstract ellipsis">
                       <div>
                         <p>
                           <xsl:value-of select="abstract"/>
                         </p>
                       </div>
                     </div>
                   </div>
                 </div>
                </li>
              </xsl:for-each>
            </ul>
          </xsl:for-each>
        </div>
      </xsl:if>
    </div>
  </xsl:template>

</xsl:stylesheet>
