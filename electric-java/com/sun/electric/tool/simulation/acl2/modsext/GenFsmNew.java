/* -*- tab-width: 4 -*-
 *
 * Electric(tm) VLSI Design System
 *
 * File: GenFsmNew.java
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
package com.sun.electric.tool.simulation.acl2.modsext;

import com.sun.electric.tool.Job;
import com.sun.electric.tool.JobException;
import com.sun.electric.tool.simulation.acl2.mods.Address;
import com.sun.electric.tool.simulation.acl2.mods.ElabMod;
import com.sun.electric.tool.simulation.acl2.mods.Lhs;
import com.sun.electric.tool.simulation.acl2.mods.ModName;
import com.sun.electric.tool.simulation.acl2.mods.Module;
import com.sun.electric.tool.simulation.acl2.mods.Name;
import com.sun.electric.tool.simulation.acl2.mods.Path;
import com.sun.electric.tool.simulation.acl2.mods.Util;
import com.sun.electric.tool.simulation.acl2.mods.Wire;
import com.sun.electric.tool.simulation.acl2.svex.Svar;
import com.sun.electric.tool.simulation.acl2.svex.Svex;
import com.sun.electric.tool.simulation.acl2.svex.SvexCall;
import com.sun.electric.tool.simulation.acl2.svex.SvexQuote;
import com.sun.electric.tool.simulation.acl2.svex.SvexVar;
import com.sun.electric.tool.simulation.acl2.svex.Vec2;
import com.sun.electric.tool.simulation.acl2.svex.Vec4;
import com.sun.electric.tool.simulation.acl2.svex.funs.Vec4Concat;
import com.sun.electric.tool.user.User;
import com.sun.electric.util.TextUtils;
import com.sun.electric.util.acl2.ACL2Reader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 *
 */
public class GenFsmNew extends GenBase
{
    public static <H extends DesignHints> void genFsm(Class<H> cls, File saoFile, String designName)
    {
        new GenFsmJob(cls, saoFile, designName).startJob();
    }

    private final String[] clockNames =
    {
        "l2clk"
    };
    final Map<ModName, ParameterizedModule> modToParMod = new HashMap<>();
    final Map<ParameterizedModule, Map<String, ModName>> parModuleInstances = new LinkedHashMap<>();
    private final Set<Integer> vec4sizes = new TreeSet<>();
    private String designName;

    private final DesignHints designHints;
    private final List<ParameterizedModule> parameterizedModules;

    protected GenFsmNew(DesignHints designHints)
    {
        this.designHints = designHints;
        parameterizedModules = designHints.getParameterizedModules();
    }

    ParameterizedModule matchParameterized(ModName modName)
    {
        for (ParameterizedModule parMod : parameterizedModules)
        {
            if (parMod.setCurBuilder(modName))
            {
                return parMod;
            }
        }
        return null;
    }

    public void scanLib(File saoFile) throws IOException
    {
        ACL2Reader sr = new ACL2Reader(saoFile);
        DesignExt design = new DesignExt(sr.root, designHints);
        scanDesign(design);
        for (ModName modName : design.downTop.keySet())
        {
            if (!modToParMod.containsKey(modName))
            {
                System.out.println(modName);
            }
        }
    }

    public void showLibs()
    {
        System.out.println("========= Instances of libs ============");
        for (ParameterizedModule parModule : parameterizedModules)
        {
            Map<String, ModName> parInsts = parModuleInstances.get(parModule);
            if (!parInsts.isEmpty())
            {
                System.out.println(parModule);
                for (ModName modName : parInsts.values())
                {
                    System.out.println("   " + parModule.matchModName(modName));
                }
            }
        }
        System.out.println("vec4 sizes");
        for (Integer width : vec4sizes)
        {
            System.out.println("(def-4vec-p " + width + ")");
        }
    }

