CLASSES = BitonicSortLoop.class Homework4.class RandomArrayGenerator.class
JAVAFLAGS = -J-Xmx48m


all: $(CLASSES)

%.class : %.java
	javac $(JAVAFLAGS) $<

clean:
	@rm -f *.class
