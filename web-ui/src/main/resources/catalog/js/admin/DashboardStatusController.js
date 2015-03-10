(function() {
  goog.provide('gn_dashboard_status_controller');


  var module = angular.module('gn_dashboard_status_controller',
      []);

  module.filter('count', function() {
    return function(input) {
      return !input ? '' : input.length;
    };
  });
  module.filter('ellipses', function() {
    return function(input) {
      if (input && input.length > 35) {
        return input.substring(0, 39) + ' ...';
      } else {
        return input;
      }
    };
  });

  /**
   *
   */
  module.controller('GnDashboardStatusController', [
    '$scope', '$routeParams', '$http',
    function($scope, $routeParams, $http) {
      $scope.healthy = undefined;
      $scope.nowarnings = undefined;
      $scope.threadSortField = undefined;
      $scope.threadSortReverse = false;
      $scope.threadInfoLoading = false;
      $scope.setThreadSortField = function(field) {
        if (field === $scope.threadSortField) {
          $scope.threadSortReverse = !$scope.threadSortReverse;
        } else {
          if (field === 'name' || field === 'state') {
            $scope.threadSortReverse = false;
          } else {
            $scope.threadSortReverse = true;
          }
          $scope.threadSortField = field;
        }
      };
      $scope.threadSortClass = function(field) {
        if ($scope.threadSortField === field) {
          if ($scope.threadSortReverse) {
            return ['fa', 'fa-sort-up'];
          } else {
            return ['fa', 'fa-sort-down'];
          }
        }
        return '';
      };
      $scope.toggleThreadContentionMonitoring = function() {
        $http.get('thread/debugging/true/' +
            $scope.threadStatus.threadContentionMonitoringEnabled).
            success(function(data) {
              $scope.threadStatus = data;
            });
      };
      $scope.toggleThreadCpuTime = function() {
        $http.get('thread/debugging/false/' +
            $scope.threadStatus.threadCpuTimeEnabled).
            success(function(data) {
              $scope.threadStatus = data;
            });
      };
      $scope.openThreadActivity = function(leaveOpen) {
        var threadActivityEl = $('#threadActivity');
        if (!leaveOpen) {
          threadActivityEl.collapse('toggle');
        }
        $scope.threadInfoLoading = true;
        $http.get('thread/status').success(function(data) {
          $scope.threadInfoLoading = false;
          $scope.threadStatus = data;

          if (!leaveOpen) {
            $('html, body').animate({
              scrollTop: $('#threadActivityHeading').offset().top
            }, 1000);
          }

          setTimeout(function() {
            if (threadActivityEl.hasClass('in')) {
              $scope.openThreadActivity(true);
            }
          }, 2000);
        }).error(function() {
          $scope.threadInfoLoading = false;
        });
      };
      $scope.showStackTrace = function(thread, $event) {
        $scope.selectedThread = thread;
        $scope.threadStackTrace = 'Loading...';
        $('#stackTrace').modal('toggle');
        $http.get('thread/trace/' + thread.id).success(function(data) {
          $scope.threadStackTrace = data.stackTrace;
        });
      };
      $http.get('../../criticalhealthcheck').success(function(data) {
        $scope.healthy = true;
        $scope.criticalhealthcheck = data;
      }).error(function(data) {
        $scope.healthy = false;
        $scope.criticalhealthcheck = data;
      });

      $http.get('../../warninghealthcheck').success(function(data) {
        $scope.nowarnings = true;
        $scope.warninghealthcheck = data;
      }).error(function(data) {
        $scope.nowarnings = false;
        $scope.warninghealthcheck = data;
      });

      $scope.indexMessages = function(md) {
        if (angular.isArray(md.idxMsg)) {
          return md.idxMsg;
        }

        return [md.idxMsg];
      };
      $scope.indexMessageTitle = function(errorMsg) {
        if (errorMsg === undefined) {
          return 'Empty error message';
        }
        return errorMsg.split('|')[0];
      };
      $scope.indexMessageReason = function(errorMsg) {
        if (errorMsg === undefined) {
          return 'Empty error message';
        }
        return errorMsg.split('|')[1];
      };
      $scope.rawIndexMessageDetail = function(errorMsg) {
        if (errorMsg === undefined) {
          return 'Empty error message';
        }
        return errorMsg.split('|')[2];
      };
      $scope.restrictMessageWidth = function(detail) {
        var maxLine = 80,
            indentPattern = /(\s*).*/;

        if (!detail || detail.trim() == '') {
          return '';
        }

        var lines = detail.split('\n');

        detail = '';

        var nextSpace = function(line) {
          for (var j = maxLine; j < line.length; j++) {
            if (' ' === line.charAt(j)) {
              return j;
            }
          }
          return line.length;
        };

        for (var i = 0; i < lines.length; i++) {
          var line = lines[i];
          var indent = indentPattern.exec(line)[1] + '    ';
          while (line.length > maxLine) {
            var ns = nextSpace(line);
            detail += line.substring(0, ns) + '\n';
            line = indent + line.substring(ns);
          }
          detail += line + '\n';
        }
        return detail;
      };
      $scope.indexMessageDetail = function(errorMsg) {
        return $scope.restrictMessageWidth(
            $scope.rawIndexMessageDetail(errorMsg));
      };
      $scope.searchObj = {
        params: {
          _indexingError: 1,
          sortBy: 'changeDate'
        }
      };
    }]);

})();
