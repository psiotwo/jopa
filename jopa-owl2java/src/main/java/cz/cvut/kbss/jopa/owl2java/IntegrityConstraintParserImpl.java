/**
 * Copyright (C) 2016 Czech Technical University in Prague
 * <p>
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package cz.cvut.kbss.jopa.owl2java;

import cz.cvut.kbss.jopa.ic.api.AtomicSubClassConstraint;
import cz.cvut.kbss.jopa.ic.api.DataDomainConstraint;
import cz.cvut.kbss.jopa.ic.api.DataParticipationConstraint;
import cz.cvut.kbss.jopa.ic.api.DataRangeConstraint;
import cz.cvut.kbss.jopa.ic.api.IntegrityConstraint;
import cz.cvut.kbss.jopa.ic.api.IntegrityConstraintFactory;
import cz.cvut.kbss.jopa.ic.api.IntegrityConstraintVisitor;
import cz.cvut.kbss.jopa.ic.api.ObjectDomainConstraint;
import cz.cvut.kbss.jopa.ic.api.ObjectParticipationConstraint;
import cz.cvut.kbss.jopa.ic.api.ObjectRangeConstraint;
import cz.cvut.kbss.jopa.ic.impl.IntegrityConstraintFactoryImpl;
import cz.cvut.kbss.jopa.model.SequencesVocabulary;
import cz.cvut.kbss.jopa.owl2java.OWL2JavaTransformer.Card;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLAsymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLAxiomVisitor;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLDatatypeDefinitionAxiom;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointUnionAxiom;
import org.semanticweb.owlapi.model.OWLEntityVisitor;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalDataPropertyAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLHasKeyAxiom;
import org.semanticweb.owlapi.model.OWLInverseFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLInverseObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLIrreflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLNegativeDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLNegativeObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLReflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLSameIndividualAxiom;
import org.semanticweb.owlapi.model.OWLSubAnnotationPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubDataPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubPropertyChainOfAxiom;
import org.semanticweb.owlapi.model.OWLSymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLTransitiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.SWRLRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IntegrityConstraintParserImpl implements OWLAxiomVisitor {

    private static final Logger LOG = LoggerFactory.getLogger(OWL2JavaTransformer.class);

    private OWLDataFactory f;

    // private Map<OWLClass, OWLClass> superClasses = new HashMap<OWLClass,
    // OWLClass>();
    // private Map<OWLObjectProperty, Set<ObjectDomainConstraint>> odConstraints
    // = new HashMap<OWLObjectProperty, Set<ObjectDomainConstraint>>();
    // private Map<OWLDataProperty, Set<DataDomainConstraint>> ddConstraints =
    // new HashMap<OWLDataProperty, Set<DataDomainConstraint>>();
    // private Map<OWLObjectProperty, ObjectRangeConstraint> orConstraints = new
    // HashMap<OWLObjectProperty, ObjectRangeConstraint>();
    // private Map<OWLObjectProperty, DataRangeConstraint> drConstraints = new
    // HashMap<OWLObjectProperty, DataRangeConstraint>();
    // private Map<OWLObjectProperty, ObjectRangeConstraint> orConstraints = new
    // HashMap<OWLObjectProperty, ObjectRangeConstraint>();
    // private Map<OWLObjectProperty, DataRangeConstraint> drConstraints = new
    // HashMap<OWLObjectProperty, DataRangeConstraint>();

    private Map<OWLClass, List<IntegrityConstraint>> cConstraints = new HashMap<>();
    private Map<OWLClass, Map<OWLObjectProperty, Set<IntegrityConstraint>>> opConstraints = new HashMap<>();
    private Map<OWLClass, Map<OWLDataProperty, Set<IntegrityConstraint>>> dpConstraints = new HashMap<>();

    // private Map<OWLClass, Map<OWLDataProperty, Set<IntegrityConstraint>>>
    // dpConstraints = new HashMap<OWLClass, Map<OWLDataProperty,
    // Set<IntegrityConstraint>>>();

    // private Map<OWLObjectProperty,Set<OWLObjectProperty>> inverseProperties =
    // new HashMap<OWLObjectProperty, Set<OWLObjectProperty>>();

    private ContextDefinition ctx;

    private IntegrityConstraintFactory integrityConstraintFactory = new IntegrityConstraintFactoryImpl();

    public IntegrityConstraintParserImpl(final OWLDataFactory f, final ContextDefinition ctx) {
        this.ctx = ctx;
        this.f = f;
    }

    private static void notSupported(final OWLObject o) {
        LOG.info("Ignoring Unsupported Axiom : {}", o);
    }

    public void parse() {
        for (final OWLAxiom a : ctx.axioms) {
            a.accept(this);
        }
    }

    public void visit(OWLAnnotationPropertyRangeAxiom axiom) {
        notSupported(axiom);
    }

    public void visit(OWLAnnotationPropertyDomainAxiom axiom) {
        notSupported(axiom);
    }

    public void visit(OWLSubAnnotationPropertyOfAxiom axiom) {
        notSupported(axiom);
    }

    public void visit(OWLAnnotationAssertionAxiom axiom) {
        notSupported(axiom);
    }

    public void visit(SWRLRule axiom) {
        notSupported(axiom);
    }

    public void visit(OWLDatatypeDefinitionAxiom axiom) {
        notSupported(axiom);
    }

    public void visit(OWLHasKeyAxiom axiom) {
        notSupported(axiom);
    }

    public void visit(OWLInverseObjectPropertiesAxiom axiom) {
        notSupported(axiom);
    }

    public void visit(OWLSubPropertyChainOfAxiom axiom) {
        notSupported(axiom);
    }

    public void visit(OWLSameIndividualAxiom axiom) {
        notSupported(axiom);
    }

    public void visit(OWLInverseFunctionalObjectPropertyAxiom axiom) {
        notSupported(axiom);
    }

    public void visit(OWLSubDataPropertyOfAxiom axiom) {
        notSupported(axiom);
    }

    public void visit(OWLIrreflexiveObjectPropertyAxiom axiom) {
        notSupported(axiom);
    }

    public void visit(OWLTransitiveObjectPropertyAxiom axiom) {
        notSupported(axiom);
    }

    public void visit(OWLDataPropertyAssertionAxiom axiom) {
        notSupported(axiom);
    }

    public void visit(OWLEquivalentClassesAxiom axiom) {
        notSupported(axiom);
    }

    public void visit(OWLClassAssertionAxiom axiom) {
        notSupported(axiom);
    }

    public void visit(OWLEquivalentDataPropertiesAxiom axiom) {
        notSupported(axiom);
    }

    public void visit(OWLFunctionalDataPropertyAxiom axiom) {
        // ic.addAll(processParticipationConstraint(f.getOWLThing(), f
        // .getOWLDataMaxCardinality(1, axiom.getProperty())));

        // processParticipationConstraint(f.getOWLThing(), f
        // .getOWLDataMaxCardinality(1, axiom.getProperty()));
        notSupported(axiom);
    }

    public void visit(OWLDataPropertyRangeAxiom axiom) {
        notSupported(axiom);
    }

    public void visit(OWLSymmetricObjectPropertyAxiom axiom) {
        notSupported(axiom);
    }

    public void visit(OWLDisjointUnionAxiom axiom) {
        notSupported(axiom);
    }

    public void visit(OWLSubObjectPropertyOfAxiom axiom) {
        notSupported(axiom);
    }

    public void visit(OWLFunctionalObjectPropertyAxiom axiom) {
        // ic.addAll(processParticipationConstraint(f.getOWLThing(), f
        // .getOWLObjectMaxCardinality(1, axiom.getProperty())));

        // processParticipationConstraint(f.getOWLThing(), f
        // .getOWLObjectMaxCardinality(1, axiom.getProperty()));
        notSupported(axiom);
    }

    public void visit(OWLObjectPropertyAssertionAxiom axiom) {
        notSupported(axiom);
    }

    public void visit(OWLObjectPropertyRangeAxiom axiom) {
        try {
            // ic.addAll(processParticipationConstraint(f.getOWLThing(), f
            // .getOWLObjectMaxCardinality(1, axiom.getProperty())));

            OWLObjectProperty op = Utils.ensureObjectProperty(axiom.getProperty());
            OWLClass clz = Utils.ensureClass(axiom.getRange());
            // ObjectRangeConstraint c = orConstraints.get(op);
            // if (c == null) {
            // orConstraints.put(op, IntegrityConstraintFactoryImpl
            // .ObjectPropertyRangeConstraint(f.getOWLThing(), op, clz));
            // } else {
            // notSupported("Multiple ranges not supported", axiom);
            // }

            processParticipationConstraint(f.getOWLThing(),
                f.getOWLObjectAllValuesFrom(op, clz));
        } catch (UnsupportedICException e) {
            notSupported(axiom);
        }
    }

    public void visit(OWLDisjointObjectPropertiesAxiom axiom) {
        notSupported(axiom);
    }

    public void visit(OWLDisjointDataPropertiesAxiom axiom) {
        notSupported(axiom);
    }

    public void visit(OWLDifferentIndividualsAxiom axiom) {
        notSupported(axiom);
    }

    public void visit(OWLNegativeDataPropertyAssertionAxiom axiom) {
        notSupported(axiom);
    }

    public void visit(OWLEquivalentObjectPropertiesAxiom axiom) {
        notSupported(axiom);
    }

    public void visit(OWLObjectPropertyDomainAxiom axiom) {
        // OWLObjectProperty op = ensureObjectProperty(axiom.getProperty());
        // OWLClass clz = ensureClass(axiom.getDomain());
        // Set<ObjectDomainConstraint> c = odConstraints.get(op);
        // if (c == null) {
        // c = new HashSet<ObjectDomainConstraint>();
        // odConstraints.put(op, c);
        // }
        //
        // c.add(IntegrityConstraintFactoryImpl
        // .ObjectPropertyDomainConstraint(op, clz));
        notSupported(axiom);
    }

    public void visit(OWLDataPropertyDomainAxiom axiom) {
        // OWLDataProperty op = ensureDataProperty(axiom.getProperty());
        // OWLClass clz = ensureClass(axiom.getDomain());
        // Set<DataDomainConstraint> c = ddConstraints.get(op);
        // if (c == null) {
        // c = new HashSet<DataDomainConstraint>();
        // ddConstraints.put(op, c);
        // }
        //
        // c.add(IntegrityConstraintFactoryImpl.DataPropertyDomainConstraint(op,
        // clz));
        notSupported(axiom);
    }

    public void visit(OWLDisjointClassesAxiom axiom) {
        notSupported(axiom);
    }

    public void visit(OWLReflexiveObjectPropertyAxiom axiom) {
        notSupported(axiom);
    }

    public void visit(OWLAsymmetricObjectPropertyAxiom axiom) {
        notSupported(axiom);
    }

    public void visit(OWLNegativeObjectPropertyAssertionAxiom axiom) {
        notSupported(axiom);
    }

    public void visit(OWLSubClassOfAxiom axiom) {
        try {
            if (!axiom.getSubClass().isAnonymous()) {
                processParticipationConstraint(axiom.getSubClass().asOWLClass(),
                    axiom.getSuperClass());
            } else {
                notSupported(axiom);
            }
        } catch (UnsupportedICException e) {
            notSupported(axiom);
        }
    }

    public void visit(final OWLDeclarationAxiom axiom) {
        axiom.getEntity().accept(new OWLEntityVisitor() {

            public void visit(OWLAnnotationProperty property) {
                ctx.annotationProperties.add(property);
            }

            public void visit(OWLDatatype datatype) {
                notSupported(axiom);
            }

            public void visit(OWLNamedIndividual individual) {
                notSupported(axiom);
            }

            public void visit(OWLDataProperty property) {
                ctx.dataProperties.add(property);
            }

            public void visit(OWLObjectProperty property) {
                ctx.objectProperties.add(property);
            }

            public void visit(OWLClass cls) {
                ctx.classes.add(cls);
            }
        });
    }

    private void processParticipationConstraint(final OWLClass subjClass, final OWLClassExpression superClass) {
        Map<OWLObjectProperty, Set<IntegrityConstraint>> setOP2 = opConstraints
            .get(subjClass);
        if (setOP2 == null) {
            setOP2 = new HashMap<>();
            opConstraints.put(subjClass, setOP2);
        }
        final Map<OWLObjectProperty, Set<IntegrityConstraint>> mapOP = setOP2;

        Map<OWLDataProperty, Set<IntegrityConstraint>> setDP2 = dpConstraints
            .get(subjClass);
        if (setDP2 == null) {
            setDP2 = new HashMap<>();
            dpConstraints.put(subjClass, setDP2);
        }
        final Map<OWLDataProperty, Set<IntegrityConstraint>> mapDP = setDP2;

        List<IntegrityConstraint> setCx = cConstraints
            .get(subjClass);
        if (setCx == null) {
            setCx = new ArrayList<>();
            cConstraints.put(subjClass, setCx);
        }
        final List<IntegrityConstraint> setC = setCx;

        final IntegrityConstraintPopulator icp = new IntegrityConstraintPopulator(subjClass, integrityConstraintFactory);
        superClass.accept(icp);

        icp.getIntegrityConstraints().forEach((ic) -> {
            ic.accept(new IntegrityConstraintVisitor() {
                @Override
                public void visit(AtomicSubClassConstraint cpc) {
                    setC.add(cpc);
                }

                @Override
                public void visit(DataParticipationConstraint cpc) {
                    if (!mapDP.containsKey(cpc.getPredicate())) {
                        mapDP.put(cpc.getPredicate(), new HashSet<>());
                    }
                    mapDP.get(cpc.getPredicate()).add(cpc);
                }

                @Override
                public void visit(ObjectParticipationConstraint cpc) {
                    if (!mapOP.containsKey(cpc.getPredicate())) {
                        mapOP.put(cpc.getPredicate(), new HashSet<>());
                    }
                    mapOP.get(cpc.getPredicate()).add(cpc);
                }

                @Override
                public void visit(ObjectDomainConstraint cpc) {
                    if (!mapOP.containsKey(cpc.getProperty())) {
                        mapOP.put(cpc.getProperty(), new HashSet<>());
                    }
                    mapOP.get(cpc.getProperty()).add(cpc);
                }

                @Override
                public void visit(ObjectRangeConstraint cpc) {
                    if (!mapOP.containsKey(cpc.getProperty())) {
                        mapOP.put(cpc.getProperty(), new HashSet<>());
                    }
                    mapOP.get(cpc.getProperty()).add(cpc);
                }

                @Override
                public void visit(DataDomainConstraint cpc) {
                    if (!mapDP.containsKey(cpc.getProperty())) {
                        mapDP.put(cpc.getProperty(), new HashSet<>());
                    }
                    mapDP.get(cpc.getProperty()).add(cpc);
                }

                @Override
                public void visit(DataRangeConstraint cpc) {
                    if (!mapDP.containsKey(cpc.getProperty())) {
                        mapDP.put(cpc.getProperty(), new HashSet<>());
                    }
                    mapDP.get(cpc.getProperty()).add(cpc);
                }
            });
        });
    }

    public List<IntegrityConstraint> getClassIntegrityConstraints(final OWLClass cls) {
        if (cConstraints.containsKey(cls)) {
            return cConstraints.get(cls);
        } else {
            return Collections.emptyList();
        }
    }

    public ClassDataPropertyComputer getClassDataPropertyComputer(
        final OWLClass clazz, final OWLDataProperty prop,
        final OWLOntology merged) {
        final Set<IntegrityConstraint> ics = new HashSet<IntegrityConstraint>();

        final Map<OWLDataProperty, Set<IntegrityConstraint>> constraints = dpConstraints
            .get(clazz);
        if (constraints != null && constraints.containsKey(prop)) {
            ics.addAll(constraints.get(prop));
        }

        return new ClassDataPropertyComputer() {

            public Card getCard() {
                if (ics.isEmpty()) {
                    return Card.NO;
                }

                for (DataParticipationConstraint opc : getParticipationConstraints()) {
                    if (!(clazz.equals(opc.getSubject()) && prop.equals(opc
                        .getPredicate()))) {
                        continue;
                    }

                    OWLDatatype dt1 = getFiller();
                    OWLDatatype dt2 = opc.getObject();
                    if (dt1.equals(dt2)
                        || dt2.equals(OWLManager.getOWLDataFactory()
                        .getTopDatatype())) {
                        if (opc.getMax() == 1) {
                            return Card.ONE;
                        }
                    }
                }
                return Card.MULTIPLE;
            }

            public OWLDatatype getFiller() {
                for (final IntegrityConstraint ic : ics) {
                    if (ic instanceof DataRangeConstraint) {
                        return ((DataRangeConstraint) ic).getRange();
                    }
                }

                return f.getRDFPlainLiteral(); // f.getTopDatatype();
            }

            public Set<DataParticipationConstraint> getParticipationConstraints() {
                Set<DataParticipationConstraint> set = new HashSet<DataParticipationConstraint>();
                for (final IntegrityConstraint ic : ics) {
                    if (ic instanceof DataParticipationConstraint) {
                        set.add((DataParticipationConstraint) ic);
                    }
                }

                return set;
            }
        };

    }

    class ClassObjectPropertyComputer {

        private Card card;

        private OWLClass object;

        private Set<ObjectParticipationConstraint> ics;

        ClassObjectPropertyComputer(final OWLClass clazz, final OWLObjectProperty prop,
                                    final OWLOntology merged) {
            final Map<OWLObjectProperty, Set<IntegrityConstraint>> constraints = opConstraints
                .get(clazz);
            ics = new HashSet<>();
            if (constraints != null && constraints.containsKey(prop)) {
                OWLClass filler = null;
                for (final IntegrityConstraint ic : constraints.get(prop)) {

                    if (ic instanceof ObjectParticipationConstraint) {
                        ics.add((ObjectParticipationConstraint) ic);
                    } else if (ic instanceof ObjectRangeConstraint) {
                        filler = ((ObjectRangeConstraint) ic).getRange();
                    }
                }

                if (filler == null) {
                    filler = f.getOWLThing();
                }

                object = filler;

                final OWLDataFactory f = merged.getOWLOntologyManager().getOWLDataFactory();

                if (filler.getSuperClasses(merged).contains(
                    f.getOWLClass(org.semanticweb.owlapi.model.IRI.create(SequencesVocabulary.c_List)))) {
                    object = new ClassObjectPropertyComputer(filler, f.getOWLObjectProperty(
                        org.semanticweb.owlapi.model.IRI.create(SequencesVocabulary.p_element)), merged)
                        .getObject();
                    card = Card.LIST;
                } else if (filler.getSuperClasses(merged).contains(
                    f.getOWLClass(org.semanticweb.owlapi.model.IRI.create(SequencesVocabulary.c_OWLSimpleList)))) {
                    object = new ClassObjectPropertyComputer(filler, f.getOWLObjectProperty(
                        org.semanticweb.owlapi.model.IRI.create(SequencesVocabulary.p_hasNext)), merged)
                        .getObject();
                    card = Card.SIMPLELIST; // TODO referenced
                } else {
                    card = Card.MULTIPLE;
                    for (ObjectParticipationConstraint opc : ics) {
                        OWLClass dt2 = opc.getObject();
                        if (filler.equals(dt2)
                            || dt2.equals(OWLManager.getOWLDataFactory()
                            .getOWLThing())) {
                            if (opc.getMax() == 1) {
                                card = Card.ONE;
                                break;
                            }
                        }
                    }
                }
            } else {
                card = Card.NO;
            }
        }

        public Card getCard() {
            return card;
        }

        public OWLClass getObject() {
            return object;
        }

        public Set<ObjectParticipationConstraint> getParticipationConstraints() {
            return ics;
        }
    }
}