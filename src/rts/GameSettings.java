package rts;

import static rts.CommandLine.ARG_CONFLICT_POLICY;
import static rts.CommandLine.ARG_IS_PARTIALLY_OBSERVABLE;
import static rts.CommandLine.ARG_LAUNCH_MODE;
import static rts.CommandLine.ARG_MAP;
import static rts.CommandLine.ARG_MAX_CYCLES;
import static rts.CommandLine.ARG_PLAYER_1;
import static rts.CommandLine.ARG_PLAYER_2;
import static rts.CommandLine.ARG_RENDER;
import static rts.CommandLine.ARG_SERIALIZATION_TYPE;
import static rts.CommandLine.ARG_SERVER_ADDRESS;
import static rts.CommandLine.ARG_SERVER_PORT;
import static rts.CommandLine.ARG_UTT_VERSION;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import net.sourceforge.argparse4j.inf.Namespace;

public class GameSettings {

    enum LaunchMode {
        STANDALONE,
        GUI,
        SERVER,
        CLIENT
    }

    static final Map<String, Class> AGENTS;
    static final Class DEFAULT_AI = ai.abstraction.WorkerRush.class;
    static final boolean DEFAULT_RENDER = true;
    static final String DEFAULT_SERVER_ADDRESS = "127.0.0.1";
    static final int DEFAULT_SERVER_PORT = 9898;
    static final int DEFAULT_SERIALIZATION_TYPE = 2;
    static final int DEFAULT_MAX_CYCLES = 5000;
    static final boolean DEFAULT_IS_PARTIALLY_OBSERVABLE = false;
    static final int DEFAULT_UTT_VERSION = 2;
    static final int DEFAULT_CONFLICT_POLICY = 1;

    // Networking
    private String serverAddress;
    private int serverPort;
    private LaunchMode launchMode;
    private int serializationType;

    // Maps
    private String mapLocation;

    // Game settings
    private boolean render;
    private int maxCycles;
    private boolean partiallyObservable;
    private int uttVersion;
    private int conflictPolicy;
    
    // Opponents:
    private Class AI1;
    private Class AI2;

    static {
        AGENTS = new HashMap<>();
        AGENTS.put("WorkerRush", ai.abstraction.WorkerRush.class);
        AGENTS.put("LightRush", ai.abstraction.LightRush.class);
        AGENTS.put("HeavyRush", ai.abstraction.HeavyRush.class);
        AGENTS.put("RangedRush", ai.abstraction.RangedRush.class);
        AGENTS.put("LightRushPO", ai.abstraction.partialobservability.POLightRush.class);
        AGENTS.put("InformedNaiveMCTS", ai.mcts.informedmcts.InformedNaiveMCTS.class);
        AGENTS.put("PassiveAI", ai.PassiveAI.class);
        AGENTS.put("RandomAI", ai.RandomAI.class);
        AGENTS.put("RandomBiasedAI", ai.RandomBiasedAI.class);
        AGENTS.put("SocketAI", ai.socket.SocketAI.class);
        AGENTS.put("BasicConfigurableScript", ai.puppet.BasicConfigurableScript.class);
        AGENTS.put("PortfolioAI", ai.portfolio.PortfolioAI.class);
        AGENTS.put("PGSAI", ai.portfolio.portfoliogreedysearch.PGSAI.class);
        AGENTS.put("IDRTMinimax", ai.minimax.RTMiniMax.IDRTMinimax.class);
        AGENTS.put("IDRTMinimaxRandomized", ai.minimax.RTMiniMax.IDRTMinimaxRandomized.class);
        AGENTS.put("IDABCD", ai.minimax.ABCD.IDABCD.class);
        AGENTS.put("MonteCarlo", ai.montecarlo.MonteCarlo.class);
        AGENTS.put("NaiveMCTS", ai.mcts.naivemcts.NaiveMCTS.class);
        AGENTS.put("UCT", ai.mcts.uct.UCT.class);
        AGENTS.put("DownsamplingUCT", ai.mcts.uct.DownsamplingUCT.class);
        AGENTS.put("UCTUnitActions", ai.mcts.uct.UCTUnitActions.class);
        AGENTS.put("AHTNAI", ai.ahtn.AHTNAI.class);
        AGENTS.put("PuppetABCDSingle", ai.puppet.PuppetSearchAB.class);
        AGENTS.put("PuppetMCTSBasic", ai.puppet.PuppetSearchMCTS.class);
    }

