# Deces Match ID

This is a module for ancestris to lookup individuals in your genealogy on https://deces.matchid.io/

## Generate the NetBeans Module

Open the project in apache NetBeans and add to the file nbproject/suite.properties the following properties:
- suite.dir: The path to ancestris source code
- nbplatform.custom.netbeans.dest.dir: The path to apache-netbeans

Then Right click on the project and click on `Create NBM`

It will generate a file `build/ancestris-modules-gedcom-decesmatchidio.nbm`

## Installation using the NetBean Module

- You need the file generated in the previous step or you can download
  directly from the releases on github.
- Launch ancestris In the menu `Options` > `Manage Plugins`
- In the new popup window `Extensions` choose `Downloaded` tab and click on button `Add Plugins...`
- Select the nbm file and then click on `Install` in the window `Extensions`.

The new menu action is available in the 'Actions menu for INDIVIDUAL' with the label 'deces.matchid.io'
