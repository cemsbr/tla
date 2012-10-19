all: 
	pdflatex relatorio.tex
	pdflatex relatorio.tex

clean:
	rm -f *.{aux,log,out,toc}

auto-buid:
	while [ true ]; do \
		inotifywait -e modify relatorio.tex; \
		make; \
	done
