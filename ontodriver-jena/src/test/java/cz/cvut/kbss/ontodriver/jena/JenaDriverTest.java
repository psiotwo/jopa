package cz.cvut.kbss.ontodriver.jena;

import cz.cvut.kbss.ontodriver.OntologyStorageProperties;
import cz.cvut.kbss.ontodriver.PreparedStatement;
import cz.cvut.kbss.ontodriver.ResultSet;
import cz.cvut.kbss.ontodriver.config.OntoDriverProperties;
import cz.cvut.kbss.ontodriver.jena.config.JenaOntoDriverProperties;
import cz.cvut.kbss.ontodriver.jena.connector.ConnectorFactory;
import cz.cvut.kbss.ontodriver.jena.connector.InferenceConnectorFactory;
import cz.cvut.kbss.ontodriver.jena.connector.ReadCommittedConnectorFactory;
import cz.cvut.kbss.ontodriver.jena.connector.SnapshotConnectorFactory;
import cz.cvut.kbss.ontodriver.jena.environment.Generator;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.reasoner.rulesys.RDFSRuleReasonerFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.RDF;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URI;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.apache.jena.rdf.model.ResourceFactory.createResource;
import static org.apache.jena.rdf.model.ResourceFactory.createStatement;
import static org.junit.Assert.*;

public class JenaDriverTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private OntologyStorageProperties storageProps;
    private final Map<String, String> properties = new HashMap<>();

    private JenaDriver driver;

    @Before
    public void setUp() {
        this.storageProps = OntologyStorageProperties.driver(JenaDataSource.class.getName())
                                                     .physicalUri(URI.create("temp:memory")).build();
        properties.put(JenaOntoDriverProperties.JENA_STORAGE_TYPE, JenaOntoDriverProperties.IN_MEMORY);
        properties.put(JenaOntoDriverProperties.JENA_ISOLATION_STRATEGY, JenaOntoDriverProperties.READ_COMMITTED);
    }

    @After
    public void tearDown() throws Exception {
        if (driver != null && driver.isOpen()) {
            driver.close();
        }
    }

    @Test
    public void initCreatesReadCommittedConnectorFactoryWhenConfigured() throws Exception {
        this.driver = new JenaDriver(storageProps, properties);
        assertNotNull(driver);
        assertTrue(driver.isOpen());
        assertTrue(getConnectorFactory() instanceof ReadCommittedConnectorFactory);
    }

    private ConnectorFactory getConnectorFactory() throws Exception {
        final Field factoryField = JenaDriver.class.getDeclaredField("connectorFactory");
        factoryField.setAccessible(true);
        return (ConnectorFactory) factoryField.get(driver);
    }

    @Test
    public void initCreatesSnapshotConnectorFactoryWhenConfigured() throws Exception {
        properties.put(JenaOntoDriverProperties.JENA_ISOLATION_STRATEGY, JenaOntoDriverProperties.SNAPSHOT);
        this.driver = new JenaDriver(storageProps, properties);
        assertNotNull(driver);
        assertTrue(driver.isOpen());
        assertTrue(getConnectorFactory() instanceof SnapshotConnectorFactory);
    }

    @Test
    public void initCreatesInferenceConnectorFactoryWhenReasonerFactoryIsConfigured() throws Exception {
        properties.put(OntoDriverProperties.REASONER_FACTORY_CLASS, RDFSRuleReasonerFactory.class.getName());
        this.driver = new JenaDriver(storageProps, properties);
        assertNotNull(driver);
        assertTrue(driver.isOpen());
        assertTrue(getConnectorFactory() instanceof InferenceConnectorFactory);
    }

    @Test
    public void acquireConnectionCreatesAndReturnsConnectionInstance() {
        this.driver = new JenaDriver(storageProps, properties);
        final JenaConnection connection = driver.acquireConnection();
        assertNotNull(connection);
        assertTrue(connection.isOpen());
    }

    @Test
    public void closeClosesAllOpenConnectionsAndConnectorFactory() throws Exception {
        this.driver = new JenaDriver(storageProps, properties);
        final JenaConnection connection = driver.acquireConnection();
        assertTrue(connection.isOpen());
        assertTrue(driver.isOpen());
        driver.close();
        assertFalse(driver.isOpen());
        assertFalse(connection.isOpen());
        assertFalse(getConnectorFactory().isOpen());
    }

    @Test
    public void closingDriverTwiceDoesNothing() throws Exception {
        this.driver = new JenaDriver(storageProps, properties);
        driver.acquireConnection();
        driver.close();
        driver.close();
        assertFalse(driver.isOpen());
    }

    @Test
    public void connectionClosedNotificationRemovesConnectionFromCollectionOfOpenConnections() throws Exception {
        this.driver = new JenaDriver(storageProps, properties);
        final JenaConnection connection = driver.acquireConnection();
        final Field openConnectionsField = JenaDriver.class.getDeclaredField("openConnections");
        openConnectionsField.setAccessible(true);
        final Set openConnections = (Set) openConnectionsField.get(driver);
        assertTrue(openConnections.contains(connection));
        driver.connectionClosed(connection);
        assertFalse(openConnections.contains(connection));
    }

    @Test
    public void driverRegistersItselfAsListenerOnCreatedConnection() throws Exception {
        this.driver = new JenaDriver(storageProps, properties);
        final JenaConnection connection = driver.acquireConnection();
        final Field listenerField = JenaConnection.class.getDeclaredField("listener");
        listenerField.setAccessible(true);
        assertEquals(driver, listenerField.get(connection));
    }

    @Test
    public void driverCreatesAutoCommitConnectionsWhenConfiguredTo() {
        properties.put(OntoDriverProperties.CONNECTION_AUTO_COMMIT, Boolean.TRUE.toString());
        this.driver = new JenaDriver(storageProps, properties);
        final JenaConnection connection = driver.acquireConnection();
        assertTrue(connection.isAutoCommit());
    }

    @Test
    public void reloadStorageReloadsUnderlyingStorage() throws Exception {
        properties.put(JenaOntoDriverProperties.JENA_STORAGE_TYPE, JenaOntoDriverProperties.FILE);
        final File storage = Files.createTempFile("jena-driver-test", ".ttl").toFile();
        storage.deleteOnExit();
        this.storageProps = OntologyStorageProperties.driver(JenaDataSource.class.getName())
                                                     .physicalUri(URI.create(storage.getAbsolutePath())).build();
        this.driver = new JenaDriver(storageProps, properties);

        final JenaConnection con = driver.acquireConnection();
        final PreparedStatement statement = con.prepareStatement("SELECT * WHERE { ?x ?y ?z .}");
        final ResultSet before = statement.executeQuery();
        assertFalse(before.hasNext());
        before.close();
        generateData(storage);

        driver.reloadStorage();
        final ResultSet after = statement.executeQuery();
        assertTrue(after.hasNext());
        after.close();
    }

    private void generateData(File storage) throws IOException {
        final Model model = RDFDataMgr.loadModel(storage.getAbsolutePath());
        model.add(createStatement(createResource(Generator.generateUri().toString()), RDF.type,
                createResource(Generator.generateUri().toString())));
        try (final BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(storage))) {
            RDFDataMgr.write(out, model, Lang.TTL);
        }
    }

    @Test
    public void acquireConnectionThrowsIllegalStateExceptionWhenDriverIsClosed() throws Exception {
        this.driver = new JenaDriver(storageProps, properties);
        driver.close();

        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Driver is closed.");
        driver.acquireConnection();
    }
}