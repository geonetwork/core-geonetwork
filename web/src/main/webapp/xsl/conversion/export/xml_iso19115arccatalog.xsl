<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <!-- <xsl:output method="xml" doctype-system="c:\temp\ESRI_ISO1.dtd"/> -->
  <xsl:template match="/root">
    <metadata>
      <xsl:apply-templates select="Metadata/mdFileID"/>
      <xsl:apply-templates select="Metadata/mdLang"/>
      <xsl:apply-templates select="Metadata/mdChar"/>
      <xsl:apply-templates select="Metadata/mdParentID"/>
      <xsl:for-each select="Metadata/mdHrLv">
        <xsl:apply-templates select="."/>
      </xsl:for-each>
      <xsl:for-each select="Metadata/mdHrLvName">
        <xsl:apply-templates select="."/>
      </xsl:for-each>
      <xsl:apply-templates select="Metadata/mdContact"/>
      <xsl:apply-templates select="Metadata/mdDateSt"/>
      <xsl:apply-templates select="Metadata/mdStanName"/>
      <xsl:apply-templates select="Metadata/mdStanVer"/>
      <xsl:apply-templates select="Metadata/distInfo"/>
      <xsl:for-each select="Metadata/dataIdInfo">
        <xsl:apply-templates select="."/>
      </xsl:for-each>
      <xsl:for-each select="Metadata/appSchInfo">
        <xsl:apply-templates select="."/>
      </xsl:for-each>
      <xsl:for-each select="Metadata/porCatInfo">
        <xsl:apply-templates select="."/>
      </xsl:for-each>
      <xsl:apply-templates select="Metadata/mdMaint"/>
      <xsl:for-each select="Metadata/mdConst">
        <xsl:apply-templates select="."/>
      </xsl:for-each>
      <xsl:for-each select="Metadata/dqInfo">
        <xsl:apply-templates select="."/>
      </xsl:for-each>
      <xsl:for-each select="Metadata/spatRepInfo">
        <xsl:apply-templates select="."/>
      </xsl:for-each>
      <xsl:for-each select="Metadata/refSysInfo">
        <xsl:apply-templates select="."/>
      </xsl:for-each>
      <xsl:for-each select="Metadata/contInfo">
        <xsl:apply-templates select="."/>
      </xsl:for-each>
    </metadata>
  </xsl:template>
  <!-- contInfo-->
  <xsl:template match="contInfo">
    <contInfo>
      <xsl:apply-templates select="ContInfo"/>
      <xsl:apply-templates select="CovDesc"/>
      <xsl:apply-templates select="FetCatDesc"/>
      <xsl:apply-templates select="ImgDesc"/>
    </contInfo>
  </xsl:template>
  <!-- ImgDesc-->
  <xsl:template match="ImgDesc">
    <ImgDesc>
      <xsl:apply-templates select="attDesc"/>
      <xsl:apply-templates select="contentTyp"/>
      <xsl:for-each select="covDim">
        <xsl:apply-templates select="."/>
      </xsl:for-each>
      <xsl:apply-templates select="illElevAng"/>
      <xsl:apply-templates select="illAziAng"/>
      <xsl:apply-templates select="imagCond"/>
      <xsl:apply-templates select="imagQuCode"/>
      <xsl:apply-templates select="cloudCovPer"/>
      <xsl:apply-templates select="prcTypCde"/>
      <xsl:apply-templates select="cmpGenQuan"/>
      <xsl:apply-templates select="trianInd"/>
      <xsl:apply-templates select="radCalDatAv"/>
      <xsl:apply-templates select="camCalInAv"/>
      <xsl:apply-templates select="filmDistInAv"/>
      <xsl:apply-templates select="lensDistInAv"/>
    </ImgDesc>
  </xsl:template>
  <!-- FetCatDesc-->
  <xsl:template match="FetCatDesc">
    <FetCatDesc>
      <xsl:apply-templates select="compCode"/>
      <xsl:for-each select="catLang">
        <xsl:apply-templates select="."/>
      </xsl:for-each>
      <xsl:apply-templates select="incWithDS"/>
      <xsl:for-each select="catFetTypes">
        <xsl:apply-templates select="."/>
      </xsl:for-each>
      <xsl:for-each select="catCitation">
        <xsl:apply-templates select="."/>
      </xsl:for-each>
    </FetCatDesc>
  </xsl:template>
  <!-- catFetTypes-->
  <xsl:template match="catFetTypes">
    <catFetTypes>
      <xsl:apply-templates select="TypeName"/>
      <xsl:apply-templates select="LocalName"/>
      <xsl:apply-templates select="ScopedName"/>
      <xsl:apply-templates select="MemberName"/>
    </catFetTypes>
  </xsl:template>
  <!-- CovDesc-->
  <xsl:template match="CovDesc">
    <CovDesc>
      <xsl:apply-templates select="attDesc"/>
      <xsl:apply-templates select="contentTyp"/>
      <xsl:for-each select="covDim">
        <xsl:apply-templates select="."/>
      </xsl:for-each>
    </CovDesc>
  </xsl:template>
  <!-- covDim-->
  <xsl:template match="covDim">
    <covDim>
      <xsl:apply-templates select="Band"/>
      <xsl:apply-templates select="RangeDim"/>
    </covDim>
  </xsl:template>
  <!-- RangeDim-->
  <xsl:template match="RangeDim">
    <RangeDim>
      <xsl:apply-templates select="seqID"/>
      <xsl:apply-templates select="dimDescrp"/>
    </RangeDim>
  </xsl:template>
  <!-- Band-->
  <xsl:template match="Band">
    <Band>
      <xsl:apply-templates select="seqID"/>
      <xsl:apply-templates select="dimDescrp"/>
      <xsl:apply-templates select="maxVal"/>
      <xsl:apply-templates select="minVal"/>
      <xsl:apply-templates select="valUnit"/>
      <xsl:apply-templates select="pkResp"/>
      <xsl:apply-templates select="bitsPerVal"/>
      <xsl:apply-templates select="toneGrad"/>
      <xsl:apply-templates select="sclFac"/>
      <xsl:apply-templates select="offset"/>
    </Band>
  </xsl:template>
  <!-- Template with scope aName attributeType-->
  <xsl:template match="seqID | MemberName">
    <xsl:variable name="name" select="local-name(.)"/>
    <xsl:element name="{$name}">
      <xsl:apply-templates select="scope"/>
      <xsl:apply-templates select="aName"/>
      <xsl:apply-templates select="attributeType"/>
    </xsl:element>
  </xsl:template>
  <!-- Template with scope aName-->
  <xsl:template match="attributeType | TypeName">
    <xsl:variable name="name" select="local-name(.)"/>
    <xsl:element name="{$name}">
      <xsl:apply-templates select="scope"/>
      <xsl:apply-templates select="aName"/>
    </xsl:element>
  </xsl:template>
  <!-- spatRepInfo-->
  <xsl:template match="spatRepInfo">
    <spatRepInfo>
      <xsl:apply-templates select="Georect"/>
      <xsl:apply-templates select="GridSpatRep"/>
      <xsl:apply-templates select="Georef"/>
      <xsl:apply-templates select="VectSpatRep"/>
    </spatRepInfo>
  </xsl:template>
  <!-- VectSpatRep-->
  <xsl:template match="VectSpatRep">
    <VectSpatRep>
      <xsl:apply-templates select="topLvl"/>
      <xsl:for-each select="geometObjs">
        <xsl:apply-templates select="."/>
      </xsl:for-each>
    </VectSpatRep>
  </xsl:template>
  <!-- geometObjs-->
  <xsl:template match="geometObjs">
    <geometObjs>
      <xsl:apply-templates select="geoObjTyp"/>
      <xsl:apply-templates select="geoObjCnt"/>
    </geometObjs>
  </xsl:template>
  <!-- Georef-->
  <xsl:template match="Georef">
    <Georef>
      <xsl:apply-templates select="numDims"/>
      <xsl:apply-templates select="axDimProps"/>
      <xsl:apply-templates select="cellGeo"/>
      <xsl:apply-templates select="tranParaAv"/>
      <xsl:apply-templates select="ctrlPtAv"/>
      <xsl:apply-templates select="orieParaAv"/>
      <xsl:apply-templates select="orieParaDesc"/>
      <xsl:apply-templates select="georefPars"/>
      <xsl:for-each select="paraCit">
        <xsl:apply-templates select="."/>
      </xsl:for-each>
    </Georef>
  </xsl:template>
  <!-- GridSpatRep-->
  <xsl:template match="GridSpatRep">
    <GridSpatRep>
      <xsl:apply-templates select="numDims"/>
      <xsl:apply-templates select="axDimProps"/>
      <xsl:apply-templates select="cellGeo"/>
      <xsl:apply-templates select="tranParaAv"/>
    </GridSpatRep>
  </xsl:template>
  <!-- Dimen-->
  <xsl:template match="Dimen">
    <Dimen>
      <xsl:apply-templates select="dimName"/>
      <xsl:apply-templates select="dimSize"/>
      <xsl:apply-templates select="dimResol"/>
    </Dimen>
  </xsl:template>
  <!-- Georect-->
  <xsl:template match="Georect">
    <Georect>
      <xsl:apply-templates select="numDims"/>
      <xsl:apply-templates select="axDimProps"/>
      <xsl:apply-templates select="cellGeo"/>
      <xsl:apply-templates select="tranParaAv"/>
      <xsl:apply-templates select="chkPtAv"/>
      <xsl:apply-templates select="chkPtDesc"/>
      <xsl:apply-templates select="cornerPts"/>
      <xsl:apply-templates select="centerPt"/>
      <xsl:apply-templates select="ptInPixel"/>
      <xsl:apply-templates select="transDimDesc"/>
      <xsl:for-each select="transDimMap">
        <xsl:apply-templates select="."/>
      </xsl:for-each>
    </Georect>
  </xsl:template>
  <!-- dqInfo-->
  <xsl:template match="dqInfo">
    <dqInfo>
      <xsl:apply-templates select="dqScope"/>
      <xsl:apply-templates select="dataLineage"/>
      <xsl:for-each select="dqReport">
        <xsl:apply-templates select="."/>
      </xsl:for-each>
    </dqInfo>
  </xsl:template>
  <!-- dqReport-->
  <xsl:template match="dqReport">
    <dqReport>
      <xsl:apply-templates select="DQRelIntPosAcc"/>
      <xsl:apply-templates select="DQQuanAttAcc"/>
      <xsl:apply-templates select="DQDomConsis"/>
      <xsl:apply-templates select="DQGridDataPosAcc"/>
      <xsl:apply-templates select="DQTempValid"/>
      <xsl:apply-templates select="DQAbsExtPosAcc"/>
      <xsl:apply-templates select="DQConcConsis"/>
      <xsl:apply-templates select="DQCompComm"/>
      <xsl:apply-templates select="DQFormConsis"/>
      <xsl:apply-templates select="DQTopConsis"/>
      <xsl:apply-templates select="DQAccTimeMeas"/>
      <xsl:apply-templates select="DQNonQuanAttAcc"/>
      <xsl:apply-templates select="DQThemClassCor"/>
      <xsl:apply-templates select="DQCompOm"/>
      <xsl:apply-templates select="DQTempConsis"/>
    </dqReport>
  </xsl:template>
  <!-- Template with measName measId measureDescription evalMethType evalMethDesc evaluationProcedure measDateTm measResult-->
  <xsl:template
    match="DQRelIntPosAcct | DQQuanAttAcc | DQDomConsis | DQGridDataPosAcc | DQTempValid | DQAbsExtPosAcc | DQConcConsis | DQCompComm | DQFormConsis | DQTopConsis | DQAccTimeMeas | DQNonQuanAttAcc | DQThemClassCor | DQCompOm | DQTempConsis">
    <xsl:variable name="name" select="local-name(.)"/>
    <xsl:element name="{$name}">
      <xsl:for-each select="measName">
        <xsl:apply-templates select="."/>
      </xsl:for-each>
      <xsl:apply-templates select="measId"/>
      <xsl:apply-templates select="measureDescription"/>
      <xsl:apply-templates select="evalMethType"/>
      <xsl:apply-templates select="evalMethDesc"/>
      <xsl:apply-templates select="evaluationProcedure"/>
      <xsl:apply-templates select="measDateTm"/>
      <xsl:for-each select="measResult">
        <xsl:apply-templates select="."/>
      </xsl:for-each>
    </xsl:element>
  </xsl:template>
  <!-- measResult-->
  <xsl:template match="measResult">
    <measResult>
      <xsl:apply-templates select="ConResult"/>
      <xsl:apply-templates select="QuanResult"/>
      <xsl:apply-templates select="Result"/>
    </measResult>
  </xsl:template>
  <!-- QuanResult-->
  <xsl:template match="QuanResult">
    <QuanResult>
      <xsl:apply-templates select="quanValType"/>
      <xsl:apply-templates select="quanValUnit"/>
      <xsl:apply-templates select="errStat"/>
      <xsl:for-each select="quanValue">
        <xsl:apply-templates select="."/>
      </xsl:for-each>
    </QuanResult>
  </xsl:template>
  <!-- ConResult-->
  <xsl:template match="ConResult">
    <ConResult>
      <xsl:apply-templates select="conSpec"/>
      <xsl:apply-templates select="conExpl"/>
      <xsl:apply-templates select="conPass"/>
    </ConResult>
  </xsl:template>
  <!-- Template with RS_Identifier MdIdent-->
  <xsl:template match="measId | imagQuCode | prcTypCde">
    <xsl:variable name="name" select="local-name(.)"/>
    <xsl:element name="{$name}">
      <xsl:apply-templates select="RS_Identifier"/>
      <xsl:apply-templates select="MdIdent"/>
    </xsl:element>
  </xsl:template>
  <!-- dqScope-->
  <xsl:template match="dqScope">
    <dqScope>
      <xsl:for-each select="scpLvl">
        <xsl:apply-templates select="."/>
      </xsl:for-each>
      <xsl:apply-templates select="scpExt"/>
      <xsl:for-each select="scpLvlDesc">
        <xsl:apply-templates select="."/>
      </xsl:for-each>
    </dqScope>
  </xsl:template>
  <!-- dataLineage-->
  <xsl:template match="dataLineage">
    <dataLineage>
      <xsl:apply-templates select="statement"/>
      <xsl:for-each select="dataSource">
        <xsl:apply-templates select="."/>
      </xsl:for-each>
      <xsl:for-each select="prcStep">
        <xsl:apply-templates select="."/>
      </xsl:for-each>
    </dataLineage>
  </xsl:template>
  <!-- Template with srcDesc srcScale srcRefSys srcCitatn srcExt srcStep-->
  <xsl:template match="dataSource | stepSrc">
    <xsl:variable name="name" select="local-name(.)"/>
    <xsl:element name="{$name}">
      <xsl:apply-templates select="srcDesc"/>
      <xsl:apply-templates select="srcScale"/>
      <xsl:apply-templates select="srcRefSys"/>
      <xsl:apply-templates select="srcCitatn"/>
      <xsl:for-each select="srcExt">
        <xsl:apply-templates select="."/>
      </xsl:for-each>
      <xsl:for-each select="srcStep">
        <xsl:apply-templates select="."/>
      </xsl:for-each>
    </xsl:element>
  </xsl:template>
  <!-- Template with RefSystem MdCoRefSys -->
  <xsl:template match="srcRefSys | refSysInfo">
    <xsl:variable name="name" select="local-name(.)"/>
    <xsl:element name="{$name}">
      <xsl:apply-templates select="RefSystem"/>
      <xsl:apply-templates select="MdCoRefSys"/>
    </xsl:element>
  </xsl:template>
  <!-- Template with stepDesc stepRat stepDateTm stepProc stepSrc-->
  <xsl:template match="srcStep | prcStep">
    <xsl:variable name="name" select="local-name(.)"/>
    <xsl:element name="{$name}">
      <xsl:apply-templates select="stepDesc"/>
      <xsl:apply-templates select="stepRat"/>
      <xsl:apply-templates select="stepDateTm"/>
      <xsl:for-each select="stepProc">
        <xsl:apply-templates select="."/>
      </xsl:for-each>
      <xsl:for-each select="stepSrc">
        <xsl:apply-templates select="."/>
      </xsl:for-each>
    </xsl:element>
  </xsl:template>
  <!-- appSchInfo-->
  <xsl:template match="appSchInfo">
    <appSchInfo>
      <xsl:apply-templates select="asName"/>
      <xsl:apply-templates select="asSchLang"/>
      <xsl:apply-templates select="asCstLang"/>
      <xsl:apply-templates select="asAscii"/>
      <xsl:apply-templates select="asGraFile"/>
      <xsl:apply-templates select="asSwDevFile"/>
      <xsl:apply-templates select="asSwDevFiFt"/>
      <xsl:apply-templates select="fetCatSup"/>
    </appSchInfo>
  </xsl:template>
  <!-- fetCatSup-->
  <xsl:template match="fetCatSup">
    <fetCatSup>
      <xsl:for-each select="featTypeList">
        <featTypeList>
          <xsl:apply-templates select="spatObj"/>
          <xsl:apply-templates select="spatSchName"/>
        </featTypeList>
      </xsl:for-each>
    </fetCatSup>
  </xsl:template>
  <!-- rpCntInfo-->
  <xsl:template match="rpCntInfo">
    <rpCntInfo>
      <xsl:apply-templates select="cntPhone"/>
      <xsl:apply-templates select="cntAddress"/>
      <xsl:apply-templates select="cntOnLineRes"/>
      <xsl:apply-templates select="cntHours"/>
      <xsl:apply-templates select="cntInstr"/>
    </rpCntInfo>
  </xsl:template>
  <!-- cntPhone-->
  <xsl:template match="cntPhone">
    <cntPhone>
      <xsl:for-each select="voiceNum">
        <xsl:apply-templates select="."/>
      </xsl:for-each>
      <xsl:for-each select="faxNum">
        <xsl:apply-templates select="."/>
      </xsl:for-each>
    </cntPhone>
  </xsl:template>
  <!-- cntAddress-->
  <xsl:template match="cntAddress">
    <cntAddress>
      <xsl:for-each select="delPoint">
        <xsl:apply-templates select="."/>
      </xsl:for-each>
      <xsl:apply-templates select="city"/>
      <xsl:apply-templates select="adminArea"/>
      <xsl:apply-templates select="postCode"/>
      <xsl:apply-templates select="country"/>
      <xsl:for-each select="eMailAdd">
        <xsl:apply-templates select="."/>
      </xsl:for-each>
    </cntAddress>
  </xsl:template>
  <!-- cntOnLineRes-->
  <xsl:template match="cntOnLineRes">
    <cntOnLineRes>
      <xsl:apply-templates select="linkage"/>
      <xsl:apply-templates select="protocol"/>
      <xsl:apply-templates select="appProfile"/>
      <xsl:apply-templates select="orName"/>
      <xsl:apply-templates select="orDesc"/>
      <xsl:apply-templates select="orFunct"/>
    </cntOnLineRes>
  </xsl:template>
  <!-- distInfo-->
  <xsl:template match="distInfo">
    <distInfo>
      <distributor>
        <xsl:for-each select="distributor">
          <xsl:apply-templates select="distorCont"/>
          <xsl:for-each select="distorFormat">
            <xsl:apply-templates select="."/>
          </xsl:for-each>
          <xsl:for-each select="distorOrdPrc">
            <distorOrdPrc>
              <xsl:apply-templates select="resFees"/>
              <xsl:apply-templates select="planAvDtTm"/>
              <xsl:apply-templates select="ordInstr"/>
              <xsl:apply-templates select="ordTurn"/>
            </distorOrdPrc>
          </xsl:for-each>
          <!--          <xsl:for-each select="distorTran">
                                  <xsl:apply-templates select="."/>
                              </xsl:for-each> -->
        </xsl:for-each>
        <xsl:for-each select="distTranOps">
          <xsl:apply-templates select="."/>
        </xsl:for-each>
      </distributor>
    </distInfo>
  </xsl:template>
  <!-- onLineMed - because of an error in ArcCatalog, we will match on onLineMed but output offLineMed -->
  <xsl:template match="onLineMed">
    <offLineMed>
      <xsl:apply-templates select="medName"/>
      <xsl:for-each select="medDensity">
        <xsl:apply-templates select="."/>
      </xsl:for-each>
      <xsl:apply-templates select="medDenUnits"/>
      <xsl:apply-templates select="medVol"/>
      <xsl:for-each select="medFormat">
        <xsl:apply-templates select="."/>
      </xsl:for-each>
      <xsl:apply-templates select="medNote"/>
    </offLineMed>
  </xsl:template>
  <!-- dataIdInfo-->
  <xsl:template match="dataIdInfo">
    <dataIdInfo>
      <xsl:apply-templates select="idCitation"/>
      <xsl:apply-templates select="idAbs"/>
      <xsl:apply-templates select="idPurp"/>
      <xsl:for-each select="idCredit">
        <xsl:apply-templates select="."/>
      </xsl:for-each>
      <xsl:for-each select="status">
        <xsl:apply-templates select="."/>
      </xsl:for-each>
      <xsl:for-each select="idPoC">
        <xsl:apply-templates select="."/>
      </xsl:for-each>
      <xsl:for-each select="resConst">
        <xsl:apply-templates select="."/>
      </xsl:for-each>
      <xsl:for-each select="dsFormat">
        <xsl:apply-templates select="."/>
      </xsl:for-each>
      <xsl:for-each select="idSpecUse">
        <idSpecUse>
          <xsl:apply-templates select="specUsage"/>
          <xsl:apply-templates select="usageDate"/>
          <xsl:apply-templates select="usrDetLim"/>
          <xsl:for-each select="usrCntInfo">
            <xsl:apply-templates select="."/>
          </xsl:for-each>
        </idSpecUse>
      </xsl:for-each>
      <xsl:for-each select="resMaint">
        <xsl:apply-templates select="."/>
      </xsl:for-each>
      <xsl:for-each select="descKeys">
        <descKeys>
          <xsl:attribute name="KeyTypCd">
            <xsl:if test="keyTyp/KeyTypCd/@value = 'discipline'">
              <xsl:text>001</xsl:text>
            </xsl:if>
            <xsl:if test="keyTyp/KeyTypCd/@value = 'place'">
              <xsl:text>002</xsl:text>
            </xsl:if>
            <xsl:if test="keyTyp/KeyTypCd/@value = 'stratum'">
              <xsl:text>003</xsl:text>
            </xsl:if>
            <xsl:if test="keyTyp/KeyTypCd/@value = 'temporal'">
              <xsl:text>004</xsl:text>
            </xsl:if>
            <xsl:if test="keyTyp/KeyTypCd/@value = 'theme'">
              <xsl:text>005</xsl:text>
            </xsl:if>
          </xsl:attribute>
          <xsl:for-each select="keyword">
            <xsl:apply-templates select="."/>
          </xsl:for-each>
          <xsl:apply-templates select="keyTyp"/>
          <xsl:apply-templates select="thesaName"/>
        </descKeys>
      </xsl:for-each>
      <xsl:for-each select="graphOver">
        <graphOver>
          <xsl:apply-templates select="bgFileName"/>
          <xsl:apply-templates select="bgFileDesc"/>
          <xsl:apply-templates select="bgFileType"/>
        </graphOver>
      </xsl:for-each>
      <xsl:for-each select="spatRpType">
        <xsl:apply-templates select="."/>
      </xsl:for-each>
      <xsl:for-each select="dataScale">
        <dataScale>
          <xsl:apply-templates select="equScale"/>
          <xsl:apply-templates select="scaleDist"/>
        </dataScale>
      </xsl:for-each>
      <xsl:for-each select="dataLang">
        <xsl:apply-templates select="."/>
      </xsl:for-each>
      <xsl:apply-templates select="dataChar"/>
      <xsl:for-each select="tpCat">
        <xsl:apply-templates select="."/>
      </xsl:for-each>
      <xsl:for-each select="geoBox">
        <geoBox esriExtentType="decdegrees"> <!-- ArcCatalog specific -->
          <xsl:apply-templates select="westBL"/>
          <xsl:apply-templates select="eastBL"/>
          <xsl:apply-templates select="southBL"/>
          <xsl:apply-templates select="northBL"/>
          <exTypeCode>1</exTypeCode> <!-- ArcCatalog specific -->
        </geoBox>
      </xsl:for-each>
      <xsl:for-each select="geoDesc">
        <geoDesc>
          <xsl:apply-templates select="exTypeCode"/>
          <xsl:apply-templates select="geoId"/>
        </geoDesc>
      </xsl:for-each>
      <xsl:apply-templates select="envirDesc"/>
      <xsl:for-each select="dataExt">
        <xsl:apply-templates select="."/>
      </xsl:for-each>
      <xsl:apply-templates select="suppInfo"/>
    </dataIdInfo>
  </xsl:template>
  <!-- SpatTempEx-->
  <xsl:template match="SpatTempEx">
    <SpatTempEx>
      <xsl:apply-templates select="exTemp"/>
      <xsl:for-each select="exSpat">
        <xsl:apply-templates select="."/>
      </xsl:for-each>
    </SpatTempEx>
  </xsl:template>
  <!-- BoundPoly-->
  <xsl:template match="BoundPoly">
    <BoundPoly>
      <xsl:apply-templates select="exTypeCode"/>
      <xsl:for-each select="polygon">
        <xsl:apply-templates select="."/>
      </xsl:for-each>
    </BoundPoly>
  </xsl:template>
  <!-- Template with MdCoRefSys coordinates -->
  <xsl:template match="GM_Polygon | cornerPts | centerPt">
    <xsl:variable name="name" select="local-name(.)"/>
    <xsl:element name="{$name}">
      <xsl:apply-templates select="MdCoRefSys"/>
      <xsl:apply-templates select="coordinates"/>
    </xsl:element>
  </xsl:template>
  <!-- MdCoRefSys-->
  <xsl:template match="MdCoRefSys">
    <MdCoRefSys>
      <xsl:apply-templates select="refSysID"/>
      <xsl:apply-templates select="projection"/>
      <xsl:apply-templates select="ellipsoid"/>
      <xsl:apply-templates select="datum"/>
      <xsl:apply-templates select="projParas"/>
      <xsl:apply-templates select="ellParas"/>
    </MdCoRefSys>
  </xsl:template>
  <!-- ellParas-->
  <xsl:template match="ellParas">
    <ellParas>
      <xsl:apply-templates select="semiMajAx"/>
      <xsl:apply-templates select="axisUnits"/>
      <xsl:apply-templates select="denFlatRat"/>
    </ellParas>
  </xsl:template>
  <!-- projParas-->
  <xsl:template match="projParas">
    <projParas>
      <xsl:apply-templates select="zone"/>
      <xsl:for-each select="stanPara">
        <xsl:apply-templates select="."/>
      </xsl:for-each>
      <xsl:apply-templates select="longCntMer"/>
      <xsl:apply-templates select="latProjOri"/>
      <xsl:apply-templates select="falEastng"/>
      <xsl:apply-templates select="falNorthng"/>
      <xsl:apply-templates select="falENUnits"/>
      <xsl:apply-templates select="sclFacEqu"/>
      <xsl:apply-templates select="hgtProsPt"/>
      <xsl:apply-templates select="longProjCnt"/>
      <xsl:apply-templates select="latProjCnt"/>
      <xsl:apply-templates select="sclFacCnt"/>
      <xsl:apply-templates select="stVrLongPl"/>
      <xsl:apply-templates select="sclFacPrOr"/>
      <xsl:apply-templates select="obLnAziPars"/>
      <xsl:for-each select="obLnPtPars">
        <obLnPtPars>
          <xsl:apply-templates select="obLineLat"/>
          <xsl:apply-templates select="obLineLong"/>
        </obLnPtPars>
      </xsl:for-each>
    </projParas>
  </xsl:template>
  <!-- obLnAziPars-->
  <xsl:template match="obLnAziPars">
    <obLnAziPars>
      <xsl:apply-templates select="aziAngle"/>
      <xsl:apply-templates select="aziPtLong"/>
    </obLnAziPars>
  </xsl:template>
  <!-- TM_GeometricPrimitive-->
  <xsl:template match="TM_GeometricPrimitive">
    <TM_GeometricPrimitive>
      <xsl:apply-templates select="TM_Instant"/>
      <xsl:apply-templates select="TM_Period"/>
    </TM_GeometricPrimitive>
  </xsl:template>
  <!-- TM_Period-->
  <xsl:template match="TM_Period">
    <TM_Period>
      <xsl:apply-templates select="begin"/>
      <xsl:apply-templates select="end"/>
    </TM_Period>
  </xsl:template>
  <!-- tmPosition-->
  <xsl:template match="tmPosition">
    <tmPosition>
      <xsl:apply-templates select="TM_DateAndTime"/>
      <xsl:apply-templates select="TM_CalDate"/>
      <xsl:apply-templates select="TM_ClockTime"/>
    </tmPosition>
  </xsl:template>
  <!-- TM_DateAndTime-->
  <xsl:template match="TM_DateAndTime">
    <TM_DateAndTime>
      <xsl:apply-templates select="calDate"/>
      <xsl:apply-templates select="clkTime"/>
    </TM_DateAndTime>
  </xsl:template>
  <!-- usrDefFreq-->
  <xsl:template match="usrDefFreq">
    <usrDefFreq>
      <xsl:apply-templates select="designator"/>
      <xsl:apply-templates select="years"/>
      <xsl:apply-templates select="months"/>
      <xsl:apply-templates select="days"/>
      <xsl:apply-templates select="timeIndicator"/>
      <xsl:apply-templates select="hours"/>
      <xsl:apply-templates select="minutes"/>
      <xsl:apply-templates select="seconds"/>
    </usrDefFreq>
  </xsl:template>
  <!-- Template with value uom-->
  <xsl:template match="scaleDist | quanValUnit | dimResol">
    <xsl:variable name="name" select="local-name(.)"/>
    <xsl:element name="{$name}">
      <xsl:apply-templates select="value"/>
      <xsl:apply-templates select="uom"/>
    </xsl:element>
  </xsl:template>
  <!-- value-->
  <xsl:template match="value">
    <value>
      <xsl:apply-templates select="Integer"/>
      <xsl:apply-templates select="Decimal"/>
      <xsl:apply-templates select="Real"/>
    </value>
  </xsl:template>
  <!-- uom-->
  <xsl:template match="uom">
    <uom>
      <xsl:apply-templates select="UomArea"/>
      <xsl:apply-templates select="UomTime"/>
      <xsl:apply-templates select="UomLength"/>
      <xsl:apply-templates select="UomVolume"/>
      <xsl:apply-templates select="UomVelocity"/>
      <xsl:apply-templates select="UomAngle"/>
      <xsl:apply-templates select="UomScale"/>
    </uom>
  </xsl:template>
  <!-- SecConsts-->
  <xsl:template match="SecConsts">
    <SecConsts>
      <xsl:for-each select="useLimit">
        <xsl:apply-templates select="."/>
      </xsl:for-each>
      <xsl:apply-templates select="class"/>
      <xsl:apply-templates select="userNote"/>
      <xsl:apply-templates select="classSys"/>
      <xsl:apply-templates select="handDesc"/>
    </SecConsts>
  </xsl:template>
  <!-- LegConsts-->
  <xsl:template match="LegConsts">
    <LegConsts>
      <xsl:for-each select="useLimit">
        <xsl:apply-templates select="."/>
      </xsl:for-each>
      <xsl:for-each select="accessConsts">
        <xsl:apply-templates select="."/>
      </xsl:for-each>
      <xsl:for-each select="useConsts">
        <xsl:apply-templates select="."/>
      </xsl:for-each>
      <xsl:for-each select="othConsts">
        <xsl:apply-templates select="."/>
      </xsl:for-each>
    </LegConsts>
  </xsl:template>
  <!-- Template with "xsl:value-of" -->
  <xsl:template
    match="mdFileID | rpIndName | spatObj | spatSchName | asSchLang | asCstLang | asAscii | asGraFile | asSwDevFile | asSwDevFiFt | mdParentID | rpOrgName | rpPosName | city | adminArea | postCode | country | linkage | protocol | appProfile | orName | cntHours | cntInstr | mdDateSt | mdStanName | mdStanVer | formatName | formatVer | formatAmdNum | formatSpec | fileDecmTech | resFees | planAvDtTm | ordInstr | handDesc | userNote | classSys | uomName | Real | Decimal | Integer | bgFileType | bgFileDesc | bgFileName | issn | isbn | collTitle | otherCitDet | artPage | issId | seriesName | resEdDate | resEd | resAltTitle | seconds | minutes | hours | timeIndicator | days | months | years | designator | other | datasetSet | attribIntSet | featIntSet | featSet | attribSet | usrDetLim | usageDate | specUsage | identCode | exTypeCode | envirDesc | suppInfo | exDesc | vertMaxVal | vertMinVal | end | begin | clkTime | calDate | coordinates | zone | longCntMer | latProjOri | falEastng | falNorthng | sclFacEqu | hgtProsPt | longProjCnt | latProjCnt | sclFacCnt | stVrLongPl | sclFacPrOr | aziPtLong | aziAngle | obLineLong | obLineLat | semiMajAx | denFlatRat | medNote | medVol | medDenUnits | transSize | unitsODist | ordTurn | resTitle | idAbs | idPurp | languageCode | rfDenom | mdHrLvName | dateNext | maintNote | voiceNum | faxNum | delPoint | eMailAdd | citId | citIdType | othConsts | useLimit | stanPara | idCredit | medDensity | keyword | statement | srcDesc | stepDesc | stepRat | stepDateTm | measName | measureDescription | evalMethDesc | measDateTm | conExpl | conPass | quanValType | errStat | quanValue | Result | numDims | tranParaAv | chkPtAv | chkPtDesc | transDimDesc | transDimMap | dimSize | ctrlPtAv | orieParaAv | orieParaDesc | georefPars | TopLvlCd | geoObjCnt | ContInfo | attDesc | scope | aName | dimDescrp | maxVal | minVal | pkResp | bitsPerVal | toneGrad | sclFac | offset | compCode | incWithDS | illElevAng | illAziAng | cloudCovPer | cmpGenQuan | trianInd | radCalDatAv | camCalInAv | filmDistInAv | lensDistInAv | westBL | eastBL | southBL | northBL">
    <xsl:variable name="name" select="local-name(.)"/>
    <xsl:element name="{$name}">
      <xsl:if test="@value">
        <xsl:attribute name="value">
          <xsl:apply-templates select="@value"/>
        </xsl:attribute>
      </xsl:if>
      <xsl:value-of select="."/>
    </xsl:element>
  </xsl:template>
  <xsl:template match="conversionToISOstandarUnit">
    <conversionToISOstandardUnit>
      <xsl:value-of select="."/>
    </conversionToISOstandardUnit>
  </xsl:template>
  <xsl:template match="refDate">
    <xsl:variable name="name" select="local-name(.)"/>
    <xsl:element name="{$name}">
      <xsl:value-of select="translate(.,'-','')"/>
    </xsl:element>
  </xsl:template>
  <xsl:template match="conversionToISOstandarUnit">
    <conversionToISOstandardUnit>
      <xsl:value-of select="."/>
    </conversionToISOstandardUnit>
  </xsl:template>
  <!-- Template with only 1 child node -->
  <xsl:template
    match="mdLang | mdChar | orFunct | role | medName | TempExtent | exTemp | TM_Instant | TM_CalDate | TM_ClockTime | vertDatum | keyTyp | equScale | dataChar | class | maintFreq | presForm | accessConsts | useConsts | medFormat | status | spatRpType | dataLang | tpCat | polygon | scpLvl | srcScale | RefSystem | evalMethType | cellGeo | ptInPixel | dimName | topLvl | geoObjTyp | contentTyp | catLang | LocalName | ScopedName | imagCond | refDateType">
    <xsl:variable name="name" select="local-name(.)"/>
    <xsl:element name="{$name}">
      <xsl:apply-templates select="node()"/>
    </xsl:element>
  </xsl:template>
  <!-- Template with only 1 child node with "xsl:for-each"-->
  <xsl:template match="porCatInfo | mdHrLv | Consts | axDimProps">
    <xsl:variable name="name" select="local-name(.)"/>
    <xsl:element name="{$name}">
      <xsl:for-each select="node()">
        <xsl:apply-templates select="."/>
      </xsl:for-each>
    </xsl:element>
  </xsl:template>
  <!-- Template with SecConsts LegConsts Consts-->
  <xsl:template match="mdConst | resConst">
    <xsl:variable name="name" select="local-name(.)"/>
    <xsl:element name="{$name}">
      <xsl:apply-templates select="SecConsts"/>
      <xsl:apply-templates select="LegConsts"/>
      <xsl:apply-templates select="Consts"/>
    </xsl:element>
  </xsl:template>
  <!-- Template with uomName conversionToISOstandarUnit-->
  <xsl:template
    match="vertUoM | axisUnits | falENUnits | UomArea | UomTime | UomLength | UomVolume | UomVelocity | UomAngle | UomScale | valUnit">
    <xsl:variable name="name" select="local-name(.)"/>
    <xsl:element name="{$name}">
      <xsl:apply-templates select="uomName"/>
      <xsl:apply-templates select="conversionToISOstandarUnit"/>
    </xsl:element>
  </xsl:template>
  <!-- Template with rpIndName rpOrgName rpPosName rpCntInfo role-->
  <xsl:template match="citRespParty | mdContact | distorCont | idPoC | usrCntInfo | stepProc">
    <xsl:variable name="name" select="local-name(.)"/>
    <xsl:element name="{$name}">
      <xsl:apply-templates select="rpIndName"/>
      <xsl:apply-templates select="rpOrgName"/>
      <xsl:apply-templates select="rpPosName"/>
      <xsl:apply-templates select="rpCntInfo"/>
      <xsl:apply-templates select="role"/>
    </xsl:element>
  </xsl:template>
  <!-- Template with attribSet featSet featIntSet attribIntSet datasetSet other-->
  <xsl:template match="upScpDesc | scpLvlDesc">
    <xsl:variable name="name" select="local-name(.)"/>
    <xsl:element name="{$name}">
      <xsl:apply-templates select="attribSet"/>
      <xsl:apply-templates select="featSet"/>
      <xsl:apply-templates select="featIntSet"/>
      <xsl:apply-templates select="attribIntSet"/>
      <xsl:apply-templates select="datasetSet"/>
      <xsl:apply-templates select="other"/>
    </xsl:element>
  </xsl:template>
  <!-- Template with maintFreq dateNext usrDefFreq maintScp upScpDesc maintNote-->
  <xsl:template match="mdMaint | resMaint">
    <xsl:variable name="name" select="local-name(.)"/>
    <xsl:element name="{$name}">
      <xsl:apply-templates select="maintFreq"/>
      <xsl:apply-templates select="dateNext"/>
      <xsl:apply-templates select="usrDefFreq"/>
      <xsl:for-each select="maintScp">
        <xsl:apply-templates select="."/>
      </xsl:for-each>
      <xsl:for-each select="upScpDesc">
        <xsl:apply-templates select="."/>
      </xsl:for-each>
      <xsl:for-each select="maintNote">
        <xsl:apply-templates select="."/>
      </xsl:for-each>
    </xsl:element>
  </xsl:template>
  <!-- Template with resTitle resAltTitle resRefDate resEd resEdDate citId citIdType citRespParty presForm otherCitDet collTitle isbn issn-->
  <xsl:template
    match="portCatCit | asName | identAuth | thesaName | idCitation | srcCitatn | evaluationProcedure | conSpec | paraCit | catCitation">
    <xsl:variable name="name" select="local-name(.)"/>
    <xsl:element name="{$name}">
      <xsl:apply-templates select="resTitle"/>
      <xsl:for-each select="resAltTitle">
        <xsl:apply-templates select="."/>
      </xsl:for-each>
      <xsl:for-each select="resRefDate">
        <resRefDate>
          <xsl:apply-templates select="refDate"/>
          <xsl:apply-templates select="refDateType"/>
        </resRefDate>
      </xsl:for-each>
      <xsl:apply-templates select="resEd"/>
      <xsl:apply-templates select="resEdDate"/>
      <xsl:for-each select="citId">
        <xsl:apply-templates select="."/>
      </xsl:for-each>
      <xsl:for-each select="citIdType">
        <xsl:apply-templates select="."/>
      </xsl:for-each>
      <xsl:for-each select="citRespParty">
        <xsl:apply-templates select="."/>
      </xsl:for-each>
      <xsl:for-each select="presForm">
        <xsl:apply-templates select="."/>
      </xsl:for-each>
      <xsl:for-each select="datasetSeries">
        <datasetSeries>
          <xsl:apply-templates select="seriesName"/>
          <xsl:apply-templates select="issId"/>
          <xsl:apply-templates select="artPage"/>
        </datasetSeries>
      </xsl:for-each>
      <xsl:apply-templates select="otherCitDet"/>
      <xsl:apply-templates select="collTitle"/>
      <xsl:apply-templates select="isbn"/>
      <xsl:apply-templates select="issn"/>
    </xsl:element>
  </xsl:template>


  <!-- Template with unitsODist transSize onLineSrc onLineMed-->
  <xsl:template match="distorTran | distTranOps">
    <!-- <xsl:variable name="name" select="local-name(.)"/> -->
    <!--    <distributor>
                <distorFormat>
                    <formatName></formatName>
                    <formatVer></formatVer>
                </distorFormat> -->
    <distorTran>
      <xsl:apply-templates select="onLineSrc"/>
      <xsl:apply-templates select="unitsODist"/>
      <xsl:apply-templates select="transSize"/>
      <xsl:apply-templates select="onLineMed"/>
    </distorTran>
    <!--    </distributor> -->
  </xsl:template>


  <!-- onLineSrc -->
  <xsl:template match="onLineSrc">
    <onLineSrc>
      <xsl:apply-templates select="linkage"/>
      <xsl:apply-templates select="protocol"/>
      <xsl:apply-templates select="appProfile"/>
      <xsl:apply-templates select="orName"/>
      <xsl:apply-templates select="orDesc"/>
      <xsl:apply-templates select="orFunct"/>
    </onLineSrc>
  </xsl:template>
  <!-- Template with identAuth identCode -->
  <xsl:template
    match="refSysID | projection | ellipsoid | datum | datumID | geoId | RS_Identifier | MdIdent">
    <xsl:variable name="name" select="local-name(.)"/>
    <xsl:element name="{$name}">
      <xsl:apply-templates select="identAuth"/>
      <xsl:apply-templates select="identCode"/>
    </xsl:element>
  </xsl:template>
  <!-- Template with BoundPoly GeoDesc-->
  <xsl:template match="geoEle | exSpat">
    <xsl:variable name="name" select="local-name(.)"/>
    <xsl:element name="{$name}">
      <xsl:apply-templates select="BoundPoly"/>
      <xsl:apply-templates select="GeoDesc"/>
    </xsl:element>
  </xsl:template>
  <!-- Template with formatName formatVer formatAmdNum formatSpec fileDecmTech-->
  <xsl:template match="dsFormat | distorFormat">
    <xsl:variable name="name" select="local-name(.)"/>
    <xsl:element name="{$name}">
      <xsl:apply-templates select="formatName"/>
      <xsl:apply-templates select="formatVer"/>
      <xsl:apply-templates select="formatAmdNum"/>
      <xsl:apply-templates select="formatSpec"/>
      <xsl:apply-templates select="fileDecmTech"/>
    </xsl:element>
  </xsl:template>
  <!-- Template with vertMinVal vertMaxVal vertUoM vertDatum-->
  <xsl:template match="vertEle">
    <xsl:variable name="name" select="local-name(.)"/>
    <xsl:element name="{$name}">
      <xsl:apply-templates select="vertMinVal"/>
      <xsl:apply-templates select="vertMaxVal"/>
      <xsl:apply-templates select="vertUoM"/>
      <xsl:apply-templates select="vertDatum"/>
    </xsl:element>
  </xsl:template>
  <!-- Template with exDesc vertEle tempEle geoEle-->
  <xsl:template match="dataExt | scpExt | srcExt">
    <xsl:variable name="name" select="local-name(.)"/>
    <xsl:element name="{$name}">
      <xsl:apply-templates select="exDesc"/>
      <xsl:for-each select="vertEle">
        <xsl:apply-templates select="."/>
      </xsl:for-each>
      <xsl:for-each select="tempEle">
        <tempEle>
          <xsl:apply-templates select="SpatTempEx"/>
          <xsl:apply-templates select="TempExtent"/>
        </tempEle>
      </xsl:for-each>
      <xsl:for-each select="geoEle">
        <xsl:apply-templates select="."/>
      </xsl:for-each>
    </xsl:element>
  </xsl:template>
  <xsl:template match="DateTypCd">
    <DateTypCd>
      <xsl:if test="@value = 'creation'">
        <xsl:attribute name="value">
          <xsl:text>001</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'publication'">
        <xsl:attribute name="value">
          <xsl:text>002</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'revision'">
        <xsl:attribute name="value">
          <xsl:text>003</xsl:text>
        </xsl:attribute>
      </xsl:if>
    </DateTypCd>
  </xsl:template>
  <xsl:template match="orDesc">
    <orDesc>
      <xsl:choose>
        <xsl:when test=".='Live Data and Maps'">
          <xsl:text>001</xsl:text>
        </xsl:when>
        <xsl:when test=".='Downloadable data'">
          <xsl:text>002</xsl:text>
        </xsl:when>
        <xsl:when test=".='Offline Data'">
          <xsl:text>003</xsl:text>
        </xsl:when>
        <xsl:when test=".='Static Map Images'">
          <xsl:text>004</xsl:text>
        </xsl:when>
        <xsl:when test=".='Other Documents'">
          <xsl:text>005</xsl:text>
        </xsl:when>
        <xsl:when test=".='Applications'">
          <xsl:text>006</xsl:text>
        </xsl:when>
        <xsl:when test=".='Geographic Services'">
          <xsl:text>007</xsl:text>
        </xsl:when>
        <xsl:when test=".='Clearinghouses'">
          <xsl:text>008</xsl:text>
        </xsl:when>
        <xsl:when test=".='Map Files'">
          <xsl:text>009</xsl:text>
        </xsl:when>
        <xsl:when test=".='Geographic Activities'">
          <xsl:text>010</xsl:text>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="."/>
        </xsl:otherwise>
      </xsl:choose>
    </orDesc>
  </xsl:template>
  <xsl:template match="OnFunctCd">
    <OnFunctCd>
      <xsl:if test="@value = 'download'">
        <xsl:attribute name="value">
          <xsl:text>001</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'information'">
        <xsl:attribute name="value">
          <xsl:text>002</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'offlineAccess'">
        <xsl:attribute name="value">
          <xsl:text>003</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'order'">
        <xsl:attribute name="value">
          <xsl:text>004</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'search'">
        <xsl:attribute name="value">
          <xsl:text>005</xsl:text>
        </xsl:attribute>
      </xsl:if>
    </OnFunctCd>
  </xsl:template>
  <xsl:template match="PresFormCd">
    <PresFormCd>
      <xsl:if test="@value = 'documentDigital'">
        <xsl:attribute name="value">
          <xsl:text>001</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'documentHardcopy'">
        <xsl:attribute name="value">
          <xsl:text>002</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'imageDigital'">
        <xsl:attribute name="value">
          <xsl:text>003</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'imageHardcopy'">
        <xsl:attribute name="value">
          <xsl:text>004</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'mapDigital'">
        <xsl:attribute name="value">
          <xsl:text>005</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'mapHardcopy'">
        <xsl:attribute name="value">
          <xsl:text>006</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'modelDigital'">
        <xsl:attribute name="value">
          <xsl:text>007</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'modelHardcopy'">
        <xsl:attribute name="value">
          <xsl:text>008</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'profileDigital'">
        <xsl:attribute name="value">
          <xsl:text>009</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'profileHardcopy'">
        <xsl:attribute name="value">
          <xsl:text>010</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'tableDigital'">
        <xsl:attribute name="value">
          <xsl:text>011</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'tableHardcopy'">
        <xsl:attribute name="value">
          <xsl:text>012</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'videoDigital'">
        <xsl:attribute name="value">
          <xsl:text>013</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'videoHardcopy'">
        <xsl:attribute name="value">
          <xsl:text>014</xsl:text>
        </xsl:attribute>
      </xsl:if>
    </PresFormCd>
  </xsl:template>
  <xsl:template match="RoleCd">
    <RoleCd>
      <xsl:if test="@value = 'resourceProvider'">
        <xsl:attribute name="value">
          <xsl:text>001</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'custodian'">
        <xsl:attribute name="value">
          <xsl:text>002</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'owner'">
        <xsl:attribute name="value">
          <xsl:text>003</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'user'">
        <xsl:attribute name="value">
          <xsl:text>004</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'distributor'">
        <xsl:attribute name="value">
          <xsl:text>005</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'originator'">
        <xsl:attribute name="value">
          <xsl:text>006</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'pointOfContact'">
        <xsl:attribute name="value">
          <xsl:text>007</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'principalInvestigator'">
        <xsl:attribute name="value">
          <xsl:text>008</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'processor'">
        <xsl:attribute name="value">
          <xsl:text>009</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'publisher'">
        <xsl:attribute name="value">
          <xsl:text>010</xsl:text>
        </xsl:attribute>
      </xsl:if>
    </RoleCd>
  </xsl:template>
  <xsl:template match="EvalMethTypeCd">
    <EvalMethTypeCd>
      <xsl:if test="@value = 'directInternal'">
        <xsl:attribute name="value">
          <xsl:text>001</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'directExternal'">
        <xsl:attribute name="value">
          <xsl:text>002</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'indirect'">
        <xsl:attribute name="value">
          <xsl:text>003</xsl:text>
        </xsl:attribute>
      </xsl:if>
    </EvalMethTypeCd>
  </xsl:template>
  <xsl:template match="AscTypeCd">
    <AscTypeCd>
      <xsl:if test="@value = 'crossReference'">
        <xsl:attribute name="value">
          <xsl:text>001</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'largerWorkCitation'">
        <xsl:attribute name="value">
          <xsl:text>002</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'partOfSeamlessDatabase'">
        <xsl:attribute name="value">
          <xsl:text>003</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'source'">
        <xsl:attribute name="value">
          <xsl:text>004</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'stereomate'">
        <xsl:attribute name="value">
          <xsl:text>005</xsl:text>
        </xsl:attribute>
      </xsl:if>
    </AscTypeCd>
  </xsl:template>
  <xsl:template match="InitTypCd">
    <InitTypCd>
      <xsl:if test="@value = 'campaign'">
        <xsl:attribute name="value">
          <xsl:text>001</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'collection'">
        <xsl:attribute name="value">
          <xsl:text>002</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'exercise'">
        <xsl:attribute name="value">
          <xsl:text>003</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'experiment'">
        <xsl:attribute name="value">
          <xsl:text>004</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'investigation'">
        <xsl:attribute name="value">
          <xsl:text>005</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'mission'">
        <xsl:attribute name="value">
          <xsl:text>006</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'nonImageSensor'">
        <xsl:attribute name="value">
          <xsl:text>007</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'operation'">
        <xsl:attribute name="value">
          <xsl:text>008</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'platform'">
        <xsl:attribute name="value">
          <xsl:text>009</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'process'">
        <xsl:attribute name="value">
          <xsl:text>010</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'program'">
        <xsl:attribute name="value">
          <xsl:text>011</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'project'">
        <xsl:attribute name="value">
          <xsl:text>012</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'study'">
        <xsl:attribute name="value">
          <xsl:text>013</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'task'">
        <xsl:attribute name="value">
          <xsl:text>014</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'trial'">
        <xsl:attribute name="value">
          <xsl:text>015</xsl:text>
        </xsl:attribute>
      </xsl:if>
    </InitTypCd>
  </xsl:template>
  <xsl:template match="CellGeoCd">
    <CellGeoCd>
      <xsl:if test="@value = 'point'">
        <xsl:attribute name="value">
          <xsl:text>001</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'area'">
        <xsl:attribute name="value">
          <xsl:text>002</xsl:text>
        </xsl:attribute>
      </xsl:if>
    </CellGeoCd>
  </xsl:template>
  <xsl:template match="CharSetCd">
    <CharSetCd>
      <xsl:if test="@value = 'ucs2'">
        <xsl:attribute name="value">
          <xsl:text>001</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'ucs4'">
        <xsl:attribute name="value">
          <xsl:text>002</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'utf7'">
        <xsl:attribute name="value">
          <xsl:text>003</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'utf8'">
        <xsl:attribute name="value">
          <xsl:text>004</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'utf16'">
        <xsl:attribute name="value">
          <xsl:text>005</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = '8859part1'">
        <xsl:attribute name="value">
          <xsl:text>006</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = '8859part2'">
        <xsl:attribute name="value">
          <xsl:text>007</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = '8859part3'">
        <xsl:attribute name="value">
          <xsl:text>008</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = '8859part4'">
        <xsl:attribute name="value">
          <xsl:text>009</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = '8859part5'">
        <xsl:attribute name="value">
          <xsl:text>010</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = '8859part6'">
        <xsl:attribute name="value">
          <xsl:text>011</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = '8859part7'">
        <xsl:attribute name="value">
          <xsl:text>012</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = '8859part8'">
        <xsl:attribute name="value">
          <xsl:text>013</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = '8859part9'">
        <xsl:attribute name="value">
          <xsl:text>014</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = '8859part11'">
        <xsl:attribute name="value">
          <xsl:text>015</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = '8859part14'">
        <xsl:attribute name="value">
          <xsl:text>016</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = '8859part15'">
        <xsl:attribute name="value">
          <xsl:text>017</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'jis'">
        <xsl:attribute name="value">
          <xsl:text>018</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'shiftJIS'">
        <xsl:attribute name="value">
          <xsl:text>019</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'eucJP'">
        <xsl:attribute name="value">
          <xsl:text>020</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'usAscii'">
        <xsl:attribute name="value">
          <xsl:text>021</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'ebcdic'">
        <xsl:attribute name="value">
          <xsl:text>022</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'eucKR'">
        <xsl:attribute name="value">
          <xsl:text>023</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'big5'">
        <xsl:attribute name="value">
          <xsl:text>024</xsl:text>
        </xsl:attribute>
      </xsl:if>
    </CharSetCd>
  </xsl:template>
  <xsl:template match="ClasscationCd">
    <ClasscationCd>
      <xsl:if test="@value = 'unclassified'">
        <xsl:attribute name="value">
          <xsl:text>001</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'restricted'">
        <xsl:attribute name="value">
          <xsl:text>002</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'confidential'">
        <xsl:attribute name="value">
          <xsl:text>003</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'secret'">
        <xsl:attribute name="value">
          <xsl:text>004</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'topsecret'">
        <xsl:attribute name="value">
          <xsl:text>005</xsl:text>
        </xsl:attribute>
      </xsl:if>
    </ClasscationCd>
  </xsl:template>
  <xsl:template match="ContentTypCd">
    <ContentTypCd>
      <xsl:if test="@value = 'image'">
        <xsl:attribute name="value">
          <xsl:text>001</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'thematicClassification'">
        <xsl:attribute name="value">
          <xsl:text>002</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'physicalMeasurement'">
        <xsl:attribute name="value">
          <xsl:text>003</xsl:text>
        </xsl:attribute>
      </xsl:if>
    </ContentTypCd>
  </xsl:template>
  <xsl:template match="DatatypeCd">
    <DatatypeCd>
      <xsl:if test="@value = 'class'">
        <xsl:attribute name="value">
          <xsl:text>001</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'codelist'">
        <xsl:attribute name="value">
          <xsl:text>002</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'enumeration'">
        <xsl:attribute name="value">
          <xsl:text>003</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'codelistElement'">
        <xsl:attribute name="value">
          <xsl:text>004</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'abstractClass'">
        <xsl:attribute name="value">
          <xsl:text>005</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'aggregateClass'">
        <xsl:attribute name="value">
          <xsl:text>006</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'specifiedClass'">
        <xsl:attribute name="value">
          <xsl:text>007</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'datatypeClass'">
        <xsl:attribute name="value">
          <xsl:text>008</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'interfaceClass'">
        <xsl:attribute name="value">
          <xsl:text>009</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'unionClass'">
        <xsl:attribute name="value">
          <xsl:text>010</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'metaclass'">
        <xsl:attribute name="value">
          <xsl:text>011</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'typeClass'">
        <xsl:attribute name="value">
          <xsl:text>012</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'characterString'">
        <xsl:attribute name="value">
          <xsl:text>013</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'integer'">
        <xsl:attribute name="value">
          <xsl:text>014</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'association'">
        <xsl:attribute name="value">
          <xsl:text>015</xsl:text>
        </xsl:attribute>
      </xsl:if>
    </DatatypeCd>
  </xsl:template>
  <xsl:template match="DimNameTypCd">
    <DimNameTypCd>
      <xsl:if test="@value = 'row'">
        <xsl:attribute name="value">
          <xsl:text>001</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'column'">
        <xsl:attribute name="value">
          <xsl:text>002</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'vertical'">
        <xsl:attribute name="value">
          <xsl:text>003</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'track'">
        <xsl:attribute name="value">
          <xsl:text>004</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'crossTrack'">
        <xsl:attribute name="value">
          <xsl:text>005</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'line'">
        <xsl:attribute name="value">
          <xsl:text>006</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'sample'">
        <xsl:attribute name="value">
          <xsl:text>007</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'time'">
        <xsl:attribute name="value">
          <xsl:text>008</xsl:text>
        </xsl:attribute>
      </xsl:if>
    </DimNameTypCd>
  </xsl:template>
  <xsl:template match="GeoObjTypCd">
    <GeoObjTypCd>
      <xsl:if test="@value = 'complexes'">
        <xsl:attribute name="value">
          <xsl:text>001</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'composites'">
        <xsl:attribute name="value">
          <xsl:text>002</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'curve'">
        <xsl:attribute name="value">
          <xsl:text>003</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'point'">
        <xsl:attribute name="value">
          <xsl:text>004</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'solid'">
        <xsl:attribute name="value">
          <xsl:text>005</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'surface'">
        <xsl:attribute name="value">
          <xsl:text>006</xsl:text>
        </xsl:attribute>
      </xsl:if>
    </GeoObjTypCd>
  </xsl:template>
  <xsl:template match="ImgCondCd">
    <ImgCondCd>
      <xsl:if test="@value = 'blurredImage'">
        <xsl:attribute name="value">
          <xsl:text>001</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'cloud'">
        <xsl:attribute name="value">
          <xsl:text>002</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'degradingObliquity'">
        <xsl:attribute name="value">
          <xsl:text>003</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'fog'">
        <xsl:attribute name="value">
          <xsl:text>004</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'heavySmokeOrDust'">
        <xsl:attribute name="value">
          <xsl:text>005</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'night'">
        <xsl:attribute name="value">
          <xsl:text>006</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'rain'">
        <xsl:attribute name="value">
          <xsl:text>007</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'semiDarkness'">
        <xsl:attribute name="value">
          <xsl:text>008</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'shadow'">
        <xsl:attribute name="value">
          <xsl:text>009</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'snow'">
        <xsl:attribute name="value">
          <xsl:text>010</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'terrainMasking'">
        <xsl:attribute name="value">
          <xsl:text>011</xsl:text>
        </xsl:attribute>
      </xsl:if>
    </ImgCondCd>
  </xsl:template>
  <xsl:template match="KeyTypCd">
    <KeyTypCd>
      <xsl:if test="@value = 'discipline'">
        <xsl:attribute name="value">
          <xsl:text>001</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'place'">
        <xsl:attribute name="value">
          <xsl:text>002</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'stratum'">
        <xsl:attribute name="value">
          <xsl:text>003</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'temporal'">
        <xsl:attribute name="value">
          <xsl:text>004</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'theme'">
        <xsl:attribute name="value">
          <xsl:text>005</xsl:text>
        </xsl:attribute>
      </xsl:if>
    </KeyTypCd>
  </xsl:template>
  <xsl:template match="MaintFreqCd">
    <MaintFreqCd>
      <xsl:if test="@value = 'continual'">
        <xsl:attribute name="value">
          <xsl:text>001</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'daily'">
        <xsl:attribute name="value">
          <xsl:text>002</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'weekly'">
        <xsl:attribute name="value">
          <xsl:text>003</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'fortnightly'">
        <xsl:attribute name="value">
          <xsl:text>004</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'monthly'">
        <xsl:attribute name="value">
          <xsl:text>005</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'quarterly'">
        <xsl:attribute name="value">
          <xsl:text>006</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'biannually'">
        <xsl:attribute name="value">
          <xsl:text>007</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'annually'">
        <xsl:attribute name="value">
          <xsl:text>008</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'asNeeded'">
        <xsl:attribute name="value">
          <xsl:text>009</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'irregular'">
        <xsl:attribute name="value">
          <xsl:text>010</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'notPlanned'">
        <xsl:attribute name="value">
          <xsl:text>011</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'unknown'">
        <xsl:attribute name="value">
          <xsl:text>998</xsl:text>
        </xsl:attribute>
      </xsl:if>
    </MaintFreqCd>
  </xsl:template>
  <xsl:template match="MedFormCd">
    <MedFormCd>
      <xsl:if test="@value = 'cpio'">
        <xsl:attribute name="value">
          <xsl:text>001</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'tar'">
        <xsl:attribute name="value">
          <xsl:text>002</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'highSierra'">
        <xsl:attribute name="value">
          <xsl:text>003</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'iso9660'">
        <xsl:attribute name="value">
          <xsl:text>004</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'iso9660RockRidge'">
        <xsl:attribute name="value">
          <xsl:text>005</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'iso9660AppleHFS'">
        <xsl:attribute name="value">
          <xsl:text>006</xsl:text>
        </xsl:attribute>
      </xsl:if>
    </MedFormCd>
  </xsl:template>
  <xsl:template match="MedNameCd">
    <MedNameCd>
      <xsl:if test="@value = 'cdRom'">
        <xsl:attribute name="value">
          <xsl:text>001</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'dvd'">
        <xsl:attribute name="value">
          <xsl:text>002</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'dvdRom'">
        <xsl:attribute name="value">
          <xsl:text>003</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = '3halfInchFloppy'">
        <xsl:attribute name="value">
          <xsl:text>004</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = '5quarterInchFloppy'">
        <xsl:attribute name="value">
          <xsl:text>005</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = '7trackTape'">
        <xsl:attribute name="value">
          <xsl:text>006</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = '9trackTape'">
        <xsl:attribute name="value">
          <xsl:text>007</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = '3480Cartridge'">
        <xsl:attribute name="value">
          <xsl:text>008</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = '3490Cartridge'">
        <xsl:attribute name="value">
          <xsl:text>009</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = '3580Cartridge'">
        <xsl:attribute name="value">
          <xsl:text>010</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = '4mmCartridgeTape'">
        <xsl:attribute name="value">
          <xsl:text>011</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = '8mmCartridgeTape'">
        <xsl:attribute name="value">
          <xsl:text>012</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = '1quarterInchCartridgeTape'">
        <xsl:attribute name="value">
          <xsl:text>013</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'digitalLinearTape'">
        <xsl:attribute name="value">
          <xsl:text>014</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'onLine'">
        <xsl:attribute name="value">
          <xsl:text>015</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'satellite'">
        <xsl:attribute name="value">
          <xsl:text>016</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'telephoneLink'">
        <xsl:attribute name="value">
          <xsl:text>017</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'hardcopy'">
        <xsl:attribute name="value">
          <xsl:text>018</xsl:text>
        </xsl:attribute>
      </xsl:if>
    </MedNameCd>
  </xsl:template>
  <xsl:template match="ObCd">
    <ObCd>
      <xsl:if test="@value = 'mandatory'">
        <xsl:attribute name="value">
          <xsl:text>001</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'optional'">
        <xsl:attribute name="value">
          <xsl:text>002</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'conditional'">
        <xsl:attribute name="value">
          <xsl:text>003</xsl:text>
        </xsl:attribute>
      </xsl:if>
    </ObCd>
  </xsl:template>
  <xsl:template match="PixOrientCd">
    <PixOrientCd>
      <xsl:if test="@value = 'center'">
        <xsl:attribute name="value">
          <xsl:text>001</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'lowerLeft'">
        <xsl:attribute name="value">
          <xsl:text>002</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'lowerRight'">
        <xsl:attribute name="value">
          <xsl:text>003</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'upperRight'">
        <xsl:attribute name="value">
          <xsl:text>004</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'upperLeft'">
        <xsl:attribute name="value">
          <xsl:text>005</xsl:text>
        </xsl:attribute>
      </xsl:if>
    </PixOrientCd>
  </xsl:template>
  <xsl:template match="ProgCd">
    <ProgCd>
      <xsl:if test="@value = 'completed'">
        <xsl:attribute name="value">
          <xsl:text>001</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'historicalArchive'">
        <xsl:attribute name="value">
          <xsl:text>002</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'obsolete'">
        <xsl:attribute name="value">
          <xsl:text>003</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'onGoing'">
        <xsl:attribute name="value">
          <xsl:text>004</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'planned'">
        <xsl:attribute name="value">
          <xsl:text>005</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'required'">
        <xsl:attribute name="value">
          <xsl:text>006</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'underdevelopment'">
        <xsl:attribute name="value">
          <xsl:text>007</xsl:text>
        </xsl:attribute>
      </xsl:if>
    </ProgCd>
  </xsl:template>
  <xsl:template match="RestrictCd">
    <RestrictCd>
      <xsl:if test="@value = 'copyright'">
        <xsl:attribute name="value">
          <xsl:text>001</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'patent'">
        <xsl:attribute name="value">
          <xsl:text>002</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'patentPending'">
        <xsl:attribute name="value">
          <xsl:text>003</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'trademark'">
        <xsl:attribute name="value">
          <xsl:text>004</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'license'">
        <xsl:attribute name="value">
          <xsl:text>005</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'intellectualPropertyRights'">
        <xsl:attribute name="value">
          <xsl:text>006</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'restricted'">
        <xsl:attribute name="value">
          <xsl:text>007</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'otherRestictions'">
        <xsl:attribute name="value">
          <xsl:text>008</xsl:text>
        </xsl:attribute>
      </xsl:if>
    </RestrictCd>
  </xsl:template>
  <xsl:template match="ScopeCd">
    <ScopeCd>
      <xsl:if test="@value = 'attribute'">
        <xsl:attribute name="value">
          <xsl:text>001</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'attributeType'">
        <xsl:attribute name="value">
          <xsl:text>002</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'collectionHardware'">
        <xsl:attribute name="value">
          <xsl:text>003</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'collectionSession'">
        <xsl:attribute name="value">
          <xsl:text>004</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'dataset'">
        <xsl:attribute name="value">
          <xsl:text>005</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'series'">
        <xsl:attribute name="value">
          <xsl:text>006</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'nonGeographicDataset'">
        <xsl:attribute name="value">
          <xsl:text>007</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'dimensionGroup'">
        <xsl:attribute name="value">
          <xsl:text>008</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'feature'">
        <xsl:attribute name="value">
          <xsl:text>009</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'featureType'">
        <xsl:attribute name="value">
          <xsl:text>010</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'propertyType'">
        <xsl:attribute name="value">
          <xsl:text>011</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'fieldSession'">
        <xsl:attribute name="value">
          <xsl:text>012</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'software'">
        <xsl:attribute name="value">
          <xsl:text>013</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'service'">
        <xsl:attribute name="value">
          <xsl:text>014</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'model'">
        <xsl:attribute name="value">
          <xsl:text>015</xsl:text>
        </xsl:attribute>
      </xsl:if>
    </ScopeCd>
  </xsl:template>
  <xsl:template match="SpatRepTypCd">
    <SpatRepTypCd>
      <xsl:if test="@value = 'vector'">
        <xsl:attribute name="value">
          <xsl:text>001</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'grid'">
        <xsl:attribute name="value">
          <xsl:text>002</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'textTable'">
        <xsl:attribute name="value">
          <xsl:text>003</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'tin'">
        <xsl:attribute name="value">
          <xsl:text>004</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'stereoModel'">
        <xsl:attribute name="value">
          <xsl:text>005</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'video'">
        <xsl:attribute name="value">
          <xsl:text>006</xsl:text>
        </xsl:attribute>
      </xsl:if>
    </SpatRepTypCd>
  </xsl:template>
  <xsl:template match="TopicCatCd">
    <TopicCatCd>
      <xsl:if test="@value = 'farming'">
        <xsl:attribute name="value">
          <xsl:text>001</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'biota'">
        <xsl:attribute name="value">
          <xsl:text>002</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'boundaries'">
        <xsl:attribute name="value">
          <xsl:text>003</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'climatologyMeteorologyAtmosphere'">
        <xsl:attribute name="value">
          <xsl:text>004</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'economy'">
        <xsl:attribute name="value">
          <xsl:text>005</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'elevation'">
        <xsl:attribute name="value">
          <xsl:text>006</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'environment'">
        <xsl:attribute name="value">
          <xsl:text>007</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'geoscientificInformation'">
        <xsl:attribute name="value">
          <xsl:text>008</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'health'">
        <xsl:attribute name="value">
          <xsl:text>009</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'imageryBaseMapsEarthCover'">
        <xsl:attribute name="value">
          <xsl:text>010</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'intelligenceMilitary'">
        <xsl:attribute name="value">
          <xsl:text>011</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'inlandWaters'">
        <xsl:attribute name="value">
          <xsl:text>012</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'location'">
        <xsl:attribute name="value">
          <xsl:text>013</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'oceans'">
        <xsl:attribute name="value">
          <xsl:text>014</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'planningCadastre'">
        <xsl:attribute name="value">
          <xsl:text>015</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'society'">
        <xsl:attribute name="value">
          <xsl:text>016</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'structure'">
        <xsl:attribute name="value">
          <xsl:text>017</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'transportation'">
        <xsl:attribute name="value">
          <xsl:text>018</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'utilitiesCommunication'">
        <xsl:attribute name="value">
          <xsl:text>019</xsl:text>
        </xsl:attribute>
      </xsl:if>
    </TopicCatCd>
  </xsl:template>
  <xsl:template match="TopoLevCd">
    <TopoLevCd>
      <xsl:if test="@value = 'geometryOnly'">
        <xsl:attribute name="value">
          <xsl:text>001</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'topology1D'">
        <xsl:attribute name="value">
          <xsl:text>002</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'planarGraph'">
        <xsl:attribute name="value">
          <xsl:text>003</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'fullPlanarGraph'">
        <xsl:attribute name="value">
          <xsl:text>004</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'surfaceGraph'">
        <xsl:attribute name="value">
          <xsl:text>005</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'fullSurfaceGraph'">
        <xsl:attribute name="value">
          <xsl:text>006</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'topology3D'">
        <xsl:attribute name="value">
          <xsl:text>007</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'fullTopology3D'">
        <xsl:attribute name="value">
          <xsl:text>008</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value = 'abstract'">
        <xsl:attribute name="value">
          <xsl:text>009</xsl:text>
        </xsl:attribute>
      </xsl:if>
    </TopoLevCd>
  </xsl:template>
</xsl:stylesheet>
