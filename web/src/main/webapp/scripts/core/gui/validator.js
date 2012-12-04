//=====================================================================================
//===
//=== Validates the content of text fields against a set of rules
//===
//=== Needs:	xml.js
//===				string.js
//===				kernel.js
//=====================================================================================
/* The 'add' accepts an array of rules. Each rule is a map like this:
		
	{ id:'...', type:'length', minSize: ..., maxSize: ...} 
	{ id:'...', type:'alphanum'} 
	{ id:'...', type:'integer', minValue: ..., maxValue: ..., empty:true} 
	{ id:'...', type:'oneof', items: ['item2', 'item3']} 

	Supports the following validators:
	 - length  : the field must be a string which length must be [minSize..maxSize].
	             Both minSize and maxSize can be omitted.
	 - alphanum: the string must start with a letter and must be formed by letters or digits.
	 - integer : the field must be an integer in the range [minValue..maxValue].	 
	             Both minValue and maxValue can be omitted. 'empty' means that the field
	             can be empty.
	 - hostname: the string can contains only letters, digits and '.','-'
	 - url     : the string must conform to the URL specification (i.e. must be a valid URL).
	             *** Notice that this validator is incomplete. ***
	 -	ipaddress:the string must be in the xxx.xxx.xxx.xxx form where each xxx must be an
	             integer in the range 0-255. 'empty' means that the field can be empty.
	 - oneof : either the control element or one of the other items must be filled
	             
	This method expects to find some strings into the xmlLoader object.
	This is a suggested layout for the string properties. Notice that this is not required:
	the element structure can be flat, without the 'validator' element.
	
	<validator>
		<string>
			<cannotBeEmpty>{NAME} cannot be mpty</cannotBeEmpty>
			<invalidMinSize>{NAME} must be at least {VALUE} character(s) long</invalidMinSize>
			<invalidMaxSize>{NAME} must be maximum {VALUE} character(s) long</invalidMaxSize>
			<notAlphaNumeric>{NAME} must be alphanumeric</notAlphaNumeric>
		</string>
		
		<integer>
			<notInt>{NAME} must be an integer</notInt>
			<invalidMinValue>{NAME} must be &gt;= {VALUE}</invalidMinValue>
			<invalidMaxValue>{NAME} must be &lt;= {VALUE}</invalidMaxValue>
		</integer>
		
		<hostname>
			<notHostName>{NAME} is not a valid host name</notHostName>
		</hostname>
		
		<url>
			<notURL>{NAME} is not a valid URL</notURL>
		</url>
		
		<ipaddress>
			<notIP>{NAME} is not a valid IP address</notIP>
		</ipaddress>
	</validator>

	An optional precond parameter is also supported on rules allowing preconditions to be  
  specified for applying the rule.  For example, adding
  
    precond: [{id:'input1', value:'value1'}, {id:'input2', value:'value2'}]
    
  to a rule will result in the associated rule being tested only if the input with 
  id='input1' has the value 'value1' and the input with id='input2' has the value 'value2'. 
  
  precond also supports testing the checked attribute.

 */

function Validator(xmlLoader)
{
	this.xmlLoader = xmlLoader;
	this.rules = [];	
}

//=====================================================================================
//===
//=== API methods
//===
//=====================================================================================

Validator.prototype.add = function(rules, parentId)
{
	for (var i=0; i<rules.length; i++)
	{
		var rule = rules[i];
		
		if (!parentId)
			rule.ctrl = $(rule.id);
		else
		{
			rule.parentId = parentId;
			rule.ctrl = xml.getElementById($(parentId), rule.id);
		}
		
		if (rule.type == 'length')
			rule.validator = ker.wrap(this, this.lengthVal);
			
		else if (rule.type == 'alphanum')
			rule.validator = ker.wrap(this, this.alphanumVal);
		
		else if (rule.type == 'integer')
			rule.validator = ker.wrap(this, this.integerVal);
			
		else if (rule.type == 'hostname')
			rule.validator = ker.wrap(this, this.hostnameVal);
		
		else if (rule.type == 'url')
			rule.validator = ker.wrap(this, this.urlVal);
		
		else if (rule.type == 'ipaddress')
			rule.validator = ker.wrap(this, this.ipaddressVal);
		
		else if (rule.type == 'oneof')
			rule.validator = ker.wrap(this, this.oneofVal);
		
		else
			throw 'Unknown validator type : '+ rule.type;
			
		this.rules.push(rule);
	}
}

