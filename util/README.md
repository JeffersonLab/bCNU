
# Build your own coatjava installation
```
git clone -b clas12swimtest-nabo git@github.com:jeffersonlab/coatjava clas12swimtest-nabo
cd clas12swimtest-nabo
./build-coatjava.sh
```

# Install that coatjava in ~/.m2 maven repo
```
cd bcnu
./util/mvn-install-coatjava.sh /path/to/previuosly/built/custom/clas12swimtest-nabo/coatjava
```

# Build bcnu against custom coatjava
```
git clone -b build-system git@github.com:jeffersonlab/bcnu
cd bcnu
mvn install
```

