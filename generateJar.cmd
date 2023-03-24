SET proj=C:\java-advanced
SET rep=C:\java-advanced\java-advanced-2023
SET lib=%rep%\lib\*
SET test=%rep%\artifacts\info.kgeorgiy.java.advanced.implementor.jar
SET dst=%proj%\out\production\java-advanced
SET man=%proj%\Manifest.txt
SET dep=info\kgeorgiy\java\advanced\implementor\
SET modules=%rep%\modules\
SET source=%proj%\src
cd %proj%
javac -d %dst% -cp %modules%;%lib%;%test%; java-solutions\info\kgeorgiy\ja\minko\implementor\Implementor.java

cd %dst%
jar xf %test% %dep%Impler.class %dep%JarImpler.class %dep%ImplerException.class
jar cfm %proj%\Implementor.jar %man% info\kgeorgiy\ja\minko\implementor\*.class %dep%*.class
cd %proj%