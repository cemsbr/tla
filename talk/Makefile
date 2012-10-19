pdf:
	pdflatex talk.tex
	pdflatex talk.tex

auto-buid:
	while [ true ]; do \
		inotifywait -e modify talk.tex; \
		make; \
	done

clean:
	rm -f talk.{aux,log,nav,out,pdf,snm,toc}

