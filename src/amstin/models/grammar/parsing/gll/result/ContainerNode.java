package amstin.models.grammar.parsing.gll.result;

import java.net.URI;
import java.util.Arrays;
import java.util.List;


import amstin.models.ast.Amb;
import amstin.models.ast.Cycle;
import amstin.models.ast.Location;
import amstin.models.ast.Obj;
import amstin.models.ast.Tree;
import amstin.models.grammar.parsing.gll.Production;
import amstin.models.grammar.parsing.gll.result.struct.Link;
import amstin.models.grammar.parsing.gll.util.ArrayList;
import amstin.models.grammar.parsing.gll.util.DoubleArrayList;
import amstin.models.grammar.parsing.gll.util.IndexedStack;
import amstin.models.grammar.parsing.gll.util.Stack;

public class ContainerNode extends AbstractNode{
	
	private final URI input;
	private final int offset;
	private final int length;
	
	private final boolean isListContainer;
	
	private boolean rejected;

	private Link firstAlternative;
	private Production firstProduction;
	private ArrayList<Link> alternatives;
	private ArrayList<Production> productions;
	
	public ContainerNode(URI input, int offset, int length, boolean isListContainer){
		super();
		
		this.input = input;
		this.offset = offset;
		this.length = length;
		
		this.isListContainer = isListContainer;
	}
	
	@Override
	public void addAlternative(Production production, Link children){
		if(firstAlternative == null){
			firstAlternative = children;
			firstProduction = production;
		}else{
			if(alternatives == null){
				alternatives = new ArrayList<Link>(1);
				productions = new ArrayList<Production>(1);
			}
			alternatives.add(children);
			productions.add(production);
		}
	}
	
	public boolean isEpsilon(){
		return false;
	}
	
	public void setRejected(){
		rejected = true;
		
		// Clean up.
		firstAlternative = null;
		alternatives = null;
		productions = null;
	}
	
	public boolean isRejected(){
		return rejected;
	}
	
	
	
	
	private void gatherAlternatives(Link child, ArrayList<String[]> gatheredAlternatives, IndexedStack<AbstractNode> stack, int depth){
		String result = child.node.toString(stack, depth);
		gatherProduction(child, new String[]{result}, gatheredAlternatives, stack, depth);
	}
	
	private void gatherProduction(Link child, String[] postFix, ArrayList<String[]> gatheredAlternatives, IndexedStack<AbstractNode> stack, int depth){
		ArrayList<Link> prefixes = child.prefixes;
		if(prefixes == null){
			gatheredAlternatives.add(postFix);
			return;
		}
		
		for(int i = prefixes.size() - 1; i >= 0; i--){
			Link prefix = prefixes.get(i);
			
			int length = postFix.length;
			String[] newPostFix = new String[length + 1];
			System.arraycopy(postFix, 0, newPostFix, 1, length);
			newPostFix[0] = prefix.node.toString(stack, depth);
			gatherProduction(prefix, newPostFix, gatheredAlternatives, stack, depth);
		}
	}
	
	private void gatherListAlternatives(Link child, ArrayList<String[]> gatheredAlternatives, IndexedStack<AbstractNode> stack, int depth){
		AbstractNode childNode = child.node;
		String result = childNode.toString(stack, depth);
		
		IndexedStack<AbstractNode> listElementStack = new IndexedStack<AbstractNode>();
		
		if(childNode.isContainer()) listElementStack.push(childNode, 0);
		gatherList(child, new String[]{result}, gatheredAlternatives, stack, depth, listElementStack, 1, new Stack<AbstractNode>());
		if(childNode.isContainer()) listElementStack.pop();
	}
	
