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
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:srv="http://www.isotc211.org/2005/srv"
                xmlns:geonet="http://www.fao.org/geonetwork"
                xmlns:java="java:org.fao.geonet.util.XslUtil"
                version="2.0"
                exclude-result-prefixes="#all">


  <!-- Template used to return a gco:CharacterString element
      in default metadata language or in a specific locale
      if exist.
      FIXME : gmd:PT_FreeText should not be in the match clause as gco:CharacterString
      is mandatory and PT_FreeText optional. Added for testing GM03 import.
  -->
  <xsl:template name="localised" mode="localised" match="*[gco:CharacterString or gmd:PT_FreeText]">
    <xsl:param name="langId"/>

    <xsl:choose>
      <xsl:when
        test="gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString[@locale=$langId] and
                gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString[@locale=$langId] != ''">
        <xsl:value-of
          select="gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString[@locale=$langId]"
        />
      </xsl:when>
      <xsl:when test="not(gco:CharacterString)">
        <!-- If no CharacterString, try to use the first textGroup available -->
        <xsl:value-of
          select="gmd:PT_FreeText/gmd:textGroup[position()=1]/gmd:LocalisedCharacterString"
        />
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="gco:CharacterString"/>
      </xsl:otherwise>
    </xsl:choose>

  </xsl:template>

  <!-- Template used to match any other element eg. gco:Boolean, gco:Date
       when looking for localised strings -->
  <xsl:template mode="localised" match="*[not(gco:CharacterString or gmd:PT_FreeText)]">
    <xsl:param name="langId"/>
    <xsl:value-of select="*[1]"/>
  </xsl:template>

  <!-- Check if the element has hidden subelements -->
  <xsl:template mode="localised"
                match="*[@gco:nilReason='withheld' and count(./*) = 0 and count(./@*) = 1]"
                priority="100">
    <xsl:value-of select="/root/gui/strings/concealed"/>
  </xsl:template>

  <!-- Map GUI language to iso3code -->
  <xsl:template name="getLangId">
    <xsl:param name="langGui"/>
    <xsl:param name="md"/>

    <xsl:call-template name="getLangIdFromMetadata">
      <xsl:with-param name="lang" select="$langGui"/>
      <xsl:with-param name="md" select="$md"/>
    </xsl:call-template>
  </xsl:template>

  <!-- Get lang #id in metadata PT_Locale section,  deprecated: if not return the 2 first letters
      of the lang iso3code in uper case.

       if not return the lang iso3code in uper case.
      -->
  <xsl:template name="getLangIdFromMetadata">
    <xsl:param name="md"/>
    <xsl:param name="lang"/>

    <xsl:choose>
      <xsl:when
        test="$md/gmd:locale/gmd:PT_Locale[gmd:languageCode/gmd:LanguageCode/@codeListValue = $lang]/@id"
      >#<xsl:value-of
        select="$md/gmd:locale/gmd:PT_Locale[gmd:languageCode/gmd:LanguageCode/@codeListValue = $lang]/@id"
      />
      </xsl:when>
      <xsl:otherwise>#<xsl:value-of select="upper-case($lang)"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- Get lang codeListValue in metadata PT_Locale section,  if not return eng by default -->
  <xsl:template name="getLangCode">
    <xsl:param name="md"/>
    <xsl:param name="langId"/>

    <xsl:choose>
      <xsl:when
        test="$md/gmd:locale/gmd:PT_Locale[@id=$langId]/gmd:languageCode/gmd:LanguageCode/@codeListValue"
      >
        <xsl:value-of
          select="$md/gmd:locale/gmd:PT_Locale[@id=$langId]/gmd:languageCode/gmd:LanguageCode/@codeListValue"
        />
      </xsl:when>
      <xsl:otherwise>eng</xsl:otherwise>
    </xsl:choose>
  </xsl:template>


  <!-- Template to get metadata title using its uuid.
      Title is loaded from current language index if available.
      If not, default title is returned.
      If failed, return uuid. -->
  <xsl:template name="getMetadataTitle">
    <xsl:param name="uuid"/>
    <xsl:variable name="metadataTitle"
                  select="java:getIndexField(string(substring(/root/gui/url, 2)), string($uuid), 'title', string(/root/gui/language))"/>
    <xsl:choose>
      <xsl:when test="$metadataTitle=''">
        <xsl:variable name="metadataDefaultTitle"
                      select="java:getIndexField(string(substring(/root/gui/url, 2)), string($uuid), '_defaultTitle', string(/root/gui/language))"/>
        <xsl:choose>
          <xsl:when test="$metadataDefaultTitle=''">
            <xsl:value-of select="$uuid"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="$metadataDefaultTitle"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$metadataTitle"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>


  <!-- Display related metadata records. Related resource are only iso19139/119 or iso19110 metadate records for now.

        Related resources are:
        * parent metadata record (if gmd:parentIdentifier is set)
        * services (dataset only)
        * datasets (service only)
        * feature catalogues (dataset only)

        In view mode link to related resources are displayed
        In edit mode link to add elements are provided.
    -->
  <xsl:template name="relatedResources">
    <xsl:param name="edit"/>

    <xsl:variable name="metadata"
                  select="/root/gmd:MD_Metadata|/root/*[@gco:isoType='gmd:MD_Metadata']"/>
    <xsl:if test="starts-with(geonet:info/schema, 'iso19139') or geonet:info/schema = 'iso19110'">

      <xsl:variable name="uuid" select="$metadata/geonet:info/uuid"/>

      <xsl:variable name="isService" select="$metadata/gmd:identificationInfo/srv:SV_ServiceIdentification|
      $metadata/gmd:identificationInfo/*[contains(@gco:isoType, 'SV_ServiceIdentification')]"/>


      <!-- Related elements -->
      <xsl:variable name="parent" select="$metadata/gmd:parentIdentifier/gco:CharacterString"/>
      <xsl:variable name="services" select="/root/gui/relation/services/response/*[geonet:info]"/>
      <xsl:variable name="children" select="/root/gui/relation/children/response/*[geonet:info]"/>
      <xsl:variable name="siblings" select="/root/gui/relation/siblings/response/sibling"/>
      <xsl:variable name="relatedRecords"
                    select="/root/gui/relation/related/response/*[geonet:info]"/> <!-- Usually feature catalogues -->
      <!-- <xsl:variable name="relatedRecords" select="/root/gui/relation/fcats/response/*[geonet:info]"/> -->

      <!-- The GetCapabilities URL -->
      <xsl:variable name="capabilitiesUrl">
        <xsl:call-template name="getServiceURL">
          <xsl:with-param name="metadata" select="$metadata"/>
        </xsl:call-template>
      </xsl:variable>
      <xsl:if test="normalize-space($parent)!='' or $children or $services or $relatedRecords or count($siblings)>0 or
        $metadata/gmd:identificationInfo/(srv:SV_ServiceIdentification | *[contains(@gco:isoType, 'SV_ServiceIdentification')])/srv:operatesOn or $edit">

        <div class="relatedElements">
          <xsl:if test="count($siblings)>0 and not($edit)">
            <xsl:for-each-group select="$siblings" group-by="@initiative">
              <div>
                <h3 style="text-transform:capitalize;">
                  <xsl:value-of select="concat('related ',current-grouping-key(),'(s)')"/>
                </h3>
                <xsl:for-each select="current-group()/*/geonet:info">
                  <ul>
                    <li>
                      <a class="arrow" href="metadata.show?uuid={uuid}">
                        <xsl:call-template name="getMetadataTitle">
                          <xsl:with-param name="uuid" select="uuid"/>
                        </xsl:call-template>
                      </a>
                    </li>
                  </ul>
                </xsl:for-each>
                <br/>
              </div>
            </xsl:for-each-group>
          </xsl:if>

          <!-- Parent/child relation
                        displayed for both service and datasets metadata.

                        Display a tree representation of parent
                    -->
          <xsl:if
            test="(normalize-space($parent)!='' or $children or $edit) and geonet:info/schema != 'iso19110'">
            <h3>
              <img src="{/root/gui/url}/images/dataset.gif"
                   alt="{/root/gui/strings/linkedParentMetadataHelp}"
                   title="{/root/gui/strings/linkedParentMetadataHelp}" align="absmiddle"/>
              <xsl:value-of select="/root/gui/strings/linkedParentMetadata"/>
            </h3>

            <xsl:if test="normalize-space($parent)!='' or $children">
              <ul>
                <xsl:if test="normalize-space($parent)!=''">
                  <li>
                    <a class="arrow" href="metadata.show?uuid={$parent}">
                      <xsl:call-template name="getMetadataTitle">
                        <xsl:with-param name="uuid" select="$parent"/>
                      </xsl:call-template>
                    </a>
                  </li>
                </xsl:if>
                <li>
                  <ul>
                    <li>
                      <xsl:call-template name="getMetadataTitle">
                        <xsl:with-param name="uuid" select="$uuid"/>
                      </xsl:call-template>
                    </li>
                    <xsl:if test="$children">
                      <li>
                        <ul>
                          <xsl:for-each select="$children">
                            <li>
                              <a class="arrow" href="metadata.show?uuid={geonet:info/uuid}">
                                <xsl:call-template name="getMetadataTitle">
                                  <xsl:with-param name="uuid" select="geonet:info/uuid"/>
                                </xsl:call-template>
                              </a>
                            </li>
                          </xsl:for-each>
                        </ul>
                      </li>
                    </xsl:if>
                  </ul>
                </li>
              </ul>
            </xsl:if>

            <xsl:choose>
              <xsl:when test="$edit">
                <img src="{/root/gui/url}/images/plus.gif"
                     alt="{/root/gui/strings/linkedParentMetadataHelp}"
                     title="{/root/gui/strings/linkedParentMetadataHelp}" align="absmiddle"/>
                <xsl:text> </xsl:text>
                <a alt="{/root/gui/strings/linkedParentMetadataHelp}"
                   title="{/root/gui/strings/linkedParentMetadataHelp}"
                   href="javascript:doTabAction('metadata.update', 'metadata');">
                  <xsl:value-of select="/root/gui/strings/addParent"/>
                </a>
              </xsl:when>
              <xsl:otherwise>
                <!-- update child option only for iso19139 schema based metadata and admin user -->
                <!-- FIXME : on edit mode, we don't know how many child are here -->
                <xsl:variable name="profile" select="/root/gui/session/profile"/>
                <xsl:variable name="childCount"
                              select="/root/gui/relation/children/response/summary/@count"/>
                <xsl:variable name="childrenIds">
                  <xsl:for-each select="/root/gui/relation/children/response/metadata">
                    <xsl:value-of select="concat(geonet:info/id,',')"/>
                  </xsl:for-each>
                </xsl:variable>
                <xsl:if
                  test="($profile = 'Administrator' or $profile = 'Editor' or $profile = 'Reviewer' or $profile = 'UserAdmin') and $childCount &gt; 0">
                  <img src="{/root/gui/url}/images/plus.gif"
                       alt="{/root/gui/strings/updateChildren}"
                       title="{/root/gui/strings/updateChildren}" align="absmiddle"/>
                  <xsl:text> </xsl:text>
                  <a alt="{/root/gui/strings/updateChildren}"
                     title="{/root/gui/strings/updateChildren}"
                     href="#"
                     onclick="javascript:batchUpdateChildren('metadata.batch.children.form?id={$metadata/geonet:info/id}&amp;schema={$metadata/geonet:info/schema}&amp;parentUuid={$metadata/geonet:info/uuid}&amp;childrenIds={$childrenIds}','{/root/gui/strings/batchUpdateChildrenTitle}',800);">
                    <xsl:value-of select="/root/gui/strings/updateChildren"/>
                  </a>
                </xsl:if>
              </xsl:otherwise>
            </xsl:choose>
            <br/>
            <br/>
          </xsl:if>


          <!-- Services linked to a dataset using an operatesOn elements.
                        Not displayed for services. -->
          <xsl:if
            test="not($isService) and ($services or $edit) and geonet:info/schema != 'iso19110'">
            <xsl:if test="$services or $edit">
              <h3>
                <img src="{/root/gui/url}/images/service.gif"
                     alt="{/root/gui/strings/associateService}"
                     title="{/root/gui/strings/associateService}" align="absmiddle"/>
                <xsl:value-of select="/root/gui/strings/linkedServices"/>
              </h3>
              <ul>
                <xsl:for-each select="$services">
                  <li>
                    <a class="arrow" href="metadata.show?uuid={geonet:info/uuid}">
                      <xsl:call-template name="getMetadataTitle">
                        <xsl:with-param name="uuid" select="geonet:info/uuid"/>
                      </xsl:call-template>
                    </a>
                  </li>
                </xsl:for-each>
              </ul>
            </xsl:if>

            <xsl:if test="$edit">
              <!-- List of services available to help user editing -->
              <img src="{/root/gui/url}/images/plus.gif"
                   alt="{/root/gui/strings/associateServiceHelp}"
                   title="{/root/gui/strings/associateServiceHelp}" align="absmiddle"/>
              <xsl:text> </xsl:text>
              <a alt="{/root/gui/strings/associateServiceHelp}"
                 title="{/root/gui/strings/associateServiceHelp}"
                 href="#"
                 onclick="javascript:showLinkedServiceMetadataSelectionPanel('attachService', '{$capabilitiesUrl}', '{$uuid}');">
                <xsl:value-of select="/root/gui/strings/associateService"/>
              </a>
            </xsl:if>
            <br/>
            <br/>
          </xsl:if>


          <!-- Datasets linked to a service
                    . -->
          <xsl:if
            test="$isService and ($edit or $metadata/gmd:identificationInfo/(srv:SV_ServiceIdentification | *[contains(@gco:isoType, 'SV_ServiceIdentification')])/srv:operatesOn)">
            <h3>
              <img src="{/root/gui/url}/images/dataset.gif"
                   align="absmiddle"/>
              <xsl:value-of select="/root/gui/strings/linkedDatasetMetadata"/>
            </h3>
            <ul>
              <xsl:for-each
                select="$metadata/gmd:identificationInfo/(srv:SV_ServiceIdentification | *[contains(@gco:isoType, 'SV_ServiceIdentification')])/srv:operatesOn[@uuidref!='']">
                <li>
                  <a class="arrow" href="metadata.show?uuid={@uuidref}">
                    <xsl:call-template name="getMetadataTitle">
                      <xsl:with-param name="uuid" select="@uuidref"/>
                    </xsl:call-template>
                  </a>
                  <xsl:if test="$edit">
                    <!-- Allow deletion of coupledResource and operatesOn element -->
                    <xsl:text> </xsl:text>
                    <a
                      href="metadata.processing?uuid={$metadata/geonet:info/uuid}&amp;process=update-srv-detachDataset&amp;uuidref={@uuidref}">
                      <img alt="{/root/gui/strings/delete}" title="{/root/gui/strings/delete}"
                           src="{/root/gui/url}/images/del.gif"
                           align="absmiddle"
                      />
                    </a>
                  </xsl:if>
                </li>
              </xsl:for-each>
            </ul>

            <xsl:if test="$edit">
              <img alt="{/root/gui/strings/associateDatasetHelp}"
                   title="{/root/gui/strings/associateDatasetHelp}"
                   src="{/root/gui/url}/images/plus.gif"
                   align="absmiddle"/>
              <xsl:text> </xsl:text>
              <a alt="{/root/gui/strings/associateDatasetHelp}"
                 title="{/root/gui/strings/associateDatasetHelp}"
                 href="#"
                 onclick="javascript:showLinkedServiceMetadataSelectionPanel('coupledResource', '{$capabilitiesUrl}', '{$uuid}');">
                <xsl:value-of select="/root/gui/strings/associateDataset"/>
              </a>
            </xsl:if>
            <br/>
            <br/>
          </xsl:if>


          <!-- Feature Catalogue (not available for service metadata records)
                    . -->
          <xsl:choose>
            <!-- If feature catalogue, list related datasets -->
            <xsl:when test="geonet:info/schema = 'iso19110'">
              <h3>
                <img src="{/root/gui/url}/images/dataset.gif"
                     align="absmiddle"/>
                <xsl:value-of select="/root/gui/strings/linkedDataset"/>
              </h3>
              <ul>
                <xsl:for-each select="$relatedRecords">
                  <li>
                    <a class="arrow" href="metadata.show?uuid={geonet:info/uuid}">
                      <xsl:call-template name="getMetadataTitle">
                        <xsl:with-param name="uuid" select="geonet:info/uuid"/>
                      </xsl:call-template>
                    </a>
                  </li>
                </xsl:for-each>
              </ul>
              <!-- TODO : Add menu to link a dataset if needed -->
            </xsl:when>
            <xsl:otherwise>
              <xsl:if test="not($isService) and ($relatedRecords or $edit)">
                <h3>
                  <img src="{/root/gui/url}/images/dataset.gif"
                       align="absmiddle"/>
                  <xsl:value-of select="/root/gui/strings/linkedFeatureCatalogue"/>
                </h3>
                <ul>
                  <xsl:for-each select="$relatedRecords">
                    <li>
                      <a class="arrow" href="metadata.show?uuid={geonet:info/uuid}">
                        <xsl:call-template name="getMetadataTitle">
                          <xsl:with-param name="uuid" select="geonet:info/uuid"/>
                        </xsl:call-template>
                      </a>
                      <xsl:if test="$edit">
                        <!-- Allow deletion of coupledResource and operatesOn element -->
                        <xsl:text> </xsl:text>

                        <a href="#"
                           onclick="detachFeatureCatalogueMd('{$metadata/geonet:info/uuid}', '{geonet:info/uuid}', '{$metadata/geonet:info/id}')">
                          <img alt="{/root/gui/strings/delete}" title="{/root/gui/strings/delete}"
                               src="{/root/gui/url}/images/del.gif"
                               align="absmiddle"
                          />
                        </a>
                      </xsl:if>
                    </li>
                  </xsl:for-each>
                </ul>

                <xsl:if test="$edit">
                  <img alt="{/root/gui/strings/linkedFeatureCatalogueHelp}"
                       title="{/root/gui/strings/linkedFeatureCatalogueHelp}"
                       src="{/root/gui/url}/images/plus.gif"
                       align="absmiddle"/>
                  <xsl:text> </xsl:text>
                  <a alt="{/root/gui/strings/linkedFeatureCatalogueHelp}"
                     title="{/root/gui/strings/linkedFeatureCatalogueHelp}"
                     href="#"
                     onclick="javascript:showLinkedMetadataSelectionPanel(null, 'iso19110');">
                    <xsl:value-of select="/root/gui/strings/createLinkedFeatureCatalogue"/>
                  </a>
                </xsl:if>
                <br/>
                <br/>
              </xsl:if>

            </xsl:otherwise>
          </xsl:choose>
        </div>
      </xsl:if>

    </xsl:if>

  </xsl:template>


  <xsl:template mode="iso19139IsEmpty" match="*|@*|text()">
    <xsl:choose>
      <!-- normal element -->
      <xsl:when test="*">
        <xsl:apply-templates mode="iso19139IsEmpty"/>
      </xsl:when>
      <!-- text element -->
      <xsl:when test="text()!=''">txt</xsl:when>
      <!-- empty element -->
      <xsl:otherwise>
      </xsl:otherwise>
    </xsl:choose>

    <!-- attributes? -->
    <xsl:for-each select="@*">
      <xsl:if test="string-length(.)!=0">att</xsl:if>
    </xsl:for-each>
  </xsl:template>

  <!-- Create a service URL for a service metadata record. -->
  <xsl:template name="getServiceURL">
    <xsl:param name="metadata"/>

    <!-- Get Service URL from GetCapabilities Operation, if null from distribution information-->
    <xsl:variable name="serviceUrl">
      <xsl:value-of select="$metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/srv:containsOperations/srv:SV_OperationMetadata[srv:operationName/gco:CharacterString='GetCapabilities']/srv:connectPoint/gmd:CI_OnlineResource/gmd:linkage/gmd:URL|
        $metadata/gmd:identificationInfo/*[contains(@gco:isoType, 'SV_ServiceIdentification')]/srv:containsOperations/srv:SV_OperationMetadata[srv:operationName/gco:CharacterString='GetCapabilities']/srv:connectPoint/gmd:CI_OnlineResource/gmd:linkage/gmd:URL"/>
    </xsl:variable>

    <!-- TODO : here we could use service type and version if
            GetCapabilities url is not complete with parameter. -->
    <xsl:variable name="parameters">&amp;SERVICE=WMS&amp;VERSION=1.1.1&amp;REQUEST=GetCapabilities
    </xsl:variable>

    <xsl:choose>
      <xsl:when test="$serviceUrl=''">
        <!-- Search for URLs related to an OGC protocol in distribution section -->
        <xsl:variable name="urlFilter">OGC:WMS</xsl:variable>
        <xsl:variable name="distributionInfoUrl"
                      select="$metadata/gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource[contains(gmd:protocol[1]/gco:CharacterString, $urlFilter)]/gmd:linkage/gmd:URL"/>
        <xsl:value-of select="$distributionInfoUrl"/>
        <!-- FIXME ? Here we assume that only one URL is related to an OGC protocol which could not be the case in all situation.
                This service URL is used to initialize the LinkedServiceMetadataPanel to search for layers. It should be the case in most
                of service metadata records, but it could be different for metadata records referencing more than one OGC service. -->
        <xsl:if test="not(contains($distributionInfoUrl[position()=1], '?'))">
          <xsl:text>?</xsl:text>
        </xsl:if>
        <xsl:value-of select="$parameters"/>
      </xsl:when>
      <xsl:when test="not(contains($serviceUrl, '?'))">
        <xsl:value-of select="$serviceUrl"/>?<xsl:value-of select="$parameters"/>
      </xsl:when>
      <xsl:when test="not(contains($serviceUrl, 'GetCapabilities'))">
        <xsl:value-of select="$serviceUrl"/><xsl:value-of select="$parameters"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$serviceUrl"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>
