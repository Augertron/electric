/* -*- tab-width: 4 -*-
 *
 * Electric(tm) VLSI Design System
 *
 * File: SvexQuote.java
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

import com.sun.electric.tool.simulation.acl2.mods.Lhs;
import static com.sun.electric.util.acl2.ACL2.*;
import com.sun.electric.util.acl2.ACL2Object;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A "quoted constant" 4vec which represents itself.
 * See <http://www.cs.utexas.edu/users/moore/acl2/manuals/current/manual/?topic=SV____SVEX-QUOTE>.
 *
 * @param <N> Type of name of Svex variables
 */
public class SvexQuote<N extends SvarName> extends Svex<N>
{
    public final Vec4 val;

    private final ACL2Object impl;
    private static final int SMALL_CACHE_EXP_LIMIT = 10;
    private static final List<SvexQuote<?>> smallCache = new ArrayList<>();
    private static final Map<Vec4, SvexQuote<?>> largeCache = new HashMap<>();

    private static final SvexQuote<?> X = new SvexQuote<>(Vec4.X);
    private static final SvexQuote<?> Z = new SvexQuote<>(Vec4.Z);

    static
    {
        for (int v = 0; v < (1 << SMALL_CACHE_EXP_LIMIT); v++)
        {
            SvexQuote<?> sq = new SvexQuote<>(new Vec2(v));
            smallCache.add(sq);
        }
        largeCache.put(X.val, X);
        largeCache.put(Z.val, Z);
    }

    private SvexQuote(Vec4 val)
    {
        if (val == null)
        {
            throw new NullPointerException();
        }
        this.val = val;
        if (val.isVec2())
        {
            impl = honscopy(val.makeAcl2Object());
        } else
        {
            impl = hons(QUOTE, hons(val.makeAcl2Object(), NIL));
        }
    }

    public static <N extends SvarName> Svex<N> valueOf(Vec4 val)
    {
        if (val.isVec2())
        {
            BigInteger bv = ((Vec2)val).getVal();
            if (bv.signum() >= 0 && bv.bitLength() < SMALL_CACHE_EXP_LIMIT)
            {
                return (Svex<N>)smallCache.get(bv.intValueExact());
            }
        }
        SvexQuote<?> sv = largeCache.get(val);
        if (sv == null)
        {
            sv = new SvexQuote<>(val);
            largeCache.put(val, sv);
        }
        return (Svex<N>)sv;
    }

    public static <N extends SvarName> Svex<N> valueOf(BigInteger val)
    {
        if (val.signum() >= 0 && val.bitLength() < SMALL_CACHE_EXP_LIMIT)
        {
            return (Svex<N>)smallCache.get(val.intValueExact());
        }
        return valueOf(new Vec2(val));
    }

    public static <N extends SvarName> Svex<N> valueOf(int val)
    {
        if (val >= 0 && val < (1 << SMALL_CACHE_EXP_LIMIT))
        {
            return (Svex<N>)smallCache.get(val);
        }
        return valueOf(BigInteger.valueOf(val));
    }

    public static <N extends SvarName> Svex<N> X()
    {
        return (Svex<N>)X;
    }

    public static <N extends SvarName> Svex<N> Z()
    {
        return (Svex<N>)Z;
    }

    @Override
    public ACL2Object getACL2Object()
    {
        return impl;
    }

    @Override
    public <N1 extends SvarName> Svex<N1> convertVars(Svar.Builder<N1> builder, Map<Svex<N>, Svex<N1>> cache)
    {
        Svex<N1> svex = cache.get(this);
        if (svex == null)
        {
            svex = SvexQuote.valueOf(val);
            cache.put(this, svex);
        }
        return svex;
    }

    @Override
    public <N1 extends SvarName> Svex<N1> addDelay(int delay, Svar.Builder<N1> builder, Map<Svex<N>, Svex<N1>> cache)
    {
        Svex<N1> svex = cache.get(this);
        if (svex == null)
        {
            svex = SvexQuote.valueOf(val);
            cache.put(this, svex);
        }
        return svex;
    }

    @Override
    protected void collectVars(Set<Svar<N>> result, Set<SvexCall<N>> visited)
    {
    }

    @Override
    public <R, D> R accept(Visitor<N, R, D> visitor, D data)
    {
        return visitor.visitConst(val, data);
    }

    @Override
    public Vec4 xeval(Map<Svex<N>, Vec4> memoize)
    {
        return val;
    }

    @Override
    public Svex<N> patch(Map<Svar<N>, Vec4> subst, Map<SvexCall<N>, SvexCall<N>> memoize)
    {
        return this;
    }

    @Override
    public boolean isLhsUnbounded()
    {
        return val.equals(Vec4.Z);
    }

    @Override
    public boolean isLhs()
    {
        return val.equals(Vec4.Z);
    }

    @Override
    public Lhs<N> lhsBound(int w)
    {
        return new Lhs<>(Collections.emptyList());
    }

    @Override
    public Lhs<N> toLhs()
    {
        return new Lhs<>(Collections.emptyList());
    }

    @Override
    public boolean equals(Object o)
    {
        return o instanceof SvexQuote && val.equals(((SvexQuote)o).val);
    }

    @Override
    public int hashCode()
    {
        return val.hashCode();
    }
}