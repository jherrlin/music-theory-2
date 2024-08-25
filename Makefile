.PHONY: all test clean echo-release-version

SERVICE_NAME = music-theory

release-frontend: shadow-cljs-release update-index-html-with-hash


dev:
	npm run dev

shadow-cljs-release:
	npx shadow-cljs release frontend

shadow-cljs-compile:
	npx shadow-cljs compile frontend

circle-ci-test:
	clojure -X:test

test:
	clj -X:test

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

tailwind-watch:
	npx tailwindcss -i ./src/css/tailwind.css -o ./resources/public/css/tailwind.css --watch

babel-compile-shadcn-components:
	npx babel npm-packages/shadcn --extensions .jsx --out-dir npm-packages/shadcn --watch

test:
	clj -X:test

update-index-html-with-hash:
	./scripts/update-index-html-with-hash.sh

deploy:
	./scripts/commit-to-github-pages.sh

echo-release-version:
	./scripts/echo-release-version.sh
