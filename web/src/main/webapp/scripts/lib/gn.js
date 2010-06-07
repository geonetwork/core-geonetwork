var digits="0123456789";var lowercaseLetters="abcdefghijklmnopqrstuvwxyz"
var uppercaseLetters="ABCDEFGHIJKLMNOPQRSTUVWXYZ"
var whitespace=" \t\n\r";var decimalPointDelimiter="."
var phoneNumberDelimiters="()- ";var validUSPhoneChars=digits+phoneNumberDelimiters;var validWorldPhoneChars=digits+phoneNumberDelimiters+"+";var SSNDelimiters="- ";var validSSNChars=digits+SSNDelimiters;var digitsInSocialSecurityNumber=9;var digitsInUSPhoneNumber=10;var ZIPCodeDelimiters="-";var ZIPCodeDelimeter="-"
var validZIPCodeChars=digits+ZIPCodeDelimiters
var digitsInZIPCode1=5
var digitsInZIPCode2=9
var creditCardDelimiters=" "
var mPrefix="You did not enter a value into the "
var mSuffix=" field. This is a required field. Please enter it now."
var sUSLastName="Last Name"
var sUSFirstName="First Name"
var sWorldLastName="Family Name"
var sWorldFirstName="Given Name"
var sTitle="Title"
var sCompanyName="Company Name"
var sUSAddress="Street Address"
var sWorldAddress="Address"
var sCity="City"
var sStateCode="State Code"
var sWorldState="State, Province, or Prefecture"
var sCountry="Country"
var sZIPCode="ZIP Code"
var sWorldPostalCode="Postal Code"
var sPhone="Phone Number"
var sFax="Fax Number"
var sDateOfBirth="Date of Birth"
var sExpirationDate="Expiration Date"
var sEmail="Email"
var sSSN="Social Security Number"
var sCreditCardNumber="Credit Card Number"
var sOtherInfo="Other Information"
var iStateCode="This field must be a valid two character U.S. state abbreviation (like CA for California). Please reenter it now."
var iZIPCode="This field must be a 5 or 9 digit U.S. ZIP Code (like 94043). Please reenter it now."
var iUSPhone="This field must be a 10 digit U.S. phone number (like 415 555 1212). Please reenter it now."
var iWorldPhone="This field must be a valid international phone number. Please reenter it now."
var iSSN="This field must be a 9 digit U.S. social security number (like 123 45 6789). Please reenter it now."
var iEmail="This field must be a valid email address (like foo@bar.com). Please reenter it now."
var iCreditCardPrefix="This is not a valid "
var iCreditCardSuffix=" credit card number. (Click the link on this form to see a list of sample numbers.) Please reenter it now."
var iDay="This field must be a day number between 1 and 31.  Please reenter it now."
var iMonth="This field must be a month number between 1 and 12.  Please reenter it now."
var iYear="This field must be a 2 or 4 digit year number.  Please reenter it now."
var iDatePrefix="The Day, Month, and Year for "
var iDateSuffix=" do not form a valid date.  Please reenter them now."
var pEntryPrompt="Please enter a "
var pStateCode="2 character code (like CA)."
var pZIPCode="5 or 9 digit U.S. ZIP Code (like 94043)."
var pUSPhone="10 digit U.S. phone number (like 415 555 1212)."
var pWorldPhone="international phone number."
var pSSN="9 digit U.S. social security number (like 123 45 6789)."
var pEmail="valid email address (like foo@bar.com)."
var pCreditCard="valid credit card number."
var pDay="day number between 1 and 31."
var pMonth="month number between 1 and 12."
var pYear="2 or 4 digit year number."
var defaultEmptyOK=false
function makeArray(n){for(var i=1;i<=n;i++){this[i]=0}
return this}
var daysInMonth=makeArray(12);daysInMonth[1]=31;daysInMonth[2]=29;daysInMonth[3]=31;daysInMonth[4]=30;daysInMonth[5]=31;daysInMonth[6]=30;daysInMonth[7]=31;daysInMonth[8]=31;daysInMonth[9]=30;daysInMonth[10]=31;daysInMonth[11]=30;daysInMonth[12]=31;var USStateCodeDelimiter="|";var USStateCodes="AL|AK|AS|AZ|AR|CA|CO|CT|DE|DC|FM|FL|GA|GU|HI|ID|IL|IN|IA|KS|KY|LA|ME|MH|MD|MA|MI|MN|MS|MO|MT|NE|NV|NH|NJ|NM|NY|NC|ND|MP|OH|OK|OR|PW|PA|PR|RI|SC|SD|TN|TX|UT|VT|VI|VA|WA|WV|WI|WY|AE|AA|AE|AE|AP"
function isEmpty(s)
{return((s==null)||(s.length==0))}
function isWhitespace(s)
{var i;if(isEmpty(s))return true;for(i=0;i<s.length;i++)
{var c=s.charAt(i);if(whitespace.indexOf(c)==-1)return false;}
return true;}
function stripCharsInBag(s,bag)
{var i;var returnString="";for(i=0;i<s.length;i++)
{var c=s.charAt(i);if(bag.indexOf(c)==-1)returnString+=c;}
return returnString;}
function stripCharsNotInBag(s,bag)
{var i;var returnString="";for(i=0;i<s.length;i++)
{var c=s.charAt(i);if(bag.indexOf(c)!=-1)returnString+=c;}
return returnString;}
function stripWhitespace(s)
{return stripCharsInBag(s,whitespace)}
function charInString(c,s)
{for(i=0;i<s.length;i++)
{if(s.charAt(i)==c)return true;}
return false}
function stripInitialWhitespace(s)
{var i=0;while((i<s.length)&&charInString(s.charAt(i),whitespace))
i++;return s.substring(i,s.length);}
function isLetter(c)
{return(((c>="a")&&(c<="z"))||((c>="A")&&(c<="Z")))}
function isDigit(c)
{return((c>="0")&&(c<="9"))}
function isLetterOrDigit(c)
{return(isLetter(c)||isDigit(c))}
function isInteger(s)
{var i;if(isEmpty(s))
if(isInteger.arguments.length==1)return defaultEmptyOK;else return(isInteger.arguments[1]==true);for(i=0;i<s.length;i++)
{var c=s.charAt(i);if(!isDigit(c))return false;}
return true;}
function isSignedInteger(s)
{if(isEmpty(s))
if(isSignedInteger.arguments.length==1)return defaultEmptyOK;else return(isSignedInteger.arguments[1]==true);else{var startPos=0;var secondArg=defaultEmptyOK;if(isSignedInteger.arguments.length>1)
secondArg=isSignedInteger.arguments[1];if((s.charAt(0)=="-")||(s.charAt(0)=="+"))
startPos=1;return(isInteger(s.substring(startPos,s.length),secondArg))}}
function isPositiveInteger(s)
{var secondArg=defaultEmptyOK;if(isPositiveInteger.arguments.length>1)
secondArg=isPositiveInteger.arguments[1];return(isSignedInteger(s,secondArg)&&((isEmpty(s)&&secondArg)||(parseInt(s)>0)));}
function isNonnegativeInteger(s)
{var secondArg=defaultEmptyOK;if(isNonnegativeInteger.arguments.length>1)
secondArg=isNonnegativeInteger.arguments[1];return(isSignedInteger(s,secondArg)&&((isEmpty(s)&&secondArg)||(parseInt(s)>=0)));}
function isNegativeInteger(s)
{var secondArg=defaultEmptyOK;if(isNegativeInteger.arguments.length>1)
secondArg=isNegativeInteger.arguments[1];return(isSignedInteger(s,secondArg)&&((isEmpty(s)&&secondArg)||(parseInt(s)<0)));}
function isNonpositiveInteger(s)
{var secondArg=defaultEmptyOK;if(isNonpositiveInteger.arguments.length>1)
secondArg=isNonpositiveInteger.arguments[1];return(isSignedInteger(s,secondArg)&&((isEmpty(s)&&secondArg)||(parseInt(s)<=0)));}
function isFloat(s)
{var i;var seenDecimalPoint=false;if(isEmpty(s))
if(isFloat.arguments.length==1)return defaultEmptyOK;else return(isFloat.arguments[1]==true);if(s==decimalPointDelimiter)return false;for(i=0;i<s.length;i++)
{var c=s.charAt(i);if((c==decimalPointDelimiter)&&!seenDecimalPoint)seenDecimalPoint=true;else if(!isDigit(c))return false;}
return true;}
function isSignedFloat(s)
{if(isEmpty(s))
if(isSignedFloat.arguments.length==1)return defaultEmptyOK;else return(isSignedFloat.arguments[1]==true);else{var startPos=0;var secondArg=defaultEmptyOK;if(isSignedFloat.arguments.length>1)
secondArg=isSignedFloat.arguments[1];if((s.charAt(0)=="-")||(s.charAt(0)=="+"))
startPos=1;return(isFloat(s.substring(startPos,s.length),secondArg))}}
function isAlphabetic(s)
{var i;if(isEmpty(s))
if(isAlphabetic.arguments.length==1)return defaultEmptyOK;else return(isAlphabetic.arguments[1]==true);for(i=0;i<s.length;i++)
{var c=s.charAt(i);if(!isLetter(c))
return false;}
return true;}
function isAlphanumeric(s)
{var i;if(isEmpty(s))
if(isAlphanumeric.arguments.length==1)return defaultEmptyOK;else return(isAlphanumeric.arguments[1]==true);for(i=0;i<s.length;i++)
{var c=s.charAt(i);if(!(isLetter(c)||isDigit(c)))
return false;}
return true;}
function reformat(s)
{var arg;var sPos=0;var resultString="";for(var i=1;i<reformat.arguments.length;i++){arg=reformat.arguments[i];if(i%2==1)resultString+=arg;else{resultString+=s.substring(sPos,sPos+arg);sPos+=arg;}}
return resultString;}
function isSSN(s)
{if(isEmpty(s))
if(isSSN.arguments.length==1)return defaultEmptyOK;else return(isSSN.arguments[1]==true);return(isInteger(s)&&s.length==digitsInSocialSecurityNumber)}
function isUSPhoneNumber(s)
{if(isEmpty(s))
if(isUSPhoneNumber.arguments.length==1)return defaultEmptyOK;else return(isUSPhoneNumber.arguments[1]==true);return(isInteger(s)&&s.length==digitsInUSPhoneNumber)}
function isInternationalPhoneNumber(s)
{if(isEmpty(s))
if(isInternationalPhoneNumber.arguments.length==1)return defaultEmptyOK;else return(isInternationalPhoneNumber.arguments[1]==true);return(isPositiveInteger(s))}
function isZIPCode(s)
{if(isEmpty(s))
if(isZIPCode.arguments.length==1)return defaultEmptyOK;else return(isZIPCode.arguments[1]==true);return(isInteger(s)&&((s.length==digitsInZIPCode1)||(s.length==digitsInZIPCode2)))}
function isStateCode(s)
{if(isEmpty(s))
if(isStateCode.arguments.length==1)return defaultEmptyOK;else return(isStateCode.arguments[1]==true);return((USStateCodes.indexOf(s)!=-1)&&(s.indexOf(USStateCodeDelimiter)==-1))}
function isEmail(s)
{if(isEmpty(s))
if(isEmail.arguments.length==1)return defaultEmptyOK;else return(isEmail.arguments[1]==true);if(isWhitespace(s))return false;var i=1;var sLength=s.length;while((i<sLength)&&(s.charAt(i)!="@"))
{i++}
if((i>=sLength)||(s.charAt(i)!="@"))return false;else i+=2;while((i<sLength)&&(s.charAt(i)!="."))
{i++}
if((i>=sLength-1)||(s.charAt(i)!="."))return false;else return true;}
function isYear(s)
{if(isEmpty(s))
if(isYear.arguments.length==1)return defaultEmptyOK;else return(isYear.arguments[1]==true);if(!isNonnegativeInteger(s))return false;return((s.length==2)||(s.length==4));}
function isIntegerInRange(s,a,b)
{if(isEmpty(s))
if(isIntegerInRange.arguments.length==3)return defaultEmptyOK;else return(isIntegerInRange.arguments[3]==true);if(!isInteger(s,false))return false;var num=parseInt(s);return((num>=a)&&(num<=b));}
function isMonth(s)
{if(isEmpty(s))
if(isMonth.arguments.length==1)return defaultEmptyOK;else return(isMonth.arguments[1]==true);return isIntegerInRange(s,1,12);}
function isDay(s)
{if(isEmpty(s))
if(isDay.arguments.length==1)return defaultEmptyOK;else return(isDay.arguments[1]==true);return isIntegerInRange(s,1,31);}
function daysInFebruary(year)
{return(((year%4==0)&&((!(year%100==0))||(year%400==0)))?29:28);}
function isDate(year,month,day)
{if(!(isYear(year,false)&&isMonth(month,false)&&isDay(day,false)))return false;var intYear=parseInt(year);var intMonth=parseInt(month);var intDay=parseInt(day);if(intDay>daysInMonth[intMonth])return false;if((intMonth==2)&&(intDay>daysInFebruary(intYear)))return false;return true;}
function prompt(s)
{window.status=s}
function promptEntry(s)
{window.status=pEntryPrompt+s}
function warnEmpty(theField,s)
{theField.focus()
alert(mPrefix+s+mSuffix)
return false}
function warnInvalid(theField,s)
{theField.focus()
theField.select()
alert(s)
return false}
function checkString(theField,s,emptyOK)
{if(checkString.arguments.length==2)emptyOK=defaultEmptyOK;if((emptyOK==true)&&(isEmpty(theField.value)))return true;if(isWhitespace(theField.value))
return warnEmpty(theField,s);else return true;}
function checkStateCode(theField,emptyOK)
{if(checkStateCode.arguments.length==1)emptyOK=defaultEmptyOK;if((emptyOK==true)&&(isEmpty(theField.value)))return true;else
{theField.value=theField.value.toUpperCase();if(!isStateCode(theField.value,false))
return warnInvalid(theField,iStateCode);else return true;}}
function reformatZIPCode(ZIPString)
{if(ZIPString.length==5)return ZIPString;else return(reformat(ZIPString,"",5,"-",4));}
function checkZIPCode(theField,emptyOK)
{if(checkZIPCode.arguments.length==1)emptyOK=defaultEmptyOK;if((emptyOK==true)&&(isEmpty(theField.value)))return true;else
{var normalizedZIP=stripCharsInBag(theField.value,ZIPCodeDelimiters)
if(!isZIPCode(normalizedZIP,false))
return warnInvalid(theField,iZIPCode);else
{theField.value=reformatZIPCode(normalizedZIP)
return true;}}}
function reformatUSPhone(USPhone)
{return(reformat(USPhone,"(",3,") ",3,"-",4))}
function checkUSPhone(theField,emptyOK)
{if(checkUSPhone.arguments.length==1)emptyOK=defaultEmptyOK;if((emptyOK==true)&&(isEmpty(theField.value)))return true;else
{var normalizedPhone=stripCharsInBag(theField.value,phoneNumberDelimiters)
if(!isUSPhoneNumber(normalizedPhone,false))
return warnInvalid(theField,iUSPhone);else
{theField.value=reformatUSPhone(normalizedPhone)
return true;}}}
function checkInternationalPhone(theField,emptyOK)
{if(checkInternationalPhone.arguments.length==1)emptyOK=defaultEmptyOK;if((emptyOK==true)&&(isEmpty(theField.value)))return true;else
{if(!isInternationalPhoneNumber(theField.value,false))
return warnInvalid(theField,iWorldPhone);else return true;}}
function checkEmail(theField,emptyOK)
{if(checkEmail.arguments.length==1)emptyOK=defaultEmptyOK;if((emptyOK==true)&&(isEmpty(theField.value)))return true;else if(!isEmail(theField.value,false))
return warnInvalid(theField,iEmail);else return true;}
function reformatSSN(SSN)
{return(reformat(SSN,"",3,"-",2,"-",4))}
function checkSSN(theField,emptyOK)
{if(checkSSN.arguments.length==1)emptyOK=defaultEmptyOK;if((emptyOK==true)&&(isEmpty(theField.value)))return true;else
{var normalizedSSN=stripCharsInBag(theField.value,SSNDelimiters)
if(!isSSN(normalizedSSN,false))
return warnInvalid(theField,iSSN);else
{theField.value=reformatSSN(normalizedSSN)
return true;}}}
function checkYear(theField,emptyOK)
{if(checkYear.arguments.length==1)emptyOK=defaultEmptyOK;if((emptyOK==true)&&(isEmpty(theField.value)))return true;if(!isYear(theField.value,false))
return warnInvalid(theField,iYear);else return true;}
function checkMonth(theField,emptyOK)
{if(checkMonth.arguments.length==1)emptyOK=defaultEmptyOK;if((emptyOK==true)&&(isEmpty(theField.value)))return true;if(!isMonth(theField.value,false))
return warnInvalid(theField,iMonth);else return true;}
function checkDay(theField,emptyOK)
{if(checkDay.arguments.length==1)emptyOK=defaultEmptyOK;if((emptyOK==true)&&(isEmpty(theField.value)))return true;if(!isDay(theField.value,false))
return warnInvalid(theField,iDay);else return true;}
function checkDate(yearField,monthField,dayField,labelString,OKtoOmitDay)
{if(checkDate.arguments.length==4)OKtoOmitDay=false;if(!isYear(yearField.value))return warnInvalid(yearField,iYear);if(!isMonth(monthField.value))return warnInvalid(monthField,iMonth);if((OKtoOmitDay==true)&&isEmpty(dayField.value))return true;else if(!isDay(dayField.value))
return warnInvalid(dayField,iDay);if(isDate(yearField.value,monthField.value,dayField.value))
return true;alert(iDatePrefix+labelString+iDateSuffix)
return false}
function getRadioButtonValue(radio)
{for(var i=0;i<radio.length;i++)
{if(radio[i].checked){break}}
return radio[i].value}
function checkCreditCard(radio,theField)
{var cardType=getRadioButtonValue(radio)
var normalizedCCN=stripCharsInBag(theField.value,creditCardDelimiters)
if(!isCardMatch(cardType,normalizedCCN))
return warnInvalid(theField,iCreditCardPrefix+cardType+iCreditCardSuffix);else
{theField.value=normalizedCCN
return true}}
function isCreditCard(st){if(st.length>19)
return(false);sum=0;mul=1;l=st.length;for(i=0;i<l;i++){digit=st.substring(l-i-1,l-i);tproduct=parseInt(digit,10)*mul;if(tproduct>=10)
sum+=(tproduct%10)+1;else
sum+=tproduct;if(mul==1)
mul++;else
mul--;}
if((sum%10)==0)
return(true);else
return(false);}
function isVisa(cc)
{if(((cc.length==16)||(cc.length==13))&&(cc.substring(0,1)==4))
return isCreditCard(cc);return false;}
function isMasterCard(cc)
{firstdig=cc.substring(0,1);seconddig=cc.substring(1,2);if((cc.length==16)&&(firstdig==5)&&((seconddig>=1)&&(seconddig<=5)))
return isCreditCard(cc);return false;}
function isAmericanExpress(cc)
{firstdig=cc.substring(0,1);seconddig=cc.substring(1,2);if((cc.length==15)&&(firstdig==3)&&((seconddig==4)||(seconddig==7)))
return isCreditCard(cc);return false;}
function isDinersClub(cc)
{firstdig=cc.substring(0,1);seconddig=cc.substring(1,2);if((cc.length==14)&&(firstdig==3)&&((seconddig==0)||(seconddig==6)||(seconddig==8)))
return isCreditCard(cc);return false;}
function isCarteBlanche(cc)
{return isDinersClub(cc);}
function isDiscover(cc)
{first4digs=cc.substring(0,4);if((cc.length==16)&&(first4digs=="6011"))
return isCreditCard(cc);return false;}
function isEnRoute(cc)
{first4digs=cc.substring(0,4);if((cc.length==15)&&((first4digs=="2014")||(first4digs=="2149")))
return isCreditCard(cc);return false;}
function isJCB(cc)
{first4digs=cc.substring(0,4);if((cc.length==16)&&((first4digs=="3088")||(first4digs=="3096")||(first4digs=="3112")||(first4digs=="3158")||(first4digs=="3337")||(first4digs=="3528")))
return isCreditCard(cc);return false;}
function isAnyCard(cc)
{if(!isCreditCard(cc))
return false;if(!isMasterCard(cc)&&!isVisa(cc)&&!isAmericanExpress(cc)&&!isDinersClub(cc)&&!isDiscover(cc)&&!isEnRoute(cc)&&!isJCB(cc)){return false;}
return true;}
function isCardMatch(cardType,cardNumber)
{cardType=cardType.toUpperCase();var doesMatch=true;if((cardType=="VISA")&&(!isVisa(cardNumber)))
doesMatch=false;if((cardType=="MASTERCARD")&&(!isMasterCard(cardNumber)))
doesMatch=false;if(((cardType=="AMERICANEXPRESS")||(cardType=="AMEX"))&&(!isAmericanExpress(cardNumber)))doesMatch=false;if((cardType=="DISCOVER")&&(!isDiscover(cardNumber)))
doesMatch=false;if((cardType=="JCB")&&(!isJCB(cardNumber)))
doesMatch=false;if((cardType=="DINERS")&&(!isDinersClub(cardNumber)))
doesMatch=false;if((cardType=="CARTEBLANCHE")&&(!isCarteBlanche(cardNumber)))
doesMatch=false;if((cardType=="ENROUTE")&&(!isEnRoute(cardNumber)))
doesMatch=false;return doesMatch;}
function IsCC(st){return isCreditCard(st);}
function IsVisa(cc){return isVisa(cc);}
function IsVISA(cc){return isVisa(cc);}
function IsMasterCard(cc){return isMasterCard(cc);}
function IsMastercard(cc){return isMasterCard(cc);}
function IsMC(cc){return isMasterCard(cc);}
function IsAmericanExpress(cc){return isAmericanExpress(cc);}
function IsAmEx(cc){return isAmericanExpress(cc);}
function IsDinersClub(cc){return isDinersClub(cc);}
function IsDC(cc){return isDinersClub(cc);}
function IsDiners(cc){return isDinersClub(cc);}
function IsCarteBlanche(cc){return isCarteBlanche(cc);}
function IsCB(cc){return isCarteBlanche(cc);}
function IsDiscover(cc){return isDiscover(cc);}
function IsEnRoute(cc){return isEnRoute(cc);}
function IsenRoute(cc){return isEnRoute(cc);}
function IsJCB(cc){return isJCB(cc);}
function IsAnyCard(cc){return isAnyCard(cc);}
function IsCardMatch(cardType,cardNumber){return isCardMatch(cardType,cardNumber);}
if(!window.Modalbox)
var Modalbox=new Object();Modalbox.Methods={overrideAlert:false,focusableElements:new Array,currFocused:0,initialized:false,active:true,options:{title:"ModalBox Window",overlayClose:true,width:500,height:90,overlayOpacity:.65,overlayDuration:.25,slideDownDuration:.5,slideUpDuration:.5,resizeDuration:.25,inactiveFade:true,transitions:false,loadingString:"Please wait. Loading...",closeString:"Close window",closeValue:"&times;",params:{},method:'get',autoFocusing:true,aspnet:false},_options:new Object,setOptions:function(options){Object.extend(this.options,options||{});},_init:function(options){Object.extend(this._options,this.options);this.setOptions(options);this.MBoverlay=new Element("div",{id:"MB_overlay",opacity:"0"});this.MBwindow=new Element("div",{id:"MB_window",style:"display: none"}).update(this.MBframe=new Element("div",{id:"MB_frame"}).update(this.MBheader=new Element("div",{id:"MB_header"}).update(this.MBcaption=new Element("div",{id:"MB_caption"}))));this.MBclose=new Element("a",{id:"MB_close",title:this.options.closeString,href:"#"}).update("<span>"+this.options.closeValue+"</span>");this.MBheader.insert({'bottom':this.MBclose});this.MBcontent=new Element("div",{id:"MB_content"}).update(this.MBloading=new Element("div",{id:"MB_loading"}).update(this.options.loadingString));this.MBframe.insert({'bottom':this.MBcontent});var injectToEl=this.options.aspnet?$(document.body).down('form'):$(document.body);injectToEl.insert({'top':this.MBwindow});injectToEl.insert({'top':this.MBoverlay});this.initScrollX=window.pageXOffset||document.body.scrollLeft||document.documentElement.scrollLeft;this.initScrollY=window.pageYOffset||document.body.scrollTop||document.documentElement.scrollTop;this.hideObserver=this._hide.bindAsEventListener(this);this.kbdObserver=this._kbdHandler.bindAsEventListener(this);this._initObservers();this.initialized=true;},show:function(content,options){if(!this.initialized)this._init(options);this.content=content;this.setOptions(options);if(this.options.title)
$(this.MBcaption).update(this.options.title);else{$(this.MBheader).hide();$(this.MBcaption).hide();}
if(this.MBwindow.style.display=="none"){this._appear();this.event("onShow");}
else{this._update();this.event("onUpdate");}},hide:function(options){if(this.initialized){if(options&&typeof options.element!='function')Object.extend(this.options,options);this.event("beforeHide");if(this.options.transitions)
Effect.SlideUp(this.MBwindow,{duration:this.options.slideUpDuration,transition:Effect.Transitions.sinoidal,afterFinish:this._deinit.bind(this)});else{$(this.MBwindow).hide();this._deinit();}}else throw("Modalbox is not initialized.");},_hide:function(event){event.stop();if(event.element().id=='MB_overlay'&&!this.options.overlayClose)return false;this.hide();},alert:function(message){var html='<div class="MB_alert"><p>'+message+'</p><input type="button" onclick="Modalbox.hide()" value="OK" /></div>';Modalbox.show(html,{title:'Alert: '+document.title,width:300});},_appear:function(){if(Prototype.Browser.IE&&!navigator.appVersion.match(/\b7.0\b/)){window.scrollTo(0,0);this._prepareIE("100%","hidden");}
this._setWidth();this._setPosition();if(this.options.transitions){$(this.MBoverlay).setStyle({opacity:0});new Effect.Fade(this.MBoverlay,{from:0,to:this.options.overlayOpacity,duration:this.options.overlayDuration,afterFinish:function(){new Effect.SlideDown(this.MBwindow,{duration:this.options.slideDownDuration,transition:Effect.Transitions.sinoidal,afterFinish:function(){this._setPosition();this.loadContent();}.bind(this)});}.bind(this)});}else{$(this.MBoverlay).setStyle({opacity:this.options.overlayOpacity});$(this.MBwindow).show();this._setPosition();this.loadContent();}
this._setWidthAndPosition=this._setWidthAndPosition.bindAsEventListener(this);Event.observe(window,"resize",this._setWidthAndPosition);},resize:function(byWidth,byHeight,options){var wHeight=$(this.MBwindow).getHeight();var wWidth=$(this.MBwindow).getWidth();var hHeight=$(this.MBheader).getHeight();var cHeight=$(this.MBcontent).getHeight();var newHeight=((wHeight-hHeight+byHeight)<cHeight)?(cHeight+hHeight-wHeight):byHeight;if(options)this.setOptions(options);if(this.options.transitions){new Effect.ScaleBy(this.MBwindow,byWidth,newHeight,{duration:this.options.resizeDuration,afterFinish:function(){this.event("_afterResize");this.event("afterResize");}.bind(this)});}else{this.MBwindow.setStyle({width:wWidth+byWidth+"px",height:wHeight+newHeight+"px"});if(Prototype.Browser.Gecko){this.MBwindow.setStyle({overflow:'hidden'});}
setTimeout(function(){this.event("_afterResize");this.event("afterResize");}.bind(this),1);}},resizeToContent:function(options){var byHeight=this.options.height-this.MBwindow.offsetHeight;if(byHeight!=0){if(options)this.setOptions(options);Modalbox.resize(0,byHeight);}},resizeToInclude:function(element,options){var el=$(element);var elHeight=el.getHeight()+parseInt(el.getStyle('margin-top'))+parseInt(el.getStyle('margin-bottom'))+parseInt(el.getStyle('border-top-width'))+parseInt(el.getStyle('border-bottom-width'));if(elHeight>0){if(options)this.setOptions(options);Modalbox.resize(0,elHeight);}},_update:function(){$(this.MBcontent).update("");this.MBcontent.appendChild(this.MBloading);$(this.MBloading).update(this.options.loadingString);this.currentDims=[this.MBwindow.offsetWidth,this.MBwindow.offsetHeight];Modalbox.resize((this.options.width-this.currentDims[0]),(this.options.height-this.currentDims[1]),{_afterResize:this._loadAfterResize.bind(this)});},loadContent:function(){if(this.event("beforeLoad")!=false){if(typeof this.content=='string'){var htmlRegExp=new RegExp(/<\/?[^>]+>/gi);if(htmlRegExp.test(this.content)){this._insertContent(this.content.stripScripts());this._putContent(function(){this.content.extractScripts().map(function(script){return eval(script.replace("<!--","").replace("// -->",""));}.bind(window));}.bind(this));}else
new Ajax.Request(this.content,{method:this.options.method.toLowerCase(),parameters:this.options.params,onSuccess:function(transport){var response=new String(transport.responseText);this._insertContent(transport.responseText.stripScripts());this._putContent(function(){response.extractScripts().map(function(script){return eval(script.replace("<!--","").replace("// -->",""));}.bind(window));});}.bind(this),onException:function(instance,exception){Modalbox.hide();throw('Modalbox Loading Error: '+exception);}});}else if(typeof this.content=='object'){this._insertContent(this.content);this._putContent();}else{Modalbox.hide();throw('Modalbox Parameters Error: Please specify correct URL or HTML element (plain HTML or object)');}}},_insertContent:function(content){$(this.MBcontent).hide().update("");if(typeof content=='string'){setTimeout(function(){this.MBcontent.update(content);}.bind(this),1);}else if(typeof content=='object'){var _htmlObj=content.cloneNode(true);if(content.id)content.id="MB_"+content.id;$(content).select('*[id]').each(function(el){el.id="MB_"+el.id;});this.MBcontent.appendChild(_htmlObj);this.MBcontent.down().show();if(Prototype.Browser.IE)
$$("#MB_content select").invoke('setStyle',{'visibility':''});}},_putContent:function(callback){if(this.options.height==this._options.height){setTimeout(function(){Modalbox.resize(0,$(this.MBcontent).getHeight()-$(this.MBwindow).getHeight()+$(this.MBheader).getHeight(),{afterResize:function(){this.MBcontent.show().makePositioned();this.focusableElements=this._findFocusableElements();this._setFocus();setTimeout(function(){if(callback!=undefined)
callback();this.event("afterLoad");}.bind(this),1);}.bind(this)});}.bind(this),1);}else{setTimeout(function(){this._setWidth();if(Prototype.Browser.Gecko){this.MBwindow.setStyle({overflow:'hidden'});}
this.MBcontent.setStyle({overflow:'auto',height:$(this.MBwindow).getHeight()-$(this.MBheader).getHeight()-20+'px'});this.MBcontent.show();this.focusableElements=this._findFocusableElements();this._setFocus();if(callback!=undefined)
callback();this.event("afterLoad");}.bind(this),1);}},activate:function(options){this.setOptions(options);this.active=true;$(this.MBclose).observe("click",this.hideObserver);if(this.options.overlayClose)
$(this.MBoverlay).observe("click",this.hideObserver);$(this.MBclose).show();if(this.options.transitions&&this.options.inactiveFade)
new Effect.Appear(this.MBwindow,{duration:this.options.slideUpDuration});},deactivate:function(options){this.setOptions(options);this.active=false;$(this.MBclose).stopObserving("click",this.hideObserver);if(this.options.overlayClose)
$(this.MBoverlay).stopObserving("click",this.hideObserver);$(this.MBclose).hide();if(this.options.transitions&&this.options.inactiveFade)
new Effect.Fade(this.MBwindow,{duration:this.options.slideUpDuration,to:.75});},_initObservers:function(){$(this.MBclose).observe("click",this.hideObserver);if(this.options.overlayClose)
$(this.MBoverlay).observe("click",this.hideObserver);if(Prototype.Browser.IE)
Event.observe(document,"keydown",this.kbdObserver);else
Event.observe(document,"keypress",this.kbdObserver);},_removeObservers:function(){$(this.MBclose).stopObserving("click",this.hideObserver);if(this.options.overlayClose)
$(this.MBoverlay).stopObserving("click",this.hideObserver);if(Prototype.Browser.IE)
Event.stopObserving(document,"keydown",this.kbdObserver);else
Event.stopObserving(document,"keypress",this.kbdObserver);},_loadAfterResize:function(){this._setWidth();this._setPosition();this.loadContent();},_setFocus:function(){if(this.focusableElements.length>0&&this.options.autoFocusing==true){var firstEl=this.focusableElements.find(function(el){return el.tabIndex==1;})||this.focusableElements.first();this.currFocused=this.focusableElements.toArray().indexOf(firstEl);firstEl.focus();}else if($(this.MBclose).visible())
$(this.MBclose).focus();},_findFocusableElements:function(){var mycontent=[];var content=this.MBcontent.descendants();for(var index=0,len=content.length;index<len;++index){var elem=content[index];if(["textarea","select","button"].include(elem.tagName.toLowerCase())){mycontent.push(elem);}else if(elem.tagName.toLowerCase()=="input"&&elem.visible()&&elem.type!="hidden"){mycontent.push(elem);}else if(elem.tagName.toLowerCase()=="a"&&elem.href){mycontent.push(elem);}}
mycontent.invoke('addClassName','MB_focusable');return mycontent;},_kbdHandler:function(event){var node=event.element();switch(event.keyCode){case Event.KEY_TAB:event.stop();if(node!=this.focusableElements[this.currFocused])
this.currFocused=this.focusableElements.toArray().indexOf(node);if(!event.shiftKey){if(this.currFocused==this.focusableElements.length-1){if(this.focusableElements.first()!=null){this.focusableElements.first().focus();}
this.currFocused=0;}else{this.currFocused++;this.focusableElements[this.currFocused].focus();}}else{if(this.currFocused==0){this.focusableElements.last().focus();this.currFocused=this.focusableElements.length-1;}else{this.currFocused--;this.focusableElements[this.currFocused].focus();}}
break;case Event.KEY_ESC:if(this.active)this._hide(event);break;case 32:this._preventScroll(event);break;case 0:if(event.which==32)this._preventScroll(event);break;case Event.KEY_UP:case Event.KEY_DOWN:case Event.KEY_PAGEDOWN:case Event.KEY_PAGEUP:case Event.KEY_HOME:case Event.KEY_END:if(Prototype.Browser.WebKit&&!["textarea","select"].include(node.tagName.toLowerCase()))
event.stop();else if((node.tagName.toLowerCase()=="input"&&["submit","button"].include(node.type))||(node.tagName.toLowerCase()=="a"))
event.stop();break;}},_preventScroll:function(event){if(!["input","textarea","select","button"].include(event.element().tagName.toLowerCase()))
event.stop();},_deinit:function()
{this._removeObservers();Event.stopObserving(window,"resize",this._setWidthAndPosition);if(this.options.transitions){Effect.toggle(this.MBoverlay,'appear',{duration:this.options.overlayDuration,afterFinish:this._removeElements.bind(this)});}else{this.MBoverlay.hide();this._removeElements();}
$(this.MBcontent).setStyle({overflow:'',height:''});},_removeElements:function(){$(this.MBoverlay).remove();$(this.MBwindow).remove();if(Prototype.Browser.IE&&!navigator.appVersion.match(/\b7.0\b/)){this._prepareIE("","");window.scrollTo(this.initScrollX,this.initScrollY);}
if(typeof this.content=='object'){if(this.content.id&&this.content.id.match(/MB_/)){this.content.id=this.content.id.replace(/MB_/,"");}
this.content.select('*[id]').each(function(el){el.id=el.id.replace(/MB_/,"");});}
this.initialized=false;this.event("afterHide");this.setOptions(this._options);},_setWidth:function(){$(this.MBwindow).setStyle({width:this.options.width+"px",height:this.options.height+"px"});},_setPosition:function(){$(this.MBwindow).setStyle({left:Math.round((Element.getWidth(document.body)-Element.getWidth(this.MBwindow))/2)+"px"});},_setWidthAndPosition:function(){$(this.MBwindow).setStyle({width:this.options.width+"px"});this._setPosition();},_getScrollTop:function(){var theTop;if(document.documentElement&&document.documentElement.scrollTop)
theTop=document.documentElement.scrollTop;else if(document.body)
theTop=document.body.scrollTop;return theTop;},_prepareIE:function(height,overflow){$$('html, body').invoke('setStyle',{width:height,height:height,overflow:overflow});$$("select").invoke('setStyle',{'visibility':overflow});},event:function(eventName){if(this.options[eventName]){var returnValue=this.options[eventName]();this.options[eventName]=null;if(returnValue!=undefined)
return returnValue;else
return true;}
return true;}};Object.extend(Modalbox,Modalbox.Methods);if(Modalbox.overrideAlert)window.alert=Modalbox.alert;Effect.ScaleBy=Class.create();Object.extend(Object.extend(Effect.ScaleBy.prototype,Effect.Base.prototype),{initialize:function(element,byWidth,byHeight,options){this.element=$(element)
var options=Object.extend({scaleFromTop:true,scaleMode:'box',scaleByWidth:byWidth,scaleByHeight:byHeight},arguments[3]||{});this.start(options);},setup:function(){this.elementPositioning=this.element.getStyle('position');this.originalTop=this.element.offsetTop;this.originalLeft=this.element.offsetLeft;this.dims=null;if(this.options.scaleMode=='box')
this.dims=[this.element.offsetHeight,this.element.offsetWidth];if(/^content/.test(this.options.scaleMode))
this.dims=[this.element.scrollHeight,this.element.scrollWidth];if(!this.dims)
this.dims=[this.options.scaleMode.originalHeight,this.options.scaleMode.originalWidth];this.deltaY=this.options.scaleByHeight;this.deltaX=this.options.scaleByWidth;},update:function(position){var currentHeight=this.dims[0]+(this.deltaY*position);var currentWidth=this.dims[1]+(this.deltaX*position);currentHeight=(currentHeight>0)?currentHeight:0;currentWidth=(currentWidth>0)?currentWidth:0;this.setDimensions(currentHeight,currentWidth);},setDimensions:function(height,width){var d={};d.width=width+'px';d.height=height+'px';var topd=Math.round((height-this.dims[0])/2);var leftd=Math.round((width-this.dims[1])/2);if(this.elementPositioning=='absolute'||this.elementPositioning=='fixed'){if(!this.options.scaleFromTop)d.top=this.originalTop-topd+'px';d.left=this.originalLeft-leftd+'px';}else{if(!this.options.scaleFromTop)d.top=-topd+'px';d.left=-leftd+'px';}
this.element.setStyle(d);}});var getGNServiceURL=function(service){return Env.locService+"/"+service;};function init(){};function translate(text){return translations[text]||text;};function get_cookie(cookie_name)
{var results=document.cookie.match(cookie_name+'=(.*?)(;|$)');if(results)
return(unescape(results[1]));else
return null;};function popNew(a)
{msgWindow=window.open(a,"displayWindow","location=no, toolbar=no, directories=no, status=no, menubar=no, scrollbars=yes, resizable=yes, width=800, height=600")
msgWindow.focus()}
function openPage(what,type)
{msgWindow=window.open(what,type,"location=yes, toolbar=yes, directories=yes, status=yes, menubar=yes, scrollbars=yes, resizable=yes, width=800, height=600")
msgWindow.focus()}
function popFeedback(a)
{msgWindow=window.open(a,"feedbackWindow","location=no, toolbar=no, directories=no, status=no, menubar=no, scrollbars=yes, resizable=yes, width=800, height=600")
msgWindow.focus()}
function popWindow(a)
{msgWindow=window.open(a,"popWindow","location=no, toolbar=no, directories=no, status=no, menubar=no, scrollbars=yes, resizable=yes, width=800, height=600")
msgWindow.focus()}
function popInterMap(a)
{msgWindow=window.open(a,"InterMap","location=no, toolbar=no, directories=no, status=no, menubar=no, scrollbars=yes, resizable=yes, width=800, height=600")
msgWindow.focus()}
function goSubmit(form_name){document.forms[form_name].submit();}
function goReset(form_name)
{document.forms[form_name].reset();}
function entSub(form_name){if(window.event&&window.event.keyCode==13)
goSubmit(form_name);else
return true;}
function goBack()
{history.back();}
function processCancel(){document.close();}
function load(url)
{document.location.href=url;}
function doConfirm(url,message)
{if(confirm(message))
{load(url);return true;}
return false;}
function feedbackSubmit()
{var f=$('feedbackf');if(isWhitespace(f.comments.value)){f.comments.value=translate('noComment');}
if(isWhitespace(f.name.value)||isWhitespace(f.org.value)){alert(translate("addName"));return;}else if(!isEmail(f.email.value)){alert(translate("checkEmail"));return;}
Modalbox.show(getGNServiceURL('file.download'),{height:400,width:600,params:f.serialize(true)});}
function doDownload(id,all){var list=$('downloadlist').getElementsByTagName('INPUT');var pars='&id='+id+'&access=private';var selected=false;for(var i=0;i<list.length;i++){if(list[i].checked||all!=null){selected=true;var name=list[i].getAttribute('name');pars+='&fname='+name;}}
if(!selected){alert(translate("selectOneFile"));return;}
Modalbox.show(getGNServiceURL('file.disclaimer')+"?"+pars,{height:400,width:600});}
function massiveOperation(service,title,width,message)
{if(message!=null){if(!confirm(message))
return;}
var url=Env.locService+'/'+service;Modalbox.show(url,{title:title,width:width,afterHide:function(){$('search-results-content').hide();}});}
function oActionsInit(name,id){if(id===undefined){id="";}
$(name+'Ele'+id).style.width=$(name+id).getWidth();$(name+'Ele'+id).style.top=$(name+id).positionedOffset().top+$(name+id).getHeight();$(name+'Ele'+id).style.left=$(name+id).positionedOffset().left;}
function oActions(name,id){var on="../../images/plus.gif";var off="../../images/minus.png";if(id===undefined){id="";}
if(!$(name+'Ele'+id).style.top)
oActionsInit(name,id);if($(name+'Ele'+id).style.display=='none'){$(name+'Ele'+id).style.display='block';$(name+'Img'+id).src=off;}else{$(name+'Ele'+id).style.display='none';$(name+'Img'+id).src=on;}}
function actionOnSelect(msg){if($('nbselected').innerHTML==0&&$('oAcOsEle').style.display=='none'){alert(msg);}else{oActions('oAcOs');}}
function checkMassiveNewOwner(action,title){if($('user').value==''){alert(translate("selectNewOwner"));return false;}
if($('group').value==''){alert(translate("selectOwnerGroup"));return false;}
Modalbox.show(getGNServiceURL(action),{title:title,params:$('massivenewowner').serialize(true),afterHide:function(){$('search-results-content').hide();}});}
function addGroups(xmlRes){var list=xml.children(xmlRes,'group');$('group').options.length=0;for(var i=0;i<list.length;i++){var id=xml.evalXPath(list[i],'id');var name=xml.evalXPath(list[i],'name');var opt=document.createElement('option');opt.text=name;opt.value=id;if(list.length==1)opt.selected=true;$('group').options.add(opt);}}
function addGroupsCallback_OK(xmlRes){if(xmlRes.nodeName=='error'){ker.showError(translate('cannotRetrieveGroup'),xmlRes);$('group').options.length=0;$('group').value='';var user=$('user');for(i=0;i<user.options.length;i++){user.options[i].selected=false;}}else{addGroups(xmlRes);}}
function doGroups(userid){var request=ker.createRequest('id',userid);ker.send('xml.usergroups.list',request,addGroupsCallback_OK);}
function processRegSub(url)
{var invalid=" ";var minLength=6;if(document.userregisterform.name.value.length==0){alert(translate('firstNameMandatory'));return;}
if(isWhitespace(document.userregisterform.name.value)){alert(translate('firstNameMandatory'));return;}
if(document.userregisterform.name.value.indexOf(invalid)>-1){alert(translate('spacesNot'));return;}
if(document.userregisterform.surname.value.length==0){alert(translate('lastNameMandatory'));return;}
if(isWhitespace(document.userregisterform.surname.value)){alert(translate('lastNameMandatory'));return;}
if(document.userregisterform.surname.value.indexOf(invalid)>-1){alert(translate('spacesNot'));return;}
if(!isEmail(document.userregisterform.email.value)){alert(translate('emailAddressInvalid'));return;}
var myAjax=new Ajax.Request(getGNServiceURL(url),{method:'post',parameters:$('userregisterform').serialize(true),onSuccess:function(req){var output=req.responseText;var title=translate('yourRegistration');Modalbox.show(output,{title:title,width:300});},onFailure:function(req){alert(translate("registrationFailed")+" "+req.responseText+" status: "+req.status+" - "+translate("tryAgain"));}});}
function displayBox(content,contentDivId,modal){var id=contentDivId+"Box";var w=Ext.getCmp(id);if(w==undefined){w=new Ext.Window({title:translate(contentDivId),id:id,layout:'fit',modal:modal,constrain:true,width:400,collapsible:(modal?false:true),autoScroll:true,iconCls:contentDivId+'Icon',closeAction:'hide',onEsc:'hide',listeners:{hide:function(){this.hide();}},contentEl:contentDivId});}
if(w){if(content!=null){$(contentDivId).innerHTML='';$(contentDivId).innerHTML=content;$(contentDivId).style.display='block'}
w.show();w.setHeight(345);w.anchorTo(Ext.getBody(),(modal?'c-c':'tr-tr'));}}