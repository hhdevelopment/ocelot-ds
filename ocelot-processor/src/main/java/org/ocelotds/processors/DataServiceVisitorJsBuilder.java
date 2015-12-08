/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. 
 */
package org.ocelotds.processors;

import org.ocelotds.annotations.JsCacheResult;
import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import org.ocelotds.KeyMaker;

/**
 * Visitor of class annoted org.ocelotds.annotations.DataService<br>
 * Generate javascript classes
 *
 * @author hhfrancois
 */
public class DataServiceVisitorJsBuilder extends AbstractDataServiceVisitor {

	protected final KeyMaker keyMaker;

	/**
	 *
	 * @param environment
	 */
	public DataServiceVisitorJsBuilder(ProcessingEnvironment environment) {
		super(environment);
		this.keyMaker = new KeyMaker();
	}

	@Override
	void _visitType(TypeElement typeElement, Writer writer) throws IOException {
		createClassComment(typeElement, writer);
		String jsclsname = getJsClassname(typeElement);
		String instanceName = getJsInstancename(jsclsname);
		writer.append("var ").append(instanceName).append(" = (function () {").append(CR);
		String classname = typeElement.getQualifiedName().toString();
		writer.append(TAB).append("var ds = ").append(QUOTE).append(classname).append(QUOTE).append(";").append(CR);
		writer.append(TAB).append("return {").append(CR);
		browseAndWriteMethods(ElementFilter.methodsIn(typeElement.getEnclosedElements()), classname, writer);
		writer.append(CR).append(TAB).append("};");
		writer.append(CR).append("})();").append(CR);
		writer.append("console.info(").append(QUOTE).append("Ocelotds create Service instance : ").append(instanceName).append(QUOTE).append(");").append(CR);
	}

	/**
	 *
	 * @param first
	 * @param classname
	 * @param methodElement
	 * @param writer
	 * @throws IOException
	 */
	@Override
	void visitMethodElement(int first, String classname, ExecutableElement methodElement, Writer writer) throws IOException {
		if (first != 0) { // previous method exist
			writer.append(",").append(CR);
		}
		String methodName = methodElement.getSimpleName().toString();
		List<String> argumentsType = getArgumentsType(methodElement);
		List<String> arguments = getArguments(methodElement);
		TypeMirror returnType = methodElement.getReturnType();
		createMethodComment(methodElement, arguments, argumentsType, returnType, writer);

		writer.append(TAB2).append(methodName).append(" : function (");
		int i = 0;
		while (i < argumentsType.size()) {
			writer.append((String) arguments.get(i));
			if ((++i) < arguments.size()) {
				writer.append(", ");
			}
		}
		writer.append(") {").append(CR);

		createMethodBody(classname, methodElement, arguments.iterator(), writer);

		writer.append(TAB2).append("}");
	}

	/**
	 * Create comment of the class
	 *
	 * @param typeElement
	 * @param writer
	 * @throws IOException
	 */
	void createClassComment(TypeElement typeElement, Writer writer) throws IOException {
		String comment = getElementUtils().getDocComment(typeElement);
		if (comment == null) {
			List<? extends TypeMirror> interfaces = typeElement.getInterfaces();
			for (TypeMirror typeMirror : interfaces) {
				TypeElement element = (TypeElement) getTypeUtils().asElement(typeMirror);
				comment = getElementUtils().getDocComment(element);
				if (comment != null) {
					writer.append("/**").append(CR).append(" *").append(computeComment(comment, " ")).append("/").append(CR);
				}
			}
		} else {
			writer.append("/**").append(CR).append(" *").append(computeComment(comment, " ")).append("/").append(CR);
		}
	}

