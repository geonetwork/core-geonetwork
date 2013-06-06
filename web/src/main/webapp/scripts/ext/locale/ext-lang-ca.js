/*!
 * Ext JS Library 3.0.0
 * Copyright(c) 2006-2009 Ext JS, LLC
 * licensing@extjs.com
 * http://www.extjs.com/license
 */
/**
 * List compiled by mystix on the extjs.com forums.
 * Thank you Mystix!
 *
 * English Translations
 * updated to 2.2 by Condor (8 Aug 2008)
 */

Ext.UpdateManager.defaults.indicatorText = '<div class="loading-indicator">Carregant...</div>';

if(Ext.DataView){
  Ext.DataView.prototype.emptyText = "";
}

if(Ext.grid.GridPanel){
  Ext.grid.GridPanel.prototype.ddText = "{0} fila seleccionada{1}";
}

if(Ext.LoadMask){
  Ext.LoadMask.prototype.msg = "Carregant...";
}

Date.shortMonthNames = [
   "Gen",
   "Febr",
   "Març",
   "Abr",
   "Maig",
   "Juny",
   "Jul",
   "Ago",
   "Set",
   "Oct",
   "Nov",
   "Des"
];

Date.monthNames = [
  "Gener",
  "Febrer",
  "Març",
  "Abril",
  "Maig",
  "Juny",
  "Juliol",
  "Agost",
  "Setembre",
  "Octubre",
  "Novembre",
  "Desembre"
];

Date.getShortMonthName = function(month) {
  return Date.monthNames[month].substring(0, 3);
};

Date.monthNumbers = {
  Jan : 0,
  Feb : 1,
  Mar : 2,
  Apr : 3,
  May : 4,
  Jun : 5,
  Jul : 6,
  Aug : 7,
  Sep : 8,
  Oct : 9,
  Nov : 10,
  Dec : 11
};

Date.getMonthNumber = function(name) {
  return Date.monthNumbers[name.substring(0, 1).toUpperCase() + name.substring(1, 3).toLowerCase()];
};

Date.dayNames = [
  "Diumenge",
  "Dilluns",
  "Dimarts",
  "Dimecres",
  "Dijous",
  "Divendres",
  "Dissabte"
];

Date.getShortDayName = function(day) {
  return Date.dayNames[day].substring(0, 3);
};

Date.parseCodes.S.s = "(?:st|nd|rd|th)";

if(Ext.MessageBox){
  Ext.MessageBox.buttonText = {
    ok     : "OK",
    cancel : "Cancel·lar",
    yes    : "Si",
    no     : "No"
  };
}

if(Ext.util.Format){
  Ext.util.Format.date = function(v, format){
    if(!v) return "";
    if(!(v instanceof Date)) v = new Date(Date.parse(v));
    return v.dateFormat(format || "m/d/Y");
  };
}

if(Ext.DatePicker){
  Ext.apply(Ext.DatePicker.prototype, {
    todayText         : "Avui",
    minText           : "Aquesta data és anterior a la data mínima",
    maxText           : "Aquesta data és posterior a la data màxima",
    disabledDaysText  : "",
    disabledDatesText : "",
    monthNames        : Date.monthNames,
    dayNames          : Date.dayNames,
    nextText          : 'Mes següent (Control+Fletxa dreta)',
    prevText          : 'Mes anterior (Control+Fletxa esquerra)',
    monthYearText     : 'Esculliu un mes (Control+ tecles Up/Down per canviar any)',
    todayTip          : "{0} (Barra d'espai)",
    format            : "m/d/y",
    okText            : "&#160;OK&#160;",
    cancelText        : "Cancel·lar",
    startDay          : 0
  });
}

if(Ext.PagingToolbar){
  Ext.apply(Ext.PagingToolbar.prototype, {
    beforePageText : "Pàgina",
    afterPageText  : "de {0}",
    firstText      : "Primera Pàgina",
    prevText       : "Pàgina anterior",
    nextText       : "Pàgina següent",
    lastText       : "Última pàgina",
    refreshText    : "Refrescar",
    displayMsg     : "Mostrant {0} - {1} de {2}",
    emptyMsg       : 'Cap dada a mostrar'
  });
}

if(Ext.form.Field){
  Ext.form.Field.prototype.invalidText = "El valor del camp és invàlid";
}

if(Ext.form.TextField){
  Ext.apply(Ext.form.TextField.prototype, {
    minLengthText : "La longitud mínima del camp és de {0} caràcters",
    maxLengthText : "La longitud màxima del camp és de {0} caràcters",
    blankText     : "Aquest camp és obligatori",
    regexText     : "",
    emptyText     : null
  });
}

if(Ext.form.NumberField){
  Ext.apply(Ext.form.NumberField.prototype, {
    decimalSeparator : ".",
    decimalPrecision : 2,
    minText : "El valor mínim del camp és {0}",
    maxText : "El valor màxim del camp és {0}",
    nanText : "{0} no és un número vàlid"
  });
}

if(Ext.form.DateField){
  Ext.apply(Ext.form.DateField.prototype, {
    disabledDaysText  : "Deshabilitat",
    disabledDatesText : "Deshabilitat",
    minText           : "La data en aquest camp no pot ser anterior a {0}",
    maxText           : "La data en aquest camp no pot ser posterior a  {0}",
    invalidText       : "{0} no és una data vàlida - ha de tenir el següent format:  {1}",
    format            : "m/d/y",
    altFormats        : "m/d/Y|m-d-y|m-d-Y|m/d|m-d|md|mdy|mdY|d|Y-m-d"
  });
}

