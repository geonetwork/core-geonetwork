#xsltproc -o test.rdf sextant-theme-broader-builder.xsl /data/dev/gn/sextant/web/src/main/webapp/WEB-INF/data/config/codelist/local/thesauri/theme/sextant-theme.rdf

#for thesaurus in place/dcsmm.area.rdf
#for thesaurus in place/dcsmm.area.rdf theme/simm.thematiques.rdf
for thesaurus in theme/sextant-theme.rdf theme/mission-atlantic-bodc-parameters.rdf theme/mission-atlantic-odemm.rdf theme/odatis_variables.rdf theme/type_jeux_donnee.rdf thesaurus in place/dcsmm.area.rdf theme/simm.thematiques.rdf
do
  THESAURUSFILE=`cut -d "/" -f2 <<< "$thesaurus"`
  #cp ../../../web/src/main/webapp/WEB-INF/data/config/codelist/local/thesauri/$thesaurus $THESAURUSFILE
  java -jar ~/.m2/repository/net/sf/saxon/saxon/9.1.0.8b-patch/saxon-9.1.0.8b-patch.jar -s:$THESAURUSFILE -xsl:thesaurus-build-hierarchy-from-slash-label.xsl -o:../../../web/src/main/webapp/WEB-INF/data/config/codelist/local/thesauri/$thesaurus
done