//=====================================================================================

Validator.prototype.removeByParent = function(parentId)
{
	var rules = this.rules;
	this.rules = [];
	
	for(var i=0; i<rules.length; i++)
		if ((!rules[i].parentId) || (rules[i].parentId != parentId && parentId))
			this.rules.push(rules[i]);
}

//=====================================================================================

Validator.prototype.validate = function()
{
	for (var i=0; i<this.rules.length; i++)
	{
		var rule   = this.rules[i];		

		if (!this.checkPrecond(rule))
			continue;

		var result = rule.validator(rule);
		
		if (result != null)
		{
			this.showError(rule.ctrl, result);
			return false;
		}
	}
	
	return true;
}

//=====================================================================================
//===
//=== Private validators methods
//===
//=====================================================================================

Validator.prototype.lengthVal = function(rule)
{
	var text  = rule.ctrl.value;
	var len   = text.length;
	var result= null;
	
	if (rule.minSize && len == 0 && rule.minSize != 0)
		result = ['cannotBeEmpty'];
	
	else if (rule.minSize && len < rule.minSize)
		result = ['invalidMinSize', rule.minSize];
		
	else if (rule.maxSize && len > rule.maxSize)
		result = ['invalidMaxSize', rule.maxSize];
		
	return result;
}

//=====================================================================================

Validator.prototype.checkPrecond = function(rule)
{
	if (!rule.precond) return true;
	
	for (var j=0; j<rule.precond.length; j++) {
		var precond = rule.precond[j];

		if ('value' in precond && $(precond.id).value != precond.value) {
			return false;
		}
		
		if ('checked' in precond && $(precond.id).checked != precond.checked) {
			return false;
		}
	}
	
	return true;
}

//=====================================================================================

Validator.prototype.alphanumVal = function(rule)
{
	var text  = rule.ctrl.value;
	var result= null;
	
	if (!this.isAlphaNumeric(text))
		result = ['notAlphaNumeric'];
		
	return result;
}

//=====================================================================================

Validator.prototype.hostnameVal = function(rule)
{
	var text  = rule.ctrl.value;
	var result= null;
	
	if (!this.isHostName(text))
		result = ['notHostName'];
		
	return result;
}

//=====================================================================================

Validator.prototype.urlVal = function(rule)
{
	var text  = rule.ctrl.value;
	var result= null;
	
	if (!this.isURL(text))
		result = ['notURL'];
		
	return result;
}

//=====================================================================================

Validator.prototype.integerVal = function(rule)
{
	var text  = rule.ctrl.value;
	var len   = text.length;
	var value = parseInt(text);
	var result= null;
	
	if (text == '' && rule.empty)
		return null;
		
	if (!this.isInteger(text))
		result = ['notInt'];
	
	else if (rule.minValue && value < rule.minValue)
		result = ['invalidMinValue', rule.minValue];
		
	else if (rule.maxValue && value > rule.maxValue)
		result = ['invalidMaxValue', rule.maxValue];
	
	return result;
}

//=====================================================================================

Validator.prototype.ipaddressVal = function(rule)
{
	var text  = rule.ctrl.value;
	var result= null;
	
	if (text == '' && rule.empty)
		return null;
	
	if (!this.isIPAddress(text))
		result = ['notIP'];
		
	return result;
}

//=====================================================================================
//=== Private methods
//=====================================================================================

