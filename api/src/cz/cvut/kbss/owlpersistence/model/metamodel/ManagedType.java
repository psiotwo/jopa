/**
 * Copyright (C) 2011 Czech Technical University in Prague
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

package cz.cvut.kbss.owlpersistence.model.metamodel;

import java.util.Set;

import cz.cvut.kbss.owlpersistence.UnusedJPA;

/**
 * Instances of the type ManagedType represent entity, mapped superclass, and
 * embeddable types.
 * 
 * @param <X>
 *            The represented type.
 */
@UnusedJPA
public interface ManagedType<X> extends Type<X> {
	/**
	 * Return the attributes of the managed type.
	 * 
	 * @return attributes of the managed type
	 */
	Set<Attribute<? super X, ?>> getAttributes();

	/**
	 * Return the attributes declared by the managed type. Returns empty set if
	 * the managed type has no declared attributes.
	 * 
	 * @return declared attributes of the managed type
	 */
	Set<Attribute<X, ?>> getDeclaredAttributes();

	/**
	 * Return the single-valued attribute of the managed type that corresponds
	 * to the specified name and Java type.
	 * 
	 * @param name
	 *            the name of the represented attribute
	 *@param type
	 *            the type of the represented attribute
	 *@return single-valued attribute with given name and type
	 *@throws IllegalArgumentException
	 *             if attribute of the given name and type is not present in the
	 *             managed type
	 */
	<Y> SingularAttribute<? super X, Y> getSingularAttribute(String name,
			Class<Y> type);

	/**
	 * Return the single-valued attribute declared by the managed type that
	 * corresponds to the specified name and Java type.
	 * 
	 * @param name
	 *            the name of the represented attribute
	 * @param type
	 *            the type of the represented attribute
	 * @return declared single-valued attribute of the given name and type
	 * @throws IllegalArgumentException
	 *             if attribute of the given name and type is not declared in
	 *             the managed type
	 * */
	<Y> SingularAttribute<X, Y> getDeclaredSingularAttribute(String name,
			Class<Y> type);

	/**
	 * Return the single-valued attributes of the managed type. Returns empty
	 * set if the managed type has no single-valued attributes.
	 * 
	 * @return single-valued attributes
	 */
	Set<SingularAttribute<? super X, ?>> getSingularAttributes();

	/**
	 * Return the single-valued attributes declared by the managed type. Returns
	 * empty set if the managed type has no declared single-valued attributes.
	 * 
	 * @return declared single-valued attributes
	 */
	Set<SingularAttribute<X, ?>> getDeclaredSingularAttributes();

	/**
	 * Return the Collection-valued attribute of the managed type that
	 * corresponds to the specified name and Java element type.
	 * 
	 * @param name
	 *            the name of the represented attribute
	 *@param elementType
	 *            the element type of the represented attribute
	 * @return CollectionAttribute of the given name and element type
	 * @throws IllegalArgumentException
	 *             if attribute of the given name and type is not present in the
	 *             managed type
	 */
	<E> CollectionAttribute<? super X, E> getCollection(String name,
			Class<E> elementType);

	/**
	 * Return the Collection-valued attribute declared by the managed type that
	 * corresponds to the specified name and Java element type.
	 * 
	 * @param name
	 *            the name of the represented attribute
	 * @param elementType
	 *            the element type of the represented attribute
	 * @return declared CollectionAttribute of the given name and element type
	 * @throws IllegalArgumentException
	 *             if attribute of the given name and type is not declared in
	 *             the managed type
	 */
	<E> CollectionAttribute<X, E> getDeclaredCollection(String name,
			Class<E> elementType);

	/**
	 * Return the Set-valued attribute of the managed type that corresponds to
	 * the specified name and Java element type.
	 * 
	 * @param name
	 *            the name of the represented attribute
	 * @param elementType
	 *            the element type of the represented attribute
	 * @return SetAttribute of the given name and element type
	 * @throws IllegalArgumentException
	 *             if attribute of the given name and type is not present in the
	 *             managed type
	 **/
	<E> SetAttribute<? super X, E> getSet(String name, Class<E> elementType);

