(function() {
  'use strict';
  goog.provide('inspire_empty_metadata_factory');

  var module = angular.module('inspire_empty_metadata_factory', []);

  module.factory('inspireEmptyMetadataLoader', [ function() {
    return function() {

        // This data is used as test data for SaveTest so run SaveTest when editing this file
        // START TEST DATA
        return {
          "roleOptions": [],
          "dateTypeOptions": [],
          "hierarchyLevelOptions": [],
          "topicCategoryOptions": [],
          "constraintOptions": [],
          "serviceTypeOptions": [],
          "scopeCodeOptions": [],
          "metadataTypeOptions": ['data', 'service'],

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
          "otherLanguages": ['ger', 'fre', 'ita', 'eng', 'roh'],
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
            }]
          },
          "constraints": {
            "legal": [],
            "generic": [],
            "security": []
          },
          "conformity": {
            "conformanceResultRef": '',
            scopeCode: '',
            "scopeCodeDescription": {},
            "title": {},
            "date": {
              "date": '',
              "dateTagName": '',
              "dateType": ''
            },
            "pass": "",
            "explanation": "",
            "lineage": {
              "ref": "",
              "statement" : {}
            }
          }
        };
        // END TEST DATA
    };
  }]);
}());

