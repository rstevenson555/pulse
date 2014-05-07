package com.bos.art.logParser.server;

import com.bos.art.logParser.collector.LiveLogUnloader;
import com.bos.art.logParser.db.ConnectionPoolT;
import com.bos.art.logServer.main.Collector;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

import org.apache.commons.dbcp2.*;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.yaml.snakeyaml.Yaml;

import javax.sql.DataSource;

public class Engine {

    //private static final int SERVER_PORT = 5557;
    private static final Logger logger = (Logger) Logger.getLogger(Engine.class.getName());
    private static String DB_URL;
    private static String DBDriverName;
    private static final int MAX_ACTIVE_CONNECTIONS = 400;
    private static final int MAX_POOL_BLOCK = 5000;
    public static String JAVA_GROUPS_GOSSIP_SERVER = "";
    public static String JAVA_GROUPS_ROUTER_SERVER = "";
    public static int JAVA_GROUPS_ROUTER_SERVER_PORT = 0;
    private static final String STRESSENV_HOSTNAME_MATCH = "stress-";
    private static final String PRODENV_HOSTNAME_MATCH = "prod-";
    private static final String PRODCONFIG = "prodconfig";
    private static final String STRESSCONFIG = "stressconfig";
    private static final String LOCALCONFIG = "localconfig";
    public static int ART_ENGINE_PORT=0;
    public static String ART_ENGINE_MACHINE="";
    private static int SOCKET_BUFFER = 262144;


    static public void init() {
        String gossipServer;
        String routerServer;

        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        InputStream in = null;
        if (System.getProperty("CONFIG_OVERRIDE")!=null) {
            try {
                in = new FileInputStream(System.getProperty("CONFIG_OVERRIDE"));
            } catch (FileNotFoundException ex) {
                logger.error("Collector.init",ex);
            }
        } else {
            in = loader.getResourceAsStream("solutions_collector_config.yml");
        }

        InputStream logging = null;
        if (System.getProperty("LOG4J_OVERRIDE")!=null) {
            try {
                logging = new FileInputStream(System.getProperty("LOG4J_OVERRIDE"));
            } catch (FileNotFoundException ex) {
                logger.error("Collector.init",ex);
            }
        } else {
            logging = loader.getResourceAsStream("solutions_log4j.properties");
        }
        Properties properties = new Properties();
        try {
            properties.load(logging);
            PropertyConfigurator.configure(properties);

        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(Collector.class.getName()).log(Level.SEVERE, null, ex);
        }

        Yaml yconfig = new Yaml();
        Map allConfig = (Map) yconfig.load(in);

        Map config = null;
        String hostname = "";
        try {
            hostname = java.net.InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException ex) {
            logger.error("Collector.loadConfig", ex);
        }

        if (hostname.indexOf(STRESSENV_HOSTNAME_MATCH) != -1) {
            config = (Map) allConfig.get(STRESSCONFIG);
            logger.info("Collector Loading the stressconfig");
        } else if (hostname.indexOf(PRODENV_HOSTNAME_MATCH) != -1) {
            logger.info("Collector Loading the prodconfig");
            config = (Map) allConfig.get(PRODCONFIG);
        } else {
            // set the default configuration to use based on hostname
            config = (Map) allConfig.get(LOCALCONFIG);
            logger.info("Collector Loading the localconfig");
        }

        //logger.info("Collector configuration:\n");
        //logger.info(config);

        Map dbsettings = (Map) config.get("db");

        DBDriverName = (String)dbsettings.get("drivername");
        DB_URL = (String)dbsettings.get("driver") + "://" + (String)dbsettings.get("host") + ":"
                + dbsettings.get("port") + "/" + dbsettings.get("instance") + "?user="
                + dbsettings.get("login") + "&password=" + dbsettings.get("pwd")
                + "&sendBufferSize=" +SOCKET_BUFFER + "&receiveBufferSize=" +SOCKET_BUFFER
                + "&recvBufferSize=" + SOCKET_BUFFER;

        Map javagroups = (Map) config.get("javagroups");
        gossipServer = (String) javagroups.get("gossipserver");
        routerServer = (String) javagroups.get("routerserver");
        Integer routerPort = Integer.valueOf( (Integer)javagroups.get("port"));
        JAVA_GROUPS_GOSSIP_SERVER = gossipServer;
        JAVA_GROUPS_ROUTER_SERVER = routerServer;
        JAVA_GROUPS_ROUTER_SERVER_PORT = routerPort;

        Map engine = (Map)config.get("engine");
        Integer port = (Integer)engine.get("port");
        String host = (String)engine.get("machine");

        ART_ENGINE_PORT = port;
        ART_ENGINE_MACHINE = host;
    }

