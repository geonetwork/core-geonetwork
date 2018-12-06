<?xml version="1.0" encoding="UTF-8" ?>
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
                xmlns:geonet="http://www.fao.org/geonetwork"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                version="2.0">

  <!--
    Return the title of an element from labels
  -->
  <xsl:function name="geonet:getTitleWithoutContext" as="xs:string">
    <xsl:param name="schema" as="xs:string"/>
    <xsl:param name="name" as="xs:string"/>
    <xsl:param name="labels" as="node()"/>
    <xsl:value-of
      select="string($labels/element[@name=$name and not(@context)]/label)"
    />
  </xsl:function>


  <!-- Transform XSD list of elements. eg.
    "http://www.isotc211.org/2005/gmd":extentTypeCode, "http://www.isotc211.org/2005/gmd":polygon
  -->
  <xsl:function name="geonet:parse-xsd-elements" as="xs:string">
    <xsl:param name="elements" as="xs:string"/>
    <xsl:param name="schema" as="xs:string"/>
    <xsl:param name="labels" as="node()"/>

    <xsl:variable name="element" select="tokenize($elements, ',')"/>
    <xsl:variable name="regex">.*/(.*)":(.*)</xsl:variable>
    <xsl:variable name="response">
      <xsl:for-each select="$element">
        <e>
          <xsl:analyze-string select="." regex="{$regex}">
            <xsl:matching-substring>
              <xsl:variable name="name" select="concat(regex-group(1), ':', regex-group(2))"/>
              <xsl:value-of select="geonet:getTitleWithoutContext($schema, $name, $labels)"/>
              (<xsl:value-of select="$name"/>)
            </xsl:matching-substring>
            <xsl:non-matching-substring>
              <xsl:value-of select="$element"/>
            </xsl:non-matching-substring>
          </xsl:analyze-string>
        </e>
      </xsl:for-each>
    </xsl:variable>
    <xsl:value-of select="string-join($response/e, ', ')"/>
  </xsl:function>


  <!-- Transform XSD error message for a set of usual validation report error messages.
    Schema labels are used to translate element name.

  Return XSD error message if no rules defined.
  -->
  <xsl:function name="geonet:parse-xsd-error-msg" as="xs:string">
    <xsl:param name="errorType" as="xs:string"/>
    <xsl:param name="error" as="xs:string"/>
    <xsl:param name="schema" as="xs:string"/>
    <xsl:param name="labels" as="node()"/>
    <xsl:param name="strings" as="node()"/>

    <!-- Set of rules processed : -->
    <xsl:variable name="rules">
      <!--Invalid content was found starting with element 'gmd:dateType'. One of '{"http://www.isotc211.org/2005/gmd":date}' is expected. (Element: gmd:dateType with parent element: gmd:CI_Date)-->
      <rule errorType="complex-type.2.4.awithparent">Invalid content was found starting with element '([a-z]{3}):(.*)'\. One of '\{"(.*)\}' is expected\. \(Element: (.*) with parent element: (.*)\)</rule>
      <rule errorType="complex-type.2.4.a">Invalid content was found starting with element '([a-z]{3}):(.*)'\. One of '\{"(.*)\}' is expected\.</rule>
      <!--cvc-complex-type.2.4.b: The content of element 'gmd:EX_BoundingPolygon' is not complete. One of '{"http://www.isotc211.org/2005/gmd":extentTypeCode, "http://www.isotc211.org/2005/gmd":polygon}' is expected. (Element: gmd:EX_BoundingPolygon with parent element: gmd:geographicElement)-->
      <rule errorType="complex-type.2.4.bwithparent">The content of element '(.*)' is not complete. One of '\{"(.*)\}' is expected\. \(Element: (.*) with parent element: (.*)\)</rule>
      <rule errorType="complex-type.2.4.b">The content of element '(.*)' is not complete. One of '\{"(.*)\}' is expected\.</rule>
      <!--cvc-datatype-valid.1.2.1: '' is not a valid value for 'dateTime'. (Element: gco:DateTime with parent element: gmd:date)-->
      <!--cvc-datatype-valid.1.2.1: 'DUMMY_DENOMINATOR' is not a valid value for 'integer'. (Element: gco:Integer with parent element: gmd:denominator)-->
      <rule errorType="datatype-valid.1.2.1withparent">'(.*)' is not a valid value for '(.*)'\. \(Element: ([a-z]{3}):(.*) with parent element: (.*)\)</rule>
      <rule errorType="datatype-valid.1.2.1">'(.*)' is not a valid value for '(.*)'\.</rule>
      <!--cvc-type.3.1.3: The value 'DUMMY_DENOMINATOR' of element 'gco:Integer' is not valid. (Element: gco:Integer with parent element: gmd:denominator)-->
      <rule errorType="type.3.1.3withparent">The value '(.*)' of element '(.*)' is not valid\. \(Element: ([a-z]{3}):(.*) with parent element: (.*)\)</rule>
      <rule errorType="type.3.1.3">The value '(.*)' of element '(.*)' is not valid\.</rule>
      <rule errorType="enumeration-valid">Value '(.*)' is not facet-valid with respect to enumeration '\[(.*)\]'\. It must be a value from the enumeration\. \(Element: ([a-z]{3}):(.*) with parent element: (.*)\)</rule>
    </xsl:variable>

    <xsl:variable name="errorWithParentName"
                  select="contains($error, 'with parent element')"/>
    <xsl:variable name="errorFullType"
                  select="if ($errorWithParentName)
                          then concat($errorType, 'withparent')
                          else $errorType"/>

    <xsl:variable name="regexp" select="$rules/rule[@errorType = $errorFullType]"/>

    <xsl:choose>
      <xsl:when test="$regexp">
        <xsl:analyze-string select="$error" regex="{$regexp}">
          <xsl:matching-substring>
            <xsl:variable name="response">

              <xsl:choose>
                <xsl:when test="$errorType = 'complex-type.2.4.a'">
                  <xsl:value-of select="$strings/invalidElement"/>
                  <xsl:value-of
                    select="geonet:getTitleWithoutContext($schema, concat(regex-group(1), ':', regex-group(2)), $labels)"
                  /> (<xsl:value-of select="concat(regex-group(1), ':', regex-group(2))"/>).
                  <xsl:value-of select="$strings/onElementOf"/>
                  <xsl:value-of
                    select="geonet:parse-xsd-elements(regex-group(3), $schema, $labels)"/>
                  <xsl:value-of select="$strings/isExpected"/>
                  <xsl:if test="$errorWithParentName">
                    <xsl:value-of select="$strings/elementLocated"/>
                    <xsl:value-of
                      select="geonet:getTitleWithoutContext($schema, regex-group(5), $labels)"/>
                    (<xsl:value-of select="regex-group(5)"/>).
                  </xsl:if>
                </xsl:when>
                <xsl:when test="$errorType = 'complex-type.2.4.b'">
                  <xsl:value-of select="$strings/missingElement"/>
                  <xsl:value-of
                    select="geonet:getTitleWithoutContext($schema, regex-group(1), $labels)"/>
                  (<xsl:value-of select="regex-group(1)"/>)
                  <xsl:value-of
                    select="$strings/isNotComplete"/>
                  <xsl:value-of select="$strings/onElementOf"/>
                  <xsl:value-of
                    select="geonet:parse-xsd-elements(regex-group(2), $schema, $labels)"/>
                  <xsl:value-of select="$strings/isExpected"/>

                  <xsl:if test="$errorWithParentName">
                    <xsl:value-of select="$strings/elementLocated"/>
                    <xsl:value-of
                      select="geonet:getTitleWithoutContext($schema, regex-group(4), $labels)"/>
                    (<xsl:value-of select="regex-group(4)"/>).
                  </xsl:if>
                </xsl:when>
                <xsl:when test="$errorType = 'complex-type.2.4.b'">
                  <xsl:value-of select="$strings/missingElement"/>
                  <xsl:value-of
                    select="geonet:getTitleWithoutContext($schema, regex-group(1), $labels)"/>
                  (<xsl:value-of select="regex-group(1)"/>)
                  <xsl:value-of
                    select="$strings/isNotComplete"/>
                  <xsl:value-of select="$strings/onElementOf"/>
                  <xsl:value-of
                    select="geonet:parse-xsd-elements(regex-group(2), $schema, $labels)"/>
                  <xsl:value-of select="$strings/isExpected"/>

                  <xsl:if test="$errorWithParentName">
                    <xsl:value-of select="$strings/elementLocated"/>
                    <xsl:value-of
                      select="geonet:getTitleWithoutContext($schema, regex-group(4), $labels)"/>
                    (<xsl:value-of select="regex-group(4)"/>).
                  </xsl:if>
                </xsl:when>
                <xsl:when test="$errorType = 'datatype-valid.1.2.1' or $errorType = 'type.3.1.3'">
                  <xsl:value-of select="$strings/invalidValue"/> '<xsl:value-of
                  select="regex-group(1)"/>'
                  <xsl:value-of select="$strings/notValidFor"/>
                  <xsl:value-of
                    select="geonet:getTitleWithoutContext($schema, regex-group(2), $labels)"
                  /> (<xsl:value-of select="regex-group(2)"/>)

                  <xsl:if test="$errorWithParentName">
                    <xsl:value-of select="$strings/inElement"/>
                    <xsl:value-of
                      select="geonet:getTitleWithoutContext($schema, regex-group(5), $labels)"/>
                    (<xsl:value-of select="regex-group(5)"/>).
                  </xsl:if>
                </xsl:when>
                <xsl:when test="$errorType = 'enumeration-valid'">
                  <xsl:value-of select="$strings/enum1"/> '<xsl:value-of
                  select="regex-group(1)"/>'
                  <xsl:value-of select="$strings/enum2"/>
                  <xsl:value-of
                    select="geonet:getTitleWithoutContext($schema, concat(regex-group(3), ':', regex-group(4)), $labels)"
                  /> (<xsl:value-of select="concat(regex-group(3), ':', regex-group(4))"/>).
                  <xsl:value-of select="$strings/enum3"/>
                  <xsl:value-of select="regex-group(2)"/>.
                </xsl:when>
              </xsl:choose>
            </xsl:variable>

            <xsl:value-of select="$response"/>
          </xsl:matching-substring>
          <xsl:non-matching-substring>
            <xsl:value-of select="$error"/>
          </xsl:non-matching-substring>
        </xsl:analyze-string>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$error"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:function>


  <!--
    Transform an XSD parser error to a more comprehensible information for end-user.
  -->
  <xsl:function name="geonet:parse-xsd-error" as="xs:string">
    <xsl:param name="error" as="xs:string"/>
    <xsl:param name="schema" as="xs:string"/>
    <xsl:param name="labels" as="node()"/>
    <xsl:param name="strings" as="node()"/>

    <!-- Extract XSD error type and message-->
    <xsl:analyze-string select="$error" regex=".*cvc-([\w\-0-9\.]+): (.*)">
      <xsl:matching-substring>
        <xsl:value-of
          select="geonet:parse-xsd-error-msg(regex-group(1), regex-group(2), $schema, $labels, $strings)"/>
      </xsl:matching-substring>
      <xsl:non-matching-substring>
        <xsl:value-of select="$error"/>
      </xsl:non-matching-substring>
    </xsl:analyze-string>
  </xsl:function>

</xsl:stylesheet>
