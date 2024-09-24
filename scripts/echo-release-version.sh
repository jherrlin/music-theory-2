#!/bin/bash


RELEASE_FILE=$(ls ./ | grep -E "main\..*\.js$" | head)

echo "$RELEASE_FILE"
