<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">
  <xsl:output method="xml"/>
  <!--  <xsl:output method="xml" doctype-system="ISO_19115.dtd"/> -->
  <xsl:template match="/">
    <Metadata>
      <xsl:apply-templates select="metadata/mdFileID"/>
      <xsl:apply-templates select="metadata/mdLang"/>
      <xsl:apply-templates select="metadata/mdChar"/>
      <xsl:apply-templates select="metadata/mdParentID"/>
      <xsl:for-each select="metadata/mdHrLv">
        <xsl:apply-templates select="."/>
      </xsl:for-each>
      <xsl:for-each select="metadata/mdHrLvName">
        <xsl:apply-templates select="."/>
      </xsl:for-each>

      <xsl:apply-templates select="metadata/mdContact"/>

      <xsl:apply-templates select="metadata/mdDateSt"/>
      <xsl:apply-templates select="metadata/mdStanName"/>
      <xsl:apply-templates select="metadata/mdStanVer"/>
      <xsl:apply-templates select="metadata/distInfo"/>
      <xsl:for-each select="metadata/dataIdInfo">
        <xsl:apply-templates select="."/>
      </xsl:for-each>
      <xsl:for-each select="metadata/appSchInfo">
        <xsl:apply-templates select="."/>
      </xsl:for-each>
      <xsl:for-each select="metadata/porCatInfo">
        <xsl:apply-templates select="."/>
      </xsl:for-each>
      <xsl:apply-templates select="metadata/mdMaint"/>
      <xsl:for-each select="metadata/mdConst">
        <xsl:apply-templates select="."/>
      </xsl:for-each>
      <xsl:for-each select="metadata/dqInfo">
        <xsl:apply-templates select="."/>
      </xsl:for-each>
      <xsl:for-each select="metadata/spatRepInfo">
        <xsl:apply-templates select="."/>
      </xsl:for-each>
      <xsl:for-each select="metadata/refSysInfo">
        <xsl:apply-templates select="."/>
      </xsl:for-each>
      <xsl:for-each select="metadata/contInfo">
        <xsl:apply-templates select="."/>
      </xsl:for-each>
    </Metadata>
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
      <xsl:for-each select="distributor">
        <xsl:if test="distorCont">
          <distributor>
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
          </distributor>
        </xsl:if>
        <xsl:for-each select="distorTran">
          <xsl:apply-templates select="."/>
        </xsl:for-each>
      </xsl:for-each>
      <!--      <xsl:for-each select="distTranOps">
                      <xsl:apply-templates select="."/>
                  </xsl:for-each> -->
    </distInfo>
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
  <!-- onLineMed - because of an error in ArcCatalog, we will match on offLineMed -->
  <xsl:template match="offLineMed">
    <onLineMed>
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
    </onLineMed>
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
        <geoBox>
          <xsl:apply-templates select="westBL"/>
          <xsl:apply-templates select="eastBL"/>
          <xsl:apply-templates select="southBL"/>
          <xsl:apply-templates select="northBL"/>
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
  <!-- <xsl:template match="mdFileID | rpIndName | spatObj | spatSchName | asSchLang | asCstLang | asAscii | asGraFile | asSwDevFile | asSwDevFiFt | mdParentID | rpOrgName | rpPosName | city | adminArea | postCode | country | linkage | protocol | appProfile | orName | orDesc | cntHours | cntInstr | mdDateSt | mdStanName | mdStanVer | formatName | formatVer | formatAmdNum | formatSpec | fileDecmTech | resFees | planAvDtTm | ordInstr | handDesc | userNote | classSys | uomName | Real | Decimal | Integer | bgFileType | bgFileDesc | bgFileName | issn | isbn | collTitle | otherCitDet | artPage | issId | seriesName | resEdDate | resEd | resAltTitle | seconds | minutes | hours | timeIndicator | days | months | years | designator | other | datasetSet | attribIntSet | featIntSet | featSet | attribSet | usrDetLim | usageDate | specUsage | identCode | exTypeCode | envirDesc | suppInfo | exDesc | vertMaxVal | vertMinVal | end | begin | clkTime | calDate | coordinates | zone | longCntMer | latProjOri | falEastng | falNorthng | sclFacEqu | hgtProsPt | longProjCnt | latProjCnt | sclFacCnt | stVrLongPl | sclFacPrOr | aziPtLong | aziAngle | obLineLong | obLineLat | semiMajAx | denFlatRat | medNote | medVol | medDenUnits | transSize | unitsODist | ordTurn | resTitle | idAbs | idPurp | languageCode | rfDenom | mdHrLvName | dateNext | maintNote | voiceNum | faxNum | delPoint | eMailAdd | citId | citIdType | othConsts | useLimit | stanPara | idCredit | medDensity | keyword | statement | srcDesc | stepDesc | stepRat | stepDateTm | measName | measureDescription | evalMethDesc | measDateTm | conExpl | conPass | quanValType | errStat | quanValue | Result | numDims | tranParaAv | chkPtAv | chkPtDesc | transDimDesc | transDimMap | dimSize | ctrlPtAv | orieParaAv | orieParaDesc | georefPars | TopLvlCd | geoObjCnt | ContInfo | attDesc | scope | aName | dimDescrp | maxVal | minVal | pkResp | bitsPerVal | toneGrad | sclFac | offset | compCode | incWithDS | illElevAng | illAziAng | cloudCovPer | cmpGenQuan | trianInd | radCalDatAv | camCalInAv | filmDistInAv | lensDistInAv | westBL | eastBL | southBL | northBL"> -->
  <xsl:template
    match="mdFileID | rpIndName | spatObj | spatSchName | asSchLang | asCstLang | asAscii | asGraFile | asSwDevFile | asSwDevFiFt | mdParentID | rpOrgName | rpPosName | city | adminArea | postCode | country | linkage | protocol | appProfile | orName | cntHours | cntInstr | mdStanName | mdStanVer | formatName | formatVer | formatAmdNum | formatSpec | fileDecmTech | resFees | planAvDtTm | ordInstr | handDesc | userNote | classSys | uomName | Real | Decimal | Integer | bgFileType | bgFileDesc | bgFileName | issn | isbn | collTitle | otherCitDet | artPage | issId | seriesName | resEdDate | resEd | resAltTitle | seconds | minutes | hours | timeIndicator | days | months | years | designator | other | datasetSet | attribIntSet | featIntSet | featSet | attribSet | usrDetLim | usageDate | specUsage | identCode | exTypeCode | envirDesc | suppInfo | exDesc | vertMaxVal | vertMinVal | end | begin | clkTime | calDate | coordinates | zone | longCntMer | latProjOri | falEastng | falNorthng | sclFacEqu | hgtProsPt | longProjCnt | latProjCnt | sclFacCnt | stVrLongPl | sclFacPrOr | aziPtLong | aziAngle | obLineLong | obLineLat | semiMajAx | denFlatRat | medNote | medVol | medDenUnits | ordTurn | resTitle | idAbs | idPurp | languageCode | rfDenom | mdHrLvName | dateNext | maintNote | voiceNum | faxNum | delPoint | eMailAdd | citId | citIdType | othConsts | useLimit | stanPara | idCredit | medDensity | keyword | statement | srcDesc | stepDesc | stepRat | stepDateTm | measName | measureDescription | evalMethDesc | measDateTm | conExpl | conPass | quanValType | errStat | quanValue | Result | numDims | tranParaAv | chkPtAv | chkPtDesc | transDimDesc | transDimMap | dimSize | ctrlPtAv | orieParaAv | orieParaDesc | georefPars | TopLvlCd | geoObjCnt | ContInfo | attDesc | scope | aName | dimDescrp | maxVal | minVal | pkResp | bitsPerVal | toneGrad | sclFac | offset | compCode | incWithDS | illElevAng | illAziAng | cloudCovPer | cmpGenQuan | trianInd | radCalDatAv | camCalInAv | filmDistInAv | lensDistInAv | westBL | eastBL | southBL | northBL">
    <xsl:variable name="name" select="local-name(.)"/>
    <xsl:element name="{$name}">
      <xsl:if test="@value">
        <xsl:attribute name="value">
          <xsl:apply-templates select="@value"/>
        </xsl:attribute>
      </xsl:if>
      <!-- Strip html tags -->
      <xsl:value-of select="replace(., '&lt;/?\w+[^&lt;]*&gt;', '')"/>
    </xsl:element>
  </xsl:template>

  <xsl:template
    match="mdDateSt">
    <xsl:variable name="name" select="local-name(.)"/>
    <xsl:element name="{$name}">
      <xsl:if test="@value">
        <xsl:attribute name="value">
          <xsl:apply-templates select="@value"/>
        </xsl:attribute>
      </xsl:if>
      <xsl:choose>
        <xsl:when test="string-length(.) = 8">
          <xsl:value-of select="substring(., 1, 4)"/>-<xsl:value-of select="substring(., 5, 2)"/>-<xsl:value-of select="substring(., 7, 2)"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="."/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:element>
  </xsl:template>

  <xsl:template match="conversionToISOstandardUnit">
    <conversionToISOstandarUnit>
      <xsl:value-of select="."/>
    </conversionToISOstandarUnit>
  </xsl:template>
  <!-- ArcCatalog 8 & 9 do not provide unit of distribution in the ISO metadata, so I've hardcoded that here, assuming is is always provided in MB -->
  <xsl:template match="transSize">
    <unitsODist>MB</unitsODist>
    <transSize>
      <xsl:value-of select="."/>
    </transSize>
  </xsl:template>
  <!-- Update to add - separators in date (it is assumed the incoming date format is CCYYMMDD)-->
  <xsl:template match="refDate">
    <xsl:variable name="name" select="local-name(.)"/>
    <xsl:variable name="year" select="substring(., 1, 4)"/>
    <xsl:variable name="month" select="substring(., 5, 2)"/>
    <xsl:variable name="day" select="substring(., 7, 2)"/>
    <xsl:variable name="date" select="string(concat($year,'-',$month,'-',$day))"></xsl:variable>
    <xsl:element name="{$name}">
      <xsl:value-of select="$date"/>
    </xsl:element>
  </xsl:template>
  <!-- Template with only 1 child node THIS IS THE CORRECT ONE ACCORDING TO ISO 19115 -->
  <!--    <xsl:template match="mdLang | mdChar | orFunct | role | medName | TempExtent | exTemp | TM_Instant | TM_CalDate | TM_ClockTime | vertDatum | keyTyp | equScale | dataChar | class | maintFreq | presForm | accessConsts | useConsts | medFormat | status | spatRpType | dataLang | tpCat | polygon | scpLvl | srcScale | RefSystem | evalMethType | cellGeo | ptInPixel | dimName | topLvl | geoObjTyp | contentTyp | catLang | LocalName | ScopedName | imagCond | refDateType">
        <xsl:variable name="name" select="local-name(.)"/>
        <xsl:element name="{$name}">
            <xsl:apply-templates select="node()"/>
        </xsl:element>
    </xsl:template> -->
  <!-- Template with only 1 child node TEMPORARILY MODIFIED TO MATCH GEONETWORK -->
  <xsl:template
    match="mdLang | mdChar | orFunct | role | medName | TempExtent | exTemp | TM_Instant | TM_CalDate | TM_ClockTime | vertDatum | keyTyp | equScale | dataChar | class | maintFreq | presForm | accessConsts | useConsts | medFormat | status | spatRpType | dataLang | tpCat | polygon | scpLvl | srcScale | RefSystem | evalMethType | cellGeo | ptInPixel | dimName | topLvl | geoObjTyp | contentTyp | catLang | LocalName | ScopedName | imagCond">
    <xsl:variable name="name" select="local-name(.)"/>
    <xsl:element name="{$name}">
      <xsl:apply-templates select="node()"/>
    </xsl:element>
  </xsl:template>
  <!-- refDateType TO BE DELETED WHEN REMOVING THE TEMPORARYTemplate with only 1 child -->
  <xsl:template match="refDateType">
    <refDateType>
      <xsl:apply-templates select="node()"/>
    </refDateType>
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
  <!-- Template with SecConsts LegConsts Consts -->
  <xsl:template match="mdConst | resConst">
    <xsl:variable name="name" select="local-name(.)"/>
    <xsl:element name="{$name}">
      <xsl:if test="node()=SecConsts">
        <xsl:apply-templates select="SecConsts"/>
      </xsl:if>
      <xsl:if test="node()=LegConsts">
        <xsl:apply-templates select="LegConsts"/>
      </xsl:if>
      <xsl:if test="node()=Consts">
        <xsl:apply-templates select="Consts"/>
      </xsl:if>
    </xsl:element>
  </xsl:template>
  <!-- Template with uomName conversionToISOstandardUnit-->
  <xsl:template
    match="vertUoM | axisUnits | falENUnits | UomArea | UomTime | UomLength | UomVolume | UomVelocity | UomAngle | UomScale | valUnit">
    <xsl:variable name="name" select="local-name(.)"/>
    <xsl:element name="{$name}">
      <xsl:apply-templates select="uomName"/>
      <xsl:apply-templates select="conversionToISOstandardUnit"/>
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

  <!-- Template for Citation with resTitle resAltTitle resRefDate resEd resEdDate citId citIdType citRespParty presForm otherCitDet collTitle isbn issn-->
  <!-- When no resRefDate is found in the source document, the whole citation object is neglected/skipped!!! -->
  <xsl:template
    match="portCatCit | asName | identAuth | thesaName | idCitation | srcCitatn | evaluationProcedure | conSpec | paraCit | catCitation">
    <!--<xsl:if test="resRefDate/refDateType">-->
    <xsl:variable name="name" select="local-name(.)"/>
    <xsl:element name="{$name}">
      <xsl:apply-templates select="resTitle"/>
      <xsl:for-each select="resAltTitle">
        <xsl:apply-templates select="."/>
      </xsl:for-each>
      <xsl:for-each select="resRefDate">
        <xsl:if test="refDateType">
          <resRefDate>
            <xsl:apply-templates select="refDate"/>
            <xsl:apply-templates select="refDateType"/>
          </resRefDate>
        </xsl:if>
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
    <!--</xsl:if>-->
  </xsl:template>

  <!-- Template with unitsODist transSize onLineSrc onLineMed-->
  <!--  <xsl:template match="distorTran | distTranOps">
          <xsl:variable name="name" select="local-name(.)"/>
          <xsl:element name="{$name}">
              <xsl:apply-templates select="unitsODist"/>
              <xsl:apply-templates select="transSize"/>
              <xsl:apply-templates select="onLineSrc"/>
              <xsl:apply-templates select="onLineMed"/>
          </xsl:element>
      </xsl:template> -->
  <xsl:template match="distorTran | distTranOps">
    <!--    <xsl:variable name="name" select="local-name(.)"/> -->
    <xsl:element name="distTranOps">
      <xsl:apply-templates select="unitsODist"/>
      <xsl:apply-templates select="transSize"/>
      <xsl:apply-templates select="onLineSrc"/>
      <xsl:apply-templates
        select="offLineMed"/>  <!-- because of an error in ArcCatalog, we will match on offLineMed -->
    </xsl:element>
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
      <!-- ArcCatalog has some non ISO content stored in the geoEle element, so just ignored here -->
      <!--      <xsl:for-each select="geoEle">
                <xsl:apply-templates select="."/>
            </xsl:for-each> -->
    </xsl:element>
  </xsl:template>
  <xsl:template match="DateTypCd">
    <DateTypCd>
      <xsl:if test="@value=001">
        <xsl:attribute name="value">
          <xsl:text>creation</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=002">
        <xsl:attribute name="value">
          <xsl:text>publication</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=003">
        <xsl:attribute name="value">
          <xsl:text>revision</xsl:text>
        </xsl:attribute>
      </xsl:if>
    </DateTypCd>
  </xsl:template>
  <xsl:template match="orDesc">
    <orDesc>
      <xsl:choose>
        <xsl:when test=".=001">
          <xsl:text>Live Data and Maps</xsl:text>
        </xsl:when>
        <xsl:when test=".=002">
          <xsl:text>Downloadable data</xsl:text>
        </xsl:when>
        <xsl:when test=".=003">
          <xsl:text>Offline Data</xsl:text>
        </xsl:when>
        <xsl:when test=".=004">
          <xsl:text>Static Map Images</xsl:text>
        </xsl:when>
        <xsl:when test=".=005">
          <xsl:text>Other Documents</xsl:text>
        </xsl:when>
        <xsl:when test=".=006">
          <xsl:text>Applications</xsl:text>
        </xsl:when>
        <xsl:when test=".=007">
          <xsl:text>Geographic Services</xsl:text>
        </xsl:when>
        <xsl:when test=".=008">
          <xsl:text>Clearinghouses</xsl:text>
        </xsl:when>
        <xsl:when test=".=009">
          <xsl:text>Map Files</xsl:text>
        </xsl:when>
        <xsl:when test=".=010">
          <xsl:text>Geographic Activities</xsl:text>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="."/>
        </xsl:otherwise>
      </xsl:choose>
    </orDesc>
  </xsl:template>
  <xsl:template match="OnFunctCd">
    <OnFunctCd>
      <xsl:if test="@value=001">
        <xsl:attribute name="value">
          <xsl:text>download</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=002">
        <xsl:attribute name="value">
          <xsl:text>information</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=003">
        <xsl:attribute name="value">
          <xsl:text>offlineAccess</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=004">
        <xsl:attribute name="value">
          <xsl:text>order</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=005">
        <xsl:attribute name="value">
          <xsl:text>search</xsl:text>
        </xsl:attribute>
      </xsl:if>
    </OnFunctCd>
  </xsl:template>
  <xsl:template match="PresFormCd">
    <PresFormCd>
      <xsl:if test="@value=001">
        <xsl:attribute name="value">
          <xsl:text>documentDigital</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=002">
        <xsl:attribute name="value">
          <xsl:text>documentHardcopy</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=003">
        <xsl:attribute name="value">
          <xsl:text>imageDigital</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=004">
        <xsl:attribute name="value">
          <xsl:text>imageHardcopy</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=005">
        <xsl:attribute name="value">
          <xsl:text>mapDigital</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=006">
        <xsl:attribute name="value">
          <xsl:text>mapHardcopy</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=007">
        <xsl:attribute name="value">
          <xsl:text>modelDigital</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=008">
        <xsl:attribute name="value">
          <xsl:text>modelHardcopy</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=009">
        <xsl:attribute name="value">
          <xsl:text>profileDigital</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=010">
        <xsl:attribute name="value">
          <xsl:text>profileHardcopy</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=011">
        <xsl:attribute name="value">
          <xsl:text>tableDigital</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=012">
        <xsl:attribute name="value">
          <xsl:text>tableHardcopy</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=013">
        <xsl:attribute name="value">
          <xsl:text>videoDigital</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=014">
        <xsl:attribute name="value">
          <xsl:text>videoHardcopy</xsl:text>
        </xsl:attribute>
      </xsl:if>
    </PresFormCd>
  </xsl:template>
  <xsl:template match="RoleCd">
    <RoleCd>
      <xsl:if test="@value=001">
        <xsl:attribute name="value">
          <xsl:text>resourceProvider</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=002">
        <xsl:attribute name="value">
          <xsl:text>custodian</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=003">
        <xsl:attribute name="value">
          <xsl:text>owner</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=004">
        <xsl:attribute name="value">
          <xsl:text>user</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=005">
        <xsl:attribute name="value">
          <xsl:text>distributor</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=006">
        <xsl:attribute name="value">
          <xsl:text>originator</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=007">
        <xsl:attribute name="value">
          <xsl:text>pointOfContact</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=008">
        <xsl:attribute name="value">
          <xsl:text>principalInvestigator</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=009">
        <xsl:attribute name="value">
          <xsl:text>processor</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=010">
        <xsl:attribute name="value">
          <xsl:text>publisher</xsl:text>
        </xsl:attribute>
      </xsl:if>
    </RoleCd>
  </xsl:template>
  <xsl:template match="EvalMethTypeCd">
    <EvalMethTypeCd>
      <xsl:if test="@value=001">
        <xsl:attribute name="value">
          <xsl:text>directInternal</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=002">
        <xsl:attribute name="value">
          <xsl:text>directExternal</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=003">
        <xsl:attribute name="value">
          <xsl:text>indirect</xsl:text>
        </xsl:attribute>
      </xsl:if>
    </EvalMethTypeCd>
  </xsl:template>
  <xsl:template match="AscTypeCd">
    <AscTypeCd>
      <xsl:if test="@value=001">
        <xsl:attribute name="value">
          <xsl:text>crossReference</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=002">
        <xsl:attribute name="value">
          <xsl:text>largerWorkCitation</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=003">
        <xsl:attribute name="value">
          <xsl:text>partOfSeamlessDatabase</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=004">
        <xsl:attribute name="value">
          <xsl:text>source</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=005">
        <xsl:attribute name="value">
          <xsl:text>stereomate</xsl:text>
        </xsl:attribute>
      </xsl:if>
    </AscTypeCd>
  </xsl:template>
  <xsl:template match="InitTypCd">
    <InitTypCd>
      <xsl:if test="@value=001">
        <xsl:attribute name="value">
          <xsl:text>campaign</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=002">
        <xsl:attribute name="value">
          <xsl:text>collection</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=003">
        <xsl:attribute name="value">
          <xsl:text>exercise</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=004">
        <xsl:attribute name="value">
          <xsl:text>experiment</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=005">
        <xsl:attribute name="value">
          <xsl:text>investigation</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=006">
        <xsl:attribute name="value">
          <xsl:text>mission</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=007">
        <xsl:attribute name="value">
          <xsl:text>nonImageSensor</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=008">
        <xsl:attribute name="value">
          <xsl:text>operation</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=009">
        <xsl:attribute name="value">
          <xsl:text>platform</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=010">
        <xsl:attribute name="value">
          <xsl:text>process</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=011">
        <xsl:attribute name="value">
          <xsl:text>program</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=012">
        <xsl:attribute name="value">
          <xsl:text>project</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=013">
        <xsl:attribute name="value">
          <xsl:text>study</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=014">
        <xsl:attribute name="value">
          <xsl:text>task</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=015">
        <xsl:attribute name="value">
          <xsl:text>trial</xsl:text>
        </xsl:attribute>
      </xsl:if>
    </InitTypCd>
  </xsl:template>
  <xsl:template match="CellGeoCd">
    <CellGeoCd>
      <xsl:if test="@value=001">
        <xsl:attribute name="value">
          <xsl:text>point</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=002">
        <xsl:attribute name="value">
          <xsl:text>area</xsl:text>
        </xsl:attribute>
      </xsl:if>
    </CellGeoCd>
  </xsl:template>
  <xsl:template match="CharSetCd">
    <CharSetCd>
      <xsl:if test="@value=001">
        <xsl:attribute name="value">
          <xsl:text>ucs2</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=002">
        <xsl:attribute name="value">
          <xsl:text>ucs4</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=003">
        <xsl:attribute name="value">
          <xsl:text>utf7</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=004">
        <xsl:attribute name="value">
          <xsl:text>utf8</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=005">
        <xsl:attribute name="value">
          <xsl:text>utf16</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=006">
        <xsl:attribute name="value">
          <xsl:text>8859part1</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=007">
        <xsl:attribute name="value">
          <xsl:text>8859part2</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=008">
        <xsl:attribute name="value">
          <xsl:text>8859part3</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=009">
        <xsl:attribute name="value">
          <xsl:text>8859part4</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=010">
        <xsl:attribute name="value">
          <xsl:text>8859part5</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=011">
        <xsl:attribute name="value">
          <xsl:text>8859part6</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=012">
        <xsl:attribute name="value">
          <xsl:text>8859part7</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=013">
        <xsl:attribute name="value">
          <xsl:text>8859part8</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=014">
        <xsl:attribute name="value">
          <xsl:text>8859part9</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=015">
        <xsl:attribute name="value">
          <xsl:text>8859part11</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=016">
        <xsl:attribute name="value">
          <xsl:text>8859part14</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=017">
        <xsl:attribute name="value">
          <xsl:text>8859part15</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=018">
        <xsl:attribute name="value">
          <xsl:text>jis</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=019">
        <xsl:attribute name="value">
          <xsl:text>shiftJIS</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=020">
        <xsl:attribute name="value">
          <xsl:text>eucJP</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=021">
        <xsl:attribute name="value">
          <xsl:text>usAscii</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=022">
        <xsl:attribute name="value">
          <xsl:text>ebcdic</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=023">
        <xsl:attribute name="value">
          <xsl:text>eucKR</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=024">
        <xsl:attribute name="value">
          <xsl:text>big5</xsl:text>
        </xsl:attribute>
      </xsl:if>
    </CharSetCd>
  </xsl:template>
  <xsl:template match="ClasscationCd">
    <ClasscationCd>
      <xsl:if test="@value=001">
        <xsl:attribute name="value">
          <xsl:text>unclassified</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=002">
        <xsl:attribute name="value">
          <xsl:text>restricted</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=003">
        <xsl:attribute name="value">
          <xsl:text>confidential</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=004">
        <xsl:attribute name="value">
          <xsl:text>secret</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=005">
        <xsl:attribute name="value">
          <xsl:text>topsecret</xsl:text>
        </xsl:attribute>
      </xsl:if>
    </ClasscationCd>
  </xsl:template>
  <xsl:template match="ContentTypCd">
    <ContentTypCd>
      <xsl:if test="@value=001">
        <xsl:attribute name="value">
          <xsl:text>image</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=002">
        <xsl:attribute name="value">
          <xsl:text>thematicClassification</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=003">
        <xsl:attribute name="value">
          <xsl:text>physicalMeasurement</xsl:text>
        </xsl:attribute>
      </xsl:if>
    </ContentTypCd>
  </xsl:template>
  <xsl:template match="DatatypeCd">
    <DatatypeCd>
      <xsl:if test="@value=001">
        <xsl:attribute name="value">
          <xsl:text>class</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=002">
        <xsl:attribute name="value">
          <xsl:text>codelist</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=003">
        <xsl:attribute name="value">
          <xsl:text>enumeration</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=004">
        <xsl:attribute name="value">
          <xsl:text>codelistElement</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=005">
        <xsl:attribute name="value">
          <xsl:text>abstractClass</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=006">
        <xsl:attribute name="value">
          <xsl:text>aggregateClass</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=007">
        <xsl:attribute name="value">
          <xsl:text>specifiedClass</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=008">
        <xsl:attribute name="value">
          <xsl:text>datatypeClass</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=009">
        <xsl:attribute name="value">
          <xsl:text>interfaceClass</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=010">
        <xsl:attribute name="value">
          <xsl:text>unionClass</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=011">
        <xsl:attribute name="value">
          <xsl:text>metaclass</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=012">
        <xsl:attribute name="value">
          <xsl:text>typeClass</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=013">
        <xsl:attribute name="value">
          <xsl:text>characterString</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=014">
        <xsl:attribute name="value">
          <xsl:text>integer</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=015">
        <xsl:attribute name="value">
          <xsl:text>association</xsl:text>
        </xsl:attribute>
      </xsl:if>
    </DatatypeCd>
  </xsl:template>
  <xsl:template match="DimNameTypCd">
    <DimNameTypCd>
      <xsl:if test="@value=001">
        <xsl:attribute name="value">
          <xsl:text>row</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=002">
        <xsl:attribute name="value">
          <xsl:text>column</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=003">
        <xsl:attribute name="value">
          <xsl:text>vertical</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=004">
        <xsl:attribute name="value">
          <xsl:text>track</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=005">
        <xsl:attribute name="value">
          <xsl:text>crossTrack</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=006">
        <xsl:attribute name="value">
          <xsl:text>line</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=007">
        <xsl:attribute name="value">
          <xsl:text>sample</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=008">
        <xsl:attribute name="value">
          <xsl:text>time</xsl:text>
        </xsl:attribute>
      </xsl:if>
    </DimNameTypCd>
  </xsl:template>
  <xsl:template match="GeoObjTypCd">
    <GeoObjTypCd>
      <xsl:if test="@value=001">
        <xsl:attribute name="value">
          <xsl:text>complexes</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=002">
        <xsl:attribute name="value">
          <xsl:text>composites</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=003">
        <xsl:attribute name="value">
          <xsl:text>curve</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=004">
        <xsl:attribute name="value">
          <xsl:text>point</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=005">
        <xsl:attribute name="value">
          <xsl:text>solid</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=006">
        <xsl:attribute name="value">
          <xsl:text>surface</xsl:text>
        </xsl:attribute>
      </xsl:if>
    </GeoObjTypCd>
  </xsl:template>
  <xsl:template match="ImgCondCd">
    <ImgCondCd>
      <xsl:if test="@value=001">
        <xsl:attribute name="value">
          <xsl:text>blurredImage</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=002">
        <xsl:attribute name="value">
          <xsl:text>cloud</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=003">
        <xsl:attribute name="value">
          <xsl:text>degradingObliquity</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=004">
        <xsl:attribute name="value">
          <xsl:text>fog</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=005">
        <xsl:attribute name="value">
          <xsl:text>heavySmokeOrDust</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=006">
        <xsl:attribute name="value">
          <xsl:text>night</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=007">
        <xsl:attribute name="value">
          <xsl:text>rain</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=008">
        <xsl:attribute name="value">
          <xsl:text>semiDarkness</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=009">
        <xsl:attribute name="value">
          <xsl:text>shadow</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=010">
        <xsl:attribute name="value">
          <xsl:text>snow</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=011">
        <xsl:attribute name="value">
          <xsl:text>terrainMasking</xsl:text>
        </xsl:attribute>
      </xsl:if>
    </ImgCondCd>
  </xsl:template>
  <xsl:template match="KeyTypCd">
    <KeyTypCd>
      <xsl:if test="@value=001">
        <xsl:attribute name="value">
          <xsl:text>discipline</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=002">
        <xsl:attribute name="value">
          <xsl:text>place</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=003">
        <xsl:attribute name="value">
          <xsl:text>stratum</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=004">
        <xsl:attribute name="value">
          <xsl:text>temporal</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=005">
        <xsl:attribute name="value">
          <xsl:text>theme</xsl:text>
        </xsl:attribute>
      </xsl:if>
    </KeyTypCd>
  </xsl:template>
  <xsl:template match="MaintFreqCd">
    <MaintFreqCd>
      <xsl:if test="@value=001">
        <xsl:attribute name="value">
          <xsl:text>continual</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=002">
        <xsl:attribute name="value">
          <xsl:text>daily</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=003">
        <xsl:attribute name="value">
          <xsl:text>weekly</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=004">
        <xsl:attribute name="value">
          <xsl:text>fortnightly</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=005">
        <xsl:attribute name="value">
          <xsl:text>monthly</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=006">
        <xsl:attribute name="value">
          <xsl:text>quarterly</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=007">
        <xsl:attribute name="value">
          <xsl:text>biannually</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=008">
        <xsl:attribute name="value">
          <xsl:text>annually</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=009">
        <xsl:attribute name="value">
          <xsl:text>asNeeded</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=010">
        <xsl:attribute name="value">
          <xsl:text>irregular</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=011">
        <xsl:attribute name="value">
          <xsl:text>notPlanned</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=998">
        <xsl:attribute name="value">
          <xsl:text>unknown</xsl:text>
        </xsl:attribute>
      </xsl:if>
    </MaintFreqCd>
  </xsl:template>
  <xsl:template match="MedFormCd">
    <MedFormCd>
      <xsl:if test="@value=001">
        <xsl:attribute name="value">
          <xsl:text>cpio</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=002">
        <xsl:attribute name="value">
          <xsl:text>tar</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=003">
        <xsl:attribute name="value">
          <xsl:text>highSierra</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=004">
        <xsl:attribute name="value">
          <xsl:text>iso9660</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=005">
        <xsl:attribute name="value">
          <xsl:text>iso9660RockRidge</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=006">
        <xsl:attribute name="value">
          <xsl:text>iso9660AppleHFS</xsl:text>
        </xsl:attribute>
      </xsl:if>
    </MedFormCd>
  </xsl:template>
  <xsl:template match="MedNameCd">
    <MedNameCd>
      <xsl:if test="@value=001">
        <xsl:attribute name="value">
          <xsl:text>cdRom</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=002">
        <xsl:attribute name="value">
          <xsl:text>dvd</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=003">
        <xsl:attribute name="value">
          <xsl:text>dvdRom</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=004">
        <xsl:attribute name="value">
          <xsl:text>3halfInchFloppy</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=005">
        <xsl:attribute name="value">
          <xsl:text>5quarterInchFloppy</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=006">
        <xsl:attribute name="value">
          <xsl:text>7trackTape</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=007">
        <xsl:attribute name="value">
          <xsl:text>9trackTape</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=008">
        <xsl:attribute name="value">
          <xsl:text>3480Cartridge</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=009">
        <xsl:attribute name="value">
          <xsl:text>3490Cartridge</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=010">
        <xsl:attribute name="value">
          <xsl:text>3580Cartridge</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=011">
        <xsl:attribute name="value">
          <xsl:text>4mmCartridgeTape</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=012">
        <xsl:attribute name="value">
          <xsl:text>8mmCartridgeTape</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=013">
        <xsl:attribute name="value">
          <xsl:text>1quarterInchCartridgeTape</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=014">
        <xsl:attribute name="value">
          <xsl:text>digitalLinearTape</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=015">
        <xsl:attribute name="value">
          <xsl:text>onLine</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=016">
        <xsl:attribute name="value">
          <xsl:text>satellite</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=017">
        <xsl:attribute name="value">
          <xsl:text>telephoneLink</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=018">
        <xsl:attribute name="value">
          <xsl:text>hardcopy</xsl:text>
        </xsl:attribute>
      </xsl:if>
    </MedNameCd>
  </xsl:template>
  <xsl:template match="ObCd">
    <ObCd>
      <xsl:if test="@value=001">
        <xsl:attribute name="value">
          <xsl:text>mandatory</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=002">
        <xsl:attribute name="value">
          <xsl:text>optional</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=003">
        <xsl:attribute name="value">
          <xsl:text>conditional</xsl:text>
        </xsl:attribute>
      </xsl:if>
    </ObCd>
  </xsl:template>
  <xsl:template match="PixOrientCd">
    <PixOrientCd>
      <xsl:if test="@value=001">
        <xsl:attribute name="value">
          <xsl:text>center</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=002">
        <xsl:attribute name="value">
          <xsl:text>lowerLeft</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=003">
        <xsl:attribute name="value">
          <xsl:text>lowerRight</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=004">
        <xsl:attribute name="value">
          <xsl:text>upperRight</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=005">
        <xsl:attribute name="value">
          <xsl:text>upperLeft</xsl:text>
        </xsl:attribute>
      </xsl:if>
    </PixOrientCd>
  </xsl:template>
  <xsl:template match="ProgCd">
    <ProgCd>
      <xsl:if test="@value=001">
        <xsl:attribute name="value">
          <xsl:text>completed</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=002">
        <xsl:attribute name="value">
          <xsl:text>historicalArchive</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=003">
        <xsl:attribute name="value">
          <xsl:text>obsolete</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=004">
        <xsl:attribute name="value">
          <xsl:text>onGoing</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=005">
        <xsl:attribute name="value">
          <xsl:text>planned</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=006">
        <xsl:attribute name="value">
          <xsl:text>required</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=007">
        <xsl:attribute name="value">
          <xsl:text>underdevelopment</xsl:text>
        </xsl:attribute>
      </xsl:if>
    </ProgCd>
  </xsl:template>
  <xsl:template match="RestrictCd">
    <RestrictCd>
      <xsl:if test="@value=001">
        <xsl:attribute name="value">
          <xsl:text>copyright</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=002">
        <xsl:attribute name="value">
          <xsl:text>patent</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=003">
        <xsl:attribute name="value">
          <xsl:text>patentPending</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=004">
        <xsl:attribute name="value">
          <xsl:text>trademark</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=005">
        <xsl:attribute name="value">
          <xsl:text>license</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=006">
        <xsl:attribute name="value">
          <xsl:text>intellectualPropertyRights</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=007">
        <xsl:attribute name="value">
          <xsl:text>restricted</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=008">
        <xsl:attribute name="value">
          <xsl:text>otherRestictions</xsl:text>
        </xsl:attribute>
      </xsl:if>
    </RestrictCd>
  </xsl:template>
  <xsl:template match="ScopeCd">
    <ScopeCd>
      <xsl:if test="@value=001">
        <xsl:attribute name="value">
          <xsl:text>attribute</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=002">
        <xsl:attribute name="value">
          <xsl:text>attributeType</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=003">
        <xsl:attribute name="value">
          <xsl:text>collectionHardware</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=004">
        <xsl:attribute name="value">
          <xsl:text>collectionSession</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=005">
        <xsl:attribute name="value">
          <xsl:text>dataset</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=006">
        <xsl:attribute name="value">
          <xsl:text>series</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=007">
        <xsl:attribute name="value">
          <xsl:text>nonGeographicDataset</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=008">
        <xsl:attribute name="value">
          <xsl:text>dimensionGroup</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=009">
        <xsl:attribute name="value">
          <xsl:text>feature</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=010">
        <xsl:attribute name="value">
          <xsl:text>featureType</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=011">
        <xsl:attribute name="value">
          <xsl:text>propertyType</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=012">
        <xsl:attribute name="value">
          <xsl:text>fieldSession</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=013">
        <xsl:attribute name="value">
          <xsl:text>software</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=014">
        <xsl:attribute name="value">
          <xsl:text>service</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=015">
        <xsl:attribute name="value">
          <xsl:text>model</xsl:text>
        </xsl:attribute>
      </xsl:if>
    </ScopeCd>
  </xsl:template>
  <xsl:template match="SpatRepTypCd">
    <SpatRepTypCd>
      <xsl:if test="@value=001">
        <xsl:attribute name="value">
          <xsl:text>vector</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=002">
        <xsl:attribute name="value">
          <xsl:text>grid</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=003">
        <xsl:attribute name="value">
          <xsl:text>textTable</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=004">
        <xsl:attribute name="value">
          <xsl:text>tin</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=005">
        <xsl:attribute name="value">
          <xsl:text>stereoModel</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=006">
        <xsl:attribute name="value">
          <xsl:text>video</xsl:text>
        </xsl:attribute>
      </xsl:if>
    </SpatRepTypCd>
  </xsl:template>
  <xsl:template match="TopicCatCd">
    <TopicCatCd>
      <xsl:if test="@value=001">
        <xsl:attribute name="value">
          <xsl:text>farming</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=002">
        <xsl:attribute name="value">
          <xsl:text>biota</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=003">
        <xsl:attribute name="value">
          <xsl:text>boundaries</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=004">
        <xsl:attribute name="value">
          <xsl:text>climatologyMeteorologyAtmosphere</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=005">
        <xsl:attribute name="value">
          <xsl:text>economy</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=006">
        <xsl:attribute name="value">
          <xsl:text>elevation</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=007">
        <xsl:attribute name="value">
          <xsl:text>environment</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=008">
        <xsl:attribute name="value">
          <xsl:text>geoscientificInformation</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=009">
        <xsl:attribute name="value">
          <xsl:text>health</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=010">
        <xsl:attribute name="value">
          <xsl:text>imageryBaseMapsEarthCover</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=011">
        <xsl:attribute name="value">
          <xsl:text>intelligenceMilitary</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=012">
        <xsl:attribute name="value">
          <xsl:text>inlandWaters</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=013">
        <xsl:attribute name="value">
          <xsl:text>location</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=014">
        <xsl:attribute name="value">
          <xsl:text>oceans</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=015">
        <xsl:attribute name="value">
          <xsl:text>planningCadastre</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=016">
        <xsl:attribute name="value">
          <xsl:text>society</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=017">
        <xsl:attribute name="value">
          <xsl:text>structure</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=018">
        <xsl:attribute name="value">
          <xsl:text>transportation</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=019">
        <xsl:attribute name="value">
          <xsl:text>utilitiesCommunication</xsl:text>
        </xsl:attribute>
      </xsl:if>
    </TopicCatCd>
  </xsl:template>
  <xsl:template match="TopoLevCd">
    <TopoLevCd>
      <xsl:if test="@value=001">
        <xsl:attribute name="value">
          <xsl:text>geometryOnly</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=002">
        <xsl:attribute name="value">
          <xsl:text>topology1D</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=003">
        <xsl:attribute name="value">
          <xsl:text>planarGraph</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=004">
        <xsl:attribute name="value">
          <xsl:text>fullPlanarGraph</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=005">
        <xsl:attribute name="value">
          <xsl:text>surfaceGraph</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=006">
        <xsl:attribute name="value">
          <xsl:text>fullSurfaceGraph</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=007">
        <xsl:attribute name="value">
          <xsl:text>topology3D</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=008">
        <xsl:attribute name="value">
          <xsl:text>fullTopology3D</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="@value=009">
        <xsl:attribute name="value">
          <xsl:text>abstract</xsl:text>
        </xsl:attribute>
      </xsl:if>
    </TopoLevCd>
  </xsl:template>
</xsl:stylesheet>
