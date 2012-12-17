package org.srtmplugin.osm.osmosis;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipFile;
import org.openstreetmap.osmosis.core.container.v0_6.BoundContainer;
import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.container.v0_6.EntityProcessor;
import org.openstreetmap.osmosis.core.container.v0_6.NodeContainer;
import org.openstreetmap.osmosis.core.container.v0_6.RelationContainer;
import org.openstreetmap.osmosis.core.container.v0_6.WayContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.CommonEntityData;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;
import org.openstreetmap.osmosis.core.task.v0_6.SinkSource;

/**
 * main class which implements all necessary methods for
 * srtm downloading, parsing and adding
 * 
 * @author Dominik Paluch
 * @modified Robert Greil
 */
public class SrtmPlugin_task implements SinkSource, EntityProcessor {

    private static final Logger log = Logger.getLogger(SrtmPlugin_task.class.getName());
    private String tagName = "height";
    private Sink sink;
    private File localDir = new File("./");
    private String srtm_base_url = "";
    private List<String> srtm_sub_dirs;
    private boolean tmpActivated = false;
    private boolean localOnly = false;
    private boolean replaceExistingTags = true;
    private Map<File, SoftReference<BufferedInputStream>> srtmMap = new HashMap<>();
    private Map<String, Integer> map_failed_srtm = new HashMap<>();

    /**
     * Constructor <br>
     * 
     * @param srtm_base_url base URL of server
     * @param srtm_sub_dirs subdirectories seperated by semicolon
     * @param localDir local directory for downloading of srtm files
     * @param tmp is @localDir the tempdirectory? true/false
     * @param localOnly should only local available files be used? true/false
     * @param replaceExistingTags replace existing height tags? true/false
     */
    public SrtmPlugin_task(final String srtm_base_url, final List<String> srtm_sub_dirs, final File localDir, 
            final boolean tmp, final boolean localOnly, final boolean replaceExistingTags, String tagName) {
        if (!localDir.exists()) {
            if (!localDir.mkdirs()) {
                throw new IllegalArgumentException("Can not create directory " + localDir.getAbsolutePath());
            }
        }
        if (!localDir.isDirectory()) {
            throw new IllegalArgumentException("Not a directory " + localDir.getAbsolutePath());
        }

        this.srtm_base_url = srtm_base_url;
        this.srtm_sub_dirs = srtm_sub_dirs;
        this.localDir = localDir;
        tmpActivated = tmp;
        this.localOnly = localOnly;
        this.replaceExistingTags = replaceExistingTags;
        this.tagName = tagName;
    }

    /**
     * Constructor2 <br>
     * srtm_base_url and srtm_sub_dirs are directly read from the srtmservers.properties
     * file without any possible interception
     * 
     * @param localDir local directory for downloading of srtm files
     * @param tmp is localDir the tempdirectory? true/false
     * @param localOnly should only local available files be used? true/false
     * @param replaceExistingTags replace existing height tags? true/false
     */
    public SrtmPlugin_task(final File localDir, final boolean tmp, final boolean localOnly, final boolean replaceExistingTags) {
        if (!localDir.exists()) {
            if (!localDir.mkdirs()) {
                throw new IllegalArgumentException("Can not create directory " + localDir.getAbsolutePath());
            }
        }
        if (!localDir.isDirectory()) {
            throw new IllegalArgumentException("Not a directory " + localDir.getAbsolutePath());
        }

        SrtmPlugin_factory f = new SrtmPlugin_factory();
        f.readServerProperties();
        this.srtm_base_url = f.getDefaultServerBase();
        this.srtm_sub_dirs = f.getDefaultServerSubDirs();

        this.localDir = localDir;
        tmpActivated = tmp;
        this.localOnly = localOnly;
        this.replaceExistingTags = replaceExistingTags;
    }

    @Override
    public void process(EntityContainer entityContainer) {
        entityContainer.process(this);
    }

    @Override
    public void process(BoundContainer boundContainer) {
        sink.process(boundContainer);
    }

