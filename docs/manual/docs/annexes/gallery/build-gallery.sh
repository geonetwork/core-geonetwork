
rm gallery.rst
rm img/*.png
cat <<EOT >> gallery.rst
.. _gallery:

Some GeoNetwork nodes
#####################
EOT


while IFS=, read -r url
do
    echo "______________________________"
    echo "Adding $url to gallery page..."
    if curl --output /dev/null --silent --head --fail "$url"
    then
      echo " * Building thumbnail for  ..."
      cd img
      pageres $url 1366x768 --filename='<%= url %>'
      THUMBNAIL_FILE=$(ls -t | head -n 1)
      echo " . Thumbnail $THUMBNAIL_FILE created."
      #wget -qO- $url | hxselect -s '\n' -c  'title' 2>/dev/null
      #PAGE_TITLE=$(wget -qO- $url | hxselect -s '\n' -c  'title')
      #echo $PAGE_TITLE
      cd ..
      cat <<EOT >> gallery.rst

* $url

.. figure:: img/$THUMBNAIL_FILE
   :target: $url

EOT
      echo " * Portal added to the gallery."
    else
      echo " * URL does not exist."
    fi
done < gallery-urls.csv


cat <<EOT >> gallery.rst
To add your catalog to this list, add it to \`this list <https://github.com/geonetwork/doc/tree/develop/source/annexes/gallery/gallery-urls.csv>\`_.

EOT

echo "Gallery updated."