    void scanDesign(DesignExt design)
    {
        List<ParameterizedModule> parModules = parameterizedModules;
        for (ParameterizedModule parModule : parModules)
        {
            parModuleInstances.put(parModule, new TreeMap<>(TextUtils.STRING_NUMBER_ORDER));
        }
        for (Map.Entry<ModName, ModuleExt> e : design.downTop.entrySet())
        {
            ModName modName = e.getKey();
            ModuleExt m = e.getValue();
            boolean found = false;
            for (ParameterizedModule parModule : parModules)
            {
                if (parModule.setCurBuilder(modName))
                {
                    assert !found;
                    found = true;
                    Map<String, ModName> parInsts = parModuleInstances.get(parModule);
                    assert parInsts != null;
                    parInsts.put(modName.toString(), modName);
                    modToParMod.put(modName, parModule);
                    Module<Address> genM = parModule.genModule();
                    if (genM == null)
                    {
                        System.out.println("Module specalizition is unfamiliar " + modName);
                    } else if (!genM.equals(m.b))
                    {
                        System.out.println("Module mismatch " + modName);
                    } else
                    {
                        Util.check(parModule.getNumInsts() == m.elabMod.modNInsts());
                        Util.check(parModule.getNumWires() == m.elabMod.modNWires());
                        Util.check(parModule.getNumAssigns() == m.elabMod.modNAssigns());
                        Util.check(parModule.getNumBits() == m.elabMod.modNBits());
                        Util.check(parModule.getTotalInsts() == m.elabMod.modTotalInsts());
                        Util.check(parModule.getTotalWires() == m.elabMod.modTotalWires());
                        Util.check(parModule.getTotalAssigns() == m.elabMod.modTotalAssigns());
                        if (parModule.getTotalBits() != m.elabMod.modTotalBits())
                            Util.check(parModule.getTotalBits() == m.elabMod.modTotalBits());
                    }
                }
            }
            for (Wire wire : m.b.wires)
            {
                vec4sizes.add(wire.width);
            }
            for (Lhs<?> lhs : m.b.assigns.keySet())
            {
                vec4sizes.add(lhs.width());
            }
        }
    }

    void gen(String designName, DesignExt design, File outDir) throws FileNotFoundException
    {
        scanDesign(design);
        this.designName = designName;

        File readSaoFile = new File(outDir, designName + "-sao.lisp");
        try (PrintStream out = new PrintStream(readSaoFile))
        {
            this.out = out;
            printReadSao();
        } finally
        {
            this.out = null;
        }

        String clockName = designHints.getGlobalClock();
        design.computeCombinationalInputs(clockName);

        for (Map.Entry<ParameterizedModule, Map<String, ModName>> e : parModuleInstances.entrySet())
        {
            ParameterizedModule parMod = e.getKey();
            Map<String, ModName> specializations = e.getValue();
            if (!parMod.hasState() || specializations.isEmpty())
            {
                continue;
            }

            File statesFile = new File(outDir, parMod.modName + "-st.lisp");
            try (PrintStream out = new PrintStream(statesFile))
            {
                this.out = out;
                printPhaseStates(design, parMod, specializations.values());
            } finally
            {
                this.out = null;
            }
        }
        for (Map.Entry<ModName, ModuleExt> e : design.downTop.entrySet())
        {
            ModName modName = e.getKey();
            ModuleExt m = e.getValue();
            if (modToParMod.containsKey(modName) || !m.hasSvtvState)
            {
                continue;
            }

            File statesFile = new File(outDir, modName + "-st.lisp");
            try (PrintStream out = new PrintStream(statesFile))
            {
                this.out = out;
                printPhaseStates(design, null, Collections.singleton(modName));
            } finally
            {
                this.out = null;
            }
        }

        for (Map.Entry<ParameterizedModule, Map<String, ModName>> e : parModuleInstances.entrySet())
        {
            ParameterizedModule parMod = e.getKey();
            Map<String, ModName> specializations = e.getValue();
            if (specializations.isEmpty())
            {
                continue;
            }

            File locFile = new File(outDir, parMod.modName + "-loc.lisp");
            try (PrintStream out = new PrintStream(locFile))
            {
                this.out = out;
                printLocs(design, parMod, specializations.values());
            } finally
            {
                this.out = null;
            }
        }
        for (ModName modName : design.downTop.keySet())
        {
            if (modToParMod.containsKey(modName))
            {
                continue;
            }

            File statesFile = new File(outDir, modName + "-loc.lisp");
            try (PrintStream out = new PrintStream(statesFile))
            {
                this.out = out;
                printLocs(design, null, Collections.singleton(modName));
            } finally
            {
                this.out = null;
            }
        }

        for (Map.Entry<ParameterizedModule, Map<String, ModName>> e : parModuleInstances.entrySet())
        {
            ParameterizedModule parMod = e.getKey();
            Map<String, ModName> specializations = e.getValue();
            if (specializations.isEmpty() || !parMod.exportsAreStrings())
            {
                continue;
            }

            File locFile = new File(outDir, parMod.modName + "-svtv.lisp");
            try (PrintStream out = new PrintStream(locFile))
            {
                this.out = out;
                printSvtvs(design, parMod, specializations.values());
            } finally
            {
                this.out = null;
            }
        }
        for (Map.Entry<ModName, ModuleExt> e : design.downTop.entrySet())
        {
            ModName modName = e.getKey();
            ModuleExt m = e.getValue();
            if (modToParMod.containsKey(modName))
            {
                continue;
            }

            File statesFile = new File(outDir, modName + "-svtv.lisp");
            try (PrintStream out = new PrintStream(statesFile))
            {
                this.out = out;
                printSvtvs(design, null, Collections.singleton(modName));
            } finally
            {
                this.out = null;
            }
        }
    }

