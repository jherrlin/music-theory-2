#!/bin/bash

mv -f ./resources/public/js/main.*.js ./
mv -f ./resources/public/index.html ./index.html.bak
mv -f ./resources/public/favicon.ico ./favicon.ico.bak
mv -f ./resources/public/abcjs-audio.css ./abcjs-audio.css.bak
git config user.email "jherrlin@gmail.com"
git config user.name "CircleCI Job"
git fetch --all
rm -f package-lock.json
git checkout gh-pages
rm -rf ./js
mkdir -p js
mv -f main.*.js ./js/
mv -f index.html.bak index.html
mv -f favicon.ico.bak favicon.ico
mv -f abcjs-audio.css.bak abcjs-audio.css
git add js index.html favicon.ico abcjs-audio.css
git commit --allow-empty -am "Latest build"
git push -f origin gh-pages
