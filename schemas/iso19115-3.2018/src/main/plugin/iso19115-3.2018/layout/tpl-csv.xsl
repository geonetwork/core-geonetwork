<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:srv="http://standards.iso.org/iso/19115/-3/srv/2.0"
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
      <title>
        <xsl:apply-templates mode="localised"
                             select="mdb:identificationInfo/*/mri:citation/*/cit:title">
          <xsl:with-param name="langId" select="$langId"/>
        </xsl:apply-templates>
      </title>
      <abstract>
        <xsl:apply-templates mode="localised" select="mdb:identificationInfo/*/mri:abstract">
          <xsl:with-param name="langId" select="$langId"/>
        </xsl:apply-templates>
      </abstract>

      <xsl:for-each select="mdb:identificationInfo/*/mri:citation/*/cit:identifier/*/mcc:code/*[. != '']">
        <resourceIdentifier>
          <xsl:value-of select="."/>
        </resourceIdentifier>
      </xsl:for-each>

      <category>
        <xsl:value-of select="mdb:metadataScope/*/mdb:resourceScope/*/@codeListValue"/>
      </category>
      <metadatacreationdate>
        <xsl:value-of select="mdb:dateInfo/*/cit:date/*"/>
      </metadatacreationdate>

      <xsl:for-each select="mdb:identificationInfo/*/mri:citation/*/cit:date">
        <xsl:element name="date-{*/cit:dateType/*/@codeListValue}">
          <xsl:value-of select="*/cit:date/*/text()"/>
        </xsl:element>
      </xsl:for-each>

      <xsl:for-each select="mdb:identificationInfo/*/mri:graphicOverview/*/mcc:fileName">
        <image>
          <xsl:value-of select="*/text()"/>
        </image>
      </xsl:for-each>

      <!-- All keywords not having thesaurus reference -->
      <xsl:for-each select="mdb:identificationInfo/*/mri:descriptiveKeywords/*[not(mri:thesaurusName)]/mri:keyword[not(@gco:nilReason)]">
        <keyword>
          <xsl:apply-templates mode="localised" select=".">
            <xsl:with-param name="langId" select="$langId"/>
          </xsl:apply-templates>
        </keyword>
      </xsl:for-each>

      <!-- One column per thesaurus -->
      <xsl:for-each select="mdb:identificationInfo/*/mri:descriptiveKeywords/*[mri:thesaurusName]">
        <xsl:variable name="thesaurusId" select="mri:thesaurusName/*/cit:identifier/*/mcc:code/*/text()"/>
        <xsl:variable name="thesaurusKey" select="if ($thesaurusId != '') then replace($thesaurusId, '[^a-zA-Z0-9]', '') else position()"/>

        <xsl:for-each select="mri:keyword[not(@gco:nilReason)]">
          <xsl:element name="keyword-{$thesaurusKey}">
            <xsl:apply-templates mode="localised" select=".">
              <xsl:with-param name="langId" select="$langId"/>
            </xsl:apply-templates>
          </xsl:element>
        </xsl:for-each>
      </xsl:for-each>

      <!-- One column per contact role -->
      <xsl:for-each select="mdb:identificationInfo/*/mri:pointOfContact">
        <xsl:variable name="key" select="*/cit:role/*/@codeListValue"/>

        <xsl:element name="contact-{$key}">
          <xsl:apply-templates mode="localised" select="*/cit:party/*/cit:name">
            <xsl:with-param name="langId" select="$langId"/>
          </xsl:apply-templates>/
          <xsl:apply-templates mode="localised" select="*/cit:contactInfo/*/cit:onlineResource/*/cit:linkage">
            <xsl:with-param name="langId" select="$langId"/>
          </xsl:apply-templates>
        </xsl:element>
      </xsl:for-each>

      <xsl:for-each select="mdb:identificationInfo/*//gex:EX_GeographicBoundingBox">
        <geoBox>
          <westBL>
            <xsl:value-of select="gex:westBoundLongitude"/>
          </westBL>
          <eastBL>
            <xsl:value-of select="gex:eastBoundLongitude"/>
          </eastBL>
          <southBL>
            <xsl:value-of select="gex:southBoundLatitude"/>
          </southBL>
          <northBL>
            <xsl:value-of select="gex:northBoundLatitude"/>
          </northBL>
        </geoBox>
      </xsl:for-each>

      <xsl:for-each select="mdb:identificationInfo/*/*/mco:MD_Constraints/*">
        <Constraints>
          <xsl:copy-of select="."/>
        </Constraints>
      </xsl:for-each>

      <xsl:for-each select="mdb:identificationInfo/*/*/mco:MD_SecurityConstraints/*">
        <SecurityConstraints>
          <xsl:copy-of select="."/>
        </SecurityConstraints>
      </xsl:for-each>

      <xsl:for-each select="mdb:identificationInfo/*/*/mco:MD_LegalConstraints/*">
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
      </xsl:for-each>

      <!-- Responsivness / MedSea specific -->
      <xsl:for-each
        select="mdb:dataQualityInfo/*/mdq:report/*[
                  mdq:measure/*/mdq:nameOfMeasure/gco:CharacterString = 'Responsiveness']">
        <responsiveness>
          <xsl:value-of select="mdq:result/*/mdq:value/gco:Record"/>
        </responsiveness>
      </xsl:for-each>

      <xsl:copy-of select="gn:info"/>
    </metadata>
  </xsl:template>
</xsl:stylesheet>
