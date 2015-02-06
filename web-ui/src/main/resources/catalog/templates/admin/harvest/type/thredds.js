// This is not that much elegant and should be replaced by some kind
// of Angular module.
var gnHarvesterthredds = {
    createNew : function() {
        return {
            "@id" : "",
            "@type" : "thredds",
            "owner" : [],
            "ownerGroup" : [],
            "site" : {
                "name" : "",
                "translations": {},
                "uuid" : "",
                "account" : {
                    "use" : false,
                    "username" : [],
                    "password" : []
                },
                "url" : "http://",
                "icon" : "blank.gif"
            },
            "content" : {
            },
            "options" : {
                "every" : "0 0 0 ? * *",
                "oneRunOnly" : false,
                "status" : "active",
                "lang" : "eng",
                "topic" : "",
                "createThumbnails" : true,
                "createAtomicDatasetMd" : false,
                "ignoreHarvestOnAtomics" : false,
                "atomicGeneration" : "default",
                "modifiedOnly": false,
                "atomicFragmentStylesheet" : "",
                "atomicMetadataTemplate" : "",
                "createAtomicSubtemplates" : "",
                "outputSchemaOnAtomicsDIF" : "",
                "outputSchemaOnAtomicsFragments" : "",
                "ignoreHarvestOnCollections" : false,
                "collectionGeneration" : "default",
                "collectionFragmentStylesheet" : "",
                "collectionMetadataTemplate" : "",
                "createCollectionSubtemplates" : false,
                "outputSchemaOnCollectionsDIF" : "",
                "outputSchemaOnCollectionsFragments" : "",
                "datasetCategory" : ""
            },
            "privileges" : [ {
                "@id" : "1",
                "operation" : [ {
                    "@name" : "view"
                }, {
                    "@name" : "dynamic"
                } ]
            } ],
            "categories" : [{'@id': ''}],
            "info" : {
                "lastRun" : [],
                "running" : false
            }
        };
    },
    buildResponse : function(h, $scope) {
        var body = '<node id="' + h['@id'] + '" ' 
                + '    type="' + h['@type'] + '">' 
                + '  <ownerGroup><id>' + h.ownerGroup[0] + '</id></ownerGroup>' 
                + '  <site>' 
                + '    <name>' + h.site.name + '</name>'
                + $scope.buildTranslations(h)
                + '    <url>' + h.site.url.replace(/&/g, '&amp;') + '</url>'
                + '    <icon>' + h.site.icon + '</icon>' 
                + '    <account>'
                + '      <use>' + h.site.account.use + '</use>'
                + '      <username>' + h.site.account.username + '</username>' 
                + '      <password>' + h.site.account.password + '</password>' 
                + '    </account>'
                + '  </site>' 
                + '  <options>' 
                + '    <oneRunOnly>' + h.options.oneRunOnly + '</oneRunOnly>' 
                + '    <every>' + h.options.every + '</every>'
                + '    <status>' + h.options.status + '</status>'
                + '    <lang>' + h.options.lang + '</lang>' 
                + '    <topic>' + h.options.topic + '</topic>'
                + '    <createThumbnails>' + h.options.createThumbnails + '</createThumbnails>' 
                + '    <createServiceMd>' + h.options.createServiceMd + '</createServiceMd>' 
                + '    <createCollectionDatasetMd>' + h.options.createCollectionDatasetMd + '</createCollectionDatasetMd>' 
                + '    <createAtomicDatasetMd>' + h.options.createAtomicDatasetMd + '</createAtomicDatasetMd>'
                + '    <ignoreHarvestOnAtomics>' + h.options.ignoreHarvestOnAtomics + '</ignoreHarvestOnAtomics>' 
                + '    <atomicGeneration>' + h.options.atomicGeneration + '</atomicGeneration>' 
                + '    <modifiedOnly>' + h.options.modifiedOnly + '</modifiedOnly>' 
                + '    <atomicFragmentStylesheet>' + h.options.atomicFragmentStylesheet + '</atomicFragmentStylesheet>' 
                + '    <atomicMetadataTemplate>' + h.options.atomicMetadataTemplate + '</atomicMetadataTemplate>' 
                + '    <createAtomicSubtemplates>' + h.options.createAtomicSubtemplates + '</createAtomicSubtemplates>' 
                + '    <outputSchemaOnAtomicsDIF>' + h.options.outputSchemaOnAtomicsDIF + '</outputSchemaOnAtomicsDIF>' 
                + '    <outputSchemaOnAtomicsFragments>' + h.options.outputSchemaOnAtomicsFragments + '</outputSchemaOnAtomicsFragments>' 
                + '    <ignoreHarvestOnCollections>' + h.options.ignoreHarvestOnCollections + '</ignoreHarvestOnCollections>' 
                + '    <collectionGeneration>' + h.options.collectionGeneration + '</collectionGeneration>' 
                + '    <collectionFragmentStylesheet>' + h.options.collectionFragmentStylesheet + '</collectionFragmentStylesheet>' 
                + '    <collectionMetadataTemplate>' + h.options.collectionMetadataTemplate + '</collectionMetadataTemplate>' 
                + '    <createCollectionSubtemplates>' + h.options.createCollectionSubtemplates + '</createCollectionSubtemplates>' 
                + '    <outputSchemaOnCollectionsDIF>' + h.options.outputSchemaOnCollectionsDIF + '</outputSchemaOnCollectionsDIF>' 
                + '    <outputSchemaOnCollectionsFragments>' + h.options.outputSchemaOnCollectionsFragments + '</outputSchemaOnCollectionsFragments>' 
                + '    <datasetCategory>' + h.options.datasetCategory + '</datasetCategory>' 
                + '  </options>' 
                + '  <content>'
                + '  </content>' 
                + $scope.buildResponseGroup(h)
                + $scope.buildResponseCategory(h) + '</node>';
        return body;
    }
};