	/**
	 * Create javascript comment from Method comment
	 *
	 * @param methodElement
	 * @param argumentsName
	 * @param argumentsType
	 * @param returnType
	 * @param writer
	 * @throws IOException
	 */
	void createMethodComment(ExecutableElement methodElement, List<String> argumentsName, List<String> argumentsType, TypeMirror returnType, Writer writer) throws IOException {
		String methodComment = getElementUtils().getDocComment(methodElement);
		writer.append(TAB2).append("/**").append(CR);
		// The javadoc comment
		if (methodComment != null) {
			methodComment = methodComment.split("@")[0];
			int lastIndexOf = methodComment.lastIndexOf(CR);
			if (lastIndexOf >= 0) {
				methodComment = methodComment.substring(0, lastIndexOf); // include the \n
			}
			writer.append(TAB2).append(" *").append(computeComment(methodComment, TAB2 + " ")).append(CR);
		}
		// La liste des arguments de la javadoc
		Iterator<String> typeIterator = argumentsType.iterator();
		for (String argumentName : argumentsName) {
			String type = typeIterator.next();
			writer.append(TAB2).append(" * @param {").append(type).append("} ").append(argumentName).append(CR);
		}
		// Si la methode retourne ou non quelque chose
		if (!returnType.toString().equals("void")) {
			writer.append(TAB2).append(" * @return {").append(returnType.toString()).append("}").append(CR);
		}
		writer.append(TAB2).append(" */").append(CR);
	}

	/**
	 * Create javascript method body
	 *
	 * @param classname
	 * @param methodElement
	 * @param arguments
	 * @param writer
	 * @throws IOException
	 */
	void createMethodBody(String classname, ExecutableElement methodElement, Iterator<String> arguments, Writer writer) throws IOException {
		String methodName = methodElement.getSimpleName().toString();
		StringBuilder args = getStringBuilder();
		StringBuilder paramNames = getStringBuilder();
		StringBuilder keys = getStringBuilder();
		if (arguments != null && arguments.hasNext()) {
			JsCacheResult jcr = methodElement.getAnnotation(JsCacheResult.class);
			boolean allArgs = true;
			// if there is a jcr annotation with value diferrent of *, so we dont use all arguments
			if (null != jcr && null != jcr.keys() && (jcr.keys().length == 0 || (jcr.keys().length > 0 && !"*".equals(jcr.keys()[0])))) {
				allArgs = false;
				for (int i = 0; i < jcr.keys().length; i++) {
					String arg = jcr.keys()[i];
					keys.append(getKeyFromArg(arg));
					if (i < jcr.keys().length - 1) {
						keys.append(",");
					}
				}
			}
			while (arguments.hasNext()) {
				String arg = arguments.next();
				if (allArgs) {
					keys.append(arg);
				}
				args.append(arg);
				paramNames.append("\"").append(arg).append("\"");
				if (arguments.hasNext()) {
					args.append(",");
					paramNames.append(",");
					if (allArgs) {
						keys.append(",");
					}
				}
			}
		}
		String md5 = keyMaker.getMd5(classname + "." + methodName);
		writer.append(TAB3).append("var id = ").append(QUOTE).append(md5).append("_").append(QUOTE).append(" + JSON.stringify([").append(keys.toString()).append("]).md5();").append(CR);
		writer.append(TAB3).append("return OcelotPromiseFactory.createPromise(ds, id, ").append(QUOTE).append(methodName).append(QUOTE).append(", [").append(paramNames.toString()).append("], [").append(args.toString()).append("]").append(");").append(CR);
	}

	/**
	 * Transform arg to valid key. protect js NPE<br>
	 * considers if arg or subfield is null<br>
	 * example : if arg == c return c<br>
	 * if arg == c.user return (c)?c.user:null<br>
	 * if arg == c.user.u_id return (c&&c.user)?c.user.u_id:null<br>
	 *
	 * @param arg
	 * @return
	 */
	String getKeyFromArg(String arg) {
		String[] objs = arg.split("\\.");
		StringBuilder result = getStringBuilder();
		if (objs.length > 1) {
			StringBuilder obj = getStringBuilder();
			obj.append(objs[0]);
			result.append("(").append(obj);
			for (int i = 1; i < objs.length - 1; i++) {
				result.append("&&");
				obj.append(".").append(objs[i]);
				result.append(obj);
			}
			result.append(")?").append(arg).append(":null");
		} else {
			result.append(arg);
		}
		return result.toString();
	}
}
