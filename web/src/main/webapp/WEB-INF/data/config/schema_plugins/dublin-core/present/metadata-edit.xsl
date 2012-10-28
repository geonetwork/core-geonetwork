<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:dc = "http://purl.org/dc/elements/1.1/"
	xmlns:dct = "http://purl.org/dc/terms/"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xmlns:ows="http://www.opengis.net/ows"
	xmlns:csw="http://www.opengis.net/cat/csw/2.0.2"
	xmlns:geonet="http://www.fao.org/geonetwork"
  exclude-result-prefixes="#all">
  
	<!-- main template - the way into processing dublin-core -->
  <xsl:template name="metadata-dublin-core">
		<xsl:param name="schema"/>
		<xsl:param name="edit" select="false()"/>
		<xsl:param name="embedded"/>
			
    <xsl:apply-templates mode="dublin-core" select="." >
      <xsl:with-param name="schema" select="$schema"/>
      <xsl:with-param name="edit"   select="$edit"/>
      <xsl:with-param name="embedded" select="$embedded" />
    </xsl:apply-templates>
  </xsl:template>

  <!-- simple -->
  <xsl:template name="metadata-dublin-coreview-simple" match="metadata-dublin-coreview-simple">
    
    <xsl:call-template name="md-content">
      <xsl:with-param name="title" select="//dc:title"/>
      <xsl:with-param name="exportButton"/>
      <xsl:with-param name="abstract">
        <xsl:call-template name="addHyperlinksAndLineBreaks">
          <xsl:with-param name="txt">
            <xsl:value-of select="dc:description/text()"/>
          </xsl:with-param>
        </xsl:call-template>
      </xsl:with-param>
      <xsl:with-param name="logo">
        <img src="../../images/logos/{//geonet:info/source}.gif" alt="logo"/>
      </xsl:with-param>
      <xsl:with-param name="relatedResources">
        <xsl:call-template name="dublin-core-relatedResources"/>
      </xsl:with-param>
      <xsl:with-param name="tabs">
        <xsl:call-template name="complexElementSimpleGui">
          <xsl:with-param name="title"
            select="/root/gui/schemas/iso19139/strings/understandResource"/>
          <xsl:with-param name="content">
            <xsl:apply-templates mode="dublin-core" select=".">
              <xsl:with-param name="schema" select="'dublin-core'"/>
              <xsl:with-param name="edit" select="false()"/>
            </xsl:apply-templates>
          </xsl:with-param>
        </xsl:call-template>
        
        <span class="madeBy">
          <xsl:value-of select="/root/gui/strings/uuid"/>&#160;<xsl:value-of select="dc:identifier"/>
        </span>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>


  <!-- List of related resources defined in the online resource section of the metadata record.
  -->
  <xsl:template name="dublin-core-relatedResources">
    <table class="related">
      <tbody>
        <tr style="display:none;"><!-- FIXME needed by JS to append other type of relation from xml.relation service -->
          <td class="main"></td><td></td>
        </tr>
        <xsl:if test="dc:relation">
          <tr>
            <td class="main">
              <span class="WWWDOWNLOAD icon">
                <xsl:value-of
                  select="/root/gui/strings/protocolChoice[@value = 'WWW:DOWNLOAD-1.0-http--download']"
                />
              </span>
            </td>
            <td>
              <ul>
                <xsl:for-each select="dc:relation">
                  <li>
                    <a href="{.}">
                      <xsl:value-of select="."/>
                    </a>
                  </li>
                </xsl:for-each>
              </ul>
            </td>
          </tr>
        </xsl:if>
      </tbody>
    </table>
  </xsl:template>



	<!-- CompleteTab template - dc just calls completeTab from 
	     metadata-utils.xsl -->
	<xsl:template name="dublin-coreCompleteTab">
		<xsl:param name="tabLink"/>

	  <xsl:call-template name="mainTab">
	    <xsl:with-param name="title" select="/root/gui/strings/completeTab"/>
	    <xsl:with-param name="default">metadata</xsl:with-param>
	    <xsl:with-param name="menu">
	      <item label="metadata">metadata</item>
	    </xsl:with-param>
	  </xsl:call-template>
	</xsl:template>

	<!--
	default: in simple mode just a flat list
	-->
	<xsl:template mode="dublin-core" match="*|@*">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>
		
		<xsl:apply-templates mode="element" select=".">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
			<xsl:with-param name="flat"   select="$currTab='simple'"/>
		</xsl:apply-templates>
	</xsl:template>
	
	<!--
	these elements should be boxed
	-->
	<xsl:template mode="dublin-core" match="simpledc|csw:Record">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>

    <xsl:apply-templates mode="elementEP" select="*">
    	<xsl:with-param name="schema" select="$schema"/>
    	<xsl:with-param name="edit"   select="$edit"/>
    </xsl:apply-templates>
	</xsl:template>

	<xsl:template mode="dublin-core" match="dc:anyCHOICE_ELEMENT0">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>

		<xsl:apply-templates mode="elementEP" select="
		                    dc:*|geonet:child[string(@prefix)='dc']|
		                    dct:*|geonet:child[string(@prefix)='dct' and name(.)!='modified']">
      <xsl:with-param name="schema" select="$schema"/>
      <xsl:with-param name="edit"   select="$edit"/>
    </xsl:apply-templates>

	</xsl:template>

	<!--
	identifier
	-->
	<xsl:template mode="dublin-core" match="dc:identifier">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>

		<xsl:apply-templates mode="simpleElement" select=".">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
			<xsl:with-param name="text"><xsl:value-of select="."/></xsl:with-param>
		</xsl:apply-templates>
	</xsl:template>
	
	
  <xsl:template mode="dublin-core" match="dc:description|dc:rights|dc:title">
    <xsl:param name="schema"/>
    <xsl:param name="edit"/>
    
    <xsl:variable name="class">
      <xsl:choose>
        <xsl:when test="name(.)='dc:title'">title</xsl:when>
        <xsl:when test="name(.)='dc:description'">medium</xsl:when>
        <xsl:when test="name(.)='dc:rights'">small</xsl:when>
        <xsl:otherwise></xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    
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
      <xsl:call-template name="getElementText">
        <xsl:with-param name="schema" select="$schema"/>
        <xsl:with-param name="edit"   select="$edit"/>
        <xsl:with-param name="class" select="$class"/>
      </xsl:call-template>
    </xsl:variable>
    
    
    <xsl:apply-templates mode="simpleElement" select=".">
      <xsl:with-param name="schema"   select="$schema"/>
      <xsl:with-param name="edit"     select="$edit"/>
      <xsl:with-param name="title"    select="$title"/>
      <xsl:with-param name="helpLink" select="$helpLink"/>
      <xsl:with-param name="text"     select="$text"/>
    </xsl:apply-templates>
    
  </xsl:template>
	
  <xsl:template mode="dublin-core" match="dc:date|dct:created|dct:dateSubmitted|
    dct:dateAccepted|dct:dateCopyrighted|dct:issued|dct:available|dct:valid">
    <xsl:param name="schema"/>
    <xsl:param name="edit"/>
    
    <xsl:apply-templates mode="simpleElement" select=".">
      <xsl:with-param name="schema"  select="$schema"/>
      <xsl:with-param name="edit"   select="$edit"/>
      <xsl:with-param name="text">
        <xsl:variable name="format">%Y-%m-%d</xsl:variable>
        
        <xsl:call-template name="calendar">
          <xsl:with-param name="ref" select="geonet:element/@ref"/>
          <xsl:with-param name="date" select="."/>
          <xsl:with-param name="format" select="$format"/>
        </xsl:call-template>
        
      </xsl:with-param>
    </xsl:apply-templates>
  </xsl:template>
	
  <xsl:template mode="dublin-core" match="dc:coverage">
    <xsl:param name="schema"/>
    <xsl:param name="edit"/>
    
    <xsl:apply-templates mode="element" select=".">
      <xsl:with-param name="schema" select="$schema"/>
      <xsl:with-param name="edit"   select="$edit"/>
      <xsl:with-param name="flat"   select="$currTab='simple'"/>
    </xsl:apply-templates>
    
    <xsl:if test="not($edit)">
      <xsl:variable name="coverage" select="."/>
      <xsl:variable name="n" select="substring-after($coverage,'North ')"/>
      <xsl:variable name="north" select="substring-before($n,',')"/>
      <xsl:variable name="s" select="substring-after($coverage,'South ')"/>
      <xsl:variable name="south" select="substring-before($s,',')"/>
      <xsl:variable name="e" select="substring-after($coverage,'East ')"/>
      <xsl:variable name="east" select="substring-before($e,',')"/>
      <xsl:variable name="w" select="substring-after($coverage,'West ')"/>
      <xsl:variable name="west" select="substring-before($w,'. ')"/>
      <xsl:variable name="p" select="substring-after($coverage,'(')"/>
      <xsl:variable name="place" select="substring-before($p,')')"/>
      
      <xsl:variable name="geoBox">
        <xsl:call-template name="geoBoxGUI">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="edit"   select="$edit"/>
          <xsl:with-param name="sValue" select="$south"/>
          <xsl:with-param name="nValue" select="$north"/>
          <xsl:with-param name="eValue" select="$east"/>
          <xsl:with-param name="wValue" select="$west"/>
        </xsl:call-template>
      </xsl:variable>
      
      
      
      <xsl:apply-templates mode="complexElement" select=".">
        <xsl:with-param name="schema" select="$schema"/>
        <xsl:with-param name="edit"   select="$edit"/>
        <xsl:with-param name="content">
          <tr>
            <td align="center">
              <xsl:copy-of select="$geoBox"/>
            </td>
          </tr>
        </xsl:with-param>
      </xsl:apply-templates>
    </xsl:if>
  </xsl:template>
	
	
	
	<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
	<!-- dublin-core brief formatting -->
	<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
	
	<xsl:template name="dublin-coreBrief">
		<metadata>
			<xsl:if test="dc:title">
				<title><xsl:value-of select="dc:title"/></title>
			</xsl:if>
			<xsl:if test="dc:description">
				<abstract><xsl:value-of select="dc:description"/></abstract>
			</xsl:if>

			<xsl:for-each select="dc:subject[text()]">
				<keyword><xsl:value-of select="."/></keyword>
			</xsl:for-each>
			<xsl:for-each select="dc:identifier[text()]">
				<link type="url"><xsl:value-of select="."/></link>
			</xsl:for-each>
			<!-- FIXME
			<image>IMAGE</image>
			-->
			<!-- TODO : ows:BoundingBox -->
			<xsl:variable name="coverage" select="dc:coverage"/>
			<xsl:variable name="n" select="substring-after($coverage,'North ')"/>
			<xsl:variable name="north" select="substring-before($n,',')"/>
			<xsl:variable name="s" select="substring-after($coverage,'South ')"/>
			<xsl:variable name="south" select="substring-before($s,',')"/>
			<xsl:variable name="e" select="substring-after($coverage,'East ')"/>
			<xsl:variable name="east" select="substring-before($e,',')"/>
			<xsl:variable name="w" select="substring-after($coverage,'West ')"/>
			<xsl:variable name="west" select="substring-before($w,'. ')"/>
			<xsl:variable name="p" select="substring-after($coverage,'(')"/>
			<xsl:variable name="place" select="substring-before($p,')')"/>
			<xsl:if test="$n!=''">
				<geoBox>
					<westBL><xsl:value-of select="$west"/></westBL>
					<eastBL><xsl:value-of select="$east"/></eastBL>
					<southBL><xsl:value-of select="$south"/></southBL>
					<northBL><xsl:value-of select="$north"/></northBL>
				</geoBox>
			</xsl:if>
		
			<xsl:copy-of select="geonet:*"/>
		</metadata>
	</xsl:template>
				
</xsl:stylesheet>
