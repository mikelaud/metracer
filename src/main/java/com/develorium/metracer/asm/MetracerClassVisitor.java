/*
 * Copyright 2015-2016 Michael Kocherov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.develorium.metracer.asm;

import java.util.*;
import java.util.regex.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

public class MetracerClassVisitor extends ClassVisitor {
	private boolean isChanged = false;
	private boolean hasSlf4Logger = false;
	private String className = null;
	private Pattern classMatchingPattern = null;
	private Pattern methodMatchingPattern = null;
	private ClassNode parsedClass = null;

	public MetracerClassVisitor(ClassVisitor theClassVisitor, Pattern theClassMatchingPattern, Pattern theMethodMatchingPattern, ClassNode theParsedClass) {
		super(Opcodes.ASM5, theClassVisitor);
		classMatchingPattern = theClassMatchingPattern;
		methodMatchingPattern = theMethodMatchingPattern;
		parsedClass = theParsedClass;
	}

	@Override
	public void visit(int theVersion, int theAccess, String theClassName, String theSignature, String theSuperClassName, String[] theInterfaces) {
		cv.visit(theVersion, theAccess, theClassName, theSignature, theSuperClassName, theInterfaces);
		className = theClassName;
	}

	@Override
	public FieldVisitor visitField(int theAccess, String theName, String theDescription, String theSignature, Object theValue) {
		FieldVisitor rv = super.visitField(theAccess, theName, theDescription, theSignature, theValue);

		if(!hasSlf4Logger)
			hasSlf4Logger = theDescription.equals("Lorg/slf4j/Logger;");

		return rv;
	}

	@Override
	public MethodVisitor visitMethod(
		int theAccess, String theName, String theDescription,
		String theSignature, String[] theExceptions) {
		MethodVisitor methodVisitor = cv.visitMethod(theAccess, theName, theDescription, theSignature, theExceptions);
		String classNameWithDots = className.replace("/", ".");

		if(classMatchingPattern == null || !com.develorium.metracer.Runtime.isClassPatternMatched(classNameWithDots, classMatchingPattern))
			return methodVisitor;
		else if(methodMatchingPattern != null && !com.develorium.metracer.Runtime.isMethodPatternMatched(classNameWithDots, theName, methodMatchingPattern)) 
			return methodVisitor;

		com.develorium.metracer.Runtime.say(String.format("Instrumenting %s::%s", classNameWithDots, theName));

		List<MethodNode> methods = parsedClass.methods;
		MethodNode method = null;
				
		for(MethodNode m : methods) {
			if(m.name.equals(theName) && m.desc.equals(theDescription)) {
				method = m;
				break;
			}
		}
				
		methodVisitor = new PatternMatchedMethodMutator(className, method, api, methodVisitor, theAccess, theName, theDescription);
		isChanged = true;
		return methodVisitor;
	}

	public boolean getIsChanged() {
		return isChanged;
	}

	public boolean getHasSlf4Logger() {
		return hasSlf4Logger;
	}
}
