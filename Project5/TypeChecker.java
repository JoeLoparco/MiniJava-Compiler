/**
 * COSC 4400 - Project 5
 * Explain briefly the functionality of the program.
 * @authors Joe Loparco Rosemary Shelton Luca Hergan
 * Instructor Jack Forden
 * TA-BOT:MAILTO joseph.loparco@marquette.edu rosemary.shelton@marquette.edu luca.hergan@marquette.edu
 */
/* Copyright (C) 1997-2003, Purdue Research Foundation of Purdue University.
 * All rights reserved.  */
/* Copyright (C) 2007, Marquette University.  All rights reserved. */
package Semant;

import Types.Type;
import Types.ARRAY;
import Types.BOOLEAN;
import Types.CLASS;
import Types.FIELD;
import Types.FUNCTION;
import Types.INT;
import Types.NIL;
import Types.OBJECT;
import Types.RECORD;
import Types.STRING;
import Types.VOID;
import Types.Visitor;

import java.util.Iterator;
import java.util.HashMap;

/**
 * Interface for Type Visitor Pattern traversals.
 */

public class TypeChecker implements Absyn.TypeVisitor
{
    Symbol.Table<CLASS>        classEnv = new Symbol.Table<CLASS>();
    Symbol.Table<Types.Type>   varEnv   = new Symbol.Table<Types.Type>();

    public static final BOOLEAN BOOLEAN = new BOOLEAN();
    public static final INT     INT     = new INT();
    public static final NIL     NIL     = new NIL();
    public static final VOID    VOID    = new VOID();
    public static final STRING  STRING  = new STRING();

    private CLASS currentClass;
    private FUNCTION currentMethod;

    public int errors = 0;
    private void error(Absyn.Absyn ast, String msg)
    {
        errors++;
		if (null == ast)
			System.err.println("ERROR " + msg + ": line not available");
		else
	    {
			System.err.print("ERROR " + msg + ": ");
			java.io.PrintWriter pw = new java.io.PrintWriter(System.err);
			Absyn.PrintVisitor pv = new Absyn.PrintVisitor(pw);
			pv.indentCount = 10;
			ast.accept(pv);
			pw.flush();
			System.err.println();
	    }
    }

    private void checkType(Type found, Type required, Absyn.Absyn ast)
    {
        if (!found.coerceTo(required))
            error(ast, "incompatible types: "
                  + required + " required, but "
                  + found + " found");
    }


	private void makeXinuType()
	{
		RECORD xinuFormals = new RECORD();
		CLASS xinuClass = new CLASS("Xinu");
		classEnv.put("Xinu", xinuClass);
		currentClass = xinuClass;

		java.util.LinkedList<Absyn.MethodDecl> xinuMethods
			= new java.util.LinkedList<Absyn.MethodDecl>();

		java.util.LinkedList<Absyn.Formal> params = new
			java.util.LinkedList<Absyn.Formal>();
		params.add(new Absyn.Formal(
					   new Absyn.IdentifierType("String"), "arg"));

		xinuMethods.add(new Absyn.MethodDecl(null, false, "print",
											 params, null, null, null));

		params = new java.util.LinkedList<Absyn.Formal>();
		params.add(new Absyn.Formal(
					   new Absyn.IntegerType(), "arg"));
		xinuMethods.add(new Absyn.MethodDecl(null, false, "printint",
											 params, null, null, null));
		xinuMethods.add(new Absyn.MethodDecl(null, false, "sleep",
											 params, null, null, null));

		params = new java.util.LinkedList<Absyn.Formal>();
		xinuMethods.add(new Absyn.MethodDecl(new Absyn.IntegerType(), 
											 false, "readint", params,
											 null, null, null));

		xinuMethods.add(new Absyn.MethodDecl(null, false, "println", params,
											 null, null, null));

		xinuMethods.add(new Absyn.MethodDecl(null, false, "yield", params,
											 null, null, null));

		params = new java.util.LinkedList<Absyn.Formal>();
		params.add(new Absyn.Formal(
					   new Absyn.IdentifierType("Thread"), "arg"));
		xinuMethods.add(new Absyn.MethodDecl(null, false, "threadCreate",
											 params, null, null, null));

		for (Absyn.MethodDecl md : xinuMethods)	
			{ 
			visit(md);
			}
			//System.out.println("Finshed Visiting Method Decl, Will Begin Adding xinuCLass Method FIELDS...");
		for (FIELD m : xinuClass.methods)//IT BREAKS HERE (MAYBE IDRK)	
		{ 	//System.out.println("Adding Xinu Methods to XINU CLASS...");
			xinuClass.instance.methods.put(m.type, m.name);
			}
		//System.out.println("Finshed Adding Xinu Methods to XINU CLASS...");
	}

