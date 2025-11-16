# Deces Match ID

This is a module for ancestris to lookup individuals in your genealogy on https://deces.matchid.io/

## Installation using sources

  mvn -Dancestris.basedir=/ancestris/ clean install

## Installation using release

  Copy jar file ancestris-modules-gedcom-decesmatchidio.jar to ${ancestris.basedir}/ancestris/modules/
  Copy file config.xml to ${ancestris.basedir}/ancestris/config/Modules/ancestris-modules-gedcom-decesmatchidio.xml
  Copy file tracking.xml to ${ancestris.basedir}/ancestris/update_tracking/ancestris-modules-gedcom-decesmatchidio.xml

## Launch

  ancestris --reload ./ancestris/modules/ancestris-modules-gedcom-decesmatchidio.jar

The new menu action is available in the 'Actions menu for INDIVIDUAL' with the label 'deces.matchid.io'
