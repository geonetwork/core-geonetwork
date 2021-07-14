/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

(function() {
  goog.provide('gn_mdview_directive');

  var module = angular.module('gn_mdview_directive', [
    'ui.bootstrap.tpls',
    'ui.bootstrap.rating']);

  /**
   * Directive to set the proper link to open
   * a metadata record in the default angular view
   * or using a formatter.
   */
  module.directive('gnMetadataOpen', [
    'gnMdViewObj', 'gnMdView',
    function(gnMdViewObj, gnMdView) {
      return {
        restrict: 'A',
        scope: {
          md: '=gnMetadataOpen',
          formatter: '=gnFormatter',
          records: '=gnRecords',
          selector: '@gnMetadataOpenSelector'
        },
        link: function(scope, element, attrs, controller) {
          scope.$watch('md', function(n, o) {
            if (n == null || n == undefined) {
              return;
            }

            var formatter = scope.formatter === undefined || scope.formatter == '' ?
              undefined :
              scope.formatter.replace('../api/records/{{uuid}}/formatters/', '');

            var hyperlinkTagName = 'A';
            if (element.get(0).tagName === hyperlinkTagName) {
              var url = window.location.pathname
                + (window.location.search === '?debug' ? '?debug' : '')
                + '#/'
                + (scope.md.draft == 'y' ? 'metadraf' : 'metadata')
                + '/'
                + encodeURIComponent(scope.md.uuid)
                + (scope.formatter === undefined || scope.formatter == ''
                  ? '' : formatter);

              element.attr('href', url);
            } else {
              element.on('click', function(e) {
                gnMdView.setLocationUuid(encodeURIComponent(scope.md.uuid), formatter);
              });
            }
            if (scope.records && scope.records.length) {
              gnMdViewObj.records = scope.records;
            } else {
              gnMdViewObj.records = [];
            }
          });
        }
      };
    }]
  );


  /**
   * https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-mlt-query.html
   */
  module.directive('gnMoreLikeThis', [
    '$http', 'gnGlobalSettings', function($http, gnGlobalSettings) {
      return {
        scope: {
          md: '=gnMoreLikeThis'
        },
        templateUrl: function(elem, attrs) {
          return attrs.template ||
            '../../catalog/components/search/mdview/partials/' +
            'morelikethis.html';
        },
        link: function(scope, element, attrs, controller) {
          scope.similarDocuments = [];
          var moreLikeThisQuery = {};
          angular.copy(gnGlobalSettings.gnCfg.mods.search.moreLikeThisConfig, moreLikeThisQuery);
          var query = {
            "_source": {
              "include": ['id',
                'uuid',
                'overview.*',
                'resource*',
                'cl_status*'
              ]
            },
            "query": {
              "bool": {
                "must": [
                  moreLikeThisQuery,
                  {"terms": {"isTemplate": ["n"]}}, // TODO: We may want to use it for subtemplate
                  {"terms": {"draft": ["n", "e"]}}
                ]}
            }
          };

          function loadMore() {
            if (scope.md == null) {
              return;
            }
            query.query.bool.must[0].more_like_this.like = scope.md.resourceTitleObject.default;
            $http.post('../api/search/records/_search', query).then(function (r) {
              scope.similarDocuments = r.data.hits;
            })
          }
          scope.$watch('md', function() {
            scope.similarDocuments = [];
            loadMore();
          });

        }
      };
    }]);


  module.directive('gnMetadataDisplay', [
    'gnMdView', 'gnSearchSettings', function(gnMdView, gnSearchSettings) {
      return {
        scope: true,
        templateUrl: function(elem, attrs) {
          return attrs.template ||
              '../../catalog/components/search/mdview/partials/' +
              'mdpanel.html';
        },
        link: function(scope, element, attrs, controller) {

          var unRegister;

          element.find('.panel-body').append(scope.fragment);
          scope.dismiss = function() {
            unRegister();
            // Do not close parent mdview
            if ($('[gn-metadata-display] ~ [gn-metadata-display]')
                .length == 0) {
              gnMdView.removeLocationUuid();
            }
            element.remove();
            //TODO: is the scope destroyed ?
          };

          if (gnSearchSettings.dismissMdView) {
            scope.dismiss = gnSearchSettings.dismissMdView;
          }
          unRegister = scope.$on('locationBackToSearchFromMdview', function() {
            scope.dismiss();
          });
        }
      };
    }]);

  module.directive('gnMetadataRate', [
    '$http', 'gnConfig', 'gnConfigService',
    function($http, gnConfig, gnConfigService) {
      return {
        templateUrl: '../../catalog/components/search/mdview/partials/' +
            'rate.html',
        restrict: 'A',
        scope: {
          md: '=gnMetadataRate',
          readonly: '@readonly'
        },

        link: function(scope, element, attrs, controller) {
          scope.isRatingEnabled = false;

          gnConfigService.load().then(function(c) {
            var statusSystemRating =
              gnConfig[gnConfig.key.isRatingUserFeedbackEnabled];
            if (statusSystemRating == 'advanced') {
              scope.isUserFeedbackEnabled = true;
            }
            if (statusSystemRating == 'basic') {
              scope.isRatingEnabled = true;
            }
          });

          scope.$watch('md', function() {
            scope.rate = scope.md ? scope.md.rating : null;
          });


          scope.rateForRecord = function() {
            return $http.put('../api/records/' + scope.md.uuid +
                             '/rate', scope.rate).success(function(data) {
              scope.rate = data;
            });
          };
        }
      };
    }]
  );

  /**
   * Directive to provide 3 visualization modes for metadata contacts
   * in metadata detail page:
   *
   * - 'default': plain list of contacts.
   *
   * - 'role': grouped by role, then by organisation. Example rendering:
   *
   *      Resource provider
   *       Organisation 1
   *       List of users with role
   *       Address organisation 1
   *
   *       Organisation 2
   *       List of users with role
   *       Address organisation 1
   *
   *      Custodian,Distributor
   *       Organisation 1
   *       List of users with role
   *       Address organisation 1
   *
   * - 'org-role': grouped by organisation, then by role. Example rendering:
   *
   *      Organisation 1
   *      Address organisation 1
   *      Resource provider : user1@mail.com
   *      Custodian, Distributor :  user2@mail.com
   *
   *      Organisation 2
   *      Address organisation 2
   *      Resource provider : user3@mail.com
   */
  module.directive('gnMetadataContacts', [
    '$http', '$filter',
    function($http, $filter) {
      return {
        templateUrl: '../../catalog/components/search/mdview/partials/' +
            'contact.html',
        restrict: 'A',
        scope: {
          mdContacts: '=gnMetadataContacts',
          mode: '@gnMode'
        },
        link: function(scope, element, attrs, controller) {
          if (['default', 'role', 'org-role'].indexOf(scope.mode) == -1) {
            scope.mode = 'default';
          }

          scope.calculateContacts = function() {
            if (scope.mode != 'default') {
              var groupByOrgAndMailOrName = function(resources) {
                return _.groupBy(resources,
                  function(contact) {
                    if (contact.email) {
                      return contact.organisation + '#' + contact.email;
                    } else {
                      return contact.organisation + '#' + contact.individual;
                    }
                  });
              };

              var aggregateRoles = function(resources) {
                return _.map(resources,
                  function(contact) {
                    var copy = angular.copy(contact[0]);
                    angular.extend(copy, {
                      roles: _.pluck(contact, 'role')
                    });

                    return copy;
                  });
              };

              if (scope.mode == 'role') {
                var contactsByOrgAndMailOrName =
                  groupByOrgAndMailOrName(scope.mdContacts);

                var contactsWithAggregatedRoles =
                  aggregateRoles(contactsByOrgAndMailOrName);

                /**
                 * Contacts format:
                 *
                 * {
               *    {[roles]: [{contact1}, {contact2}, ... },
               *    {[roles]: [{contact3}, {contact4}, ... },
               * }
                 *
                 */
                scope.mdContactsByRole =
                  _.groupBy(contactsWithAggregatedRoles, function(c) {
                    return c.roles;
                  });
              } else if (scope.mode == 'org-role') {
                /**
                 * Contacts format:
                 *
                 * {
               *    {organisation1: [{contact1}, {contact2}, ... },
               *    {organisation2: [{contact3}, {contact4}, ... },
               * }
                 *
                 */
                scope.orgWebsite = {};
                scope.mdContactsByOrgRole = _.groupBy(scope.mdContacts,
                  function(contact) {
                    if (contact.website !== '') {
                     scope.orgWebsite[contact.organisation] = contact.website;
                    }
                    return contact.organisation;
                  });

                for (var key in scope.mdContactsByOrgRole) {
                  var value = scope.mdContactsByOrgRole[key];

                  var contactsByOrgAndMailOrName = groupByOrgAndMailOrName(value);

                  scope.mdContactsByOrgRole[key] =
                    aggregateRoles(contactsByOrgAndMailOrName);
                }
              }
            }
          };

          /**
           * Splits a comma separated list of role keys and
           * returns a comma separated list of role translations.
           *
           * @param roles
           * @returns {string|*}
           */
          scope.translateRoles = function(roles) {
            if (roles) {
              var rolesList = roles.split(',');
              var roleTranslations = [];

              for(var i = 0; i < rolesList.length; i++) {
                roleTranslations.push($filter('translate')(rolesList[i]));
              }

              return roleTranslations.join(',');
            } else {
              return '';
            }
          };

          scope.$watch('mdContacts', function () {
            scope.calculateContacts();
          });
        }
      };
    }]
  );
})();
