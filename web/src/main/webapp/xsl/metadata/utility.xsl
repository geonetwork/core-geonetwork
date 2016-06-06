<!-- WARNING Do not remove those namespaces as
     saxon:evaluate needs them for matching -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:exslt="http://exslt.org/common"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:gmx="http://www.isotc211.org/2005/gmx"
                xmlns:srv="http://www.isotc211.org/2005/srv"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:geonet="http://www.fao.org/geonetwork"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                xmlns:date="http://exslt.org/dates-and-times"
                xmlns:saxon="http://saxon.sf.net/"
                extension-element-prefixes="saxon"
                exclude-result-prefixes="#all">

  <!-- ================================================================================ -->
  <!--
    returns the help url
    -->
  <xsl:template name="getHelpLink">
    <xsl:param name="name"/>
    <xsl:param name="schema"/>

    <xsl:choose>
      <xsl:when test="contains($name,'_ELEMENT')">
        <xsl:value-of select="''"/>
      </xsl:when>
      <xsl:otherwise>

        <xsl:variable name="fullContext">
          <xsl:call-template name="getXPath"/>
        </xsl:variable>

        <xsl:value-of
          select="concat($schema,'|', $name ,'|', name(parent::node()) ,'|', $fullContext ,'|', ../@gco:isoType)"
        />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>


  <!-- Get the suggestion for an element -->
  <xsl:function name="geonet:getHelper" as="node()">
    <xsl:param name="schema" as="xs:string"/>
    <xsl:param name="node" as="node()"/>
    <xsl:param name="labels" as="node()"/>

    <!-- Define the element to look for.
         In dublin core element contains value.
         In ISO, attribute also but element contains characterString which contains the value -->
    <xsl:variable name="name" select="name($node)"/>

    <!-- The parent of the parent, may be used to customize a label. -->
    <xsl:variable name="context" select="name($node/parent::node())"/>

    <!-- Retrieve the xpath (until the name of the element), may be used to customize a label. -->
    <xsl:variable name="xpath">
      <xsl:for-each select="$node">
        <xsl:call-template name="getXPath"/>
      </xsl:for-each>
    </xsl:variable>


    <xsl:variable name="helpers">
      <xsl:choose>
        <xsl:when
          test="starts-with($schema,'iso19139')">
          <xsl:choose>
            <!-- Exact schema, name and full context match -->
            <xsl:when
              test="$labels/schemas/*[name(.)=$schema]/labels/element[@name = $name and @context=$xpath]/helper">
              <xsl:copy-of
                select="$labels/schemas/*[name(.)=$schema]/labels/element[@name = $name and (@context=$xpath or @context=$context)]/helper"/>
            </xsl:when>
            <!-- ISO19139, name and full context match -->
            <xsl:when
              test="$labels/schemas/iso19139/labels/element[@name = $name and @context=$xpath]/helper">
              <xsl:copy-of
                select="$labels/schemas/iso19139/labels/element[@name = $name and (@context=$xpath or @context=$context)]/helper"/>
            </xsl:when>
            <!-- Exact schema, name and parent-only match -->
            <xsl:when
              test="$labels/schemas/*[name(.)=$schema]/labels/element[@name = $name and @context=$context]/helper">
              <xsl:copy-of
                select="$labels/schemas/*[name(.)=$schema]/labels/element[@name = $name and (@context=$xpath or @context=$context)]/helper"/>
            </xsl:when>
            <!-- ISO19139, name and parent-only match -->
            <xsl:when
              test="$labels/schemas/iso19139/labels/element[@name = $name and @context=$context]/helper">
              <xsl:copy-of
                select="$labels/schemas/iso19139/labels/element[@name = $name and (@context=$xpath or @context=$context)]/helper"/>
            </xsl:when>
            <!-- Exact schema, name match -->
            <xsl:when
              test="$labels/schemas/*[name(.)=$schema]/labels/element[@name = $name and not(@context)]/helper">
              <xsl:copy-of
                select="$labels/schemas/*[name(.)=$schema]/labels/element[@name = $name and not(@context)]/helper"/>
            </xsl:when>
            <!-- ISO19139 schema, name match -->
            <xsl:when
              test="$labels/schemas/iso19139/labels/element[@name = $name and not(@context)]/helper">
              <xsl:copy-of
                select="$labels/schemas/iso19139/labels/element[@name = $name and not(@context)]/helper"/>
            </xsl:when>
            <xsl:otherwise>
              <null/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:when>
        <xsl:otherwise>

          <xsl:variable name="result"
                        select="$labels/schemas/*[name(.)=$schema]/labels/element[@name = $name]/helper"
          />

          <xsl:choose>
            <xsl:when test="$result">
              <xsl:copy-of select="$result"/>
            </xsl:when>
            <xsl:otherwise>
              <null/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <xsl:choose>
      <xsl:when test="count($helpers/*) = 1">
        <xsl:copy-of select="$helpers/*"/>
      </xsl:when>
      <xsl:otherwise>
        <!-- Conditional helpers which may define an xpath expression to evaluate
        if the xpath match -->
        <xsl:variable name="matchingHelpers">
          <xsl:for-each select="$helpers/*">
            <xsl:if test="@displayIf and saxon:evaluate(@displayIf, $node)">
              <xsl:copy-of select="."/>
            </xsl:if>
          </xsl:for-each>
        </xsl:variable>

        <xsl:choose>
          <xsl:when test="$matchingHelpers/helper">
            <xsl:copy-of select="$matchingHelpers/helper"/>
          </xsl:when>
          <xsl:when test="$helpers/helper[not(@displayIf)]">
            <xsl:copy-of select="$helpers/helper[not(@displayIf)]"/>
          </xsl:when>
          <xsl:otherwise>
            <null/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:function>

  <xsl:template name="getXPath">
    <xsl:for-each select="ancestor-or-self::*">
      <xsl:if test="not(position() = 1)">
        <xsl:value-of select="name()"/>
      </xsl:if>
      <xsl:if test="not(position() = 1) and not(position() = last())">
        <xsl:text>/</xsl:text>
      </xsl:if>
    </xsl:for-each>
    <!-- Check if is an attribute: http://www.dpawson.co.uk/xsl/sect2/nodetest.html#d7610e91 -->
    <xsl:if test="count(. | ../@*) = count(../@*)">/@<xsl:value-of select="name()"/>
    </xsl:if>
  </xsl:template>

  <xsl:template name="getTitleColor">
    <xsl:param name="name"/>
    <xsl:param name="schema"/>

    <xsl:variable name="fullContext">
      <xsl:call-template name="getXPath"/>
    </xsl:variable>

    <xsl:variable name="context" select="name(parent::node())"/>
    <xsl:variable name="contextIsoType" select="parent::node()/@gco:isoType"/>

    <xsl:variable name="color">
      <xsl:choose>
        <xsl:when test="starts-with($schema,'iso19139')">

          <!-- Name with context in current schema -->
          <xsl:variable name="colorTitleWithContext"
                        select="string(/root/gui/schemas/*[name(.)=$schema]/element[@name=$name and (@context=$fullContext or @context=$context or @context=$contextIsoType)]/label_color)"/>

          <!-- Name with context in base schema -->
          <xsl:variable name="colorTitleWithContextIso"
                        select="string(/root/gui/schemas/iso19139/element[@name=$name and (@context=$fullContext or @context=$context or @context=$contextIsoType)]/label_color)"/>

          <!-- Name in current schema -->
          <xsl:variable name="colorTitle"
                        select="string(/root/gui/schemas/*[name(.)=$schema]/element[@name=$name and not(@context)]/label_color)"/>

          <xsl:choose>

            <xsl:when
              test="normalize-space($colorTitle)='' and
              normalize-space($colorTitleWithContext)='' and
              normalize-space($colorTitleWithContextIso)=''">
              <xsl:value-of
                select="string(/root/gui/schemas/iso19139/element[@name=$name]/label_color)"/>
            </xsl:when>
            <xsl:when
              test="normalize-space($colorTitleWithContext)='' and
              normalize-space($colorTitleWithContextIso)=''">
              <xsl:value-of select="$colorTitle"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="$colorTitleWithContext"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:when>

        <!-- otherwise just get the title out of the approriate schema help file -->

        <xsl:otherwise>
          <xsl:value-of
            select="string(/root/gui/schemas/*[name(.)=$schema]/element[@name=$name]/label_color)"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <xsl:value-of select="$color"/>
  </xsl:template>

  <!--
    Returns the title of an element. If the schema is an ISO profil then search:
    * the ISO profil help first
    * with context (ie. context is the class where the element is defined)
    * with no context
    and if not found search the iso19139 main help.

    If not iso based, search in corresponding schema.

    If not found return the element name.
  -->
  <xsl:template name="getTitle">
    <xsl:param name="name"/>
    <xsl:param name="schema"/>

    <xsl:variable name="fullContext">
      <xsl:call-template name="getXPath"/>
    </xsl:variable>

    <xsl:variable name="context" select="name(parent::node())"/>
    <xsl:variable name="contextIsoType" select="parent::node()/@gco:isoType"/>

    <xsl:variable name="title">
      <xsl:choose>
        <xsl:when test="starts-with($schema,'iso19139')">

          <!-- Name with context in current schema -->
          <xsl:variable name="schematitleWithContext"
                        select="string(/root/gui/schemas/*[name(.)=$schema]/labels/element[@name=$name and (@context=$fullContext or @context=$context or @context=$contextIsoType)]/label)"/>

          <!-- Name with context in base schema -->
          <xsl:variable name="schematitleWithContextIso"
                        select="string(/root/gui/schemas/iso19139/labels/element[@name=$name and (@context=$fullContext or @context=$context or @context=$contextIsoType)]/label)"/>

          <!-- Name in current schema -->
          <xsl:variable name="schematitle"
                        select="/root/gui/schemas/*[name(.)=$schema]/labels/element[@name=$name and not(@context)]/label/text()"/>

          <xsl:choose>
            <xsl:when
              test="normalize-space($schematitleWithContext)!=''">
              <xsl:value-of select="$schematitleWithContext"/>
            </xsl:when>
            <xsl:when
              test="normalize-space($schematitleWithContextIso)!=''">
              <xsl:value-of select="$schematitleWithContextIso"/>
            </xsl:when>
            <xsl:when
              test="normalize-space($schematitle)!=''">
              <xsl:value-of select="$schematitle"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of
                select="/root/gui/schemas/iso19139/labels/element[@name=$name and not(@context)]/label/string()"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:when>

        <!-- otherwise just get the title out of the approriate schema help file -->

        <xsl:otherwise>
          <xsl:value-of
            select="string(/root/gui/schemas/*[name(.)=$schema]/labels/element[@name=$name and not(@context)]/label)"
          />
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <xsl:choose>
      <xsl:when test="normalize-space($title)!=''">
        <xsl:value-of select="$title"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$name"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>


  <!-- build attribute name (in place of standard attribute name) as a
    work-around to deal with qualified attribute names like gml:id
    which if not modified will cause JDOM errors on update because of the
    way in which changes to ref'd elements are parsed as XML -->
  <xsl:template name="getAttributeName">
    <xsl:param name="name"/>
    <xsl:choose>
      <xsl:when test="contains($name,':')">
        <xsl:value-of
          select="concat(substring-before($name,':'),'COLON',substring-after($name,':'))"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$name"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- Copy all elements and attributes excluding GeoNetwork elements.
    This could be useful to get the source XML when working on a metadocument.
    This is used in edit mode usually to populate a _X element and & are escaped.
  -->
  <xsl:template match="@*|node()[namespace-uri()!='http://www.fao.org/geonetwork']"
                mode="geonet-cleaner">
    <xsl:copy>
      <xsl:for-each select="@*[namespace-uri()!='http://www.fao.org/geonetwork']">
        <xsl:attribute name="{name()}">
          <xsl:call-template name="replaceString">
            <xsl:with-param name="expr" select="string()"/>
            <xsl:with-param name="pattern" select="'&amp;'"/>
            <xsl:with-param name="replacement" select="'&amp;amp;'"/>
          </xsl:call-template>
        </xsl:attribute>
      </xsl:for-each>
      <xsl:apply-templates select="node()" mode="geonet-cleaner"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template mode="geonet-cleaner" match="text()" priority="2">
    <xsl:apply-templates mode="escapeXMLEntities" select="."/>
  </xsl:template>

</xsl:stylesheet>
