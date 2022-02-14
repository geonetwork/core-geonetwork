<xsl:stylesheet version="2.0" xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                xmlns:skos="http://www.w3.org/2004/02/skos/core#"
                xmlns:dc="http://purl.org/dc/terms/"
                xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
                xmlns:grg="http://www.isotc211.org/schemas/grg/"
                xmlns:owl="http://www.w3.org/2002/07/owl#"
                xmlns:void="http://rdfs.org/ns/void#"
                xmlns:util="java:java.util.UUID"
                xmlns:gml="http://www.opengis.net/gml#"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" exclude-result-prefixes="#all">

  <xsl:output encoding="UTF-8" indent="yes" method="xml"/>

  <xsl:param name="createNewKey"
             select="true()"/>

  <xsl:variable name="separator"
                select="'/'"/>

  <xsl:template match="/rdf:RDF">
    <xsl:copy>
      <!--
      <rdf:Description rdf:about="http://geonetwork-opensource.org/Variables ODATIS">
        <rdf:type rdf:resource="http://www.w3.org/2004/02/skos/core#ConceptScheme"/>
      -->
      <xsl:variable name="conceptScheme"
                    select=".//rdf:Description[rdf:type/@rdf:resource = 'http://www.w3.org/2004/02/skos/core#ConceptScheme']"/>
      <!-- https://sextant.ifremer.fr/-->
      <xsl:variable name="baseUri"
                    select="if ($conceptScheme/@rdf:about != '')
                            then replace(replace(replace(
                              $conceptScheme/@rdf:about,
                              'http://geonetwork-opensource.org/',
                              'https://registry.sextant.ifremer.fr/'),
                              'http://www.ifremer.fr/thesaurus/sextant/',
                              'https://registry.sextant.ifremer.fr/'),
                            ' ', '')
                            else 'https://registry.sextant.ifremer.fr/'"/>
      <xsl:for-each select="$conceptScheme">
        <xsl:copy>
          <xsl:attribute name="rdf:about" select="$baseUri"/>
          <xsl:copy-of select="*"/>
        </xsl:copy>
      </xsl:for-each>

      <xsl:variable name="root"
                    select="."/>

      <xsl:variable name="existingConcepts"
                    select=".//rdf:Description[(
                  not(rdf:type/@rdf:resource)
                  or rdf:type/@rdf:resource != 'http://www.w3.org/2004/02/skos/core#ConceptScheme')
                 and skos:prefLabel != '']"/>
      <xsl:message><xsl:copy-of select=".//rdf:Description[(
                  not(rdf:type/@rdf:resource)
                  or rdf:type/@rdf:resource != 'http://www.w3.org/2004/02/skos/core#ConceptScheme')
                 and skos:prefLabel != '']"/></xsl:message>
      <!-- Create one concept per level. -->
      <xsl:variable name="concepts">
        <xsl:for-each-group select="$existingConcepts"
                            group-by="@rdf:about">
          <xsl:variable name="about"
                        select="@rdf:about"/>
          <xsl:variable name="id"
                        select="count(preceding-sibling::*)"/>
          <xsl:variable name="freLabel"
                        select="$root/rdf:Description[@rdf:about = $about]/skos:prefLabel[@xml:lang = 'fr' and . != ''][1]"/>
          <xsl:variable name="engLabel"
                        select="$root/rdf:Description[@rdf:about = $about]/skos:prefLabel[@xml:lang = 'en' and . != ''][1]"/>
          <xsl:variable name="frePath"
                        select="tokenize($freLabel, $separator)"/>
          <xsl:variable name="engPath"
                        select="tokenize($engLabel, $separator)"/>

          <xsl:variable name="pathToUse"
                        select="if (count($frePath) >= count($engPath))
                                then $frePath else $engPath"/>

          <xsl:if test="count($frePath) != count($engPath)">
            <xsl:message>WARNING: Number of <xsl:value-of select="$separator"/> in french and english label does not match. Check labels for '<xsl:value-of select="$freLabel"/>' vs '<xsl:value-of select="$engLabel"/>'.</xsl:message>
          </xsl:if>

          <xsl:for-each select="$pathToUse">
            <xsl:variable name="position"
                          select="position()"/>
            <xsl:variable name="enLabel"
                          select="normalize-space(string-join($engPath[position() &lt;= $position], $separator))"/>
            <xsl:variable name="frLabel"
                          select="normalize-space(string-join($frePath[position() &lt;= $position], $separator))"/>

            <keyword>
              <key><xsl:value-of select="if ($frLabel != '') then $frLabel else $enLabel"/></key>
              <fre><xsl:value-of select="$frLabel"/></fre>
              <eng><xsl:value-of select="$enLabel"/></eng>
            </keyword>
          </xsl:for-each>
        </xsl:for-each-group>
      </xsl:variable>

      <xsl:variable name="conceptsWithIds">
        <xsl:for-each select="distinct-values($concepts/keyword/key[. != ''])">
          <xsl:sort select="." order="ascending"/>

          <xsl:variable name="concept"
                        select="$concepts/keyword[key = current()][1]/*"/>

          <keyword>
            <!-- Preserve ids. -->
            <xsl:variable name="existingConceptId"
                          select="$existingConcepts[skos:prefLabel = current()]/@rdf:about"/>
            <id><xsl:value-of select="if ($existingConceptId != '')
                                      then $existingConceptId
                                      else concat($baseUri, '#', util:toString(util:randomUUID()))"/></id>
            <xsl:copy-of select="$concepts/keyword[key = current()][1]/*"/>
          </keyword>
        </xsl:for-each>
      </xsl:variable>
      <xsl:message><xsl:copy-of select="$conceptsWithIds"/></xsl:message>

      <xsl:for-each select="distinct-values($conceptsWithIds/keyword/key[. != ''])">
        <xsl:sort select="." order="ascending"/>

        <xsl:variable name="k"
                      select="($conceptsWithIds/keyword[key = current()])[1]"/>
        <rdf:Description rdf:about="{$k/id}">
          <rdf:type rdf:resource="http://www.w3.org/2004/02/skos/core#Concept"/>
          <skos:prefLabel xml:lang="en"><xsl:value-of select="$k/eng"/></skos:prefLabel>
          <skos:definition xml:lang="en"></skos:definition>
          <skos:prefLabel xml:lang="fr"><xsl:value-of select="$k/fre"/></skos:prefLabel>
          <skos:definition xml:lang="fr"></skos:definition>

          <xsl:variable name="broaderPath"
                        select="tokenize($k/key, '/')"/>

          <xsl:variable name="levelOfCurrent"
                        select="count($broaderPath)"/>
          <xsl:for-each select="$broaderPath">
            <xsl:variable name="position"
                          select="position()"/>
            <xsl:variable name="keyValue"
                          select="normalize-space(string-join($broaderPath[position() &lt;= $position], '/'))"/>

            <xsl:variable name="broaderConcept"
                          select="($conceptsWithIds/keyword[key = $keyValue])[1]"/>
            <xsl:variable name="broaderId"
                          select="$broaderConcept/id"/>

            <xsl:variable name="levelOfBroader"
                          select="position()"/>

            <xsl:if test="$broaderId != $k/id
                           and $broaderId != concat($baseUri, '#')
                           and $levelOfBroader = ($levelOfCurrent - 1)">
              <skos:broader rdf:resource="{$broaderId}"/>
            </xsl:if>
          </xsl:for-each>


          <xsl:variable name="narrower"
                        select="$conceptsWithIds/keyword[starts-with(key, concat($k/key, '/'))]"/>
          <xsl:for-each-group select="$narrower" group-by="id">

            <xsl:variable name="levelOfNarrower"
                          select="count(tokenize(./key, '/'))"/>
            <xsl:if test="($levelOfCurrent + 1) = $levelOfNarrower">
              <skos:narrower rdf:resource="{id}"/>
            </xsl:if>
          </xsl:for-each-group>
        </rdf:Description>
      </xsl:for-each>

    </xsl:copy>
  </xsl:template>
</xsl:stylesheet>
