# Examples #
Below are some examples on how to use the command line arguments. Example 4 shows the xml code of some nodes before and after the use of this plugin.

## Example 1 ##
```
osmosis.bat --read-xml test.osm --write-srtm <additional commands> --write-xml test_with_srtm.osm
```

## Result ##
The SRTMPlugin uses the default values of all commands.

## Example 2 ##
```
osmosis.bat --read-xml test.osm --write-srtm locDir=C:\Users\Test\Desktop\SRTM locOnly=true repExisting=false --write-xml test_with_srtm.osm
```

## Result 2 ##
  * `locDir=C:\Users\Test\Desktop\SRTM` local directory for all SRTM files
  * `locOnly=true` forces the plugin to work only with already existing SRTM files inside the **locDir** directory
  * `repExisting=false` does not allow the replacement of nodes already tagged with some kind of height information

## Example 3 ##
```
osmosis.bat --read-xml test.osm --write-srtm srvBase=http://any.srtm.server.url.here srvSubDirs=America;Africa;Eurasia;Australia --write-xml test_with_srtm.osm
```

## Result 3 ##
  * `srvBase=http://any.srtm.server.url.here` as base URL for any available SRTM server
  * `srvSubDirs=America;Africa;Eurasia;Australia` forces the plugin to look only in the subdirectories of America, Africa, Eurasia and Australia for matching SRTM files while all other subdirectories are being ignored

## Example 4 ##
Before:
```
<node id="410720" version="7" timestamp="2009-07-12T16:13:22Z" uid="70800" user="Cellpaq" changeset="1808153" lat="47.7828962" lon="11.5540814">
  </node>
  <node id="410721" version="9" timestamp="2011-03-24T20:29:21Z" uid="417684" user="Maler" changeset="7659298" lat="47.7816345" lon="11.5542844">
  </node>
  <node id="410722" version="7" timestamp="2011-03-24T20:29:26Z" uid="417684" user="Maler" changeset="7659298" lat="47.7814866" lon="11.5543847">
  </node>
  <node id="410723" version="5" timestamp="2009-07-12T16:13:22Z" uid="70800" user="Cellpaq" changeset="1808153" lat="47.7810803" lon="11.5547061">
  </node>
  <node id="410724" version="6" timestamp="2011-03-24T21:00:34Z" uid="417684" user="Maler" changeset="7659298" lat="47.7807118" lon="11.554952">
  </node>
```

After:
```
  <node id="410720" version="7" timestamp="2009-07-12T16:13:22Z" uid="70800" user="Cellpaq" changeset="1808153" lat="47.7828962" lon="11.5540814">
    <tag k="height" v="650.1172421248041"/>
  </node>
  <node id="410721" version="9" timestamp="2011-03-24T20:29:21Z" uid="417684" user="Maler" changeset="7659298" lat="47.7816345" lon="11.5542844">
    <tag k="height" v="642.6583463680294"/>
  </node>
  <node id="410722" version="7" timestamp="2011-03-24T20:29:26Z" uid="417684" user="Maler" changeset="7659298" lat="47.7814866" lon="11.5543847">
    <tag k="height" v="641.8766993152096"/>
  </node>
  <node id="410723" version="5" timestamp="2009-07-12T16:13:22Z" uid="70800" user="Cellpaq" changeset="1808153" lat="47.7810803" lon="11.5547061">
    <tag k="height" v="639.0801990207939"/>
  </node>
  <node id="410724" version="6" timestamp="2011-03-24T21:00:34Z" uid="417684" user="Maler" changeset="7659298" lat="47.7807118" lon="11.554952">
    <tag k="height" v="636.9814403840018"/>
  </node>
```