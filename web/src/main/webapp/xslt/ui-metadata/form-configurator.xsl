<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:gn="http://www.fao.org/geonetwork"
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

    <xsl:choose>
      <xsl:when test="@name">
        <fieldset>
          <!-- TODO : get translation for labels -->
          <legend>
            <xsl:value-of select="@name"/>
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
      <!--<xsl:variable name="nodes" select="saxon:evaluate(concat('$p1/..', @xpath), $base)"/>-->
      <xsl:variable name="nodes">
        <saxon:call-template name="{concat('evaluate-', $schema)}">
          <xsl:with-param name="base" select="$base"/>
          <xsl:with-param name="in" select="@xpath"/>
        </saxon:call-template>
      </xsl:variable>


      <!-- Check if this field is controlled by a condition (eg. display that field for 
                service metadata record only).
                If @if expression return false, the field is not displayed. -->

      <xsl:variable name="isDisplayed">
        <xsl:choose>
          <xsl:when test="@if">
            <saxon:call-template name="{concat('evaluate-', $schema)}">
              <xsl:with-param name="base" select="$base"/>
              <xsl:with-param name="in" select="@if"/>
            </saxon:call-template>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="true()"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>

      <xsl:if test="$isDisplayed">
        <xsl:for-each select="$nodes">
          <saxon:call-template name="{concat('dispatch-', $schema)}">
            <xsl:with-param name="base" select="."/>
          </saxon:call-template>
        </xsl:for-each>

        <!-- TODO: for non existing node create a XML snippet to be edited 
        No match in current document. 2 scenario here:
        1) the requested element is a direct child of a node of the document. 
        In that case, a geonet:child element should exist in the document.
        
        2) the requested element is a subchild and is not described in the
        metadocument.
        -->
        <xsl:if test="normalize-space($nodes) = ''">

          <!-- Match non existing child also
      geonet:child[string(@name)='referenceSystemInfo']
      
 -->
          <xsl:choose>
            <!-- 1)
            FIXME: What happens if more than one matching geonet:child ?
            -->
            <xsl:when test="@localName">
              <xsl:variable name="elementLocalName" select="@localName"/>
              <saxon:call-template name="{concat('dispatch-', $schema)}">
                <xsl:with-param name="base"
                  select="$base/descendant-or-self::node()/gn:child[string(@name) = $elementLocalName]"
                />
              </saxon:call-template>
            </xsl:when>
            <!-- 2)
            
            -->
            <xsl:otherwise>
              <xsl:message>!<xsl:copy-of select="template"/></xsl:message>

              <div class="form-group">
                <label title="{@xpath}" class="col-lg-2">
                  <xsl:value-of select="@name"/>
                </label>
                <div class="col-lg-8">
                  <textarea class="form-control">
                    <xsl:copy-of select="template"/>
                  </textarea>
                </div>
              </div>

            </xsl:otherwise>
          </xsl:choose>
        </xsl:if>
      </xsl:if>
    </xsl:if>
  </xsl:template>
</xsl:stylesheet>
