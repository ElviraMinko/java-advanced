SET project=C:\java-advanced
SET repository=C:\java-advanced\java-advanced-2023
SET library=%repository%\lib\*
SET tests=%repository%\artifacts\info.kgeorgiy.java.advanced.implementor.jar
SET dst=%project%\out\production\java-advanced
SET manifest=%project%\Manifest.txt
SET dep=info\kgeorgiy\java\advanced\implementor\
SET modules=%repository%\modules\
SET source=%project%\src
cd %project%
javac -d %dst% -cp %modules%;%library%;%tests%; java-solutions\info\kgeorgiy\ja\minko\implementor\Implementor.java

cd %dst%
jar xf %tests% %dep%Impler.class %dep%JarImpler.class %dep%ImplerException.class
jar cfm %project%\Implementor.jar %manifest% info\kgeorgiy\ja\minko\implementor\*.class %dep%*.class

pause