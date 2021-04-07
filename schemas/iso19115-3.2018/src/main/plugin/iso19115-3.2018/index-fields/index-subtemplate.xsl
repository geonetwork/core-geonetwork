<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet version="2.0"
                xmlns:cit="http://standards.iso.org/iso/19115/-3/cit/2.0"
                xmlns:dqm="http://standards.iso.org/iso/19157/-2/dqm/1.0"
                xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
                xmlns:lan="http://standards.iso.org/iso/19115/-3/lan/1.0"
                xmlns:mcc="http://standards.iso.org/iso/19115/-3/mcc/1.0"
                xmlns:mrc="http://standards.iso.org/iso/19115/-3/mrc/2.0"
                xmlns:mco="http://standards.iso.org/iso/19115/-3/mco/1.0"
                xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0"
                xmlns:mri="http://standards.iso.org/iso/19115/-3/mri/1.0"
                xmlns:mrs="http://standards.iso.org/iso/19115/-3/mrs/1.0"
                xmlns:mrl="http://standards.iso.org/iso/19115/-3/mrl/2.0"
                xmlns:mrd="http://standards.iso.org/iso/19115/-3/mrd/1.0"
                xmlns:gml="http://www.opengis.net/gml/3.2"
                xmlns:srv="http://standards.iso.org/iso/19115/-3/srv/2.1"
                xmlns:gcx="http://standards.iso.org/iso/19115/-3/gcx/1.0"
                xmlns:gex="http://standards.iso.org/iso/19115/-3/gex/1.0"
                xmlns:mdq="http://standards.iso.org/iso/19157/-2/mdq/1.0"
                xmlns:geonet="http://www.fao.org/geonetwork"
                xmlns:util="java:org.fao.geonet.util.XslUtil"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:skos="http://www.w3.org/2004/02/skos/core#">

  <!-- Subtemplate indexing

  Add the [count(ancestor::node()) =  1] to only match element at the root of the document.
  This is the method to identify a subtemplate.
  -->
  <xsl:param name="id"/>
  <xsl:param name="uuid"/>
  <xsl:param name="title"/>


  <xsl:variable name="isMultilingual" select="count(distinct-values(*//lan:LocalisedCharacterString/@locale)) > 0"/>

  <!-- Subtemplate indexing -->
  <xsl:template match="/">
    <xsl:variable name="root" select="/"/>
    <xsl:variable name="isoDocLangId" select="util:getLanguage()"></xsl:variable>

    <Documents>

      <xsl:choose>
        <xsl:when test="$isMultilingual">
          <xsl:for-each select="distinct-values(//lan:LocalisedCharacterString/@locale)">
            <xsl:variable name="locale" select="string(.)"/>
            <xsl:variable name="langId" select="substring($locale,2,2)"/>
            <xsl:variable name="isoLangId" select="util:threeCharLangCode($langId)"/>

            <Document locale="{$isoLangId}">
              <Field name="_locale" string="{$isoLangId}" store="true" index="true"/>
              <Field name="_docLocale" string="{$isoDocLangId}" store="true" index="true"/>
              <xsl:apply-templates mode="index" select="$root">
                <xsl:with-param name="locale" select="$locale"/>
                <xsl:with-param name="isoLangId" select="$isoLangId"/>
                <xsl:with-param name="langId" select="$langId"></xsl:with-param>
              </xsl:apply-templates>
            </Document>
          </xsl:for-each>
        </xsl:when>
        <xsl:otherwise>
          <Document locale="">
            <xsl:apply-templates mode="index" select="$root"/>
          </Document>
        </xsl:otherwise>
      </xsl:choose>
    </Documents>
  </xsl:template>





  <xsl:template mode="index"
                match="cit:CI_Responsibility[count(ancestor::node()) =  1]">

    <xsl:variable name="org"
                  select="normalize-space(cit:party/cit:CI_Organisation/cit:name/gco:CharacterString)"/>
    <xsl:variable name="name"
                  select="string-join(.//cit:individual/cit:CI_Individual/cit:name/gco:CharacterString, ', ')"/>

    <xsl:variable name="mail"
                  select="string-join(.//cit:CI_Address/cit:electronicMailAddress[1]/gco:CharacterString, ', ')"/>

    <Field name="_title"
           string="{if ($title != '') then $title
                    else if ($name != '') then concat($org, ' (', $name, ')')
                    else if ($mail != '') then concat($org, ' (', $mail, ')')
                    else $org}"
           store="true" index="true"/>

    <Field name="orgName" string="{$org}" store="true" index="true"/>

    <xsl:call-template name="subtemplate-common-fields"/>
  </xsl:template>

  <xsl:template mode="index" match="cit:CI_Organisation">

    <xsl:variable name="org" select="normalize-space(cit:name/gco:CharacterString)"/>
    <xsl:variable name="name" select="string-join(.//cit:individual/cit:CI_Individual/cit:name/gco:CharacterString, ', ')"/>

    <xsl:variable name="mail" select="string-join(.//cit:CI_Address/cit:electronicMailAddress[1]/gco:CharacterString, ', ')"/>

    <Field name="_title" string="{concat($name,' @ ',$org)}" store="true" index="true"/>

    <Field name="personOrganisation" string="{concat($name,' @ ',$org)}" store="true" index="true"/>
    <xsl:call-template name="subtemplate-common-fields"/>
  </xsl:template>

  <xsl:template mode="index"
                match="mcc:MD_BrowseGraphic[count(ancestor::node()) =  1]">

    <xsl:variable name="fileName"
                  select="normalize-space(mcc:fileName/gco:CharacterString)"/>
    <xsl:variable name="fileDescription"
                  select="normalize-space(mcc:fileDescription/gco:CharacterString)"/>
    <Field name="_title"
           string="{if ($fileDescription != '')
                    then $fileDescription
                    else $fileName}"
           store="true" index="true"/>
    <xsl:call-template name="subtemplate-common-fields"/>
  </xsl:template>


  <!-- Indexing DQ report -->
  <xsl:template mode="index"
                match="mdq:*[count(ancestor::node()) =  1]">

    <xsl:variable name="type" select="local-name(.)"/>
    <xsl:variable name="name"
                  select="string-join(.//mdq:nameOfMeasure/gco:CharacterString, ', ')"/>
    <Field name="_title"
           string="{if ($name != '')
                    then concat($type, ' / ', $name, '')
                    else $type}"
           store="true" index="true"/>

    <xsl:call-template name="subtemplate-common-fields"/>
  </xsl:template>


  <!-- Indexing constraints -->
  <xsl:template mode="index"
                match="mco:MD_Constraints[count(ancestor::node()) =  1 and mco:reference/cit:CI_Citation/cit:title/gco:CharacterString]|
                        mco:MD_LegalConstraints[count(ancestor::node()) =  1 and mco:reference/cit:CI_Citation/cit:title/gco:CharacterString]|
                        mco:MD_SecurityConstraints[count(ancestor::node()) =  1 and mco:reference/cit:CI_Citation/cit:title/gco:CharacterString]">

    <xsl:variable name="type" select="local-name(.)"/>
    <xsl:variable name="name"
                  select="string-join(mco:reference/cit:CI_Citation/cit:title/gco:CharacterString, ', ')"/>
    <Field name="_title"
           string="{if ($name != '')
                    then $name
                    else $type}"
           store="true" index="true"/>

    <xsl:call-template name="subtemplate-common-fields"/>
  </xsl:template>



  <xsl:template name="subtemplate-common-fields">
    <Field name="any" string="{normalize-space(string(.))}" store="false" index="true"/>
    <Field name="_root" string="{name(.)}" store="true" index="true"/>
  </xsl:template>

</xsl:stylesheet>
