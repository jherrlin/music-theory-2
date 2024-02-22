.PHONY: all test clean

SERVICE_NAME = music-theory
VERSION = 2


release-frontend:
	npx shadow-cljs release frontend && ./update-index-html-with-hash.sh

compile-frontend:
	npx shadow-cljs compile frontend

clean:
	rm -rf ./node_modules ./.cpcache ./.shadow-cljs ./target ./.lsp ./classes

update:
	clojure -Moutdated --write
	npm update

install:
	npm install

format:
	clojure-lsp format
	clojure-lsp clean-ns

diagnostics:
	clojure-lsp diagnostics
