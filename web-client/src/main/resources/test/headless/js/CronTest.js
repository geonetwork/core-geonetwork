describe("Cron", function() {
      var dayTranslation = {
          MON: 'MON',
          TUE: 'TUE',
          WED: 'WED',
          THU: 'THU',
          FRI: 'FRI',
          SAT: 'SAT',
          SUN: 'SUN'
      };
      beforeEach(function() {
        
      });

      it("0 5 * ? * ? should each hour on the 5th minute each day", function() {
        var cronString = "0 5 * ? * ?";
        var cron = new Cron(cronString);
        
        expect(cron.mins).toEqual('5');
        expect(cron.hours).toEqual('*');
        expect(cron.intervalHours).toBeUndefined;
        expect(cron.days.mon).toBeTruthy;
        expect(cron.days.tue).toBeTruthy;
        expect(cron.days.wed).toBeTruthy;
        expect(cron.days.thu).toBeTruthy;
        expect(cron.days.fri).toBeTruthy;
        expect(cron.days.sat).toBeTruthy;
        expect(cron.days.sun).toBeTruthy;
        expect(cron.asString()).toEqual(cronString);
        expect(cron.nodeAtSummary()).toEqual("00:05");
        expect(cron.nodeIntervalSummary()).toEqual("1 h (MON-SUN)");
      });
      it("0 5 * MON,TUE,WED * ? should repeat each hour on the 5th minute on monday, tuesday and wednesday", function() {
        var cronString = "0 5 * ? * MON,TUE,WED";
        var cron = new Cron(cronString);
        
        expect(cron.mins).toEqual('5');
        expect(cron.hours).toEqual('*');
        expect(cron.intervalHours).toBeUndefined;
        expect(cron.days.mon).toBeTruthy;
        expect(cron.days.tue).toBeTruthy;
        expect(cron.days.wed).toBeTruthy;
        expect(cron.days.thu).toBeFalsy;
        expect(cron.days.fri).toBeFalsy;
        expect(cron.days.sat).toBeFalsy;
        expect(cron.days.sun).toBeFalsy;
        expect(cron.asString()).toEqual(cronString);
        expect(cron.nodeAtSummary()).toEqual("00:05");
        expect(cron.nodeIntervalSummary(dayTranslation)).toEqual("1 h (MON,TUE,WED)");
        expect(cron.nodeIntervalSummary({
            MON: 'MAN',
            TUE: 'DIE',
            WED: 'MIT',
            THU: 'DON',
            FRI: 'FRI',
            SAT: 'SAM',
            SUN: 'SON'
        })).toEqual("1 h (MAN,DIE,MIT)");
      });
      it("0 5 10 ? * ? should repeat at 10:05 each day", function() {
          var cronString = "0 5 10 ? * ?";
          var cron = new Cron(cronString);
          
          expect(cron.mins).toEqual('5');
          expect(cron.hours).toEqual('10');
          expect(cron.intervalHours).toBeUndefined;
          expect(cron.days.mon).toBeTruthy;
          expect(cron.days.tue).toBeTruthy;
          expect(cron.days.wed).toBeTruthy;
          expect(cron.days.thu).toBeTruthy;
          expect(cron.days.fri).toBeTruthy;
          expect(cron.days.sat).toBeTruthy;
          expect(cron.days.sun).toBeTruthy;
          expect(cron.asString()).toEqual(cronString);
          expect(cron.nodeAtSummary()).toEqual("10:05");
          expect(cron.nodeIntervalSummary()).toEqual("MON-SUN");
      });
      it("0 5 10/5 ? * ? should each day starting at 10:05 repeat every 5 hours ", function() {
          var cronString = "0 5 10/5 ? * ?";
          var cron = new Cron(cronString);
          
          expect(cron.mins).toEqual('5');
          expect(cron.hours).toEqual('10');
          expect(cron.intervalHours).toEqual('5')
          expect(cron.days.mon).toBeTruthy;
          expect(cron.days.tue).toBeTruthy;
          expect(cron.days.wed).toBeTruthy;
          expect(cron.days.thu).toBeTruthy;
          expect(cron.days.fri).toBeTruthy;
          expect(cron.days.sat).toBeTruthy;
          expect(cron.days.sun).toBeTruthy;
          expect(cron.asString()).toEqual(cronString);
          expect(cron.nodeAtSummary()).toEqual("10:05");
          expect(cron.nodeIntervalSummary()).toEqual("5 h (MON-SUN)");
      });
      it("default should repeat once a day", function() {
          var cron = new Cron();
          
          expect(cron.mins).toEqual('0');
          expect(cron.hours).toEqual('0');
          expect(cron.intervalHours).toBeUndefined;
          expect(cron.days.mon).toBeTruthy;
          expect(cron.days.tue).toBeTruthy;
          expect(cron.days.wed).toBeTruthy;
          expect(cron.days.thu).toBeTruthy;
          expect(cron.days.fri).toBeTruthy;
          expect(cron.days.sat).toBeTruthy;
          expect(cron.days.sun).toBeTruthy;
          expect(cron.asString()).toEqual("0 0 0 ? * ?");
          expect(cron.nodeAtSummary()).toEqual("00:00");
          expect(cron.nodeIntervalSummary()).toEqual("MON-SUN");
      });
      ['5', '12', '60', '119'].each(function(v) {
          it("the value "+v+" should create a task repeating every hour", function() {
              var cron=new Cron(v);
              expect(cron.mins).toEqual('0');
              expect(cron.hours).toEqual('0');
              expect(cron.intervalHours).toEqual('1');
              expect(cron.asString()).toEqual('0 0 0/1 ? * ?');            
          });
      });
      ['121', '180', '240'].each(function(v) {
          it("the value "+v+" should create a task repeating 3 hours", function() {
              var cron=new Cron(v);
              expect(cron.mins).toEqual('0');
              expect(cron.hours).toEqual('0');
              expect(cron.intervalHours).toEqual('3');
              expect(cron.asString()).toEqual('0 0 0/3 ? * ?');              
          });
      });
      ['300', '480', '539'].each(function(v) {
          it("the value "+v+" should create a task repeating 6 hours", function() {
              var cron=new Cron(v);
              expect(cron.mins).toEqual('0');
              expect(cron.hours).toEqual('0');
              expect(cron.intervalHours).toEqual('6');
              expect(cron.asString()).toEqual('0 0 0/6 ? * ?');              
          });
      });
      ['570', '720', '1020'].each(function(v) {
          it("the value "+v+" should create a task repeating 12 hours", function() {
              var cron=new Cron(v);
              expect(cron.mins).toEqual('0');
              expect(cron.hours).toEqual('0');
              expect(cron.intervalHours).toEqual('12');
              expect(cron.asString()).toEqual('0 0 0/12 ? * ?');              
          });
      });
      ['1081', '1320', '1440', '2150'].each(function(v) {
          it("the value "+v+" should create a task repeating 24 hours", function() {
              var cron=new Cron(v);
              expect(cron.mins).toEqual('0');
              expect(cron.hours).toEqual('0');
              expect(cron.intervalHours).toBeUndefined;
              expect(cron.asString()).toEqual('0 0 0 ? * ?');              
          });
      });
      ['2232', '2880', '3456'].each(function(v) {
          it("the value "+v+" should create a task which repeats more or less every 2 days", function() {
              var cron=new Cron(v);
              expect(cron.mins).toEqual('0');
              expect(cron.hours).toEqual('0');
              expect(cron.intervalHours).toBeUndefined;
              var cronString = cron.asString();
              expect(cronString).toEqual('0 0 0 ? * MON,WED,FRI,SUN');              
          });
      });
      it("the value 3672, 4320, 4896 should create a task which repeats more or less every 3 days", function() {
          ['3672', '4320', '4896'].each(function(v) {
              var cron=new Cron(v);
              expect(cron.mins).toEqual('0');
              expect(cron.hours).toEqual('0');
              expect(cron.intervalHours).toBeUndefined;
              expect(cron.asString()).toEqual('0 0 0 ? * MON,THU,SUN');              
          });
      });
      ['5112', '5760', '6336'].each(function(v) {
          it("the value "+v+" should create a task which repeats more or less every 4 days", function() {
              var cron=new Cron(v);
              expect(cron.mins).toEqual('0');
              expect(cron.hours).toEqual('0');
              expect(cron.intervalHours).toBeUndefined;
              expect(cron.asString()).toEqual('0 0 0 ? * MON,FRI');              
          });
      });
      ['6552', '7200', '7776'].each(function(v) {
          it("the value "+v+" should create a task which repeats more or less every 5 days", function() {
              var cron=new Cron(v);
              expect(cron.mins).toEqual('0');
              expect(cron.hours).toEqual('0');
              expect(cron.intervalHours).toBeUndefined;
              expect(cron.asString()).toEqual('0 0 0 ? * MON,SAT');              
          });
      });
      ['7992', '8640', '9216', '30000'].each(function(v) {
          it("the value "+v+" should create a task which repeats once each week", function() {
              var cron=new Cron(v);
              expect(cron.mins).toEqual('0');
              expect(cron.hours).toEqual('0');
              expect(cron.intervalHours).toBeUndefined;
              expect(cron.asString()).toEqual('0 0 0 ? * MON');              
          });
      });
      
});