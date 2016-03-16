#!/bin/bash
grep -v "#"| cut -f1,2,4,5,7 | sort | uniq | awk -v ds=$1 '
{ if ( (length($3) == 1 && length($4) == 1) && ($5 == "PASS" || $5 == ".")) print ds";"$1";"$2";"$4}
' > $1.SNPs
COUNT="$(wc -l < $1.SNPs)"
echo "${COUNT}"
