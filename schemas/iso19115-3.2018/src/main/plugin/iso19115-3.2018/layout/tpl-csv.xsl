<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:srv="http://standards.iso.org/iso/19115/-3/srv/2.1"
                xmlns:mds="http://standards.iso.org/iso/19115/-3/mds/2.0"
                xmlns:mcc="http://standards.iso.org/iso/19115/-3/mcc/1.0"
                xmlns:mri="http://standards.iso.org/iso/19115/-3/mri/1.0"
                xmlns:mrs="http://standards.iso.org/iso/19115/-3/mrs/1.0"
                xmlns:mrd="http://standards.iso.org/iso/19115/-3/mrd/1.0"
                xmlns:mco="http://standards.iso.org/iso/19115/-3/mco/1.0"
                xmlns:msr="http://standards.iso.org/iso/19115/-3/msr/2.0"
                xmlns:lan="http://standards.iso.org/iso/19115/-3/lan/1.0"
                xmlns:gcx="http://standards.iso.org/iso/19115/-3/gcx/1.0"
                xmlns:mdq="http://standards.iso.org/iso/19157/-2/mdq/1.0"
                xmlns:gex="http://standards.iso.org/iso/19115/-3/gex/1.0"
                xmlns:dqm="http://standards.iso.org/iso/19157/-2/dqm/1.0"
                xmlns:cit="http://standards.iso.org/iso/19115/-3/cit/2.0"
                xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0"
                xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
                xmlns:mmi="http://standards.iso.org/iso/19115/-3/mmi/1.0"
                xmlns:gml="http://www.opengis.net/gml"
                xmlns:gn="http://www.fao.org/geonetwork"
                xmlns:gn-fn-core="http://geonetwork-opensource.org/xsl/functions/core"
                xmlns:gn-fn-iso19115-3.2018="http://geonetwork-opensource.org/xsl/functions/profiles/iso19115-3.2018"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                exclude-result-prefixes="#all" version="2.0">

  <xsl:import href="utility-fn.xsl"/>
  <xsl:import href="utility-tpl.xsl"/>

  <xsl:template mode="csv" match="mdb:MD_Metadata|*[@gco:isoType='mdb:MD_Metadata']"
                priority="2">
    <metadata>

      <xsl:variable name="langId" select="gn-fn-iso19115-3.2018:getLangId(., $lang)"/>

      <Identifier>
        <xsl:value-of select="gn:info/uuid"/>
      </Identifier>
      <Title>
        <xsl:apply-templates mode="localised"
                             select="mdb:identificationInfo/*/mri:citation/*/cit:title">
          <xsl:with-param name="langId" select="$langId"/>
        </xsl:apply-templates>
      </Title>



      <!--<id>
        <xsl:value-of select="gn:info/id"/>
      </id>
      <uuid>
        <xsl:value-of select="gn:info/uuid"/>
      </uuid>
      <title>
        <xsl:apply-templates mode="localised"
                             select="mdb:identificationInfo/*/mri:citation/*/cit:title">
          <xsl:with-param name="langId" select="$langId"/>
        </xsl:apply-templates>
      </title>-->
      <Discription>
        <xsl:apply-templates mode="localised" select="mdb:identificationInfo/*/mri:abstract">
          <xsl:with-param name="langId" select="$langId"/>
        </xsl:apply-templates>
      </Discription>

      <Point_of_Contact>
        <xsl:value-of select="mdb:contact/cit:CI_Responsibility/cit:party/*/cit:contactInfo/*/cit:address/*/cit:electronicMailAddress"/>
      </Point_of_Contact>

      <xsl:variable name="classification" select="mdb:identificationInfo/*/mri:resourceConstraints/mco:MD_SecurityConstraints/mco:useLimitation/*"/>
      <Access_Rights>
        <xsl:choose>
          <xsl:when test="$classification=('PROTECTED', 'SECRET', 'TOP SECRET')">Restricted</xsl:when>
          <xsl:when test="$classification=('OFFICIAL', 'OFFICIAL: Sensitive')">Conditional</xsl:when>
          <xsl:otherwise>Open</xsl:otherwise>
        </xsl:choose>
      </Access_Rights>
      <Security_Constraints>
        <xsl:value-of select="$classification"/>
      </Security_Constraints>

      <xsl:for-each select="mdb:identificationInfo/*/mri:pointOfContact/*[cit:role/cit:CI_RoleCode/codeListValue='pointOfContact']">
        <Data_Custodian>
          <xsl:value-of select="mdb:identificationInfo/*/mri:pointOfContact/*/cit:party/*/cit:name"/>
        </Data_Custodian>
      </xsl:for-each>

      <xsl:for-each select="mdb:identificationInfo/*/mri:descriptiveKeywords/*/mri:keyword[not(@gco:nilReason)]">
        <Keyword>
          <xsl:apply-templates mode="localised" select=".">
            <xsl:with-param name="langId" select="$langId"/>
          </xsl:apply-templates>
        </Keyword>
      </xsl:for-each>

     <!-- <xsl:for-each select="mdb:identificationInfo/*/mri:citation/*/cit:identifier/*/mcc:code/*[. != '']">
        <resourceIdentifier>
          <xsl:value-of select="."/>
        </resourceIdentifier>
      </xsl:for-each>-->

      <Resource_Type>
        <xsl:value-of select="mdb:metadataScope/*/mdb:resourceScope/*/@codeListValue"/>
      </Resource_Type>

      <xsl:for-each select="mdb:dateInfo/*[cit:dateType/cit:CI_DateTypeCode/@codeListValue='revision']">
        <Date_Modified>
          <xsl:value-of select="cit:date/*"/>
        </Date_Modified>
      </xsl:for-each>

      <xsl:for-each select="mdb:identificationInfo/*/mri:citation/*/cit:date/cit:CI_Date[cit:dateType/cit:CI_DateTypeCode/@codeListValue='publication']">
        <Date_Published>
          <xsl:value-of select="cit:date/*"/>
        </Date_Published>
      </xsl:for-each>
      <!--<xsl:for-each select="mdb:identificationInfo/*/mri:citation/*/cit:date">
        <xsl:element name="date-{*/cit:dateType/*/@codeListValue}">
          <xsl:value-of select="*/cit:date/*/text()"/>
        </xsl:element>
      </xsl:for-each>-->

      <!--<xsl:for-each select="mdb:identificationInfo/*/mri:graphicOverview/*/mcc:fileName">
        <image>
          <xsl:value-of select="*/text()"/>
        </image>
      </xsl:for-each>-->

      <!-- All keywords not having thesaurus reference -->
      <!--<xsl:for-each select="mdb:identificationInfo/*/mri:descriptiveKeywords/*[not(mri:thesaurusName)]/mri:keyword[not(@gco:nilReason)]">
        <keyword>
          <xsl:apply-templates mode="localised" select=".">
            <xsl:with-param name="langId" select="$langId"/>
          </xsl:apply-templates>
        </keyword>
      </xsl:for-each>-->

      <!-- One column per thesaurus -->
      <!--<xsl:for-each select="mdb:identificationInfo/*/mri:descriptiveKeywords/*[mri:thesaurusName]">
        <xsl:variable name="thesaurusId" select="mri:thesaurusName/*/cit:identifier/*/mcc:code/*/text()"/>
        <xsl:variable name="thesaurusKey" select="if ($thesaurusId != '') then replace($thesaurusId, '[^a-zA-Z0-9]', '') else position()"/>

        <xsl:for-each select="mri:keyword[not(@gco:nilReason)]">
          <xsl:element name="keyword-{$thesaurusKey}">
            <xsl:apply-templates mode="localised" select=".">
              <xsl:with-param name="langId" select="$langId"/>
            </xsl:apply-templates>
          </xsl:element>
        </xsl:for-each>
      </xsl:for-each>-->

      <!-- One column per contact role -->
      <!--<xsl:for-each select="mdb:identificationInfo/*/mri:pointOfContact">
        <xsl:variable name="key" select="*/cit:role/*/@codeListValue"/>

        <xsl:element name="contact-{$key}">
          <xsl:apply-templates mode="localised" select="*/cit:party/*/cit:name">
            <xsl:with-param name="langId" select="$langId"/>
          </xsl:apply-templates>/
          <xsl:apply-templates mode="localised" select="*/cit:contactInfo/*/cit:onlineResource/*/cit:linkage">
            <xsl:with-param name="langId" select="$langId"/>
          </xsl:apply-templates>
        </xsl:element>
      </xsl:for-each>-->

      <xsl:for-each select="mdb:identificationInfo/*//gex:EX_GeographicBoundingBox">
        <GeoBox>
          <SouthBL>
            <xsl:value-of select="concat(gex:southBoundLatitude, ', ')"/>
          </SouthBL>
          <WestBL>
            <xsl:value-of select="concat(gex:westBoundLongitude, ', ')"/>
          </WestBL>
          <NorthBL>
            <xsl:value-of select="concat(gex:northBoundLatitude, ', ')"/>
          </NorthBL>
          <EastBL>
            <xsl:value-of select="gex:eastBoundLongitude"/>
          </EastBL>
        </GeoBox>
      </xsl:for-each>

      <xsl:for-each select="mdb:identificationInfo/*//gex:EX_TemporalExtent/gex:extent">
      <Temporal_coverage_from>
        <xsl:value-of select="*/*[1]"/>
      </Temporal_coverage_from>

      <Temporal_coverage_to>
        <xsl:value-of select="*/*[2]"/>
      </Temporal_coverage_to>
      </xsl:for-each>

      <Update_Frequency>
        <xsl:value-of select="mdb:identificationInfo/*/mri:resourceMaintenance/mmi:MD_MaintenanceInformation/mmi:maintenanceAndUpdateFrequency/mmi:MD_MaintenanceFrequencyCode/@codeListValue"/>
      </Update_Frequency>

      <Purpose>
        <xsl:value-of select="mdb:identificationInfo/*/mri:purpose"/>
      </Purpose>

      <xsl:for-each select="mdb:identificationInfo/*/mri:descriptiveKeywords/*[mri:type/mri:MD_KeywordTypeCode/@codeListValue='place']">
        <Location>
          <xsl:value-of select="mri:keyword"/>
        </Location>
      </xsl:for-each>

      <Access_URL>
        <xsl:value-of select="mdb:metadataLinkage/cit:CI_OnlineResource/cit:linkage/*"/>
      </Access_URL>

      <License>
        <xsl:value-of select="mdb:identificationInfo/*/mri:resourceConstraints/mco:MD_LegalConstraints/mco:reference/*/cit:onlineResource/*/cit:linkage/*"/>
      </License>

      <Sensitive_Data>

      </Sensitive_Data>

      <Legal_Authority>

      </Legal_Authority>

      <Disposal>
        <xsl:for-each select="mdb:identificationInfo/*/mri:resourceConstraints/mco:MD_Constraints/mco:useLimitation/*">
          <xsl:if test="starts-with(., 'Disposal')">
            <xsl:value-of select="."/>
          </xsl:if>
        </xsl:for-each>

      </Disposal>

      <!--<xsl:for-each select="mdb:identificationInfo/*/*/mco:MD_Constraints/*">
        <Constraints>
          <xsl:copy-of select="."/>
        </Constraints>
      </xsl:for-each>

      <xsl:for-each select="mdb:identificationInfo/*/*/mco:MD_SecurityConstraints/*">
        <SecurityConstraints>
          <xsl:copy-of select="."/>
        </SecurityConstraints>
      </xsl:for-each>-->

      <!--<xsl:for-each select="mdb:identificationInfo/*/*/mco:MD_LegalConstraints/*">
        <LegalConstraints>
          <xsl:value-of select="*/text()|*/@codeListValue"/>
        </LegalConstraints>
      </xsl:for-each>

      <xsl:for-each select="mdb:distributionInfo//cit:linkage">
        <link>
          <xsl:value-of select="*/text()"/>
        </link>
      </xsl:for-each>

      <xsl:for-each select="mdb:identificationInfo/*/mri:pointOfContact">
        <xsl:variable name="email"
                      select="*/cit:party/*/cit:contactInfo/*/cit:address/*/cit:electronicMailAddress/gco:CharacterString"/>
        <contact>
          <xsl:value-of select="*/cit:party/*/cit:name/gco:CharacterString"/>
          <xsl:if test="$email">
          (<xsl:value-of select="$email"/>)
          </xsl:if>
        </contact>
      </xsl:for-each>-->

      <!-- Responsivness / MedSea specific -->
      <!--<xsl:for-each
        select="mdb:dataQualityInfo/*/mdq:report/*[
                  mdq:measure/*/mdq:nameOfMeasure/gco:CharacterString = 'Responsiveness']">
        <responsiveness>
          <xsl:value-of select="mdq:result/*/mdq:value/gco:Record"/>
        </responsiveness>
      </xsl:for-each>-->

      <xsl:copy-of select="gn:info"/>
    </metadata>
  </xsl:template>
</xsl:stylesheet>
