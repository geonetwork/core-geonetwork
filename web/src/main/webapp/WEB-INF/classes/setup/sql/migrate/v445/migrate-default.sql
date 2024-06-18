UPDATE Settings SET value='4.4.5' WHERE name='system/platform/version';
UPDATE Settings SET value='SNAPSHOT' WHERE name='system/platform/subVersion';

INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/translation/provider', '', 0, 7301, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/translation/serviceUrl', '', 0, 7302, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/translation/apiKey', '', 0, 7303, 'y');

INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('geonetwork-ui/datahub/enabled', 'false', 2, 7304, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('geonetwork-ui/datahub/configuration', '
[global]
proxy_path = ""
# metadata_language = "current"
# login_url = "/cas/login?service=${current_url}"
# web_component_embedder_url = "/datahub/wc-embedder.html"
# languages = [''en'', ''fr'', ''de'']
# contact_email = "opendata@mycompany.com"

[theme]
primary_color = "#c82850"
secondary_color = "#001638"
main_color = "#555"
background_color = "#fdfbff"
# header_background = "center /cover url(''assets/img/header_bg.webp'')" or "var(--color-gray-500)"
# header_foreground_color = ''white''
# thumbnail_placeholder = ''assets/img/my_custom_placeholder.png''
# main_font = "''My Custom Font'', fallback-font"
# title_font = "''My Custom Title Font'', fallback-font-title"
# fonts_stylesheet_url = "https://fonts.googleapis.com/css2?family=Open+Sans:wght@400;700&family=Permanent+Marker&display=swap"
# favicon = "assets/favicon.ico"

[search]
# filter_geometry_url = "https://my.domain.org/assets/boundary.geojson"
# filter_geometry_data = ''{ "coordinates": [...], "type": "Polygon" }''
# advanced_filters = [''publisher'', ''format'', ''publicationYear'', ''topic'', ''isSpatial'', ''license'']

# [[search_preset]]
#     name = ''filterByName''
#     filters.q = ''Full text search''
#     filters.publisher = [''Org 1'', ''Org 2'']
#     filters.format = [''format 1'', ''format 2'']
#     filters.documentStandard = [''iso19115-3.2018'']
#     filters.inspireKeyword = [''keyword 1'', ''keyword 2'']
#     filters.topic = [''boundaries'']
#     filters.publicationYear = [''2023'', ''2022'']
#     filters.isSpatial = [''yes'']
#     filters.license = [''unknown'']
#     sort = ''createDate''

[metadata-quality]
# enabled = true

[map]
# max_zoom = 10
# max_extent = [-418263.418776, 5251529.591305, 961272.067714, 6706890.609855]
# external_viewer_url_template = ''https://dev.geo2france.fr/mapstore/#/?actions=[{"type":"CATALOG:ADD_LAYERS_FROM_CATALOGS","layers":["${layer_name}"],"sources":[{"url":"${service_url}","type":"${service_type}"}]}]''
# external_viewer_open_new_tab = false
# do_not_tile_wms = false
# do_not_use_default_basemap = false

# [[map_layer]]
# type = "xyz"
# url = "https://{a-c}.basemaps.cartocdn.com/light_all/{z}/{x}/{y}.png"
# [[map_layer]]
# type = "wfs"
# url = "https://www.geo2france.fr/geoserver/cr_hdf/ows"
# name = "masque_hdf_ign_carto_latin1"
# [[map_layer]]
# type = "geojson"
# data = """
# {
#   "type": "FeatureCollection",
#   "features": [{"type": "Feature", "geometry": {"type": "Point", "coordinates": [125.6, 10.1]}}]
# }
# """

# [translations.en]
# results.sortBy.dateStamp = "Last time someone changed something"
# [translations.fr]
# results.sortBy.dateStamp = "Dernière fois que quelqu''un a modifié quelque chose"

', 0, 7305, 'n');