	private void gatherList(Link child, String[] postFix, ArrayList<String[]> gatheredAlternatives, IndexedStack<AbstractNode> stack, int depth, IndexedStack<AbstractNode> listElementStack, int elementNr, Stack<AbstractNode> blackList){
		ArrayList<Link> prefixes = child.prefixes;
		if(prefixes == null){
			gatheredAlternatives.add(postFix);
			return;
		}
		
		for(int i = prefixes.size() - 1; i >= 0; i--){
			Link prefix = prefixes.get(i);
			
			if(prefix == null){
				gatheredAlternatives.add(postFix);
				continue;
			}
			
			AbstractNode prefixNode = prefix.node;
			
			if(blackList.contains(prefixNode)) continue;
			
			int index = listElementStack.contains(prefixNode);
			if(index != -1){
				int length = postFix.length;
				int repeatLength = elementNr - index;
				
				String[] newPostFix = new String[length - repeatLength + 1];
				System.arraycopy(postFix, repeatLength, newPostFix, 1, length - repeatLength);
				
				StringBuilder buffer = new StringBuilder();
				buffer.append("repeat(");
				for(int j = 0; j < repeatLength; j++){
					buffer.append(postFix[j]);
				}
				buffer.append(')');
				newPostFix[0] = buffer.toString();
				
				blackList.push(prefixNode);
				gatherList(prefix, newPostFix, gatheredAlternatives, stack, depth, listElementStack, elementNr + 1, blackList);
				blackList.pop();
			}else{
				int length = postFix.length;
				String[] newPostFix = new String[length + 1];
				System.arraycopy(postFix, 0, newPostFix, 1, length);
				
				if(prefixNode.isContainer()) listElementStack.push(prefixNode, elementNr);
				
				newPostFix[0] = prefixNode.toString(stack, depth);
				gatherList(prefix, newPostFix, gatheredAlternatives, stack, depth, listElementStack, elementNr + 1, blackList);
				
				if(prefixNode.isContainer()) listElementStack.pop();
			}
		}
	}
	
	private String getName() {
		return firstProduction.toString();
	}
	
	private void printAlternative(String[] children, StringBuilder out){
		out.append(getName());
		out.append('(');
		out.append(children[0]);
		for(int i = 1; i < children.length; i++){
			out.append(',');
			out.append(children[i]);
		}
		out.append(')');
	}
	
	private void print(StringBuilder out, IndexedStack<AbstractNode> stack, int depth){
		int index = stack.contains(this);
		if(index != -1){ // Cycle found.
			out.append("cycle(");
			out.append(getName());
			out.append(',');
			out.append((depth - index));
			out.append(")");
			return;
		}
		
		int childDepth = depth + 1;
		
		stack.push(this, depth); // Push
		
		// Gather
		ArrayList<String[]> gatheredAlternatives = new ArrayList<String[]>();
		if(!isListContainer){
			gatherAlternatives(firstAlternative, gatheredAlternatives, stack, childDepth);
			if(alternatives != null){
				for(int i = alternatives.size() - 1; i >= 0; i--){
					gatherAlternatives(alternatives.get(i), gatheredAlternatives, stack, childDepth);
				}
			}
		}else{
			gatherListAlternatives(firstAlternative, gatheredAlternatives, stack, childDepth);
			if(alternatives != null){
				for(int i = alternatives.size() - 1; i >= 0; i--){
					gatherListAlternatives(alternatives.get(i), gatheredAlternatives, stack, childDepth);
				}
			}
		}
		
		// Print
		int nrOfAlternatives = gatheredAlternatives.size();
		if(nrOfAlternatives == 1){
			printAlternative(gatheredAlternatives.get(0), out);
		}else{
			out.append('{');
			printAlternative(gatheredAlternatives.get(nrOfAlternatives - 1), out);
			for(int i = nrOfAlternatives - 2; i >= 0; i--){
				out.append(',');
				printAlternative(gatheredAlternatives.get(i), out);
			}
			out.append('}');
		}
		
		stack.purge(); // Pop
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		print(sb, new IndexedStack<AbstractNode>(), 0);
		
		return sb.toString();
	}
	
	public String toString(IndexedStack<AbstractNode> stack, int depth){
		StringBuilder sb = new StringBuilder();
		print(sb, stack, depth);
		
		return sb.toString();
	}
	
	
	////////////////////////////////////////
	////////////////////////////////////////
	////////////////////////////////////////
	////////////////////////////////////////
	////////////////////////////////////////
	////////////////////////////////////////
	////////////////////////////////////////
	////////////////////////////////////////
	////////////////////////////////////////
	
