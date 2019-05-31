INPUT=index.adoc

all: html

html:
	bundle exec	asciidoctor -r asciidoctor-diagram $(INPUT)

deps:
	bundle install