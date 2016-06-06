<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:exslt="http://exslt.org/common"
                xmlns:geonet="http://www.fao.org/geonetwork" xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                xmlns:date="http://exslt.org/dates-and-times"
                xmlns:saxon="http://saxon.sf.net/" version="2.0"
                extension-element-prefixes="saxon"
                exclude-result-prefixes="exslt xlink geonet svrl saxon date">


  <xsl:template name="md-content">
    <xsl:param name="title"/>
    <xsl:param name="logo"/>
    <xsl:param name="exportButton"/>
    <xsl:param name="abstract"/>
    <xsl:param name="relatedResources"/>
    <xsl:param name="tabs"/>

    <table class="gn">
      <tr>
        <td colspan="2">
          <span class="title">
            <xsl:copy-of select="$title"/>
          </span>
        </td>
      </tr>
      <tr>
        <td>
          <xsl:copy-of select="$abstract"/>
        </td>
        <td class="right">
          <xsl:copy-of select="$logo"/>
        </td>
      </tr>
      <tr>
        <td colspan="2">
          <xsl:copy-of select="$relatedResources"/>
          <xsl:copy-of select="$tabs"/>
        </td>
      </tr>
    </table>

  </xsl:template>


  <xsl:template name="simpleElement">
    <xsl:param name="id"/>
    <xsl:param name="type"/>
    <xsl:param name="title"/>
    <xsl:param name="help"/>
    <xsl:param name="content"/>

    <div class="el {$type}">
      <label for="{$id}">
        <xsl:attribute name="alt">
          <xsl:value-of select="$help"/>
        </xsl:attribute>
        <xsl:value-of select="$title"/>
      </label>
      <div>
        <xsl:copy-of select="$content"/>
      </div>
    </div>
  </xsl:template>


  <xsl:template name="mapView">
    <xsl:param name="e"/>
    <xsl:param name="n"/>
    <xsl:param name="s"/>
    <xsl:param name="w"/>

    <table>
      <tr>
        <td></td>
        <td>
          <xsl:value-of select="$n"/>
        </td>
        <td></td>
      </tr>
      <tr>
        <td>
          <xsl:value-of select="$e"/>
        </td>
        <td></td>
        <td>
          <xsl:value-of select="$w"/>
        </td>
      </tr>
      <tr>
        <td></td>
        <td>
          <xsl:value-of select="$s"/>
        </td>
        <td></td>
      </tr>
    </table>
  </xsl:template>


  <xsl:template name="thumbnailView">
    <!-- TODO -->
  </xsl:template>


  <xsl:template name="listElement">
    <!-- TODO -->
  </xsl:template>

  <!--
  Use to display a codelist value with its definition
  -->
  <xsl:template name="simpleElementWithDefinition">
    <xsl:param name="id"/>
    <xsl:param name="type"/>
    <xsl:param name="title"/>
    <xsl:param name="help"/>
    <xsl:param name="content"/>
    <xsl:param name="definition"/>

    <div class="{$type}">
      <label for="{$id}">
        <xsl:attribute name="alt">
          <xsl:value-of select="$help"/>
        </xsl:attribute>
        <xsl:value-of select="$title"/>
      </label>
      <span>
        <xsl:copy-of select="$content"/>
      </span>
      <span class="definition">
        <xsl:copy-of select="$definition"/>
      </span>
    </div>
  </xsl:template>


  <xsl:template name="complexElement">
    <xsl:param name="id"/>
    <xsl:param name="type"/>
    <xsl:param name="title"/>
    <xsl:param name="help"/>
    <xsl:param name="helpLink"/>
    <xsl:param name="content"/>

    <fieldset id="{$id}">
      <legend>
        <xsl:attribute name="alt">
          <xsl:value-of select="$help"/>
        </xsl:attribute>
        <xsl:if test="$helpLink">
          <xsl:attribute name="id">
            <xsl:value-of select="concat('stip.', $helpLink)"></xsl:value-of>
          </xsl:attribute>
        </xsl:if>
        <xsl:value-of select="$title"/>
      </legend>
      <xsl:copy-of select="$content"/>
    </fieldset>
  </xsl:template>


</xsl:stylesheet>
