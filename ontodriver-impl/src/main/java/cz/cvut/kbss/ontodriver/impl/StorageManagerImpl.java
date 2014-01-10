package cz.cvut.kbss.ontodriver.impl;

import java.lang.reflect.Field;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import cz.cvut.kbss.ontodriver.JopaStatement;
import cz.cvut.kbss.ontodriver.Context;
import cz.cvut.kbss.ontodriver.PersistenceProviderFacade;
import cz.cvut.kbss.ontodriver.ResultSet;
import cz.cvut.kbss.ontodriver.StorageManager;
import cz.cvut.kbss.ontodriver.StorageModule;
import cz.cvut.kbss.ontodriver.exceptions.OntoDriverException;

public class StorageManagerImpl extends StorageManager {

	private static final Logger LOG = Logger.getLogger(StorageManagerImpl.class.getName());

	/** Reference to the driver */
	private final OntoDriverImpl driver;

	/** Contexts sorted by priority */
	private List<Context> contexts;
	/** Contexts mapped by URIs */
	private Map<URI, Context> uriToContext;
	/** Storage modules mapped by their context */
	private Map<Context, StorageModule> modules;
	private Map<Context, StorageModule> modulesWithChanges;

	/**
	 * Constructor
	 * 
	 * @param metamodel
	 *            Metamodel
	 * @param contexts
	 *            List of available contexts
	 */
	public StorageManagerImpl(PersistenceProviderFacade persistenceProvider,
			List<Context> contexts, OntoDriverImpl driver) {
		super(persistenceProvider);
		if (contexts == null) {
			throw new NullPointerException();
		}
		if (contexts.isEmpty()) {
			throw new IllegalArgumentException("Contexts list cannot be empty.");
		}
		if (driver == null) {
			throw new NullPointerException();
		}
		this.contexts = contexts;
		this.modulesWithChanges = new HashMap<Context, StorageModule>();
		this.driver = driver;
		initModules();
	}

	@Override
	public void close() throws OntoDriverException {
		if (LOG.isLoggable(Level.CONFIG)) {
			LOG.config("Closing the storage manager.");
		}
		for (StorageModule m : modules.values()) {
			// Just close the module, any pending changes will be rolled back
			// implicitly
			if (m != null) {
				driver.getFactory(m.getContext()).releaseStorageModule(m);
			}
		}
		super.close();
	}

	@Override
	public void commit() throws OntoDriverException {
		if (LOG.isLoggable(Level.FINE)) {
			LOG.fine("Committing changes.");
		}
		ensureState();
		commitInternal();
	}

	@Override
	public ResultSet executeStatement(JopaStatement statement) throws OntoDriverException {
		if (LOG.isLoggable(Level.FINER)) {
			LOG.finer("Executing statement.");
		}
		ensureState();
		if (statement == null) {
			LOG.severe("Null argument passed: statement");
			throw new NullPointerException();
		}
		final Context c = uriToContext.get(statement.getContext());
		if (c == null) {
			throw new OntoDriverException("Unknown ontology context URI " + statement.getContext());
		}
		final StorageModule m = getModule(c);
		return m.executeStatement(statement);
	}

	@Override
	public boolean contains(Object primaryKey, Context entityContext) throws OntoDriverException {
		if (LOG.isLoggable(Level.FINER)) {
			LOG.finer("Checking whether context " + entityContext
					+ " contains entity with primary key " + primaryKey);
		}
		ensureState();
		if (primaryKey == null || entityContext == null) {
			LOG.severe("Null argument passed: primaryKey = " + primaryKey + ", entityContext = "
					+ entityContext);
			throw new NullPointerException();
		}
		final StorageModule m = getModule(entityContext);
		return m.contains(primaryKey);
	}

	@Override
	public <T> T find(Class<T> cls, Object primaryKey, Context entityContext,
			Map<String, Context> attributeContexts) throws OntoDriverException {
		if (LOG.isLoggable(Level.FINER)) {
			LOG.finer("Retrieving entity with primary key " + primaryKey + " from context "
					+ entityContext);
		}
		ensureState();
		if (cls == null || primaryKey == null || entityContext == null || attributeContexts == null) {
			LOG.severe("Null argument passed: cls = " + cls + ", primaryKey = " + primaryKey
					+ ", entityContext = " + entityContext + ", attributeContexts = "
					+ attributeContexts);
			throw new NullPointerException();
		}
		// NOTE: We cannot handle attribute contexts yet
		checkForContextValidity(entityContext);
		final StorageModule module = getModule(entityContext);
		final T entity = module.find(cls, primaryKey);
		return entity;
	}