    public Type visit(Absyn.Program ast)
    {
		RECORD program = new RECORD();
		Symbol.Table<Types.Type> localEnv = new Symbol.Table<Types.Type>();
		classEnv.beginScope();

		classEnv.put("String", new  CLASS("String"));
		classEnv.put("Thread", new CLASS("Thread"));
		makeXinuType();
	

		for (Absyn.ClassDecl c : ast.classes) {
			/*
			* TODO: Pass 1 - Register classes and detect duplicates
			* - Add all classes to the symbol table to track them.
			* - Create incomplete class types for later use.
			* - Check for duplicate class definitions and raise errors.
			*/
			//System.out.println("Starting Pass 1...");
			currentClass = (CLASS)c.accept(this);
			//System.out.println(currentClass.name);
			if (program.get(c.toString()) != null) {   // Table keeps track of hashmap to check duplicate initializations
				error(null,c.name);
			}
			else {
				program.put(currentClass,c.name);  // Add to program, keeps record of our new logical AST
			}
			
			//System.out.println("Ending Pass 1...");
			}
	


		for (Absyn.ClassDecl c : ast.classes) {
		/*
		* TODO: Pass 2 - Set up inheritance and define class members
		* - Link child classes to their parent classes (if any).
		* - Add fields and methods to each class.
		* - Ensure parent classes exist and raise errors if not found.
		*/
		//System.out.println("Starting Pass 2...");
		currentClass = classEnv.get(c.name);
		 if(c.parent != null){ // Check if current Absyn.ClassDecl is a child class
		 //System.out.println("c.parent != null");
			if(classEnv.get(c.parent) != null){ // Check to make sure parrentclass CLASS obj exists 
			currentClass.parent = classEnv.get(c.parent); // Link child class to parent class
			//System.out.println("We have gotten parent class from class env");
		  }
		  else{
			//System.out.println("Error: Parent Class OBJ DNE in Class Table");
			
		  }
		 }
		for (Absyn.MethodDecl methods : c.methods) {
			methods.accept(this);
			//System.out.println(methods.name);
			//System.out.println("HOWMANY METHODS?!");
			//System.out.println("In pass 2 running accept on method decl");
		}
		for (Absyn.VarDecl field : c.fields) {
			//System.out.println("HOW MANY FIELDS" + x);
			//System.out.println(field.name);
			visitFields(field);	
		}
		/*
		for (Absyn.VarDecl field : c.fields) {
			System.out.print("HOW MANY FIELDS");
			visitFields(field);

		}
		*/
		//System.out.println("Ending Pass 2...");
		}

        if (errors > 0) return program;

		for (Absyn.ClassDecl c : ast.classes) {
		/*
		* TODO: Pass 3 - Detect inheritance cycles and merge class members
		* - Check for cyclic inheritance and raise errors if found.
		* - Merge inherited fields and methods from parent classes.
		* - Ensure overridden methods match the parent types.
		*/
		//System.out.println("Starting Pass 3...");
		HashMap< String,Types.CLASS> map = new HashMap<>();
		currentClass = classEnv.get(c.name);
		map.put(c.name,currentClass);
		while (currentClass.parent != null){ //checking if next parent exists
			if(map.get(currentClass.parent) == null){ // if that parent is not in the hastable
				map.put(currentClass.parent.name,currentClass); // put parent in hastable
				currentClass = classEnv.get(currentClass.parent.name);
			}else{
				error(ast,"Error Cyclical Inheritance Detected");
				return VOID;
			}		
		}
		mergeParents(currentClass.parent, currentClass.instance, c);
		//System.out.println("Ending Pass3...");
		}

        if (errors > 0) return program;

        /*
         * 4th pass
         * - check method bodies
         */
		//System.out.println("Starting Pass 4...");
	for (Absyn.ClassDecl c : ast.classes) {
		currentClass = classEnv.get(c.name);
		varEnv.beginScope();
		
		// Add this reference to scope
		varEnv.put("this", currentClass);

		/*
		// Process fields first
		for (Absyn.VarDecl field : c.fields) {
			if (varEnv.get(field.name) != null) {
				error(field, "Duplicate field name: " + field.name);
			}
			//System.out.println("Inside pass 4 var decl loop");
			Type fieldType = field.accept(this);
			varEnv.put(field.name, fieldType);
		}
		*/
		
		// Process methods
		for (Absyn.MethodDecl method : c.methods) {
			currentMethod = (FUNCTION)method.checktype;
			//varEnv.printTable();
			//System.out.println("method scope before start scope");
			varEnv.beginScope();
			//varEnv.printTable();
			//System.out.println("method scope after start scope");
			
			
			// Process method parameters
			for (Absyn.Formal param : method.params) {
				if (varEnv.get(param.name) != null) {
					error(param, "Duplicate parameter name: " + param.name);
				}
				Type paramType = param.type.accept(this);
				varEnv.put(param.name, paramType);
			}

			for (Absyn.VarDecl field : method.locals) {
				if (varEnv.get(field.name) != null) {
					error(field, "Duplicate field name: " + field.name);
				}
				//System.out.println("Inside pass 4 var decl loop");
			
				Type fieldType = field.accept(this);
				varEnv.put(field.name, fieldType);
			}

			// Check return type matches (except for main)
			if (!method.name.equals("main") && method.returnVal != null) {
				Type returnType = method.returnVal.accept(this);
				//System.out.println(returnType.toString());

				if (method.returnType != null) {
					Type declaredType = method.checktype.result;
					//System.out.println(method.name);
					//System.out.println(declaredType.toString());
					//System.out.println(returnType.toString());
					//System.out.println("I am before checking checktype for non-main method in pass 4");
					checkType(returnType, declaredType, method.returnVal);
					//System.out.println("I am checking checktype for non-main method in pass 4");
				}
			}
			//varEnv.printTable();
			//System.out.println("method scope before end scope");
			varEnv.endScope(); // End method scope
			//varEnv.printTable();
			//System.out.println("method scope after end scope");
		}
		//varEnv.printTable();
		//System.out.println("class scope before end scope");
		varEnv.endScope(); // End class scope
		//varEnv.printTable();
		//System.out.println("class scope after end scope");

	}

		classEnv.endScope();
		//System.out.println("End of pass4 ");
		return program; 
    }

