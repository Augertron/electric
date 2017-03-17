/* -*- tab-width: 4 -*-
 *
 * Electric(tm) VLSI Design System
 *
 * File: ACL2Atom.java
 *
 * Copyright (c) 2017, Static Free Software. All rights reserved.
 *
 * Electric(tm) is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Electric(tm) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.sun.electric.util.acl2;

/**
 * Leaf node of ACL2 object.
 * It subclasses are
 * {@link com.sun.electric.util.acl2.ACL2Number},
 * {@link com.sun.electric.util.acl2.ACL2Character},
 * {@link com.sun.electric.util.acl2.ACL2String},
 * {@link com.sun.electric.util.acl2.ACL2Symbol},
 */
public abstract class ACL2Atom extends ACL2Object
{

    ACL2Atom(int id, boolean norm)
    {
        super(id, norm);
    }
}