	private Tree cachedResult;
	
	private void gatherAlternatives(Link child, DoubleArrayList<Tree[], Production> gatheredAlternatives, Production production, IndexedStack<AbstractNode> stack, int depth){
		AbstractNode resultNode = child.node;
		
		if(!(resultNode.isEpsilon() && child.prefixes == null)){
			Tree result = resultNode.toTree(stack, depth);
			if(result == null) return; // Rejected.
			
			Tree[] postFix = new Tree[]{result};
			gatherProduction(child, postFix, gatheredAlternatives, production, stack, depth);
		}else{
			gatheredAlternatives.add(new Tree[]{}, production);
		}
	}
	
	private void gatherProduction(Link child, Tree[] postFix, DoubleArrayList<Tree[], Production> gatheredAlternatives, Production production, IndexedStack<AbstractNode> stack, int depth){
		ArrayList<Link> prefixes = child.prefixes;
		if(prefixes == null){
			gatheredAlternatives.add(postFix, production);
			return;
		}
		
		for(int i = prefixes.size() - 1; i >= 0; i--){
			Link prefix = prefixes.get(i);
			
			AbstractNode resultNode = prefix.node;
			if(!resultNode.isRejected()){
				Tree result = resultNode.toTree(stack, depth);
				if(result == null) return; // Rejected.
				
				int length = postFix.length;
				Tree[] newPostFix = new Tree[length + 1];
				System.arraycopy(postFix, 0, newPostFix, 1, length);
				newPostFix[0] = result;
				gatherProduction(prefix, newPostFix, gatheredAlternatives, production, stack, depth);
			}
		}
	}
	
	private void gatherListAlternatives(Link child, DoubleArrayList<Tree[], Production> gatheredAlternatives, Production production, IndexedStack<AbstractNode> stack, int depth){
		AbstractNode resultNode = child.node;
		
		if(!(resultNode.isEpsilon() && child.prefixes == null)){
			Tree result = resultNode.toTree(stack, depth);
			if(result == null) return; // Rejected.
			
			IndexedStack<AbstractNode> listElementStack = new IndexedStack<AbstractNode>();
			
			if(resultNode.isContainer()) listElementStack.push(resultNode, 0);
			gatherList(child, new Tree[]{result}, gatheredAlternatives, production, stack, depth, listElementStack, 1, new Stack<AbstractNode>());
			if(resultNode.isContainer()) listElementStack.purge();
		}else{
			gatheredAlternatives.add(new Tree[]{}, production);
		}
	}
	
	private void gatherList(Link child, Tree[] postFix, DoubleArrayList<Tree[], Production> gatheredAlternatives, Production production, IndexedStack<AbstractNode> stack, int depth, IndexedStack<AbstractNode> listElementStack, int elementNr, Stack<AbstractNode> blackList){
		ArrayList<Link> prefixes = child.prefixes;
		if(prefixes == null){
			gatheredAlternatives.add(postFix, production);
			return;
		}
		
		for(int i = prefixes.size() - 1; i >= 0; i--){
			Link prefix = prefixes.get(i);
			
			if(prefix == null){
				gatheredAlternatives.add(postFix, production);
				continue;
			}
			
			AbstractNode prefixNode = prefix.node;
			
			if(!prefixNode.isRejected()){
				if(blackList.contains(prefixNode)) continue;
				
				int index = listElementStack.contains(prefixNode);
				if(index != -1){
					int length = postFix.length;
					int repeatLength = elementNr - index;
					
					Tree[] newPostFix = new Tree[length - repeatLength + 1];
					System.arraycopy(postFix, repeatLength, newPostFix, 1, length - repeatLength);
					
					List<Tree> subList = new java.util.ArrayList<Tree>();
					for(int j = repeatLength - 1; j >= 0; j--){
						subList.add(postFix[j]);
					}
					
					List<Tree> cycleChildren = new java.util.ArrayList<Tree>();
					Obj subListNode = new Obj();
					subListNode.name = production.toString();
					subListNode.args = subList;
					
					//subListNode = subListNode.setAnnotation(Factory.Location, vf.sourceLocation(input, offset, length, -1, -1, -1, -1));
					cycleChildren.add(subListNode);
					
					Obj cycleNode = new Obj();
					cycleNode.name = production.toString();
					
					cycleChildren.add(cycleNode);
					
					Amb ambSubListNode = new Amb();
					ambSubListNode.alternatives = cycleChildren;
					newPostFix[0] = ambSubListNode;
					
					blackList.push(prefixNode);
					gatherList(prefix, newPostFix, gatheredAlternatives, production, stack, depth, listElementStack, elementNr + 1, blackList);
					blackList.pop();
				}else{
					int length = postFix.length;
					Tree[] newPostFix = new Tree[length + 1];
					System.arraycopy(postFix, 0, newPostFix, 1, length);
					
					if(prefixNode.isContainer()) listElementStack.push(prefixNode, elementNr);
					
					Tree result = prefixNode.toTree(stack, depth);
					if(result == null) return; // Rejected.
					
					newPostFix[0] = result;
					gatherList(prefix, newPostFix, gatheredAlternatives, production, stack, depth, listElementStack, elementNr + 1, blackList);
					
					if(prefixNode.isContainer()) listElementStack.purge();
				}
			}
		}
	}
	
