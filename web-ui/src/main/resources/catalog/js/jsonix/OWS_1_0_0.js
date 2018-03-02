goog.provide('OWS_1_0_0');

var OWS_1_0_0_Module_Factory = function() {
  var OWS_1_0_0 = {
    n: 'OWS_1_0_0',
    dens: 'http:\/\/www.opengis.net\/ows',
    dans: 'http:\/\/www.w3.org\/1999\/xlink',
    tis: [{
      ln: 'OperationsMetadata',
      ps: [{
        n: 'operation',
        col: true,
        en: 'Operation',
        ti: 'OWS_1_0_0.Operation'
      }, {
        n: 'parameter',
        col: true,
        en: 'Parameter',
        ti: 'OWS_1_0_0.DomainType'
      }, {
        n: 'constraint',
        col: true,
        en: 'Constraint',
        ti: 'OWS_1_0_0.DomainType'
      }, {
        n: 'extendedCapabilities',
        en: 'ExtendedCapabilities',
        ti: 'AnyType'
      }]
    }, {
      ln: 'Operation',
      ps: [{
        n: 'dcp',
        col: true,
        en: 'DCP',
        ti: 'OWS_1_0_0.DCP'
      }, {
        n: 'parameter',
        col: true,
        en: 'Parameter',
        ti: 'OWS_1_0_0.DomainType'
      }, {
        n: 'constraint',
        col: true,
        en: 'Constraint',
        ti: 'OWS_1_0_0.DomainType'
      }, {
        n: 'metadata',
        col: true,
        en: 'Metadata',
        ti: 'OWS_1_0_0.MetadataType'
      }, {
        n: 'name',
        an: {
          lp: 'name'
        },
        t: 'a'
      }]
    }, {
      ln: 'DCP',
      ps: [{
        n: 'http',
        en: 'HTTP',
        ti: 'OWS_1_0_0.HTTP'
      }]
    }, {
      ln: 'HTTP',
      ps: [{
        n: 'getOrPost',
        col: true,
        mx: false,
        dom: false,
        typed: true,
        etis: [{
                en: 'Get',
                ti: 'OWS_1_0_0.RequestMethodType'
              }, {
                en: 'Post',
                ti: 'OWS_1_0_0.RequestMethodType'
              }],
        t: 'ers'
      }]
    }, {
      ln: 'RequestMethodType',
      bti: 'OWS_1_0_0.OnlineResourceType',
      ps: [{
        n: 'constraint',
        col: true,
        en: 'Constraint',
        ti: 'OWS_1_0_0.DomainType'
      }]
    }, {
      ln: 'DomainType',
      ps: [{
        n: 'value',
        col: true,
        en: 'Value'
      }, {
        n: 'metadata',
        col: true,
        en: 'Metadata',
        ti: 'OWS_1_0_0.MetadataType'
      }, {
        n: 'name',
        an: {
          lp: 'name'
        },
        t: 'a'
      }]
    }, {
      ln: 'MetadataType',
      ps: [{
        n: 'abstractMetaData',
        en: 'AbstractMetaData',
        ti: 'AnyType'
      }, {
        n: 'about',
        an: {
          lp: 'about'
        },
        t: 'a'
      }, {
        n: 'type',
        ti: 'XLink_1_0.TypeType',
        t: 'a'
      }, {
        n: 'href',
        t: 'a'
      }, {
        n: 'role',
        t: 'a'
      }, {
        n: 'arcrole',
        t: 'a'
      }, {
        n: 'title',
        t: 'a'
      }, {
        n: 'show',
        ti: 'XLink_1_0.ShowType',
        t: 'a'
      }, {
        n: 'actuate',
        ti: 'XLink_1_0.ActuateType',
        t: 'a'
      }]
    }, {
      ln: 'KeywordsType',
      ps: [{
        n: 'keyword',
        col: true,
        en: 'Keyword'
      }, {
        n: 'type',
        en: 'Type',
        ti: 'OWS_1_0_0.CodeType'
      }]
    }, {
      ln: 'ExceptionReport',
      ps: [{
        n: 'exception',
        col: true,
        en: 'Exception',
        ti: 'OWS_1_0_0.ExceptionType'
      }, {
        n: 'version',
        an: {
          lp: 'version'
        },
        t: 'a'
      }, {
        n: 'language',
        an: {
          lp: 'language'
        },
        t: 'a'
      }]
    }, {
      ln: 'ExceptionType',
      ps: [{
        n: 'exceptionText',
        col: true,
        en: 'ExceptionText'
      }, {
        n: 'exceptionCode',
        an: {
          lp: 'exceptionCode'
        },
        t: 'a'
      }, {
        n: 'locator',
        an: {
          lp: 'locator'
        },
        t: 'a'
      }]
    }, {
      ln: 'WGS84BoundingBoxType',
      bti: 'OWS_1_0_0.BoundingBoxType'
    }, {
      ln: 'BoundingBoxType',
      ps: [{
        n: 'lowerCorner',
        en: 'LowerCorner',
        ti: {
          t: 'l',
          ti: 'Double'
        }
      }, {
        n: 'upperCorner',
        en: 'UpperCorner',
        ti: {
          t: 'l',
          ti: 'Double'
        }
      }, {
        n: 'crs',
        an: {
          lp: 'crs'
        },
        t: 'a'
      }, {
        n: 'dimensions',
        ti: 'Integer',
        an: {
          lp: 'dimensions'
        },
        t: 'a'
      }]
    }, {
      ln: 'CodeType',
      ps: [{
        n: 'value',
        t: 'v'
      }, {
        n: 'codeSpace',
        an: {
          lp: 'codeSpace'
        },
        t: 'a'
      }]
    }, {
      ln: 'GetCapabilitiesType',
      ps: [{
        n: 'acceptVersions',
        en: 'AcceptVersions',
        ti: 'OWS_1_0_0.AcceptVersionsType'
      }, {
        n: 'sections',
        en: 'Sections',
        ti: 'OWS_1_0_0.SectionsType'
      }, {
        n: 'acceptFormats',
        en: 'AcceptFormats',
        ti: 'OWS_1_0_0.AcceptFormatsType'
      }, {
        n: 'updateSequence',
        an: {
          lp: 'updateSequence'
        },
        t: 'a'
      }]
    }, {
      ln: 'ContactType',
      ps: [{
        n: 'phone',
        en: 'Phone',
        ti: 'OWS_1_0_0.TelephoneType'
      }, {
        n: 'address',
        en: 'Address',
        ti: 'OWS_1_0_0.AddressType'
      }, {
        n: 'onlineResource',
        en: 'OnlineResource',
        ti: 'OWS_1_0_0.OnlineResourceType'
      }, {
        n: 'hoursOfService',
        en: 'HoursOfService'
      }, {
        n: 'contactInstructions',
        en: 'ContactInstructions'
      }]
    }, {
      ln: 'ServiceIdentification',
      bti: 'OWS_1_0_0.DescriptionType',
      ps: [{
        n: 'serviceType',
        en: 'ServiceType',
        ti: 'OWS_1_0_0.CodeType'
      }, {
        n: 'serviceTypeVersion',
        col: true,
        en: 'ServiceTypeVersion'
      }, {
        n: 'fees',
        en: 'Fees'
      }, {
        n: 'accessConstraints',
        col: true,
        en: 'AccessConstraints'
      }]
    }, {
      ln: 'DescriptionType',
      ps: [{
        n: 'title',
        en: 'Title'
      }, {
        n: '_abstract',
        en: 'Abstract'
      }, {
        n: 'keywords',
        col: true,
        en: 'Keywords',
        ti: 'OWS_1_0_0.KeywordsType'
      }]
    }, {
      ln: 'ResponsiblePartyType',
      ps: [{
        n: 'individualName',
        en: 'IndividualName'
      }, {
        n: 'organisationName',
        en: 'OrganisationName'
      }, {
        n: 'positionName',
        en: 'PositionName'
      }, {
        n: 'contactInfo',
        en: 'ContactInfo',
        ti: 'OWS_1_0_0.ContactType'
      }, {
        n: 'role',
        en: 'Role',
        ti: 'OWS_1_0_0.CodeType'
      }]
    }, {
      ln: 'ServiceProvider',
      ps: [{
        n: 'providerName',
        en: 'ProviderName'
      }, {
        n: 'providerSite',
        en: 'ProviderSite',
        ti: 'OWS_1_0_0.OnlineResourceType'
      }, {
        n: 'serviceContact',
        en: 'ServiceContact',
        ti: 'OWS_1_0_0.ResponsiblePartySubsetType'
      }]
    }, {
      ln: 'OnlineResourceType',
      ps: [{
        n: 'type',
        ti: 'XLink_1_0.TypeType',
        t: 'a'
      }, {
        n: 'href',
        t: 'a'
      }, {
        n: 'role',
        t: 'a'
      }, {
        n: 'arcrole',
        t: 'a'
      }, {
        n: 'title',
        t: 'a'
      }, {
        n: 'show',
        ti: 'XLink_1_0.ShowType',
        t: 'a'
      }, {
        n: 'actuate',
        ti: 'XLink_1_0.ActuateType',
        t: 'a'
      }]
    }, {
      ln: 'ResponsiblePartySubsetType',
      ps: [{
        n: 'individualName',
        en: 'IndividualName'
      }, {
        n: 'positionName',
        en: 'PositionName'
      }, {
        n: 'contactInfo',
        en: 'ContactInfo',
        ti: 'OWS_1_0_0.ContactType'
      }, {
        n: 'role',
        en: 'Role',
        ti: 'OWS_1_0_0.CodeType'
      }]
    }, {
      ln: 'TelephoneType',
      ps: [{
        n: 'voice',
        col: true,
        en: 'Voice'
      }, {
        n: 'facsimile',
        col: true,
        en: 'Facsimile'
      }]
    }, {
      ln: 'CapabilitiesBaseType',
      ps: [{
        n: 'serviceIdentification',
        en: 'ServiceIdentification',
        ti: 'OWS_1_0_0.ServiceIdentification'
      }, {
        n: 'serviceProvider',
        en: 'ServiceProvider',
        ti: 'OWS_1_0_0.ServiceProvider'
      }, {
        n: 'operationsMetadata',
        en: 'OperationsMetadata',
        ti: 'OWS_1_0_0.OperationsMetadata'
      }, {
        n: 'version',
        an: {
          lp: 'version'
        },
        t: 'a'
      }, {
        n: 'updateSequence',
        an: {
          lp: 'updateSequence'
        },
        t: 'a'
      }]
    }, {
      ln: 'AcceptVersionsType',
      ps: [{
        n: 'version',
        col: true,
        en: 'Version'
      }]
    }, {
      ln: 'IdentificationType',
      bti: 'OWS_1_0_0.DescriptionType',
      ps: [{
        n: 'identifier',
        en: 'Identifier',
        ti: 'OWS_1_0_0.CodeType'
      }, {
        n: 'boundingBox',
        col: true,
        mx: false,
        dom: false,
        typed: true,
        en: 'BoundingBox',
        ti: 'OWS_1_0_0.BoundingBoxType',
        t: 'er'
      }, {
        n: 'outputFormat',
        col: true,
        en: 'OutputFormat'
      }, {
        n: 'availableCRS',
        col: true,
        mx: false,
        dom: false,
        typed: true,
        en: 'AvailableCRS',
        t: 'er'
      }, {
        n: 'metadata',
        col: true,
        en: 'Metadata',
        ti: 'OWS_1_0_0.MetadataType'
      }]
    }, {
      ln: 'AcceptFormatsType',
      ps: [{
        n: 'outputFormat',
        col: true,
        en: 'OutputFormat'
      }]
    }, {
      ln: 'SectionsType',
      ps: [{
        n: 'section',
        col: true,
        en: 'Section'
      }]
    }, {
      ln: 'AddressType',
      ps: [{
        n: 'deliveryPoint',
        col: true,
        en: 'DeliveryPoint'
      }, {
        n: 'city',
        en: 'City'
      }, {
        n: 'administrativeArea',
        en: 'AdministrativeArea'
      }, {
        n: 'postalCode',
        en: 'PostalCode'
      }, {
        n: 'country',
        en: 'Country'
      }, {
        n: 'electronicMailAddress',
        col: true,
        en: 'ElectronicMailAddress'
      }]
    }],
    eis: [{
      en: 'OperationsMetadata',
      ti: 'OWS_1_0_0.OperationsMetadata'
    }, {
      en: 'Operation',
      ti: 'OWS_1_0_0.Operation'
    }, {
      en: 'DCP',
      ti: 'OWS_1_0_0.DCP'
    }, {
      en: 'HTTP',
      ti: 'OWS_1_0_0.HTTP'
    }, {
      en: 'ExceptionReport',
      ti: 'OWS_1_0_0.ExceptionReport'
    }, {
      en: 'ServiceIdentification',
      ti: 'OWS_1_0_0.ServiceIdentification'
    }, {
      en: 'ServiceProvider',
      ti: 'OWS_1_0_0.ServiceProvider'
    }, {
      en: 'WGS84BoundingBox',
      ti: 'OWS_1_0_0.WGS84BoundingBoxType',
      sh: 'BoundingBox'
    }, {
      en: 'Fees'
    }, {
      en: 'Exception',
      ti: 'OWS_1_0_0.ExceptionType'
    }, {
      en: 'Metadata',
      ti: 'OWS_1_0_0.MetadataType'
    }, {
      en: 'OutputFormat'
    }, {
      en: 'Title'
    }, {
      en: 'PositionName'
    }, {
      en: 'Abstract'
    }, {
      en: 'AccessConstraints'
    }, {
      en: 'ExtendedCapabilities',
      ti: 'AnyType'
    }, {
      en: 'Keywords',
      ti: 'OWS_1_0_0.KeywordsType'
    }, {
      en: 'AbstractMetaData',
      ti: 'AnyType'
    }, {
      en: 'SupportedCRS',
      sh: 'AvailableCRS'
    }, {
      en: 'OrganisationName'
    }, {
      en: 'ContactInfo',
      ti: 'OWS_1_0_0.ContactType'
    }, {
      en: 'AvailableCRS'
    }, {
      en: 'Language'
    }, {
      en: 'PointOfContact',
      ti: 'OWS_1_0_0.ResponsiblePartyType'
    }, {
      en: 'GetCapabilities',
      ti: 'OWS_1_0_0.GetCapabilitiesType'
    }, {
      en: 'BoundingBox',
      ti: 'OWS_1_0_0.BoundingBoxType'
    }, {
      en: 'IndividualName'
    }, {
      en: 'Role',
      ti: 'OWS_1_0_0.CodeType'
    }, {
      en: 'Identifier',
      ti: 'OWS_1_0_0.CodeType'
    }, {
      en: 'Post',
      ti: 'OWS_1_0_0.RequestMethodType',
      sc: 'OWS_1_0_0.HTTP'
    }, {
      en: 'Get',
      ti: 'OWS_1_0_0.RequestMethodType',
      sc: 'OWS_1_0_0.HTTP'
    }]
  };
  return {
    OWS_1_0_0: OWS_1_0_0
  };
};
if (typeof define === 'function' && define.amd) {
  define([], OWS_1_0_0_Module_Factory);
}
else {
  if (typeof module !== 'undefined' && module.exports) {
    /**
     *
     * @type {{n: string, dens: string, dans: string, tis: *[], eis: *[]}|OWS_1_0_0}
     */
    module.exports.OWS_1_0_0 = OWS_1_0_0_Module_Factory().OWS_1_0_0;
  }
  else {
    var OWS_1_0_0 = OWS_1_0_0_Module_Factory().OWS_1_0_0;
  }
}
