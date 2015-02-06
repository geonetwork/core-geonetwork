(function() {
  goog.provide('gn_utility');


  goog.require('gn_utility_directive');
  goog.require('gn_utility_service');

  angular.module('gn_utility', [
    'gn_utility_service',
    'gn_utility_directive'
  ])
  .filter('characters', function() {
        return function(input, chars, breakOnWord) {
          if (isNaN(chars)) return input;
          if (chars <= 0) return '';
          if (input && input.length >= chars) {
            input = input.substring(0, chars);

            if (!breakOnWord) {
              var lastspace = input.lastIndexOf(' ');
              //get last space
              if (lastspace !== -1) {
                input = input.substr(0, lastspace);
              }
            }else {
              while (input.charAt(input.length - 1) == ' ') {
                input = input.substr(0, input.length - 1);
              }
            }
            return input + '...';
          }
          return input;
        };
      })
.filter('words', function() {
        return function(input, words) {
          if (isNaN(words)) return input;
          if (words <= 0) return '';
          if (input) {
            var inputWords = input.split(/\s+/);
            if (inputWords.length > words) {
              input = inputWords.slice(0, words).join(' ') + '...';
            }
          }
          return input;
        };
      })
.filter('striptags', function() {
        return function(value, allowed) {
          if (!value) return value;
          allowed = (((allowed || '') + '').toLowerCase().
              match(/<[a-z][a-z0-9]*>/g) || []).join('');
          var tags = /<\/?([a-z][a-z0-9]*)\b[^>]*>/gi,
              commentsAndPhpTags = /<!--[\s\S]*?-->|<\?(?:php)?[\s\S]*?\?>/gi;
          return value.replace(commentsAndPhpTags, '').
              replace(tags, function($0, $1) {
                return allowed.indexOf(
                        '<' + $1.toLowerCase() + '>') > -1 ? $0 : '';
              });
        };
      })
.filter('split', function() {
		//filter to split a string and grab the nth item (default splitter: '|', default item: 1st), used on {{metadata[n].type | split:',':0 }}
        return function(input, splitChar, splitIndex) {
			if (!input) return "";
			if (!splitIndex) splitIndex=0;
			if (!splitChar) splitChar="|";
			if (!input.split(splitChar).length > splitIndex) return "";
            return input.split(splitChar)[splitIndex];
        }
    });

})();
