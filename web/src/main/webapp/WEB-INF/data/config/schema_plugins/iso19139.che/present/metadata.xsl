<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl ="http://www.w3.org/1999/XSL/Transform"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gmx="http://www.isotc211.org/2005/gmx"
                xmlns:gts="http://www.isotc211.org/2005/gts"
                xmlns:srv="http://www.isotc211.org/2005/srv"
                xmlns:che="http://www.geocat.ch/2008/che"
                xmlns:gml="http://www.opengis.net/gml"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:geonet="http://www.fao.org/geonetwork"
                xmlns:util="java:org.fao.geonet.util.XslUtil"
                xmlns:exslt="http://exslt.org/common"
                xmlns:xalan = "http://xml.apache.org/xalan">

    <xsl:include href="metadata-che-layouts.xsl"/>
    <xsl:include href="metadata-che-fop.xsl"/>
    <xsl:include href="xml-to-string.xsl"/>

    <xsl:template name="iso19139.che-javascript"/>
    <xsl:template name="iso19139.cheCompleteTab">
        <xsl:param name="tabLink"/>
        <xsl:param name="schema"/>


        <xsl:call-template name="displayTab">
            <xsl:with-param name="tab"     select="'complete'"/>
            <xsl:with-param name="text"    select="/root/gui/strings/completeTab"/>
            <xsl:with-param name="tabLink" select="$tabLink"/>
        </xsl:call-template>
        <xsl:call-template name="iso19139CompleteTab">
            <xsl:with-param name="schema" select="$schema"/>
            <xsl:with-param name="tabLink" select="$tabLink"/>
        </xsl:call-template>
        <xsl:call-template name="displayTab">
            <xsl:with-param name="tab" select="'legislationInformation'"/>
            <xsl:with-param name="text" select="/root/gui/strings/legislationInformation"/>
            <xsl:with-param name="indent" select="'&#xA0;&#xA0;&#xA0;'"/>
            <xsl:with-param name="tabLink" select="$tabLink"/>
        </xsl:call-template>

    </xsl:template>

    <!-- main template - the way into processing iso19139 -->
    <xsl:template name="metadata-iso19139.che">
        <xsl:param name="schema"/>
        <xsl:param name="edit" select="false()"/>
        <xsl:param name="embedded"/>

        <xsl:call-template name="toggle-visibility-edit">
            <xsl:with-param name="edit" select="$edit"/>
        </xsl:call-template>


        <xsl:apply-templates mode="iso19139" select=".">
            <xsl:with-param name="schema" select="'iso19139'"/>
            <xsl:with-param name="edit"   select="$edit"/>
            <xsl:with-param name="currTab"   select="$currTab"/>
        </xsl:apply-templates>


    </xsl:template>


    <!-- Do not display those elements -->
    <xsl:template mode="elementEP"
                  match="gmd:describes|gmd:propertyType|gmd:featureType|gmd:featureAttribute" priority="2"/>
    <xsl:template mode="elementEP"
                  match="geonet:child[@name='describes' and @prefix='gmd']|
		geonet:child[@name='propertyType' and @prefix='gmd']|
		geonet:child[@name='featureType' and @prefix='gmd']|
		geonet:child[@name='featureAttribute' and @prefix='gmd']" priority="2"/>


    <!-- In ISO profil for switzerland not all text fields are
    multilingual. -->
    <xsl:template mode="iso19139"
                  match="
        che:basicGeodataID[gco:CharacterString]|
        che:streetName[gco:CharacterString]|
        che:streetNumber[gco:CharacterString]|
        che:addressLine[gco:CharacterString]|
        che:postBox[gco:CharacterString]|
        che:directNumber[gco:CharacterString]|
        che:mobile[gco:CharacterString]|
        che:individualFirstName[gco:CharacterString]|
        che:individualLastName[gco:CharacterString]|
        che:internalReference[gco:CharacterString]
        "
                  priority="100">
        <xsl:param name="schema" />
        <xsl:param name="edit" />

        <xsl:call-template name="iso19139String">
            <xsl:with-param name="schema" select="$schema"/>
            <xsl:with-param name="edit"   select="$edit"/>
        </xsl:call-template>
    </xsl:template>


    <!-- In ISO profil for switzerland not all text fields are 
    multilingual. -->
    <xsl:template mode="iso19139" match="gmd:explanation[gco:CharacterString]"
                  priority="100">
        <xsl:param name="schema" />
        <xsl:param name="edit" />

        <xsl:call-template name="iso19139String">
            <xsl:with-param name="schema" select="$schema"/>
            <xsl:with-param name="edit"   select="$edit"/>
            <xsl:with-param name="rows" select="3" />
        </xsl:call-template>
    </xsl:template>

    <!-- Use this template to define which elements
        are not multilingual and mandatory-->
    <xsl:template mode="iso19139"
                  match="
		che:CHE_MD_AbstractClass/che:name[gco:CharacterString]|
		che:CHE_MD_Attribute/che:name[gco:CharacterString]|
		che:CHE_MD_CodeDomain/che:name[gco:CharacterString]|
		che:CHE_MD_Role/che:name[gco:CharacterString]|
		che:CHE_MD_Association/che:name[gco:CharacterString]|
		che:CHE_MD_Class/che:name[gco:CharacterString]|
		che:CHE_MD_Type/che:name[gco:CharacterString]|
		che:attribute/che:name[gco:CharacterString]|	
		che:CHE_MD_CodeValue/che:name[gco:CharacterString]"
                  priority="100">
        <xsl:param name="schema" />
        <xsl:param name="edit" />

        <xsl:call-template name="iso19139String">
            <xsl:with-param name="schema" select="$schema"/>
            <xsl:with-param name="edit"   select="$edit"/>
            <xsl:with-param name="validator" select="'validateNonEmpty(this)'" />
        </xsl:call-template>
    </xsl:template>

    <!-- ============================================================================= -->

    <xsl:template mode="iso19139" match="gmd:contact|gmd:pointOfContact|gmd:distributorContact|gmd:citedResponsibleParty|gmd:userContactInfo|*[@gco:isoType='gmd:CI_ResponsibleParty']" priority="5">
        <xsl:param name="schema"/>
        <xsl:param name="edit"/>

        <xsl:choose>
            <xsl:when test="che:CHE_CI_ResponsibleParty">
                <xsl:call-template name="cheContactTemplate">
                    <xsl:with-param name="edit" select="$edit" />
                    <xsl:with-param name="schema" select="$schema" />
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:call-template name="contactTemplate">
                    <xsl:with-param name="edit" select="$edit"/>
                    <xsl:with-param name="schema" select="$schema"/>
                </xsl:call-template>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="cheContactTemplate">
        <xsl:param name="schema"/>
        <xsl:param name="edit"/>

        <xsl:variable name="lang">
            <xsl:call-template name="getLangId">
                <xsl:with-param name="langGui" select="/root/gui/language" />
                <xsl:with-param name="md"
                                select="ancestor-or-self::*[name(.)='gmd:MD_Metadata' or @gco:isoType='gmd:MD_Metadata']" />
            </xsl:call-template>
        </xsl:variable>
        <xsl:variable name="content">
            <xsl:for-each select="che:CHE_CI_ResponsibleParty">
                <tr>
                    <td class="padded-content" width="100%" colspan="2">
                        <table width="100%">
                            <tr>
                                <td width="50%" valign="top">
                                    <table width="100%">
                                        <xsl:apply-templates mode="elementEP" select="../@xlink:href">
                                            <xsl:with-param name="schema" select="$schema"/>
                                            <xsl:with-param name="edit"   select="$edit"/>
                                        </xsl:apply-templates>

                                        <xsl:choose>
                                            <xsl:when test="$edit = 'true'">
                                                <xsl:variable name="link">
                                                    <xsl:value-of select="/root/gui/locService" />
                                                    <xsl:text>/shared.user.edit?closeOnSave&amp;operation=fullupdate&amp;id=</xsl:text>
                                                    <xsl:value-of select="substring(../@xlink:href, string-length(substring-before(../@xlink:href, 'id'))+4)"/>
                                                    <xsl:text>&amp;validated=</xsl:text>
                                                    <xsl:choose>
                                                        <xsl:when test="../@xlink:role = 'http://www.geonetwork.org/non_valid_obj'">
                                                            <xsl:text>n</xsl:text>
                                                        </xsl:when>
                                                        <xsl:otherwise>
                                                            <xsl:text>y</xsl:text>
                                                        </xsl:otherwise>
                                                    </xsl:choose>
                                                </xsl:variable>
                                                <xsl:choose>
                                                    <xsl:when test="not(contains(../@xlink:href, 'deleted'))">
                                                        <a style="cursor:pointer;font-weight: bold;" href="{$link}" target="_userEditTab">Edit Contact</a>
                                                    </xsl:when>
                                                    <xsl:otherwise>
                                                        <xsl:text>Contact is deleted: </xsl:text><xsl:value-of select="../@xlink:href"/>
                                                    </xsl:otherwise>
                                                </xsl:choose>

                                                <xsl:apply-templates mode="elementEP" select="che:individualFirstName|geonet:child[string(@name)='individualFirstName']">
                                                    <xsl:with-param name="schema" select="$schema"/>
                                                    <xsl:with-param name="edit"   select="$edit"/>
                                                </xsl:apply-templates>

                                                <xsl:apply-templates mode="elementEP" select="che:individualLastName|geonet:child[string(@name)='individualLastName']">
                                                    <xsl:with-param name="schema" select="$schema"/>
                                                    <xsl:with-param name="edit"   select="$edit"/>
                                                </xsl:apply-templates>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <xsl:apply-templates mode="simpleElement"
                                                                     select="(che:individualFirstName|geonet:child[string(@name)='individualFirstName']|
		                                            che:individualLastName|geonet:child[string(@name)='individualLastName'])[1]">
                                                    <xsl:with-param name="schema" select="$schema"/>
                                                    <xsl:with-param name="edit"   select="$edit"/>
                                                    <xsl:with-param name="title" select="/root/gui/schemas/iso19139/labels/element[@name='gmd:individualName']/label"/>
                                                    <xsl:with-param name="text">
                                                        <xsl:value-of select="(che:individualFirstName|geonet:child[string(@name)='individualFirstName'])/gco:CharacterString"/>
                                                        <xsl:text> </xsl:text>
                                                        <xsl:value-of select="(che:individualLastName|geonet:child[string(@name)='individualLastName'])/gco:CharacterString"/>
                                                    </xsl:with-param>
                                                </xsl:apply-templates>
                                            </xsl:otherwise>
                                        </xsl:choose>

                                        <xsl:apply-templates mode="elementEP" select="gmd:organisationName|geonet:child[string(@name)='organisationName']">
                                            <xsl:with-param name="schema" select="$schema"/>
                                            <xsl:with-param name="edit"   select="$edit"/>
                                        </xsl:apply-templates>

                                        <xsl:apply-templates mode="elementEP" select="che:organisationAcronym|geonet:child[string(@name)='organisationAcronym']">
                                            <xsl:with-param name="schema" select="$schema"/>
                                            <xsl:with-param name="edit"   select="$edit"/>
                                        </xsl:apply-templates>

                                        <xsl:apply-templates mode="elementEP" select="gmd:positionName|geonet:child[string(@name)='positionName']">
                                            <xsl:with-param name="schema" select="$schema"/>
                                            <xsl:with-param name="edit"   select="$edit"/>
                                        </xsl:apply-templates>

                                        <xsl:apply-templates mode="elementEP" select="gmd:role|geonet:child[string(@name)='role']">
                                            <xsl:with-param name="schema" select="$schema"/>
                                            <xsl:with-param name="edit"   select="$edit"/>
                                        </xsl:apply-templates>

                                    </table>
                                </td>
                                <td valign="top">
                                    <table width="100%">
                                        <xsl:apply-templates mode="elementEP" select="gmd:contactInfo|geonet:child[string(@name)='contactInfo']">
                                            <xsl:with-param name="schema" select="$schema"/>
                                            <xsl:with-param name="edit"   select="$edit"/>
                                        </xsl:apply-templates>
                                    </table>
                                </td>
                            </tr>
                        </table>
                    </td>
                </tr>
            </xsl:for-each>
        </xsl:variable>

        <xsl:apply-templates mode="complexElement" select=".">
            <xsl:with-param name="schema"  select="$schema"/>
            <xsl:with-param name="edit"    select="$edit"/>
            <xsl:with-param name="content" select="$content"/>
        </xsl:apply-templates>

    </xsl:template>

    <!-- ============================================================================= -->

    <!-- Use this template to define which elements
        are multilingual and mandatory  
    <xsl:template mode="iso19139"
        match=" "
        priority="100">
        <xsl:param name="schema" />
        <xsl:param name="edit" />
        
        <xsl:call-template name="localizedCharStringField">
            <xsl:with-param name="schema" select="$schema" />
            <xsl:with-param name="edit" select="$edit" />
            <xsl:with-param name="validator" select="'validateNonEmpty(this)'" />
        </xsl:call-template>
        </xsl:template> -->

    <xsl:template mode="iso19139"
                  match="che:organisationAcronym"
                  priority="100">
        <xsl:param name="schema" />
        <xsl:param name="edit" />

        <xsl:call-template name="localizedCharStringField">
            <xsl:with-param name="schema" select="$schema" />
            <xsl:with-param name="edit" select="$edit" />
        </xsl:call-template>
    </xsl:template>


    <!-- Multilingual editor widget is composed of input box
    with a list of languages. Metadata languages are :
     * the main language (gmd:language) and
     * all languages defined in gmd:locale. -->
    <xsl:template mode="iso19139"
                  match="gmd:CI_OnlineResource/gmd:linkage |
                         che:historyConceptURL |
                         che:archiveConceptURL |
                         che:dataModel |
                         che:portrayalCatalogueURL |
                        *[che:LocalisedURL|che:PT_FreeURL|@xsi:type='che:PT_FreeURL_PropertyType']"
                  priority="20">
        <xsl:param name="schema" />
        <xsl:param name="edit" />
        <xsl:param name="rows" select="1" />
        <xsl:param name="validator" />

        <xsl:variable name="langId">
            <xsl:call-template name="getLangId">
                <xsl:with-param name="langGui" select="/root/gui/language" />
                <xsl:with-param name="md"
                                select="ancestor-or-self::*[name(.)='gmd:MD_Metadata' or @gco:isoType='gmd:MD_Metadata']" />
            </xsl:call-template>
        </xsl:variable>
        <!-- Use this template for mandatory text fields.
            User editing is validated on the editor. If not valid,
            form input will be highlighted (red).
        -->
        <xsl:variable name="validator">
            <xsl:choose>
                <xsl:when test="(name(.)='gmd:linkage' and ancestor::node()[name(.) = 'gmd:MD_DigitalTransferOptions'])">
                    <xsl:value-of select="'validateNonEmpty(this)'"/>
                </xsl:when>
            </xsl:choose>
        </xsl:variable>

        <xsl:variable name="widget">
            <xsl:choose>
                <xsl:when test="$edit=true()">
                    <xsl:variable name="tmpFreeURL">
                        <xsl:call-template name="PT_FreeURL_Tree" />
                    </xsl:variable>

                    <xsl:variable name="ptFreeURLTree" select="$tmpFreeURL" />

                    <xsl:variable name="mainLang"
                                  select="string(/root/*/gmd:language/gco:CharacterString)" />
                    <xsl:variable name="mainLangId"
                                  select="concat('#',/root/*/gmd:locale/gmd:PT_Locale[gmd:languageCode/gmd:LanguageCode/@codeListValue=$mainLang]/@id)" />

                    <table><tr><td>
                        <!-- Match gmd:URL element which is in default language or
                        process a PT_FreeText with a reference to the main metadata language. -->
                        <xsl:choose>
                            <xsl:when test="gmd:URL|che:LocalisedURL">
                                <xsl:for-each select="gmd:URL|che:LocalisedURL">
                                    <xsl:call-template name="getElementText">
                                        <xsl:with-param name="schema" select="$schema" />
                                        <xsl:with-param name="visible" select="'true'" />
                                        <xsl:with-param name="edit" select="true()" />
                                        <xsl:with-param name="rows" select="$rows" />
                                        <xsl:with-param name="validator" select="$validator" />
                                    </xsl:call-template>
                                </xsl:for-each>
                            </xsl:when>
                            <xsl:when test="che:PT_FreeURL/che:URLGroup/che:LocalisedURL[@locale=$mainLangId]">
                                <xsl:for-each select="che:PT_FreeURL/che:URLGroup/che:LocalisedURL[@locale=$mainLangId]">
                                    <xsl:call-template name="getElementText">
                                        <xsl:with-param name="schema" select="$schema" />
                                        <xsl:with-param name="edit" select="true()" />
                                        <xsl:with-param name="visible" select="'true'" />
                                        <xsl:with-param name="rows" select="$rows" />
                                        <xsl:with-param name="validator" select="$validator" />
                                    </xsl:call-template>
                                </xsl:for-each>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:for-each select="$ptFreeURLTree//che:LocalisedURL[@locale=$mainLangId]">
                                    <xsl:call-template name="getElementText">
                                        <xsl:with-param name="schema" select="$schema" />
                                        <xsl:with-param name="edit" select="true()" />
                                        <xsl:with-param name="visible" select="'true'" />
                                        <xsl:with-param name="rows" select="$rows" />
                                        <xsl:with-param name="validator" select="$validator" />
                                    </xsl:call-template>
                                </xsl:for-each>
                            </xsl:otherwise>
                        </xsl:choose>

                        <xsl:for-each select="$ptFreeURLTree//che:LocalisedURL[@locale!=$mainLangId]">
                            <xsl:call-template name="getElementText">
                                <xsl:with-param name="schema" select="$schema" />
                                <xsl:with-param name="edit" select="true()" />
                                <xsl:with-param name="visible" select="'false'" />
                                <xsl:with-param name="rows" select="$rows" />
                                <xsl:with-param name="validator" select="$validator" />
                            </xsl:call-template>
                        </xsl:for-each>
                    </td>
                        <td align="left">&#160;
                            <select class="md lang_selector" name="localization" onchange="enableLocalInput(this)" SELECTED="true">
                                <xsl:choose>
                                    <xsl:when test="gmd:*|che:LocalisedURL">
                                        <xsl:variable name="ref">
                                            <xsl:choose>
                                                <xsl:when test="che:LocalisedURL">
                                                    <xsl:value-of select="che:LocalisedURL/geonet:element/@ref"></xsl:value-of>
                                                </xsl:when>
                                                <xsl:otherwise><xsl:value-of select="gmd:*/geonet:element/@ref"></xsl:value-of></xsl:otherwise>
                                            </xsl:choose>
                                        </xsl:variable>
                                        <option value="_{$ref}" code="{substring-after($mainLangId, '#')}">
                                            <xsl:choose>
                                                <xsl:when test="normalize-space($mainLang)=''">
                                                    <xsl:value-of select="/root/gui/strings/mainMetadataLanguageNotSet"/>
                                                </xsl:when>
                                                <xsl:otherwise>
                                                    <xsl:value-of
                                                            select="/root/gui/isoLang/record[code=$mainLang]/label/*[name(.)=/root/gui/language]" />
                                                </xsl:otherwise>
                                            </xsl:choose>
                                        </option>
                                        <xsl:for-each select="$ptFreeURLTree//che:LocalisedURL[@locale!=$mainLangId]">
                                            <option value="_{geonet:element/@ref}" code="{substring-after(@locale, '#')}">
                                                <xsl:value-of select="@language" />
                                            </option>
                                            <xsl:value-of select="name(.)" />
                                        </xsl:for-each>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:for-each select="$ptFreeURLTree//che:LocalisedURL">
                                            <option value="_{geonet:element/@ref}" code="{substring-after(@locale, '#')}">
                                                <xsl:value-of select="@language" />
                                            </option>
                                            <xsl:value-of select="name(.)" />
                                        </xsl:for-each>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </select>

                        </td></tr></table>
                </xsl:when>
                <xsl:otherwise>
                    ERROR
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

        <xsl:choose>
            <xsl:when test="$edit=true()">
                <xsl:call-template name="iso19139String">
                    <xsl:with-param name="schema" select="$schema" />
                    <xsl:with-param name="edit" select="$edit" />
                    <xsl:with-param name="langId" select="$langId" />
                    <xsl:with-param name="widget" select="$widget" />
                    <xsl:with-param name="rows" select="$rows" />
                    <xsl:with-param name="validator" select="$validator" />
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:call-template name="cheString">
                    <xsl:with-param name="schema" select="$schema" />
                    <xsl:with-param name="edit" select="$edit" />
                    <xsl:with-param name="langId" select="$langId" />
                    <xsl:with-param name="rows" select="$rows" />
                </xsl:call-template>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>


    <xsl:template name="PT_FreeURL_Tree">
        <xsl:variable name="mainLang"
                      select="string(/root/*/gmd:language/gco:CharacterString)" />
        <xsl:variable name="languages"
                      select="/root/*/gmd:locale/gmd:PT_Locale/gmd:languageCode/gmd:LanguageCode/@codeListValue" />
        <xsl:variable name="xlinkedAncestor"><xsl:call-template name="validatedXlink"/></xsl:variable>

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
            <xsl:variable name="disabled" select="$currentNode/../geonet:element/@disabled" />
            <xsl:variable name="min" select="$currentNode/../geonet:element/@min" />
            <xsl:variable name="guiLang" select="/root/gui/language" />
            <xsl:variable name="language">
                <xsl:choose>
                    <xsl:when test="/root/gui/isoLang/record[code=$code]/label/*[name(.)=$guiLang]">
                        <xsl:value-of select="/root/gui/isoLang/record[code=$code]/label/*[name(.)=$guiLang]"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="$code"/> no found in <xsl:value-of select="$guiLang"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:variable>
            <che:PT_FreeURL xlink:title="{$currentNode/ancestor-or-self::*[contains(@xlink:title,'rejected')]/@xlink:title}">
                <!-- Propagate xlink attribute to the var which contains translation
                in order to turn off editing. -->
                <xsl:if test="$xlinkedAncestor = 'true'">
                    <xsl:attribute name="xlink:href"></xsl:attribute>
                </xsl:if>
                <che:URLGroup>
                    <che:LocalisedURL locale="{$langId}"
                                      code="{$code}" language="{$language}">
                        <xsl:value-of
                                select="$currentNode//che:LocalisedURL[@locale=$langId]" />
                        <xsl:choose>
                            <xsl:when
                                    test="$currentNode//che:LocalisedURL[@locale=$langId]">
                                <geonet:element
                                        ref="{$currentNode//che:LocalisedURL[@locale=$langId]/geonet:element/@ref}" >
                                    <xsl:if test="$disabled">
                                        <xsl:attribute name="disabled">true</xsl:attribute>
                                    </xsl:if>
                                </geonet:element>
                            </xsl:when>
                            <xsl:otherwise>
                                <geonet:element ref="url_{substring($langId,2)}_{$ref}" >
                                    <xsl:if test="$disabled">
                                        <xsl:attribute name="disabled">true</xsl:attribute>
                                    </xsl:if>
                                </geonet:element>
                            </xsl:otherwise>
                        </xsl:choose>
                    </che:LocalisedURL>
                    <geonet:element ref="" >
                        <xsl:if test="$disabled">
                            <xsl:attribute name="disabled">true</xsl:attribute>
                        </xsl:if>
                    </geonet:element>
                </che:URLGroup>
                <geonet:element ref="" >
                    <!-- Add min attribute from current node to PT_FreeText
                        child in order to turn on validation criteria. -->
                    <xsl:if test="$min = 1">
                        <xsl:attribute name="min">1</xsl:attribute>
                    </xsl:if>
                    <xsl:if test="$disabled">
                        <xsl:attribute name="disabled">true</xsl:attribute>
                    </xsl:if>
                </geonet:element>
            </che:PT_FreeURL>
        </xsl:for-each>
    </xsl:template>

    <xsl:template name="translateURL">
        <xsl:param name="schema"/>
        <xsl:param name="edit"/>
        <xsl:param name="rows" select="1"/>
        <xsl:param name="cols" select="40"/>
        <xsl:param name="langId" />
        <xsl:param name="widget" />
        <xsl:param name="validator" />

        <xsl:variable name="defaultLang">
            <xsl:call-template name="getLangId">
                <xsl:with-param name="langGui"
                                select="/root/*/gmd:language/gco:CharacterString" />
                <xsl:with-param name="md"
                                select="ancestor-or-self::*[name(.)='gmd:MD_Metadata' or @gco:isoType='gmd:MD_Metadata']" />
            </xsl:call-template>
        </xsl:variable>
        <xsl:choose>
            <xsl:when test="not($edit=true() and $widget)">
                <xsl:choose>
                    <xsl:when test="(.//che:LocalisedURL)[@locale=$langId]">
                        <xsl:value-of select="(.//che:LocalisedURL)[@locale=$langId]" />
                    </xsl:when>

                    <xsl:when test="(.//che:LocalisedURL)[@locale=$defaultLang]">
                        <xsl:value-of select=".//che:LocalisedURL[@locale=$defaultLang]" />
                    </xsl:when>
                    <xsl:when test="che:LocalisedURL">
                        <xsl:value-of select=".//che:LocalisedURL[@locale=$defaultLang]" />
                    </xsl:when>

                    <xsl:otherwise>
                        <xsl:value-of select="gmd:URL" />
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <xsl:otherwise>
                <xsl:copy-of select="$widget" />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="cheString">
        <xsl:param name="schema" />
        <xsl:param name="edit" />
        <xsl:param name="rows" select="1" />
        <xsl:param name="cols" select="50" />
        <xsl:param name="langId" />
        <xsl:param name="widget" />
        <xsl:param name="validator" />

        <xsl:variable name="title">
            <xsl:call-template name="getTitle">
                <xsl:with-param name="name" select="name(.)" />
                <xsl:with-param name="schema" select="$schema" />
            </xsl:call-template>
        </xsl:variable>
        <xsl:variable name="helpLink">
            <xsl:call-template name="getHelpLink">
                <xsl:with-param name="name" select="name(.)" />
                <xsl:with-param name="schema" select="$schema" />
            </xsl:call-template>
        </xsl:variable>
        <xsl:variable name="text">
            <xsl:call-template name="translateURL">
                <xsl:with-param name="schema" select="$schema"/>
                <xsl:with-param name="rows" select="$rows"/>
                <xsl:with-param name="cols" select="$cols"/>
                <xsl:with-param name="edit" select="$edit"/>
                <xsl:with-param name="langId" select="$langId" />
                <xsl:with-param name="validator" select="$validator"/>
                <xsl:with-param name="widget" select="$widget"/>
            </xsl:call-template>
        </xsl:variable>
        <xsl:variable name="attrs">
            <xsl:for-each select="che:*/@*">
                <xsl:value-of select="name(.)" />
            </xsl:for-each>
        </xsl:variable>

        <xsl:choose>
            <xsl:when test="normalize-space($attrs)!=''">
                <xsl:apply-templates mode="complexElement"
                                     select=".">
                    <xsl:with-param name="schema" select="$schema" />
                    <xsl:with-param name="edit" select="$edit" />
                    <xsl:with-param name="title" select="$title" />
                    <xsl:with-param name="helpLink" select="$helpLink" />
                    <xsl:with-param name="content">

                        <!-- existing attributes -->
                        <xsl:for-each select="che:*/@*">
                            <xsl:apply-templates mode="simpleElement"
                                                 select=".">
                                <xsl:with-param name="schema" select="$schema" />
                                <xsl:with-param name="edit" select="$edit" />
                            </xsl:apply-templates>
                        </xsl:for-each>

                        <!-- existing content -->
                        <xsl:apply-templates mode="simpleElement" select=".">
                            <xsl:with-param name="schema" select="$schema" />
                            <xsl:with-param name="edit" select="$edit" />
                            <xsl:with-param name="title" select="$title" />
                            <xsl:with-param name="helpLink" select="$helpLink" />
                            <xsl:with-param name="text" select="$text" />
                        </xsl:apply-templates>
                    </xsl:with-param>
                </xsl:apply-templates>
            </xsl:when>
            <xsl:otherwise>
                <xsl:apply-templates mode="simpleElement" select=".">
                    <xsl:with-param name="schema" select="$schema" />
                    <xsl:with-param name="edit" select="$edit" />
                    <xsl:with-param name="title" select="$title" />
                    <xsl:with-param name="helpLink" select="$helpLink" />
                    <xsl:with-param name="text" select="$text" />
                </xsl:apply-templates>
            </xsl:otherwise>
        </xsl:choose>

    </xsl:template>

    <xsl:template mode="iso19139" match="gco:Record" priority="2">
        <xsl:param name="schema" />
        <xsl:param name="edit" />

        <xsl:variable name="text">
            <xsl:variable name="ref" select="geonet:element/@ref" />
            <xsl:variable name="data">
                <xsl:apply-templates mode="xml-to-string">
                    <xsl:with-param name="depth">10</xsl:with-param>
                </xsl:apply-templates>
            </xsl:variable>
            <xsl:choose>
                <xsl:when test="$edit = true()">
                    <textarea class="md" name="_B{$ref}" id="_B{$ref}" cols="50" rows="5" style="display:block">
                        <xsl:value-of select="normalize-space($data)"/>
                    </textarea>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="$data"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:apply-templates mode="simpleElement"
                             select=".">
            <xsl:with-param name="schema" select="$schema" />
            <xsl:with-param name="edit" select="true()" />
            <xsl:with-param name="text" select="$text" />
        </xsl:apply-templates>
    </xsl:template>

    <xsl:template mode="iso19139"
                  match="gmd:CI_OnlineResource[gmd:linkage/che:PT_FreeURL or
                                 gmd:linkage/che:LocalisedURL or 
                                 gmd:linkage[xsi:type='che:PT_FreeURL_PropertyType']]" priority="10">
        <xsl:param name="schema"/>
        <xsl:param name="edit"/>

        <xsl:choose>
            <xsl:when test="$edit!=true()">

                <xsl:variable name="langId">
                    <xsl:call-template name="getLangId">
                        <xsl:with-param name="langGui" select="/root/gui/language" />
                        <xsl:with-param name="md"
                                        select="ancestor-or-self::*[name(.)='gmd:MD_Metadata' or @gco:isoType='gmd:MD_Metadata']" />
                    </xsl:call-template>
                </xsl:variable>

                <xsl:variable name="defaultLang">
                    <xsl:call-template name="getLangId">
                        <xsl:with-param name="langGui" select="/root/*/gmd:language/gco:CharacterString" />
                        <xsl:with-param name="md"
                                        select="ancestor-or-self::*[name(.)='gmd:MD_Metadata' or @gco:isoType='gmd:MD_Metadata']" />
                    </xsl:call-template>
                </xsl:variable>

                <xsl:variable name="linkLang">
                    <xsl:choose>
                        <xsl:when test="(.//che:LocalisedURL)[@locale=$langId]" >
                            <xsl:value-of select="$langId" />
                        </xsl:when>

                        <xsl:when test="(.//che:LocalisedURL)[@locale=$defaultLang]" >
                            <xsl:value-of select="$defaultLang" />
                        </xsl:when>

                        <xsl:when test="(.//che:LocalisedURL)[@locale=$defaultLang]" >
                            <xsl:value-of select="$defaultLang" />
                        </xsl:when>

                        <xsl:when test="gmd:linkage/che:LocalisedURL" >
                            <xsl:value-of select="$defaultLang" />
                        </xsl:when>

                        <xsl:when test="gmd:linkage/gmd:URL" >
                            <xsl:value-of select="$defaultLang" />
                        </xsl:when>

                        <xsl:when test=".//che:LocalisedURL[position()=1]" >
                            <xsl:value-of select=".//che:LocalisedURL[position()=1]/@locale" />
                        </xsl:when>

                        <xsl:otherwise>
                            <xsl:text/>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>

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


                <xsl:variable name="linkage">
                    <xsl:choose>
                        <xsl:when test="(.//che:LocalisedURL)[@locale=$linkLang]" >
                            <xsl:value-of select="(.//che:LocalisedURL)[@locale=$linkLang]" />
                        </xsl:when>

                        <xsl:when test="gmd:linkage/che:LocalisedURL" >
                            <xsl:value-of select="gmd:linkage/che:LocalisedURL" />
                        </xsl:when>

                        <xsl:when test="gmd:linkage/gmd:URL" >
                            <xsl:value-of select="gmd:linkage/gmd:URL" />
                        </xsl:when>

                        <xsl:otherwise>
                            <xsl:text/>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>

                <xsl:choose>
                    <xsl:when test="string($linkage)!=''">
                        <xsl:apply-templates mode="simpleElement" select=".">
                            <xsl:with-param name="schema" select="$schema"/>
                            <xsl:with-param name="text">
                                <a href="{$linkage}" target="_new">
                                    <xsl:choose>
                                        <xsl:when test="string($description)!='' and string($name)!=''">
                                            <xsl:value-of select="$name"/><xsl:text> (</xsl:text><xsl:value-of select="$description"/>)
                                        </xsl:when>
                                        <xsl:when test="string($description)!=''">
                                            <xsl:value-of select="$description"/>
                                        </xsl:when>
                                        <xsl:when test="string($name)!=''">
                                            <xsl:value-of select="$name"/>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <xsl:value-of select="$linkage"/>
                                        </xsl:otherwise>
                                    </xsl:choose>
                                </a>
                            </xsl:with-param>
                        </xsl:apply-templates>
                    </xsl:when>
                </xsl:choose>
            </xsl:when>
            <xsl:otherwise>
                <xsl:apply-templates mode="iso19139EditOnlineRes" select=".">
                    <xsl:with-param name="schema" select="$schema"/>
                </xsl:apply-templates>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>







    <!-- ============================================================================= -->
    <!-- Restrict list of languages on editing mode for swiss profil -->

    <xsl:template mode="iso19139" match="gmd:language[ancestor::*[name(.)='che:CHE_MD_Metadata']]" priority="100">
        <xsl:param name="schema"/>
        <xsl:param name="edit"/>

        <xsl:apply-templates mode="simpleElement" select=".">
            <xsl:with-param name="schema" select="$schema"/>
            <xsl:with-param name="edit" select="$edit"/>
            <xsl:with-param name="text">
                <xsl:apply-templates mode="iso19139.cheGetIsoLanguage" select="gco:CharacterString">
                    <xsl:with-param name="schema" select="$schema"/>
                    <xsl:with-param name="edit" select="$edit"/>
                </xsl:apply-templates>
            </xsl:with-param>
        </xsl:apply-templates>
    </xsl:template>


    <xsl:template mode="iso19139.cheGetIsoLanguage" match="*">
        <xsl:param name="schema"/>
        <xsl:param name="edit"/>

        <xsl:variable name="lang" select="/root/gui/language"/>
        <xsl:variable name="value" select="string(.|@codeListValue)"/>

        <!-- If the current metadata record is not a template
        do not allow editor to change the default language.
        This is mainly defined because if you change default
        language, you also have to switch all elements from the
        default to the new one (and eventually create the old
        one in local). -->
        <xsl:variable name="isTemplate">
            <xsl:choose>
                <xsl:when test="../../geonet:info/isTemplate='n' and
					(name(../..)='gmd:MD_Metadata' or  ../../@gco:isoType='gmd:MD_Metadata')">
                    <xsl:value-of select="false()"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="true()"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

        <xsl:choose>
            <xsl:when test="$edit='true' and $isTemplate='true'">
                <select class="md" name="_{geonet:element/@ref}" size="1">
                    <option name=""/>

                    <xsl:for-each select="/root/gui/isoLang/record[code='eng' or code='fre' or code='ger' or code='ita' or code='roh']">
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
                <xsl:value-of
                        select="/root/gui/isoLang/record[code=$value]/label/child::*[name() = $lang]"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template mode="iso19139" match="gmd:LanguageCode[ancestor::*[name(.)='che:CHE_MD_Metadata']]" priority="100">
        <xsl:param name="edit"/>

        <xsl:variable name="value" select="@codeListValue" />
        <xsl:variable name="lang" select="/root/gui/language" />
        <xsl:choose>
            <xsl:when test="$edit=true()">
                <select class="md" name="_{geonet:element/@ref}_codeListValue"
                        size="1">
                    <option name="" />

                    <xsl:for-each select="/root/gui/isoLang/record[code='eng' or code='fre' or code='ger' or code='ita' or code='roh']">
                        <option value="{code}">
                            <xsl:if test="code = $value">
                                <xsl:attribute name="selected" />
                            </xsl:if>
                            <xsl:value-of select="label/child::*[name() = $lang]" />
                        </option>
                    </xsl:for-each>
                </select>
            </xsl:when>

            <xsl:otherwise>
                <xsl:value-of
                        select="/root/gui/isoLang/record[code=$value]/label/child::*[name() = $lang]" />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- ===================================================================== -->
    <!-- these elements should be boxed -->
    <!-- ===================================================================== -->

    <xsl:template mode="iso19139" match="che:legislationInformation|che:CHE_MD_ArchiveConcept|che:CHE_MD_HistoryConcept|che:CHE_CI_ResponsibleParty|
		che:CHE_MD_DataIdentification|che:CHE_MD_FeatureCatalogueDescription|che:class|che:attribute|che:CHE_MD_MaintenanceInformation|
		che:CHE_MD_LegalConstraints">
        <xsl:param name="schema"/>
        <xsl:param name="edit"/>

        <xsl:apply-templates mode="complexElement" select=".">
            <xsl:with-param name="schema" select="$schema"/>
            <xsl:with-param name="edit"   select="$edit"/>
        </xsl:apply-templates>
    </xsl:template>

    <xsl:template mode="iso19139" match="che:country[*/@codeList]" priority="1">
        <xsl:param name="schema" />
        <xsl:param name="edit" />

        <xsl:variable name="qname" select="name(.)"/>
        <xsl:variable name="value" select="gmd:Country/@codeListValue"/>


        <xsl:apply-templates mode="simpleElement" select=".">
            <xsl:with-param name="schema" select="$schema" />
            <xsl:with-param name="edit" select="$edit" />
            <xsl:with-param name="text">
                <xsl:choose>
                    <xsl:when test="$edit=true()">

                        <xsl:variable name="xlinkedAncestor"><xsl:call-template name="validatedXlink"/></xsl:variable>

                        <!-- codelist in edit mode -->
                        <select class="md" name="_{gmd:Country/geonet:element/@ref}_codeListValue"
                                size="1">
                            <xsl:if test="$xlinkedAncestor = 'true'">
                                <xsl:attribute name="disabled">true</xsl:attribute>
                            </xsl:if>

                            <option name="" />
                            <xsl:for-each select="/root/gui/countries/country">
                                <xsl:sort select="text()"/>
                                <option>
                                    <xsl:if test="upper-case(@iso2) = upper-case($value)">
                                        <xsl:attribute name="selected" />
                                    </xsl:if>
                                    <xsl:attribute name="value">
                                        <xsl:value-of select="upper-case(@iso2)" />
                                    </xsl:attribute>
                                    <xsl:value-of select="text()" />
                                </option>
                            </xsl:for-each>
                        </select>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of
                                select="/root/gui/countries/country[upper-case($value)=upper-case(text()) or upper-case($value)=upper-case(@iso2)]/text()" />
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:with-param>
        </xsl:apply-templates>

    </xsl:template>

    <xsl:template mode="iso19139" match="che:*[*/@codeList]">
        <xsl:param name="schema"/>
        <xsl:param name="edit"/>
        <xsl:call-template name="iso19139Codelist">
            <xsl:with-param name="schema" select="$schema"/>
            <xsl:with-param name="edit" select="$edit"/>
        </xsl:call-template>
    </xsl:template>


    <xsl:template mode="iso19139"
                  match="che:*[gco:Integer|gco:Decimal|gco:Real|gco:Length|gco:Measure|gco:Scale]"
            >
        <xsl:param name="schema"/>
        <xsl:param name="edit"/>
        <xsl:variable name="langId">
            <xsl:call-template name="getLangId">
                <xsl:with-param name="langGui" select="/root/gui/language"/>
                <xsl:with-param name="md" select="ancestor-or-self::*[name(.)='gmd:MD_Metadata' or @gco:isoType='gmd:MD_Metadata']"/>
            </xsl:call-template>
        </xsl:variable>

        <xsl:call-template name="iso19139String">
            <xsl:with-param name="schema" select="$schema"/>
            <xsl:with-param name="edit" select="$edit"/>
            <xsl:with-param name="langId" select="$langId"/>
            <xsl:with-param name="validator" select="'validateNumber(this)'" />
        </xsl:call-template>
    </xsl:template>

    <xsl:template mode="iso19139"
                  match="*[gco:Distance and ancestor::che:CHE_MD_Metadata]"
                  priority="2">
        <xsl:param name="schema"/>
        <xsl:param name="edit"/>
        <xsl:variable name="langId">
            <xsl:call-template name="getLangId">
                <xsl:with-param name="langGui" select="/root/gui/language"/>
                <xsl:with-param name="md" select="ancestor-or-self::*[name(.)='gmd:MD_Metadata' or @gco:isoType='gmd:MD_Metadata']"/>
            </xsl:call-template>
        </xsl:variable>

        <xsl:call-template name="iso19139String">
            <xsl:with-param name="schema" select="$schema"/>
            <xsl:with-param name="edit" select="$edit"/>
            <xsl:with-param name="langId" select="$langId"/>
            <xsl:with-param name="validator" select="'validateGM03Distance(this, false, false)'" />
        </xsl:call-template>
    </xsl:template>

    <!--

      MemberName (used in gmd:sequenceIdentifier) is a NAME (composed of
      letters and digits, starting with a letter, ili-Refmanual, p. 23)
      according to INTERLIS
      GenericName (used in gmd:featureTypes of
      MD_FeatureCatalogueDescription) is also a NAME (composed of
      letters and digits, starting with a letter, ili-Refmanual, p. 23)
      according to INTERLIS (should not be a choice as it is now)
        * gmd:featureTypes/gco:LocalName
        * gco:MemberName/gco:aName
    -->
    <xsl:template mode="iso19139" match="gmd:featureTypes/gco:LocalName[ancestor::che:CHE_MD_Metadata]">
        <xsl:param name="schema" />
        <xsl:param name="edit" />

        <xsl:variable name="text">
            <xsl:call-template name="getElementText">
                <xsl:with-param name="edit" select="$edit" />
                <xsl:with-param name="visible" select="'true'" />
                <xsl:with-param name="schema" select="$schema" />
                <xsl:with-param name="validator" select="'validateGM03NAME(this)'" />
            </xsl:call-template>
        </xsl:variable>
        <xsl:apply-templates mode="simpleElement" select=".">
            <xsl:with-param name="schema" select="$schema" />
            <xsl:with-param name="edit" select="$edit" />
            <xsl:with-param name="title" select="'Name'" />
            <xsl:with-param name="text" select="$text" />
        </xsl:apply-templates>
    </xsl:template>

    <xsl:template mode="iso19139"
                  match="gco:MemberName/gco:aName[ancestor::che:CHE_MD_Metadata]"
                  priority="2">
        <xsl:param name="schema"/>
        <xsl:param name="edit"/>
        <xsl:variable name="langId">
            <xsl:call-template name="getLangId">
                <xsl:with-param name="langGui" select="/root/gui/language"/>
                <xsl:with-param name="md" select="ancestor-or-self::*[name(.)='gmd:MD_Metadata' or @gco:isoType='gmd:MD_Metadata']"/>
            </xsl:call-template>
        </xsl:variable>

        <xsl:call-template name="iso19139String">
            <xsl:with-param name="schema" select="$schema"/>
            <xsl:with-param name="edit" select="$edit"/>
            <xsl:with-param name="langId" select="$langId"/>
            <xsl:with-param name="validator" select="'validateGM03NAME(this)'" />
        </xsl:call-template>
    </xsl:template>




    <xsl:template mode="iso19139"
                  match="che:*[gco:Date|gco:DateTime|gco:Boolean|
        gco:Angle|gco:RecordType]"
            >
        <xsl:param name="schema"/>
        <xsl:param name="edit"/>
        <xsl:variable name="langId">
            <xsl:call-template name="getLangId">
                <xsl:with-param name="langGui" select="/root/gui/language"/>
                <xsl:with-param name="md" select="ancestor-or-self::*[name(.)='gmd:MD_Metadata' or @gco:isoType='gmd:MD_Metadata']"/>
            </xsl:call-template>
        </xsl:variable>

        <xsl:call-template name="iso19139String">
            <xsl:with-param name="schema" select="$schema"/>
            <xsl:with-param name="edit" select="$edit"/>
            <xsl:with-param name="langId" select="$langId"/>
        </xsl:call-template>
    </xsl:template>



    <xsl:template mode="iso19139"
                  match="che:*[gco:CharacterString]">
        <xsl:param name="schema" />
        <xsl:param name="edit" />

        <xsl:call-template name="localizedCharStringField">
            <xsl:with-param name="schema" select="$schema" />
            <xsl:with-param name="edit" select="$edit" />
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="che:PT_FreeURL_PropertyType"/>


    <xsl:template mode="iso19139" match="che:dateOfLastUpdate|che:dateOfMonitoringState" priority="2">
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

    <!-- ===================================================================== -->
    <!-- === CSV formatting for che profil. Mainly defined in order
        to do geobasic data monitoring as defined in exportAttributes.xls
        document. 														   === -->
    <!-- ===================================================================== -->

    <xsl:template match="che:CHE_MD_Metadata" mode="csv">
        <xsl:param name="internalSep"/>

        <metadata>
            <!-- Copy header -->
            <xsl:copy-of select="geonet:info"/>


            <!-- Identification -->
            <gmd:title>
                <xsl:for-each select="gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString|
					gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:title/gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString">
                    <xsl:value-of select="."/><xsl:if test="position()!=last()"><xsl:value-of select="$internalSep"/></xsl:if>
                </xsl:for-each>
            </gmd:title>

            <gmd:abstract>
                <xsl:for-each select="gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:abstract/gco:CharacterString|
					gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:abstract/gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString">
                    <xsl:value-of select="."/><xsl:if test="position()!=last()"><xsl:value-of select="$internalSep"/></xsl:if>
                </xsl:for-each>
            </gmd:abstract>

            <gmd:organisationName>
                <xsl:for-each select="gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:pointOfContact/che:CHE_CI_ResponsibleParty/gmd:organisationName/gco:CharacterString|
					gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:pointOfContact/che:CHE_CI_ResponsibleParty/gmd:organisationName/gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString">
                    <xsl:value-of select="."/><xsl:if test="position()!=last()"><xsl:value-of select="$internalSep"/></xsl:if>
                </xsl:for-each>
            </gmd:organisationName>

            <gmd:individualFirstName>
                <xsl:for-each select="gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:pointOfContact/che:CHE_CI_ResponsibleParty/che:individualFirstName/gco:CharacterString">
                    <xsl:value-of select="."/><xsl:if test="position()!=last()"><xsl:value-of select="$internalSep"/></xsl:if>
                </xsl:for-each>
            </gmd:individualFirstName>

            <gmd:individualLastName>
                <xsl:for-each select="gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:pointOfContact/che:CHE_CI_ResponsibleParty/che:individualLastName/gco:CharacterString">
                    <xsl:value-of select="."/><xsl:if test="position()!=last()"><xsl:value-of select="$internalSep"/></xsl:if>
                </xsl:for-each>
            </gmd:individualLastName>

            <gmd:electronicMailAddress>
                <xsl:for-each select="gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:pointOfContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/gmd:electronicMailAddress/gco:CharacterString">
                    <xsl:value-of select="."/><xsl:if test="position()!=last()"><xsl:value-of select="$internalSep"/></xsl:if>
                </xsl:for-each>
            </gmd:electronicMailAddress>

            <gmd:fileIdentifier>
                <xsl:value-of select="gmd:fileIdentifier/gco:CharacterString"/>
            </gmd:fileIdentifier>
            <gmd:dateStamp>
                <xsl:value-of select="gmd:dateStamp/gco:DateTime"/>
            </gmd:dateStamp>

            <!-- Basic geodata -->
            <che:basicGeodataID><xsl:value-of select="gmd:identificationInfo/che:CHE_MD_DataIdentification/che:basicGeodataID/gco:CharacterString"/></che:basicGeodataID>>

            <che:basicGeodataIDType><xsl:value-of select="gmd:identificationInfo/che:CHE_MD_DataIdentification/che:basicGeodataIDType/che:basicGeodataIDTypeCode/@codeListValue"/></che:basicGeodataIDType>>

            <che:dateOfMonitoringState>
                <xsl:for-each select="gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:resourceMaintenance/che:CHE_MD_MaintenanceInformation/che:dateOfMonitoringState/gco:Date">
                    <xsl:value-of select="."/><xsl:if test="position()!=last()"><xsl:value-of select="$internalSep"/></xsl:if>
                </xsl:for-each>
            </che:dateOfMonitoringState>

            <!-- Objektkatalog -->
            <xsl:choose>
                <xsl:when test="count(gmd:contentInfo/che:CHE_MD_FeatureCatalogueDescription[che:modelType/che:CHE_MD_modelTypeCode/@codeListValue='FeatureDescription'])=1">
                    <xsl:variable name="fc" select="gmd:contentInfo/che:CHE_MD_FeatureCatalogueDescription[che:modelType/che:CHE_MD_modelTypeCode/@codeListValue='FeatureDescription']"></xsl:variable>
                    <che:featureCat.FeatureDescription.title>
                        <xsl:for-each select="$fc/gmd:featureCatalogueCitation/gmd:CI_Citation/gmd:title/gco:CharacterString|
							$fc/gmd:featureCatalogueCitation/gmd:CI_Citation/gmd:title/gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString">
                            <xsl:value-of select="."/><xsl:if test="position()!=last()"><xsl:value-of select="$internalSep"/></xsl:if>
                        </xsl:for-each>
                    </che:featureCat.FeatureDescription.title>

                    <che:featureCat.FeatureDescription.otherCitationDetails>
                        <xsl:for-each select="$fc/gmd:featureCatalogueCitation/gmd:CI_Citation/gmd:otherCitationDetails/gco:CharacterString|
							$fc/gmd:featureCatalogueCitation/gmd:CI_Citation/gmd:otherCitationDetails/gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString">
                            <xsl:value-of select="."/><xsl:if test="position()!=last()"><xsl:value-of select="$internalSep"/></xsl:if>
                        </xsl:for-each>
                    </che:featureCat.FeatureDescription.otherCitationDetails>

                    <che:featureCat.FeatureDescription.date>
                        <xsl:for-each select="$fc/gmd:featureCatalogueCitation/gmd:CI_Citation//gmd:CI_Date">
                            <xsl:value-of select="gmd:date/gco:Date"/><xsl:text> </xsl:text><xsl:value-of select="gmd:dateType/gmd:CI_DateTypeCode/@codeListValue"/><xsl:if test="position()!=last()"><xsl:value-of select="$internalSep"/></xsl:if>
                        </xsl:for-each>
                    </che:featureCat.FeatureDescription.date>

                    <che:featureCat.FeatureDescription.url>
                        <xsl:for-each select="$fc/che:dataModel/che:PT_FreeURL/che:URLGroup/che:LocalisedURL">
                            <xsl:value-of select="."/><xsl:if test="position()!=last()"><xsl:value-of select="$internalSep"/></xsl:if>
                        </xsl:for-each>
                    </che:featureCat.FeatureDescription.url>
                </xsl:when>
                <xsl:otherwise>
                    <featureCat.FeatureDescription.title/>
                    <featureCat.FeatureDescription.otherCitationDetails/>
                    <featureCat.FeatureDescription.date/>
                    <featureCat.FeatureDescription.url/>
                </xsl:otherwise>
            </xsl:choose>
            <xsl:choose>
                <xsl:when test="count(gmd:contentInfo/che:CHE_MD_FeatureCatalogueDescription[che:modelType/che:CHE_MD_modelTypeCode/@codeListValue='UMLdiagram'])=1">
                    <xsl:variable name="fc" select="gmd:contentInfo/che:CHE_MD_FeatureCatalogueDescription[che:modelType/che:CHE_MD_modelTypeCode/@codeListValue='UMLdiagram']"></xsl:variable>
                    <che:featureCat.UMLdiagram.title>
                        <xsl:for-each select="$fc/gmd:featureCatalogueCitation/gmd:CI_Citation/gmd:title/gco:CharacterString|
							$fc/gmd:featureCatalogueCitation/gmd:CI_Citation/gmd:title/gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString">
                            <xsl:value-of select="."/><xsl:if test="position()!=last()"><xsl:value-of select="$internalSep"/></xsl:if>
                        </xsl:for-each>
                    </che:featureCat.UMLdiagram.title>

                    <che:featureCat.UMLdiagram.otherCitationDetails>
                        <xsl:for-each select="$fc/gmd:featureCatalogueCitation/gmd:CI_Citation/gmd:otherCitationDetails/gco:CharacterString|
							$fc/gmd:featureCatalogueCitation/gmd:CI_Citation/gmd:otherCitationDetails/gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString">
                            <xsl:value-of select="."/><xsl:if test="position()!=last()"><xsl:value-of select="$internalSep"/></xsl:if>
                        </xsl:for-each>
                    </che:featureCat.UMLdiagram.otherCitationDetails>

                    <che:featureCat.UMLdiagram.date>
                        <xsl:for-each select="$fc/gmd:featureCatalogueCitation/gmd:CI_Citation//gmd:CI_Date">
                            <xsl:value-of select="gmd:date/gco:Date"/><xsl:text> </xsl:text><xsl:value-of select="gmd:dateType/gmd:CI_DateTypeCode/@codeListValue"/><xsl:if test="position()!=last()"><xsl:value-of select="$internalSep"/></xsl:if>
                        </xsl:for-each>
                    </che:featureCat.UMLdiagram.date>

                    <che:featureCat.UMLdiagram.url>
                        <xsl:for-each select="$fc/che:dataModel/che:PT_FreeURL/che:URLGroup/che:LocalisedURL">
                            <xsl:value-of select="."/><xsl:if test="position()!=last()"><xsl:value-of select="$internalSep"/></xsl:if>
                        </xsl:for-each>
                    </che:featureCat.UMLdiagram.url>
                </xsl:when>
                <xsl:otherwise>
                    <featureCat.UMLdiagram.title/>
                    <featureCat.UMLdiagram.otherCitationDetails/>
                    <featureCat.UMLdiagram.date/>
                    <featureCat.UMLdiagram.url/>
                </xsl:otherwise>
            </xsl:choose>
            <xsl:choose>
                <xsl:when test="count(gmd:contentInfo/che:CHE_MD_FeatureCatalogueDescription[che:modelType/che:CHE_MD_modelTypeCode/@codeListValue='INTERLIS1'])=1">
                    <xsl:variable name="fc" select="gmd:contentInfo/che:CHE_MD_FeatureCatalogueDescription[che:modelType/che:CHE_MD_modelTypeCode/@codeListValue='INTERLIS1']"></xsl:variable>
                    <che:featureCat.INTERLIS1.title>
                        <xsl:for-each select="$fc/gmd:featureCatalogueCitation/gmd:CI_Citation/gmd:title/gco:CharacterString|
							$fc/gmd:featureCatalogueCitation/gmd:CI_Citation/gmd:title/gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString">
                            <xsl:value-of select="."/><xsl:if test="position()!=last()"><xsl:value-of select="$internalSep"/></xsl:if>
                        </xsl:for-each>
                    </che:featureCat.INTERLIS1.title>

                    <che:featureCat.INTERLIS1.otherCitationDetails>
                        <xsl:for-each select="$fc/gmd:featureCatalogueCitation/gmd:CI_Citation/gmd:otherCitationDetails/gco:CharacterString|
							$fc/gmd:featureCatalogueCitation/gmd:CI_Citation/gmd:otherCitationDetails/gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString">
                            <xsl:value-of select="."/><xsl:if test="position()!=last()"><xsl:value-of select="$internalSep"/></xsl:if>
                        </xsl:for-each>
                    </che:featureCat.INTERLIS1.otherCitationDetails>

                    <che:featureCat.INTERLIS1.date>
                        <xsl:for-each select="$fc/gmd:featureCatalogueCitation/gmd:CI_Citation//gmd:CI_Date">
                            <xsl:value-of select="gmd:date/gco:Date"/><xsl:text> </xsl:text><xsl:value-of select="gmd:dateType/gmd:CI_DateTypeCode/@codeListValue"/><xsl:if test="position()!=last()"><xsl:value-of select="$internalSep"/></xsl:if>
                        </xsl:for-each>
                    </che:featureCat.INTERLIS1.date>

                    <che:featureCat.INTERLIS1.url>
                        <xsl:for-each select="$fc/che:dataModel/che:PT_FreeURL/che:URLGroup/che:LocalisedURL">
                            <xsl:value-of select="."/><xsl:if test="position()!=last()"><xsl:value-of select="$internalSep"/></xsl:if>
                        </xsl:for-each>
                    </che:featureCat.INTERLIS1.url>
                </xsl:when>
                <xsl:otherwise>
                    <featureCat.INTERLIS1.title/>
                    <featureCat.INTERLIS1.otherCitationDetails/>
                    <featureCat.INTERLIS1.date/>
                    <featureCat.INTERLIS1.url/>
                </xsl:otherwise>
            </xsl:choose>
            <xsl:choose>
                <xsl:when test="count(gmd:contentInfo/che:CHE_MD_FeatureCatalogueDescription[che:modelType/che:CHE_MD_modelTypeCode/@codeListValue='INTERLIS2'])=1">
                    <xsl:variable name="fc" select="gmd:contentInfo/che:CHE_MD_FeatureCatalogueDescription[che:modelType/che:CHE_MD_modelTypeCode/@codeListValue='INTERLIS2']"></xsl:variable>
                    <che:featureCat.INTERLIS2.title>
                        <xsl:for-each select="$fc/gmd:featureCatalogueCitation/gmd:CI_Citation/gmd:title/gco:CharacterString|
							$fc/gmd:featureCatalogueCitation/gmd:CI_Citation/gmd:title/gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString">
                            <xsl:value-of select="."/><xsl:if test="position()!=last()"><xsl:value-of select="$internalSep"/></xsl:if>
                        </xsl:for-each>
                    </che:featureCat.INTERLIS2.title>

                    <che:featureCat.INTERLIS2.otherCitationDetails>
                        <xsl:for-each select="$fc/gmd:featureCatalogueCitation/gmd:CI_Citation/gmd:otherCitationDetails/gco:CharacterString|
							$fc/gmd:featureCatalogueCitation/gmd:CI_Citation/gmd:otherCitationDetails/gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString">
                            <xsl:value-of select="."/><xsl:if test="position()!=last()"><xsl:value-of select="$internalSep"/></xsl:if>
                        </xsl:for-each>
                    </che:featureCat.INTERLIS2.otherCitationDetails>

                    <che:featureCat.INTERLIS2.date>
                        <xsl:for-each select="$fc/gmd:featureCatalogueCitation/gmd:CI_Citation//gmd:CI_Date">
                            <xsl:value-of select="gmd:date/gco:Date"/><xsl:text> </xsl:text><xsl:value-of select="gmd:dateType/gmd:CI_DateTypeCode/@codeListValue"/><xsl:if test="position()!=last()"><xsl:value-of select="$internalSep"/></xsl:if>
                        </xsl:for-each>
                    </che:featureCat.INTERLIS2.date>

                    <che:featureCat.INTERLIS2.url>
                        <xsl:for-each select="$fc/che:dataModel/che:PT_FreeURL/che:URLGroup/che:LocalisedURL">
                            <xsl:value-of select="."/><xsl:if test="position()!=last()"><xsl:value-of select="$internalSep"/></xsl:if>
                        </xsl:for-each>
                    </che:featureCat.INTERLIS2.url>
                </xsl:when>
                <xsl:otherwise>
                    <featureCat.INTERLIS2.title/>
                    <featureCat.INTERLIS2.otherCitationDetails/>
                    <featureCat.INTERLIS2.date/>
                    <featureCat.INTERLIS2.url/>
                </xsl:otherwise>
            </xsl:choose>

            <!-- Portrayal catalogue info -->
            <che:portrayalCat.title>
                <xsl:for-each select="gmd:portrayalCatalogueInfo/che:CHE_MD_PortrayalCatalogueReference/gmd:portrayalCatalogueCitation/gmd:CI_Citation/gmd:title/gco:CharacterString|
					gmd:portrayalCatalogueInfo/gmd:MD_PortrayalCatalogueReference/gmd:portrayalCatalogueCitation/gmd:CI_Citation/gmd:title/gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString">
                    <xsl:value-of select="."/><xsl:if test="position()!=last()"><xsl:value-of select="$internalSep"/></xsl:if>
                </xsl:for-each>
            </che:portrayalCat.title>

            <che:portrayalCat.date>
                <xsl:for-each select="gmd:portrayalCatalogueInfo/che:CHE_MD_PortrayalCatalogueReference/gmd:portrayalCatalogueCitation/gmd:CI_Citation//gmd:CI_Date">
                    <xsl:value-of select="gmd:date/gco:Date"/><xsl:text> </xsl:text><xsl:value-of select="gmd:dateType/gmd:CI_DateTypeCode/@codeListValue"/><xsl:if test="position()!=last()"><xsl:value-of select="$internalSep"/></xsl:if>
                </xsl:for-each>
            </che:portrayalCat.date>

            <che:portrayalCat.otherCitationDetails>
                <xsl:for-each select="gmd:portrayalCatalogueInfo/che:CHE_MD_PortrayalCatalogueReference/gmd:portrayalCatalogueCitation/gmd:CI_Citation/gmd:otherCitationDetails/gco:CharacterString|
					gmd:portrayalCatalogueInfo/gmd:MD_PortrayalCatalogueReference/gmd:portrayalCatalogueCitation/gmd:CI_Citation/gmd:otherCitationDetails/gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString">
                    <xsl:value-of select="."/><xsl:if test="position()!=last()"><xsl:value-of select="$internalSep"/></xsl:if>
                </xsl:for-each>
            </che:portrayalCat.otherCitationDetails>

            <che:portrayalCatalogueURL>
                <xsl:for-each select="gmd:portrayalCatalogueInfo/che:CHE_MD_PortrayalCatalogueReference/che:portrayalCatalogueURL/che:PT_FreeURL/che:URLGroup/che:LocalisedURL">
                    <xsl:value-of select="."/><xsl:if test="position()!=last()"><xsl:value-of select="$internalSep"/></xsl:if>
                </xsl:for-each>
            </che:portrayalCatalogueURL>

            <!-- Maintenance -->
            <gmd:maintenanceAndUpdateFrequency>
                <xsl:for-each select="gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:resourceMaintenance/che:CHE_MD_MaintenanceInformation/gmd:maintenanceAndUpdateFrequency/gmd:MD_MaintenanceFrequencyCode/@codeListValue">
                    <xsl:value-of select="."/><xsl:if test="position()!=last()"><xsl:value-of select="$internalSep"/></xsl:if>
                </xsl:for-each>
            </gmd:maintenanceAndUpdateFrequency>

            <gmd:userDefinedMaintenanceFrequency>
                <xsl:for-each select="gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:resourceMaintenance/che:CHE_MD_MaintenanceInformation/gmd:userDefinedMaintenanceFrequency/gts:TM_PeriodDuration">
                    <xsl:value-of select="."/><xsl:if test="position()!=last()"><xsl:value-of select="$internalSep"/></xsl:if>
                </xsl:for-each>
            </gmd:userDefinedMaintenanceFrequency>

            <gmd:maintenanceNote>
                <xsl:for-each select="gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:resourceMaintenance/che:CHE_MD_MaintenanceInformation/gmd:maintenanceNote/gco:CharacterString|
					gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:resourceMaintenance/gmd:MD_MaintenanceInformation/gmd:maintenanceNote/gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString">
                    <xsl:value-of select="."/><xsl:if test="position()!=last()"><xsl:value-of select="$internalSep"/></xsl:if>
                </xsl:for-each>
            </gmd:maintenanceNote>


            <!-- History info -->
            <che:historyConcept.title>
                <xsl:for-each select="gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:resourceMaintenance/
					che:CHE_MD_MaintenanceInformation/che:historyConcept/che:CHE_MD_HistoryConcept/ 
					che:historyConceptCitation/gmd:CI_Citation/gmd:title/gco:CharacterString|
					gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:resourceMaintenance/
					che:CHE_MD_MaintenanceInformation/che:historyConcept/che:CHE_MD_HistoryConcept/ 
					che:historyConceptCitation/gmd:CI_Citation/gmd:title/gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString">
                    <xsl:value-of select="."/><xsl:if test="position()!=last()"><xsl:value-of select="$internalSep"/></xsl:if>
                </xsl:for-each>
            </che:historyConcept.title>

            <che:historyConcept.otherCitationDetails>
                <xsl:for-each select="gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:resourceMaintenance/
					che:CHE_MD_MaintenanceInformation/che:historyConcept/che:CHE_MD_HistoryConcept/ 
					che:historyConceptCitation/gmd:CI_Citation/gmd:otherCitationDetails/gco:CharacterString|
					gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:resourceMaintenance/
					che:CHE_MD_MaintenanceInformation/che:historyConcept/che:CHE_MD_HistoryConcept/ 
					che:historyConceptCitation/gmd:CI_Citation/gmd:otherCitationDetails/gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString">
                    <xsl:value-of select="."/><xsl:if test="position()!=last()"><xsl:value-of select="$internalSep"/></xsl:if>
                </xsl:for-each>
            </che:historyConcept.otherCitationDetails>

            <che:historyConcept.date>
                <xsl:for-each select="gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:resourceMaintenance/
					che:CHE_MD_MaintenanceInformation/che:historyConcept/che:CHE_MD_HistoryConcept/ 
					che:historyConceptCitation/gmd:CI_Citation//gmd:CI_Date">
                    <xsl:value-of select="gmd:date/gco:Date"/><xsl:text> </xsl:text><xsl:value-of select="gmd:dateType/gmd:CI_DateTypeCode/@codeListValue"/><xsl:if test="position()!=last()"><xsl:value-of select="$internalSep"/></xsl:if>
                </xsl:for-each>
            </che:historyConcept.date>

            <che:historyConcept.LocalisedURL>
                <xsl:for-each select="gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:resourceMaintenance/
					che:CHE_MD_MaintenanceInformation/che:historyConcept/che:CHE_MD_HistoryConcept/che:historyConceptURL/che:LocalisedURL">
                    <xsl:value-of select="."/><xsl:if test="position()!=last()"><xsl:value-of select="$internalSep"/></xsl:if>
                </xsl:for-each>
            </che:historyConcept.LocalisedURL>

            <!-- Archive info -->
            <che:archiveConcept.title>
                <xsl:for-each select="gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:resourceMaintenance/
					che:CHE_MD_MaintenanceInformation/che:archiveConcept/che:CHE_MD_ArchiveConcept/ 
					che:archiveConceptCitation/gmd:CI_Citation/gmd:title/gco:CharacterString|
					gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:resourceMaintenance/
					che:CHE_MD_MaintenanceInformation/che:archiveConcept/che:CHE_MD_ArchiveConcept/ 
					che:archiveConceptCitation/gmd:CI_Citation/gmd:title/gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString">
                    <xsl:value-of select="."/><xsl:if test="position()!=last()"><xsl:value-of select="$internalSep"/></xsl:if>
                </xsl:for-each>
            </che:archiveConcept.title>

            <che:archiveConcept.otherCitationDetails>
                <xsl:for-each select="gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:resourceMaintenance/
					che:CHE_MD_MaintenanceInformation/che:archiveConcept/che:CHE_MD_ArchiveConcept/ 
					che:archiveConceptCitation/gmd:CI_Citation/gmd:otherCitationDetails/gco:CharacterString|
					gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:resourceMaintenance/
					che:CHE_MD_MaintenanceInformation/che:archiveConcept/che:CHE_MD_ArchiveConcept/ 
					che:archiveConceptCitation/gmd:CI_Citation/gmd:otherCitationDetails/gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString">
                    <xsl:value-of select="."/><xsl:if test="position()!=last()"><xsl:value-of select="$internalSep"/></xsl:if>
                </xsl:for-each>
            </che:archiveConcept.otherCitationDetails>

            <che:archiveConcept.date>
                <xsl:for-each select="gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:resourceMaintenance/
					che:CHE_MD_MaintenanceInformation/che:archiveConcept/che:CHE_MD_ArchiveConcept/ 
					che:archiveConceptCitation/gmd:CI_Citation//gmd:CI_Date">
                    <xsl:value-of select="gmd:date/gco:Date"/><xsl:text> </xsl:text><xsl:value-of select="gmd:dateType/gmd:CI_DateTypeCode/@codeListValue"/><xsl:if test="position()!=last()"><xsl:value-of select="$internalSep"/></xsl:if>
                </xsl:for-each>
            </che:archiveConcept.date>

            <che:archiveConcept.LocalisedURL>
                <xsl:for-each select="gmd:identificationInfo/che:CHE_MD_DataIdentification/gmd:resourceMaintenance/
					che:CHE_MD_MaintenanceInformation/che:archiveConcept/che:CHE_MD_ArchiveConcept/che:archiveConceptURL/che:LocalisedURL">
                    <xsl:value-of select="."/><xsl:if test="position()!=last()"><xsl:value-of select="$internalSep"/></xsl:if>
                </xsl:for-each>
            </che:archiveConcept.LocalisedURL>



            <!-- Metadata Maintenance -->
            <gmd:metadata.maintenanceAndUpdateFrequency>
                <xsl:for-each select="gmd:metadataMaintenance/che:CHE_MD_MaintenanceInformation/gmd:maintenanceAndUpdateFrequency/gmd:MD_MaintenanceFrequencyCode/@codeListValue">
                    <xsl:value-of select="."/><xsl:if test="position()!=last()"><xsl:value-of select="$internalSep"/></xsl:if>
                </xsl:for-each>
            </gmd:metadata.maintenanceAndUpdateFrequency>

            <gmd:metadata.userDefinedMaintenanceFrequency>
                <xsl:for-each select="gmd:metadataMaintenance/che:CHE_MD_MaintenanceInformation/gmd:userDefinedMaintenanceFrequency/gts:TM_PeriodDuration">
                    <xsl:value-of select="."/><xsl:if test="position()!=last()"><xsl:value-of select="$internalSep"/></xsl:if>
                </xsl:for-each>
            </gmd:metadata.userDefinedMaintenanceFrequency>

            <gmd:metadata.maintenanceNote>
                <xsl:for-each select="gmd:metadataMaintenance/che:CHE_MD_MaintenanceInformation/gmd:maintenanceNote/gco:CharacterString|
					gmd:metadataMaintenance/che:CHE_MD_MaintenanceInformation/gmd:maintenanceNote/gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString">
                    <xsl:value-of select="."/><xsl:if test="position()!=last()"><xsl:value-of select="$internalSep"/></xsl:if>
                </xsl:for-each>
            </gmd:metadata.maintenanceNote>




            <!-- Distribution info -->
            <gmd:onlineSrc>
                <xsl:for-each select="gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource/gmd:linkage/che:LocalisedURL">
                    <xsl:value-of select="."/><xsl:if test="position()!=last()"><xsl:value-of select="$internalSep"/></xsl:if>
                </xsl:for-each>
            </gmd:onlineSrc>



            <!-- Service metadata -->

            <srv:service.title>
                <xsl:for-each select="gmd:identificationInfo/che:CHE_SV_ServiceIdentification/gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString|
					gmd:identificationInfo/che:CHE_SV_ServiceIdentification/gmd:citation/gmd:CI_Citation/gmd:title/gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString">
                    <xsl:value-of select="."/><xsl:if test="position()!=last()"><xsl:value-of select="$internalSep"/></xsl:if>
                </xsl:for-each>
            </srv:service.title>

            <srv:service.abstract>
                <xsl:for-each select="gmd:identificationInfo/che:CHE_SV_ServiceIdentification/gmd:abstract/gco:CharacterString|
					gmd:identificationInfo/che:CHE_SV_ServiceIdentification/gmd:abstract/gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString">
                    <xsl:value-of select="."/><xsl:if test="position()!=last()"><xsl:value-of select="$internalSep"/></xsl:if>
                </xsl:for-each>
            </srv:service.abstract>

            <srv:service.organisationName>
                <xsl:for-each select="gmd:identificationInfo/che:CHE_SV_ServiceIdentification/gmd:pointOfContact/che:CHE_CI_ResponsibleParty/gmd:organisationName/gco:CharacterString|
					gmd:identificationInfo/che:CHE_SV_ServiceIdentification/gmd:pointOfContact/che:CHE_CI_ResponsibleParty/gmd:organisationName/gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString">
                    <xsl:value-of select="."/><xsl:if test="position()!=last()"><xsl:value-of select="$internalSep"/></xsl:if>
                </xsl:for-each>
            </srv:service.organisationName>

            <srv:service.individualLastName>
                <xsl:for-each select="gmd:identificationInfo/che:CHE_SV_ServiceIdentification/gmd:pointOfContact/che:CHE_CI_ResponsibleParty/che:individualLastName/gco:CharacterString">
                    <xsl:value-of select="."/><xsl:if test="position()!=last()"><xsl:value-of select="$internalSep"/></xsl:if>
                </xsl:for-each>
            </srv:service.individualLastName>

            <srv:service.individualFirstName>
                <xsl:for-each select="gmd:identificationInfo/che:CHE_SV_ServiceIdentification/gmd:pointOfContact/che:CHE_CI_ResponsibleParty/che:individualFirstName/gco:CharacterString">
                    <xsl:value-of select="."/><xsl:if test="position()!=last()"><xsl:value-of select="$internalSep"/></xsl:if>
                </xsl:for-each>
            </srv:service.individualFirstName>

            <srv:service.electronicMailAddress>
                <xsl:for-each select="gmd:identificationInfo/che:CHE_SV_ServiceIdentification/gmd:pointOfContact/che:CHE_CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/che:CHE_CI_Address/gmd:electronicMailAddress/gco:CharacterString">
                    <xsl:value-of select="."/><xsl:if test="position()!=last()"><xsl:value-of select="$internalSep"/></xsl:if>
                </xsl:for-each>
            </srv:service.electronicMailAddress>

            <srv:serviceType>
                <xsl:for-each select="gmd:identificationInfo/che:CHE_SV_ServiceIdentification/srv:serviceType/gco:LocalName">
                    <xsl:value-of select="."/><xsl:if test="position()!=last()"><xsl:value-of select="$internalSep"/></xsl:if>
                </xsl:for-each>
            </srv:serviceType>

            <srv:serviceTypeVersion>
                <xsl:for-each select="gmd:identificationInfo/che:CHE_SV_ServiceIdentification/srv:serviceTypeVersion/gco:CharacterString">
                    <xsl:value-of select="."/><xsl:if test="position()!=last()"><xsl:value-of select="$internalSep"/></xsl:if>
                </xsl:for-each>
            </srv:serviceTypeVersion>

        </metadata>
    </xsl:template>


    <!-- GM03 unique value for units is Interlis.m. -->
    <xsl:template mode="simpleElement" match="@uom" priority="2">
        <xsl:param name="schema"/>
        <xsl:param name="edit"   select="false()"/>
        <xsl:param name="title">
            <xsl:call-template name="getTitle">
                <xsl:with-param name="name"   select="name(.)"/>
                <xsl:with-param name="schema" select="$schema"/>
            </xsl:call-template>
        </xsl:param>
        <xsl:param name="text">
            <xsl:choose>
                <xsl:when test="$edit=true()">
                    m
                    <input class="md" type="hidden" id="_{../geonet:element/@ref}_uom" name="_{../geonet:element/@ref}_uom" value="m" readonly="1"/>
                </xsl:when>
                <xsl:otherwise><xsl:value-of select="."/></xsl:otherwise>
            </xsl:choose>
        </xsl:param>
        <xsl:param name="helpLink">
            <xsl:call-template name="getHelpLink">
                <xsl:with-param name="name"   select="name(.)"/>
                <xsl:with-param name="schema" select="$schema"/>
            </xsl:call-template>
        </xsl:param>

        <xsl:choose>
            <xsl:when test="$edit=true()">
                <xsl:call-template name="editAttribute">
                    <xsl:with-param name="schema"   select="$schema"/>
                    <xsl:with-param name="title"    select="$title"/>
                    <xsl:with-param name="text"     select="$text"/>
                    <xsl:with-param name="helpLink" select="$helpLink"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:call-template name="showSimpleElement">
                    <xsl:with-param name="schema"   select="$schema"/>
                    <xsl:with-param name="title"    select="$title"/>
                    <xsl:with-param name="text"     select="$text"/>
                    <xsl:with-param name="helpLink" select="$helpLink"/>
                </xsl:call-template>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>


    <!-- ===================================================================== -->
    <!-- === iso19139 brief formatting === -->
    <!-- ===================================================================== -->

    <xsl:template name="iso19139.cheBrief">
        <metadata>
            <xsl:choose>
                <xsl:when test="geonet:info/isTemplate='s'">
                    <xsl:apply-templates mode="iso19139-subtemplate" select="."/>
                    <xsl:copy-of select="geonet:info" copy-namespaces="no"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:call-template name="iso19139.che-brief"/>
                </xsl:otherwise>
            </xsl:choose>
        </metadata>
    </xsl:template>


    <xsl:template name="iso19139.che-brief">
        <xsl:variable name="download_check"><xsl:text>&amp;fname=&amp;access</xsl:text></xsl:variable>
        <xsl:variable name="info" select="geonet:info"/>
        <xsl:variable name="id" select="$info/id"/>
        <xsl:variable name="uuid" select="$info/uuid"/>

        <xsl:if test="normalize-space(gmd:parentIdentifier/gco:CharacterString)!=''">
            <parentId><xsl:value-of select="gmd:parentIdentifier/*"/></parentId>
        </xsl:if>

        <xsl:variable name="langId">
            <xsl:call-template name="getLangId">
                <xsl:with-param name="langGui" select="/root/gui/language"/>
                <xsl:with-param name="md" select="."/>
            </xsl:call-template>
        </xsl:variable>

        <xsl:apply-templates mode="briefster" select="gmd:identificationInfo/gmd:MD_DataIdentification|gmd:identificationInfo/*[@gco:isoType='gmd:MD_DataIdentification']|gmd:identificationInfo/srv:SV_ServiceIdentification|gmd:identificationInfo/*[@gco:isoType='srv:SV_ServiceIdentification']">
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
                    <xsl:attribute name="title"><xsl:value-of select="$desc"/></xsl:attribute>
                    <xsl:attribute name="href"><xsl:value-of select="$linkage"/></xsl:attribute>
                    <xsl:attribute name="name"><xsl:value-of select="$name"/></xsl:attribute>
                    <xsl:attribute name="protocol"><xsl:value-of select="$protocol"/></xsl:attribute>
                    <xsl:attribute name="type" select="geonet:protocolMimeType($linkage, $protocol, $mimeType)"/>
                </xsl:element>
            </xsl:if>

            <!-- Generate a KML output link for a WMS service -->
            <xsl:if test="string($linkage)!='' and starts-with($protocol,'OGC:WMS-') and contains($protocol,'-get-map') and string($linkage)!='' and string($name)!=''">

                <xsl:element name="link">
                    <xsl:attribute name="title"><xsl:value-of select="$desc"/></xsl:attribute>
                    <xsl:attribute name="href">
                        <xsl:value-of select="concat(/root/gui/env/server/protocol,'://',/root/gui/env/server/host,':',/root/gui/env/server/port,/root/gui/locService,'/google.kml?uuid=',$uuid,'&amp;layers=',$name)"/>
                    </xsl:attribute>
                    <xsl:attribute name="name"><xsl:value-of select="$name"/></xsl:attribute>
                    <xsl:attribute name="type">application/vnd.google-earth.kml+xml</xsl:attribute>
                </xsl:element>
            </xsl:if>
        </xsl:for-each>

        <xsl:for-each select="gmd:contact/*">
            <xsl:variable name="role" select="gmd:role/*/@codeListValue"/>
            <xsl:if test="normalize-space($role)!=''">
                <responsibleParty role="{geonet:getCodeListValue(/root/gui/schemas, 'iso19139', 'gmd:CI_RoleCode', $role)}" appliesTo="metadata">
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
                <xsl:when test="/root/gui/env/harvester/enableEditing='false' and geonet:info/isHarvested='y'">
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
                    <xsl:when test="gmd:identificationInfo/srv:SV_ServiceIdentification|gmd:identificationInfo/*[@gco:isoType='srv:SV_ServiceIdentification']">service</xsl:when>
                    <xsl:otherwise>dataset</xsl:otherwise>
                </xsl:choose>
            </category>
        </geonet:info>
    </xsl:template>

    <xsl:template mode="addXMLFragment" match="gmd:referenceSystemInfo|geonet:child[@name='referenceSystemInfo' and @prefix='gmd']" priority="100"/>


    <xsl:template mode="iso19139" match="gmd:EX_BoundingPolygon" priority="40">
        <xsl:param name="schema" />
        <xsl:param name="edit" />
        <xsl:apply-templates mode="iso19139" select="gmd:extentTypeCode">
            <xsl:with-param name="schema" select="$schema" />
            <xsl:with-param name="edit" select="$edit" />
        </xsl:apply-templates>


        <xsl:apply-templates mode="iso19139" select="gmd:polygon">
            <xsl:with-param name="schema" select="$schema" />
            <xsl:with-param name="edit" select="$edit" />
        </xsl:apply-templates>
    </xsl:template>

    <xsl:template mode="iso19139" match="gmd:polygon"
                  priority="40">
        <xsl:param name="schema" />
        <xsl:param name="edit" />

        <xsl:variable name="targetId" select="geonet:element/@ref"/>
        <xsl:variable name="geometry">
            <xsl:apply-templates mode="editXMLElement"/>
        </xsl:variable>

        <xsl:apply-templates mode="complexElement" select=".">
            <xsl:with-param name="schema" select="$schema" />
            <xsl:with-param name="edit" select="$edit" />
            <xsl:with-param name="content">
                <xsl:if test="$edit='true'">
                    <input type="hidden" id="_X{$targetId}" name="_X{$targetId}" value="{string($geometry)}"/>
                </xsl:if>
                <td class="padded" align="center" style="width:100%;">
                    <xsl:variable name="geom" select="util:gmlToWKT($geometry)"/>
                    <xsl:call-template name="showMap">
                        <xsl:with-param name="edit" select="$edit" />
                        <xsl:with-param name="coords" select="$geom"/>
                        <xsl:with-param name="targetPolygon" select="$targetId"/>
                    </xsl:call-template>
                </td>
            </xsl:with-param>
        </xsl:apply-templates>
    </xsl:template>


    <xsl:template mode="iso19139" match="gmd:EX_GeographicBoundingBox[../../gmd:geographicElement/gmd:EX_BoundingPolygon]"
                  priority="23">
        <!-- don't display bounding boxes when there is a bounding polygon. It's
             managed behind the scene by the server automatically-->
    </xsl:template>

    <xsl:template mode="iso19139" match="gmd:EX_GeographicBoundingBox"
                  priority="20">
        <xsl:param name="schema" />
        <xsl:param name="edit" />

        <xsl:apply-templates mode="iso19139" select="gmd:extentTypeCode">
            <xsl:with-param name="schema" select="$schema" />
            <xsl:with-param name="edit" select="$edit" />
        </xsl:apply-templates>

        <xsl:variable name="geoBox">
            <xsl:apply-templates mode="iso19139GeoBox"
                                 select=".">
                <xsl:with-param name="schema" select="$schema" />
                <xsl:with-param name="edit" select="$edit" />
            </xsl:apply-templates>
        </xsl:variable>

        <xsl:apply-templates mode="complexElement"
                             select=".">
            <xsl:with-param name="schema" select="$schema" />
            <xsl:with-param name="edit" select="$edit" />
            <xsl:with-param name="content">
                <tr>
                    <td>
                        <xsl:copy-of select="$geoBox" />
                    </td>
                </tr>
            </xsl:with-param>
        </xsl:apply-templates>
    </xsl:template>

    <!--
        =============================================================================
    -->
    <xsl:template mode="iso19139" priority="20" match="
		gmd:topicCategory[normalize-space(gmd:MD_TopicCategoryCode)='environment' and 
		(preceding-sibling::gmd:topicCategory[starts-with(normalize-space(gmd:MD_TopicCategoryCode), 'environment')] or 
		following-sibling::gmd:topicCategory[starts-with(normalize-space(gmd:MD_TopicCategoryCode), 'environment')])] "/>
    <xsl:template mode="iso19139" priority="20" match="
		gmd:topicCategory[normalize-space(gmd:MD_TopicCategoryCode)='geoscientificInformation' and 
		(preceding-sibling::gmd:topicCategory[starts-with(normalize-space(gmd:MD_TopicCategoryCode), 'geoscientificInformation')] or 
		following-sibling::gmd:topicCategory[starts-with(normalize-space(gmd:MD_TopicCategoryCode), 'geoscientificInformation')])] "/>
    <xsl:template mode="iso19139" priority="20" match="
		gmd:topicCategory[normalize-space(gmd:MD_TopicCategoryCode)='planningCadastre' and 
		(preceding-sibling::gmd:topicCategory[starts-with(normalize-space(gmd:MD_TopicCategoryCode), 'planningCadastre')] or 
		following-sibling::gmd:topicCategory[starts-with(normalize-space(gmd:MD_TopicCategoryCode), 'planningCadastre')])] "/>
    <xsl:template mode="iso19139" priority="20" match="
		gmd:topicCategory[normalize-space(gmd:MD_TopicCategoryCode)='imageryBaseMapsEarthCover' and 
		(preceding-sibling::gmd:topicCategory[starts-with(normalize-space(gmd:MD_TopicCategoryCode), 'imageryBaseMapsEarthCover')] or 
		following-sibling::gmd:topicCategory[starts-with(normalize-space(gmd:MD_TopicCategoryCode), 'imageryBaseMapsEarthCover')])] "/>
    <xsl:template mode="iso19139" priority="20" match="
		gmd:topicCategory[normalize-space(gmd:MD_TopicCategoryCode)='utilitiesCommunication' and 
		(preceding-sibling::gmd:topicCategory[starts-with(normalize-space(gmd:MD_TopicCategoryCode), 'utilitiesCommunication')] or 
		following-sibling::gmd:topicCategory[starts-with(normalize-space(gmd:MD_TopicCategoryCode), 'utilitiesCommunication')])] "/>


    <xsl:template mode="iso19139" match="gmd:MD_TopicCategoryCode" priority="10">
        <xsl:param name="schema"/>
        <xsl:param name="edit"/>

        <xsl:choose>
            <xsl:when test="$edit">
                <xsl:variable name="name"  select="name(.)"/>
                <xsl:variable name="value" select="string(.)"/>

                <xsl:variable name="list">
                    <items>
                        <xsl:for-each select="geonet:element/geonet:text">
                            <xsl:variable name="choiceValue" select="string(@value)"/>
                            <xsl:variable name="label" select="/root/gui/schemas/*[name(.)=$schema]/codelists/codelist[@name = $name]/entry[code = $choiceValue]/label"/>
                            <item>
                                <value>
                                    <xsl:if test="contains(@value,'_')">
                                        <xsl:attribute name="parent"><xsl:value-of select="substring-before(@value, '_')" /></xsl:attribute>
                                    </xsl:if>
                                    <xsl:value-of select="@value"/>
                                </value>
                                <label>
                                    <xsl:choose>
                                        <xsl:when test="$label"><xsl:value-of select="$label"/></xsl:when>
                                        <xsl:otherwise><xsl:value-of select="$choiceValue"/></xsl:otherwise>
                                    </xsl:choose>
                                </label>
                            </item>
                        </xsl:for-each>
                    </items>
                </xsl:variable>
                <xsl:variable name="selector">
                    <select id="topic{geonet:element/@ref}" class="md topicCategory" name="_{geonet:element/@ref}" size="1" onchange="validateTopicCategory('topic{geonet:element/@ref}')">
                        <option name=""/>

                        <xsl:for-each select="exslt:node-set($list)//item">
                            <xsl:sort select="label" />
                            <xsl:variable name="curValue" select="value"/>
                            <xsl:choose>
                                <xsl:when test="count(exslt:node-set($list)//item/value[@parent=$curValue]) > 0">
                                    <optgroup>
                                        <xsl:attribute name="label"><xsl:value-of select="label" /></xsl:attribute>
                                        <xsl:if test="value=$value">
                                            <option value="{value}" disabled="true">
                                                <xsl:if test="value=$value">
                                                    <xsl:attribute name="selected" />
                                                </xsl:if>
                                                <xsl:value-of select="label" />
                                            </option>
                                        </xsl:if>
                                        <xsl:for-each select="exslt:node-set($list)//item[value/@parent=$curValue]">
                                            <option value="{value}">
                                                <xsl:if test="value=$value">
                                                    <xsl:attribute name="selected" />
                                                </xsl:if>
                                                <xsl:value-of select="label" />
                                            </option>
                                        </xsl:for-each>
                                    </optgroup>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:if test="not(value/@parent)">
                                        <option value="{value}">
                                            <xsl:if test="value=$value">
                                                <xsl:attribute name="selected" />
                                            </xsl:if>
                                            <xsl:value-of select="label" />
                                        </option>
                                    </xsl:if>
                                </xsl:otherwise>
                            </xsl:choose>

                        </xsl:for-each>

                    </select>
                </xsl:variable>

                <xsl:apply-templates mode="simpleElement" select=".">
                    <xsl:with-param name="schema" select="$schema"/>
                    <xsl:with-param name="edit"   select="$edit"/>
                    <xsl:with-param name="text"   select="$selector"/>
                </xsl:apply-templates>
            </xsl:when>
            <xsl:otherwise>
                <xsl:apply-templates mode="simpleElement" select=".">
                    <xsl:with-param name="schema" select="$schema"/>
                    <xsl:with-param name="edit"   select="false()"/>
                </xsl:apply-templates>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <!--
        =============================================================================
    -->

    <xsl:template mode="iso19139GeoBox" match="*">
        <xsl:param name="schema" />
        <xsl:param name="edit" />

        <xsl:variable name="eltRef">
            <xsl:choose>
                <xsl:when test="$edit=true()">
                    <xsl:value-of select="geonet:element/@ref"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="generate-id(.)"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

        <xsl:variable name="wID">
            <xsl:choose>
                <xsl:when test=".//gmd:westBoundLongitude/gco:Decimal/geonet:element/@ref"><xsl:value-of select=".//gmd:westBoundLongitude/gco:Decimal/geonet:element/@ref"/></xsl:when>
                <xsl:otherwise>w<xsl:value-of select="$eltRef"/></xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

        <xsl:variable name="eID">
            <xsl:choose>
                <xsl:when test="./gmd:eastBoundLongitude/gco:Decimal/geonet:element/@ref"><xsl:value-of select="./gmd:eastBoundLongitude/gco:Decimal/geonet:element/@ref"/></xsl:when>
                <xsl:otherwise>e<xsl:value-of select="$eltRef"/></xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

        <xsl:variable name="nID">
            <xsl:choose>
                <xsl:when test="./gmd:northBoundLatitude/gco:Decimal/geonet:element/@ref"><xsl:value-of select="./gmd:northBoundLatitude/gco:Decimal/geonet:element/@ref"/></xsl:when>
                <xsl:otherwise>n<xsl:value-of select="$eltRef"/></xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

        <xsl:variable name="sID">
            <xsl:choose>
                <xsl:when test="./gmd:southBoundLatitude/gco:Decimal/geonet:element/@ref"><xsl:value-of select="./gmd:southBoundLatitude/gco:Decimal/geonet:element/@ref"/></xsl:when>
                <xsl:otherwise>s<xsl:value-of select="$eltRef"/></xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

        <xsl:if test="util:allowScripting() = 'true'">
            <input id="ch03_{$eltRef}" type="radio" name="proj_{$eltRef}" value="ch03" checked="checked" />
            <label for="ch03_{$eltRef}">CH03</label>
            <input id="wgs84_{$eltRef}" type="radio" name="proj_{$eltRef}" value="wgs84" />
            <label for="wgs84_{$eltRef}">WGS84</label>
        </xsl:if>
        <table style="width:100%">
            <tr>
                <td />
                <div id="native_{$eltRef}" style="display:none"><xsl:value-of select="comment()"/></div>
                <td class="padded" align="center">
                    <xsl:apply-templates mode="iso19139VertElement"
                                         select="gmd:northBoundLatitude/gco:Decimal">
                        <xsl:with-param name="schema" select="$schema" />
                        <xsl:with-param name="edit" select="$edit" />
                        <xsl:with-param name="name" select="'gmd:northBoundLatitude'" />
                        <xsl:with-param name="eltRef" select="$nID"/>
                    </xsl:apply-templates>
                </td>
                <td />
            </tr>
            <tr>
                <td class="padded" align="center">
                    <xsl:apply-templates mode="iso19139VertElement"
                                         select="gmd:westBoundLongitude/gco:Decimal">
                        <xsl:with-param name="schema" select="$schema" />
                        <xsl:with-param name="edit" select="$edit" />
                        <xsl:with-param name="name" select="'gmd:westBoundLongitude'" />
                        <xsl:with-param name="eltRef" select="$wID"/>
                    </xsl:apply-templates>
                </td>


                <td class="padded" style="width:100%">
                    <xsl:variable name="w" select="./gmd:westBoundLongitude/gco:Decimal"/>
                    <xsl:variable name="e" select="./gmd:eastBoundLongitude/gco:Decimal"/>
                    <xsl:variable name="n" select="./gmd:northBoundLatitude/gco:Decimal"/>
                    <xsl:variable name="s" select="./gmd:southBoundLatitude/gco:Decimal"/>

                    <xsl:variable name="geom" >
                        <xsl:value-of select="concat('Polygon((', $w, ' ', $s,',',$e,' ',$s,',',$e,' ',$n,',',$w,' ',$n,',',$w,' ',$s, '))')"/>
                    </xsl:variable>
                    <xsl:call-template name="showMap">
                        <xsl:with-param name="edit" select="$edit" />
                        <xsl:with-param name="coords" select="$geom"/>
                        <xsl:with-param name="watchedBbox" select="concat($wID, ',', $sID, ',', $eID, ',', $nID)"/>
                        <xsl:with-param name="eltRef" select="$eltRef"/>
                    </xsl:call-template>
                </td>

                <td class="padded" align="center">
                    <xsl:apply-templates mode="iso19139VertElement"
                                         select="gmd:eastBoundLongitude/gco:Decimal">
                        <xsl:with-param name="schema" select="$schema" />
                        <xsl:with-param name="edit" select="$edit" />
                        <xsl:with-param name="name" select="'gmd:eastBoundLongitude'" />
                        <xsl:with-param name="eltRef" select="$eID"/>
                    </xsl:apply-templates>
                </td>
            </tr>
            <tr>
                <td />
                <td class="padded" align="center">
                    <xsl:apply-templates mode="iso19139VertElement"
                                         select="gmd:southBoundLatitude/gco:Decimal">
                        <xsl:with-param name="schema" select="$schema" />
                        <xsl:with-param name="edit" select="$edit" />
                        <xsl:with-param name="name" select="'gmd:southBoundLatitude'" />
                        <xsl:with-param name="eltRef" select="$sID"/>
                    </xsl:apply-templates>
                </td>
                <td />
            </tr>
        </table>
    </xsl:template>


    <xsl:template mode="iso19139VertElement" match="*">
        <xsl:param name="schema" />
        <xsl:param name="edit" />
        <xsl:param name="name" />
        <xsl:param name="eltRef" />

        <xsl:variable name="title">
            <xsl:call-template name="getTitle">
                <xsl:with-param name="schema" select="$schema" />
                <xsl:with-param name="name" select="$name" />
            </xsl:call-template>
        </xsl:variable>
        <xsl:variable name="helpLink">
            <xsl:call-template name="getHelpLink">
                <xsl:with-param name="schema" select="$schema" />
                <xsl:with-param name="name" select="$name" />
            </xsl:call-template>
        </xsl:variable>
        <b>
            <xsl:choose>
                <xsl:when test="$helpLink!=''">
                    <span id="tip.{$helpLink}" style="cursor:help;">
                        <xsl:value-of select="$title" />
                        <xsl:call-template name="asterisk">
                            <xsl:with-param name="link" select="$helpLink" />
                            <xsl:with-param name="edit" select="$edit" />
                        </xsl:call-template>
                    </span>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="$title" />
                </xsl:otherwise>
            </xsl:choose>
        </b>
        <br/>
        <xsl:choose>
            <xsl:when test="$edit=true()">
                <xsl:call-template name="getElementText">
                    <xsl:with-param name="schema" select="$schema" />
                    <xsl:with-param name="edit" select="$edit" />
                    <xsl:with-param name="cols" select="10" />
                    <xsl:with-param name="visible" select="'true'" />
                    <xsl:with-param name="validator" select="'validateNumber(this, false)'" />
                    <xsl:with-param name="no_name" select="true()" />
                </xsl:call-template>
                <xsl:call-template name="getElementText">
                    <xsl:with-param name="schema" select="$schema" />
                    <xsl:with-param name="edit" select="true()" />
                    <xsl:with-param name="visible" select="'true'" />
                    <xsl:with-param name="cols" select="10" />
                    <xsl:with-param name="input_type" select="'hidden'" />
                </xsl:call-template>
            </xsl:when>
            <xsl:when test="util:allowScripting() = false()">
                <div class="md"><xsl:value-of select="substring-before(text(), '.')"/>.<xsl:value-of select="substring(substring-after(text(), '.'),0,4)"/></div>
            </xsl:when>
            <xsl:otherwise>
                <input class="md" type="text" id="{$eltRef}" value="{substring-before(text(), '.')}.{substring(substring-after(text(), '.'),0,4)}" readonly="readonly"/>
                <input class="md" type="hidden" id="_{$eltRef}" name="_{$eltRef}" value="{substring-before(text(), '.')}.{substring(substring-after(text(), '.'),0,4)}" readonly="readonly"/>
            </xsl:otherwise>
        </xsl:choose>

    </xsl:template>


</xsl:stylesheet>
