'use strict';

var appDir = "../../app/"

var checkBreadCrumbs = function (crumb1, crumb2) {
    it('breadcrumbs should show ' + crumb1 + ' and ' + crumb2, function () {
        expect(element('#breadCrumb1').text()).toMatch(crumb1);
        expect(element('#breadCrumb2').text()).toMatch(crumb2);
    })
}

var hasResultsDescription = function (resultDesc) {
    it('should list ' + resultDesc + ' as one of the listed results descriptions', function () {
        var result = element('.data-description').text();
        expect(result).toMatch(new RegExp("\\s+"+resultDesc+"\\s+"));
    })
}
