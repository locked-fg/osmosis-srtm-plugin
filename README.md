# SRTM Plugin for OpenStreetMap's Osmosis 

Data nodes from OpenStreetMap are defined by id, latitude and longitude (plus additional attributes), which is enough for painting 2D maps and standard routing queries.
Modern routing algorithms for example can also take advantage of elevation data
(If for example fuel costs should be taken into account).

As the elevation is not added to OpenStreetMap data directly, there plugins are needed that make use of Nasa SRTM data to obtain the elevation information. This osmosis-srtm-plugin is intended to be used as a plugin for [Osmosis](http://wiki.openstreetmap.org/wiki/Osmosis) to process OSM-input files together with SRTM data in order to add a height tag to each node.

The plugin works out-of-the-box but provides configurable settings to satisfy hopefully all requirements (See [Usage](Usage) or [Examples](Examples)!).

# History
v1.1.0:
 - added possibility to define the tag name (Issue 3)

v1.0.1:
 - compatibility fix with newer osmosis-versions

v1:
 - first release

## Other formats 
As there a quite some different formats of elevation data available, it should be pointed out that this plugin currently only supports NASA SRTM data.
Support for other formats would of course be appreciated.

## Relevant links 
  * [OpenStreetMap](http://www.openstreetmap.org/)
  * [Osmosis](http://wiki.openstreetmap.org/wiki/Osmosis)
  * [NASA SRTM](http://www2.jpl.nasa.gov/srtm/)
  * [plugin page at OpenStreetMap](http://wiki.openstreetmap.org/wiki/Srtm_to_Nodes)

## Used by 
  * [TrafficMining](http://code.google.com/p/trafficmining/)