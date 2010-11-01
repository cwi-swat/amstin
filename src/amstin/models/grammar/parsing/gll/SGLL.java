package amstin.models.grammar.parsing.gll;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Method;
import java.net.URI;

import amstin.models.grammar.parsing.gll.prods.Production;
import amstin.models.grammar.parsing.gll.result.AbstractNode;
import amstin.models.grammar.parsing.gll.result.ContainerNode;
import amstin.models.grammar.parsing.gll.result.struct.Link;
import amstin.models.grammar.parsing.gll.stack.AbstractStackNode;
import amstin.models.grammar.parsing.gll.stack.IReducableStackNode;
import amstin.models.grammar.parsing.gll.util.ArrayList;
import amstin.models.grammar.parsing.gll.util.DoubleArrayList;
import amstin.models.grammar.parsing.gll.util.IndexedStack;
import amstin.models.grammar.parsing.gll.util.IntegerKeyedHashMap;
import amstin.models.grammar.parsing.gll.util.LinearIntegerKeyedMap;
import amstin.models.grammar.parsing.gll.util.ObjectIntegerKeyedHashMap;
import amstin.models.grammar.parsing.gll.util.RotatingQueue;
import amstin.models.parsetree.Tree;

public abstract class SGLL implements IGLL{
	private final static int STREAM_READ_SEGMENT_SIZE = 8192;
	
	private URI inputURI;
	private char[] input;
	
	private final ArrayList<AbstractStackNode> todoList;
	
	// Updatable
	private final ArrayList<AbstractStackNode> stacksToExpand;
	private final RotatingQueue<AbstractStackNode> stacksWithTerminalsToReduce;
	private final RotatingQueue<AbstractStackNode> stacksWithNonTerminalsToReduce;
	private final ArrayList<AbstractStackNode[]> lastExpects;
	private final DoubleArrayList<AbstractStackNode, AbstractStackNode> possiblySharedExpects;
	private final ArrayList<AbstractStackNode> possiblySharedNextNodes;
	private final IntegerKeyedHashMap<ArrayList<AbstractStackNode>> possiblySharedEdgeNodesMap;

	private final ObjectIntegerKeyedHashMap<Object, ContainerNode> resultStoreCache;
	
	private int previousLocation;
	private int location;
	
	private AbstractStackNode root;
	
	public SGLL(){
		super();
		
		todoList = new ArrayList<AbstractStackNode>();
		
		stacksToExpand = new ArrayList<AbstractStackNode>();
		stacksWithTerminalsToReduce = new RotatingQueue<AbstractStackNode>();
		stacksWithNonTerminalsToReduce = new RotatingQueue<AbstractStackNode>();
		
		lastExpects = new ArrayList<AbstractStackNode[]>();
		possiblySharedExpects = new DoubleArrayList<AbstractStackNode, AbstractStackNode>();
		
		possiblySharedNextNodes = new ArrayList<AbstractStackNode>();
		possiblySharedEdgeNodesMap = new IntegerKeyedHashMap<ArrayList<AbstractStackNode>>();
		
		resultStoreCache = new ObjectIntegerKeyedHashMap<Object, ContainerNode>();
		
		previousLocation = -1;
		location = 0;
	}
	
	protected void expect(Production production, AbstractStackNode... symbolsToExpect){
		lastExpects.add(symbolsToExpect);
		
		AbstractStackNode lastNode = symbolsToExpect[symbolsToExpect.length - 1];
		lastNode.setParentProduction(production);
	}
	
	protected void expect(Production production, IReducableStackNode[] followRestrictions, AbstractStackNode... symbolsToExpect){
		lastExpects.add(symbolsToExpect);
		
		AbstractStackNode lastNode = symbolsToExpect[symbolsToExpect.length - 1];
		lastNode.setParentProduction(production);
		lastNode.setFollowRestriction(followRestrictions);
	}
	
	protected void expectReject(Production production, AbstractStackNode... symbolsToExpect){
		lastExpects.add(symbolsToExpect);
		
		AbstractStackNode lastNode = symbolsToExpect[symbolsToExpect.length - 1];
		lastNode.setParentProduction(production);
		lastNode.markAsReject();
	}
	
