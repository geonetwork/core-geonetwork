<?xml version="1.0" encoding="UTF-8" ?>
<!--
  ~ Copyright (C) 2001-2016 Food and Agriculture Organization of the
  ~ United Nations (FAO-UN), United Nations World Food Programme (WFP)
  ~ and United Nations Environment Programme (UNEP)
  ~
  ~ This program is free software; you can redistribute it and/or modify
  ~ it under the terms of the GNU General Public License as published by
  ~ the Free Software Foundation; either version 2 of the License, or (at
  ~ your option) any later version.
  ~
  ~ This program is distributed in the hope that it will be useful, but
  ~ WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  ~ General Public License for more details.
  ~
  ~ You should have received a copy of the GNU General Public License
  ~ along with this program; if not, write to the Free Software
  ~ Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
  ~
  ~ Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
  ~ Rome - Italy. email: geonetwork@osgeo.org
  -->

<xsl:stylesheet xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:util="java:org.fao.geonet.util.XslUtil"
                version="2.0"
>
    <xsl:param name="id"/>
    <xsl:param name="uuid"/>
    <xsl:param name="title"/>


    <xsl:variable name="isMultilingual" select="count(distinct-values(*//gmd:LocalisedCharacterString/@locale)) > 0"/>

    <!-- Subtemplate indexing -->
    <xsl:template match="/">
        <xsl:variable name="root" select="/"/>
        <xsl:variable name="isoDocLangId" select="util:getLanguage()"></xsl:variable>

        <Documents>

            <xsl:choose>
                <xsl:when test="$isMultilingual">
                    <xsl:for-each select="distinct-values(//gmd:LocalisedCharacterString/@locale)">
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
                        <Field name="_docLocale" string="{$isoDocLangId}" store="true" index="true"/>
                        <xsl:apply-templates mode="index" select="$root">
                        </xsl:apply-templates>
                    </Document>
                </xsl:otherwise>
            </xsl:choose>
        </Documents>
    </xsl:template>

    <!--Contacts & Organisations-->
    <xsl:template mode="index" match="gmd:CI_ResponsibleParty[count(ancestor::node()) =  1]|*[@gco:isoType='gmd:CI_ResponsibleParty'][count(ancestor::node()) = 1]">
        <xsl:param name="isoLangId"/>
        <xsl:param name="langId"/>
        <xsl:param name="locale"/>

        <xsl:choose>
            <xsl:when test="$isMultilingual">
                <xsl:variable name="org">
                  <xsl:choose>
                    <xsl:when test="normalize-space(gmd:organisationName/gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString[@locale = $locale]) != ''">
                      <xsl:copy-of select="normalize-space(gmd:organisationName/gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString[@locale = $locale])"/>
                    </xsl:when>
                    <xsl:otherwise>
                      <xsl:copy-of select="normalize-space((gmd:organisationName/gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString[./text()!=''])[1])"/>
                    </xsl:otherwise>
                  </xsl:choose>
                </xsl:variable>
                <xsl:variable name="name"
                              select="normalize-space(gmd:individualName/gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString[@locale = $locale])"/>
                <xsl:variable name="mail"
                              select="normalize-space(gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:electronicMailAddress[1]/gco:CharacterString)"/>

                <Field name="_title"
                       string="{if ($title != '') then $title
                                else if ($name != '') then concat($org, ' (', $name, ')')
                                else if ($mail != '') then concat($org, ' (', $mail, ')')
                                else $org}"
                       store="true" index="true"/>
                <Field name="orgName" string="{$org}" store="true" index="true"/>
                <Field name="orgNameTree" string="{$org}" store="true" index="true"/>
                <xsl:for-each
                        select="gmd:contactInfo/*/gmd:address/*/gmd:electronicMailAddress">
                    <Field name="email" string="{gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString[@locale = $locale]}" store="true" index="true"/>
                </xsl:for-each>

            </xsl:when>
            <xsl:otherwise>
                <xsl:variable name="org"
                              select="normalize-space(gmd:organisationName/gco:CharacterString)"/>
                <xsl:variable name="name"
                              select="normalize-space(gmd:individualName/gco:CharacterString)"/>
               <xsl:variable name="mail"
                              select="normalize-space(gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:electronicMailAddress[1]/gco:CharacterString)"/>

              <Field name="_title"
                       string="{if ($title != '') then $title
                                else if ($name != '') then concat($org, ' (', $name, ')')
                                else if ($mail != '') then concat($org, ' (', $mail, ')')
                                else $org}"
                       store="true" index="true"/>
                <Field name="orgName" string="{$org}" store="true" index="true"/>
                <Field name="orgNameTree" string="{$org}" store="true" index="true"/>
                <xsl:for-each
                        select="gmd:contactInfo/*/gmd:address/*/gmd:electronicMailAddress/gco:CharacterString">
                    <Field name="email" string="{.}" store="true" index="true"/>
                </xsl:for-each>

            </xsl:otherwise>
        </xsl:choose>

        <xsl:call-template name="subtemplate-common-fields"/>

    </xsl:template>

    <!--Distribution information-->
    <xsl:template mode="index" match="gmd:MD_Distribution[count(ancestor::node()) =  1]">
        <Field name="_title"
               string="{if ($title != '') then $title
        else string-join(gmd:transferOptions/gmd:MD_DigitalTransferOptions/
        gmd:onLine/gmd:CI_OnlineResource/gmd:linkage/gmd:URL, ', ')}"
               store="true" index="true"/>

        <xsl:call-template name="subtemplate-common-fields"/>
    </xsl:template>


    <!--Online resources-->
    <xsl:template mode="index" match="gmd:CI_OnlineResource[count(ancestor::node()) =  1]">
        <Field name="_title"
               string="{if ($title != '') then $title else gmd:linkage/gmd:URL}"
               store="true" index="true"/>

        <xsl:call-template name="subtemplate-common-fields"/>
    </xsl:template>



    <xsl:template mode="index" match="gmd:EX_Extent[count(ancestor::node()) =  1]">
      <xsl:param name="locale"/>
      <xsl:choose>
        <xsl:when test="normalize-space(gmd:description) != ''">
          <xsl:choose>
            <xsl:when test="$isMultilingual">
              <xsl:variable name="localizedDesc"
                          select="gmd:description/gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString[@locale = $locale]"/>
              <xsl:variable name="nonEmptyDesc"
                          select="(gmd:description/gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString[./text()!=''])[1]"/>
              <Field name="_title"
                     string="{if ($title != '') then $title else if ($localizedDesc != '') then $localizedDesc
                             else $nonEmptyDesc}"
                     store="true" index="true"/>
            </xsl:when>
            <xsl:otherwise>
              <Field name="_title"
                     string="{if ($title != '') then $title else gmd:description/gco:CharacterString}"
                     store="true" index="true"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:when>
        <xsl:otherwise>
          <Field name="_title"
                 string="{if ($title != '') then $title
                          else if (normalize-space(gmd:description/gco:CharacterString) != '')
                          then gmd:description/gco:CharacterString
                          else string-join(.//gco:Decimal, ', ')}"
                 store="true" index="true"/>
        </xsl:otherwise>
      </xsl:choose>

        <xsl:call-template name="subtemplate-common-fields"/>
    </xsl:template>



    <xsl:template mode="index" match="gmd:DQ_DomainConsistency[count(ancestor::node()) =  1]">
        <Field name="_title"
               string="{if ($title != '') then $title
                        else gmd:result/*/gmd:specification/*/gmd:title/*/text()}"
               store="true" index="true"/>

        <xsl:call-template name="subtemplate-common-fields"/>
    </xsl:template>



    <xsl:template mode="index" match="gmd:MD_Format[count(ancestor::node()) =  1]">
        <Field name="_title"
               string="{if ($title != '') then $title else gmd:name/*/text()}"
               store="true" index="true"/>

        <xsl:call-template name="subtemplate-common-fields"/>
    </xsl:template>

	<xsl:template mode="index" match="gmd:resourceConstraints[count(ancestor::node()) =  1]">
        <Field name="_title"
            string="{if ($title != '') then $title
                     else concat(  
                        string-join(gmd:MD_LegalConstraints/*/gmd:MD_RestrictionCode/@codeListValue[@codeListValue!='otherConstraints'], ', '), 
                        ' ', string-join(gmd:MD_LegalConstraints/gmd:otherConstraints/*/text(), ', '))}"
            store="true" index="true"/>

        <xsl:call-template name="subtemplate-common-fields"/>
    </xsl:template>

    <xsl:template name="subtemplate-common-fields">
        <Field name="any" string="{normalize-space(string(.))}" store="false" index="true"/>
        <Field name="_root" string="{name(.)}" store="true" index="true"/>
    </xsl:template>

</xsl:stylesheet>
