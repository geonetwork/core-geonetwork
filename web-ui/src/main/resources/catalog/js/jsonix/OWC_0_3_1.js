goog.provide("OWC_0_3_1");

var OWC_0_3_1_Module_Factory = function () {
  var OWC_0_3_1 = {
    n: "OWC_0_3_1",
    dens: "http://www.opengis.net/ows-context",
    tis: [
      {
        ln: "ResourceListType",
        ps: [
          {
            n: "layer",
            col: true,
            en: "Layer",
            ti: "OWC_0_3_1.LayerType"
          }
        ]
      },
      {
        ln: "OWSContextType",
        ps: [
          {
            n: "general",
            en: "General",
            ti: "OWC_0_3_1.GeneralType"
          },
          {
            n: "resourceList",
            en: "ResourceList",
            ti: "OWC_0_3_1.ResourceListType"
          },
          {
            n: "version",
            t: "a"
          },
          {
            n: "id",
            t: "a"
          }
        ]
      },
      {
        ln: "LayerType",
        bti: "OWC_0_3_1.AbstractResourceType",
        ps: [
          {
            n: "dimensionList",
            en: "DimensionList",
            ti: "OWC_0_3_1.DimensionListType"
          },
          {
            n: "responseCRS",
            en: "ResponseCRS"
          },
          {
            n: "parameterList",
            en: "ParameterList",
            ti: "OWC_0_3_1.ParameterListType"
          },
          {
            n: "depth",
            en: "Depth"
          },
          {
            n: "resx",
            en: "Resx"
          },
          {
            n: "resy",
            en: "Resy"
          },
          {
            n: "resz",
            en: "Resz"
          },
          {
            n: "maxFeatures",
            en: "MaxFeatures",
            ti: "Integer"
          },
          {
            n: "filter",
            en: {
              lp: "Filter",
              ns: "http://www.opengis.net/ogc"
            },
            ti: "Filter_1_0_0.FilterType"
          },
          {
            n: "inlineGeometry",
            en: "InlineGeometry",
            ti: "OWC_0_3_1.InlineFeatureCollectionType"
          },
          {
            n: "document",
            col: true,
            mx: false,
            t: "ae"
          },
          {
            n: "vendorExtension",
            en: "VendorExtension",
            ti: "OWC_0_3_1.VendorExtensionType"
          },
          {
            n: "queryable",
            ti: "Boolean",
            t: "a"
          }
        ]
      },
      {
        ln: "ServerType",
        ps: [
          {
            n: "onlineResource",
            col: true,
            en: "OnlineResource",
            ti: "OWC_0_3_1.OnlineResourceType"
          },
          {
            n: "_default",
            ti: "Boolean",
            an: "default",
            t: "a"
          },
          {
            n: "service",
            t: "a"
          },
          {
            n: "version",
            t: "a"
          },
          {
            n: "title",
            t: "a"
          }
        ]
      },
      {
        ln: "Attribution",
        ps: [
          {
            n: "onlineResource",
            col: true,
            en: "OnlineResource",
            ti: "OWC_0_3_1.OnlineResourceType"
          },
          {
            n: "title",
            en: "Title"
          }
        ]
      },
      {
        ln: "DimensionType",
        ps: [
          {
            n: "value",
            t: "v"
          },
          {
            n: "name",
            t: "a"
          },
          {
            n: "units",
            t: "a"
          },
          {
            n: "unitSymbol",
            t: "a"
          },
          {
            n: "userValue",
            t: "a"
          },
          {
            n: "_default",
            an: "default",
            t: "a"
          },
          {
            n: "multipleValues",
            ti: "Boolean",
            t: "a"
          },
          {
            n: "nearestValue",
            ti: "Boolean",
            t: "a"
          },
          {
            n: "current",
            ti: "Boolean",
            t: "a"
          }
        ]
      },
      {
        ln: "StyleType",
        ps: [
          {
            n: "name",
            en: "Name"
          },
          {
            n: "title",
            en: "Title"
          },
          {
            n: "_abstract",
            en: "Abstract"
          },
          {
            n: "legendURL",
            en: "LegendURL",
            ti: "OWC_0_3_1.URLType"
          },
          {
            n: "sld",
            en: "SLD",
            ti: "OWC_0_3_1.SLDType"
          },
          {
            n: "current",
            ti: "Boolean",
            t: "a"
          }
        ]
      },
      {
        ln: "DimensionListType",
        ps: [
          {
            n: "dimension",
            col: true,
            en: "Dimension",
            ti: "OWC_0_3_1.DimensionType"
          }
        ]
      },
      {
        ln: "GeneralType",
        ps: [
          {
            n: "window",
            en: "Window",
            ti: "OWC_0_3_1.WindowType"
          },
          {
            n: "boundingBox",
            mx: false,
            dom: false,
            en: {
              lp: "BoundingBox",
              ns: "http://www.opengis.net/ows"
            },
            ti: "OWS_1_0_0.BoundingBoxType",
            t: "er"
          },
          {
            n: "minScaleDenominator",
            en: {
              lp: "MinScaleDenominator",
              ns: "http://www.opengis.net/sld"
            },
            ti: "Double"
          },
          {
            n: "maxScaleDenominator",
            en: {
              lp: "MaxScaleDenominator",
              ns: "http://www.opengis.net/sld"
            },
            ti: "Double"
          },
          {
            n: "title",
            en: {
              lp: "Title",
              ns: "http://www.opengis.net/ows"
            }
          },
          {
            n: "_abstract",
            en: {
              lp: "Abstract",
              ns: "http://www.opengis.net/ows"
            }
          },
          {
            n: "keywords",
            en: {
              lp: "Keywords",
              ns: "http://www.opengis.net/ows"
            },
            ti: "OWS_1_0_0.KeywordsType"
          },
          {
            n: "logoURL",
            en: "LogoURL",
            ti: "OWC_0_3_1.URLType"
          },
          {
            n: "descriptionURL",
            en: "DescriptionURL",
            ti: "OWC_0_3_1.URLType"
          },
          {
            n: "serviceProvider",
            en: {
              lp: "ServiceProvider",
              ns: "http://www.opengis.net/ows"
            },
            ti: "OWS_1_0_0.ServiceProvider"
          },
          {
            n: "extension",
            en: "Extension",
            ti: "OWC_0_3_1.ExtensionType"
          }
        ]
      },
      {
        ln: "FormatType",
        ps: [
          {
            n: "value",
            t: "v"
          },
          {
            n: "current",
            ti: "Boolean",
            t: "a"
          }
        ]
      },
      {
        ln: "InlineFeatureCollectionType",
        bti: "GML_2_1_2.AbstractFeatureCollectionType"
      },
      {
        ln: "OnlineResourceType",
        bti: "OWS_1_0_0.OnlineResourceType",
        ps: [
          {
            n: "method",
            t: "a"
          }
        ]
      },
      {
        ln: "StyleListType",
        ps: [
          {
            n: "style",
            col: true,
            en: "Style",
            ti: "OWC_0_3_1.StyleType"
          }
        ]
      },
      {
        ln: "ExtensionType",
        ps: [
          {
            n: "any",
            mx: true,
            t: "ae"
          }
        ]
      },
      {
        ln: "VendorExtensionType",
        ps: [
          {
            n: "any",
            mx: true,
            t: "ae"
          },
          {
            n: "attribution",
            col: true,
            en: "Attribution",
            ti: "OWC_0_3_1.Attribution"
          }
        ]
      },
      {
        ln: "FormatListType",
        ps: [
          {
            n: "format",
            col: true,
            en: "Format",
            ti: "OWC_0_3_1.FormatType"
          }
        ]
      },
      {
        ln: "AbstractResourceType",
        bti: "OWS_1_0_0.IdentificationType",
        ps: [
          {
            n: "server",
            col: true,
            en: "Server",
            ti: "OWC_0_3_1.ServerType"
          },
          {
            n: "dataURL",
            en: "DataURL",
            ti: "OWC_0_3_1.URLType"
          },
          {
            n: "metadataURL",
            en: "MetadataURL",
            ti: "OWC_0_3_1.URLType"
          },
          {
            n: "minScaleDenominator",
            en: {
              lp: "MinScaleDenominator",
              ns: "http://www.opengis.net/sld"
            },
            ti: "Double"
          },
          {
            n: "maxScaleDenominator",
            en: {
              lp: "MaxScaleDenominator",
              ns: "http://www.opengis.net/sld"
            },
            ti: "Double"
          },
          {
            n: "styleList",
            en: "StyleList",
            ti: "OWC_0_3_1.StyleListType"
          },
          {
            n: "extension",
            en: "Extension",
            ti: "OWC_0_3_1.ExtensionType"
          },
          {
            n: "layer",
            col: true,
            en: "Layer",
            ti: "OWC_0_3_1.LayerType"
          },
          {
            n: "name",
            t: "a"
          },
          {
            n: "id",
            t: "a"
          },
          {
            n: "group",
            t: "a"
          },
          {
            n: "groupcombo",
            t: "a"
          },
          {
            n: "hidden",
            ti: "Boolean",
            t: "a"
          },
          {
            n: "opacity",
            ti: "Decimal",
            t: "a"
          }
        ]
      },
      {
        ln: "WindowType",
        ps: [
          {
            n: "width",
            ti: "Integer",
            t: "a"
          },
          {
            n: "height",
            ti: "Integer",
            t: "a"
          }
        ]
      },
      {
        ln: "ParameterListType",
        ps: [
          {
            n: "parameter",
            col: true,
            en: "Parameter",
            ti: "OWC_0_3_1.ExtensionType"
          }
        ]
      },
      {
        ln: "SLDType",
        ps: [
          {
            n: "name",
            en: "Name"
          },
          {
            n: "title",
            en: "Title"
          },
          {
            n: "legendURL",
            en: "LegendURL",
            ti: "OWC_0_3_1.URLType"
          },
          {
            n: "onlineResource",
            en: "OnlineResource",
            ti: "OWC_0_3_1.OnlineResourceType"
          },
          {
            n: "styledLayerDescriptor",
            en: {
              lp: "StyledLayerDescriptor",
              ns: "http://www.opengis.net/sld"
            },
            ti: "SLD_1_0_0.StyledLayerDescriptor"
          },
          {
            n: "featureTypeStyle",
            en: {
              lp: "FeatureTypeStyle",
              ns: "http://www.opengis.net/sld"
            },
            ti: "SLD_1_0_0.FeatureTypeStyle"
          }
        ]
      },
      {
        ln: "URLType",
        ps: [
          {
            n: "onlineResource",
            en: "OnlineResource",
            ti: "OWC_0_3_1.OnlineResourceType"
          },
          {
            n: "width",
            ti: "Integer",
            t: "a"
          },
          {
            n: "height",
            ti: "Integer",
            t: "a"
          },
          {
            n: "format",
            t: "a"
          }
        ]
      },
      {
        t: "enum",
        ln: "ServiceType",
        vs: [
          "urn:ogc:serviceType:WMS",
          "urn:ogc:serviceType:WFS",
          "urn:ogc:serviceType:WCS",
          "urn:ogc:serviceType:GML",
          "urn:ogc:serviceType:SLD",
          "urn:ogc:serviceType:FES",
          "urn:ogc:serviceType:KML"
        ]
      },
      {
        t: "enum",
        ln: "MethodType",
        vs: ["GET", "POST"]
      }
    ],
    eis: [
      {
        en: "OWSContext",
        ti: "OWC_0_3_1.OWSContextType"
      },
      {
        en: "Layer",
        ti: "OWC_0_3_1.LayerType"
      },
      {
        en: "ResourceList",
        ti: "OWC_0_3_1.ResourceListType"
      }
    ]
  };
  return {
    OWC_0_3_1: OWC_0_3_1
  };
};
if (typeof define === "function" && define.amd) {
  define([], OWC_0_3_1_Module_Factory);
} else {
  if (typeof module !== "undefined" && module.exports) {
    /**
     *
     * @type {{n: string, dens: string, tis: *[], eis: *[]}|OWC_0_3_1}
     */
    module.exports.OWC_0_3_1 = OWC_0_3_1_Module_Factory().OWC_0_3_1;
  } else {
    var OWC_0_3_1 = OWC_0_3_1_Module_Factory().OWC_0_3_1;
  }
}
