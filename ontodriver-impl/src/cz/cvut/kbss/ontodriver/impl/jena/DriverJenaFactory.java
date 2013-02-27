package cz.cvut.kbss.ontodriver.impl.jena;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import cz.cvut.kbss.jopa.model.metamodel.Metamodel;
import cz.cvut.kbss.ontodriver.Context;
import cz.cvut.kbss.ontodriver.DriverAbstractFactory;
import cz.cvut.kbss.ontodriver.OntologyConnectorType;
import cz.cvut.kbss.ontodriver.OntologyStorageProperties;
import cz.cvut.kbss.ontodriver.PersistenceProviderFacade;
import cz.cvut.kbss.ontodriver.StorageModule;
import cz.cvut.kbss.ontodriver.exceptions.OntoDriverException;
import cz.cvut.kbss.ontodriver.impl.OntoDriverImpl;
import cz.cvut.kbss.ontodriver.impl.owlapi.DriverOwlapiFactory;

public class DriverJenaFactory extends DriverAbstractFactory {

	static {
		try {
			OntoDriverImpl
					.registerFactoryClass(OntologyConnectorType.JENA, DriverJenaFactory.class);
		} catch (OntoDriverException e) {
			LOG.severe("Unable to register " + DriverOwlapiFactory.class
					+ " at the driver. Message: " + e.getMessage());
		}
	}

	public DriverJenaFactory(List<OntologyStorageProperties> storageProperties,
			Map<String, String> properties, PersistenceProviderFacade persistenceProvider)
			throws OntoDriverException {
		super(storageProperties, properties, persistenceProvider);
	}

	@Override
	public StorageModule createStorageModule(Context ctx, Metamodel metamodel, boolean autoCommit)
			throws OntoDriverException {
		ensureState(ctx, metamodel);
		if (LOG.isLoggable(Level.FINER)) {
			LOG.finer("Creating Jena storage module.");
		}
		final JenaStorageModule m = new JenaStorageModule(ctx, metamodel, this);
		registerModule(m);
		return m;
	}

	@Override
	public JenaStorageConnector createStorageConnector(Context ctx, boolean autoCommit)
			throws OntoDriverException {
		ensureState(ctx);
		if (LOG.isLoggable(Level.FINER)) {
			LOG.finer("Creating Jena storage connector.");
		}
		final JenaStorageConnector c = new JenaStorageConnector(contextsToProperties.get(ctx));
		registerConnector(c);
		return c;
	}
}
