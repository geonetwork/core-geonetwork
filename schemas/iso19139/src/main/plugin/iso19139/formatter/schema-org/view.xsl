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
                xmlns:xslUtils="java:org.fao.geonet.util.XslUtil"
                extension-element-prefixes="saxon"
                exclude-result-prefixes="#all">




  <xsl:variable name="baseurl" select="xslUtils:getSiteUrl()"/>	
  
  <!-- Load the editor configuration to be able
  to render the different views -->
  <xsl:variable name="configuration"
                select="document('../../layout/config-editor.xml')"/>

			
  <!-- Some utility -->
  <xsl:include href="../../layout/evaluate.xsl"/>
  <xsl:include href="../../layout/utility-tpl-multilingual.xsl"/>
  
  <!-- Retrieve default layout -->
  <xsl:include href="../../../../../../../xslt/common/base-variables.xsl"/>
  <xsl:include href="../../../../../../../xslt/skin/default/skin.xsl"/>
  <xsl:include href="../../../../../../../xslt/base-layout-cssjs-loader.xsl"/>
  
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
  <xsl:template mode="render-field"
                match="*[gco:CharacterString|gco:Integer|gco:Decimal|
       gco:Boolean|gco:Real|gco:Measure|gco:Length|gco:Distance|
       gco:Angle|gmx:FileName|
       gco:Scale|gco:Record|gco:RecordType|gmx:MimeFileType|gmd:URL|
       gco:LocalName|gmd:PT_FreeText|gml:beginPosition|gml:endPosition|
       gco:Date|gco:DateTime|*/@codeListValue]"
                priority="50">
    <xsl:param name="fieldName" select="''" as="xs:string"/>

    <dl>
      <dt>
        <xsl:value-of select="if ($fieldName)
                                then $fieldName
                                else tr:node-label(tr:create($schema), name(), null)"/>
      </dt>
      <dd>
        <xsl:apply-templates mode="render-value" select="*|*/@codeListValue"/>
        <xsl:apply-templates mode="render-value" select="@*"/>
      </dd>
    </dl>
  </xsl:template>

  <!-- Some elements are only containers so bypass them -->
  <xsl:template mode="render-field"
                match="*[count(gmd:*) = 1]"
                priority="50">

    <xsl:apply-templates mode="render-value" select="@*"/>
    <xsl:apply-templates mode="render-field" select="*"/>
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
        <xsl:apply-templates mode="render-value"
                             select="@*"/>
      </h3>
      <div class="target">
        <xsl:apply-templates mode="render-field" select="*"/>
      </div>
    </div>
  </xsl:template>


  <!-- Bbox is displayed with an overview and the geom displayed on it
  and the coordinates displayed around -->
  <xsl:template mode="render-field"
                match="gmd:EX_GeographicBoundingBox[gmd:westBoundLongitude/gco:Decimal != '']">
	<div itemprop="spatial"  itemscope="itemscope" itemtype="http://schema.org/Place">
		  <span itemprop="geo" itemscope="itemscope" itemtype="http://schema.org/geoShape">
		  <dl><dt><xsl:value-of select="tr:node-label(tr:create($schema), name(), null)"/></dt><dd>
		  <div class="thumbnail">
		  <i><xsl:value-of select="gmd:southBoundLatitude/gco:Decimal"/></i>, 
		  <i><xsl:value-of select="gmd:eastBoundLongitude/gco:Decimal"/></i><xsl:text> </xsl:text> 
		  <i><xsl:value-of select="gmd:northBoundLatitude/gco:Decimal"/></i>, 
		  <i><xsl:value-of select="gmd:westBoundLongitude/gco:Decimal"/></i>
		  </div>
		  </dd>
		  </dl>
		  <meta itemprop="box" content="{gmd:southBoundLatitude/gco:Decimal},{gmd:eastBoundLongitude/gco:Decimal} {gmd:northBoundLatitude/gco:Decimal},{gmd:westBoundLongitude/gco:Decimal}" />
    </span>
	</div>
		

  </xsl:template>

    <!-- A contact is displayed with its role as header -->
  <xsl:template mode="render-field"
                match="*/gmd:lineage"
                priority="100">
	<dl>
    <dt><xsl:value-of select="tr:node-label(tr:create($schema), name(), null)"/></dt>
	<dd itemprop="about">
      <xsl:value-of select="gco:CharacterString"/>
	</dd>
	</dl>
  </xsl:template>
  
     
  <xsl:template mode="render-field"
                match="*/gmd:purpose"
                priority="100">
	<dl>
    <dt><xsl:value-of select="tr:node-label(tr:create($schema), name(), null)"/></dt>
	<dd itemprop="about">
      <xsl:value-of select="gco:CharacterString"/>
	</dd>
	</dl>
  </xsl:template>
  
    
  <xsl:template mode="render-field"
                match="*/gmd:abstract"
                priority="100">
	<!-- nothing, because is on the header already -->
  </xsl:template>

  <xsl:template mode="render-field"
                match="*/gmd:MD_LegalConstraints/gmd:otherConstraints"
                priority="100">
	<dl>
    <dt><xsl:value-of select="tr:node-label(tr:create($schema), name(), null)"/></dt>
	<dd itemprop="license">
      <xsl:value-of select="gco:CharacterString"/>
	</dd>
	</dl>
  </xsl:template>
  
  <xsl:template mode="render-field"
                match="*/gmd:useLimitation"
                priority="100">
	<dl>
    <dt><xsl:value-of select="tr:node-label(tr:create($schema), name(), null)"/></dt>
	<dd>
      <xsl:value-of select="gco:CharacterString" />
	</dd>
	</dl>
  </xsl:template>
  
  <xsl:template mode="render-field"
                match="*/gmd:alternateTitle"
                priority="100">
	<dl>
    <dt><xsl:value-of select="tr:node-label(tr:create($schema), name(), null)"/></dt>
	<dd itemprop="alternateName">
      <xsl:apply-templates mode="render-value" select="*/gmd:alternateTitle" />
	</dd>
	</dl>
  </xsl:template>
  
   <xsl:template mode="render-field"
                match="*/gmd:language"
                priority="200">		
	<dl>
    <dt><xsl:value-of select="tr:node-label(tr:create($schema), name(), null)"/></dt>
	<dd itemprop="inLanguage">
      <xsl:apply-templates mode="render-value" select="*/gmd:language" />
	</dd>
	</dl>
  </xsl:template>
  
  <xsl:template mode="render-field"
                match="*/srv:coupledResource"
                priority="1000">		
			<!-- skip -->
  </xsl:template>
  
  <xsl:template mode="render-field"
                match="*/srv:SV_CoupledResource"
                priority="1000">		
			<!-- skip -->
  </xsl:template>
  
  <xsl:template mode="render-field"
                match="*/srv:couplingType"
                priority="100">		
			<!-- skip -->
  </xsl:template>
  
  <xsl:template mode="render-field"
                match="*/srv:containsOperations"
                priority="100">		
			<!-- skip -->
  </xsl:template>
  
  <xsl:template mode="render-field"
                match="*/srv:SV_OperationMetadata"
                priority="100">		
			<!-- skip -->
  </xsl:template>

  <xsl:template mode="render-field"
                match="*/gmd:title"
                priority="100">		
			<!-- skip -->
  </xsl:template>
  
  <xsl:template mode="render-field"
                match="*/srv:couplingType"
                priority="100">		
			<!-- skip -->
  </xsl:template>
  
  <xsl:template mode="render-field"
                match="*/srv:containsOperations"
                priority="100">		
			<!-- skip -->
  </xsl:template>
  

    
