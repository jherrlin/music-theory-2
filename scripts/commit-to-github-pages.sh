#!/bin/bash

git config user.email "jherrlin@gmail.com"
git config user.name "CircleCI Job"
git fetch --all
rm -f package-lock.json
git checkout gh-pages
rm -rf ./js
mkdir -p js
mv -f ./resources/public/js/main.*.js ./js/
mv -f ./resources/public/index.html index.html
mv -f ./resources/public/favicon.ico favicon.ico
git add js index.html favicon.ico
git commit --allow-empty -am "Latest build"
git push -f origin gh-pages
