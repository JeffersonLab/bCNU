#!/bin/bash

version=$1
branch_or_tag=$2

# abort on any non-zero exit code:
set -e

# git and build coatjava:
git clone https://github.com/jeffersonlab/coatjava cjvbuild
cd cjvbuild
git checkout $branch_or_tag
./build-coatjava.sh
cd -

# install coatjava in local maven repo:
mvn install:install-file \
  -Dfile=cjvbuild/coatjava/lib/clas/coat-libs-$version.jar \
  -DgroupId=org.jlab.coat \
  -DartifactId=coat-libs \
  -Dversion=$version \
  -Dpackaging=jar

# install subset of coatjava in cedbuild:
mkdir -p cedbuild/coatjava/etc/bankdefs cedbuild/coatjava/lib/clas
cp -r cjvbuild/coatjava/etc/bankdefs/hipo4 cedbuild/coatjava/etc/bankdefs
cp -r cjvbuild/coatjava/lib/clas/coat-libs-$version.jar cedbuild/coatjava/lib/clas

# cleanup:
rm -rf cjvbuild

