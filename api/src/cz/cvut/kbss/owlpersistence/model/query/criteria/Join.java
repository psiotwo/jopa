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

package cz.cvut.kbss.owlpersistence.model.query.criteria;

import cz.cvut.kbss.owlpersistence.model.metamodel.Attribute;

/**
 * A join to an entity, embeddable, or basic type.
 * 
 * @param <Z>
 *            the source type of the join
 * @param <X>
 *            the target type of the join
 */
public interface Join<Z, X> extends From<Z, X> {
	/**
	 * Return the metamodel attribute corresponding to the jo* @return metamodel
	 * attribute corresponding to the join
	 */
	Attribute<? super Z, ?> getAttribute();

	/**
	 * Return the parent of the join.
	 * 
	 * @return join parent
	 */
	From<?, Z> getParent();

	/**
	 * Return the join type.
	 * 
	 * @return join type
	 */
	JoinType getJoinType();
}