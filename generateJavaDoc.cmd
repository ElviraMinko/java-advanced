SET proj=C:\java-advanced
SET rep=C:\java-advanced\java-advanced-2023
SET lib=%rep%\lib\*
SET test=%rep%\artifacts\info.kgeorgiy.java.advanced.implementor.jar
SET data=%rep%\modules\info.kgeorgiy.java.advanced.implementor\info\kgeorgiy\java\advanced\implementor\
SET link=https://docs.oracle.com/en/java/javase/11/docs/api/
SET package=info.kgeorgiy.ja.minko.implementor

cd %proj%

javadoc -d %proj%\javadoc -link %link% -cp java-solutions\;%lib%;%test%; -private -author -version %package% %data%Impler.java %data%JarImpler.java %data%ImplerException.java

pause