#xsltproc -o test.rdf sextant-theme-broader-builder.xsl /data/dev/gn/sextant/web/src/main/webapp/WEB-INF/data/config/codelist/local/thesauri/thesauri/theme/sextant-theme.rdf

#for thesaurus in local/thesauri/place/dcsmm.area.rdf
#for thesaurus in local/thesauri/place/dcsmm.area.rdf local/thesauri/theme/simm.thematiques.rdf
#for thesaurus in local/thesauri/theme/sextant-theme.rdf local/thesauri/theme/mission-atlantic-bodc-parameters.rdf local/thesauri/theme/mission-atlantic-odemm.rdf local/thesauri/theme/odatis_variables.rdf local/thesauri/theme/type_jeux_donnee.rdf place/dcsmm.area.rdf local/thesauri/theme/simm.thematiques.rdf
for thesaurus in external/thesauri/theme/GCMDparameter.rdf
do
  THESAURUSFILE=`cut -d "/" -f4 <<< "$thesaurus"`
  echo $THESAURUSFILE
  #cp ../../../web/src/main/webapp/WEB-INF/data/config/codelist/local/thesauri/thesauri/$thesaurus $THESAURUSFILE
  java -jar ~/.m2/repository/net/sf/saxon/saxon/9.1.0.8b-patch/saxon-9.1.0.8b-patch.jar -s:$THESAURUSFILE -xsl:../../../web/src/main/webapp/xslt/services/thesaurus/sextant-formatter.xsl -o:../../../web/src/main/webapp/WEB-INF/data/config/codelist/$thesaurus
done
