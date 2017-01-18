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

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                xmlns:gn="http://www.fao.org/geonetwork"
                version="2.0"
>


  <!-- Copy all elements and attributes excluding GeoNetwork elements.

    Geonet element could be gn:child, gn:element or extra node containing
    ELEMENT (used in dublin-core - GROUP_ELEMENT, CHOICE_ELEMENT).

    This could be useful to get the source XML when working on a metadocument.
    <xsl:if test="not(contains(name(.),'_ELEMENT'))">
  -->
  <xsl:template
    match="@*|
    node()[namespace-uri()!='http://www.fao.org/geonetwork' and
           not(contains(name(.),'_ELEMENT'))]"
    mode="gn-element-cleaner">
    <xsl:copy copy-namespaces="no">
      <xsl:copy-of select="namespace::*[. != 'http://www.fao.org/geonetwork']"/>
      <xsl:copy-of select="@*[namespace-uri() != 'http://www.fao.org/geonetwork']"/>
      <xsl:apply-templates select="node()" mode="gn-element-cleaner"/>
    </xsl:copy>
  </xsl:template>

  <!-- Remove GeoNetwork info element and children -->
  <xsl:template mode="gn-element-cleaner" match="gn:info" priority="2"/>

  <!-- Remove Schematron error report element and children -->
  <xsl:template mode="gn-element-cleaner" match="svrl:*" priority="2"/>


  <!-- Combine the context node with the node-to-merge
  children. Make a copy of everything for all elements of the
  context node and combined when gn:copy node is found.

  Example:

      <gmd:CI_Citation>
        <gmd:title>
          <gco:CharacterString>{{conformity_title}}</gco:CharacterString>
        </gmd:title>
        <gn:copy select="gmd:alternateTitle"/>

  A more advanced merging strategy could have been done
  in Java based on the information from the SchemaManager
  which could know where children must be inserted (TODO).
  -->
  <xsl:template mode="gn-merge" match="*" exclude-result-prefixes="#all">
    <xsl:param name="node-to-merge"/>

    <xsl:variable name="nodeName" select="name(.)"/>
    <xsl:variable name="parentName" select="name(..)"/>
    <xsl:variable name="attrs" select="@*"/>

    <xsl:copy>
      <!-- Copy all attribute from the template mode first
       and then all existing attributes of the equivalent node
       which are not defined in the template (to avoid to override
       the template attribute by the matching node ones). -->
      <xsl:copy-of select="$attrs"/>
      <xsl:for-each select="$node-to-merge/descendant-or-self::node()[
                              name() = $nodeName and name(..) = $parentName
                            ]/@*">
        <xsl:variable name="attrName" select="name()"/>
        <xsl:if test="count($attrs[name() = $attrName]) = 0">
          <xsl:copy-of select="."/>
        </xsl:if>
      </xsl:for-each>
      <xsl:apply-templates mode="gn-merge" select="*">
        <xsl:with-param name="node-to-merge" select="$node-to-merge"/>
      </xsl:apply-templates>
      <xsl:copy-of select="text()"/>
    </xsl:copy>
  </xsl:template>

  <!-- Combine the context node with the node-to-merge
    matching children. It works on XML records or Metadocuments
    (ie. having gn:info elements).

  -->
  <xsl:template mode="gn-merge" match="gn:copy" priority="2" exclude-result-prefixes="#all">
    <xsl:param name="node-to-merge"/>

    <xsl:variable name="nodeName" select="@select"/>
    <xsl:variable name="parentName" select="name(..)"/>
    <!-- FIXME: if a target node as same node name and parent node name, extra element may be duplicated. -->
    <xsl:apply-templates mode="gn-element-cleaner"
                         select="$node-to-merge/descendant-or-self::node()[name() = $nodeName and name(..) = $parentName]"/>
  </xsl:template>


  <!--
    2 types of errors are added to a record on validation:
    * XSD
    <gmd:dateStamp geonet:xsderror="\ncvc-complex-type.2.4.a: Invalid conte

    * Schematron
    <geonet:schematronerrors>
      <geonet:report geonet:rule="schematron-rules-iso">
      ...
      <svrl:fired-rule context="//*[gmd:CI_ResponsibleParty]"/>
        <svrl:failed-assert ref="#_391" test="$count > 0
          ....
          <svrl:text
    -->
  <xsl:template name="get-errors">
    <xsl:param name="theElement" required="no"/>

    <xsl:variable name="ref" select="concat('#_', gn:element/@ref)"/>

    <xsl:variable name="listOfErrors">
      <xsl:if
        test="@gn:xsderror
        or */@gn:xsderror
        or $metadata//svrl:failed-assert[@ref=$ref]">

        <errors>
          <xsl:choose>
            <!-- xsd validation -->
            <xsl:when test="@gn:xsderror">
              <xsl:choose>
                <xsl:when test="contains(@gn:xsderror, '\n')">
                  <xsl:variable name="root" select="/"/>
                  <!-- DataManager#getXSDXmlReport concat errors in attribute -->
                  <xsl:for-each select="tokenize(@gn:xsderror, '\\n')">
                    <xsl:if test=". != ''">
                      <error>
                        <xsl:value-of select="."/>
                        <!--<xsl:copy-of select="concat($root/root/gui/strings/xsdError, ': ',
                              geonet:parse-xsd-error(., $schema, $labels))"/>-->
                      </error>
                    </xsl:if>
                  </xsl:for-each>
                </xsl:when>
                <xsl:otherwise>
                  <error>
                    <!-- <xsl:copy-of select="concat(/root/gui/strings/xsdError, ': ',
                          geonet:parse-xsd-error(@geonet:xsderror, $schema, $labels))"/>-->
                    <xsl:value-of select="@gn:xsderror"/>
                  </error>
                </xsl:otherwise>
              </xsl:choose>

            </xsl:when>
            <!-- some simple elements hide lower elements to remove some
                        complexity from the display (eg. gco: in iso19139)
                        so check if they have a schematron/xsderror and move it up
                        if they do -->
            <xsl:when test="*/@gn:xsderror">
              <error>
                <!--<xsl:copy-of select="concat(/root/gui/strings/xsdError, ': ',
                      geonet:parse-xsd-error(*/@geonet:xsderror, $schema, $labels))"/>-->
                <xsl:value-of select="*/@gn:xsderror"></xsl:value-of>
              </error>
            </xsl:when>
            <!-- schematrons -->
            <xsl:when test="$metadata//svrl:failed-assert[@ref=$ref]">
              <xsl:for-each select="$metadata//svrl:failed-assert[@ref=$ref]">
                <error>
                  <xsl:value-of select="preceding-sibling::svrl:active-pattern[1]/@name"/> :
                  <xsl:copy-of select="svrl:text/*"/>
                </error>
              </xsl:for-each>
            </xsl:when>
          </xsl:choose>
        </errors>
      </xsl:if>
    </xsl:variable>
    <xsl:copy-of select="if (count($listOfErrors//error) > 0) then $listOfErrors else ''"/>
  </xsl:template>
</xsl:stylesheet>
