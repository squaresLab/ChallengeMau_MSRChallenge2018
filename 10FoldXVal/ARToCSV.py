#/bin/python

import argparse
import os
import xml.etree.ElementTree
import subprocess
import sys
import shutil
import time
import re

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
rulesMatrix = [["?" for x in range(w)] for y in range(h)] 

def convertToCsv(args):
	cmd = "rm -f "+ args.outputCsvFile
	subprocess.call(cmd, shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE) 
	nextIsConsequent=False
	with open(args.rulesFile) as f:
		lineNum=0
		for line in f:
			if str(line.split('.')[0]).strip().isdigit():
				rule = line.split('.')[1].split('<')[0].strip()
				for s in rule.split(' '):
					if s.endswith("=t"):
						if nextIsConsequent:
							edit=s[:-2]
							rulesMatrix[lineNum][editMap[edit]] = "c"
						else:
							edit=s[:-2]
							rulesMatrix[lineNum][editMap[edit]] = "t"
					if s == "==>":
						nextIsConsequent = True
				ruleInCsv=""
				for i in range(len(rulesMatrix[lineNum])):
					ruleInCsv+= str(rulesMatrix[lineNum][i])+","
				ruleInCsv=ruleInCsv[:-1]
				#print ruleInCsv
				
				#create output csv file
				
				cmd = "echo "+ruleInCsv+ " >> " + args.outputCsvFile
				subprocess.call(cmd, shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE) 
				
				nextIsConsequent=False
				lineNum+=1

def getOptions():
        parser = argparse.ArgumentParser(description="Example of usage: python ARToCSV.py FoldsMutOps/Complement10RulesLB0.001C1NR10000.txt FoldsMutOps/Complement10RulesLB0.001C1NR10000.csv")
        parser.add_argument("rulesFile", help="file with rules")
        parser.add_argument("outputCsvFile", help="output file")
        return parser.parse_args()

def main():
	args=getOptions()
	convertToCsv(args)

main()