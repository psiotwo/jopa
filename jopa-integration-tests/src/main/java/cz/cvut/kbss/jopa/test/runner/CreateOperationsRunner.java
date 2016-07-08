/**
 * Copyright (C) 2016 Czech Technical University in Prague
 * <p>
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package cz.cvut.kbss.jopa.test.runner;

import cz.cvut.kbss.jopa.exceptions.OWLEntityExistsException;
import cz.cvut.kbss.jopa.exceptions.RollbackException;
import cz.cvut.kbss.jopa.model.EntityManager;
import cz.cvut.kbss.jopa.test.*;
import cz.cvut.kbss.jopa.test.environment.Generators;
import cz.cvut.kbss.ontodriver.exception.PrimaryKeyNotSetException;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;

import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

public abstract class CreateOperationsRunner extends BaseRunner {

    protected CreateOperationsRunner(Logger logger) {
        super(logger);
    }

    @Test
    public void testPersistWithGeneratedId() {
        this.em = getEntityManager("PersistWithGenerated", false);
        persist(entityA, entityE);

        final OWLClassA resA1 = em.find(OWLClassA.class, entityA.getUri());
        assertNotNull(resA1);
        assertEquals(entityA.getStringAttribute(), resA1.getStringAttribute());
        assertEquals(entityA.getTypes().size(), resA1.getTypes().size());
        assertTrue(entityA.getTypes().containsAll(resA1.getTypes()));

        assertNotNull(entityE.getUri());
        final OWLClassE resE = em.find(OWLClassE.class, entityE.getUri());
        assertNotNull(resE);
        assertEquals(entityE.getStringAttribute(), resE.getStringAttribute());
    }

    @Test(expected = PrimaryKeyNotSetException.class)
    public void persistingEntityWithoutIdAndWithoutGeneratedIdThrowsException() {
        this.em = getEntityManager("PersistWithoutId", false);
        final OWLClassB b = new OWLClassB();
        b.setStringAttribute("someValue");
        persist(b);
    }

    @Test(expected = NullPointerException.class)
    public void persistNullThrowsNPX() {
        this.em = getEntityManager("PersistNull", false);
        em.getTransaction().begin();
        em.persist(null);
    }

    @Test
    public void testPersistAndRollbackChanges() {
        this.em = getEntityManager("PersistRollback", false);
        em.getTransaction().begin();
        em.persist(entityE);
        assertTrue(em.contains(entityE));
        em.getTransaction().rollback();

        assertFalse(em.contains(entityE));
        assertNull(em.find(entityE.getClass(), entityE.getUri()));
    }

    @Test(expected = RollbackException.class)
    public void persistingInRollbackOnlyThrowsExceptionOnCommit() {
        this.em = getEntityManager("PersistRollbackOnly", false);
        em.getTransaction().begin();
        em.getTransaction().setRollbackOnly();
        em.persist(entityE);
        em.getTransaction().commit();
    }

    @Test
    public void testPersistWithCascade() {
        this.em = getEntityManager("PersistWithCascade", false);
        persist(entityG);

        final OWLClassA resA2 = em.find(OWLClassA.class, entityA.getUri());
        assertNotNull(resA2);
        final OWLClassH resH = em.find(OWLClassH.class, entityH.getUri());
        assertNotNull(resH);
        assertEquals(resH.getOwlClassA(), resA2);
        final OWLClassG resG = em.find(OWLClassG.class, entityG.getUri());
        assertNotNull(resG);
        assertEquals(resG.getOwlClassH(), resH);
        assertEquals(resG.getOwlClassH().getOwlClassA(), resA2);
    }

    @Test(expected = OWLEntityExistsException.class)
    public void persistingInstanceMultipleTimesIsNotAllowed() {
        this.em = getEntityManager("PersistTwice", false);
        persist(entityB, entityB);
    }

    @Test(expected = RollbackException.class)
    public void persistingOnlyOnePartOfRelationWithoutCascadeThrowsRollbackException() {
        this.em = getEntityManager("PersistWithoutCascade", false);
        persist(entityD);
    }

    @Test(expected = OWLEntityExistsException.class)
    public void persistingDetachedEntityIsIllegal() {
        this.em = getEntityManager("PersistDetached", false);
        persist(entityA);

        final OWLClassA det = em.find(OWLClassA.class, entityA.getUri());
        assertNotNull(det);
        em.detach(det);
        em.persist(det);
    }

    @Test
    public void testPersistWithSimpleList() {
        this.em = getEntityManager("PersistSimpleList", false);
        entityC.setSimpleList(Generators.createSimpleList(10));
        em.getTransaction().begin();
        em.persist(entityC);
        entityC.getSimpleList().forEach(em::persist);
        em.getTransaction().commit();

        final OWLClassA a = em.find(OWLClassA.class, entityC.getSimpleList().get(1).getUri());
        assertNotNull(a);
        final OWLClassC c = em.find(OWLClassC.class, entityC.getUri());
        assertNotNull(c);
        assertNotNull(c.getSimpleList());
        assertFalse(c.getSimpleList().isEmpty());
        assertEquals(entityC.getSimpleList().size(), c.getSimpleList().size());
        assertTrue(c.getSimpleList().contains(a));
    }

    @Test(expected = RollbackException.class)
    public void persistingEntityWithSimpleListWithoutCascadeIsIllegal() {
        this.em = getEntityManager("PersistSimpleListNoCascade", false);
        entityC.setSimpleList(Generators.createSimpleList(10));
        persist(entityC);
    }

    @Test
    public void testPersistWithReferencedList() {
        this.em = getEntityManager("PersistReferencedList", false);
        entityC.setReferencedList(Generators.createReferencedList(5));
        em.getTransaction().begin();
        em.persist(entityC);
        entityC.getReferencedList().forEach(em::persist);
        assertTrue(em.contains(entityC));
        assertTrue(em.contains(entityC.getReferencedList().get(0)));
        em.getTransaction().commit();

        final OWLClassC c = em.find(OWLClassC.class, entityC.getUri());
        assertNotNull(c);
        assertNotNull(c.getReferencedList());
        assertFalse(c.getReferencedList().isEmpty());
        assertEquals(entityC.getReferencedList().size(), c.getReferencedList().size());
        for (OWLClassA a : entityC.getReferencedList()) {
            final OWLClassA resA = em.find(OWLClassA.class, a.getUri());
            assertNotNull(resA);
            assertEquals(a.getStringAttribute(), resA.getStringAttribute());
            assertTrue(c.getReferencedList().contains(resA));
        }
    }

    @Test(expected = RollbackException.class)
    public void persistingEntityWithReferencedListWithoutCascadeIsIllegal() {
        this.em = getEntityManager("PersistReferencedListNoCascade", false);
        entityC.setReferencedList(Generators.createReferencedList(5));
        persist(entityC);
    }

    @Test
    public void testPersistSimpleAndReferencedList() {
        this.em = getEntityManager("PersistSimpleAndReferencedList", false);
        entityC.setReferencedList(Generators.createReferencedList(5));
        entityC.setSimpleList(Generators.createSimpleList(5));
        em.getTransaction().begin();
        em.persist(entityC);
        entityC.getSimpleList().forEach(em::persist);
        entityC.getReferencedList().forEach(em::persist);
        em.getTransaction().commit();

        final OWLClassC c = em.find(OWLClassC.class, entityC.getUri());
        assertNotNull(c);
        assertNotNull(c.getSimpleList());
        assertEquals(entityC.getSimpleList().size(), c.getSimpleList().size());
        assertNotNull(c.getReferencedList());
        assertEquals(entityC.getReferencedList().size(), c.getReferencedList().size());
        for (OWLClassA a : entityC.getSimpleList()) {
            final OWLClassA resA = em.find(OWLClassA.class, a.getUri());
            assertNotNull(resA);
            assertTrue(c.getSimpleList().contains(resA));
        }
        for (OWLClassA a : entityC.getReferencedList()) {
            final OWLClassA resA = em.find(OWLClassA.class, a.getUri());
            assertNotNull(resA);
            assertTrue(c.getReferencedList().contains(resA));
        }
    }

    @Test
    public void testPersistWithProperties() {
        this.em = getEntityManager("PersistWithProperties", false);
        final Map<String, Set<String>> props = new HashMap<>(3);
        props.put("http://krizik.felk.cvut.cz/ontologies/jopa/attributes#propertyOne", Collections
                .singleton("http://krizik.felk.cvut.cz/ontologies/jopa/tests/Individual10"));
        props.put("http://krizik.felk.cvut.cz/ontologies/jopa/attributes#propertyTwo", Collections
                .singleton("http://krizik.felk.cvut.cz/ontologies/jopa/tests/SomeEntity"));
        props.put("http://krizik.felk.cvut.cz/ontologies/jopa/attributes#propertyThree",
                Collections.singleton("http://krizik.felk.cvut.cz/ontologies/jopa/tests/entityG"));
        final Map<String, Set<String>> expected = new HashMap<>(4);
        expected.putAll(props);
        entityB.setProperties(props);
        persist(entityB);
        em.clear();

        final OWLClassB res = em.find(OWLClassB.class, entityB.getUri());
        assertNotNull(res);
        assertEquals(entityB.getStringAttribute(), res.getStringAttribute());
        assertNotNull(res.getProperties());
        assertFalse(res.getProperties().isEmpty());
        assertEquals(expected.size(), res.getProperties().size());
        for (Map.Entry<String, Set<String>> e : expected.entrySet()) {
            assertTrue(res.getProperties().containsKey(e.getKey()));
            final Set<String> s = e.getValue();
            final Set<String> resS = res.getProperties().get(e.getKey());
            assertNotNull(resS);
            assertEquals(1, resS.size());
            assertEquals(s.iterator().next(), resS.iterator().next());
        }
    }

    @Test
    public void testPersistWithEmptyProperties() {
        this.em = getEntityManager("PersistWithPropertiesEmpty", false);
        entityB.setProperties(Collections.emptyMap());
        em.getTransaction().begin();
        em.persist(entityB);
        assertTrue(em.contains(entityB));
        em.getTransaction().commit();
        em.clear();

        final OWLClassB b = em.find(OWLClassB.class, entityB.getUri());
        assertNotNull(b);
        assertEquals(entityB.getUri(), b.getUri());
        assertEquals(entityB.getStringAttribute(), b.getStringAttribute());
        assertNull(b.getProperties());
    }

    @Test(expected = OWLEntityExistsException.class)
    public void persistTwoInstancesOfDifferentClassesWithSameUriIntoTheSamePersistenceContextIsIllegal() {
        this.em = getEntityManager("PersistURITwiceInDifferentClassesSamePC", false);
        final URI pk = URI.create("http://krizik.felk.cvut.cz/jopa/onto/sameEntity");
        final OWLClassA a = new OWLClassA();
        a.setUri(pk);
        final OWLClassB b = new OWLClassB();
        b.setUri(pk);
        em.getTransaction().begin();
        em.persist(a);
        em.persist(b);
        em.getTransaction().commit();
    }

    @Ignore
    @Test
    public void persistTwoInstancesWithTheSameUriIntoDifferentPersistenceContextsIsLegal() {
        this.em = getEntityManager("PersistURITwiceInDifferentClassesDifferentPCs", false);
        final URI uri = URI.create("http://krizik.felk.cvut.cz/jopa/onto/sameEntity");
        entityA.setUri(uri);
        entityB.setUri(uri);
        em.getTransaction().begin();
        em.persist(entityA);
        em.getTransaction().commit();
        final EntityManager emTwo = em.getEntityManagerFactory().createEntityManager();
        try {
            emTwo.getTransaction().begin();
            emTwo.persist(entityB);
            emTwo.getTransaction().commit();

            assertNotNull(emTwo.find(OWLClassA.class, entityA.getUri()));
            assertNotNull(em.find(OWLClassB.class, entityB.getUri()));
        } finally {
            emTwo.close();
        }
    }

    @Test
    public void testPersistEntityWithBasicTypeAttributes() {
        this.em = getEntityManager("PersistEntityWithBasicTypeAttributes", false);
        persist(entityM);
        em.clear();

        final OWLClassM res = em.find(OWLClassM.class, entityM.getKey());
        assertNotNull(res);
        assertEquals(entityM.getKey(), res.getKey());
        assertEquals(entityM.getBooleanAttribute(), res.getBooleanAttribute());
        assertEquals(entityM.getIntAttribute(), res.getIntAttribute());
        assertEquals(entityM.getLongAttribute(), res.getLongAttribute());
        assertEquals(entityM.getDoubleAttribute(), res.getDoubleAttribute());
        assertEquals(entityM.getDateAttribute(), res.getDateAttribute());
    }

    @Test
    public void testPersistAndUpdateAttributeBeforeCommit() {
        this.em = getEntityManager("PersistAndUpdateBeforeCommit", false);
        final String updatedValue = "updatedStringAttributeValue";
        em.getTransaction().begin();
        em.persist(entityA);
        entityA.setStringAttribute(updatedValue);
        em.getTransaction().commit();
        em.clear();

        final OWLClassA res = em.find(OWLClassA.class, entityA.getUri());
        assertNotNull(res);
        assertEquals(updatedValue, res.getStringAttribute());
    }

    @Test
    public void testPersistEntityWithEnumAttribute() {
        this.em = getEntityManager("PersistEntityWithEnum", false);
        persist(entityM);

        final OWLClassM res = em.find(OWLClassM.class, entityM.getKey());
        assertNotNull(res);
        assertEquals(entityM.getEnumAttribute(), res.getEnumAttribute());
    }

    @Test
    public void testPersistTypedProperties() {
        this.em = getEntityManager("PersistTypedProperties", false);
        entityP.setProperties(Generators.createTypedProperties());
        em.getTransaction().begin();
        em.persist(entityP);
        em.getTransaction().commit();
        em.clear();

        final OWLClassP res = em.find(OWLClassP.class, entityP.getUri());
        assertNotNull(res);
        assertEquals(entityP.getProperties(), res.getProperties());
    }

    @Test
    public void testPersistInstanceWithPlainIdentifierObjectPropertyValue() {
        this.em = getEntityManager("PersistInstanceWithIdentifierObjectPropertyValue", false);
        final URI value = URI.create("http://krizik.felk.cvut.cz/ontologies/jopa#individualAAA");
        entityP.setIndividualUri(value);
        em.getTransaction().begin();
        em.persist(entityP);
        em.getTransaction().commit();
        em.clear();

        final OWLClassP res = em.find(OWLClassP.class, entityP.getUri());
        assertNotNull(res);
        assertEquals(value, res.getIndividualUri());
    }

    @Test
    public void testPersistInstanceWithPluralObjectPropertyAttributeRepresentedByUrls() {
        this.em = getEntityManager("PersistInstanceWithPluralIdentifierObjectPropertyValue", false);
        final Set<URL> urls = Generators.createUrls();
        entityP.setIndividuals(urls);
        em.getTransaction().begin();
        em.persist(entityP);
        em.getTransaction().commit();
        em.clear();

        final OWLClassP res = em.find(OWLClassP.class, entityP.getUri());
        assertNotNull(res);
        assertEquals(urls, res.getIndividuals());
    }

    @Test
    public void testPersistInstanceWithSimpleListOfIdentifiers() {
        this.em = getEntityManager("PersistInstanceWithSimpleListOfIdentifiers", false);
        entityP.setSimpleList(Generators.createListOfIdentifiers());
        em.getTransaction().begin();
        em.persist(entityP);
        em.getTransaction().commit();
        em.clear();

        final OWLClassP res = em.find(OWLClassP.class, entityP.getUri());
        assertNotNull(res);
        assertEquals(entityP.getSimpleList(), res.getSimpleList());
    }

    @Test
    public void testPersistInstanceWithReferencedListOfIdentifiers() {
        this.em = getEntityManager("PersistInstanceWithReferencedListOfIdentifiers", false);
        entityP.setReferencedList(Generators.createListOfIdentifiers());
        em.getTransaction().begin();
        em.persist(entityP);
        em.getTransaction().commit();
        em.clear();

        final OWLClassP res = em.find(OWLClassP.class, entityP.getUri());
        assertNotNull(res);
        assertEquals(entityP.getReferencedList(), res.getReferencedList());
    }

    @Test
    public void testPersistInstanceWithAnnotationProperties() {
        this.em = getEntityManager("PersistInstanceWithAnnotationPropertyValues", false);
        final String apValue = "annotationPropertyValue";
        final URI apUriValue = URI.create("http://krizik.felk.cvut.cz/ontologies/jopa#annotationPropertyValue");
        entityN.setAnnotationProperty(apValue);
        entityN.setAnnotationUri(apUriValue);
        em.getTransaction().begin();
        em.persist(entityN);
        em.getTransaction().commit();
        em.clear();
        assertNotNull(entityN.getId());

        final OWLClassN res = em.find(OWLClassN.class, entityN.getId());
        assertEquals(apValue, res.getAnnotationProperty());
        assertEquals(apUriValue, res.getAnnotationUri());
    }

    @Test
    public void persistEntityWithNonNullGeneratedIdentifierDoesNotRewriteIdentifier() {
        this.em = getEntityManager("PersistEntityWithNonNullGeneratedIdentifiersDoesNotRewriteIdentifier", false);
        final URI u = URI.create("http://krizik.felk.cvut.cz/ontolgoies/jopa#EntityELives");
        entityE.setUri(u);
        em.getTransaction().begin();
        em.persist(entityE);
        em.getTransaction().commit();

        assertEquals(u, entityE.getUri());
        final OWLClassE res = em.find(OWLClassE.class, u);
        assertNotNull(res);
        assertEquals(u, res.getUri());
        assertEquals(entityE.getStringAttribute(), res.getStringAttribute());
    }

    @Test
    public void persistEntityAndReferenceWithNonNullGeneratedIdentifiersDoesNotRewriteThem() {
        this.em = getEntityManager("PersistEntityAndReferenceWithNonNullGeneratedIdentifiersDoesNotRewriteThem", false);
        final URI uK = URI.create("http://krizik.felk.cvut.cz/ontolgoies/jopa#EntityKLives");
        final URI uE = URI.create("http://krizik.felk.cvut.cz/ontolgoies/jopa#EntityELives");
        final OWLClassK entityK = new OWLClassK();
        entityK.setUri(uK);
        entityK.setOwlClassE(entityE);
        entityE.setUri(uE);
        em.getTransaction().begin();
        em.persist(entityK);
        em.persist(entityE);
        em.getTransaction().commit();

        assertEquals(uK, entityK.getUri());
        assertEquals(uE, entityE.getUri());
        final OWLClassK resK = em.find(OWLClassK.class, uK);
        assertNotNull(resK);
        assertEquals(uE, resK.getOwlClassE().getUri());
        final OWLClassE resE = em.find(OWLClassE.class, uE);
        assertNotNull(resE);
        assertEquals(uE, resE.getUri());
    }

    @Test
    public void testPersistEntityWithUriTypes() {
        this.em = getEntityManager("PersistEntityWithUriTypes", false);
        entityP.setTypes(Generators.createUriTypes());
        em.getTransaction().begin();
        em.persist(entityP);
        em.getTransaction().commit();
        em.clear();

        final OWLClassP result = em.find(OWLClassP.class, entityP.getUri());
        assertEquals(entityP.getTypes().size(), result.getTypes().size());
        assertTrue(entityP.getTypes().containsAll(result.getTypes()));
    }

    @Test
    public void testPersistEntityWithMappedSuperclass() {
        this.em = getEntityManager("PersistEntityWithMappedSuperclass", false);
        em.getTransaction().begin();
        em.persist(entityA);
        em.persist(entityQ);
        em.getTransaction().commit();
        em.clear();

        assertNotNull(entityQ.getUri());
        final OWLClassQ result = em.find(OWLClassQ.class, entityQ.getUri());
        assertNotNull(result);
        assertEquals(entityQ.getStringAttribute(), result.getStringAttribute());
        assertEquals(entityQ.getParentString(), result.getParentString());
        assertEquals(entityQ.getLabel(), result.getLabel());
        assertNotNull(result.getOwlClassA());
        assertEquals(entityQ.getOwlClassA().getUri(), result.getOwlClassA().getUri());
        assertEquals(entityA.getStringAttribute(), result.getOwlClassA().getStringAttribute());
        assertEquals(entityA.getTypes(), result.getOwlClassA().getTypes());
    }
}
