#!/usr/bin/env bash

# Script to download BODC vocabularies.

cd codelist/external/thesauri/feature-type
wget --output-document=NVS.L02.rdf http://vocab.nerc.ac.uk/collection/L02/current/
cd ../../../..



cd codelist/external/thesauri/parameter
wget --output-document=NVS.P01.rdf http://vocab.nerc.ac.uk/collection/P01/current/
sed -i 's/BODC Parameter Usage Vocabulary/Parameter Usage Vocabulary \(P01\)/g' NVS.P01.rdf

wget --output-document=NVS.P02.rdf http://vocab.nerc.ac.uk/collection/P02/current/
sed -i 's/SeaDataNet Parameter Discovery Vocabulary/Parameter Discovery Vocabulary \(P02\)/g' NVS.P02.rdf

wget --output-document=NVS.P03.rdf http://vocab.nerc.ac.uk/collection/P03/current/
sed -i 's/SeaDataNet Agreed Parameter Groups/Agreed Parameter Groups \(P03\)/g' NVS.P03.rdf

wget --output-document=NVS.P35.rdf http://vocab.nerc.ac.uk/collection/P35/current/
wget --output-document=NVS.A05.rdf http://vocab.nerc.ac.uk/collection/A05/current/
cd ../../../..



cd codelist/external/thesauri/reference-geographical-area
wget --output-document=NVS.C19.rdf http://vocab.nerc.ac.uk/collection/C19/current/
cd ../../../..



cd codelist/external/thesauri/use-limitation
wget --output-document=NVS.L08.rdf http://vocab.nerc.ac.uk/collection/L08/current/
cd ../../../..



cd codelist/external/thesauri/theme
wget --output-document=NVS.L11.rdf http://vocab.nerc.ac.uk/collection/L11/current/
wget --output-document=NVS.L23.rdf http://vocab.nerc.ac.uk/collection/L23/current/
wget --output-document=NVS.L05.rdf http://vocab.nerc.ac.uk/collection/L05/current/
# TODO: LO5POS is derived from L05
cd ../../../..


# TODO: Replace XSL header
# <?xml-stylesheet href="/VocabV2/skosrdf2html.xsl" type="text/xsl" media="screen"?>
# not supported by Sesame library
