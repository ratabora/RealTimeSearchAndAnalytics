Real Time Search and Analytics on Big Data
=============

About the READMEs
-------
Markdown (.md) is a human readable text format often used in version control software like GitHub. MarkDown easily converts to HTML so users can choose which format their prefer. Several text editors like Sublime Text 2 and TextMate have markdown bundles in which users can use to view a color formatted version of the files. If you do not have one of these text editors you can always use your OS default text editor to open the MarkDown files or use a browser to view the HTML files.

Introduction
-------

Tips
-------

Prerequisites
-------

### Setup

In addition, I would recommend the user has the following

* Eclipse IDE for J2EE (not required, but helpful to browse code and write JUnit tests). I use Eclipse Juno but older versions should work.
* Maven2Eclipse Plugin for Eclipse (not required, but much easier to import Maven projects into Eclipse)
* Maven version 3 or higher (required to build shaded executable jars)

### Environment

Internet connectivity will be required to build the Maven projects as Maven will automatically download any dependencies specified in the POM file. 

### Knowledge
Students should have some unix knowledge (basic commands like cd, ls, mkdir, etc). It is helpful but not required to know Java and SQL.

### Set the properties

Make sure to set all of the variables in the tutorial.properties file found in the exercises directory. These properties will be picked up by the all of the jar files in the exercises.

For example:

### To Download Eclipse

* Navigate your web browser to http://www.eclipse.org/downloads/
* Download the version that corresponds with your system. I use the Eclipse IDE for Java EE Developers
* Installation instructions may vary per your OS. For Linux and Mac OS X the download will include an executable called eclipse that you can run directly from the download folder (or if you would prefer you can move the download into an applications directory). 

### To install the M2E Plugin

* Eclipse -> Help -> Install New Software
* You will see the following page
* Select add next to the work with box
* Name: Whatever you would like to name the plugin 
* Location: http://download.eclipse.org/technology/m2e/releases
* Select OK.
* Check the box that comes up, and press next.
* Continue to press next, accept agreement, etc until the plugin is installed.

### To import the project in Eclipse with the M2E Plugin

* File -> Import
* Maven -> Existing Maven Project
* Browse
* Find github directory on your computer -> open
* Select all projects -> finish
* Pat yourself on the back, all of the projects are imported

Sample Data
-------