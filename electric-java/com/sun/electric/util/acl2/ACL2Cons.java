/* -*- tab-width: 4 -*-
 *
 * Electric(tm) VLSI Design System
 *
 * File: ACL2Cons.java
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
 * Non-leaf node of ACL2 object.
 * Often a ACL2 object are used to represent a list.
 * In this case {@link #car} is the first element
 * and {@link #cdr} is the tail.
 */
public class ACL2Cons extends ACL2Object
{

    /**
     * The left son.
     */
    public final ACL2Object car;
    /**
     * The right sun.
     */
    public final ACL2Object cdr;

    ACL2Cons(int id, boolean norm, ACL2Object car, ACL2Object cdr)
    {
        super(id, norm);
        this.car = car;
        this.cdr = cdr;
    }

    @Override
    public int len()
    {
        ACL2Object o = this;
        int n = 0;
        while (o instanceof ACL2Cons)
        {
            n++;
            o = ((ACL2Cons)o).cdr;
        }
        return n;
    }

    @Override
    public ACL2Cons asCons()
    {
        return this;
    }

    @Override
    public String toString()
    {
        return id + "!" + len();
    }

    @Override
    public String rep()
    {
        return "(" + car.rep() + "." + cdr.rep() + ")";
    }

    @Override
    public boolean equals(Object o)
    {
        return o instanceof ACL2Cons
            && car.equals(((ACL2Cons)o).car)
            && cdr.equals(((ACL2Cons)o).cdr);
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 29 * hash + car.hashCode();
        hash = 29 * hash + cdr.hashCode();
        return hash;
    }
}
