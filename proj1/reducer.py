#!/usr/bin/python

import sys

def main(argv):
    (last_title,sum,dates)=(None,0,"")

    for line in sys.stdin:
	(cur_title, value)=line.strip().split('\t')
	(views, date)=value.strip().split('&')

	if last_title and last_title!=cur_title:
	    print str(sum)+"\t"+last_title+"\t"+dates
	    
 	    (last_title, sum, dates)=(cur_title, int(views), date+"\t")
	else:
	    (last_title, sum, dates)=(cur_title, sum+int(views), dates+date+"\t")

    if last_title:
	print str(sum)+"\t"+last_title+"\t"+dates

if __name__=='__main__':
    main(sys.argv)
	
	

