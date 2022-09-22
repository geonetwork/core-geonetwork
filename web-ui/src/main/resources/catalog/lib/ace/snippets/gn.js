define("ace/snippets/gn", ["require", "exports", "module"], function (e, t, n) {
  "use strict";
  t.snippetText =
    'snippet agg-term\n	"${1:indexfield}":{"terms": {"field": "${1:indexfield}"}}\n' +
    'snippet agg-term-with-size\n	"${1:indexfield}": {"terms": {"field": "${1:indexfield}", "size": 5}}\n' +
    'snippet agg-term-with-size-and-filter\n	"${1:indexfield}": {"terms": {"field": "${1:indexfield}", "size": 5, "include": ".*"}, "meta": {"caseInsensitiveInclude": true}}\n' +
    'snippet agg-histogram\n	"${1:indexfield}": {"histogram": {"field": "${1:indexfield}", "interval": ${2:interval}, "keyed" : true, "min_doc_count": 1}}\n' +
    'snippet agg-meta\n	"meta": {"collapsed": true, "userHasRole": "isReviewerOrMore"}\n' +
    '',
    t.scope = "json"
});
(function () {
  window.require(["ace/snippets/gn"], function (m) {
    if (typeof module == "object" && typeof exports == "object" && module) {
      module.exports = m;
    }
  });
})();




