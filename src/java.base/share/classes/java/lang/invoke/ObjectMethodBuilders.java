/*
 * Copyright (c) 2017, Oracle and/or its affiliates. All rights reserved.
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

package java.lang.invoke;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * ObjectMethodBuilders
 *
 * @author Brian Goetz
 */
public class ObjectMethodBuilders {
    private static final MethodType DESCRIPTOR_MT = MethodType.methodType(MethodType.class);
    private static final MethodType NAMES_MT = MethodType.methodType(List.class);
    private static final MethodHandle FALSE = MethodHandles.constant(boolean.class, false);
    private static final MethodHandle TRUE = MethodHandles.constant(boolean.class, true);
    private static final MethodHandle ZERO = MethodHandles.constant(int.class, 0);
    private static final MethodHandle CLASS_IS_INSTANCE;
    private static final MethodHandle OBJECT_EQUALS;
    private static final MethodHandle OBJECTS_EQUALS;
    private static final MethodHandle OBJECTS_HASHCODE;
    private static final MethodHandle OBJECTS_TOSTRING;
    private static final MethodHandle OBJECT_EQ;
    private static final MethodHandle OBJECT_HASHCODE;
    private static final MethodHandle OBJECT_TO_STRING;
    private static final MethodHandle STRING_FORMAT;
    private static final MethodHandle HASH_COMBINER;

    private static final HashMap<Class<?>, MethodHandle> primitiveEquals = new HashMap<>();
    private static final HashMap<Class<?>, MethodHandle> primitiveHashers = new HashMap<>();
    private static final HashMap<Class<?>, MethodHandle> primitiveToString = new HashMap<>();

