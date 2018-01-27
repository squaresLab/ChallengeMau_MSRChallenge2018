/**
 * Copyright 2016 University of Zurich
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package examples;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Set;
import java.util.Vector;

import cc.kave.commons.model.events.IDEEvent;
import cc.kave.commons.model.events.completionevents.Context;
import cc.kave.commons.model.events.testrunevents.TestCaseResult;
import cc.kave.commons.model.events.testrunevents.TestResult;
import cc.kave.commons.model.events.testrunevents.TestRunEvent;
import cc.kave.commons.model.events.visualstudio.EditEvent;
import cc.kave.commons.model.naming.codeelements.IMethodName;
import cc.kave.commons.model.ssts.ISST;
import cc.kave.commons.model.ssts.IStatement;
import cc.kave.commons.model.ssts.declarations.IMethodDeclaration;
import cc.kave.commons.model.ssts.visitor.ISSTNode;
import cc.kave.commons.utils.io.IReadingArchive;
import cc.kave.commons.utils.io.ReadingArchive;
import costmodel.PerEditOperationStringNodeDataCostModel;
import distance.APTED;
import node.Node;
import node.StringNodeData;
import parser.BracketStringInputParser;

/**
 * Simple example that shows how the interaction dataset can be opened, all
 * users identified, and all contained events deserialized.
 */
public class GettingStarted {

	private String eventsDir;
	private int NUMBEROFSTMTTYPES = 84;

