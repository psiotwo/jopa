/**
 * Copyright (C) 2020 Czech Technical University in Prague
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package cz.cvut.kbss.ontodriver.descriptor;

import cz.cvut.kbss.ontodriver.model.Assertion;
import cz.cvut.kbss.ontodriver.model.NamedResource;

import java.net.URI;
import java.util.*;

/**
 * This descriptor specifies subject and properties of axioms to search for in the ontology.
 * <p>
 * Additionally, it can specify context URIs for both the descriptor and individual properties so that the underlying
 * driver knows where to look for the corresponding axioms.
 */
public class AxiomDescriptor {

    private final NamedResource subject;
    private final Set<Assertion> assertions;

    private URI subjectContext;
    private final Map<Assertion, URI> assertionContexts;

    public AxiomDescriptor(NamedResource subject) {
        this.subject = Objects.requireNonNull(subject);
        this.assertions = new HashSet<>();
        this.assertionContexts = new HashMap<>();
    }

    /**
     * Sets subject context.
     *
     * @param context The context to use for subject
     */
    public void setSubjectContext(URI context) {
        this.subjectContext = context;
    }

    /**
     * Adds the specified assertion to this descriptor.
     *
     * @param assertion The assertion to add
     * @throws NullPointerException When {@code assertion} is {@code null}
     */
    public void addAssertion(Assertion assertion) {
        Objects.requireNonNull(assertion);
        assertions.add(assertion);
    }

    /**
     * Sets context for the specified assertion.
     * <p>
     * Note that the assertion has to be already present in this descriptor.
     *
     * @param assertion The property to set context for
     * @param context   Context URI
     * @throws IllegalArgumentException If there is no such assertion in this descriptor
     * @throws NullPointerException     When {@code assertion} is {@code null}
     */
    public void setAssertionContext(Assertion assertion, URI context) {
        Objects.requireNonNull(assertion);
        if (!assertions.contains(assertion)) {
            throw new IllegalArgumentException("Assertion " + assertion + " is not present in this descriptor.");
        }
        assertionContexts.put(assertion, context);
    }

    public NamedResource getSubject() {
        return subject;
    }

    public URI getSubjectContext() {
        return subjectContext;
    }

    /**
     * Gets context of the specified assertion.
     * <p>
     * If the context was not explicitly set, the same context as the subject's is returned.
     *
     * @param assertion Assertion for which context should be resolved
     * @return Assertion context
     */
    public URI getAssertionContext(Assertion assertion) {
        Objects.requireNonNull(assertion);
        if (!assertionContexts.containsKey(assertion)) {
            return subjectContext;
        }
        return assertionContexts.get(assertion);
    }

    /**
     * Gets unmodifiable view of the properties in this descriptor.
     *
     * @return Set of assertions in this descriptor
     */
    public Set<Assertion> getAssertions() {
        return Collections.unmodifiableSet(assertions);
    }

    /**
     * Checks whether this descriptor contains the specified assertion.
     *
     * @param assertion The assertion to check
     * @return True if the assertion is already present in this descriptor, false otherwise
     */
    public boolean containsAssertion(Assertion assertion) {
        return assertions.contains(assertion);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + subject.hashCode();
        result = prime * result + ((subjectContext == null) ? 0 : subjectContext.hashCode());
        result = prime * result + assertions.hashCode();
        result = prime * result + assertionContexts.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AxiomDescriptor other = (AxiomDescriptor) obj;
        if (!subject.equals(other.subject))
            return false;
        if (subjectContext == null) {
            if (other.subjectContext != null)
                return false;
        } else if (!subjectContext.equals(other.subjectContext))
            return false;
        return assertions.equals(other.assertions) && assertionContexts.equals(other.assertionContexts);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("[").append(subject);
        if (subjectContext != null) {
            sb.append(" - ").append(subjectContext);
        }
        if (!assertionContexts.isEmpty()) {
            sb.append(", properties: ").append(assertionContexts);
        } else {
            sb.append(", properties: ").append(assertions);
        }
        sb.append("]");
        return sb.toString();
    }
}
