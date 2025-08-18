#!/bin/bash

version=13.2.0-CED
coatjava=$1

mvn install:install-file \
  -Dfile=$coatjava/lib/clas/coat-libs-$version-CED.jar \
  -DgroupId=org.jlab.coat \
  -DartifactId=coat-libs \
  -Dversion=$version \
  -Dpackaging=jar

