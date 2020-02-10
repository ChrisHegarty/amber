/**
 * @test
 * @compile --enable-preview -source ${jdk.version} SimpleDeconstructionPattern.java
 * @run main/othervm --enable-preview SimpleDeconstructionPattern
 */

public class SimpleDeconstructionPattern {

    public static void main(String... args) throws Throwable {
//        if (!test1(new P(42))) {
//            throw new IllegalStateException();
//        }
//        if (test1(new P(41))) {
//            throw new IllegalStateException();
//        }
        if (!test2(new P(42))) {
            throw new IllegalStateException();
        }
        if (test2(new P(41))) {
            throw new IllegalStateException();
        }
//        if (!test3(new P2(new P(42), ""))) {
//            throw new IllegalStateException();
//        }
//        if (test3(new P2(new P(41), ""))) {
//            throw new IllegalStateException();
//        }
//        if (test3(new P2(new P(42), "a"))) {
//            throw new IllegalStateException();
//        }
        if (!test4(new P2(new P(42), ""))) {
            throw new IllegalStateException();
        }
        if (test4(new P2(new P(41), ""))) {
            throw new IllegalStateException();
        }
        if (test4(new P2(new P(42), "a"))) {
            throw new IllegalStateException();
        }
        if (!test5(new P(42))) {
            throw new IllegalStateException();
        }
        if (test5(new P(41))) {
            throw new IllegalStateException();
        }
        if (!test6(new P(42))) {
            throw new IllegalStateException();
        }
        if (!test6(new P(41))) {
            throw new IllegalStateException();
        }
//        if (!((new BaseUse(new BaseSubclass(0))) instanceof BaseUse(BaseSubclass(0)))) {
//            throw new IllegalStateException();
//        }
        if (!test7(new P3(""))) {
            throw new IllegalStateException();
        }
        if (test7(new P3("a"))) {
            throw new IllegalStateException();
        }
        if (test8(new P4(""))) {
            throw new IllegalStateException();
        }
        if (!test8(new P4(new P3("")))) {
            throw new IllegalStateException();
        }
        if (test8(new P4(new P3("a")))) {
            throw new IllegalStateException();
        }
    }

//    private static boolean test1(Object o) throws Throwable {
//        return o instanceof P(42);
//    }
//
    private static void exp(Object o) throws Throwable {
        if (o instanceof P(var i)) {
            System.err.println("i=" + i);
        }
    }

    private static boolean test2(Object o) throws Throwable {
        return o instanceof P(var i) && i == 42;
    }

//    private static boolean test3(Object o) throws Throwable {
//        return o instanceof P2(P(42), "");
//    }
//
    private static boolean test4(Object o) throws Throwable {
        return o instanceof P2(P(var i), var s) && i == 42 && "".equals(s);
    }

    private static boolean test5(Object o) throws Throwable {
        return o instanceof P(var i) && i == 42;
    }

    private static boolean test6(Object o) throws Throwable {
        return o instanceof P(_);
    }

    private static boolean test7(Object o) throws Throwable {
        return o instanceof P3(var s) && "".equals(s);
    }

    private static boolean test8(Object o) throws Throwable {
        return o instanceof P4(P3(var s)) && "".equals(s);
    }

    public record P(int i) {
    }

    public record P2(P p, String s) {
    }

    public record P3(String s) {
    }

    public record P4(Object o) {}

    public interface Base {}
    public record BaseUse(Base b) {}
    public record BaseSubclass(int i) implements Base {}
}