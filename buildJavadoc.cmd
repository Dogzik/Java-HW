SET proj=C:\Users\levdo\Desktop\Programming\ITMO\java-advanced-2018
SET lib=lib\*
SET test=artifacts\JarImplementorTest.jar
SET data=java\info\kgeorgiy\java\advanced\implementor\
SET link=https://docs.oracle.com/javase/8/docs/api/

cd %proj%

javadoc -d javadoc -link %link% -cp java\;%lib%;%test%; -private -author -version ru.ifmo.rain.dovzhik.implementor %data%Impler.java %data%JarImpler.java %data%ImplerException.java
