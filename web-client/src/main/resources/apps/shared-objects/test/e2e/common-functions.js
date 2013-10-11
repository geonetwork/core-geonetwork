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
       is,true);
};
var hasValidateAction = function (is) {
   var desc = is ? '' : ' not';

   hasActionFullPathImpl(
       'should'+desc+' have a validate action',
       'a.action-validate',
       is,true);
};
var hasRejectNonValidAction = function (is) {
   var desc = is ? '' : ' not';

   hasActionFullPathImpl(
       'should'+desc+' have a reject non valid action',
       'a.action-non-valid-reject',
       is,true);
};
var hasRejectValidAction = function (is) {
   var desc = is ? '' : ' not';

   hasActionFullPathImpl(
       'should'+desc+' have a reject valid action',
       'a.action-valid-reject',
       is,true);
};
var hasDeleteAction = function (is) {
   var desc = is ? '' : ' not';

   hasActionFullPathImpl(
       'should'+desc+' have a delete action',
       'button.action-delete',
       is,true);
};

var actionButtonPath = '.data-actions button[data-toggle=dropdown]';

var hasActionsButton = function (is) {
   var desc = is ? '' : ' not';

   hasActionFullPathImpl(
       'should'+desc+' have an action button',
       actionButtonPath,
       is,true);
};

var hasActionFullPathImpl = function (desc, path, is) {
    it(desc, function () {
        element(actionButtonPath).click();
//        expect(element('div.dropdown.open').count()).toBe(1);
        var result = element(path).count();
        if (is) {
            expect(result).toBe(1)
        } else {
            expect(result).toBe(0)
        }
    });
};

var listsRelatedMetadata = function (id, title, name, email) {
it ('clicking label should show referenced metadata', function() {
            element('.data-description a.accordion-toggle').click();
            expect(repeater('.metadata-list').count()).toBe(1)
            expect(element('.metadata-list td.id').text()).toEqual(id)
            expect(element('.metadata-list td.title').text()).toEqual(title)
            expect(element('.metadata-list td.name').text()).toEqual(name)
            expect(element('.metadata-list td.email').text()).toEqual(email)
            expect(element('.metadata-list button.show-md').text()).toEqual("Show")
        });
}