    @Override
    public void process(NodeContainer container) {
        //backup existing node entity
        Node node = container.getEntity();
        //backup lat and lon of node entity
        double lat = node.getLatitude();
        double lon = node.getLongitude();
        //try to get srtm height
        Double srtmHeight = new Double(srtmHeight(lat, lon));

        //look for existing height tag
        Collection<Tag> tags = node.getTags();
        Tag pbf_tag = null;
        for (Tag tag : tags) {
            if (tag.getKey().equalsIgnoreCase(tagName)) {
                pbf_tag = tag;
                break;
            }
        }

        //work with possible existing height tag
        //check if it should be replaced or not
        boolean addHeight = true;
        if (pbf_tag != null) {
            if (srtmHeight.isNaN()) {
                addHeight = false;
            } else {
                if (replaceExistingTags) {
                    tags.remove(pbf_tag);
                } else {
                    addHeight = false;
                }
            }
        }

        //add new srtm height tag
        if (addHeight) {
            tags.add(new Tag(tagName, srtmHeight.toString()));
        }

        //create new node entity with new srtm height tag
        CommonEntityData ced = new CommonEntityData(
                node.getId(),
                node.getVersion(),
                node.getTimestamp(),
                node.getUser(),
                node.getChangesetId(),
                tags);

        //distribute the new nodecontainer to the following sink
        sink.process(new NodeContainer(new Node(ced, lat, lon)));
    }

    @Override
    public void process(WayContainer container) {
        sink.process(container);
    }

    @Override
    public void process(RelationContainer container) {
        sink.process(container);
    }

    @Override
    public void complete() {
        sink.complete();
    }

    @Override
    public void release() {
        sink.release();
    }

    @Override
    public void setSink(Sink sink) {
        this.sink = sink;
    }
    
    @Override
    public void initialize(Map<String, Object> metaData) {
    	// added in osmosis 0.41
    }

    /**
     * Determine the filename of the srtm file
     * corresponding to the lat and lon coordinates
     * of the actual node
     * @param lat latitue
     * @param lon longitude
     * @return srtm height
     */
    private double srtmHeight(double lat, double lon) {
        int nlat = Math.abs((int) Math.floor(lat));
        int nlon = Math.abs((int) Math.floor(lon));
        double val;
        String ID_file = "";
        try {
            NumberFormat nf = NumberFormat.getInstance();
            String NS, WE;
            String f_nlat, f_nlon;

            if (lat > 0) {
                NS = "N";
            } else {
                NS = "S";
            }
            if (lon > 0) {
                WE = "E";
            } else {
                WE = "W";
            }

            nf.setMinimumIntegerDigits(2);
            f_nlat = nf.format(nlat);
            nf.setMinimumIntegerDigits(3);
            f_nlon = nf.format(nlon);

            File file = new File(NS + f_nlat + WE + f_nlon + ".hgt");

            ID_file = file.getName();
            if (map_failed_srtm.containsKey(ID_file)) {
//                log.fine("STRM file " + ID_file + " already blacklisted, Returning height: 0.0");
                return Double.NaN;
            }
            double ilat = getILat(lat);
            double ilon = getILon(lon);
            int rowmin = (int) Math.floor(ilon);
            int colmin = (int) Math.floor(ilat);
            double[] values = new double[4];
            values[0] = getValues(file, rowmin, colmin);
            values[1] = getValues(file, rowmin + 1, colmin);
            values[2] = getValues(file, rowmin, colmin + 1);
            values[3] = getValues(file, rowmin + 1, colmin + 1);
            double coefrowmin = rowmin + 1 - ilon;
            double coefcolmin = colmin + 1 - ilat;
            double val1 = values[0] * coefrowmin + values[1] * (1 - coefrowmin);
            double val2 = values[2] * coefrowmin + values[3] * (1 - coefrowmin);
            val = val1 * coefcolmin + val2 * (1 - coefcolmin);
        } catch (Exception e) {
            log.log(Level.SEVERE, "Invalid height format detected in {0} for lat={1} | lon={2}\n returning Double.NaN", new Object[]{ID_file, lat, lon});
            return Double.NaN;
        }
        return val;
    }

    private static double getILat(double lat) {
        double dlat = lat - Math.floor(lat);
        double ilat = dlat * 1200;
        return ilat;
    }

    private static double getILon(double lon) {
        double dlon = lon - Math.floor(lon);
        double ilon = dlon * 1200;
        return ilon;
    }

    private short readShort(BufferedInputStream in) throws IOException {
        int ch1 = in.read();
        int ch2 = in.read();
        return (short) ((ch1 << 8) + (ch2));
    }

