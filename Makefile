.PHONY: all test clean release-version

SERVICE_NAME = music-theory

release-frontend: shadow-cljs-release update-index-html-with-hash



shadow-cljs-release:
	npx shadow-cljs release frontend

shadow-cljs-compile:
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

circle-ci-test:
	clojure -X:test

test:
	clj -X:test

update-index-html-with-hash:
	./scripts/update-index-html-with-hash.sh

deploy:
	./scripts/commit-to-github-pages.sh

release-version:
	./scripts/echo-release-version.sh
