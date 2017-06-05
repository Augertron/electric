/* -*- tab-width: 4 -*-
 *
 * Electric(tm) VLSI Design System
 *
 * File: Vec4Pow.java
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

import com.sun.electric.tool.simulation.acl2.svex.Svar;
import com.sun.electric.tool.simulation.acl2.svex.Svex;
import com.sun.electric.tool.simulation.acl2.svex.SvexCall;
import com.sun.electric.tool.simulation.acl2.svex.SvexFunction;
import com.sun.electric.tool.simulation.acl2.svex.Vec2;
import com.sun.electric.tool.simulation.acl2.svex.Vec4;
import java.math.BigInteger;
import java.util.Map;

/**
 * Power operator (** in SystemVerilog).
 * See<http://www.cs.utexas.edu/users/moore/acl2/manuals/current/manual/?topic=SV____4VEC-POW>.
 */
public class Vec4Pow<V extends Svar> extends SvexCall<V>
{
    public static final Function FUNCTION = new Function();
    public final Svex<V> x;
    public final Svex<V> y;

    public Vec4Pow(Svex<V> x, Svex<V> y)
    {
        super(FUNCTION, x, y);
        this.x = x;
        this.y = y;
    }

    public static class Function extends SvexFunction
    {
        private Function()
        {
            super(FunctionSyms.SV_POW, 2);
        }

        @Override
        public <V extends Svar> Vec4Pow<V> build(Svex<V>[] args)
        {
            return new Vec4Pow<>(args[0], args[1]);
        }

        @Override
        public Vec4 apply(Vec4... args)
        {
            Vec4 base = args[0];
            Vec4 exp = args[1];
            if (base.isVec2() && exp.isVec2())
            {
                BigInteger basev = ((Vec2)base).getVal();
                int expv = ((Vec2)exp).getVal().intValueExact();
                if (expv >= 0 || basev.abs().equals(BigInteger.ONE))
                {
                    return new Vec2(basev.pow(expv));
                }
                if (basev.signum() != 0)
                {
                    return Vec2.ZERO;
                }
            }
            return Vec4.X;
        }

        @Override
        protected <V extends Svar> BigInteger[] svmaskFor(BigInteger mask, Svex<V>[] args, Map<Svex<V>, Vec4> xevalMemoize)
        {
            return new BigInteger[]
            {
                v4maskAllOrNone(mask), v4maskAllOrNone(mask)
            };
        }
    }
}