	protected void expectReject(Production production, IReducableStackNode[] followRestrictions, AbstractStackNode... symbolsToExpect){
		lastExpects.add(symbolsToExpect);
		
		AbstractStackNode lastNode = symbolsToExpect[symbolsToExpect.length - 1];
		lastNode.setParentProduction(production);
		lastNode.setFollowRestriction(followRestrictions);
		lastNode.markAsReject();
	}
	
	protected void invokeExpects(String name){
		try{
			Method method = getClass().getMethod(name);
			method.invoke(this);
		}catch(Exception ex){
			// Not going to happen.
			ex.printStackTrace(); // Temp
		}
	}
	
	private void updateProductionEndNode(AbstractStackNode sharedNode, AbstractStackNode node){
		AbstractStackNode prev = node;
		AbstractStackNode next = node.getNext();
		
		AbstractStackNode sharedPrev = sharedNode;
		AbstractStackNode sharedNext = sharedNode.getNext().getCleanCopy();
		do{
			prev = next;
			next = next.getNext();
			
			sharedNext = sharedNext.getCleanCopy();
			sharedPrev.addNext(sharedNext);
			
			sharedPrev = sharedNext;
			sharedNext = sharedNext.getNext();
			
			if(prev.hasEdges()){
				sharedPrev.addEdges(prev.getEdges());
			}
		} while (!(next == null || next == node));
	}
	
	private void updateNextNode(AbstractStackNode next, AbstractStackNode node){
		for(int i = possiblySharedNextNodes.size() - 1; i >= 0; i--){
			AbstractStackNode possibleAlternative = possiblySharedNextNodes.get(i);
			if(possibleAlternative.isSimilar(next)){
				addPrefixes(possibleAlternative, node);
				
				LinearIntegerKeyedMap<ArrayList<AbstractStackNode>> edges;
				if((edges = next.getEdges()) != null){
					possibleAlternative.addEdges(next.getEdges());
					
					if(!possibleAlternative.isClean()){
						// Something horrible happened; update the prefixes.
						updatePrefixes(possibleAlternative, node, edges);
					}
				}else{
					// Don't lose any edges.
					updateProductionEndNode(possibleAlternative, next);
				}
				return;
			}
		}
		
		if(next.startLocationIsSet()){
			next = next.getCleanCopy();
		}
		
		next.setStartLocation(location);
		possiblySharedNextNodes.add(next);
		stacksToExpand.add(next);
		
		addPrefixes(next, node);
	}
	
	private void addPrefixes(AbstractStackNode next, AbstractStackNode node){
		LinearIntegerKeyedMap<ArrayList<Link>> prefixesMap = node.getPrefixesMap();
		AbstractNode result = node.getResult();
		
		if(prefixesMap == null){
			next.addPrefix(new Link(null, result), node.getStartLocation());
		}else{
			int nrOfPrefixes = prefixesMap.size();
			for(int i = nrOfPrefixes - 1; i >= 0; i--){
				int startLocation = prefixesMap.getKey(i);
				
				next.addPrefix(new Link(prefixesMap.getValue(i), result), startLocation);
			}
		}
	}
	
	private void updatePrefixes(AbstractStackNode next, AbstractStackNode node, LinearIntegerKeyedMap<ArrayList<AbstractStackNode>> edges){
		Production production = next.getParentProduction();
		
		LinearIntegerKeyedMap<ArrayList<Link>> prefixesMap = node.getPrefixesMap();
		AbstractNode result = node.getResult();
		
		// Update results (if necessary).
		for(int i = edges.size() - 1; i >= 0; i--){
			int startLocation = edges.getKey(i);
			ArrayList<AbstractStackNode> edgesPart = edges.getValue(i);
			for(int j = edgesPart.size() - 1; j >= 0; j--){
				AbstractStackNode edge = edgesPart.get(j);
				
				if(edge.isMarkedAsWithResults()){
					Link prefix = constructPrefixesFor(prefixesMap, result, startLocation);
					if(prefix != null){
						ArrayList<Link> edgePrefixes = new ArrayList<Link>();
						edgePrefixes.add(prefix);
						ContainerNode resultStore = edge.getResultStore();
						if(!resultStore.isRejected()){
							resultStore.addAlternative(production, new Link(edgePrefixes, next.getResult()));
						}
					}
				}
			}
		}
	}
	
