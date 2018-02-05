#/bin/python

import argparse
import os
import xml.etree.ElementTree
import subprocess
import sys
import shutil
import time
import re
from random import randint

complementArray = []  

def removeEmptyOnes(args):
	EDaccerts = 0
	HBaccerts = 0
	foldCount = 0
	buildComplementArray(args)
	with open(args.fold) as foldInstances:
		for foldInstance in foldInstances:
			instance = foldInstance.split(",")
			randED = getEDRand()
			randHB = getHBRand()
			if instance[randED] == "t":
				EDaccerts += 1
			if instance[randHB] == "t": 
				HBaccerts += 1
			foldCount+=1
	
	print "EDaccerts:"+str(EDaccerts)
	print "HBaccerts:"+str(HBaccerts)
	print "Instance count in fold: "+str(foldCount)
		
def getEDRand():
	return randint(0,83)

def getHBRand():
	size = len(complementArray)
	#print "size:"+str(size)
	rand = randint(0,size-1)
	#print "rand:"+ str(rand)
	return complementArray[rand]

	
def buildComplementArray(args):
	global complementArray
	with open(args.comp) as complementInstances:
		for complementInstance in complementInstances:
			instance = complementInstance.split(",")
			for i in range(0,83):
				if instance[i] == "t":
					complementArray.append(i)

def getOptions():
        parser = argparse.ArgumentParser(description="Example of usage: python 10FoldDistribution.py StmtTransactionsFold1.csv StmtTransactionsComplement1.csv")
        parser.add_argument("fold", help="fold csv")
        parser.add_argument("comp", help="complement csv")
        return parser.parse_args()

def main():
	args=getOptions()
	removeEmptyOnes(args)
	
main()