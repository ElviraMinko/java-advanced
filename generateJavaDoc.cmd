SET project=C:\java-advanced
SET repository=C:\java-advanced\java-advanced-2023
SET lib=%repository%\lib\*
SET test=%repository%\artifacts\info.kgeorgiy.java.advanced.implementor.jar
SET data=%repository%\modules\info.kgeorgiy.java.advanced.implementor\info\kgeorgiy\java\advanced\implementor\
SET link=https://docs.oracle.com/en/java/javase/11/docs/api/
SET package=info.kgeorgiy.ja.minko.implementor

cd %project%

javadoc -d %project%\javadoc -link %link% -cp java-solutions\;%lib%;%test%; -private -author -version %package% %data%Impler.java %data%JarImpler.java %data%ImplerException.java

pause