    public static void run(String args[]) {

        int connections = 1;
        BasicThreadFactory tFactory = new BasicThreadFactory.Builder()
                .namingPattern("Engine Client Handler-%d")
                .build();

        ExecutorService pool = Executors.newCachedThreadPool(tFactory);

        try {

            LiveLogUnloader.startHeapThread();
            LiveLogUnloader.startDBThread();
            LiveLogUnloader.startSystemTaskThread();
            ServerSocket server = new ServerSocket();
            SocketAddress localSocketAddress = new InetSocketAddress(ART_ENGINE_PORT);
            server.setReceiveBufferSize(SOCKET_BUFFER);
            server.bind(localSocketAddress);

            while(true) {
                pool.execute(new EngineClientHandler(server.accept(), connections++));
            }

        } catch (Exception e) {
            pool.shutdown();
            logger.error("Engine.init pool", e);
        }
    }

    public static void main(String args[]) {
        Engine.init();
        Engine.run(args);

//        Engine.init();
//        Engine.initializeDatabaseConnectionPooling();
    }

    public static void initializeDatabaseConnectionPooling() {

        //
        // First we load the underlying JDBC driver.
        // You need this if you don't use the jdbc.drivers
        // system property.
        //
        logger.debug("Loading underlying JDBC driver.");
        try {
            Class.forName(DBDriverName).newInstance();
        } catch (ClassNotFoundException e) {
            logger.error("Engine.init DB", e);
        } catch (Throwable ei) {
            logger.error("Engine.init DB", ei);
        }
        logger.debug("Done.");
        //
        // Then we set up and register the PoolingDriver.
        // Normally this would be handled auto-magically by
        // an external configuration, but in this example we'll
        // do it manually.
        //
        logger.debug("Setting up driver : " + DB_URL);
        setupDriver(DB_URL);
        logger.debug("Done.");

    }


    public static void setupDriver(String connectURI) {

        Properties props = new Properties();

        props.put("logAbandoned", "true");
        props.put("poolPreparedStatements","true");
        props.put("maxOpenPreparedStatements", "200");
        props.put("removeAbandoned", "true");
        props.put("sendBufferSize",String.valueOf(SOCKET_BUFFER));
        props.put("receiveBufferSize",String.valueOf(SOCKET_BUFFER));
        props.put("recvBufferSize",String.valueOf(SOCKET_BUFFER));

        BasicDataSource ds = new BasicDataSource();
        ds.setLogAbandoned(true);
        ds.setMaxTotal(MAX_ACTIVE_CONNECTIONS);
        ds.setPoolPreparedStatements(true);
        ds.setDriverClassName(DBDriverName);
        ds.setUrl(connectURI);
        //ds.setAbandonedUsageTracking(true);
        ds.setRemoveAbandonedOnMaintenance(true);
        ds.setMaxOpenPreparedStatements(200);
        //ds.setUsername(dbsettings.get("login"));

        dataSource = ds;

    }

    private static DataSource dataSource;

    public static DataSource getDataSource() {
        return dataSource;
    }

    public static void init(String args[]) {
        main(args);
    }

    public static void start() {
    }

    public static void stop() {
    }

    public static void destroy() {
    }
}
