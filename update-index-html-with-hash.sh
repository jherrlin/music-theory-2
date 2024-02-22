#!/bin/bash
ls ./resources/public/js | grep -E "main\..*\.js$" | awk '{ print "<script src=\"" $1 "\"></script>" }' | xargs -I"{}" -d '\n' sed -i -e '10s@.*@{}@g' ./resources/public/index.html
