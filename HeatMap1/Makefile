CLASSES = Observation.class ColoredGrid.class CGDemo.class GeneralScanV3.class HeatMapTally.class HeatMapScan.class hw5.class
JAVAFLAGS = -J-Xmx48m

all: $(CLASSES)

%.class : %.java
	javac $(JAVAFLAGS) $<

clean:
	@rm -f *.class