	private void updateEdgeNode(AbstractStackNode node, ArrayList<Link> prefixes, AbstractNode result, Production production){
		int startLocation = node.getStartLocation();
		ArrayList<AbstractStackNode> possiblySharedEdgeNodes = possiblySharedEdgeNodesMap.get(startLocation);
		if(possiblySharedEdgeNodes != null){
			for(int i = possiblySharedEdgeNodes.size() - 1; i >= 0; i--){
				AbstractStackNode possibleAlternative = possiblySharedEdgeNodes.get(i);
				if(possibleAlternative.isSimilar(node)){
					if(possibleAlternative.isMarkedAsWithResults()){
						ContainerNode resultStore = possibleAlternative.getResultStore();
						if(!resultStore.isRejected()){
							resultStore.addAlternative(production, new Link(prefixes, result));
						}
					}
					return;
				}
			}
		}else{
			possiblySharedEdgeNodes = new ArrayList<AbstractStackNode>();
			possiblySharedEdgeNodesMap.unsafePut(startLocation, possiblySharedEdgeNodes);
		}
		
		if(!node.isClean()){
			node = node.getCleanCopyWithPrefix();
			node.setStartLocation(startLocation);
		}
		
		//ProductionAdapter.getRhs(production);
		ContainerNode resultStore = resultStoreCache.get(production, startLocation);
		if(resultStore == null){
			resultStore = new ContainerNode(inputURI, startLocation, location - startLocation, node.isList());
			resultStoreCache.unsafePut(production, startLocation, resultStore);
			node.markAsWithResults();
			
			resultStore.addAlternative(production, new Link(prefixes, result));
		}
		node.setResultStore(resultStore);
		
		if(location == input.length && !node.hasEdges() && !node.hasNext()){
			root = node; // Root reached.
		}
		
		possiblySharedEdgeNodes.add(node);
		stacksWithNonTerminalsToReduce.put(node);
	}
	
	private void rejectEdgeNode(AbstractStackNode node, Production production){
		int startLocation = node.getStartLocation();
		ArrayList<AbstractStackNode> possiblySharedEdgeNodes = possiblySharedEdgeNodesMap.get(startLocation);
		if(possiblySharedEdgeNodes != null){
			for(int i = possiblySharedEdgeNodes.size() - 1; i >= 0; i--){
				AbstractStackNode possibleAlternative = possiblySharedEdgeNodes.get(i);
				if(possibleAlternative.isSimilar(node)){
					ContainerNode resultStore = possibleAlternative.getResultStore();
					resultStore.setRejected();
					return;
				}
			}
		}else{
			possiblySharedEdgeNodes = new ArrayList<AbstractStackNode>();
			possiblySharedEdgeNodesMap.unsafePut(startLocation, possiblySharedEdgeNodes);
		}
		
		if(!node.isClean()){
			node = node.getCleanCopyWithPrefix();
			node.setStartLocation(startLocation);
		}
		
		possiblySharedEdgeNodes.add(node);
	}
	
	private void move(AbstractStackNode node){
		Production production = node.getParentProduction();
		
		LinearIntegerKeyedMap<ArrayList<AbstractStackNode>> edges;
		if((edges = node.getEdges()) != null){
			LinearIntegerKeyedMap<ArrayList<Link>> prefixesMap = node.getPrefixesMap();
			if(!node.isReject()){
				AbstractNode result = node.getResult();
				
				for(int i = edges.size() - 1; i >= 0; i--){
					ArrayList<Link> prefixes = null;
					if(prefixesMap != null){
						int startLocation = edges.getKey(i);
						prefixes = prefixesMap.findValue(startLocation);
						if(prefixes == null) continue;
					}

					ArrayList<AbstractStackNode> edgeList = edges.getValue(i);
					for(int j = edgeList.size() - 1; j >= 0; j--){
						AbstractStackNode edge = edgeList.get(j);
						updateEdgeNode(edge, prefixes, result, production);
					}
				}
				
			}else if(node.isReducable() || !node.getResultStore().isRejected()){
				for(int i = edges.size() - 1; i >= 0; i--){
					ArrayList<Link> prefixes = null;
					if(prefixesMap != null){
						int startLocation = edges.getKey(i);
						prefixes = prefixesMap.findValue(startLocation);
						if(prefixes == null) continue;
					}

					ArrayList<AbstractStackNode> edgeList = edges.getValue(i);
					for(int j = edgeList.size() - 1; j >= 0; j--){
						AbstractStackNode edge = edgeList.get(j);
						rejectEdgeNode(edge, production);
					}
				}
			}
		}
		
		AbstractStackNode next;
		if((next = node.getNext()) != null){
			updateNextNode(next, node);
		}
	}
	
