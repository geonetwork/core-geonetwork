#!/usr/bin/env bash

# Script to download BODC vocabularies.
UTILITY_PATH=$(pwd)
cd ../../../web/src/main/webapp/WEB-INF/data/config


cd codelist/external/thesauri/feature-type
wget --output-document=NVS.L02.rdf http://vocab.nerc.ac.uk/collection/L02/current/
sed -i 's/<?xml version="1.0" encoding="UTF-8"?><?xml-stylesheet href="\/VocabV2\/skosrdf2html\.xsl" type="text\/xsl" media="screen"?>//g' NVS.L02.rdf
cd ../../../..


cd codelist/external/thesauri/parameter
wget --output-document=NVS.P01.rdf http://vocab.nerc.ac.uk/collection/P01/current/
sed -i 's/BODC Parameter Usage Vocabulary/Parameter Usage Vocabulary \(P01\)/g' NVS.P01.rdf
sed -i 's/<?xml version="1.0" encoding="UTF-8"?><?xml-stylesheet href="\/VocabV2\/skosrdf2html\.xsl" type="text\/xsl" media="screen"?>//g' NVS.P01.rdf

wget --output-document=NVS.P02.rdf http://vocab.nerc.ac.uk/collection/P02/current/
sed -i 's/SeaDataNet Parameter Discovery Vocabulary/Parameter Discovery Vocabulary \(P02\)/g' NVS.P02.rdf
sed -i 's/<?xml version="1.0" encoding="UTF-8"?><?xml-stylesheet href="\/VocabV2\/skosrdf2html\.xsl" type="text\/xsl" media="screen"?>//g' NVS.P02.rdf

wget --output-document=NVS.P03.rdf http://vocab.nerc.ac.uk/collection/P03/current/
sed -i 's/SeaDataNet Agreed Parameter Groups/Agreed Parameter Groups \(P03\)/g' NVS.P03.rdf
sed -i 's/<?xml version="1.0" encoding="UTF-8"?><?xml-stylesheet href="\/VocabV2\/skosrdf2html\.xsl" type="text\/xsl" media="screen"?>//g' NVS.P03.rdf

wget --output-document=NVS.P35.rdf http://vocab.nerc.ac.uk/collection/P35/current/
sed -i 's/<?xml version="1.0" encoding="UTF-8"?><?xml-stylesheet href="\/VocabV2\/skosrdf2html\.xsl" type="text\/xsl" media="screen"?>//g' NVS.P35.rdf

wget --output-document=NVS.A05.rdf http://vocab.nerc.ac.uk/collection/A05/current/
sed -i 's/<?xml version="1.0" encoding="UTF-8"?><?xml-stylesheet href="\/VocabV2\/skosrdf2html\.xsl" type="text\/xsl" media="screen"?>//g' NVS.A05.rdf

wget --output-document=NVS.L04.rdf http://vocab.nerc.ac.uk/collection/L04/current/
sed -i 's/<?xml version="1.0" encoding="UTF-8"?><?xml-stylesheet href="\/VocabV2\/skosrdf2html\.xsl" type="text\/xsl" media="screen"?>//g' NVS.L04.rdf

wget --output-document=NVS.P36.rdf http://vocab.nerc.ac.uk/collection/P36/current/
sed -i 's/<?xml version="1.0" encoding="UTF-8"?><?xml-stylesheet href="\/VocabV2\/skosrdf2html\.xsl" type="text\/xsl" media="screen"?>//g' NVS.P36.rdf

wget --output-document=NVS.P08.rdf "http://vocab.nerc.ac.uk/collection/P08/current/?_profile=nvs&_mediatype=application/rdf+xml"
cd ../../../..



cd codelist/external/thesauri/reference-geographical-area
wget --output-document=NVS.C19.rdf http://vocab.nerc.ac.uk/collection/C19/current/
sed -i 's/<?xml version="1.0" encoding="UTF-8"?><?xml-stylesheet href="\/VocabV2\/skosrdf2html\.xsl" type="text\/xsl" media="screen"?>//g' NVS.C19.rdf
cd ../../../..



cd codelist/external/thesauri/use-limitation
wget --output-document=NVS.L08.rdf http://vocab.nerc.ac.uk/collection/L08/current/
sed -i 's/<?xml version="1.0" encoding="UTF-8"?><?xml-stylesheet href="\/VocabV2\/skosrdf2html\.xsl" type="text\/xsl" media="screen"?>//g' NVS.L08.rdf
cd ../../../..



cd codelist/external/thesauri/theme
wget --output-document=NVS.L11.rdf http://vocab.nerc.ac.uk/collection/L11/current/
sed -i 's/<?xml version="1.0" encoding="UTF-8"?><?xml-stylesheet href="\/VocabV2\/skosrdf2html\.xsl" type="text\/xsl" media="screen"?>//g' NVS.L11.rdf
wget --output-document=NVS.L23.rdf http://vocab.nerc.ac.uk/collection/L23/current/
sed -i 's/<?xml version="1.0" encoding="UTF-8"?><?xml-stylesheet href="\/VocabV2\/skosrdf2html\.xsl" type="text\/xsl" media="screen"?>//g' NVS.L23.rdf
wget --output-document=NVS.L05.rdf http://vocab.nerc.ac.uk/collection/L05/current/
sed -i 's/<?xml version="1.0" encoding="UTF-8"?><?xml-stylesheet href="\/VocabV2\/skosrdf2html\.xsl" type="text\/xsl" media="screen"?>//g' NVS.L05.rdf

# LO5POS is derived from L05
xsltproc -o NVS.L05POS.rdf $UTILITY_PATH/L05POSbuilder.xsl NVS.L05.rdf

cd $UTILITY_PATH
