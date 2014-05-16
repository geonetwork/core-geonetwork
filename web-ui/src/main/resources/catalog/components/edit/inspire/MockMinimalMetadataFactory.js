(function() {
  goog.provide('inspire_mock_minimal_metadata_factory');

  var module = angular.module('inspire_metadata_factory', []);

  module.factory('inspireMetadataLoader', [ '$q', function($q) {
    return function(url, mdId) {
      var deferred = $q.defer();
      deferred.resolve({
        roleOptions: ['pointOfContact', 'owner', 'custodian'],
        dateTypeOptions: ['creation', 'publication', 'revision'],
        hierarchyLevelOptions: [
          'Attribute',
          'AttributeType',
          'Dataset'
        ],

        language: "",
        characterSet: "",
        hierarchyLevel: "",
        contact: [{
          id: '',
          name: '',
          surname: '',
          email: '',
          organization: {},
          role: '',
          validated: false
        }],
        otherLanguages: [],
        identification: {
          type: 'data',
          title: {},
          date: '',
          dateType: '',
          citationIdentifier: '',
          abstract: {fre: ''},
          pointOfContact:  [{
            id: '',
            name: '',
            surname: '',
            email: '',
            organization: {},
            role: '',
            validated: false
          }],
          descriptiveKeywords: [],
          extents: []
        }
      });

      return deferred.promise;
    };
  }]);
})();