    public Type visit(Absyn.ClassDecl ast) 
    {
		// TODO: Implement visit(ClassDecl)
		// 
		// This method processes class declarations by associating the class name with a 
		// new `CLASS` type object. The class type is then registered in the `classEnv` 
		// to make it accessible throughout the program. Ensure that every class declaration 
		// is correctly typed and added to the environment without conflicts.
		//System.out.println("Begining Class Visit method....");
		CLASS newClass = new CLASS(ast.name);
		ast.checktype = newClass;
		classEnv.put(ast.name,newClass);
		//System.out.println("End of Class Visit method, CLASS visited: " +ast.name+"...");
		return newClass;
    }

    public Type visit(Absyn.ThreadDecl ast) 
    {
		// TODO: Implement visit(ThreadDecl)
		// 
		// Similar to class declarations, this method handles thread declarations by assigning 
		// them a `CLASS` type. The thread's type is also stored in the `classEnv`. Ensure that 
		// the thread is treated like a specialized class, and conflicts with other declarations 
		// are avoided.
		return null; 
    }

    public Type visit(Absyn.MethodDecl ast) 
    { 
	// TODO: Implement visit(MethodDecl)
	// 
	// This method handles method declarations by assigning types to return values and 
	// parameters, and adding them to the current classes method environment. A `FUNCTION` 
	// object is created to represent the method. 
	//
	//System.out.println("Begining Test");
	//System.out.println("Visiting Method "  + ast.name + "....");
	if(ast == null){
		error(ast,"Error MethodDecl null");
		return VOID;
	}
	RECORD parameters = new RECORD();
	Type returnType;
	//CHeck for null returnTyp
	if(ast.returnType == null){
		 //error(ast, "Error null methodDecl");
		 returnType = VOID;
	}
	else{
		 returnType = ast.returnType.accept(this); // Grab return type from return Val
	}
	
	FUNCTION newMethod = new FUNCTION(ast.name, currentClass.instance, parameters, returnType);
	//type check this statement
	currentClass.methods.put(newMethod, newMethod.name);
	ast.checktype = newMethod;
	//System.out.println(newMethod.toString());
	
	for (Absyn.Formal param : ast.params){ 
		//System.out.println("PIUTTING FIELD IN METHOD DECL");
		parameters.put(param.type.accept(this), param.name); 
		//System.out.println("DEBUG");

		
		// no idea if Im using this correctly tbh 
		//System.out.println("EXITING FIELD IN METHOD DECL");
	}
	//System.out.println("End Visit Method: " + ast.name);
	//System.out.println("New method name: " + newMethod.name);
	return newMethod;
    }

