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
import com.sun.electric.tool.simulation.acl2.svex.Svex;
import static com.sun.electric.util.acl2.ACL2.car;
import static com.sun.electric.util.acl2.ACL2.cdr;
import com.sun.electric.util.acl2.ACL2Object;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * SV module.
 * See <http://www.cs.utexas.edu/users/moore/acl2/manuals/current/manual/?topic=SV____MODULE>.
 *
 * @param <V> Type of Svex variables
 */
public class Module<V extends Svar>
{
    public final List<Wire> wires = new ArrayList<>();
    public final List<ModInst> insts = new ArrayList<>();
    public final Map<Lhs<V>, Driver<V>> assigns = new LinkedHashMap<>();
    public final Map<Lhs<V>, Lhs<V>> aliaspairs = new LinkedHashMap<>();

    Module(Svar.Builder<V> builder, ACL2Object impl)
    {
        List<ACL2Object> fields = Util.getList(impl, true);
        Util.check(fields.size() == 4);
        ACL2Object pair;

        pair = fields.get(0);
        Util.check(car(pair).equals(Util.SV_WIRES));
        for (ACL2Object o : Util.getList(cdr(pair), true))
        {
            Wire wire = new Wire(o);
            wires.add(wire);
        }

        pair = fields.get(1);
        Util.check(car(pair).equals(Util.SV_INSTS));
        for (ACL2Object o : Util.getList(cdr(pair), true))
        {
            ModInst modInst = new ModInst(o);
            insts.add(modInst);
        }
        pair = fields.get(2);
        Util.check(car(pair).equals(Util.SV_ASSIGNS));
        Map<ACL2Object, Svex<V>> svexCache = new HashMap<>();
        for (ACL2Object o : Util.getList(cdr(pair), true))
        {
            pair = o;
            Lhs<V> lhs = new Lhs<>(builder, car(pair));
            Driver<V> driver = new Driver<>(builder, svexCache, cdr(pair));
            Driver old = assigns.put(lhs, driver);
            Util.check(old == null);
        }

        pair = fields.get(3);
        Util.check(car(pair).equals(Util.SV_ALIASPAIRS));
        for (ACL2Object o : Util.getList(cdr(pair), true))
        {
            pair = o;
            Lhs<V> lhs = new Lhs<>(builder, car(pair));
            Lhs<V> rhs = new Lhs<>(builder, cdr(pair));
            Lhs old = aliaspairs.put(lhs, rhs);
            Util.check(old == null);
        }
    }
}