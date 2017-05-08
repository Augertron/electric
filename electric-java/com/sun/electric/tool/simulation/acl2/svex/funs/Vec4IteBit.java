/* -*- tab-width: 4 -*-
 *
 * Electric(tm) VLSI Design System
 *
 * File: Vec4IteBit.java
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
 * Bitwise multiple if-then-elses of 4vecs; doesn’t unfloat then/else values.
 * See<http://www.cs.utexas.edu/users/moore/acl2/manuals/current/manual/?topic=SV____4VEC-BIT_F3>.
 */
public class Vec4IteBit extends SvexCall
{
    public static final Function FUNCTION = new Function();
    public final Svex test;
    public final Svex then;
    public final Svex els;

    public Vec4IteBit(Svex test, Svex then, Svex els)
    {
        super(FUNCTION, test, then, els);
        this.test = test;
        this.then = then;
        this.els = els;
    }

    public static class Function extends SvexFunction
    {
        private Function()
        {
            super(FunctionSyms.SV_BIT_QUEST, 3);
        }

        @Override
        public Vec4IteBit build(Svex... args)
        {
            return new Vec4IteBit(args[0], args[1], args[2]);
        }

        @Override
        public Vec4 apply(Vec4... args)
        {
            return apply3(args[0].fix3(), args[1], args[2]);
        }

        private Vec4 apply3(Vec4 test, Vec4 th, Vec4 el)
        {
            if (test.isVec2())
            {
                BigInteger testv = ((Vec2)test).getVal();
                return Vec4.valueOf(
                    th.getUpper().and(testv)
                        .or(el.getUpper().andNot(testv)),
                    th.getLower().and(testv)
                        .or(el.getLower().andNot(testv)));
            }
            BigInteger testX = test.getUpper().andNot(test.getLower());
            return Vec4.valueOf(
                th.getUpper().and(test.getLower())
                    .or(el.getUpper().andNot(test.getUpper()))
                    .or(testX.and(th.getUpper().or(th.getLower()).or(el.getUpper()).or(el.getLower()))),
                th.getLower().and(test.getLower())
                    .or(el.getLower().andNot(test.getUpper()))
                    .or(testX.and(th.getUpper()).and(th.getLower()).and(el.getUpper()).and(el.getLower())));
        }
    }
}
