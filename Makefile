sources = $(wildcard *.java)
classes = $(sources:.java=.class)

JRE=/usr/lib/jvm/java-6-jre/jre
CPFLAGS=-cp '.:*'

all: compile run

compile: $(classes)

clean:
	rm -f *.class

%.class : %.java
	javac -source 1.6 -target 1.6 -bootclasspath $(JRE)/lib/rt.jar -cp '.:*' $<

jar: $(classes)
	jar cmf Manifest.txt LedConfig.jar default.eep *.class com/smartg/swing/*RangeSlider*.class com/smartg/java/util/EventListenerListIterator*.class

run: $(classes)
	java $(CPFLAGS) LedConfig

runjar: jar
	java $(CPFLAGS) -jar LedConfig.jar

export:
	mkdir -p tmp
	cd tmp; jar xf ../LedConfig.jar
	cd tmp; jar xf ../nrjavaserial-3.10.0.jar
	jar cmf Manifest.txt LedConfig-Serial.jar -C tmp .
