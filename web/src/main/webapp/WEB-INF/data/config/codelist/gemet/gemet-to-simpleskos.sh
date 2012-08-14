#!/bin/sh

if [ $1 ]
then
    echo "Downloading GEMET thesaurus:"
    echo "  * backbone ..."
    wget http://www.eionet.europa.eu/gemet/gemet-backbone.rdf
    echo "  * skoscore ..."
    wget http://www.eionet.europa.eu/gemet/gemet-skoscore.rdf
    echo "  * langague files:"
    for locale in $*; do
        echo "    loading: $locale ..."
        wget --output-document=gemet-definitions-$locale.rdf  http://www.eionet.europa.eu/gemet/gemet-definitions.rdf?langcode=$locale
    done

    # Creating list of locales for XSL processing
    export LOCALES="<locales>"
    for locale in $*; do
        export LOCALES=$LOCALES"<locale>"$locale"</locale>"
    done
    export LOCALES=$LOCALES"</locales>"
    echo $LOCALES > locales.xml

    echo "Creating thesaurus ..."
    export GEONETWORK_HOME=../../../..
    export CLASSPATH=.:$GEONETWORK_HOME/WEB-INF/lib/xml-apis-1.3.04.jar:$GEONETWORK_HOME/WEB-INF/lib/xercesImpl-2.7.1.jar:$GEONETWORK_HOME/WEB-INF/lib/xalan-2.7.1.jar:$GEONETWORK_HOME/WEB-INF/lib/serializer-2.7.1.jar
    java org.apache.xalan.xslt.Process -IN gemet-backbone.rdf -XSL gemet-to-simpleskos.xsl -OUT gemet.rdf

    echo "Deploying to catalogue codelist directory:"
#    mv gemet.rdf ../external/thesauri/theme/.
#    rm locales.xml
#    rm *.rdf
    echo "Done."
else
    echo "Usage: ./gemet-to-simpleskos.sh en fr de";
    echo "to create a GEMET thesaurus with english, french and deutsch languages."
fi
