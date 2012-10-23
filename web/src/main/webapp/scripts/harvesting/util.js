//=====================================================================================
//===
//=== Utility methods
//===
//=====================================================================================

var hvutil = new Object();

//=====================================================================================

//return the value of the radio button that is checked
//return a null if none are checked

function getCheckedValue(radioGroup) {
	var checked = $$("input[type=radio][name="+radioGroup+"]").find(
	  function(re) {return re.checked;}
	);
	return (checked) ? $F(checked) : null;
}


//set the radio button with the given value as being checked
//if the given value does not exist, all the radio buttons
//are reset to unchecked
function setCheckedValue(radioGroup, newValue) {
	var radioButtons  = $$('input[type=radio][name='+radioGroup+']');
	radioButtons.each(function(radioButton) {
		radioButton.checked = false;
		if(radioButton.value == newValue.toString()) {
			radioButton.checked = true;
		}
	});
}

hvutil.setRadioOption = function(node, name, radioGroup)
{
	var value = hvutil.find(node, name);

	if (value == null)
		throw 'Cannot find node with name : '+ name;
	
	setCheckedValue(radioGroup, value);
}


hvutil.setOption = function(node, name, ctrlId)
{
	var value = hvutil.find(node, name);
	var ctrl  = $(ctrlId);
	var type  = ctrl.getAttribute('type');
	
	if (value == null)
		throw 'Cannot find node with name : '+ name;
	
	if (!ctrl)
		throw 'Cannot find control with id : '+ ctrlId;
		
	if (type == 'checkbox')	{
		ctrl.checked = (value == 'true');
	} else						ctrl.value   = value;
}

hvutil.setOptionIfExists = function(node, name, ctrlId)
{
	var value = hvutil.find(node, name);
	var ctrl  = $(ctrlId);
	if (!ctrl) return;	
	var type  = ctrl.getAttribute('type');
	
	if (value == null)
		return;
		
	if (type == 'checkbox')	ctrl.checked = (value == 'true');		
	else	ctrl.value   = value;
}
//=====================================================================================

hvutil.find = function(node, name)
{
	var array = [ node ];
	
	while (array.length != 0)
	{
		node = array.shift();
		
		if (node.nodeName == name)
			return xml.textContent(node);
			
		node = node.firstChild;
		
		while (node != null)
		{
			if (node.nodeType == Node.ELEMENT_NODE)
				array.push(node);
			
			node = node.nextSibling;
		}
	}
	
	return null;
}

//=====================================================================================
//===
//=== Every
//===
//=====================================================================================

function Every(every)
{
	if (typeof every == 'string')
		every = parseInt(every);
	
	this.mins = every % 60;
	
	every -= this.mins;	
	this.hours = every / 60 % 24;	
	this.days  = (every - this.hours * 60) / 1440;	
}

//=====================================================================================

Every.build = function(days, hours, mins)
{
	if (typeof days == 'string')
		days = parseInt(days);
	
	if (typeof hours == 'string')
		hours = parseInt(hours);
	
	if (typeof mins == 'string')
		mins = parseInt(mins);
		
	return days*1440 + hours*60 + mins;
}

//=====================================================================================

//=====================================================================================
//===
//=== Cron
//===
//=====================================================================================

function Cron(cron)
{
	this.days = {
			mon: true,
			tue: true,
			wed: true,
			thu: true,
			fri: true,
			sat: true,
			sun: true
    	};

	if (cron == undefined) {
		this.defaults();
    } else {
        var tokens = cron.split (" ");
        if(tokens.length == 1) {
        	this.parsePeriod(cron);
        } else {
    		this.parseCron(tokens);
        }
    }
}
//=====================================================================================
Cron.prototype.defaults = function () {
	this.mins = '0';
	this.hours = '0';
	this.intervalHours = undefined;
};
Cron.prototype.parsePeriod = function (string) {
	var period = parseInt(string);
	this.defaults();
	if(isNaN(period)) {
		// do nothing, defaults are fine
	} else if(period < (60 * 2)){
		this.intervalHours = '1';
	} else if(period < (60 * 4.5) ){
		this.intervalHours = '3';
	} else if(period < (60 * 9) ){
		this.intervalHours = '6';
	} else if(period < (60 * 18) ){
		this.intervalHours = '12';
	} else if(period <= (60 * 24) ){
		this.intervalHours = undefined;
	} else if (Math.round(period / (60 * 24)) > 5) {
		this.eachDay(function(days,day){
			days[day] = false;
		})
		this.days.mon = true;
	} else {
		var days = this.days;
		switch (Math.round(period / (60 * 24))) {
			case 1 : 
				// default value is good
				break;
			case 2:
				['tue','thu','sat'].each(function (d) {
					days[d] = false;
				});
				break;
			case 3:
				['tue','wed','fri','sat'].each(function (d) {
					days[d] = false;
				});
				break;
			case 4:
				['tue','wed','thu','sat','sun'].each(function (d) {
					days[d] = false;
				});
				break;
			case 5:
				['tue','wed','thu','fri','sun'].each(function (d) {
					days[d] = false;
				});
				break;
			default:
				throw new Exception(Math.round(period / (60 * 24))+" should not have made this else statement");
		}
	}
};
Cron.prototype.parseCron = function (tokens) {
    var tokensHour = tokens[2].split("/");
    
    this.mins = tokens[1];
    this.hours = tokensHour[0];
    this.intervalHours = tokensHour[1];
    if(tokens[5] != '*' && tokens[5] != '?') {
    	this.eachDay(function(days, day){days[day] = false});
	    var tokensDay = tokens[5].split(",");
	    var days = this.days;
	    tokensDay.each(function(day){
	    	days[day.toLowerCase()] = true;
	    })
    }
};
//=====================================================================================
/** 
 * Iterate through each day passing (daysObject, day, this).
 * 
 * It will process the days in order.
 */	
