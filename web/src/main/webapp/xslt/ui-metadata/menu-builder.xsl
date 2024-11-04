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

<xsl:stylesheet xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:gn-fn-metadata="http://geonetwork-opensource.org/xsl/functions/metadata"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:saxon="http://saxon.sf.net/"
                version="2.0"
                extension-element-prefixes="saxon"
                exclude-result-prefixes="#all">

  <!--
    Build the menu on top of the metadata
  to switch view mode and tabs in a view.
  -->
  <xsl:template name="menu-builder">
    <xsl:param name="config" as="node()"/>
    <xsl:variable name="currentView" select="$config/editor/views/view[tab/@id = $tab]"/>

    <div class="gn-scroll-spy"
         data-gn-scroll-spy="gn-editor-{$metadataId}"
         data-watch=""
         data-all-depth="{if ($isFlatMode) then 'true' else 'false'}"/>

    <div class="nav nav-tabs">
      <!-- Make a drop down choice to swith to one view to another -->
      <span id="gn-view-menu-{$metadataId}" class="btn-group">
        <button type="button"
                class="btn btn-default navbar-btn dropdown-toggle"
                data-toggle="dropdown"
                aria-label="{$i18n/selectView}"
                title="{$i18n/selectView}"
                aria-expanded="false">
          <i class="fa fa-fw fa-eye"></i>
          <span class="caret"></span>
        </button>
        <ul class="dropdown-menu" role="menu">
          <!-- links -->
          <xsl:choose>
            <xsl:when test="$isTemplate = ('s', 't')">
              <li role="menuitem">
                <xsl:if test="'simple' = $currentView/@name">
                  <xsl:attribute name="class">disabled</xsl:attribute>
                </xsl:if>
                <a data-ng-click="switchToTab('simple', '')" href="">
                  <xsl:value-of select="$strings/*[name() = 'simple']"/>
                </a>
              </li>
              <li role="menuitem">
                <xsl:if test="'xml' = $currentView/@name">
                  <xsl:attribute name="class">disabled</xsl:attribute>
                </xsl:if>
                <a data-ng-click="switchToTab('xml', '')" href="">
                  <xsl:value-of select="$strings/*[name() = 'xml']"/>
                </a>
              </li>
            </xsl:when>
            <xsl:otherwise>
              <xsl:for-each select="$config/editor/views/view[not(@disabled='true')]">

                <xsl:variable name="isViewDisplayed"
                              as="xs:boolean"
                              select="gn-fn-metadata:check-elementandsession-visibility(
                                        $schema, $metadata, $serviceInfo,
                                        @displayIfRecord,
                                        @displayIfServiceInfo)"/>


                <xsl:if test="$isViewDisplayed">
                  <li role="menuitem">
                    <xsl:if test="@name = $currentView/@name">
                      <xsl:attribute name="class">disabled</xsl:attribute>
                    </xsl:if>
                    <!-- When a view contains multiple tab, the one with
                  the default attribute is the one to open -->
                    <xsl:variable name="defaultTab"
                                  select="tab[@default and gn-fn-metadata:check-elementandsession-visibility($schema, $metadata, $serviceInfo, @displayIfRecord, @displayIfServiceInfo)]"/>
                    <a data-ng-click="switchToTab('{$defaultTab/@id}', '{$defaultTab/@mode}')"
                       href="">
                      <xsl:variable name="viewName" select="@name"/>
                      <xsl:value-of select="($strings/*[name() = $viewName]|$viewName)[1]"/>
                    </a>
                  </li>
                </xsl:if>
              </xsl:for-each>

              <li class="divider" role="menuitem"/>
              <li role="menuitem">
                <a data-ng-click="toggleAttributes(true)" href="">
                  <i class="fa"
                     data-ng-class="gnCurrentEdit.displayAttributes ? 'fa-check-square-o' : 'fa-square-o'"/>
                  &#160;
                  <span data-translate="">toggleAttributes</span>
                </a>
              </li>
              <li role="menuitem">
                <a data-ng-click="toggleTooltips(true)" href="">
                  <i class="fa"
                     data-ng-class="gnCurrentEdit.displayTooltips ? 'fa-check-square-o' : 'fa-square-o'"/>
                  &#160;
                  <span data-translate="">toggleTooltips</span>
                </a>
              </li>
            </xsl:otherwise>
          </xsl:choose>
        </ul>
      </span>

      <!-- Make a tab switcher for all tabs of the current view -->
      <xsl:if test="count($currentView/tab) > 1">
        <xsl:apply-templates mode="menu-builder"
                             select="$config/editor/views/view[tab/@id = $tab]/tab[not(@toggle)]"/>


        <!-- Some views may define tab to be grouped in an extra button -->
        <xsl:if test="count($config/editor/views/view[tab/@id = $tab]/tab[@toggle]) > 0">
          <li class="dropdown" role="menuitem">
            <a class="dropdown-toggle" data-toggle="dropdown" href=""
               title="{$i18n/moreTabs}">
              <i class="fa fa-ellipsis-h"></i>
              <b class="caret"/>
            </a>
            <ul class="dropdown-menu" role="menu">
              <!-- links -->
              <xsl:for-each select="$config/editor/views/view[tab/@id = $tab]/tab[@toggle]">
                <li role="menuitem">
                  <xsl:if test="$tab = @id">
                    <xsl:attribute name="class">disabled</xsl:attribute>
                  </xsl:if>
                  <a href="">
                    <xsl:if test="$tab != @id">
                      <xsl:attribute name="data-ng-click"
                                     select="concat('switchToTab(''', @id, ''', ''', @mode, ''')')"/>
                    </xsl:if>
                    <xsl:variable name="tabId" select="@id"/>
                    <xsl:value-of select="($strings/*[name() = $tabId]|$tabId)[1]"/>
                  </a>
                </li>
              </xsl:for-each>
            </ul>
          </li>
        </xsl:if>
      </xsl:if>

    </div>
  </xsl:template>


  <!-- Create a link to a tab based on its identifier -->
  <xsl:template mode="menu-builder" match="tab">
    <xsl:variable name="isTabDisplayed"
                  as="xs:boolean"
                  select="gn-fn-metadata:check-elementandsession-visibility(
                                        $schema, $metadata, $serviceInfo,
                                        @displayIfRecord,
                                        @displayIfServiceInfo)"/>
    <!-- When tab displayIf filter return false,
     if hideIfNotDisplayed is set to true for the tab, the tab is hidden
     else the tab is disabled.
    -->
    <xsl:if test="$isTabDisplayed or (not(@hideIfNotDisplayed))">
      <li role="menuitem"
          class="{if ($tab = @id) then 'active' else ''} {if ($isTabDisplayed) then '' else 'disabled'}">
        <a href="">
          <xsl:if test="$tab != @id and $isTabDisplayed">
            <xsl:attribute name="data-ng-click"
                           select="concat('switchToTab(''', @id, ''', ''', @mode, ''')')"/>
          </xsl:if>
          <xsl:variable name="tabId" select="@id"/>
          <xsl:variable name="tabLabel" select="$strings/*[name() = $tabId]"/>
          <xsl:value-of select="($tabLabel|$tabId)[1]"/>
        </a>
      </li>
    </xsl:if>

  </xsl:template>
</xsl:stylesheet>
