-injars build/libs/applicationbase.jar
-outjars build/libs/applicationbase-obfuscated.jar

-dontwarn
-dontoptimize
-dontpreverify

-keep public class * {
    public static void main(java.lang.String[]);
}