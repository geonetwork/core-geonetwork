(function() {
  goog.provide('inspire_mock_full_metadata_factory');

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

        language: "eng",
        characterSet: "UTF8",
        hierarchyLevel: "Dataset",
        contact: [{
          id: '1',
          name: 'Florent',
          surname: 'Gravin',
          email: 'florent.gravin@camptocamp.com',
          organization: {
            eng:"camptocamp SA"
          },
          role: 'owner',
          validated: false
        },{
          id: '2',
          name: 'Jesse',
          surname: 'Eichar',
          email: 'jesse.eichar@camptocamp.com',
          organization: {
            eng: "Camptocamp SA",
            ger: "Camptocamp AG"
          },
          role: 'pointOfContact',
          validated: true
        }],
        otherLanguages: ['eng', 'ger'],
        identification: {
          type: 'data',
          title: {eng: 'Title',fre: 'Titre'},
          date: '2008-06-23',
          dateTagName: 'gco:Date',
          dateType: 'creation',
          citationIdentifier: 'identifier',
          abstract: {fre: 'Abstract'},
          pointOfContact:  [{
            id: '1',
            name: 'Florent',
            surname: 'Gravin',
            email: 'florent.gravin@camptocamp.com',
            organization: {
              eng:"camptocamp SA"
            },
            role: 'owner',
            validated: false
          },{
            id: '2',
            name: 'Jesse',
            surname: 'Eichar',
            email: 'jesse.eichar@camptocamp.com',
            organization: {
              eng: "Camptocamp SA",
              ger: "Camptocamp AG"
            },
            role: 'pointOfContact',
            validated: true
          }],
          descriptiveKeywords: [
            {words: {eng: 'building'}}, {words: {eng: 'farm', ger: 'Bauernhof'}}],
          extents: [
            {
              description: {
                eng: 'Bern',
                ger: 'Bern'
              },
              geom: "kantone:2"
            },
            {
              description: {
                eng: 'Fribourg',
                ger: 'Fribourg'
              },
              geom: "gemeinden:2196"
            }
          ]
        }
      });

      return deferred.promise;
    };
  }]);
})();