	/**
	 * Return the Set-valued attribute declared by the managed type that
	 * corresponds to the specified name and Java element type.
	 * 
	 * @param name
	 *            the name of the represented attribute
	 * @param elementType
	 *            the element type of the represented attribute
	 * @return declared SetAttribute of the given name and element type
	 * @throws IllegalArgumentException
	 *             if attribute of the given name and type is not declared in
	 *             the managed type
	 */

	<E> SetAttribute<X, E> getDeclaredSet(String name, Class<E> elementType);

	/**
	 * Return the List-valued attribute of the managed type that corresponds to
	 * the specified name and Java element type.
	 * 
	 * @param name
	 *            the name of the represented attribute
	 * @param elementType
	 *            the element type of the represented attribute
	 * @return ListAttribute of the given name and element type
	 * @throws IllegalArgumentException
	 *             if attribute of the given name and type is not present in the
	 *             managed type
	 */
	<E> ListAttribute<? super X, E> getList(String name, Class<E> elementType);

	/**
	 * Return the List-valued attribute declared by the managed type that
	 * corresponds to the specified name and Java element type.
	 * 
	 * @param name
	 *            the name of the represented attribute
	 * @param elementType
	 *            the element type of the represented attribute
	 * @return declared ListAttribute of the given name and element type
	 * @throws IllegalArgumentException
	 *             if attribute of the given name and type is not declared in
	 *             the managed type
	 */
	<E> ListAttribute<X, E> getDeclaredList(String name, Class<E> elementType);

	/**
	 * Return the Map-valued attribute of the managed type that corresponds to
	 * the specified name and Java key and value types.
	 * 
	 * @param name
	 *            the name of the represented attribute
	 * @param keyType
	 *            the key type of the represented attribute
	 * @param valueType
	 *            the value type of the represented attribute
	 * @return MapAttribute of the given name and key and value types
	 * @throws IllegalArgumentException
	 *             if attribute of the given name and type is not present in the
	 *             managed type
	 */
	<K, V> MapAttribute<? super X, K, V> getMap(String name, Class<K> keyType,
			Class<V> valueType);

	/**
	 * Return the Map-valued attribute declared by the managed type that
	 * corresponds to the specified name and Java key and value types.
	 * 
	 * @param name
	 *            the name of the represented attribute
	 * @param keyType
	 *            the key type of the represented attribute
	 * @param valueType
	 *            the value type of the represented attribute
	 * @return declared MapAttribute of the given name and key and value types
	 * @throws IllegalArgumentException
	 *             if attribute of the given name and type is not declared in
	 *             the managed type
	 */
	<K, V> MapAttribute<X, K, V> getDeclaredMap(String name, Class<K> keyType,
			Class<V> valueType);

	/**
	 * Return all multi-valued attributes (Collection-, Set-, List-, and
	 * Map-valued attributes) of the managed type. Returns empty set if the
	 * managed type has no multi-valued attributes.
	 * 
	 * @return Collection-, Set-, List-, and Map-valued attributes
	 */
	Set<PluralAttribute<? super X, ?, ?>> getPluralAttributes();

	/**
	 * Return all multi-valued attributes (Collection-, Set-, List-, and
	 * Map-valued attributes) declared by the managed type. Returns empty set if
	 * the managed type has no declared multi-valued attributes.
	 * 
	 * @return declared Collection-, Set-, List-, and Map-valued
	 * 
	 *         attributes
	 */
	Set<PluralAttribute<X, ?, ?>> getDeclaredPluralAttributes();

	// String-based:
	/**
	 * Return the attribute of the managed type that corresponds to the
	 * specified name.
	 * 
	 * @param name
	 *            the name of the represented attribute
	 * @return attribute with given name
	 * @throws IllegalArgumentException
	 *             if attribute of the given
	 * 
	 *             name is not present in the managed type
	 */
	Attribute<? super X, ?> getAttribute(String name);

	/**
	 * Return the attribute declared by the managed type that corresponds to the
	 * specified name.
	 * 
	 * @param name
	 *            the name of the represented attribute
	 * @return attribute with given name
	 * @throws IllegalArgumentException
	 *             if attribute of the given
	 * 
	 *             name is not declared in the managed type
	 */
	Attribute<X, ?> getDeclaredAttribute(String name);

