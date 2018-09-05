/*
 * Copyright (c) 2018, Oracle and/or its affiliates. All rights reserved.
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
package java.lang.constant;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static java.lang.constant.ConstantDescs.BSM_INVOKE;
import static java.lang.constant.ConstantDescs.MHR_METHODTYPEDESC_FACTORY;
import static java.util.Objects.requireNonNull;

/**
 * A <a href="package-summary.html#nominal">nominal descriptor</a> for a
 * {@link MethodType}.  A {@linkplain MethodTypeDescImpl} corresponds to a
 * {@code Constant_MethodType_info} entry in the constant pool of a classfile.
 */
final class MethodTypeDescImpl implements MethodTypeDesc {
    private final ClassDesc returnType;
    private final ClassDesc[] argTypes;

    /**
     * Construct a {@linkplain MethodTypeDesc} with the specified return type
     * and parameter types
     *
     * @param returnType a {@link ClassDesc} describing the return type
     * @param argTypes {@link ClassDesc}s describing the parameter types
     */
    MethodTypeDescImpl(ClassDesc returnType, ClassDesc[] argTypes) {
        this.returnType = requireNonNull(returnType);
        this.argTypes = requireNonNull(argTypes);

        for (ClassDesc cr : argTypes)
            if (cr.isPrimitive() && cr.descriptorString().equals("V"))
                throw new IllegalArgumentException("Void parameters not permitted");
    }

    /**
     * Create a {@linkplain MethodTypeDescImpl} given a method descriptor string.
     *
     * @param descriptor the method descriptor string, as per JVMS 4.3.3
     * @return a {@linkplain MethodTypeDescImpl} describing the desired method type
     * @throws IllegalArgumentException if the descriptor string is not a valid
     * method descriptor
     * @jvms 4.3.3 Method Descriptors
     */
    static MethodTypeDescImpl ofDescriptor(String descriptor) {
        requireNonNull(descriptor);
        List<String> types = ConstantUtils.parseMethodDescriptor(descriptor);
        ClassDesc[] paramTypes = types.stream().skip(1).map(ClassDesc::ofDescriptor).toArray(ClassDesc[]::new);
        return new MethodTypeDescImpl(ClassDesc.ofDescriptor(types.get(0)), paramTypes);
    }

    @Override
    public ClassDesc returnType() {
        return returnType;
    }

    @Override
    public int parameterCount() {
        return argTypes.length;
    }

    @Override
    public ClassDesc parameterType(int index) {
        return argTypes[index];
    }

    @Override
    public List<ClassDesc> parameterList() {
        return List.of(argTypes);
    }

    @Override
    public ClassDesc[] parameterArray() {
        return argTypes.clone();
    }

    @Override
    public MethodTypeDesc changeReturnType(ClassDesc returnType) {
        return MethodTypeDesc.of(returnType, argTypes);
    }

    @Override
    public MethodTypeDesc changeParameterType(int index, ClassDesc paramType) {
        ClassDesc[] newArgs = argTypes.clone();
        newArgs[index] = paramType;
        return MethodTypeDesc.of(returnType, newArgs);
    }

    @Override
    public MethodTypeDesc dropParameterTypes(int start, int end) {
        if (start < 0 || start >= argTypes.length || end < 0 || end > argTypes.length)
            throw new IndexOutOfBoundsException();
        else if (start > end)
            throw new IllegalArgumentException(String.format("Range (%d, %d) not valid for size %d", start, end, argTypes.length));
        ClassDesc[] newArgs = new ClassDesc[argTypes.length - (end - start)];
        System.arraycopy(argTypes, 0, newArgs, 0, start);
        System.arraycopy(argTypes, end, newArgs, start, argTypes.length - end);
        return MethodTypeDesc.of(returnType, newArgs);
    }

    @Override
    public MethodTypeDesc insertParameterTypes(int pos, ClassDesc... paramTypes) {
        if (pos < 0 || pos > argTypes.length)
            throw new IndexOutOfBoundsException(pos);
        ClassDesc[] newArgs = new ClassDesc[argTypes.length + paramTypes.length];
        System.arraycopy(argTypes, 0, newArgs, 0, pos);
        System.arraycopy(paramTypes, 0, newArgs, pos, paramTypes.length);
        System.arraycopy(argTypes, pos, newArgs, pos+paramTypes.length, argTypes.length - pos);
        return MethodTypeDesc.of(returnType, newArgs);
    }

    @Override
    public MethodType resolveConstantDesc(MethodHandles.Lookup lookup) {
        return MethodType.fromMethodDescriptorString(descriptorString(), lookup.lookupClass().getClassLoader());
    }

    @Override
    public Optional<? extends ConstantDesc<ConstantDesc<MethodType>>> describeConstable() {
        return Optional.of(DynamicConstantDesc.of(BSM_INVOKE, MHR_METHODTYPEDESC_FACTORY, descriptorString()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MethodTypeDescImpl constant = (MethodTypeDescImpl) o;

        return returnType.equals(constant.returnType)
               && Arrays.equals(argTypes, constant.argTypes);
    }

    @Override
    public int hashCode() {
        int result = returnType.hashCode();
        result = 31 * result + Arrays.hashCode(argTypes);
        return result;
    }

    @Override
    public String toString() {
        return String.format("MethodTypeDesc[%s]", displayDescriptor());
    }
}
