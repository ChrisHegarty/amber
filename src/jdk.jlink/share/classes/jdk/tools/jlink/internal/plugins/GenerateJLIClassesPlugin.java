/*
 * Copyright (c) 2016, 2020, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package jdk.tools.jlink.internal.plugins;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import jdk.internal.access.JavaLangInvokeAccess;
import jdk.internal.access.SharedSecrets;
import jdk.tools.jlink.plugin.PluginException;
import jdk.tools.jlink.plugin.ResourcePool;
import jdk.tools.jlink.plugin.ResourcePoolBuilder;
import jdk.tools.jlink.plugin.ResourcePoolEntry;

/**
 * Plugin to generate java.lang.invoke classes.
 *
 * The plugin reads in a file generated by running any application with
 * {@code -Djava.lang.invoke.MethodHandle.TRACE_RESOLVE=true}. This is done
 * automatically during build, see make/GenerateLinkOptData.gmk. See
 * build/tools/classlist/HelloClasslist.java for the training application.
 *
 * HelloClasslist tries to reflect common use of java.lang.invoke during early
 * startup and warmup in various applications. To ensure a good default
 * trade-off between static footprint and startup the application should be
 * relatively conservative.
 *
 * When using jlink to build a custom application runtime, generating a trace
 * file using {@code -Djava.lang.invoke.MethodHandle.TRACE_RESOLVE=true} and
 * feeding that into jlink using {@code --generate-jli-classes=@trace_file} can
 * help improve startup time.
 */
public final class GenerateJLIClassesPlugin extends AbstractPlugin {


    private static final String DEFAULT_TRACE_FILE = "default_jli_trace.txt";

    private static final JavaLangInvokeAccess JLIA
            = SharedSecrets.getJavaLangInvokeAccess();

    private String mainArgument;
    private Stream<String> traceFileStream;

    public GenerateJLIClassesPlugin() {
        super("generate-jli-classes");
    }

    @Override
    public Set<State> getState() {
        return EnumSet.of(State.AUTO_ENABLED, State.FUNCTIONAL);
    }

    @Override
    public boolean hasArguments() {
        return true;
    }

    @Override
    public void configure(Map<String, String> config) {
        mainArgument = config.get(getName());
    }

    public void initialize(ResourcePool in) {
        // Load configuration from the contents in the supplied input file
        // - if none was supplied we look for the default file
        if (mainArgument == null || !mainArgument.startsWith("@")) {
            try (InputStream traceFile =
                    this.getClass().getResourceAsStream(DEFAULT_TRACE_FILE)) {
                if (traceFile != null) {
                    traceFileStream = new BufferedReader(new InputStreamReader(traceFile)).lines();
                }
            } catch (Exception e) {
                throw new PluginException("Couldn't read " + DEFAULT_TRACE_FILE, e);
            }
        } else {
            File file = new File(mainArgument.substring(1));
            if (file.exists()) {
                traceFileStream = fileLines(file);
            }
        }
    }

    private Stream<String> fileLines(File file) {
        try {
            return Files.lines(file.toPath());
        } catch (IOException io) {
            throw new PluginException("Couldn't read file");
        }
    }

    @Override
    public ResourcePool transform(ResourcePool in, ResourcePoolBuilder out) {
        initialize(in);
        // Copy all but DMH_ENTRY to out
        in.transformAndCopy(entry -> {
                // No trace file given.  Copy all entries.
                if (traceFileStream == null) return entry;

                // filter out placeholder entries
                String path = entry.path();
                if (path.equals(DIRECT_METHOD_HOLDER_ENTRY) ||
                    path.equals(DELEGATING_METHOD_HOLDER_ENTRY) ||
                    path.equals(INVOKERS_HOLDER_ENTRY) ||
                    path.equals(BASIC_FORMS_HOLDER_ENTRY)) {
                    return null;
                } else {
                    return entry;
                }
            }, out);

        // Generate Holder classes
        if (traceFileStream != null) {
            try {
                JLIA.generateHolderClasses(traceFileStream)
                    .forEach((cn, bytes) -> {
                        String entryName = "/java.base/" + cn + ".class";
                        ResourcePoolEntry ndata = ResourcePoolEntry.create(entryName, bytes);
                        out.add(ndata);
                    });
            } catch (Exception ex) {
                throw new PluginException(ex);
            }
        }
        return out.build();
    }

    private static final String DIRECT_METHOD_HOLDER_ENTRY =
            "/java.base/java/lang/invoke/DirectMethodHandle$Holder.class";
    private static final String DELEGATING_METHOD_HOLDER_ENTRY =
            "/java.base/java/lang/invoke/DelegatingMethodHandle$Holder.class";
    private static final String BASIC_FORMS_HOLDER_ENTRY =
            "/java.base/java/lang/invoke/LambdaForm$Holder.class";
    private static final String INVOKERS_HOLDER_ENTRY =
            "/java.base/java/lang/invoke/Invokers$Holder.class";
}
