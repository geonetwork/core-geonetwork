//=====================================================================================
//===
//=== Thesaurus support functions
//===
//=== Needs : prototype.js
//===
//=====================================================================================


//=====================================================================================
/*	Creates an XML request in order to add the thesaurus
	like <request>...</request>.
 */

function addThesaurus(idThesaRow, idThesaName){
	var thesaName = $F(idThesaName);
	var type = 'local';
	var dname = idThesaRow;
	var fname = thesaName;
				
	var request= '<request><fname>'+fname+'</fname><dname>'+dname+'</dname><type>'+type+'</type></request>';
				
	var opt = 
	{
		method: 'post',
		postBody: request,
		requestHeaders: ['Content-type', 'application/xml'],

		onSuccess: function(t) 
		{
			var xmlobject = t.responseXML;
			if (xmlobject.getElementsByTagName("error").length > 0){
						alert('Error : ' + xmlobject.getElementsByTagName("message")[0].firstChild.nodeValue);								
						return;
					} 
			var ref=xmlobject.getElementsByTagName("ref")[0].firstChild.nodeValue;
			var tname=xmlobject.getElementsByTagName("thesaName")[0].firstChild.nodeValue;
			
			var tbl = document.getElementById('myTable');
			for(i = 1; i < tbl.rows.length; i++){
				if (tbl.rows[i].id==idThesaRow){
					break;
				}
			}			
			var index = i;
			// Delete edit row
			tbl.deleteRow(index);
			// Add row
			var row = tbl.insertRow(index);
			// WARNING : this has to be consistent with XSLT thesaurusadmin.xsl
			// cell 0
			var cell0 = row.insertCell(0);
			var style = 'padded-content';
			cell0.className = style;
			var textNode = document.createTextNode(' ');
			cell0.appendChild(textNode);
			// cell 1
			var cell1 = row.insertCell(1);
			cell1.className = style;
			var textNode = document.createTextNode('local');
			cell1.appendChild(textNode);
			// cell 2
			var cell2 = row.insertCell(2);
			cell2.className = style;						  
			var textNode = document.createTextNode(tname);
			cell2.appendChild(textNode);
			// cell 3
			var cell3 = row.insertCell(3);
			cell3.className = style;						  
			
			var btDownload = document.createElement('button');		  
			btDownload.setAttribute("type", "button");  
			btDownload.className = 'content';
			btDownload.innerHTML = download;
			btDownload.onclick = function () { load(locService +'/thesaurus.download?ref=' + ref); };
			cell3.appendChild(btDownload);
			textNode = document.createTextNode(' ');
			cell3.appendChild(textNode);						
			var btDelete = document.createElement('button');		  
			btDelete.setAttribute("type", "button");  
			btDelete.className = 'content';
			btDelete.innerHTML = remove;
			btDelete.onclick = function () { load(locService +'/thesaurus.delete?ref=' + ref); };
			cell3.appendChild(btDelete);						
			textNode = document.createTextNode(' ');
			cell3.appendChild(textNode);						
			var btEdit = document.createElement('button');		
			btEdit.setAttribute("type", "button");  
			//btEdit.type='button';	
			btEdit.className = 'content';
			btEdit.innerHTML = edit;
			btEdit.onclick = function () { load(locService +'/thesaurus.edit?selected='+ref+'&mode=edit'); }
			cell3.appendChild(btEdit);		
			$('_div'+idThesaRow).style.display='inline';				  					
					},
		on404: function(t) 
		{
			alert('Error 404: location "' + t.statusText + '" was not found.');
		},
		onFailure: function(t) 
		{
			alert('Error ' + t.status + ' -- ' + t.statusText);
		}
	}			
	new Ajax.Request(locService +'/thesaurus.add',opt);
}


//=====================================================================================
/*	Add a row to the thesaurus table
*/

							
function addRowToTable(idrow)
{
  var tbl = document.getElementById('myTable');
  var idRowBefore='_tr'+idrow;
  var idRowInserted = idrow;	
  for(i = 1; i < tbl.rows.length; i++){
	if (tbl.rows[i].id==idRowBefore){
	break;
	}
  }
		  
  lastRow=i + 1;		
  var iteration = lastRow;
  var row = tbl.insertRow(lastRow);	
  row.setAttribute("id", idRowInserted);
  var style = 'padded-content';
			
  // cell 0
  var cell0 = row.insertCell(0);
  cell0.className = style;
  var textNode = document.createTextNode(' ');
  cell0.appendChild(textNode);

  // cell 1
  var cell1 = row.insertCell(1);
  cell1.className = style;
  var textNode = document.createTextNode('local');
  cell1.appendChild(textNode);
  
  // cell 2
  var cell2 = row.insertCell(2);
  cell2.className = style;
  var el = document.createElement('input');
  el.type = 'text';
  var idThesaName = 'thesaName' + iteration;
  el.name = idThesaName;
  el.id = idThesaName;
  el.size = 40;		  
  cell2.appendChild(el);
  
  // cell 3	
  var cell3 = row.insertCell(3);
  cell3.className = 'padded-content';
  var but = document.createElement('button');
  but.name = 'btCreate';
  but.className = 'content';
  but.innerHTML = create;
  but.onclick = function () { addThesaurus(idRowInserted, idThesaName); return false; }
  cell3.appendChild(but);
  
  $(idThesaName).focus();
}
					
//=====================================================================================
/*	Add the row for thesaurus name
*/

function addThesaurusEditRow(idstring){			
	addRowToTable(idstring);
	$('_div'+idstring).style.display='none';
}			
		
