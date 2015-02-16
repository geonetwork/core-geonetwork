<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gmx="http://www.isotc211.org/2005/gmx"
                xmlns:srv="http://www.isotc211.org/2005/srv"
                xmlns:gml="http://www.opengis.net/gml"
                xmlns:geonet="http://www.fao.org/geonetwork"
                xmlns:gts="http://www.isotc211.org/2005/gts"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:gn-fn-metadata="http://geonetwork-opensource.org/xsl/functions/metadata"
                xmlns:saxon="http://saxon.sf.net/"
                extension-element-prefixes="saxon"
                exclude-result-prefixes="#all">


  <!-- Load labels. -->
  <xsl:variable name="schema"
                select="'iso19139'"/>
  <xsl:variable name="label"
                select="/root/schemas/iso19139"/>
  <xsl:variable name="schemaStrings"
                select="/root/schemas/iso19139/strings"/>
  <xsl:variable name="schemaLabels"
                select="/root/schemas/iso19139/labels"/>
  <xsl:variable name="schemaCodelists"
                select="/root/schemas/iso19139/codelists"/>
  <xsl:variable name="metadata"
                select="/root/gmd:MD_Metadata"/>
  <xsl:variable name="dateFormat"
              select="'[H1]:[m01]:[s01] [D1] [MNn] [Y]'"/>

  <!-- The configuration should be the one from the schema
  TODO add relative link to the document ? -->
  <xsl:variable name="iso19139-configuration"
                select="document('config-editor.xml')"/>


  <!-- ########################################################### -->
  <!-- A set of existing templates copied from other XSLTs - start -->

  <!-- dispatcher.xsl -->
  <xsl:template name="evaluate-iso19139">
    <xsl:param name="base" as="node()"/>
    <xsl:param name="in"/>
    <xsl:variable name="nodeOrAttribute" select="saxon:evaluate(concat('$p1', $in), $base)"/>
    <xsl:choose>
      <xsl:when test="$nodeOrAttribute/*">
        <xsl:copy-of select="$nodeOrAttribute"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$nodeOrAttribute"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>


  <!-- functions-metadata.xsl -->
  <xsl:function name="gn-fn-metadata:getLabel" as="node()">
    <xsl:param name="schema" as="xs:string"/>
    <xsl:param name="name" as="xs:string"/>
    <xsl:param name="labels" as="node()"/>
    <xsl:param name="parent" as="xs:string?"/>
    <xsl:param name="parentIsoType" as="xs:string?"/>
    <xsl:param name="xpath" as="xs:string?"/>

    <!-- TODO : add fallback schema
    Add try/catch block to log out when a label id duplicated
    in loc files. XSLv3 could be useful for that.
    -->
    <!--<xsl:message>#<xsl:value-of select="$name"/></xsl:message>
    <xsl:message>#<xsl:value-of select="$xpath"/></xsl:message>
    <xsl:message>#<xsl:value-of select="$parent"/></xsl:message>-->

    <xsl:variable name="escapedName">
      <xsl:choose>
        <xsl:when test="matches($name, '.*CHOICE_ELEMENT.*')">
          <xsl:value-of select="substring-before($name, 'CHOICE_ELEMENT')"/>
        </xsl:when>
        <xsl:when test="matches($name, '.*GROUP_ELEMENT.*')">
          <xsl:value-of select="substring-before($name, 'GROUP_ELEMENT')"/>
        </xsl:when>
        <xsl:otherwise><xsl:value-of select="$name"/></xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <!-- Name with context in current schema -->
    <xsl:variable name="schemaLabelWithContext"
                  select="$labels/element[@name=$escapedName and (@context=$xpath or @context=$parent or @context=$parentIsoType)]"/>

    <!-- Name in current schema -->
    <xsl:variable name="schemaLabel" select="$labels/element[@name=$escapedName and not(@context)]"/>

    <xsl:choose>
      <xsl:when test="$schemaLabelWithContext">
        <xsl:copy-of select="$schemaLabelWithContext[1]" copy-namespaces="no"/>
      </xsl:when>
      <xsl:when test="$schemaLabel">
        <xsl:copy-of select="$schemaLabel" copy-namespaces="no"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:choose>
          <xsl:when test="starts-with($schema, 'iso19139.')">
            <xsl:copy-of select="gn-fn-metadata:getLabel('iso19139', $name, $schemaLabels,
              $parent, $parentIsoType, $xpath)"/>
          </xsl:when>
          <xsl:otherwise>
            <element>
              <label>
                <xsl:value-of select="$escapedName"/>
              </label>
            </element>
            <xsl:message>gn-fn-metadata:getLabel | missing translation in schema <xsl:value-of
                    select="$schema"/> for <xsl:value-of select="$name"/>.</xsl:message>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:otherwise>
    </xsl:choose>

  </xsl:function>

  <!-- A set of existing templates copied from other XSLTs - end -->
  <!-- ########################################################### -->




  <xsl:template
          mode="iso19139" match="geonet:info"/>
  <!-- Root element matching. -->
  <xsl:template match="/" priority="5">
    <html>
      <!-- Set some vars. -->
      <xsl:variable name="title"
                    select="/root/gmd:MD_Metadata/gmd:identificationInfo/*/
											gmd:citation/*/gmd:title/gco:CharacterString"/>
      <xsl:variable name="identifier"
                    select="/root/gmd:MD_Metadata/gmd:fileIdentifier/gco:CharacterString"/>

      <head>
        <title>
          <xsl:value-of select="$title"/>
        </title>
      </head>
      <body>

        <link rel="stylesheet" type="text/css"
              href="{root/url}/apps/sextant/css/schema/reset.css"/>
        <link rel="stylesheet" type="text/css"
              href="{root/url}/apps/sextant/css/schema/default.css"/>
        <link rel="stylesheet" type="text/css"
              href="{root/url}/apps/sextant/css/schema/emodnet.css"/>
        <link rel="stylesheet" type="text/css"
              href="{root/url}/apps/sextant/css/schema/medsea.css"/>
        <div class="tpl-emodnet">
          <div class="ui-layout-content mdshow-tabpanel">
            <a id="md-print-btn" class="file-pdf" title="Export PDF" href="{/root/url}/srv/eng/pdf?uuid={$identifier}"></a>
            <a id="md-xml-btn" class="file-xml" title="Export XML" href="{/root/url}/srv/eng/xml.metadata.get?uuid={$identifier}"></a>

             <xsl:for-each
                    select="$iso19139-configuration//view[@name = 'medsea']/tab">

              <xsl:variable name="viewName" select="@id"/>

              <table class="print_table" border="0" cellpadding="0"
                     cellspacing="0">
                <tbody>

                  <tr valign="top">
                    <td class="print_ttl">
                      <xsl:value-of select="$schemaStrings/*[name() = $viewName]/text()"/>
                    </td>
                    <td class="print_data">
                    </td>
                  </tr>


                  <xsl:for-each select=".//field">
                    <xsl:variable name="configName"
                                  select="@name"/>
                    <xsl:variable name="fieldConfig"
                                  select="."/>
                    <xsl:variable name="overrideLabel"
                                  select="$schemaStrings/*[name() = $configName]"/>

                    <!-- In MedSea configuration all fields are based on
                    xpath expression. Here we get nodes in the documents ... -->
                    <xsl:variable name="nodes">
                      <saxon:call-template name="{concat('evaluate-', $schema)}">
                        <xsl:with-param name="base" select="$metadata"/>
                        <xsl:with-param name="in" select="concat('/../', @xpath)"/>
                      </saxon:call-template>
                    </xsl:variable>

                    <!--<xsl:message>#<xsl:copy-of select="$nodes"/></xsl:message>-->
                    <!-- ... and then display values
                    Display only non empty fields.
                    -->
                    <xsl:choose>
                      <xsl:when test="$nodes/*[count(gmd:MD_Keywords/gmd:keyword[* != '']) > 0 or
                                              */@codeListValue != '' or
                                              (not(gmd:MD_Keywords) and normalize-space() != '')]">
                        <xsl:variable name="label"
                                      select="if (normalize-space($overrideLabel) != '')
                                  then $overrideLabel
                                  else gn-fn-metadata:getLabel(
                                          'iso19139',
                                          $nodes[1]/*/name(),
                                          $schemaLabels,
                                          '', '', '')/label"/>

                        <tr valign="top">
                          <td class="print_desc">
                            <xsl:value-of select="$label"/>
                          </td>
                          <td class="print_data">
                            <xsl:for-each select="$nodes">
                              <xsl:apply-templates mode="render-value"/>
                            </xsl:for-each>
                          </td>
                        </tr>
                      </xsl:when>
                      <xsl:otherwise>
                        <!--<xsl:message>No match for field: <xsl:copy-of select="$fieldConfig"/></xsl:message>-->
                      </xsl:otherwise>
                    </xsl:choose>
                  </xsl:for-each>
                </tbody>
              </table>

             </xsl:for-each>
          </div>
        </div>
      </body>
    </html>
  </xsl:template>

  <xsl:template mode="render-value" match="gmd:EX_GeographicBoundingBox[
                                            gmd:northBoundLatitude/gco:Decimal != '' and
                                            gmd:southBoundLatitude/gco:Decimal != '' and
                                            gmd:eastBoundLongitude/gco:Decimal != '' and
                                            gmd:westBoundLongitude/gco:Decimal != '']">

    <xsl:variable name="box"
                  select="concat('POLYGON((',
                  gmd:eastBoundLongitude/gco:Decimal, ' ',
                  gmd:southBoundLatitude/gco:Decimal, ',',
                  gmd:eastBoundLongitude/gco:Decimal, ' ',
                  gmd:northBoundLatitude/gco:Decimal, ',',
                  gmd:westBoundLongitude/gco:Decimal, ' ',
                  gmd:northBoundLatitude/gco:Decimal, ',',
                  gmd:westBoundLongitude/gco:Decimal, ' ',
                  gmd:southBoundLatitude/gco:Decimal, ',',
                  gmd:eastBoundLongitude/gco:Decimal, ' ',
                  gmd:southBoundLatitude/gco:Decimal, '))')"/>
    <xsl:variable name="numberFormat" select="'0.00'"/>
    <table class="bbox">
      <tr>
        <td></td>
        <td><xsl:value-of select="format-number(gmd:northBoundLatitude/gco:Decimal, $numberFormat)"/></td>
        <td></td>
      </tr>
      <tr>
        <td><xsl:value-of select="format-number(gmd:westBoundLongitude/gco:Decimal, $numberFormat)"/></td>
        <td>
          <img class="gn-img-extent"
                 src="{root/url}/geonetwork/srv/fre/region.getmap.png?mapsrs=EPSG:3857&amp;width=250&amp;background=osm&amp;geomsrs=EPSG:4326&amp;geom={$box}"/>
        </td>
        <td><xsl:value-of select="format-number(gmd:eastBoundLongitude/gco:Decimal, $numberFormat)"/></td>
      </tr>
      <tr>
        <td></td>
        <td><xsl:value-of select="format-number(gmd:southBoundLatitude/gco:Decimal, $numberFormat)"/></td>
        <td></td>
      </tr>
    </table>

  </xsl:template>

  <xsl:template mode="render-value" match="gmd:MD_Keywords">
    <ul>
      <xsl:for-each select="gmd:keyword">
        <li><xsl:value-of select="gco:CharacterString"/></li>
      </xsl:for-each>
    </ul>
  </xsl:template>


  <xsl:template mode="render-value" match="gmd:CI_ResponsibleParty">
    <ul>
      <li><xsl:value-of select="gmd:organisationName/gco:CharacterString"/></li>
      <li><xsl:value-of select=".//gmd:electronicMailAddress/gco:CharacterString"/></li>
    </ul>
  </xsl:template>

  <xsl:template mode="render-value" match="@codeListValue">
    <xsl:variable name="id" select="."/>

    <xsl:for-each select="$schemaCodelists//entry[code = $id]/label">
      <!-- Hack : position filter [1] does not work on above xpath for obscur reason -->
      <xsl:if test="position() = 1">
        <xsl:copy-of select="."/>
      </xsl:if>
    </xsl:for-each>
  </xsl:template>

  <xsl:template mode="render-value" match="gco:CharacterString|gco:Integer|gco:Decimal|
       gco:Boolean|gco:Real|gco:Measure|gco:Length|gco:Distance|gco:Angle|gmx:FileName|
       gco:Scale|gco:Record|gco:RecordType|gmx:MimeFileType|gmd:URL|gco:Date|gco:DateTime|
       gco:LocalName|gmd:PT_FreeText|gml:beginPosition|gml:endPosition|gco:Date|gco:DateTime|
        gml:beginPosition|gml:endPosition">
    <p><xsl:value-of select="."/>
    <xsl:if test="@uom">
      <xsl:text> </xsl:text><xsl:value-of select="@uom"/>
    </xsl:if>
    </p>
  </xsl:template>


  <xsl:template mode="render-value" match="gmd:dateStamp/*">
    <xsl:value-of select="format-dateTime(., $dateFormat)"/>
  </xsl:template>


  <xsl:template mode="render-value" match="*|@*"/>

  <xsl:template mode="render-value" match="*">
    <xsl:apply-templates mode="render-value" select="*|@*"/>
  </xsl:template>


  <xsl:template mode="iso19139" match="gmd:EX_GeographicBoundingBox">

    <xsl:variable name="name" select="name(.)"/>
    <xsl:variable name="title">
      <xsl:call-template name="getTitle">
        <xsl:with-param name="name" select="$name"/>
      </xsl:call-template>
    </xsl:variable>

    <tr valign="top">
      <td class="print_desc">
        <xsl:value-of select="concat($title,' (WGS 84)')"/>
      </td>
      <td class="print_data">
        <xsl:value-of select="gmd:westBoundLongitude/gco:Decimal"/>
        /
        <xsl:value-of select="gmd:southBoundLatitude/gco:Decimal"/>
        /
        <xsl:value-of select="gmd:eastBoundLongitude/gco:Decimal"/>
        /
        <xsl:value-of select="gmd:northBoundLatitude/gco:Decimal"/>
      </td>
    </tr>
  </xsl:template>



  <!-- Display characterString -->
  <xsl:template mode="iso19139"
                match="gmd:*[gco:CharacterString or gmd:PT_FreeText]|
                      srv:*[gco:CharacterString or gmd:PT_FreeText]|
                      gco:aName[gco:CharacterString]|
                      gmd:*[gmd:URL]|
                      gmd:*[gco:Integer]"
                priority="2">
    <xsl:variable name="name" select="name(.)"/>
    <xsl:variable name="title">
      <xsl:call-template name="getTitle">
        <xsl:with-param name="name" select="$name"/>
      </xsl:call-template>
    </xsl:variable>

    <xsl:if test="gco:CharacterString!=''">
      <tr valign="top">
        <td class="print_desc">
          <xsl:value-of select="$title"/>
        </td>
        <td class="print_data">
          <xsl:value-of select="gco:CharacterString"/>
        </td>
      </tr>
    </xsl:if>
    <xsl:if test="gmd:URL!=''">
      <tr valign="top">
        <td class="print_desc">
          <xsl:value-of select="$title"/>
        </td>
        <td class="print_data">
          <xsl:value-of select="gmd:URL"/>
        </td>
      </tr>
    </xsl:if>
    <xsl:if test="gco:Integer!=''">
      <tr valign="top">
        <td class="print_desc">
          <xsl:value-of select="$title"/>
        </td>
        <td class="print_data">
          <xsl:value-of select="gco:Integer"/>
        </td>
      </tr>
    </xsl:if>
    <!-- Here you could display translation using PT_FreeText -->
  </xsl:template>


  <!-- Get title from the profil if exist, if not default to iso. -->
  <xsl:template name="getTitle">
    <xsl:param name="name"/>
    <xsl:variable name="title"
                  select="string($label/labels/element[@name=$name][1]/label)"/>
    <xsl:choose>
      <xsl:when test="normalize-space($title)">
        <xsl:value-of select="$title"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of
                select="string(/root/schemas/iso19139/labels/element[@name=$name][1]/label)"/>
      </xsl:otherwise>
    </xsl:choose>

  </xsl:template>

</xsl:stylesheet>