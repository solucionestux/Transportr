#!/bin/bash

# set forked public-transport-enabler url
git config -f .gitmodules submodule.sublibs/public-transport-enabler.url https://github.com/jamescr/public-transport-enabler.git

# set modified branch
git config -f .gitmodules submodule.sublibs/public-transport-enabler.branch incofer-cr

# note: when updating module use --remote argument.
