# Manage EDMO contacts

## Generate contact XML files

1. Get the XML file available at the following address: http://seadatanet.maris2.nl/webservices/edmo/ws_edmo_get_list

2. Apply the transformation EDMO2Geonetwork.xsl to this XML. This transformation is available at /home/isi-projets/seadatanet2/EDMO

One xml file by contact will be created. They will be created in the following directory: /home/isi-projets/seadatanet2/EDMO/geonetwork.


## Import contact in geonetwork

You have to ways to add these contacts in geonetwork:

### Add/update a single organisation

1. go to geonetwork's admin panel (/geonetwork/srv/eng/admin) and select “Manage directories” in the “Thesauri and classification systems” section. 
2. Click on Add
3. Copy and paste your xml text in the text input
4. Select the right group in the dropdown list
5. Click on Add
6. Refresh contacts list by clicking search again

### Organisations batch import

You have the possibility to import organisations with a batch import action. Here is the procedure to follow:

1. First of all, all organisations must be defined in separate ISO19139 xml files and located in a common directory.
2. Go to system administration > Import, export & harvesting > Batch Import

3. In the batch import form:
    1. define the directory where the contacts are located
    2. Choose overwrite metadata with same UUID
    3. Select Kind > Subtemplate
    4. Select the right group 
    5. Category > none

## Set contact privileges

* EDMO must be available for : Emodnet chemistry2 , seadatanet and Medsea
* EDMERP must be available for: Medsea
* MYOCEAN must be available for MYOCEAN-CORE-PRODUCTS, MYOCEAN-INTERMEDIATE-PRODUCTS, MYOCEAN-UPSTREAM-PRODUCTS


# Test : Harvest contact
