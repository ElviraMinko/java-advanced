SET path_to_javadoc=../javadoc
SET author_package=info.kgeorgiy.ja.minko.implementor
SET java_advanced_package=info.kgeorgiy.java.advanced.implementor
SET path_to_files=../java-advanced-2023/modules/%java_advanced_package%/info/kgeorgiy/java/advanced/implementor/
SET path_to_source=../java-advanced-2023/artifacts/%java_advanced_package%.jar
SET path_to_libs=../java-advanced-2023/lib/*
SET link=https://docs.oracle.com/en/java/javase/19/docs/api/
javadoc -d %path_to_javadoc% -link %link% -cp ../java-solutions/;%path_to_source%;%path_to_libs% -private %author_package% %path_to_files%Impler.java %path_to_files%JarImpler.java %path_to_files%ImplerException.java

pause