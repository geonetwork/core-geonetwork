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
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:gndoc="http://geonetwork-opensource.org/doc"
                xmlns:saxon="http://saxon.sf.net/"
                xmlns:java="http://www.java.com/"
                xmlns:digest="org.apache.commons.codec.digest.DigestUtils"
                extension-element-prefixes="saxon"
                exclude-result-prefixes="#all"
                version="2.0">

  <xsl:output method="text" encoding="UTF-8"/>
  <xsl:strip-space elements="*"/>

  <xsl:output name="default-serialize-mode"
              indent="yes"
              omit-xml-declaration="yes"
              encoding="utf-8"
              escape-uri-attributes="yes"/>

  <xsl:function xmlns:file="java.io.File"
                name="java:file-exists"
                as="xs:boolean">
    <xsl:param name="file" as="xs:string"/>
    <xsl:param name="base-uri" as="xs:string"/>

    <xsl:variable name="absolute-uri"
                  select="resolve-uri($file, $base-uri)"
                  as="xs:anyURI"/>
    <xsl:sequence select="file:exists(file:new($absolute-uri))"/>
  </xsl:function>


  <!-- Write a line -->
  <xsl:function name="gndoc:writeln">
    <xsl:param name="line" as="xs:string?"/>
    <xsl:text>&#xA;</xsl:text><xsl:value-of select="normalize-space($line)"/>
  </xsl:function>

  <xsl:function name="gndoc:writelnhtml">
    <xsl:param name="line"/>

    <xsl:if test="normalize-space($line) != ''">
      <xsl:text>&#xA;.. raw:: html</xsl:text>
      <xsl:text>&#xA;</xsl:text>
      <xsl:text>&#xA;</xsl:text>
      <!--<xsl:text>&#xA;  &lt;embed&gt;</xsl:text>-->
      <xsl:for-each select="tokenize($line, '\n')">
        <xsl:text>&#xA;  </xsl:text>
        <xsl:copy-of select="."/>
      </xsl:for-each>
      <!--<xsl:text>&#xA;  &lt;/embed&gt;</xsl:text>-->
      <xsl:text>&#xA;</xsl:text>
    </xsl:if>
  </xsl:function>


  <xsl:function name="gndoc:writelnfield">
    <xsl:param name="line" as="xs:string?"/>
    <xsl:text>&#xA;</xsl:text>
    <xsl:text>&#xA;:</xsl:text><xsl:value-of select="normalize-space($line)"/>:
  </xsl:function>
  <xsl:function name="gndoc:writelnfield">
    <xsl:param name="field" as="xs:string?"/>
    <xsl:param name="value" as="xs:string?"/>
    <xsl:text>&#xA;</xsl:text>
    <xsl:text>&#xA;:</xsl:text><xsl:value-of select="normalize-space($field)"/>:
    <xsl:text>&#xA;    </xsl:text><xsl:value-of
    select="replace(normalize-space($value), '\*', '\\*')"/>
  </xsl:function>

  <!-- Write line and underline it -->
  <xsl:function name="gndoc:writeln">
    <xsl:param name="line" as="xs:string?"/>
    <xsl:param name="underline" as="xs:string?"/>
    <xsl:text>&#xA;</xsl:text><xsl:value-of select="normalize-space($line)"/>
    <xsl:text>&#xA;</xsl:text><xsl:value-of
    select="replace(normalize-space($line), '.', $underline)"/>
    <xsl:text>&#xA;</xsl:text>
  </xsl:function>


  <!-- Create a RST reference. Prefixed by schema identifier to have them unique
   accross all documentation refs. -->
  <xsl:function name="gndoc:ref">
    <xsl:param name="id" as="xs:string?"/>
    <xsl:param name="hash" as="xs:boolean?"/>

    <!-- Append a hash because in RST, ref are not space and case sensitive -->
    <xsl:choose>
      <xsl:when test="$hash">
        <xsl:variable name="hash" select="digest:md5Hex(normalize-space($id))"/>
        <xsl:text>&#xA;.. _</xsl:text><xsl:value-of
        select="concat(replace($id, ':', '-'), '-', $hash)"/>:
      </xsl:when>
      <xsl:otherwise>
        <xsl:text>&#xA;.. _</xsl:text><xsl:value-of select="replace($id, ':', '-')"/>:
      </xsl:otherwise>
    </xsl:choose>
    <xsl:text>&#xA;</xsl:text>
  </xsl:function>

  <xsl:function name="gndoc:refTo">
    <xsl:param name="id" as="xs:string?"/>
    <xsl:param name="hash" as="xs:boolean?"/>

    <!-- Append a hash because in RST, ref are not space and case sensitive -->
    <xsl:choose>
      <xsl:when test="$hash">
        <xsl:variable name="hash" select="digest:md5Hex(normalize-space($id))"/>
        <xsl:text>:ref:`</xsl:text><xsl:value-of
        select="concat(replace($id, ':', '-'), '-', $hash)"/>`
      </xsl:when>
      <xsl:otherwise>
        <xsl:text>:ref:`</xsl:text><xsl:value-of select="replace($id, ':', '-')"/>`
      </xsl:otherwise>
    </xsl:choose>
  </xsl:function>

  <xsl:function name="gndoc:figure">
    <xsl:param name="folder" as="xs:string?"/>
    <xsl:param name="image" as="xs:string?"/>

    <xsl:choose>
      <xsl:when
        xmlns:file="java.io.File"
        test="file:exists(file:new(concat($folder, '/', $image)))">
        <xsl:text>&#xA;.. figure:: </xsl:text><xsl:value-of select="$image"/><xsl:text>&#xA;</xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:if test="$verbose">
          <xsl:message>* Missing figure <xsl:value-of select="concat($folder, '/', $image, '. Not added to the doc.')"/></xsl:message>
        </xsl:if>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:function>


  <!-- max-line-length -->
  <xsl:function name="gndoc:mll" as="xs:integer">
    <xsl:param name="arg" as="node()*"/>
    <xsl:sequence select="max(
                     for $line in $arg
                     return string-length($line))"/>
  </xsl:function>


  <!-- Table builder

      =======  ==========
      code     label
      =======  ==========
      after    Après
      before   Avant
      now      Maintenant
      unknown  Inconnu
      =======  ==========
  -->
  <xsl:function name="gndoc:table">
    <xsl:param name="rows" as="node()*"/>
    <!-- Hide in edit mode-->

    <xsl:variable name="s" select="'  '"/>
    <xsl:value-of select="gndoc:nl(2)"/>

    <!-- Rows -->
    <xsl:for-each select="$rows">
      <!-- TODO: Should we sort ? -->
      <!-- Header -->
      <xsl:if test="position() = 1">
        <!-- Cols -->
        <xsl:for-each select="*">
          <xsl:variable name="colName" select="name()"/>
          <xsl:variable name="values">
            <xsl:copy-of select="$rows/*[name() = $colName]"/>
            <label>
              <xsl:value-of select="$colName"/>
            </label>
          </xsl:variable>
          <xsl:for-each select="1 to gndoc:mll($values)">=</xsl:for-each>
          <xsl:if test="position() != last()">
            <xsl:value-of select="$s"/>
          </xsl:if>
        </xsl:for-each>
        <xsl:text>&#xA;</xsl:text>
        <!-- Cols label -->
        <xsl:for-each select="*">
          <xsl:variable name="colName" select="name()"/>
          <xsl:variable name="values">
            <xsl:copy-of select="$rows/*[name() = $colName]"/>
            <label>
              <xsl:value-of select="$colName"/>
            </label>
          </xsl:variable>
          <xsl:variable name="maxColLength"
                        select="gndoc:mll($values)"/>
          <xsl:variable name="missingSpaces"
                        select="$maxColLength - string-length($colName)"/>

          <xsl:value-of select="$colName"/>
          <xsl:if test="position() != last()">
            <xsl:value-of select="$s"/>
          </xsl:if>
          <xsl:for-each select="1 to $missingSpaces">
            <xsl:text> </xsl:text>
          </xsl:for-each>
        </xsl:for-each>
        <xsl:text>&#xA;</xsl:text>
        <xsl:for-each select="*">
          <xsl:variable name="colName" select="name()"/>
          <xsl:variable name="values">
            <xsl:copy-of select="$rows/*[name() = $colName]"/>
            <label>
              <xsl:value-of select="$colName"/>
            </label>
          </xsl:variable>
          <xsl:for-each select="1 to gndoc:mll($values)">=</xsl:for-each>
          <xsl:if test="position() != last()">
            <xsl:value-of select="$s"/>
          </xsl:if>
        </xsl:for-each>
        <xsl:value-of select="gndoc:nl()"/>
      </xsl:if>


      <xsl:for-each select="*">
        <xsl:variable name="colName" select="name()"/>
        <xsl:variable name="colValue" select="replace(normalize-space(), '\*', '\\*')"/>
        <xsl:variable name="values">
          <xsl:copy-of select="$rows/*[name() = $colName]"/>
          <label>
            <xsl:value-of select="$colName"/>
          </label>
        </xsl:variable>
        <xsl:variable name="maxColLength"
                      select="gndoc:mll($values)"/>
        <xsl:variable name="missingSpaces"
                      select="$maxColLength - string-length($colValue)"/>
        <xsl:value-of select="$colValue"/>
        <xsl:if test="position() != last()">
          <xsl:value-of select="$s"/>
        </xsl:if>
        <xsl:for-each select="1 to $missingSpaces">
          <xsl:text> </xsl:text>
        </xsl:for-each>
      </xsl:for-each>
      <xsl:value-of select="gndoc:nl()"/>

      <!-- Footer -->
      <xsl:if test="position() = last()">
        <xsl:for-each select="*">
          <xsl:variable name="colName" select="name()"/>
          <xsl:variable name="values">
            <xsl:copy-of select="$rows/*[name() = $colName]"/>
            <label>
              <xsl:value-of select="$colName"/>
            </label>
          </xsl:variable>
          <xsl:for-each select="1 to gndoc:mll($values)">=</xsl:for-each>
          <xsl:if test="position() != last()">
            <xsl:value-of select="$s"/>
          </xsl:if>
        </xsl:for-each>
        <xsl:value-of select="gndoc:nl()"/>
      </xsl:if>
    </xsl:for-each>

    <xsl:value-of select="gndoc:nl(2)"/>
  </xsl:function>

  <xsl:function name="gndoc:writeCode">
    <xsl:param name="code" as="node()*"/>

    <xsl:text>&#xA;</xsl:text>.. code-block:: xml
    <xsl:value-of select="gndoc:nl(2)"/>
    <xsl:for-each select="$code">
      <xsl:variable name="text"
                    select="saxon:serialize(., 'default-serialize-mode')"/>
      <xsl:for-each select="tokenize($text, '\n')">
        <xsl:choose>
          <!-- Strip namespaces -->
          <xsl:when test="matches(normalize-space(.), '^xmlns:.*&quot;$')">
            <!--<xsl:if test="position() = 2"><xsl:text>    </xsl:text>...<xsl:text>&#xA;</xsl:text></xsl:if>-->
          </xsl:when>
          <xsl:otherwise>
            <xsl:text>    </xsl:text><xsl:value-of select="."/><xsl:text>&#xA;</xsl:text>
          </xsl:otherwise>
        </xsl:choose>

      </xsl:for-each>
      <xsl:value-of select="gndoc:nl(2)"/>
    </xsl:for-each>
  </xsl:function>


  <!-- New line -->
  <xsl:function name="gndoc:nl">
    <xsl:value-of select="gndoc:nl(1)"/>
  </xsl:function>

  <xsl:function name="gndoc:nl">
    <xsl:param name="howMany" as="xs:integer"/>

    <xsl:for-each select="1 to $howMany">
      <xsl:text>&#xA;</xsl:text>
    </xsl:for-each>
  </xsl:function>
</xsl:stylesheet>
