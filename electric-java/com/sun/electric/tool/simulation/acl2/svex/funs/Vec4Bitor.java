/* -*- tab-width: 4 -*-
 *
 * Electric(tm) VLSI Design System
 *
 * File: Vec4Bitor.java
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

import com.sun.electric.tool.simulation.acl2.svex.Svex;
import com.sun.electric.tool.simulation.acl2.svex.SvexCall;
import com.sun.electric.tool.simulation.acl2.svex.SvexFunction;
import com.sun.electric.tool.simulation.acl2.svex.Vec2;
import com.sun.electric.tool.simulation.acl2.svex.Vec4;
import java.math.BigInteger;

/**
 * Bitwise logical OR of 4vecs.
 * See<http://www.cs.utexas.edu/users/moore/acl2/manuals/current/manual/?topic=SV____4VEC-BITOR>.
 */
public class Vec4Bitor extends SvexCall
{
    public static final Function FUNCTION = new Function();
    public final Svex x;
    public final Svex y;

    public Vec4Bitor(Svex x, Svex y)
    {
        super(FUNCTION, x, y);
        this.x = x;
        this.y = y;
    }

    public static class Function extends SvexFunction
    {
        private Function()
        {
            super(FunctionSyms.SV_BITOR, 2);
        }

        @Override
        public Vec4Bitor build(Svex... args)
        {
            return new Vec4Bitor(args[0], args[1]);
        }

        @Override
        public Vec4 apply(Vec4... args)
        {
            return apply3(args[0].fix3(), args[1].fix3());
        }

        private Vec4 apply3(Vec4 x, Vec4 y)
        {
            if (x.isVec2() && y.isVec2())
            {
                BigInteger xv = ((Vec2)x).getVal();
                BigInteger yv = ((Vec2)x).getVal();
                return new Vec2(xv.or(yv));
            }
            return Vec4.valueOf(
                x.getUpper().or(y.getUpper()),
                x.getLower().or(y.getLower()));

        }
    }
}
