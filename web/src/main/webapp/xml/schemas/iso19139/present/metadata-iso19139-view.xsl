<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:gts="http://www.isotc211.org/2005/gts"
  xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:gmx="http://www.isotc211.org/2005/gmx"
  xmlns:srv="http://www.isotc211.org/2005/srv" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xmlns:gml="http://www.opengis.net/gml" xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns:geonet="http://www.fao.org/geonetwork" xmlns:exslt="http://exslt.org/common"
  xmlns:saxon="http://saxon.sf.net/" extension-element-prefixes="saxon"
  exclude-result-prefixes="gmx xsi gmd gco gml gts srv xlink exslt geonet">

  <!-- View templates are available only in view mode and does not provide editing
  capabilities. Template MUST start with "view". -->
  <!-- ===================================================================== -->
  <!-- iso19139-simple -->
  <xsl:template name="metadata-iso19139view-simple" match="metadata-iso19139view-simple">
    <!--<xsl:apply-templates mode="iso19139-simple" select="*"/>-->

    <xsl:call-template name="md-content">
      <xsl:with-param name="title">
        <xsl:apply-templates mode="localised"
          select="gmd:identificationInfo/*/gmd:citation/gmd:CI_Citation/gmd:title">
          <xsl:with-param name="langId" select="$langId"/>
        </xsl:apply-templates>
      </xsl:with-param>
      <xsl:with-param name="exportButton"/>
      <xsl:with-param name="abstract">
        <xsl:call-template name="addHyperlinksAndLineBreaks">
          <xsl:with-param name="txt">
            <xsl:apply-templates mode="localised" select="gmd:identificationInfo/*/gmd:abstract">
              <xsl:with-param name="langId" select="$langId"/>
            </xsl:apply-templates>
          </xsl:with-param>
        </xsl:call-template>
      </xsl:with-param>
      <xsl:with-param name="logo">
        <img src="../../images/logos/{//geonet:info/source}.gif" alt="logo"/>
      </xsl:with-param>
      <xsl:with-param name="relatedResources">
        <xsl:apply-templates mode="relatedResources"
          select="gmd:distributionInfo"
        />
      </xsl:with-param>
      <xsl:with-param name="tabs">
        <xsl:call-template name="complexElementSimpleGui">
          <xsl:with-param name="title"
            select="/root/gui/schemas/iso19139/strings/understandResource"/>
          <xsl:with-param name="content">
            <xsl:apply-templates mode="block"
              select="
                gmd:identificationInfo/*/gmd:citation/gmd:CI_Citation[gmd:date]
                |gmd:identificationInfo/*/gmd:language
                |gmd:topicCategory
                |gmd:identificationInfo/*/gmd:descriptiveKeywords
                |gmd:identificationInfo/*/gmd:graphicOverview[1]
                |gmd:identificationInfo/*/gmd:extent/gmd:EX_Extent/gmd:geographicElement
                "> </xsl:apply-templates>

          </xsl:with-param>
        </xsl:call-template>


        <xsl:call-template name="complexElementSimpleGui">
          <xsl:with-param name="title" select="/root/gui/schemas/iso19139/strings/contactInfo"/>
          <xsl:with-param name="content">
            <xsl:apply-templates mode="block"
              select="gmd:identificationInfo/*/gmd:pointOfContact"/> 
            <xsl:apply-templates mode="block"
              select="gmd:contact"/>
          </xsl:with-param>
        </xsl:call-template>

        <xsl:call-template name="complexElementSimpleGui">
          <xsl:with-param name="title" select="/root/gui/schemas/iso19139/strings/techInfo"/>
          <xsl:with-param name="content">
            <xsl:apply-templates mode="block"
              select="
              gmd:identificationInfo/*/gmd:spatialResolution/gmd:MD_Resolution
              |gmd:identificationInfo/*/gmd:spatialRepresentationType
              |gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:lineage/gmd:LI_Lineage/gmd:statement
              |gmd:identificationInfo/*/gmd:resourceConstraints[1]
              "
            > </xsl:apply-templates>
          </xsl:with-param>
        </xsl:call-template>


        <span class="madeBy">
          <xsl:value-of select="/root/gui/strings/changeDate"/><xsl:value-of select="substring-before(gmd:dateStamp, 'T')"/>
        </span>

      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <xsl:template mode="block" match="gmd:identificationInfo/*/gmd:citation/gmd:CI_Citation[gmd:date]"
    priority="100">
    <xsl:call-template name="simpleElementSimpleGUI">
      <xsl:with-param name="title" select="/root/gui/schemas/iso19139/strings/temporalRef"/>
      <xsl:with-param name="content">
        <xsl:apply-templates mode="iso19139-simple" select="gmd:date/gmd:CI_Date"/>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <xsl:template mode="block" match="gmd:identificationInfo/*/gmd:resourceConstraints[1]"
    priority="100">
    <xsl:call-template name="simpleElementSimpleGUI">
      <xsl:with-param name="title" select="/root/gui/schemas/iso19139/strings/constraintInfo"/>
      <xsl:with-param name="content">
        <xsl:apply-templates mode="iso19139-simple"
          select="*|following-sibling::node()[name(.)='gmd:resourceConstraints']/*"/>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <xsl:template mode="block" match="gmd:contact|gmd:pointOfContact" priority="100">
    <xsl:call-template name="simpleElementSimpleGUI">
      <xsl:with-param name="title">
        <xsl:value-of
          select="geonet:getCodeListValue(/root/gui/schemas, 'iso19139', 'gmd:CI_RoleCode', */gmd:role/gmd:CI_RoleCode/@codeListValue)"
        />
      </xsl:with-param>
      <xsl:with-param name="content">
        <xsl:apply-templates mode="iso19139-simple"
          select="
          descendant::node()[gco:CharacterString]
          "/>
        <xsl:if test="descendant::gmx:FileName">
          <img src="{descendant::gmx:FileName/@src}" alt="logo" class="orgLogo" style="float:right;"/>
          <!-- FIXME : css -->
        </xsl:if>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <xsl:template mode="block" match="gmd:identificationInfo/*/gmd:descriptiveKeywords
    "
    priority="90">
    <xsl:call-template name="simpleElementSimpleGUI">
      <xsl:with-param name="title">
        <xsl:call-template name="getTitle">
          <xsl:with-param name="name" select="name(.)"/>
          <xsl:with-param name="schema" select="$schema"/>
        </xsl:call-template>
      </xsl:with-param>
      <xsl:with-param name="content">
        <xsl:for-each select="gmd:MD_Keywords">
          <!-- TODO multilingual -->
          <xsl:value-of select="string-join(gmd:keyword/gco:CharacterString, ', ')"/> (<xsl:value-of
            select="gmd:type/gmd:MD_KeywordTypeCode/@codeListValue"/>) </xsl:for-each>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <xsl:template mode="block" match="gmd:identificationInfo/*/gmd:language" priority="99">
    <xsl:call-template name="simpleElementSimpleGUI">
      <xsl:with-param name="title">
        <xsl:call-template name="getTitle">
          <xsl:with-param name="name" select="name(.)"/>
          <xsl:with-param name="schema" select="$schema"/>
        </xsl:call-template>
      </xsl:with-param>
      <xsl:with-param name="content">
        <xsl:call-template name="iso19139GetIsoLanguage">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="edit"   select="false()"/>
          <xsl:with-param name="value" select="gco:CharacterString|gmd:LanguageCode/@codeListValue"/>
        </xsl:call-template>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <xsl:template mode="block" match="gmd:topicCategory
    " priority="98">
    <xsl:call-template name="simpleElementSimpleGUI">
      <xsl:with-param name="title">
        <xsl:call-template name="getTitle">
          <xsl:with-param name="name" select="name(.)"/>
          <xsl:with-param name="schema" select="$schema"/>
        </xsl:call-template>
      </xsl:with-param>
      <xsl:with-param name="content">
        <xsl:value-of select="gmd:MD_TopicCategoryCode"/>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <xsl:template mode="block"
    match="gmd:identificationInfo/*/gmd:extent/gmd:EX_Extent/gmd:geographicElement" priority="99">
    <xsl:apply-templates mode="iso19139" select="gmd:EX_GeographicBoundingBox">
      <xsl:with-param name="schema" select="$schema"/>
      <xsl:with-param name="edit" select="false()"/>
    </xsl:apply-templates>
  </xsl:template>


  <xsl:template mode="block" match="gmd:graphicOverview" priority="98">
    <xsl:call-template name="simpleElementSimpleGUI">
      <xsl:with-param name="title">
        <xsl:call-template name="getTitle">
          <xsl:with-param name="name" select="name(.)"/>
          <xsl:with-param name="schema" select="$schema"/>
        </xsl:call-template>
      </xsl:with-param>
      <xsl:with-param name="content">
        <!-- FIXME template name or move to generic layout -->
        <xsl:apply-templates mode="logo"
          select=".|following-sibling::node()[name(.)='gmd:graphicOverview']"/>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>


  <xsl:template mode="block" match="*[*/@codeList]" priority="100">
    <xsl:call-template name="simpleElementSimpleGUI">
      <xsl:with-param name="title">
        <xsl:call-template name="getTitle">
          <xsl:with-param name="name" select="name(.)"/>
          <xsl:with-param name="schema" select="$schema"/>
        </xsl:call-template>
      </xsl:with-param>
      <xsl:with-param name="content">
        <xsl:apply-templates mode="iso19139GetAttributeText" select="*/@codeListValue">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="edit"   select="false()"/>
        </xsl:apply-templates>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>
  
  <xsl:template mode="block" match="*[gco:Integer]
    " priority="99">
    <xsl:call-template name="simpleElementSimpleGUI">
      <xsl:with-param name="title">
        <xsl:call-template name="getTitle">
          <xsl:with-param name="name" select="name(.)"/>
          <xsl:with-param name="schema" select="$schema"/>
        </xsl:call-template>
      </xsl:with-param>
      <xsl:with-param name="content">
        <xsl:value-of select="gco:Integer"/>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>
  <xsl:template mode="block" match="*[gco:CharacterString]
    " priority="98">
    <xsl:call-template name="simpleElementSimpleGUI">
      <xsl:with-param name="title">
        <xsl:call-template name="getTitle">
          <xsl:with-param name="name" select="name(.)"/>
          <xsl:with-param name="schema" select="$schema"/>
        </xsl:call-template>
      </xsl:with-param>
      <xsl:with-param name="content">
        <!-- TODO multilingual -->
        <xsl:value-of select="gco:CharacterString"/>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  
  <xsl:template mode="block" match="*|@*">
    <xsl:apply-templates mode="block" select="*"/>
  </xsl:template>


  <!-- List of related resources defined in the online resource section of the metadata record.
