(function() {
  goog.provide('gn_session_service');

  var module = angular.module('gn_session_service', ['ngCookies']);

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
        session.start = moment(parseInt($cookies.serverTime));
        // 0 session length means user is not authenticated.
        session.length =
            ($cookies.sessionExpiry - $cookies.serverTime) / 1000;
        session.remainingTime =
            Math.round(
            moment(parseInt($cookies.sessionExpiry)).diff(
            moment()) / 1000);
        return session.remainingTime;
      };
      function check(user) {
        // User is not yet authenticated
        if ($cookies.sessionExpiry === $cookies.serverTime) {
          return;
        }

        getRemainingTime();

        if (user.isConnected &&
            user.isConnected() &&
            session.remainingTime < session.alertWhen) {
          if (session.remainingTime < 0) {
            $rootScope.$broadcast('StatusUpdated', {
              title: $translate('sessionIsProbablyCancelled'),
              msg: $translate('sessionAlertDisconnectedMsg', {
                startedAt: session.start.format('YYYY-MM-DD HH:mm:ss'),
                length: session.length
              }),
              timeout: session.checkInterval,
              type: 'danger'});
          } else {
            $rootScope.$broadcast('StatusUpdated', {
              title: $translate('sessionIsAboutToBeCancelled'),
              msg: $translate('sessionAlertMsg', {
                startedAt: session.start.format('YYYY-MM-DD HH:mm:ss'),
                willBeCancelledIn: session.remainingTime,
                length: session.length
              }),
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
