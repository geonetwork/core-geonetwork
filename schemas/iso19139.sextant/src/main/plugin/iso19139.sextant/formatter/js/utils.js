  var divs = $('.ellipsis-collapse');

  $.each(divs, function(i,p) {

    var maxLength = 350;
    var element = $(p);
    var text = element.text();

    if (text.length > maxLength) {
      // split the text in two parts, the first always showing
      var firstPart = String(text).substring(0, maxLength);
      var secondPart = String(text).substring(maxLength, text.length);

      // create some new html elements to hold the separate info
      var firstSpan = '<span>' + firstPart + '</span>';
      var secondSpan = '<span ng-if="collapsed">' +
          secondPart + '</span>';

      var moreIndicatorSpan = '<span ng-if="!collapsed">... </span>';
      var lineBreak = '<br ng-if="collapsed">';
      var toggleButton =  '<span class="collapse-text-toggle" click="toggle()">' +
              '  <span ng-show="collapsed" translate>less</span>' +
              '  <span ng-show="!collapsed" translate>more</span>' +
              '</span>';


      // remove the current contents of the element
      // and add the new ones we created
      element.empty();
      element.append(firstSpan);
      element.append(secondSpan);
      element.append(moreIndicatorSpan);
      element.append(lineBreak);
      element.append(toggleButton);
    }
    else {
      element.empty();
      element.append(text);
    }

  });