#/bin/sh

set -x

DUMP_FILE="dump-`date '+%y%m%d-%H%M'`.sql.gz"

copy () {
  ssh -A  ssh0.bgdi.admin.ch "scp -r deploy@ec2-46-137-6-225.eu-west-1.compute.amazonaws.com:/srv/tomcat/geocat/work/geonetwork/$1  ."
  ssh -A  ssh0.bgdi.admin.ch "scp -r $1 ec2-46-137-20-48.eu-west-1.compute.amazonaws.com:/var/www/vhosts/tc-geocat/private/geocat.ch.deploy/hooks/geocat/data"
  ssh -A  ssh0.bgdi.admin.ch "rm -rf $1"
}
ssh -A  ssh0.bgdi.admin.ch "ssh admin@db0.bgdi.admin.ch pg_dump -U www-data -E UTF-8 -Z 9 geocat2 > $DUMP_FILE"
ssh -A  ssh0.bgdi.admin.ch "scp admin@db0.bgdi.admin.ch:$DUMP_FILE ."
ssh -A  ssh0.bgdi.admin.ch "scp $DUMP_FILE ec2-46-137-20-48.eu-west-1.compute.amazonaws.com:/var/www/vhosts/tc-geocat/private/geocat.ch.deploy/hooks/geocat/initialDump.sql.gz"
ssh -A  ssh0.bgdi.admin.ch "rm $DUMP_FILE"

copy logos
copy data
copy codelist

