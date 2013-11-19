<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:svrl="http://purl.oclc.org/dsdl/svrl" 
  xmlns:gn="http://www.fao.org/geonetwork" xmlns:xs="http://www.w3.org/2001/XMLSchema">


  <!-- Copy all elements and attributes excluding GeoNetwork elements. 
    
    Geonet element could be gn:child, gn:element or extra node containing
    ELEMENT (used in dublin-core - GROUP_ELEMENT, CHOICE_ELEMENT).
    
    This could be useful to get the source XML when working on a metadocument.
    <xsl:if test="not(contains(name(.),'_ELEMENT'))">
  -->
  <xsl:template
    match="@*|
    node()[namespace-uri()!='http://www.fao.org/geonetwork' and not(contains(name(.),'_ELEMENT'))]"
    mode="gn-element-cleaner">
    <xsl:copy>
      <xsl:copy-of select="@*[namespace-uri()!='http://www.fao.org/geonetwork']"/>
      <xsl:apply-templates select="node()" mode="gn-element-cleaner"/>
    </xsl:copy>
  </xsl:template>
  <!-- Remove GeoNetwork info element and children -->
  <xsl:template mode="gn-element-cleaner" match="gn:info" priority="2"/>


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
                <error><xsl:value-of select="preceding-sibling::svrl:active-pattern[1]/@name"/> :
                  <xsl:copy-of select="svrl:text/*"/></error>
              </xsl:for-each>
            </xsl:when>
          </xsl:choose>
        </errors>
      </xsl:if>
    </xsl:variable>
    <xsl:copy-of select="if (count($listOfErrors//error) > 0) then $listOfErrors else ''"/>
  </xsl:template>
</xsl:stylesheet>
