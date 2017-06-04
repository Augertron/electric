/* -*- tab-width: 4 -*-
 *
 * Electric(tm) VLSI Design System
 *
 * File: Vec4Wildeq.java
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
package com.sun.electric.tool.simulation.acl2.svex.funs;

import com.sun.electric.tool.simulation.acl2.svex.BigIntegerUtil;
import com.sun.electric.tool.simulation.acl2.svex.Svar;
import com.sun.electric.tool.simulation.acl2.svex.Svex;
import com.sun.electric.tool.simulation.acl2.svex.SvexCall;
import com.sun.electric.tool.simulation.acl2.svex.SvexFunction;
import com.sun.electric.tool.simulation.acl2.svex.Vec4;
import java.math.BigInteger;
import java.util.Map;

/**
 * True if for every pair of corresponding bits of a and b, either they are equal or the bit from b is X or Z.
 * See<http://www.cs.utexas.edu/users/moore/acl2/manuals/current/manual/?topic=SV____4VEC-WILDEQ>.
 */
public class Vec4Wildeq<V extends Svar> extends SvexCall<V>
{
    public static final Function FUNCTION = new Function();
    public final Svex<V> x;
    public final Svex<V> y;

    public Vec4Wildeq(Svex<V> x, Svex<V> y)
    {
        super(FUNCTION, x, y);
        this.x = x;
        this.y = y;
    }

    @Override
    public Vec4 xeval(Map<Svex<V>, Vec4> memoize)
    {
        Vec4 result = memoize.get(this);
        if (result == null)
        {
            result = Vec4WildeqSafe.FUNCTION.apply(Svex.listXeval(args, memoize));
            memoize.put(this, result);
        }
        return result;
    }

    public static class Function extends SvexFunction
    {
        private Function()
        {
            super(FunctionSyms.SV_EQ_EQ_QUEST, 2);
        }

        @Override
        public <V extends Svar> Vec4Wildeq<V> build(Svex<V>... args)
        {
            return new Vec4Wildeq<>(args[0], args[1]);
        }

        @Override
        public Vec4 apply(Vec4... args)
        {
            Vec4 a = args[0];
            Vec4 b = args[1];
            BigInteger zxMask = b.getUpper().xor(b.getLower());
            return eq(a, b, zxMask);
        }

        @Override
        protected <V extends Svar> BigInteger[] svmaskFor(BigInteger mask, Svex<V>[] args, Map<Svex<V>, Vec4> xevalMemoize)
        {
            if (mask.signum() == 0)
            {
                return new BigInteger[]
                {
                    BigInteger.ZERO, BigInteger.ZERO
                };
            }
            Svex<V> b = args[1];
            Vec4 bVal = b.xeval(xevalMemoize);
            BigInteger bNonZ = bVal.getLower().andNot(bVal.getUpper()).not();
            return new BigInteger[]
            {
                bNonZ, BigIntegerUtil.MINUS_ONE
            };
        }
    }
}
