<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:gn="http://www.fao.org/geonetwork"
  xmlns:gn-fn-metadata="http://geonetwork-opensource.org/xsl/functions/metadata"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" exclude-result-prefixes="#all">

  <!--
    Build the menu on top of the metadata  
  to switch view mode and tabs in a view. 
  -->
  <xsl:template name="menu-builder">
    <xsl:param name="config" as="node()"/>

    <xsl:variable name="currentView" select="$config/editor/view[tab/@id = $tab]"/>


    <ul class="nav nav-pills">

      <!-- Make a tab switcher for all tabs of the current view -->
      <xsl:if test="count($currentView/tab) > 1">
        <xsl:apply-templates mode="menu-builder" select="$config/editor/view[tab/@id = $tab]/tab"/>
      </xsl:if>



      <!-- Make a drop down choice to swith to one view to another -->
      <li class="dropdown pull-right">
        <a class="dropdown-toggle" data-toggle="dropdown" href="#">
          <xsl:value-of select="$i18n/selectView"/>
          <b class="caret"/>
        </a>
        <ul class="dropdown-menu">
          <!-- links -->
          <xsl:for-each select="$config/editor/view">
            <li>
              <xsl:if test="@name = $currentView/@name">
                <xsl:attribute name="class">disabled</xsl:attribute>
              </xsl:if>
              <!-- When a view contains multiple tab, the one with
                the default attribute is the one to open -->
              <a data-ng-click="switchToTab('{tab[@default]/@id}')">
                <xsl:variable name="viewName" select="@name"/>
                <xsl:value-of select="$strings/*[name() = $viewName]"/>
              </a>
            </li>
          </xsl:for-each>
        </ul>
      </li>
    </ul>
  </xsl:template>


  <!-- Create a link to a tab based on its identifier -->
  <xsl:template mode="menu-builder" match="tab">
    <li>
      <xsl:if test="$tab = @id">
        <xsl:attribute name="class">active</xsl:attribute>
      </xsl:if>
      <a data-ng-click="switchToTab('{@id}')">
        <xsl:variable name="tabId" select="@id"/>
        <xsl:value-of select="$strings/*[name() = $tabId]"/>
      </a>
    </li>
  </xsl:template>
</xsl:stylesheet>