	@Override
	public boolean isConsistent(Context context) throws OntoDriverException {
		if (context == null) {
			throw new NullPointerException();
		}
		final StorageModule m = getModule(context);
		return m.isConsistent();
	}

	@Override
	public List<Context> getAvailableContexts() {
		return Collections.unmodifiableList(contexts);
	}

	@Override
	public Map<URI, Context> getContextsByUris() {
		return Collections.unmodifiableMap(uriToContext);
	}

	@Override
	public <T> void loadFieldValue(T entity, Field field, Context context)
			throws OntoDriverException {
		if (entity == null || field == null || context == null) {
			LOG.severe("Null argument passed: entity = " + entity + ", field = " + field
					+ ", context = " + context);
			throw new NullPointerException();
		}
		final StorageModule m = getModule(context);
		m.loadFieldValue(entity, field);
	}

	@Override
	public <T> void merge(Object primaryKey, T entity, Context entityContext,
			Map<String, Context> attributeContexts) throws OntoDriverException {
		if (LOG.isLoggable(Level.FINER)) {
			LOG.finer("Merging entity with primary key " + primaryKey + " into context "
					+ entityContext);
		}
		ensureState();
		if (primaryKey == null || entity == null || entityContext == null
				|| attributeContexts == null) {
			LOG.severe("Null argument passed: primaryKey = " + primaryKey + ", entity = " + entity
					+ ", entityContext = " + entityContext + ", attributeContexts = "
					+ attributeContexts);
			throw new NullPointerException();
		}
		// NOTE: We cannot handle attribute contexts yet
		checkForContextValidity(entityContext);
		final StorageModule module = getModule(entityContext);
		module.merge(primaryKey, entity);
		modulesWithChanges.put(entityContext, module);
	}

	@Override
	public <T> void persist(Object primaryKey, T entity, Context entityContext,
			Map<String, Context> attributeContexts) throws OntoDriverException {
		if (LOG.isLoggable(Level.FINER)) {
			LOG.finer("Persisting entity into context " + entityContext);
		}
		ensureState();
		if (entity == null || entityContext == null || attributeContexts == null) {
			LOG.severe("Null argument passed: entity = " + entity + ", entityContext = "
					+ entityContext + ", attributeContexts = " + attributeContexts);
			throw new NullPointerException();
		}
		// NOTE: We cannot handle attribute contexts yet
		checkForContextValidity(entityContext);
		final StorageModule module = getModule(entityContext);
		module.persist(primaryKey, entity);
		modulesWithChanges.put(entityContext, module);
	}

	@Override
	public void remove(Object primaryKey, Context entityContext) throws OntoDriverException {
		if (LOG.isLoggable(Level.FINER)) {
			LOG.finer("Removing entity with primary key " + primaryKey + " from context "
					+ entityContext);
		}
		ensureState();
		if (primaryKey == null || entityContext == null) {
			LOG.severe("Null argument passed: primaryKey = " + primaryKey + ", entityContext = "
					+ entityContext);
			throw new NullPointerException();
		}
		checkForContextValidity(entityContext);
		StorageModule module = getModule(entityContext);
		module.remove(primaryKey);
		modulesWithChanges.put(entityContext, module);
	}

	@Override
	public void rollback() throws OntoDriverException {
		if (LOG.isLoggable(Level.FINE)) {
			LOG.fine("Rolling back changes.");
		}
		ensureState();
		rollbackInternal();
	}

	private void commitInternal() throws OntoDriverException {
		for (StorageModule module : modulesWithChanges.values()) {
			module.commit();
		}
		modulesWithChanges.clear();
	}

	private void rollbackInternal() throws OntoDriverException {
		for (StorageModule module : modulesWithChanges.values()) {
			module.rollback();
		}
	}

	private void checkForContextValidity(Context ctx) throws OntoDriverException {
		assert ctx != null;
		if (!modules.containsKey(ctx)) {
			throw new OntoDriverException("The context " + ctx
					+ " is not valid within this storage manager.");
		}
	}

	private void initModules() {
		this.uriToContext = new HashMap<URI, Context>(contexts.size());
		this.modules = new HashMap<Context, StorageModule>(contexts.size());
		for (Context ctx : contexts) {
			uriToContext.put(ctx.getUri(), ctx);
			// Modules will be lazily loaded
			modules.put(ctx, null);
		}
	}

	private StorageModule getModule(Context context) throws OntoDriverException {
		StorageModule m = modules.get(context);
		if (m == null) {
			m = driver.getFactory(context).createStorageModule(context, persistenceProvider, false);
			modules.put(context, m);
		}
		return m;
	}
}