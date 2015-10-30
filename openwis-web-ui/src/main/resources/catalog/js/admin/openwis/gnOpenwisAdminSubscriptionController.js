(function() {
  goog.provide('gn_openwis_admin_subscription_controller');

  var module = angular.module('gn_openwis_admin_subscription_controller', [
      'datatables', 'datatables.fixedcolumns', 'datatables.columnfilter'
  ]);

  module
      .controller(
          'gnOpenwisAdminSubscriptionController',
          [
              '$scope',
              '$routeParams',
              '$http',
              '$rootScope',
              'DTOptionsBuilder',
              'DTColumnBuilder',
              '$timeout',
              '$translate',
              function($scope, $routeParams, $http, $rootScope,
                  DTOptionsBuilder, DTColumnBuilder, $timeout, $translate) {

                $scope.myself = $("*[name='myself']").val();
                $scope.type = "" + $scope.type + "";
                $scope.dtInstance = {},

                $scope.groups = [];
                $scope.lang = location.href.split('/')[5].substring(0, 3)
                    || 'eng';

                $http({
                  url : $scope.url + 'xml.info?type=groups&_content_type=json',
                  method : 'GET'
                }).success(function(data) {
                  $scope.groups = data.group;
                });

                $scope.dtOptions = DTOptionsBuilder.newOptions().withOption(
                    "sAjaxSource",
                    $scope.url + 'openwis.subscription.search?myself='
                        + $scope.myself).withDataProp('data').withOption(
                    'processing', true).withOption('serverSide', true)
                    .withOption('scrollCollapse', true).withOption('autoWidth',
                        true).withOption('bFilter', false).withPaginationType(
                        'full_numbers');

                $scope.dtColumns = [
                    DTColumnBuilder.newColumn('id').withOption('name', 'id')
                        .notVisible(),
                    DTColumnBuilder.newColumn('title').withOption('name',
                        'title'),
                    DTColumnBuilder.newColumn('user')
                        .withOption('name', 'user'),
                    DTColumnBuilder.newColumn('urn').withOption('name', 'urn'),
                    DTColumnBuilder.newColumn('status').withOption('name',
                        'status'),
                    DTColumnBuilder.newColumn('backup').withOption('name',
                        'backup'),
                    DTColumnBuilder.newColumn('starting_date').withOption(
                        'name', 'starting_date'),
                    DTColumnBuilder
                        .newColumn('actions')
                        .renderWith(
                            function(data, type, full) {

                              var susres = "<a class=\"btn btn-link\" target=\"_blank\" onclick=\"angular.element(this).scope().resume("
                                  + full.id
                                  + ")\"  title=\"Resume\"><i class=\"fa fa-play\"></i></a>";

                              if (full.status == 'ACTIVE') {
                                susres = "<a class=\"btn btn-link\" target=\"_blank\" onclick=\"angular.element(this).scope().suspend("
                                    + full.id
                                    + ")\"  title=\"Suspend\"><i class=\"fa fa-pause\"></i></a>";
                              }

                              return "<div style=\"width:160px\">"
                                  + "<a class=\"btn btn-link\" target=\"_blank\" href=\"catalog.search#/metadata/"
                                  + full.urn
                                  + "\" title=\"View Product\"><i class=\"fa fa-eye\"></i></a>"
                                  + "<a class=\"btn btn-link\" onclick=\"angular.element(this).scope().edit('"
                                  + full.id
                                  + "', '"
                                  + full.title
                                  + "', '"
                                  + full.urn
                                  + "')\" title=\"Edit subscription\"><i class=\"fa fa-edit\"></i></a>"
                                  + susres
                                  + "<a class=\"btn btn-link\" onclick=\"angular.element(this).scope().discard('"
                                  + full.id
                                  + "')\" title=\"Discard subscription\"><i class=\"fa fa-times text-danger\">"
                                  + "</i></a></div>";
                            })
                ];

                $scope.updateData = function() {
                  // Refresh the table
                  $scope.dtOptions.sAjaxSource = $scope.url
                      + 'openwis.subscription.search?myself=' + $scope.myself
                      + '&group=' + $scope.group;
                  if ($scope.dtInstance.dataTable) {
                    $scope.dtInstance.dataTable._fnDraw()
                  }
                };

                $scope.$watch('group', $scope.updateData);

                $scope.suspend = function(id) {
                  $http(
                      {
                        url : $scope.url
                            + 'openwis.subscription.suspend?subscriptionId='
                            + id,
                        method : 'GET'
                      }).success(function(data) {
                    console.log(data);
                    $scope.updateData();
                  });
                };

                $scope.resume = function(id) {
                  $http(
                      {
                        url : $scope.url
                            + 'openwis.subscription.resume?subscriptionId='
                            + id,
                        method : 'GET'
                      }).success(function(data) {
                    console.log(data);
                    $scope.updateData();
                  });
                };

                $scope.edit = function(id, title, urn) {
                  $http(
                      {
                        url : $scope.url
                            + 'openwis.subscription.get?subscriptionId=' + id,
                        method : 'GET'
                      })
                      .success(
                          function(data) {
                            // Convert data so the data model understands it
                            data.primary = data.primaryDissemination.diffusion;
                            if (!data.primary) {
                              data.primary = {};
                            }
                            data.primary.compression = data.primaryDissemination.zipMode;
                            data.primary.email = data.primary.address;
                            data.primary.attachmentMode = data.primary.mailAttachmentMode;
                            data.primary.dispatchMode = data.primary.mailDispatchMode;
                            data.primary.port = parseInt(data.primary.port);
                            data.primary.fileSize = data.primary.checkFileSize;

                            if (data.secondaryDissemination) {
                              data.secondary = data.secondaryDissemination.diffusion;
                              if (!data.secondary) {
                                data.secondary = {};
                              }
                              data.secondary.compression = data.secondaryDissemination.zipMode;
                              data.secondary.email = data.secondary.address;
                              data.secondary.attachmentMode = data.secondary.mailAttachmentMode;
                              data.secondary.dispatchMode = data.secondary.mailDispatchMode;
                              data.secondary.port = parseInt(data.secondary.port);
                              data.secondary.fileSize = data.secondary.checkFileSize;
                            }

                            data.username = data.user;
                            data.title = title;
                            data.metadataUrn = urn;

                            $scope.data = data;
                            $("#" + $scope.type + "Modal").modal();

                            // Set up accordion
                            if (data.primary.email) {
                              if (!$("#" + $scope.type + "mail").hasClass("in")) {
                                $("*[aria-controls=" + $scope.type + "mail]")
                                    .trigger('click');
                              }
                            } else if (data.primary.host) {
                              if (!$("#" + $scope.type + "advanced").hasClass(
                                  "in")) {
                                $(
                                    "*[aria-controls=" + $scope.type
                                        + "advanced]").trigger('click');
                              }
                            } else {

                              if (!$("#" + $scope.type + "stagingPost")
                                  .hasClass("in")) {
                                $(
                                    "*[aria-controls=" + $scope.type
                                        + "stagingPost]").trigger('click');
                              }
                            }
                            if (data.secondary) {
                              if (data.secondary.email) {
                                if (!$(
                                    "#" + $scope.type + "publicDissemination2")
                                    .hasClass("in")) {
                                  $(
                                      "*[aria-controls=" + $scope.type
                                          + "publicDissemination2]").trigger(
                                      'click');
                                }
                                if (!$("#" + $scope.type + "mail2").hasClass(
                                    "in")) {
                                  $("*[aria-controls=" + $scope.type + "mail2]")
                                      .trigger('click');
                                }
                              } else if (data.secondary.host) {

                                if (!$(
                                    "#" + $scope.type + "publicDissemination2")
                                    .hasClass("in")) {
                                  $(
                                      "*[aria-controls=" + $scope.type
                                          + "publicDissemination2]").trigger(
                                      'click');
                                }

                                if (!$("#" + $scope.type + "advanced2")
                                    .hasClass("in")) {
                                  $(
                                      "*[aria-controls=" + $scope.type
                                          + "advanced2]").trigger('click');
                                }
                              } else {
                                if (!$("#" + $scope.type + "stagingPost2")
                                    .hasClass("in")) {
                                  $(
                                      "*[aria-controls=" + $scope.type
                                          + "stagingPost2]").trigger('click');
                                }
                              }
                            }
                          });
                };

                $scope.discard = function(id) {
                  if (window.confirm('Are you sure you want to delete?')) {
                    $http(
                        {
                          url : $scope.url
                              + 'openwis.subscription.discard?subscriptionId='
                              + id,
                          method : 'GET'
                        }).success(function(data) {
                      $scope.updateData();
                    });
                  }
                }

                $scope.next = function() {
                  $timeout(function() {
                    $("li.active", "#" + $scope.type + "Modal").next('li')
                        .find('a').trigger('click')
                  });
                }
                $scope.prev = function() {
                  $timeout(function() {
                    $("li.active", "#" + $scope.type + "Modal").prev('li')
                        .find('a').trigger('click')
                  });
                }
                $scope.close = function() {
                  $("#" + $scope.type + "Modal").modal('hide');
                  $timeout(function() {
                    $($("li", "#" + $scope.type + "Modal")[0]).find('a')
                        .trigger('click')
                  });
                }

                $scope.save = function() {
                  if (!$("#subscribepublicDissemination").hasClass("in")) {
                    $scope.data.primary.email = null;
                    $scope.data.primary.host = null;
                  } else {
                    if (!$("#subscribemail").hasClass("in")) {
                      $scope.data.primary.email = null;
                    } else {
                      $scope.data.primary.host = null;
                    }
                  }

                  if (!$("#subscribepublicDissemination2").hasClass("in")) {
                    $scope.data.primary.email = null;
                    $scope.data.primary.host = null;
                  } else {
                    if (!$("#subscribemail2").hasClass("in")) {
                      $scope.data.primary.email = null;
                    } else {
                      $scope.data.primary.host = null;
                    }
                  }

                  $http({
                    url : 'openwis.subscription.set',
                    params : {
                      'data' : JSON.stringify($scope.data)
                    },
                    method : 'GET'
                  }).success(function(data) {
                    $scope.close();
                    $rootScope.$broadcast('StatusUpdated', {
                      title : $translate('openwisSuccessSubscribe'),
                      message : $translate('openwisSuccessTrackID') + data,
                      timeout : 0,
                      type : 'success'
                    });
                  }).error(
                      function(data) {
                        $rootScope.$broadcast('StatusUpdated', {
                          title : $translate('openwisError'),
                          error : data,
                          message : data.substring(data.indexOf("<body>") + 6,
                              data.lastIndexOf("</body>")),
                          timeout : 0,
                          type : 'danger'
                        });
                      });
                }
              }

          ]);
})();
