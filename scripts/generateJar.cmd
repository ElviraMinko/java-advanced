SET package=info\kgeorgiy\ja\minko\implementor
SET path_to_main_file=..\java-solutions\%package%\Implementor.java
SET path_to_source=..\java-advanced-2023\artifacts\info.kgeorgiy.java.advanced.implementor.jar
SET path_to_manifest=..\MANIFEST.MF
javac -d . -cp %path_to_source% %path_to_main_file%
jar cfm Implementor.jar %path_to_manifest% %package%\*.class

pause