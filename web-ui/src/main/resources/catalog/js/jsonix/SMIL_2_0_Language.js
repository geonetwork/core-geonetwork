goog.provide('SMIL_2_0_Language');

var SMIL_2_0_Language_Module_Factory = function() {
  var SMIL_2_0_Language = {
    n: 'SMIL_2_0_Language',
    dens: 'http:\/\/www.w3.org\/2001\/SMIL20\/Language',
    deps: ['SMIL_2_0'],
    tis: [{
      ln: 'AnimateType',
      tn: 'animateType',
      bti: 'SMIL_2_0.AnimatePrototype',
      ps: [{
        n: 'otherAttributes',
        t: 'aa'
      }, {
        n: 'any',
        col: true,
        mx: false,
        t: 'ae'
      }, {
        n: 'targetElement',
        ti: 'IDREF',
        an: {
          lp: 'targetElement'
        },
        t: 'a'
      }, {
        n: 'calcMode',
        an: {
          lp: 'calcMode'
        },
        t: 'a'
      }, {
        n: 'skipContent',
        ti: 'Boolean',
        an: {
          lp: 'skip-content'
        },
        t: 'a'
      }, {
        n: 'fill',
        an: {
          lp: 'fill'
        },
        t: 'a'
      }, {
        n: 'syncBehavior',
        an: {
          lp: 'syncBehavior'
        },
        t: 'a'
      }, {
        n: 'syncTolerance',
        an: {
          lp: 'syncTolerance'
        },
        t: 'a'
      }, {
        n: 'restart',
        an: {
          lp: 'restart'
        },
        t: 'a'
      }, {
        n: 'restartDefault',
        an: {
          lp: 'restartDefault'
        },
        t: 'a'
      }, {
        n: 'syncBehaviorDefault',
        an: {
          lp: 'syncBehaviorDefault'
        },
        t: 'a'
      }, {
        n: 'syncToleranceDefault',
        an: {
          lp: 'syncToleranceDefault'
        },
        t: 'a'
      }, {
        n: 'begin',
        an: {
          lp: 'begin'
        },
        t: 'a'
      }, {
        n: 'end',
        an: {
          lp: 'end'
        },
        t: 'a'
      }, {
        n: 'min',
        an: {
          lp: 'min'
        },
        t: 'a'
      }, {
        n: 'max',
        an: {
          lp: 'max'
        },
        t: 'a'
      }, {
        n: 'repeat',
        ti: 'Integer',
        an: {
          lp: 'repeat'
        },
        t: 'a'
      }, {
        n: 'repeatDur',
        an: {
          lp: 'repeatDur'
        },
        t: 'a'
      }, {
        n: 'repeatCount',
        ti: 'Decimal',
        an: {
          lp: 'repeatCount'
        },
        t: 'a'
      }, {
        n: 'dur',
        an: {
          lp: 'dur'
        },
        t: 'a'
      }, {
        n: 'fillDefault',
        an: {
          lp: 'fillDefault'
        },
        t: 'a'
      }, {
        n: 'alt',
        an: {
          lp: 'alt'
        },
        t: 'a'
      }, {
        n: 'longdesc',
        an: {
          lp: 'longdesc'
        },
        t: 'a'
      }, {
        n: 'id',
        ti: 'ID',
        an: {
          lp: 'id'
        },
        t: 'a'
      }, {
        n: 'clazz',
        an: {
          lp: 'class'
        },
        t: 'a'
      }, {
        n: 'lang',
        an: {
          lp: 'lang',
          ns: 'http:\/\/www.w3.org\/XML\/1998\/namespace'
        },
        t: 'a'
      }]
    }, {
      ln: 'SetType',
      tn: 'setType',
      bti: 'SMIL_2_0.SetPrototype',
      ps: [{
        n: 'otherAttributes',
        t: 'aa'
      }, {
        n: 'any',
        col: true,
        mx: false,
        t: 'ae'
      }, {
        n: 'skipContent',
        ti: 'Boolean',
        an: {
          lp: 'skip-content'
        },
        t: 'a'
      }, {
        n: 'targetElement',
        ti: 'IDREF',
        an: {
          lp: 'targetElement'
        },
        t: 'a'
      }, {
        n: 'alt',
        an: {
          lp: 'alt'
        },
        t: 'a'
      }, {
        n: 'longdesc',
        an: {
          lp: 'longdesc'
        },
        t: 'a'
      }, {
        n: 'id',
        ti: 'ID',
        an: {
          lp: 'id'
        },
        t: 'a'
      }, {
        n: 'clazz',
        an: {
          lp: 'class'
        },
        t: 'a'
      }, {
        n: 'lang',
        an: {
          lp: 'lang',
          ns: 'http:\/\/www.w3.org\/XML\/1998\/namespace'
        },
        t: 'a'
      }, {
        n: 'fill',
        an: {
          lp: 'fill'
        },
        t: 'a'
      }, {
        n: 'syncBehavior',
        an: {
          lp: 'syncBehavior'
        },
        t: 'a'
      }, {
        n: 'syncTolerance',
        an: {
          lp: 'syncTolerance'
        },
        t: 'a'
      }, {
        n: 'restart',
        an: {
          lp: 'restart'
        },
        t: 'a'
      }, {
        n: 'restartDefault',
        an: {
          lp: 'restartDefault'
        },
        t: 'a'
      }, {
        n: 'syncBehaviorDefault',
        an: {
          lp: 'syncBehaviorDefault'
        },
        t: 'a'
      }, {
        n: 'syncToleranceDefault',
        an: {
          lp: 'syncToleranceDefault'
        },
        t: 'a'
      }, {
        n: 'begin',
        an: {
          lp: 'begin'
        },
        t: 'a'
      }, {
        n: 'end',
        an: {
          lp: 'end'
        },
        t: 'a'
      }, {
        n: 'min',
        an: {
          lp: 'min'
        },
        t: 'a'
      }, {
        n: 'max',
        an: {
          lp: 'max'
        },
        t: 'a'
      }, {
        n: 'repeat',
        ti: 'Integer',
        an: {
          lp: 'repeat'
        },
        t: 'a'
      }, {
        n: 'repeatDur',
        an: {
          lp: 'repeatDur'
        },
        t: 'a'
      }, {
        n: 'repeatCount',
        ti: 'Decimal',
        an: {
          lp: 'repeatCount'
        },
        t: 'a'
      }, {
        n: 'dur',
        an: {
          lp: 'dur'
        },
        t: 'a'
      }, {
        n: 'fillDefault',
        an: {
          lp: 'fillDefault'
        },
        t: 'a'
      }]
    }, {
      ln: 'AnimateMotionType',
      tn: 'animateMotionType',
      bti: 'SMIL_2_0.AnimateMotionPrototype',
      ps: [{
        n: 'otherAttributes',
        t: 'aa'
      }, {
        n: 'any',
        col: true,
        mx: false,
        t: 'ae'
      }, {
        n: 'alt',
        an: {
          lp: 'alt'
        },
        t: 'a'
      }, {
        n: 'longdesc',
        an: {
          lp: 'longdesc'
        },
        t: 'a'
      }, {
        n: 'id',
        ti: 'ID',
        an: {
          lp: 'id'
        },
        t: 'a'
      }, {
        n: 'clazz',
        an: {
          lp: 'class'
        },
        t: 'a'
      }, {
        n: 'lang',
        an: {
          lp: 'lang',
          ns: 'http:\/\/www.w3.org\/XML\/1998\/namespace'
        },
        t: 'a'
      }, {
        n: 'fill',
        an: {
          lp: 'fill'
        },
        t: 'a'
      }, {
        n: 'syncBehavior',
        an: {
          lp: 'syncBehavior'
        },
        t: 'a'
      }, {
        n: 'syncTolerance',
        an: {
          lp: 'syncTolerance'
        },
        t: 'a'
      }, {
        n: 'restart',
        an: {
          lp: 'restart'
        },
        t: 'a'
      }, {
        n: 'restartDefault',
        an: {
          lp: 'restartDefault'
        },
        t: 'a'
      }, {
        n: 'syncBehaviorDefault',
        an: {
          lp: 'syncBehaviorDefault'
        },
        t: 'a'
      }, {
        n: 'syncToleranceDefault',
        an: {
          lp: 'syncToleranceDefault'
        },
        t: 'a'
      }, {
        n: 'begin',
        an: {
          lp: 'begin'
        },
        t: 'a'
      }, {
        n: 'end',
        an: {
          lp: 'end'
        },
        t: 'a'
      }, {
        n: 'min',
        an: {
          lp: 'min'
        },
        t: 'a'
      }, {
        n: 'max',
        an: {
          lp: 'max'
        },
        t: 'a'
      }, {
        n: 'repeat',
        ti: 'Integer',
        an: {
          lp: 'repeat'
        },
        t: 'a'
      }, {
        n: 'repeatDur',
        an: {
          lp: 'repeatDur'
        },
        t: 'a'
      }, {
        n: 'repeatCount',
        ti: 'Decimal',
        an: {
          lp: 'repeatCount'
        },
        t: 'a'
      }, {
        n: 'dur',
        an: {
          lp: 'dur'
        },
        t: 'a'
      }, {
        n: 'fillDefault',
        an: {
          lp: 'fillDefault'
        },
        t: 'a'
      }, {
        n: 'skipContent',
        ti: 'Boolean',
        an: {
          lp: 'skip-content'
        },
        t: 'a'
      }, {
        n: 'targetElement',
        ti: 'IDREF',
        an: {
          lp: 'targetElement'
        },
        t: 'a'
      }, {
        n: 'calcMode',
        an: {
          lp: 'calcMode'
        },
        t: 'a'
      }]
    }, {
      ln: 'AnimateColorType',
      tn: 'animateColorType',
      bti: 'SMIL_2_0.AnimateColorPrototype',
      ps: [{
        n: 'otherAttributes',
        t: 'aa'
      }, {
        n: 'any',
        col: true,
        mx: false,
        t: 'ae'
      }, {
        n: 'alt',
        an: {
          lp: 'alt'
        },
        t: 'a'
      }, {
        n: 'longdesc',
        an: {
          lp: 'longdesc'
        },
        t: 'a'
      }, {
        n: 'id',
        ti: 'ID',
        an: {
          lp: 'id'
        },
        t: 'a'
      }, {
        n: 'clazz',
        an: {
          lp: 'class'
        },
        t: 'a'
      }, {
        n: 'lang',
        an: {
          lp: 'lang',
          ns: 'http:\/\/www.w3.org\/XML\/1998\/namespace'
        },
        t: 'a'
      }, {
        n: 'fill',
        an: {
          lp: 'fill'
        },
        t: 'a'
      }, {
        n: 'syncBehavior',
        an: {
          lp: 'syncBehavior'
        },
        t: 'a'
      }, {
        n: 'syncTolerance',
        an: {
          lp: 'syncTolerance'
        },
        t: 'a'
      }, {
        n: 'restart',
        an: {
          lp: 'restart'
        },
        t: 'a'
      }, {
        n: 'restartDefault',
        an: {
          lp: 'restartDefault'
        },
        t: 'a'
      }, {
        n: 'syncBehaviorDefault',
        an: {
          lp: 'syncBehaviorDefault'
        },
        t: 'a'
      }, {
        n: 'syncToleranceDefault',
        an: {
          lp: 'syncToleranceDefault'
        },
        t: 'a'
      }, {
        n: 'begin',
        an: {
          lp: 'begin'
        },
        t: 'a'
      }, {
        n: 'end',
        an: {
          lp: 'end'
        },
        t: 'a'
      }, {
        n: 'min',
        an: {
          lp: 'min'
        },
        t: 'a'
      }, {
        n: 'max',
        an: {
          lp: 'max'
        },
        t: 'a'
      }, {
        n: 'repeat',
        ti: 'Integer',
        an: {
          lp: 'repeat'
        },
        t: 'a'
      }, {
        n: 'repeatDur',
        an: {
          lp: 'repeatDur'
        },
        t: 'a'
      }, {
        n: 'repeatCount',
        ti: 'Decimal',
        an: {
          lp: 'repeatCount'
        },
        t: 'a'
      }, {
        n: 'dur',
        an: {
          lp: 'dur'
        },
        t: 'a'
      }, {
        n: 'fillDefault',
        an: {
          lp: 'fillDefault'
        },
        t: 'a'
      }, {
        n: 'calcMode',
        an: {
          lp: 'calcMode'
        },
        t: 'a'
      }, {
        n: 'skipContent',
        ti: 'Boolean',
        an: {
          lp: 'skip-content'
        },
        t: 'a'
      }, {
        n: 'targetElement',
        ti: 'IDREF',
        an: {
          lp: 'targetElement'
        },
        t: 'a'
      }]
    }],
    eis: [{
      en: 'animate',
      ti: '.AnimateType'
    }, {
      en: 'set',
      ti: '.SetType'
    }, {
      en: 'animateColor',
      ti: '.AnimateColorType'
    }, {
      en: 'animateMotion',
      ti: '.AnimateMotionType'
    }]
  };
  return {
    SMIL_2_0_Language: SMIL_2_0_Language
  };
};
if (typeof define === 'function' && define.amd) {
  define([], SMIL_2_0_Language_Module_Factory);
}
else {
  var SMIL_2_0_Language_Module = SMIL_2_0_Language_Module_Factory();
  if (typeof module !== 'undefined' && module.exports) {
    /**
     *
     * @type {{n: string, dens: string, deps: string[], tis: *[], eis: *[]}|SMIL_2_0_Language}
     */
    module.exports.SMIL_2_0_Language =
        SMIL_2_0_Language_Module.SMIL_2_0_Language;
  }
  else {
    var SMIL_2_0_Language = SMIL_2_0_Language_Module.SMIL_2_0_Language;
  }
}
