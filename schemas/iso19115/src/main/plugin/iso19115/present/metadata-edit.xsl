<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:exslt="http://exslt.org/common"
	xmlns:geonet="http://www.fao.org/geonetwork" exclude-result-prefixes="geonet exslt">
  
  <!-- main template - the way into processing iso19115 -->
  <xsl:template name="metadata-iso19115">
    <xsl:param name="schema"/>
    <xsl:param name="edit" select="false()"/>
    <xsl:param name="embedded"/>
    
    <xsl:apply-templates mode="iso19115" select="." >
      <xsl:with-param name="schema" select="$schema"/>
      <xsl:with-param name="edit"   select="$edit"/>
      <xsl:with-param name="embedded" select="$embedded" />
    </xsl:apply-templates>
  </xsl:template>
  
  <!-- simple -->
  <xsl:template name="metadata-iso19115view-simple" match="metadata-iso19115view-simple">
    
    <xsl:call-template name="md-content">
      <xsl:with-param name="title" select="//idCitation/resTitle"/>
      <xsl:with-param name="exportButton"/>
      <xsl:with-param name="abstract"/>
      <xsl:with-param name="logo"/>
      <xsl:with-param name="relatedResources">
      </xsl:with-param>
      <xsl:with-param name="tabs">
        
        <xsl:apply-templates mode="iso19115" select=".">
          <xsl:with-param name="schema" select="$schema"/>
          <xsl:with-param name="edit" select="false()"/>
        </xsl:apply-templates>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>
	<!--
	default: in simple mode just a flat list
	-->
	<xsl:template mode="iso19115" match="*|@*">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>
		
		<!-- do not show empty elements in view mode -->
		<xsl:choose>
			<xsl:when test="$edit=true()">
				<xsl:apply-templates mode="element" select=".">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="true()"/>
					<xsl:with-param name="flat"   select="$currTab='simple'"/>
				</xsl:apply-templates>
			</xsl:when>
			
			<xsl:otherwise>
				<xsl:variable name="empty">
					<xsl:apply-templates mode="iso19115IsEmpty" select="."/>
				</xsl:variable>
				
				<xsl:if test="$empty!=''">
					<xsl:apply-templates mode="element" select=".">
						<xsl:with-param name="schema" select="$schema"/>
						<xsl:with-param name="edit"   select="false()"/>
						<xsl:with-param name="flat"   select="$currTab='simple'"/>
					</xsl:apply-templates>
				</xsl:if>
				
			</xsl:otherwise>
		</xsl:choose>
			
	</xsl:template>
	
	<!--
	these elements should be boxed
	-->
	<xsl:template mode="iso19115" match="mdContact|dataIdInfo|distInfo|graphOver|descKeys|spatRepInfo|idPoC|onLineSrc|dqInfo|refSysInfo|equScale|projection|ellipsoid|dataExt|geoBox|distributor">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>
		
		<xsl:apply-templates mode="complexElement" select=".">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
		</xsl:apply-templates>
	</xsl:template>
	
	<!--
	Metadata
	-->
	<xsl:template mode="iso19115" match="Metadata">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>
		<xsl:param name="embedded" select="false()"/>
		
		<xsl:choose>
		
			<!-- metadata tab -->
			<xsl:when test="$currTab='metadata'">
				<xsl:call-template name="iso19115Metadata">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
				</xsl:call-template>
			</xsl:when>

			<!-- identification tab -->
			<xsl:when test="$currTab='identification'">
				<xsl:call-template name="iso19115Identification">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
				</xsl:call-template>
			</xsl:when>

			<!-- maintenance tab -->
			<xsl:when test="$currTab='maintenance'">
				<xsl:apply-templates mode="elementEP" select="dataIdInfo/resMaint|dataIdInfo/geonet:child[string(@name)='resMaint']">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
				</xsl:apply-templates>
			</xsl:when>

			<!-- constraints tab -->
			<xsl:when test="$currTab='constraints'">
				<xsl:apply-templates mode="elementEP" select="dataIdInfo/resConst|dataIdInfo/geonet:child[string(@name)='resConst']">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
				</xsl:apply-templates>
			</xsl:when>

			<!-- spatial tab -->
			<xsl:when test="$currTab='spatial'">
				<xsl:call-template name="iso19115Spatial">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
				</xsl:call-template>
			</xsl:when>

			<!-- spatial2 tab -->
			<xsl:when test="$currTab='spatial2'">
				<xsl:apply-templates mode="elementEP" select="spatRepInfo|geonet:child[string(@name)='spatRepInfo']">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
				</xsl:apply-templates>
			</xsl:when>

			<!-- refSys tab -->
			<xsl:when test="$currTab='refSys'">
				<xsl:apply-templates mode="elementEP" select="refSysInfo|geonet:child[string(@name)='refSysInfo']">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
				</xsl:apply-templates>
			</xsl:when>

			<!-- distribution tab -->
			<xsl:when test="$currTab='distribution'">
				<xsl:apply-templates mode="elementEP" select="distInfo|geonet:child[string(@name)='distInfo']">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
				</xsl:apply-templates>
			</xsl:when>

			<!-- embedded distribution tab -->
			<xsl:when test="$currTab='distribution2'">
				<xsl:apply-templates mode="elementEP" select="distInfo/distTranOps">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
				</xsl:apply-templates>
			</xsl:when>
			
			<!-- dataQuality tab -->
			<xsl:when test="$currTab='dataQuality'">
				<xsl:apply-templates mode="elementEP" select="dqInfo|geonet:child[string(@name)='dqInfo']">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
				</xsl:apply-templates>
			</xsl:when>

			<!-- appSchInfo tab -->
			<xsl:when test="$currTab='appSchInfo'">
				<xsl:apply-templates mode="elementEP" select="appSchInfo|geonet:child[string(@name)='appSchInfo']">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
				</xsl:apply-templates>
			</xsl:when>

			<!-- porCatInfo tab -->
			<xsl:when test="$currTab='porCatInfo'">
				<xsl:apply-templates mode="elementEP" select="porCatInfo|geonet:child[string(@name)='porCatInfo']">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
				</xsl:apply-templates>
			</xsl:when>

			<!-- default -->
			<xsl:otherwise>
				<xsl:call-template name="iso19115Simple">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
					<xsl:with-param name="flat"   select="$currTab='simple'"/>
					<xsl:with-param name="embedded" select="$embedded"/>
				</xsl:call-template>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!--
	simple mode; ISO order is:
	- mdFileID
	- mdLang
	- mdChar
	- mdParentID
	- mdHrLv
	- mdHrLvName
	- mdContact
	- mdDateSt
	- mdStanName
	- mdStanVer
	- distInfo
	- dataIdInfo
	- appSchInfo
	- porCatInfo
	- mdMaint
	- mdConst
	- dqInfo
	- spatRepInfo
	- refSysInfo
	- contInfo
	- mdExtInfo
	-->
	<xsl:template name="iso19115Simple">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>
		<xsl:param name="flat"/>
		<xsl:param name="embedded" />

		<xsl:apply-templates mode="elementEP" select="dataIdInfo|geonet:child[string(@name)='dataIdInfo']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
			<xsl:with-param name="flat"   select="$flat"/>
			<xsl:with-param name="embedded" select="$embedded"/>
		</xsl:apply-templates>
		
		<xsl:apply-templates mode="elementEP" select="distInfo|geonet:child[string(@name)='distInfo']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
			<xsl:with-param name="flat"   select="$flat"/>
		</xsl:apply-templates>
		
		<xsl:apply-templates mode="elementEP" select="spatRepInfo|geonet:child[string(@name)='spatRepInfo']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
			<xsl:with-param name="flat"   select="$flat"/>
		</xsl:apply-templates>
		
		<xsl:apply-templates mode="elementEP" select="refSysInfo|geonet:child[string(@name)='refSysInfo']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
			<xsl:with-param name="flat"   select="$flat"/>
		</xsl:apply-templates>
		
		<xsl:apply-templates mode="elementEP" select="appSchInfo|geonet:child[string(@name)='appSchInfo']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
			<xsl:with-param name="flat"   select="$flat"/>
		</xsl:apply-templates>
		
		<xsl:apply-templates mode="elementEP" select="porCatInfo|geonet:child[string(@name)='porCatInfo']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
			<xsl:with-param name="flat"   select="$flat"/>
		</xsl:apply-templates>
		
		<xsl:apply-templates mode="elementEP" select="dqInfo|geonet:child[string(@name)='dqInfo']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
			<xsl:with-param name="flat"   select="$flat"/>
		</xsl:apply-templates>
		
		<xsl:call-template name="complexElementGui">
			<xsl:with-param name="title" select="/root/gui/strings/metadata"/>
			<xsl:with-param name="content">
				<xsl:call-template name="iso19115Simple2">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
			<xsl:with-param name="flat"   select="$flat"/>
				</xsl:call-template>
			</xsl:with-param>
			<xsl:with-param name="schema" select="$schema"/>
		</xsl:call-template>
		
		<xsl:apply-templates mode="elementEP" select="contInfo|geonet:child[string(@name)='contInfo']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
			<xsl:with-param name="flat"   select="$flat"/>
		</xsl:apply-templates>
		
		<xsl:apply-templates mode="elementEP" select="mdExtInfo|geonet:child[string(@name)='mdExtInfo']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
			<xsl:with-param name="flat"   select="$flat"/>
		</xsl:apply-templates>
		
	</xsl:template>
	
	<xsl:template name="iso19115Simple2">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>
		<xsl:param name="flat"/>
		
		<xsl:apply-templates mode="elementEP" select="mdFileID|geonet:child[string(@name)='mdFileID']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
			<xsl:with-param name="flat"   select="$flat"/>
		</xsl:apply-templates>
		
		<xsl:apply-templates mode="elementEP" select="mdLang|geonet:child[string(@name)='mdLang']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
			<xsl:with-param name="flat"   select="$flat"/>
		</xsl:apply-templates>
		
		<xsl:apply-templates mode="elementEP" select="mdChar|geonet:child[string(@name)='mdChar']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
			<xsl:with-param name="flat"   select="$flat"/>
		</xsl:apply-templates>
		
		<xsl:apply-templates mode="elementEP" select="mdParentID|geonet:child[string(@name)='mdParentID']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
			<xsl:with-param name="flat"   select="$flat"/>
		</xsl:apply-templates>
		
		<xsl:apply-templates mode="elementEP" select="mdHrLv|geonet:child[string(@name)='mdHrLv']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
			<xsl:with-param name="flat"   select="$flat"/>
		</xsl:apply-templates>
		
		<xsl:apply-templates mode="elementEP" select="mdHrLvName|geonet:child[string(@name)='mdHrLvName']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
			<xsl:with-param name="flat"   select="$flat"/>
		</xsl:apply-templates>
		
		<xsl:apply-templates mode="elementEP" select="mdDateSt|geonet:child[string(@name)='mdDateSt']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
			<xsl:with-param name="flat"   select="$flat"/>
		</xsl:apply-templates>
		
		<xsl:apply-templates mode="elementEP" select="mdStanName|geonet:child[string(@name)='mdStanName']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
			<xsl:with-param name="flat"   select="$flat"/>
		</xsl:apply-templates>
		
		<xsl:apply-templates mode="elementEP" select="mdStanVer|geonet:child[string(@name)='mdStanVer']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
			<xsl:with-param name="flat"   select="$flat"/>
		</xsl:apply-templates>
		
		<xsl:apply-templates mode="elementEP" select="mdConst|geonet:child[string(@name)='mdConst']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
			<xsl:with-param name="flat"   select="$flat"/>
		</xsl:apply-templates>
		
		<xsl:apply-templates mode="elementEP" select="mdMaint|geonet:child[string(@name)='mdMaint']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
			<xsl:with-param name="flat"   select="$flat"/>
		</xsl:apply-templates>
		
		<xsl:apply-templates mode="elementEP" select="mdContact|geonet:child[string(@name)='mdContact']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
			<xsl:with-param name="flat"   select="$flat"/>
		</xsl:apply-templates>
		
	</xsl:template>

	<xsl:template name="iso19115Metadata">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>
		
		<xsl:apply-templates mode="elementEP" select="mdFileID|geonet:child[string(@name)='mdFileID']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
		</xsl:apply-templates>
		
		<xsl:apply-templates mode="elementEP" select="mdLang|geonet:child[string(@name)='mdLang']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
		</xsl:apply-templates>
		
		<xsl:apply-templates mode="elementEP" select="mdChar|geonet:child[string(@name)='mdChar']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
		</xsl:apply-templates>
		
		<xsl:apply-templates mode="elementEP" select="mdParentID|geonet:child[string(@name)='mdParentID']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
		</xsl:apply-templates>
		
		<xsl:apply-templates mode="elementEP" select="mdHrLv|geonet:child[string(@name)='mdHrLv']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
		</xsl:apply-templates>
		
		<xsl:apply-templates mode="elementEP" select="mdHrLvName|geonet:child[string(@name)='mdHrLvName']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
		</xsl:apply-templates>
		
		<xsl:apply-templates mode="elementEP" select="mdDateSt|geonet:child[string(@name)='mdDateSt']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
		</xsl:apply-templates>
		
		<xsl:apply-templates mode="elementEP" select="mdStanName|geonet:child[string(@name)='mdStanName']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
		</xsl:apply-templates>
		
		<xsl:apply-templates mode="elementEP" select="mdStanVer|geonet:child[string(@name)='mdStanVer']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
		</xsl:apply-templates>
		
		<xsl:apply-templates mode="elementEP" select="mdContact|geonet:child[string(@name)='mdContact']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
		</xsl:apply-templates>
		
		<xsl:apply-templates mode="elementEP" select="contInfo|geonet:child[string(@name)='contInfo']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
		</xsl:apply-templates>
		
		<xsl:apply-templates mode="elementEP" select="mdExtInfo|geonet:child[string(@name)='mdExtInfo']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
		</xsl:apply-templates>
		
		<xsl:apply-templates mode="elementEP" select="mdConst|geonet:child[string(@name)='mdConst']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
		</xsl:apply-templates>
		
		<xsl:apply-templates mode="elementEP" select="mdMaint|geonet:child[string(@name)='mdMaint']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
		</xsl:apply-templates>
		
	</xsl:template>
	
	<xsl:template name="iso19115Identification">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>
		
		<xsl:apply-templates mode="elementEP" select="dataIdInfo/idCitation|dataIdInfo/geonet:child[string(@name)='idCitation']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
		</xsl:apply-templates>
		
		<xsl:apply-templates mode="elementEP" select="dataIdInfo/dataLang|dataIdInfo/geonet:child[string(@name)='dataLang']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
		</xsl:apply-templates>
		
		<xsl:apply-templates mode="elementEP" select="dataIdInfo/dataChar|dataIdInfo/geonet:child[string(@name)='dataChar']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
		</xsl:apply-templates>
		
		<xsl:apply-templates mode="elementEP" select="dataIdInfo/idAbs|dataIdInfo/geonet:child[string(@name)='idAbs']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
		</xsl:apply-templates>
		
		<xsl:apply-templates mode="elementEP" select="dataIdInfo/suppInfo|dataIdInfo/geonet:child[string(@name)='suppInfo']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
		</xsl:apply-templates>
		
		<xsl:apply-templates mode="elementEP" select="dataIdInfo/idPurp|dataIdInfo/geonet:child[string(@name)='idPurp']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
		</xsl:apply-templates>
		
		<xsl:apply-templates mode="elementEP" select="dataIdInfo/idCredit|dataIdInfo/geonet:child[string(@name)='idCredit']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
		</xsl:apply-templates>
		
		<xsl:apply-templates mode="elementEP" select="dataIdInfo/status|dataIdInfo/geonet:child[string(@name)='status']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
		</xsl:apply-templates>
		
		<xsl:apply-templates mode="elementEP" select="dataIdInfo/tpCat|dataIdInfo/geonet:child[string(@name)='tpCat']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
		</xsl:apply-templates>
		
		<xsl:apply-templates mode="elementEP" select="dataIdInfo/descKeys|dataIdInfo/geonet:child[string(@name)='descKeys']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
		</xsl:apply-templates>
		
		<xsl:apply-templates mode="elementEP" select="dataIdInfo/graphOver|dataIdInfo/geonet:child[string(@name)='graphOver']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
		</xsl:apply-templates>
		
		<xsl:apply-templates mode="elementEP" select="dataIdInfo/dsFormat|dataIdInfo/geonet:child[string(@name)='dsFormat']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
		</xsl:apply-templates>
		
		<xsl:apply-templates mode="elementEP" select="dataIdInfo/idSpecUse|dataIdInfo/geonet:child[string(@name)='idSpecUse']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
		</xsl:apply-templates>
		
		<xsl:apply-templates mode="elementEP" select="dataIdInfo/idPoC|dataIdInfo/geonet:child[string(@name)='idPoC']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
		</xsl:apply-templates>
		
		<xsl:apply-templates mode="elementEP" select="dataIdInfo/envirDesc|dataIdInfo/geonet:child[string(@name)='envirDesc']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
		</xsl:apply-templates>
		
		<xsl:apply-templates mode="elementEP" select="dataIdInfo/dataExt|dataIdInfo/geonet:child[string(@name)='dataExt']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
		</xsl:apply-templates>
		
	</xsl:template>
	
	<xsl:template name="iso19115Spatial">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>
		
		<xsl:apply-templates mode="elementEP" select="dataIdInfo/spatRpType|dataIdInfo/geonet:child[string(@name)='spatRpType']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
		</xsl:apply-templates>
		<xsl:apply-templates mode="elementEP" select="dataIdInfo/dataScale|dataIdInfo/geonet:child[string(@name)='dataScale']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
		</xsl:apply-templates>
		<xsl:apply-templates mode="elementEP" select="dataIdInfo/geoBox|dataIdInfo/geonet:child[string(@name)='geoBox']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
		</xsl:apply-templates>
		<xsl:apply-templates mode="elementEP" select="dataIdInfo/geoDesc|dataIdInfo/geonet:child[string(@name)='geoDesc']">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
		</xsl:apply-templates>
	</xsl:template>
	
	<!--
	date and date type
	-->
	<xsl:template mode="iso19115" match="resRefDate">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>
		
		<xsl:choose>
			<xsl:when test="$edit=false()">
				<xsl:apply-templates mode="simpleElement" select=".">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
					<xsl:with-param name="text"><xsl:value-of select="refDate"/><xsl:text> (</xsl:text><xsl:value-of select="refDateType/DateTypCd/@value"/>)</xsl:with-param>
				</xsl:apply-templates>
			</xsl:when>
			
			<xsl:otherwise>
				<xsl:apply-templates mode="element" select=".">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
				</xsl:apply-templates>
			</xsl:otherwise>
		</xsl:choose>
		
	</xsl:template>
	
	<!-- dataIdInfo; ISO order is:
	- idCitation
	- idAbs
	- idPurp
	- idCredit
	- status
	- idPoC
	- resConst
	- dsFormat
	- idSpecUse
	- resMaint
	- descKeys
	- graphOver
	- spatRpType
	- dataScale
	- dataLang
	- dataChar
	- tpCat
	- geoBox
	- geoDesc
	- envirDesc
	- dataExt
	- suppInfo
	-->
	<xsl:template mode="iso19115" match="dataIdInfo">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>
		<xsl:param name="embedded" select="false()"/>

		<xsl:variable name="content">
		
			<xsl:apply-templates mode="elementEP" select="idCitation|geonet:child[string(@name)='idCitation']">
				<xsl:with-param name="schema" select="$schema"/>
				<xsl:with-param name="edit"   select="$edit"/>
			</xsl:apply-templates>
			
			<xsl:apply-templates mode="elementEP" select="dataLang|geonet:child[string(@name)='dataLang']">
				<xsl:with-param name="schema" select="$schema"/>
				<xsl:with-param name="edit"   select="$edit"/>
			</xsl:apply-templates>
			
			<xsl:apply-templates mode="elementEP" select="dataChar|geonet:child[string(@name)='dataChar']">
				<xsl:with-param name="schema" select="$schema"/>
				<xsl:with-param name="edit"   select="$edit"/>
			</xsl:apply-templates>
			
			<xsl:apply-templates mode="elementEP" select="idAbs|geonet:child[string(@name)='idAbs']">
				<xsl:with-param name="schema" select="$schema"/>
				<xsl:with-param name="edit"   select="$edit"/>
			</xsl:apply-templates>
			
			<xsl:apply-templates mode="elementEP" select="suppInfo|geonet:child[string(@name)='suppInfo']">
				<xsl:with-param name="schema" select="$schema"/>
				<xsl:with-param name="edit"   select="$edit"/>
			</xsl:apply-templates>
			
			<xsl:apply-templates mode="elementEP" select="idPurp|geonet:child[string(@name)='idPurp']">
				<xsl:with-param name="schema" select="$schema"/>
				<xsl:with-param name="edit"   select="$edit"/>
			</xsl:apply-templates>
			
			<xsl:apply-templates mode="elementEP" select="idCredit|geonet:child[string(@name)='idCredit']">
				<xsl:with-param name="schema" select="$schema"/>
				<xsl:with-param name="edit"   select="$edit"/>
			</xsl:apply-templates>
			
			<xsl:apply-templates mode="elementEP" select="status|geonet:child[string(@name)='status']">
				<xsl:with-param name="schema" select="$schema"/>
				<xsl:with-param name="edit"   select="$edit"/>
			</xsl:apply-templates>
			
			<xsl:apply-templates mode="elementEP" select="tpCat|geonet:child[string(@name)='tpCat']">
				<xsl:with-param name="schema" select="$schema"/>
				<xsl:with-param name="edit"   select="$edit"/>
			</xsl:apply-templates>
			
			<xsl:apply-templates mode="elementEP" select="descKeys|geonet:child[string(@name)='descKeys']">
				<xsl:with-param name="schema" select="$schema"/>
				<xsl:with-param name="edit"   select="$edit"/>
			</xsl:apply-templates>
			
			<xsl:apply-templates mode="elementEP" select="graphOver|geonet:child[string(@name)='graphOver']">
				<xsl:with-param name="schema" select="$schema"/>
				<xsl:with-param name="edit"   select="$edit"/>
			</xsl:apply-templates>
			
			<xsl:apply-templates mode="elementEP" select="spatRpType|geonet:child[string(@name)='spatRpType']">
				<xsl:with-param name="schema" select="$schema"/>
				<xsl:with-param name="edit"   select="$edit"/>
			</xsl:apply-templates>
			
			<xsl:apply-templates mode="elementEP" select="dataScale|geonet:child[string(@name)='dataScale']">
				<xsl:with-param name="schema" select="$schema"/>
				<xsl:with-param name="edit"   select="$edit"/>
			</xsl:apply-templates>
			
			<xsl:apply-templates mode="elementEP" select="geoBox|geonet:child[string(@name)='geoBox']">
				<xsl:with-param name="schema" select="$schema"/>
				<xsl:with-param name="edit"   select="$edit"/>
			</xsl:apply-templates>
			
			<xsl:apply-templates mode="elementEP" select="geoDesc|geonet:child[string(@name)='geoDesc']">
				<xsl:with-param name="schema" select="$schema"/>
				<xsl:with-param name="edit"   select="$edit"/>
			</xsl:apply-templates>
			
			<xsl:apply-templates mode="elementEP" select="dataExt|geonet:child[string(@name)='dataExt']">
				<xsl:with-param name="schema" select="$schema"/>
				<xsl:with-param name="edit"   select="$edit"/>
			</xsl:apply-templates>
			
			<xsl:apply-templates mode="elementEP" select="resConst|geonet:child[string(@name)='resConst']">
				<xsl:with-param name="schema" select="$schema"/>
				<xsl:with-param name="edit"   select="$edit"/>
			</xsl:apply-templates>
			
			<xsl:apply-templates mode="elementEP" select="dsFormat|geonet:child[string(@name)='dsFormat']">
				<xsl:with-param name="schema" select="$schema"/>
				<xsl:with-param name="edit"   select="$edit"/>
			</xsl:apply-templates>
			
			<xsl:apply-templates mode="elementEP" select="idSpecUse|geonet:child[string(@name)='idSpecUse']">
				<xsl:with-param name="schema" select="$schema"/>
				<xsl:with-param name="edit"   select="$edit"/>
			</xsl:apply-templates>
			
			<xsl:apply-templates mode="elementEP" select="resMaint|geonet:child[string(@name)='resMaint']">
				<xsl:with-param name="schema" select="$schema"/>
				<xsl:with-param name="edit"   select="$edit"/>
			</xsl:apply-templates>
			
			<xsl:apply-templates mode="elementEP" select="idPoC|geonet:child[string(@name)='idPoC']">
				<xsl:with-param name="schema" select="$schema"/>
				<xsl:with-param name="edit"   select="$edit"/>
			</xsl:apply-templates>
			
			<xsl:apply-templates mode="elementEP" select="envirDesc|geonet:child[string(@name)='envirDesc']">
				<xsl:with-param name="schema" select="$schema"/>
				<xsl:with-param name="edit"   select="$edit"/>
			</xsl:apply-templates>
	
		</xsl:variable>
		
		<xsl:apply-templates mode="complexElement" select=".">
			<xsl:with-param name="schema"  select="$schema"/>
			<xsl:with-param name="edit"    select="$edit"/>
			<xsl:with-param name="content" select="$content"/>
		</xsl:apply-templates>
		
	</xsl:template>
	
	<!--
	rpCntInfo: ISO order is:
	- rpIndName
	- rpOrgName
	- rpPosName
	- rpCntInfo
	- role
	-->
	<xsl:template mode="iso19115" match="mdContact|idPoC">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>

		<xsl:variable name="content">
			<xsl:if test="*">
				<td class="padded-content" width="100%" colspan="2">
					<table width="100%">
						<tr>
							<td width="50%" valign="top">
								<table width="100%">

									<xsl:apply-templates mode="elementEP" select="rpIndName|geonet:child[string(@name)='rpIndName']">
										<xsl:with-param name="schema" select="$schema"/>
										<xsl:with-param name="edit"   select="$edit"/>
									</xsl:apply-templates>
									
									<xsl:apply-templates mode="elementEP" select="rpOrgName|geonet:child[string(@name)='rpOrgName']">
										<xsl:with-param name="schema" select="$schema"/>
										<xsl:with-param name="edit"   select="$edit"/>
									</xsl:apply-templates>
									
									<xsl:apply-templates mode="elementEP" select="rpPosName|geonet:child[string(@name)='rpPosName']">
										<xsl:with-param name="schema" select="$schema"/>
										<xsl:with-param name="edit"   select="$edit"/>
									</xsl:apply-templates>
									
									<xsl:apply-templates mode="elementEP" select="role|geonet:child[string(@name)='role']">
										<xsl:with-param name="schema" select="$schema"/>
										<xsl:with-param name="edit"   select="$edit"/>
									</xsl:apply-templates>
									
								</table>
							</td>
							<td valign="top">
								<table width="100%">
									<xsl:apply-templates mode="elementEP" select="rpCntInfo|geonet:child[string(@name)='rpCntInfo']">
										<xsl:with-param name="schema" select="$schema"/>
										<xsl:with-param name="edit"   select="$edit"/>
									</xsl:apply-templates>
								</table>
							</td>
						</tr>
					</table>
				</td>
			</xsl:if>
		</xsl:variable>

		<xsl:apply-templates mode="complexElement" select=".">
			<xsl:with-param name="schema"  select="$schema"/>
			<xsl:with-param name="edit"    select="$edit"/>
			<xsl:with-param name="content" select="$content"/>
		</xsl:apply-templates>

	</xsl:template>
	
	<!--
	codelists
	-->
	<xsl:template mode="iso19115" match="*[(substring(name(*), string-length(name(*))-1, 2)='Cd' and name(.)!='mdChar') or name(*)='languageCode']">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>
		
		<xsl:call-template name="iso19115Codelist">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
		</xsl:call-template>
	</xsl:template>
	
	<xsl:template name="iso19115Codelist">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>
		
		<xsl:apply-templates mode="simpleElement" select=".">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
			<xsl:with-param name="text">
				<xsl:apply-templates mode="iso19115GetAttributeText" select="*/@value">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
				</xsl:apply-templates>
			</xsl:with-param>
		</xsl:apply-templates>
	</xsl:template>
	
	<xsl:template mode="iso19115GetAttributeText" match="@*">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>
		
		<xsl:call-template name="getAttributeText">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
		</xsl:call-template>
	</xsl:template>
	
	<!--
	make the following fields always not editable:
	mdDateSt
	mdStanName
	mdStanVer
	mdFileID
	mdChar
	-->
	<xsl:template mode="iso19115" match="mdDateSt|mdStanName|mdStanVer|mdFileID">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>
		
		<xsl:apply-templates mode="element" select=".">
			<xsl:with-param name="schema"  select="$schema"/>
			<xsl:with-param name="edit"    select="false()"/>
		</xsl:apply-templates>
	</xsl:template>

	<xsl:template mode="iso19115" match="mdChar">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>
		
		<xsl:call-template name="iso19115Codelist">
			<xsl:with-param name="schema"  select="$schema"/>
			<xsl:with-param name="edit"    select="false()"/>
		</xsl:call-template>
	</xsl:template>
	
	<!--
	online resources
	-->
	<xsl:template mode="iso19115" match="onLineSrc">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>
		
		<xsl:choose>
			<xsl:when test="$edit=true()">
				<xsl:apply-templates mode="iso19115EditOnlineRes" select=".">
					<xsl:with-param name="schema" select="$schema"/>
				</xsl:apply-templates>
			</xsl:when>
			<xsl:when test="string(linkage)!=''">
				<xsl:apply-templates mode="simpleElement" select=".">
					<xsl:with-param name="schema"  select="$schema"/>
					<xsl:with-param name="text">
						<a href="{linkage}" target="_new">
							<xsl:choose>
								<xsl:when test="string(orDesc)!=''">
									<xsl:value-of select="orDesc"/>
								</xsl:when>
								<xsl:otherwise>
									<xsl:value-of select="linkage"/>
								</xsl:otherwise>
							</xsl:choose>
						</a>
					</xsl:with-param>
				</xsl:apply-templates>
			</xsl:when>
		</xsl:choose>
	</xsl:template>

	<xsl:template mode="iso19115EditOnlineRes" match="*">
		<xsl:param name="schema"/>
	
		<xsl:variable name="id" select="generate-id(.)"/>
		<div id="{$id}"/>
		<xsl:apply-templates mode="complexElement" select=".">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="true()"/>
			<xsl:with-param name="content">
				
				<xsl:apply-templates mode="elementEP" select="linkage|geonet:child[string(@name)='linkage']">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="true()"/>
				</xsl:apply-templates>
				
				<xsl:apply-templates mode="elementEP" select="protocol|geonet:child[string(@name)='protocol']">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="true()"/>
				</xsl:apply-templates>
				
				<xsl:apply-templates mode="elementEP" select="appProfile|geonet:child[string(@name)='appProfile']">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="true()"/>
				</xsl:apply-templates>
				
				<xsl:choose>
					<xsl:when test="protocol='WWW:DOWNLOAD-1.0-http--download' and string(orName)!=''">
						<xsl:apply-templates mode="iso19115FileRemove" select="orName">
							<xsl:with-param name="access" select="'private'"/>
							<xsl:with-param name="id" select="$id"/>
						</xsl:apply-templates>
					</xsl:when>
					<xsl:when test="protocol='WWW:DOWNLOAD-1.0-http--download' and orName">
						<xsl:apply-templates mode="iso19115FileUpload" select="orName">
							<xsl:with-param name="access" select="'private'"/>
							<xsl:with-param name="id" select="$id"/>
						</xsl:apply-templates>
					</xsl:when>
					<xsl:when test="protocol='WWW:LINK-1.0-http--link'"/> <!-- hide orName for www links -->
					<xsl:otherwise>
						<xsl:apply-templates mode="elementEP" select="orName|geonet:child[string(@name)='orName']">
							<xsl:with-param name="schema" select="$schema"/>
							<xsl:with-param name="edit"   select="true()"/>
						</xsl:apply-templates>
					</xsl:otherwise>
				</xsl:choose>

				<xsl:apply-templates mode="elementEP" select="orDesc|geonet:child[string(@name)='orDesc']">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="true()"/>
				</xsl:apply-templates>
				
				<xsl:apply-templates mode="elementEP" select="orFunct|geonet:child[string(@name)='orFunct']">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="true()"/>
				</xsl:apply-templates>
			</xsl:with-param>
		</xsl:apply-templates>
	</xsl:template>
	
	<!--
	online resources: WMS get map
	-->
	<xsl:template mode="iso19115" match="onLineSrc[starts-with(protocol,'OGC:WMS-') and contains(protocol,'-get-map') and orName]">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>
		<xsl:variable name="linkage" select="linkage" />
		<xsl:variable name="name" select="normalize-space(orName)" />
		<xsl:variable name="description" select="normalize-space(orDesc)" />
		
		<xsl:choose>
			<xsl:when test="$edit=true()">
				<xsl:apply-templates mode="iso19115EditOnlineRes" select=".">
					<xsl:with-param name="schema" select="$schema"/>
				</xsl:apply-templates>
			</xsl:when>
			<xsl:when test="string(../../../geonet:info/dynamic)='true' and string($name)!='' and string($linkage)!=''">
			<!-- Create a link for a WMS service that will open in InterMap opensource -->
				<xsl:apply-templates mode="simpleElement" select=".">
					<xsl:with-param name="schema"  select="$schema"/>
					<xsl:with-param name="title"  select="/root/gui/strings/interactiveMap"/>
					<xsl:with-param name="text">
						<a href="javascript:addWMSServerLayers('{$linkage}')" title="{/root/strings/interactiveMap}"> 
								<xsl:choose>
								<xsl:when test="string($description)!=''">
									<xsl:value-of select="$description"/>
								</xsl:when>
								<xsl:otherwise>
									<xsl:value-of select="$name"/>
								</xsl:otherwise>
							</xsl:choose>
						</a>   (OGC-WMS Service: <xsl:value-of select="$linkage"/>)
					</xsl:with-param>
				</xsl:apply-templates>
				<!-- Create a link for a WMS service that will open in Google Earth through the reflector -->
				<xsl:apply-templates mode="simpleElement" select=".">
					<xsl:with-param name="schema"  select="$schema"/>
					<xsl:with-param name="title"  select="/root/gui/strings/viewInGE"/>
					<xsl:with-param name="text">
						<a href="{/root/gui/locService}/google.kml?uuid={../../../geonet:info/uuid}&amp;layers={$name}" title="{/root/strings/interactiveMap}">
							<xsl:choose>
								<xsl:when test="string($description)!=''">
									<xsl:value-of select="$description"/>
								</xsl:when>
								<xsl:otherwise>
									<xsl:value-of select="$name"/>
								</xsl:otherwise>
							</xsl:choose>
							&#160;
							<img src="{/root/gui/url}/images/google_earth_link.gif" alt="{/root/gui/strings/viewInGE}" style="border: 0px solid;"/>
						</a>
					</xsl:with-param>
				</xsl:apply-templates>
			</xsl:when>
		</xsl:choose>
	</xsl:template>

	<!--
	online resources: WMS get capabilities
	-->
	<xsl:template mode="iso19115" match="onLineSrc[starts-with(protocol,'OGC:WMS-') and contains(protocol,'-get-capabilities') and orName]">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>
		<xsl:variable name="linkage" select="linkage" />
		<xsl:variable name="name" select="normalize-space(orName)" />
		<xsl:variable name="description" select="normalize-space(orDesc)" />
		
		<xsl:choose>
			<xsl:when test="$edit=true()">
				<xsl:apply-templates mode="iso19115EditOnlineRes" select=".">
					<xsl:with-param name="schema" select="$schema"/>
				</xsl:apply-templates>
			</xsl:when>
			<xsl:when test="string(../../../geonet:info/dynamic)='true' and string($linkage)!=''">
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

	<!--
	online resources: download
	-->
	<xsl:template mode="iso19115" match="onLineSrc[starts-with(./protocol,'WWW:DOWNLOAD-') and contains(./protocol,'http--download') and ./orName]">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>
		<xsl:variable name="download_check"><xsl:text>&amp;fname=&amp;access</xsl:text></xsl:variable>
		<xsl:variable name="linkage" select="linkage" />
		<xsl:variable name="name" select="normalize-space(orName)" />
		<xsl:variable name="description" select="normalize-space(orDesc)" />
		
		<xsl:choose>
			<xsl:when test="$edit=true()">
				<xsl:apply-templates mode="iso19115EditOnlineRes" select=".">
					<xsl:with-param name="schema" select="$schema"/>
				</xsl:apply-templates>
			</xsl:when>
			<xsl:when test="string(../../../geonet:info/download)='true' and string($linkage)!='' and not(contains($linkage,$download_check))">
				<xsl:apply-templates mode="simpleElement" select=".">
					<xsl:with-param name="schema"  select="$schema"/>
					<xsl:with-param name="title"  select="/root/gui/strings/downloadData"/>
					<xsl:with-param name="text">
						<a href="{$linkage}" target="_blank">
							<xsl:choose>
								<xsl:when test="string($description)!=''">
									<xsl:value-of select="$description"/>
								</xsl:when>
								<xsl:otherwise>
									<xsl:value-of select="$description"/>
								</xsl:otherwise>
							</xsl:choose>
						</a>
					</xsl:with-param>
				</xsl:apply-templates>
			</xsl:when>
		</xsl:choose>
	</xsl:template>
	
	<!--
	graphOver
	-->
	<xsl:template mode="iso19115" match="graphOver">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>
		
		<xsl:choose>
		  <xsl:when test="$edit=true()">
		    <xsl:apply-templates mode="iso19115EditGraphOver" select=".">
		      <xsl:with-param name="schema" select="$schema"/>
		    </xsl:apply-templates>
		  </xsl:when>
		  <xsl:otherwise>
		    <xsl:apply-templates mode="simpleElement" select=".">
  		    <xsl:with-param name="schema" select="$schema" />
  		    <xsl:with-param name="text">&#160;
  		      
  		      <xsl:variable name="imageTitle">
  		        <xsl:choose>
  		          <xsl:when test="bgFileDesc">
  		            <xsl:value-of select="bgFileDesc"/>
  		          </xsl:when>
  		          <xsl:otherwise>
  		            <xsl:value-of select="bgFileName"/>
  		          </xsl:otherwise>
  		        </xsl:choose>
  		      </xsl:variable>
  		      
  		      <xsl:variable name="fileName" select="bgFileName"/>
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
	
	<xsl:template mode="iso19115EditGraphOver" match="*">
		<xsl:param name="schema"/>
	
		<xsl:variable name="id" select="generate-id(.)"/>
		<div id="{$id}"/>
		<xsl:apply-templates mode="complexElement" select=".">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="true()"/>
			<xsl:with-param name="content">
			
				<xsl:choose>
					<xsl:when test="(string(bgFileDesc)='thumbnail' or string(bgFileDesc)='large_thumbnail') and string(bgFileName)!=''">
						<xsl:apply-templates mode="iso19115FileRemove" select="bgFileName">
							<xsl:with-param name="id" select="$id"/>
						</xsl:apply-templates>
					</xsl:when>
					<xsl:when test="string(bgFileDesc)='thumbnail' or string(bgFileDesc)='large_thumbnail'">
						<xsl:apply-templates mode="iso19115FileUpload" select="bgFileName">
							<xsl:with-param name="id" select="$id"/>
						</xsl:apply-templates>
					</xsl:when>
					<xsl:otherwise>
						<xsl:apply-templates mode="elementEP" select="bgFileName|geonet:child[string(@name)='bgFileName']">
							<xsl:with-param name="schema" select="$schema"/>
							<xsl:with-param name="edit"   select="true()"/>
						</xsl:apply-templates>
					</xsl:otherwise>
				</xsl:choose>

				<xsl:apply-templates mode="elementEP" select="bgFileDesc|geonet:child[string(@name)='bgFileDesc']">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="true()"/>
				</xsl:apply-templates>
				
				<xsl:apply-templates mode="elementEP" select="bgFileType|geonet:child[string(@name)='bgFileType']">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="true()"/>
				</xsl:apply-templates>
			</xsl:with-param>
		</xsl:apply-templates>
	</xsl:template>
	
	<!--
	bgFileDesc
	-->
	<xsl:template mode="iso19115" match="bgFileDesc">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>
		
		<xsl:choose>
			<xsl:when test="$edit=true()">
				<xsl:apply-templates mode="simpleElement" select=".">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="text">
						<xsl:variable name="value" select="string(.)"/>
						<select name="_{geonet:element/@ref}" size="1">
							<xsl:if test="string(.)=''">
								<option value=""/>
							</xsl:if>
							<xsl:for-each select="/root/gui/strings/bgFileDescChoice[@value]">
								<option value="{string(@value)}">
									<xsl:if test="string(@value)=$value">
										<xsl:attribute name="selected"/>
									</xsl:if>
									<xsl:value-of select="string(.)"/>
								</option>
							</xsl:for-each>
						</select>
					</xsl:with-param>
				</xsl:apply-templates>
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
  <!-- Templates to retrieve thumbnails -->
  <xsl:template mode="get-thumbnail" match="Metadata">
    <xsl:apply-templates mode="get-thumbnail" select="//graphOver"/>
  </xsl:template>
  
  <xsl:template mode="get-thumbnail" match="graphOver">
    <xsl:variable name="fileName" select="bgFileName"/>
    <xsl:variable name="desc" select="bgFileDesc"/>
    <xsl:variable name="info" select="ancestor::*[name(.) = 'Metadata']/geonet:info"></xsl:variable>
    
    <thumbnail>
      <href><xsl:value-of select="geonet:get-thumbnail-url($fileName, $info, /root/gui/locService)"/></href>
      <desc><xsl:value-of select="$desc"/></desc>
      <mimetype><xsl:value-of select="bgFileType"/></mimetype>
      <type><xsl:value-of select="if (geonet:contains-any-of($desc, ('thumbnail', 'large_thumbnail'))) then 'local' else ''"/></type>
    </thumbnail>
  </xsl:template>
  
	<!--
	eMailAdd
	-->
	<xsl:template mode="iso19115" match="eMailAdd">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>
		
		<xsl:choose>
			<xsl:when test="$edit=true()">
				<xsl:apply-templates mode="element" select=".">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="true()"/>
				</xsl:apply-templates>
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
	descKeys
	-->
	<xsl:template mode="iso19115" match="descKeys">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>
		
		<xsl:choose>
			<xsl:when test="$edit=true()">
				<xsl:apply-templates mode="element" select=".">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="true()"/>
				</xsl:apply-templates>
			</xsl:when>
			<xsl:otherwise>
				<xsl:apply-templates mode="simpleElement" select=".">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="text">
						<xsl:for-each select="keyword">
							<xsl:if test="position() &gt; 1">,	</xsl:if>
							<xsl:value-of select="."/>
						</xsl:for-each>
						<xsl:if test="keyTyp/KeyTypCd/@value!=''">
							<xsl:text> (</xsl:text>
							<xsl:value-of select="keyTyp/KeyTypCd/@value"/>
							<xsl:text>)</xsl:text>
						</xsl:if>
						<xsl:text>.</xsl:text>
					</xsl:with-param>
				</xsl:apply-templates>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!--
	place keyword; only called in edit mode (see descKeys template)
	-->
	<xsl:template mode="iso19115" match="keyword[following-sibling::keyTyp/KeyTypCd/@value='place']">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>
		
		<xsl:variable name="text">
			<xsl:variable name="ref" select="geonet:element/@ref"/>
			<xsl:variable name="keyword" select="string(.)"/>
			
			<input class="md" type="text" name="_{$ref}" value="{text()}" size="50" />

			<!-- regions combobox -->

			<xsl:variable name="lang" select="/root/gui/language"/>

			<select name="place" size="1" onChange="document.mainForm._{$ref}.value=this.options[this.selectedIndex].text">
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
		
	<!--
	geoBox
	-->
	<xsl:template mode="iso19115" match="geoBox">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>
		
		<xsl:variable name="geoBox">
			<xsl:apply-templates mode="iso19115GeoBox" select=".">
				<xsl:with-param name="schema" select="$schema"/>
				<xsl:with-param name="edit"   select="$edit"/>
			</xsl:apply-templates>
		</xsl:variable>
		
		<xsl:choose>
			<xsl:when test="$edit=true()">
				<xsl:variable name="places">
					<xsl:variable name="ref" select="geonet:element/@ref"/>
					<xsl:variable name="keyword" select="string(.)"/>
					
					<xsl:variable name="selection" select="concat(westBL,';',eastBL,';',southBL,';',northBL)"/>

					<!-- regions combobox -->

					<xsl:variable name="lang" select="/root/gui/language"/>

					<select name="place" size="1" onChange="javascript:setRegion(document.mainForm._{westBL/geonet:element/@ref}, document.mainForm._{eastBL/geonet:element/@ref}, document.mainForm._{southBL/geonet:element/@ref}, document.mainForm._{northBL/geonet:element/@ref}, this.options[this.selectedIndex].value)">
						<option value=""/>
						<xsl:for-each select="/root/gui/regions/record">
							<xsl:sort select="label/child::*[name() = $lang]" order="ascending"/>

							<xsl:variable name="value" select="concat(west,';',east,';',south,';',north)"/>
							<option value="{$value}">
								<xsl:if test="$value=$selection">
									<xsl:attribute name="selected"/>
								</xsl:if>
								<xsl:value-of select="label/child::*[name() = $lang]"/>
							</option>
						</xsl:for-each>
					</select>
				</xsl:variable>
				<xsl:apply-templates mode="complexElement" select=".">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
					<xsl:with-param name="content">
						<tr>
							<td align="center">
								<xsl:copy-of select="$geoBox"/>
							</td>
							<td>
								<xsl:copy-of select="$places"/>
							</td>
						</tr>
					</xsl:with-param>
				</xsl:apply-templates>
			</xsl:when>
			<xsl:otherwise>
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
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template mode="iso19115GeoBox" match="*">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>

		<table>
			<tr>
				<td/>
				<td class="padded" align="center">
					<xsl:apply-templates mode="iso19115VertElement" select="northBL">
						<xsl:with-param name="schema" select="$schema"/>
						<xsl:with-param name="edit"   select="$edit"/>
					</xsl:apply-templates>
				</td>
				<td/>
			</tr>
			<tr>
				<td class="padded" align="center">
					<xsl:apply-templates mode="iso19115VertElement" select="westBL">
						<xsl:with-param name="schema" select="$schema"/>
						<xsl:with-param name="edit"   select="$edit"/>
					</xsl:apply-templates>
				</td>
				
				<!--
				<td class="box" width="100" height="100" align="center">
				-->
				<xsl:variable name="md">
					<xsl:apply-templates mode="brief" select="../.."/>
				</xsl:variable>
				<xsl:variable name="metadata" select="exslt:node-set($md)/*[1]"/>
				<td width="100" height="100" align="center">
					<!--xsl:call-template name="thumbnail">
						<xsl:with-param name="metadata" select="$metadata"/>
					</xsl:call-template-->
				</td>
				
				<td class="padded" align="center">
					<xsl:apply-templates mode="iso19115VertElement" select="eastBL">
						<xsl:with-param name="schema" select="$schema"/>
						<xsl:with-param name="edit"   select="$edit"/>
					</xsl:apply-templates>
				</td>
			</tr>
			<tr>
				<td/>
				<td class="padded" align="center">
					<xsl:apply-templates mode="iso19115VertElement" select="southBL">
						<xsl:with-param name="schema" select="$schema"/>
						<xsl:with-param name="edit"   select="$edit"/>
					</xsl:apply-templates>
				</td>
				<td/>
			</tr>
		</table>
	</xsl:template>
	
	<xsl:template mode="iso19115VertElement" match="*">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>
		
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
		<b>
			<xsl:value-of select="$title"/>
		</b>
		<br/>
		<xsl:call-template name="getElementText">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
		</xsl:call-template>
	</xsl:template>

	<!--
	idAbs
	-->
	<xsl:template mode="iso19115" match="idAbs">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>
		
		<xsl:apply-templates mode="simpleElement" select=".">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
			<xsl:with-param name="text">
				<xsl:call-template name="getElementText">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
				</xsl:call-template>
			</xsl:with-param>
		</xsl:apply-templates>
	</xsl:template>
	
	<!--
	suppInfo
	idPurp
	-->
	<xsl:template mode="iso19115" match="suppInfo|idPurp">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>
		
		<xsl:apply-templates mode="simpleElement" select=".">
			<xsl:with-param name="schema" select="$schema"/>
			<xsl:with-param name="edit"   select="$edit"/>
			<xsl:with-param name="text">
				<xsl:call-template name="getElementText">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
				</xsl:call-template>
			</xsl:with-param>
		</xsl:apply-templates>
	</xsl:template>
	
	<!--
	DateTime (format = %Y-%m-%dT%H:%M:00)
	measDateTm
	stepDateTm
	usageDate
	planAvDtTm
	begin (with TM_Period as parent element)
	end (with TM_Period as parent element)
	clkTime (should only select time, not date. so not added at this point as the calendar is maybe not the best option for this)
	-->
	<xsl:template mode="iso19115" match="measDateTm|stepDateTm|usageDate|planAvDtTm|begin[parent::TM_Period]|end[parent::TM_Period]">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>
		
		<xsl:choose>
			<xsl:when test="$edit=true()">
				<xsl:apply-templates mode="simpleElement" select=".">
				  <xsl:with-param name="schema"  select="$schema"/>
				  <xsl:with-param name="edit"  select="$edit"/>
				  <xsl:with-param name="text">
				    <xsl:variable name="ref" select="geonet:element/@ref"/>
				    <xsl:call-template name="calendar">
				      <xsl:with-param name="ref" select="$ref"/>
				      <xsl:with-param name="date" select="text()"/>
				      <xsl:with-param name="format" select="'%Y-%m-%dT%H:%M:00'"/>
				    </xsl:call-template>
				  </xsl:with-param>
				</xsl:apply-templates>
			</xsl:when>
			<xsl:otherwise>
				<xsl:apply-templates mode="element" select=".">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
				</xsl:apply-templates>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!--
	- Date (format = %Y-%m-%d)
	refDate (child from CI_Date)
	resEdDate
	dateNext
	calDate
	mdDateSt is not editable (!we use DateTime instead of only Date!)
	-->
	<xsl:template mode="iso19115" match="refDate|resEdDate|dateNext|calDate">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>
		
		<xsl:choose>
			<xsl:when test="$edit=true()">
				<xsl:apply-templates mode="simpleElement" select=".">
				  <xsl:with-param name="schema"  select="$schema"/>
				  <xsl:with-param name="edit"  select="$edit"/>
				  <xsl:with-param name="text">
				    <xsl:variable name="ref" select="geonet:element/@ref"/>
				    <xsl:call-template name="calendar">
				      <xsl:with-param name="ref" select="$ref"/>
				      <xsl:with-param name="date" select="text()"/>
				      <xsl:with-param name="format" select="'%Y-%m-%d'"/>
				    </xsl:call-template>
					</xsl:with-param>
				</xsl:apply-templates>
			</xsl:when>
			<xsl:otherwise>
				<xsl:apply-templates mode="element" select=".">
					<xsl:with-param name="schema" select="$schema"/>
					<xsl:with-param name="edit"   select="$edit"/>
				</xsl:apply-templates>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<!--
	placeholder
	<xsl:template mode="iso19115" match="TAG">
		<xsl:param name="schema"/>
		<xsl:param name="edit"/>
		
		BODY
	</xsl:template>
	-->
	
	<!--
	file upload/download utilities
	-->
	
	<xsl:template mode="iso19115FileUpload" match="*">
		<xsl:param name="access" select="'public'"/>
		<xsl:param name="id"/>
	
		<xsl:call-template name="simpleElementGui">
			<xsl:with-param name="title" select="/root/gui/strings/file"/>
			<xsl:with-param name="text">
				<table width="100%"><tr>
					<xsl:variable name="ref" select="geonet:element/@ref"/>
					<td width="70%"><input type="file" class="content" name="f_{$ref}" value="{string(.)}"/>&#160;</td>
					<td align="right"><button class="content" onclick="javascript:doFileUploadAction('{/root/gui/locService}/resources.upload','{$ref}',document.mainForm.f_{$ref}.value,'{$access}','{$id}')"><xsl:value-of select="/root/gui/strings/upload"/></button></td>
				</tr></table>
			</xsl:with-param>
			<xsl:with-param name="schema"/>
		</xsl:call-template>
	</xsl:template>
	
	<xsl:template mode="iso19115FileRemove" match="*">
		<xsl:param name="access" select="'public'"/>
		<xsl:param name="id"/>
	
		<xsl:call-template name="simpleElementGui">
			<xsl:with-param name="title" select="/root/gui/strings/file"/>
			<xsl:with-param name="text">
				<table width="100%"><tr>
					<xsl:variable name="ref" select="geonet:element/@ref"/>
					<td width="70%"><xsl:value-of select="string(.)"/></td>
					<td align="right"><button class="content" onclick="javascript:doFileRemoveAction('{/root/gui/locService}/resources.del','{$ref}','{$access}','{$id}')"><xsl:value-of select="/root/gui/strings/remove"/></button></td>
				</tr></table>
			</xsl:with-param>
			<xsl:with-param name="schema"/>
		</xsl:call-template>
	</xsl:template>
	
	<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
	<!-- iso19115 brief formatting -->
	<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
	
	<xsl:template name="iso19115Brief">
		<metadata>
			<xsl:variable name="download_check"><xsl:text>&amp;fname=&amp;access</xsl:text></xsl:variable>
			<xsl:variable name="id" select="geonet:info/id"/>
			<xsl:variable name="uuid" select="geonet:info/uuid"/>
			
			<xsl:if test="dataIdInfo/idCitation/resTitle">
				<title><xsl:value-of select="dataIdInfo/idCitation/resTitle"/></title>
			</xsl:if>
			<xsl:if test="dataIdInfo/idAbs">
				<abstract><xsl:value-of select="dataIdInfo/idAbs"/></abstract>
			</xsl:if>

			<xsl:for-each select="dataIdInfo/descKeys/keyword[text()]">
				<xsl:copy-of select="."/>
			</xsl:for-each>

			<xsl:for-each select="distInfo/distTranOps/onLineSrc">
				
				<xsl:comment>The links here are meant to replace the custom links as created in the next section</xsl:comment>
				
				<xsl:variable name="protocol" select="protocol"/>
				<xsl:variable name="linkage"  select="linkage"/>
				<xsl:variable name="name"     select="orName"/>
				<xsl:variable name="desc"     select="orDesc"/>
				
				<xsl:if test="string($linkage)!=''">
					
					<xsl:element name="link">
						<xsl:attribute name="title"><xsl:value-of select="$desc"/></xsl:attribute>
						<xsl:attribute name="href"><xsl:value-of select="$linkage"/></xsl:attribute>
						<xsl:attribute name="name"><xsl:value-of select="$name"/></xsl:attribute>
						<xsl:choose>
							<xsl:when test="starts-with($protocol,'WWW:LINK-')">
								<xsl:attribute name="type">text/html</xsl:attribute>
							</xsl:when>
							<xsl:when test="starts-with($protocol,'WWW:DOWNLOAD-') and contains($linkage,'.jpg')">
								<xsl:attribute name="type">image/jpeg</xsl:attribute>
							</xsl:when>
							<xsl:when test="starts-with($protocol,'WWW:DOWNLOAD-') and contains($linkage,'.png')">
								<xsl:attribute name="type">image/png</xsl:attribute>
							</xsl:when>
							<xsl:when test="starts-with($protocol,'WWW:DOWNLOAD-') and contains($linkage,'.gif')">
								<xsl:attribute name="type">image/gif</xsl:attribute>
							</xsl:when>
							<xsl:when test="starts-with($protocol,'WWW:DOWNLOAD-') and contains($linkage,'.doc')">
								<xsl:attribute name="type">application/word</xsl:attribute>
							</xsl:when>
							<xsl:when test="starts-with($protocol,'WWW:DOWNLOAD-') and contains($linkage,'.zip')">
								<xsl:attribute name="type">application/zip</xsl:attribute>
							</xsl:when>
							<xsl:when test="starts-with($protocol,'WWW:DOWNLOAD-') and contains($linkage,'.pdf')">
								<xsl:attribute name="type">application/pdf</xsl:attribute>
							</xsl:when>
							<xsl:when test="starts-with($protocol,'GLG:KML-') and contains($linkage,'.kml')">
								<xsl:attribute name="type">application/vnd.google-earth.kml+xml</xsl:attribute>
							</xsl:when>
							<xsl:when test="starts-with($protocol,'GLG:KML-') and contains($linkage,'.kmz')">
								<xsl:attribute name="type">application/vnd.google-earth.kmz</xsl:attribute>
							</xsl:when>
							<xsl:when test="starts-with($protocol,'OGC:WMS-')">
								<xsl:attribute name="type">application/vnd.ogc.wms_xml</xsl:attribute>
							</xsl:when>
							<xsl:when test="$protocol='ESRI:AIMS-'">
								<xsl:attribute name="type">application/vnd.esri.arcims_axl</xsl:attribute>
							</xsl:when>
							<xsl:when test="$protocol!=''">
								<xsl:attribute name="type"><xsl:value-of select="$protocol"/></xsl:attribute>
							</xsl:when>
							<xsl:otherwise>
								<!-- fall back to the default content type -->
								<xsl:attribute name="type">text/plain</xsl:attribute>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:element>
					
				</xsl:if>

				<!-- Generate a KML output link for a WMS service -->
				<xsl:if test="string($linkage)!='' and starts-with($protocol,'OGC:WMS-') and contains($protocol,'-get-map') and $name">
					
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
					<xsl:when test="starts-with(./protocol,'WWW:DOWNLOAD-') and contains(./protocol,'http--download') and string($linkage)!='' and not(contains($linkage,$download_check))"> <!-- FIXME -->
						<link type="download"><xsl:value-of select="$linkage"/></link>
					</xsl:when>
					<xsl:when test="starts-with(./protocol,'OGC:WMS-') and contains(./protocol,'-get-map') and string($linkage)!='' and string($name)!=''">
						<link type="wms">
							<xsl:value-of select="concat('javascript:addWMSServerLayers(&#34;',$linkage,'&#34;);')"/>
						</link>
						<link type="googleearth">
							<xsl:value-of select="concat(/root/gui/locService,'/google.kml?uuid=',$uuid,'&amp;layers=',$name)"/>
						</link>
					</xsl:when>
					<xsl:when test="starts-with(./protocol,'OGC:WMS-') and contains(./protocol,'-get-capabilities') and string($linkage)!=''">
						<link type="wms">
							<xsl:value-of select="concat('javascript:runIM_selectService(&#34;',$linkage,'&#34;,2,',$id,');')"/>
						</link>
					</xsl:when>
					<xsl:when test="$linkage[text()]">
						<link type="url"><xsl:value-of select="$linkage[text()]"/></link>
					</xsl:when>
				</xsl:choose>
			</xsl:for-each>

			<xsl:if test="dataIdInfo/geoBox">
				<geoBox>
					<westBL><xsl:value-of select="dataIdInfo/geoBox/westBL"/></westBL>
					<eastBL><xsl:value-of select="dataIdInfo/geoBox/eastBL"/></eastBL>
					<southBL><xsl:value-of select="dataIdInfo/geoBox/southBL"/></southBL>
					<northBL><xsl:value-of select="dataIdInfo/geoBox/northBL"/></northBL>
				</geoBox>
			</xsl:if>
		
			<xsl:if test="not(geonet:info/server)">
				<xsl:variable name="info" select="geonet:info"/>

				<xsl:for-each select="dataIdInfo/graphOver">
					<xsl:if test="bgFileName != ''">
						<xsl:choose>

							<!-- the thumbnail is an url -->
	
							<xsl:when test="contains(bgFileName ,'://')">
								<image type="unknown"><xsl:value-of select="bgFileName"/></image>								
							</xsl:when>

							<!-- small thumbnail -->
	
							<xsl:when test="string(bgFileDesc)='thumbnail'">
								<xsl:choose>
									<xsl:when test="$info/isHarvested = 'y'">
										[<xsl:if test="$info/harvestInfo/smallThumbnail">
											<image type="thumbnail">
												<xsl:value-of select="concat($info/harvestInfo/smallThumbnail, bgFileName)"/>
											</image>
										</xsl:if>]
									</xsl:when>
									
									<xsl:otherwise>
										<image type="thumbnail">
											<xsl:value-of select="concat(/root/gui/locService,'/resources.get?id=',$id,'&amp;fname=',bgFileName,'&amp;access=public')"/>
										</image>
									</xsl:otherwise>
								</xsl:choose>
							</xsl:when>
	
							<!-- large thumbnail -->
	
							<xsl:when test="string(bgFileDesc)='large_thumbnail'">
								<xsl:choose>
									<xsl:when test="$info/isHarvested = 'y'">
										<xsl:if test="$info/harvestInfo/largeThumbnail">
											<image type="overview">
												<xsl:value-of select="concat($info/harvestInfo/largeThumbnail, bgFileName)"/>
											</image>
										</xsl:if>
									</xsl:when>
									
									<xsl:otherwise>
										<image type="overview">
											<xsl:value-of select="concat(/root/gui/locService,'/graphover.show?id=',$id,'&amp;fname=',bgFileName,'&amp;access=public')"/>
										</image>
									</xsl:otherwise>
								</xsl:choose>
							</xsl:when>
	
						</xsl:choose>
					</xsl:if>
				</xsl:for-each>
			</xsl:if>

			<xsl:copy-of select="geonet:info"/>
		</metadata>
	</xsl:template>
				
	<!--
	iso19115 complete tab template
	-->
	<xsl:template name="iso19115CompleteTab">
		<xsl:param name="tabLink"/>
		
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
	        <item label="spatial2Tab">spatial2</item>
	        <item label="refSysTab">refSys</item>
	        <item label="distributionTab">distribution</item>
	        <item label="dataQualityTab">dataQuality</item>
	        <item label="appSchInfoTab">appSchInfo</item>
	        <item label="porCatInfoTab">porCatInfo</item>
	      </xsl:with-param>
	    </xsl:call-template>
	  </xsl:if>
		
	</xsl:template>
	
	<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
	<!-- utilities -->
	<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
	
	<xsl:template mode="iso19115IsEmpty" match="*|@*">
		<xsl:choose>
			<!-- normal element -->
			<xsl:when test="*">
				<xsl:apply-templates mode="iso19115IsEmpty"/>
			</xsl:when>
			<!-- text element -->
			<xsl:when test="text()!=''">txt</xsl:when>
			<!-- empty element -->
			<xsl:otherwise>
				<!-- codelist? -->
				<xsl:variable name="name" select="name(.)"/>
				<xsl:if test="substring($name, string-length($name)-1, 2)='Cd' or $name='languageCode'">
					<xsl:if test="@value!=''">cdl</xsl:if>
				</xsl:if>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

</xsl:stylesheet>

