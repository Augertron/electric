/* -*- tab-width: 4 -*-
 *
 * Electric(tm) VLSI Design System
 *
 * File: ModDb.java
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
import com.sun.electric.tool.simulation.acl2.svex.SvexCall;
import com.sun.electric.tool.simulation.acl2.svex.SvexQuote;
import com.sun.electric.tool.simulation.acl2.svex.SvexVar;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * SV module Database.
 * See <http://www.cs.utexas.edu/users/moore/acl2/manuals/current/manual/?topic=SV____MODDB>.
 */
public class ModDb
{
    final List<ElabMod> mods = new ArrayList<>();
    final Map<ModName, ElabMod> modnameIdxes = new HashMap<>();

    <V extends Svar> ModDb(ModName modName, Map<ModName, Module<V>> modalist)
    {
        moduleToDb(modName, modalist);
    }

    /**
     * See <http://www.cs.utexas.edu/users/moore/acl2/manuals/current/manual/?topic=SV____MODDB-MOD-NWIRES>
     *
     * @param modidx module index
     * @return number of local wires in the module
     */
    public int modNWires(int modidx)
    {
        return mods.get(modidx).wireTable.length;
    }

    /**
     * See <http://www.cs.utexas.edu/users/moore/acl2/manuals/current/manual/?topic=SV____MODDB-MOD-NINSTS>
     *
     * @param modidx module index
     * @return number of local insts in the module
     */
    public int modNInsts(int modidx)
    {
        return mods.get(modidx).modInstTable.length;
    }

    /**
     * See <http://www.cs.utexas.edu/users/moore/acl2/manuals/current/manual/?topic=SV____MODDB-MOD-TOTALWIRES>
     *
     * @param modidx module index
     * @return number of total wires in the module
     */
    public int modTotalWires(int modidx)
    {
        return mods.get(modidx).totalWires;
    }

    /**
     * See <http://www.cs.utexas.edu/users/moore/acl2/manuals/current/manual/?topic=SV____MODDB-MOD-TOTALINSTS>
     *
     * @param modidx module index
     * @return number of total instances in the module
     */
    public int modTotalInsts(int modidx)
    {
        return mods.get(modidx).totalWires;
    }

    /**
     * Convert a wire index to a path relative to the module it’s in
     * See <http://www.cs.utexas.edu/users/moore/acl2/manuals/current/manual/?topic=SV____MODDB-WIREIDX-_E3PATH>
     *
     * @param wireidx
     * @param modidx
     * @return
     */
    public Path wireidxToPath(int wireidx, int modidx)
    {
        ElabMod elabMod = mods.get(modidx);
        if (wireidx < 0 || wireidx >= elabMod.totalWires)
        {
            throw new IllegalArgumentException();
        }
        List<Name> stack = new LinkedList<>();
        while (wireidx >= elabMod.wireTable.length)
        {
            int instIdx = elabMod.wireFindInst(wireidx);
            ElabModInst elabModInst = elabMod.modInstTable[instIdx];
            stack.add(elabModInst.instName);
            instIdx -= elabModInst.wireOffrset;
            elabMod = elabModInst.modidx;
        }
        return Path.makePath(stack, elabMod.wireTable[wireidx].name);
    }

    /**
     * See <http://www.cs.utexas.edu/users/moore/acl2/manuals/current/manual/?topic=SV____MODDB-PATH-_E3WIREIDX>
     *
     * @param path
     * @param modidx
     * @return
     */
    public int pathToWireIdx(Path path, int modidx)
    {
        ElabMod elabMod = mods.get(modidx);
        int wireOffset = 0;
        while (path instanceof Path.Scope)
        {
            Path.Scope pathScope = (Path.Scope)path;
            ElabModInst instidx = elabMod.modInstNameIdxes.get(pathScope.namespace);
            if (instidx == null)
            {
                throw new RuntimeException("In module " + elabMod.modName + ": missing: " + pathScope.namespace);
            }
            wireOffset += instidx.wireOffrset;
            elabMod = instidx.modidx;
            path = pathScope.subpath;
        }
        Path.Wire pathWire = (Path.Wire)path;
        Integer wireIdx = elabMod.wireNameIdxes.get(pathWire.name);
        if (wireIdx == null)
        {
            throw new RuntimeException("In module " + elabMod.modName + ": missing: " + pathWire.name);
        }
        return wireOffset + wireIdx;
    }

