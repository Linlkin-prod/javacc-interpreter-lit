name=Parser

make:
	rm bin/*.class
	rm java/*.java
	javacc -OUTPUT_DIRECTORY=java src/$(name).jj
	javac -s java java/*.java -d bin

run: make
	java  -cp bin Parser
