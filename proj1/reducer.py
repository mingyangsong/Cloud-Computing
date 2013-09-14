#!/usr/bin/python

import sys

def main(argv):
    dates = [0] * 30
    (last_title, sum)=(None,0)
    
    for line in sys.stdin:
        (cur_title, value)=line.strip().split('\t')
        (date, views)=value.strip().split('&')

        if last_title and last_title!=cur_title:
            date_views=""
            for i in dates:
                date_views+="\t"+str(i)
            print str(sum)+"\t"+last_title+date_views
            
            hour_views=int(views)
            (last_title, sum)=(cur_title, hour_views)
            dates[int(date)-1]+=hour_views
        
        else:
            hour_views=int(views)
            (last_title, sum)=(cur_title, sum+hour_views)
            dates[int(date)-1]+=hour_views

    if last_title:
        date_views=""
        for i in dates:
            date_views+="\t"+str(i)
        print str(sum)+"\t"+last_title+date_views

if __name__=='__main__':
    main(sys.argv)
	
	