	private Link constructPrefixesFor(LinearIntegerKeyedMap<ArrayList<Link>> prefixesMap, AbstractNode result, int startLocation){
		if(prefixesMap == null){
			return new Link(null, result);
		}
		
		ArrayList<Link> prefixes = prefixesMap.findValue(startLocation);
		if(prefixes != null){
			return new Link(prefixes, result);
		}
		return null;
	}
	
	private void reduceTerminal(AbstractStackNode terminal){
		if(!terminal.reduce(input)) return;
		
		// Filtering
		if(terminal.isReductionFiltered(input, location)) return;
		
		move(terminal);
	}
	
	private void reduceNonTerminal(AbstractStackNode nonTerminal){
		// Filtering
		if(nonTerminal.isReductionFiltered(input, location)) return;
		
		move(nonTerminal);
	}
	
	private void reduce(){
		if(previousLocation != location){ // Epsilon fix.
			possiblySharedNextNodes.clear();
			possiblySharedEdgeNodesMap.clear();
			resultStoreCache.clear();
		}
		
		// Reduce terminals.
		while(!stacksWithTerminalsToReduce.isEmpty()){
			AbstractStackNode terminal = stacksWithTerminalsToReduce.unsafeGet();
			reduceTerminal(terminal);

			todoList.remove(terminal);
		}
		
		// Reduce non-terminals.
		while(!stacksWithNonTerminalsToReduce.isEmpty()){
			AbstractStackNode nonTerminal = stacksWithNonTerminalsToReduce.unsafeGet();
			reduceNonTerminal(nonTerminal);
		}
	}
	
	private void findStacksToReduce(){
		// Find the stacks that will progress the least.
		int closestNextLocation = Integer.MAX_VALUE;
		for(int i = todoList.size() - 1; i >= 0; i--){
			AbstractStackNode node = todoList.get(i);
			int nextLocation = node.getStartLocation() + node.getLength();
			if(nextLocation < closestNextLocation){
				stacksWithTerminalsToReduce.dirtyClear();
				stacksWithTerminalsToReduce.put(node);
				closestNextLocation = nextLocation;
			}else if(nextLocation == closestNextLocation){
				stacksWithTerminalsToReduce.put(node);
			}
		}
		
		previousLocation = location;
		location = closestNextLocation;
	}
	
	private boolean shareNode(AbstractStackNode node, AbstractStackNode stack){
		if(!node.isEpsilon()){
			for(int j = possiblySharedExpects.size() - 1; j >= 0; j--){
				AbstractStackNode possiblySharedNode = possiblySharedExpects.getFirst(j);
				if(possiblySharedNode.isSimilar(node)){
					possiblySharedExpects.getSecond(j).addEdge(stack);
					return true;
				}
			}
		}
		return false;
	}
	
	private void handleExpects(AbstractStackNode stackBeingWorkedOn){
		for(int i = lastExpects.size() - 1; i >= 0; i--){
			AbstractStackNode[] expectedNodes = lastExpects.get(i);
			int numberOfNodes = expectedNodes.length;
			AbstractStackNode first = expectedNodes[0];
			
			// Handle sharing (and loops).
			if(!shareNode(first, stackBeingWorkedOn)){
				AbstractStackNode last = expectedNodes[numberOfNodes - 1].getCleanCopy();
				AbstractStackNode next = last;
				
				for(int k = numberOfNodes - 2; k >= 0; k--){
					AbstractStackNode current = expectedNodes[k].getCleanCopy();
					current.addNext(next);
					next = current;
				}
				
				last.addEdge(stackBeingWorkedOn);
				
				next.setStartLocation(location);
				
				stacksToExpand.add(next);
				possiblySharedExpects.add(next, last);
			}
		}
	}
	
