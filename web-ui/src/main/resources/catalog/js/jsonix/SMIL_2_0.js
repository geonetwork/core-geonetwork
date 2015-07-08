goog.provide('SMIL_2_0');

var SMIL_2_0_Module_Factory = function() {
  var SMIL_2_0 = {
    n: 'SMIL_2_0',
    dens: 'http:\/\/www.w3.org\/2001\/SMIL20\/',
    deps: ['SMIL_2_0_Language'],
    tis: [{
      ln: 'AnimateColorPrototype',
      tn: 'animateColorPrototype',
      ps: [{
        n: 'from',
        an: {
          lp: 'from'
        },
        t: 'a'
      }, {
        n: 'by',
        an: {
          lp: 'by'
        },
        t: 'a'
      }, {
        n: 'values',
        an: {
          lp: 'values'
        },
        t: 'a'
      }, {
        n: 'to',
        an: {
          lp: 'to'
        },
        t: 'a'
      }, {
        n: 'attributeName',
        an: {
          lp: 'attributeName'
        },
        t: 'a'
      }, {
        n: 'attributeType',
        an: {
          lp: 'attributeType'
        },
        t: 'a'
      }, {
        n: 'additive',
        an: {
          lp: 'additive'
        },
        t: 'a'
      }, {
        n: 'accumulate',
        an: {
          lp: 'accumulate'
        },
        t: 'a'
      }]
    }, {
      ln: 'AnimateMotionPrototype',
      tn: 'animateMotionPrototype',
      ps: [{
        n: 'origin',
        an: {
          lp: 'origin'
        },
        t: 'a'
      }, {
        n: 'from',
        an: {
          lp: 'from'
        },
        t: 'a'
      }, {
        n: 'by',
        an: {
          lp: 'by'
        },
        t: 'a'
      }, {
        n: 'values',
        an: {
          lp: 'values'
        },
        t: 'a'
      }, {
        n: 'to',
        an: {
          lp: 'to'
        },
        t: 'a'
      }, {
        n: 'additive',
        an: {
          lp: 'additive'
        },
        t: 'a'
      }, {
        n: 'accumulate',
        an: {
          lp: 'accumulate'
        },
        t: 'a'
      }]
    }, {
      ln: 'SetPrototype',
      tn: 'setPrototype',
      ps: [{
        n: 'attributeName',
        an: {
          lp: 'attributeName'
        },
        t: 'a'
      }, {
        n: 'attributeType',
        an: {
          lp: 'attributeType'
        },
        t: 'a'
      }, {
        n: 'to',
        an: {
          lp: 'to'
        },
        t: 'a'
      }]
    }, {
      ln: 'AnimatePrototype',
      tn: 'animatePrototype',
      ps: [{
        n: 'additive',
        an: {
          lp: 'additive'
        },
        t: 'a'
      }, {
        n: 'accumulate',
        an: {
          lp: 'accumulate'
        },
        t: 'a'
      }, {
        n: 'from',
        an: {
          lp: 'from'
        },
        t: 'a'
      }, {
        n: 'by',
        an: {
          lp: 'by'
        },
        t: 'a'
      }, {
        n: 'values',
        an: {
          lp: 'values'
        },
        t: 'a'
      }, {
        n: 'to',
        an: {
          lp: 'to'
        },
        t: 'a'
      }, {
        n: 'attributeName',
        an: {
          lp: 'attributeName'
        },
        t: 'a'
      }, {
        n: 'attributeType',
        an: {
          lp: 'attributeType'
        },
        t: 'a'
      }]
    }, {
      t: 'enum',
      ln: 'SyncBehaviorDefaultType',
      vs: ['canSlip', 'locked', 'independent', 'inherit']
    }, {
      t: 'enum',
      ln: 'FillDefaultType',
      vs: ['remove', 'freeze', 'hold', 'auto', 'inherit', 'transition']
    }, {
      t: 'enum',
      ln: 'SyncBehaviorType',
      vs: ['canSlip', 'locked', 'independent', 'default']
    }, {
      t: 'enum',
      ln: 'FillTimingAttrsType',
      vs: ['remove', 'freeze', 'hold', 'auto', 'default', 'transition']
    }, {
      t: 'enum',
      ln: 'RestartDefaultType',
      vs: ['never', 'always', 'whenNotActive', 'inherit']
    }, {
      t: 'enum',
      ln: 'RestartTimingType',
      vs: ['never', 'always', 'whenNotActive', 'default']
    }],
    eis: [{
      en: 'animateMotion',
      ti: 'SMIL_2_0_Language.AnimateMotionType',
      sh: {
        lp: 'animateMotion',
        ns: 'http:\/\/www.w3.org\/2001\/SMIL20\/Language'
      }
    }, {
      en: 'animateColor',
      ti: 'SMIL_2_0_Language.AnimateColorType',
      sh: {
        lp: 'animateColor',
        ns: 'http:\/\/www.w3.org\/2001\/SMIL20\/Language'
      }
    }, {
      en: 'animate',
      ti: 'SMIL_2_0_Language.AnimateType',
      sh: {
        lp: 'animate',
        ns: 'http:\/\/www.w3.org\/2001\/SMIL20\/Language'
      }
    }, {
      en: 'set',
      ti: 'SMIL_2_0_Language.SetType',
      sh: {
        lp: 'set',
        ns: 'http:\/\/www.w3.org\/2001\/SMIL20\/Language'
      }
    }]
  };
  return {
    SMIL_2_0: SMIL_2_0
  };
};
if (typeof define === 'function' && define.amd) {
  define([], SMIL_2_0_Module_Factory);
}
else {
  var SMIL_2_0_Module = SMIL_2_0_Module_Factory();
  if (typeof module !== 'undefined' && module.exports) {
    /**
     *
     * @type {{n: string, dens: string, deps: string[], tis: *[], eis: *[]}|SMIL_2_0}
     */
    module.exports.SMIL_2_0 = SMIL_2_0_Module.SMIL_2_0;
  }
  else {
    var SMIL_2_0 = SMIL_2_0_Module.SMIL_2_0;
  }
}
