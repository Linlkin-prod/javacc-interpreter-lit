name=Parser

make:
	rm bin/*.class || true
	rm java/*.java || true
	cp src/ASTNode.java java/ASTNode.java
	javacc -OUTPUT_DIRECTORY=java src/$(name).jj
	javac -s java java/*.java -d bin

run: make
	java  -cp bin Parser tests/test1.lit