    private void printReadSao()
    {
        s("(in-package \"SV\")");
        s("(include-book \"std/util/defconsts\" :dir :system)");
        s("(include-book \"std/util/define\" :dir :system)");
        s("(include-book \"../4vec-nnn\")");
        s("(include-book \"../design-fetch-svex\")");
        s();
        s("(defconsts (*" + designName + "-sao* state)");
        sb("(serialize-read \"" + designName + ".sao\"))");
        e();
        s();
        s("(define " + designName + "-sao ()");
        sb(":returns (design design-p)");
        s("*" + designName + "-sao*)");
        e();
        s();
        s("(in-theory (disable (:executable-counterpart " + designName + "-sao)))");
        s();
        s("(define " + designName + "-sao-fetch-svex-guard (modname assign-idx)");
        sb(":returns (ok booleanp)");
        s("(design-fetch-svex-guard (" + designName + "-sao) modname assign-idx))");
        e();
        s();
        s("(define " + designName + "-sao-fetch-svex");
        sb("(modname");
        sb("assign-idx)");
        e();
        s(":guard (" + designName + "-sao-fetch-svex-guard modname assign-idx)");
        s(":returns (svex svex-p)");
        s("(design-fetch-svex (" + designName + "-sao) modname assign-idx)");
        s(":guard-hints ((\"goal\" :in-theory (enable " + designName + "-sao-fetch-svex-guard))))");
        e();
        s();
        s("(define " + designName + "-sao-svex-eval");
        sb("(modname");
        sb("assign-idx");
        s("(width posp)");
        s("(env svex-env-p))");
        e();
        s(":guard (" + designName + "-sao-fetch-svex-guard modname assign-idx)");
        s(":returns (result (4vec-n-p width result) :hyp (posp width))");
        s(":guard-hints ((\"goal\" :in-theory (enable " + designName + "-sao-fetch-svex-guard)))");
        s("(let*");
        sb("((svex (" + designName + "-sao-fetch-svex modname assign-idx))");
        sb("(width (pos-fix width))");
        s("(svex (list 'concat width svex 0)))");
        e();
        s("(with-fast-alist env (svex-eval svex env)))");
        e();
        s("///");
        s("(deffixequiv " + designName + "-sao-svex-eval))");
        e();
        assert indent == 0;
    }

    private void printPhaseStates(DesignExt design, ParameterizedModule parMod, Collection<ModName> modNames)
    {
        s("(in-package \"SV\")");
        s("(include-book \"centaur/fty/top\" :dir :system)");
        s("(include-book \"../4vec-nnn\")");
        s();
        s("(set-rewrite-stack-limit 2000)");
        Set<String> imports = new HashSet<>();
        for (ModName modName : modNames)
        {
            ModuleExt m = design.downTop.get(modName);
            if (!m.hasPhaseState)
            {
                continue;
            }
            for (ModInstExt inst : m.insts)
            {
                ModuleExt proto = inst.proto;
                if (proto.hasPhaseState)
                {
                    ParameterizedModule protoParMod = modToParMod.get(proto.modName);
                    String importStr = protoParMod != null ? protoParMod.modName : proto.modName.toString();
                    if (!imports.contains(importStr))
                    {
                        s("(include-book \"" + importStr + "-st\")");
                        imports.add(importStr);
                    }
                }
            }
        }

        for (ModName modName : modNames)
        {
            ModuleExt m = design.downTop.get(modName);
            if (m.hasPhaseState)
            {
                s();
                s("; " + modName);
                printPhaseState(modName, m);
                if (m.hasCycleState)
                {
                    printCycleState(modName, m);
                }
            }
        }
    }

