.PHONY: jar

entrega: jar
	make -C doc/
	rm -rf CarlosThiago
	mkdir -p CarlosThiago/bin/
	cp -r doc/relatorio.pdf files/ pom.xml src/ CarlosThiago/
	cp target/tla-1.0-job.jar CarlosThiago/bin/
	tar cjf CarlosThiago.tar.bz2 CarlosThiago

jar:
	mvn clean package

clean:
	rm -rf CarlosThiago*
