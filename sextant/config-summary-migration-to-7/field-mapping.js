var facetMapping = {
  "keyword": {
    "tag.default":
      {
        "terms": {
          "field": "tag.default",
          "size": 15
        }
      }
  },
  "keywordType-parameter": {
    "keywordType-parameter.default":
      {
        "terms": {
          "field": "keywordType-parameter.default",
          "size": 300
        }
      }
  },
  "sextantTheme": {
    "th_sextant-theme_tree.key": {
      "terms": {
        "field": "th_sextant-theme_tree.key",
        "size": 100,
        "order": {"_key": "asc"}
      },
      "meta": {
        "collapsed": true,
        "translateOnLoad": true,
        "treeKeySeparator": "/"
      }
    }
  },
  "topicCat": {
    "cl_topic.key":
      {
        "terms": {
          "field": "cl_topic.key",
          "size": 20
        }
      }
  },
  "category": {
    "cat":
      {
        "terms": {
          "field": "cat",
          "size": 99
          , "order": {"_key": "asc"}

        }
      }
  },
  "inspireTheme": {
    "inspireThemeUri": {
      "terms": {
        "field": "inspireThemeUri",
        "size": 34
      }
    }
  },
  "inspireTheme_en": {
    "inspireThemeUri": {
      "terms": {
        "field": "inspireThemeUri",
        "size": 34
      }
    }
  },
  "inspireTheme_fr": {
    "inspireThemeUri": {
      "terms": {
        "field": "inspireThemeUri",
        "size": 34
      }
    }
  },
  "inspireThemeWithAc": {
    "inspireThemeUri": {
      "terms": {
        "field": "inspireThemeUri",
        "size": 34
      }
    }
  },
  "inspireThemeURI": {
    "inspireThemeUri": {
      "terms": {
        "field": "inspireThemeUri",
        "size": 34
      }
    }
  },
  "denominator": {
    "resolutionScaleDenominator": {
      "histogram": {
        "field": "resolutionScaleDenominator",
        "interval": 10000,
        "keyed": true,
        "min_doc_count": 1
      }
    }
  },
  "resolution": {
    "resolutionDistance": {
      "terms": {
        "field": "resolutionDistance",
        "include": ".* (m|km)"
      }
    }
  },
  "spatialRepresentationType": {
    "cl_spatialRepresentationType.key":
      {
        "terms": {
          "field": "cl_spatialRepresentationType.key",
          "size": 10
        }
      }
  },
  "orgName": {
    "OrgForResource": {
      "terms": {
        "field": "OrgForResource",
        "include": ".*",
        "size": 15
      },
      "meta": {
        "caseInsensitiveInclude": true
      }
    }
  },
  "metadataPOC": {
    "Org": {
      "terms": {
        "field": "Org",
        "include": ".*",
        "size": 15
      },
      "meta": {
        "caseInsensitiveInclude": true
      }
    }
  },
  "serviceType": {
    "serviceType":
      {
        "terms": {
          "field": "serviceType",
          "size": 10
        }
      }
  },
  "type": {
    "cl_hierarchyLevel.key":
      {
        "terms": {
          "field": "cl_hierarchyLevel.key",
          "size": 10
        }
      }
  },
  "createDateYear": {
    "creationYearForResource":
      {
        "terms": {
          "field": "creationYearForResource",
          "size": 40
          , "order": {"_key": "desc"}

        }
      }
  },
  "format": {
    "format":
      {
        "terms": {
          "field": "format",
          "size": 15
          , "order": {"_key": "asc"}

        }
      }
  },
  "title": {
    "resourceTitleObject.default.keyword":
      {
        "terms": {
          "field": "resourceTitleObject.default.keyword",
          "size": 100
        }
      }
  },
  "metadataType": {
    "isTemplate":
      {
        "terms": {
          "field": "isTemplate",
          "size": 3
          , "order": {"_key": "asc"}

        }
      }
  },
  "isValid": {
    "valid":
      {
        "terms": {
          "field": "valid",
          "size": 3
          , "order": {"_key": "asc"}

        }
      }
  },
  "isValidInspire": {
    "valid_inspire":
      {
        "terms": {
          "field": "valid_inspire",
          "size": 3
          , "order": {"_key": "asc"}

        }
      }
  },
  "isHarvested": {
    "isHarvested":
      {
        "terms": {
          "field": "isHarvested",
          "size": 2
          , "order": {"_key": "asc"}

        }
      }
  },
  "mdStatus": {
    "mdStatus":
      {
        "terms": {
          "field": "mdStatus",
          "size": 10
          , "order": {"_key": "asc"}

        }
      }
  },
  "status": {
    "cl_status.key":
      {
        "terms": {
          "field": "cl_status.key",
          "size": 10
        }
      }
  },
  "sourceCatalog": {
    "cl_status.key":
      {
        "terms": {
          "field": "cl_status.key",
          "size": 10
        }
      }
  },
  "standard": {
    "documentStandard":
      {
        "terms": {
          "field": "documentStandard",
          "size": 15
          , "order": {"_key": "asc"}

        }
      }
  },
  "subTemplateType": {
    "root":
      {
        "terms": {
          "field": "root",
          "size": 10
        }
      }
  },
  "recordOwner": {
    "recordOwner":
      {
        "terms": {
          "field": "recordOwner",
          "size": 45
          , "order": {"_key": "asc"}

        }
      }
  },
  "groupOwner": {
    "groupOwner":
      {
        "terms": {
          "field": "groupOwner",
          "size": 199
          , "order": {"_key": "asc"}

        }
      }
  },
  "publishedForGroup": {
    "groupPublished":
      {
        "terms": {
          "field": "groupPublished",
          "size": 199
          , "order": {"_key": "asc"}

        }
      }
  },
  "isPublishedToAll": {
    "isPublishedToAll":
      {
        "terms": {
          "field": "isPublishedToAll",
          "size": 2
          , "order": {"_key": "asc"}

        }
      }
  },
  "p01": {
    "th_BODC-Parameter-Usage-Vocabulary.default":
      {
        "terms": {
          "field": "th_BODC-Parameter-Usage-Vocabulary.default",
          "size": 300
        }
      }
  },
  "p02": {
    "th_P02.default":
      {
        "terms": {
          "field": "th_P02.default",
          "size": 300
        }
      }
  },
  "p03": {
    "th_P03.default":
      {
        "terms": {
          "field": "th_P03.default",
          "size": 300
        }
      }
  },
  "p35": {
    "th_P35.default":
      {
        "terms": {
          "field": "th_P35.default",
          "size": 300
        }
      }
  },
  "A05": {
    "th_A05.default":
      {
        "terms": {
          "field": "th_A05.default",
          "size": 300
        }
      }
  },
  "C19": {
    "th_C19.default":
      {
        "terms": {
          "field": "th_C19.default",
          "size": 300
        }
      }
  },
  "L04": {
    "th_L04.default":
      {
        "terms": {
          "field": "th_L04.default",
          "size": 300
        }
      }
  },
  "P36": {
    "th_P36.default":
      {
        "terms": {
          "field": "th_P36.default",
          "size": 300
        }
      }
  },
  "P08": {
    "th_P08.default":
      {
        "terms": {
          "field": "th_P08.default",
          "size": 300
        }
      }
  },
  "atlantos_element": {
    "th_atlantos_element.default":
      {
        "terms": {
          "field": "th_atlantos_element.default",
          "size": 300
        }
      }
  },
  "portail-donnees-facette-discipline": {
    "th_portail-donnees-facette-comment.default":
      {
        "terms": {
          "field": "th_portail-donnees-facette-comment.default",
          "size": 300
        }
      }
  },
  "portail-donnees-facette-type": {
    "th_portail-donnees-facette-type.default":
      {
        "terms": {
          "field": "th_portail-donnees-facette-type.default",
          "size": 300
        }
      }
  },
  "portail-donnees-facette-comment": {
    "th_portail-donnees-facette-comment.default":
      {
        "terms": {
          "field": "th_portail-donnees-facette-comment.default",
          "size": 300
        }
      }
  },
  "SIH_Especes_commerciales": {
    "th_SIH_Especes_commerciales.default":
      {
        "terms": {
          "field": "th_SIH_Especes_commerciales.default",
          "size": 300
        }
      }
  },
  "SIH_Groupes_Especes": {
    "th_SIH_Groupes_Especes.default":
      {
        "terms": {
          "field": "th_SIH_Groupes_Especes.default",
          "size": 300
        }
      }
  },
  "SIH_Engins_valides": {
    "th_SIH_Engins_valides.default":
      {
        "terms": {
          "field": "th_SIH_Engins_valides.default",
          "size": 300
        }
      }
  },
  "SIH_Categories_Engins": {
    "th_SIH_Categories_Engins.default":
      {
        "terms": {
          "field": "th_SIH_Categories_Engins.default",
          "size": 300
        }
      }
  },
  "SIH_Types_donnees": {
    "th_sih_type_donnees.default":
      {
        "terms": {
          "field": "th_sih_type_donnees.default",
          "size": 300
        }
      }
  },
  "SIH_Facade_maritime": {
    "th_sih_facade_maritime.default":
      {
        "terms": {
          "field": "th_sih_facade_maritime.default",
          "size": 300
        }
      }
  },
  "emodnet-checkpoint.environmental.matrix": {
    "th_matrix.default":
      {
        "terms": {
          "field": "th_matrix.default",
          "size": 300
        }
      }
  },
  "emodnet-checkpoint.challenges": {
    "th_challenges.default":
      {
        "terms": {
          "field": "th_challenges.default",
          "size": 300
        }
      }
  },
  "emodnet-checkpoint.level.of.characteristics": {
    "th_characteristics.default":
      {
        "terms": {
          "field": "th_characteristics.default",
          "size": 300
        }
      }
  },
  "emodnet-checkpoint.production.mode": {
    "th_mode.default":
      {
        "terms": {
          "field": "th_mode.default",
          "size": 300
        }
      }
  },
  "ParameterUsageVocabulary-other": {
    "th_P01.default":
      {
        "terms": {
          "field": "th_P01.default",
          "size": 300
        }
      }
  },
  "dcsmm-methode": {
    "th_dcsmm-methode.default":
      {
        "terms": {
          "field": "th_dcsmm-methode.default",
          "size": 300
        }
      }
  },
  "dcsmm-type-espace": {
    "th_dcsmm-type-espace.default":
      {
        "terms": {
          "field": "th_dcsmm-type-espace.default",
          "size": 300
        }
      }
  },
  "dcsmm-area": {
    "th_dcsmm.area.default":
      {
        "terms": {
          "field": "th_dcsmm.area.default",
          "size": 300
        }
      }
  },
  "_groupPublished": {
    "group": {
      "terms": {
        "field": "groupPublishedId",
        "size": 200,
        "include": ".*",
        "exclude": "1"
      },
      "meta": {
        "field": "groupPublishedId",
        "orderByTranslation": true,
        "filterByTranslation": true,
        "displayFilter": true,
        "collapsed": true
      }
    }
  },
  "maintenanceAndUpdateFrequency": {
    "cl_maintenanceAndUpdateFrequency.default":
      {
        "terms": {
          "field": "cl_maintenanceAndUpdateFrequency.default",
          "size": 10
        }
      }
  },
  "odatis_variables": {
    "th_odatis_thematiques.default":
      {
        "terms": {
          "field": "th_odatis_thematiques.default",
          "size": 300
        }
      }
  },
  "odatis_centre_donnees": {
    "th_odatis_centre_donnees.default":
      {
        "terms": {
          "field": "th_odatis_centre_donnees.default",
          "size": 300
        }
      }
  },
  "odatis_type_jeux_donnee": {
    "th_type_jeux_donnee.default":
      {
        "terms": {
          "field": "th_type_jeux_donnee.default",
          "size": 300
        }
      }
  },
  "lops-projets": {
    "th_lops_projets.default":
      {
        "terms": {
          "field": "th_lops_projets.default",
          "size": 300
        }
      }
  },
  "simm-reglementaire": {
    "th_reglementaire.default":
      {
        "terms": {
          "field": "th_reglementaire.default",
          "size": 300
        }
      }
  },
  "simm-thematiques": {
    "th_thematiques.default":
      {
        "terms": {
          "field": "th_thematiques.default",
          "size": 300
        }
      }
  },
  "cersat-platform": {
    "th_cersat_platform.default":
      {
        "terms": {
          "field": "th_cersat_platform.default",
          "size": 300
        }
      }
  },
  "cersat-latency": {
    "th_cersat_latency.default":
      {
        "terms": {
          "field": "th_cersat_latency.default",
          "size": 300
        }
      }
  },
  "cersat-parameter": {
    "th_cersat_parameter.default":
      {
        "terms": {
          "field": "th_cersat_parameter.default",
          "size": 300
        }
      }
  },
  "cersat-sensor": {
    "th_cersat_sensor.default":
      {
        "terms": {
          "field": "th_cersat_sensor.default",
          "size": 300
        }
      }
  },
  "cersat-processing-level": {
    "th_cersat_processing_level.default":
      {
        "terms": {
          "field": "th_cersat_processing_level.default",
          "size": 300
        }
      }
  },
  "cersat-project": {
    "th_cersat_project.default":
      {
        "terms": {
          "field": "th_cersat_project.default",
          "size": 300
        }
      }
  },
  "myocean-reference-geographical-area": {
    "th_reference-geographical-area.default":
      {
        "terms": {
          "field": "th_reference-geographical-area.default",
          "size": 300
        }
      }
  },
  "theme-GCMDparameter": {
    "th_GCMDparameter.default":
      {
        "terms": {
          "field": "th_GCMDparameter.default",
          "size": 300
        }
      }
  },
}