	public GettingStarted(String eventsDir) {
		this.eventsDir = eventsDir;
		try {
			Files.write(Paths.get("StmtTransactions.arff"), "".getBytes());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	int debuggingCases = 0;
	int debuggingEvents = 0;
	public void run() {

		System.out.printf("looking (recursively) for events in folder %s\n", new File(eventsDir).getAbsolutePath());

		/*
		 * Each .zip that is contained in the eventsDir represents all events that we
		 * have collected for a specific user, the folder represents the first day when
		 * the user uploaded data.
		 */
		Set<String> userZips = IoHelper.findAllZips(eventsDir);

		for (String userZip : userZips) {
			System.out.printf("\n#### processing user zip: %s #####\n", userZip);
			processUserZip(userZip);
		}
		System.out.println("Total debuggingCases: "+debuggingCases);
		System.out.println("Total debuggingEvents: "+debuggingEvents);
	}

	private void processUserZip(String userZip) {
		boolean isDebugging = false;
		ISST prevSST = null;
		ISST currSST = null;
		// open the .zip file ...
		try (IReadingArchive ra = new ReadingArchive(new File(eventsDir, userZip))) {
			// ... and iterate over content.
			while (ra.hasNext() /*&& goOn<10*/ ) {
				IDEEvent e = ra.getNext(IDEEvent.class);
				isDebugging = checkIfDebugging(e,isDebugging);
				if(isDebugging){
					debuggingEvents++;
					if (e instanceof EditEvent) {
						Context c =((EditEvent) e).Context2;
						if(treeIsValid(c)){
							currSST = ((EditEvent)e).Context2.getSST();
							String idP = prevSST!=null? prevSST.toString().substring(0, prevSST.toString().indexOf("{")):"";
							String idC = currSST.toString().substring(0, currSST.toString().indexOf("{"));
							if(prevSST != null && !Objects.equals(idP,idC)){
								//System.out.println("The two trees are different");
								checkTreeDifferences(prevSST, currSST);
							}
							prevSST = currSST;

						}
					}
				}
			}
		}
	}

	private boolean treeIsValid(Context c){
		if(c == null) return false;
		if(c.getSST().getMethods().size() > 0 || c.getSST().getFields().size() > 0){
			return true;
		}
		return false;

	}

	private boolean checkIfDebugging(IDEEvent e, boolean isDebugging){
		if (e instanceof TestRunEvent && !((TestRunEvent)e).WasAborted){
			if(containsAtLeastOneFailingTC((TestRunEvent)e)){
				if(!isDebugging){
					isDebugging = true;
					debuggingCases++;
					System.out.println("debuggingCases:"+debuggingCases);
				}
			}else{
				if(isDebugging){
					isDebugging = false;
				}
			}
		}
		return isDebugging;
	}


	private boolean containsAtLeastOneFailingTC(TestRunEvent tre){
		for(TestCaseResult tc: tre.Tests){
			if(tc.Result == TestResult.Failed){
				return true;
			}
		}
		return false;
	}

	private String transforToStringForm(ISST tree){
		StringBuilder strRep = new StringBuilder();
		strRep.append("{SST");
		for(IMethodDeclaration md : tree.getMethods()) {
			IMethodName m = md.getName();
			strRep.append("{"+m.toString().substring(0, m.toString().indexOf("(")));
			for (IStatement stmt : md.getBody()) {
				strRep.append(recursiveStmtToStringTransform(stmt));
			}
			strRep.append("}");
		}
		strRep.append("}");

		return strRep.toString();
	}

	private String recursiveStmtToStringTransform(IStatement stmt){
		String ret = String.valueOf(stmt).substring(0, String.valueOf(stmt).indexOf("{"));
		String children = "";
		if(stmt.getChildren().iterator().hasNext()){
			for( ISSTNode childNode : stmt.getChildren()){
				if(childNode instanceof IStatement){
					children += recursiveStmtToStringTransform((IStatement)childNode);
				}
			}
		}
		return "{"+ret+children+"}";
	}

	int counter =0;
	private Node<StringNodeData> findNodeInPostorder(Node<StringNodeData> node, int lookingFor){
		Node<StringNodeData> ret = null;
		Vector<Node<StringNodeData> > children = node.getChildren();
		for(Node<StringNodeData> child : children){
			if(ret==null){
				ret = findNodeInPostorder(child,lookingFor);
			}
		}
		counter++;
		if(counter == lookingFor){
			return node;
		}
		return ret;
	}

	private void checkTreeDifferences(ISST prevT, ISST currT){

		String sourceTree = transforToStringForm(prevT);
		String destinationTree = transforToStringForm(currT);

		BracketStringInputParser parser = new BracketStringInputParser();
		Node<StringNodeData> source = parser.fromString(sourceTree);
		Node<StringNodeData> dest = parser.fromString(destinationTree);
		// Initialise APTED.
		PerEditOperationStringNodeDataCostModel myCM = new PerEditOperationStringNodeDataCostModel(1,1,1);
		APTED<PerEditOperationStringNodeDataCostModel , StringNodeData> apted = new APTED<>(myCM);
		// Execute APTED.
		float ed = apted.computeEditDistance(source, dest);
		//System.out.println("editDistance:"+ed);

		LinkedList<int[]> em = apted.computeEditMapping();
		/*for(int[] i : em){
			System.out.print("("+i[0]+" "+ i[1]+")");
		}
		System.out.println();
		 */

		Set<String> typesSet = new HashSet<String>();
		//get the deleted nodes
		for(int[] i : em){
			if(i[1]==0){
				counter=0;
				Node node = findNodeInPostorder(source,i[0]);
				if(notMethodDeclOrSST(node)){
					typesSet.add(extractStmtType(node));
					//System.out.println(" "+node);
				}
			}
		}
		//get the added nodes
		for(int[] i : em){
			if(i[0]==0){
				counter=0;
				Node node = findNodeInPostorder(dest,i[1]);
				if(notMethodDeclOrSST(node)){
					typesSet.add(extractStmtType(node));
					//System.out.print(" "+node);
				}
			}
		}
		//System.out.println("\n");

		boolean[] stmtTypesArray = new boolean[NUMBEROFSTMTTYPES];
		for(String type: typesSet){
			stmtTypesArray[numeralStmtType(type)-1] = true;
			//System.out.println(type);
		}
		
		appendToFile(stmtTypesArray);
		
	}

	private void appendToFile(boolean[] stmtTypesArray ){
		StringBuilder toFileStr = new StringBuilder();
		toFileStr.append("\n");
		for(boolean b : stmtTypesArray){
			if(b){
				toFileStr.append("\"t\"");
			}else{
				toFileStr.append("?");
			}
			toFileStr.append(",");
		}
		toFileStr.deleteCharAt(toFileStr.length()-1);
		try {
		    Files.write(Paths.get("StmtTransactions.arff"), toFileStr.toString().getBytes(), StandardOpenOption.APPEND);
		}catch (IOException e) {
		    //exception handling left as an exercise for the reader
		}
		
	}
	
	private int numeralStmtType(String type){

		switch(type){
		case "IfElseBlock": return 1;
		case "ExpressionStatement": return 2;
		case "Assignment": return 3;
		case "ReturnStatement": return 4;
		case "VariableDeclaration": return 5;
		case "UsingBlock": return 6;
		case "ForEachLoop": return 7;
		case "TryBlock": return 8;
		case "CatchBlock": return 9;
		case "CaseBlock": return 10;
		case "DoLoop": return 11;
		case "ForLoop": return 12;
		case "LockBlock": return 13;
		case "SwitchBlock": return 14;
		case "UncheckedBlock": return 15;
		case "UnsafeBlock": return 16;
		case "WhileLoop": return 17;
		case "DelegateDeclaration": return 18;
		case "EventDeclaration": return 19;
		case "FieldDeclaration": return 20;
		case "MethodDeclaration": return 21;
		case "PropertyDeclaration": return 22;
		case "BinaryExpression": return 23;
		case "CastExpression": return 24;
		case "CompletionExpression": return 25;
		case "ComposedExpression": return 26;
		case "IfElseExpression": return 27;
		case "IndexAccessExpression": return 28;
		case "InvocationExpression": return 29;
		case "LambdaExpression": return 30;
		case "TypeCheckExpression": return 31;
		case "UnaryExpression": return 32;
		case "LoopHeaderBlockExpression": return 33;
		case "ConstantValueExpression": return 34;
		case "NullExpression": return 35;
		case "ReferenceExpression": return 36;
		case "UnknownExpression": return 37;
		case "EventReference": return 38;
		case "FieldReference": return 39;
		case "IndexAccessReference": return 40;
		case "MethodReference": return 41;
		case "PropertyReference": return 42;
		case "UnknownReference": return 43;
		case "VariableReference": return 44;
		case "BreakStatement": return 45;
		case "ContinueStatement": return 46;
		case "EventSubscriptionStatement": return 47;
		case "GotoStatement": return 48;
		case "LabelledStatement": return 49;
		case "ThrowStatement": return 50;
		case "UnknownStatement": return 51;
		case "AbstractExpressionNormalizationVisitor": return 52;
		case "AbstractStatementNormalizationVisitor": return 53;
		case "BooleanDeclarationUtil": return 54;
		case "BinaryOperatorUtil": return 55;
		case "BooleanNormalizationVisitor": return 56;
		case "ExpressionNormalizationVisitor": return 57;
		case "MethodLookup": return 58;
		case "ReferenceCollectorContext": return 59;
		case "ReferenceCollectorVisitor": return 60;
		case "RefLookup": return 61;
		case "StatementRegistry": return 62;
		case "AbstractConstantCollectorVisitor": return 63;
		case "ConstantCollectorVisitor": return 64;
		case "InlineConstantVisitor": return 65;
		case "DoLoopNormalizationVisitor": return 66;
		case "ForEachLoopNormalizationVisitor": return 67;
		case "ForLoopNormalizationVisitor": return 68;
		case "IteratorUtil": return 69;
		case "LoopNormalizationVisitor": return 70;
		case "StatementInsertionContext": return 71;
		case "StatementInsertionVisitor": return 72;
		case "StepInsertionContext": return 73;
		case "AbstractNodeFinderVisitor": return 74;
		case "NodeFinderVisitor": return 75;
		case "SwitchBlockNormalizationContext": return 76;
		case "SwitchBlockNormalizationVisitor": return 77;
		case "AbstractThrowingNodeVisitor": return 78;
		case "AbstractTraversingNodeVisitor": return 79;
		case "ToStringVisitor": return 80;
		case "CountReturnContext": return 81;
		case "CountReturnsVisitor": return 82;
		case "InvocationMethodNameVisitor": return 83;
		case "NameScopeVisitor": return 84;
		default: System.out.println("Could not transfer this type:"+type);
		}
		return -1;
	}

	private String extractStmtType(Node<StringNodeData> node){
		String nodeStr = String.valueOf(node);
		nodeStr = nodeStr.substring(1,nodeStr.length()-1);
		String nodeType = nodeStr.substring(0, nodeStr.indexOf("@"));
		return nodeType;
	}

	private boolean notMethodDeclOrSST(Node<StringNodeData> node){
		String nodeStr = String.valueOf(node);
		if(nodeStr.contains("MethodName")) return false;
		return true;
	}
}