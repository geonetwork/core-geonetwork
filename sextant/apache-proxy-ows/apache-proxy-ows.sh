#!/usr/bin/env bash

# Aide
if [ "$1" == "-h" ] ; then
    echo "Utilisation: ./ `basename $0` fichier_de_configuration.yaml"
    echo "This Help File: sh `basename $0` -h"
    echo "Rajouter nom de colonne? `basename $0` -c"
    exit 0
fi

# Verifie que le fichier de conf est bien présent
if [ ! -e "$1" ]; then
    echo "Fichier de configuration introuvable ou non spécifié"
    exit 0
fi

rm apache_conf.csv
rm sextant_services.conf

shopt -s globstar
echo 'Début de parcours de fichier...'

if [ "$2" == "-c" ] ; then
  echo "url_publique, url_interne" >> apache_conf.csv
fi

# loop over yaml configuration
readarray identityMappings < <(yq eval ".sites[].filename" $1)
readarray urlPubliques < <(yq eval ".sites[].url_publique" $1)
readarray urlInternes< <(yq eval ".sites[].url_interne" $1)

configCounter=0
touch temp.csv
for identityMapping in "${identityMappings[@]}"; do
    arrVar=()
    echo "Travail sur : $identityMapping"
    extension=`echo $identityMapping | sed 's/^.*\.//'`

    if [[ $extension == *\) ]]
    then
      extension='no_extension'
    fi
    if [ -z "$extension" ]
    then
      urlPublique=$(echo ${urlPubliques[$configCounter]} | sed -e 's/\r//g')
      urlInterne=$(echo ${urlInternes[$configCounter]} | sed -e 's/\r//g')
      echo "$urlPublique, $urlInterne" >> temp.csv
      ((configCounter++))
      continue
    fi
    identityMapping=${identityMapping%/*}
    for f in $identityMapping/** ; do

      # Keep only file and filename
      currentFileExtension=`echo $f |  sed 's/^.*\.//'`
      if [[ -f "${f}" ]] && [ "${currentFileExtension}" = "${extension}" ]
      then
        arrVar+=("$(basename "${f%.*}")")
      elif [[ -f "${f}" ]] && [ "${extension}" == "no_extension" ]
      then
        arrVar+=("$(basename "${f%.*}")")
      fi
    done

    for value in "${arrVar[@]}"
    do
      # Get url publique and interne and remove new line
      urlPublique=$(echo ${urlPubliques[$configCounter]} | sed -e 's/\r//g')
      urlInterne=$(echo ${urlInternes[$configCounter]} | sed -e 's/\r//g')
      urlPublique=$(echo $urlPublique | sed -e "s/\$1/$value/g")
      urlInterne=$(echo $urlInterne | sed -e "s/\$1/$value/g")
      # echo "${urlPubliques[$configCounter]}/$value, ${urlInternes[$configCounter]}/$value"  >> apache_conf.csv
      if grep -wR "$urlInterne" temp.csv
        then
          echo "[WARNING] $urlInterne existe déja"
      fi
      echo "$urlPublique, $urlInterne" >> temp.csv
    done

    ((configCounter++))

done
cat temp.csv | env LC_COLLATE=c sort -rt"|" -k1 >> apache_conf.csv
# supprime le fichier csv existant.
rm temp.csv
while IFS=, read -r field1 field2
do
    echo "ProxyPass $field1$field2" >> sextant_services.conf
    echo "ProxyPassReverse $field1$field2" >> sextant_services.conf
    echo >> sextant_services.conf
done < apache_conf.csv
