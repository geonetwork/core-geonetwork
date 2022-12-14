setTimeout(function() {

  var basePath = "../../catalog/lib/zip/";
  if (sxtGnUrl !== '') {
    basePath = sxtGnUrl + "../catalog/lib/zip/";
  }

  var buildBlob = function(response) {
    var blob;
    try {
      blob = new Blob([response], {type: 'application/javascript'});
    } catch (e) {
      window.BlobBuilder = window.BlobBuilder || window.WebKitBlobBuilder || window.MozBlobBuilder;
      blob = new BlobBuilder();
      blob.append(response);
      blob = blob.getBlob();
    }
    return URL.createObjectURL(blob);
  };

  var loadWorker = function(fileName) {
    return $.ajax({
      url        : basePath + fileName,
      converters : {
        'text script' : function (text) { return text; }
      }
    });
  }

  $.when(
    loadWorker('worker_deflater.js'),
    loadWorker('worker_inflater.js')
  ).then(function(deflaterResponse, inflaterResponse) {
    zip.workerScriptsPath = null;
    zip.workerScripts = {
      deflater: [ buildBlob(deflaterResponse[0]) ],
      inflater: [ buildBlob(inflaterResponse[0]) ]
    };
  }).fail(function(){
    console.log('Fail to load zip workers');
  });

},1);
