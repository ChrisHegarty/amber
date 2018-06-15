/* /nodynamiccopyright/ */

import java.lang.invoke.*;
import java.lang.constant.*;

import static java.lang.invoke.Intrinsics.*;

@IgnoreTest
public class ConstantDefinitions {
    public static final MethodType mtStatic = ldc(MethodTypeDesc.of(ConstantDescs.CR_String));
    public final MethodType mtInstance = ldc(MethodTypeDesc.of(ConstantDescs.CR_String));
}
