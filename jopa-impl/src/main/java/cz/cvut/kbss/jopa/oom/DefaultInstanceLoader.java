package cz.cvut.kbss.jopa.oom;

import cz.cvut.kbss.jopa.exceptions.StorageAccessException;
import cz.cvut.kbss.jopa.model.metamodel.EntityType;
import cz.cvut.kbss.jopa.model.metamodel.Metamodel;
import cz.cvut.kbss.jopa.oom.exceptions.EntityReconstructionException;
import cz.cvut.kbss.jopa.sessions.LoadingParameters;
import cz.cvut.kbss.ontodriver.Connection;
import cz.cvut.kbss.ontodriver.descriptor.AxiomDescriptor;
import cz.cvut.kbss.ontodriver.exception.OntoDriverException;
import cz.cvut.kbss.ontodriver.model.Axiom;

import java.util.Collection;

/**
 * Loads entities which do not require polymorphic handling.
 */
class DefaultInstanceLoader extends EntityInstanceLoader {

    DefaultInstanceLoader(Connection storageConnection, Metamodel metamodel, AxiomDescriptorFactory descriptorFactory,
                          EntityConstructor entityBuilder) {
        super(storageConnection, metamodel, descriptorFactory, entityBuilder);
    }

    @Override
    <T> T loadEntity(LoadingParameters<T> loadingParameters) {
        final EntityType<T> et = metamodel.entity(loadingParameters.getEntityType());
        return loadInstance(loadingParameters, et);
    }
}
