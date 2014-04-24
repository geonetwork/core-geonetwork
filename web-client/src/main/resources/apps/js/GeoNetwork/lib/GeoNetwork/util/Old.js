/*
 * Copyright (C) 2001-2011 Food and Agriculture Organization of the
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
var translations = {};

// TODO : move elsewhere
function translate(text){
    return translations[text] || text;
}


/**
 * This file contains GeoNetwork JS functions used in loaded rendered HTML pages
 * like massive operations for example.
 *
 * Objectives : remove all this code
 */
/**
 * Update value of all input checkbox elements found in container
 * element identified by id.
 */
function checkAllInputsIn(id, checked){
    var list = Ext.getDom(id).getElementsByTagName('input'), i;
    
    for (i = 0; i < list.length; i++) {
        list[i].checked = checked;
    }
}

function setAll(id){
    checkAllInputsIn(id, true);
}

function clearAll(id){
    checkAllInputsIn(id, false);
}

/** 
 *  Modal box with checkbox validation
 *
 */
function checkBoxModalUpdate(div, service, modalbox, title){
    var boxes = Ext.DomQuery.select('input[type="checkbox"]');
    var pars = "?";
    if (service === 'metadata.admin' || service === 'metadata.category') {
        pars += "id=" + Ext.getDom('metadataid').value;
    }
    Ext.each(boxes, function(s){
        if (s.checked && s.name != "") {
            pars += "&" + s.name + "=on";
        }
    });
    
    // FIXME : title is not an error message title
    catalogue.doAction(service + pars, null, null, title, function(response){
        Ext.getDom(div).innerHTML = response.responseText;
        if (service === 'metadata.admin' || service === 'metadata.category') {
            Ext.getCmp('modalWindow').close();
        }
    }, null);
}

function radioModalUpdate(div, service, modalbox, title) {
    var pars = '?';
    var inputs = Ext.DomQuery.select('input[type="hidden"],textarea,select', div);
    Ext.each(inputs, function(s) {
        pars += "&" + s.name + "=" + s.value;
    });
    var radios = Ext.DomQuery.select('input[type="radio"]', div);
    Ext.each(radios, function(s){
        if (s.checked) {
            pars += "&" + s.name + "=" + s.value;
        }
    });
    
    catalogue.doAction(service + pars, null, null, title, function(response){
        Ext.getDom(div).innerHTML = response.responseText;
        if (service === 'metadata.status') {
            Ext.getCmp('modalWindow').close();
            catalogue.onAfterStatus();
        }
    }, null);
}



function addGroups(xmlRes){
    var list = xmlRes.getElementsByTagName('group'), i;
    Ext.getDom('group').options.length = 0;
    for (i = 0; i < list.length; i++) {
        var id = list[i].getElementsByTagName('id')[0].firstChild.nodeValue;
        var name = list[i].getElementsByTagName('name')[0].firstChild.nodeValue;
        var opt = document.createElement('option');
        opt.text = name;
        opt.value = id;
        if (list.length === 1) {
            opt.selected = true;
        }
        Ext.getDom('group').options.add(opt);
    }
}

/** Update owner modal box
 *
 */
function doGroups(userid){
    catalogue.doAction('xml.usergroups.list?id=' + userid, null, null, "Error retrieving groups", function(xmlRes){
        if (xmlRes.nodeName === 'error') {
            //ker.showError(translate('cannotRetrieveGroup'), xmlRes);
            Ext.getDom('group').options.length = 0; // clear out the options
            Ext.getDom('group').value = '';
            var user = Ext.getDom('user');
            for (i = 0; i < user.options.length; i++) {
                user.options[i].selected = false;
            }
        } else {
            addGroups(xmlRes.responseXML);
        }
    });
}

/** Massive new owner
 *
 */
function checkMassiveNewOwner(action, title){
    var user = Ext.getDom('user').value;
    var group = Ext.getDom('group').value;
    if (user === '') {
        Ext.Msg.alert(title, "selectNewOwner");
        return false;
    }
    if (group.value === '') {
        Ext.Msg.alert(title, "selectOwnerGroup");
        return false;
    }
    catalogue.doAction(action + '?user=' + user + '&group=' + group, null, null, null, function(response){
        Ext.getDom('massivenewowner').parentNode.innerHTML = response.responseText;
    });
}

/** Prepare download
 *
 */
function doDownload(id, all){
    var list = Ext.getDom('downloadlist').getElementsByTagName('INPUT'), pars = '&id=' + id + '&access=private', selected = false;
    
    for (var i = 0; i < list.length; i++) {
        if (list[i].checked || all !== null) {
            selected = true;
            var name = list[i].getAttribute('name');
            pars += '&fname=' + name;
        }
    }
    
    if (!selected) {
        Ext.Msg.alert('Alert', OpenLayers.i18n('selectOneFile'));
        return;
    }
    
    catalogue.doAction(catalogue.services.fileDisclaimer + "?" + pars, null, null, null, function(response){
        Ext.getDom('downloadlist').parentNode.innerHTML = response.responseText;
    });
}

