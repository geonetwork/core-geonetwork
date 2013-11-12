<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:gn="http://www.fao.org/geonetwork"
  xmlns:gn-fn-metadata="http://geonetwork-opensource.org/xsl/functions/metadata"
  xmlns:saxon="http://saxon.sf.net/" extension-element-prefixes="saxon"
  exclude-result-prefixes="#all" version="2.0">
  <!-- 
    Build the form from the schema plugin form configuration.
    -->


  <!-- Create a fieldset in the editor with custom
    legend if attribute name is defined or default 
    legend according to the matching element. -->
  <xsl:template mode="form-builer" match="section[@name]|fieldset">
    <xsl:param name="base" as="node()"/>

    <xsl:variable name="sectionName" select="@name"/>

    <xsl:choose>
      <xsl:when test="$sectionName">
        <fieldset>
          <!-- Get translation for labels.
          If labels contains ':', search into labels.xml. -->
          <legend>
            <xsl:value-of
              select="if (contains($sectionName, ':')) 
                then gn-fn-metadata:getLabel($schema, $sectionName, $labels)/label 
                else $strings/*[name() = $sectionName]"
            />
          </legend>
          <xsl:apply-templates mode="form-builer" select="@*|*">
            <xsl:with-param name="base" select="$base"/>
          </xsl:apply-templates>
        </fieldset>
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates mode="form-builer" select="@*|*">
          <xsl:with-param name="base" select="$base"/>
        </xsl:apply-templates>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>



  <!-- Element to ignore in that mode -->
  <xsl:template mode="form-builer" match="@name"/>

  <!-- For each field, fieldset and section, check the matching xpath
    is in the current document. In that case dispatch to the schema mode
    or create an XML snippet editor for non matching document based on the
    template element. -->
  <xsl:template mode="form-builer" match="field|fieldset|section[@xpath]">
    <!-- The XML document to edit -->
    <xsl:param name="base" as="node()"/>

    <xsl:if test="@xpath">
      <!-- Match any nodes in the metadata with the XPath -->
      <!--<xsl:variable name="nodes" select="saxon:evaluate(concat('$p1/..', @xpath), $base)"/>
      does not work here because namespace of the context (ie. this XSL) are used
      to resolve the xpath. It needs to be in a profile specific XSL which declare profile
      namespaces. -->
      <xsl:variable name="nodes">
        <saxon:call-template name="{concat('evaluate-', $schema)}">
          <xsl:with-param name="base" select="$base"/>
          <xsl:with-param name="in" select="concat('/../', @xpath)"/>
        </saxon:call-template>
      </xsl:variable>

      <!-- Match any gn:child nodes from the metadocument which
      correspond to non existing node but available in the schema. -->
      <xsl:variable name="nonExistingChildParent">
        <xsl:if test="@or and @in">
          <saxon:call-template name="{concat('evaluate-', $schema)}">
            <xsl:with-param name="base" select="$base"/>
            <xsl:with-param name="in" select="concat('/../', @in, '[gn:child/@name=''', @or, ''']')"/>
          </saxon:call-template>
        </xsl:if>
      </xsl:variable>




      <!-- Check if this field is controlled by a condition (eg. display that field for 
                service metadata record only).
                If @if expression return false, the field is not displayed. -->
      <xsl:variable name="isDisplayed">
        <xsl:choose>
          <xsl:when test="@if">
            <saxon:call-template name="{concat('evaluate-', $schema)}">
              <xsl:with-param name="base" select="$base"/>
              <xsl:with-param name="in" select="concat('/../', @if)"/>
            </saxon:call-template>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="true()"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>
      <!--
      <xsl:message>Xpath         : <xsl:copy-of select="@xpath"/></xsl:message>
      <xsl:message>Matching nodes: <xsl:copy-of select="$nodes"/></xsl:message>
      <xsl:message>Non existing child path: <xsl:value-of select="concat(@in, '/gn:child[@name = ''', @or, ''']')"/></xsl:message>
      <xsl:message>Non existing child: <xsl:copy-of select="$nonExistingChildParent"/></xsl:message>
      <xsl:message>       display: <xsl:copy-of select="$isDisplayed"/></xsl:message>-->



      <!-- For non existing node create a XML snippet to be edited 
        No match in current document. 2 scenario here:
        1) the requested element is a direct child of a node of the document. 
        In that case, a geonet:child element should exist in the document.
        -->
      <xsl:choose>
        <xsl:when test="$isDisplayed and not(@templateModeOnly)">

          <!-- Display the matching node using standard editor mode
          propagating to the schema mode ... -->
          <xsl:for-each select="$nodes">
            <saxon:call-template name="{concat('dispatch-', $schema)}">
              <xsl:with-param name="base" select="."/>
            </saxon:call-template>
          </xsl:for-each>


          <!-- Display the matching non existing child node with a + control to 
          add it if :
            * a gn:child element is found and the ifNotExist attribute is not set
            in the editor configuration.
            or
            * a gn:child element is found, the matching node does not exist and
            the ifNotExist attribute is set - restrict cardinality to 0..1 even
            if element is 0..n in the schema.
           -->
          <xsl:if test="($nonExistingChildParent/* and not(@ifNotExist)) or 
            ($nonExistingChildParent/* and count($nodes/*) = 0 and @ifNotExist)">
            <xsl:variable name="childName" select="@or"/>

            <xsl:for-each select="$nonExistingChildParent/*/gn:child[@name = $childName]">
              <xsl:call-template name="render-element-to-add">
                <xsl:with-param name="label"
                  select="gn-fn-metadata:getLabel($schema, concat(@prefix, ':', @name), $labels)/label"/>
                <xsl:with-param name="childEditInfo" select="."/>
                <xsl:with-param name="parentEditInfo" select="../gn:element"/>
              </xsl:call-template>
            </xsl:for-each>
          </xsl:if>

        </xsl:when>
        <xsl:when test="$isDisplayed and (@templateModeOnly or template)">
          <xsl:message>xpath<xsl:value-of select="@xpath"/></xsl:message>
          <xsl:message>xpath<xsl:value-of select="@templateModeOnly"/></xsl:message>
          <!-- 
              templateModeOnly 
              
              or the requested element is a subchild and is not described in the
            metadocument. This mode will probably take precedence over the others
            if defined in a view.
            -->
          
          
          <xsl:variable name="xpath" select="@xpath"/>
          <xsl:variable name="name" select="@name"/>
          <xsl:variable name="template" select="template"/>
          
          <xsl:for-each select="$nodes/*">
            <!-- Retrieve matching key values 
              CHECKME: if more than one node match
              Only text values are supported. Separator is #.
              
              
              TODO: When existing, the template should be combined with 
              the existing node to add element not available in the template
              and available in the source XML document. 
              
              eg. editing a format which may be defined with the following
              <gmd:distributionFormat>
                  <gmd:MD_Format>
                    <gmd:name>
                      <gco:CharacterString>{{format}}</gco:CharacterString>
                    </gmd:name>
                    <gmd:version>
                      <gco:CharacterString>{{format_version}}</gco:CharacterString>
                    </gmd:version>
                  </gmd:MD_Format>
                </gmd:distributionFormat>
                extra elements (eg. specification) will not be part of the 
                templates and removed.
                
              -->
            <xsl:variable name="currentNode" select="."/>
            
            <xsl:variable name="keyValues">
              <xsl:for-each select="$template/values/key">
                <xsl:variable name="matchingNodeValue">
                  <saxon:call-template name="{concat('evaluate-', $schema)}">
                    <xsl:with-param name="base" select="$currentNode"/>
                    <xsl:with-param name="in" select="concat('/', @xpath)"/>
                  </saxon:call-template>
                </xsl:variable>
                
                <value><xsl:value-of select="normalize-space($matchingNodeValue)"/></value>
              </xsl:for-each>
            </xsl:variable>
            
            <!-- If the element exist, use the _X<ref> mode which
                  insert the snippet for the element if not use the 
                  XPATH mode which will create the new element at the 
                  correct location. -->
            <xsl:variable name="id" select="concat('_X', gn:element/@ref, '_replace')"/>
            
            <xsl:call-template name="render-element-template-field">
              <xsl:with-param name="name" select="$name"/>
              <xsl:with-param name="id" select="$id"/>
              <xsl:with-param name="isExisting" select="true()"/>
              <xsl:with-param name="template" select="$template"/>
              <xsl:with-param name="keyValues" select="$keyValues"/>
            </xsl:call-template>
          </xsl:for-each>
          
          
          <xsl:if test="count($nodes/*) = 0">
            <!-- If the element exist, use the _X<ref> mode which
            insert the snippet for the element if not use the 
            XPATH mode which will create the new element at the 
            correct location. -->
            <xsl:variable name="xpathFieldId" select="concat('_P', generate-id())"/>
            <xsl:variable name="id" select="concat($xpathFieldId, '_xml')"/>
            
            <xsl:call-template name="render-element-template-field">
              <xsl:with-param name="name" select="$name"/>
              <xsl:with-param name="id" select="$id"/>
              <xsl:with-param name="xpathFieldId" select="$xpathFieldId"/>
              <xsl:with-param name="isExisting" select="false()"/>
              <xsl:with-param name="template" select="$template"/>
            </xsl:call-template>
          </xsl:if>
          
        </xsl:when>
      </xsl:choose>
    </xsl:if>
  </xsl:template>
  
  
</xsl:stylesheet>
