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
                xmlns:gns="http://geonetwork-opensource.org/schemas/schema-ident"
                xmlns:gndoc="http://geonetwork-opensource.org/doc"
                exclude-result-prefixes="xs"
                version="2.0">

  <xsl:import href="rst-writer.xsl"/>


  <xsl:param name="verbose"
             select="false()"
             as="xs:boolean"/>
  <xsl:param name="lang"
             select="'fre'"/>
  <xsl:param name="iso2lang"
             select="'fr'"/>
  <xsl:param name="schema"
             select="'iso19115-3'"/>
  <xsl:param name="baseDir"
             select="''"/>


  <xsl:variable name="i18n"
                select="document('config-editor-i18n.xml')
                          /i18n"/>
  <xsl:variable name="folder"
                select="concat('../../../../../schemas/', $schema)"/>
  <!-- Path is relative to maven execution folder, not to stylesheet
  because we use Java IO to check for file existence. -->
  <xsl:variable name="docFolder"
                select="concat($baseDir, '/../../schemas/', $schema, '/doc/', $iso2lang)"/>
  <xsl:variable name="pluginFolder"
                select="concat($folder, '/src/main/plugin/', $schema)"/>
  <xsl:variable name="ec"
                select="document(concat($pluginFolder, '/layout/config-editor.xml'))"/>
  <!-- A metadata template to detail encoding -->
  <xsl:variable name="tpl"
                select="document(concat($folder, '/doc/tpl.xml'))"/>
  <xsl:variable name="sc"
                select="document(concat($pluginFolder, '/schema-ident.xml'))
                          /gns:schema"/>
  <xsl:variable name="l"
                select="document(concat($pluginFolder, '/loc/', $lang, '/labels.xml'))
                          /labels/element"/>
  <xsl:variable name="s"
                select="document(concat($pluginFolder, '/loc/', $lang, '/strings.xml'))
                          /strings/*"/>
  <xsl:variable name="c"
                select="document(concat($pluginFolder, '/loc/', $lang, '/codelists.xml'))
                          /codelists/codelist"/>
  <xsl:variable name="t"
                select="$i18n/*[name() = $lang]"/>
  <xsl:variable name="schemaId"
                select="$sc/gns:name"/>
  <xsl:variable name="schemaTitle"
                select="$sc/gns:title[@xml:lang = $iso2lang or not(@xml:lang)]"/>
  <xsl:variable name="schemaDesc"
                select="$sc/gns:description[@xml:lang = $iso2lang or not(@xml:lang)]"/>
  <xsl:variable name="schemaUrl"
                select="$sc/gns:standardUrl[@xml:lang = $iso2lang or not(@xml:lang)]"/>


  <xsl:template match="/">
    <!-- Reference based on schema identifier -->
    <xsl:value-of select="gndoc:ref($schemaId, false())"/>

    <!-- Schema details -->
    <xsl:value-of select="gndoc:writeln(concat($schemaTitle, ' (', $schema, ')'), '#')"/>

    <xsl:value-of select="gndoc:nl(2)"/>
    <xsl:copy-of select="$schemaDesc"/>
<!--    <xsl:value-of select="gndoc:writeln($schemaDesc)"/>-->
    <xsl:value-of select="gndoc:nl(2)"/>
    <xsl:value-of select="gndoc:writeln(concat($t/schema-url, $schemaUrl))"/>
    <xsl:value-of select="gndoc:nl(2)"/>

    <xsl:call-template name="write-editor-doc"/>
    <xsl:call-template name="write-schema-details"/>
    <xsl:call-template name="write-glossary"/>
    <xsl:call-template name="write-codelist"/>
  </xsl:template>


  <!-- Build documentation based on
  editor configuration. -->
  <xsl:template name="write-editor-doc">
    <xsl:value-of select="gndoc:writeln($t/editor-config, '*')"/>
    <xsl:value-of select="gndoc:nl()"/>


    <xsl:value-of select="gndoc:writeln(concat($t/nbOfViews, count($ec//views/view), $t/views))"/>
    <xsl:value-of select="gndoc:nl(2)"/>
    <xsl:for-each select="$ec//views/view">
      <xsl:text>* </xsl:text><xsl:value-of
      select="if (@disabled = 'true') then concat(' (', $t/disabled, ') ') else ''"/><xsl:value-of
      select="gndoc:refTo(concat($schemaId, '-view-', normalize-space(@name)), false())"/>
      <xsl:value-of select="gndoc:nl()"/>
    </xsl:for-each>

    <!-- Editor configuration description -->
    <xsl:for-each select="$ec//views/view">
      <!-- Describe each views -->
      <xsl:variable name="viewName"
                    select="@name"/>
      <xsl:variable name="viewHelp"
                    select="$s[name() = concat($viewName,'-help')]"/>

      <xsl:value-of select="gndoc:ref(concat($schemaId, '-view-', $viewName), false())"/>

      <xsl:value-of select="gndoc:writeln(
                              concat($t/view, ' ', $s[name() = $viewName],
                                     ' (', $viewName, ')'), '=')"/>
      <xsl:if test="$viewHelp != ''">
        <xsl:value-of select="gndoc:writeln($viewHelp)"/>
      </xsl:if>

      <!-- Number of tabs in this view -->
      <xsl:value-of select="gndoc:writeln(concat($t/nbOfTabs, count(tab), $t/tabs))"/>
      <xsl:value-of select="gndoc:nl(2)"/>
      <xsl:for-each select="tab">
        <xsl:text>* </xsl:text><xsl:value-of
        select="gndoc:refTo(concat($schemaId, '-tab-', normalize-space(@id)), false())"/>
        <xsl:value-of select="gndoc:nl()"/>
      </xsl:for-each>

      <xsl:for-each select="flatModeExceptions/for">
        <xsl:if test="position() = 1">
          <xsl:value-of select="gndoc:nl(2)"/>
          <xsl:value-of select="gndoc:writeln($t/flatModeExceptions)"/>
          <xsl:value-of select="gndoc:nl()"/>
        </xsl:if>

        <xsl:variable name="name" select="@name"/>
        <xsl:choose>
          <xsl:when test="$l[@name = $name]/label = ''">
            <xsl:message>* Missing label for <xsl:value-of select="$name"/></xsl:message>
          </xsl:when>
          <xsl:otherwise>
            <!-- TODO handle context/xpath and remove select first -->
            <xsl:value-of
              select="gndoc:writeln(concat('* ', $l[@name = $name][1]/label, ' (', $name, ')'))"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:for-each>
      <xsl:value-of select="gndoc:nl(2)"/>


      <!-- Tab details -->
      <xsl:for-each select="tab">
        <xsl:variable name="tabName"
                      select="@id"/>
        <xsl:variable name="tabHelp"
                      select="$s/*[name() = concat($tabName,'-help')]"/>

        <xsl:value-of select="gndoc:ref(concat($schemaId, '-tab-', $tabName), false())"/>

        <xsl:value-of select="gndoc:writeln(
                              concat($t/tab, ' ', $s[name() = $tabName],
                                     ' (', $tabName, ')'), '-')"/>
        <xsl:if test="$tabHelp != ''">
          <xsl:value-of select="gndoc:writeln($tabHelp)"/>
        </xsl:if>
        <xsl:value-of select="gndoc:nl()"/>

        <xsl:value-of
          select="gndoc:figure($docFolder, concat('img/', $schemaId, '-tab-', $tabName, '.png'))"/>


        <xsl:value-of select="gndoc:writeln(
            if (@mode = 'flat') then $t/flatMode else $t/notFlatMode)"/>
        <xsl:value-of select="gndoc:nl()"/>


        <!-- Sections
        TODO: check if field are allowed here ?
        -->
        <xsl:apply-templates mode="write-doc" select=".//section"/>
      </xsl:for-each>
    </xsl:for-each>
  </xsl:template>


  <xsl:template match="field" mode="write-doc">
    <!-- TODO Handle template field -->
    <!-- TODO CODELISTS - generate full mtd to find links between labels definition and codelists-->
    <xsl:if test="@name">
      <!-- TODO same as section / use apply-templates -->
      <xsl:variable name="name"
                    select="@name"/>
      <xsl:variable name="help"
                    select="$s[name() = concat($name, '-help')]"/>
      <!-- name is optional -->
      <xsl:choose>
        <xsl:when test="contains($name,':')">
          <!-- Section name is a standard element eg. mdb:MD_Metadata. Use labels.xml. -->
          <xsl:variable name="nodeDesc"
                        select="$l[@name = $name]"/>
          <xsl:value-of select="gndoc:writeln($nodeDesc/label, '^')"/>

          <xsl:for-each select="$nodeDesc/(description|help)">
            <xsl:value-of select="gndoc:writelnhtml(normalize-space(.))"/>
          </xsl:for-each>
          <xsl:value-of select="gndoc:nl(2)"/>

          <xsl:value-of select="concat($t/cf, ' ')"/><xsl:value-of
          select="gndoc:refTo(concat($schemaId, '-elem-', normalize-space($name)), true())"/>
        </xsl:when>
        <xsl:when test="$name != ''">
          <xsl:value-of select="gndoc:writeln($s[name() = $name], '^')"/>
        </xsl:when>
      </xsl:choose>

      <!-- help is optional -->
      <xsl:if test="$help != ''">
        <xsl:value-of select="gndoc:writeln($help)"/>
      </xsl:if>
      <xsl:value-of select="gndoc:nl()"/>
    </xsl:if>

    <xsl:if test="@xpath">
      <!-- TODO same as section / use apply-templates -->
      <!-- Extract node name from XPath,
          ie. last element name before the filter expression -->
      <xsl:variable name="xpath" select="@xpath"/>
      <xsl:variable name="xpathTokens"
                    select="tokenize(
                                      tokenize(
                                        normalize-space(@xpath),
                                      '\[')[1],
                                    '/')"/>
      <xsl:variable name="nodeName"
                    select="$xpathTokens[last()]"/>
      <xsl:variable name="parentNodeName"
                    select="$xpathTokens[last() - 1]"/>
      <!-- Handle element with context = parent element or full xpath -->
      <xsl:variable name="nodeDescWithoutContext"
                    select="$l[@name = $nodeName and not(@context)]"/>
      <xsl:variable name="nodeDescWithContext"
                    select="$l[@name = $nodeName and @context = $parentNodeName]"/>
      <!-- Try to match on xpath even if config editor use * in xpath -->

      <xsl:variable name="nodeDescWithXpath"
                    select="$l[@name = $nodeName and
                              matches(@context,
                                replace(replace(replace($xpath,
                                  '\*', '.*'), '\[', '\\['), '\]', '\\]'))]"/>

      <xsl:variable name="nodeDesc">
        <xsl:choose>
          <xsl:when test="count($nodeDescWithXpath/*) > 0">
            <xsl:copy-of select="$nodeDescWithXpath[1]/*"/>
          </xsl:when>
          <xsl:when test="count($nodeDescWithContext/*) > 0">
            <xsl:copy-of select="$nodeDescWithContext[1]/*"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:copy-of select="$nodeDescWithoutContext[1]/*"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>

      <!-- TODO Add inspire or other flag -->
      <xsl:value-of select="gndoc:writeln($nodeDesc/label, '^')"/>
      <xsl:value-of select="gndoc:nl()"/>
      <xsl:for-each select="$nodeDesc/(description|help)">
        <xsl:value-of select="gndoc:writelnhtml(normalize-space(.))"/>
      </xsl:for-each>

      <xsl:apply-templates select="$nodeDesc/helper" mode="doc"/>

      <xsl:value-of select="gndoc:writelnfield($t/xpath, @xpath)"/>
      <xsl:value-of select="gndoc:nl(2)"/>
      <xsl:if test="$nodeName != '.'">
        <xsl:value-of select="concat($t/cf, ' ')"/><xsl:value-of
        select="gndoc:refTo(concat($schemaId, '-elem-', normalize-space($nodeName)), true())"/>
      </xsl:if>
    </xsl:if>
  </xsl:template>


  <xsl:template match="text" mode="write-doc">
    <xsl:variable name="ref" select="@ref"/>

    <xsl:value-of select="gndoc:writelnfield($t/instruction)"/>
    <xsl:value-of select="gndoc:writelnhtml($s[name() = $ref])"/>

    <xsl:value-of
      select="gndoc:figure($docFolder, concat('img/', $schemaId, '-tab-', ancestor::tab/@id, '-text-', $ref, '.png'))"/>

    <xsl:if test="exists(@if)">
      <xsl:value-of select="gndoc:writelnfield($t/displayIf, @if)"/>
      <xsl:value-of select="gndoc:nl(2)"/>
    </xsl:if>
  </xsl:template>


  <xsl:template match="action" mode="write-doc">
    <xsl:variable name="key"
                  select="if (@btnLabel) then @btnLabel else @name"/>
    <xsl:for-each select="@btnLabel|@name">
      <xsl:variable name="ref" select="."/>
      <xsl:value-of select="gndoc:writelnfield($t/actionName, $s[name() = $ref])"/>
    </xsl:for-each>
    <xsl:if test="exists(@type)">
      <xsl:value-of select="gndoc:writelnfield($t/actionType, @type)"/>
      <xsl:value-of select="gndoc:nl(2)"/>
    </xsl:if>

    <xsl:value-of
      select="gndoc:figure($docFolder, concat('img/', $schemaId, '-tab-', ancestor::tab/@id, '-action-', $key, '.png'))"/>

    <xsl:if test="exists(@if)">
      <xsl:value-of select="gndoc:writelnfield($t/displayIf, @if)"/>
      <xsl:value-of select="gndoc:nl(2)"/>
    </xsl:if>
    <xsl:if test="exists(template/snippet)">
      <xsl:value-of select="gndoc:writeCode(template/snippet/*)"/>
    </xsl:if>
  </xsl:template>


  <xsl:template match="section" mode="write-doc">
    <xsl:variable name="sectionName"
                  select="@name"/>

    <!-- Section name is optional -->
    <xsl:choose>
      <!-- Section name is an element name -->
      <xsl:when test="contains($sectionName,':')">
        <!-- Section name is a standard element eg. mdb:MD_Metadata. Use labels.xml. -->
        <xsl:variable name="nodeDesc"
                      select="$l[@name = $sectionName]"/>
        <xsl:value-of select="gndoc:writeln(
                                        concat($t/section, ' ', $nodeDesc/label), '^')"/>

        <xsl:for-each select="$nodeDesc/(description|help)">
          <xsl:value-of select="gndoc:writelnhtml(normalize-space(.))"/>
        </xsl:for-each>
        <xsl:value-of select="gndoc:nl(2)"/>

        <xsl:value-of select="concat($t/cf, ' ')"/><xsl:value-of
        select="gndoc:refTo(concat($schemaId, '-elem-', normalize-space($sectionName)), true())"/>
      </xsl:when>
      <xsl:when test="$sectionName != ''">
        <!-- Section name is a custom name. Use strings.xml. -->
        <xsl:choose>
          <xsl:when test="normalize-space($s[name() = $sectionName][1]) = ''">
            <xsl:message>* Missing section name for <xsl:value-of select="$sectionName"/></xsl:message>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="gndoc:writeln(
                                        concat($t/section, ' ', $s[name() = $sectionName]), '^')"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
    </xsl:choose>

    <!-- Section help is optional. -->
    <xsl:variable name="sectionHelp"
                  select="$s/*[name() = concat($sectionName,'-help')]"/>

    <xsl:if test="$sectionHelp != ''">
      <xsl:value-of select="gndoc:writeln($sectionHelp)"/>
    </xsl:if>
    <xsl:value-of select="gndoc:nl()"/>


    <!-- Section is an XPath. Retrieve last element name of the XPath
    and display information from labels.xml -->
    <xsl:if test="@xpath">
      <xsl:variable name="nodeName"
                    select="tokenize(
                                      tokenize(
                                        normalize-space(@xpath),
                                      '\[')[1],
                                    '/')[last()]"/>
      <!-- TODO: Handle context -->
      <xsl:variable name="nodeDesc"
                    select="$l[@name = $nodeName][1]"/>

      <xsl:value-of select="gndoc:writeln(concat($t/section, ' ', $nodeDesc/label), '^')"/>
      <xsl:for-each select="$nodeDesc/description">
        <xsl:value-of select="gndoc:writelnhtml(normalize-space(.))"/>
      </xsl:for-each>
      <xsl:value-of select="gndoc:nl(2)"/>

      <xsl:value-of select="concat($t/cf, ' ')"/><xsl:value-of
      select="gndoc:refTo(concat($schemaId, '-elem-', normalize-space($nodeName)), true())"/>
      <xsl:value-of select="gndoc:nl(2)"/>
    </xsl:if>


    <xsl:apply-templates mode="write-doc" select="field|section|text|action"/>
  </xsl:template>

  <xsl:template name="write-schema-details">
    <xsl:value-of select="gndoc:writeln($t/schema-details, '*')"/>
    <xsl:value-of select="gndoc:writelnfield($t/schema-id, $sc/gns:name)"/>
    <xsl:value-of select="gndoc:writelnfield($t/schema-version, $sc/gns:version)"/>
    <xsl:value-of select="gndoc:writelnfield($t/schema-location, $sc/gns:schemaLocation)"/>
    <xsl:value-of select="gndoc:writelnfield($t/schema-ns)"/>
    <xsl:for-each select="$sc/gns:autodetect/namespace::*">
      <xsl:sort select="."/>
      <xsl:value-of select="gndoc:writeln(concat(' * ', .))"/>
      <xsl:value-of select="gndoc:nl(1)"/>
    </xsl:for-each>
    <xsl:value-of select="gndoc:writelnfield(
                                  $t/schema-dm,
                                  concat(name($sc/gns:autodetect/*),
                                  ' (', $sc/gns:autodetect/*/@type, ')'))"/>
    <xsl:value-of select="gndoc:nl(1)"/>
    <xsl:value-of select="gndoc:writelnfield($t/schema-dme)"/>
    <xsl:for-each select="$sc/gns:autodetect/*/*/name()">
      <xsl:sort select="."/>
      <xsl:value-of select="gndoc:writeln(concat(' * ', .))"/>
      <xsl:value-of select="gndoc:nl(1)"/>
    </xsl:for-each>

  </xsl:template>


  <xsl:template name="write-glossary">
    <xsl:value-of select="gndoc:writeln($t/glossary, '*')"/>
    <xsl:value-of select="gndoc:nl()"/>
    <xsl:value-of select="gndoc:writeln($t/glossary-help)"/>
    <xsl:value-of select="gndoc:nl(2)"/>

    <xsl:for-each select="$l">
      <xsl:sort select="@name"/>

      <xsl:variable name="name" select="@name"/>

      <xsl:choose>
        <xsl:when test="exists(@context)">
          <xsl:value-of
            select="gndoc:ref(concat($schemaId, '-elem-', $name, '-', @context), true())"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="gndoc:ref(concat($schemaId, '-elem-', $name), true())"/>
        </xsl:otherwise>
      </xsl:choose>
      <xsl:value-of select="gndoc:writeln(label, '=')"/>
      <xsl:value-of select="gndoc:nl(2)"/>

      <xsl:value-of select="gndoc:writelnfield($t/nodename,  $name)"/>
      <xsl:if test="exists(@context)">
        <xsl:value-of select="gndoc:writelnfield($t/context,  @context)"/>
        <xsl:value-of select="gndoc:nl(2)"/>
      </xsl:if>

      <xsl:value-of select="gndoc:writelnfield($t/desc)"/>
      <xsl:for-each select="(description|help)[text() != '']">
        <xsl:if test="exists(@for)">
          <xsl:value-of select="gndoc:writelnfield($t/domain, @for)"/>
          <xsl:value-of select="gndoc:nl(2)"/>
        </xsl:if>
        <xsl:value-of select="gndoc:writelnhtml(.)"/>
        <xsl:value-of select="gndoc:nl()"/>
      </xsl:for-each>

      <xsl:value-of select="gndoc:nl()"/>

      <xsl:for-each select="condition[normalize-space() != '']">
        <xsl:value-of select="gndoc:writelnfield($t/cond, normalize-space(.))"/>
        <xsl:value-of select="gndoc:nl()"/>
      </xsl:for-each>

      <xsl:apply-templates select="helper" mode="doc"/>

      <xsl:apply-templates select="$c[@name = $name]" mode="doc">
        <xsl:with-param name="withRef" select="false()"/>
      </xsl:apply-templates>

      <xsl:variable name="xmlSnippet" select="$tpl/descendant-or-self::*[name() = $name]"/>
      <xsl:if test="$xmlSnippet instance of node()">
        <xsl:value-of select="gndoc:writeCode($xmlSnippet)"/>
      </xsl:if>

      <xsl:value-of select="gndoc:nl(3)"/>
    </xsl:for-each>
  </xsl:template>


  <xsl:template name="write-codelist">
    <xsl:value-of select="gndoc:writeln($t/codelists, '*')"/>
    <xsl:value-of select="gndoc:nl()"/>
    <xsl:value-of select="gndoc:writeln($t/codelists-help)"/>
    <xsl:value-of select="gndoc:nl(2)"/>

    <xsl:if test="not($c)">
      <xsl:value-of select="$t/noCodelist"/>
    </xsl:if>

    <xsl:apply-templates select="$c" mode="doc"/>
  </xsl:template>


  <xsl:template match="helper" mode="doc">
    <xsl:value-of select="gndoc:writeln($t/helper)"/>

    <xsl:variable name="rows">
      <xsl:for-each select="option">
        <row>
          <code>
            <xsl:value-of select="@value"/>
          </code>
          <label>
            <xsl:value-of select="."/>
          </label>
        </row>
      </xsl:for-each>
    </xsl:variable>
    <xsl:value-of select="gndoc:table($rows/*)"/>

    <!--<xsl:for-each select="option">
      <xsl:if test="position() = 1">
        <xsl:value-of select="gndoc:writeln($t/helper)"/>
        &lt;!&ndash; TODO conditional helper &ndash;&gt;
        <xsl:value-of select="gndoc:nl(2)"/>
        <xsl:for-each select="@displayIf">
          <xsl:value-of select="gndoc:writeln(concat('(', $t/displayIf, '``', ., '``'))"/>
        </xsl:for-each>
      </xsl:if>
      <xsl:value-of select="gndoc:writeln(concat('* ', normalize-space(.), ' (', @value, ')'))"/>
    </xsl:for-each>-->
  </xsl:template>


  <xsl:template match="codelist" mode="doc">
    <xsl:param name="withRef" select="true()"/>

    <xsl:value-of select="gndoc:nl(2)"/>

    <xsl:variable name="name" select="@name"/>
    <xsl:if test="$withRef">
      <xsl:choose>
        <xsl:when test="exists(@displayIf)">
          <xsl:value-of select="gndoc:ref(concat($schemaId, '-cl-', $name, '-',
                  replace(@displayIf, '\*', '\\*')), false())"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="gndoc:ref(concat($schemaId, '-cl-', $name), false())"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:if>

    <xsl:if test="exists(@displayIf)">
      <xsl:value-of select="gndoc:writelnfield($t/displayIf, @displayIf)"/>
      <xsl:value-of select="gndoc:nl(2)"/>
    </xsl:if>

    <!-- TODO: Handle context instead of selecting first -->
    <xsl:value-of select="gndoc:writeln(concat(
                            $t/codelist, ' ', $l[@name = $name][1]/label, ' (', @name, ')'
                            ), '=')"/>
    <xsl:value-of select="gndoc:nl()"/>
    <!--
    Display using list
    <xsl:apply-templates select="entry" mode="doc"/>
    <xsl:value-of select="gndoc:nl(3)"/>

    ... or table layout
    -->
    <!--<xsl:message><xsl:copy-of select="*[not(@hideInEditMode)]"/></xsl:message>-->
    <xsl:value-of select="gndoc:table(*[not(@hideInEditMode)])"/>

    <xsl:if test="count(*[@hideInEditMode]) > 0">
      <xsl:value-of select="gndoc:nl()"/>
      <xsl:value-of select="gndoc:writeln($t/hiddenInEditMode)"/>

      <xsl:value-of select="gndoc:table(*[@hideInEditMode])"/>
    </xsl:if>
  </xsl:template>


  <xsl:template match="entry" mode="doc">
    <xsl:value-of select="gndoc:writeln(concat(
                            '* ', label, ' (', code, '): ', normalize-space(description)
                            ))"/>
  </xsl:template>
</xsl:stylesheet>
