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
                xmlns:saxon="http://saxon.sf.net/"
                version="2.0"
                extension-element-prefixes="saxon"
                exclude-result-prefixes="#all">


  <xsl:template mode="getLicense" match="gmd:MD_Metadata[$view = 'sextant']">
    <xsl:apply-templates mode="render-value"
                         select="$metadata/gmd:identificationInfo/*/gmd:resourceConstraints/*/gmd:useLimitation/gmx:Anchor"/>
  </xsl:template>


  <xsl:template name="sextant-summary-view">
    <xsl:for-each
      select="$metadata/gmd:identificationInfo/*/gmd:descriptiveKeywords/*[gmd:thesaurusName/*/gmd:identifier/*/gmd:code/* = 'geonetwork.thesaurus.local.theme.sextant-theme']/gmd:keyword">
      <xsl:variable name="path">
        <xsl:apply-templates mode="localised" select=".">
          <xsl:with-param name="langId" select="$langId"/>
        </xsl:apply-templates>
      </xsl:variable>

      <xsl:value-of select="string-join(tokenize($path, '/'), ' > ')"/>
    </xsl:for-each>


    <table class="table gn-sextant-view-table">
      <tr>
        <td>
          <xsl:value-of select="$schemaStrings/sxt-view-date"/>
        </td>
        <td>

          <xsl:for-each select="$metadata/gmd:identificationInfo/*/gmd:citation/*/gmd:date/*">
            <div class="row">
              <div class="col-md-2">
                <xsl:apply-templates mode="render-value" select="gmd:dateType/*/@codeListValue"/>
              </div>
              <div class="col-md-10">
                <xsl:apply-templates mode="render-value" select="gmd:date"/>
              </div>
            </div>
          </xsl:for-each>

          <xsl:variable name="temporalCoverageContent">
            <xsl:for-each select="$metadata/gmd:identificationInfo/*/gmd:extent/*/gmd:temporalElement/*/gmd:extent/*">
              <xsl:variable name="indeterminatePositionLabel">
                <xsl:apply-templates mode="render-value"
                                     select="gml:beginPosition/@indeterminatePosition"/>
              </xsl:variable>

              <xsl:if test="gml:beginPosition != '' or normalize-space($indeterminatePositionLabel) != ''">
                <xsl:value-of
                  select="concat ((normalize-space($indeterminatePositionLabel), $schemaStrings/sxt-view-temporal-from)[1], ' ', gml:beginPosition, ' > ')"/>
              </xsl:if>

              <xsl:variable name="indeterminatePositionLabel">
                <xsl:apply-templates mode="render-value"
                                     select="gml:endPosition/@indeterminatePosition"/>
              </xsl:variable>
              <xsl:if test="gml:endPosition != '' or normalize-space($indeterminatePositionLabel) != ''">
                <xsl:value-of
                  select="concat ((normalize-space($indeterminatePositionLabel), $schemaStrings/sxt-view-temporal-to)[1], ' ', gml:endPosition)"/>
              </xsl:if>

              <xsl:if test="gml:timePosition != ''">
                <xsl:value-of select="concat ($schemaStrings/sxt-view-temporal-at, ' ', gml:timePosition)"/>
              </xsl:if>
              <br/>
            </xsl:for-each>
          </xsl:variable>

          <xsl:if test="$temporalCoverageContent != ''">
            <div class="row">
              <div class="col-md-2">
                <xsl:value-of select="$schemaStrings/sxt-view-temporal"/>
              </div>
              <div class="col-md-10">
                <xsl:copy-of select="$temporalCoverageContent"/>
              </div>
            </div>
          </xsl:if>

        </td>
      </tr>
      <xsl:variable name="authors"
                    select="$metadata/gmd:identificationInfo/*/gmd:pointOfContact[*/gmd:role/*/@codeListValue = 'author']"/>
      <xsl:if test="count($authors) > 0">
        <tr>
          <td>
            <xsl:value-of select="$schemaStrings/sxt-view-author"/>
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
      <tr>
        <td>
          <xsl:value-of select="$schemaStrings/sxt-view-contact"/>
        </td>
        <td>
          <xsl:for-each
            select="$metadata/gmd:identificationInfo/*/gmd:pointOfContact[*/gmd:role/*/@codeListValue != 'author']">
            <xsl:apply-templates mode="render-field"
                                 select=".">
              <xsl:with-param name="layout" select="'short'"/>
            </xsl:apply-templates>
            <br/>
          </xsl:for-each>
        </td>
      </tr>
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
            <xsl:value-of select="$schemaStrings/sxt-view-source"/>
          </td>
          <td>
            <xsl:for-each select="$credits">
              <xsl:apply-templates mode="render-value" select="."/>
              <br/>
            </xsl:for-each>
          </td>
        </tr>
      </xsl:if>
      <tr>
        <td>
          <xsl:value-of select="$schemaStrings/sxt-view-lineage"/>
        </td>
        <td>
          <xsl:apply-templates mode="render-value"
                               select="$metadata/gmd:dataQualityInfo/*/gmd:lineage/*/gmd:statement"/>
        </td>
      </tr>
      <tr>
        <td>
          <xsl:value-of select="$schemaStrings/sxt-view-constraints"/>
        </td>
        <td>

          <xsl:apply-templates mode="render-field"
                               select="$metadata/gmd:identificationInfo/*/gmd:resourceConstraints/*/gmd:useLimitation"/>

          <xsl:apply-templates mode="render-field"
                               select="$metadata/gmd:identificationInfo/*/gmd:resourceConstraints/*/*[*/@codeListValue != 'otherRestrictions']"/>

          <xsl:apply-templates mode="render-field"
                               select="$metadata/gmd:identificationInfo/*/gmd:resourceConstraints/*/gmd:otherConstraints"/>
        </td>
      </tr>
      <tr>
        <td>
          <xsl:value-of select="$schemaStrings/sxt-view-geoinfo"/>
        </td>
        <td>
          <div class="row">
            <div class="col-md-6">
              <xsl:if test="$metadata/gmd:identificationInfo/*/gmd:spatialRepresentationType/*/@codeListValue != ''">
                <div class="row">
                  <div class="col-md-5">
                    <xsl:value-of select="$schemaStrings/sxt-view-spatialRepresentationType"/>
                  </div>
                  <div class="col-md-7">
                    <xsl:apply-templates mode="render-value"
                                         select="$metadata/gmd:identificationInfo/*/gmd:spatialRepresentationType/*/@codeListValue"/>
                  </div>
                </div>
              </xsl:if>
              <xsl:if test="$metadata/gmd:referenceSystemInfo">
                <div class="row">
                  <div class="col-md-5">
                    <xsl:value-of select="$schemaStrings/sxt-view-crs"/>
                  </div>
                  <div class="col-md-7">
                    <xsl:for-each select="$metadata/gmd:referenceSystemInfo/*/gmd:referenceSystemIdentifier/*/gmd:code">
                      <xsl:apply-templates mode="render-value" select="."/>
                      <br/>
                    </xsl:for-each>
                  </div>
                </div>
              </xsl:if>
            </div>
            <div class="col-md-6">
              <xsl:variable name="scales"
                            select="$metadata/gmd:identificationInfo/*/gmd:spatialResolution/*/gmd:equivalentScale/*/gmd:denominator/*[. != '']"/>
              <xsl:if test="count($scales) > 0">
                <div class="row">
                  <div class="col-md-5">
                    <xsl:value-of select="$schemaStrings/sxt-view-scale"/>
                  </div>
                  <div class="col-md-7">
                    <xsl:for-each select="$scales">
                      <xsl:value-of select="concat('1:', .)"/>
                      <br/>
                    </xsl:for-each>
                  </div>
                </div>
              </xsl:if>
              <xsl:variable name="resolutions"
                            select="$metadata/gmd:identificationInfo/*/gmd:spatialResolution/*/gmd:distance/gco:Distance[. != '']"/>
              <xsl:if test="count($resolutions) > 0">
                <div class="row">
                  <div class="col-md-5">
                    <xsl:value-of select="$schemaStrings/sxt-view-resolution"/>
                  </div>
                  <div class="col-md-7">
                    <xsl:for-each select="$resolutions">
                      <xsl:value-of select="concat(., ' ', ./@uom)"/>
                      <br/>
                    </xsl:for-each>
                  </div>
                </div>
              </xsl:if>
            </div>
          </div>
        </td>
      </tr>
    </table>

    <div gn-related="md"
         data-user="user"
         data-types="{$sideRelated}">
    </div>

  </xsl:template>
</xsl:stylesheet>
