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
  goog.provide('gn_session_service');

  var module = angular.module('gn_session_service',
      ['ngCookies', 'ngSanitize']);

  /**
   * Session check & warning service
   *
   * The cookie store information about:
   * * last activity time done on the server (serverTime)
   * * when the session will expire (sessionExpiry)
   *
   * On the client side, we check that the current time
   * is getting closer to the session expiration time
   * and warn user when the session is about to be
   * cancelled on the server. Anonymous user will not have
   * warnings. Session timeout is set in web.xml.
   */
  module.factory('gnSessionService', [
    '$rootScope', '$translate', '$timeout', '$cookies',
    function($rootScope, $translate, $timeout, $cookies) {

      var session = {
        remainingTime: null,
        start: null,
        length: null,
        // Alert user when (time in seconds)
        alertWhen: 60,
        // Add message next to user name when (time in seconds)
        alertInTitleWhen: 60 * 3,
        checkInterval: 10000
      };
      function getSession() {
        return session;
      };
      function getRemainingTime() {
        session.start = moment(parseInt($cookies.get('serverTime')));
        // 0 session length means user is not authenticated.
        session.length =
            ($cookies.get('sessionExpiry') - $cookies.get('serverTime')) / 1000;
        session.remainingTime =
            Math.round(
            moment(parseInt($cookies.get('sessionExpiry'))).diff(
            moment()) / 1000);

        return session.remainingTime;
      };
      function check(user) {
        // User is not yet authenticated
        if ($cookies.get('sessionExpiry') === $cookies.get('serverTime')) {
          return;
        }

        getRemainingTime();

        if (user.isConnected &&
            user.isConnected() &&
            session.remainingTime < session.alertWhen) {
          if (session.remainingTime < 0) {
            $rootScope.$broadcast('StatusUpdated', {
              title: $translate.instant('sessionIsProbablyCancelled'),
              msg: $translate.instant('sessionAlertDisconnectedMsg', {
                startedAt: session.start.format('YYYY-MM-DD HH:mm:ss'),
                length: session.length
              }),
              id: 'session-alert',
              timeout: session.checkInterval,
              type: 'danger'});
          } else {
            $rootScope.$broadcast('StatusUpdated', {
              title: $translate.instant('sessionIsAboutToBeCancelled'),
              msg: $translate.instant('sessionAlertMsg', {
                startedAt: session.start.format('YYYY-MM-DD HH:mm:ss'),
                willBeCancelledIn: session.remainingTime,
                length: session.length
              }),
              id: 'session-alert',
              timeout: session.checkInterval,
              type: 'danger'});
          }
        }
      };
      function scheduleCheck(user, interval) {
        $timeout(function() {
          check(user);
          scheduleCheck(user, interval);
        }, interval || session.checkInterval);
      };
      return {
        getSession: getSession,
        getRemainingTime: getRemainingTime,
        check: check,
        scheduleCheck: scheduleCheck
      };
    }]);
})();
