
# GeoNetwork gallery page

To add a new site to the gallery, add the URL to the `gallery-urls.csv` file.

To build the gallery page, install the following:

```shell script
npm install pageres
sudo apt install html-xml-utils pageres-cli
```

And run 

```shell script
./build-gallery.sh
```

to create the RST page.