    /**
     * See <http://www.cs.utexas.edu/users/moore/acl2/manuals/current/manual/?topic=SV____MODDB-PATH-_E3WIREIDX>
     *
     * @param path
     * @param modidx
     * @return
     */
    public Wire pathToWireDecl(Path path, int modidx)
    {
        ElabMod elabMod = mods.get(modidx);
        while (path instanceof Path.Scope)
        {
            Path.Scope pathScope = (Path.Scope)path;
            ElabModInst instidx = elabMod.modInstNameIdxes.get(pathScope.namespace);
            if (instidx == null)
            {
                throw new RuntimeException("In module " + elabMod.modName + ": missing: " + pathScope.namespace);
            }
            elabMod = instidx.modidx;
            path = pathScope.subpath;
        }
        Path.Wire pathWire = (Path.Wire)path;
        Integer wireIdx = elabMod.wireNameIdxes.get(pathWire.name);
        if (wireIdx == null)
        {
            throw new RuntimeException("In module " + elabMod.modName + ": missing: " + pathWire.name);
        }
        return elabMod.wireTable[wireIdx];
    }

    public int addressToWireIdx(Address addr, ModScope scope)
    {
        ModScope scope1 = addr.scope == Address.SCOPE_ROOT ? scope.top() : scope.nth(addr.scope);
        int localIdx = pathToWireIdx(addr.path, scope1.modIdx.index);
        return localIdx + scope1.wireOffset;
    }

    public Wire addressToWireDecl(Address addr, ModScope scope)
    {
        ModScope scope1 = addr.scope == Address.SCOPE_ROOT ? scope.top() : scope.nth(addr.scope);
        return pathToWireDecl(addr.path, scope1.modIdx.index);
    }

    public Path[] wireidxToPaths(int[] wires, int modidx)
    {
        Path[] result = new Path[wires.length];
        for (int i = 0; i < wires.length; i++)
        {
            result[i] = wireidxToPath(wires[i], modidx);
        }
        return result;
    }

    public SvarAddr svarNamedToIndexed(SvarAddr svar, int modidx)
    {
        Address addr = svar.getAddress();
        if (addr.scope != 0)
        {
            return svar.withIndex(Address.INDEX_NIL);
        }
        int idx = pathToWireIdx(addr.path, modidx);
        return svar.withIndex(idx);
    }

    private Svex<SvarAddr> svexNamedToIndex(Svex<SvarAddr> x, int modidx, Map<Svex<SvarAddr>, Svex<SvarAddr>> svexCache)
    {
        Svex<SvarAddr> result = svexCache.get(x);
        if (result == null)
        {
            if (x instanceof SvexVar)
            {
                SvexVar<SvarAddr> xv = (SvexVar<SvarAddr>)x;
                SvarAddr name = svarNamedToIndexed(xv.svar, modidx);
                result = new SvexVar<>(name);
            } else if (x instanceof SvexQuote)
            {
                result = x;
            } else
            {
                SvexCall<SvarAddr> sc = (SvexCall<SvarAddr>)x;
                Svex<SvarAddr>[] args = sc.getArgs();
                Svex<SvarAddr>[] newArgs = Svex.newSvexArray(args.length);
                for (int i = 0; i < args.length; i++)
                {
                    newArgs[i] = svexNamedToIndex(args[i], modidx, svexCache);
                }
                result = SvexCall.newCall(sc.fun, newArgs);
            }
            svexCache.put(x, result);
        }
        return result;
    }

