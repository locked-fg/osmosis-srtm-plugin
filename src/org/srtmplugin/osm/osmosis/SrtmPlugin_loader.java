package org.srtmplugin.osm.osmosis;

import java.util.HashMap;
import java.util.Map;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManagerFactory;
import org.openstreetmap.osmosis.core.plugin.PluginLoader;

public class SrtmPlugin_loader implements PluginLoader {

    @Override
    public Map<String, TaskManagerFactory> loadTaskFactories() {
        Map<String, TaskManagerFactory> factoryMap = new HashMap<>();
        SrtmPlugin_factory srtmplugin = new SrtmPlugin_factory();

        factoryMap.put("write-srtm", srtmplugin);

        return factoryMap;
    }
}
