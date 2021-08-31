#xsltproc -o test.rdf sextant-theme-broader-builder.xsl /data/dev/gn/sextant/web/src/main/webapp/WEB-INF/data/config/codelist/local/thesauri/theme/sextant-theme.rdf

java -jar /home/francois/.m2/repository/net/sf/saxon/saxon/9.1.0.8b-patch/saxon-9.1.0.8b-patch.jar -s:/data/dev/gn/sextant/web/src/main/webapp/WEB-INF/data/config/codelist/local/thesauri/theme/sextant-theme.rdf -xsl:sextant-theme-broader-builder.xsl -o:test.rdf
