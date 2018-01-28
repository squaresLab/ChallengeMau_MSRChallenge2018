#/bin/python

import argparse
import os
import xml.etree.ElementTree
import subprocess
import sys
import shutil
import time
import re

fullyCoveredInstances=0
partiallyCoveredInstances=0
nonCoveredInstances=0
instanceCount=0
ruleCount=0

w,h = 84,100000
editMap = {
"IfElseBlock":0,
"ExpressionStatement":1,
"Assignment":2,
"ReturnStatement":3,
"VariableDeclaration":4,
"UsingBlock":5,
"ForEachLoop":6,
"TryBlock":7,
"CatchBlock":8,
"CaseBlock":9,
"DoLoop":10,
"ForLoop":11,
"LockBlock":12,
"SwitchBlock":13,
"UncheckedBlock":14,
"UnsafeBlock":15,
"WhileLoop":16,
"DelegateDeclaration":17,
"EventDeclaration":18,
"FieldDeclaration":19,
"MethodDeclaration":20,
"PropertyDeclaration":21,
"BinaryExpression":22,
"CastExpression":23,
"CompletionExpression":24,
"ComposedExpression":25,
"IfElseExpression":26,
"IndexAccessExpression":27,
"InvocationExpression":28,
"LambdaExpression":29,
"TypeCheckExpression":30,
"UnaryExpression":31,
"LoopHeaderBlockExpression":32,
"ConstantValueExpression":33,
"NullExpression":34,
"ReferenceExpression":35,
"UnknownExpression":36,
"EventReference":37,
"FieldReference":38,
"IndexAccessReference":39,
"MethodReference":40,
"PropertyReference":41,
"UnknownReference":42,
"VariableReference":43,
"BreakStatement":44,
"ContinueStatement":45,
"EventSubscriptionStatement":46,
"GotoStatement":47,
"LabelledStatement":48,
"ThrowStatement":49,
"UnknownStatement":50,
"AbstractExpressionNormalizationVisitor":51,
"AbstractStatementNormalizationVisitor":52,
"BooleanDeclarationUtil":53,
"BinaryOperatorUtil":54,
"BooleanNormalizationVisitor":55,
"ExpressionNormalizationVisitor":56,
"MethodLookup":57,
"ReferenceCollectorContext":58,
"ReferenceCollectorVisitor":59,
"RefLookup":60,
"StatementRegistry":61,
"AbstractConstantCollectorVisitor":62,
"ConstantCollectorVisitor":63,
"InlineConstantVisitor":64,
"DoLoopNormalizationVisitor":65,
"ForEachLoopNormalizationVisitor":66,
"ForLoopNormalizationVisitor":67,
"IteratorUtil":68,
"LoopNormalizationVisitor":69,
"StatementInsertionContext":70,
"StatementInsertionVisitor":71,
"StepInsertionContext":72,
"AbstractNodeFinderVisitor":73,
"NodeFinderVisitor":74,
"SwitchBlockNormalizationContext":75,
"SwitchBlockNormalizationVisitor":76,
"AbstractThrowingNodeVisitor":77,
"AbstractTraversingNodeVisitor":78,
"ToStringVisitor":79,
"CountReturnContext":80,
"CountReturnsVisitor":81,
"InvocationMethodNameVisitor":82,
"NameScopeVisitor":83
}
rulesMatrix = [['?' for x in range(w)] for y in range(h)] 

def getStats(args):

	global fullyCoveredInstances
	global partiallyCoveredInstances
	global nonCoveredInstances
	global instanceCount
	global ruleCount
	#cmd = "rm -f "+ args.outputCsvFile
	#subprocess.call(cmd, shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE) 
	
	with open(args.instancesCsvFile) as fIns:
		for lineIns in fIns:
			ruleCount=0
			#print "\nINSTANCE: "
			#print str(lineIns.strip())
			coveredEdits = [0 for x in range(w)]
			numberOfEditsInInstance = 0
			for i in range(len(lineIns.strip().split(','))):
				if lineIns.strip().split(',')[i] != '?':
					numberOfEditsInInstance+=1
				
			with open(args.rulesCsvFile) as fRules:
				for lineRules in fRules:
					
					lineApplies=True
					for i in range(len(lineRules.strip().split(','))):
						if lineRules.strip().split(',')[i] != '?' and lineIns.strip().split(',')[i] == '?':
							#print "Rule Doesnt Apply: instance: "+str(lineIns) +" rule: "+ str(lineRules)
							lineApplies=False
							break
					if lineApplies:
						#print "Applies! rule: "+ str(lineRules.strip())
						for i in range(len(lineRules.strip().split(','))):
							if lineRules.strip().split(',')[i] != '?':
								coveredEdits[i] = 1
					#lineApplies=True
					ruleCount+=1
					
			#evaluate if all edits were covered
			numberOfEditsCovered=0
			for i in range(len(lineIns.strip().split(','))):
				if coveredEdits[i] == 1:
					numberOfEditsCovered+=1
			
			#print "Edits in instance: "+str(numberOfEditsInInstance) + " How many edits are covered by the rules: " +str(numberOfEditsCovered) 
			if numberOfEditsCovered==0:
				nonCoveredInstances+=1
				#print "Non covered"
			elif numberOfEditsCovered == numberOfEditsInInstance:
				fullyCoveredInstances+=1
				#print "Fully covered"
			else:
				partiallyCoveredInstances+=1
				#print "Partially covered"
				
			instanceCount+=1

def getOptions():
        parser = argparse.ArgumentParser(description="Example of usage: python getRuleAccuracy.py ComplementRulesLB0.001C1NR10000/Complement10RulesLB0.001C1NR10000.csv UsefulFold10.csv")
        parser.add_argument("rulesCsvFile", help="file with rules")
        parser.add_argument("instancesCsvFile", help="file with instances")
        return parser.parse_args()

def main():
	args=getOptions()
	getStats(args)
	print "fullyCoveredInstances," + str(fullyCoveredInstances)
	print "partiallyCoveredInstances," + str(partiallyCoveredInstances)
	print "nonCoveredInstances," + str(nonCoveredInstances)
	print "instanceCount," + str(instanceCount)
	print "ruleCount," + str(ruleCount)

main()