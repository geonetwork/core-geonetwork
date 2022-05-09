
XPATH_SCHEME=".//*[local-name() = 'Description'][*[local-name() = 'type']/@*[local-name() = 'resource'] = 'http://www.w3.org/2004/02/skos/core#ConceptScheme']"
XPATH_CONCEPT=".//*[local-name() = 'Description'][*[local-name() = 'type']/@*[local-name() = 'resource'] != 'http://www.w3.org/2004/02/skos/core#ConceptScheme' and *[local-name() = 'prefLabel'] != '']"
XPATH_BOUNDS=".//*[local-name() = 'BoundedBy']"
for f in ../../web/src/main/webapp/WEB-INF/data/config/codelist/local/thesauri/*/*.rdf
do
  echo "_______________________"
  echo "$f"
  xmllint --xpath "string($XPATH_SCHEME/@*[local-name() = 'about'])" $f
  xmllint --xpath "$XPATH_SCHEME/*[local-name() = 'title']/text()" $f
  echo "Concepts: `xmllint --xpath "count($XPATH_CONCEPT)" $f`"
  echo "Concepts (label fr): `xmllint --xpath "count($XPATH_CONCEPT/*[local-name() = 'prefLabel' != '' and @*[local-name() = 'lang'] = 'fr'])" $f`"
  echo "Concepts (label en): `xmllint --xpath "count($XPATH_CONCEPT/*[local-name() = 'prefLabel' != '' and @*[local-name() = 'lang'] = 'en'])" $f`"
  echo "Bounds: `xmllint --xpath "count(XPATH_BOUNDS)" $f`"
done
