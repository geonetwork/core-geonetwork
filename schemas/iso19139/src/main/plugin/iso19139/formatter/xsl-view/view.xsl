<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gmx="http://www.isotc211.org/2005/gmx"
                xmlns:srv="http://www.isotc211.org/2005/srv"
                xmlns:gml="http://www.opengis.net/gml"
                xmlns:gts="http://www.isotc211.org/2005/gts"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:tr="java:org.fao.geonet.services.metadata.format.SchemaLocalizations"
                xmlns:gn-fn-render="http://geonetwork-opensource.org/xsl/functions/render"
                xmlns:gn-fn-metadata="http://geonetwork-opensource.org/xsl/functions/metadata"
                xmlns:saxon="http://saxon.sf.net/"
                extension-element-prefixes="saxon"
                exclude-result-prefixes="#all">

  <!-- Load the editor configuration to be able
  to render the different views -->
  <xsl:variable name="configuration"
                select="document('../../layout/config-editor.xml')"/>

  <!-- Some utility -->
  <xsl:include href="../../layout/evaluate.xsl"/>
  <xsl:include href="../../layout/utility-tpl-multilingual.xsl"/>

  <!-- The core formatter XSL layout based on the editor configuration -->
  <xsl:include href="sharedFormatterDir/xslt/render-layout.xsl"/>
  <!--<xsl:include href="../../../../../data/formatter/xslt/render-layout.xsl"/>-->

  <!-- Define the metadata to be loaded for this schema plugin-->
  <xsl:variable name="metadata"
                select="/root/gmd:MD_Metadata"/>




  <!-- Specific schema rendering -->
  <xsl:template mode="getMetadataTitle" match="gmd:MD_Metadata">
    <xsl:variable name="value"
                  select="gmd:identificationInfo/*/gmd:citation/*/gmd:title"/>
    <xsl:value-of select="$value/gco:CharacterString"/>
  </xsl:template>

  <xsl:template mode="getMetadataAbstract" match="gmd:MD_Metadata">
    <xsl:variable name="value"
                  select="gmd:identificationInfo/*/gmd:abstract"/>
    <xsl:value-of select="$value/gco:CharacterString"/>
  </xsl:template>





  <!-- Most of the elements are ... -->
  <xsl:template mode="render-field" match="*[gco:CharacterString|gco:Integer|gco:Decimal|
       gco:Boolean|gco:Real|gco:Measure|gco:Length|gco:Distance|gco:Angle|gmx:FileName|
       gco:Scale|gco:Record|gco:RecordType|gmx:MimeFileType|gmd:URL|
       gco:LocalName|gmd:PT_FreeText|gml:beginPosition|gml:endPosition|gco:Date|gco:DateTime|*/@codeListValue]">
    <xsl:param name="fieldName" select="''" as="xs:string"/>

    <dl>
      <dt>
        <xsl:value-of select="if ($fieldName)
                                then $fieldName
                                else tr:node-label(tr:create($schema), name(), null)"/>
      </dt>
      <dd>
        <xsl:apply-templates mode="render-value" select="*|*/@codeListValue"/>
      </dd>
    </dl>
  </xsl:template>


  <!-- Some major sections are boxed -->
  <xsl:template mode="render-field"
                match="*[name() = $configuration/editor/fieldsWithFieldset/name
    or @gco:isoType = $configuration/editor/fieldsWithFieldset/name]|
      gmd:report/*|
      gmd:result/*|
      gmd:extent[name(..)!='gmd:EX_TemporalExtent']|
      *[$isFlatMode = false() and gmd:* and not(gco:CharacterString) and not(gmd:URL)]">
    <div class="entry name">
      <h3>
        <xsl:value-of select="tr:node-label(tr:create($schema), name(), null)"/>
      </h3>
      <div class="target">
        <xsl:apply-templates mode="render-field" select="*"/>
      </div>
    </div>
  </xsl:template>

  <!-- Bbox is displayed with an overview and the geom displayed on it
  and the coordinates displayed around -->
  <xsl:template mode="render-field"
                match="gmd:EX_GeographicBoundingBox[
          gmd:westBoundLongitude/gco:Decimal != '']">
    <xsl:copy-of select="gn-fn-render:bbox(
                            xs:double(gmd:westBoundLongitude/gco:Decimal),
                            xs:double(gmd:southBoundLatitude/gco:Decimal),
                            xs:double(gmd:eastBoundLongitude/gco:Decimal),
                            xs:double(gmd:northBoundLatitude/gco:Decimal))"/>
  </xsl:template>


  <!-- A contact is displayed with its role as header -->
  <xsl:template mode="render-field"
                match="*[gmd:CI_ResponsibleParty]">
    <dl class="gn-contact">
      <dt>
        <xsl:apply-templates mode="render-value"
                             select="*/gmd:role/*/@codeListValue"/>
      </dt>
      <dd>
        <div>
          <xsl:apply-templates mode="render-field"
                               select="*/(gmd:organisationName|gmd:individualName)"/>
        </div>
      </dd>
      <dd>
        <div>
          <xsl:apply-templates mode="render-field"
                               select="*/gmd:contactInfo"/>
        </div>
      </dd>
    </dl>
  </xsl:template>


  <!-- Display thesaurus name and the list of keywords -->
  <xsl:template mode="render-field"
                match="gmd:descriptiveKeywords[*/gmd:thesaurusName/gmd:CI_Citation/gmd:title]"
                priority="200">
    <dl class="gn-keyword">
      <dt>
        <xsl:apply-templates mode="render-value"
                             select="*/gmd:thesaurusName/gmd:CI_Citation/gmd:title/*"/>

        <xsl:if test="*/gmd:type/*[@codeListValue != '']">
          (<xsl:apply-templates mode="render-value"
                                select="*/gmd:type/*/@codeListValue"/>)
        </xsl:if>
      </dt>
      <dd>
        <div>
          <ul>
            <li>
              <xsl:apply-templates mode="render-value"
                                   select="*/gmd:keyword/*"/>
            </li>
          </ul>
        </div>
      </dd>
    </dl>
  </xsl:template>
  <xsl:template mode="render-field"
                match="gmd:descriptiveKeywords[not(*/gmd:thesaurusName/gmd:CI_Citation/gmd:title)]"
                priority="200">
    <dl class="gn-keyword">
      <dt>
        <xsl:value-of select="$schemaStrings/noThesaurusName"/>
        <xsl:if test="*/gmd:type/*[@codeListValue != '']">
          (<xsl:apply-templates mode="render-value"
                                select="*/gmd:type/*/@codeListValue"/>)
        </xsl:if>
      </dt>
      <dd>
        <div>
          <ul>
            <li>
              <xsl:apply-templates mode="render-value"
                                   select="*/gmd:keyword/*"/>
            </li>
          </ul>
        </div>
      </dd>
    </dl>
  </xsl:template>

  <!-- Display all graphic overviews in one block -->
  <xsl:template mode="render-field"
                match="gmd:graphicOverview[1]">
    <dl>
      <dt>
        <xsl:value-of select="tr:node-label(tr:create($schema), name(), null)"/>
      </dt>
      <dd>
        <ul>
        <xsl:for-each select="parent::node()/gmd:graphicOverview">
          <xsl:variable name="label">
            <xsl:apply-templates mode="localised"
                               select="gmd:MD_BrowseGraphic/gmd:fileDescription"/>
          </xsl:variable>
          <li>
            <img src="{gmd:MD_BrowseGraphic/gmd:fileName/*}"
                 alt="{$label}"
                 class="img-thumbnail"/>
          </li>
        </xsl:for-each>
        </ul>
      </dd>
    </dl>
  </xsl:template>
  <xsl:template mode="render-field"
                match="gmd:graphicOverview[position() > 1]"/>


  <xsl:template mode="render-field"
                match="gmd:distributionFormat[1]"
                priority="200">
    <dl class="gn-format">
      <dt>
        <xsl:value-of select="tr:node-label(tr:create($schema), name(), null)"/>
      </dt>
      <dd>
        <ul>
          <xsl:for-each select="parent::node()/gmd:distributionFormat">
            <li>
              <xsl:apply-templates mode="render-value"
                                   select="*/gmd:name"/>
              (<xsl:apply-templates mode="render-value"
                                    select="*/gmd:version"/>)
              <p>
              <xsl:apply-templates mode="render-field"
                      select="*/(gmd:amendmentNumber|gmd:specification|
                              gmd:fileDecompressionTechnique|gmd:formatDistributor)"/>
              </p>
            </li>
          </xsl:for-each>
        </ul>
      </dd>
    </dl>
  </xsl:template>
  <xsl:template mode="render-field"
                match="gmd:distributionFormat[position() > 1]"/>


  <xsl:template mode="render-field"
                match="gmd:date"
                priority="200">
    <dl class="gn-date">
      <dt>
        <xsl:value-of select="tr:node-label(tr:create($schema), name(), null)"/>
        <xsl:if test="*/gmd:dateType/*[@codeListValue != '']">
          <!-- TODO: translate -->
          (<xsl:apply-templates mode="render-value"
                                select="*/gmd:dateType/*/@codeListValue"/>)
        </xsl:if>
      </dt>
      <dd>
          <xsl:apply-templates mode="render-value"
                               select="*/gmd:date/*"/>
      </dd>
    </dl>
  </xsl:template>

  <!-- Traverse the tree -->
  <xsl:template mode="render-field" match="*">
    <xsl:apply-templates mode="render-field"/>
  </xsl:template>







  <!-- ########################## -->
  <!-- Render values for text ... -->
  <xsl:template mode="render-value" match="gco:CharacterString|gco:Integer|gco:Decimal|
       gco:Boolean|gco:Real|gco:Measure|gco:Length|gco:Distance|gco:Angle|gmx:FileName|
       gco:Scale|gco:Record|gco:RecordType|gmx:MimeFileType|gmd:URL|
       gco:LocalName|gml:beginPosition|gml:endPosition">
    <xsl:value-of select="."/>
  </xsl:template>

  <xsl:template mode="render-value" match="gmd:PT_FreeText">
    <xsl:apply-templates mode="localised" select="../node()">
      <xsl:with-param name="langId" select="$language"/>
    </xsl:apply-templates>
  </xsl:template>

  <!-- ... URL -->
  <xsl:template mode="render-value" match="gmd:URL">
    <a href="{.}"><xsl:value-of select="."/></a>
  </xsl:template>

  <!-- ... Dates -->
  <xsl:template mode="render-value"
                match="gco:Date[matches(., '[0-9]{4}')]">
    <span data-gn-humanize-time="{.}" data-format="YYYY"></span>
  </xsl:template>

  <xsl:template mode="render-value"
                match="gco:Date[matches(., '[0-9]{4}-[0-9]{2}')]">
    <span data-gn-humanize-time="{.}" data-format="MMM YYYY"></span>
  </xsl:template>

  <xsl:template mode="render-value"
                match="gco:Date[matches(., '[0-9]{4}-[0-9]{2}-[0-9]{2}')]">
    <span data-gn-humanize-time="{.}" data-format="DD MMM YYYY"></span>
  </xsl:template>

  <xsl:template mode="render-value"
                match="gco:DateTime[matches(., '[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}')]">
    <span data-gn-humanize-time="{.}"></span>
  </xsl:template>

  <xsl:template mode="render-value"
                match="gco:Date|gco:DateTime">
    <span data-gn-humanize-time="{.}"></span>
  </xsl:template>

  <!-- ... Codelists -->
  <xsl:template mode="render-value" match="@codeListValue">
    <xsl:variable name="id" select="."/>
    <!--<xsl:value-of select="tr:node-label(tr:create($schema), .)"/>-->
    <xsl:variable name="codelistTranslation"
                  select="$schemaCodelists//entry[code = $id]/label"/>

    <xsl:choose>
      <xsl:when test="$codelistTranslation != ''">
        <xsl:value-of select="$codelistTranslation"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$id"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>






</xsl:stylesheet>