Validator.prototype.showError = function(ctrl, result)
{
	var msg   = this.xmlLoader.getText(result[0]);
	var name  = ctrl.getAttribute('id');
	var value = (result.length == 1) ? '' : ''+ result[1];	
	var pos   = name.lastIndexOf('.');
	
	if (pos != -1)
		name = name.substring(pos +1);
		
	name = this.xmlLoader.getText(name).toLowerCase();
	
	msg = str.replace(msg, '{NAME}',  name);
	msg = str.replace(msg, '{VALUE}', value);
	
	alert(msg);
	ctrl.focus();
//	ctrl.select();
}

//=====================================================================================
//=== Functions that implements the validation rules
//=====================================================================================

Validator.prototype.isInteger = function(text)
{
	for (var i=0; i<text.length; i++)
	{
		var c = text.charAt(i);
		
		if (this.isDigit(c))
			continue;
			
		if (c == '-' && i == 0)
			continue;
			
		return false;
	}
	
	return (text.length != 0);
}

//=====================================================================================

Validator.prototype.isAlphaNumeric = function(text)
{
	for (var i=0; i<text.length; i++)
	{
		var c = text.charAt(i);
		
		if (this.isLetter(c))
			continue;
			
		if (i != 0 && this.isDigit(c))
			continue;
			
		return false;
	}
	
	return true;
}

//=====================================================================================

Validator.prototype.isHostName = function(text)
{
	for (var i=0; i<text.length; i++)
	{
		var c = text.charAt(i);
		
		if (this.isLetter(c) || this.isDigit(c))
			continue;
			
		if (c == '.' || c == '-')
			continue;
			
		return false;
	}
	
	return true;
}

//=====================================================================================

Validator.prototype.isURL = function(text)
{
	var http  = (text.indexOf('http://' ) == 0);
	var https = (text.indexOf('https://') == 0);
	var ftp   = (text.indexOf('ftp://'  ) == 0);
	var ftps  = (text.indexOf('ftps://' ) == 0);
	
	if (!(http || https || ftp || ftps))
		return false;
		
	for (var i=0; i<text.length; i++)
	{
		var c = text.charAt(i);
		
		if (this.isLetter(c) || this.isDigit(c))
			continue;
			
		if ('.-:/_%?&=$#[]@()*+,;'.indexOf(c) != -1)
			continue;
			
		return false;
	}
	
	return true;
}

//=====================================================================================

Validator.prototype.isIPAddress = function(text)
{
	if (text.length < 7 || text.length > 15)
		return false;
		
	var blocks = text.split('.');
	
	if (blocks.length != 4)
		return false;
		
	for (var i=0; i<blocks.length; i++)
	{
		if (!this.isInteger(blocks[i]))
			return false;
			
		var value = parseInt(blocks[i]);
		
		if (value <0 || value >255)
			return false;
	}
	
	return true;
}

//=====================================================================================

Validator.prototype.isLetter = function(c)
{
	return ( ((c >= "a") && (c <= "z")) || ((c >= "A") && (c <= "Z")) ); 
}

//=====================================================================================

Validator.prototype.isDigit = function(c)
{
	return ((c >= "0") && (c <= "9"));
}

//=====================================================================================

Validator.prototype.isLetterOrDigit = function(c)
{
	return (isLetter(c) || isDigit(c)); 
}

//=====================================================================================

//=====================================================================================

Validator.prototype.oneofVal = function(rule)
{
	var found = rule.ctrl.value && rule.ctrl.value.replace(/^\s+|\s+$/g,'').length !== 0;
	var i, item;
	for (i = 0; !found && i < rule.items.length; i++) {
		item = $(rule.items[i]);
		found = found || item.value && item.value.replace(/^\s+|\s+$/g,'').length !== 0;
	}
	
	var result= null;
	
	if (!found) {
		result = ['oneof'];
	}

	return result;
}
