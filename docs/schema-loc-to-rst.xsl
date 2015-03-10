<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  exclude-result-prefixes="xs"
  version="2.0">
  
  <xsl:output method="text"/>
  
  <xsl:param name="lang" select="'fre'"/>
  <xsl:param name="schema" select="'iso19139'"/>
  
  <xsl:variable name="i18n">
    <title>
      <eng>List of element for schema:</eng>
      <fre>Liste des éléments du standard :</fre>
    </title>
    <code>
      <eng>Tag name</eng>
      <fre>Nom de la balise</fre>
    </code>
    <desc>
      <eng>Description</eng>
      <fre>Description</fre>
    </desc>
    <moreInfo>
      <eng>More details</eng>
      <fre>Informations complémentaires</fre>
    </moreInfo>
    <helper>
      <eng>Recommended values</eng>
      <fre>Valeurs recommandées</fre>
    </helper>
  </xsl:variable>
  
  <xsl:variable name="labels" select="document(concat('../web/src/main/webapp/WEB-INF/data/config/schema_plugins/', $schema, '/loc/eng/labels.xml'))"/>
  
  <xsl:template match="/">

# <xsl:value-of select="concat($i18n/title/*[name() = $lang], ' ', $schema)"/>

<xsl:text>


</xsl:text>

    <xsl:for-each select="$labels//element">
      <xsl:sort select="name"/>
      
<xsl:variable name="rstRef" 
  select="concat('.. _', $schema, '-', 
  replace(@name, ':', '-'), ':')"/>

<xsl:variable name="rstContext" select="if (@context) 
  then concat(' (cf. `', 
    @context, ' &lt;#', 
    $schema, '-', replace(@context, ':', '-'), '&gt;', 
    @context, '`_)') 
  else ''"/>


<xsl:value-of select="$rstRef"/>

<xsl:text>

</xsl:text>

      <xsl:value-of select="concat(label, $rstContext)"/>



- *<xsl:value-of select="$i18n/desc/*[name() = $lang]"/> :* <xsl:value-of select="label"/>
      
      
- *<xsl:value-of select="$i18n/code/*[name() = $lang]"/> :* <xsl:value-of select="@name"/>

- *<xsl:value-of select="$i18n/moreInfo/*[name() = $lang]"/> :* 

<xsl:for-each select="help">
  <xsl:value-of select="."/>
</xsl:for-each>


<xsl:if test="helper">
<xsl:value-of select="$i18n/helper/*[name() = $lang]"/>
<xsl:for-each select="helper/option">
  - <xsl:value-of select="text()"/> (<xsl:value-of select="@value"/>)
</xsl:for-each>
</xsl:if>
      
      
      
 <xsl:text>


</xsl:text>
    </xsl:for-each>
  </xsl:template>
</xsl:stylesheet>