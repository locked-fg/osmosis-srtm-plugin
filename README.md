# SRTM Plugin for OpenStreetMap's Osmosis 
Data nodes from OpenStreetMap are defined by id, latitude and longitude (plus additional attributes), which is 
enough for painting 2D maps and standard routing queries. Modern routing algorithms for example can also take 
advantage of elevation data (If for example fuel costs should be taken into account).

As the elevation is not added to OpenStreetMap data directly, there plugins are needed that make use of Nasa 
SRTM data to obtain the elevation information. This osmosis-srtm-plugin is intended to be used as a plugin for 
[Osmosis](http://wiki.openstreetmap.org/wiki/Osmosis) to process OSM-input files together with SRTM data in 
order to add a height tag to each node.

The plugin works out-of-the-box but provides configurable settings to satisfy hopefully all requirements 

## History
    - v1.1.1: moved into suitable package hierarchy
    - v1.1.0: added possibility to define the tag name (Issue 3)
    - v1.0.1: compatibility fix with newer osmosis-versions
    - v1.0.0: first release

## Relevant links 
* [OpenStreetMap](http://www.openstreetmap.org/)
* [Osmosis](http://wiki.openstreetmap.org/wiki/Osmosis)
* [NASA SRTM](http://www2.jpl.nasa.gov/srtm/)
* [plugin page at OpenStreetMap](http://wiki.openstreetmap.org/wiki/Srtm_to_Nodes)

* Used by [TrafficMining](https://github.com/locked-fg/trafficmining)

# Usage
- Download and unpack [Osmosis latest](http://wiki.openstreetmap.org/wiki/Osmosis#Latest_stable_version).
- Check that `lib\default` contains `plexus-classworlds-2.4.jar` If it carries another version number, you possibly have to adjust `bin\osmosis.bat` ~L.38.
- Clone and build the repo or download the binary from [bintray](https://dl.bintray.com/locked-fg/Osmosis-Srtm-Plugin/).
- Create `bin\plugins` and put the build/downloaded jar there

Get an OSM file (for example via export from OpenStreetmap.org) and try it: 
`osmosis.bat --read-xml ... --write-srtm <your args> --write-xml ...`
Afterwards enjoy your osm file with added srtm height tags.

## Restrictions / SRTM Formats
The plugin currently supports only SRTM3 files created by the NGA and reprocessed to Version 2.1 
(http://dds.cr.usgs.gov/srtm/What_are_these.pdf), but any other SRTM files with the same binary format should 
be usable, too. Therefore it is not recommend to overwrite the default values for **srvBase** and **srvSubDirs**.

## Commandline Params
  * `locDir` = Local directory for download and storage of SRTM files (**default**: temp-directory)
  * `srvBase` = Base URL of SRTM server (**default**: have a look at the `srtmservers.properties` inside the jar)
  * `srvSubDirs` = Subdirectories of SRTM server, where to look for the SRTM files (.hgt), separeted by semicolons (**default**: have a look at the `srtmservers.properties` inside the jar)
  * `locOnly` = Prohibit the download of new SRTM files and try to use the files available at **locDir** (**default**: false)
  * `repExisting` = Replace all existing height tags of nodes (**default**: true)
  * `tagName` = The tag name used for the elevation data. (**default**: height due to historical reasons. Yet, the [ele tag should be used](http://wiki.openstreetmap.org/wiki/Key:ele) according to Openstreetmap)


# Examples
**Example 1**
```
osmosis.bat --read-xml test.osm --write-srtm <additional commands> --write-xml test_with_srtm.osm
```

**Result**
The SRTMPlugin uses the default values of all commands.

***

**Example 2**
```
osmosis.bat --read-xml test.osm --write-srtm locDir=C:\Users\Test\Desktop\SRTM locOnly=true repExisting=false --write-xml test_with_srtm.osm
```

**Result:**
  * `locDir=C:\Users\Test\Desktop\SRTM` local directory for all SRTM files
  * `locOnly=true` forces the plugin to work only with already existing SRTM files inside the **locDir** directory
  * `repExisting=false` does not allow the replacement of nodes already tagged with some kind of height information

***

**Example 3**
```
osmosis.bat --read-xml test.osm --write-srtm srvBase=http://any.srtm.server.url.here srvSubDirs=America;Africa;Eurasia;Australia --write-xml test_with_srtm.osm
```

**Result:**
  * `srvBase=http://any.srtm.server.url.here` as base URL for any available SRTM server
  * `srvSubDirs=America;Africa;Eurasia;Australia` forces the plugin to look only in the subdirectories of America, Africa, Eurasia and Australia for matching SRTM files while all other subdirectories are being ignored
