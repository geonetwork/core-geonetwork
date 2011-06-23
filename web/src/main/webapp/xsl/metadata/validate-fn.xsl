<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:geonet="http://www.fao.org/geonetwork" xmlns:xs="http://www.w3.org/2001/XMLSchema"
  version="2.0">
  
  <!--
    Return the title of an element from labels
  -->
  <xsl:function name="geonet:getTitleWithoutContext" as="xs:string">
    <xsl:param name="schema" as="xs:string"/>
    <xsl:param name="name" as="xs:string"/>
    <xsl:param name="labels" as="node()"/>
    
    <xsl:value-of
      select="string($labels/schemas/*[name(.)=$schema]/labels/element[@name=$name and not(@context)]/label)"
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
    
    <!-- Set of rules processed : -->
    <xsl:variable name="rules">
      <!--Invalid content was found starting with element 'gmd:dateType'. One of '{"http://www.isotc211.org/2005/gmd":date}' is expected. (Element: gmd:dateType with parent element: gmd:CI_Date)-->
      <rule errorType="complex-type.2.4.a">Invalid content was found starting with element '([a-z]{3}):(.*)'\. One of '\{"(.*)\}' is expected\. \(Element: (.*) with parent element: (.*)\)</rule>
      <!--cvc-complex-type.2.4.b: The content of element 'gmd:EX_BoundingPolygon' is not complete. One of '{"http://www.isotc211.org/2005/gmd":extentTypeCode, "http://www.isotc211.org/2005/gmd":polygon}' is expected. (Element: gmd:EX_BoundingPolygon with parent element: gmd:geographicElement)-->
      <rule errorType="complex-type.2.4.b">The content of element '(.*)' is not complete. One of '\{"(.*)\}' is expected\. \(Element: (.*) with parent element: (.*)\)</rule>
      <!--cvc-datatype-valid.1.2.1: '' is not a valid value for 'dateTime'. (Element: gco:DateTime with parent element: gmd:date)-->
      <!--cvc-datatype-valid.1.2.1: 'DUMMY_DENOMINATOR' is not a valid value for 'integer'. (Element: gco:Integer with parent element: gmd:denominator)-->
      <rule errorType="datatype-valid.1.2.1">'(.*)' is not a valid value for '(.*)'\. \(Element: ([a-z]{3}):(.*) with parent element: (.*)\)</rule>
      <!--cvc-type.3.1.3: The value 'DUMMY_DENOMINATOR' of element 'gco:Integer' is not valid. (Element: gco:Integer with parent element: gmd:denominator)-->
      <rule errorType="type.3.1.3">The value '(.*)' of element '(.*)' is not valid\. \(Element: ([a-z]{3}):(.*) with parent element: (.*)\)</rule>
    </xsl:variable>
    
    <xsl:variable name="regexp" select="$rules/rule[@errorType=$errorType]"/>
    <xsl:choose>
      <xsl:when test="$regexp">
        <xsl:analyze-string select="$error" regex="{$regexp}">
          <xsl:matching-substring>
            <xsl:variable name="response">
              
              <xsl:choose>
                <xsl:when test="$errorType = 'complex-type.2.4.a'">
                  <xsl:value-of select="$labels/validation/invalidElement"/>
                  <xsl:value-of select="geonet:getTitleWithoutContext($schema, concat(regex-group(1), ':', regex-group(2)), $labels)"/>
                  (<xsl:value-of select="concat(regex-group(1), ':', regex-group(2))"/>).
                  <xsl:value-of select="$labels/validation/onElementOf"/>
                  <xsl:value-of select="geonet:parse-xsd-elements(regex-group(3), $schema, $labels)"/>
                  <xsl:value-of select="$labels/validation/isExpected"/>
                  <xsl:value-of select="$labels/validation/elementLocated"/>
                  <xsl:value-of select="geonet:getTitleWithoutContext($schema, regex-group(5), $labels)"/>
                  (<xsl:value-of select="regex-group(5)"/>).
                </xsl:when>
                <xsl:when test="$errorType = 'complex-type.2.4.b'">
                  <xsl:value-of select="$labels/validation/missingElement"/>
                  <xsl:value-of select="geonet:getTitleWithoutContext($schema, regex-group(1), $labels)"/>
                  (<xsl:value-of select="regex-group(1)"/>)
                  <xsl:value-of select="$labels/validation/isNotComplete"/>
                  <xsl:value-of select="$labels/validation/onElementOf"/>
                  <xsl:value-of select="geonet:parse-xsd-elements(regex-group(2), $schema, $labels)"/>
                  <xsl:value-of select="$labels/validation/isExpected"/>
                  <xsl:value-of select="$labels/validation/elementLocated"/> 
                  <xsl:value-of select="geonet:getTitleWithoutContext($schema, regex-group(4), $labels)"/>
                  (<xsl:value-of select="regex-group(4)"/>).
                </xsl:when>
                <xsl:when test="$errorType = 'datatype-valid.1.2.1' or $errorType = 'type.3.1.3'">
                  <xsl:value-of select="$labels/validation/invalidValue"/>
                  '<xsl:value-of select="regex-group(1)"/>' 
                  <xsl:value-of select="$labels/validation/notValidFor"/>
                  <xsl:value-of select="geonet:getTitleWithoutContext($schema, concat(regex-group(3), ':', regex-group(4)), $labels)"/>
                  (<xsl:value-of select="concat(regex-group(3), ':', regex-group(4))"/>) 
                  <xsl:value-of select="$labels/validation/inElement"/>
                  <xsl:value-of select="geonet:getTitleWithoutContext($schema, regex-group(5), $labels)"/>
                  (<xsl:value-of select="regex-group(5)"/>).
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
      <xsl:otherwise><xsl:value-of select="$error"/></xsl:otherwise>
    </xsl:choose>
  </xsl:function>
  
  
  <!-- 
    Transform an XSD parser error to a more comprehensible information for end-user.
  -->
  <xsl:function name="geonet:parse-xsd-error" as="xs:string">
    <xsl:param name="error" as="xs:string"/>
    <xsl:param name="schema" as="xs:string"/>
    <xsl:param name="labels" as="node()"/>
    
    <!-- Extract XSD error type and message-->
    <xsl:analyze-string select="$error" regex="cvc-([\w\-0-9\.]+): (.*)">
      <xsl:matching-substring>
        <xsl:value-of select="geonet:parse-xsd-error-msg(regex-group(1), regex-group(2), $schema, $labels)"/>
      </xsl:matching-substring>
      <xsl:non-matching-substring>
        <xsl:value-of select="$error"/>
      </xsl:non-matching-substring>
    </xsl:analyze-string>
  </xsl:function>

</xsl:stylesheet>
