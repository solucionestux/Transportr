#!/bin/sh

# local public-transport-enabler

cp ../public-transport-enabler/ sublibs/ -r 
cd sublibs/public-transport-enabler/enabler
sed -i "s#ONTARIO, QUEBEC#ONTARIO, NZ, SPAIN, BR, BRFLORIPA, QUEBEC#" src/de/schildbach/pte/NetworkId.java



