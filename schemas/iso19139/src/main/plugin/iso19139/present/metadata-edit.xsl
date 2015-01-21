<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl ="http://www.w3.org/1999/XSL/Transform"
  xmlns:gmd="http://www.isotc211.org/2005/gmd"
  xmlns:gts="http://www.isotc211.org/2005/gts"
  xmlns:gco="http://www.isotc211.org/2005/gco"
  xmlns:gmx="http://www.isotc211.org/2005/gmx"
  xmlns:srv="http://www.isotc211.org/2005/srv"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xmlns:gml="http://www.opengis.net/gml"
  xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns:geonet="http://www.fao.org/geonetwork"
  xmlns:exslt="http://exslt.org/common"
  exclude-result-prefixes="#all">

  <xsl:include href="metadata-utils.xsl"/>
  <xsl:include href="metadata-geo.xsl"/>
  <xsl:include href="metadata-inspire.xsl"/>
  <xsl:include href="metadata-view.xsl"/>

  <!-- Use this mode on the root element to add hidden fields to the editor -->
  <xsl:template mode="schema-hidden-fields" match="gmd:MD_Metadata|*[@gco:isoType='gmd:MD_Metadata']" priority="2">
    <!-- The GetCapabilities URL -->
    <xsl:variable name="capabilitiesUrl">
      <xsl:call-template name="getServiceURL">
        <xsl:with-param name="metadata" select="."/>
      </xsl:call-template>
    </xsl:variable>
    <input type="hidden" id="serviceUrl" value="{$capabilitiesUrl}"/>
    
  </xsl:template>


  <!-- main template - the way into processing iso19139 -->
  <xsl:template name="metadata-iso19139">
    <xsl:param name="schema"/>
    <xsl:param name="edit" select="false()"/>
    <xsl:param name="embedded"/>

    <xsl:apply-templates mode="iso19139" select="." >
      <xsl:with-param name="schema" select="$schema"/>
      <xsl:with-param name="edit"   select="$edit"/>
      <xsl:with-param name="embedded" select="$embedded" />
    </xsl:apply-templates>
  </xsl:template>

  <!-- =================================================================== -->
  <!-- default: in simple mode just a flat list -->
  <!-- =================================================================== -->

  <xsl:template mode="iso19139" match="*|@*">
    <xsl:param name="schema"/>
    <xsl:param name="edit"/>
    
    <!-- do not show empty elements in view mode -->
    <xsl:choose>
      <xsl:when test="$edit=true()">
        <xsl:apply-templates mode="element" select=".">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="edit"   select="true()"/>
          <xsl:with-param name="flat"   select="/root/gui/config/metadata-tab/*[name(.)=$currTab]/@flat"/>
        </xsl:apply-templates>
      </xsl:when>
      
      <xsl:otherwise>
        <xsl:variable name="empty">
          <xsl:apply-templates mode="iso19139IsEmpty" select="."/>
        </xsl:variable>
        
        <xsl:if test="$empty!=''">
          <xsl:apply-templates mode="element" select=".">
            <xsl:with-param name="schema" select="$schema"/>
            <xsl:with-param name="edit"   select="false()"/>
            <xsl:with-param name="flat"   select="/root/gui/config/metadata-tab/*[name(.)=$currTab]/@flat"/>
          </xsl:apply-templates>
        </xsl:if>
        
      </xsl:otherwise>
    </xsl:choose>
      
  </xsl:template>
  
  
  <!--=====================================================================-->
  <!-- these elements should not be displayed 
    * do not display graphicOverview managed by GeoNetwork (ie. having a 
    fileDescription set to thumbnail or large_thumbnail). Those thumbnails
    are managed in then thumbnail popup. Others could be valid URL pointing to
    an image available on the Internet.
  -->
  <!--=====================================================================
  
  <xsl:template mode="iso19139"
    match="gmd:graphicOverview[gmd:MD_BrowseGraphic/gmd:fileDescription/gco:CharacterString='thumbnail' or gmd:MD_BrowseGraphic/gmd:fileDescription/gco:CharacterString='large_thumbnail']"
    priority="20" />-->
  
  
  <!-- ===================================================================== -->
  <!-- these elements should be boxed -->
  <!-- ===================================================================== -->

  <xsl:template mode="iso19139" match="gmd:identificationInfo|gmd:distributionInfo|
              gmd:portrayalCatalogueInfo|gmd:portrayalCatalogueCitation|
              gmd:descriptiveKeywords|gmd:thesaurusName|
              *[name(..)='gmd:resourceConstraints']|gmd:spatialRepresentationInfo|gmd:pointOfContact|
              gmd:dataQualityInfo|gmd:contentInfo|gmd:distributionFormat|
              gmd:referenceSystemInfo|gmd:spatialResolution|gmd:offLine|gmd:projection|gmd:ellipsoid|gmd:extent[name(..)!='gmd:EX_TemporalExtent']|gmd:attributes|gmd:verticalCRS|
              gmd:geographicBox|gmd:EX_TemporalExtent|gmd:MD_Distributor|
              srv:containsOperations|srv:SV_CoupledResource|
              gmd:metadataConstraints|gmd:aggregationInfo|gmd:report/*|gmd:result/*|
              gmd:processStep|gmd:lineage">
    <xsl:param name="schema"/>
    <xsl:param name="edit"/>
    
    <xsl:apply-templates mode="complexElement" select=".">
      <xsl:with-param name="schema" select="$schema"/>
      <xsl:with-param name="edit"   select="$edit"/>
    </xsl:apply-templates>
  </xsl:template>
  
  <!-- ===================================================================== -->
  <!-- some gco: elements and gmx:MimeFileType are swallowed -->
  <!-- ===================================================================== -->

  <xsl:template mode="iso19139" match="gmd:*[gco:Date|gco:DateTime|gco:Integer|gco:Decimal|gco:Boolean|gco:Real|gco:Measure|gco:Length|gco:Distance|gco:Angle|gco:Scale|gco:RecordType|gmx:MimeFileType]|
                  srv:*[gco:Date|gco:DateTime|gco:Integer|gco:Decimal|gco:Boolean|gco:Real|gco:Measure|gco:Length|gco:Distance|gco:Angle|gco:Scale|gco:RecordType|gmx:MimeFileType]">
    <xsl:param name="schema"/>
    <xsl:param name="edit"/>
    
    <xsl:call-template name="iso19139String">
      <xsl:with-param name="schema" select="$schema"/>
      <xsl:with-param name="edit"   select="$edit"/>
    </xsl:call-template>
  </xsl:template>
  
  <!-- ==================================================================== -->

  <!-- Distance widget with value + uom attribute in one line.
  Suggestion (from label files) update the value element. 
  -->
  <xsl:template mode="iso19139" match="gmd:distance" priority="2">
    <xsl:param name="schema"/>
    <xsl:param name="edit"/>
    
    <xsl:choose>
      <xsl:when test="$edit=true()">
        <xsl:variable name="text">
          <xsl:variable name="ref" select="gco:Distance/geonet:element/@ref"/>
          
          <!-- Look for the helper to check if a radio edit mode is activated
            If yes, hide the input text which will be updated when clicking the radio
            or the other option. -->
          <xsl:variable name="helper" select="geonet:getHelper($schema, ., /root/gui)"/>
          
          <div>
            <xsl:if test="contains($helper/@editorMode, 'radio')">
              <xsl:attribute name="style">display:none;</xsl:attribute>
            </xsl:if>
            <input type="number" class="md" name="_{$ref}" id="_{$ref}"  
              onkeyup="validateNumber(this,true,true);"
              onchange="validateNumber(this,true,true);"
              value="{gco:Distance}" size="30">
            </input>
            
            &#160;
            <xsl:value-of select="/root/gui/schemas/iso19139/labels/element[@name = 'uom']/label"/>
            &#160;
            <input type="text" class="md" name="_{$ref}_uom" id="_{$ref}_uom"  
              value="{gco:Distance/@uom}" style="width:30px;"/>
          </div>
          <xsl:for-each select="gco:Distance">
            <xsl:call-template name="helper">
              <xsl:with-param name="schema" select="$schema"/>
              <xsl:with-param name="attribute" select="false()"/>
            </xsl:call-template>
          </xsl:for-each>
          
        </xsl:variable>
        
        <xsl:apply-templates mode="simpleElement" select=".">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="edit"   select="true()"/>
          <xsl:with-param name="text"   select="$text"/>
        </xsl:apply-templates>
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates mode="simpleElement" select=".">
          <xsl:with-param name="schema"  select="$schema"/>
          <xsl:with-param name="text">
            <xsl:value-of select="gco:Distance"/>
            <xsl:if test="gco:Distance/@uom"><xsl:text>&#160;</xsl:text>
              <xsl:choose>
                <xsl:when test="contains(gco:Distance/@uom, '#')">
                  <a href="{gco:Distance/@uom}"><xsl:value-of select="substring-after(gco:Distance/@uom, '#')"/></a>
                </xsl:when>
                <xsl:otherwise><xsl:value-of select="gco:Distance/@uom"/></xsl:otherwise>
              </xsl:choose>
            </xsl:if>
          </xsl:with-param>
        </xsl:apply-templates>
      </xsl:otherwise>
    </xsl:choose>
    
  </xsl:template>
  
  <!-- ==================================================================== -->

  <!--
    OperatesOn element display or edit attribute uuidref. In edit mode
    the metadata selection panel is provided to set the uuid.
    In view mode, the title of the metadata is displayed.
    
    Note: it could happen that linked metadata record is not accessible
    to current user. In such a situation, clicking the link will return
    a privileges exception.
    -->
  <xsl:template mode="iso19139" match="srv:operatesOn|gmd:featureCatalogueCitation|gmd:source[name(parent::node())='gmd:LI_ProcessStep' or name(parent::node())='gmd:LI_Lineage']" priority="99">
    <xsl:param name="schema"/>
    <xsl:param name="edit"/>
    <xsl:variable name="text">
      
      
      <xsl:choose>
        <xsl:when test="$edit=true()">
          <xsl:variable name="ref" select="geonet:element/@ref"/>
          <xsl:variable name="typeOfLink" select="if (local-name(.)='featureCatalogueCitation') then 'iso19110' else 'uuidref'"/>
          
          <input type="text" name="_{$ref}_uuidref" id="_{$ref}_uuidref" value="{./@uuidref}" size="20"
            onfocus="javascript:Ext.getCmp('editorPanel').showLinkedMetadataSelectionPanel('{$ref}', 'uuidref', '{$typeOfLink}');"/>
          <img src="../../images/find.png" alt="{/root/gui/strings/search}" title="{/root/gui/strings/search}"
            onclick="javascript:Ext.getCmp('editorPanel').showLinkedMetadataSelectionPanel('{$ref}', 'uuidref', '{$typeOfLink}');" 
            onmouseover="this.style.cursor='pointer';"/>
        </xsl:when>
        <xsl:otherwise>
          <a href="#" onclick="javascript:catalogue.metadataShow('{@uuidref}');return false;">
            <xsl:call-template name="getMetadataTitle">
              <xsl:with-param name="uuid" select="@uuidref"/>
            </xsl:call-template>
          </a>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:apply-templates mode="simpleElement" select=".">
      <xsl:with-param name="schema" select="$schema"/>
      <xsl:with-param name="edit"   select="$edit"/>
      <xsl:with-param name="text"   select="$text"/>
      <xsl:with-param name="editAttributes" select="false()"/>
    </xsl:apply-templates>
<!--<xsl:apply-templates mode="iso19139" select="*">
      <xsl:with-param name="schema" select="$schema"/>
      <xsl:with-param name="edit"   select="$edit"/>
    </xsl:apply-templates>-->
  </xsl:template>



    <!-- ============================================================================= -->
    <!--   
   Display a list of related resources to which the current service metadata operatesOn.
   
     Ie. User should define related metadata record using operatesOn elements and then if
     needed, set a coupledResource to create a link to the data itself
      (using layer name/feature type/coverage name as described in capabilities documents). 
     
     To create a relation best is using the related resources panel (see relatedResources 
     template in metadata-iso19139-utils.xsl).
      -->
    <xsl:template mode="iso19139" match="srv:coupledResource/srv:SV_CoupledResource/srv:identifier" priority="200">
        <xsl:param name="schema"/>
        <xsl:param name="edit"/>

        <xsl:choose>
            <xsl:when test="$edit=true()">
                <xsl:variable name="text">
                    <xsl:variable name="ref" select="gco:CharacterString/geonet:element/@ref"/>
                     <xsl:variable name="currentUuid" select="gco:CharacterString/text()"/>
                    <input type="text" class="md" name="_{$ref}" id="_{$ref}" onchange="validateNonEmpty(this)" value="{$currentUuid}" size="30"/>
                    <xsl:choose>
                        <xsl:when test="count(//srv:operatesOn[@uuidref!=''])=0">
                            <xsl:value-of select="/root/gui/strings/noOperatesOn"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <select onchange="javascript:Ext.getDom('_{$ref}').value=this.options[this.selectedIndex].value;" class="md">
                                <option></option>
                                <xsl:for-each select="//srv:operatesOn[@uuidref!='']">
                                    <option value="{@uuidref}">
                                      <xsl:if test="@uuidref = $currentUuid">
                                        <xsl:attribute name="selected">selected</xsl:attribute>
                                      </xsl:if>
                                        <xsl:call-template name="getMetadataTitle">
                                            <xsl:with-param name="uuid" select="@uuidref"/>
                                        </xsl:call-template>
                                    </option>
                                </xsl:for-each>
                            </select>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>

                <xsl:apply-templates mode="simpleElement" select=".">
                    <xsl:with-param name="schema" select="$schema"/>
                    <xsl:with-param name="edit"   select="true()"/>
                    <xsl:with-param name="text"   select="$text"/>
                </xsl:apply-templates>
            </xsl:when>
            <xsl:otherwise>
                <xsl:apply-templates mode="simpleElement" select=".">
                    <xsl:with-param name="schema"  select="$schema"/>
                    <xsl:with-param name="text">
                      <a href="#" onclick="javascript:catalogue.metadataShow('{gco:CharacterString}');return false;">
                        <xsl:call-template name="getMetadataTitle">
                          <xsl:with-param name="uuid" select="gco:CharacterString"/>
                        </xsl:call-template>
                      </a>
                    </xsl:with-param>
                </xsl:apply-templates>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>




  <!--
    Create widget to handle editing of xsd:duration elements.
    
    Format: PnYnMnDTnHnMnS
    
    *  P indicates the period (required)
    * nY indicates the number of years
    * nM indicates the number of months
    * nD indicates the number of days
    * T indicates the start of a time section (required if you are going to specify hours, minutes, or seconds)
    * nH indicates the number of hours
    * nM indicates the number of minutes
    * nS indicates the number of seconds
    
    TODO : onload, we should run validateNumber handler in order to change 
    input class when needed.
    
  -->
  <xsl:template mode="iso19139" match="gts:TM_PeriodDuration|gml:duration" priority="100">
    <xsl:param name="schema" />
    <xsl:param name="edit" />
    <xsl:param name="title">
        <xsl:call-template name="getTitle">
            <xsl:with-param name="name" select="name(.)"/>
            <xsl:with-param name="schema" select="$schema"/>
        </xsl:call-template>
    </xsl:param>   
 
    <!--Set default value -->
    <xsl:variable name="p">
      <xsl:choose>
        <xsl:when test=".=''">P0Y0M0DT0H0M0S</xsl:when>
        <xsl:otherwise><xsl:value-of select="."/></xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    
    <!-- Extract fragment -->
    <xsl:variable name="NEG">
      <xsl:choose>
        <xsl:when test="starts-with($p, '-')">true</xsl:when>
        <xsl:otherwise></xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="Y" select="substring-before(substring-after($p, 'P'), 'Y')"/>
    <xsl:variable name="M" select="substring-before(substring-after($p, 'Y'), 'M')"/>
    <xsl:variable name="D" select="substring-before(substring-after($p, 'M'), 'DT')"/>
    <xsl:variable name="H" select="substring-before(substring-after($p, 'DT'), 'H')"/>
    <xsl:variable name="MI" select="substring-before(substring-after($p, 'H'), 'M')"/>
    <xsl:variable name="S" select="substring-before(substring-after(substring-after($p,'M' ),'M' ), 'S')"/>
    
    <xsl:variable name="text">
      <xsl:choose>
        <xsl:when test="$edit=true()">
          <xsl:variable name="ref" select="geonet:element/@ref"/>
          
          <input type="checkbox" id="N{$ref}" onchange="buildDuration('{$ref}');">
            <xsl:if test="$NEG!=''"><xsl:attribute name="checked">checked</xsl:attribute></xsl:if>
          </input>
          <label for="N{$ref}"><xsl:value-of select="/root/gui/strings/durationSign"/></label><br/>
          <xsl:value-of select="/root/gui/strings/durationNbYears"/><input type="text" id="Y{$ref}" class="small" value="{substring-before(substring-after($p, 'P'), 'Y')}" size="4" onchange="buildDuration('{$ref}');" onkeyup="validateNumber(this,true,false);"/>-
          <xsl:value-of select="/root/gui/strings/durationNbMonths"/><input type="text" id="M{$ref}" class="small" value="{substring-before(substring-after($p, 'Y'), 'M')}" size="4" onchange="buildDuration('{$ref}');" onkeyup="validateNumber(this,true,false);"/>-
          <xsl:value-of select="/root/gui/strings/durationNbDays"/><input type="text" id="D{$ref}" class="small" value="{substring-before(substring-after($p, 'M'), 'DT')}" size="4" onchange="buildDuration('{$ref}');" onkeyup="validateNumber(this,true,false);"/><br/>
          <xsl:value-of select="/root/gui/strings/durationNbHours"/><input type="text" id="H{$ref}" class="small" value="{substring-before(substring-after($p, 'DT'), 'H')}" size="4" onchange="buildDuration('{$ref}');" onkeyup="validateNumber(this,true,false);"/>-
          <xsl:value-of select="/root/gui/strings/durationNbMinutes"/><input type="text" id="MI{$ref}" class="small" value="{substring-before(substring-after($p, 'H'), 'M')}" size="4" onchange="buildDuration('{$ref}');" onkeyup="validateNumber(this,true,false);"/>-
          <xsl:value-of select="/root/gui/strings/durationNbSeconds"/><input type="text" id="S{$ref}" class="small" value="{substring-before(substring-after(substring-after($p,'M' ),'M' ), 'S')}" size="4" onchange="buildDuration('{$ref}');" onkeyup="validateNumber(this,true,true);"/><br/>
          <input type="hidden" name="_{$ref}" id="_{$ref}" value="{$p}" size="20"/><br/>
          
        </xsl:when>
        <xsl:otherwise>
          <xsl:if test="$NEG!=''">-</xsl:if><xsl:text> </xsl:text>
          <xsl:value-of select="$Y"/><xsl:text> </xsl:text><xsl:value-of select="/root/gui/strings/durationYears"/><xsl:text>  </xsl:text>
          <xsl:value-of select="$M"/><xsl:text> </xsl:text><xsl:value-of select="/root/gui/strings/durationMonths"/><xsl:text>  </xsl:text>
          <xsl:value-of select="$D"/><xsl:text> </xsl:text><xsl:value-of select="/root/gui/strings/durationDays"/><xsl:text> / </xsl:text>
          <xsl:value-of select="$H"/><xsl:text> </xsl:text><xsl:value-of select="/root/gui/strings/durationHours"/><xsl:text>  </xsl:text>
          <xsl:value-of select="$MI"/><xsl:text> </xsl:text><xsl:value-of select="/root/gui/strings/durationMinutes"/><xsl:text>  </xsl:text>
          <xsl:value-of select="$S"/><xsl:text> </xsl:text><xsl:value-of select="/root/gui/strings/durationSeconds"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:apply-templates mode="simpleElement" select=".">
      <xsl:with-param name="schema" select="$schema"/>
      <xsl:with-param name="edit"   select="$edit"/>
      <xsl:with-param name="title"   select="$title"/>
      <xsl:with-param name="text"   select="$text"/>
    </xsl:apply-templates>
  </xsl:template>

    <!-- ==================================================================== -->

  <xsl:template name="translatedString">
      <xsl:param name="schema"/>
      <xsl:param name="langId" />
      <xsl:param name="edit" select="false()"/>
      <xsl:param name="validator" />
          <xsl:choose>
              <xsl:when test="not(gco:*)">
                  <xsl:for-each select="gmd:PT_FreeText">
                      <xsl:call-template name="getElementText">
                          <xsl:with-param name="edit" select="$edit" />
                          <xsl:with-param name="schema" select="$schema" />
                          <xsl:with-param name="langId" select="$langId" />
                          <xsl:with-param name="validator" select="$validator" />
                        </xsl:call-template>
                    </xsl:for-each>
                </xsl:when>
              <xsl:otherwise>
                  <xsl:for-each select="gco:*">
                      <xsl:call-template name="getElementText">
                            <xsl:with-param name="edit" select="$edit" />
                            <xsl:with-param name="schema" select="$schema" />
                            <xsl:with-param name="langId" select="$langId" />
                            <xsl:with-param name="validator" select="$validator" />
                        </xsl:call-template>
                    </xsl:for-each>
                </xsl:otherwise>
            </xsl:choose>
    </xsl:template>

  <xsl:template name="iso19139String">
    <xsl:param name="schema"/>
    <xsl:param name="edit"/>
    <xsl:param name="class" select="''"/>
    <xsl:param name="langId" />
    <xsl:param name="widget" />
    <xsl:param name="validator" />

    <xsl:variable name="title">
      <xsl:call-template name="getTitle">
        <xsl:with-param name="name"   select="name(.)"/>
        <xsl:with-param name="schema" select="$schema"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="helpLink">
      <xsl:call-template name="getHelpLink">
        <xsl:with-param name="name"   select="name(.)"/>
        <xsl:with-param name="schema" select="$schema"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="text">
      <xsl:choose>
        <xsl:when test="not($edit=true() and $widget)">
          <!-- Having only gmd:PT_FreeText is allowed by schema.
            So using a PT_FreeText to set a translation even
            in main metadata language could be valid.-->
          <xsl:call-template name="translatedString">
            <xsl:with-param name="schema" select="$schema"/>
            <xsl:with-param name="edit" select="$edit"/>
            <xsl:with-param name="langId" select="$langId" />
            <xsl:with-param name="validator" select="$validator"/>
          </xsl:call-template>
        </xsl:when>
        <xsl:otherwise>
          <xsl:copy-of select="$widget" />
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="attrs">
      <xsl:for-each select="gco:*/@*">
        <xsl:value-of select="name(.)"/>
      </xsl:for-each>
    </xsl:variable>


    <xsl:choose>
    <xsl:when test="normalize-space($attrs)!=''">
      <xsl:apply-templates mode="complexElement" select=".">
        <xsl:with-param name="schema"   select="$schema"/>
        <xsl:with-param name="edit"     select="$edit"/>
        <xsl:with-param name="title"    select="$title"/>
        <xsl:with-param name="helpLink" select="$helpLink"/>
        <xsl:with-param name="content">

        <!-- existing attributes -->
        <xsl:for-each select="gco:*/@*">
          <xsl:apply-templates mode="simpleElement" select=".">
            <xsl:with-param name="schema" select="$schema"/>
            <xsl:with-param name="edit"   select="$edit"/>
          </xsl:apply-templates>
        </xsl:for-each>

        <!-- existing content -->
        <xsl:apply-templates mode="simpleElement" select=".">
          <xsl:with-param name="schema"   select="$schema"/>
          <xsl:with-param name="edit"     select="$edit"/>
          <xsl:with-param name="title"    select="$title"/>
          <xsl:with-param name="helpLink" select="$helpLink"/>
          <xsl:with-param name="text"     select="$text"/>
        </xsl:apply-templates>
        </xsl:with-param>
      </xsl:apply-templates>
    </xsl:when>
    <xsl:otherwise>
      <xsl:apply-templates mode="simpleElement" select=".">
        <xsl:with-param name="schema"   select="$schema"/>
        <xsl:with-param name="edit"     select="$edit"/>
        <xsl:with-param name="title"    select="$title"/>
        <xsl:with-param name="helpLink" select="$helpLink"/>
        <xsl:with-param name="text"     select="$text"/>
      </xsl:apply-templates>
    </xsl:otherwise>
    </xsl:choose>

  </xsl:template>

  <!-- ==================================================================== -->

  <xsl:template mode="iso19139" match="gco:ScopedName|gco:LocalName">
    <xsl:param name="schema"/>
    <xsl:param name="edit"/>

    <xsl:variable name="text">
      <xsl:call-template name="getElementText">
        <xsl:with-param name="edit"   select="$edit"/>
        <xsl:with-param name="schema" select="$schema"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:apply-templates mode="simpleElement" select=".">
      <xsl:with-param name="schema"   select="$schema"/>
      <xsl:with-param name="edit"     select="$edit"/>
      <xsl:with-param name="text"     select="$text"/>
    </xsl:apply-templates>
  </xsl:template>


  <!-- GML time interval -->
  <xsl:template mode="iso19139" match="gml:timeInterval">
    <xsl:param name="schema"/>
    <xsl:param name="edit"/>
    
    <xsl:choose>
      <xsl:when test="$edit">
        <xsl:apply-templates mode="simpleElement" select=".">
          <xsl:with-param name="schema"   select="$schema"/>
          <xsl:with-param name="edit"     select="$edit"/>
          <xsl:with-param name="title"    select="/root/gui/schemas/iso19139/labels/element[@name='gml:timeInterval']/label"/>
        </xsl:apply-templates>
      </xsl:when>
      <xsl:otherwise>
        <xsl:variable name="text">
          <xsl:choose>
            <xsl:when test="@radix and @factor"><xsl:value-of select=". * @factor div @radix"/>&#160;<xsl:value-of select="@unit"/></xsl:when>
            <xsl:when test="@factor"><xsl:value-of select=". * @factor"/>&#160;<xsl:value-of select="@unit"/></xsl:when>
            <xsl:when test="@radix"><xsl:value-of select=". div @radix"/>&#160;<xsl:value-of select="@unit"/></xsl:when>
            <xsl:otherwise><xsl:value-of select="."/>&#160;<xsl:value-of select="@unit"/></xsl:otherwise>
          </xsl:choose>
        </xsl:variable>
        <xsl:apply-templates mode="simpleElement" select=".">
          <xsl:with-param name="schema"   select="$schema"/>
          <xsl:with-param name="edit"     select="$edit"/>
          <xsl:with-param name="title"    select="/root/gui/schemas/iso19139/labels/element[@name='gml:timeInterval']/label"/>
          <xsl:with-param name="text"     select="$text"/>
        </xsl:apply-templates>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <!-- Display element attributes only in edit mode 
    * GML time interval 
  -->
  <xsl:template mode="simpleAttribute" match="gml:timeInterval/@*" priority="99">
    <xsl:param name="schema"/>
    <xsl:param name="edit"/>
    
    <xsl:choose>
      <xsl:when test="$edit">
        <xsl:call-template name="simpleAttribute">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="edit" select="$edit"/>
        </xsl:call-template>
      </xsl:when>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template mode="simpleAttribute" match="@xsi:type" priority="99"/>

  <!-- ================================================================= -->
  <!-- some elements that have both attributes and content               -->
  <!-- ================================================================= -->

  <xsl:template mode="iso19139" match="gml:identifier|gml:axisDirection|gml:descriptionReference">
    <xsl:param name="schema"/>
    <xsl:param name="edit"/>

    <xsl:apply-templates mode="complexElement" select=".">
      <xsl:with-param name="schema"   select="$schema"/>
      <xsl:with-param name="edit"     select="$edit"/>
      <xsl:with-param name="content">
    
        <!-- existing attributes -->
        <xsl:apply-templates mode="simpleElement" select="@*">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="edit"   select="$edit"/>
        </xsl:apply-templates>
    
        <!-- existing content -->
        <xsl:apply-templates mode="simpleElement" select=".">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="edit"   select="$edit"/>
        </xsl:apply-templates>

      </xsl:with-param>
    </xsl:apply-templates>
  </xsl:template>


  <!-- gmx:FileName could be used as substitution of any
    gco:CharacterString. To turn this on add a schema 
    suggestion.
    -->
  <xsl:template mode="iso19139" name="file-upload" match="*[gmx:FileName]">
    <xsl:param name="schema"/>
    <xsl:param name="edit"/>
    
    <xsl:variable name="src" select="gmx:FileName/@src"/>
    
    <xsl:call-template name="file-or-logo-upload">
      <xsl:with-param name="schema" select="$schema"/>
      <xsl:with-param name="edit" select="$edit"/>
      <xsl:with-param name="ref" select="gmx:FileName/geonet:element/@ref"/>
      <xsl:with-param name="value" select="gmx:FileName"/>
      <xsl:with-param name="src" select="$src"/>
      <xsl:with-param name="delButton" select="normalize-space(gmx:FileName)!=''"/>
      <xsl:with-param name="setButton" select="normalize-space(gmx:FileName)=''"/>
      <xsl:with-param name="visible" select="false()"/>
      <xsl:with-param name="action" select="concat('Ext.getCmp(', $apos, 'editorPanel', $apos, 
        ').showFileUploadPanel(', //geonet:info/id, ', ', $apos, gmx:FileName/geonet:element/@ref, $apos, ');')"/>
    </xsl:call-template>
  </xsl:template>
 
 	<!-- gmx:Anchor is a substitute of gco:CharacterString and 
	     could be use to create a hyperlink for an element.
	   -->
	   <xsl:template mode="iso19139"
	     match="*[gmx:Anchor]" priority="99">
	     <xsl:param name="schema" />
	     <xsl:param name="edit" />
	     
	     <xsl:apply-templates mode="complexElement" select=".">
	       <xsl:with-param name="schema"   select="$schema"/>
	       <xsl:with-param name="edit"     select="$edit"/>
	       <xsl:with-param name="content">
	         <xsl:choose>
	           <xsl:when test="$edit=true()">
	             <!-- existing content -->
	             <xsl:apply-templates mode="simpleElement" select="gmx:Anchor/.">
	               <xsl:with-param name="schema" select="$schema"/>
	               <xsl:with-param name="edit"   select="$edit"/>
	             </xsl:apply-templates>
	           </xsl:when>
	           <xsl:otherwise>
	             <a href="{gmx:Anchor/@xlink:href}"><xsl:value-of select="gmx:Anchor"/></a>    
	           </xsl:otherwise> 
	         </xsl:choose>
	       </xsl:with-param>  
	     </xsl:apply-templates>
	   </xsl:template>
  
  <!-- Add exception to update-fixed-info to avoid URL creation for downloadable resources -->
  <xsl:template mode="iso19139" match="gmd:contactInstructions[gmx:FileName]" priority="2">
    <xsl:param name="schema"/>
    <xsl:param name="edit"/>
    
    <xsl:call-template name="file-or-logo-upload">
      <xsl:with-param name="schema" select="$schema"/>
      <xsl:with-param name="edit" select="$edit"/>
      <xsl:with-param name="ref" select="gmx:FileName/geonet:element/@ref"/>
      <xsl:with-param name="value" select="gmx:FileName"/>
      <xsl:with-param name="src" select="gmx:FileName/@src"/>
      <xsl:with-param name="action" select="concat('Ext.getCmp(', $apos ,'editorPanel', $apos, ').showLogoSelectionPanel(', $apos, '_', 
        gmx:FileName/geonet:element/@ref, '_src', $apos, ');')"/>
      <xsl:with-param name="delButton" select="false()"/>
      <xsl:with-param name="setButton" select="true()"/>
      <xsl:with-param name="visible" select="true()"/>
      <xsl:with-param name="setButtonLabel" select="/root/gui/strings/chooseLogo"/>
      <xsl:with-param name="label" select="/root/gui/strings/orgLogo"/>
    </xsl:call-template>
  </xsl:template>
  
  <xsl:template name="file-or-logo-upload">
    <xsl:param name="schema"/>
    <xsl:param name="edit"/>
    <xsl:param name="ref"/>
    <xsl:param name="value"/>
    <xsl:param name="src"/>
    <xsl:param name="action"/>
    <xsl:param name="delButton" select="normalize-space($value)!=''"/>
    <xsl:param name="setButton" select="normalize-space($value)!=''"/>
    <xsl:param name="visible" select="not($setButton)"/>
    <xsl:param name="setButtonLabel" select="/root/gui/strings/insertFileMode"/>
    <xsl:param name="label" select="/root/gui/strings/file"/>
    
    
    <xsl:apply-templates mode="complexElement" select=".">
      <xsl:with-param name="schema"   select="$schema"/>
      <xsl:with-param name="edit"     select="$edit"/>
      <xsl:with-param name="content">
        
        <xsl:choose>
          <xsl:when test="$edit">
            <xsl:variable name="id" select="generate-id(.)"/>
            <xsl:variable name="isXLinked" select="count(ancestor-or-self::node()[@xlink:href]) > 0" />
            
            <div id="{$id}"/>
            
            <xsl:call-template name="simpleElementGui">
              <xsl:with-param name="schema" select="$schema"/>
              <xsl:with-param name="edit" select="$edit"/>
              <xsl:with-param name="title" select="$label"/>
              <xsl:with-param name="text">
                <xsl:if test="$visible">
                  <input id="_{$ref}_src" class="md" type="text" name="_{$ref}_src" value="{$src}" size="40">
                    <xsl:if test="$isXLinked"><xsl:attribute name="disabled">disabled</xsl:attribute></xsl:if>
                  </input>
                </xsl:if>
                <button class="content" onclick="{$action}" type="button">
                  <xsl:value-of select="$setButtonLabel"/>
                </button>
              </xsl:with-param>
              <xsl:with-param name="id" select="concat('db_',$ref)"/>
              <xsl:with-param name="visible" select="$setButton"/>
            </xsl:call-template>
            
            <xsl:if test="$delButton">
              <xsl:apply-templates mode="iso19139FileRemove" select="gmx:FileName">
                <xsl:with-param name="access" select="'public'"/>
                <xsl:with-param name="id" select="$id"/>
                <xsl:with-param name="geo" select="false()"/>
              </xsl:apply-templates>
            </xsl:if>
            
            <xsl:call-template name="simpleElementGui">
              <xsl:with-param name="schema" select="$schema"/>
              <xsl:with-param name="edit" select="$edit"/>
              <xsl:with-param name="title">
                <xsl:call-template name="getTitle">
                  <xsl:with-param name="name"   select="name(.)"/>
                  <xsl:with-param name="schema" select="$schema"/>
                </xsl:call-template>
              </xsl:with-param>
              <xsl:with-param name="text">
                <input id="_{$ref}" class="md" type="text" name="_{$ref}" value="{$value}" size="40" >
                  <xsl:if test="$isXLinked"><xsl:attribute name="disabled">disabled</xsl:attribute></xsl:if>
                </input>
              </xsl:with-param>
              <xsl:with-param name="id" select="concat('di_',$ref)"/>
            </xsl:call-template>
          </xsl:when>
          <xsl:otherwise>
            <!-- in view mode, if a label is provided display a simple element for this label 
              with the link variable (could be an image or a hyperlink)-->
            <xsl:variable name="link">
              <xsl:choose>
                <xsl:when test="geonet:is-image(gmx:FileName/@src)">
                  <div class="logo-wrap"><img class="logo" src="{gmx:FileName/@src}"/></div>
                </xsl:when>
                <xsl:otherwise>
                  <a href="{gmx:FileName/@src}"><xsl:value-of select="gmx:FileName"/></a>
                </xsl:otherwise>
              </xsl:choose>
            </xsl:variable>
            
            <xsl:if test="$label">
              <xsl:call-template name="simpleElementGui">
                <xsl:with-param name="schema" select="$schema"/>
                <xsl:with-param name="edit" select="$edit"/>
                <xsl:with-param name="title" select="$label"/>
                <xsl:with-param name="text">
                  <xsl:copy-of select="$link"/>
                </xsl:with-param>
              </xsl:call-template>
            </xsl:if>
            
            <xsl:call-template name="simpleElementGui">
              <xsl:with-param name="schema" select="$schema"/>
              <xsl:with-param name="edit" select="$edit"/>
              <xsl:with-param name="title">
                <xsl:call-template name="getTitle">
                  <xsl:with-param name="name"   select="name(.)"/>
                  <xsl:with-param name="schema" select="$schema"/>
                </xsl:call-template>
              </xsl:with-param>
              <xsl:with-param name="text">
                <xsl:choose>
                  <xsl:when test="$label">
                    <xsl:value-of select="gmx:FileName"/>
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:copy-of select="$link"/>
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:with-param>
            </xsl:call-template>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:with-param>
    </xsl:apply-templates>
  </xsl:template>



  <!-- ================================================================= -->
  <!-- codelists -->
  <!-- ================================================================= -->

  <xsl:template mode="iso19139" match="gmd:*[*/@codeList]|srv:*[*/@codeList]">
    <xsl:param name="schema"/>
    <xsl:param name="edit"/>
    
    <xsl:variable name="elementName" select="name(.)"/>
    
    <!-- Multiple mode is activated only if cardinality for current element is > than 1
    and editorMode attribute of codelist is set to 'select' -->
    <xsl:variable name="multiple">
      <xsl:choose>
        <xsl:when test="geonet:element/@max > 1">
          <xsl:variable name="codelist" select="geonet:getCodelist($schema, name(*[@codeListValue]), /root/gui)" />
          <xsl:value-of select="if ($codelist/codelist/@editorMode='select') then true() else false()"/>
        </xsl:when>
        <xsl:otherwise><xsl:value-of select="false()"/></xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    
    <xsl:choose>
      <xsl:when test="$multiple=true()">
        <xsl:variable name="firstElement" select="count(preceding-sibling::node()[name()=$elementName])=0"/>
        <xsl:if test="$firstElement">
          <xsl:call-template name="iso19139CodelistMultiple">
            <xsl:with-param name="schema" select="$schema"/>
            <xsl:with-param name="edit"   select="$edit"/>
          </xsl:call-template>
        </xsl:if>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="iso19139Codelist">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="edit"   select="$edit"/>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  

  <xsl:template name="iso19139Codelist">
    <xsl:param name="schema"/>
    <xsl:param name="edit"/>
    <xsl:apply-templates mode="simpleElement" select=".">
      <xsl:with-param name="schema" select="$schema"/>
      <xsl:with-param name="edit"   select="$edit"/>
      <xsl:with-param name="text">
        <xsl:apply-templates mode="iso19139GetAttributeText" select="*/@codeListValue">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="edit"   select="$edit"/>
        </xsl:apply-templates>
      </xsl:with-param>
    </xsl:apply-templates>
    
  </xsl:template>
  
  <!-- 
    One control for setting one or more codelist values. The form will set up 
    XML fragments to be inserted into the record.
    
  -->
  <xsl:template name="iso19139CodelistMultiple">
    <xsl:param name="schema"/>
    <xsl:param name="edit"/>
    
    <xsl:variable name="elementName" select="name(.)"/>
    <xsl:variable name="codeListElementName" select="name(*[@codeListValue])"/>
    <xsl:variable name="max" select="geonet:element/@max"/>
    <xsl:variable name="isXLinked" select="count(ancestor-or-self::node()[@xlink:href]) > 0" />
    
    <xsl:variable name="codelist" select="geonet:getCodelist($schema, $codeListElementName, /root/gui)" />
    
    <xsl:apply-templates mode="simpleElement" select=".">
      <xsl:with-param name="schema" select="$schema"/>
      <xsl:with-param name="edit"   select="$edit"/>
      <xsl:with-param name="refs" select="concat(geonet:element/@ref, ',', string-join(following-sibling::node()[name()=$elementName]/geonet:element/@ref, ','))"/>
      <xsl:with-param name="text">
        <!-- An hidden textarea which contains XML fragments to be added according to select field. 
        XML fragments is inserted in the parent of the element and replace existing children of same type.
        
        FIXME : in ajax mode, parent is not available in snippet and ../geonet:element/@ref is unresolved (see #122).
        -->
        <textarea id="X{geonet:element/@ref}" 
                  name="_X{../geonet:element/@ref}_{replace($elementName, ':', 'COLON')}_replace"
                  style="display:none;">
          <xsl:element name="{$elementName}">
            <xsl:element name="{$codeListElementName}">
              <xsl:copy-of select="child::node()[@codeListValue]/@*"/>
            </xsl:element>
          </xsl:element></textarea>
        
        <xsl:variable name="element" select="."/>

        <select id="{geonet:element/@ref}" class="md codelist_multiple" multiple="multiple" size="{if($max &lt; 7) then $max else '7'}">
          <xsl:if test="$edit">
            <xsl:attribute name="onchange">validateNonEmpty(this);</xsl:attribute>
          </xsl:if>
          <xsl:if test="$isXLinked">
            <xsl:attribute name="disabled">disabled</xsl:attribute>
          </xsl:if>
          <option name=""/>
          <xsl:for-each select="$codelist/codelist/entry[not(@hideInEditMode)]">
            <xsl:sort select="label"/>
            <xsl:variable name="code" select="code"/>
            <option>
              <xsl:if test="count($element/../node()[name()=$elementName]/child::node()[@codeListValue=$code])!=0">
                <xsl:attribute name="selected"/>
              </xsl:if>
              <xsl:attribute name="value"><xsl:value-of select="code"/></xsl:attribute>
              <xsl:attribute name="title"><xsl:value-of select="description"/></xsl:attribute>
              <xsl:value-of select="label"/>
            </option>
          </xsl:for-each>
          
          <!-- Add non existing values -->
          <xsl:for-each select="../node()[name()=$elementName]/child::node()[@codeListValue and not(@codeListValue=$codelist/codelist/entry/code)]">
            <option selected="selected" value="{@codeListValue}"><xsl:value-of select="@codeListValue"/></option>
          </xsl:for-each>
        </select>
      </xsl:with-param>
    </xsl:apply-templates>
    
  </xsl:template>
  
  
  <!-- ============================================================================= -->
  
  <!-- LanguageCode is a codelist, but retrieving
  the list of language as defined in the language database table
  allows to create the list for selection.
  
  This table is also used for gmd:language element.
  -->
  <xsl:template mode="iso19139" match="gmd:LanguageCode" priority="2">
    <xsl:param name="schema"/>
    <xsl:param name="edit"/>
    
    <xsl:call-template name="iso19139GetIsoLanguage">
      <xsl:with-param name="value" select="string(@codeListValue)"/>
      <xsl:with-param name="ref" select="concat('_', geonet:element/@ref, '_codeListValue')"/>
      <xsl:with-param name="schema" select="$schema"/>
      <xsl:with-param name="edit"   select="$edit"/>
    </xsl:call-template>
  </xsl:template>

  <!--  Do not allow editing of id to end user. Id is based on language selection
  and iso code.-->
  <xsl:template mode="iso19139" match="gmd:PT_Locale/@id"
    priority="2">
    <xsl:param name="schema" />
    <xsl:param name="edit" />
    
    <xsl:apply-templates mode="simpleElement" select=".">
      <xsl:with-param name="schema" select="$schema" />
      <xsl:with-param name="edit" select="false()" />
    </xsl:apply-templates>
  </xsl:template>
  
  
  <!-- ============================================================================= -->
  <!-- 
    Return a codelist entry from localization files.
    If schema is an ISO profil, then the codelist is from the profil 
    is returned if found. If not, the ISO19139 codelist is returned.
  -->
  <xsl:function name="geonet:getCodelist" as="node()">
    <xsl:param name="schema" as="xs:string"/>
    <xsl:param name="qname" as="xs:string"/>
    <xsl:param name="labels" as="node()"/>
      <!--
      Get codelist from profil first and use use default one if not
      available.
    -->
    <xsl:variable name="codelistProfil">
      <xsl:choose>
        <xsl:when test="starts-with($schema,'iso19139.')">
          <xsl:copy-of
            select="$labels/schemas/*[name(.)=$schema]/codelists/codelist[@name = $qname and not(@displayIf)]" />
        </xsl:when>
        <xsl:otherwise />
      </xsl:choose>
    </xsl:variable>
    
    <xsl:variable name="codelistCore">
      <xsl:choose>
        <xsl:when test="normalize-space($codelistProfil)!=''">
          <xsl:copy-of select="$codelistProfil" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:copy-of
            select="$labels/schemas/*[name(.)='iso19139']/codelists/codelist[@name = $qname and not(@displayIf)]" />
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:copy-of select="exslt:node-set($codelistCore)"/>
  </xsl:function>
  
  <xsl:template mode="iso19139GetAttributeText" match="@*">
    <xsl:param name="schema"/>
    <xsl:param name="edit"/>
    
    <xsl:variable name="name"     select="local-name(..)"/>
    <xsl:variable name="qname"    select="name(..)"/>
    <xsl:variable name="value"    select="../@codeListValue"/>
    
    <xsl:choose>
      <xsl:when test="$qname='gmd:LanguageCode'">
        <xsl:apply-templates mode="iso19139" select="..">
          <xsl:with-param name="edit" select="$edit"/>
        </xsl:apply-templates>
      </xsl:when>
      <xsl:otherwise>
        
        
        <xsl:variable name="codelist" select="geonet:getCodelist($schema, $qname, /root/gui)" />
        <xsl:variable name="isXLinked" select="count(ancestor-or-self::node()[@xlink:href]) > 0" />
        
        <xsl:choose>
          <xsl:when test="$edit=true()">
            <!-- codelist in edit mode 
            Mode : 
             * select one with combo = combo (default)
             * select one with radio = radio
             * select one with radio multilines = radio_linebreak
             * select one with radio multilines and definition = radio_withdesc
             * select one with select with size = select
            -->
            <xsl:variable name="mode" select="$codelist/codelist/@editorMode"/>
            <xsl:variable name="oneOnly" select="../../geonet:element/@min='1'"/>
            <xsl:variable name="defaultSize" select="xs:integer(7)"/>
            <xsl:variable name="max" select="../../geonet:element/@max"/>
            <xsl:variable name="name" select="concat('_', ../geonet:element/@ref, '_', name(.))"/>
            
            <xsl:choose>
              <!-- 
                Radio button modes
              -->
              <xsl:when test="contains($mode, 'radio')">
                <div class="helper helper-{$mode}">
                  <div>
                    <input class="md" type="radio" id="{$name}" name="{$name}" value="">
                      <xsl:if test="$isXLinked">
                        <xsl:attribute name="disabled">disabled</xsl:attribute>
                      </xsl:if>
                    </input>
                    <label for="{$name}"><xsl:value-of select="/root/gui/strings/nodata"/></label>
                  </div>
                  
                  
                  <xsl:for-each select="$codelist/codelist/entry[not(@hideInEditMode)]">
                    <xsl:sort select="label"/>
                    <div>
                      <input class="md" type="radio" name="{$name}" id="{$name}{position()}" value="{code}">
                        <xsl:if test="$isXLinked">
                          <xsl:attribute name="disabled">disabled</xsl:attribute>
                        </xsl:if>
                        <xsl:if test="code=$value">
                          <xsl:attribute name="checked"/>
                        </xsl:if>
                      </input>
                      <label for="{$name}{position()}" title="{description}">
                        <xsl:value-of select="label"/>
                        <xsl:if test="$mode='radio_withdesc'"><span><xsl:value-of select="description"/></span></xsl:if>
                      </label>
                    </div>
                  </xsl:for-each>
                
                  <!-- Add non existing values -->
                  <xsl:if test="count($codelist/codelist/entry[code=$value])=0">
                    <div>
                      <input class="md" type="radio" name="{$name}" id="{$name}{position()}" checked="checked" value="{$value}"/>
                      <label for="{$name}{position()}"><xsl:value-of select="$value"/></label>
                    </div>
                  </xsl:if>
                </div>
                
              </xsl:when>
              <!-- 
                Select list modes
              -->
              <xsl:otherwise>
                <select class="md" name="{$name}" id="{$name}">
                  <xsl:if test="$mode='select'">
                    <xsl:attribute name="size"><xsl:value-of select="if(count($codelist/codelist/entry[not(@hideInEditMode)]) &lt; $defaultSize) 
                                                                      then count($codelist/codelist/entry[not(@hideInEditMode)]) 
                                                                      else $defaultSize"/></xsl:attribute>
                  </xsl:if>
                  <!-- Check element is mandatory or not -->
                  <xsl:if test="$oneOnly and $edit">
                    <xsl:attribute name="onchange">validateNonEmpty(this);</xsl:attribute>
                  </xsl:if>
                  <xsl:if test="$isXLinked">
                    <xsl:attribute name="disabled">disabled</xsl:attribute>
                  </xsl:if>
                  <option name=""/>
                  <xsl:for-each select="$codelist/codelist/entry[not(@hideInEditMode)]">
                    <xsl:sort select="label"/>
                    <option>
                      <xsl:if test="code=$value">
                        <xsl:attribute name="selected"/>
                      </xsl:if>
                      <xsl:attribute name="value"><xsl:value-of select="code"/></xsl:attribute>
                      <xsl:attribute name="title"><xsl:value-of select="description"/></xsl:attribute>
                      <xsl:value-of select="label"/>
                    </option>
                  </xsl:for-each>
                  
                  
                  <!-- Add non existing values -->
                  <xsl:if test="count($codelist/codelist/entry[code=$value])=0">
                    <option selected="selected" value="{$value}"><xsl:value-of select="$value"/></option>
                  </xsl:if>
                </select>
                
              </xsl:otherwise>
            </xsl:choose>
            
          </xsl:when>
          <xsl:otherwise>
            <!-- codelist in view mode -->
            <xsl:if test="normalize-space($value)!=''">
              <xsl:variable name="label" select="$codelist/codelist/entry[code = $value]/label"/>
              <xsl:choose>
                <xsl:when test="normalize-space($label)!=''">
                  <b><xsl:value-of select="$label"/></b>
                  <xsl:value-of select="concat(': ',$codelist/codelist/entry[code = $value]/description)"/>
                </xsl:when>
                <xsl:otherwise>
                  <b><xsl:value-of select="$value"/></b>
                </xsl:otherwise>
              </xsl:choose>
            </xsl:if>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:otherwise>
    </xsl:choose>
    <!--
    <xsl:call-template name="getAttributeText">
      <xsl:with-param name="schema" select="$schema"/>
      <xsl:with-param name="edit"   select="$edit"/>
    </xsl:call-template>
    -->
  </xsl:template>
  
  <!-- ============================================================================= -->
  <!--
  make the following fields always not editable:
  dateStamp
  metadataStandardName
  metadataStandardVersion
  fileIdentifier
  characterSet
  -->
  <!-- ============================================================================= -->

  <xsl:template mode="iso19139" match="gmd:dateStamp|gmd:fileIdentifier" priority="2">
    <xsl:param name="schema"/>
    <xsl:param name="edit"/>
    
    <xsl:apply-templates mode="simpleElement" select=".">
      <xsl:with-param name="schema"  select="$schema"/>
      <xsl:with-param name="edit"    select="false()"/>
      <xsl:with-param name="text">
        <xsl:choose>
          <xsl:when test="string-join(gco:*, '')=''">
            <span class="info">
              - <xsl:value-of select="/root/gui/strings/setOnSave"/> - 
            </span>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="gco:*"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:with-param>
    </xsl:apply-templates>
  </xsl:template>

  <!-- Attributes 
   * deprecated: gmd:PT_Locale/@id is set by update-fixed-info using first 2 letters.
   * gmd:PT_Locale/@id is set by update-fixed-info with 639-2 iso code
  -->
  <xsl:template mode="iso19139" match="gmd:PT_Locale/@id"
    priority="2">
    <xsl:param name="schema" />
    <xsl:param name="edit" />
    
    <xsl:apply-templates mode="simpleElement" select=".">
      <xsl:with-param name="schema" select="$schema" />
      <xsl:with-param name="edit" select="false()" />
    </xsl:apply-templates>
  </xsl:template>
  

  <xsl:template mode="iso19139" match="//gmd:MD_Metadata/gmd:characterSet|//*[@gco:isoType='gmd:MD_Metadata']/gmd:characterSet" priority="2">
    <xsl:param name="schema"/>
    <xsl:param name="edit"/>
    
    <xsl:call-template name="iso19139Codelist">
      <xsl:with-param name="schema"  select="$schema"/>
      <xsl:with-param name="edit"    select="false()"/>
    </xsl:call-template>
  </xsl:template>
  
  <!-- ============================================================================= -->
  <!-- electronicMailAddress -->
  <!-- ============================================================================= -->

  <xsl:template mode="iso19139" match="gmd:electronicMailAddress" priority="2">
    <xsl:param name="schema"/>
    <xsl:param name="edit"/>
    
    <xsl:choose>
      <xsl:when test="$edit=true()">
        <xsl:call-template name="iso19139String">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="edit"   select="true()"/>
          <xsl:with-param name="validator" select="'validateEmail(this);'"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates mode="simpleElement" select=".">
          <xsl:with-param name="schema"  select="$schema"/>
          <xsl:with-param name="text">
            <a href="mailto:{string(.)}"><xsl:value-of select="string(.)"/></a>
          </xsl:with-param>
        </xsl:apply-templates>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <!--
  Keyword editing using classic mode (ie. one field per XML tag)
  based on geonet:element/@ref.
  -->
  <xsl:template match="gmd:MD_Keywords" mode="classic-editor">
    <xsl:param name="schema"/>
    <xsl:param name="edit"/>
    
    <!-- FIXME : layout should move to metadata.xsl -->
    <col>
      <xsl:apply-templates mode="elementEP" select="gmd:keyword|geonet:child[string(@name)='keyword']">
        <xsl:with-param name="schema" select="$schema"/>
        <xsl:with-param name="edit"   select="$edit"/>
      </xsl:apply-templates>
      <xsl:apply-templates mode="elementEP" select="gmd:type|geonet:child[string(@name)='type']">
        <xsl:with-param name="schema" select="$schema"/>
        <xsl:with-param name="edit"   select="$edit"/>
      </xsl:apply-templates>
    </col>
    <col>                    
      <xsl:apply-templates mode="elementEP" select="gmd:thesaurusName|geonet:child[string(@name)='thesaurusName']">
        <xsl:with-param name="schema" select="$schema"/>
        <xsl:with-param name="edit"   select="$edit"/>
      </xsl:apply-templates>
    </col>
  </xsl:template>
  
  <!-- 
  Widget editor based on edition of an XML snippet.
  -->
  <xsl:template match="gmd:MD_Keywords" mode="snippet-editor">
    <xsl:param name="schema"/>
    <xsl:param name="edit"/>
    
    <!-- 
        TODO : multilingual md
       
    -->
    <!-- Create a div which contains the JSON configuration 
    * thesaurus: thesaurus to use
    * keywords: list of keywords in the element
    * transformations: list of transformations
    * transformation: current transformation
    -->
    
    <!-- Single quote are escaped inside keyword. -->
    <xsl:variable name="listOfKeywords" select="replace(replace(string-join(gmd:keyword/*[1], '#,#'), '''', '\\'''), '#', '''')"/>
    
    <!-- Get current transformation mode based on XML fragement analysis -->
    <xsl:variable name="transformation"
                  select="if (count(descendant::gmd:keyword/gmx:Anchor) > 0) then 'to-iso19139-keyword-with-anchor'
      else if (../@xlink:href) then 'to-iso19139-keyword-as-xlink'
      else 'to-iso19139-keyword'"/>
    
    <!-- Define the list of transformation mode available.
    -->
    <xsl:variable name="parentName" select="name(..)"/>
    
    <xsl:variable name="listOfTransformations">'to-iso19139-keyword', 'to-iso19139-keyword-with-anchor', 'to-iso19139-keyword-as-xlink'</xsl:variable>
    
    <!-- Create custom widget: 
      * '' for item selector, 
      * 'combo' for simple combo, 
      * 'list' for selection list, 
      * 'multiplelist' for multiple selection list
      -->
    <xsl:variable name="widgetMode" select="''"/>
    
    <!-- Retrieve the thesaurus identifier from the thesaurus citation. The thesaurus 
    identifier should be defined in the citation identifier. By default, GeoNetwork
    define it in a gmx:Anchor. Retrieve the first child of the code which might be a
    gco:CharacterString. 
    -->
    <xsl:variable name="thesaurusName" select="gmd:thesaurusName/gmd:CI_Citation/gmd:title/gco:CharacterString"></xsl:variable>
    <xsl:variable name="thesaurusId" select="if (gmd:thesaurusName/gmd:CI_Citation/
      gmd:identifier/gmd:MD_Identifier/gmd:code/*[1]) then gmd:thesaurusName/gmd:CI_Citation/
      gmd:identifier/gmd:MD_Identifier/gmd:code/*[1] else /root/gui/thesaurus/thesauri/thesaurus[title=$thesaurusName]/key"/>
    
    
    <!-- The element identifier in the metadocument-->
    <xsl:variable name="elementRef" select="../geonet:element/@ref"/>
    
    <xsl:call-template name="snippet-editor">
      <xsl:with-param name="elementRef" select="$elementRef"/>
      <xsl:with-param name="widgetMode" select="$widgetMode"/>
      <xsl:with-param name="thesaurusId" select="$thesaurusId"/>
      <xsl:with-param name="listOfKeywords" select="$listOfKeywords"/>
      <xsl:with-param name="listOfTransformations" select="$listOfTransformations"/>
      <xsl:with-param name="transformation" select="$transformation"/>
    </xsl:call-template>
    
  </xsl:template>
  
  
  <xsl:template name="snippet-editor">
    <xsl:param name="elementRef"/>
    <xsl:param name="widgetMode" select="''"/>
    <xsl:param name="thesaurusId"/>
    <xsl:param name="listOfKeywords"/>
    <xsl:param name="listOfTransformations"/>
    <xsl:param name="transformation"/> 
    <xsl:param name="maxKeywords" select="'100'"/>
    <xsl:param name="searchOnLoad" select="'true'"/>
    <xsl:param name="itemSelectorHeight" select="'undefined'" required="no"/>
    <xsl:param name="itemSelectorWidth" select="'undefined'" required="no"/>
    <xsl:param name="identificationMode" select="'value'" required="no"/>
    
    <!-- The widget configuration -->
    <div class="thesaurusPickerCfg" id="thesaurusPicker_{$elementRef}" 
      config="{{mode: '{$widgetMode}', thesaurus:'{$thesaurusId
      }', maxKeywords: {$maxKeywords}, searchOnLoad: '{$searchOnLoad}', keywords: ['{$listOfKeywords
      }'], transformations: [{$listOfTransformations
      }], transformation: '{$transformation
      }', identificationMode: '{$identificationMode}', itemSelectorHeight: {$itemSelectorHeight}, itemSelectorWidth: {$itemSelectorWidth}}}"/>
    
    <!-- The widget container -->
    <div class="thesaurusPicker" id="thesaurusPicker_{$elementRef}_panel"/>
    
    <!-- Create a textarea which contains the XML snippet for updates.
    The name of the element starts with _X which means XML snippet update mode.
    -->
    <textarea id="thesaurusPicker_{$elementRef}_xml" name="_X{$elementRef}_replace" rows="" cols="" class="debug">
      <xsl:apply-templates mode="geonet-cleaner" select="parent::node()"/>
    </textarea>
    
  </xsl:template>
  
  
  
  <!-- ============================================================================= -->
  <!-- descriptiveKeywords -->
  <!-- ============================================================================= -->
  <xsl:template mode="iso19139" match="gmd:descriptiveKeywords">
    <xsl:param name="schema"/>
    <xsl:param name="edit"/>
    
    <xsl:choose>
      <xsl:when test="$edit=true()">
    
        <xsl:apply-templates mode="complexElement" select=".">
          <xsl:with-param name="schema"  select="$schema"/>
          <xsl:with-param name="edit"    select="$edit"/>
          <xsl:with-param name="content">
            
            <xsl:variable name="thesaurusName" select="gmd:MD_Keywords/gmd:thesaurusName/gmd:CI_Citation/gmd:title/gco:CharacterString"></xsl:variable>
            <xsl:variable name="thesaurusCode" select="if (gmd:thesaurusName/gmd:CI_Citation/
              gmd:identifier/gmd:MD_Identifier/gmd:code/*[1]) then gmd:thesaurusName/gmd:CI_Citation/
              gmd:identifier/gmd:MD_Identifier/gmd:code/*[1] else /root/gui/thesaurus/thesauri/thesaurus[title=$thesaurusName]/key"/>
            
            <!-- Check that thesaurus is available locally. Check that the thesaurus is available in the catalogue to not 
              to try to initialize a widget with a non existing thesaurus.  -->
            <xsl:variable name="isThesaurusAvailable"
              select="count(/root/gui/thesaurus/thesauri/thesaurus[key = $thesaurusCode]) > 0"></xsl:variable>
            
            <xsl:choose>
              <!-- If a thesaurus is attached to that keyword group 
              use a snippet editor.-->
              <xsl:when test="$isThesaurusAvailable">
                <xsl:apply-templates select="gmd:MD_Keywords" mode="snippet-editor">
                  <xsl:with-param name="edit" select="$edit"/>
                  <xsl:with-param name="schema" select="$schema"/>
                </xsl:apply-templates>
              </xsl:when>
              <xsl:otherwise>
                <xsl:variable name="content">
                  <xsl:apply-templates select="gmd:MD_Keywords" mode="classic-editor">
                    <xsl:with-param name="edit" select="$edit"/>
                    <xsl:with-param name="schema" select="$schema"/>
                  </xsl:apply-templates>
                </xsl:variable>
                
                <xsl:call-template name="columnElementGui">
                  <xsl:with-param name="cols" select="$content"/>
                </xsl:call-template>
              </xsl:otherwise>
            </xsl:choose>
            
            
          </xsl:with-param>
        </xsl:apply-templates>
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates mode="simpleElement" select=".">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="title">
            <xsl:call-template name="getTitle">
              <xsl:with-param name="name" select="name(.)"/>
              <xsl:with-param name="schema" select="$schema"/>
            </xsl:call-template>
            <xsl:if test="gmd:MD_Keywords/gmd:thesaurusName/gmd:CI_Citation/gmd:title/gco:CharacterString">
              (<xsl:value-of
                select="gmd:MD_Keywords/gmd:thesaurusName/gmd:CI_Citation/gmd:title/gco:CharacterString"/>)
            </xsl:if>
          </xsl:with-param>
          <xsl:with-param name="text">
            <xsl:variable name="value">
              <xsl:for-each select="gmd:MD_Keywords/gmd:keyword">
                <xsl:if test="position() &gt; 1"><xsl:text>, </xsl:text></xsl:if>

								<xsl:choose>
									<xsl:when test="gmx:Anchor">
										<a href="{gmx:Anchor/@xlink:href}"><xsl:value-of select="if (gmx:Anchor/text()) then gmx:Anchor/text() else gmx:Anchor/@xlink:href"/></a>
									</xsl:when>
									<xsl:otherwise>

                <xsl:call-template name="translatedString">
                  <xsl:with-param name="schema" select="$schema"/>
                  <xsl:with-param name="langId">
                        <xsl:call-template name="getLangId">
                              <xsl:with-param name="langGui" select="/root/gui/language"/>
                              <xsl:with-param name="md" select="ancestor-or-self::*[name(.)='gmd:MD_Metadata' or @gco:isoType='gmd:MD_Metadata']" />
                          </xsl:call-template>
                    </xsl:with-param>
                  </xsl:call-template>

										</xsl:otherwise>
									</xsl:choose>

              </xsl:for-each>
              <xsl:variable name="type" select="gmd:MD_Keywords/gmd:type/gmd:MD_KeywordTypeCode/@codeListValue"/>
              <xsl:if test="$type">
                (<xsl:value-of
                  select="/root/gui/schemas/*[name(.)='iso19139']/codelists/codelist[@name = 'gmd:MD_KeywordTypeCode']/
                  entry[code = $type]/label"/>)
              </xsl:if>
              <xsl:text>.</xsl:text>
            </xsl:variable>
            <xsl:copy-of select="$value"/>
          </xsl:with-param>
        </xsl:apply-templates>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- ============================================================================= -->
  <!-- place keyword; only called in edit mode (see descriptiveKeywords template) -->
  <!-- ============================================================================= -->

  <xsl:template mode="iso19139" match="gmd:keyword[following-sibling::gmd:type/gmd:MD_KeywordTypeCode/@codeListValue='place']">
    <xsl:param name="schema"/>
    <xsl:param name="edit"/>
    
    <xsl:variable name="text">
      <xsl:variable name="ref" select="gco:CharacterString/geonet:element/@ref"/>
      <xsl:variable name="keyword" select="gco:CharacterString/text()"/>
      
      <input class="md" type="text" name="_{$ref}" value="{gco:CharacterString/text()}" size="40" />

      <!-- regions combobox -->

      <xsl:variable name="lang" select="/root/gui/language"/>
      <xsl:text> </xsl:text>
      <select name="place" size="1" onChange="document.mainForm._{$ref}.value=this.options[this.selectedIndex].text" class="md">
        <option value=""/>
        <xsl:for-each select="/root/gui/regions/record">
          <xsl:sort select="label/child::*[name() = $lang]" order="ascending"/>
          <option value="{id}">
            <xsl:if test="string(label/child::*[name() = $lang])=$keyword">
              <xsl:attribute name="selected"/>
            </xsl:if>
            <xsl:value-of select="label/child::*[name() = $lang]"/>
          </option>
        </xsl:for-each>
      </select>
    </xsl:variable>
    <xsl:apply-templates mode="simpleElement" select=".">
      <xsl:with-param name="schema" select="$schema"/>
      <xsl:with-param name="edit"   select="true()"/>
      <xsl:with-param name="text"   select="$text"/>
    </xsl:apply-templates>
  </xsl:template>
      

  <!-- ============================================================================= -->
  <!--
  dateTime (format = %Y-%m-%dT%H:%M:00)
  usageDateTime
  plannedAvailableDateTime
  -->
  <!-- ============================================================================= -->

  <xsl:template mode="iso19139" match="gmd:dateTime|gmd:usageDateTime|gmd:plannedAvailableDateTime" priority="2">
    <xsl:param name="schema"/>
    <xsl:param name="edit"/>
    
    <xsl:choose>
      <xsl:when test="$edit=true()">
        <xsl:apply-templates mode="simpleElement" select=".">
          <xsl:with-param name="schema"  select="$schema"/>
          <xsl:with-param name="edit"   select="$edit"/>
          <xsl:with-param name="text">
            <xsl:variable name="ref" select="gco:Date/geonet:element/@ref|gco:DateTime/geonet:element/@ref"/>
            <xsl:variable name="format">
              <xsl:choose>
                <xsl:when test="gco:Date"><xsl:text>%Y-%m-%d</xsl:text></xsl:when>
                <xsl:otherwise><xsl:text>%Y-%m-%dT%H:%M:00</xsl:text></xsl:otherwise>
              </xsl:choose>
            </xsl:variable>
            
            <xsl:call-template name="calendar">
              <xsl:with-param name="ref" select="$ref"/>
              <xsl:with-param name="date" select="gco:DateTime/text()|gco:Date/text()"/>
              <xsl:with-param name="format" select="$format"/>
            </xsl:call-template>
            
          </xsl:with-param>
        </xsl:apply-templates>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="iso19139String">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="edit"   select="$edit"/>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- ============================================================================= -->
  <!--
  date (format = %Y-%m-%d)
  editionDate
  dateOfNextUpdate
  mdDateSt is not editable (!we use DateTime instead of only Date!)
  -->
  <!-- ============================================================================= -->

  <xsl:template mode="iso19139" match="gmd:date[gco:DateTime|gco:Date]|gmd:editionDate|gmd:dateOfNextUpdate" priority="2">
    <xsl:param name="schema"/>
    <xsl:param name="edit"/>
    
    <xsl:choose>
      <xsl:when test="$edit=true()">
        <xsl:apply-templates mode="simpleElement" select=".">
          <xsl:with-param name="schema"  select="$schema"/>
          <xsl:with-param name="edit"   select="$edit"/>
          <xsl:with-param name="text">
            <xsl:variable name="ref" select="gco:DateTime/geonet:element/@ref|gco:Date/geonet:element/@ref"/>
            <xsl:variable name="format">
              <xsl:choose>
                <xsl:when test="gco:Date"><xsl:text>%Y-%m-%d</xsl:text></xsl:when>
                <xsl:otherwise><xsl:text>%Y-%m-%dT%H:%M:00</xsl:text></xsl:otherwise>
              </xsl:choose>
            </xsl:variable>
            
            <xsl:call-template name="calendar">
              <xsl:with-param name="ref" select="$ref"/>
              <xsl:with-param name="date" select="gco:DateTime/text()|gco:Date/text()"/>
              <xsl:with-param name="format" select="$format"/>
            </xsl:call-template>
            
          </xsl:with-param>
        </xsl:apply-templates>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="iso19139String">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="edit"   select="$edit"/>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- ===================================================================== -->
  <!-- gml:TimePeriod (format = %Y-%m-%dThh:mm:ss) -->
  <!-- ===================================================================== -->

  <xsl:template mode="iso19139" match="gml:*[gml:beginPosition|gml:endPosition]|gml:TimeInstant[gml:timePosition]" priority="2">
    <xsl:param name="schema"/>
    <xsl:param name="edit"/>
    <xsl:param name="title">
      <xsl:call-template name="getTitle">
        <xsl:with-param name="name" select="name(.)"/>
        <xsl:with-param name="schema" select="$schema"/>
      </xsl:call-template>
    </xsl:param>
    <xsl:for-each select="*">
    <xsl:choose>
      <xsl:when test="$edit=true() and (name(.)='gml:beginPosition' or name(.)='gml:endPosition' or name(.)='gml:timePosition')">
        <xsl:apply-templates mode="simpleElement" select=".">
          <xsl:with-param name="schema"  select="$schema"/>
          <xsl:with-param name="edit"   select="$edit"/>
          <xsl:with-param name="title"   select="$title"/>
          <xsl:with-param name="text">
            <xsl:variable name="ref" select="geonet:element/@ref"/>
            <!-- 
              TODO : Add the capability to edit those elements as:
               * xs:time
               * xs:dateTime
               * xs:anyURI
               * xs:decimal
               * gml:CalDate
              See http://trac.osgeo.org/geonetwork/ticket/661
              
            -->
            <xsl:variable name="format">
              <xsl:choose>
                <!-- Add basic support of %Y-%m-%d format in edit mode -->
                <xsl:when test="string-length(text()) = 10"><xsl:text>%Y-%m-%d</xsl:text></xsl:when>
                <xsl:otherwise><xsl:text>%Y-%m-%dT%H:%M:00</xsl:text></xsl:otherwise>
              </xsl:choose>
            </xsl:variable>
            
            <xsl:call-template name="calendar">
              <xsl:with-param name="ref" select="$ref"/>
              <xsl:with-param name="date" select="text()"/>
              <xsl:with-param name="format" select="$format"/>
            </xsl:call-template>
                        
          </xsl:with-param>
        </xsl:apply-templates>
      </xsl:when>
      <xsl:when test="name(.)='gml:timeInterval'">
        <xsl:apply-templates mode="iso19139" select="."/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates mode="simpleElement" select=".">
          <xsl:with-param name="schema"  select="$schema"/>
          <xsl:with-param name="text">
            <xsl:value-of select="text()"/>
          </xsl:with-param>
        </xsl:apply-templates>
      </xsl:otherwise>
    </xsl:choose>
    </xsl:for-each>
  </xsl:template>
  
  <!-- =================================================================== -->
  <!-- subtemplates -->
  <!-- =================================================================== -->

  <xsl:template mode="iso19139" match="*[geonet:info/isTemplate='s']" priority="3">
    <xsl:param name="schema"/>
    <xsl:param name="edit"/>
    
    <xsl:apply-templates mode="element" select=".">
      <xsl:with-param name="schema" select="$schema"/>
      <xsl:with-param name="edit"   select="$edit"/>
    </xsl:apply-templates>
  </xsl:template>
  
  <!-- ==================================================================== -->

  <xsl:template mode="iso19139" match="@gco:isoType"/>

  <!-- ==================================================================== -->
  <!-- Metadata -->
  <!-- ==================================================================== -->

  <xsl:template mode="iso19139" match="gmd:MD_Metadata|*[@gco:isoType='gmd:MD_Metadata']">
    <xsl:param name="schema"/>
    <xsl:param name="edit"/>
    <xsl:param name="embedded"/>

    <xsl:variable name="dataset" select="gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue='dataset' or normalize-space(gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue)=''"/>
    
    
    <xsl:choose>
    
      <!-- metadata tab -->
      <xsl:when test="$currTab='metadata'">
        <xsl:call-template name="iso19139Metadata">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="edit"   select="$edit"/>
        </xsl:call-template>
      </xsl:when>

      <!-- identification tab -->
      <xsl:when test="$currTab='identification'">
        <xsl:apply-templates mode="elementEP" select="gmd:identificationInfo|geonet:child[string(@name)='identificationInfo']">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="edit"   select="$edit"/>
        </xsl:apply-templates>
      </xsl:when>

      <!-- maintenance tab -->
      <xsl:when test="$currTab='maintenance'">
        <xsl:apply-templates mode="elementEP" select="gmd:metadataMaintenance|geonet:child[string(@name)='metadataMaintenance']">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="edit"   select="$edit"/>
        </xsl:apply-templates>
      </xsl:when>

      <!-- constraints tab -->
      <xsl:when test="$currTab='constraints'">
        <xsl:apply-templates mode="elementEP" select="gmd:metadataConstraints|geonet:child[string(@name)='metadataConstraints']">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="edit"   select="$edit"/>
        </xsl:apply-templates>
      </xsl:when>

      <!-- spatial tab -->
      <xsl:when test="$currTab='spatial'">
        <xsl:apply-templates mode="elementEP" select="gmd:spatialRepresentationInfo|geonet:child[string(@name)='spatialRepresentationInfo']">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="edit"   select="$edit"/>
        </xsl:apply-templates>
      </xsl:when>

      <!-- refSys tab -->
      <xsl:when test="$currTab='refSys'">
        <xsl:apply-templates mode="elementEP" select="gmd:referenceSystemInfo|geonet:child[string(@name)='referenceSystemInfo']">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="edit"   select="$edit"/>
        </xsl:apply-templates>
      </xsl:when>

      <!-- distribution tab -->
      <xsl:when test="$currTab='distribution'">
        <xsl:apply-templates mode="elementEP" select="gmd:distributionInfo|geonet:child[string(@name)='distributionInfo']">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="edit"   select="$edit"/>
        </xsl:apply-templates>
      </xsl:when>

      <!-- embedded distribution tab -->
      <xsl:when test="$currTab='distribution2'">
        <xsl:apply-templates mode="elementEP" select="gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="edit"   select="$edit"/>
        </xsl:apply-templates>
      </xsl:when>
      
      <!-- dataQuality tab -->
      <xsl:when test="$currTab='dataQuality'">
        <xsl:apply-templates mode="elementEP" select="gmd:dataQualityInfo|geonet:child[string(@name)='dataQualityInfo']">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="edit"   select="$edit"/>
        </xsl:apply-templates>
      </xsl:when>

      <!-- appSchInfo tab -->
      <xsl:when test="$currTab='appSchInfo'">
        <xsl:apply-templates mode="elementEP" select="gmd:applicationSchemaInfo|geonet:child[string(@name)='applicationSchemaInfo']">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="edit"   select="$edit"/>
        </xsl:apply-templates>
      </xsl:when>

      <!-- porCatInfo tab -->
      <xsl:when test="$currTab='porCatInfo'">
        <xsl:apply-templates mode="elementEP" select="gmd:portrayalCatalogueInfo|geonet:child[string(@name)='portrayalCatalogueInfo']">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="edit"   select="$edit"/>
        </xsl:apply-templates>
      </xsl:when>

      <!-- contentInfo tab -->
      <xsl:when test="$currTab='contentInfo'">
      <xsl:apply-templates mode="elementEP" select="gmd:contentInfo|geonet:child[string(@name)='contentInfo']">
        <xsl:with-param name="schema" select="$schema"/>
        <xsl:with-param name="edit"   select="$edit"/>
      </xsl:apply-templates>
      </xsl:when>
      
      <!-- extensionInfo tab -->
      <xsl:when test="$currTab='extensionInfo'">
      <xsl:apply-templates mode="elementEP" select="gmd:metadataExtensionInfo|geonet:child[string(@name)='metadataExtensionInfo']">
        <xsl:with-param name="schema" select="$schema"/>
        <xsl:with-param name="edit"   select="$edit"/>
      </xsl:apply-templates>
      </xsl:when>

      <!-- ISOMinimum tab -->
      <xsl:when test="$currTab='ISOMinimum'">
        <xsl:call-template name="isotabs">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="edit"   select="$edit"/>
          <xsl:with-param name="dataset" select="$dataset"/>
          <xsl:with-param name="core" select="false()"/>
        </xsl:call-template>
      </xsl:when>

      <!-- ISOCore tab -->
      <xsl:when test="$currTab='ISOCore'">
        <xsl:call-template name="isotabs">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="edit"   select="$edit"/>
          <xsl:with-param name="dataset" select="$dataset"/>
          <xsl:with-param name="core" select="true()"/>
        </xsl:call-template>
      </xsl:when>
      
      <!-- ISOAll tab -->
      <xsl:when test="$currTab='ISOAll'">
        <xsl:call-template name="iso19139Complete">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="edit"   select="$edit"/>
        </xsl:call-template>
      </xsl:when>
      
      <!-- INSPIRE tab -->
      <xsl:when test="$currTab='inspire'">
        <xsl:call-template name="inspiretabs">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="edit"   select="$edit"/>
          <xsl:with-param name="dataset" select="$dataset"/>          
        </xsl:call-template>
      </xsl:when>
      
      
      <!-- default -->
      <xsl:otherwise>
        <xsl:call-template name="iso19139Simple">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="edit"   select="$edit"/>
          <xsl:with-param name="flat"   select="/root/gui/config/metadata-tab/*[name(.)=$currTab]/@flat"/>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- ============================================================================= -->

  <xsl:template name="isotabs">
    <xsl:param name="schema"/>
    <xsl:param name="edit"/>
    <xsl:param name="dataset"/>
    <xsl:param name="core"/>

    <!-- dataset or resource info in its own box -->
  
    <xsl:for-each select="gmd:identificationInfo/*">
      <xsl:call-template name="complexElementGuiWrapper">
        <xsl:with-param name="title">
        <xsl:choose>
          <xsl:when test="$dataset=true()">
            <xsl:value-of select="/root/gui/schemas/iso19139/labels/element[@name='gmd:MD_DataIdentification']/label"/>
          </xsl:when>
          <xsl:when test="local-name(.)='SV_ServiceIdentification' or contains(@gco:isoType, 'SV_ServiceIdentification')">
            <xsl:value-of select="/root/gui/schemas/iso19139/labels/element[@name='srv:SV_ServiceIdentification']/label"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="'Resource Identification'"/><!-- FIXME i18n-->
          </xsl:otherwise>
        </xsl:choose>
        </xsl:with-param>
        <xsl:with-param name="content">
    
        <xsl:apply-templates mode="elementEP" select="gmd:citation/gmd:CI_Citation/gmd:title|gmd:citation/gmd:CI_Citation/geonet:child[string(@name)='title']
          |gmd:citation/gmd:CI_Citation/gmd:date|gmd:citation/gmd:CI_Citation/geonet:child[string(@name)='date']
          |gmd:abstract|geonet:child[string(@name)='abstract']
          |gmd:pointOfContact|geonet:child[string(@name)='pointOfContact']
          |gmd:descriptiveKeywords|geonet:child[string(@name)='descriptiveKeywords']
          ">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="edit"   select="$edit"/>
        </xsl:apply-templates>

        
        <xsl:if test="$core and $dataset">
          <xsl:apply-templates mode="elementEP" select="gmd:spatialRepresentationType|geonet:child[string(@name)='spatialRepresentationType']
            |gmd:spatialResolution|geonet:child[string(@name)='spatialResolution']">
            <xsl:with-param name="schema" select="$schema"/>
            <xsl:with-param name="edit"   select="$edit"/>
          </xsl:apply-templates>
        </xsl:if>

        <xsl:apply-templates mode="elementEP" select="gmd:language|geonet:child[string(@name)='language']
          |gmd:characterSet|geonet:child[string(@name)='characterSet']
          |gmd:topicCategory|geonet:child[string(@name)='topicCategory']
          ">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="edit"   select="$edit"/>
        </xsl:apply-templates>


        <xsl:if test="$dataset">
          <xsl:for-each select="gmd:extent/gmd:EX_Extent">
            <xsl:call-template name="complexElementGuiWrapper">
              <xsl:with-param name="title" select="/root/gui/schemas/iso19139/labels/element[@name='gmd:EX_Extent']/label"/>
              <xsl:with-param name="content">
                <xsl:apply-templates mode="elementEP" select="*">
                  <xsl:with-param name="schema" select="$schema"/>
                  <xsl:with-param name="edit"   select="$edit"/>
                </xsl:apply-templates>
              </xsl:with-param>
              <xsl:with-param name="schema" select="$schema"/>
              <xsl:with-param name="edit"   select="$edit"/>
              <xsl:with-param name="realname"   select="'gmd:EX_Extent'"/>
            </xsl:call-template>
          </xsl:for-each>
        </xsl:if>

        </xsl:with-param>
        <xsl:with-param name="schema" select="$schema"/>
        <xsl:with-param name="edit"   select="$edit"/>
        <xsl:with-param name="realname"   select="name(.)"/>
      </xsl:call-template>
    </xsl:for-each>

    <xsl:if test="$core and $dataset">

    <!-- scope and lineage in their own box -->
    
      <xsl:call-template name="complexElementGuiWrapper">
        <xsl:with-param name="title" select="/root/gui/schemas/iso19139/labels/element[@name='gmd:LI_Lineage']/label"/>
        <xsl:with-param name="id" select="generate-id(/root/gui/schemas/iso19139/labels/element[@name='gmd:LI_Lineage']/label)"/>
        <xsl:with-param name="content">

          <xsl:for-each select="gmd:dataQualityInfo/gmd:DQ_DataQuality">
            <xsl:apply-templates mode="elementEP" select="gmd:scope|geonet:child[string(@name)='scope']
              |gmd:lineage|geonet:child[string(@name)='lineage']">
              <xsl:with-param name="schema" select="$schema"/>
              <xsl:with-param name="edit"   select="$edit"/>
            </xsl:apply-templates>
          </xsl:for-each>

        </xsl:with-param>
        <xsl:with-param name="schema" select="$schema"/>
        <xsl:with-param name="group" select="/root/gui/strings/dataQualityTab"/>
        <xsl:with-param name="edit" select="$edit"/>
        <xsl:with-param name="realname"   select="'gmd:DataQualityInfo'"/>
      </xsl:call-template>

    <!-- referenceSystemInfo in its own box -->
    
      <xsl:call-template name="complexElementGuiWrapper">
        <xsl:with-param name="title" select="/root/gui/schemas/iso19139/labels/element[@name='gmd:referenceSystemInfo']/label"/>
        <xsl:with-param name="id" select="generate-id(/root/gui/schemas/iso19139/labels/element[@name='gmd:referenceSystemInfo']/label)"/>
        <xsl:with-param name="content">

        <xsl:for-each select="gmd:referenceSystemInfo/gmd:MD_ReferenceSystem">
          <xsl:apply-templates mode="elementEP" select="gmd:referenceSystemIdentifier/gmd:RS_Identifier/gmd:code
            |gmd:referenceSystemIdentifier/gmd:RS_Identifier/geonet:child[string(@name)='code']
            |gmd:referenceSystemIdentifier/gmd:RS_Identifier/gmd:codeSpace
            |gmd:referenceSystemIdentifier/gmd:RS_Identifier/geonet:child[string(@name)='codeSpace']
            ">
            <xsl:with-param name="schema" select="$schema"/>
            <xsl:with-param name="edit"   select="$edit"/>
          </xsl:apply-templates>
        </xsl:for-each>

        </xsl:with-param>
        <xsl:with-param name="schema" select="$schema"/>
        <xsl:with-param name="group" select="/root/gui/strings/refSysTab"/>
        <xsl:with-param name="edit" select="$edit"/>
        <xsl:with-param name="realname"   select="'gmd:referenceSystemInfo'"/>
      </xsl:call-template>

      <!-- distribution Format and onlineResource(s) in their own box -->

      <xsl:call-template name="complexElementGuiWrapper">
        <xsl:with-param name="title" select="/root/gui/schemas/iso19139/labels/element[@name='gmd:distributionInfo']/label"/>
        <xsl:with-param name="id" select="generate-id(/root/gui/schemas/iso19139/labels/element[@name='gmd:distributionInfo']/label)"/>
        <xsl:with-param name="content">

        <xsl:for-each select="gmd:distributionInfo">
          <xsl:apply-templates mode="elementEP" select="*/gmd:distributionFormat|*/geonet:child[string(@name)='distributionFormat']">
            <xsl:with-param name="schema" select="$schema"/>
            <xsl:with-param name="edit"   select="$edit"/>
          </xsl:apply-templates>

          <xsl:apply-templates mode="elementEP" select="*/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine|*/gmd:transferOptions/gmd:MD_DigitalTransferOptions/geonet:child[string(@name)='onLine']">
            <xsl:with-param name="schema" select="$schema"/>
            <xsl:with-param name="edit"   select="$edit"/>
          </xsl:apply-templates>
        </xsl:for-each>

        </xsl:with-param>
        <xsl:with-param name="schema" select="$schema"/>
        <xsl:with-param name="group" select="/root/gui/strings/distributionTab"/>
        <xsl:with-param name="edit" select="$edit"/>
        <xsl:with-param name="realname" select="gmd:distributionInfo"/>
      </xsl:call-template>
      
    </xsl:if>

    <!-- metadata info in its own box -->

    <xsl:call-template name="complexElementGuiWrapper">
      <xsl:with-param name="title" select="/root/gui/schemas/iso19139/labels/element[@name='gmd:MD_Metadata']/label"/>
      <xsl:with-param name="id" select="generate-id(/root/gui/schemas/iso19139/labels/element[@name='gmd:MD_Metadata']/label)"/>
      <xsl:with-param name="content">

      <xsl:apply-templates mode="elementEP" select="gmd:fileIdentifier|geonet:child[string(@name)='fileIdentifier']
        |gmd:language|geonet:child[string(@name)='language']
        |gmd:characterSet|geonet:child[string(@name)='characterSet']
        |gmd:parentIdentifier|geonet:child[string(@name)='parentIdentifier']
        |gmd:hierarchyLevel|geonet:child[string(@name)='hierarchyLevel']
        |gmd:hierarchyLevelName|geonet:child[string(@name)='hierarchyLevelName']
        ">
        <xsl:with-param name="schema" select="$schema"/>
        <xsl:with-param name="edit"   select="$edit"/>
      </xsl:apply-templates>
    
      <!-- metadata contact info in its own box -->

      <xsl:for-each select="gmd:contact">

        <xsl:call-template name="complexElementGuiWrapper">
          <xsl:with-param name="title" select="/root/gui/schemas/iso19139/labels/element[@name='gmd:contact']/label"/>
          <xsl:with-param name="content">

            <xsl:apply-templates mode="elementEP" select="*/gmd:individualName|*/geonet:child[string(@name)='individualName']
              |*/gmd:organisationName|*/geonet:child[string(@name)='organisationName']
              |*/gmd:positionName|*/geonet:child[string(@name)='positionName']
              |*/gmd:contactInfo|*/geonet:child[string(@name)='contactInfo']
              |*/gmd:role|*/geonet:child[string(@name)='role']">
              <xsl:with-param name="schema" select="$schema"/>
              <xsl:with-param name="edit"   select="$edit"/>
            </xsl:apply-templates>

          </xsl:with-param>
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="group" select="/root/gui/strings/metadata"/>
          <xsl:with-param name="edit" select="$edit"/>
        </xsl:call-template>
    
      </xsl:for-each>

      <!-- more metadata elements -->

      <xsl:apply-templates mode="elementEP" select="gmd:dateStamp|geonet:child[string(@name)='dateStamp']">
        <xsl:with-param name="schema" select="$schema"/>
        <xsl:with-param name="edit"   select="$edit"/>
      </xsl:apply-templates>
    
      <xsl:if test="$core and $dataset">
        <xsl:apply-templates mode="elementEP" select="gmd:metadataStandardName|geonet:child[string(@name)='metadataStandardName']
          |gmd:metadataStandardVersion|geonet:child[string(@name)='metadataStandardVersion']">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="edit"   select="$edit"/>
        </xsl:apply-templates>
      </xsl:if>

      </xsl:with-param>
      <xsl:with-param name="schema" select="$schema"/>
      <xsl:with-param name="group" select="/root/gui/strings/metadataTab"/>
      <xsl:with-param name="edit" select="$edit"/>
    </xsl:call-template>
    
  </xsl:template>


  <!-- ================================================================== -->
  <!-- complete mode we just display everything - tab = complete          -->
  <!-- ================================================================== -->

  <xsl:template name="iso19139Complete">
    <xsl:param name="schema"/>
    <xsl:param name="edit"/>

    <xsl:apply-templates mode="elementEP" select="gmd:identificationInfo|geonet:child[string(@name)='identificationInfo']
      |gmd:spatialRepresentationInfo|geonet:child[string(@name)='spatialRepresentationInfo']
      |gmd:referenceSystemInfo|geonet:child[string(@name)='referenceSystemInfo']
      |gmd:contentInfo|geonet:child[string(@name)='contentInfo']
      |gmd:distributionInfo|geonet:child[string(@name)='distributionInfo']
      |gmd:dataQualityInfo|geonet:child[string(@name)='dataQualityInfo']
      |gmd:portrayalCatalogueInfo|geonet:child[string(@name)='portrayalCatalogueInfo']
      |gmd:metadataConstraints|geonet:child[string(@name)='metadataConstraints']
      |gmd:applicationSchemaInfo|geonet:child[string(@name)='applicationSchemaInfo']
      |gmd:metadataMaintenance|geonet:child[string(@name)='metadataMaintenance']">
      <xsl:with-param name="schema" select="$schema"/>
      <xsl:with-param name="edit"   select="$edit"/>
    </xsl:apply-templates>

    <xsl:call-template name="complexElementGuiWrapper">
      <xsl:with-param name="title" select="'Metadata Info'"/>
      <xsl:with-param name="content">

        <xsl:apply-templates mode="elementEP" select="gmd:fileIdentifier|geonet:child[string(@name)='fileIdentifier']
          |gmd:language|geonet:child[string(@name)='language']
          |gmd:characterSet|geonet:child[string(@name)='characterSet']
          |gmd:parentIdentifier|geonet:child[string(@name)='parentIdentifier']
          |gmd:hierarchyLevel|geonet:child[string(@name)='hierarchyLevel']
          |gmd:hierarchyLevelName|geonet:child[string(@name)='hierarchyLevelName']
          |gmd:dateStamp|geonet:child[string(@name)='dateStamp']
          |gmd:metadataStandardName|geonet:child[string(@name)='metadataStandardName']
          |gmd:metadataStandardVersion|geonet:child[string(@name)='metadataStandardVersion']
          |gmd:contact|geonet:child[string(@name)='contact']
          |gmd:dataSetURI|geonet:child[string(@name)='dataSetURI']
          |gmd:locale|geonet:child[string(@name)='locale']
          |gmd:series|geonet:child[string(@name)='series']
          |gmd:describes|geonet:child[string(@name)='describes']
          |gmd:propertyType|geonet:child[string(@name)='propertyType']
          |gmd:featureType|geonet:child[string(@name)='featureType']
          |gmd:featureAttribute|geonet:child[string(@name)='featureAttribute']
          ">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="edit"   select="$edit"/>
        </xsl:apply-templates>

      </xsl:with-param>
      <xsl:with-param name="schema" select="$schema"/>
      <xsl:with-param name="group" select="/root/gui/strings/metadataTab"/>
      <xsl:with-param name="edit" select="$edit"/>
    </xsl:call-template>

<!-- metadata Extension Information - dead last because its boring and
     can clutter up the rest of the metadata record! -->

    <xsl:apply-templates mode="elementEP" select="gmd:metadataExtensionInfo|geonet:child[string(@name)='metadataExtensionInfo']">
      <xsl:with-param name="schema" select="$schema"/>
      <xsl:with-param name="edit"   select="$edit"/>
    </xsl:apply-templates>

  </xsl:template>
  
  
  <!-- ============================================================================= -->

  <xsl:template name="iso19139Metadata">
    <xsl:param name="schema"/>
    <xsl:param name="edit"/>
    <xsl:param name="flat"/>
    
    <xsl:variable name="ref" select="concat('#_',geonet:element/@ref)"/>
    <xsl:variable name="validationLink">
      <xsl:call-template name="validationLink">
        <xsl:with-param name="ref" select="$ref"/>
      </xsl:call-template>      
    </xsl:variable>
    
    <xsl:call-template name="complexElementGui">
      <xsl:with-param name="title" select="/root/gui/strings/metadata"/>
      <xsl:with-param name="validationLink" select="$validationLink"/>

      <xsl:with-param name="helpLink">
        <xsl:call-template name="getHelpLink">
            <xsl:with-param name="name" select="name(.)"/>
            <xsl:with-param name="schema" select="$schema"/>
        </xsl:call-template>
      </xsl:with-param>
      
      <xsl:with-param name="edit" select="true()"/>
      <xsl:with-param name="content">
    
      <!-- if the parent is root then display fields not in tabs -->
        <xsl:choose>
          <xsl:when test="name(..)='root'">
          <xsl:apply-templates mode="elementEP" select="gmd:fileIdentifier|geonet:child[string(@name)='fileIdentifier']
            |gmd:language|geonet:child[string(@name)='language']
            |gmd:characterSet|geonet:child[string(@name)='characterSet']
            |gmd:parentIdentifier|geonet:child[string(@name)='parentIdentifier']
            |gmd:hierarchyLevel|geonet:child[string(@name)='hierarchyLevel']
            |gmd:hierarchyLevelName|geonet:child[string(@name)='hierarchyLevelName']
            |gmd:dateStamp|geonet:child[string(@name)='dateStamp']
            |gmd:metadataStandardName|geonet:child[string(@name)='metadataStandardName']
            |gmd:metadataStandardVersion|geonet:child[string(@name)='metadataStandardVersion']
            |gmd:contact|geonet:child[string(@name)='contact']
            |gmd:dataSetURI|geonet:child[string(@name)='dataSetURI']
            |gmd:locale|geonet:child[string(@name)='locale']
            |gmd:series|geonet:child[string(@name)='series']
            |gmd:describes|geonet:child[string(@name)='describes']
            |gmd:propertyType|geonet:child[string(@name)='propertyType']
            |gmd:featureType|geonet:child[string(@name)='featureType']
            |gmd:featureAttribute|geonet:child[string(@name)='featureAttribute']
            ">
            <xsl:with-param name="schema" select="$schema"/>
            <xsl:with-param name="edit"   select="$edit"/>
            <xsl:with-param name="flat"   select="$flat"/>
          </xsl:apply-templates>
        </xsl:when>
        <!-- otherwise, display everything because we have embedded MD_Metadata -->
        <xsl:otherwise>
          <xsl:apply-templates mode="elementEP" select="*">
            <xsl:with-param name="schema" select="$schema"/>
            <xsl:with-param name="edit"   select="$edit"/>
          </xsl:apply-templates>
        </xsl:otherwise>
      </xsl:choose>

      </xsl:with-param>
      <xsl:with-param name="schema" select="$schema"/>
    </xsl:call-template>
    
  </xsl:template>
  
  <!-- ============================================================================= -->
  <!--
  simple mode; ISO order is:
  - gmd:fileIdentifier
  - gmd:language
  - gmd:characterSet
  - gmd:parentIdentifier
  - gmd:hierarchyLevel
  - gmd:hierarchyLevelName
  - gmd:contact
  - gmd:dateStamp
  - gmd:metadataStandardName
  - gmd:metadataStandardVersion
  + gmd:dataSetURI
  + gmd:locale
  - gmd:spatialRepresentationInfo
  - gmd:referenceSystemInfo
  - gmd:metadataExtensionInfo
  - gmd:identificationInfo
  - gmd:contentInfo
  - gmd:distributionInfo
  - gmd:dataQualityInfo
  - gmd:portrayalCatalogueInfo
  - gmd:metadataConstraints
  - gmd:applicationSchemaInfo
  - gmd:metadataMaintenance
  + gmd:series
  + gmd:describes
  + gmd:propertyType
  + gmd:featureType
  + gmd:featureAttribute
  -->
  <!-- ============================================================================= -->

  <xsl:template name="iso19139Simple">
    <xsl:param name="schema"/>
    <xsl:param name="edit"/>
    <xsl:param name="flat"/>


    <xsl:apply-templates mode="elementEP" select="gmd:identificationInfo|geonet:child[string(@name)='identificationInfo']">
      <xsl:with-param name="schema" select="$schema"/>
      <xsl:with-param name="edit"   select="$edit"/>
      <xsl:with-param name="flat"   select="$flat"/>
    </xsl:apply-templates>
    
    <xsl:apply-templates mode="elementEP" select="gmd:distributionInfo|geonet:child[string(@name)='distributionInfo']">
      <xsl:with-param name="schema" select="$schema"/>
      <xsl:with-param name="edit"   select="$edit"/>
      <xsl:with-param name="flat"   select="$flat"/>
    </xsl:apply-templates>
    
    <xsl:apply-templates mode="elementEP" select="gmd:spatialRepresentationInfo|geonet:child[string(@name)='spatialRepresentationInfo']">
      <xsl:with-param name="schema" select="$schema"/>
      <xsl:with-param name="edit"   select="$edit"/>
      <xsl:with-param name="flat"   select="$flat"/>
    </xsl:apply-templates>
    
    <xsl:apply-templates mode="elementEP" select="gmd:referenceSystemInfo|geonet:child[string(@name)='referenceSystemInfo']">
      <xsl:with-param name="schema" select="$schema"/>
      <xsl:with-param name="edit"   select="$edit"/>
      <xsl:with-param name="flat"   select="$flat"/>
    </xsl:apply-templates>
    
    <xsl:apply-templates mode="elementEP" select="gmd:applicationSchemaInfo|geonet:child[string(@name)='applicationSchemaInfo']">
      <xsl:with-param name="schema" select="$schema"/>
      <xsl:with-param name="edit"   select="$edit"/>
      <xsl:with-param name="flat"   select="$flat"/>
    </xsl:apply-templates>
    
    <xsl:apply-templates mode="elementEP" select="gmd:portrayalCatalogueInfo|geonet:child[string(@name)='portrayalCatalogueInfo']">
      <xsl:with-param name="schema" select="$schema"/>
      <xsl:with-param name="edit"   select="$edit"/>
      <xsl:with-param name="flat"   select="$flat"/>
    </xsl:apply-templates>
    
    <xsl:apply-templates mode="elementEP" select="gmd:dataQualityInfo|geonet:child[string(@name)='dataQualityInfo']">
      <xsl:with-param name="schema" select="$schema"/>
      <xsl:with-param name="edit"   select="$edit"/>
      <xsl:with-param name="flat"   select="$flat"/>
    </xsl:apply-templates>
    
    <xsl:apply-templates mode="elementEP" select="gmd:metadataConstraints|geonet:child[string(@name)='metadataConstraints']">
      <xsl:with-param name="schema" select="$schema"/>
      <xsl:with-param name="edit"   select="$edit"/>
      <xsl:with-param name="flat"   select="$flat"/>
    </xsl:apply-templates>
    
    
    <xsl:call-template name="complexElementGui">
      <xsl:with-param name="title" select="/root/gui/strings/metadata"/>
      <xsl:with-param name="content">
        <xsl:call-template name="iso19139Metadata">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="edit"   select="$edit"/>
          <xsl:with-param name="flat"   select="$flat"/>
        </xsl:call-template>
      </xsl:with-param>
      <xsl:with-param name="schema" select="$schema"/>
    </xsl:call-template>
    
    <xsl:apply-templates mode="elementEP" select="gmd:contentInfo|geonet:child[string(@name)='contentInfo']
      |gmd:metadataExtensionInfo|geonet:child[string(@name)='metadataExtensionInfo']">
      <xsl:with-param name="schema" select="$schema"/>
      <xsl:with-param name="edit"   select="$edit"/>
      <xsl:with-param name="flat"   select="$flat"/>
    </xsl:apply-templates>
    
  </xsl:template>
  
  <!-- ============================================================================= -->

  <xsl:template mode="iso19139" match="//gmd:language[gco:CharacterString]" priority="20">
    <xsl:param name="schema"/>
    <xsl:param name="edit"/>

    <xsl:apply-templates mode="simpleElement" select=".">
      <xsl:with-param name="schema" select="$schema"/>
      <xsl:with-param name="edit"   select="$edit"/>
      <xsl:with-param name="text">
        <xsl:call-template name="iso19139GetIsoLanguage">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="edit"   select="$edit"/>
          <xsl:with-param name="value" select="gco:CharacterString"/>
          <xsl:with-param name="ref" select="concat('_', gco:CharacterString/geonet:element/@ref)"/>
        </xsl:call-template>
      </xsl:with-param>
    </xsl:apply-templates>
  </xsl:template>
  
  <!-- ============================================================================= -->

  <xsl:template name="iso19139GetIsoLanguage" mode="iso19139GetIsoLanguage" match="*">
    <xsl:param name="schema"/>
    <xsl:param name="edit"/>
    <xsl:param name="value"/>
      <xsl:param name="ref"/>
    
    <xsl:variable name="lang"  select="/root/gui/language"/>
    
    <xsl:choose>
      <xsl:when test="$edit=true()">
        <select class="md" name="{$ref}" size="1">
          <option name=""/>

          <xsl:for-each select="/root/gui/isoLang/record">
            <xsl:sort select="label/child::*[name() = $lang]"/>
            <option value="{code}">
              <xsl:if test="code = $value">
                <xsl:attribute name="selected"/>
              </xsl:if>
              <xsl:value-of select="label/child::*[name() = $lang]"/>
            </option>
          </xsl:for-each>
        </select>
      </xsl:when>

      <xsl:otherwise>
        <xsl:value-of select="/root/gui/isoLang/record[code=$value]/label/child::*[name() = $lang]"/>
        
        <!-- In view mode display other languages from gmd:locale of gmd:MD_Metadata element -->
        <xsl:if test="../gmd:locale or ../../gmd:locale">
          <xsl:text> (</xsl:text><xsl:value-of select="string(/root/gui/schemas/iso19139/labels/element[@name='gmd:locale' and not(@context)]/label)"/>
          <xsl:text>:</xsl:text>
          <xsl:for-each select="../gmd:locale|../../gmd:locale">
            <xsl:variable name="c" select="gmd:PT_Locale/gmd:languageCode/gmd:LanguageCode/@codeListValue"/>
            <xsl:value-of select="/root/gui/isoLang/record[code=$c]/label/child::*[name() = $lang]"/>
            <xsl:if test="position()!=last()">, </xsl:if>
          </xsl:for-each><xsl:text>)</xsl:text>
        </xsl:if>
       
      </xsl:otherwise>
    </xsl:choose>    
  </xsl:template>


  <!-- ============================================================================= -->
  <!-- FIXME HTML should move to layout -->
  <xsl:template mode="iso19139" match="gmd:transferOptions">
    <xsl:param name="schema"/>
    <xsl:param name="edit"/>

    
    <xsl:if test="$edit=false()">
      <xsl:if test="count(gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource/gmd:protocol/gco:CharacterString[contains(string(.),'download')])>1 and
                  //geonet:info/download='true'">
        <xsl:call-template name="complexElementGui">
          <xsl:with-param name="title" select="/root/gui/strings/downloadSummary"/>
          <xsl:with-param name="content">
            <tr>
              <td  align="center">
                <button class="content" onclick="javascript:runFileDownloadSummary('{//geonet:info/uuid}','{/root/gui/strings/downloadSummary}')" type="button">
                  <xsl:value-of select="/root/gui/strings/showFileDownloadSummary"/>  
                </button>
              </td>
            </tr>
          </xsl:with-param>
          <xsl:with-param name="helpLink">
            <xsl:call-template name="getHelpLink">
              <xsl:with-param name="name"   select="name(.)"/>
              <xsl:with-param name="schema" select="$schema"/>
            </xsl:call-template>
          </xsl:with-param>
          <xsl:with-param name="schema" select="$schema"/>
        </xsl:call-template>
      </xsl:if>
    </xsl:if>
    <xsl:apply-templates mode="complexElement" select=".">
      <xsl:with-param name="schema" select="$schema"/>
      <xsl:with-param name="edit"   select="$edit"/>
    </xsl:apply-templates>
  </xsl:template>
  
  <!-- ============================================================================= 
  Custom element layout
  -->

  <xsl:template mode="iso19139" match="gmd:contact|gmd:pointOfContact|gmd:citedResponsibleParty">
    <xsl:param name="schema"/>
    <xsl:param name="edit"/>

    <xsl:call-template name="contactTemplate">
      <xsl:with-param name="edit" select="$edit"/>
      <xsl:with-param name="schema" select="$schema"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template name="contactTemplate">
    <xsl:param name="schema"/>
    <xsl:param name="edit"/>
    
    <xsl:variable name="content">
      
      <xsl:for-each select="gmd:CI_ResponsibleParty">
        <col>
                  <xsl:apply-templates mode="elementEP" select="../@xlink:href">
                    <xsl:with-param name="schema" select="$schema"/>
                    <xsl:with-param name="edit"   select="$edit"/>
                  </xsl:apply-templates>
                  
                  <xsl:apply-templates mode="elementEP" select="gmd:individualName|geonet:child[string(@name)='individualName']
                    |gmd:organisationName|geonet:child[string(@name)='organisationName']
                    |gmd:positionName|geonet:child[string(@name)='positionName']
                    |gmd:role|geonet:child[string(@name)='role']
                    ">
                    <xsl:with-param name="schema" select="$schema"/>
                    <xsl:with-param name="edit"   select="$edit"/>
                  </xsl:apply-templates>
        </col>
        <col>
                  <xsl:apply-templates mode="elementEP" select="gmd:contactInfo|geonet:child[string(@name)='contactInfo']">
                    <xsl:with-param name="schema" select="$schema"/>
                    <xsl:with-param name="edit"   select="$edit"/>
                  </xsl:apply-templates>
        </col>        
      </xsl:for-each>
    </xsl:variable>
    
    <xsl:apply-templates mode="complexElement" select=".">
      <xsl:with-param name="schema"  select="$schema"/>
      <xsl:with-param name="edit"    select="$edit"/>
      <xsl:with-param name="content">
        <xsl:call-template name="columnElementGui">
          <xsl:with-param name="cols" select="$content"/>
        </xsl:call-template>
      </xsl:with-param>
    </xsl:apply-templates>
    
  </xsl:template>

  <!-- ============================================================================= -->
  <!-- online resources -->
  <!-- ============================================================================= -->

  <xsl:template mode="iso19139" match="gmd:CI_OnlineResource" priority="2">
    <xsl:param name="schema"/>
    <xsl:param name="edit"/>
    
    <xsl:variable name="langId">
      <xsl:call-template name="getLangId">
        <xsl:with-param name="langGui" select="/root/gui/language" />
        <xsl:with-param name="md"
          select="ancestor-or-self::*[name(.)='gmd:MD_Metadata' or @gco:isoType='gmd:MD_Metadata']" />
      </xsl:call-template>
    </xsl:variable>

    <xsl:variable name="linkage" select="gmd:linkage/gmd:URL" />
    <xsl:variable name="name">
      <xsl:for-each select="gmd:name">
        <xsl:call-template name="localised">
          <xsl:with-param name="langId" select="$langId"/>
        </xsl:call-template>
      </xsl:for-each>
    </xsl:variable>
    <xsl:variable name="description">
      <xsl:for-each select="gmd:description">
        <xsl:call-template name="localised">
          <xsl:with-param name="langId" select="$langId"/>
        </xsl:call-template>
      </xsl:for-each>
    </xsl:variable>
    
    <xsl:choose>
      <xsl:when test="$edit=true()">
        <xsl:apply-templates mode="iso19139EditOnlineRes" select=".">
          <xsl:with-param name="schema" select="$schema"/>
        </xsl:apply-templates>
      </xsl:when>
      <xsl:when test="string($linkage)!=''">
        <xsl:apply-templates mode="simpleElement" select=".">
          <xsl:with-param name="schema"  select="$schema"/>
          <xsl:with-param name="text">
            <a href="{$linkage}" target="_new">
              <xsl:choose>
                <xsl:when test="string($name)!=''">
                  <xsl:value-of select="$name"/>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:value-of select="$linkage"/>
                </xsl:otherwise>
              </xsl:choose>
            </a>
            <xsl:if test="string($description)!=''">
              <br/><xsl:value-of select="$description"/>
            </xsl:if>
          </xsl:with-param>
        </xsl:apply-templates>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <!-- ============================================================================= -->

  <xsl:template mode="iso19139EditOnlineRes" match="*">
    <xsl:param name="schema"/>
  
    <xsl:variable name="id" select="generate-id(.)"/>
    <xsl:apply-templates mode="complexElement" select=".">
      <xsl:with-param name="schema" select="$schema"/>
      <xsl:with-param name="edit"   select="true()"/>
      <xsl:with-param name="content">
        
        <xsl:apply-templates mode="elementEP" select="gmd:linkage|geonet:child[string(@name)='linkage']">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="edit"   select="true()"/>
        </xsl:apply-templates>
      
        <!-- use elementEP for geonet:child only -->
        <xsl:apply-templates mode="elementEP" select="geonet:child[string(@name)='protocol']">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="edit"   select="true()"/>
        </xsl:apply-templates>

        <xsl:apply-templates mode="iso19139" select="gmd:protocol">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="edit"   select="true()"/>
        </xsl:apply-templates>
        
        
        <xsl:apply-templates mode="elementEP" select="gmd:applicationProfile|geonet:child[string(@name)='applicationProfile']">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="edit"   select="true()"/>
        </xsl:apply-templates>
        
        <xsl:choose>
          <xsl:when test="matches(gmd:protocol[1]/gco:CharacterString,'^WWW:DOWNLOAD-.*-http--download.*') 
            and string(gmd:name/gco:CharacterString|gmd:name/gmx:MimeFileType)!=''">
            <xsl:apply-templates mode="iso19139FileRemove" select="gmd:name/gco:CharacterString|gmd:name/gmx:MimeFileType">
              <xsl:with-param name="access" select="'private'"/>
              <xsl:with-param name="id" select="$id"/>
            </xsl:apply-templates>
          </xsl:when>
          <xsl:when test="string(gmd:protocol[1]/gco:CharacterString)='DB:POSTGIS' 
            and string(gmd:name/gco:CharacterString|gmd:name/gmx:MimeFileType)!=''">
            <xsl:apply-templates mode="iso19139GeoPublisher" select="gmd:name/gco:CharacterString">
            <xsl:with-param name="access" select="'db'"/>
            <xsl:with-param name="id" select="$id"/>
            </xsl:apply-templates>
          </xsl:when>
          <xsl:when test="(string(gmd:protocol[1]/gco:CharacterString)='FILE:GEO'
            or string(gmd:protocol[1]/gco:CharacterString)='FILE:RASTER') 
            and string(gmd:linkage/gmd:URL)!=''">
            <xsl:apply-templates mode="iso19139GeoPublisher" select="gmd:name/gco:CharacterString">
              <xsl:with-param name="access" select="'fileOrUrl'"/>
              <xsl:with-param name="id" select="$id"/>
            </xsl:apply-templates>
          </xsl:when>
          <xsl:otherwise>
            <!-- use elementEP for geonet:child only -->
            <xsl:apply-templates mode="elementEP" select="geonet:child[string(@name)='name']">
              <xsl:with-param name="schema" select="$schema"/>
              <xsl:with-param name="edit"   select="true()"/>
            </xsl:apply-templates>

            <xsl:apply-templates mode="iso19139" select="gmd:name">
              <xsl:with-param name="schema" select="$schema"/>
              <xsl:with-param name="edit"   select="true()"/>
            </xsl:apply-templates>
          </xsl:otherwise>
        </xsl:choose>

        <xsl:apply-templates mode="elementEP" select="gmd:description|geonet:child[string(@name)='description']">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="edit"   select="true()"/>
        </xsl:apply-templates>
        
        <xsl:apply-templates mode="elementEP" select="gmd:function|geonet:child[string(@name)='function']">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="edit"   select="true()"/>
        </xsl:apply-templates>
      </xsl:with-param>
    </xsl:apply-templates>
  </xsl:template>
  
  <!-- ============================================================================= -->
  <!-- online resources: WMS get map -->
  <!-- ============================================================================= -->

  <xsl:template mode="iso19139" match="gmd:CI_OnlineResource[
               starts-with(gmd:protocol/gco:CharacterString,'OGC:WMS') or
               starts-with(gmd:protocol/gco:CharacterString,'OGC:WMC') or
               starts-with(gmd:protocol/gco:CharacterString,'OGC:OWS')]" priority="2">
    <xsl:param name="schema"/>
    <xsl:param name="edit"/>
    <xsl:variable name="metadata_id" select="//geonet:info/id" />
    <xsl:variable name="linkage" select="gmd:linkage/gmd:URL" />
    <xsl:variable name="name" select="normalize-space(gmd:name/gco:CharacterString|gmd:name/gmx:MimeFileType)" />
    <xsl:variable name="description" select="normalize-space(gmd:description/gco:CharacterString)" />

    <xsl:choose>
      <xsl:when test="$edit=true()">
        <xsl:apply-templates mode="iso19139EditOnlineRes" select=".">
          <xsl:with-param name="schema" select="$schema"/>
        </xsl:apply-templates>
      </xsl:when>
      <xsl:when test="string(//geonet:info/dynamic)='true' and string($name)!='' and string($linkage)!=''">
        <!-- Create a link for a WMS service that will open in the map viewer -->
        <xsl:apply-templates mode="simpleElement" select=".">
          <xsl:with-param name="schema"  select="$schema"/>
          <xsl:with-param name="title"  select="/root/gui/strings/interactiveMap"/>
          <xsl:with-param name="text">
            <xsl:variable name="fn">
              <xsl:choose>
                <xsl:when test="starts-with(gmd:protocol/gco:CharacterString,'OGC:WMC') or
               starts-with(gmd:protocol/gco:CharacterString,'OGC:OWS')">
                  <xsl:value-of select="concat('app.getIMap().addWMC(''', $linkage, ''')')"/>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:value-of select="concat('app.getIMap().addWMSLayer([[''', $name, ''',''', $linkage, ''',''', $name, ''',''', $metadata_id, ''']])')"/>
                </xsl:otherwise>
              </xsl:choose>
            </xsl:variable>

            <a href="javascript:{$fn}" title="{/root/strings/interactiveMap}">
              <xsl:choose>
                <xsl:when test="string($description)!=''">
                  <xsl:value-of select="$description"/>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:value-of select="$name"/>
                </xsl:otherwise>
              </xsl:choose>
            </a><br/>(URL : <xsl:value-of select="$linkage"/> )
          </xsl:with-param>
        </xsl:apply-templates>
        <!-- Create a link for a WMS service that will open in Google Earth through the reflector -->
        <xsl:if test="starts-with(gmd:protocol/gco:CharacterString,'OGC:WMS')">
          <xsl:apply-templates mode="simpleElement" select=".">
            <xsl:with-param name="schema"  select="$schema"/>
            <xsl:with-param name="title"  select="/root/gui/strings/viewInGE"/>
            <xsl:with-param name="text">
              <a href="{/root/gui/locService}/google.kml?uuid={//geonet:info/uuid}&amp;layers={$name}" title="{/root/strings/interactiveMap}">
                <xsl:choose>
                  <xsl:when test="string($description)!=''">
                    <xsl:value-of select="$description"/>
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:value-of select="$name"/>
                  </xsl:otherwise>
                </xsl:choose>
                &#160;
                <img src="{/root/gui/url}/images/google_earth_link.gif" height="20px" width="20px" alt="{/root/gui/strings/viewInGE}" title="{/root/gui/strings/viewInGE}" style="border: 0px solid;"/>
              </a>
            </xsl:with-param>
          </xsl:apply-templates>
        </xsl:if>
      </xsl:when>
    </xsl:choose>
  </xsl:template>
  <!-- ============================================================================= -->
  <!-- online resources: WMS get capabilities -->
  <!-- ============================================================================= -->

  <xsl:template mode="iso19139" match="gmd:CI_OnlineResource[starts-with(gmd:protocol/gco:CharacterString,'OGC:WMS-') and contains(gmd:protocol/gco:CharacterString,'-get-capabilities') and gmd:name]" priority="2">
    <xsl:param name="schema"/>
    <xsl:param name="edit"/>
    <xsl:variable name="linkage" select="gmd:linkage/gmd:URL" />
    <xsl:variable name="name" select="normalize-space(gmd:name/gco:CharacterString|gmd:name/gmx:MimeFileType)" />
    <xsl:variable name="description" select="normalize-space(gmd:description/gco:CharacterString)" />
    
    <xsl:choose>
      <xsl:when test="$edit=true()">
        <xsl:apply-templates mode="iso19139EditOnlineRes" select=".">
          <xsl:with-param name="schema" select="$schema"/>
        </xsl:apply-templates>
      </xsl:when>
      <xsl:when test="string(//geonet:info/dynamic)='true' and string($linkage)!=''">
        <xsl:apply-templates mode="simpleElement" select=".">
          <xsl:with-param name="schema"  select="$schema"/>
          <xsl:with-param name="title"  select="/root/gui/strings/interactiveMap"/>
          <xsl:with-param name="text">
            <a href="javascript:runIM_selectService('{$linkage}',2,{//geonet:info/id})" title="{/root/strings/interactiveMap}">              
              <xsl:choose>
                <xsl:when test="string($description)!=''">
                  <xsl:value-of select="$description"/>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:value-of select="$name"/>
                </xsl:otherwise>
              </xsl:choose>
            </a>
          </xsl:with-param>
        </xsl:apply-templates>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <!-- ============================================================================= -->
  <!-- online resources: download -->
  <!-- ============================================================================= -->

  <xsl:template mode="iso19139" match="gmd:CI_OnlineResource[matches(gmd:protocol/gco:CharacterString,'^WWW:DOWNLOAD-.*-http--download.*') and gmd:name]" priority="2">
    <xsl:param name="schema"/>
    <xsl:param name="edit"/>
    <xsl:variable name="download_check"><xsl:text>&amp;fname=&amp;access</xsl:text></xsl:variable>
    <xsl:variable name="linkage" select="gmd:linkage/gmd:URL" />
    <xsl:variable name="name" select="normalize-space(gmd:name/gco:CharacterString|gmd:name/gmx:MimeFileType)" />
    <xsl:variable name="description" select="normalize-space(gmd:description/gco:CharacterString)" />
    
    <xsl:choose>
      <xsl:when test="$edit=true()">
        <xsl:apply-templates mode="iso19139EditOnlineRes" select=".">
          <xsl:with-param name="schema" select="$schema"/>
        </xsl:apply-templates>
      </xsl:when>
      <xsl:when test="string(//geonet:info/download)='true' and string($linkage)!='' and not(contains($linkage,$download_check))">
        <xsl:apply-templates mode="simpleElement" select=".">
          <xsl:with-param name="schema"  select="$schema"/>
          <xsl:with-param name="title"  select="/root/gui/strings/downloadData"/>
          <xsl:with-param name="text">
            <xsl:variable name="title">
              <xsl:choose>
                <xsl:when test="string($description)!=''">
                  <xsl:value-of select="$description"/>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:value-of select="$name"/>
                </xsl:otherwise>
              </xsl:choose>
            </xsl:variable>
            <a href="{$linkage}" title="{$title}" onclick="runFileDownload(this.href, this.title); return false;"><xsl:value-of select="$title"/></a>
          </xsl:with-param>
        </xsl:apply-templates>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <!-- ============================================================================= -->
  <!-- protocol -->
  <!-- ============================================================================= -->

  <xsl:template mode="iso19139" match="gmd:protocol" priority="2">
    <xsl:param name="schema"/>
    <xsl:param name="edit"/>
    
    <xsl:choose>
      <xsl:when test="$edit=true()">
        <xsl:call-template name="simpleElementGui">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="edit" select="$edit"/>
          <xsl:with-param name="title">
            <xsl:call-template name="getTitle">
              <xsl:with-param name="name"   select="name(.)"/>
              <xsl:with-param name="schema" select="$schema"/>
            </xsl:call-template>
          </xsl:with-param>

          <xsl:with-param name="helpLink">
              <xsl:call-template name="getHelpLink">
                  <xsl:with-param name="name" select="name(.)"/>
                  <xsl:with-param name="schema" select="$schema"/>
              </xsl:call-template>
          </xsl:with-param>
                
          <xsl:with-param name="text">
            <xsl:variable name="value" select="string(gco:CharacterString)"/>
            <xsl:variable name="ref" select="gco:CharacterString/geonet:element/@ref"/>
            <xsl:variable name="isXLinked" select="count(ancestor-or-self::node()[@xlink:href]) > 0"/>
            <xsl:variable name="fref" select="../gmd:name/gco:CharacterString/geonet:element/@ref|../gmd:name/gmx:MimeFileType/geonet:element/@ref"/>
            
            <!-- Look for the helper to check if a radio edit mode is activated
            If yes, hide the input text which will be updated when clicking the radio
            or the other option. -->
            <xsl:variable name="helper" select="geonet:getHelper($schema, ., /root/gui)"/>
            
            <xsl:variable name="relatedJsAction">
              <xsl:value-of select="concat('checkForFileUpload(&quot;',$fref,'&quot;, &quot;',$ref,'&quot;, ', 
                (if (contains($helper/@editorMode, 'radio')) then 'this.value' else 'this.options[this.selectedIndex].value')
                , ');')" />
            </xsl:variable>
            
            <input type="text" id="_{$ref}" name="_{$ref}" value="{$value}">
              <xsl:if test="contains($helper/@editorMode, 'radio')">
                <xsl:attribute name="style">display:none;</xsl:attribute>
              </xsl:if>
            </input>
            <xsl:for-each select="gco:CharacterString">
             <xsl:call-template name="helper">
               <xsl:with-param name="schema" select="$schema"/>
               <xsl:with-param name="attribute" select="false()"/>
               <xsl:with-param name="jsAction" select="$relatedJsAction"/>
             </xsl:call-template>
           </xsl:for-each>
          </xsl:with-param>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates mode="element" select=".">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="edit"   select="false()"/>
        </xsl:apply-templates>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- ===================================================================== -->
  <!-- name for onlineresource only -->
  <!-- ===================================================================== -->

  <xsl:template mode="iso19139" match="gmd:name[name(..)='gmd:CI_OnlineResource']" priority="2">
    <xsl:param name="schema"/>
    <xsl:param name="edit"/>

    <xsl:choose>
      <xsl:when test="$edit=true()">
        <xsl:variable name="protocol" select="../gmd:protocol/gco:CharacterString"/>
        <xsl:variable name="pref" select="../gmd:protocol/gco:CharacterString/geonet:element/@ref"/>
        <xsl:variable name="ref" select="gco:CharacterString/geonet:element/@ref|gmx:MimeFileType/geonet:element/@ref"/>
        <xsl:variable name="value" select="gco:CharacterString|gmx:MimeFileType"/>
        <xsl:variable name="button" select="matches($protocol,'^WWW:DOWNLOAD-.*-http--download.*') and normalize-space($value)=''"/>

        <xsl:call-template name="simpleElementGui">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="edit" select="$edit"/>
          <xsl:with-param name="title" select="/root/gui/strings/file"/>
          <xsl:with-param name="text">
            <button class="content" onclick="Ext.getCmp('editorPanel').showFileUploadPanel({//geonet:info/id}, '{$ref}');" type="button">
              <xsl:value-of select="/root/gui/strings/insertFileMode"/>
            </button>
          </xsl:with-param>
          <xsl:with-param name="id" select="concat('db_',$ref)"/>
          <xsl:with-param name="visible" select="$button"/>
        </xsl:call-template>

        <xsl:call-template name="simpleElementGui">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="edit" select="$edit"/>
          <xsl:with-param name="title">
            <xsl:call-template name="getTitle">
              <xsl:with-param name="name"   select="name(.)"/>
              <xsl:with-param name="schema" select="$schema"/>
            </xsl:call-template>
          </xsl:with-param>
          <xsl:with-param name="text">
            <input id="_{$ref}" class="md" type="text" name="_{$ref}" value="{$value}" size="40" />
            </xsl:with-param>
          <xsl:with-param name="id" select="concat('di_',$ref)"/>
          <xsl:with-param name="visible" select="not($button)"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates mode="element" select=".">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="edit"   select="false()"/>
        </xsl:apply-templates>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- ============================================================================= -->

  <xsl:template mode="iso19139FileRemove" match="*">
    <xsl:param name="access" select="'public'"/>
    <xsl:param name="id"/>
    <xsl:param name="geo" select="true()"/>
  
    <xsl:call-template name="simpleElementGui">
      <xsl:with-param name="title" select="/root/gui/strings/file"/>
      <xsl:with-param name="text">
        <table width="100%"><tr>
          <xsl:variable name="ref" select="geonet:element/@ref"/>
          <xsl:variable name="value" select="string(.)"/>
          <td width="70%"><xsl:value-of select="$value"/></td>
          <td align="right">
            <input type="hidden" id="_{$ref}" value="{$value}"/>
            <button type="button" onclick="javascript:doFileRemoveAction('{/root/gui/locService}/resources.del.new','{$ref}','{$access}','{$id}')"><xsl:value-of select="/root/gui/strings/remove"/></button>
            <xsl:if test="$geo">
              <xsl:call-template name="iso19139GeoPublisherButton">
                <xsl:with-param name="access" select="$access"/>
              </xsl:call-template>
            </xsl:if>
          </td>
        </tr></table>
      </xsl:with-param>
      <xsl:with-param name="schema"/>
    </xsl:call-template>
  </xsl:template>


  <!-- Add button for publication in GeoServer -->
  <xsl:template mode="iso19139GeoPublisher" match="*">
    <xsl:param name="access" select="'public'"/>
    <xsl:param name="id"/>
    <xsl:if test="/root/gui/config/editor-geopublisher">
      <xsl:call-template name="simpleElementGui">
        <xsl:with-param name="title" select="/root/gui/strings/file"/>
        <xsl:with-param name="text">
          <table width="100%"><tr>
            <xsl:variable name="ref" select="geonet:element/@ref"/>
            <xsl:variable name="value" select="string(.)"/>
            
            <td width="70%">
              <input id="_{$ref}" class="md" type="text" name="_{$ref}" value="{.}" size="40" />
            </td>
            <td align="right">
              <xsl:call-template name="iso19139GeoPublisherButton">
                <xsl:with-param name="access" select="$access"/>
              </xsl:call-template>
            </td>
          </tr></table>
        </xsl:with-param>
        <xsl:with-param name="schema"/>
      </xsl:call-template>
    </xsl:if>
  </xsl:template>
  
  
  <xsl:template name="iso19139GeoPublisherButton">
    <xsl:param name="access" select="'public'"/>
    
    <xsl:if test="/root/gui/config/editor-geopublisher">
      <xsl:variable name="bbox">
        <xsl:call-template name="iso19139-global-bbox"/>
      </xsl:variable>
      <xsl:variable name="layer">
        <xsl:choose>
          <xsl:when test="../../gmd:protocol/gco:CharacterString='DB:POSTGIS'">
            <xsl:value-of select="concat(../../gmd:linkage/gmd:URL, '#', .)"/>
          </xsl:when>
          <xsl:when test="../../gmd:protocol/gco:CharacterString='FILE:GEO'
            or ../../gmd:protocol/gco:CharacterString='FILE:RASTER'">
            <xsl:value-of select="../../gmd:linkage/gmd:URL"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="."/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>
      
      <xsl:variable name="title">
        <xsl:apply-templates mode="escapeXMLEntities" select="/root/gmd:MD_Metadata/gmd:identificationInfo/*/gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString">
          <xsl:with-param name="includingCRLF" select="true()"/>
        </xsl:apply-templates>
      </xsl:variable>
    
      <xsl:variable name="abstract">
        <xsl:apply-templates mode="escapeXMLEntities" select="/root/gmd:MD_Metadata/gmd:identificationInfo/*/gmd:abstract/gco:CharacterString">
          <xsl:with-param name="includingCRLF" select="true()"/>
        </xsl:apply-templates>
      </xsl:variable>
    
      <button type="button" class="content repository" 
        onclick="javascript:Ext.getCmp('editorPanel').showGeoPublisherPanel('{/root/*/geonet:info/id}',
        '{/root/*/geonet:info/uuid}', 
        '{$title}',
        '{$abstract}',
        '{$layer}', 
        '{$access}', 'gmd:onLine', '{ancestor::gmd:MD_DigitalTransferOptions/geonet:element/@ref}', [{$bbox}]);" 
        alt="{/root/gui/strings/publishHelp}" 
        title="{/root/gui/strings/geopublisherHelp}"><xsl:value-of select="/root/gui/strings/geopublisher"/></button>
    </xsl:if>
  </xsl:template>
  
  
  <!-- ===================================================================== -->
  <!-- === iso19139 brief formatting === -->
  <!-- ===================================================================== -->
  <xsl:template mode="superBrief" match="gmd:MD_Metadata|*[@gco:isoType='gmd:MD_Metadata']" priority="2">
    <xsl:variable name="langId">
      <xsl:call-template name="getLangId">
        <xsl:with-param name="langGui" select="/root/gui/language"/>
        <xsl:with-param name="md" select="."/>
      </xsl:call-template>
    </xsl:variable>
    
    <id><xsl:value-of select="geonet:info/id"/></id>
    <uuid><xsl:value-of select="geonet:info/uuid"/></uuid>
    <title>
      <xsl:apply-templates mode="localised" select="gmd:identificationInfo/*/gmd:citation/*/gmd:title">
        <xsl:with-param name="langId" select="$langId"/>
      </xsl:apply-templates>
    </title>
    <abstract>
      <xsl:apply-templates mode="localised" select="gmd:identificationInfo/*/gmd:abstract">
        <xsl:with-param name="langId" select="$langId"/>
      </xsl:apply-templates>
    </abstract>
  </xsl:template>    
  
  <xsl:template match="iso19139Brief">
    <metadata>
     <xsl:call-template name="iso19139-brief"/>
    </metadata>
  </xsl:template>

  <xsl:template name="iso19139-brief">
      <xsl:variable name="download_check"><xsl:text>&amp;fname=&amp;access</xsl:text></xsl:variable>
      <xsl:variable name="info" select="geonet:info"/>
      <xsl:variable name="id" select="$info/id"/>
      <xsl:variable name="uuid" select="$info/uuid"/>

      <xsl:if test="normalize-space(gmd:parentIdentifier/*)!=''">
        <parentId><xsl:value-of select="gmd:parentIdentifier/*"/></parentId>
      </xsl:if>

      <xsl:variable name="langId">
        <xsl:call-template name="getLangId">
          <xsl:with-param name="langGui" select="/root/gui/language"/>
          <xsl:with-param name="md" select="."/>
        </xsl:call-template>
      </xsl:variable>
      
      <xsl:apply-templates mode="briefster" select="gmd:identificationInfo/*">
        <xsl:with-param name="id" select="$id"/>
        <xsl:with-param name="langId" select="$langId"/>
        <xsl:with-param name="info" select="$info"/>
      </xsl:apply-templates>

      <xsl:for-each select="gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource">
        <xsl:variable name="protocol" select="gmd:protocol[1]/gco:CharacterString"/>
        <xsl:variable name="linkage"  select="normalize-space(gmd:linkage/gmd:URL)"/>
        <xsl:variable name="name">
          <xsl:for-each select="gmd:name">
            <xsl:call-template name="localised">
              <xsl:with-param name="langId" select="$langId"/>
            </xsl:call-template>
          </xsl:for-each>
        </xsl:variable>

        <xsl:variable name="mimeType" select="normalize-space(gmd:name/gmx:MimeFileType/@type)"/>
        
        <xsl:variable name="desc">
          <xsl:for-each select="gmd:description">
            <xsl:call-template name="localised">
              <xsl:with-param name="langId" select="$langId"/>
            </xsl:call-template>
          </xsl:for-each>
        </xsl:variable>
        
        <xsl:if test="string($linkage)!=''">
            <xsl:element name="link">
              <xsl:attribute name="title" select="$desc"/>
              <xsl:attribute name="href" select="$linkage"/>
              <xsl:attribute name="name" select="$name"/>
              <xsl:attribute name="protocol" select="$protocol"/>
              <xsl:attribute name="type" select="geonet:protocolMimeType($linkage, $protocol, $mimeType)"/>
            </xsl:element>

        </xsl:if>

        <!-- Generate a KML output link for a WMS service -->
        <xsl:if test="string($linkage)!='' and starts-with($protocol,'OGC:WMS-') and contains($protocol,'-get-map') and string($linkage)!='' and string($name)!=''">
          
          <xsl:element name="link">
            <xsl:attribute name="title"><xsl:value-of select="$desc"/></xsl:attribute>
            <xsl:attribute name="href">
              <xsl:value-of select="concat('http://',/root/gui/env/server/host,':',/root/gui/env/server/port,/root/gui/locService,'/google.kml?uuid=',$uuid,'&amp;layers=',$name)"/>
            </xsl:attribute>
            <xsl:attribute name="name"><xsl:value-of select="$name"/></xsl:attribute>
            <xsl:attribute name="type">application/vnd.google-earth.kml+xml</xsl:attribute>
          </xsl:element>
        </xsl:if>

        <!-- The old links still in use by some systems. Deprecated -->
        <xsl:choose>
          <xsl:when test="matches($protocol,'^WWW:DOWNLOAD-.*-http--download.*') and not(contains($linkage,$download_check))">
            <link type="download"><xsl:value-of select="$linkage"/></link>
          </xsl:when>
          <xsl:when test="starts-with($protocol,'OGC:WMS-') and contains($protocol,'-get-map') and string($linkage)!='' and string($name)!=''">
            <link type="wms">
              <xsl:value-of select="concat('javascript:addWMSLayer([[&#34;' , $name , '&#34;,&#34;' ,  $linkage  ,  '&#34;, &#34;', $name  ,'&#34;,&#34;',$id,'&#34;]])')"/>
            </link>
            <link type="googleearth">
              <xsl:value-of select="concat(/root/gui/locService,'/google.kml?uuid=',$uuid,'&amp;layers=',$name)"/>
            </link>
          </xsl:when>
          <xsl:when test="starts-with($protocol,'OGC:WMS-') and contains($protocol,'-get-capabilities') and string($linkage)!=''">
            <link type="wms">
              <!--xsl:value-of select="concat('javascript:runIM_selectService(&#34;'  ,  $linkage  ,  '&#34;, 2,',$id,')' )"/-->
              <xsl:value-of select="concat('javascript:addWMSLayer([[&#34;' , $name , '&#34;,&#34;' ,  $linkage  ,  '&#34;, &#34;', $name  ,'&#34;,&#34;',$id,'&#34;]])')"/>            
            </link>
          </xsl:when>
          <xsl:when test="string($linkage)!=''">
            <link type="url"><xsl:value-of select="$linkage"/></link>
          </xsl:when>
          
        </xsl:choose>
      </xsl:for-each>

      <xsl:for-each select="gmd:contact/*">
        <xsl:variable name="role" select="gmd:role/*/@codeListValue"/>
        <xsl:if test="normalize-space($role)!=''">
          <responsibleParty role="{$role}" appliesTo="metadata">
            <xsl:apply-templates mode="responsiblepartysimple" select="."/>
          </responsibleParty>
        </xsl:if>
      </xsl:for-each>

      <metadatacreationdate>
        <xsl:value-of select="gmd:dateStamp/*"/>
      </metadatacreationdate>
      <geonet:info>
        <xsl:copy-of select="geonet:info/*[name(.)!='edit']"/>
        <xsl:choose>
          <xsl:when test="/root/gui/env/harvester/enableEditing='false' and geonet:info/isHarvested='y' and geonet:info/edit='true'">
            <edit>false</edit>
          </xsl:when>
          <xsl:otherwise>
            <xsl:copy-of select="geonet:info/edit"/>
          </xsl:otherwise>
        </xsl:choose>
        
         
        <!-- 
          Internal category could be define using different informations
        in a metadata record (according to standard). This could be improved.
        This type of categories could be added to Lucene index also in order
        to be queriable. 
        Services and datasets are at least the required internal categories
        to be distinguished for INSPIRE requirements (hierarchyLevel could be
        use also). TODO
        -->
        <category internal="true">
          <xsl:choose>
            <xsl:when test="gmd:identificationInfo/srv:SV_ServiceIdentification">service</xsl:when>
            <xsl:otherwise>dataset</xsl:otherwise>
          </xsl:choose>
        </category>
      </geonet:info>
  </xsl:template>

  <xsl:template mode="briefster" match="*">
    <xsl:param name="id"/>
    <xsl:param name="langId"/>
    <xsl:param name="info"/>
    
      <xsl:if test="gmd:citation/gmd:CI_Citation/gmd:title">
        <title>
          <xsl:apply-templates mode="localised" select="gmd:citation/gmd:CI_Citation/gmd:title">
            <xsl:with-param name="langId" select="$langId"></xsl:with-param>
          </xsl:apply-templates>
        </title>
      </xsl:if>

      <xsl:if test="gmd:citation/*/gmd:date/*/gmd:dateType/*[@codeListValue='creation']">
        <datasetcreationdate>
          <xsl:value-of select="gmd:citation/*/gmd:date/*/gmd:date/gco:DateTime"/>
        </datasetcreationdate>
      </xsl:if>
      
      <xsl:if test="gmd:abstract">
        <abstract>
          <xsl:apply-templates mode="localised" select="gmd:abstract">
            <xsl:with-param name="langId" select="$langId"></xsl:with-param>
          </xsl:apply-templates>
        </abstract>
      </xsl:if>

      <xsl:for-each select=".//gmd:keyword[not(@gco:nilReason)]">
        <keyword>
          <xsl:apply-templates mode="localised" select=".">
            <xsl:with-param name="langId" select="$langId"></xsl:with-param>
          </xsl:apply-templates>
        </keyword>
      </xsl:for-each>
    
      <xsl:for-each select="gmd:extent/*/gmd:geographicElement/gmd:EX_GeographicBoundingBox">
        <geoBox>
          <westBL><xsl:value-of select="gmd:westBoundLongitude"/></westBL>
          <eastBL><xsl:value-of select="gmd:eastBoundLongitude"/></eastBL>
          <southBL><xsl:value-of select="gmd:southBoundLatitude"/></southBL>
          <northBL><xsl:value-of select="gmd:northBoundLatitude"/></northBL>
        </geoBox>
      </xsl:for-each>

      <xsl:for-each select="*/gmd:MD_Constraints/*">
        <Constraints preformatted="true">
          <xsl:apply-templates mode="iso19139" select=".">
            <xsl:with-param name="schema" select="$info/schema"/>
            <xsl:with-param name="edit" select="false()"/>
          </xsl:apply-templates>
        </Constraints>
        <Constraints preformatted="false">
          <xsl:copy-of select="."/>
        </Constraints>
      </xsl:for-each>

      <xsl:for-each select="*/gmd:MD_SecurityConstraints/*">
        <SecurityConstraints preformatted="true">
          <xsl:apply-templates mode="iso19139" select=".">
            <xsl:with-param name="schema" select="$info/schema"/>
            <xsl:with-param name="edit" select="false()"/>
          </xsl:apply-templates>
        </SecurityConstraints>
        <SecurityConstraints preformatted="false">
          <xsl:copy-of select="."/>
        </SecurityConstraints>
      </xsl:for-each>

      <xsl:for-each select="*/gmd:MD_LegalConstraints/*">
        <LegalConstraints preformatted="true">
          <xsl:apply-templates mode="iso19139" select=".">
            <xsl:with-param name="schema" select="$info/schema"/>
            <xsl:with-param name="edit" select="false()"/>
          </xsl:apply-templates>
        </LegalConstraints>
        <LegalConstraints preformatted="false">
          <xsl:copy-of select="."/>
        </LegalConstraints>
      </xsl:for-each>

      <xsl:for-each select="gmd:extent/*/gmd:temporalElement/*/gmd:extent/gml:TimePeriod">
        <temporalExtent>
          <begin><xsl:apply-templates mode="brieftime" select="gml:beginPosition|gml:begin/gml:TimeInstant/gml:timePosition"/></begin>
          <end><xsl:apply-templates mode="brieftime" select="gml:endPosition|gml:end/gml:TimeInstant/gml:timePosition"/></end>
        </temporalExtent>
      </xsl:for-each>

      <xsl:if test="not($info/server)">
        <xsl:for-each select="gmd:graphicOverview/gmd:MD_BrowseGraphic">
          <xsl:variable name="fileName"  select="gmd:fileName/gco:CharacterString"/>
          <xsl:if test="$fileName != ''">
            <xsl:variable name="fileDescr" select="gmd:fileDescription/gco:CharacterString"/>
            <xsl:choose>

              <!-- the thumbnail is an url -->

              <xsl:when test="contains($fileName ,'://')">
                <image type="unknown"><xsl:value-of select="$fileName"/></image>                
              </xsl:when>

              <!-- small thumbnail -->

              <xsl:when test="string($fileDescr)='thumbnail'">
                <xsl:choose>
                  <xsl:when test="$info/isHarvested = 'y'">
                    <xsl:choose>
                      <xsl:when test="$info/harvestInfo/smallThumbnail">
                        <image type="thumbnail">
                          <xsl:value-of select="concat($info/harvestInfo/smallThumbnail, $fileName)"/>
                        </image>
                      </xsl:when>
                      <xsl:otherwise>
                        <!-- When harvested, thumbnail is stored in local node (eg. ogcwxs). 
                        Only GeoNetHarvester set smallThumbnail elements.
                        -->
                        <image type="thumbnail">
                          <xsl:value-of select="concat(/root/gui/locService,'/resources.get?id=',$id,'&amp;fname=',$fileName,'&amp;access=public')"/>
                        </image>
                      </xsl:otherwise>
                    </xsl:choose>
                  </xsl:when>
                  
                  <xsl:otherwise>
                    <image type="thumbnail">
                      <xsl:value-of select="concat(/root/gui/locService,'/resources.get?id=',$id,'&amp;fname=',$fileName,'&amp;access=public')"/>
                    </image>
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:when>

              <!-- large thumbnail -->

              <xsl:when test="string($fileDescr)='large_thumbnail'">
                <xsl:choose>
                  <xsl:when test="$info/isHarvested = 'y'">
                    <xsl:if test="$info/harvestInfo/largeThumbnail">
                      <image type="overview">
                        <xsl:value-of select="concat($info/harvestInfo/largeThumbnail, $fileName)"/>
                      </image>
                    </xsl:if>
                  </xsl:when>
                  
                  <xsl:otherwise>
                    <image type="overview">
                      <xsl:value-of select="concat(/root/gui/locService,'/resources.get?id=',$id,'&amp;fname=',$fileName,'&amp;access=public')"/>
                    </image>
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:when>

            </xsl:choose>
          </xsl:if>
        </xsl:for-each>
      </xsl:if>

      <xsl:for-each select="gmd:pointOfContact/*">
        <xsl:variable name="role" select="gmd:role/*/@codeListValue"/>
        <xsl:if test="normalize-space($role)!=''">
          <responsibleParty role="{$role}" appliesTo="resource">
            <xsl:if test="descendant::*/gmx:FileName">
              <xsl:attribute name="logo"><xsl:value-of select="descendant::*/gmx:FileName/@src"/></xsl:attribute>
            </xsl:if>
            <xsl:apply-templates mode="responsiblepartysimple" select="."/>
          </responsibleParty>
        </xsl:if>
      </xsl:for-each>

  </xsl:template>

  <!-- helper to create a very simplified view of a CI_ResponsibleParty block -->

  <xsl:template mode="responsiblepartysimple" match="*">
    <xsl:for-each select=".//gco:CharacterString|.//gmd:URL">
      <xsl:if test="normalize-space(.)!=''">
        <xsl:element name="{local-name(..)}">
          <xsl:value-of select="."/>
        </xsl:element>
      </xsl:if>
    </xsl:for-each>
  </xsl:template>

  <xsl:template mode="brieftime" match="*">
    <xsl:choose>
      <xsl:when test="normalize-space(.)=''">
        <xsl:value-of select="@indeterminatePosition"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="."/>
        <xsl:if test="@indeterminatePosition">
          <xsl:value-of select="concat(' (Qualified by indeterminatePosition',': ',@indeterminatePosition,')')"/>
        </xsl:if>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <!-- In order to add profil specific tabs 
    add a template in this mode.
    
    To add some more tabs.
    <xsl:template name="iso19139.profileIdCompleteTab">
    <xsl:param name="tabLink"/>
    <xsl:param name="schema"/>
    
    Load iso19139 complete tab if needed
    <xsl:call-template name="iso19139CompleteTab">
      <xsl:with-param name="tabLink" select="$tabLink"/>
      <xsl:with-param name="schema" select="$schema"/>
    </xsl:call-template>
    
    Add Extra tabs
    <xsl:call-template name="mainTab">
      <xsl:with-param name="title" select="/root/gui/schemas/*[name()=$schema]/strings/tab"/>
      <xsl:with-param name="default">profileId</xsl:with-param>
      <xsl:with-param name="menu">
      <item label="profileIdTab">profileId</item>
      </xsl:with-param>
    </xsl:call-template>
    </xsl:template>
  -->
  
  
  <!-- ============================================================================= -->
  <!-- iso19139 complete tab template  -->
  <!-- ============================================================================= -->

  <xsl:template name="iso19139CompleteTab">
    <xsl:param name="tabLink"/>
    <xsl:param name="schema"/>
    
    <!-- INSPIRE tab -->
    <xsl:if test="/root/gui/env/inspire/enable = 'true' and /root/gui/env/metadata/enableInspireView = 'true'">
      <xsl:call-template name="mainTab">
        <xsl:with-param name="title" select="/root/gui/strings/inspireTab"/>
        <xsl:with-param name="default">inspire</xsl:with-param>
        <xsl:with-param name="menu">
          <item label="inspireTab">inspire</item>
        </xsl:with-param>
      </xsl:call-template>
    </xsl:if>
    
    <xsl:if test="/root/gui/env/metadata/enableIsoView = 'true'">
      <xsl:call-template name="mainTab">
        <xsl:with-param name="title" select="/root/gui/strings/byGroup"/>
        <xsl:with-param name="default">ISOCore</xsl:with-param>
        <xsl:with-param name="menu">
          <item label="isoMinimum">ISOMinimum</item>
          <item label="isoCore">ISOCore</item>
          <item label="isoAll">ISOAll</item>
        </xsl:with-param>
      </xsl:call-template>
     </xsl:if>
    
    
    
    <xsl:if test="/root/gui/config/metadata-tab/advanced">
      <xsl:call-template name="mainTab">
        <xsl:with-param name="title" select="/root/gui/strings/byPackage"/>
        <xsl:with-param name="default">identification</xsl:with-param>
        <xsl:with-param name="menu">
          <item label="metadata">metadata</item>
          <item label="identificationTab">identification</item>
          <item label="maintenanceTab">maintenance</item>
          <item label="constraintsTab">constraints</item>
          <item label="spatialTab">spatial</item>
          <item label="refSysTab">refSys</item>
          <item label="distributionTab">distribution</item>
          <item label="dataQualityTab">dataQuality</item>
          <item label="appSchInfoTab">appSchInfo</item>
          <item label="porCatInfoTab">porCatInfo</item>
          <item label="contentInfoTab">contentInfo</item>
          <item label="extensionInfoTab">extensionInfo</item>
        </xsl:with-param>
      </xsl:call-template>
    </xsl:if>    
  </xsl:template>
  
  <!-- ============================================================================= -->
  <!-- utilities -->
  <!-- ============================================================================= -->
  
  <!-- List of regions to define country.
  gmd:country is not a codelist (only country in PT_Local is).
  A list of existing countries in Regions table is suggested to the editor.
  The input text could also be used to type another value.
  -->
  <xsl:template mode="iso19139" match="gmd:country[gco:CharacterString]" priority="1">
    <xsl:param name="schema" />
    <xsl:param name="edit" />
    
    <xsl:variable name="qname" select="name(.)"/>
    <xsl:variable name="value" select="gco:CharacterString"/>
    <xsl:variable name="isXLinked" select="count(ancestor-or-self::node()[@xlink:href]) > 0" />    
    
    <xsl:apply-templates mode="simpleElement" select=".">
      <xsl:with-param name="schema" select="$schema" />
      <xsl:with-param name="edit" select="$edit" />
      <xsl:with-param name="text">
        <xsl:choose>                    
          <xsl:when test="$edit=true()">
            
            <xsl:variable name="lang" select="/root/gui/language"/>
            <input class="md" name="_{gco:CharacterString/geonet:element/@ref}"
            id="_{gco:CharacterString/geonet:element/@ref}" value="{gco:CharacterString}">
              <xsl:if test="$isXLinked">
                <xsl:attribute name="disabled">disabled</xsl:attribute>
              </xsl:if>          
            </input>
            <xsl:if test="not($isXLinked)">
              <xsl:text> </xsl:text>
              <select class="md"
                onchange="Ext.getDom('_{gco:CharacterString/geonet:element/@ref}').value = this.options[this.selectedIndex].value;"
                size="1">
                <option name="" />
                <xsl:for-each select="/root/gui/regions/record">
                  <xsl:sort select="label/child::*[name() = $lang]" order="ascending"/>
                  
                  <option value="{label/child::*[name() = $lang]}">
                    <xsl:if test="$value = label/child::*[name() = $lang]">
                      <xsl:attribute name="selected"/>
                    </xsl:if>
                    <xsl:value-of select="label/child::*[name() = $lang]"/>
                  </option>
                </xsl:for-each>
              </select>
            </xsl:if>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of
              select="$value" />
          </xsl:otherwise>
        </xsl:choose>
      </xsl:with-param>
    </xsl:apply-templates>
  </xsl:template>



   <!--
        Open a popup to select a parent and set the parent identifier field or
				select a related metadata record as a sibling.
        In view mode display an hyperlink to the parent or sibling metadata record.
    -->
    <xsl:template mode="iso19139" match="gmd:parentIdentifier|gmd:code[name(../..)='gmd:aggregateDataSetIdentifier']"
        priority="2">
        <xsl:param name="schema" />
        <xsl:param name="edit" />

        <xsl:choose>
            <xsl:when test="$edit=true()">
                <xsl:variable name="text">
                    <xsl:variable name="ref"
                        select="gco:CharacterString/geonet:element/@ref" />
                    <input onfocus="javascript:Ext.getCmp('editorPanel').showLinkedMetadataSelectionPanel('{$ref}', '');"
                        class="md" type="text" name="_{$ref}" id="_{$ref}" value="{gco:CharacterString/text()}" size="20" />
                    <img src="../../images/find.png" alt="{/root/gui/strings/parentSearch}" title="{/root/gui/strings/parentSearch}"
                      onclick="javascript:Ext.getCmp('editorPanel').showLinkedMetadataSelectionPanel('{$ref}', '');"/>
                </xsl:variable>

                <xsl:apply-templates mode="simpleElement"
                    select=".">
                    <xsl:with-param name="schema" select="$schema" />
                    <xsl:with-param name="edit" select="true()" />
                    <xsl:with-param name="text" select="$text" />
                </xsl:apply-templates>
            </xsl:when>
            <xsl:otherwise>
                <xsl:apply-templates mode="simpleElement"
                    select=".">
                    <xsl:with-param name="schema" select="$schema" />
                    <xsl:with-param name="text">
                    
                        <xsl:variable name="metadataTitle">
                            <xsl:call-template name="getMetadataTitle">
                                <xsl:with-param name="uuid" select="gco:CharacterString"></xsl:with-param>
                            </xsl:call-template>
                        </xsl:variable>
                        <a href="#" onclick="javascript:catalogue.metadataShow('{gco:CharacterString}');return false;">
                            <xsl:value-of select="$metadataTitle"/>
                        </a>
                    </xsl:with-param>
                </xsl:apply-templates>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>



  <!-- Display extra thumbnails (not managed by GeoNetwork).
    Thumbnails managed by GeoNetwork are displayed on header.
    If fileName does not start with http://, just display as
    simple elements.
  -->
  <xsl:template mode="iso19139" match="gmd:graphicOverview" priority="2">
    <xsl:param name="schema" />
    <xsl:param name="edit" />

    <!-- do not show empty elements in view mode -->
    <xsl:choose>
      <xsl:when test="$edit=true()">
        <xsl:apply-templates mode="element" select=".">
          <xsl:with-param name="schema" select="$schema" />
          <xsl:with-param name="edit" select="true()" />
        </xsl:apply-templates>
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates mode="simpleElement"
          select=".">
          <xsl:with-param name="schema" select="$schema" />
          <xsl:with-param name="text">&#160;
              
              
            <xsl:variable name="langId">
              <xsl:call-template name="getLangId">
                <xsl:with-param name="langGui" select="/root/gui/language" />
                <xsl:with-param name="md"
                  select="ancestor-or-self::*[name(.)='gmd:MD_Metadata' or @gco:isoType='gmd:MD_Metadata']" />
              </xsl:call-template>
            </xsl:variable>
            
            <xsl:variable name="imageTitle">
                <xsl:choose>
                    <xsl:when test="gmd:MD_BrowseGraphic/gmd:fileDescription/gco:CharacterString
                      and not(gmd:MD_BrowseGraphic/gmd:fileDescription/@gco:nilReason)">
                      <xsl:for-each select="gmd:MD_BrowseGraphic/gmd:fileDescription">
                        <xsl:call-template name="localised">
                          <xsl:with-param name="langId" select="$langId"/>
                        </xsl:call-template>
                      </xsl:for-each>
                    </xsl:when>
                  <xsl:otherwise>
                    <!-- Filename is not multilingual -->
                    <xsl:value-of select="gmd:MD_BrowseGraphic/gmd:fileName/gco:CharacterString"/>
                  </xsl:otherwise>
                </xsl:choose>
            </xsl:variable>
              
            <xsl:variable name="fileName" select="gmd:MD_BrowseGraphic/gmd:fileName/gco:CharacterString"/>
            <xsl:variable name="url" select="if (contains($fileName, '://')) 
                                                then $fileName 
                                                else geonet:get-thumbnail-url($fileName, //geonet:info, /root/gui/locService)"/>

            <div class="md-view">
              <a rel="lightbox-viewset" href="{$url}">
                <img class="logo" src="{$url}">
                  <xsl:attribute name="alt"><xsl:value-of select="$imageTitle"/></xsl:attribute>
                    <xsl:attribute name="title"><xsl:value-of select="$imageTitle"/></xsl:attribute>
                </img>
              </a>  
              <br/>
              <span class="thumbnail"><a href="{$url}" target="thumbnail-view"><xsl:value-of select="$imageTitle"/></a></span>
            </div>
          </xsl:with-param>
        </xsl:apply-templates>
      </xsl:otherwise>
    </xsl:choose>    
  </xsl:template>
  
  <!-- ===================================================================== -->
  <!-- Templates to retrieve thumbnails -->
  <xsl:template mode="get-thumbnail" match="gmd:MD_Metadata|*[@gco:isoType='gmd:MD_Metadata']">
    <xsl:apply-templates mode="get-thumbnail" select="gmd:identificationInfo/*/gmd:graphicOverview"/>
  </xsl:template>
  
  <xsl:template mode="get-thumbnail" match="gmd:graphicOverview">
    <xsl:variable name="fileName" select="gmd:MD_BrowseGraphic/gmd:fileName/gco:CharacterString"/>
    <xsl:variable name="desc" select="gmd:MD_BrowseGraphic/gmd:fileDescription/gco:CharacterString"/>
    <xsl:variable name="info" select="ancestor::*[name(.) = 'gmd:MD_Metadata' or @gco:isoType='gmd:MD_Metadata']/geonet:info"></xsl:variable>
    
    <thumbnail>
      <href><xsl:value-of select="geonet:get-thumbnail-url($fileName, $info, /root/gui/locService)"/></href>
      <desc><xsl:value-of select="$desc"/></desc>
      <mimetype><xsl:value-of select="gmd:MD_BrowseGraphic/gmd:fileType/gco:CharacterString"/></mimetype>
      <type><xsl:value-of select="if (geonet:contains-any-of($desc, ('thumbnail', 'large_thumbnail'))) then 'local' else ''"/></type>
    </thumbnail>
  </xsl:template>


  <!--
    =====================================================================
    Multilingual metadata:
    =====================================================================
    * ISO 19139 define how to store multilingual content in a metadata
    record. 
    1) A record is defined by a main language set in 
    gmd:MD_Metadata/gmd:language element. All gco:CharacterString are
    then defined in that language. 
    2) In order to add translation editor
    should add a gmd:locale element in gmd:MD_Metadata:  
    <gmd:locale>
      <gmd:PT_Locale id="FR">
        <gmd:languageCode>
          <gmd:LanguageCode codeList="#FR" codeListValue="fra"/>
        </gmd:languageCode>
        <gmd:characterEncoding/>
      </gmd:PT_Locale>
    </gmd:locale>
    3) Once declared in gmd:locale, all gco:CharacterString could
    be translated using the following mechanism:
      * add xsi:type attribute (@see DataManager.updatedLocalizedTextElement)
      * add gmd:PT_FreeText element linked to locale using the locale
      attribute.
    <gmd:title xsi:type="gmd:PT_FreeText_PropertyType">
        <gco:CharacterString>Template for Vector data in ISO19139
        (preferred!)</gco:CharacterString>
      <gmd:PT_FreeText>
        <gmd:textGroup>
          <gmd:LocalisedCharacterString locale="#FR">Modèle de saisie pour les données vecteurs en ISO19139</gmd:LocalisedCharacterString>
        </gmd:textGroup>
      </gmd:PT_FreeText>
    </gmd:title>
    
    =====================================================================    
    Editor principles:
    =====================================================================    
    * available locales in metadata records are not displayed in view
    mode, only used in editing mode in order to add multilingual content.
  -->  
  <xsl:template mode="iso19139" match="gmd:locale|geonet:child[string(@name)='locale']" priority="1">
    <xsl:param name="schema" />
    <xsl:param name="edit" />
    <xsl:choose>
      <xsl:when test="$edit = true()">
        <xsl:variable name="content">
          <xsl:apply-templates mode="elementEP" select="*/gmd:languageCode|*/geonet:child[string(@name)='languageCode']">
            <xsl:with-param name="schema" select="$schema"/>
            <xsl:with-param name="edit"   select="$edit"/>
          </xsl:apply-templates>
        </xsl:variable>          
        
        <xsl:apply-templates mode="complexElement" select=".">
          <xsl:with-param name="schema"  select="$schema"/>
          <xsl:with-param name="edit"    select="$edit"/>
          <xsl:with-param name="content" select="$content"/>
        </xsl:apply-templates>
      </xsl:when>
      <!-- In view mode, gmd:locale is displayed next to gmd:language -->
    </xsl:choose>
    
  </xsl:template>



  <!--
    =====================================================================        
    * All elements having gco:CharacterString or gmd:PT_FreeText elements
    have to display multilingual editor widget. Even if default language
    is set, an element could have gmd:PT_FreeText and no gco:CharacterString
    (ie. no value for default metadata language) .
  -->
  <xsl:template mode="iso19139"
    match="gmd:*[gco:CharacterString or gmd:PT_FreeText]|
    srv:*[gco:CharacterString or gmd:PT_FreeText]|
    gco:aName[gco:CharacterString]"
    >
    <xsl:param name="schema" />
    <xsl:param name="edit" />
    
    <!-- Define a class variable if form element as
      to be a textarea instead of a simple text input.
      This parameter define the class of the textarea (see CSS). -->
    <xsl:variable name="class">
      <xsl:choose>
        <xsl:when test="name(.)='gmd:title' and name(../../../..)='gmd:identificationInfo'">title</xsl:when>
        <xsl:when test="name(.)='gmd:abstract'">large</xsl:when>
        <xsl:when test="name(.)='gmd:supplementalInformation'
          or name(.)='gmd:purpose'
          or name(.)='gmd:orderingInstructions'
          or name(.)='gmd:statement'">medium</xsl:when>
        <xsl:when test="name(.)='gmd:description'
          or name(.)='gmd:specificUsage'
          or name(.)='gmd:explanation'
          or name(.)='gmd:credit'
          or name(.)='gmd:evaluationMethodDescription'
          or name(.)='gmd:measureDescription'
          or name(.)='gmd:maintenanceNote'
          or name(.)='gmd:useLimitation'
          or name(.)='gmd:otherConstraints'
          or name(.)='gmd:handlingDescription'
          or name(.)='gmd:userNote'
          or name(.)='gmd:checkPointDescription'
          or name(.)='gmd:evaluationMethodDescription'
          or name(.)='gmd:measureDescription'
          ">small</xsl:when>
        <xsl:otherwise></xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    
    <xsl:call-template name="localizedCharStringField">
      <xsl:with-param name="schema" select="$schema" />
      <xsl:with-param name="edit" select="$edit" />
      <xsl:with-param name="class" select="$class" />
    </xsl:call-template>
  </xsl:template>
  
  <!-- =====================================================================        
    * Anyway some elements should not be multilingual.
    
    Use this template to define which elements
    are not multilingual.
    If an element is not multilingual and require
    a specific widget (eg. protocol list), create
    a new template for this new element.
    
    !!! WARNING: this is not defined in ISO19139. !!!
    This list of element mainly focus on identifier (eg. postal code)
    which are usually not multilingual. The list has been defined
    based on ISO profil for Switzerland recommendations. Feel free
    to adapt this list according to your needs.
  -->
  <xsl:template mode="iso19139"
    match="
    gmd:identifier[gco:CharacterString]|
    gmd:metadataStandardName[gco:CharacterString]|
    gmd:metadataStandardVersion[gco:CharacterString]|
    gmd:hierarchyLevelName[gco:CharacterString]|
    gmd:dataSetURI[gco:CharacterString]|
    gmd:postalCode[gco:CharacterString]|
    gmd:city[gco:CharacterString]|
    gmd:administrativeArea[gco:CharacterString]|
    gmd:voice[gco:CharacterString]|
    gmd:facsimile[gco:CharacterString]|
    gmd:MD_ScopeDescription/gmd:dataset[gco:CharacterString]|
    gmd:MD_ScopeDescription/gmd:other[gco:CharacterString]|
    gmd:hoursOfService[gco:CharacterString]|
    gmd:applicationProfile[gco:CharacterString]|
    gmd:CI_Series/gmd:page[gco:CharacterString]|
    gmd:MD_BrowseGraphic/gmd:fileName[gco:CharacterString]|
    gmd:MD_BrowseGraphic/gmd:fileType[gco:CharacterString]|
    gmd:unitsOfDistribution[gco:CharacterString]|
    gmd:amendmentNumber[gco:CharacterString]|
    gmd:specification[gco:CharacterString]|
    gmd:fileDecompressionTechnique[gco:CharacterString]|
    gmd:turnaround[gco:CharacterString]|
    gmd:fees[gco:CharacterString]|
    gmd:userDeterminedLimitations[gco:CharacterString]|
    gmd:RS_Identifier/gmd:codeSpace[gco:CharacterString]|
    gmd:RS_Identifier/gmd:version[gco:CharacterString]|
    gmd:edition[gco:CharacterString]|
    gmd:ISBN[gco:CharacterString]|
    gmd:ISSN[gco:CharacterString]|
    gmd:errorStatistic[gco:CharacterString]|
    gmd:schemaAscii[gco:CharacterString]|
    gmd:softwareDevelopmentFileFormat[gco:CharacterString]|
    gmd:MD_ExtendedElementInformation/gmd:shortName[gco:CharacterString]|
    gmd:MD_ExtendedElementInformation/gmd:condition[gco:CharacterString]|
    gmd:MD_ExtendedElementInformation/gmd:maximumOccurence[gco:CharacterString]|
    gmd:MD_ExtendedElementInformation/gmd:domainValue[gco:CharacterString]|
    gmd:densityUnits[gco:CharacterString]|
    gmd:MD_RangeDimension/gmd:descriptor[gco:CharacterString]|
    gmd:classificationSystem[gco:CharacterString]|
    gmd:checkPointDescription[gco:CharacterString]|
    gmd:transformationDimensionDescription[gco:CharacterString]|
    gmd:orientationParameterDescription[gco:CharacterString]|
    srv:SV_OperationChainMetadata/srv:name[gco:CharacterString]|
    srv:SV_OperationMetadata/srv:invocationName[gco:CharacterString]|
    srv:serviceTypeVersion[gco:CharacterString]|
    srv:operationName[gco:CharacterString]|
    srv:identifier[gco:CharacterString]
    "
    priority="100">
    <xsl:param name="schema" />
    <xsl:param name="edit" />
    
    <xsl:call-template name="iso19139String">
      <xsl:with-param name="schema" select="$schema"/>
      <xsl:with-param name="edit"   select="$edit"/>
    </xsl:call-template>
  </xsl:template>
  
  
  
  
  <!-- =====================================================================
    Multilingual editor widget is composed of input box
    with a list of languages defined in current metadata record. 
    
    Metadata languages are:
    * the main language (gmd:MD_Metadata/gmd:language) and
    * all languages defined in gmd:locale section. 
  
    Change this template to defined another multilingual widget.
  -->
  <xsl:template name="localizedCharStringField" >
    <xsl:param name="schema" />
    <xsl:param name="edit" />
    <xsl:param name="class" select="''" />
    
    <xsl:variable name="langId">
      <xsl:call-template name="getLangId">
        <xsl:with-param name="langGui" select="/root/gui/language" />
        <xsl:with-param name="md"
          select="ancestor-or-self::*[name(.)='gmd:MD_Metadata' or @gco:isoType='gmd:MD_Metadata']" />
      </xsl:call-template>
    </xsl:variable>
    
    <xsl:variable name="widget">
      <xsl:if test="$edit=true()">
        <xsl:variable name="tmpFreeText">
          <xsl:call-template name="PT_FreeText_Tree" />
        </xsl:variable>
      

        <xsl:variable name="ptFreeTextTree" select="exslt:node-set($tmpFreeText)" />
        
        <xsl:variable name="mainLang"
          select="string(/root/*/gmd:language/gco:CharacterString|/root/*/gmd:language/gmd:LanguageCode/@codeListValue)" />
        <xsl:variable name="mainLangId">
          <xsl:call-template name="getLangIdFromMetadata">
            <xsl:with-param name="lang" select="$mainLang" />
            <xsl:with-param name="md"
              select="ancestor-or-self::*[name(.)='gmd:MD_Metadata' or @gco:isoType='gmd:MD_Metadata']" />
          </xsl:call-template>
        </xsl:variable>

        <span>
          <!-- Match gco:CharacterString element which is in default language or
            process a PT_FreeText with a reference to the main metadata language. -->
          <xsl:choose>
            <xsl:when test="gco:*">
              <xsl:for-each select="gco:*">
                <xsl:call-template name="getElementText">
                  <xsl:with-param name="schema" select="$schema" />
                  <xsl:with-param name="edit" select="true()" />
                  <xsl:with-param name="class" select="$class" />
                </xsl:call-template>
              </xsl:for-each>                        
            </xsl:when>
            <xsl:when test="gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString[@locale=$mainLangId]">
              <xsl:for-each select="gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString[@locale=$mainLangId]">
                <xsl:call-template name="getElementText">
                  <xsl:with-param name="schema" select="$schema" />
                  <xsl:with-param name="edit" select="true()" />
                  <xsl:with-param name="class" select="$class" />
                </xsl:call-template>
              </xsl:for-each>         
            </xsl:when>
            <xsl:otherwise>
              <xsl:for-each select="$ptFreeTextTree//gmd:LocalisedCharacterString[@locale=$mainLangId]">
                <xsl:call-template name="getElementText">
                  <xsl:with-param name="schema" select="$schema" />
                  <xsl:with-param name="edit" select="true()" />
                  <xsl:with-param name="class" select="$class" />
                </xsl:call-template>
              </xsl:for-each>         
            </xsl:otherwise>
          </xsl:choose>
          
          <xsl:for-each select="$ptFreeTextTree//gmd:LocalisedCharacterString[@locale!=$mainLangId]">
            <xsl:call-template name="getElementText">
              <xsl:with-param name="schema" select="$schema" />
              <xsl:with-param name="edit" select="true()" />
              <xsl:with-param name="visible" select="false()" />
              <xsl:with-param name="class" select="$class" />
            </xsl:call-template>
          </xsl:for-each>
        </span>
        <span class="lang">
            <xsl:choose>
              <xsl:when test="$ptFreeTextTree//gmd:LocalisedCharacterString">                
                <!-- Create combo to select language.
                On change, the input with selected language is displayed. Others hidden. -->

                <xsl:variable name="mainLanguageRef">
                  <xsl:choose>
                      <xsl:when test="gco:CharacterString/geonet:element/@ref" >
                          <xsl:value-of select="concat('_', gco:CharacterString/geonet:element/@ref)"/>
                      </xsl:when>
                      <xsl:otherwise>
                          <xsl:value-of select="concat('_',
                                  gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString[@locale=$mainLangId]/geonet:element/@ref)"/>
                      </xsl:otherwise>
                  </xsl:choose>
                </xsl:variable>

                <xsl:variable name="suggestionDiv" select="concat('suggestion', $mainLanguageRef)"/>
                
                <!-- Language selector is only displayed when more than one language
                is set in gmd:locale. -->
                <select class="md lang_selector" name="localization" id="localization_{geonet:element/@ref}" 
                  onchange="enableLocalInput(this);clearSuggestion('{$suggestionDiv}');" 
                  selected="true">
                  <xsl:attribute name="style">
                    <xsl:choose>
                      <xsl:when test="count($ptFreeTextTree//gmd:LocalisedCharacterString)=0">display:none;</xsl:when>
                      <xsl:otherwise>display:block;</xsl:otherwise>
                    </xsl:choose>
                  </xsl:attribute>
                  <xsl:choose>
                    <xsl:when test="gco:*">
                      <option value="_{gco:*/geonet:element/@ref}" code="{substring-after($mainLangId, '#')}">
                        <xsl:value-of
                              select="/root/gui/isoLang/record[code=$mainLang]/label/*[name(.)=/root/gui/language]" />
                      </option>
                      <xsl:for-each select="$ptFreeTextTree//gmd:LocalisedCharacterString[@locale!=$mainLangId]">
                        <option value="_{geonet:element/@ref}" code="{substring-after(@locale, '#')}">
                          <xsl:value-of select="@language" />
                        </option>
                      </xsl:for-each>
                    </xsl:when>
                    <xsl:otherwise>
                      <xsl:for-each select="$ptFreeTextTree//gmd:LocalisedCharacterString">
                        <option value="_{geonet:element/@ref}" code="{substring-after(@locale, '#')}">
                          <xsl:value-of select="@language" />
                        </option>
                      </xsl:for-each>
                    </xsl:otherwise>
                  </xsl:choose>
                </select>
            
                <!-- =================================
                    Google translation API demo
                    See: http://code.google.com/apis/ajaxlanguage/documentation/
                    =================================
                    Simple button to translate one element from one language to another.
                    This is useful to help editor to translate metadata content.
                    
                    To be improved :
                     * check that jeeves GUI language is equal to Google language code
                     * target parameter of translate function could be set to:
                     $('localization_{geonet:element/@ref}').options[$('localization_{geonet:element/@ref}').selectedIndex].value
                     but this will copy Google results to a form field. User should review suggested translation.
                -->
                <xsl:if test="/root/gui/config/editor-google-translate = 1">
                  <xsl:text> </xsl:text>
                  <a href="javascript:googleTranslate('{$mainLanguageRef}',
                      '{$suggestionDiv}',
                      null,
                      '{substring-after($mainLangId, '#')}', 
                      Ext.getDom('localization_{geonet:element/@ref}').options[Ext.getDom('localization_{geonet:element/@ref}').selectedIndex].getAttribute('code'));"                      
                      alt="{/root/gui/strings/translateWithGoogle}" title="{/root/gui/strings/translateWithGoogle}">
                    <img width="14px" src="../../images/translate.png"/>
                  </a>
                  <br/>
                  <div id="suggestion_{gco:CharacterString/geonet:element/@ref|
                    gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString[@locale=$mainLangId]/geonet:element/@ref}"
                    style="display:none;"
                    class="suggestion"
                    alt="{/root/gui/strings/translateWithGoogle}" title="{/root/gui/strings/translateWithGoogle}"
                  />
                </xsl:if>
              </xsl:when>
            </xsl:choose>
        </span>
          
      </xsl:if>
    </xsl:variable>
    <xsl:call-template name="iso19139String">
      <xsl:with-param name="schema" select="$schema" />
      <xsl:with-param name="edit" select="$edit" />
      <xsl:with-param name="langId" select="$langId" />
      <xsl:with-param name="widget" select="$widget" />
      <xsl:with-param name="class" select="$class" />
    </xsl:call-template>
  </xsl:template>
  
  
  <!-- 
    Create a PT_FreeText_Tree for multilingual editing.
    
    The lang prefix for geonet:element is used by the DataManager 
    to clean multilingual content and add required attribute (xsi:type).
  -->  
  <xsl:template name="PT_FreeText_Tree">
    <xsl:variable name="mainLang"
      select="string(/root/*/gmd:language/gco:CharacterString|/root/*/gmd:language/gmd:LanguageCode/@codeListValue)" />
    <xsl:variable name="languages"
      select="/root/*/gmd:locale/gmd:PT_Locale/gmd:languageCode/gmd:LanguageCode/@codeListValue" />
    
    <xsl:variable name="currentNode" select="node()" />
    <xsl:for-each select="$languages">
      <xsl:variable name="langId"
        select="concat('&#35;',string(../../../@id))" />
      <xsl:variable name="code">
        <xsl:call-template name="getLangCode">
          <xsl:with-param name="md"
            select="ancestor-or-self::*[name(.)='gmd:MD_Metadata' or @gco:isoType='gmd:MD_Metadata']" />
          <xsl:with-param name="langId" select="substring($langId,2)" />
        </xsl:call-template>
      </xsl:variable>
        
      <xsl:variable name="ref" select="$currentNode/../geonet:element/@ref" />
      <xsl:variable name="min" select="$currentNode/../geonet:element/@min" />
      <xsl:variable name="guiLang" select="/root/gui/language" />
      <xsl:variable name="language"
        select="/root/gui/isoLang/record[code=$code]/label/*[name(.)=$guiLang]" />
      <gmd:PT_FreeText>
        <gmd:textGroup>
          <gmd:LocalisedCharacterString locale="{$langId}"
            code="{$code}" language="{$language}">
            <xsl:value-of
              select="$currentNode//gmd:LocalisedCharacterString[@locale=$langId]" />
            <xsl:choose>
              <xsl:when
                test="$currentNode//gmd:LocalisedCharacterString[@locale=$langId]">
                <geonet:element
                  ref="{$currentNode//gmd:LocalisedCharacterString[@locale=$langId]/geonet:element/@ref}" />
              </xsl:when>
              <xsl:otherwise>
                <geonet:element ref="lang_{substring($langId,2)}_{$ref}" />
              </xsl:otherwise>
            </xsl:choose>
          </gmd:LocalisedCharacterString>
          <geonet:element ref="" />
        </gmd:textGroup>
        <geonet:element ref="">
          <!-- Add min attribute from current node to PT_FreeText
          child in order to turn on validation criteria. -->
          <xsl:if test="$min = 1">
            <xsl:attribute name="min">1</xsl:attribute>
          </xsl:if>
        </geonet:element>
      </gmd:PT_FreeText>
    </xsl:for-each>
  </xsl:template>
  
  <!-- Template to return the function name to be use
  to build the XML fragment in the editor. -->
  <xsl:template mode="addXMLFragment" match="gmd:descriptiveKeywords|
        geonet:child[@name='descriptiveKeywords' and @prefix='gmd']">
    <xsl:text>Ext.getCmp('editorPanel').showKeywordSelectionPanel</xsl:text>
  </xsl:template>
  <xsl:template mode="addXMLFragment" match="gmd:referenceSystemInfo|
        geonet:child[@name='referenceSystemInfo' and @prefix='gmd']">
    <xsl:text>Ext.getCmp('editorPanel').showCRSSelectionPanel</xsl:text>
  </xsl:template>
  <xsl:template mode="addXMLFragment" match="*|@*"></xsl:template>  
</xsl:stylesheet>
