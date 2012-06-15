#!/bin/sh
export GEONETWORK_HOME=../geocat2/web/target/geonetwork/
export CLASSPATH=.:$GEONETWORK_HOME/WEB-INF/lib/xml-apis-1.3.04.jar:$GEONETWORK_HOME/WEB-INF/lib/xercesImpl-2.7.1.jar:$GEONETWORK_HOME/WEB-INF/lib/xalan-2.7.1.jar:$GEONETWORK_HOME/WEB-INF/lib/serializer-2.7.1.jar

if [ $1 ]
then
    echo "Downloading GEMET theme thesaurus:"
    echo "  * langague files:"
    for locale in $*; do
        echo "    loading: $locale ..."
        wget --output-document=inspire-theme-$locale.rdf http://www.eionet.europa.eu/gemet/gemet-groups.rdf?langcode=$locale
    done

    # Creating list of locales for XSL processing
    export LOCALES="<locales>"
    for locale in $*; do
        export LOCALES=$LOCALES"<locale>"$locale"</locale>"
    done
    export LOCALES=$LOCALES"</locales>"
    echo $LOCALES > locales.xml

    echo "Creating thesaurus ..."
    java org.apache.xalan.xslt.Process -IN locales.xml -XSL gemet-theme.xsl -OUT gemet-theme.rdf

    echo "Deploying to thesauri directory:"
#    mv gemet-theme.rdf thesauri/.
#    rm locales.xml
#    rm *.rdf
    echo "Done."
else
    echo "Usage: ./gemet-theme.sh en fr de";
    echo "to create a GEMET theme thesaurus with english, french and deutsch languages."
fi