	private void expandStack(AbstractStackNode node){
		if(node.isReducable()){
			if((location + node.getLength()) <= input.length) todoList.add(node);
			return;
		}
		
		if(!node.isList()){
			invokeExpects(node.getName());
			
			handleExpects(node);
		}else{ // List
			AbstractStackNode[] listChildren = node.getChildren();
			
			AbstractStackNode child = listChildren[0];
			if(!shareNode(child, node)){
				stacksToExpand.add(child);
				possiblySharedExpects.add(child, child);
				possiblySharedNextNodes.add(child); // For epsilon list cycles.
			}
			
			if(listChildren.length > 1){ // Star list or optional.
				// This is always epsilon; so shouldn't be shared.
				stacksToExpand.add(listChildren[1]);
			}
		}
	}
	
	private void expand(){
		if(previousLocation != location){
			possiblySharedExpects.clear();
		}
		while(stacksToExpand.size() > 0){
			lastExpects.dirtyClear();
			expandStack(stacksToExpand.remove(stacksToExpand.size() - 1));
		}
	}
	
	protected boolean isInLookAhead(char[][] ranges, char[] characters){
		char next = input[location];
		for(int i = ranges.length - 1; i >= 0; i--){
			char[] range = ranges[i];
			if(next >= range[0] && next <= range[1]) return true;
		}
		
		for(int i = characters.length - 1; i >= 0; i--){
			if(next == characters[i]) return true;
		}
		
		return false;
	}
	
	public Tree parse(AbstractStackNode startNode, URI inputURI, char[] input){
		// Initialize.
		this.inputURI = inputURI;
		this.input = input;
		
		AbstractStackNode rootNode = startNode.getCleanCopy();
		rootNode.setStartLocation(0);
		stacksToExpand.add(rootNode);
		expand();
		
		do{
			findStacksToReduce();
			
			reduce();
			
			expand();
		}while(todoList.size() > 0);
		
		if(root == null){
			int errorLocation = (location == Integer.MAX_VALUE ? 0 : location);
			throw new RuntimeException("Parse Error before: " + errorLocation);
		}
		
		System.out.println(root.getResult());
		
		Tree result = root.getResult().toTree(new IndexedStack<AbstractNode>(), 0);
		return result;
	}
	
	public Tree parse(AbstractStackNode startNode, URI inputURI, String inputString){
		return parse(startNode, inputURI, inputString.toCharArray());
	}
	
	public Tree parse(AbstractStackNode startNode, URI inputURI, File inputFile) throws IOException{
		int inputFileLength = (int) inputFile.length();
		char[] input = new char[inputFileLength];
		Reader in = new BufferedReader(new FileReader(inputFile));
		try{
			in.read(input, 0, inputFileLength);
		}finally{
			in.close();
		}
		
		return parse(startNode, inputURI, input);
	}
	
	// This is kind of ugly.
	public Tree parse(AbstractStackNode startNode, URI inputURI, Reader in) throws IOException{
		ArrayList<char[]> segments = new ArrayList<char[]>();
		
		// Gather segments.
		int nrOfWholeSegments = -1;
		int bytesRead;
		do{
			char[] segment = new char[STREAM_READ_SEGMENT_SIZE];
			bytesRead = in.read(segment, 0, STREAM_READ_SEGMENT_SIZE);
			
			segments.add(segment);
			nrOfWholeSegments++;
		}while(bytesRead == STREAM_READ_SEGMENT_SIZE);
		
		// Glue the segments together.
		char[] segment = segments.get(nrOfWholeSegments);
		char[] input;
		if(bytesRead != -1){
			input = new char[(nrOfWholeSegments * STREAM_READ_SEGMENT_SIZE) + bytesRead];
			System.arraycopy(segment, 0, input, (nrOfWholeSegments * STREAM_READ_SEGMENT_SIZE), bytesRead);
		}else{
			input = new char[(nrOfWholeSegments * STREAM_READ_SEGMENT_SIZE)];
		}
		for(int i = nrOfWholeSegments - 1; i >= 0; i--){
			segment = segments.get(i);
			System.arraycopy(segment, 0, input, (i * STREAM_READ_SEGMENT_SIZE), STREAM_READ_SEGMENT_SIZE);
		}
		
		return parse(startNode, inputURI, input);
	}
	
	public Tree parse(AbstractStackNode startNode, URI inputURI, InputStream in) throws IOException{
		return parse(startNode, inputURI, new InputStreamReader(in));
	}
	
	private Tree makeParseTree(Tree tree){
		return tree;
	}
}