	private Tree buildAlternative(Production production, Tree[] children){		
		Obj result = new Obj();
		result.args = new java.util.ArrayList<Tree>();
		for(int i = children.length - 1; i >= 0; i--){
			result.args.add(children[i]);
		}
		// TEMPORARILY
		result.name = production.toString();
		return result;
	}
	
	public Tree toTree(IndexedStack<AbstractNode> stack, int depth){
		if(cachedResult != null) return cachedResult;
		
		if(rejected) return null;
		
		int index = stack.contains(this);
		if(index != -1){ // Cycle found.
			// TODO: make a direct link to stack[index]
			Cycle cycle = new Cycle();
			cycle.value = depth - index;
			return (cachedResult = cycle);
		}
		
		int childDepth = depth + 1;
		
		stack.push(this, depth); // Push.
		
		// Gather
		DoubleArrayList<Tree[],Production> gatheredAlternatives = new DoubleArrayList<Tree[],Production>();
		
		if(!isListContainer){
			gatherAlternatives(firstAlternative, gatheredAlternatives, firstProduction, stack, childDepth);
			if(alternatives != null){
				for(int i = alternatives.size() - 1; i >= 0; i--){
					gatherAlternatives(alternatives.get(i), gatheredAlternatives, productions.get(i), stack, childDepth);
				}
			}
		}else{
			gatherListAlternatives(firstAlternative, gatheredAlternatives, firstProduction, stack, childDepth);
			if(alternatives != null){
				for(int i = alternatives.size() - 1; i >= 0; i--){
					gatherListAlternatives(alternatives.get(i), gatheredAlternatives, productions.get(i), stack, childDepth);
				}
			}
		}
		
		// Output.
		Tree result;
		
		int nrOfAlternatives = gatheredAlternatives.size();
		if(nrOfAlternatives == 1){ // Not ambiguous.
			Production production = gatheredAlternatives.getSecond(0);
			Tree[] alternative = gatheredAlternatives.getFirst(0);
			result = buildAlternative(production, alternative);
			if(input != null) {
				Location loc = new Location();
				loc.offset = offset;
				loc.length = length;
				// TODO: make loc field URI
				loc.file = input.toString();
				// TODO: locations on all trees
			}
			
		}else if(nrOfAlternatives == 0){ // Filtered.
			result = null;
		}else{ // Ambiguous.
			List<Tree> alts = new java.util.ArrayList<Tree>();
			for(int i = nrOfAlternatives - 1; i >= 0; i--){
				Production production = gatheredAlternatives.getSecond(i);
				Tree[] kids = gatheredAlternatives.getFirst(i);
				Obj alt = new Obj();
				alt.name = production.toString();
				// TODO: this makes them immutable(?)
				alt.args = Arrays.asList(kids);
				alts.add(alt);
			}
			
			result = new Amb();
			((Amb)result).alternatives = alts;
		}
		
		stack.purge(); // Pop.
		
		return (cachedResult = result);
	}
	
}
