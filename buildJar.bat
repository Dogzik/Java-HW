SET proj=C:\Users\levdo\Desktop\Programming\ITMO\java-advanced-2018
SET lib=lib\*
SET test=artifacts\JarImplementorTest.jar
SET dst=out\production\java-advanced-2018
SET man=..\..\..\Manifest.txt
SET dep=info\kgeorgiy\java\advanced\implementor\

cd %proj%
javac -d %dst% -cp %lib%;%test%; java\ru\ifmo\rain\dovzhik\implementor\Implementor.java

cd %dst%
jar xf ..\..\..\%test% %dep%Impler.class %dep%JarImpler.class %dep%ImplerException.class
jar cfm Implementor.jar %man% ru\ifmo\rain\dovzhik\implementor\*.class %dep%*.class
rmdir info /s /q
