# ol-cesium and Cesium

This folder holds the libraries used by the map viewer 3D mode (`?with3d=true`):

* `olcesium.js`: [ol-cesium](https://github.com/openlayers/ol-cesium) (`olcs`), which links OpenLayers and Cesium. Built in GeoNetwork, see below.
* `Cesium/`: [Cesium](https://cesium.com/platform/cesiumjs/), copied from the npm package's prebuilt files.
* `olcs.css`: ol-cesium's stylesheet (`node_modules/olcs/css/olcs.css`).

Current versions: ol-cesium `2.23.1`, Cesium `1.146.0`, working with OpenLayers `10.9.0`.

ol-cesium must match the OpenLayers version in `../openlayers/ol.js`. Upgrade it whenever OpenLayers gets a major upgrade.

## Choosing compatible versions

ol-cesium is the link between OpenLayers and Cesium, so start from it and the OpenLayers version GeoNetwork uses:

```bash
# List ol-cesium releases
npm view olcs versions

# Show which OpenLayers and Cesium versions a release supports
npm view olcs@<version> peerDependencies devDependencies
```

* Pick the latest ol-cesium release whose `peerDependencies.ol` range includes GeoNetwork's OpenLayers version.
* For Cesium, use a version inside that release's `peerDependencies.cesium` range. The `devDependencies.cesium` version is the one ol-cesium was tested with, so stay close to it or newer.
* Read the ol-cesium [changelog](https://github.com/openlayers/ol-cesium/blob/master/CHANGES.md) for breaking changes, such as renamed layer properties.

## How to upgrade

Run these steps in a temporary folder outside the repository.

### 1. Install the packages

```bash
npm init -y
npm i --ignore-scripts olcs@<olcs-version> cesium@<cesium-version> esbuild
```

### 2. Replace Cesium

Copy the prebuilt files into the repository, deleting the old folder first so no files from the old version are left behind:

```bash
DEST=<repo>/web-ui/src/main/resources/catalog/lib/olcesium/Cesium
rm -rf $DEST && mkdir $DEST
cp -R node_modules/cesium/Build/Cesium/{Cesium.js,Assets,ThirdParty,Widgets,Workers} $DEST/
cp node_modules/cesium/LICENSE.md $DEST/
```

You don't need to change any paths: `CESIUM_BASE_URL` and the script tags in `web/src/main/webapp/xslt/base-layout-cssjs-loader.xsl` still point to the same folder.

### 3. Rebuild `olcesium.js`

#### Why a rebuild is needed

The `olcs` npm package only ships ES modules (`import ... from 'ol/...'`). GeoNetwork loads its libraries as plain scripts, through the `lib3d` group in `WEB-INF/classes/web-ui-wro-sources.xml` or as individual `<script>` tags in debug mode. So it needs a single file that:

* creates a global `olcs` object, and keeps `olcs.OLCesium` available because `ViewerDirective.js` calls `new olcs.OLCesium({map})`;
* uses the global `ol` loaded by `lib/openlayers/ol.js` and the global `Cesium` loaded by `lib/olcesium/Cesium/Cesium.js`, instead of bundling its own copies. A second copy of OpenLayers would make ol-cesium's `instanceof` checks fail against GeoNetwork's layers and sources, and would make the file much larger.

#### The build files

`entry.js` re-exports everything from `olcs` and adds `OLCesium` as a named export. With `globalName: 'olcs'`, this produces `olcs.OLCesium` and the rest of the API (`olcs.OLImageryProvider`, and so on):

```js
import OLCesium from 'olcs';
export * from 'olcs';
export {OLCesium};
```

`build.mjs` bundles `entry.js` into one IIFE (a self-contained script). Replace `<olcs-version>` in its `banner` line with the ol-cesium version: the banner is a comment at the top of `olcesium.js` and is the only place the minified file records its version. A small plugin replaces every `ol/...` and `cesium` import with the matching global:

| Import in ol-cesium                           | Replaced with              |
|-----------------------------------------------|----------------------------|
| `import Overlay from 'ol/Overlay.js'`         | `ol.Overlay`               |
| `import {get} from 'ol/proj.js'`              | `ol.proj.get`              |
| `import {circular} from 'ol/geom/Polygon.js'` | `ol.geom.Polygon.circular` |
| `import {Cartesian3} from 'cesium'`           | `Cesium.Cartesian3`        |

```js
import * as esbuild from 'esbuild';
// Map ol/* and cesium imports to the globals loaded by ol.js and Cesium.js
const globals = {
  name: 'globals',
  setup(b) {
    b.onResolve({filter: /^(ol(\/.*)?|cesium)$/}, (a) => ({path: a.path, namespace: 'g'}));
    b.onLoad({filter: /.*/, namespace: 'g'}, (a) => {
      const ns = a.path === 'cesium' ? 'Cesium'
        : a.path.replace(/\.js$/, '').split('/').join('.');
      return {contents: `module.exports = ${ns};`, loader: 'js'};
    });
  }
};
await esbuild.build({
  entryPoints: ['entry.js'], bundle: true, format: 'iife', globalName: 'olcs',
  minify: true, sourcemap: true, target: 'es2020', plugins: [globals],
  legalComments: 'inline',
  banner: {js: '/* ol-cesium <olcs-version> (olcs) | BSD-2-Clause | expects global ol and Cesium */'},
  outfile: 'out/olcesium.js'
});
```

#### Build and check the output

```bash
node build.mjs
```

1. **Check that no copy of OpenLayers or Cesium got bundled.** The file should be well under 100 KB.

   ```bash
   ls -la out/olcesium.js
   ```

2. **List the OpenLayers globals the bundle uses:**

   ```bash
   grep -oE '=ol(\.[A-Za-z]+)+' out/olcesium.js | sort -u
   ```

3. **Check that GeoNetwork's `ol.js` provides all of them.** Open any GeoNetwork page, paste the list from step 2 into the browser console, and run:

   ```js
   ['ol.Overlay', 'ol.proj', /* ... */]
     .filter(p => p.split('.').reduce((o, k) => o?.[k], window) === undefined)
   ```

   This should return `[]`. If something is missing, `ol.js` is older than or different from what ol-cesium expects. Go back to [Choosing compatible versions](#choosing-compatible-versions).

   This checks each OpenLayers object, not every function inside it (such as `ol.proj.get`). A missing function shows up as an error as soon as 3D mode is switched on.

4. **Copy the result into the repository:**

   ```bash
   cp out/olcesium.js out/olcesium.js.map <repo>/web-ui/src/main/resources/catalog/lib/olcesium/
   ```

   If `node_modules/olcs/css/olcs.css` changed, copy it too.

### 4. Update GeoNetwork's code if needed

Search GeoNetwork's code for `olcs.` and `olcs_` property names, and update any that the changelog says were renamed.

### 5. Test

1. Turn on 3D mode in the UI configuration (map settings, `is3DModeAllowed`), or open the viewer with `?with3d=true`. The 2D/3D button only shows when `is3DModeAllowed` is set in the UI configuration.
2. Clear the JS & CSS cache (Admin console > Tools). Otherwise normal mode keeps serving the old `lib3d.js`. Debug mode (`?debug`) reads the files directly and doesn't need this.
3. Open the map viewer, switch to 3D, and add a WMS layer.
4. Check that the globe renders the layers with no console errors, that `Cesium.VERSION` shows the new version, and that you can switch back to 2D.

## Example: upgrade to ol-cesium 2.23.1 and Cesium 1.146.0

**1. Check versions.** GeoNetwork uses OpenLayers `10.9.0`.

```bash
npm view olcs version
npm view olcs@2.23.1 peerDependencies devDependencies
npm view cesium version
```

Output:

```text
2.23.1
peerDependencies = { cesium: '>= 1.90.0', ol: '>= 9.2, >= 10' }
devDependencies  = { cesium: '^1.141.0', ol: '10.8.0', ... }
1.146.0
```

`olcs@2.23.1` supports OpenLayers 10 and requires Cesium `>= 1.90`, tested with `^1.141`. The latest Cesium, `1.146.0`, fits that range.

**2. Install the packages.**

```bash
mkdir olcsbuild && cd olcsbuild
npm init -y
npm i --ignore-scripts olcs@2.23.1 cesium@1.146.0 esbuild
```

**3. Replace Cesium.**

```bash
DEST=<repo>/web-ui/src/main/resources/catalog/lib/olcesium/Cesium
rm -rf $DEST && mkdir $DEST
cp -R node_modules/cesium/Build/Cesium/{Cesium.js,Assets,ThirdParty,Widgets,Workers} $DEST/
cp node_modules/cesium/LICENSE.md $DEST/
```

**4. Build `olcesium.js`.** Create `entry.js` and `build.mjs` as shown above, replacing `<olcs-version>` in the `banner` line of `build.mjs` with `2.23.1`. The built file then starts with `/* ol-cesium 2.23.1 (olcs) | ... */`, which records the version.

```bash
node build.mjs
ls -la out
grep -oE '=ol(\.[A-Za-z]+)+' out/olcesium.js | sort -u
```

Output (`ls` shows `olcesium.js` at 67420 bytes and `olcesium.js.map` at 283979 bytes; `grep` prints one name per line, shown here on fewer lines):

```text
=ol.Observable =ol.Overlay =ol.easing =ol.extent =ol.format.MVT =ol.geom
=ol.geom.Point =ol.geom.Polygon =ol.geom.SimpleGeometry =ol.layer.BaseVector
=ol.layer.Group =ol.layer.Image =ol.layer.Layer =ol.layer.Tile =ol.layer.Vector
=ol.layer.VectorTile =ol.proj =ol.render =ol.render.Feature =ol.source
=ol.source.Cluster =ol.source.ImageStatic =ol.source.ImageWMS =ol.source.TileImage
=ol.source.TileWMS =ol.source.Vector =ol.source.VectorTile =ol.structs.LRUCache
=ol.style.Icon =ol.style.Stroke =ol.style.Style =ol.tilegrid =ol.tileurlfunction
```

All of these exist in GeoNetwork's `ol.js` 10.9.0. `olcs.css` didn't change.

```bash
cp out/olcesium.js out/olcesium.js.map <repo>/web-ui/src/main/resources/catalog/lib/olcesium/
```

**5. Update GeoNetwork's code.** The ol-cesium layer property `olcs.proxy` was renamed to `olcs_proxy`. That was updated in `catalog/components/common/map/mapService.js`. Nothing else in GeoNetwork needed changing.

**6. Test.** `Cesium.VERSION` returns `1.146.0`. The globe renders OSM and WMS layers in both debug and normal mode, and WMS tiles go through the GeoNetwork proxy when `cesiumProxy` is turned on.
