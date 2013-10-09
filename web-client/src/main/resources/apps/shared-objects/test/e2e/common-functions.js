'use strict';

var appDir = "../../app/"

var checkBreadCrumbs = function (crumb1, crumb2) {
    it('breadcrumbs should show ' + crumb1 + ' and ' + crumb2, function () {
        expect(element('#breadCrumb1').text()).toMatch(crumb1);
        expect(element('#breadCrumb2').text()).toMatch(crumb2);
    })
};

var hasResultsDescription = function (resultDesc) {
    it('should list ' + resultDesc + ' as one of the listed results descriptions', function () {
        var result = element('.data-description').text();
        expect(result).toMatch(new RegExp("\\s+"+resultDesc+"\\s+"));
    })
};

var hasEditAction = function (isNot) {
   var desc = isNot ? ' not' : '';

   hasActionFullPathImpl(
       'should'+desc+' have an edit action',
       '.data-actions a.edit',
       'Edit',
       isNot);
};
var hasValidateAction = function (isNot) {
   var desc = isNot ? ' not' : '';

   hasActionFullPathImpl(
       'should'+desc+' have a validate action',
       '.data-actions a.validate',
       'Validate',
       isNot);
};
var hasRejectNonValidAction = function (isNot) {
   var desc = isNot ? ' not' : '';

   hasActionFullPathImpl(
       'should'+desc+' have a reject non valid action',
       '.data-actions a.non-valid-reject',
       'Reject',
       isNot);
};
var hasRejectValidAction = function (isNot) {
   var desc = isNot ? ' not' : '';

   hasActionFullPathImpl(
       'should'+desc+' have a reject valid action',
       '.data-actions a.valid-reject',
       'Delete',
       isNot);
};
var hasDeleteAction = function (isNot) {
   var desc = isNot ? ' not' : '';

   hasActionFullPathImpl(
       'should'+desc+' have a delete action',
       '.data-actions button.delete',
       'Delete',
       isNot);
};

var hasActionFullPathImpl = function (desc, path, text, isNot) {
    it(desc, function () {
        var result = element(path).text();
        if (isNot) {
            expect(result).toEqual(text);
        } else {
            expect(result).not().toEqual(text);
        }
    });
};

