# OpenLayers

`ol.js` is the OpenLayers full build (`dist/ol.js` of the `ol` npm package), currently 10.9.0.

## Upgrading

- Replace `ol.js`, `ol.js.map` and `ol.css`.
- `ol.source.GeoTIFF` (Cloud Optimized GeoTIFF layers in the map viewer) needs the
  `geotiff` library, which the full build does not include: it uses the global
  `GeoTIFF` from `../geotiff/geotiff.js`. That file is not in the page bundles:
  it is served as its own wro4j bundle, `/static/geotiff.js` (the file itself in
  debug mode), which the `gnCogImport` directive loads the first time a COG is
  added. Check the `geotiff` version in the
  `dependencies` of the new `ol` package (`npm view ol@<version> dependencies`)
  and, when it changed, replace `../geotiff/geotiff.js` and `geotiff.js.map` with the
  `dist-browser` build of that version (currently 3.0.5).
- Test adding a COG in the map viewer, eg.
  `https://sentinel-cogs.s3.us-west-2.amazonaws.com/sentinel-s2-l2a-cogs/36/Q/WD/2020/7/S2A_36QWD_20200701_0_L2A/TCI.tif`.
