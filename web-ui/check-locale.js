var fs = require('fs');
var sourceLang = 'en';
var targetLang = 'fr';
var sourceFile = 'src/main/resources/catalog/locales/' + sourceLang + '-admin.json';
var targetFile = 'src/main/resources/catalog/locales/' + targetLang + '-admin.json';
 
fs.readFile(sourceFile, 'utf8', function (err, source) {
  if (err) {
    console.log('Error reading source file: ' + err);
    return;
  }
 
  fs.readFile(targetFile, 'utf8', function (targetErr, target) {
    if (targetErr) {
      console.log('Error reading target file: ' + targetErr);
      return;
    }


    source = JSON.parse(source);
    target = JSON.parse(target);
    targetAndMissing = {};
    for (key in source) {
      if (source.hasOwnProperty(key)) {
        console.log(key + ':' + source[key]);
        // check entry is in target locale
        targetAndMissing[key] = target[key] || source[key];
      }
    }
    console.log('Missing terms: ' + (source.length - targetAndMissing.length));
    fs.writeFile(targetFile + '.new', JSON.stringify(targetAndMissing, null, 4), function (err) {
      if (err) throw err;
      console.log('Target file updated.');
    });
  }); 
});