    private void printLocs(DesignExt design, ParameterizedModule parMod, Collection<ModName> modNames)
    {
        s("(in-package \"SV\")");
        s("(include-book \"centaur/fty/top\" :dir :system)");
        s("(include-book \"../4vec-nnn\")");
        s("(include-book \"" + designName + "-sao\")");
        for (ModName modName : modNames)
        {
            ModuleExt m = design.downTop.get(modName);
            printPhase2(modName, m);
        }
    }

    private void printSvtvs(DesignExt design, ParameterizedModule parMod, Collection<ModName> modNames)
    {
        ModName modName0 = modNames.iterator().next();
        ModuleExt m0 = design.downTop.get(modName0);
        String parModName = parMod != null ? parMod.modName : modNames.iterator().next().toString();
        s("(in-package \"SV\")");
        s();
//        s("(include-book \"centaur/misc/tshell\" :dir :system)");
//        s("(include-book \"centaur/sv/svtv/process\" :dir :system)");
        s("(include-book \"centaur/gl/gl\" :dir :system)");
        s("(include-book \"centaur/gl/bfr-satlink\" :dir :system)");
        s("(include-book \"centaur/sv/svtv/top\" :dir :system)");
        s("(include-book \"" + designName + "-sao\")");
        if (parMod != null ? parMod.hasState() : m0.hasSvtvState)
        {
            s("(include-book \"" + parModName + "-st\")");
        }
        s();
        s("(local (include-book \"centaur/sv/svex/gl-rules\" :dir :system))");
        s("(local (include-book \"centaur/bitops/top\" :dir :system))");
        s();
        s("(value-triple (acl2::tshell-ensure))");
        s("(local (include-book \"centaur/aig/g-aig-eval\" :dir :system))");
        s("(local (gl::def-gl-clause-processor boothpipe-glcp))");
        s();
        s("(local (gl::gl-satlink-mode))");

        for (ModName modName : modNames)
        {
            ModuleExt m = design.downTop.get(modName);
            printSvtv(design, modName, m);
        }
    }

    private void printPhase2(ModName modName, ModuleExt m)
    {
        s();
        s("; " + modName);
        int assignIndex = 0;
        for (Map.Entry<Lhs<PathExt>, DriverExt> e1 : m.assigns.entrySet())
        {
            Lhs<PathExt> lhs = e1.getKey();
            DriverExt drv = e1.getValue();
            s();
            s("(define |" + modName + "-" + lhs + "-loc| (");
            b();
            b();
            Set<Svar<PathExt>> svars = drv.collectVars();
            for (Svar<PathExt> svar : svars)
            {
                WireExt lw = (WireExt)svar.getName();
                s("(|" + svar + "| 4vec-" + lw.getWidth() + "-p)");
            }
            out.print(")");
            e();
            s(":returns (|" + lhs + "| 4vec-" + lhs.width() + "-p)");
            s("(let ((env (list");
            b();
            for (Svar<PathExt> svar : svars)
            {
                WireExt lw = (WireExt)svar.getName();
                String s = "(cons ";
                if (svar.getDelay() == 0)
                {
                    s += "\"" + lw.getName() + "\"";
                } else
                {
                    s += "'(:var " + "\"" + lw.getName() + "\" . " + svar.getDelay() + ")";
                }
                s += " (4vec-" + lw.getWidth() + "-fix |" + svar + "|))";
                s(s);
            }
            out.print(")))");
            e();
            s("(" + designName + "-sao-svex-eval " + modName.toLispString() + " "
                + assignIndex + " " + lhs.width() + " " + "env))");
            s("///");
            s("(deffixequiv |" + modName + "-" + lhs + "-loc|))");
//                        out.print("  (declare (ignore");
//                        for (Svar<PathExt> svar : svars)
//                        {
//                            out.print(" |" + svar + "|");
//                        }
//                        out.print("))");
//                        printSvex(out, drv.getSvex(), lhs.width());
            e();
            if (svars.isEmpty())
            {
                s("(in-theory (disable (|" + modName + "-" + lhs + "-loc|)))");
            }
            assignIndex++;
        }
    }

