<xsl:stylesheet version="2.0" xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                xmlns:skos="http://www.w3.org/2004/02/skos/core#" xmlns:dc="http://purl.org/dc/terms/"
                xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
                xmlns:grg="http://www.isotc211.org/schemas/grg/" xmlns:owl="http://www.w3.org/2002/07/owl#"
                xmlns:void="http://rdfs.org/ns/void#" xmlns:gml="http://www.opengis.net/gml#"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" exclude-result-prefixes="#all">

  <xsl:output encoding="UTF-8" indent="yes" method="xml"/>

  <xsl:template match="/rdf:RDF">
    <xsl:copy>
      <xsl:copy-of select="rdf:Description[@rdf:about = 'http://www.ifremer.fr/thesaurus/sextant/theme']"/>

      <xsl:variable name="root"
                    select="."/>

      <xsl:variable name="themes">
        <xsl:for-each select=".//rdf:Description[@rdf:about != 'http://www.ifremer.fr/thesaurus/sextant/theme' and skos:prefLabel != '']">
          <xsl:variable name="about"
                        select="@rdf:about"/>
          <xsl:variable name="frePath"
                        select="tokenize($root/rdf:Description[@rdf:about = $about]/skos:prefLabel[@xml:lang = 'fr'][1], '/')"/>
          <xsl:variable name="engPath"
                        select="tokenize($root/rdf:Description[@rdf:about = $about]/skos:prefLabel[@xml:lang = 'en'][1], '/')"/>
          <xsl:for-each select="$frePath">
            <xsl:variable name="position"
                          select="position()"/>
            <xsl:variable name="enLabel"
                          select="normalize-space(string-join($engPath[position() &lt;= $position], '/'))"/>
            <xsl:variable name="frLabel"
                          select="normalize-space(string-join($frePath[position() &lt;= $position], '/'))"/>

            <keyword>
              <fre><xsl:value-of select="$frLabel"/></fre>
              <eng><xsl:value-of select="$enLabel"/></eng>
              <id><xsl:value-of select="concat(
                  'http://www.ifremer.fr/thesaurus/sextant/theme#',
                  replace($enLabel, '[^a-zA-Z0-9-_]', ''))"/></id>
            </keyword>
          </xsl:for-each>
        </xsl:for-each>
      </xsl:variable>


      <xsl:for-each select="distinct-values($themes/keyword/fre[. != ''])">
        <xsl:sort select="." order="ascending"/>

        <xsl:variable name="k"
                      select="($themes/keyword[fre = current()])[1]"/>
        <rdf:Description rdf:about="{$k/id}">
          <rdf:type rdf:resource="http://www.w3.org/2004/02/skos/core#Concept"/>
          <skos:prefLabel xml:lang="en"><xsl:value-of select="$k/eng"/></skos:prefLabel>
          <skos:definition xml:lang="en"></skos:definition>
          <skos:prefLabel xml:lang="fr"><xsl:value-of select="$k/fre"/></skos:prefLabel>
          <skos:definition xml:lang="fr"></skos:definition>

          <xsl:variable name="broaderPath"
                        select="tokenize($k/eng, '/')"/>
          <xsl:for-each select="$broaderPath">
            <xsl:variable name="position"
                          select="position()"/>
            <xsl:variable name="enLabel"
                          select="normalize-space(string-join($broaderPath[position() &lt;= $position], '/'))"/>

            <xsl:variable name="broaderId"
                          select="concat(
                  'http://www.ifremer.fr/thesaurus/sextant/theme#',
                  replace($enLabel, '[^a-zA-Z0-9-_]', ''))"/>
            <xsl:if test="$broaderId != $k/id and $broaderId != 'http://www.ifremer.fr/thesaurus/sextant/theme#'">
              <skos:broader rdf:resource="{$broaderId}"/>
            </xsl:if>
          </xsl:for-each>


          <xsl:variable name="narrower"
                        select="$themes/keyword[starts-with(fre, concat($k/fre, '/'))]"/>
          <xsl:for-each-group select="$narrower" group-by="id">
            <skos:narrower rdf:resource="{id}"/>
          </xsl:for-each-group>
        </rdf:Description>
      </xsl:for-each>

      <xsl:message> <xsl:copy-of select="count(distinct-values($themes/keyword/fre))"/></xsl:message>
    </xsl:copy>
  </xsl:template>
</xsl:stylesheet>