    private GameSettings( LaunchMode launchMode, String serverAddress, int serverPort,
                          int serializationType, String mapLocation, int maxCycles,
                          boolean partiallyObservable, int uttVersion, int confictPolicy,
                          Class AI1, Class AI2, boolean render) {
        this.launchMode = launchMode;
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.serializationType = serializationType;
        this.mapLocation = mapLocation;
        this.maxCycles = maxCycles;
        this.partiallyObservable = partiallyObservable;
        this.uttVersion = uttVersion;
        this.conflictPolicy = confictPolicy;
        this.AI1 = AI1;
        this.AI2 = AI2;
        this.render = render;
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public int getServerPort() {
        return serverPort;
    }

    public int getSerializationType() {
        return serializationType;
    }

    public String getMapLocation() {
        return mapLocation;
    }

    public boolean isRender() {
        return render;
    }

    public int getMaxCycles() {
        return maxCycles;
    }

    public boolean isPartiallyObservable() {
        return partiallyObservable;
    }

    public int getUTTVersion() {
        return uttVersion;
    }

    public int getConflictPolicy() {
        return conflictPolicy;
    }

    public LaunchMode getLaunchMode() {
        return launchMode;
    }

    public Class getAI1() {
        return AI1;
    }

    public Class getAI2() {
        return AI2;
    }

    /**
     * Fetches the default configuration file which will be located in the root direction called "config.properties".
     */
    public static Properties fetchDefaultConfig() throws IOException {
        Properties prop = new Properties();
        InputStream is = GameSettings.class.getResourceAsStream("/config.properties");
        if (is == null) is = new FileInputStream("resources/config.properties");
        prop.load(is);
        return prop;
    }

    /**
     * Generates game settings based on the provided configuration file.
     */
    public static GameSettings loadFromConfig(Properties prop) {

        assert !prop.isEmpty();

        String serverAddress = prop.getProperty("server_address");
        int serverPort = readIntegerProperty(prop, "server_port", 9898);
        int serializationType = readIntegerProperty(prop, "serialization_type", 2);
        String mapLocation = prop.getProperty("map_location");
        int maxCycles = readIntegerProperty(prop, "max_cycles", 5000);
        boolean partiallyObservable = Boolean.parseBoolean(prop.getProperty("partially_observable"));
        int uttVersion = readIntegerProperty(prop, "UTT_version", 2);
        int conflictPolicy = readIntegerProperty(prop, "conflict_policy", 1);
        LaunchMode launchMode = LaunchMode.valueOf(prop.getProperty("launch_mode"));
        Class AI1 = null;
        Class AI2 = null;
        try {
            AI1 = Class.forName(prop.getProperty("AI1"));
            AI2 = Class.forName(prop.getProperty("AI2"));
        } catch (ClassNotFoundException e) {
            // do nothing
        }

        return new GameSettings(launchMode, serverAddress, serverPort,
                                serializationType, mapLocation, maxCycles,
                                partiallyObservable, uttVersion, conflictPolicy, 
                                AI1, AI2, true);
    }

    /**
     * Load game settings from command line arguments.
     *
     * @param namespace An argparse4j args container.
     * @return Game settings.
     */
    public static GameSettings loadFromArgs(Namespace namespace) {
        return new GameSettings(
            LaunchMode.valueOf(namespace.getString(ARG_LAUNCH_MODE).toUpperCase()),
            namespace.getString(ARG_SERVER_ADDRESS),
            namespace.getInt(ARG_SERVER_PORT),
            namespace.getInt(ARG_SERIALIZATION_TYPE),
            namespace.getString(ARG_MAP),
            namespace.getInt(ARG_MAX_CYCLES),
            namespace.getBoolean(ARG_IS_PARTIALLY_OBSERVABLE),
            namespace.getInt(ARG_UTT_VERSION),
            namespace.getInt(ARG_CONFLICT_POLICY),
            AGENTS.getOrDefault(namespace.getString(ARG_PLAYER_1), DEFAULT_AI),
            AGENTS.getOrDefault(namespace.getString(ARG_PLAYER_2), DEFAULT_AI),
            namespace.getBoolean(ARG_RENDER)
        );
    }
    
    public static int readIntegerProperty(Properties prop, String name, int defaultValue)
    {
        String stringValue = prop.getProperty(name);
        if (stringValue == null) return defaultValue;
        return Integer.parseInt(stringValue);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("----------Game Settings----------\n");
        sb.append("Running as Server: ").append( getLaunchMode().toString() ).append("\n");
        sb.append("Server Address: ").append( getServerAddress() ).append("\n");
        sb.append("Server Port: ").append( getServerPort() ).append("\n");
        sb.append("Serialization Type: ").append( getSerializationType()).append("\n");
        sb.append("Map Location: ").append( getMapLocation() ).append("\n");
        sb.append("Max Cycles: ").append( getMaxCycles() ).append("\n");
        sb.append("Partially Observable: ").append( isPartiallyObservable() ).append("\n");
        sb.append("Rules Version: ").append(getUTTVersion() ).append("\n");
        sb.append("Conflict Policy: ").append( getConflictPolicy() ).append("\n");
        sb.append("AI1: ").append( getAI1() ).append("\n");
        sb.append("AI2: ").append( getAI2() ).append("\n");
        sb.append("------------------------------------------------");
        return sb.toString();
    }
}