    private void printPhaseState(ModName modName, ModuleExt m)
    {
        s();
        s("(defprod |" + modName + "-phase-st| (");
        b();
        b();
        for (WireExt wire : m.wires)
        {
            if (!m.stateWires.contains(wire))
            {
                continue;
            }
            Svar<PathExt> svar = wire.getVar(1);
            if (m.stateVars0.containsKey(svar) || m.stateVars1.containsKey(svar))
            {
                s("(|" + wire + "| 4vec-" + wire.getWidth() + ")");
            }
        }
        for (ModInstExt inst : m.insts)
        {
            if (inst.proto.hasPhaseState)
            {
                s("(|" + inst.getInstname() + "| |" + inst.proto.modName + "-phase-st|)");
            }
        }
        out.print(")");
        e();
        s(":layout :fulltree)");
        e();
        assert indent == 0;
    }

    private void printCycleState(ModName modName, ModuleExt m)
    {
        s();
        s("(defprod |" + modName + "-cycle-st| (");
        b();
        b();
        for (WireExt wire : m.wires)
        {
            if (!m.stateWires.contains(wire))
            {
                continue;
            }
            Svar<PathExt> svar = wire.getVar(1);
            if (m.stateVars0.containsKey(svar))
            {
                s("(|" + wire + "| 4vec-" + wire.getWidth() + ")");
            }
        }
        for (ModInstExt inst : m.insts)
        {
            if (inst.proto.hasCycleState)
            {
                s("(|" + inst.getInstname() + "| |" + inst.proto.modName + "-cycle-st|)");
            }
        }
        out.print(")");
        e();
        s(":layout :fulltree)");
        e();
        assert indent == 0;
    }