    private Lhs<SvarAddr> lhsNamedToIndex(Lhs<SvarAddr> x, int modidx)
    {
        List<Lhrange<SvarAddr>> newRanges = new ArrayList<>();
        for (Lhrange<SvarAddr> range : x.ranges)
        {
            SvarAddr svar = range.getVar();
            if (svar != null)
            {
                svar = svarNamedToIndexed(svar, modidx);
                range = new Lhrange<>(range.getWidth(), new Lhatom.Var<>(svar, range.getRsh()));
            }
            newRanges.add(range);
        }
        return new Lhs<>(newRanges);
    }

    private Module<SvarAddr> moduleNamedToIndex(Module<SvarAddr> m, int modidx)
    {
        Map<Svex<SvarAddr>, Svex<SvarAddr>> svexCache = new HashMap<>();
        Map<Lhs<SvarAddr>, Driver<SvarAddr>> newAssigns = new LinkedHashMap<>();
        for (Map.Entry<Lhs<SvarAddr>, Driver<SvarAddr>> e : m.assigns.entrySet())
        {
            Lhs<SvarAddr> newLhs = lhsNamedToIndex(e.getKey(), modidx);
            Driver<SvarAddr> driver = e.getValue();
            Svex<SvarAddr> newSvex = svexNamedToIndex(driver.svex, modidx, svexCache);
            Driver<SvarAddr> newDriver = new Driver<>(newSvex, driver.strength);
            newAssigns.put(newLhs, newDriver);
        }
        Map<Lhs<SvarAddr>, Lhs<SvarAddr>> newAliasepairs = new LinkedHashMap<>();
        for (Map.Entry<Lhs<SvarAddr>, Lhs<SvarAddr>> e : m.aliaspairs.entrySet())
        {
            Lhs<SvarAddr> newLhs = lhsNamedToIndex(e.getKey(), modidx);
            Lhs<SvarAddr> newRhs = lhsNamedToIndex(e.getValue(), modidx);
            newAliasepairs.put(newLhs, newRhs);
        }
        return new Module<>(m.wires, m.insts, newAssigns, newAliasepairs);
    }

    public Map<ModName, Module<SvarAddr>> modalistNamedToIndex(Map<ModName, Module<SvarAddr>> modalist)
    {
        Map<ModName, Module<SvarAddr>> result = new LinkedHashMap<>();
        for (Map.Entry<ModName, Module<SvarAddr>> e : modalist.entrySet())
        {
            ModName modName = e.getKey();
            Module<SvarAddr> module = e.getValue();
            int modidx = modnameIdxes.get(modName).index;
            Module<SvarAddr> newModule = moduleNamedToIndex(module, modidx);
            result.put(modName, newModule);
        }
        return result;
    }

    private <V extends Svar> void moduleToDb(ModName modName, Map<ModName, Module<V>> modalist)
    {
        ElabMod elabMod = modnameIdxes.get(modName);
        if (elabMod != null)
        {
            return;
        }
        if (modnameIdxes.containsKey(modName))
        {
            throw new IllegalArgumentException("Module loop " + modName);
        }
        modnameIdxes.put(modName, null);
        Module<V> module = modalist.get(modName);
        if (module == null)
        {
            throw new IllegalArgumentException("Module not found " + modName);
        }
        for (ModInst modInst : module.insts)
        {
            moduleToDb(modInst.modname, modalist);
        }
        elabMod = new ElabMod(mods.size(), modName, module);
        mods.add(elabMod);
        ElabMod old = modnameIdxes.put(modName, elabMod);
        assert old == null;
    }

    public class ElabMod
    {
        final int index;
        final ModName modName;
        final Wire[] wireTable;
        final Map<Name, Integer> wireNameIdxes = new HashMap<>();
        final ElabModInst[] modInstTable;
        final Map<Name, ElabModInst> modInstNameIdxes = new HashMap<>();
        final int totalWires;
        final int totalInsts;
        final Module<?> origMod;
        final int modMeas;

