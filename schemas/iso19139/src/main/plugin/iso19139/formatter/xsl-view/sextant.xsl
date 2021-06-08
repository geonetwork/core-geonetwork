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
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gmx="http://www.isotc211.org/2005/gmx"
                xmlns:gml="http://www.opengis.net/gml/3.2"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:saxon="http://saxon.sf.net/"
                version="2.0"
                extension-element-prefixes="saxon"
                exclude-result-prefixes="#all">

  <xsl:variable name="dateFormatRegex"
                select="'(\d{4}-[01]\d-[0-3]\d.*)'"/>

  <xsl:template mode="getLicense" match="gmd:MD_Metadata[$view = 'sextant']">
    <xsl:apply-templates mode="render-value"
                         select="$metadata/gmd:identificationInfo/*/gmd:resourceConstraints/*/gmd:useLimitation/gmx:Anchor"/>
  </xsl:template>



  <xsl:template name="sextant-summary-view">
    <!--<xsl:for-each
      select="$metadata/gmd:identificationInfo/*/gmd:descriptiveKeywords/*[gmd:thesaurusName/*/gmd:identifier/*/gmd:code/* = 'geonetwork.thesaurus.local.theme.sextant-theme']/gmd:keyword">
      <xsl:variable name="path">
        <xsl:apply-templates mode="localised" select=".">
          <xsl:with-param name="langId" select="$langId"/>
        </xsl:apply-templates>
      </xsl:variable>

      <xsl:value-of select="string-join(tokenize($path, '/'), ' > ')"/>
    </xsl:for-each>-->


    <table class="table gn-sextant-view-table">

      <xsl:variable name="networkLink"
                    select="$metadata//gmd:CI_OnlineResource[gmd:protocol/*/text() = 'NETWORK:LINK']"/>
      <xsl:if test="count($networkLink) > 0">
        <tr class="md-network-link-description">
          <td>
            <xsl:call-template name="landingpage-label">
              <xsl:with-param name="key" select="'sxt-view-networkLink'"/>
            </xsl:call-template>
          </td>
          <td>
            <xsl:for-each select="$networkLink">
              <xsl:value-of select="gmd:linkage/gmd:URL"/>
              <br/>
            </xsl:for-each>
          </td>
        </tr>
      </xsl:if>

      <tr>
        <td>
          <xsl:call-template name="landingpage-label">
            <xsl:with-param name="key" select="'sxt-view-date'"/>
          </xsl:call-template>
        </td>
        <td>
          <xsl:for-each select="$metadata/gmd:identificationInfo/*/gmd:citation/*/gmd:date/*[gmd:date/* != '']">
            <dl>
              <!--<dt>
                <xsl:apply-templates mode="render-value" select="gmd:dateType/*/@codeListValue"/>
              </dt>-->

              <xsl:variable name="dateType">
                <xsl:apply-templates  mode="render-value" select="gmd:dateType/*/@codeListValue"/>
              </xsl:variable>


              <dd>
                <xsl:value-of select="if (matches(gmd:date/*, $dateFormatRegex)) then format-date(xs:date(tokenize(gmd:date/*, 'T')[1]), '[D01]-[M01]-[Y0001]') else gmd:date/*"/>
                <xsl:text>&#10;(</xsl:text>
                <xsl:copy-of select="if (count($dateType/element()) = 0) then concat(' ', $dateType, ' ') else  $dateType/element()"/>
                <xsl:text>)</xsl:text>
              </dd>
            </dl>
          </xsl:for-each>

          <xsl:variable name="temporalCoverageContent">
            <xsl:for-each select="$metadata/gmd:identificationInfo/*/gmd:extent/*/gmd:temporalElement/*/gmd:extent/*">
              <xsl:variable name="indeterminatePositionLabel">
                <xsl:apply-templates mode="render-value"
                                     select="gml:beginPosition/@indeterminatePosition"/>
              </xsl:variable>

              <xsl:if test="gml:beginPosition != '' or normalize-space($indeterminatePositionLabel) != ''">
                <xsl:value-of select="concat((normalize-space($indeterminatePositionLabel), $schemaStrings/sxt-view-temporal-from)[1], ' ')"/>
                <xsl:value-of select="if (matches(gml:beginPosition, $dateFormatRegex)) then format-date(xs:date(tokenize(gml:beginPosition, 'T')[1]), '[D01]-[M01]-[Y0001]') else gml:beginPosition"/>
                <!--                    <xsl:apply-templates mode="render-value" select="gml:beginPosition"/>-->
                <i class="fa fa-fw fa-arrow-right">&#160;</i>
              </xsl:if>

              <xsl:variable name="indeterminatePositionLabel">
                <xsl:apply-templates mode="render-value"
                                     select="gml:endPosition/@indeterminatePosition"/>
              </xsl:variable>
              <xsl:if test="gml:endPosition != '' or normalize-space($indeterminatePositionLabel) != ''">
                <xsl:value-of select="concat((normalize-space($indeterminatePositionLabel), $schemaStrings/sxt-view-temporal-to)[1], ' ')"/>
                <xsl:value-of select="if (matches(gml:endPosition, $dateFormatRegex)) then format-date(xs:date(tokenize(gml:endPosition, 'T')[1]), '[D01]-[M01]-[Y0001]') else gml:endPosition"/>
