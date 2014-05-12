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
    node()[namespace-uri()!='http://www.fao.org/geonetwork' and 
           not(contains(name(.),'_ELEMENT'))]"
    mode="gn-element-cleaner">
    <xsl:copy>
      <xsl:copy-of select="@*[namespace-uri()!='http://www.fao.org/geonetwork']"/>
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
    
    <xsl:copy>
      <xsl:copy-of select="@*"/>
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
    
    <xsl:variable name="nodeNames" select="@select"/>
    <!-- FIXME: Descendant could probably select more than 
    what is expected. -->
    <xsl:apply-templates mode="gn-element-cleaner" 
      select="$node-to-merge/descendant-or-self::node()[name() = $nodeNames]"/>
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
