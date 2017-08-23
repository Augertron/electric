/* -*- tab-width: 4 -*-
 *
 * Electric(tm) VLSI Design System
 *
 * File: ModuleExt.java
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
import com.sun.electric.tool.simulation.acl2.svex.SvarName;
import com.sun.electric.tool.simulation.acl2.svex.Svex;
import com.sun.electric.tool.simulation.acl2.svex.SvexManager;
import static com.sun.electric.util.acl2.ACL2.*;
import com.sun.electric.util.acl2.ACL2Backed;
import com.sun.electric.util.acl2.ACL2Object;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * SV module.
 * See <http://www.cs.utexas.edu/users/moore/acl2/manuals/current/manual/?topic=SV____MODULE>.
 *
 * @param <N> Type of name of Svex variables
 */
public class Module<N extends SvarName>
{
    public final SvexManager<N> sm;

    public final List<Wire> wires = new ArrayList<>();
    public final List<ModInst> insts = new ArrayList<>();
    public final Map<Lhs<N>, Driver<N>> assigns = new LinkedHashMap<>();
    public final Map<Lhs<N>, Lhs<N>> aliaspairs = new LinkedHashMap<>();

    public Module(SvexManager<N> sm, Collection<Wire> wires, Collection<ModInst> insts,
        Map<Lhs<N>, Driver<N>> assigns, Map<Lhs<N>, Lhs<N>> aliaspairs)
    {
        this.sm = sm;
        this.wires.addAll(wires);
        this.insts.addAll(insts);
        this.assigns.putAll(assigns);
        this.aliaspairs.putAll(aliaspairs);
    }

    static <N extends SvarName> Module<N> fromACL2(SvarName.Builder<N> snb, ACL2Object impl)
    {
        SvexManager<N> sm = new SvexManager<>();
        List<ACL2Object> fields = Util.getList(impl, true);
        Util.check(fields.size() == 4);
        ACL2Object pair;

        pair = fields.get(0);
        Util.check(car(pair).equals(Util.SV_WIRES));
        List<Wire> wires = new ArrayList<>();
        for (ACL2Object o : Util.getList(cdr(pair), true))
        {
            Wire wire = new Wire(o);
            wires.add(wire);
        }

        pair = fields.get(1);
        Util.check(car(pair).equals(Util.SV_INSTS));
        List<ModInst> insts = new ArrayList<>();
        for (ACL2Object o : Util.getList(cdr(pair), true))
        {
            ModInst modInst = ModInst.fromACL2(o);
            insts.add(modInst);
        }
        pair = fields.get(2);
        Util.check(car(pair).equals(Util.SV_ASSIGNS));
        Map<Lhs<N>, Driver<N>> assigns = new LinkedHashMap<>();
        Map<ACL2Object, Svex<N>> svexCache = new HashMap<>();
        for (ACL2Object o : Util.getList(cdr(pair), true))
        {
            pair = o;
            Lhs<N> lhs = Lhs.fromACL2(snb, sm, car(pair));
            Driver<N> driver = Driver.fromACL2(snb, sm, cdr(pair), svexCache);
            Driver old = assigns.put(lhs, driver);
            Util.check(old == null);
        }

        pair = fields.get(3);
        Util.check(car(pair).equals(Util.SV_ALIASPAIRS));
        Map<Lhs<N>, Lhs<N>> aliaspairs = new LinkedHashMap<>();
        for (ACL2Object o : Util.getList(cdr(pair), true))
        {
            pair = o;
            Lhs<N> lhs = Lhs.fromACL2(snb, sm, car(pair));
            Lhs<N> rhs = Lhs.fromACL2(snb, sm, cdr(pair));
            Lhs old = aliaspairs.put(lhs, rhs);
            Util.check(old == null);
        }

        return new Module<>(sm, wires, insts, assigns, aliaspairs);
    }

    public ACL2Object getACL2Object()
    {
        Map<ACL2Backed, ACL2Object> backedCache = new HashMap<>();
        ACL2Object wiresList = NIL;
        for (int i = wires.size() - 1; i >= 0; i--)
        {
            wiresList = cons(wires.get(i).getACL2Object(), wiresList);
        }
        ACL2Object instsList = NIL;
        for (int i = insts.size() - 1; i >= 0; i--)
        {
            instsList = cons(insts.get(i).getACL2Object(backedCache), instsList);
        }
        ACL2Object assignsList = NIL;
        for (Map.Entry<Lhs<N>, Driver<N>> e : assigns.entrySet())
        {
            assignsList = cons(cons(e.getKey().getACL2Object(backedCache),
                e.getValue().getACL2Object(backedCache)), assignsList);
        }
        assignsList = Util.revList(assignsList);
        ACL2Object aliasesList = NIL;
        for (Map.Entry<Lhs<N>, Lhs<N>> e : aliaspairs.entrySet())
        {
            aliasesList = cons(cons(e.getKey().getACL2Object(backedCache),
                e.getValue().getACL2Object(backedCache)), aliasesList);
        }
        aliasesList = Util.revList(aliasesList);
        return hons(hons(Util.SV_WIRES, wiresList),
            hons(hons(Util.SV_INSTS, instsList),
                hons(hons(Util.SV_ASSIGNS, assignsList),
                    hons(hons(Util.SV_ALIASPAIRS, aliasesList),
                        NIL))));
    }

    @Override
    public boolean equals(Object o)
    {
        if (o instanceof Module)
        {
            Module<?> that = (Module<?>)o;
            return this.wires.equals(that.wires)
                && this.insts.equals(that.insts)
                && this.assigns.equals(that.assigns)
                && this.aliaspairs.equals(that.aliaspairs);
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        int hash = 3;
        hash = 89 * hash + wires.hashCode();
        hash = 89 * hash + insts.hashCode();
        hash = 89 * hash + assigns.hashCode();
        hash = 89 * hash + aliaspairs.hashCode();
        return hash;
    }

    void vars(Collection<Svar<N>> vars)
    {
        for (Map.Entry<Lhs<N>, Driver<N>> e : assigns.entrySet())
        {
            Lhs<N> lhs = e.getKey();
            Driver<N> driver = e.getValue();
            lhs.vars(vars);
            driver.vars(vars);
        }
        for (Map.Entry<Lhs<N>, Lhs<N>> e : aliaspairs.entrySet())
        {
            Lhs<N> lhs = e.getKey();
            Lhs<N> rhs = e.getValue();
            lhs.vars(vars);
            rhs.vars(vars);
        }
    }

}
