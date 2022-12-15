/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

(function () {
  goog.provide("sx_fields");

  goog.require("sx_batch_process_button");
  goog.require("sx_bounding");
  goog.require("sx_checkbox_with_nilreason");
  goog.require("sx_crs_selector");
  goog.require("sx_date_picker_directive");
  goog.require("sx_directory_entry_selector");
  goog.require("sx_editor_helper");
  goog.require("sx_field_duration_directive");
  goog.require("sx_fields_directive");
  goog.require("sx_logo_selector_directive");
  goog.require("sx_md_validation_tools_directive");
  goog.require("sx_multilingual_field_directive");
  goog.require("sx_organisation_entry_selector");
  goog.require("sx_record_fragment_selector");
  goog.require("sx_template_field_directive");
  goog.require("sx_anchor_switcher_directive");
  goog.require("sx_field_upload_directive");
  goog.require("sx_multientry_combiner");

  angular.module("gn_fields", [
    "gn_fields_directive",
    "gn_crs_selector",
    "gn_field_duration_directive",
    "gn_editor_helper",
    "gn_template_field_directive",
    "gn_directory_entry_selector",
    "gn_organisation_entry_selector",
    "gn_batch_process_button",
    "gn_multilingual_field_directive",
    "gn_logo_selector_directive",
    "gn_date_picker_directive",
    "gn_record_fragment_selector",
    "gn_checkbox_with_nilreason",
    "gn_md_validation_tools_directive",
    "gn_bounding",
    "gn_anchor_switcher_directive",
    "gn_field_upload_directive",
    "gn_multientry_combiner"
  ]);
})();