<xsl:template mode="render-field"
                match="*/srv:operatesOn"
                priority="105">
  <div itemprop="dataset" itemscope="itemscope" itemtype="http://schema.org/Dataset">
   <meta itemprop="url" content="{$baseurl}/doc/dataset/{./@uuidref}" />
   
   <xsl:variable name="dsUUID">
   <xsl:choose>
   <xsl:when test="contains(lower-case(./@xlink:href),'id=')">
  	 <xsl:value-of select="tokenize(tokenize(lower-case(./@xlink:href),'id=')[2],'&amp;')[1]"/>
   </xsl:when>
   <xsl:otherwise>
   	<xsl:value-of select="./@uuidref"/>
   </xsl:otherwise>	
   </xsl:choose>
   </xsl:variable>

	<xsl:variable name="mdTitle" select="xslUtils:getIndexField(null, $dsUUID, 'title','eng')"/>
	<xsl:variable name="mdTitle2" select="xslUtils:getIndexField(null, $dsUUID, '_defaultTitle','eng')"/>

   <xsl:if test="string-length($dsUUID)>0">
     <a href="{$baseurl}/doc/dataset/{$dsUUID}" class="btn btn-sm btn-primary">
     <xsl:value-of select="$dsUUID"/></a><br/>
   </xsl:if>
 </div>
 </xsl:template>
  
  
     <xsl:template mode="render-field"
                match="*/gmd:dateStamp"
                priority="100">		
	<dl>
    <dt><xsl:value-of select="tr:node-label(tr:create($schema), name(), null)"/></dt>
	<dd itemprop="dateModified">
      <xsl:apply-templates mode="render-value" select="*/gmd:datestamp" />
	</dd>
	</dl>
  </xsl:template>
  

  
  <!-- A contact is displayed with its role as header -->
  <xsl:template mode="render-field"
                match="*[gmd:CI_ResponsibleParty]"
                priority="100">
    <xsl:variable name="email">
	<span itemprop="email">
      <xsl:apply-templates mode="render-value"
                           select="*/gmd:contactInfo/
                                      */gmd:address/*/gmd:electronicMailAddress"/></span>
    </xsl:variable>

    <!-- Display name is <org name> - <individual name> (<position name> -->
    <xsl:variable name="displayName">
      <span itemprop="name">
	  <xsl:choose>
        <xsl:when
                test="*/gmd:organisationName and */gmd:individualName">
          <!-- Org name may be multilingual -->
          <xsl:apply-templates mode="render-value"
                               select="*/gmd:organisationName"/>
          -
          <xsl:value-of select="*/gmd:individualName"/>
		  
          <xsl:if test="*/gmd:positionName">
            (<xsl:apply-templates mode="render-value"
                                  select="*/gmd:positionName"/>)
          </xsl:if>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="*/gmd:organisationName|*/gmd:individualName"/>
        </xsl:otherwise>
      </xsl:choose>
	  </span>
    </xsl:variable>

    <div class="gn-contact">
      <h3>
        <i class="fa fa-envelope"></i>
        <xsl:apply-templates mode="render-value"
                             select="*/gmd:role/*/@codeListValue"/>
      </h3>
      <div class="row">
        <div class="col-xs-12">
          <address itemprop="author" itemscope="itemscope" itemtype="http://schema.org/Organization">
            <strong>
              <xsl:choose>
                <xsl:when test="$email!=''">
				  <meta content="{normalize-space($email)}" itemprop="email" />
                  <a href="mailto:{normalize-space($email)}"><xsl:value-of select="$displayName"/></a>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:value-of select="$displayName"/>
                </xsl:otherwise>
              </xsl:choose>
            </strong><br/>
            <xsl:for-each select="*/gmd:contactInfo/*">
				<span itemprop="address"  itemscope="itemscope" itemtype="http://schema.org/PostalAddress">
              <xsl:for-each select="gmd:address/*/(gmd:deliveryPoint)">
				<span itemprop="streetAddress">
                <xsl:apply-templates mode="render-value" select="."/></span><br/>
              </xsl:for-each>
			  <xsl:for-each select="gmd:address/*/(gmd:city)">
                <span itemprop="addressLocality">
                <xsl:apply-templates mode="render-value" select="."/></span><br/>
              </xsl:for-each>
			  <xsl:for-each select="gmd:address/*/(gmd:administrativeArea)">
                <span itemprop="addressRegion">
                <xsl:apply-templates mode="render-value" select="."/></span><br/>
              </xsl:for-each>
			  <xsl:for-each select="gmd:address/*/(gmd:postalCode)">
                <span itemprop="postalCode">
                <xsl:apply-templates mode="render-value" select="."/></span><br/>
              </xsl:for-each>
			  <xsl:for-each select="gmd:address/*/(gmd:country)">
                <span itemprop="addressCountry">
                <xsl:apply-templates mode="render-value" select="."/></span><br/>
              </xsl:for-each>
			  </span>
            
			  <xsl:variable name="phoneNumber">
			  
			  <xsl:for-each select="gmd:phone/*/gmd:voice[normalize-space(.) != '']">
				  <xsl:if test="not(contains(gco:CharacterString,'31'))"><xsl:text>(+31)</xsl:text></xsl:if>
                  <xsl:apply-templates mode="render-value" select="."/>
              </xsl:for-each>
			  </xsl:variable>
			  <xsl:variable name="faxNumber">
              <xsl:for-each select="gmd:phone/*/gmd:facsimile[normalize-space(.) != '']">
                  <xsl:apply-templates mode="render-value" select="."/>  
              </xsl:for-each>
			  </xsl:variable>
			  
			  <xsl:variable name="CPUrl">
				  <xsl:for-each select="gmd:onlineResource/gmd:CI_OnlineResource/gmd:linkage/gmd:URL[normalize-space(.) != '']">
				  	<xsl:if test="not(starts-with(., 'http'))">http://</xsl:if>
					  <xsl:value-of select="."/>
				  </xsl:for-each>
			  </xsl:variable>
			  <xsl:if test="$CPUrl!=''">
				   <a href="{$CPUrl}" target="_blank">
				   <i class="fa fa-link"></i> <span itemprop="url"><xsl:value-of select="$CPUrl"/></span></a>
			  </xsl:if>	
			  
			  
			  <xsl:if test="$phoneNumber!=''">
			  <span itemprop="contactPoint" itemscope="itemscope" itemtype="http://schema.org/ContactPoint">
			  <meta itemprop="contactType" content="customer support"/>	
			  
			  <xsl:if test="normalize-space(gmd:contactInstructions)!=''">
			  <span itemprop="description">
              <xsl:apply-templates mode="render-field"
                                   select="gmd:contactInstructions"/></span>				
				</xsl:if>
				
				<xsl:if test="normalize-space($phoneNumber)!=''">
				<a href="tel:{normalize-space($phoneNumber)}">
                  <i class="fa fa-phone"></i> <span  itemprop="telephone"><xsl:value-of select="$phoneNumber"/></span>
                </a><br/>
				</xsl:if>
                <xsl:if test="normalize-space($faxNumber)!=''">
					<a href="fax:{normalize-space($faxNumber)}">
					  <i class="fa fa-fax"></i> <span itemprop="faxNumber"><xsl:value-of select="$faxNumber"/></span>
					</a><br/>
				  </xsl:if>				  
			  
			  
			  
			  <xsl:if test="normalize-space(gmd:hoursOfService)!=''">
			  <span itemprop="hoursAvailable" itemscope="itemscope" itemtype="http://schema.org/OpeningHoursSpecification">	
				  <span itemprop="description">
					<xsl:apply-templates mode="render-field"
									   select="gmd:hoursOfService"/>
				  </span>
			  </span></xsl:if>	
			  
			  </span>
			  
			</xsl:if>
            </xsl:for-each>
          </address>
        </div>
      </div>
    </div>
  </xsl:template>

  <xsl:template mode="render-field"
                match="gmd:fileIdentifier"
                priority="100">
   <!-- nothing -->
  </xsl:template>

  <!-- Linkage -->
  <xsl:template mode="render-field"
                match="*[gmd:CI_OnlineResource and */gmd:linkage/gmd:URL != '']"
                priority="100">
    <dl class="gn-link" itemprop="distribution" itemscope="itemscope" itemtype="http://schema.org/DataDownload" >
      <dt>
	  <xsl:choose>
		  <xsl:when test="contains(*/gmd:protocol,'WMS') or contains(*/gmd:protocol,'WMTS') or contains(*/gmd:protocol,'SOS') or contains(*/gmd:protocol,'WCS')">
			View service
		  </xsl:when>
		  <xsl:otherwise>Download</xsl:otherwise>
	  </xsl:choose>
        
      </dt>
      <dd>
        <xsl:variable name="linkDescription">
          <xsl:apply-templates mode="render-value" select="*/gmd:description"/>
        </xsl:variable>

		<xsl:variable name="dlUrl">
		<xsl:if test="not(starts-with(*/gmd:linkage/gmd:URL, 'http'))">http://</xsl:if>
		<xsl:choose>
		  <xsl:when test="contains(*/gmd:protocol,'WMS')">
				<xsl:value-of select="*/gmd:linkage/gmd:URL"/>
				<xsl:if test="not(contains(*/gmd:linkage/gmd:URL,'?'))"><xsl:text>?</xsl:text></xsl:if>
				<xsl:if test="not(contains(*/gmd:linkage/gmd:URL,'request='))">
					<xsl:text>&amp;request=GetCapabilities&amp;service=WMS&amp;version=1.3.0</xsl:text>
				</xsl:if>
		  </xsl:when>
		  <xsl:when test="contains(*/gmd:protocol,'WFS')">
				<xsl:value-of select="*/gmd:linkage/gmd:URL"/>
				<xsl:if test="not(contains(*/gmd:linkage/gmd:URL,'?'))"><xsl:text>?</xsl:text></xsl:if>
				<xsl:if test="not(contains(*/gmd:linkage/gmd:URL,'request='))">
					<xsl:text>&amp;service=WFS&amp;version=2.0.0&amp;request=GetFeature&amp;typename=</xsl:text>
					<xsl:value-of select="normalize-space(*/gmd:name)" />
				</xsl:if>
		  </xsl:when>
		  <xsl:when test="contains(*/gmd:protocol,'WMTS')">
			<xsl:value-of select="*/gmd:linkage/gmd:URL"/>
				<xsl:if test="not(contains(*/gmd:linkage/gmd:URL,'?'))"><xsl:text>?</xsl:text></xsl:if>
				<xsl:if test="not(contains(*/gmd:linkage/gmd:URL,'request='))">
					<xsl:text>&amp;request=GetCapabilities&amp;service=WMTS&amp;version=1.0.0</xsl:text>
				</xsl:if>
		  </xsl:when>
		  <xsl:when test="contains(*/gmd:protocol,'SOS')">
			<xsl:value-of select="*/gmd:linkage/gmd:URL"/>
				<xsl:if test="not(contains(*/gmd:linkage/gmd:URL,'?'))"><xsl:text>?</xsl:text></xsl:if>
				<xsl:if test="not(contains(*/gmd:linkage/gmd:URL,'request='))">
					<xsl:text>&amp;request=GetCapabilities&amp;service=SOS&amp;version=2.0</xsl:text>
				</xsl:if>
		  </xsl:when>
		  <xsl:when test="contains(*/gmd:protocol,'WCS')">
			<xsl:value-of select="*/gmd:linkage/gmd:URL"/>
				<xsl:if test="not(contains(*/gmd:linkage/gmd:URL,'?'))"><xsl:text>?</xsl:text></xsl:if>
				<xsl:if test="not(contains(*/gmd:linkage/gmd:URL,'request='))">
					<xsl:text>&amp;request=GetCapabilities&amp;service=WCS&amp;version=2.0.1</xsl:text>
				</xsl:if>
		  </xsl:when>
		  <xsl:otherwise><xsl:value-of select="*/gmd:linkage/gmd:URL"/></xsl:otherwise>
		  </xsl:choose>
		</xsl:variable>
		
		
		
		<meta content="{$dlUrl}" itemprop="contentUrl"/>
        <a href="{$dlUrl}" target="_blank" class="btn btn-success">Download</a>
        
      </dd>
    </dl>
	
  </xsl:template>

  <!-- Identifier -->
  <xsl:template mode="render-field"
                match="*[(gmd:RS_Identifier or gmd:MD_Identifier) and
                  */gmd:code/gco:CharacterString != '']"
                priority="100">
    <dl class="gn-code">
      <dt>
        <xsl:value-of select="tr:node-label(tr:create($schema), name(), null)"/>
      </dt>
      <dd>

        <xsl:if test="*/gmd:codeSpace">
          <xsl:apply-templates mode="render-value"
                               select="*/gmd:codeSpace"/>
          /
        </xsl:if>
        <xsl:apply-templates mode="render-value"
                             select="*/gmd:code"/>
        <xsl:if test="*/gmd:version">
          / <xsl:apply-templates mode="render-value"
                                 select="*/gmd:version"/>
        </xsl:if>
        <p>
          <xsl:apply-templates mode="render-field"
                               select="*/gmd:authority"/>
        </p>
      </dd>
    </dl>
  </xsl:template>


  <!-- Display thesaurus name and the list of keywords -->
  <xsl:template mode="render-field"
                match="gmd:descriptiveKeywords[*/gmd:thesaurusName/gmd:CI_Citation/gmd:title]"
                priority="100">
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
        
            <span itemprop="keywords">
              <xsl:apply-templates mode="render-value"
                                   select="*/gmd:keyword/*"/></span>
  
      </dd>
    </dl>
  </xsl:template>


  <xsl:template mode="render-field"
                match="gmd:descriptiveKeywords[not(*/gmd:thesaurusName/gmd:CI_Citation/gmd:title)]"
                priority="100">
    <dl class="gn-keyword">
      <dt>
        <xsl:value-of select="$schemaStrings/noThesaurusName"/>
        <xsl:if test="*/gmd:type/*[@codeListValue != '']">
          (<xsl:apply-templates mode="render-value"
                                select="*/gmd:type/*/@codeListValue"/>)
        </xsl:if>
      </dt>
      <dd>
        
            <span itemprop="keywords">
              <xsl:apply-templates mode="render-value"
                                   select="*/gmd:keyword/*"/>
            </span>
          
      </dd>
    </dl>
  </xsl:template>

  <!-- Display all graphic overviews in one block -->
  <xsl:template mode="render-field"
                match="gmd:graphicOverview[1]"
                priority="100">
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
            
              <img src="{gmd:MD_BrowseGraphic/gmd:fileName/*}"
                   alt="{$label}" style="max-height:150px;"
				   itemprop="thumbnailUrl"
                   class="img-thumbnail"/>
           
          </xsl:for-each>
        </ul>
      </dd>
    </dl>
  </xsl:template>
  <xsl:template mode="render-field"
                match="gmd:graphicOverview[position() > 1]"
                priority="100"/>


  <xsl:template mode="render-field"
                match="gmd:distributionFormat[1]"
                priority="100">
    <dl class="gn-format">
      <dt>
        <xsl:value-of select="tr:node-label(tr:create($schema), name(), null)"/>
      </dt>
      <dd>
        <ul>
          <xsl:for-each select="parent::node()/gmd:distributionFormat">
            <li><span itemprop="fileFormat">
              <xsl:apply-templates mode="render-value"
                                   select="*/gmd:name"/></span>
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
                match="gmd:distributionFormat[position() > 1]"
                priority="100"/>

  <!-- Date -->
  <xsl:template mode="render-field"
                match="gmd:date"
                priority="100">
    <dl class="gn-date">
      <dt>

        <xsl:value-of select="tr:node-label(tr:create($schema), name(), null)"/>
		<xsl:if test="*/gmd:dateType/*/@codeListValue!=''">(<xsl:value-of select="*/gmd:dateType/*/@codeListValue"/>)</xsl:if>
      </dt>
	  <xsl:variable name="dt">
	  <xsl:choose>
	  <xsl:when test="*/gmd:dateType/*/@codeListValue='creation'"><xsl:text>dateCreated</xsl:text></xsl:when>
	  <xsl:when test="*/gmd:dateType/*/@codeListValue='publication'"><xsl:text>datePublished</xsl:text></xsl:when>
	  <xsl:when test="*/gmd:dateType/*/@codeListValue='modification'"><xsl:text>dateModified</xsl:text></xsl:when>
	  </xsl:choose>
	  </xsl:variable> 
      <dd>
	  <i>
	  <span itemprop="{$dt}">
        <xsl:apply-templates mode="render-value" select="*/gmd:date/*"/>
	  </span>
      </i>
	  </dd>
    </dl>
  </xsl:template>


  <!-- Enumeration -->
  <xsl:template mode="render-field"
                match="gmd:topicCategory[1]|gmd:obligation[1]|gmd:pointInPixel[1]"
                priority="100">
    <dl class="gn-date">
      <dt>
        <xsl:value-of select="tr:node-label(tr:create($schema), name(), null)"/>
      </dt>
      <dd>
          <xsl:for-each select="parent::node()/(gmd:topicCategory|gmd:obligation|gmd:pointInPixel)">
              <xsl:apply-templates mode="render-value"
                                   select="*"/>
          </xsl:for-each>
      </dd>
    </dl>
  </xsl:template>
  <xsl:template mode="render-field"
                match="gmd:topicCategory[position() > 1]|
                        gmd:obligation[position() > 1]|
                        gmd:pointInPixel[position() > 1]"
                priority="100"/>


  <!-- Link to other metadata records -->
  <xsl:template mode="render-field"
                match="*[@uuidref]"
                priority="100">
    <xsl:variable name="nodeName" select="name()"/>

    <!-- Only render the first element of this kind and render a list of
    following siblings. -->
    <xsl:variable name="isFirstOfItsKind"
                  select="count(preceding-sibling::node()[name() = $nodeName]) = 0"/>
    <xsl:if test="$isFirstOfItsKind">
      <dl class="gn-md-associated-resources">
        <dt>
          <xsl:value-of select="tr:node-label(tr:create($schema), name(), null)"/>
        </dt>
        <dd>
          <ul>
            <xsl:for-each select="parent::node()/*[name() = $nodeName]">
              <li><a href="#uuid={@uuidref}">
                <i class="fa fa-link"></i>
                <xsl:value-of select="gn-fn-render:getMetadataTitle(@uuidref, $language)"/>
              </a></li>
            </xsl:for-each>
          </ul>
        </dd>
      </dl>
    </xsl:if>
  </xsl:template>

  <!-- Traverse the tree -->
  <xsl:template mode="render-field"
                match="*">
    <xsl:apply-templates mode="render-field"/>
  </xsl:template>

  <!-- ########################## -->
  <!-- Render values for text ... -->
  <xsl:template mode="render-value"
                match="gco:CharacterString|gco:Integer|gco:Decimal|
       gco:Boolean|gco:Real|gco:Measure|gco:Length|gco:Distance|gco:Angle|gmx:FileName|
       gco:Scale|gco:Record|gco:RecordType|gmx:MimeFileType|gmd:URL|
       gco:LocalName|gml:beginPosition|gml:endPosition">

    <xsl:choose>
      <xsl:when test="contains(., 'http')">
        <!-- Replace hyperlink in text by an hyperlink -->
        <xsl:variable name="textWithLinks"
                      select="replace(., '([a-z][\w-]+:/{1,3}[^\s()&gt;&lt;]+[^\s`!()\[\]{};:'&apos;&quot;.,&gt;&lt;?«»“”‘’])',
                                    '&lt;a href=''$1''&gt;$1&lt;/a&gt;')"/>

        <xsl:if test="$textWithLinks != ''">
          <xsl:copy-of select="saxon:parse(
                          concat('&lt;p&gt;',
                          replace($textWithLinks, '&amp;', '&amp;amp;'),
                          '&lt;/p&gt;'))"/>
        </xsl:if>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="normalize-space(.)"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template mode="render-value"
                match="gmd:PT_FreeText">
    <xsl:apply-templates mode="localised" select="../node()">
      <xsl:with-param name="langId" select="$language"/>
    </xsl:apply-templates>
  </xsl:template>

  <!-- ... URL -->
  <xsl:template mode="render-value"
                match="gmd:URL">
    
    <xsl:variable name="myURL">
    <xsl:if test="not(starts-with(., 'http'))">http://</xsl:if>
    <xsl:value-of select="."/>
    </xsl:variable>            
                
    <a href="{$myURL}" target="_blank"><xsl:value-of select="$myURL"/></a>
  </xsl:template>

  <!-- ... Dates - formatting is made on the client side by the directive  -->
  
  <xsl:template mode="render-value"
                match="gco:Date|gco:DateTime">
    <xsl:value-of select="."/>
  </xsl:template>

  <!-- ... Codelists -->
  <xsl:template mode="render-value"
                match="@codeListValue">
    <xsl:variable name="id" select="."/>
    <xsl:variable name="codelistTranslation"
                  select="tr:codelist-value-label(
                            tr:create($schema),
                            parent::node()/local-name(), $id)"/>
    <xsl:choose>
      <xsl:when test="$codelistTranslation != ''">

        <xsl:variable name="codelistDesc"
                      select="tr:codelist-value-desc(
                            tr:create($schema),
                            parent::node()/local-name(), $id)"/>
        <span title="{$codelistDesc}"><xsl:value-of select="$codelistTranslation"/></span>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$id"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- Enumeration -->
  <xsl:template mode="render-value"
                match="gmd:MD_TopicCategoryCode|
                        gmd:MD_ObligationCode|
                        gmd:MD_PixelOrientationCode">
    <xsl:variable name="id" select="."/>
    <xsl:variable name="codelistTranslation"
                  select="tr:codelist-value-label(
                            tr:create($schema),
                            local-name(), $id)"/>
    <xsl:choose>
      <xsl:when test="$codelistTranslation != ''">

        <xsl:variable name="codelistDesc"
                      select="tr:codelist-value-desc(
                            tr:create($schema),
                            local-name(), $id)"/>
        <span title="{$codelistDesc}" itemprop="keywords"><xsl:value-of select="$codelistTranslation"/></span>
      </xsl:when>
      <xsl:otherwise>
        <span itemprop="keywords"><xsl:value-of select="$id"/></span>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template mode="render-value"
                match="@gco:nilReason[. = 'withheld']"
                priority="100">
    <i class="fa fa-lock text-warning" title="{{{{'withheld' | translate}}}}"></i>
  </xsl:template>
  <xsl:template mode="render-value"
                match="@*"/>
   
				
	
					
  <!-- Starting point -->
  <xsl:template match="/" priority="100">
  
