goog.provide('WFS_1_1_0');

var WFS_1_1_0_Module_Factory = function() {
  var WFS_1_1_0 = {
    n: 'WFS_1_1_0',
    dens: 'http:\/\/www.opengis.net\/wfs',
    deps: ['OWS_1_0_0', 'GML_3_1_1', 'Filter_1_1_0'],
    tis: [{
      ln: 'XlinkPropertyName',
      tn: null,
      ps: [{
        n: 'value',
        t: 'v'
      }, {
        n: 'traverseXlinkDepth',
        an: {
          lp: 'traverseXlinkDepth'
        },
        t: 'a'
      }, {
        n: 'traverseXlinkExpiry',
        ti: 'Integer',
        an: {
          lp: 'traverseXlinkExpiry'
        },
        t: 'a'
      }]
    }, {
      ln: 'OperationsType',
      ps: [{
        n: 'operation',
        col: true,
        en: 'Operation'
      }]
    }, {
      ln: 'GetCapabilitiesType',
      bti: 'OWS_1_0_0.GetCapabilitiesType',
      ps: [{
        n: 'service',
        an: {
          lp: 'service'
        },
        t: 'a'
      }]
    }, {
      ln: 'FeatureTypeType',
      ps: [{
        n: 'name',
        en: 'Name',
        ti: 'QName'
      }, {
        n: 'title',
        en: 'Title'
      }, {
        n: '_abstract',
        en: 'Abstract'
      }, {
        n: 'keywords',
        col: true,
        en: {
          lp: 'Keywords',
          ns: 'http:\/\/www.opengis.net\/ows'
        },
        ti: 'OWS_1_0_0.KeywordsType'
      }, {
        n: 'defaultSRS',
        en: 'DefaultSRS'
      }, {
        n: 'otherSRS',
        col: true,
        en: 'OtherSRS'
      }, {
        n: 'noSRS',
        en: 'NoSRS',
        ti: '.FeatureTypeType.NoSRS'
      }, {
        n: 'operations',
        en: 'Operations',
        ti: '.OperationsType'
      }, {
        n: 'outputFormats',
        en: 'OutputFormats',
        ti: '.OutputFormatListType'
      }, {
        n: 'wgs84BoundingBox',
        col: true,
        en: {
          lp: 'WGS84BoundingBox',
          ns: 'http:\/\/www.opengis.net\/ows'
        },
        ti: 'OWS_1_0_0.WGS84BoundingBoxType'
      }, {
        n: 'metadataURL',
        col: true,
        en: 'MetadataURL',
        ti: '.MetadataURLType'
      }]
    }, {
      ln: 'TransactionSummaryType',
      ps: [{
        n: 'totalInserted',
        ti: 'Integer'
      }, {
        n: 'totalUpdated',
        ti: 'Integer'
      }, {
        n: 'totalDeleted',
        ti: 'Integer'
      }]
    }, {
      ln: 'MetadataURLType',
      ps: [{
        n: 'value',
        t: 'v'
      }, {
        n: 'type',
        an: {
          lp: 'type'
        },
        t: 'a'
      }, {
        n: 'format',
        an: {
          lp: 'format'
        },
        t: 'a'
      }]
    }, {
      ln: 'DeleteElementType',
      ps: [{
        n: 'filter',
        en: {
          lp: 'Filter',
          ns: 'http:\/\/www.opengis.net\/ogc'
        },
        ti: 'Filter_1_1_0.FilterType'
      }, {
        n: 'handle',
        an: {
          lp: 'handle'
        },
        t: 'a'
      }, {
        n: 'typeName',
        ti: 'QName',
        an: {
          lp: 'typeName'
        },
        t: 'a'
      }]
    }, {
      ln: 'QueryType',
      ps: [{
        n: 'propertyNameOrXlinkPropertyNameOrFunction',
        col: true,
        etis: [{
          en: 'PropertyName'
        }, {
          en: 'XlinkPropertyName',
          ti: '.XlinkPropertyName'
        }, {
          en: {
            lp: 'Function',
            ns: 'http:\/\/www.opengis.net\/ogc'
          },
          ti: 'Filter_1_1_0.FunctionType'
        }],
        t: 'es'
      }, {
        n: 'filter',
        en: {
          lp: 'Filter',
          ns: 'http:\/\/www.opengis.net\/ogc'
        },
        ti: 'Filter_1_1_0.FilterType'
      }, {
        n: 'sortBy',
        en: {
          lp: 'SortBy',
          ns: 'http:\/\/www.opengis.net\/ogc'
        },
        ti: 'Filter_1_1_0.SortByType'
      }, {
        n: 'handle',
        an: {
          lp: 'handle'
        },
        t: 'a'
      }, {
        n: 'typeName',
        ti: {
          t: 'l',
          bti: 'QName'
        },
        an: {
          lp: 'typeName'
        },
        t: 'a'
      }, {
        n: 'featureVersion',
        an: {
          lp: 'featureVersion'
        },
        t: 'a'
      }, {
        n: 'srsName',
        an: {
          lp: 'srsName'
        },
        t: 'a'
      }]
    }, {
      ln: 'PropertyType',
      ps: [{
        n: 'name',
        en: 'Name',
        ti: 'QName'
      }, {
        n: 'value',
        en: 'Value',
        ti: 'AnyType'
      }]
    }, {
      ln: 'FeatureCollectionType',
      bti: 'GML_3_1_1.AbstractFeatureCollectionType',
      ps: [{
        n: 'lockId',
        an: {
          lp: 'lockId'
        },
        t: 'a'
      }, {
        n: 'timeStamp',
        ti: 'Calendar',
        an: {
          lp: 'timeStamp'
        },
        t: 'a'
      }, {
        n: 'numberOfFeatures',
        ti: 'Integer',
        an: {
          lp: 'numberOfFeatures'
        },
        t: 'a'
      }]
    }, {
      ln: 'UpdateElementType',
      ps: [{
        n: 'property',
        col: true,
        en: 'Property',
        ti: '.PropertyType'
      }, {
        n: 'filter',
        en: {
          lp: 'Filter',
          ns: 'http:\/\/www.opengis.net\/ogc'
        },
        ti: 'Filter_1_1_0.FilterType'
      }, {
        n: 'handle',
        an: {
          lp: 'handle'
        },
        t: 'a'
      }, {
        n: 'typeName',
        ti: 'QName',
        an: {
          lp: 'typeName'
        },
        t: 'a'
      }, {
        n: 'inputFormat',
        an: {
          lp: 'inputFormat'
        },
        t: 'a'
      }, {
        n: 'srsName',
        an: {
          lp: 'srsName'
        },
        t: 'a'
      }]
    }, {
      ln: 'LockType',
      ps: [{
        n: 'filter',
        en: {
          lp: 'Filter',
          ns: 'http:\/\/www.opengis.net\/ogc'
        },
        ti: 'Filter_1_1_0.FilterType'
      }, {
        n: 'handle',
        an: {
          lp: 'handle'
        },
        t: 'a'
      }, {
        n: 'typeName',
        ti: 'QName',
        an: {
          lp: 'typeName'
        },
        t: 'a'
      }]
    }, {
      ln: 'BaseRequestType',
      ps: [{
        n: 'service',
        an: {
          lp: 'service'
        },
        t: 'a'
      }, {
        n: 'version',
        an: {
          lp: 'version'
        },
        t: 'a'
      }, {
        n: 'handle',
        an: {
          lp: 'handle'
        },
        t: 'a'
      }]
    }, {
      ln: 'GetGmlObjectType',
      bti: '.BaseRequestType',
      ps: [{
        n: 'gmlObjectId',
        en: {
          lp: 'GmlObjectId',
          ns: 'http:\/\/www.opengis.net\/ogc'
        },
        ti: 'Filter_1_1_0.GmlObjectIdType'
      }, {
        n: 'outputFormat',
        an: {
          lp: 'outputFormat'
        },
        t: 'a'
      }, {
        n: 'traverseXlinkDepth',
        an: {
          lp: 'traverseXlinkDepth'
        },
        t: 'a'
      }, {
        n: 'traverseXlinkExpiry',
        ti: 'Integer',
        an: {
          lp: 'traverseXlinkExpiry'
        },
        t: 'a'
      }]
    }, {
      ln: 'TransactionType',
      bti: '.BaseRequestType',
      ps: [{
        n: 'lockId',
        en: 'LockId'
      }, {
        n: 'insertOrUpdateOrDelete',
        col: true,
        etis: [{
          en: 'Insert',
          ti: '.InsertElementType'
        }, {
          en: 'Update',
          ti: '.UpdateElementType'
        }, {
          en: 'Delete',
          ti: '.DeleteElementType'
        }, {
          en: 'Native',
          ti: '.NativeType'
        }],
        t: 'es'
      }, {
        n: 'releaseAction',
        an: {
          lp: 'releaseAction'
        },
        t: 'a'
      }]
    }, {
      ln: 'OutputFormatListType',
      ps: [{
        n: 'format',
        col: true,
        en: 'Format'
      }]
    }, {
      ln: 'InsertResultsType',
      ps: [{
        n: 'feature',
        col: true,
        en: 'Feature',
        ti: '.InsertedFeatureType'
      }]
    }, {
      ln: 'DescribeFeatureTypeType',
      bti: '.BaseRequestType',
      ps: [{
        n: 'typeName',
        col: true,
        en: 'TypeName',
        ti: 'QName'
      }, {
        n: 'outputFormat',
        an: {
          lp: 'outputFormat'
        },
        t: 'a'
      }]
    }, {
      ln: 'GetFeatureType',
      bti: '.BaseRequestType',
      ps: [{
        n: 'query',
        col: true,
        en: 'Query',
        ti: '.QueryType'
      }, {
        n: 'resultType',
        an: {
          lp: 'resultType'
        },
        t: 'a'
      }, {
        n: 'outputFormat',
        an: {
          lp: 'outputFormat'
        },
        t: 'a'
      }, {
        n: 'maxFeatures',
        ti: 'Integer',
        an: {
          lp: 'maxFeatures'
        },
        t: 'a'
      }, {
        n: 'traverseXlinkDepth',
        an: {
          lp: 'traverseXlinkDepth'
        },
        t: 'a'
      }, {
        n: 'traverseXlinkExpiry',
        ti: 'Integer',
        an: {
          lp: 'traverseXlinkExpiry'
        },
        t: 'a'
      }]
    }, {
      ln: 'LockFeatureType',
      bti: '.BaseRequestType',
      ps: [{
        n: 'lock',
        col: true,
        en: 'Lock',
        ti: '.LockType'
      }, {
        n: 'expiry',
        ti: 'Integer',
        an: {
          lp: 'expiry'
        },
        t: 'a'
      }, {
        n: 'lockAction',
        an: {
          lp: 'lockAction'
        },
        t: 'a'
      }]
    }, {
      ln: 'FeaturesLockedType',
      ps: [{
        n: 'featureId',
        col: true,
        en: {
          lp: 'FeatureId',
          ns: 'http:\/\/www.opengis.net\/ogc'
        },
        ti: 'Filter_1_1_0.FeatureIdType'
      }]
    }, {
      ln: 'TransactionResultsType',
      ps: [{
        n: 'action',
        col: true,
        en: 'Action',
        ti: '.ActionType'
      }]
    }, {
      ln: 'InsertElementType',
      ps: [{
        n: 'feature',
        col: true,
        mx: false,
        dom: false,
        en: {
          lp: '_Feature',
          ns: 'http:\/\/www.opengis.net\/gml'
        },
        ti: 'GML_3_1_1.AbstractFeatureType',
        t: 'er'
      }, {
        n: 'idgen',
        an: {
          lp: 'idgen'
        },
        t: 'a'
      }, {
        n: 'handle',
        an: {
          lp: 'handle'
        },
        t: 'a'
      }, {
        n: 'inputFormat',
        an: {
          lp: 'inputFormat'
        },
        t: 'a'
      }, {
        n: 'srsName',
        an: {
          lp: 'srsName'
        },
        t: 'a'
      }]
    }, {
      ln: 'TransactionResponseType',
      ps: [{
        n: 'transactionSummary',
        en: 'TransactionSummary',
        ti: '.TransactionSummaryType'
      }, {
        n: 'transactionResults',
        en: 'TransactionResults',
        ti: '.TransactionResultsType'
      }, {
        n: 'insertResults',
        en: 'InsertResults',
        ti: '.InsertResultsType'
      }, {
        n: 'version',
        an: {
          lp: 'version'
        },
        t: 'a'
      }]
    }, {
      ln: 'NativeType',
      ps: [{
        n: 'vendorId',
        an: {
          lp: 'vendorId'
        },
        t: 'a'
      }, {
        n: 'safeToIgnore',
        ti: 'Boolean',
        an: {
          lp: 'safeToIgnore'
        },
        t: 'a'
      }]
    }, {
      ln: 'WFSCapabilitiesType',
      tn: 'WFS_CapabilitiesType',
      bti: 'OWS_1_0_0.CapabilitiesBaseType',
      ps: [{
        n: 'featureTypeList',
        en: 'FeatureTypeList',
        ti: '.FeatureTypeListType'
      }, {
        n: 'servesGMLObjectTypeList',
        en: 'ServesGMLObjectTypeList',
        ti: '.GMLObjectTypeListType'
      }, {
        n: 'supportsGMLObjectTypeList',
        en: 'SupportsGMLObjectTypeList',
        ti: '.GMLObjectTypeListType'
      }, {
        n: 'filterCapabilities',
        en: {
          lp: 'Filter_Capabilities',
          ns: 'http:\/\/www.opengis.net\/ogc'
        },
        ti: 'Filter_1_1_0.FilterCapabilities'
      }]
    }, {
      ln: 'FeaturesNotLockedType',
      ps: [{
        n: 'featureId',
        col: true,
        en: {
          lp: 'FeatureId',
          ns: 'http:\/\/www.opengis.net\/ogc'
        },
        ti: 'Filter_1_1_0.FeatureIdType'
      }]
    }, {
      ln: 'GMLObjectTypeType',
      ps: [{
        n: 'name',
        en: 'Name',
        ti: 'QName'
      }, {
        n: 'title',
        en: 'Title'
      }, {
        n: '_abstract',
        en: 'Abstract'
      }, {
        n: 'keywords',
        col: true,
        en: {
          lp: 'Keywords',
          ns: 'http:\/\/www.opengis.net\/ows'
        },
        ti: 'OWS_1_0_0.KeywordsType'
      }, {
        n: 'outputFormats',
        en: 'OutputFormats',
        ti: '.OutputFormatListType'
      }]
    }, {
      ln: 'InsertedFeatureType',
      ps: [{
        n: 'featureId',
        col: true,
        en: {
          lp: 'FeatureId',
          ns: 'http:\/\/www.opengis.net\/ogc'
        },
        ti: 'Filter_1_1_0.FeatureIdType'
      }, {
        n: 'handle',
        an: {
          lp: 'handle'
        },
        t: 'a'
      }]
    }, {
      ln: 'ActionType',
      ps: [{
        n: 'message',
        en: 'Message'
      }, {
        n: 'locator',
        an: {
          lp: 'locator'
        },
        t: 'a'
      }, {
        n: 'code',
        an: {
          lp: 'code'
        },
        t: 'a'
      }]
    }, {
      ln: 'GMLObjectTypeListType',
      ps: [{
        n: 'gmlObjectType',
        col: true,
        en: 'GMLObjectType',
        ti: '.GMLObjectTypeType'
      }]
    }, {
      ln: 'FeatureTypeType.NoSRS',
      tn: null
    }, {
      ln: 'LockFeatureResponseType',
      ps: [{
        n: 'lockId',
        en: 'LockId'
      }, {
        n: 'featuresLocked',
        en: 'FeaturesLocked',
        ti: '.FeaturesLockedType'
      }, {
        n: 'featuresNotLocked',
        en: 'FeaturesNotLocked',
        ti: '.FeaturesNotLockedType'
      }]
    }, {
      ln: 'FeatureTypeListType',
      ps: [{
        n: 'operations',
        en: 'Operations',
        ti: '.OperationsType'
      }, {
        n: 'featureType',
        col: true,
        en: 'FeatureType',
        ti: '.FeatureTypeType'
      }]
    }, {
      ln: 'GetFeatureWithLockType',
      bti: '.BaseRequestType',
      ps: [{
        n: 'query',
        col: true,
        en: 'Query',
        ti: '.QueryType'
      }, {
        n: 'expiry',
        ti: 'Integer',
        an: {
          lp: 'expiry'
        },
        t: 'a'
      }, {
        n: 'resultType',
        an: {
          lp: 'resultType'
        },
        t: 'a'
      }, {
        n: 'outputFormat',
        an: {
          lp: 'outputFormat'
        },
        t: 'a'
      }, {
        n: 'maxFeatures',
        ti: 'Integer',
        an: {
          lp: 'maxFeatures'
        },
        t: 'a'
      }, {
        n: 'traverseXlinkDepth',
        an: {
          lp: 'traverseXlinkDepth'
        },
        t: 'a'
      }, {
        n: 'traverseXlinkExpiry',
        ti: 'Integer',
        an: {
          lp: 'traverseXlinkExpiry'
        },
        t: 'a'
      }]
    }, {
      t: 'enum',
      ln: 'ResultTypeType',
      vs: ['results', 'hits']
    }, {
      t: 'enum',
      ln: 'IdentifierGenerationOptionType',
      vs: ['UseExisting', 'ReplaceDuplicate', 'GenerateNew']
    }, {
      t: 'enum',
      ln: 'AllSomeType',
      vs: ['ALL', 'SOME']
    }, {
      t: 'enum',
      ln: 'OperationType',
      vs: ['Insert', 'Update', 'Delete', 'Query', 'Lock', 'GetGmlObject']
    }],
    eis: [{
      en: 'FeatureTypeList',
      ti: '.FeatureTypeListType'
    }, {
      en: 'Delete',
      ti: '.DeleteElementType'
    }, {
      en: 'Transaction',
      ti: '.TransactionType'
    }, {
      en: 'DescribeFeatureType',
      ti: '.DescribeFeatureTypeType'
    }, {
      en: 'LockId'
    }, {
      en: 'Native',
      ti: '.NativeType'
    }, {
      en: 'Insert',
      ti: '.InsertElementType'
    }, {
      en: 'LockFeatureResponse',
      ti: '.LockFeatureResponseType'
    }, {
      en: 'ServesGMLObjectTypeList',
      ti: '.GMLObjectTypeListType'
    }, {
      en: 'GetCapabilities',
      ti: '.GetCapabilitiesType'
    }, {
      en: 'GetGmlObject',
      ti: '.GetGmlObjectType'
    }, {
      en: 'XlinkPropertyName',
      ti: '.XlinkPropertyName'
    }, {
      en: 'LockFeature',
      ti: '.LockFeatureType'
    }, {
      en: 'PropertyName'
    }, {
      en: 'FeatureCollection',
      ti: '.FeatureCollectionType',
      sh: {
        lp: '_FeatureCollection',
        ns: 'http:\/\/www.opengis.net\/gml'
      }
    }, {
      en: 'GetFeatureWithLock',
      ti: '.GetFeatureWithLockType'
    }, {
      en: 'WFS_Capabilities',
      ti: '.WFSCapabilitiesType'
    }, {
      en: 'SupportsGMLObjectTypeList',
      ti: '.GMLObjectTypeListType'
    }, {
      en: 'GetFeature',
      ti: '.GetFeatureType'
    }, {
      en: 'Property',
      ti: '.PropertyType'
    }, {
      en: 'Query',
      ti: '.QueryType'
    }, {
      en: 'Update',
      ti: '.UpdateElementType'
    }, {
      en: 'TransactionResponse',
      ti: '.TransactionResponseType'
    }]
  };
  return {
    WFS_1_1_0: WFS_1_1_0
  };
};
if (typeof define === 'function' && define.amd) {
  define([], WFS_1_1_0_Module_Factory);
}
else {
  var WFS_1_1_0_Module = WFS_1_1_0_Module_Factory();
  if (typeof module !== 'undefined' && module.exports) {
    /**
     *
     * @type {{n: string, dens: string, deps: string[], tis: *[], eis: *[]}|WFS_1_1_0}
     */
    module.exports.WFS_1_1_0 = WFS_1_1_0_Module.WFS_1_1_0;
  }
  else {
    var WFS_1_1_0 = WFS_1_1_0_Module.WFS_1_1_0;
  }
}
