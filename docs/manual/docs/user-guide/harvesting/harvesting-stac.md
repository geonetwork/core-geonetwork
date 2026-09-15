# Simple URL harvesting (STAC Collection)

The STAC collection harvester, via a simple URL, allows for reading collections from a STAC catalog and generating metadata records with a "series" hierarchy level for each collection.

The elements being read are the following:

    id => uuid

    self link => online resource

        href => linkage

        collection/id => name

        “STAC Collection” => protocole

    items link : online resource

        href => linkage

        collection/id => name

        “STAC Items” => protocole

    example link => online resource

        href => linkage

        title => name

        description => description

        “WWW:LINK-1.0-http--link” => protocole

    title => title

    assets/thumbnail => graphic overview

    extent/spatial => extent as geographic element

    extent/temporal => extent as spatial element

    contact => contact as point of contact

    license => legal constraints as other

    keywords => keywords

    description => abstract

    sci:doi

        identifier

            sci:doi => code with anchor

For example, a “sci:doi” of "10.5281/zenodo.15489231" will produce “<gcx:Anchor xlink:href="https://doi.org/10.18167/GEODE/8429">10.18167/GEODE/8429</gcx:Anchor>”

    “doi.org” => code space

    “Digital Object Identifier (DOI)” => description

    online resource

        same URL as the one forged for the code anchor => linkage

        “DOI” => protocole

        “Digital Object Identifier (DOI)” => name


## Adding a simple URL harvester

Follow exactly the same steps as other **Simple URL** harvesters.