<!--                    <xsl:apply-templates mode="render-value" select="gml:endPosition"/>-->
              </xsl:if>

              <xsl:if test="gml:timePosition != ''">
                <xsl:value-of select="concat ($schemaStrings/sxt-view-temporal-at, ' ', gml:timePosition)"/>
              </xsl:if>
              &#160;
              <xsl:variable name="type">
                <xsl:call-template name="landingpage-label">
                  <xsl:with-param name="key" select="'sxt-view-temporal'"/>
                </xsl:call-template>
              </xsl:variable>
              (<xsl:copy-of select="if (count($type/element()) = 0) then concat(' ', $type, ' ') else $type/element()"/>)
              <br/>
            </xsl:for-each>
          </xsl:variable>

          <xsl:if test="$temporalCoverageContent != ''">
            <dl>
              <!--<dt>
                <xsl:call-template name="landingpage-label">
                  <xsl:with-param name="key" select="'sxt-view-temporal'"/>
                </xsl:call-template>
              </dt>-->
              <dd>
                <xsl:copy-of select="$temporalCoverageContent"/>
              </dd>
            </dl>
          </xsl:if>
        </td>
      </tr>
      <xsl:variable name="authors"
                    select="$metadata/gmd:identificationInfo/*/gmd:pointOfContact[*/gmd:role/*/@codeListValue = 'author']"/>
      <xsl:if test="count($authors) > 0">
        <tr>
          <td>
            <xsl:call-template name="landingpage-label">
              <xsl:with-param name="key" select="'sxt-view-author'"/>
            </xsl:call-template>
          </td>
          <td>
            <xsl:for-each select="$authors">
              <xsl:apply-templates mode="render-field"
                                   select=".">
                <xsl:with-param name="layout" select="'short'"/>
              </xsl:apply-templates>
              <br/>
            </xsl:for-each>
          </td>
        </tr>
      </xsl:if>


      <xsl:variable name="contacts"
                    select="$metadata/gmd:identificationInfo/*/gmd:pointOfContact[*/gmd:role/*/@codeListValue != 'author']"/>
      <xsl:if test="count($contacts) > 0">
        <tr>
          <td>
            <xsl:call-template name="landingpage-label">
              <xsl:with-param name="key" select="'sxt-view-contact'"/>
            </xsl:call-template>
          </td>
          <td>
            <xsl:for-each select="$contacts">
