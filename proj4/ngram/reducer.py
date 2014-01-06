#!/usr/bin/python
import sys

current_ngram = None
current_count = 0
ngram = None

for line in sys.stdin:
    line = line.strip()
    ngram, count = line.split('\t', 1)

    # convert count (currently a string) to int
    #try:
    #    count = int(count)
    #except ValueError: continue

    if current_ngram == ngram: current_count += 1
#if current_ngram == ngram: current_count += count
    else:
        if current_ngram: print current_ngram + '\t' + str(current_count)
        current_count = count
        current_ngram = ngram

# out put the last word
if current_ngram == ngram:
    print current_ngram + '\t' + str(current_count)
