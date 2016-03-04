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
