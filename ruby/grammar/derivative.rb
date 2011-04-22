function insertGood(array, val) {
	if (val != undefined)
		array.push(val);
}
function checkToken(token, kind, result) {
	if (token == kind)
		result.CLASS = "Emptiness";
	else
		return false;
}
function delta(token) {
	var newrules = [];
	var mentioned = [];
	var RuleNum = 1;
	Delta = {
		Grammar: function(token, gram, result, recurse, i) {
			result.CLASS = gram.CLASS;
			result.name = gram.name;
			result.rules = [];
			result.start = recurse(gram.start);
			if (newRule == undefined)
				error("parse fail");
			for (i in newrules)
				insertGood(result.rules, newrules[i]);
			copy = MakeCopier(gram, result);
			for (i in mentioned)
				result.rules.push(copy(mentioned[i]));
		},
		Rule: function(_, obj, result, recurse, i) {
			result.name = obj.name + (RuleNum++);
			result.alts = [];
			obj.alts.ForEach(function(alt) {
				insertGood(result.rules, recurse(alt));
			});
		},
		Ref: function(_, ref, result, recurse, i) {
			result.rule = recurse(ref.rule);			
		},
		IterStar: function(_, obj, result, recurse, i) {
			if (CanGenerateNull.lookup(obj))
				result.sub = recurse(obj.sub);
			else
				f.SequenceInit(result, recurse(obj.sub), copy(obj.sub));
		},
		Opt: function(_, obj, result, recurse, i) {
			f.OptInit(result, recurse(obj.sub));
		},
		Sequence: function(_, obj, result, recurse, i) {
			if (CanGenerateNull(obj.left))
				f.AltInit(result, recurse(obj.right),
						f.Seq(recurse(obj.left), copy(obj.right)));
			else
				f.SeqInit(result, recurse(obj.left), copy(obj.right));
		},		
		RefSpec: function(_, obj, result, recurse, i) {
			return checkToken(token, IDENT, result);
		},
		Int: function(_, obj, result, recurse, i) {
			return checkToken(token, INT, result);
		},
		Real: function(_, obj, result, recurse, i) {
			return checkToken(token, REAL, result);
		},
		Id: function(_, obj, result, recurse, i) {
			return checkToken(token, IDENT, result);
		},
		Key: function(_, obj, result, recurse, i) {
			return checkToken(token, IDENT, result);
		},
		Lit: function(_, obj, result, recurse, i) {
			return checkToken(token, obj.value, result);
		}
	}
}
	

What about Fixpoint Cyclic Map
  
data = info[obj.key]
if data.computed then
	return data.value;
if !CHANGE {
	INCYCLE = true
	data.visited = true
	do {
		CHANGE = false
		compute();
	} while (CHANGE);
    data.computed = true;
	data.visited = false;
	INCYCLE = false;
}
else if !data.visited {
	data.visited = true
	compute();
	data.visited = false;
}
return data.value;

compute()
	val = call(...);
	CHANGE |= val != data.value;
	data.value = val;




