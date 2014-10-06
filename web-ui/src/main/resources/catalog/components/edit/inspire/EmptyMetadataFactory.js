(function() {
  'use strict';
  goog.provide('inspire_empty_metadata_factory');

  var module = angular.module('inspire_empty_metadata_factory', []);

  module.factory('inspireEmptyMetadataLoader', [ function() {
    return function() {

        // This data is used as test data for SaveTest so run SaveTest when editing this file
        // START TEST DATA
        return {
          "metadataIsXsdValid": true,
          "roleOptions": [],
          "dateTypeOptions": [],
          "hierarchyLevelOptions": [],
          "topicCategoryOptions": [],
          "constraintOptions": [],
          "serviceTypeOptions": [],
          "scopeCodeOptions": [],
          "metadataTypeOptions": ['data', 'service'],
          "conformityTitleOptions": [],
          "dcpListOptions": [],
          "couplingTypeOptions": [],
          "refSysOptions": [],

          "language": "",
          "characterSet": "utf-8",
          "hierarchyLevel": "",
          "hierarchyLevelName": "",
          "contact": [{
            "id": '', // id indicates this is a shared object
            "name": '',
            "surname": '',
            "email": '',
            "organization": {},
            "role": '',
            "validated": false
          }],
          "otherLanguages": [],
          "identification": {
            "type": 'data',
            "title": {},
            "date": {
              "date": '',
              "dateTagName": '',
              "dateType": ''
            },
            "citationIdentifier": '',
            "language": '',
            "abstract": {},
            "pointOfContact":  [{
              "id": '', // id indicates this is a shared object
              "name": '',
              "surname": '',
              "email": '',
              "organization": {},
              "role": '',
              "validated": false
            }],
            "descriptiveKeywords": [{code: '', words: {}}],
            "topicCategory": [''],
            "serviceType": "",
            "extents": [{
              "description": {},
              "geom": ""
            }],
            "containsOperations": [
              {
                "ref": "",
                "operationName": "",
                "dcpList": "",
                "links": [
                  {
                    "ref": "",
                    "protocol": "",
                    "localizedURL": {},
                    "description": {},
                    "xpath": "gmd:linkage"
                  }
                ]
              }
            ],
            "couplingType": ""
          },
          "constraints": {
            "legal": [],
            "generic": [],
            "security": []
          },
          "conformity": {
            "updateResultRef": "",
            "allConformanceReports": [{
              "conformanceResultRef":"",
              "scopeCode" : "",
              "levelDescription" : "",
              "title": {},
              "pass": "",
              "explanation": ""
            }],
            "isTitleSet": false,
            "reportIndex" : -1,
            "lineage": {
              "ref": "",
              "statement" : {}
            }
          },
          "distributionFormats": [],
          "refSys": []
        };
        // END TEST DATA
    };
  }]);
}());