    private void printSvtv(DesignExt design, ModName modName, ModuleExt m)
    {
        ElabMod modIdx = design.moddb.modnameGetIndex(modName);
        s();
        s("(defconst |*" + modName + "-design*|");
        sb("(change-design *" + designName + "-sao* :top " + modName.toLispString() + "))");
        e();
        s();
        s("(defsvtv |" + modName + "-phase|");
        sb(":mod |*" + modName + "-design*|");
        s(":inputs '(");
        b();
        for (WireExt wire : m.wires)
        {
            if (wire.isInput())
            {
                s("(" + wire.b.name.toLispString() + " |" + wire.b.name + "|)");
            }
        }
        out.print(")");
        e();
        s(":outputs '(");
        b();
        for (WireExt wire : m.wires)
        {
            if (wire.isOutput())
            {
                s("(" + wire.b.name.toLispString() + " |" + wire.b.name + "|)");
            }
        }
        out.print(")");
        e();
        s(":state-machine t)");
        e();
        s();
        s("(rule");
        sb("(equal");
        sb("(strip-cars (svtv->outexprs (|" + modName + "-phase|))) '(");
        b();
        for (WireExt wire : m.wires)
        {
            if (wire.isOutput())
            {
                s("|" + wire.b.name + "|");
            }
        }
        out.print("))");
        e();
        s(":enable ((|" + modName + "-phase|)))");
        e();
        e();
        s();
        Path[] svtvState = makeSvtvState(modName, design);
        String[] svtvVars = new String[svtvState.length];
        Svar.Builder svarBuilder = new Path.SvarBuilder();
        for (int i = 0; i < svtvState.length; i++)
        {
            Path path = svtvState[i];
            Svar<Path> svar = svarBuilder.newVar(path, 1, false);
            svtvVars[i] = svar.makeACL2Object().rep();
        }
        s("(rule");
        sb("(equal");
        sb("(strip-cars (svtv->nextstate (|" + modName + "-phase|))) '(");
        b();
        for (int i = 0; i < svtvState.length; i++)
        {
            s(svtvVars[i]);
        }
        out.print("))");
        e();
        e();
        s(":enable ((|" + modName + "-phase|)))");
        e();
        s();
        s("(define |" + modName + "-truncate-in|");
        sb("((in svex-env-p))");
        s(":guard (equal (alist-keys in) '(");
        b();
        for (WireExt wire : m.wires)
        {
            if (wire.isInput())
            {
                s("|" + wire.b.name.toString() + "|");
            }
        }
        out.print("))");
        e();
        s(":returns (new-in (and (svex-env-p new-in) (equal (alist-keys new-in) '(");
        b();
        for (WireExt wire : m.wires)
        {
            if (wire.isInput())
            {
                s("|" + wire.b.name.toString() + "|");
            }
        }
        out.print("))))");
        e();
        s("(list");
        b();
        for (WireExt wire : m.wires)
        {
            if (wire.isInput())
            {
                s("(cons '|" + wire.b.name.toString() + "|"
                    + " (4vec-concat " + wire.getWidth()
                    + " (svex-env-lookup '|" + wire.b.name.toString() + "| in) 0))");
            }
        }
        out.print("))");
        e();
        e();
        if (m.hasPhaseState)
        {
            s();
            s("(define |" + modName + "-truncate-st|");
            sb("((st svex-env-p))");
            s(":guard (equal (alist-keys st) '(svex-alist-keys (svtv->nextstate (|" + modName + "-phase|))))");
            s(":returns");
            s("(new-st (and (svex-env-p new-st)");
            sb("(equal (alist-keys new-st) (svex-alist-keys (svtv->nextstate (|" + modName + "-phase|)))))");
            s(":hints ((\"goal\" :in-theory (enable (|" + modName + "-phase|)))))");
            e();
            s("(let (");
            b();
            b();
            for (int i = 0; i < svtvState.length; i++)
            {
                s("(key-" + i + " '" + svtvVars[i] + ")");
            }
            out.print(")");
            e();
            s("(list");
            b();
            for (int i = 0; i < svtvState.length; i++)
            {
                int width = modIdx.pathToWireDecl(svtvState[i]).width;
                s("(cons key-" + i + " (4vec-concat " + width + " (svex-env-lookup key-" + i + " st) 0))");
            }
            out.print(")))");
            e();
            e();
            e();
        }
        s();
        s("(local (gl::def-gl-rule |" + modName + "-truncate-vectors-lemma|");
        sb(":hyp t");
        s(":concl");
        s("(b*");
        sb("(");
        sb("(in0 `(");
        b();
        for (WireExt wire : m.wires)
        {
            if (wire.isInput())
            {
                s("(|" + wire.b.name.toString() + "| . ,|" + wire.b.name.toString() + "|)");
            }
        }
        out.print("))");
        e();
        s("(in0 (make-fast-alist in0))");
        s("(ins0 (list in0))");
        s("(st0 `(");
        b();
        for (int i = 0; i < svtvState.length; i++)
        {
            s("(" + svtvVars[i] + " . ,key-" + i + ")");
        }
        out.print("))");
        e();
        s("(st0 (make-fast-alist st0))");
        s("(in1 `(");
        b();
        for (WireExt wire : m.wires)
        {
            if (wire.isInput())
            {
                s("(|" + wire.b.name.toString()
                    + "| . ,(4vec-concat " + wire.getWidth()
                    + " |" + wire.b.name.toString() + "| 0))");

            }
        }
        out.print("))");
        e();
        s("(in1 (make-fast-alist in1))");
        s("(ins1 (list in1))");
        s("(st1 `(");
        b();
        for (int i = 0; i < svtvState.length; i++)
        {
            int width = modIdx.pathToWireDecl(svtvState[i]).width;
            s("(" + svtvVars[i] + " . ,(4vec-concat " + width
                + " key-" + i + " 0))");
        }
        out.print("))");
        e();
        s("(st1 (make-fast-alist st1))");
        s("(out-signals (list (svex-alist-keys (svtv->outexprs (|" + modName + "-phase|)))))");
        s("(state-signals (list (svex-alist-keys (svtv->nextstate (|" + modName + "-phase|)))))");
        s("((mv outs0 states0)");
        sb("(svtv-fsm-run-outs-and-states");
        sb("ins0 st0 (|" + modName + "-phase|)");
        s(":out-signals out-signals :state-signals state-signals))");
        e();
        e();
        s("((mv outs1 states1)");
        sb("(svtv-fsm-run-outs-and-states");
        sb("ins1 st1 (|" + modName + "-phase|)");
        s(":out-signals out-signals :state-signals state-signals)))");
        e();
        e();
        e();
        s("(and (equal outs0 outs1) (equal states0 states1)))");
        e();
        s(":g-bindings nil");
        s(":rule-classes ()");
        s(":ctrex-transform (lambda (x) (ctrex-clean-envs '(");
        sb("(ins0 . :fast-alist-list)");
        s("(ins1 . :fast-alist-list)");
        s("(st0 . :fast-alist)");
        s("(st1 . :fast-alist))");
        s("x))))");
        e();
        e();
    }

