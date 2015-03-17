# Usage #
Copy the compiled jar into your osmosis plugin directory. This can be easily located by reading the Osmosis: Detailed Usage site at http://wiki.openstreetmap.org/wiki/Osmosis/Detailed_Usage#Plugin_Tasks.

Then use your common osmosis task commandline with the `--write-srtm <additional-commands>` between the in and output of osm files.

Afterwards enjoy your osm file with added srtm height tags.

# Restrictions #
The plugin currently supports only SRTM3 files created by the NGA and reprocessed to Version 2.1 (http://dds.cr.usgs.gov/srtm/What_are_these.pdf), but any other SRTM files with the same binary format should be usable, too.
Therefore it is not recommend to overwrite the default values for **srvBase** and **srvSubDirs**.

# Commandline Params #
  * `locDir` = Local directory for download and storage of SRTM files (**default**: temp-directory)
  * `srvBase` = Base URL of SRTM server (**default**: have a look at the `srtmservers.properties` inside the jar)
  * `srvSubDirs` = Subdirectories of SRTM server, where to look for the SRTM files (.hgt), separeted by semicolons (**default**: have a look at the `srtmservers.properties` inside the jar)
  * `locOnly` = Prohibit the download of new SRTM files and try to use the files available at **locDir** (**default**: false)
  * `repExisting` = Replace all existing height tags of nodes (**default**: true)
  * `tagName` = The tag name used for the elevation data. (**default**: height due to historical reasons. Yet, the [ele tag should be used](http://wiki.openstreetmap.org/wiki/Key:ele) according to Openstreetmap)

Examples of use cases can be found at the [example wiki page](http://code.google.com/p/osmosis-srtm-plugin/wiki/Examples).