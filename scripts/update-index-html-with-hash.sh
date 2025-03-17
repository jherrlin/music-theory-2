#!/bin/bash

sleep 5

ORIGINAL_FILE=$(ls ./resources/public/js | grep -E "main\..+\.js$" | head)
ORIGINAL_FILE_WITHOUT_FILE_JS=$(ls ./resources/public/js | grep -E "main\..*\.js$" | head | rev | cut -c3- | rev)
FILENAME_WITH_DATE_AND_TIME="$ORIGINAL_FILE_WITHOUT_FILE_JS$(date +"%Y-%m-%d_%H-%M").js"

echo $ORIGINAL_FILE
echo $ORIGINAL_FILE_WITHOUT_FILE_JS
echo $FILENAME_WITH_DATE_AND_TIME

mv "./resources/public/js/${ORIGINAL_FILE}" "./resources/public/js/${FILENAME_WITH_DATE_AND_TIME}"

ls ./resources/public/js | grep -E "main\..*\.js$" | awk '{ print "<script src=\"js/" $1 "\"></script>" }' | xargs -I"{}" -d '\n' sed -i -e '11s@.*@{}@g' ./resources/public/index.html
