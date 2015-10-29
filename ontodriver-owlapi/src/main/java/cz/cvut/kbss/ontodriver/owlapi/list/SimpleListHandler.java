package cz.cvut.kbss.ontodriver.owlapi.list;

import cz.cvut.kbss.ontodriver.owlapi.OwlapiAdapter;
import cz.cvut.kbss.ontodriver.owlapi.connector.OntologySnapshot;
import cz.cvut.kbss.ontodriver.owlapi.util.MutableAddAxiom;
import cz.cvut.kbss.ontodriver_new.descriptors.ListDescriptor;
import cz.cvut.kbss.ontodriver_new.descriptors.SimpleListDescriptor;
import cz.cvut.kbss.ontodriver_new.descriptors.SimpleListValueDescriptor;
import cz.cvut.kbss.ontodriver_new.model.Assertion;
import cz.cvut.kbss.ontodriver_new.model.AxiomImpl;
import cz.cvut.kbss.ontodriver_new.model.NamedResource;
import cz.cvut.kbss.ontodriver_new.model.Value;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntologyChange;

import java.util.ArrayList;
import java.util.List;

class SimpleListHandler extends ListHandler<SimpleListDescriptor, SimpleListValueDescriptor> {

    SimpleListHandler(OwlapiAdapter adapter, OntologySnapshot snapshot) {
        super(adapter, snapshot);
    }

    @Override
    OwlapiListIterator iterator(ListDescriptor descriptor) {
        if (descriptor.getListProperty().isInferred() || descriptor.getNextNode().isInferred()) {
            return new InferredSimpleListIterator(descriptor, snapshot, axiomAdapter);
        } else {
            return new SimpleListIterator(descriptor, snapshot, axiomAdapter);
        }
    }

    @Override
    List<OWLOntologyChange> createListAxioms(SimpleListValueDescriptor descriptor) {
        final List<OWLOntologyChange> changes = new ArrayList<>(descriptor.getValues().size());
        NamedResource previous = descriptor.getListOwner();
        boolean first = true;
        for (NamedResource item : descriptor.getValues()) {
            final OWLAxiom axiom;
            if (first) {
                axiom = appendNode(previous, descriptor.getListProperty(), item);
                first = false;
            } else {
                axiom = appendNode(previous, descriptor.getNextNode(), item);
            }
            previous = item;
            changes.add(new MutableAddAxiom(ontology, axiom));
        }
        return changes;
    }

    private OWLAxiom appendNode(NamedResource current, Assertion property, NamedResource next) {
        return axiomAdapter.toOwlObjectPropertyAssertionAxiom(new AxiomImpl<>(current, property, new Value<>(next)));
    }

    @Override
    boolean isOrigEmpty(SimpleListValueDescriptor descriptor) {
        final OwlapiListIterator it = iterator(descriptor);
        return !it.hasNext();
    }

    @Override
    void addNewNodes(SimpleListValueDescriptor descriptor, int index, NamedResource lastNode) {
        if (index >= descriptor.getValues().size()) {
            return;
        }
        final List<OWLOntologyChange> changes = new ArrayList<>(descriptor.getValues().size() - index);
        for (; index < descriptor.getValues().size(); index++) {
            final NamedResource next = descriptor.getValues().get(index);
            changes.add(new MutableAddAxiom(ontology, appendNode(lastNode, descriptor.getNextNode(), next)));
            lastNode = next;
        }
        owlapiAdapter.addTransactionalChanges(snapshot.applyChanges(changes));
    }
}