    static {
        try {
            MethodHandles.Lookup publicLookup = MethodHandles.publicLookup();
            MethodHandles.Lookup lookup = MethodHandles.Lookup.IMPL_LOOKUP;

            CLASS_IS_INSTANCE = publicLookup.findVirtual(Class.class, "isInstance", MethodType.methodType(boolean.class, Object.class));
            OBJECT_EQUALS = publicLookup.findVirtual(Object.class, "equals", MethodType.methodType(boolean.class, Object.class));
            OBJECT_HASHCODE = publicLookup.findVirtual(Object.class, "hashCode", MethodType.fromMethodDescriptorString("()I", null));
            OBJECT_TO_STRING = publicLookup.findVirtual(Object.class, "toString", MethodType.methodType(String.class));
            STRING_FORMAT = publicLookup.findStatic(String.class, "format", MethodType.methodType(String.class, String.class, Object[].class));
            OBJECTS_EQUALS = publicLookup.findStatic(Objects.class, "equals", MethodType.methodType(boolean.class, Object.class, Object.class));
            OBJECTS_HASHCODE = publicLookup.findStatic(Objects.class, "hashCode", MethodType.methodType(int.class, Object.class));
            OBJECTS_TOSTRING = publicLookup.findStatic(Objects.class, "toString", MethodType.methodType(String.class, Object.class));

            OBJECT_EQ = lookup.findStatic(ObjectMethodBuilders.class, "eq", MethodType.methodType(boolean.class, Object.class, Object.class));
            HASH_COMBINER = lookup.findStatic(ObjectMethodBuilders.class, "hashCombiner", MethodType.fromMethodDescriptorString("(II)I", null));
            primitiveEquals.put(byte.class, lookup.findStatic(ObjectMethodBuilders.class, "eq", MethodType.fromMethodDescriptorString("(BB)Z", null)));
            primitiveEquals.put(short.class, lookup.findStatic(ObjectMethodBuilders.class, "eq", MethodType.fromMethodDescriptorString("(SS)Z", null)));
            primitiveEquals.put(char.class, lookup.findStatic(ObjectMethodBuilders.class, "eq", MethodType.fromMethodDescriptorString("(CC)Z", null)));
            primitiveEquals.put(int.class, lookup.findStatic(ObjectMethodBuilders.class, "eq", MethodType.fromMethodDescriptorString("(II)Z", null)));
            primitiveEquals.put(long.class, lookup.findStatic(ObjectMethodBuilders.class, "eq", MethodType.fromMethodDescriptorString("(JJ)Z", null)));
            primitiveEquals.put(float.class, lookup.findStatic(ObjectMethodBuilders.class, "eq", MethodType.fromMethodDescriptorString("(FF)Z", null)));
            primitiveEquals.put(double.class, lookup.findStatic(ObjectMethodBuilders.class, "eq", MethodType.fromMethodDescriptorString("(DD)Z", null)));
            primitiveEquals.put(boolean.class, lookup.findStatic(ObjectMethodBuilders.class, "eq", MethodType.fromMethodDescriptorString("(ZZ)Z", null)));

            primitiveHashers.put(byte.class, lookup.findStatic(Byte.class, "hashCode", MethodType.fromMethodDescriptorString("(B)I", null)));
            primitiveHashers.put(short.class, lookup.findStatic(Short.class, "hashCode", MethodType.fromMethodDescriptorString("(S)I", null)));
            primitiveHashers.put(char.class, lookup.findStatic(Character.class, "hashCode", MethodType.fromMethodDescriptorString("(C)I", null)));
            primitiveHashers.put(int.class, lookup.findStatic(Integer.class, "hashCode", MethodType.fromMethodDescriptorString("(I)I", null)));
            primitiveHashers.put(long.class, lookup.findStatic(Long.class, "hashCode", MethodType.fromMethodDescriptorString("(J)I", null)));
            primitiveHashers.put(float.class, lookup.findStatic(Float.class, "hashCode", MethodType.fromMethodDescriptorString("(F)I", null)));
            primitiveHashers.put(double.class, lookup.findStatic(Double.class, "hashCode", MethodType.fromMethodDescriptorString("(D)I", null)));
            primitiveHashers.put(boolean.class, lookup.findStatic(Boolean.class, "hashCode", MethodType.fromMethodDescriptorString("(Z)I", null)));

            primitiveToString.put(byte.class, lookup.findStatic(Byte.class, "toString", MethodType.methodType(String.class, byte.class)));
            primitiveToString.put(short.class, lookup.findStatic(Short.class, "toString", MethodType.methodType(String.class, short.class)));
            primitiveToString.put(char.class, lookup.findStatic(Character.class, "toString", MethodType.methodType(String.class, char.class)));
            primitiveToString.put(int.class, lookup.findStatic(Integer.class, "toString", MethodType.methodType(String.class, int.class)));
            primitiveToString.put(long.class, lookup.findStatic(Long.class, "toString", MethodType.methodType(String.class, long.class)));
            primitiveToString.put(float.class, lookup.findStatic(Float.class, "toString", MethodType.methodType(String.class, float.class)));
            primitiveToString.put(double.class, lookup.findStatic(Double.class, "toString", MethodType.methodType(String.class, double.class)));
            primitiveToString.put(boolean.class, lookup.findStatic(Boolean.class, "toString", MethodType.methodType(String.class, boolean.class)));
        }
        catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private static int hashCombiner(int x, int y) {
        return x*31 + y;
    }

    private static boolean eq(Object a, Object b) { return a == b; }
    private static boolean eq(byte a, byte b) { return a == b; }
    private static boolean eq(short a, short b) { return a == b; }
    private static boolean eq(char a, char b) { return a == b; }
    private static boolean eq(int a, int b) { return a == b; }
    private static boolean eq(long a, long b) { return a == b; }
    private static boolean eq(float a, float b) { return Float.compare(a, b) == 0; }
    private static boolean eq(double a, double b) { return Double.compare(a, b) == 0; }
    private static boolean eq(boolean a, boolean b) { return a == b; }

    /** Get the method handle for combining two values of a given type */
    private static MethodHandle equalator(Class<?> clazz) {
        return (clazz.isPrimitive()
                ? primitiveEquals.get(clazz)
                : OBJECTS_EQUALS.asType(MethodType.methodType(boolean.class, clazz, clazz)));
    }

    /** Get the hasher for a value of a given type */
    private static MethodHandle hasher(Class<?> clazz) {
        return (clazz.isPrimitive()
                ? primitiveHashers.get(clazz)
                : OBJECTS_HASHCODE.asType(MethodType.methodType(int.class, clazz)));
    }

    /** Get the stringifier for a value of a given type */
    private static MethodHandle stringifier(Class<?> clazz) {
        return (clazz.isPrimitive()
                ? primitiveToString.get(clazz)
                : OBJECTS_TOSTRING.asType(MethodType.methodType(String.class, clazz)));
    }

    /**
     * Generates a method handle for the {@code equals} method for a given data class
     * @param receiverClass   the data class
     * @param getters         the list of getters
     * @return the method handle
     */
    private static MethodHandle makeEquals(Class<?> receiverClass,
                                          List<MethodHandle> getters) {
        MethodType rr = MethodType.methodType(boolean.class, receiverClass, receiverClass);
        MethodType ro = MethodType.methodType(boolean.class, receiverClass, Object.class);
        MethodHandle instanceFalse = MethodHandles.dropArguments(FALSE, 0, receiverClass, Object.class); // (RO)Z
        MethodHandle instanceTrue = MethodHandles.dropArguments(TRUE, 0, receiverClass, Object.class); // (RO)Z
        MethodHandle isSameObject = OBJECT_EQ.asType(ro); // (RO)Z
        MethodHandle isInstance = MethodHandles.dropArguments(CLASS_IS_INSTANCE.bindTo(receiverClass), 0, receiverClass); // (RO)Z
        MethodHandle accumulator = MethodHandles.dropArguments(TRUE, 0, receiverClass, receiverClass); // (RR)Z

        for (MethodHandle getter : getters) {
            MethodHandle equalator = equalator(getter.type().returnType()); // (TT)Z
            MethodHandle thisFieldEqual = MethodHandles.filterArguments(equalator, 0, getter, getter); // (RR)Z
            accumulator = MethodHandles.guardWithTest(thisFieldEqual, accumulator, instanceFalse.asType(rr));
        }

        return MethodHandles.guardWithTest(isSameObject,
                                           instanceTrue,
                                           MethodHandles.guardWithTest(isInstance, accumulator.asType(ro), instanceFalse));
    }

    /**
     * Generates a method handle for the {@code hashCode} method for a given data class
     * @param receiverClass   the data class
     * @param getters         the list of getters
     * @return the method handle
     */
    private static MethodHandle makeHashCode(Class<?> receiverClass,
                                            List<MethodHandle> getters) {
        MethodHandle accumulator = MethodHandles.dropArguments(ZERO, 0, receiverClass); // (R)I

        // @@@ Use loop combinator instead?
        for (MethodHandle getter : getters) {
            MethodHandle hasher = hasher(getter.type().returnType()); // (T)I
            MethodHandle hashThisField = MethodHandles.filterArguments(hasher, 0, getter);    // (R)I
            MethodHandle combineHashes = MethodHandles.filterArguments(HASH_COMBINER, 0, accumulator, hashThisField); // (RR)I
            accumulator = MethodHandles.permuteArguments(combineHashes, accumulator.type(), 0, 0); // adapt (R)I to (RR)I
        }

        return accumulator;
    }

    /**
     * Generates a method handle for the {@code toString} method for a given data class
     * @param receiverClass   the data class
     * @param getters         the list of getters
     * @param names           the names
     * @return the method handle
     */
    private static MethodHandle makeToString(Class<?> receiverClass,
                                            List<MethodHandle> getters,
                                            List<String> names) {
        // This is a pretty lousy algorithm; we spread the receiver over N places,
        // apply the N getters, apply N toString operations, and concat the result with String.format
        // Better to use String.format directly, or delegate to StringConcatFactory
        // Also probably want some quoting around String components

        assert getters.size() == names.size();

        int[] invArgs = new int[getters.size()];
        Arrays.fill(invArgs, 0);
        MethodHandle[] filters = new MethodHandle[getters.size()];
        StringBuilder sb = new StringBuilder();
        sb.append(receiverClass.getSimpleName()).append("[");
        for (int i=0; i<getters.size(); i++) {
            MethodHandle getter = getters.get(i); // (R)T
            MethodHandle stringify = stringifier(getter.type().returnType()); // (T)String
            MethodHandle stringifyThisField = MethodHandles.filterArguments(stringify, 0, getter);    // (R)String
            filters[i] = stringifyThisField;
            sb.append(names.get(i)).append("=%s");
            if (i != getters.size() - 1)
                sb.append(", ");
        }
        sb.append(']');
        String formatString = sb.toString();
        MethodHandle formatter = MethodHandles.insertArguments(STRING_FORMAT, 0, formatString)
                                              .asCollector(String[].class, getters.size()); // (R*)String
        if (getters.size() == 0) {
            // Add back extra R
            formatter = MethodHandles.dropArguments(formatter, 0, receiverClass);
        }
        else {
            MethodHandle filtered = MethodHandles.filterArguments(formatter, 0, filters);
            formatter = MethodHandles.permuteArguments(filtered, MethodType.methodType(String.class, receiverClass), invArgs);
        }

        return formatter;
    }

    /**
     * Bootstrap method to generate the {@code equals}, {@code hashCode}, and {@code toString} methods for a given data class
     * @param lookup       the lookup
     * @param methodName   the method name
     * @param type         the descriptor type
     * @param theClass     the data class
     * @param names        the list of field names joined into a string, separated by ";"
     * @param getters      the list of getters
     * @return a call site if invoked by and indy or a method handle if invoked by a condy
     * @throws Throwable if any exception is thrown during call site construction
     */
    public static Object bootstrap(MethodHandles.Lookup lookup, String methodName, TypeDescriptor type,
                                   Class<?> theClass, String names, MethodHandle... getters) throws Throwable {
        MethodType methodType;
        if (type instanceof MethodType)
            methodType = (MethodType) type;
        else {
            methodType = null;
            if (!MethodHandle.class.equals(type))
                throw new IllegalArgumentException(type.toString());
        }
        List<MethodHandle> getterList = List.of(getters);
        MethodHandle handle;
        switch (methodName) {
            case "equals":
                // validate method type
                handle = makeEquals(theClass, getterList);
                return methodType != null ? new ConstantCallSite(handle) : handle;
            case "hashCode":
                // validate method type
                handle = makeHashCode(theClass, getterList);
                return methodType != null ? new ConstantCallSite(handle) : handle;
            case "toString":
                // validate method type
                handle = makeToString(theClass, getterList, List.of(names.split(";")));
                return methodType != null ? new ConstantCallSite(handle) : handle;
            default:
                throw new IllegalArgumentException(methodName);
        }
    }
}