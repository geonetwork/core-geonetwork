#xsltproc -o test.rdf sextant-theme-broader-builder.xsl /data/dev/gn/sextant/web/src/main/webapp/WEB-INF/data/config/codelist/local/thesauri/theme/sextant-theme.rdf

for thesaurus in sextant-theme.rdf mission-atlantic-bodc-parameters.rdf mission-atlantic-odemm.rdf odatis_variables.rdf type_jeux_donnee.rdf
do
  cp ../../../web/src/main/webapp/WEB-INF/data/config/codelist/local/thesauri/theme/$thesaurus $thesaurus
  java -jar ~/.m2/repository/net/sf/saxon/saxon/9.1.0.8b-patch/saxon-9.1.0.8b-patch.jar -s:$thesaurus -xsl:sextant-theme-broader-builder.xsl -o:../../../web/src/main/webapp/WEB-INF/data/config/codelist/local/thesauri/theme/$thesaurus
done