    private void printSvex(Svex<PathExt> top, int width)
    {
        top = SvexCall.newCall(Vec4Concat.FUNCTION,
            SvexQuote.valueOf(width),
            top,
            SvexQuote.valueOf(0));
        Set<SvexCall<PathExt>> multirefs = top.multirefs();
        int indent = 2;
        Map<Svex<PathExt>, String> multirefNames = Collections.emptyMap();
        if (!multirefs.isEmpty())
        {
            out.println(" ;; MULTIREFS " + multirefs.size());
            out.print("  (let* (");
            multirefNames = new HashMap<>();
            Svex<PathExt>[] toposort = top.toposort();
//            for (int i = 0; i < toposort.length; i++)
            for (int i = toposort.length - 1; i >= 0; i--)
            {
                Svex<PathExt> svex = toposort[i];
                if (svex instanceof SvexCall && multirefs.contains((SvexCall<PathExt>)svex))
                {
                    String name = "temp" + multirefNames.size();
                    out.println();
                    out.print("   (" + name);
                    printSvex(svex, multirefNames, 4);
                    out.print(')');
                    multirefNames.put(svex, name);
                }
            }
            out.print(')');
            indent = 4;
        }
        printSvex(top, multirefNames, indent);
        out.println(multirefs.isEmpty() ? ")" : "))");
    }

    private void printSvex(Svex<PathExt> top, Map<Svex<PathExt>, String> multirefsNames, int indent)
    {
        out.println();
        for (int i = 0; i < indent; i++)
        {
            out.print(' ');
        }
        if (top instanceof SvexQuote)
        {
            SvexQuote<PathExt> sq = (SvexQuote<PathExt>)top;
            if (sq.val.isVec2())
            {
                Vec2 val = (Vec2)sq.val;
                out.print(val.getVal());
            } else if (sq.val.equals(Vec4.X))
            {
                out.print("(4vec-x)");
            } else if (sq.val.equals(Vec4.Z))
            {
                out.print("(4vec-z)");
            } else
            {
                out.print("'(" + sq.val.getUpper() + " . " + sq.val.getLower() + ")");
            }
        } else if (top instanceof SvexVar)
        {
            SvexVar<PathExt> sv = (SvexVar<PathExt>)top;
            out.print("|" + sv.svar + "|");
        } else
        {
            String name = multirefsNames.get(top);
            if (name != null)
            {
                out.print(name);
            } else
            {
                SvexCall<PathExt> sc = (SvexCall<PathExt>)top;
                out.print("(" + sc.fun.applyFn);
                for (Svex<PathExt> arg : sc.getArgs())
                {
                    printSvex(arg, multirefsNames, indent + 1);
                }
                out.print(')');
            }
        }
    }

    private Path[] makeSvtvState(ModName modName, DesignExt design)
    {
        List<Name> scopes = new ArrayList<>();
        Set<Path> statePaths = new LinkedHashSet<>();
        makeSvtvState(scopes, modName, Collections.emptyMap(), design, statePaths);
        Path[] result = new Path[statePaths.size()];
        int i = result.length;
        for (Iterator<Path> it = statePaths.iterator(); it.hasNext();)
        {
            result[--i] = it.next();
        }
        assert i == 0;
        return result;
    }

