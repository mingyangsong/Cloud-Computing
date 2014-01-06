#!/usr/bin/python
import sys
import re

def ngrams(words, n):
	output = []
	for i in range( len(words)-n+1 ):
		output.append(words[i:i+n])
	return output

for line in sys.stdin :
    # several constrians, only
    line = re.sub('[^a-zA-Z]', ' ', line)

    line = line.lower().strip()
    words = line.split()
    #print words
    
    for i in range (1, 6) :
        grams = ngrams(words, i)
        for w in grams :
            print ' '.join(w) + '\t1'
