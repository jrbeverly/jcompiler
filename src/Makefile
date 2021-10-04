# this is just a wrapper for ant so that we meet assignment requirements

all: grammar ant

grammar:
	cd tools && python expand_cfgx.py joos1w.cfgx joos1w.cfg
	cd tools && _JAVA_OPTIONS=-Xmx256M javac jlalr/Jlalr1.java
	cd tools && java -Xmx512M jlalr.Jlalr1 < joos1w.cfg > joos1w.lalr1

ant:
	_JAVA_OPTIONS=-Xmx256M ant clean jar
