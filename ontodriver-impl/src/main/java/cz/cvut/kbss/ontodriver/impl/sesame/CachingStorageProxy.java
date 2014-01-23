package cz.cvut.kbss.ontodriver.impl.sesame;

import info.aduna.iteration.Iterations;

import java.util.Collection;

import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import cz.cvut.kbss.ontodriver.exceptions.OntoDriverException;

class CachingStorageProxy extends TransparentStorageProxy {

	private Model explicitModel;
	private Model model;

	public CachingStorageProxy(RepositoryConnection conn) throws OntoDriverException {
		super(conn);

		this.init();
	}

	private void init() throws OntoDriverException {
		try {
			this.explicitModel = Iterations.addAll(conn.getStatements(null, null, null, false),
					new LinkedHashModel());
			this.model = Iterations.addAll(conn.getStatements(null, null, null, true),
					new LinkedHashModel());
		} catch (RepositoryException e) {
			throw new OntoDriverException(e);
		}
	}

	@Override
	public Model filter(Resource subject, URI predicate, Value object, boolean includeInferred) {
		ensureOpen();
		if (includeInferred) {
			return model.filter(subject, predicate, object);
		} else {
			return explicitModel.filter(subject, predicate, object);
		}
	}

	@Override
	public void addStatements(Collection<Statement> statements) {
		ensureOpen();
		explicitModel.addAll(statements);
		model.addAll(statements);
	}

	@Override
	public void addStatement(Statement statement) {
		ensureOpen();
		explicitModel.add(statement);
		model.add(statement);
	}

	@Override
	public void removeStatements(Collection<Statement> statements) {
		ensureOpen();
		explicitModel.removeAll(statements);
		model.removeAll(statements);
	}

	@Override
	public boolean contains(URI uri) {
		ensureOpen();
		return explicitModel.contains(uri, null, null) || explicitModel.contains(null, null, uri);
	}
}