<!-- set schema.org class -->  
<xsl:variable name="oType" select="$metadata/gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue"/>
<xsl:variable name="metadataUuid" select="/root/request/uuid" />
<xsl:variable name="schemaType">
<xsl:choose>
<xsl:when test="$oType='dataset'">http://schema.org/Dataset</xsl:when>
<xsl:when test="$oType='series'">http://schema.org/DataCatalog</xsl:when>
<xsl:when test="$oType='service'">http://schema.org/DataCatalog</xsl:when>
<xsl:when test="$oType='application'">http://schema.org/SoftwareApplication</xsl:when>
<xsl:when test="$oType='collectionHardware'">http://schema.org/Thing</xsl:when>
<xsl:when test="$oType='nonGeographicDataset'">http://schema.org/Dataset</xsl:when>
<xsl:when test="$oType='dimensionGroup'">http://schema.org/Dataset</xsl:when>
<xsl:when test="$oType='featureType'">http://schema.org/Dataset</xsl:when>
<xsl:when test="$oType='model'">http://schema.org/APIReference</xsl:when>
<xsl:when test="$oType='tile'">http://schema.org/Dataset</xsl:when>
<xsl:when test="$oType='fieldSession'">http://schema.org/Thing</xsl:when>
<xsl:when test="$oType='collectionSession'">http://schema.org/Thing</xsl:when>
<xsl:otherwise>http://schema.org/Thing</xsl:otherwise>
</xsl:choose>
</xsl:variable>
  
  <html>
  <head>
  <title><xsl:apply-templates mode="getMetadataTitle" select="$metadata"/></title>
  <xsl:variable name="abs"><xsl:apply-templates mode="getMetadataAbstract" select="$metadata"/></xsl:variable>
  <meta name="description" content="{normalize-space($abs)}"/>
  <xsl:call-template name="css-load"/>
  </head>
  
	<body>
     
	<xsl:call-template name="header"/>
              	
    <div class="container gn-metadata-view">
      <article id="{$metadataUuid}" itemscope="itemscope" itemtype="{$schemaType}">
      <div class="pull-right">
       <meta itemprop="url" content="{$baseurl}/resource/{$metadataUuid}" />
		<a class="btn btn-sm btn-default" href="{$baseurl}/srv/eng/xml.metadata.get?uuid={$metadataUuid}">
          <span>as XML</span>
        </a> <a class="btn btn-sm btn-default" href="{$baseurl}/srv/eng/rdf.metadata.get?uuid={$metadataUuid}">
          <span>as RDF/XML</span>
        </a>
      </div>
        <header>
          <h1 itemprop="name" ><xsl:apply-templates mode="getMetadataTitle" select="$metadata"/></h1>
        </header>
        <p><xsl:apply-templates mode="getMetadataAbstract" select="$metadata"/></p>
        
        <xsl:apply-templates mode="render-view" select="$viewConfig/*"/>
            
      </article>
    </div>

	<xsl:call-template name="footer"/>

	</body>
</html>
  </xsl:template>					
					
							
</xsl:stylesheet>
