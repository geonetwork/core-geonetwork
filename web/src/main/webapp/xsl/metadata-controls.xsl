<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:exslt="http://exslt.org/common"
                xmlns:str="http://exslt.org/strings"
                xmlns:geonet="http://www.fao.org/geonetwork"
                version="2.0"
                exclude-result-prefixes="exslt geonet str">

  <xsl:template name="getButtons">
    <xsl:param name="addLink"/>
    <xsl:param name="addXMLFragment"/>
    <xsl:param name="removeLink"/>
    <xsl:param name="upLink"/>
    <xsl:param name="downLink"/>
    <xsl:param name="validationLink"/>
    <xsl:param name="id"/>


    <span id="buttons_{$id}">
      <!--
                add as remote XML fragment button when relevant -->
      <xsl:if test="normalize-space($addXMLFragment)">
        <xsl:variable name="xlinkTokens" select="tokenize($addXMLFragment,'!')"/>
        <xsl:text> </xsl:text>
        <xsl:choose>
          <xsl:when test="normalize-space($xlinkTokens[2])">
            <a id="addXlink_{$id}" onclick="if (noDoubleClick()) {$xlinkTokens[1]}"
               style="display:none;">
              <img src="{/root/gui/url}/images/find.png" alt="{/root/gui/strings/addXMLFragment}"
                   title="{/root/gui/strings/addXMLFragment}"/>
            </a>
          </xsl:when>
          <xsl:otherwise>
            <a id="addXlink_{$id}" onclick="{$addXMLFragment}" style="cursor:pointer;">
              <img src="{/root/gui/url}/images/find.png" alt="{/root/gui/strings/addXMLFragment}"
                   title="{/root/gui/strings/addXMLFragment}"/>
            </a>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:if>


      <!-- add button -->
      <xsl:choose>
        <xsl:when test="normalize-space($addLink)">
          <xsl:variable name="linkTokens" select="tokenize($addLink,'!')"/>
          <xsl:text> </xsl:text>
          <xsl:choose>
            <xsl:when test="normalize-space($linkTokens[2])">
              <a id="add_{$id}" style="display:none;cursor:hand;cursor:pointer;"
                 onclick="if (noDoubleClick()) {$linkTokens[1]}" target="_blank">
                <img src="{/root/gui/url}/images/plus.gif" alt="{/root/gui/strings/add[not(@js)]}"
                     title="{/root/gui/strings/add[not(@js)]}"/>
              </a>
            </xsl:when>
            <xsl:otherwise>
              <a id="add_{$id}" style="cursor:hand;cursor:pointer;"
                 onclick="if (noDoubleClick()) {$addLink}" target="_blank">
                <img src="{/root/gui/url}/images/plus.gif" alt="{/root/gui/strings/add[not(@js)]}"
                     title="{/root/gui/strings/add[not(@js)]}"/>
              </a>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:when>
        <xsl:otherwise>
          <span id="add_{$id}"/>
        </xsl:otherwise>
      </xsl:choose>


      <!-- remove button -->
      <xsl:choose>
        <xsl:when test="normalize-space($removeLink)">
          <xsl:variable name="linkTokens" select="tokenize($removeLink,'!')"/>
          <xsl:text> </xsl:text>
          <xsl:choose>
            <xsl:when test="normalize-space($linkTokens[2])">
              <a id="remove_{$id}" style="display:none;cursor:hand;cursor:pointer;"
                 onclick="if (noDoubleClick()) {$linkTokens[1]}" target="_blank">
                <img src="{/root/gui/url}/images/del.gif" alt="{/root/gui/strings/del}"
                     title="{/root/gui/strings/del}"/>
              </a>
            </xsl:when>
            <xsl:otherwise>
              <a id="remove_{$id}" style="cursor:hand;cursor:pointer;"
                 onclick="if (noDoubleClick()) {$removeLink}" target="_blank">
                <img src="{/root/gui/url}/images/del.gif" alt="{/root/gui/strings/del}"
                     title="{/root/gui/strings/del}"/>
              </a>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:when>
        <xsl:otherwise>
          <span id="remove_{$id}"/>
        </xsl:otherwise>
      </xsl:choose>

      <!-- up button -->
      <xsl:choose>
        <xsl:when test="normalize-space($upLink)">
          <xsl:variable name="linkTokens" select="tokenize($upLink,'!')"/>
          <xsl:text> </xsl:text>
          <xsl:choose>
            <xsl:when test="normalize-space($linkTokens[2])">
              <a id="up_{$id}" style="display:none;cursor:hand;cursor:pointer;"
                 onclick="if (noDoubleClick()) {$linkTokens[1]}" target="_blank">
                <img src="{/root/gui/url}/images/up.gif" alt="{/root/gui/strings/up}"
                     title="{/root/gui/strings/up}"/>
              </a>
            </xsl:when>
            <xsl:otherwise>
              <a id="up_{$id}" style="cursor:hand;cursor:pointer;"
                 onclick="if (noDoubleClick()) {$upLink}" target="_blank">
                <img src="{/root/gui/url}/images/up.gif" alt="{/root/gui/strings/up}"
                     title="{/root/gui/strings/up}"/>
              </a>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:when>
        <xsl:otherwise>
          <span id="up_{$id}"/>
        </xsl:otherwise>
      </xsl:choose>

      <!-- down button -->
      <xsl:choose>
        <xsl:when test="normalize-space($downLink)">
          <xsl:variable name="linkTokens" select="tokenize($downLink,'!')"/>
          <xsl:text> </xsl:text>
          <xsl:choose>
            <xsl:when test="normalize-space($linkTokens[2])">
              <a id="down_{$id}" style="display:none;cursor:hand;cursor:pointer;"
                 onclick="if (noDoubleClick()) {$linkTokens[1]}" target="_blank">
                <img src="{/root/gui/url}/images/down.gif" alt="{/root/gui/strings/down}"
                     title="{/root/gui/strings/down}"/>
              </a>
            </xsl:when>
            <xsl:otherwise>
              <a id="down_{$id}" style="cursor:hand;cursor:pointer;"
                 onclick="if (noDoubleClick()) {$downLink}" target="_blank">
                <img src="{/root/gui/url}/images/down.gif" alt="{/root/gui/strings/down}"
                     title="{/root/gui/strings/down}"/>
              </a>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:when>
        <xsl:otherwise>
          <span id="down_{$id}"/>
        </xsl:otherwise>
      </xsl:choose>

      <!-- xsd and schematron validation error button -->
      <xsl:if test="normalize-space($validationLink)">
        <xsl:text> </xsl:text>
        <a id="validationError{$id}" onclick="setBunload(false);"
           href='javascript:doEditorAlert("error_{$id}", "errorimg_{$id}");'>
          <img id="errorimg_{$id}" src="{/root/gui/url}/images/validationError.gif"/>
        </a>
        <div style="display:none;" class="toolTipOverlay" id="error_{$id}"
             onclick="this.style.display='none';">
          <xsl:copy-of select="$validationLink"></xsl:copy-of>
        </div>
      </xsl:if>
    </span>
  </xsl:template>

  <!-- Template to display a calendar with a clear button -->
  <xsl:template name="calendar">
    <xsl:param name="ref"/>
    <xsl:param name="date"/>
    <xsl:param name="format" select="'%Y-%m-%d'"/>

    <table width="100%">
      <tr>
        <td>
          <div class="cal" id="_{$ref}"></div>
          <input type="hidden" id="_{$ref}_format" value="{$format}"/>
          <input type="hidden" id="_{$ref}_cal" value="{$date}"/>
        </td>
      </tr>
    </table>
  </xsl:template>

</xsl:stylesheet>
