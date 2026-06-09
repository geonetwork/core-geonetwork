<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                exclude-result-prefixes="#all">

  <xsl:variable name="odsThemeToIsoTopic" as="node()*">
    <health>
      <theme>Santé</theme>
      <theme>Health</theme>
      <theme>Qualité de Vie</theme>
    </health>
    <environment>
      <theme>Environnement</theme>
      <theme>Environment</theme>
    </environment>
    <transportation>
      <theme>Transports, Déplacements</theme>
      <theme>Transport, Movements</theme>
    </transportation>
    <structure>
      <theme>Aménagement du territoire, Urbanisme, Bâtiments, Equipements, Habitat</theme>
      <theme>Spatial planning, Town planning, Buildings, Equipment, Housing</theme>
    </structure>
    <economy>
      <theme>Economie, Entreprise, PME, Développement économique, Emploi</theme>
      <theme>Economy, Business, SME, Economic development, Employment</theme>
    </economy>
    <society>
      <theme>Patrimoine culturel</theme>
      <theme>Culture, Heritage</theme>
      <theme>Education, Formation, Recherche, Enseignement</theme>
      <theme>Education, Training, Research, Teaching</theme>
      <theme>Administration, Gouvernement, Finances publiques, Citoyenneté</theme>
      <theme>Administration, Government, Public finances, Citizenship</theme>
      <theme>Justice, Sécurité, Police, Criminalité</theme>
      <theme>Justice, Safety, Police, Crime</theme>
      <theme>Sports, Loisirs</theme>
      <theme>Sports, Leisure</theme>
      <theme>Hébergement, industrie hôtelière</theme>
      <theme>Accommodation, Hospitality Industry</theme>
      <theme>Services sociaux</theme>
      <theme>Services, Social</theme>
    </society>
  </xsl:variable>
</xsl:stylesheet>
