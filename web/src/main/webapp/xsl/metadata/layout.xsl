<?xml version="1.0" encoding="UTF-8"?>
<!--
  All layout templates
  -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:exslt="http://exslt.org/common"
                xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:geonet="http://www.fao.org/geonetwork"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                xmlns:saxon="http://saxon.sf.net/"
                version="2.0" extension-element-prefixes="saxon"
                exclude-result-prefixes="#all">

  <xsl:import href="../text-utilities.xsl"/>

  <xsl:include href="../utils-fn.xsl"/>
  <xsl:include href="../utils.xsl"/>
  <xsl:include href="utility.xsl"/>
  <xsl:include href="validate-fn.xsl"/>
  <xsl:include href="layout-simple.xsl"/>
  <xsl:include href="layout-xml.xsl"/>
  <xsl:include href="controls.xsl"/>

  <xsl:template mode="schema" match="*">
    <xsl:choose>
      <xsl:when test="string(geonet:info/schema)!=''">
        <xsl:value-of select="geonet:info/schema"/>
      </xsl:when>
      <xsl:otherwise>UNKNOWN</xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!--
    hack to extract geonet URI; I know, I could have used a string constant like
    <xsl:variable name="geonetUri" select="'http://www.fao.org/geonetwork'"/>
    but this is more interesting
  -->
  <xsl:variable name="geonetNodeSet">
    <geonet:dummy/>
  </xsl:variable>

  <xsl:variable name="geonetUri">
    <xsl:value-of select="namespace-uri(exslt:node-set($geonetNodeSet)/*)"/>
  </xsl:variable>

  <xsl:variable name="currTab">
    <xsl:choose>
      <xsl:when test="/root/gui/currTab">
        <xsl:value-of select="/root/gui/currTab"/>
      </xsl:when>
      <xsl:otherwise>simple</xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <!--
  Control mode for current tab. Flat mode does not display non existing elements.
  -->
  <xsl:variable name="flat" select="/root/gui/config/metadata-tab/*[name(.)=$currTab]/@flat"/>
  <xsl:variable name="ancestorException"
                select="/root/gui/config/metadata-tab/*[name(.)=$currTab]/ancestorException/@for"/>
  <xsl:variable name="elementException"
                select="/root/gui/config/metadata-tab/*[name(.)=$currTab]/exception/@for"/>
  <xsl:variable name="flatException"
                select="/root/gui/config/metadata-tab/*[name(.)=$currTab]/flatException/@for"/>


  <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
  <!-- main schema mode selector -->
  <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

  <xsl:template mode="elementEP" match="*|@*">
    <xsl:param name="schema">
      <xsl:apply-templates mode="schema" select="."/>
    </xsl:param>
    <xsl:param name="edit" select="false()"/>
    <xsl:param name="embedded"/>
    <xsl:variable name="schemaTemplate" select="concat('metadata-',$schema)"/>
    <saxon:call-template name="{$schemaTemplate}">
      <xsl:with-param name="schema" select="$schema"/>
      <xsl:with-param name="edit" select="$edit"/>
      <xsl:with-param name="embedded" select="$embedded"/>
    </saxon:call-template>

  </xsl:template>

  <!--
  new children
  View mode variables (ie. $flat, $ancestorException and $elementException) are defined in XSL header.
  -->
  <xsl:template mode="elementEP" match="geonet:child">
    <xsl:param name="schema"/>
    <xsl:param name="edit" select="false()"/>
    <xsl:param name="embedded"/>

    <!-- draw child element place holder if
      - child is an OR element or
      - there is no other element with the name of this placeholder
    -->
    <xsl:variable name="name">
      <xsl:choose>
        <xsl:when test="@prefix=''">
          <xsl:value-of select="@name"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="concat(@prefix,':',@name)"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <!-- build a qualified name with COLON as the separator -->
    <xsl:variable name="qname">
      <xsl:choose>
        <xsl:when test="@prefix=''">
          <xsl:value-of select="@name"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="concat(@prefix,'COLON',@name)"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <xsl:variable name="parentName" select="../geonet:element/@ref|@parent"/>
    <xsl:variable name="max"
                  select="if (../geonet:element/@max) then ../geonet:element/@max else @max"/>
    <xsl:variable name="prevBrother" select="preceding-sibling::*[1]"/>

    <!--
      Exception for:
       * gmd:graphicOverview because GeoNetwork manage thumbnail using specific interface
       for thumbnail and large_thumbnail but user should be able to add  thumbnail using a simple URL.
       * from config-gui.xml (ancestor or element)
    -->
    <xsl:variable name="exception"
                  select="
      @name='graphicOverview'
      or count(ancestor::*[contains($ancestorException, local-name())]) > 0
      or contains($elementException, @name)
      "/>
    <xsl:variable name="isXLinked" select="count(ancestor-or-self::node()[@xlink:href]) > 0"/>

    <xsl:if test="(not($flat) or $exception) and not($isXLinked)">
      <xsl:if test="(geonet:choose or name($prevBrother)!=$name or $name='gmd:graphicOverview')">

        <xsl:variable name="text">
          <xsl:if test="geonet:choose">

            <xsl:variable name="defaultSelection"
                          select="/root/gui/config/editor-default-substitutions/element[@name=$name]/@default"/>

            <xsl:variable name="options">
              <options>
                <xsl:for-each select="geonet:choose">
                  <option name="{@name}">
                    <xsl:if test="@name = $defaultSelection">
                      <xsl:attribute name="selected">selected</xsl:attribute>
                    </xsl:if>

                    <xsl:call-template name="getTitle">
                      <xsl:with-param name="name" select="@name"/>
                      <xsl:with-param name="schema" select="$schema"/>
                    </xsl:call-template>
                  </option>
                </xsl:for-each>
              </options>
            </xsl:variable>

            <select class="md" name="_{$parentName}_{$qname}" size="1">
              <xsl:if test="$isXLinked">
                <xsl:attribute name="disabled">disabled</xsl:attribute>
              </xsl:if>
              <xsl:for-each select="exslt:node-set($options)//option">
                <xsl:sort select="."/>
                <option value="{@name}">
                  <xsl:value-of select="concat(., ' (', @name, ')')"/>
                </option>
              </xsl:for-each>
            </select>
          </xsl:if>
        </xsl:variable>
        <xsl:variable name="id" select="@uuid"/>
        <xsl:variable name="addLink">
          <xsl:choose>
            <xsl:when test="geonet:choose">
              <xsl:value-of
                select="concat('doNewORElementAction(',$apos,'metadata.elem.add.new',$apos,',',$parentName,',',$apos,$name,$apos,',document.mainForm._',$parentName,'_',$qname,'.value,',$apos,$id,$apos,',',$apos,@action,$apos,',',$max,');')"
              />
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of
                select="concat('doNewElementAction(',$apos,'metadata.elem.add.new',$apos,',',$parentName,',',$apos,$name,$apos,',',$apos,$id,$apos,',',$apos,@action,$apos,',',$max,');')"
              />
            </xsl:otherwise>
          </xsl:choose>
        </xsl:variable>
        <xsl:variable name="addXMLFragment">
          <!-- Add the XML fragment selector for lonely geonet:child elements -->
          <xsl:variable name="function">
            <xsl:apply-templates mode="addXMLFragment" select="."/>
          </xsl:variable>
          <xsl:if test="normalize-space($function)!=''">
            <xsl:value-of
              select="concat('javascript:', $function, '(',$parentName,',',$apos,$name,$apos, ', this);')"
            />
          </xsl:if>
        </xsl:variable>
        <xsl:variable name="addXmlFragmentSubTemplate">
          <xsl:call-template name="addXMLFragment">
            <xsl:with-param name="id" select="$id"/>
            <xsl:with-param name="subtemplate" select="true()"/>
            <xsl:with-param name="schema" select="$schema"/>
          </xsl:call-template>
        </xsl:variable>
        <xsl:variable name="helpLink">
          <xsl:call-template name="getHelpLink">
            <xsl:with-param name="name" select="$name"/>
            <xsl:with-param name="schema" select="$schema"/>
          </xsl:call-template>
        </xsl:variable>

        <xsl:call-template name="simpleElementGui">
          <xsl:with-param name="title">
            <xsl:call-template name="getTitle">
              <xsl:with-param name="name" select="$name"/>
              <xsl:with-param name="schema" select="$schema"/>
            </xsl:call-template>
          </xsl:with-param>
          <xsl:with-param name="text" select="$text"/>
          <xsl:with-param name="addLink" select="$addLink"/>
          <xsl:with-param name="addXMLFragment" select="$addXMLFragment"/>
          <xsl:with-param name="addXMLFragmentSubTemplate" select="$addXmlFragmentSubTemplate"/>
          <xsl:with-param name="helpLink" select="$helpLink"/>
          <xsl:with-param name="edit" select="$edit"/>
          <xsl:with-param name="id" select="$id"/>
        </xsl:call-template>
      </xsl:if>
    </xsl:if>
  </xsl:template>

  <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
  <!-- callbacks from schema templates -->
  <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

  <xsl:template mode="element" match="*|@*">
    <xsl:param name="schema"/>
    <xsl:param name="edit" select="false()"/>
    <xsl:param name="flat" select="false()"/>
    <xsl:param name="embedded"/>

    <xsl:choose>
      <!-- has children or attributes, existing or potential -->
      <xsl:when
        test="*[namespace-uri(.)!=$geonetUri]|@*|geonet:child|geonet:element/geonet:attribute">
        <xsl:choose>

          <!-- display as a list -->
          <xsl:when test="$flat=true() and not(contains($flatException, local-name(.)))">

            <!-- if it does not have children show it as a simple element -->
            <xsl:if
              test="not(*[namespace-uri(.)!=$geonetUri]|geonet:child|geonet:element/geonet:attribute)">
              <xsl:apply-templates mode="simpleElement" select=".">
                <xsl:with-param name="schema" select="$schema"/>
                <xsl:with-param name="edit" select="$edit"/>
              </xsl:apply-templates>
            </xsl:if>

            <!-- existing and new children -->
            <xsl:apply-templates mode="elementEP"
                                 select="*[namespace-uri(.)!=$geonetUri]|geonet:child">
              <xsl:with-param name="schema" select="$schema"/>
              <xsl:with-param name="edit" select="$edit"/>
            </xsl:apply-templates>
          </xsl:when>

          <!-- display boxed -->
          <xsl:otherwise>
            <xsl:apply-templates mode="complexElement" select=".">
              <xsl:with-param name="schema" select="$schema"/>
              <xsl:with-param name="edit" select="$edit"/>
            </xsl:apply-templates>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>

      <!-- neither children nor attributes, just text -->
      <xsl:otherwise>
        <xsl:apply-templates mode="simpleElement" select=".">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="edit" select="$edit"/>
        </xsl:apply-templates>
      </xsl:otherwise>

    </xsl:choose>
  </xsl:template>

  <xsl:template mode="simpleElement" match="*">
    <xsl:param name="schema"/>
    <xsl:param name="edit" select="false()"/>
    <xsl:param name="editAttributes" select="true()"/>
    <xsl:param name="refs"/>
    <xsl:param name="title">
      <xsl:call-template name="getTitle">
        <xsl:with-param name="name" select="name(.)"/>
        <xsl:with-param name="schema" select="$schema"/>
      </xsl:call-template>
    </xsl:param>
    <xsl:param name="text">
      <xsl:call-template name="getElementText">
        <xsl:with-param name="schema" select="$schema"/>
        <xsl:with-param name="edit" select="$edit"/>
      </xsl:call-template>
    </xsl:param>
    <xsl:param name="helpLink">
      <xsl:call-template name="getHelpLink">
        <xsl:with-param name="name" select="name(.)"/>
        <xsl:with-param name="schema" select="$schema"/>
      </xsl:call-template>
    </xsl:param>

    <xsl:choose>
      <xsl:when test="$edit=true()">
        <xsl:call-template name="editSimpleElement">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="title" select="$title"/>
          <xsl:with-param name="editAttributes" select="$editAttributes"/>
          <xsl:with-param name="text" select="$text"/>
          <xsl:with-param name="helpLink" select="$helpLink"/>
          <xsl:with-param name="refs" select="$refs"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="showSimpleElement">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="title" select="$title"/>
          <xsl:with-param name="text" select="$text"/>
          <xsl:with-param name="helpLink" select="$helpLink"/>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template mode="simpleElement" match="@*"/>

  <xsl:template name="simpleAttribute" mode="simpleAttribute" match="@*" priority="2">
    <xsl:param name="schema"/>
    <xsl:param name="edit" select="false()"/>
    <xsl:param name="title">
      <xsl:call-template name="getTitle">
        <xsl:with-param name="name" select="name(.)"/>
        <xsl:with-param name="schema" select="$schema"/>
      </xsl:call-template>
    </xsl:param>
    <xsl:param name="text">
      <xsl:call-template name="getAttributeText">
        <xsl:with-param name="schema" select="$schema"/>
        <xsl:with-param name="edit" select="$edit"/>
      </xsl:call-template>
    </xsl:param>
    <xsl:param name="helpLink">
      <xsl:call-template name="getHelpLink">
        <xsl:with-param name="name" select="name(.)"/>
        <xsl:with-param name="schema" select="$schema"/>
      </xsl:call-template>
    </xsl:param>
    <xsl:choose>
      <xsl:when test="$edit=true()">
        <xsl:variable name="name" select="name(.)"/>
        <xsl:variable name="id" select="concat('_', ../geonet:element/@ref, '_', $name)"/>

        <xsl:call-template name="editAttribute">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="title" select="$title"/>
          <xsl:with-param name="id" select="concat($id, '_block')"/>
          <xsl:with-param name="text" select="$text"/>
          <xsl:with-param name="helpLink" select="$helpLink"/>
          <xsl:with-param name="name" select="$name"/>
          <xsl:with-param name="elemId" select="geonet:element/@uuid"/>
          <xsl:with-param name="removeLink">
            <xsl:if test="count(../geonet:attribute[@name=$name and @del])!=0">
              <xsl:value-of
                select="concat('doRemoveAttributeAction(',$apos,'/metadata.attr.delete',$apos,',',$apos,$id,$apos,',',$apos,../geonet:element/@ref,$apos,',', $apos,$id,$apos,',',$apos,$apos,');')"
              />
            </xsl:if>
          </xsl:with-param>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="showSimpleElement">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="title" select="$title"/>
          <xsl:with-param name="text" select="$text"/>
          <xsl:with-param name="helpLink" select="$helpLink"/>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>


  <!-- Display non existing geonet:attribute -->
  <xsl:template mode="simpleAttribute" match="geonet:attribute" priority="2">
    <xsl:param name="schema"/>
    <xsl:param name="edit" select="false()"/>
    <xsl:param name="title">
      <xsl:call-template name="getTitle">
        <xsl:with-param name="name" select="@name"/>
        <xsl:with-param name="schema" select="$schema"/>
      </xsl:call-template>
    </xsl:param>
    <xsl:param name="text"/>
    <xsl:param name="helpLink">
      <xsl:call-template name="getHelpLink">
        <xsl:with-param name="name" select="name(.)"/>
        <xsl:with-param name="schema" select="$schema"/>
      </xsl:call-template>
    </xsl:param>
    <xsl:variable name="name" select="@name"/>

    <!-- Display non existing child only -->
    <xsl:if test="$edit=true() and count(../@*[name(.)=$name])=0">
      <xsl:variable name="id"
                    select="concat('_', ../geonet:element/@ref, '_', replace(@name, ':', 'COLON'))"/>
      <xsl:call-template name="editAttribute">
        <xsl:with-param name="schema" select="$schema"/>
        <xsl:with-param name="title" select="$title"/>
        <xsl:with-param name="text" select="$text"/>
        <xsl:with-param name="helpLink" select="$helpLink"/>
        <xsl:with-param name="name" select="@name"/>
        <xsl:with-param name="elemId" select="../geonet:element/@uuid"/>
        <xsl:with-param name="addLink">
          <xsl:if test="@add='true'">
            <xsl:value-of
              select="concat('doNewAttributeAction(',$apos,'metadata.elem.add.new',$apos,',',../geonet:element/@ref,',',$apos,@name,$apos,',',
              $apos,$id,$apos,',',$apos,'add',$apos,');')"
            />
          </xsl:if>
        </xsl:with-param>
      </xsl:call-template>
    </xsl:if>
  </xsl:template>


  <xsl:template mode="complexElement" match="*">
    <xsl:param name="schema"/>
    <xsl:param name="edit" select="false()"/>
    <xsl:param name="title">
      <xsl:call-template name="getTitle">
        <xsl:with-param name="name" select="name(.)"/>
        <xsl:with-param name="schema" select="$schema"/>
      </xsl:call-template>
    </xsl:param>
    <xsl:param name="content">
      <xsl:call-template name="getContent">
        <xsl:with-param name="schema" select="$schema"/>
        <xsl:with-param name="edit" select="$edit"/>
      </xsl:call-template>
    </xsl:param>
    <xsl:param name="helpLink">
      <xsl:call-template name="getHelpLink">
        <xsl:with-param name="name" select="name(.)"/>
        <xsl:with-param name="schema" select="$schema"/>
      </xsl:call-template>
    </xsl:param>

    <xsl:choose>
      <xsl:when test="$edit=true()">
        <xsl:call-template name="editComplexElement">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="title" select="$title"/>
          <xsl:with-param name="content" select="$content"/>
          <xsl:with-param name="helpLink" select="$helpLink"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="showComplexElement">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="title" select="$title"/>
          <xsl:with-param name="content" select="$content"/>
          <xsl:with-param name="helpLink" select="$helpLink"/>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>

  </xsl:template>

  <!--
  prevent drawing of geonet:* elements
  -->
  <xsl:template mode="element"
                match="geonet:null|geonet:element|geonet:info|geonet:attribute|geonet:schematronerrors|@geonet:xsderror|@xlink:type|@gco:isoType"/>
  <xsl:template mode="simpleElement"
                match="geonet:null|geonet:element|geonet:info|geonet:attribute|geonet:schematronerrors|@geonet:xsderror|@xlink:type|@gco:isoType"/>
  <xsl:template mode="complexElement"
                match="geonet:null|geonet:element|geonet:info|geonet:attribute|geonet:schematronerrors|@geonet:xsderror|@xlink:type|@gco:isoType"/>
  <xsl:template mode="simpleAttribute" match="@geonet:xsderror|@geonet:addedObj" priority="2"/>
  <!--
  prevent drawing of attributes starting with "_", used in old GeoNetwork versions
  -->
  <xsl:template mode="simpleElement" match="@*[starts-with(name(.),'_')]"/>

  <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
  <!-- elements/attributes templates -->
  <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->


  <!--
  shows a simple element
  -->
  <xsl:template name="showSimpleElement">
    <xsl:param name="schema"/>
    <xsl:param name="title"/>
    <xsl:param name="text"/>
    <xsl:param name="helpLink"/>
    <xsl:variable name="hiddenChildren">
      <xsl:call-template name="hasHiddenChildren"/>
    </xsl:variable>

    <!-- don't show it if there isn't anything in it! -->
    <xsl:choose>
      <xsl:when test="$hiddenChildren = true() and normalize-space($text)=''">
        <xsl:call-template name="hiddenElement">
          <xsl:with-param name="title" select="$title"/>
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="helpLink" select="$helpLink"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="normalize-space($text)!=''">
        <xsl:call-template name="simpleElementGui">
          <xsl:with-param name="title" select="$title"/>
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="text" select="$text"/>
          <xsl:with-param name="helpLink" select="$helpLink"/>
        </xsl:call-template>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <!--
  shows a complex element
  -->
  <xsl:template name="showComplexElement">
    <xsl:param name="schema"/>
    <xsl:param name="title"/>
    <xsl:param name="content"/>
    <xsl:param name="helpLink"/>
    <xsl:variable name="hiddenChildren">
      <xsl:call-template name="hasHiddenChildren"/>
    </xsl:variable>

    <!-- don't show it if there isn't anything in it! -->
    <xsl:choose>
      <xsl:when test="$hiddenChildren = true() and normalize-space($content)=''">
        <xsl:call-template name="hiddenElement">
          <xsl:with-param name="title" select="$title"/>
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="helpLink" select="$helpLink"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="normalize-space($content)!=''">
        <xsl:call-template name="complexElementGui">
          <xsl:with-param name="title" select="$title"/>
          <xsl:with-param name="text" select="text()"/>
          <xsl:with-param name="content" select="$content"/>
          <xsl:with-param name="helpLink" select="$helpLink"/>
          <xsl:with-param name="schema" select="$schema"/>
        </xsl:call-template>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <!--
  shows editable fields for a simple element
  -->
  <xsl:template name="editSimpleElement">
    <xsl:param name="schema"/>
    <xsl:param name="title"/>
    <xsl:param name="editAttributes"/>
    <xsl:param name="text"/>
    <xsl:param name="helpLink"/>
    <!-- A comma separated values of reference to element to be deleted by (-).
      No (+) and (down) are displayed.
    -->
    <xsl:param name="refs"/>

    <!-- if it's the last brother of it's type and there is a new brother make addLink -->

    <xsl:variable name="id" select="geonet:element/@uuid"/>
    <xsl:variable name="addLink">
      <xsl:if test="$refs=''">
        <xsl:call-template name="addLink">
          <xsl:with-param name="id" select="$id"/>
        </xsl:call-template>
      </xsl:if>
    </xsl:variable>
    <xsl:variable name="addXMLFragment">
      <xsl:call-template name="addXMLFragment">
        <xsl:with-param name="id" select="$id"/>
        <xsl:with-param name="schema" select="$schema"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="removeLink">
      <xsl:value-of
        select="concat('doRemoveElementAction(',$apos,'metadata.elem.delete.new',$apos,',',$apos, if ($refs!='') then $refs else geonet:element/@ref,$apos,',',geonet:element/@parent,',',$apos,$id,$apos,',',geonet:element/@min,');')"/>
      <xsl:if test="not(geonet:element/@del='true')">
        <xsl:text>!OPTIONAL</xsl:text>
      </xsl:if>
    </xsl:variable>
    <xsl:variable name="upLink">
      <xsl:value-of
        select="concat('doMoveElementAction(',$apos,'metadata.elem.up',$apos,',',geonet:element/@ref,',',$apos,$id,$apos,');')"/>
      <xsl:if test="not(geonet:element/@up='true')">
        <xsl:text>!OPTIONAL</xsl:text>
      </xsl:if>
    </xsl:variable>
    <xsl:variable name="downLink">
      <xsl:if test="$refs=''">
        <xsl:value-of
          select="concat('doMoveElementAction(',$apos,'metadata.elem.down',$apos,',',geonet:element/@ref,',',$apos,$id,$apos,');')"/>
        <xsl:if test="not(geonet:element/@down='true')">
          <xsl:text>!OPTIONAL</xsl:text>
        </xsl:if>
      </xsl:if>
    </xsl:variable>
    <!-- xsd and schematron validation info -->
    <xsl:variable name="validationLink">
      <xsl:variable name="ref" select="concat('#_',geonet:element/@ref)"/>
      <xsl:call-template name="validationLink">
        <xsl:with-param name="ref" select="$ref"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="hiddenChildren">
      <xsl:call-template name="hasHiddenChildren"/>
    </xsl:variable>

    <!-- don't show it if there isn't anything in it! -->
    <xsl:choose>
      <xsl:when test="$hiddenChildren = true()">
        <xsl:call-template name="hiddenElement">
          <xsl:with-param name="title" select="$title"/>
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="helpLink" select="$helpLink"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="simpleElementGui">
          <xsl:with-param name="title" select="$title"/>
          <xsl:with-param name="text" select="$text"/>
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="addLink" select="$addLink"/>
          <xsl:with-param name="addXMLFragment" select="$addXMLFragment"/>
          <xsl:with-param name="removeLink" select="$removeLink"/>
          <xsl:with-param name="upLink" select="$upLink"/>
          <xsl:with-param name="downLink" select="$downLink"/>
          <xsl:with-param name="helpLink" select="$helpLink"/>
          <xsl:with-param name="validationLink" select="$validationLink"/>
          <xsl:with-param name="edit" select="true()"/>
          <xsl:with-param name="editAttributes" select="$editAttributes"/>
          <xsl:with-param name="id" select="$id"/>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>


  <xsl:template name="addLink">
    <xsl:param name="id"/>

    <xsl:variable name="name" select="name(.)"/>
    <xsl:variable name="nextBrother" select="following-sibling::*[1]"/>
    <xsl:variable name="nb">
      <xsl:if test="name($nextBrother)='geonet:child'">
        <xsl:choose>
          <xsl:when test="$nextBrother/@prefix=''">
            <xsl:if test="$nextBrother/@name=$name">
              <xsl:copy-of select="$nextBrother"/>
            </xsl:if>
          </xsl:when>
          <xsl:otherwise>
            <xsl:if test="concat($nextBrother/@prefix,':',$nextBrother/@name)=$name">
              <xsl:copy-of select="$nextBrother"/>
            </xsl:if>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:if>
    </xsl:variable>
    <xsl:variable name="newBrother" select="exslt:node-set($nb)"/>

    <xsl:choose>
      <!-- place + because schema insists ie. next element is geonet:child -->
      <xsl:when test="$newBrother/* and not($newBrother/*/geonet:choose)">
        <xsl:value-of
          select="concat('doNewElementAction(',$apos,'metadata.elem.add.new',$apos,',',geonet:element/@parent,',',$apos,name(.),$apos,',',$apos,$id,$apos,',',$apos,'add',$apos,',',geonet:element/@max,');')"
        />
      </xsl:when>
      <!-- place optional + for use when re-ordering etc -->
      <xsl:when test="geonet:element/@add='true' and name($nextBrother)=name(.)">
        <xsl:value-of
          select="concat('doNewElementAction(',$apos,'metadata.elem.add.new',$apos,',',geonet:element/@parent,',',$apos,name(.),$apos,',',$apos,$id,$apos,',',$apos,'add',$apos,',',geonet:element/@max,');!OPTIONAL')"
        />
      </xsl:when>
      <!-- place + because schema insists but no geonet:child nextBrother
           this case occurs in the javascript handling of the + -->
      <xsl:when test="geonet:element/@add='true' and not($newBrother/*/geonet:choose)">
        <xsl:value-of
          select="concat('doNewElementAction(',$apos,'metadata.elem.add.new',$apos,',',geonet:element/@parent,',',$apos,name(.),$apos,',',$apos,$id,$apos,',',$apos,'add',$apos,',',geonet:element/@max,');')"
        />
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <!--
    Add elements : will popup a remote element selector
    and add the XML fragment in the metadata
  -->
  <xsl:template name="addXMLFragment">
    <xsl:param name="id"/>
    <xsl:param name="subtemplate" select="false()"/>
    <xsl:param name="schema"/>


    <xsl:variable name="name" select="name(.)"/>

    <!-- Some sub-template are relevant in different type of elements
    TODO : improve, at least move to schema XSL as this is schema based.
    -->
    <xsl:variable name="elementName"
                  select="if (name(.)='geonet:child') then concat(./@prefix,':',./@name) else $name"/>
    <xsl:variable name="subTemplateName"
                  select="/root/gui/config/editor-subtemplate/mapping/subtemplate[parent/@id=$elementName]/@type"/>

    <xsl:variable name="function">
      <xsl:choose>
        <xsl:when test="$subtemplate">
          <!-- FIXME: remove ref to editorPanel -->
          <xsl:if test="count(/root/gui/subtemplates/record[type=$subTemplateName]) &gt; 0"
          >Ext.getCmp('editorPanel').showSubTemplateSelectionPanel
          </xsl:if>
        </xsl:when>
        <xsl:otherwise>
          <xsl:apply-templates mode="addXMLFragment" select="."/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>


    <xsl:variable name="namespaces">
      <xsl:value-of select="/root/gui/schemalist/name[text()=$schema]/@namespaces"/>
    </xsl:variable>

    <xsl:choose>
      <!-- Create link only when a function is available -->
      <xsl:when test="normalize-space($function)!=''">

        <!--
          Example with contact :
          a) a non existing contact (citedResponsibleParty)
          <geonet:child name="identifier" prefix="gmd" ...
          <geonet:child name="citedResponsibleParty" prefix="gmd" ...
          <gmd:presentationForm>
           ...

           b) an existing one
           <gmd:pointOfContact>...</gmd:pointOfContact>
           <geonet:child name="pointOfContact" prefix="gmd"
        -->

        <!-- Retrieve the next geonet:child brother having the same defined in prefix and name attribute -->
        <xsl:variable name="nextBrother" select="following-sibling::*[1]"/>
        <xsl:variable name="nb">
          <xsl:if test="name($nextBrother)='geonet:child'">
            <xsl:choose>
              <xsl:when test="$nextBrother/@prefix=''">
                <xsl:if test="$nextBrother/@name=$name">
                  <xsl:copy-of select="$nextBrother"/>
                </xsl:if>
              </xsl:when>
              <xsl:otherwise>
                <xsl:if test="concat($nextBrother/@prefix,':',$nextBrother/@name)=$name">
                  <xsl:copy-of select="$nextBrother"/>
                </xsl:if>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:if>
        </xsl:variable>

        <xsl:variable name="newBrother" select="exslt:node-set($nb)"/>

        <xsl:choose>
          <!--
            with a new brother similar to current :
            place button because schema insists ie. next element is geonet:child -->
          <xsl:when
            test="$newBrother/* and not($newBrother/*/geonet:choose) and $nextBrother/@prefix=''">
            <xsl:value-of
              select="concat('javascript:', $function, '(',../geonet:element/@ref,',',$apos,$nextBrother/@name,$apos,', this);')"
            />
          </xsl:when>
          <xsl:when test="$newBrother/* and not($newBrother/*/geonet:choose)">
            <xsl:choose>
              <xsl:when test="$subtemplate">
                <xsl:value-of
                  select="concat('javascript:', $function, '(',../geonet:element/@ref,',',$apos,$nextBrother/@prefix,':',$nextBrother/@name,$apos, ',', $apos, $subTemplateName, $apos, ',', $apos, $namespaces, $apos, ');')"
                />
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of
                  select="concat('javascript:', $function, '(',../geonet:element/@ref,',',$apos,$nextBrother/@prefix,':',$nextBrother/@name,$apos,', this);')"
                />
              </xsl:otherwise>
            </xsl:choose>
          </xsl:when>
          <!-- place optional +/x for use when re-ordering etc -->
          <xsl:when test="geonet:element/@add='true' and name($nextBrother)=name(.)">
            <xsl:value-of
              select="concat('javascript:', $function, '(',../geonet:element/@ref,',',$apos,$nextBrother/@name,$apos,', this);!OPTIONAL')"
            />
          </xsl:when>
          <!-- place +/x because schema insists but no geonet:child nextBrother
               this case occurs in the javascript handling of the +/+ -->
          <xsl:when test="geonet:element/@add='true' and not($newBrother/*/geonet:choose)">
            <xsl:value-of
              select="concat('javascript:', $function, '(',geonet:element/@parent,',',$apos,$name,$apos,');')"
            />
          </xsl:when>
          <!-- A lonely geonet:child element to replace, propose the add button.
          Always a sub-template.
          TODO : not sure about action=before, required for gmd:report, probably related to geonet:choose element
          -->
          <xsl:when test="$name='geonet:child' and (@action='replace' or @action='before')">
            <xsl:value-of
              select="concat('javascript:', $function, '(', ../geonet:element/@ref, ', ', $apos, $elementName,  $apos, ',', $apos, $subTemplateName, $apos, ',', $apos, $namespaces, $apos, ');')"
            />
          </xsl:when>
        </xsl:choose>
      </xsl:when>
    </xsl:choose>

  </xsl:template>


  <!--
  shows editable fields for an attribute
  -->
  <xsl:template name="editAttribute">
    <xsl:param name="schema"/>
    <xsl:param name="title"/>
    <xsl:param name="id"/>
    <xsl:param name="text"/>
    <xsl:param name="helpLink"/>
    <xsl:param name="elemId"/>
    <xsl:param name="name"/>
    <xsl:param name="addLink"/>
    <xsl:param name="removeLink"/>

    <xsl:variable name="value" select="string(.)"/>

    <xsl:call-template name="simpleElementGui">
      <xsl:with-param name="title" select="$title"/>
      <xsl:with-param name="text" select="$text"/>
      <xsl:with-param name="id" select="$id"/>
      <xsl:with-param name="helpLink" select="$helpLink"/>
      <xsl:with-param name="edit" select="true()"/>
      <xsl:with-param name="addLink" select="$addLink"/>
      <xsl:with-param name="removeLink" select="$removeLink"/>
    </xsl:call-template>
  </xsl:template>

  <!--
  shows editable fields for a complex element
  -->
  <xsl:template name="editComplexElement">
    <xsl:param name="schema"/>
    <xsl:param name="title"/>
    <xsl:param name="content"/>
    <xsl:param name="helpLink"/>

    <xsl:variable name="id" select="geonet:element/@uuid"/>
    <xsl:variable name="addLink">
      <xsl:call-template name="addLink">
        <xsl:with-param name="id" select="$id"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="addXMLFragment">
      <xsl:call-template name="addXMLFragment">
        <xsl:with-param name="id" select="$id"/>
        <xsl:with-param name="subtemplate" select="false()"/>
        <xsl:with-param name="schema" select="$schema"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="addXmlFragmentSubTemplate">
      <xsl:call-template name="addXMLFragment">
        <xsl:with-param name="id" select="$id"/>
        <xsl:with-param name="subtemplate" select="true()"/>
        <xsl:with-param name="schema" select="$schema"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="removeLink">
      <xsl:value-of
        select="concat('doRemoveElementAction(',$apos,'metadata.elem.delete.new',$apos,',',geonet:element/@ref,',',geonet:element/@parent,',',$apos,$id,$apos,',',geonet:element/@min,');')"/>
      <xsl:if test="not(geonet:element/@del='true')">
        <xsl:text>!OPTIONAL</xsl:text>
      </xsl:if>
    </xsl:variable>
    <xsl:variable name="upLink">
      <xsl:value-of
        select="concat('doMoveElementAction(',$apos,'metadata.elem.up',$apos,',',geonet:element/@ref,',',$apos,$id,$apos,');')"/>
      <xsl:if test="not(geonet:element/@up='true')">
        <xsl:text>!OPTIONAL</xsl:text>
      </xsl:if>
    </xsl:variable>
    <xsl:variable name="downLink">
      <xsl:value-of
        select="concat('doMoveElementAction(',$apos,'metadata.elem.down',$apos,',',geonet:element/@ref,',',$apos,$id,$apos,');')"/>
      <xsl:if test="not(geonet:element/@down='true')">
        <xsl:text>!OPTIONAL</xsl:text>
      </xsl:if>
    </xsl:variable>
    <!-- xsd and schematron validation info -->
    <xsl:variable name="validationLink">
      <xsl:variable name="ref" select="concat('#_',geonet:element/@ref)"/>
      <xsl:call-template name="validationLink">
        <xsl:with-param name="ref" select="$ref"/>
      </xsl:call-template>
    </xsl:variable>

    <xsl:call-template name="complexElementGui">
      <xsl:with-param name="title" select="$title"/>
      <xsl:with-param name="text" select="text()"/>
      <xsl:with-param name="content" select="$content"/>
      <xsl:with-param name="addLink" select="$addLink"/>
      <xsl:with-param name="addXMLFragment" select="$addXMLFragment"/>
      <xsl:with-param name="addXmlFragmentSubTemplate" select="$addXmlFragmentSubTemplate"/>
      <xsl:with-param name="removeLink" select="$removeLink"/>
      <xsl:with-param name="upLink" select="$upLink"/>
      <xsl:with-param name="downLink" select="$downLink"/>
      <xsl:with-param name="helpLink" select="$helpLink"/>
      <xsl:with-param name="validationLink" select="$validationLink"/>
      <xsl:with-param name="schema" select="$schema"/>
      <xsl:with-param name="edit" select="true()"/>
      <xsl:with-param name="id" select="$id"/>
    </xsl:call-template>
  </xsl:template>


  <!-- =============================================================================
    Create a complex element with the content param in it.

    @param id : If using complexElementGuiWrapper in a same for-each statement, generate-id function will
    be identical for all call to this template (because id is computed on base node).
    In some situation it could be better to define id parameter when calling the template
    to override default values (eg. id are used for collapsible fieldset).
  -->

  <xsl:template name="complexElementGuiWrapper">
    <xsl:param name="title"/>
    <xsl:param name="content"/>
    <xsl:param name="schema"/>
    <xsl:param name="group"/>
    <xsl:param name="edit"/>
    <xsl:param name="realname" select="name(.)"/>
    <xsl:param name="id" select="generate-id(.)"/>

    <!-- do not show empty elements when editing -->

    <xsl:choose>
      <xsl:when test="normalize-space($content)!=''">
        <xsl:call-template name="complexElementGui">
          <xsl:with-param name="title" select="$title"/>
          <xsl:with-param name="content" select="$content"/>
          <xsl:with-param name="helpLink">
            <xsl:call-template name="getHelpLink">
              <xsl:with-param name="name" select="$realname"/>
              <xsl:with-param name="schema" select="$schema"/>
            </xsl:call-template>
          </xsl:with-param>
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="id" select="$id"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:choose>
          <xsl:when test="$edit">
            <xsl:call-template name="complexElementGui">
              <xsl:with-param name="title" select="$title"/>
              <xsl:with-param name="content">
                <span class="missing">-
                  <xsl:value-of select="/root/gui/strings/missingSeeTab"/>
                  "<xsl:value-of select="$group"/>" -
                </span>
              </xsl:with-param>
              <xsl:with-param name="helpLink">
                <xsl:call-template name="getHelpLink">
                  <xsl:with-param name="name" select="$realname"/>
                  <xsl:with-param name="schema" select="$schema"/>
                </xsl:call-template>
              </xsl:with-param>
              <xsl:with-param name="schema" select="$schema"/>
              <xsl:with-param name="id" select="$id"/>
            </xsl:call-template>
          </xsl:when>
          <xsl:otherwise>
            <xsl:call-template name="complexElementGui">
              <xsl:with-param name="title" select="$title"/>
              <xsl:with-param name="helpLink">
                <xsl:call-template name="getHelpLink">
                  <xsl:with-param name="name" select="$realname"/>
                  <xsl:with-param name="schema" select="$schema"/>
                </xsl:call-template>
              </xsl:with-param>
              <xsl:with-param name="content">
                <span class="missing">-
                  <xsl:value-of select="/root/gui/strings/missing"/> -
                </span>
              </xsl:with-param>
              <xsl:with-param name="schema" select="$schema"/>
              <xsl:with-param name="id" select="$id"/>
            </xsl:call-template>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:otherwise>
    </xsl:choose>

  </xsl:template>

  <!--
  returns the content of a complex element
  -->
  <xsl:template name="getContent">
    <xsl:param name="schema"/>
    <xsl:param name="edit" select="false()"/>

    <xsl:choose>
      <xsl:when test="$edit=true()">
        <xsl:apply-templates mode="elementEP" select="@*">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="edit" select="true()"/>
        </xsl:apply-templates>
        <xsl:apply-templates mode="elementEP" select="*[namespace-uri(.)!=$geonetUri]|geonet:child">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="edit" select="true()"/>
        </xsl:apply-templates>
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates mode="elementEP" select="@*">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="edit" select="false()"/>
        </xsl:apply-templates>
        <xsl:apply-templates mode="elementEP" select="*">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="edit" select="false()"/>
        </xsl:apply-templates>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- Create an helper list for the current input element.
    Current input could be an element or an attribute (eg. uom).

  In editing mode, for gco:CharacterString elements (with no codelist
  or enumeration defined in the schema) an helper list could be defined
  in loc files (labels.xml) using the helper tag.
  <element name="gmd:denominator" id="57.0">
      <label>Denominator</label>
      <helper>
        <option value="5000">1:5´000</option>
        <option value="10000">1:10´000</option>
        ...

  Then a list of values is displayed next to the input field.

  The helper list could be sorted if stort attribute is set to true:
  <helper sort="true" ...

  By default, the list of suggestion is displayed in a combo next to the
  element. The layout may be customized by setting the editorMode. Supported
  modes are:
  * radio
  * radio_withdesc
  * radio_linebreak

  <helper editorMode="radio_withdesc" ...

  One related element (sibbling) could be link to current element using the @rel attribute.
  This related element is updated with the title value of the selected option.
  -->
  <xsl:template name="helper">
    <xsl:param name="schema"/>
    <xsl:param name="attribute"/>
    <xsl:param name="jsAction"/>
    <xsl:param name="isTextArea" required="no" select="false()"/>

    <!-- Define the element to look for.
         In dublin core element contains value.
         In ISO, attribute also but element contains characterString which contains the value -->

    <xsl:variable name="node" select="if ($attribute = true() or $schema = 'dublin-core')
                                      then .
                                      else parent::node()"></xsl:variable>
    <!-- Look for the helper -->
    <xsl:variable name="helper"
                  select="if (empty(/root/gui)) then '' else geonet:getHelper($schema, $node, /root/gui)"/>


    <!-- Display the helper list if found -->
    <xsl:if test="normalize-space($helper)!=''">

      <!-- Helper configuration -->
      <xsl:variable name="sortHelper" select="if ($helper/@sort='true') then true() else false()"/>
      <xsl:variable name="mode" select="$helper/@editorMode"/>
      <xsl:variable name="value" select="."/>


      <xsl:variable name="refId"
                    select="if ($attribute=true()) then concat(../geonet:element/@ref, '_', name(.)) else geonet:element/@ref"/>
      <xsl:variable name="relatedElementName" select="$helper/@rel"/>
      <xsl:variable name="relatedAttributeName" select="$helper/@relAtt"/>


      <xsl:variable name="relatedElementAction">
        <xsl:if test="$relatedElementName!=''">
          <xsl:variable name="relatedElement"
                        select="../following-sibling::node()[name()=$relatedElementName]/gco:CharacterString"/>
          <xsl:variable name="relatedElementRef"
                        select="../following-sibling::node()[name()=$relatedElementName]/gco:CharacterString/geonet:element/@ref"/>
          <xsl:variable name="relatedElementIsEmpty" select="normalize-space($relatedElement)=''"/>
          <!--<xsl:value-of select="concat('if (Ext.getDom(&quot;_', $relatedElementRef, '&quot;).value===&quot;&quot;) Ext.getDom(&quot;_', $relatedElementRef, '&quot;).value=this.options[this.selectedIndex].title;')"/>-->

          <xsl:choose>
            <!-- Layout with radio button -->
            <xsl:when test="contains($mode, 'radio')">
              <xsl:value-of
                select="concat('if (Ext.getDom(&quot;_', $relatedElementRef, '&quot;)) Ext.getDom(&quot;_', $relatedElementRef, '&quot;).value=this.title;')"
              />
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of
                select="concat('if (Ext.getDom(&quot;_', $relatedElementRef, '&quot;)) Ext.getDom(&quot;_', $relatedElementRef, '&quot;).value=this.options[this.selectedIndex].title;')"
              />
            </xsl:otherwise>
          </xsl:choose>
        </xsl:if>
      </xsl:variable>

      <xsl:variable name="relatedAttributeAction">
        <xsl:if test="$relatedAttributeName!=''">
          <xsl:variable name="relatedAttributeRef"
                        select="concat($refId, '_', $relatedAttributeName)"/>


          <xsl:choose>
            <!-- Layout with radio button -->
            <xsl:when test="contains($mode, 'radio')">
              <xsl:value-of
                select="concat('if (Ext.getDom(&quot;_', $relatedAttributeRef, '&quot;)) Ext.getDom(&quot;_', $relatedAttributeRef, '&quot;).value=this.title;')"
              />
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of
                select="concat('if (Ext.getDom(&quot;_', $relatedAttributeRef, '&quot;)) Ext.getDom(&quot;_', $relatedAttributeRef, '&quot;).value=this.options[this.selectedIndex].title;')"
              />
            </xsl:otherwise>
          </xsl:choose>
        </xsl:if>
      </xsl:variable>


      <div class="helper helper-{$mode}">
        <xsl:choose>
          <!-- Layout with radio button -->
          <xsl:when test="contains($mode, 'radio')">
            <xsl:for-each select="$helper/option">
              <div>
                <!-- Some helper may not contains values. That may be used to separate items.
                Don't put a radio in that case. -->
                <xsl:if test="@value">
                  <input class="md" type="radio" name="radio_{$refId}"
                         id="radio_{$refId}{position()}"
                         value="{@value}" title="{@title}"
                         onchange="Ext.getDom('_{$refId}').value=this.value; if (Ext.getDom('_{$refId}').onkeyup) Ext.getDom('_{$refId}').onkeyup();{$relatedElementAction} {$relatedAttributeAction} {$jsAction}">
                    <xsl:if test="@value=$value">
                      <xsl:attribute name="checked"/>
                    </xsl:if>
                  </input>
                </xsl:if>
                <label for="radio_{$refId}{position()}" title="{@title}">
                  <xsl:value-of select="text()"/>
                  <xsl:if test="$mode='radio_withdesc'">
                    <span>
                      <xsl:value-of select="@title"/>
                    </span>
                  </xsl:if>
                </label>
              </div>
            </xsl:for-each>

            <xsl:variable name="valueInHelper" select="count($helper/option[@value = $value]) = 1"/>

            <!-- Add a input for typing a custom value if not available in the helper list.
            When value change, the related field is updated and other radio is selected -->
            <div>
              <input class="md" type="radio" name="radio_{$refId}" id="otherradio_{$refId}" value=""
                     onchange="Ext.getDom('other_{$refId}').focus();Ext.getDom('_{$refId}').value=Ext.getDom('other_{$refId}').value;">
                <xsl:if test="not($valueInHelper)">
                  <xsl:attribute name="checked"/>
                </xsl:if>
              </input>
              <label for="otherradio_{$refId}">
                <xsl:value-of select="/root/gui/strings/otherValue"/>
              </label>

              <xsl:choose>
                <xsl:when test="$isTextArea">
                  <textarea id="other_{$refId}" type="text"
                            onfocus="Ext.getDom('otherradio_{$refId}').checked = true; Ext.getDom('_{$refId}').value=this.value;"
                            onkeyup="Ext.getDom('_{$refId}').value=this.value; if (Ext.getDom('_{$refId}').onkeyup) Ext.getDom('_{$refId}').onkeyup();"
                  >
                    <xsl:if test="not($valueInHelper)">
                      <xsl:value-of select="$value"/>
                    </xsl:if>
                  </textarea>
                </xsl:when>
                <xsl:otherwise>
                  <input id="other_{$refId}" type="text"
                         onfocus="Ext.getDom('otherradio_{$refId}').checked = true; Ext.getDom('_{$refId}').value=this.value;"
                         onkeyup="Ext.getDom('_{$refId}').value=this.value; if (Ext.getDom('_{$refId}').onkeyup) Ext.getDom('_{$refId}').onkeyup();"
                  >
                    <xsl:if test="not($valueInHelper)">
                      <xsl:attribute name="value" select="$value"/>
                    </xsl:if>
                  </input>
                </xsl:otherwise>
              </xsl:choose>

            </div>
          </xsl:when>
          <!-- Default layout, combo next to the element -->
          <xsl:otherwise>
            <xsl:text> </xsl:text> (<xsl:value-of select="/root/gui/strings/helperList"/>
            <select id="s_{$refId}" name="s_{$refId}" size="1"
                    onchange="Ext.getDom('_{$refId}').value=this.options[this.selectedIndex].value; if (Ext.getDom('_{$refId}').onkeyup) Ext.getDom('_{$refId}').onkeyup(); {$relatedElementAction} {$relatedAttributeAction} {$jsAction}"
                    class="md">
              <option/>
              <!-- This assume that helper list is already sort in alphabetical order in loc file. -->
              <xsl:choose>
                <xsl:when test="$sortHelper">
                  <xsl:for-each select="$helper/option">
                    <xsl:sort select="./text()"/>
                    <xsl:copy-of select="."/>
                  </xsl:for-each>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:copy-of select="$helper/*"/>
                </xsl:otherwise>
              </xsl:choose>
            </select>
            )
          </xsl:otherwise>
        </xsl:choose>
      </div>

    </xsl:if>
  </xsl:template>


  <!--
  prevent drawing of geonet:* elements
  -->
  <xsl:template mode="showXMLElement" match="geonet:*"/>
  <xsl:template mode="editXMLElement" match="geonet:*"/>


  <!-- ======================================= -->
  <!-- Layout -->

  <!--
    Template to create validation link popup on XSD errors
    or schematron errors.
  -->
  <xsl:template name="validationLink">
    <xsl:param name="ref"/>

    <xsl:if
      test="@geonet:xsderror
      or */@geonet:xsderror
      or //svrl:failed-assert[@ref=$ref]">
      <ul>
        <xsl:variable name="labels" select="/root/gui"/>

        <xsl:choose>
          <!-- xsd validation -->
          <xsl:when test="@geonet:xsderror">
            <xsl:choose>
              <xsl:when test="contains(@geonet:xsderror, '\n')">
                <xsl:variable name="root" select="/"/>
                <!-- DataManager#getXSDXmlReport concat errors in attribute -->
                <xsl:for-each select="tokenize(@geonet:xsderror, '\\n')">
                  <xsl:if test=". != ''">
                    <li>
                      <xsl:copy-of select="concat($root/root/gui/strings/xsdError, ': ',
                        geonet:parse-xsd-error(., $schema, $labels))"/>
                    </li>
                  </xsl:if>
                </xsl:for-each>
              </xsl:when>
              <xsl:otherwise>
                <li>
                  <xsl:copy-of select="concat(/root/gui/strings/xsdError, ': ',
                    geonet:parse-xsd-error(@geonet:xsderror, $schema, $labels))"/>
                </li>
              </xsl:otherwise>
            </xsl:choose>

          </xsl:when>
          <!-- some simple elements hide lower elements to remove some
                        complexity from the display (eg. gco: in iso19139)
                        so check if they have a schematron/xsderror and move it up
                        if they do -->
          <xsl:when test="*/@geonet:xsderror">
            <li>
              <xsl:copy-of select="concat(/root/gui/strings/xsdError, ': ',
                geonet:parse-xsd-error(*/@geonet:xsderror, $schema, $labels))"/>
            </li>
          </xsl:when>
          <!-- schematrons -->
          <xsl:when test="//svrl:failed-assert[@ref=$ref]">
            <xsl:for-each select="//svrl:failed-assert[@ref=$ref]">
              <li>
                <xsl:value-of select="preceding-sibling::svrl:active-pattern[1]/@name"/> :
                <xsl:copy-of select="svrl:text/*"/>
              </li>
            </xsl:for-each>
          </xsl:when>
        </xsl:choose>
      </ul>
    </xsl:if>
  </xsl:template>


  <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
  <!-- gui templates -->
  <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->


  <!-- Create a column layout
  -->
  <xsl:template name="columnElementGui">
    <xsl:param name="cols"/>
    <tr>
      <xsl:for-each select="$cols/col">
        <td class="col">
          <table class="gn">
            <tbody>
              <xsl:copy-of select="*"/>
            </tbody>
          </table>
        </td>
      </xsl:for-each>
    </tr>
  </xsl:template>


  <!--
    GUI to show a simple element in a table row with all
    attributes in a fieldset. Could be use in edit or view mode.
  -->
  <xsl:template name="simpleElementGui">
    <xsl:param name="title"/>
    <xsl:param name="text"/>
    <xsl:param name="helpLink"/>
    <xsl:param name="addLink"/>
    <xsl:param name="addXMLFragment"/>
    <xsl:param name="addXMLFragmentSubTemplate"/>
    <xsl:param name="removeLink"/>
    <xsl:param name="upLink"/>
    <xsl:param name="downLink"/>
    <xsl:param name="validationLink"/>
    <xsl:param name="schema"/>
    <xsl:param name="edit" select="false()"/>
    <xsl:param name="editAttributes" select="true()"/>
    <xsl:param name="showAttributes" select="true()"/>
    <xsl:param name="id" select="generate-id(.)"/>
    <xsl:param name="visible" select="true()"/>

    <!-- When element is a child of an element having an XLink, the
          element is in readonly mode except for gmx:Anchor.
    -->
    <xsl:variable name="isXLinked" select="count(ancestor-or-self::node()[@xlink:href]) > 0
                    and (name(.) != 'gmx:Anchor' and name(..) != 'gmx:Anchor')"/>
    <xsl:variable name="geonet" select="starts-with(name(.),'geonet:')"/>

    <tr id="{$id}">
      <xsl:if test="not($visible)">
        <xsl:attribute name="style">display:none;</xsl:attribute>
      </xsl:if>
      <xsl:attribute name="class">
        <!-- Add codelist value in CSS class -->
        <xsl:if test="*/@codeListValue and not($edit)">
          <xsl:value-of select="*/@codeListValue"/>
        </xsl:if>
      </xsl:attribute>
      <th id="stip.{$helpLink}">
        <xsl:attribute name="class">
          <xsl:text>main </xsl:text>
          <xsl:value-of select="geonet:clear-string-for-css(name(.))"/>
          <xsl:text> </xsl:text>
          <xsl:if test="$isXLinked">xlinked</xsl:if>
          <xsl:if test="geonet:element/@min='1' and $edit">mandatory</xsl:if>
        </xsl:attribute>
        <label
          for="_{if (gco:CharacterString) then gco:CharacterString/geonet:element/@ref else if (gmd:file) then '' else ''}">
          <xsl:choose>
            <xsl:when test="$helpLink!=''">
              <xsl:value-of select="$title"/>
              <span class="editor-help-inline">
                <xsl:attribute name="onclick">
                  <xsl:text>GeoNetwork.util.HelpTools.get(&quot;</xsl:text>
                  <xsl:value-of select="$helpLink"/>
                  <xsl:text>&quot;,&quot;</xsl:text>
                  <xsl:value-of select="$schema"/>
                  <xsl:text>&quot;,catalogue.services.schemaInfo, GeoNetwork.util.HelpTools.showtt)</xsl:text>
                </xsl:attribute>
                <img class="x-panel-inline-icon">
                  <xsl:attribute name="src">
                    <xsl:value-of select="/root/gui/url"/>
                    <xsl:text>/apps/images/default/help.png</xsl:text>
                  </xsl:attribute>
                </img>
              </span>
            </xsl:when>
            <xsl:otherwise>
              <xsl:call-template name="showTitleWithTag">
                <xsl:with-param name="title" select="$title"/>
              </xsl:call-template>
            </xsl:otherwise>
          </xsl:choose>
        </label>
        <xsl:text>&#160;</xsl:text>

        <!-- srv:operatesOn is an element which contains xlink:href attribute
          (due to INSPIRE usage added in r7710) and must be editable in any cases (#705).
          The xLink for this element is used for linking to a full
          XML metadata records and is part of the Jeeves XLink resolver exception (jeeves.xlink.Processor#doXLink).
        -->
        <xsl:if test="$edit and (not($isXLinked) or name(.)='srv:operatesOn')">
          <xsl:call-template name="getButtons">
            <xsl:with-param name="addLink" select="$addLink"/>
            <xsl:with-param name="addXMLFragment" select="$addXMLFragment"/>
            <xsl:with-param name="addXmlFragmentSubTemplate" select="$addXMLFragmentSubTemplate"/>
            <xsl:with-param name="removeLink" select="$removeLink"/>
            <xsl:with-param name="upLink" select="$upLink"/>
            <xsl:with-param name="downLink" select="$downLink"/>
            <xsl:with-param name="validationLink" select="$validationLink"/>
            <xsl:with-param name="id" select="$id"/>
          </xsl:call-template>
        </xsl:if>
      </th>
      <td>

        <xsl:variable name="textnode" select="exslt:node-set($text)"/>
        <xsl:choose>
          <xsl:when test="$edit">
            <xsl:copy-of select="$text"/>
          </xsl:when>
          <xsl:when test="count($textnode/*) &gt; 0">
            <!-- In some templates, text already contains HTML (eg. codelist, link for download).
              In that case copy text content and does not resolve
              hyperlinks. -->
            <xsl:copy-of select="$text"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:call-template name="addLineBreaksAndHyperlinks">
              <xsl:with-param name="txt" select="$text"/>
            </xsl:call-template>
          </xsl:otherwise>
        </xsl:choose>
        <!-- Display attributes for :
          * non codelist element
          * empty field with nilReason attributes
        -->
        <xsl:choose>
          <xsl:when
            test="$edit and $editAttributes
            and count(geonet:attribute)&gt;0
            and count(*/geonet:attribute[@name='codeList'])=0
            ">
            <!-- Display attributes if used and not only contains a gco:nilReason = missing. -->
            <xsl:variable name="visibleAttributes"
                          select="count(@*[name(.)!='nilReason' and  normalize-space()!='missing']) > 0"/>
            <div class="attr">
              <div title="{/root/gui/strings/editAttributes}"
                   onclick="toggleFieldset(this, Ext.getDom('toggled{$id}'));"
                   style="display: none;">
                <xsl:attribute name="class">
                  <xsl:choose>
                    <xsl:when test="$visibleAttributes">toggle-attr tgDown button</xsl:when>
                    <xsl:otherwise>toggle-attr tgRight button</xsl:otherwise>
                  </xsl:choose>
                </xsl:attribute>
              </div>
              <table id="toggled{$id}">
                <xsl:attribute name="style">
                  <xsl:if test="not($visibleAttributes)">display:none;</xsl:if>
                </xsl:attribute>
                <tbody>
                  <xsl:apply-templates mode="simpleAttribute" select="@*|geonet:attribute">
                    <xsl:with-param name="schema" select="$schema"/>
                    <xsl:with-param name="edit" select="$edit"/>
                  </xsl:apply-templates>
                </tbody>
              </table>
            </div>
          </xsl:when>
          <xsl:when test="not($edit) and @* and $showAttributes">
            <xsl:apply-templates mode="simpleAttribute" select="@*">
              <xsl:with-param name="schema" select="$schema"/>
              <xsl:with-param name="edit" select="$edit"/>
            </xsl:apply-templates>
          </xsl:when>
        </xsl:choose>
      </td>
    </tr>
  </xsl:template>

  <!-- GUI to create simple element in a table row with title and content only.
    Usually not used in edit mode.
  -->
  <xsl:template name="simpleElementSimpleGUI">
    <xsl:param name="title"/>
    <xsl:param name="helpLink"/>
    <xsl:param name="content"/>

    <xsl:variable name="hiddenChildren">
      <xsl:call-template name="hasHiddenChildren"/>
    </xsl:variable>

    <!-- don't show it if there isn't anything in it! -->
    <xsl:choose>
      <xsl:when test="$hiddenChildren = true()">
        <xsl:call-template name="hiddenElement">
          <xsl:with-param name="title" select="$title"/>
          <xsl:with-param name="helpLink" select="$helpLink"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <tr>
          <th class="main" id="stip.{$helpLink}|{generate-id()}">
            <xsl:value-of select="$title"/>
          </th>
          <td>
            <xsl:copy-of select="$content"/>
          </td>
        </tr>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>


  <!-- Display coordinates with 2 fields:
    * one to store the value but hidden (always in WGS84 as defined in ISO).
    This element is post via the form.
    * one to display the coordinate in user defined projection.
  -->
  <xsl:template mode="coordinateElementGUI" match="*">
    <xsl:param name="schema"/>
    <xsl:param name="edit"/>
    <xsl:param name="name"/>
    <xsl:param name="eltRef"/>
    <xsl:param name="tabIndex"/>

    <xsl:variable name="title">
      <xsl:call-template name="getTitle">
        <xsl:with-param name="schema" select="$schema"/>
        <xsl:with-param name="name" select="$name"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="helpLink">
      <xsl:call-template name="getHelpLink">
        <xsl:with-param name="schema" select="$schema"/>
        <xsl:with-param name="name" select="$name"/>
      </xsl:call-template>
    </xsl:variable>
    <b>
      <xsl:choose>
        <xsl:when test="$helpLink!=''">
          <span id="tip.{$helpLink}" style="cursor:help;">
            <xsl:value-of select="$title"/>
          </span>
        </xsl:when>
        <xsl:otherwise>
          <xsl:call-template name="showTitleWithTag">
            <xsl:with-param name="title" select="$title"/>
          </xsl:call-template>
        </xsl:otherwise>
      </xsl:choose>
    </b>
    <br/>
    <xsl:variable name="size" select="'8'"/>

    <xsl:choose>
      <!-- Hidden text field is use to store WGS84 values which are stored in metadata records. -->
      <xsl:when test="$edit=true()">
        <xsl:call-template name="getElementText">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="edit" select="$edit"/>
          <xsl:with-param name="input_type" select="'number'"/>
          <xsl:with-param name="input_step" select="'0.00001'"/>
          <xsl:with-param name="validator" select="'validateNumber(this, false)'"/>
          <xsl:with-param name="no_name" select="true()"/>
          <xsl:with-param name="tabindex" select="$tabIndex"/>
        </xsl:call-template>
        <xsl:call-template name="getElementText">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="edit" select="true()"/>
          <xsl:with-param name="input_type" select="'hidden'"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <input class="md" type="number" id="{$eltRef}" value="{text()}" readonly="readonly"
               size="{$size}"/>
        <input class="md" type="hidden" id="_{$eltRef}" name="_{$eltRef}" value="{text()}"
               readonly="readonly"/>
      </xsl:otherwise>
    </xsl:choose>

  </xsl:template>


  <!-- Display the extent widget composed of
    * 4 input text fields with bounds coordinates
    * a coordinate system switcher. Coordinates are stored in WGS84 but could be displayed
    or editied in antother projection.
  -->
  <xsl:template name="geoBoxGUI">
    <xsl:param name="schema"/>
    <xsl:param name="edit"/>
    <xsl:param name="nEl"/>
    <xsl:param name="nId"/>
    <xsl:param name="nValue"/>
    <xsl:param name="sEl"/>
    <xsl:param name="sId"/>
    <xsl:param name="sValue"/>
    <xsl:param name="wEl"/>
    <xsl:param name="wId"/>
    <xsl:param name="wValue"/>
    <xsl:param name="eEl"/>
    <xsl:param name="eId"/>
    <xsl:param name="eValue"/>
    <xsl:param name="descId"/>
    <xsl:param name="id"/>


    <xsl:variable name="eltRef">
      <xsl:choose>
        <xsl:when test="$edit=true()">
          <xsl:value-of select="$id"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="generate-id(.)"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>


    <table class="map">
      <tbody>
        <tr>
          <td colspan="3">
            <!-- Loop on all projections defined in config-gui.xml -->
            <xsl:for-each select="/root/gui/config/map/proj/crs">
              <!-- Set label from loc file -->
              <label for="{@code}_{$eltRef}">
                <xsl:variable name="code" select="@code"/>
                <xsl:choose>
                  <xsl:when test="/root/gui/strings/*[@code=$code]">
                    <xsl:value-of select="/root/gui/strings/*[@code=$code]"/>
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:value-of select="@code"/>
                  </xsl:otherwise>
                </xsl:choose>
                <input id="{@code}_{$eltRef}" class="proj" type="radio" name="proj_{$eltRef}"
                       value="{@code}">
                  <xsl:if test="@default='1'">
                    <xsl:attribute name="checked">checked</xsl:attribute>
                  </xsl:if>
                </input>
              </label>
            </xsl:for-each>

          </td>
        </tr>
        <xsl:if test="$nEl">
          <tr>
            <td colspan="3">
              <xsl:apply-templates mode="coordinateElementGUI" select="$nEl/gco:Decimal">
                <xsl:with-param name="schema" select="$schema"/>
                <xsl:with-param name="edit" select="$edit"/>
                <xsl:with-param name="name" select="name($nEl)"/>
                <xsl:with-param name="eltRef" select="concat('n', $eltRef)"/>
                <xsl:with-param name="tabIndex" select="100"/>
              </xsl:apply-templates>
            </td>
          </tr>
        </xsl:if>
        <tr>
          <xsl:if test="$wEl">
            <td>
              <xsl:apply-templates mode="coordinateElementGUI" select="$wEl/gco:Decimal">
                <xsl:with-param name="schema" select="$schema"/>
                <xsl:with-param name="edit" select="$edit"/>
                <xsl:with-param name="name" select="name($wEl)"/>
                <xsl:with-param name="eltRef" select="concat('w', $eltRef)"/>
                <xsl:with-param name="tabIndex" select="101"/>
              </xsl:apply-templates>
            </td>
          </xsl:if>
          <td>
            <xsl:variable name="wID">
              <xsl:choose>
                <xsl:when test="$edit=true()">
                  <xsl:value-of select="$wId"/>
                </xsl:when>
                <xsl:otherwise>w<xsl:value-of select="$eltRef"/>
                </xsl:otherwise>
              </xsl:choose>
            </xsl:variable>

            <xsl:variable name="eID">
              <xsl:choose>
                <xsl:when test="$edit=true()">
                  <xsl:value-of select="$eId"/>
                </xsl:when>
                <xsl:otherwise>e<xsl:value-of select="$eltRef"/>
                </xsl:otherwise>
              </xsl:choose>
            </xsl:variable>

            <xsl:variable name="nID">
              <xsl:choose>
                <xsl:when test="$edit=true()">
                  <xsl:value-of select="$nId"/>
                </xsl:when>
                <xsl:otherwise>n<xsl:value-of select="$eltRef"/>
                </xsl:otherwise>
              </xsl:choose>
            </xsl:variable>

            <xsl:variable name="sID">
              <xsl:choose>
                <xsl:when test="$edit=true()">
                  <xsl:value-of select="$sId"/>
                </xsl:when>
                <xsl:otherwise>s<xsl:value-of select="$eltRef"/>
                </xsl:otherwise>
              </xsl:choose>
            </xsl:variable>


            <xsl:variable name="geom">
              <xsl:value-of
                select="concat('Polygon((', $wValue, ' ', $sValue,',',$eValue,' ',$sValue,',',$eValue,' ',$nValue,',',$wValue,' ',$nValue,',',$wValue,' ',$sValue, '))')"
              />
            </xsl:variable>
            <xsl:call-template name="showMap">
              <xsl:with-param name="edit" select="$edit"/>
              <xsl:with-param name="mode" select="'bbox'"/>
              <xsl:with-param name="coords" select="$geom"/>
              <xsl:with-param name="watchedBbox"
                              select="concat($wID, ',', $sID, ',', $eID, ',', $nID)"/>
              <xsl:with-param name="eltRef" select="$eltRef"/>
              <xsl:with-param name="descRef" select="$descId"/>
            </xsl:call-template>
          </td>
          <xsl:if test="$eEl">
            <td>
              <xsl:apply-templates mode="coordinateElementGUI" select="$eEl/gco:Decimal">
                <xsl:with-param name="schema" select="$schema"/>
                <xsl:with-param name="edit" select="$edit"/>
                <xsl:with-param name="name" select="name($eEl)"/>
                <xsl:with-param name="eltRef" select="concat('e', $eltRef)"/>
                <xsl:with-param name="tabIndex" select="103"/>
              </xsl:apply-templates>
            </td>
          </xsl:if>
        </tr>
        <xsl:if test="$sEl">
          <tr>
            <td colspan="3">
              <xsl:apply-templates mode="coordinateElementGUI" select="$sEl/gco:Decimal">
                <xsl:with-param name="schema" select="$schema"/>
                <xsl:with-param name="edit" select="$edit"/>
                <xsl:with-param name="name" select="name($sEl)"/>
                <xsl:with-param name="eltRef" select="concat('s', $eltRef)"/>
                <xsl:with-param name="tabIndex" select="102"/>
              </xsl:apply-templates>
            </td>
          </tr>
        </xsl:if>
      </tbody>
    </table>
  </xsl:template>


  <!--
    gui to show a title and do special mapping for container elements
  -->
  <xsl:template name="showTitleWithTag">
    <xsl:param name="title"/>
    <xsl:param name="class"/>
    <xsl:variable name="shortTitle" select="normalize-space($title)"/>
    <xsl:variable name="conthelp"
                  select="concat('This is a container element name - you can give it a title and help by entering some help for ',$shortTitle,' in the help file')"/>
    <xsl:variable name="nohelp"
                  select="concat('This is an element/attribute name - you can give it a title and help by entering some help for ',$shortTitle,' in the help file')"/>

    <xsl:choose>
      <xsl:when test="contains($title,'CHOICE_ELEMENT')">
        <span class="{$class}" title="{$conthelp}">
          <xsl:value-of select="/root/gui/strings/choice"/>
        </span>
      </xsl:when>
      <xsl:when test="contains($title,'GROUP_ELEMENT')">
        <span class="{$class}" title="{$conthelp}">
          <xsl:value-of select="/root/gui/strings/group"/>
        </span>
      </xsl:when>
      <xsl:when test="contains($title,'SEQUENCE_ELEMENT')">
        <span class="{$class}" title="{$conthelp}">
          <xsl:value-of select="/root/gui/strings/sequence"/>
        </span>
      </xsl:when>
      <xsl:otherwise>
        <span class="{$class}" title="{$nohelp}">
          <xsl:value-of select="$title"/>
        </span>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <!--
    gui to show a complex element
  -->
  <xsl:template name="complexElementGui">
    <xsl:param name="title"/>
    <xsl:param name="text"/>
    <xsl:param name="content"/>
    <xsl:param name="helpLink"/>
    <xsl:param name="addLink"/>
    <xsl:param name="addXMLFragment"/>
    <xsl:param name="addXmlFragmentSubTemplate"/>
    <xsl:param name="removeLink"/>
    <xsl:param name="upLink"/>
    <xsl:param name="downLink"/>
    <xsl:param name="validationLink"/>
    <xsl:param name="schema"/>
    <xsl:param name="edit" select="false()"/>
    <xsl:param name="id" select="generate-id(.)"/>

    <xsl:variable name="isXLinked" select="count(ancestor::node()[@xlink:href]) > 0"/>
    <tr id="{$id}">
      <td colspan="2" class="complex">
        <fieldset>
          <legend id="stip.{$helpLink}|{$id}">

            <span class="toggle">
              <xsl:if test="/root/gui/config/metadata-view-toggleTab">
                <xsl:attribute name="onclick">toggleFieldset(Ext.getDom('toggled-bt-<xsl:value-of
                  select="$id"/>'), Ext.getDom('toggled<xsl:value-of select="$id"/>'));
                </xsl:attribute>
                <div class="button tgDown" id="toggled-bt-{$id}">&#160;</div>
              </xsl:if>

              <xsl:choose>
                <xsl:when test="$helpLink!=''">
                  <xsl:value-of select="$title"/>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:call-template name="showTitleWithTag">
                    <xsl:with-param name="title" select="$title"/>
                  </xsl:call-template>
                </xsl:otherwise>
              </xsl:choose>

            </span>
            <xsl:if test="$edit and not($isXLinked)">
              <xsl:call-template name="getButtons">
                <xsl:with-param name="addLink" select="$addLink"/>
                <xsl:with-param name="addXMLFragment" select="$addXMLFragment"/>
                <xsl:with-param name="addXmlFragmentSubTemplate"
                                select="$addXmlFragmentSubTemplate"/>
                <xsl:with-param name="removeLink" select="$removeLink"/>
                <xsl:with-param name="upLink" select="$upLink"/>
                <xsl:with-param name="downLink" select="$downLink"/>
                <xsl:with-param name="validationLink" select="$validationLink"/>
                <xsl:with-param name="id" select="$id"/>
              </xsl:call-template>
            </xsl:if>
          </legend>
          <!-- Check if divs could be used instead ? -->
          <table class="gn" id="toggled{$id}">
            <tbody>
              <xsl:if test="count(geonet:attribute[@name='gco:nilReason']) > 0">

                <!-- Display attributes if used and not only contains a gco:nilReason = missing.
                Only support gco:nilReason attribute for complex element.
                -->
                <xsl:variable name="visibleAttributes"
                              select="count(@*[name(.)!='nilReason' and  normalize-space()!='missing']) > 0"/>
                <tr>
                  <td>
                    <div class="toggle-attr">
                      <xsl:attribute name="style">
                        <xsl:if test="not($visibleAttributes)">display:none;</xsl:if>
                      </xsl:attribute>
                      <table>
                        <tbody>
                          <xsl:apply-templates mode="simpleAttribute"
                                               select="@gco:nilReason|geonet:attribute[@name='gco:nilReason']">
                            <xsl:with-param name="schema" select="$schema"/>
                            <xsl:with-param name="edit" select="$edit"/>
                          </xsl:apply-templates>
                        </tbody>
                      </table>
                    </div>
                  </td>
                </tr>
              </xsl:if>

              <xsl:copy-of select="$content"/>
            </tbody>
          </table>
        </fieldset>
      </td>
    </tr>
  </xsl:template>

  <xsl:template name="complexElementSimpleGui">
    <xsl:param name="title"/>
    <xsl:param name="content"/>
    <xsl:variable name="hiddenChildren">
      <xsl:call-template name="hasHiddenChildren"/>
    </xsl:variable>

    <!-- don't show it if there isn't anything in it! -->
    <xsl:choose>
      <xsl:when test="$hiddenChildren = true()">
        <xsl:call-template name="hiddenElement">
          <xsl:with-param name="title" select="$title"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <fieldset>
          <legend>
            <xsl:value-of select="$title"/>
          </legend>
          <table class="gn">
            <xsl:copy-of select="$content"/>
          </table>
        </fieldset>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!--
    returns the text of an element
  -->
  <xsl:template name="getElementText">
    <xsl:param name="schema"/>
    <xsl:param name="edit" select="false()"/>
    <!-- Define the class to apply to textarea element, use
    to create a textarea -->
    <xsl:param name="class"/>
    <xsl:param name="langId"/>
    <xsl:param name="visible" select="true()"/>
    <!-- Add javascript validator function. By default, if element
      is mandatory a non empty validator is defined. -->
    <xsl:param name="validator"/>
    <!-- Use input_type parameter to create an hidden field.
      Default is a text input. -->
    <xsl:param name="input_type">text</xsl:param>
    <!--
      See http://www.w3.org/TR/html-markup/input.number.html
    -->
    <xsl:param name="input_step"></xsl:param>
    <!-- Set to true no_name parameter in order to create an element
      which will not be submitted to the form. -->
    <xsl:param name="no_name" select="false()"/>
    <xsl:param name="tabindex"/>

    <xsl:variable name="edit" select="xs:boolean($edit)"/>
    <xsl:variable name="name" select="name(.)"/>
    <xsl:variable name="value" select="string(.)"/>
    <xsl:variable name="isXLinked" select="count(ancestor-or-self::node()[@xlink:href]) > 0
                                            and $name != 'gmx:Anchor'"/>
    <xsl:choose>
      <!-- list of values -->
      <xsl:when test="geonet:element/geonet:text">

        <xsl:variable name="mandatory"
                      select="geonet:element/@min='1' and
          geonet:element/@max='1'"/>

        <!-- This code is mainly run under FGDC
          but also for enumeration like topic category and
          service parameter direction in ISO.

          Create a temporary list and retrive label in
          current gui language which is sorted after. -->
        <xsl:variable name="list">
          <items>
            <xsl:for-each select="geonet:element/geonet:text">
              <xsl:variable name="choiceValue" select="string(@value)"/>
              <xsl:variable name="schemaLabel"
                            select="/root/gui/schemas/*[name(.)=$schema]/codelists/codelist[@name = $name]/entry[code = $choiceValue]/label"/>

              <xsl:variable name="label">
                <xsl:choose>
                  <xsl:when
                    test="normalize-space($schemaLabel) = '' and starts-with($schema, 'iso19139.')">
                    <!-- Check iso19139 label -->
                    <xsl:value-of
                      select="/root/gui/schemas/*[name(.)='iso19139']/codelists/codelist[@name = $name]/entry[code = $choiceValue]/label"/>
                  </xsl:when>
                  <xsl:when test="$schemaLabel">
                    <xsl:value-of select="$schemaLabel"/>
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:value-of select="$choiceValue"/>
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:variable>

              <item>
                <value>
                  <xsl:value-of select="@value"/>
                </value>
                <label>
                  <xsl:value-of select="$label"/>
                </label>
              </item>
            </xsl:for-each>
          </items>
        </xsl:variable>
        <select class="md" name="_{geonet:element/@ref}" size="1">
          <xsl:if test="$visible = false()">
            <xsl:attribute name="style">display:none;</xsl:attribute>
          </xsl:if>
          <xsl:if test="$isXLinked">
            <xsl:attribute name="disabled">disabled</xsl:attribute>
          </xsl:if>
          <xsl:if test="$mandatory and $edit">
            <xsl:attribute name="onchange">validateNonEmpty(this);</xsl:attribute>
          </xsl:if>
          <option name=""/>
          <xsl:for-each select="exslt:node-set($list)//item">
            <xsl:sort select="label"/>
            <option>
              <xsl:if test="value=$value">
                <xsl:attribute name="selected"/>
              </xsl:if>
              <xsl:attribute name="value">
                <xsl:value-of select="value"/>
              </xsl:attribute>
              <xsl:value-of select="label"/>
            </option>
          </xsl:for-each>
        </select>
      </xsl:when>
      <xsl:when test="$edit=true() and $class=''">
        <xsl:choose>
          <!-- heikki doeleman: for gco:Boolean, use checkbox.
            Default value set to false. -->
          <xsl:when test="name(.)='gco:Boolean'">
            <input type="hidden" name="_{geonet:element/@ref}" id="_{geonet:element/@ref}"
                   value="{.}">
              <xsl:if test="$isXLinked">
                <xsl:attribute name="disabled">disabled</xsl:attribute>
              </xsl:if>
              <xsl:choose>
                <xsl:when test=". = ''">
                  <xsl:attribute name="value">false</xsl:attribute>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:attribute name="value">
                    <xsl:value-of select="."/>
                  </xsl:attribute>
                </xsl:otherwise>
              </xsl:choose>
            </input>

            <xsl:choose>
              <xsl:when test="text()='true' or text()='1'">
                <input class="md" type="checkbox" id="_{geonet:element/@ref}_checkbox"
                       onclick="handleCheckboxAsBoolean(this, '_{geonet:element/@ref}');"
                       checked="checked">
                  <xsl:if test="$isXLinked">
                    <xsl:attribute name="disabled">disabled</xsl:attribute>
                  </xsl:if>
                </input>
              </xsl:when>
              <xsl:otherwise>
                <input class="md" type="checkbox" id="_{geonet:element/@ref}_checkbox"
                       onclick="handleCheckboxAsBoolean(this, '_{geonet:element/@ref}');">
                  <xsl:if test="$isXLinked">
                    <xsl:attribute name="disabled">disabled</xsl:attribute>
                  </xsl:if>
                </input>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:when>

          <xsl:otherwise>
            <!-- Look for the helper to check if a radio edit mode is activated
            If yes, hide the input text which will be updated when clicking the radio
            or the other option. -->
            <xsl:variable name="helper"
                          select="if (empty(/root/gui)) then '' else geonet:getHelper($schema, parent::node(), /root/gui)"/>


            <input class="md {$class}" type="{$input_type}" value="{text()}">
              <xsl:if test="$isXLinked">
                <xsl:attribute name="disabled">disabled</xsl:attribute>
              </xsl:if>
              <xsl:if test="$input_step">
                <xsl:attribute name="step">
                  <xsl:value-of select="$input_step"/>
                </xsl:attribute>
              </xsl:if>
              <xsl:if test="$tabindex">
                <xsl:attribute name="tabindex" select="$tabindex"/>
              </xsl:if>
              <xsl:if test="if ($helper) then contains($helper/@editorMode, 'radio') else false()">
                <xsl:attribute name="style">display:none;</xsl:attribute>
              </xsl:if>
              <xsl:choose>
                <xsl:when test="$no_name=false()">
                  <xsl:attribute name="name">_<xsl:value-of select="geonet:element/@ref"
                  />
                  </xsl:attribute>
                  <xsl:attribute name="id">_<xsl:value-of select="geonet:element/@ref"
                  />
                  </xsl:attribute>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:attribute name="id">
                    <xsl:value-of select="geonet:element/@ref"/>
                  </xsl:attribute>
                </xsl:otherwise>
              </xsl:choose>

              <xsl:if test="$visible = false()">
                <xsl:attribute name="style">display:none;</xsl:attribute>
              </xsl:if>

              <xsl:variable name="mandatory"
                            select="(name(.)='gmd:LocalisedCharacterString'
                and ../../geonet:element/@min='1')
                or ../geonet:element/@min='1'"/>

              <xsl:choose>
                <!-- Numeric field -->
                <xsl:when
                  test="name(.)='gco:Integer' or
                  name(.)='gco:Decimal' or name(.)='gco:Real'">
                  <xsl:choose>
                    <xsl:when test="name(.)='gco:Integer'">
                      <xsl:attribute name="onkeyup">validateNumber(this, <xsl:value-of
                        select="not($mandatory)"/>, false);
                      </xsl:attribute>
                    </xsl:when>
                    <xsl:otherwise>
                      <xsl:attribute name="onkeyup">validateNumber(this, <xsl:value-of
                        select="not($mandatory)"/>, true);
                      </xsl:attribute>
                    </xsl:otherwise>
                  </xsl:choose>
                </xsl:when>
                <!-- Mandatory field (with extra validator) -->
                <xsl:when test="$mandatory
                  and $edit">
                  <xsl:attribute name="onkeyup">validateNonEmpty(this);</xsl:attribute>
                </xsl:when>
                <!-- Custom validator -->
                <xsl:when test="$validator">
                  <xsl:attribute name="onkeyup">
                    <xsl:value-of select="$validator"/>
                  </xsl:attribute>
                </xsl:when>
              </xsl:choose>
            </input>
            <xsl:call-template name="helper">
              <xsl:with-param name="schema" select="$schema"/>
              <xsl:with-param name="attribute" select="false()"/>
            </xsl:call-template>

          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:when test="$edit=true()">
        <xsl:variable name="helper"
                      select="if (empty(/root/gui)) then '' else geonet:getHelper($schema, parent::node(), /root/gui)"/>

        <textarea class="md {$class}" name="_{geonet:element/@ref}" id="_{geonet:element/@ref}">
          <xsl:if test="$isXLinked">
            <xsl:attribute name="disabled">disabled</xsl:attribute>
          </xsl:if>
          <xsl:if test="$visible = false()">
            <xsl:attribute name="style">display:none;</xsl:attribute>
          </xsl:if>
          <xsl:if test="if ($helper) then contains($helper/@editorMode, 'radio') else false()">
            <xsl:attribute name="style">display:none;</xsl:attribute>
          </xsl:if>
          <xsl:if
            test="(
            (name(.)='gmd:LocalisedCharacterString' and ../../geonet:element/@min='1')
            or ../geonet:element/@min='1'
            ) and $edit">
            <xsl:attribute name="onkeyup">validateNonEmpty(this);</xsl:attribute>
          </xsl:if>
          <xsl:value-of select="text()"/>
        </textarea>
        <xsl:call-template name="helper">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="attribute" select="false()"/>
          <xsl:with-param name="isTextArea" select="true()"/>
        </xsl:call-template>

      </xsl:when>
      <xsl:when test="$edit=false() and $class!=''">
        <!-- CHECKME -->
        <xsl:choose>
          <xsl:when test="starts-with($schema,'iso19139')">
            <xsl:apply-templates mode="localised" select="..">
              <xsl:with-param name="langId" select="$langId"/>
            </xsl:apply-templates>
          </xsl:when>
          <xsl:otherwise>
            <xsl:copy-of select="$value"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:otherwise>
        <!-- not editable text/codelists -->
        <xsl:variable name="label"
                      select="/root/gui/schemas/*[name(.)=$schema]/codelists/codelist[@name = $name]/entry[code=$value]/label"/>
        <xsl:choose>
          <xsl:when test="$label">
            <xsl:value-of select="$label"/>
          </xsl:when>
          <xsl:when
            test="starts-with($schema,'iso19139') and (../gco:CharacterString or ../gmd:PT_FreeText)">
            <xsl:apply-templates mode="localised" select="..">
              <xsl:with-param name="langId" select="$langId"/>
            </xsl:apply-templates>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="$value"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <!--
  returns the text of an attribute
  -->
  <xsl:template name="getAttributeText">
    <xsl:param name="schema"/>
    <xsl:param name="edit" select="false()"/>
    <xsl:param name="class" select="''"/>

    <xsl:variable name="name">
      <xsl:choose>
        <xsl:when test="@name">
          <xsl:value-of select="@name"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="name(.)"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="value" select="string(.)"/>
    <xsl:variable name="parent" select="name(..)"/>
    <!-- the following variable is used in place of name as a work-around to
         deal with qualified attribute names like gml:id
         which if not modified will cause JDOM errors on update because of the
         way in which changes to ref'd elements are parsed as XML -->
    <xsl:variable name="updatename">
      <xsl:call-template name="getAttributeName">
        <xsl:with-param name="name" select="$name"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="isXLinked" select="count(ancestor-or-self::node()[@xlink:href]) > 0"/>

    <xsl:choose>
      <!-- list of values for existing attribute or non existing ones -->
      <xsl:when test="../geonet:attribute[string(@name)=$name]/geonet:text|geonet:text">
        <select class="md" name="_{../geonet:element/@ref}_{$updatename}" size="1">
          <xsl:if test="$isXLinked">
            <xsl:attribute name="disabled">disabled</xsl:attribute>
          </xsl:if>
          <option name=""/>
          <xsl:for-each select="../geonet:attribute/geonet:text">
            <option>
              <xsl:if test="@value=$value">
                <xsl:attribute name="selected"/>
              </xsl:if>
              <xsl:variable name="choiceValue" select="string(@value)"/>
              <xsl:attribute name="value">
                <xsl:value-of select="$choiceValue"/>
              </xsl:attribute>

              <!-- codelist in edit mode -->
              <xsl:variable name="label"
                            select="/root/gui/schemas/*[name(.)=$schema]/codelists/codelist[@name = $name]/entry[code=$choiceValue]/label"/>
              <xsl:choose>
                <xsl:when test="$label">
                  <xsl:value-of select="$label"/>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:value-of select="$choiceValue"/>
                </xsl:otherwise>
              </xsl:choose>
            </option>
          </xsl:for-each>
        </select>
      </xsl:when>
      <xsl:when test="$edit=true() and $class=''">
        <input class="md {$class}" type="text" id="_{../geonet:element/@ref}_{$updatename}"
               name="_{../geonet:element/@ref}_{$updatename}" value="{string()}"/>

        <xsl:call-template name="helper">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="attribute" select="true()"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="$edit=true()">
        <textarea class="md {$class}" name="_{../geonet:element/@ref}_{$updatename}"
                  id="_{../geonet:element/@ref}_{$updatename}">
          <xsl:value-of select="string()"/>
        </textarea>
      </xsl:when>
      <xsl:otherwise>
        <!-- codelist in view mode -->
        <xsl:variable name="label"
                      select="/root/gui/schemas/*[name(.)=$schema]//codelists/codelist[@name = $name]/entry[code = $value]/label"/>
        <xsl:choose>
          <xsl:when test="$label">
            <xsl:value-of select="$label"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="$value"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>


  <!-- Create a div with class name set to extentViewer in
    order to generate a new map. -->
  <xsl:template name="showMap">
    <xsl:param name="edit"/>
    <xsl:param name="coords"/>
    <!-- Indicate which drawing mode is used (ie. bbox or polygon) -->
    <xsl:param name="mode"/>
    <xsl:param name="targetPolygon"/>
    <xsl:param name="watchedBbox"/>
    <xsl:param name="eltRef"/>
    <xsl:param name="descRef"/>
    <div class="extentViewer"
         style="width:{/root/gui/config/map/metadata/width}; height:{/root/gui/config/map/metadata/height};"
         edit="{$edit}" target_polygon="{$targetPolygon}" watched_bbox="{$watchedBbox}"
         elt_ref="{$eltRef}" desc_ref="{$descRef}" mode="{$mode}">
      <div style="display:none;" id="coords_{$eltRef}">
        <xsl:value-of select="$coords"/>
      </div>
    </div>
  </xsl:template>

  <xsl:template name="hiddenElement">
    <xsl:param name="schema"/>
    <xsl:param name="title"/>
    <xsl:param name="helpLink"/>
    <xsl:call-template name="simpleElementGui">
      <xsl:with-param name="title" select="$title"/>
      <xsl:with-param name="schema" select="$schema"/>
      <xsl:with-param name="removeLink" select="false()"/>
      <xsl:with-param name="upLink" select="false()"/>
      <xsl:with-param name="downLink" select="false()"/>
      <xsl:with-param name="showAttributes" select="false()"/>
      <xsl:with-param name="text">&#160;
        <img class="helplink" id="{generate-id()}{name(.)}|hidden-elements"
             src="{/root/gui/url}/images/important.png"/>
      </xsl:with-param>
      <xsl:with-param name="helpLink" select="$helpLink"/>
    </xsl:call-template>
  </xsl:template>
</xsl:stylesheet>
