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

var hasEditAction = function (is) {
   var desc = is ? '' : ' not';

   hasActionFullPathImpl(
       'should'+desc+' have an edit action',
       'a.action-edit',
       'Edit',
       is,true);
};
var hasValidateAction = function (is) {
   var desc = is ? '' : ' not';

   hasActionFullPathImpl(
       'should'+desc+' have a validate action',
       'a.action-validate',
       'Validate',
       is,true);
};
var hasRejectNonValidAction = function (is) {
   var desc = is ? '' : ' not';

   hasActionFullPathImpl(
       'should'+desc+' have a reject non valid action',
       'a.action-non-valid-reject',
       'Reject',
       is,true);
};
var hasRejectValidAction = function (is) {
   var desc = is ? '' : ' not';

   hasActionFullPathImpl(
       'should'+desc+' have a reject valid action',
       'a.action-valid-reject',
       'Delete',
       is,true);
};
var hasDeleteAction = function (is) {
   var desc = is ? '' : ' not';

   hasActionFullPathImpl(
       'should'+desc+' have a delete action',
       'button.action-delete',
       'Delete',
       is,true);
};

var actionButtonPath = '.data-actions button[data-toggle=dropdown]';

var hasActionsButton = function (is) {
   var desc = is ? '' : ' not';

   hasActionFullPathImpl(
       'should'+desc+' have an action button',
       actionButtonPath,
       'Actions',
       is,true);
};

var hasActionFullPathImpl = function (desc, path, text, is) {
    it(desc, function () {
        element(actionButtonPath).click();
        var result = element(path).text();
        var regex = new RegExp('\\s*'+text+'\\s*');

        if (is) {
            expect(result).toMatch(regex);
        } else {
            expect(result).not().toMatch(regex);
        }
    });
};