    private double getValues(File file, int rowmin, int colmin) throws MalformedURLException, FileNotFoundException, IOException {
        file = new File(localDir, file.getName());
        boolean ex1 = false;
        URL exUrl = new URL("http://127.0.0.1/");
        if (!file.exists()) {
            String ID_file = file.getName();
            //if srtm filename is already blacklisted
            //return height 0
            if (map_failed_srtm.containsKey(ID_file)) {
//                log.fine("SRTM file " + ID_file + " already blacklisted, Returning height: Double.NaN");
                return Double.NaN;
            }
            if (!localOnly) {
                log.log(Level.FINE, "Local SRTM file ''{0}'' not found. Trying to uncompress.", file.getName());
            }
            File zipped = new File(localDir, file.getName() + ".zip");

            String subDir = "";
            if (!localOnly) {
                if (!zipped.exists()) {
                    log.log(Level.FINE, "Local zipped SRTM file ''{0}.zip'' not found. Trying to download from server.", file.getName());
                    for (String srv_subdir : srtm_sub_dirs) {
                        String url_rm_file = srtm_base_url + srv_subdir + file.getName() + ".zip";
                        exUrl = new URL(url_rm_file);
                        if (urlExist(exUrl)) {
                            ex1 = true;
                            subDir = srv_subdir;
                            break;
                        }
                    }
                    //if zipped srtm file cannot be found at any subdirectory
                    //return height Double.NaN
                    if (!ex1) {
                        log.log(Level.FINE, "Remote zipped SRTM file ''{0}.zip'' not found. Returning no height", file.getName());
                        map_failed_srtm.put(file.getName(), 1);
                        return Double.NaN;
                    }
                }
            } else {
                //if local files only is true and no matching srtm file is found
                //return height Double.NaN;
                return Double.NaN;
            }

            ZipFile zipfile;
            File srtmzip = null;
            if (ex1) {
                log.log(Level.FINE, "Remote zipped SRTM file ''{0}.zip'' found in server subdir ''{1}''. Downloading...", new Object[]{file.getName(), subDir});
                BufferedOutputStream outp;
                try (InputStream inp = new BufferedInputStream(exUrl.openStream())) {
                    srtmzip = File.createTempFile(file.getName(), ".zip", localDir);
                    outp = new BufferedOutputStream(new FileOutputStream(srtmzip), 1024);
                    copyInputStream(inp, outp);
                }
                outp.close();
                zipfile = new ZipFile(srtmzip, ZipFile.OPEN_READ);
            } else {
                zipfile = new ZipFile(zipped, ZipFile.OPEN_READ);
            }
            InputStream inp = zipfile.getInputStream(zipfile.getEntry(file.getName()));
            BufferedOutputStream outp = new BufferedOutputStream(new FileOutputStream(file), 1024);

            copyInputStream(inp, outp);
            outp.flush();

            if (srtmzip != null) {
                srtmzip.deleteOnExit();
            }
            if (tmpActivated) {
                srtmzip.deleteOnExit();
                file.deleteOnExit();
            }
            log.log(Level.FINE, "Uncompressed zipped SRTM file ''{0}.zip'' to ''{1}''.", new Object[]{zipped.getName(), file.getName()});

        }

        //if file can not be succesfully downloaded
        //or any other error occurs which prevents the 
        //normal use of the srtm file
        //return height Double.NaN;
        if (!file.exists()) {
            return Double.NaN;
        }

        SoftReference<BufferedInputStream> inRef = srtmMap.get(file);
        BufferedInputStream in = (inRef != null) ? inRef.get() : null;
        if (in == null) {
            int srtmbuffer = 4 * 1024 * 1024;
            in = new BufferedInputStream(new FileInputStream(file), srtmbuffer);
            srtmMap.put(file, new SoftReference<>(in));
            in.mark(srtmbuffer);
        }
        in.reset();

        long starti = ((1200 - colmin) * 2402) + rowmin * 2;
        in.skip(starti);
        short readShort = readShort(in);
        return readShort;
    }

    private static void copyInputStream(InputStream in, BufferedOutputStream out) throws IOException {
        byte[] buffer = new byte[1024 * 1024];
        int len = in.read(buffer);
        while (len >= 0) {
            out.write(buffer, 0, len);
            len = in.read(buffer);
        }
        in.close();
        out.close();
    }

    /**
     * checks a given URL for availbility
     * @param urlN given URL
     * @return true if URL exists otherwise false
     */
    private static boolean urlExist(URL urlN) {
        try {
            HttpURLConnection.setFollowRedirects(false);
            HttpURLConnection con = (HttpURLConnection) urlN.openConnection();
            con.setRequestMethod("HEAD");
            return (con.getResponseCode() == HttpURLConnection.HTTP_OK);
        } catch (Exception e) {
            log.log(Level.SEVERE, "Invalid server URL found: {0}", urlN);
            return false;
        }
    }
}
