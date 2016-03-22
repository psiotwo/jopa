/**
 * Copyright (C) 2016 Czech Technical University in Prague
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
package cz.cvut.kbss.ontodriver.model;

import java.net.URI;

/**
 * Base assertion axiom class. </p> <p> Defines just whether the assertion uses inferred values and existing types of
 * assertions. </p>
 * <p>
 * The usage of types may seem as not being very object-oriented, but since the hierarchy is fixed (there aren't any
 * other kinds of assertions in ontologies) and since the subclasses essentially don't contain any behavior, we can use
 * this way.
 *
 * @author ledvima1
 */
public abstract class Assertion extends NamedResource {

    private static final long serialVersionUID = 2641835840569464452L;

    private final boolean inferred;

    public enum AssertionType {
        /**
         * PROPERTY assertion is used in cases where we don't know the property type, for instance when loading value of
         * the Properties attribute
         */
        CLASS, PROPERTY, OBJECT_PROPERTY, DATA_PROPERTY, ANNOTATION_PROPERTY
    }

    protected Assertion(URI identifier, boolean isInferred) {
        super(identifier);
        this.inferred = isInferred;
    }

    /**
     * Whether this assertion is based on inferred values.
     *
     * @return True if inferred, false otherwise
     */
    public boolean isInferred() {
        return inferred;
    }

    /**
     * Whether this assertion is a class assertion.
     * <p>
     * This is a convenience method, its functionality could be emulated by retrieving this assertion's identifier and
     * checking whether it equals to the rdf:type URI.
     *
     * @return True if this assertion is a class assertion, false otherwise.
     */
    public boolean isClassAssertion() {
        return getIdentifier().equals(ClassAssertion.RDF_TYPE);
    }

    /**
     * Gets type of this assertion.
     *
     * @return Assertion type
     */
    public abstract AssertionType getType();

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (inferred ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        Assertion other = (Assertion) obj;
        if (inferred != other.inferred)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return super.toString() + (inferred ? " - inferred" : " - non-inferred");
    }

    /**
     * Creates new class assertion. </p>
     * <p>
     * Class assertions use the rdf:type identifier.
     *
     * @param isInferred Whether the assertion uses inferred values
     * @return Assertion
     */
    public static Assertion createClassAssertion(boolean isInferred) {
        return new ClassAssertion(isInferred);
    }

    /**
     * Creates a property assertion without specifying the assertion identifier. </p>
     * <p>
     * Note that the returned instances are all equals as long as their inferred status is the same.
     *
     * @param isInferred Whether the assertion uses inferred values
     * @return Assertion
     */
    public static Assertion createUnspecifiedPropertyAssertion(boolean isInferred) {
        return new PropertyAssertion(isInferred);
    }

    /**
     * Creates new property assertion without specifying what kind of property it is. </p>
     *
     * @param assertionIdentifier Assertion identifier
     * @param isInferred          Whether the assertion uses inferred values
     * @return Assertion
     */
    public static Assertion createPropertyAssertion(URI assertionIdentifier, boolean isInferred) {
        return new PropertyAssertion(assertionIdentifier, isInferred);
    }

    /**
     * Creates new object property assertion.
     *
     * @param assertionIdentifier Assertion identifier
     * @param isInferred          Whether the assertion uses inferred values
     * @return Assertion
     */
    public static Assertion createObjectPropertyAssertion(URI assertionIdentifier,
                                                          boolean isInferred) {
        return new ObjectPropertyAssertion(assertionIdentifier, isInferred);
    }

    /**
     * Creates new data property assertion.
     *
     * @param assertionIdentifier Assertion identifier
     * @param isInferred          Whether the assertion uses inferred values
     * @return Assertion
     */
    public static Assertion createDataPropertyAssertion(URI assertionIdentifier, boolean isInferred) {
        return new DataPropertyAssertion(assertionIdentifier, isInferred);
    }

    /**
     * Creates new annotation property assertion.
     *
     * @param assertionIdentifier Assertion identifier
     * @param isInferred          Whether the assertion uses inferred values
     * @return Assertion
     */
    public static Assertion createAnnotationPropertyAssertion(URI assertionIdentifier,
                                                              boolean isInferred) {
        return new AnnotationPropertyAssertion(assertionIdentifier, isInferred);
    }
}