    private void makeSvtvStateBad(List<Name> scopes, ModName modName, Map<Name, Path[]> bind, DesignExt design, Set<Path> statePaths)
    {
        ModuleExt mod = design.downTop.get(modName);

        for (int i = mod.insts.size() - 1; i >= 0; i--)
        {
            ModInstExt inst = mod.insts.get(i);
            if (inst.proto.hasSvtvState)
            {
                Map<Name, Path[]> subBind = new HashMap<>();
                for (PathExt.PortInst pi : inst.portInsts)
                {
                    Path[] bits = new Path[pi.getWidth()];
                    for (int bit = 0; bit < bits.length; bit++)
                    {
                        Path path;
                        PathExt.Bit pb = pi.getParentBit(bit);
                        if (pb.getPath() instanceof PathExt.PortInst)
                        {
                            assert pb.getPath() == pi;
                            scopes.add(inst.getInstname());
                            path = Path.makePath(scopes, pi.getProtoName());
                            scopes.remove(scopes.size() - 1);
                        } else
                        {
                            WireExt lw = (WireExt)pb.getPath();
                            Path[] paths = bind.get(lw.getName());
                            if (paths != null)
                            {
                                path = paths[pb.bit];
                            } else
                            {
                                path = Path.makePath(scopes, lw.getName());
                            }
                        }
                        bits[bit] = path;
                    }
                    subBind.put(pi.getProtoName(), bits);
                }
                scopes.add(inst.getInstname());
                makeSvtvState(scopes, inst.getModname(), subBind, design, statePaths);
                scopes.remove(scopes.size() - 1);
            }
        }
        //
        WireExt[] wiresArr = mod.stateWires.toArray(new WireExt[mod.stateWires.size()]);
        for (int i = wiresArr.length - 1; i >= 0; i--)
        {
            WireExt wire = wiresArr[i];
            Name name = wire.getName();
            Path[] paths = bind.get(name);
            if (paths != null)
            {
                assert paths.length == wire.getWidth();
                for (Path path : paths)
                {
                    statePaths.add(path);
                }
            } else
            {
                Path path = Path.makePath(scopes, wire.getName());
                statePaths.add(path);
            }
        }
    }

    private void makeSvtvState(List<Name> scopes, ModName modName, Map<Name, Path[]> bind, DesignExt design, Set<Path> statePaths)
    {
        ModuleExt mod = design.downTop.get(modName);

        //
        for (WireExt wire : mod.stateWires)
        {
            Name name = wire.getName();
            Path[] paths = bind.get(name);
            if (paths != null)
            {
                assert paths.length == wire.getWidth();
                for (Path path : paths)
                {
                    statePaths.add(path);
                }
            } else
            {
                Path path = Path.makePath(scopes, wire.getName());
                statePaths.add(path);
            }
        }
        for (ModInstExt inst : mod.insts)
        {
            if (inst.proto.hasSvtvState)
            {
                Map<Name, Path[]> subBind = new HashMap<>();
                for (PathExt.PortInst pi : inst.portInsts)
                {
                    Path[] bits = new Path[pi.getWidth()];
                    for (int bit = 0; bit < bits.length; bit++)
                    {
                        Path path;
                        PathExt.Bit pb = pi.getParentBit(bit);
                        if (pb.getPath() instanceof PathExt.PortInst)
                        {
                            assert pb.getPath() == pi;
                            scopes.add(inst.getInstname());
                            path = Path.makePath(scopes, pi.getProtoName());
                            scopes.remove(scopes.size() - 1);
                        } else
                        {
                            WireExt lw = (WireExt)pb.getPath();
                            Path[] paths = bind.get(lw.getName());
                            if (paths != null)
                            {
                                path = paths[pb.bit];
                            } else
                            {
                                path = Path.makePath(scopes, lw.getName());
                            }
                        }
                        bits[bit] = path;
                    }
                    subBind.put(pi.getProtoName(), bits);
                }
                scopes.add(inst.getInstname());
                makeSvtvState(scopes, inst.getModname(), subBind, design, statePaths);
                scopes.remove(scopes.size() - 1);
            }
        }
    }

    static class GenFsmJob<H extends DesignHints> extends Job
    {
        private final Class<H> cls;
        private final File saoFile;
        private final String designName;

        private GenFsmJob(Class<H> cls, File saoFile, String designName)
        {
            super("Dump SV Design", User.getUserTool(), Job.Type.SERVER_EXAMINE, null, null, Job.Priority.USER);
            this.cls = cls;
            this.saoFile = saoFile;
            this.designName = designName;
        }

        @Override
        public boolean doIt() throws JobException
        {
            try
            {
                DesignHints designHints = cls.newInstance();
                ACL2Reader sr = new ACL2Reader(saoFile);
                DesignExt design = new DesignExt(sr.root, designHints);
                GenFsmNew gen = new GenFsmNew(designHints);
                File outDir = saoFile.getParentFile();
                gen.gen(designName, design, outDir);
            } catch (InstantiationException | IllegalAccessException | IOException e)
            {
                System.out.println(e.getMessage());
                return false;
            }
            return true;
        }
    }

}