        ElabMod(int index, ModName modName, Module<?> origMod)
        {
            this.index = index;
            this.modName = modName;
            this.origMod = origMod;
            wireTable = origMod.wires.toArray(new Wire[origMod.wires.size()]);
            for (int i = 0; i < wireTable.length; i++)
            {
                Wire wire = wireTable[i];
                Integer old = wireNameIdxes.put(wire.name, i);
                assert old == null;
            }
            modInstTable = new ElabModInst[origMod.insts.size()];
            int wireOfs = wireTable.length;
            int instOfs = modInstTable.length;
            int meas = 1;
            for (int i = 0; i < origMod.insts.size(); i++)
            {
                ModInst modInst = origMod.insts.get(i);
                ElabMod modidx = modnameIdxes.get(modInst.modname);
                if (modidx == null)
                {
                    throw new IllegalArgumentException();
                }
                ElabModInst elabModInst = new ElabModInst(i, modInst.instname, modidx, wireOfs, instOfs);
                modInstTable[i] = elabModInst;
                ElabModInst old = modInstNameIdxes.put(modInst.instname, elabModInst);
                assert old == null;
                wireOfs += modidx.totalWires;
                instOfs += modidx.totalInsts;
                meas += 1 + elabModInst.instMeas;
            }
            totalWires = wireOfs;
            totalInsts = instOfs;
            modMeas = meas;
        }

        int wireFindInst(int wire)
        {
            if (wire < wireTable.length || wire >= totalWires)
            {
                throw new IllegalArgumentException();
            }
            int minInst = 0;
            int minOffset = wireTable.length;
            int maxInst = modInstTable.length;
            int maxOffset = totalWires;
            while (maxInst > 1 + minInst)
            {
                int guess = (maxInst - minInst) >> 1;
                int pivot = minInst + guess;
                int pivotOffset = modInstTable[pivot].wireOffrset;
                if (wire < pivotOffset)
                {
                    maxInst = pivot;
                    maxOffset = pivotOffset;
                } else
                {
                    minInst = pivot;
                    minOffset = pivotOffset;
                }

            }
            return minInst;
        }
    }

    public static class ElabModInst
    {
        final int instIndex;
        final Name instName;
        final ElabMod modidx;
        final int wireOffrset;
        final int instOffset;
        final int instMeas;

        ElabModInst(int instIndex, Name instName, ElabMod modidx, int wireOffset, int instOffset)
        {
            this.instIndex = instIndex;
            this.instName = instName;
            this.modidx = modidx;
            this.wireOffrset = wireOffset;
            this.instOffset = instOffset;
            instMeas = modidx.modMeas + 1;
        }
    }

    public class ModScope
    {
        final ElabMod modIdx;
        final int wireOffset;
        final int instOffset;
        final ModScope upper;

        ModScope(ElabMod modIdx)
        {
            this(modIdx, 0, 0, null);
        }

        private ModScope(ElabMod modIdx, int wireOffset, int instOffset, ModScope upper)
        {
            this.modIdx = modIdx;
            this.wireOffset = wireOffset;
            this.instOffset = instOffset;
            this.upper = upper;
        }

        boolean okp()
        {
            if (mods.get(modIdx.index) != modIdx)
            {
                return false;
            }
            if (upper == null)
            {
                return wireOffset == 0 && instOffset == 0;
            } else
            {
                return upper.okp()
                    && upper.wireOffset <= wireOffset
                    && modIdx.totalWires + wireOffset <= upper.modIdx.totalWires + upper.wireOffset
                    && upper.instOffset <= instOffset
                    && modIdx.totalInsts + instOffset >= upper.modIdx.totalInsts + upper.instOffset;
            }
        }

        ModScope pushFrame(int instidx)
        {
            ElabModInst elabModInst = modIdx.modInstTable[instidx];
            return new ModScope(elabModInst.modidx,
                elabModInst.wireOffrset + wireOffset,
                elabModInst.instOffset + instOffset,
                this);
        }

        ModScope top()
        {
            ModScope result = this;
            while (result.upper != null)
            {
                result = result.upper;
            }
            return result;
        }

        ModScope nth(int n)
        {
            ModScope result = this;
            while (n > 0 && result.upper != null)
            {
                result = result.upper;
            }
            return result;
        }
    }
}