if(Ext.form.ComboBox){
  Ext.apply(Ext.form.ComboBox.prototype, {
    loadingText       : "Carregant...",
    valueNotFoundText : undefined
  });
}

if(Ext.form.VTypes){
  Ext.apply(Ext.form.VTypes, {
    emailText    : 'Aquest camp ha de contenir una adreça de correu en format "user@example.com"',
    urlText      : 'Aquest camp ha de contenir una URL en format "http:/'+'/www.example.com"',
    alphaText    : 'Aquest camp només pot contenir lletres i el caràcter _',
    alphanumText : 'Aquest camp només pot contenir lletres, números i el caràcter _'
  });
}

if(Ext.form.HtmlEditor){
  Ext.apply(Ext.form.HtmlEditor.prototype, {
    createLinkText : "Si us plau, entreu la URL per l'enllaç:",
    buttonTips : {
      bold : {
        title: 'Negreta (Ctrl+B)',
        text: 'Posar en negreta el text seleccionat.',
        cls: 'x-html-editor-tip'
      },
      italic : {
        title: 'Cursiva (Ctrl+I)',
        text: 'Posar en cursiva el text seleccionat.',
        cls: 'x-html-editor-tip'
      },
      underline : {
        title: 'Subratllar (Ctrl+U)',
        text: 'Subratllar el text seleccionat.',
        cls: 'x-html-editor-tip'
      },
      increasefontsize : {
        title: 'Augmentar text',
        text: 'Augmentar la mida de font.',
        cls: 'x-html-editor-tip'
      },
      decreasefontsize : {
        title: 'Disminuir text',
        text: 'Disminuir la mida de font.',
        cls: 'x-html-editor-tip'
      },
      backcolor : {
        title: 'Color de fons',
        text: 'Canviar el color de fons del text seleccionat.',
        cls: 'x-html-editor-tip'
      },
      forecolor : {
        title: 'Color de text',
        text: 'Canviar el color del text seleccionat.',
        cls: 'x-html-editor-tip'
      },
      justifyleft : {
        title: "Alinear a l'esquerra",
        text: "Alinear el text a l'esquerra.",
        cls: 'x-html-editor-tip'
      },
      justifycenter : {
        title: 'Centrar',
        text: "Centrar el text a l'editor.",
        cls: 'x-html-editor-tip'
      },
      justifyright : {
        title: 'Alinear a la dreta',
        text: 'Alinear el text a la dreta.',
        cls: 'x-html-editor-tip'
      },
      insertunorderedlist : {
        title: 'Llista',
        text: 'Començar una llista.',
        cls: 'x-html-editor-tip'
      },
      insertorderedlist : {
        title: 'Llista numerada',
        text: 'Començar una llista numerada.',
        cls: 'x-html-editor-tip'
      },
      createlink : {
        title: 'Enllaç',
        text: 'Fer un enllaç amb el text seleccionat.',
        cls: 'x-html-editor-tip'
      },
      sourceedit : {
        title: 'Edició',
        text: 'Canviar a mode edició.',
        cls: 'x-html-editor-tip'
      }
    }
  });
}

if(Ext.grid.GridView){
  Ext.apply(Ext.grid.GridView.prototype, {
    sortAscText  : "Ordre ascendent",
    sortDescText : "Ordre descendent",
    columnsText  : "Columnes"
  });
}

if(Ext.grid.GroupingView){
  Ext.apply(Ext.grid.GroupingView.prototype, {
    emptyGroupText : '(Cap)',
    groupByText    : 'Agrupar per aquest camp',
    showGroupsText : 'Mostrar per grups'
  });
}

if(Ext.grid.PropertyColumnModel){
  Ext.apply(Ext.grid.PropertyColumnModel.prototype, {
    nameText   : "Nom",
    valueText  : "Valor",
    dateFormat : "m/d/Y"
  });
}

if(Ext.grid.BooleanColumn){
   Ext.apply(Ext.grid.BooleanColumn.prototype, {
      trueText  : "true",
      falseText : "false",
      undefinedText: '&#160;'
   });
}

if(Ext.grid.NumberColumn){
    Ext.apply(Ext.grid.NumberColumn.prototype, {
        format : '0,000.00'
    });
}

if(Ext.grid.DateColumn){
    Ext.apply(Ext.grid.DateColumn.prototype, {
        format : 'm/d/Y'
    });
}

if(Ext.layout.BorderLayout && Ext.layout.BorderLayout.SplitRegion){
  Ext.apply(Ext.layout.BorderLayout.SplitRegion.prototype, {
    splitTip            : "Arrossegar per ajustar la mida.",
    collapsibleSplitTip : "Arrossegar per ajustar la mida. Doble clic per amagar."
  });
}

if(Ext.form.TimeField){
  Ext.apply(Ext.form.TimeField.prototype, {
    minText : "L'hora en aquest camp ha de ser igual o anterior a {0}",
    maxText : "L'hora en aquest camp ha de ser igual o posterior a {0}",
    invalidText : "{0} no és una hora vàlida",
    format : "g:i A",
    altFormats : "g:ia|g:iA|g:i a|g:i A|h:i|g:i|H:i|ga|ha|gA|h a|g a|g A|gi|hi|gia|hia|g|H"
  });
}

if(Ext.form.CheckboxGroup){
  Ext.apply(Ext.form.CheckboxGroup.prototype, {
    blankText : "Heu de seleccionar com a mínim un ítem del grup"
  });
}

if(Ext.form.RadioGroup){
  Ext.apply(Ext.form.RadioGroup.prototype, {
    blankText : "Heu de seleccionar un ítem del grup"
  });
}
