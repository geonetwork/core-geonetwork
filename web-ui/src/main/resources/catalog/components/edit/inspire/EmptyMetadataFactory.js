(function() {
  goog.provide('inspire_empty_metadata_factory');

  var module = angular.module('inspire_empty_metadata_factory', []);

  module.factory('inspireEmptyMetadataLoader', [ '$q', function($q) {
    return function(guiLanguage) {

        // This data is used as test data for SaveTest so run SaveTest when editing this file
        // START TEST DATA
        return {
          roleOptions: [],
          dateTypeOptions: [],
          hierarchyLevelOptions: [],
          topicCategoryOptions: [],
          constraintOptions: [],
          serviceTypeOptions: [],
          metadataTypeOptions: ['data', 'service'],

          language: "",
          characterSet: "utf-8",
          hierarchyLevel: "",
          hierarchyLevelName: "",
          contact: [{
            id: '', // id indicates this is a shared object
            name: '',
            surname: '',
            email: '',
            organization: {},
            role: '',
            validated: false
          }],
          otherLanguages: ['ger', 'fre', 'ita', 'eng', 'roh'],
          identification: {
            type: 'data',
            title: {},
            date: {
              date:'',
              dateTagName: '',
              dateType: ''
            },
            citationIdentifier: {},
            language: '',
            abstract: {},
            pointOfContact:  [{
              id: '', // id indicates this is a shared object
              name: '',
              surname: '',
              email: '',
              organization: {},
              role: '',
              validated: false
            }],
            descriptiveKeywords: [{code: '', words: {}}],
            topicCategory: [''],
            serviceType: "",
            extents: [{
              description: {},
              geom: ""
            }]
          },
          constraints: {
            legal: [],
            generic: [],
            security: []
          }
        };
        // END TEST DATA
    };
  }]);
})();

