<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:geonet="http://www.fao.org/geonetwork"
                xmlns:dc="http://purl.org/dc/elements/1.1/"
                xmlns:dct="http://purl.org/dc/terms/"
                xmlns:saxon="http://saxon.sf.net/"
                extension-element-prefixes="saxon"
                exclude-result-prefixes="#all">


  <!-- Example of replacements parameter:

     <replacements>
      <caseInsensitive>i</caseInsensitive>
      <replacement>
        <field>id.contact.individualName</field>
        <searchValue>John Doe</searchValue>
        <replaceValue>Jennifer Smith</replaceValue>
      </replacement>
      <replacement>
        <field>id.contact.organisationName</field>
        <searchValue>Acme</searchValue>
        <replaceValue>New Acme</replaceValue>
      </replacement>
    </replacements>
  -->
  <xsl:param name="replacements"/>

  <!-- Flags http://www.w3.org/TR/xpath-functions/#flags -->
  <xsl:variable name="flags"
                select="$replacements/replacements/flags"/>


  <xsl:template match="@*|node()">
    <!--
    Element key is generic and based on ancestors local name.
    Class element are ignored - maybe this only works for ISO* standards.
    For example, gmd:abstract is identified by identification.abstract.
    -->
    <xsl:variable name="elementKey"
                  select="concat('dublin-core.',
                            string-join(
                              ./ancestor-or-self::*/local-name(), '.')
                            )"/>

    <!--
    A field match a replacer when its key match a field expression
    eg.
    * .*.individualName will replace all individualName
    *
    -->
    <xsl:variable name="hasReplacement"
                  select="count($replacements/replacements/
                            replacement[
                              matches($elementKey, field) and
                              string(searchValue) != '']) > 0"/>


    <!--<xsl:message>Key: <xsl:value-of select="$elementKey"/></xsl:message>-->
    <!--<xsl:message>hasReplacement: <xsl:copy-of select="$hasReplacement"/></xsl:message>-->

    <xsl:choose>
      <xsl:when test="$hasReplacement">
        <xsl:call-template name="replaceValueForField">
          <xsl:with-param name="fieldId" select="$elementKey" />
          <xsl:with-param name="value" select="." />
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:copy>
          <xsl:apply-templates select="@*|node()" mode="copy"/>
        </xsl:copy>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="geonet:*" priority="2"/>
  <xsl:template match="geonet:*" priority="2" mode="copy"/>
  <xsl:template match="@*|node()" mode="copy">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>


  <xsl:template name="replaceValueForField">
    <xsl:param name="fieldId" />
    <xsl:param name="value" />

    <xsl:variable name="replacementDetails"
                  select="$replacements/replacements/
                            replacement[matches($fieldId, field)]"/>

    <!-- Match all replacements define for this type of fields. -->
    <xsl:variable name="changes">
      <xsl:for-each select="$replacementDetails">

        <!-- If the value match the search value -->
        <xsl:if test="if ($flags)
                            then matches(
                              $value,
                              searchValue,
                              $flags)
                            else matches(
                              $value,
                              searchValue)">

          <!-- Replace content -->
          <xsl:variable name="newValue"
                        select="if ($flags)
                          then replace(
                            $value,
                            searchValue,
                            replaceValue,
                            $flags)
                          else replace(
                            $value,
                            searchValue,
                            replaceValue)"/>

          <change original="{$value}" new="{$newValue}"/>
        </xsl:if>
      </xsl:for-each>
    </xsl:variable>




    <!-- Report the changes to the record.
    If more than one match, only the first one is take into account. -->
    <xsl:choose>
      <xsl:when test="$changes/change[@original != @new]">
        <xsl:variable name="newValue" select="$changes/change[1]/@new/string()"/>
        <xsl:attribute name="geonet:change" select="$fieldId"/>
        <xsl:attribute name="geonet:original" select="$value"/>
        <xsl:attribute name="geonet:new" select="$newValue"/>
        <xsl:value-of select="$newValue"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$value"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>