Cron.prototype.eachDay = function (func) {
	func(this.days, "mon",this);
	func(this.days, "tue",this);
	func(this.days, "wed",this);
	func(this.days, "thu",this);
	func(this.days, "fri",this);
	func(this.days, "sat",this);
	func(this.days, "sun",this);
}
//=====================================================================================

/**
 * Read ui widgets for values of this cron job and return this again
 */
Cron.prototype.readUI = function (prefix) {
    this.hours = $(prefix+'.atHour') .value;
    this.mins = $(prefix+'.atMin').value;
    this.intervalHours = $(prefix+'.atIntervalHours').value;

	this.eachDay(function(days, day){ 
		days[day] = $(prefix+'.'+day.toUpperCase()).checked;
    });
    
    return this;
}

//=====================================================================================

/**
 * Create a string to summarize this job for harvester UI.
 * 
 * This is the execution time string
 */
Cron.prototype.nodeAtSummary = function() {
    var min = ''+this.mins;
    if(min.length == 1) {
        min = '0'+min;
    }
    
    var hour = ''+this.hours
    if(hour.length == 1) {
        hour = '0'+hour;
    }
	return (hour+":"+min).replace('*', '0');
}

/**
 * Create a string to summarize this job for harvester UI
 * 
 * This is the repeat interval string
 * 
 * @param dayTranslation translation MON,TUE,WED,THU,FRI,SAT,SUN to current locale
 */
Cron.prototype.nodeIntervalSummary = function(dayTranslation) {
    if(!dayTranslation) {
        dayTranslation  = {
                MON: 'MON',
                TUE: 'TUE',
                WED: 'WED',
                THU: 'THU',
                FRI: 'FRI',
                SAT: 'SAT',
                SUN: 'SUN'
            };
    }
    var result;
	if (this.intervalHours) { 
	    result = this.intervalHours + ' h';
	} else if(this.hours === '*') {
	    result = '1 h';
	} 
    var days = this.dayString(dayTranslation);
    if(days === '?') {
        days = dayTranslation.MON+'-'+dayTranslation.SUN;
    } 
    
    if(!result) {
        return days;
    } else {
        return result + ' ('+days+')';
    }

}

//=====================================================================================

Cron.prototype.setUI = function(prefix) {
  $(prefix+'.atHour') .value = this.hours;
	$(prefix+'.atMin').value = this.mins;
	$(prefix+'.atIntervalHours').value = this.intervalHours;
	var cron = this;
	$$("input.filesystem_day").each(function (elem, idx) {
		var day = elem.getAttribute('id').split('.')[1].toLowerCase();
		if(cron.days[day]) {
			elem.checked = true;
		} else {
			elem.checked = false;
		}
	});
	
	return this;
}

//=====================================================================================
/**
 * convert days to a string. If dayTranslation is supplied use that for translations.  
 * 
 * dayTranslation is a map of upperCase 3 letter day abbreviations to string.
 */
Cron.prototype.dayString = function(dayTranslation) {
	var dayArray = [];
	var allTrue = true;
	this.eachDay(function(days,day) {
		if(days[day]) {
		    var dayS = day.toUpperCase();
		    if(dayTranslation && dayTranslation[dayS]) {
		        dayS = dayTranslation[dayS];
		    }
		    dayArray.push(dayS);
		} else {
		    allTrue=false;
		}
	});
	return allTrue ? '*' : dayArray.join(',');
}

//=====================================================================================

Cron.prototype.asString = function() {
	var hourString = (this.intervalHours != "undefined")? this.hours+'/'+this.intervalHours : this.hours;
	
	return ['0',this.mins, hourString, '?', '*', this.dayString()].join(' '); 
}
