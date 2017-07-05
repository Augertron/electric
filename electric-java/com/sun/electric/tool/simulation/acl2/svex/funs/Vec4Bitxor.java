/* -*- tab-width: 4 -*-
 *
 * Electric(tm) VLSI Design System
 *
 * File: Vec4Bitxor.java
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

import com.sun.electric.tool.simulation.acl2.svex.SvarName;
import com.sun.electric.tool.simulation.acl2.svex.Svex;
import com.sun.electric.tool.simulation.acl2.svex.SvexCall;
import com.sun.electric.tool.simulation.acl2.svex.SvexFunction;
import com.sun.electric.tool.simulation.acl2.svex.Vec2;
import com.sun.electric.tool.simulation.acl2.svex.Vec4;
import java.math.BigInteger;
import java.util.Map;

/**
 * Bitwise logical XOR of 4vecs.
 * See<http://www.cs.utexas.edu/users/moore/acl2/manuals/current/manual/?topic=SV____4VEC-BITXOR>.
 *
 * @param <N> Type of name of Svex variables
 */
public class Vec4Bitxor<N extends SvarName> extends SvexCall<N>
{
    public static final Function FUNCTION = new Function();
    public final Svex<N> x;
    public final Svex<N> y;

    public Vec4Bitxor(Svex<N> x, Svex<N> y)
    {
        super(FUNCTION, x, y);
        this.x = x;
        this.y = y;
    }

    public static class Function extends SvexFunction
    {
        private Function()
        {
            super(FunctionSyms.SV_BITXOR, 2, "4vec-bitxor");
        }

        @Override
        public <N extends SvarName> Vec4Bitxor<N> build(Svex<N>[] args)
        {
            return new Vec4Bitxor<>(args[0], args[1]);
        }

        @Override
        public Vec4 apply(Vec4... args)
        {
            Vec4 x = args[0];
            Vec4 y = args[1];
            if (x.isVec2() && y.isVec2())
            {
                BigInteger xv = ((Vec2)x).getVal();
                BigInteger yv = ((Vec2)x).getVal();
                return new Vec2(xv.xor(yv));
            }
            BigInteger xmask = x.getUpper().xor(x.getLower())
                .or(y.getUpper().xor(y.getLower()));
            return Vec4.valueOf(
                x.getUpper().xor(y.getUpper()).or(xmask),
                x.getLower().xor(y.getLower()).andNot(xmask));
        }

        @Override
        protected <N extends SvarName> BigInteger[] svmaskFor(BigInteger mask, Svex<N>[] args, Map<Svex<N>, Vec4> xevalMemoize)
        {
            Svex<N> x = args[0];
            Svex<N> y = args[1];
            Vec4 xv = x.xeval(xevalMemoize);
            Vec4 yv = y.xeval(xevalMemoize);
            BigInteger xZ = xv.getLower().andNot(xv.getUpper());
            BigInteger yZ = yv.getLower().andNot(yv.getUpper());
            BigInteger sharedZs = xZ.and(yZ).and(mask);
            BigInteger xmNonZ = mask.andNot(xZ);
            BigInteger ymNonZ = mask.andNot(yZ);
            if (sharedZs.signum() == 0)
            {
                return new BigInteger[]
                {
                    ymNonZ, xmNonZ
                };
            }
            BigInteger yX = yv.getUpper().andNot(yv.getLower());
            BigInteger ymX = mask.and(yX);
            if (ymX.signum() == 0)
            {
                return new BigInteger[]
                {
                    ymNonZ, xmNonZ.or(sharedZs)
                };
            }
            return new BigInteger[]
            {
                ymNonZ.or(sharedZs), xmNonZ
            };
        }
    }
}