function feedbackSubmit(){
    var f = Ext.getDom('feedbackf');
    // TODO : restore form control.
    //    if (isWhitespace(f.comments.value)) {
    //        f.comments.value = OpenLayers.i18n('noComment');
    //    }
    //    
    //    if (isWhitespace(f.name.value) || isWhitespace(f.org.value)) {
    //        alert(OpenLayers.i18n("addName"));
    //        return;
    //    } else if (!isEmail(f.email.value)) {
    //        alert(OpenLayers.i18n("checkEmail"));
    //        return;
    //    }
    catalogue.doAction(catalogue.services.fileDownload + "?" + Ext.Ajax.serializeForm(f), null, null, null, function(response){
        Ext.getDom('feedbackf').parentNode.innerHTML = response.responseText;
    });
}

function goSubmit(form_name){
    document.forms[form_name].submit();
}


function checkBatchNewOwner(action, title) {
    if (Ext.getDom('user').value == '') {
        Ext.Msg.alert(title, "selectNewOwner");
        return false;
    }
    if (Ext.getDom('group').value == '') {
        Ext.Msg.alert(title, "selectOwnerGroup");
        return false;
    }
    catalogue.doAction(catalogue.services.metadataMassiveNewOwner + "?" + Ext.Ajax.serializeForm(Ext.getDom('batchnewowner')), null, null, null, function(response){
        Ext.getDom('batchnewowner').parentNode.innerHTML = response.responseText;
    });
}


/**
* Build duration format for gts:TM_PeriodDuration onkeyup or onchange
* events of duration widget define in metadata-iso19139.xsl.
*
* This only apply to iso19139 (or iso profil) metadata.
*
* Duration format is: PnYnMnDTnHnMnS and could be negative.
*
* Parameters:
* ref - {String} Identifier of a form element (ie. geonet:element/@ref)
*/
function buildDuration(ref) {
    if ($('Y' + ref).value == '')
    $('Y' + ref).value = 0;
    if ($('M' + ref).value == '')
    $('M' + ref).value = 0;
    if ($('D' + ref).value == '')
    $('D' + ref).value = 0;
    if ($('H' + ref).value == '')
    $('H' + ref).value = 0;
    if ($('MI' + ref).value == '')
    $('MI' + ref).value = 0;
    if ($('S' + ref).value == '')
    $('S' + ref).value = 0;
    
    $('_' + ref).value =
    ($('N' + ref).checked? "-": "") +
    "P" +
    $('Y' + ref).value + "Y" +
    $('M' + ref).value + "M" +
    $('D' + ref).value + "DT" +
    $('H' + ref).value + "H" +
    $('MI' + ref).value + "M" +
    $('S' + ref).value + "S";
}


/**********************************************************************
 * Massive metadata replacemnets
 **********************************************************************/

function massiveMetadataReplace_test(action) {
  massiveMetadataReplace_action(action, true);
}

function massiveMetadataReplace_execute(action) {
  massiveMetadataReplace_action(action, false);
}

function massiveMetadataReplace_action(action, test) {

  // Validate that has been defined at least 1 replacement
  var massiveUpdatesTable = $('massivereplace-updates');
  if (massiveUpdatesTable.rows.length == 1) {
    Ext.Msg.show({
      title: OpenLayers.i18n('massivereplace-title'),
      msg: OpenLayers.i18n('massivereplace-noreplacements'),
      buttons: Ext.Msg.OK
    });

    return false;
  }

  // Set form to non test mode
  $('test').value = test;

  // Clean results report
  $('massivereplace-results-container').innerHTML = "";

  Ext.Msg.show({
    title: OpenLayers.i18n('massivereplace-title'),
    msg: (test?OpenLayers.i18n('massivereplace-test'):OpenLayers.i18n('massivereplace-execute')),
    buttons: Ext.Msg.YESNO,
    fn: function (id, value) {
      if (id === 'yes') {
        Ext.get("MB_loading").show();

        OpenLayers.Request.GET({
          url: catalogue.services.rootUrl + action + "?" + Ext.Ajax.serializeForm(Ext.getDom('massiveupdateform')),
          success: function(response){
            Ext.get('MB_loading').hide();
            Ext.get('massivereplace-results-container').show();
            $('massivereplace-results-container').innerHTML = response.responseText;
          },
          failure: function(response){
            Ext.get('MB_loading').hide();
            Ext.Msg.alert('', response.responseText);
          }
        });
      }
    }
  });
}


