#!/usr/bin/python

import sys
import os

def main(argv):
    date=str(os.environ["map.input.file"].split('-')[2])
    date=date[-2:]
    
    for line in sys.stdin:
        if line.startswith("en "):
            el=line.strip().split(' ')
            title=el[1]
            views=el[2]
            fc=ord(title[0])-ord('a')
            if fc<=25 and fc>=0 :continue
            if "404_error/" == title: continue
            if "Main_Page" == title: continue
            if "Hypertext_Transfer_Protocol" == title: continue
            if "Favicon.ico" == title: continue
            if "Search" == title: continue
            if title.startswith("Media"): continue
            if title.startswith("Special"): continue
            if title.startswith("Talk"): continue
            if title.startswith("User"): continue
            if title.startswith("User_talk"): continue
            if title.startswith("Project"): continue
            if title.startswith("Project_talk"): continue
            if title.startswith("File"): continue
            if title.startswith("File_talk"): continue
            if title.startswith("MediaWiki"): continue
            if title.startswith("MediaWiki_talk"): continue
            if title.startswith("Template"): continue
            if title.startswith("Template_talk"): continue
            if title.startswith("Help"): continue
            if title.startswith("Help_talk"): continue
            if title.startswith("Category"): continue
            if title.startswith("Category_talk"): continue
            if title.startswith("Portal"): continue
            if title.startswith("Wikipedia"): continue
            if title.startswith("Wikipedia_talk"): continue
            if title.endswith(".jpg"): continue
            if title.endswith(".gif"): continue
            if title.endswith(".png"): continue
            if title.endswith(".JPG"): continue
            if title.endswith(".GIF"): continue
            if title.endswith(".PNG"): continue
            if title.endswith(".txt"): continue
            if title.endswith(".ico"): continue

            print title+"\t"+date+"&"+views

if __name__=='__main__':
    main(sys.argv)
	
	

