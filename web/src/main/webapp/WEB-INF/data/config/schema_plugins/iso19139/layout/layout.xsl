<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:gts="http://www.isotc211.org/2005/gts"
  xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:gmx="http://www.isotc211.org/2005/gmx"
  xmlns:srv="http://www.isotc211.org/2005/srv" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xmlns:gml="http://www.opengis.net/gml" xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns:gn="http://www.fao.org/geonetwork"
  xmlns:gn-fn-core="http://geonetwork-opensource.org/xsl/functions/core" 
  xmlns:gn-fn-metadata="http://geonetwork-opensource.org/xsl/functions/metadata"
  xmlns:gn-fn-iso19139="http://geonetwork-opensource.org/xsl/functions/profiles/iso19139"
  xmlns:exslt="http://exslt.org/common" exclude-result-prefixes="#all">

  <xsl:include href="utility-fn.xsl"/>
  <xsl:include href="utility-tpl.xsl"/>
  <xsl:include href="layout-custom-fields.xsl"/>

  <!-- Ignore all gn element -->
  <xsl:template mode="mode-iso19139" match="gn:*|@gn:*" priority="1000"/>


  <!-- Template to display non existing element ie. geonet:child element
	of the metadocument. Display in editing mode only and if 
  the editor mode is not flat mode. -->
  <xsl:template mode="mode-iso19139" match="gn:child" priority="2000">
    <xsl:param name="schema" select="$schema" required="no"/>
    <xsl:param name="labels" select="$labels" required="no"/>

    <!-- TODO: this should be common to all schemas -->
    <xsl:if test="$isEditing and 
      not($isFlatMode)">
      <xsl:call-template name="render-element-to-add">
        <xsl:with-param name="label"
          select="gn-fn-metadata:getLabel($schema, concat(@prefix, ':', @name), $labels)/label"/>
        <xsl:with-param name="childEditInfo" select="."/>
        <xsl:with-param name="parentEditInfo" select="../gn:element"/>
      </xsl:call-template>
    </xsl:if>
  </xsl:template>


  <!-- Visit all XML tree recursively -->
  <xsl:template mode="mode-iso19139" match="gmd:*|gmx:*|gml:*|srv:*|gts:*">
    <xsl:param name="schema" select="$schema" required="no"/>
    <xsl:param name="labels" select="$labels" required="no"/>

    <xsl:apply-templates mode="mode-iso19139" select="*|@*">
      <xsl:with-param name="schema" select="$schema"/>
      <xsl:with-param name="labels" select="$labels"/>
    </xsl:apply-templates>
  </xsl:template>

  <!-- Boxed element -->
  <!-- Could be nice to externalize TODO
  <xsl:template mode="mode-iso19139" priority="200"
      match="*[gn-fn-core:contains-any-of(name(.), ('gmd:MD_Metadata', 'gmd:identificationInfo', 'gmd:distributionInfo'))]|
              *[namespace-uri(.) != $gnUri and $isFlatMode = false() and gmd:*]">
  -->
  <xsl:template mode="mode-iso19139" priority="200"
    match="gmd:MD_Metadata|*[@gco:isoType='gmd:MD_Metadata']|
		gmd:identificationInfo|
		gmd:distributionInfo|
		gmd:portrayalCatalogueInfo|
		gmd:portrayalCatalogueCitation|
		gmd:descriptiveKeywords|
		gmd:thesaurusName|
		*[name(..)='gmd:resourceConstraints']|
		gmd:spatialRepresentationInfo|
		gmd:pointOfContact|
		gmd:contact|
		gmd:dataQualityInfo|
		gmd:contentInfo|
		gmd:distributionFormat|
		gmd:referenceSystemInfo|
		gmd:spatialResolution|
		gmd:offLine|
		gmd:onLine|
		gmd:address|
		gmd:projection|
		gmd:ellipsoid|
		gmd:extent[name(..)!='gmd:EX_TemporalExtent']|
		gmd:attributes|
		gmd:verticalCRS|
		gmd:geographicBox|
		gmd:EX_TemporalExtent|
		gmd:MD_Distributor|
		srv:containsOperations|
		srv:SV_CoupledResource|
		gmd:metadataConstraints|
		gmd:aggregationInfo|
		gmd:report/*|
		gmd:result/*|
		gmd:processStep|
		gmd:lineage|
		*[namespace-uri(.) != $gnUri and $isFlatMode = false() and gmd:*]">
    <xsl:param name="schema" select="$schema" required="no"/>
    <xsl:param name="labels" select="$labels" required="no"/>
    
    <xsl:variable name="xpath" select="gn-fn-metadata:getXPath(.)"/>
    <xsl:variable name="isoType" select="if (../@gco:isoType) then ../@gco:isoType else ''"/>

    <xsl:variable name="attributes">
      <xsl:if test="$isEditing">
        <!-- Create form for all existing attribute (not in gn namespace)
        and all non existing attributes not already present. -->
        <xsl:apply-templates mode="render-for-field-for-attribute" 
          select="
          @*|
          gn:attribute[not(@name = parent::node()/@*/name())]">
          <xsl:with-param name="ref" select="gn:element/@ref"/>
          <xsl:with-param name="insertRef" select="gn:element/@ref"/>
        </xsl:apply-templates>
      </xsl:if>
    </xsl:variable>

    <xsl:call-template name="render-boxed-element">
      <xsl:with-param name="label"
        select="gn-fn-metadata:getLabel($schema, name(), $labels, name(..), $isoType, $xpath)/label"/>
      <xsl:with-param name="editInfo" select="gn:element"/>
      <xsl:with-param name="cls" select="local-name()"/>
      <xsl:with-param name="xpath" select="$xpath"/>
      <xsl:with-param name="attributesSnippet" select="$attributes"/>
      <xsl:with-param name="subTreeSnippet">
        <!-- Process child of those element. Propagate schema
        and labels to all subchilds (eg. needed like iso19110 elements
        contains gmd:* child. -->
        <xsl:apply-templates mode="mode-iso19139" select="*">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="labels" select="$labels"/>
        </xsl:apply-templates>
      </xsl:with-param>
    </xsl:call-template>

  </xsl:template>

  
  
  <!-- Render simple element which usually match a form field -->
  <xsl:template mode="mode-iso19139" priority="100"
    match="*[gco:CharacterString|gco:Date|gco:DateTime|gco:Integer|gco:Decimal|
		gco:Boolean|gco:Real|gco:Measure|gco:Length|gco:Distance|gco:Angle|
		gco:Scale|gco:RecordType|gmx:MimeFileType|gmd:URL]">
    <xsl:param name="schema" select="$schema" required="no"/>
    <xsl:param name="labels" select="$labels" required="no"/>

    <xsl:variable name="xpath" select="gn-fn-metadata:getXPath(.)"/>
    <xsl:variable name="isoType" select="if (../@gco:isoType) then ../@gco:isoType else ''"/>
    <xsl:variable name="labelConfig"
      select="gn-fn-metadata:getLabel($schema, name(), $labels, name(..), $isoType, $xpath)"/>
    <xsl:variable name="helper" select="gn-fn-metadata:getHelper($labelConfig/helper, .)"/>
    
    <xsl:variable name="attributes">
      <xsl:if test="$isEditing">
        
        <!-- Create form for all existing attribute (not in gn namespace)
        and all non existing attributes not already present for the
        current element and its children (eg. @uom in gco:Distance). 
        A list of exception is defined in form-builder.xsl#render-for-field-for-attribute. -->
        <xsl:apply-templates mode="render-for-field-for-attribute" 
          select="
              @*|
              gn:attribute[not(@name = parent::node()/@*/name())]">
          <xsl:with-param name="ref" select="gn:element/@ref"/>
          <xsl:with-param name="insertRef" select="*/gn:element/@ref"/>
        </xsl:apply-templates>
        <xsl:apply-templates mode="render-for-field-for-attribute" 
          select="
          */@*|
          */gn:attribute[not(@name = parent::node()/@*/name())]">
          <xsl:with-param name="ref" select="*/gn:element/@ref"/>
          <xsl:with-param name="insertRef" select="*/gn:element/@ref"/>
        </xsl:apply-templates>
      </xsl:if>
    </xsl:variable>
    
    
    <xsl:call-template name="render-element">
      <xsl:with-param name="label" select="$labelConfig/label"/>
      <xsl:with-param name="value" select="*"/>
      <xsl:with-param name="cls" select="local-name()"/>
      <!--<xsl:with-param name="widget"/>
			<xsl:with-param name="widgetParams"/>-->
      <xsl:with-param name="xpath" select="$xpath"/>
      <xsl:with-param name="attributesSnippet" select="$attributes"/>
      <xsl:with-param name="type"
        select="gn-fn-iso19139:getFieldType(name(), 
            name(gco:CharacterString|gco:Date|gco:DateTime|gco:Integer|gco:Decimal|
                gco:Boolean|gco:Real|gco:Measure|gco:Length|gco:Distance|gco:Angle|
                gco:Scale|gco:RecordType|gmx:MimeFileType|gmd:URL))"/>
      <xsl:with-param name="name" select="if ($isEditing) then */gn:element/@ref else ''"/>
      <xsl:with-param name="editInfo" select="*/gn:element"/>
      <xsl:with-param name="parentEditInfo" select="gn:element"/>
      <!-- TODO: Handle conditional helper -->
      <xsl:with-param name="listOfValues" select="$helper"/>
    </xsl:call-template>

  </xsl:template>


  <!-- 
    
    gmx:FileName could be used as substitution of any
    gco:CharacterString. To turn this on add a schema 
    suggestion.
    
   <gmd:otherCitationDetails>
      <gmx:FileName 
                    src="http://localhost:8080/geonetwork/srv/eng/resources.get?uuid=da165110-88fd-11da-a88f-000d939bc5d8&amp;fname=&amp;access=private">
                    Quality report</gmd:FileName>
   </gmd:otherCitationDetails>
  -->
  <xsl:template mode="mode-iso19139" match="*[gmx:FileName]">
    
    <xsl:variable name="src" select="gmx:FileName/@src"/>
    
    <xsl:variable name="xpath" select="gn-fn-metadata:getXPath(.)"/>
    <xsl:variable name="isoType" select="if (../@gco:isoType) then ../@gco:isoType else ''"/>
    <xsl:variable name="labelConfig"
      select="gn-fn-metadata:getLabel($schema, name(), $labels, name(..), $isoType, $xpath)"/>
    <xsl:variable name="helper" select="gn-fn-metadata:getHelper($labelConfig/helper, .)"/>
    
    <xsl:variable name="attributes">
      <xsl:if test="$isEditing">
        <!-- Add specific attribute for the child element -->
        <xsl:apply-templates mode="render-for-field-for-attribute" 
          select="
          gmx:FileName/@src">
          <xsl:with-param name="ref" select="gmx:FileName/gn:element/@ref"/>
        </xsl:apply-templates>
        
        <!-- Create form for all existing attribute (not in gn namespace)
        and all non existing attributes not already present. -->
        <xsl:apply-templates mode="render-for-field-for-attribute" 
          select="
          @*|
          gn:attribute[not(@name = parent::node()/@*/name())]">
          <xsl:with-param name="ref" select="gn:element/@ref"/>
          <xsl:with-param name="insertRef" select="*/gn:element/@ref"/>
        </xsl:apply-templates>
      </xsl:if>
    </xsl:variable>
    
    <xsl:call-template name="render-element">
      <xsl:with-param name="label" select="$labelConfig/label"/>
      <xsl:with-param name="value" select="*"/>
      <xsl:with-param name="cls" select="local-name()"/>
      <!--<xsl:with-param name="widget"/>
      <xsl:with-param name="widgetParams"/>-->
      <xsl:with-param name="xpath" select="$xpath"/>
      <xsl:with-param name="attributesSnippet" select="$attributes"/>
      <xsl:with-param name="type"
        select="gn-fn-iso19139:getFieldType(name(), 
        name(gmx:FileName))"/>
      <xsl:with-param name="name" select="if ($isEditing) then */gn:element/@ref else ''"/>
      <xsl:with-param name="editInfo" select="*/gn:element"/>
      <xsl:with-param name="parentEditInfo" select="gn:element"/>
      <!-- TODO: Handle conditional helper -->
      <xsl:with-param name="listOfValues" select="$helper"/>
    </xsl:call-template>
    
  </xsl:template>


  <!-- Match codelist values.
  
  eg. 
  <gmd:CI_RoleCode codeList="./resources/codeList.xml#CI_RoleCode" codeListValue="pointOfContact">
    <geonet:element ref="42" parent="41" uuid="gmd:CI_RoleCode_e75c8ec6-b994-4e98-b7c8-ecb48bda3725" min="1" max="1"/>
    <geonet:attribute name="codeList"/>
    <geonet:attribute name="codeListValue"/>
    <geonet:attribute name="codeSpace" add="true"/>
  
  -->
  <xsl:template mode="mode-iso19139" priority="200" match="gmd:*[*/@codeList]|srv:*[*/@codeList]">
    <xsl:param name="schema" select="$schema" required="no"/>
    <xsl:param name="labels" select="$labels" required="no"/>
    <xsl:param name="codelists" select="$iso19139codelists" required="no"/>

    <xsl:variable name="xpath" select="gn-fn-metadata:getXPath(.)"/>
    <xsl:variable name="isoType" select="if (../@gco:isoType) then ../@gco:isoType else ''"/>


    <xsl:call-template name="render-element">
      <xsl:with-param name="label"
        select="gn-fn-metadata:getLabel($schema, name(), $labels, name(..), $isoType, $xpath)/label"/>
      <xsl:with-param name="value" select="*/@codeListValue"/>
      <xsl:with-param name="cls" select="local-name()"/>
      <!--<xsl:with-param name="widget"/>
            <xsl:with-param name="widgetParams"/>-->
      <xsl:with-param name="xpath" select="$xpath"/>
      <!--<xsl:with-param name="attributesSnippet" as="node()"/>-->
      <xsl:with-param name="type" select="gn-fn-iso19139:getCodeListType(name())"/>
      <xsl:with-param name="name"
        select="if ($isEditing) then concat(*/gn:element/@ref, '_codeListValue') else ''"/>
      <xsl:with-param name="editInfo" select="*/gn:element"/>
      <xsl:with-param name="listOfValues"
        select="gn-fn-metadata:getCodeListValues($schema, name(*[@codeListValue]), $codelists)"/>
    </xsl:call-template>

  </xsl:template>

</xsl:stylesheet>
