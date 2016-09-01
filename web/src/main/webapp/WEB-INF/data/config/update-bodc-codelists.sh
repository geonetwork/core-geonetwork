#!/usr/bin/env bash

# Script to download BODC vocabularies.

# After download, thesaurus titles need to be manually updated for P01, P02, P03.
# eg.
#-<skos:prefLabel>Agreed Parameter Groups (P03)</skos:prefLabel>
#-<dc:title>Agreed Parameter Groups (P03)</dc:title>
#+<skos:prefLabel>SeaDataNet Agreed Parameter Groups</skos:prefLabel>
#+<dc:title>SeaDataNet Agreed Parameter Groups</dc:title>


cd codelist/external/thesauri/feature-type
wget --output-document=NVS.L02.rdf http://vocab.nerc.ac.uk/collection/L02/current/
cd ../../../..

cd codelist/external/thesauri/parameter
wget --output-document=NVS.P01.rdf http://vocab.nerc.ac.uk/collection/P01/current/
wget --output-document=NVS.P02.rdf http://vocab.nerc.ac.uk/collection/P02/current/
wget --output-document=NVS.P03.rdf http://vocab.nerc.ac.uk/collection/P03/current/
wget --output-document=NVS.P35.rdf http://vocab.nerc.ac.uk/collection/P35/current/
cd ../../../..

cd codelist/external/thesauri/reference-geographical-area
wget --output-document=NVS.C19.rdf http://vocab.nerc.ac.uk/collection/C19/current/
cd ../../../..

cd codelist/external/thesauri/use-limitation
wget --output-document=NVS.L08.rdf http://vocab.nerc.ac.uk/collection/L08/current/
cd ../../../..