<!--            <xsl:for-each select="$contacts" group-by="">-->
              <xsl:apply-templates mode="render-field"
                                   select=".">
                <xsl:with-param name="layout" select="'short'"/>
              </xsl:apply-templates>
              <br/>
            </xsl:for-each>
          </td>
        </tr>
      </xsl:if>
      <!--<tr>
        <td></td>
        <td>
          <div class="">
            <i class="fa fa-tag"></i>

            <xsl:for-each select="$metadata/gmd:identificationInfo/*/gmd:descriptiveKeywords/*[gmd:type/*/@codeListValue = 'theme' and gmd:thesaurusName/*/gmd:identifier/*/gmd:code/* != 'geonetwork.thesaurus.local.theme.sextant-theme']/gmd:keyword">
              <span class="badge"><xsl:apply-templates mode="render-value" select="."/></span>
            </xsl:for-each>
          </div>
          <div class="">
            <i class="fa fa-pin"></i>
            <xsl:for-each select="$metadata/gmd:identificationInfo/*/gmd:descriptiveKeywords/*[gmd:type/*/@codeListValue = 'place']/gmd:keyword">
              <span class="badge"><xsl:apply-templates mode="render-value" select="."/></span>
            </xsl:for-each>
          </div>
          &lt;!&ndash;<xsl:apply-templates mode="render-field"
                               select="$metadata/gmd:identificationInfo/*/gmd:descriptiveKeywords"/>&ndash;&gt;
        </td>
      </tr>-->
      <xsl:variable name="credits"
                    select="$metadata/gmd:identificationInfo/*/gmd:credit[normalize-space(.) != '']"/>
      <xsl:if test="count($credits) > 0">
        <tr>
          <td>
            <xsl:call-template name="landingpage-label">
              <xsl:with-param name="key" select="'sxt-view-source'"/>
            </xsl:call-template>
          </td>
          <td>
            <xsl:for-each select="$credits">
              <xsl:apply-templates mode="render-value" select="."/>
              <br/>
            </xsl:for-each>
          </td>
        </tr>
      </xsl:if>

      <xsl:variable name="lineage"
                    select="$metadata/gmd:dataQualityInfo/*/gmd:lineage/*[normalize-space(gmd:statement) != '']"/>
      <xsl:if test="count($lineage) > 0">
        <tr>
          <td>
            <xsl:call-template name="landingpage-label">
              <xsl:with-param name="key" select="'sxt-view-lineage'"/>
            </xsl:call-template>
          </td>
          <td>
            <xsl:apply-templates mode="render-value"
                                 select="$metadata/gmd:dataQualityInfo/*/gmd:lineage/*/gmd:statement"/>
          </td>
        </tr>
      </xsl:if>
      <tr>
        <td>
          <xsl:call-template name="landingpage-label">
            <xsl:with-param name="key" select="'sxt-view-constraints'"/>
          </xsl:call-template>
        </td>
        <td class="gn-record-view_tablecell_aerate">

          <xsl:for-each select="$metadata/gmd:identificationInfo/*/gmd:resourceConstraints/*/gmd:useLimitation[normalize-space(.) != '']">
            <xsl:apply-templates mode="render-field" select="."/>
          </xsl:for-each>

          <xsl:for-each select="$metadata/gmd:identificationInfo/*/gmd:resourceConstraints/*/*[*/@codeListValue != 'otherRestrictions']">
            <xsl:apply-templates mode="render-field"
                                 select="."/>
          </xsl:for-each>

          <xsl:for-each select="$metadata/gmd:identificationInfo/*/gmd:resourceConstraints/*/gmd:otherConstraints">
            <xsl:apply-templates mode="render-field"
                                 select="."/>
          </xsl:for-each>
        </td>
      </tr>
      <tr>
        <td>
          <xsl:call-template name="landingpage-label">
            <xsl:with-param name="key" select="'sxt-view-geoinfo'"/>
          </xsl:call-template>
        </td>
        <td>
          <xsl:if test="$metadata/gmd:identificationInfo/*/gmd:spatialRepresentationType/*/@codeListValue != ''">
            <dl>
              <dt>
                <xsl:call-template name="landingpage-label">
                  <xsl:with-param name="key" select="'sxt-view-spatialRepresentationType'"/>
                </xsl:call-template>
              </dt>
              <dd>
                <xsl:apply-templates mode="render-value"
                                     select="$metadata/gmd:identificationInfo/*/gmd:spatialRepresentationType/*/@codeListValue"/>
              </dd>
            </dl>
          </xsl:if>


          <xsl:variable name="scales"
                        select="$metadata/gmd:identificationInfo/*/gmd:spatialResolution/*/gmd:equivalentScale/*/gmd:denominator/*[. != '']"/>
          <xsl:if test="count($scales) > 0">
            <dl>
             <dt>
               <xsl:call-template name="landingpage-label">
                 <xsl:with-param name="key" select="'sxt-view-scale'"/>
               </xsl:call-template>
              </dt>
              <dd>
                <xsl:for-each select="$scales">
                  <xsl:value-of select="concat('1:', .)"/>
                  <br/>
                </xsl:for-each>
              </dd>
            </dl>
          </xsl:if>
          <xsl:variable name="resolutions"
                        select="$metadata/gmd:identificationInfo/*/gmd:spatialResolution/*/gmd:distance/gco:Distance[. != '']"/>
          <xsl:if test="count($resolutions) > 0">
            <dl>
              <dt>
                <xsl:call-template name="landingpage-label">
                  <xsl:with-param name="key" select="'sxt-view-resolution'"/>
                </xsl:call-template>
              </dt>
              <dd>
                <xsl:for-each select="$resolutions">
                  <xsl:value-of select="concat(., ' ', ./@uom)"/>
                  <br/>
                </xsl:for-each>
              </dd>
            </dl>
          </xsl:if>

          <xsl:if test="$metadata/gmd:referenceSystemInfo">
            <dl>
              <dt>
                <xsl:call-template name="landingpage-label">
                  <xsl:with-param name="key" select="'sxt-view-crs'"/>
                </xsl:call-template>
              </dt>
              <dd>
                <xsl:value-of select="string-join($metadata/gmd:referenceSystemInfo/*/gmd:referenceSystemIdentifier/*/gmd:code/gco:CharacterString/text()[. != ''], ', ')"/>
              </dd>
            </dl>
          </xsl:if>
        </td>
      </tr>
      <xsl:if test="$portalLink = ''">
        <tr id="sextant-related">
          <td>
            <xsl:call-template name="landingpage-label">
              <xsl:with-param name="key" select="'sxt-view-related'"/>
            </xsl:call-template>
          </td>
          <td>
            <div gn-related="md"
                 data-user="user"
                 data-container="#sextant-related"
                 data-types="{$sideRelated}"><xsl:comment>.</xsl:comment>
            </div>
          </td>
        </tr>
      </xsl:if>
    </table>
    <br/>
  </xsl:template>
</xsl:stylesheet>
