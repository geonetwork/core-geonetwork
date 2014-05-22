(function() {
  goog.provide('inspire_mock_full_metadata_factory');

  var module = angular.module('inspire_metadata_factory', []);

  module.factory('inspireMetadataLoader', [ '$q', function($q) {
    return function(url, mdId) {
      var deferred = $q.defer();
      deferred.resolve(
        // This data is used as test data for SaveTest so run SaveTest when editing this file
        // START TEST DATA
        {
          roleOptions: ['pointOfContact', 'owner', 'custodian'],
          dateTypeOptions: ['creation', 'publication', 'revision'],
          hierarchyLevelOptions: ['attribute', 'attributeType', 'dataset'],
          topicCategoryOptions: ['transportation', 'imageryBaseMapsEarthCover_BaseMaps', 'location'],
          constraintOptions: ['intellectualPropertyRights', 'copyright', 'otherRestrictions'],

          language: "eng",
          characterSet: "utf8",
          hierarchyLevel: "dataset",
          hierarchyLevelName: "",
          contact: [{
            id: '1', // id indicates this is a shared object
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
          },{
            id: '',
            name: 'New',
            surname: 'User',
            email: 'new.user@camptocamp.com',
            organization: {
              eng: "Camptocamp SA",
              ger: "Camptocamp AG"
            },
            role: 'pointOfContact',
            validated: false
          }],
          otherLanguages: ['eng', 'ger', 'fre'],
          identification: {
            type: 'data',
            title: {eng: 'Title',fre: 'Titre'},
            date: '2008-06-23',
            dateTagName: 'gco:Date',
            dateType: 'creation',
            citationIdentifier: "Citation Identifier",
            language: "ger",
            abstract: {eng: 'Abstract EN', fre: 'Abstract FR'},
            pointOfContact:  [{
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
            },{
              id: '1',
              name: 'Florent',
              surname: 'Gravin',
              email: 'florent.gravin@camptocamp.com',
              organization: {
                eng:"camptocamp SA"
              },
              role: 'owner',
              validated: false
            }],
            descriptiveKeywords: [
              {code: 'http://rdfdata.eionet.europa.eu/inspirethemes/themes/15', words: {eng: 'Buildings'}},
              {code: 'http://rdfdata.eionet.europa.eu/inspirethemes/themes/9', words: {eng: 'Hydrography', ger: 'Schutzgebiete'}}],
            topicCategory: ['transportation', 'imageryBaseMapsEarthCover_BaseMaps'],
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
          },
          constraints: {
            legal: [{
              ref: 'ref',
              type: 'legal',
              accessConstraint: 'copyright',
              useConstraint: 'intellectualPropertyRights',
              useLimitations: [{eng:'leg limitation 1'}, {eng:'leg limitation 2'}],
              otherConstraints: [{eng:'otherConstraint'}, {eng: 'other constraint 2'}],
              legislationConstraints: [{eng: 'legislation constraint title'}]
            },{
              ref: 'ref',
              type: 'legal',
              accessConstraint: 'otherRestrictions',
              useConstraint: 'intellectualPropertyRights',
              useLimitations: [],
              otherConstraints: [],
              legislationConstraints: []
            }],
            generic: [{
              ref: 'genRef1',
              type: 'generic',
              useLimitations: [{eng:'limitation 1'}, {eng: 'limitation 2'}]
            }],
            security: [{
              ref: 'secRef',
              type: 'security',
              useLimitations: [{eng:'sec limitation 1'}, {eng:'sec limitation 2'}],
              classification: 'restricted'
            }]
          }
        }
      // END TEST DATA
      );

      return deferred.promise;
    };
  }]);
})();

