/* -*- tab-width: 4 -*-
 *
 * Electric(tm) VLSI Design System
 *
 * File: IndexName.java
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
package com.sun.electric.tool.simulation.acl2.mods;

import com.sun.electric.tool.simulation.acl2.svex.Svar;
import com.sun.electric.tool.simulation.acl2.svex.SvarImpl;
import com.sun.electric.tool.simulation.acl2.svex.SvarName;
import static com.sun.electric.util.acl2.ACL2.*;
import com.sun.electric.util.acl2.ACL2Object;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class IndexName implements SvarName
{
    private final ACL2Object impl;

    public static ElabMod curElabMod = null;

    private IndexName(int index)
    {
        if (index < 0)
        {
            throw new IllegalArgumentException();
        }
        impl = honscopy(ACL2Object.valueOf(BigInteger.valueOf(index)));
    }

    @Override
    public ACL2Object getACL2Object()
    {
        return impl;
    }

    @Override
    public String toString()
    {
        return toString(null);
    }

    @Override
    public String toString(BigInteger mask)
    {
        String s = "{" + Integer.toString(impl.intValueExact());
        if (mask != null)
        {
            s += "#" + mask.toString(16);
        }
        s += "}";
        if (curElabMod != null)
        {
            s += curElabMod.wireidxToPath(getIndex());
        }
        return s;
    }

    public Name asName()
    {
        return new Name(impl);
    }

    public Path asPath()
    {
        return Path.simplePath(asName());
    }

    public Address asAddress()
    {
        return Address.fromACL2(impl);
    }

    public int getIndex()
    {
        return impl.intValueExact();
    }
    
    public static class SvarBuilder extends SvarImpl.Builder<IndexName>
    {
        private final List<IndexName> cache = new ArrayList<>();

        @Override
        public IndexName newName(ACL2Object nameImpl)
        {
            return newName(nameImpl.intValueExact());
        }

        public IndexName newName(int index)
        {
            while (index >= cache.size())
            {
                cache.add(null);
            }
            IndexName name = cache.get(index);
            if (name == null)
            {
                name = new IndexName(index);
                cache.set(index, name);
            }
            return name;
        }

        Svar<IndexName> setIndex(Svar<IndexName> svar, int index)
        {
            if (index < 0)
            {
                throw new IllegalArgumentException();
            }
            return newVar(ACL2Object.valueOf(index), svar.getDelay(), svar.isNonblocking());
        }
    }
}