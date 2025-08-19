#!/bin/bash

version=$1
path=$2

mvn install:install-file \
  -Dfile=$path/lib/clas/coat-libs-$version.jar \
  -DgroupId=org.jlab.coat \
  -DartifactId=coat-libs \
  -Dversion=$version \
  -Dpackaging=jar

