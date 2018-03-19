SET proj=C:\Users\levdo\Desktop\Programming\ITMO\java-advanced-2018
SET lib=lib\*
SET test=artifacts\
SET dst=out\production\java-advanced-2018

cd %proj%
javac -d %dst% -cp %lib%;%test%%1Test.jar; java\ru\ifmo\rain\dovzhik\%2\*.java
cd %dst%
