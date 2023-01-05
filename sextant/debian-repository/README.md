
# Sextant package repository

Ifremer needs some debian packages with some resources which can't be made public (eg. OCI driver).

## History

* 2023-01-05 / GDAL 3.6.2 / https://gitlab.ifremer.fr/sextant/gdal/-/tree/ifremer-bullseye-3.6.2

## Debian distribution target
 
`bullseye`

## Packages to create

* oracle-instantclient
* GDAL (with OCI)

## Building packages

Download OCI RPM from https://www.oracle.com/fr/database/technologies/instant-client/downloads.html
and convert RPM to DEB using [Alien](https://joeyh.name/code/alien/).

Then clone GDAL debian GIS repository and create a new release for Sextant with OCI support.

```shell
export DISTRIBUTION=bullseye

# Download OCI driver from Oracle
mkdir -p packages/oci
cd packages/oci

wget https://download.oracle.com/otn_software/linux/instantclient/218000/oracle-instantclient-basic-21.8.0.0.0-1.el8.x86_64.rpm
wget https://download.oracle.com/otn_software/linux/instantclient/218000/oracle-instantclient-devel-21.8.0.0.0-1.el8.x86_64.rpm



# Download GDAL source and configure package
export GDAL_VERSION=3.6.2
cd ..
git clone https://salsa.debian.org/debian-gis-team/gdal 
cd gdal
git remote add sextant https://gitlab.ifremer.fr/sextant/gdal 
git fetch sextant
git branch -d pristine-tar
git checkout -b pristine-tar origin/pristine-tar
git checkout -b ifremer-$DISTRIBUTION-$GDAL_VERSION debian/3.6.2_rc1+dfsg-1_exp1

cd debian
# Cf. https://gitlab.ifremer.fr/sextant/gdal/-/commit/c1ea8efb038275c379818cf52c3a422066809b7b

vi control
# Add oracle-instantclient-devel

vi rules
# Add for version before 3.6
#			--with-oci-lib=/usr/lib/oracle/11.2/client64/lib/ \
#			--with-oci-include=/usr/include/oracle/11.2/client64/ \
# After Version 3.6+
#   -DGDAL_USE_ORACLE=ON \
#   -DOracle_ROOT=/usr/lib/oracle/21/client64 \
#   -DOracle_INCLUDE_DIR=/usr/include/oracle/21/client64 \

# Add an entry on debian changelog
# Update distribution and name
dch -i 

git add changelog control rules
git commit -m "Ifremer / $DISTRIBUTION version $GDAL_VERSION"
git clean -fd
git status


# Get the debian distribution to build and pack
docker run --privileged -it -v$(pwd):/usr/src/packages debian:$DISTRIBUTION bash

echo "deb-src http://deb.debian.org/debian bullseye main" >> /etc/apt/sources.list
apt update && apt install alien vim wget gpg git-buildpackage lsb-release bash-completion libpthread-stubs0-dev libc++-dev libaio1 cmake libblosc-dev liblz4-dev libpcre2-dev pkg-kde-tools python3-setuptools


# Convert RPM to DEB using Alien
cd /usr/src/packages/oci
alien oracle-instantclient-basic-21.8.0.0.0-1.el8.x86_64.rpm
alien oracle-instantclient-devel-21.8.0.0.0-1.el8.x86_64.rpm

# Check
dpkg-deb -c oracle-instantclient-devel

# Install
dpkg -i oracle-instantclient-*.deb

# apt install libaio1 
# Maybe check if alien can register that dependency to the deb produced?
# cd /usr/lib/oracle
# ldd libclntsh.so
# Check for not found
# ldconfig

# Install dependency from distribution version of GDAL.
# Not the one we are building. We may need to install them
# manually from build-deps list in .
apt build-dep gdal


cd /usr/src/packages/gdal
gbp buildpackage --git-debian-branch=ifremer-bullseye-3.6.2 -uc -us -sa

# If Unmet build dependencies
#apt install cmake ...
#apt install libopencl-c-headers
```

All packages are created one folder below.



### Common issues

* `pristine-tar` branch issue

```shell
gbp:error: Can not find pristine tar commit for archive 'gdal_3.6.2+dfsg.orig.tar.xz'
```

Update the pristine-tar branch 


* `gbp` complains about:

```shell
gbp:error: You have uncommitted changes in your source tree:
gbp:error: On branch ifremer-bullseye-3.6.2
```

Cleanup source tree:

```shell
git clean -fd && git reset --hard
```



## Publishing packages with Aptly

A GPG key was created for Ifremer
```
/root/.gnupg/pubring.kbx
------------------------
pub   rsa4096 2023-01-05 [SC]
      BAA003AB9C3253805A0E2F3E18EA8CC7A12C8C92
uid           [ultimate] Ifremer Debian packages <debian-packages@ifremer.fr>
sub   rsa4096 2023-01-05 [E]
```


```shell
cd aptly

DEB_DIR=../packages
DISTRIBUTION=bullseye

mkdir -p $DISTRIBUTION/dev
cp $DEB_DIR/*.deb $DISTRIBUTION/dev/.
cp $DEB_DIR/oci/*.deb $DISTRIBUTION/dev/.

docker-compose up
docker ps
# To get the aptly container ref

# To get the key details
docker exec 73f34f750428 gpg --list-keys

# To upload distribution folder containing deb to the repository
docker exec 73f34f750428 /mnt/update-repos.sh BAA003AB9C3253805A0E2F3E18EA8CC7A12C8C92

docker exec 73f34f750428 gpg --armor --output /mnt/aptly-public/gpg --export BAA003AB9C3253805A0E2F3E18EA8CC7A12C8C92
```

Testing the repository in the `aptly_test-docker_1` container:

```shell
docker exec -it 1553fc8aa26d bash

apt update && apt install vim gnupg2 wget

# Add the new repo
echo "deb http://httpd/ bullseye dev" > /etc/apt/sources.list.d/aptly.list

# apt update complains about missing key
# W: GPG error: http://httpd bullseye InRelease: The following signatures couldn't be verified because the public key is not available: NO_PUBKEY 18EA8CC7A12C8C92
# Add the key by getting it from the aptly container using
# docker exec 73f34f750428 gpg --armor --export BAA003AB9C3253805A0E2F3E18EA8CC7A12C8C92

wget -O - -q http://httpd/gpg | apt-key add -

apt update

apt install gdal-bin libaio1

ogrinfo --formats | grep Oracle
```


### Known issue

* Missing `libaio` lib
```
ogrinfo: error while loading shared libraries: libaio.so.1: cannot open shared object file: No such file or directory
```

which is probably a dependency of Oracle instant client ... So it needs to be installed with GDAL.



