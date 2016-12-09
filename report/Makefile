TEX = pdflatex
REPORT = rapport.pdf
TARGETS = $(REPORT)

all: $(TARGETS)

report: $(REPORT)

%.pdf: %.tex
	$(TEX) $(OPTS) $^
	$(TEX) $(OPTS) $^
	$(TEX) $(OPTS) $^

clean:
	@rm *.aux *.log *.out $(TARGETS)

.PHONY:
	clean