    public Type visit(Absyn.VoidDecl ast) 
    { 
		// This method processes method declarations by managing return types, parameters, 
		// and their integration into the class’s method environment. It creates a `FUNCTION` 
		// object to represent the method and ensures type correctness and uniqueness.
		//
		return null; 
    }

    public Type visitFields(Absyn.VarDecl ast) 
    { 
		/*
		* TODO: Handle field declarations in classes
		* - Determine the field's type using the `accept` method.
		* - Assign the type to the field’s `checktype`.
		* - Register the field in the class environment, raising an error for name conflicts.
		*/
		if(currentClass.fields.get(ast.name) == null){ // Check for name conflicts
			//System.out.println("Before type accept");
			Type type = ast.accept(this);
			//System.out.println(type.toString());
			ast.checktype = type;
			//System.out.print("INNOVATION!!");

			currentClass.fields.put(type, ast.name); // Register Field in current classEnv by adding it to current field record.
			//System.out.print("ARE WE ACTUALLY PUTTING IT IN?");
			//FIELD newField = new FIELD(ast.name, type);
			return null;
		}
		else{
			error(ast, "Error Name conflict found " + ast.name + " already Exists in class Env" + currentClass.name);
			return VOID;
		}
    }

    public Type visit(Absyn.VarDecl ast) 
    {
		//System.out.println("Before ast.type.accept");
		Type type;
		if(ast.type != null){
			type = ast.type.accept(this); // Determine Variable Type
			//System.out.println(type.toString());
			//System.out.println("AFter ast.type.accept");
			ast.checktype = type;
		}
		else{
			type = VOID;
		}
		//System.out.println("preinit");
		Type exprType;
		if(ast.init !=null){
			exprType = ast.init.accept(this); // Assign Var checktype
			//System.out.println("postinit");
		}
		else{
			exprType = VOID;
		}
		/*
		if(type == null){
			//System.out.println("Type is null");
		}*/
		if(exprType == VOID){
			//System.out.println("exprType is null");
			type = VOID;
		}
		//System.out.println("test");
		/*
		if(ast.init == null){
			//System.out.println("ast.init returned as null");
		}*/
		//System.out.println("after ast.init.accept");
		//System.out.println("Before");
		//System.out.println(exprType.toString());
		//System.out.println(type.toString());
		checkType(exprType, type, ast);
		//System.out.println("After");
		//System.out.println("After checktype in vardecl");

		if(varEnv.get(ast.name) == null) // check if variable exists
		{
			//System.out.println("Before varenv put");
			varEnv.put(ast.name, type);
			//System.out.println("After varenv put");
			//currentClass.fields.put(type, ast.name);
			//System.out.println("After fields put");
			return type;
		}
		else{
			//error(ast,"ERROR: variable found in Var Envionrment");
			return VOID;
		}
    }

    private boolean isLoop(CLASS c)
    {
        String name = c.name;
        boolean any;
        c.name = null;
        if (null == name) any = true;
        else if (c.parent != null) any = isLoop(c.parent);
        else any = false;
        c.name = name;
        return any;
    }
    
    private void mergeParents(CLASS parent, OBJECT instance, Absyn.ClassDecl ast)
    {
		if (null == parent) return;
        mergeParents(parent.parent, instance, ast);
		for (FIELD f : parent.fields)
	    {
			instance.fields.put(f.type, f.name);
	    }
		for (FIELD m : parent.methods)
	    {	for (FIELD i_m : instance.methods) {
				if (i_m.type != m.type){
					error(ast,"merging parents has failed, overriden method types do not match.");
				}else{
					FIELD old = instance.methods.get(m.name);
					if (old == null) instance.methods.put(m.type, m.name);
					else old.type = m.type;
				}
		}
	    }
    }
    