	/**
	 * Return the single-valued attribute of the managed type that corresponds
	 * to the specified name.
	 * 
	 * @param name
	 *            the name of the represented attribute
	 * @return single-valued attribute with the given name
	 * @throws IllegalArgumentException
	 *             if attribute of the given
	 * 
	 *             name is not present in the managed type
	 */
	SingularAttribute<? super X, ?> getSingularAttribute(String name);

	/**
	 * Return the single-valued attribute declared by the managed type that
	 * corresponds to the specified name.
	 * 
	 * @param name
	 *            the name of the represented attribute
	 * @return declared single-valued attribute of the given
	 * 
	 *         name
	 * @throws IllegalArgumentException
	 *             if attribute of the given
	 * 
	 *             name is not declared in the managed type
	 */
	SingularAttribute<X, ?> getDeclaredSingularAttribute(String name);

	/**
	 * Return the Collection-valued attribute of the managed type that
	 * corresponds to the specified name.
	 * 
	 * @param name
	 *            the name of the represented attribute
	 * @return CollectionAttribute of the given name
	 * @throws IllegalArgumentException
	 *             if attribute of the given
	 * 
	 *             name is not present in the managed type
	 */
	CollectionAttribute<? super X, ?> getCollection(String name);

	/**
	 * Return the Collection-valued attribute declared by the managed type that
	 * corresponds to the specified name.
	 * 
	 * @param name
	 *            the name of the represented attribute
	 * @return declared CollectionAttribute of the given name
	 * @throws IllegalArgumentException
	 *             if attribute of the given
	 * 
	 *             name is not declared in the managed type
	 */
	CollectionAttribute<X, ?> getDeclaredCollection(String name);

	/**
	 * Return the Set-valued attribute of the managed type that corresponds to
	 * the specified name.
	 * 
	 * @param name
	 *            the name of the represented attribute
	 * @return SetAttribute of the given name
	 * @throws IllegalArgumentException
	 *             if attribute of the given
	 * 
	 *             name is not present in the managed type
	 */
	SetAttribute<? super X, ?> getSet(String name);

	/**
	 * Return the Set-valued attribute declared by the managed type that
	 * corresponds to the specified name.
	 * 
	 * @param name
	 *            the name of the represented attribute
	 * @return declared SetAttribute of the given name
	 * @throws IllegalArgumentException
	 *             if attribute of the given
	 * 
	 *             name is not declared in the managed type
	 */
	SetAttribute<X, ?> getDeclaredSet(String name);

	/**
	 * Return the List-valued attribute of the managed type that corresponds to
	 * the specified name.
	 * 
	 * @param name
	 *            the name of the represented attribute
	 * @return ListAttribute of the given name
	 * @throws IllegalArgumentException
	 *             if attribute of the given
	 * 
	 *             name is not present in the managed type
	 */
	ListAttribute<? super X, ?> getList(String name);

	/**
	 * Return the List-valued attribute declared by the managed type that
	 * corresponds to the specified name.
	 * 
	 * @param name
	 *            the name of the represented attribute
	 * @return declared ListAttribute of the given name
	 * @throws IllegalArgumentException
	 *             if attribute of the given
	 * 
	 *             name is not declared in the managed type
	 */
	ListAttribute<X, ?> getDeclaredList(String name);

	/**
	 * Return the Map-valued attribute of the managed type that corresponds to
	 * the specified name.
	 * 
	 * @param name
	 *            the name of the represented attribute
	 * @return MapAttribute of the given name
	 * @throws IllegalArgumentException
	 *             if attribute of the given
	 * 
	 *             name is not present in the managed type
	 */
	MapAttribute<? super X, ?, ?> getMap(String name);

	/**
	 * Return the Map-valued attribute declared by the managed type that
	 * corresponds to the specified name.
	 * 
	 * @param name
	 *            the name of the represented attribute
	 * @return declared MapAttribute of the given name
	 * @throws IllegalArgumentException
	 *             if attribute of the given
	 * 
	 *             name is not declared in the managed type
	 */
	MapAttribute<X, ?, ?> getDeclaredMap(String name);
}