function massiveMetadataReplace_setDropDown(field, data, method, index) {
  field.options.length = index == null ? 1 : index;

  Ext.each(data, function(e) {
      field.options.add(method(e));
  });

}

function massiveMetadataReplace_updateFields(section) {
  var mdfieldsSelectBox = $("mdfield");

  var sectionValues;

  if (section == "metadata"){
    sectionValues = sectionMetadataFields;
  } else if (section == "dataIdentification"){
    sectionValues = sectionDataIdentificationFields;
  } else if (section == "serviceIdentification"){
    sectionValues = sectionServiceIdentificationFields;
  } else if (section == "maintenanceInformation"){
    sectionValues = sectionMaintenanceInformationFields;
  } else if(section == "contentInformation"){
    sectionValues = sectionContentInformationFields;
  } else if(section == "distributionInformation"){
    sectionValues = sectionDistributionInformationFields;
  }

  // Empty mdfields list
  massiveMetadataReplace_setDropDown(mdfieldsSelectBox ,[]);


  // Fill mdfields list depending on section selection
  massiveMetadataReplace_setDropDown(
    mdfieldsSelectBox,
    sectionValues.FIELDS,
    function(e) {
      var opt =  new Option( e.NAME, e.KEY );
      if (e.KEY == '---') opt.disabled = 'disabled';
      return opt;
    },
    null
  );
}

function massiveMetadataReplace_addRow() {

  if (($('mdsection').value === '') ||
      ($('mdfield').value === '') ||
      ($('searchValue').value === '') ||
      ($('replaceValue').value === '')) {

    Ext.Msg.show({
      title: OpenLayers.i18n('massivereplace-add-title'),
      msg: OpenLayers.i18n('massivereplace-add-msg'),
      buttons: Ext.Msg.OK
    });

    return;
  }

  var massiveUpdatesTable = $('massivereplace-updates');

  // Show replacements table
  Ext.get('massivereplace-updates').show();
  Ext.get('noReplacements').setVisibilityMode(Ext.Element.DISPLAY);
  Ext.get('noReplacements').hide();

  // Clean results report
  $('massivereplace-results-container').innerHTML = "";

  var newDate = new Date;
  var tmpID = newDate.getTime();

  // insert at second row: first row has column headers
  var newRow = massiveUpdatesTable.insertRow(massiveUpdatesTable.rows.length);


  newRow.setAttribute("id", tmpID+"-row");
  var nameCell = newRow.insertCell(0);
  var el = $('mdsection');
  var text = el.options[el.selectedIndex].innerHTML;
  var sel= text + "<input type='hidden' name='mdsection-" + tmpID + "' value='" + el.value + "' />";

  nameCell.innerHTML = sel;

  var elementCell = newRow.insertCell(1);
  var el = $('mdfield');
  var text = el.options[el.selectedIndex].innerHTML;
  sel= text + "<input type='hidden' name='mdfield-" + tmpID + "' value='" + el.value + "' />";
  elementCell.innerHTML=sel;

  var searchCell = newRow.insertCell(2);
  sel= $('searchValue').value + "<input type='hidden' name='searchValue-" + tmpID + "' value='" + $('searchValue').value + "' />";
  searchCell.innerHTML=sel;

  var replaceCell = newRow.insertCell(3);
  sel=$('replaceValue').value + "<input type='hidden' name='replaceValue-" + tmpID + "' value='" + $('replaceValue').value + "' />";
  replaceCell.innerHTML=sel;

  var removeCell = newRow.insertCell(4);
  removeCell.style.padding = "5px 0px 0px 0px";
  var deleteText = "delete";
  removeCell.innerHTML = "<center><img id='imgdel-" + tmpID + "' src='../../images/map/delete_layer.png' alt='"+deleteText+"' title='"+deleteText+"' onclick='massiveMetadataReplace_removeRow("+tmpID+");' onmouseover='this.style.cursor=\"pointer\";'/></center>";

  // Clear values
  $('mdfield').value = '';
  $('searchValue').value = '';
  $('replaceValue').value = '';
}

function massiveMetadataReplace_removeRow(id) {
  var massiveUpdatesTableRow = $(id+"-row");
  massiveUpdatesTableRow.parentNode.removeChild(massiveUpdatesTableRow);

  // Hide replacements table if no replacements defined
  var massiveUpdatesTable = $('massivereplace-updates');
  if (massiveUpdatesTable.rows.length == 1) {
    Ext.get('noReplacements').show();
    Ext.get('massivereplace-updates').setVisibilityMode(Ext.Element.DISPLAY);
    Ext.get('massivereplace-updates').hide();
  }

  // Clean results report
  $('massivereplace-results-container').innerHTML = "";
}