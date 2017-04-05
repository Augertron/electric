/* -*- tab-width: 4 -*-
 *
 * Electric(tm) VLSI Design System
 *
 * File: Vec4.java
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
package com.sun.electric.tool.simulation.acl2.svex;

import static com.sun.electric.util.acl2.ACL2.*;
import com.sun.electric.util.acl2.ACL2Object;
import java.math.BigInteger;

/**
 * The fundamental 4-valued vector representation used throughout SV expressions.
 * See <http://www.cs.utexas.edu/users/moore/acl2/manuals/current/manual/?topic=SV____4VEC>.
 */
public abstract class Vec4
{
    public abstract boolean isVec2();

    public abstract BigInteger getUpper();

    public abstract BigInteger getLower();

    public abstract ACL2Object makeAcl2Object();

    public static Vec4 X = new Impl(BigInteger.valueOf(-1), BigInteger.valueOf(0));

    public static Vec4 valueOf(BigInteger upper, BigInteger lower)
    {
        return upper.equals(lower) ? new Vec2(upper) : new Impl(upper, lower);
    }

    public static Vec4 valueOf(ACL2Object impl)
    {
        if (consp(impl).bool())
        {
            return valueOf(ifix(car(impl)).bigIntegerValueExact(), ifix(cdr(impl)).bigIntegerValueExact());
        }
        if (integerp(impl).bool())
        {
            return new Vec2(impl.bigIntegerValueExact());
        }
        return X;
    }

    static class Impl extends Vec4
    {
        private final BigInteger upper;
        private final BigInteger lower;

        Impl(BigInteger upper, BigInteger lower)
        {
            if (upper.equals(lower))
            {
                throw new IllegalArgumentException();
            }
            this.upper = upper;
            this.lower = lower;
        }

        @Override
        public boolean isVec2()
        {
            return false;
        }

        @Override
        public BigInteger getUpper()
        {
            return upper;
        }

        @Override
        public BigInteger getLower()
        {
            return lower;
        }

        @Override
        public ACL2Object makeAcl2Object()
        {
            return cons(ACL2Object.valueOf(upper), ACL2Object.valueOf(lower));
        }

        @Override
        public boolean equals(Object o)
        {
            if (o instanceof Impl)
            {
                Impl that = (Impl)o;
                return this.upper.equals(that.upper) && this.lower.equals(that.lower);
            }
            return false;
        }

        @Override
        public int hashCode()
        {
            int hash = 3;
            hash = 67 * hash + upper.hashCode();
            hash = 67 * hash + lower.hashCode();
            return hash;
        }
    }
}