    public Type visit(java.util.AbstractList list)
    {
		for (Object o : list)
	    {
			((Absyn.Visitable)o).accept(this);
	    }
		return null; 
    }

    /* The Statements */
    public Type visit(Absyn.AssignStmt ast){
		//**
		return null; 
    }
    public Type visit(Absyn.BlockStmt ast)
    {
		return null; 
    }

    public Type visit(Absyn.IfStmt ast)
    { 
		/*
		* TODO: Handle `if` statements and type-check branches
		* - Ensure the condition evaluates to `BOOLEAN` 
		* - Visit the "then" branch to confirm it is valid.
		* - If an "else" branch exists, visit and validate it.
		* - Return `VOID` since `if` statements do not produce a value.
		*/
		return null; 
    }

	public Types.Type visit(Absyn.XinuCallStmt ast)
	{
		return null; 
	}

    public Types.Type visit(Absyn.XinuCallExpr ast)
	{
		return null; 
	}

	public Type visit(Absyn.WhileStmt ast) 
	{ 
		return null; 
	}

	private Type visit(Absyn.BinOpExpr e, String op, Type t, Type rt) 
	{ 
		return null; 
	}

	private Type visit(Absyn.BinOpExpr e, String op, Type t) 
	{   
		return null; 
	}


    private Type visit (Absyn.BinOpExpr e, String op)
    {
      return null; 
    }

    /* The Expressions */
    public Type visit(Absyn.AddExpr ast)  { return visit(ast, "+", INT); }
    public Type visit(Absyn.AndExpr ast)  { return visit(ast, "&&", BOOLEAN); }
    public Type visit(Absyn.DivExpr ast)  { return visit(ast, "/", INT); }
    public Type visit(Absyn.MulExpr ast)  { return visit(ast, "*", INT); }
    public Type visit(Absyn.SubExpr ast)  { return visit(ast, "-", INT); }
    public Type visit(Absyn.EqualExpr ast){ return visit(ast, "==", BOOLEAN); }
    public Type visit(Absyn.GreaterExpr ast) { return visit(ast, ">", INT, BOOLEAN); }
	public Type visit(Absyn.LesserExpr ast) { return visit(ast, "<", INT, BOOLEAN); }
    public Type visit(Absyn.NotEqExpr ast) { return visit(ast, "!=", BOOLEAN); }
	public Type visit(Absyn.NotExpr ast) 
	{ 
		// This method processes logical negation expressions (`!`). 
		// It ensures that the operand is of type `BOOLEAN`.
		// If the operand's type is incompatible, an error is raised.
		// The result of this expression is always `BOOLEAN`.
		//System.out.println("Visiting NotExpr...");
		
		Type operandExpr = ast.e1.accept(this);
		//System.out.println("Visiting NotExpr...");
		//System.out.println(operandExpr.toString());
		checkType(operandExpr, BOOLEAN, ast.e1);
	 	//System.out.println("Visiting NotExpr... Finished");
		return BOOLEAN; 
	}

	public Type visit(Absyn.NegExpr ast) 
	{ 
		//
		// This method handles arithmetic negation expressions (`-`). 
		// It ensures that the operand is of type `INT`.
		// If the operand's type is not compatible, an error is raised.
		// The result of this expression is always `INT`.
		Type numExpr = ast.e1.accept(this);
		checkType(numExpr, INT, ast.e1);
		return INT; 
	}

    public Type visit(Absyn.OrExpr ast) 
	{ 
		return visit(ast, "||", BOOLEAN); 
	}

    public Type visit(Absyn.ArrayExpr ast) 
    { 	/*
		Absyn.Expr target = ast.target;
		if (null != ast.index){
			Absyn.Expr index = ast.index;
			System.out.print(INT.toString());
			checkType(index.accept(this),INT,ast);
		}

		Type t = target.accept(this);
		//t = new ARRAY(t);
		*/
		return null;
    }