-->
  <xsl:template mode="relatedResources"
    match="gmd:distributionInfo">
    <table class="related">
      <tbody>
        <tr style="display:none;"><!-- FIXME needed by JS to append other type of relation from xml.relation service -->
          <td class="main"></td><td></td>
        </tr>
        <xsl:for-each-group select="descendant::gmd:onLine[gmd:CI_OnlineResource/gmd:linkage/gmd:URL!='']" group-by="gmd:CI_OnlineResource/gmd:protocol">
        <tr>
          <td class="main">
            <!-- Usually, protocole format is OGC:WMS-version-blahblah, remove ':' and get
            prefix of the protocol to set the CSS icon class-->
            <span class="{translate(substring-before(current-grouping-key(), '-'), ':', '')} icon">
              <xsl:value-of
                select="/root/gui/strings/protocolChoice[@value=normalize-space(current-grouping-key())]"
              />
            </span>
          </td>
          <td>
            <ul>
              <xsl:for-each select="current-group()">
                <xsl:variable name="desc">
                  <xsl:apply-templates mode="localised"
                    select="gmd:CI_OnlineResource/gmd:description">
                    <xsl:with-param name="langId" select="$langId"/>
                  </xsl:apply-templates>
                </xsl:variable>
                <li>
                  <a href="{gmd:CI_OnlineResource/gmd:linkage/gmd:URL}">
                    <xsl:choose>
                      <xsl:when test="contains(current-grouping-key(), 'OGC') or contains(current-grouping-key(), 'DOWNLOAD')">
                        <!-- Name contains layer, feature type, coverage ... -->
                        <xsl:choose>
                          <xsl:when test="normalize-space($desc)!=''">
                            <xsl:value-of select="$desc"/>
                            <xsl:if test="gmd:CI_OnlineResource/gmd:name/gmx:MimeFileType/@type">
                              (<xsl:value-of select="gmd:CI_OnlineResource/gmd:name/gmx:MimeFileType/@type"/>)
                            </xsl:if>
                          </xsl:when>
                          <xsl:when
                            test="normalize-space(gmd:CI_OnlineResource/gmd:name/gco:CharacterString)!=''">
                            <xsl:value-of select="gmd:CI_OnlineResource/gmd:name/gco:CharacterString"/>
                          </xsl:when>
                          <xsl:otherwise>
                            <xsl:value-of select="gmd:CI_OnlineResource/gmd:linkage/gmd:URL"/>
                          </xsl:otherwise>
                        </xsl:choose>
                      </xsl:when>
                      <xsl:otherwise>
                        <xsl:if test="normalize-space($desc)!=''">
                          <xsl:attribute name="title"><xsl:value-of select="$desc"/></xsl:attribute>
                        </xsl:if>
                        <xsl:choose>
                          <xsl:when
                            test="normalize-space(gmd:CI_OnlineResource/gmd:name/gco:CharacterString)!=''">
                            <xsl:value-of select="gmd:CI_OnlineResource/gmd:name/gco:CharacterString"/>
                          </xsl:when>
                          <xsl:otherwise>
                            <xsl:value-of select="gmd:CI_OnlineResource/gmd:linkage/gmd:URL"/>
                          </xsl:otherwise>
                        </xsl:choose>
                      </xsl:otherwise>
                    </xsl:choose>
                  </a>
                  
                  <!-- Display add to map action for WMS -->
                  <xsl:if test="contains(current-grouping-key(), 'WMS')">
                  &#160;
                  <a href="#" class="md-mn addLayer"
                    onclick="app.switchMode('1', true);app.getIMap().addWMSLayer([[
                              '{gmd:CI_OnlineResource/gmd:description/gco:CharacterString}', 
                              '{gmd:CI_OnlineResource/gmd:linkage/gmd:URL}', 
                              '{gmd:CI_OnlineResource/gmd:name/gco:CharacterString}', '{generate-id()}']]);">&#160;</a>
                  </xsl:if>
                </li>
              </xsl:for-each>
            </ul>
          </td>
        </tr>
      </xsl:for-each-group>
      </tbody>
    </table>
  </xsl:template>


  <!-- Extract logo -->
  <xsl:template mode="logo" match="gmd:graphicOverview">
    <xsl:variable name="fileName" select="gmd:MD_BrowseGraphic/gmd:fileName/gco:CharacterString"/>
    <xsl:variable name="url"
      select="if (contains($fileName, '://')) 
      then $fileName 
      else geonet:get-thumbnail-url($fileName, //geonet:info, /root/gui/locService)"/>

    <a href="{$url}" rel="lightbox-viewset">
      <img class="logo" src="{$url}" alt="thumbnail"
        title="{gmd:MD_BrowseGraphic/gmd:fileDescription/gco:CharacterString}"/>
    </a>
  </xsl:template>


  <!-- Hide them -->
  <xsl:template mode="iso19139-simple" match="
    geonet:*|*[@gco:nilReason='missing']|@gco:isoType" priority="99"/>
  <!-- Don't display -->
  
  <!-- these elements should be boxed -->
  <xsl:template mode="iso19139-simple"
    match="gmd:identificationInfo|gmd:distributionInfo
    |gmd:descriptiveKeywords|gmd:thesaurusName
    |gmd:spatialRepresentationInfo
    |gmd:pointOfContact|gmd:contact
    |gmd:dataQualityInfo
    |gmd:MD_Constraints|gmd:MD_LegalConstraints|gmd:MD_SecurityConstraints
    |gmd:referenceSystemInfo|gmd:equivalentScale|gmd:projection|gmd:ellipsoid
    |gmd:extent[name(..)!='gmd:EX_TemporalExtent']|gmd:geographicBox|gmd:EX_TemporalExtent
    |gmd:MD_Distributor
    |srv:containsOperations|srv:SV_CoupledResource|gmd:metadataConstraints"
    priority="2">

    <xsl:call-template name="complexElement">
      <xsl:with-param name="id" select="generate-id(.)"/>
      <xsl:with-param name="title">
        <xsl:call-template name="getTitle">
          <xsl:with-param name="name" select="name(.)"/>
          <xsl:with-param name="schema" select="$schema"/>
        </xsl:call-template>
      </xsl:with-param>
      <xsl:with-param name="help"/>
      <xsl:with-param name="content">
        <xsl:apply-templates mode="iso19139-simple" select="@*|*">
          <xsl:with-param name="schema" select="$schema"/>
        </xsl:apply-templates>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>


  <xsl:template mode="iso19139-simple"
    match="
    gmd:*[gco:Date|gco:DateTime|gco:Integer|gco:Decimal|gco:Boolean|gco:Real|gco:Measure|gco:Length|gco:Distance|gco:Angle|gco:Scale|gco:RecordType|gmx:MimeFileType]|
    srv:*[gco:Date|gco:DateTime|gco:Integer|gco:Decimal|gco:Boolean|gco:Real|gco:Measure|gco:Length|gco:Distance|gco:Angle|gco:Scale|gco:RecordType|gmx:MimeFileType]"
    priority="2">
    
    <xsl:call-template name="simpleElement">
      <xsl:with-param name="id" select="generate-id(.)"/>
      <xsl:with-param name="title">
        <xsl:call-template name="getTitle">
          <xsl:with-param name="name" select="name(.)"/>
          <xsl:with-param name="schema" select="$schema"/>
        </xsl:call-template>
      </xsl:with-param>
      <xsl:with-param name="help"></xsl:with-param>
      <xsl:with-param name="content">
       <xsl:value-of
          select="gco:Date|gco:DateTime|gco:Integer|gco:Decimal|gco:Boolean|gco:Real|gco:Measure|gco:Length|gco:Distance|gco:Angle|gco:Scale|gco:RecordType|gmx:MimeFileType"
        />
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>


  <!-- gco:CharacterString are swallowed -->
  <!-- TODO : PT_FreeText -->
  <xsl:template mode="iso19139-simple" match="*[gco:CharacterString]" priority="2">

    <xsl:call-template name="simpleElement">
      <xsl:with-param name="id" select="generate-id(.)"/>
      <xsl:with-param name="title">
        <xsl:call-template name="getTitle">
          <xsl:with-param name="name" select="name(.)"/>
          <xsl:with-param name="schema" select="$schema"/>
        </xsl:call-template>
      </xsl:with-param>
      <xsl:with-param name="help"></xsl:with-param>
      <xsl:with-param name="content">
        <xsl:value-of select="gco:CharacterString"/>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <xsl:template mode="iso19139-simple" match="gmd:*[*/@codeList]|srv:*[*/@codeList]">
    
    <xsl:call-template name="simpleElement">
      <xsl:with-param name="id" select="generate-id(.)"/>
      <xsl:with-param name="title">
        <xsl:call-template name="getTitle">
          <xsl:with-param name="name" select="name(.)"/>
          <xsl:with-param name="schema" select="$schema"/>
        </xsl:call-template>
      </xsl:with-param>
      <xsl:with-param name="help"></xsl:with-param>
      <xsl:with-param name="content">
        <xsl:apply-templates mode="iso19139GetAttributeText" select="*/@codeListValue">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="edit"   select="false()"/>
        </xsl:apply-templates>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>



  <!-- All others
   -->
  <xsl:template mode="iso19139-simple" match="*|@*">
    <xsl:call-template name="simpleElement">
      <xsl:with-param name="id" select="generate-id(.)"/>
      <xsl:with-param name="title">
        <xsl:call-template name="getTitle">
          <xsl:with-param name="name" select="name(.)"/>
          <xsl:with-param name="schema" select="$schema"/>
        </xsl:call-template>
      </xsl:with-param>
      <xsl:with-param name="help"></xsl:with-param>
      <xsl:with-param name="content">
        <xsl:variable name="empty">
          <xsl:apply-templates mode="iso19139IsEmpty" select="."/>
        </xsl:variable>

        <xsl:if test="$empty!=''">
          <xsl:apply-templates mode="iso19139-simple" select="*|@*"/>
        </xsl:if>
      </xsl:with-param>
    </xsl:call-template>

  </xsl:template>

</xsl:stylesheet>
