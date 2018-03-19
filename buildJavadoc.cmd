SET proj=C:\Users\levdo\Desktop\Programming\ITMO\java-advanced-2018
SET lib=lib\*
SET test=artifacts\JarImplementorTest.jar
SET data=java\info\kgeorgiy\java\advanced\implementor\
SET link=https://docs.oracle.com/javase/8/docs/api/
SET package=ru.ifmo.rain.dovzhik.implementor

cd %proj%

javadoc -d javadoc -link %link% -cp java\;%lib%;%test%; -private -author -version %package% %data%Impler.java %data%JarImpler.java %data%ImplerException.java