    public Type visit(Absyn.CallExpr ast)
    {	
		Type target = ast.target.accept(this);
		if (!(target instanceof OBJECT))
	    {
			error(ast, "target not object, type "
				  + target);
			return VOID;
	    }
		FIELD meth  = ((OBJECT)target).methods.get(ast.method);
		if (null == meth)
	    {
			error(ast, "cannot resolve method " 
				  + ast.method);
			return VOID;
	    }
		FUNCTION methType = (FUNCTION)meth.type;
		if ((null != methType.self) && (!target.coerceTo(methType.self)))
	    {
			error(ast, "implicit self parameter not "
				  + "compatible" + methType.self + ", "
				  + target);
	    }
		Iterator formals = methType.formals.iterator();
		Iterator actuals = ast.args.iterator();
		while (formals.hasNext() && actuals.hasNext())
	    {
			Type formal = (Type)formals.next();
			Absyn.Expr actual = (Absyn.Expr)actuals.next();
			checkType(actual.accept(this), 
					  ((FIELD)formal).type,
					  actual);
	    }
		if (formals.hasNext() || actuals.hasNext())
            error(ast, "mismatch in number of arguments");
		ast.typeIndex = meth.index;
		return methType.result; 
    }

    public Type visit(Absyn.FieldExpr ast)
    {
		Type target = ast.target.accept(this);
		if ((target instanceof ARRAY) && (ast.field.equals("length")))
		{
			ast.typeIndex = -1;
			return INT;
		}
		if (!(target instanceof OBJECT))
	    {
			error(ast, "target not object, type "
				  + target);
			return VOID;
	    }
		FIELD field  = ((OBJECT)target).fields.get(ast.field);
		if (null == field)
	    {
			error(ast, "cannot resolve field " 
				  + ast.field);
			return VOID;
	    }
		ast.typeIndex = field.index;
		return field.type;
    }

    public Type visit(Absyn.Formal ast)
    {
		ast.checktype = ast.type.accept(this);
		return ast.checktype; 
    }

    public Type visit(Absyn.IdentifierExpr ast)
    { 
		
		CLASS classType = classEnv.get(ast.id);
		if(classType != null) {
    		return classType.instance;
    	}
		

		if(varEnv.get(ast.id) != null){
			return varEnv.get(ast.id);
		}else{
			return null;
		}
    }

    public Type visit(Absyn.NewArrayExpr ast)
    { 
		Type t = ast.type.accept(this);
		for (Absyn.Expr e : ast.dimensions)
	    {
			if (null != e) {
				//System.out.println("Before CHECHTYPE");
				checkType(e.accept(this), INT, ast);
				//System.out.println(e.accept(this).toString());
			}

			t = new ARRAY(t);
			//System.out.println(t.toString());
	    }
		return t;
    }

    public Type visit(Absyn.NewObjectExpr ast)
    {   
		Type type = ast.type.accept(this);
		return type; 
    }

    public Type visit(Absyn.NullExpr ast) { 
		return NIL; 
		}

    public Type visit(Absyn.ThisExpr ast)
    {
		if (currentClass == null) {
        	error(ast, "'this' cannot be used outside of a class context");
        	return VOID;
    	}
    
    	return currentClass.instance; 
    }

    /* The Types */
    public Type visit(Absyn.ArrayType ast) {
		Type t = ast.base.accept(this);
		t = new ARRAY(t);
		return t;
	}
    public Type visit(Absyn.IdentifierType ast) 
    { 
		
		/*if((currentClass = classEnv.get(ast.id)) == null){
			error(ast,"Error: Identifer " + ast.id + " not found in var Envionrment" );
		 }else{
			return currentClass.instance.accept(this);	 
		 }
			*/

    	CLASS classType = classEnv.get(ast.id);
		if (null != classType){
			return classType.instance;
		}

		if(varEnv.get(ast.id) != null){
			return varEnv.get(ast.id);
		}else{
			return null;
		}
	}
    public Type visit(Absyn.IntegerType ast)    { 
		return INT; }
    public Type visit(Absyn.IntegerLiteral ast) { 
		return INT; }
    public Type visit(Absyn.StringLiteral ast)  { 
		return STRING; }
    public Type visit(Absyn.BooleanType ast)    { 
		return BOOLEAN; }
    public Type visit(Absyn.FalseExpr ast)      {  
		return BOOLEAN; }
    public Type visit(Absyn.TrueExpr ast)       {  
		return BOOLEAN; }
}
