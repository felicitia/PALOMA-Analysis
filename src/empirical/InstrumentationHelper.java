package empirical;


import java.util.HashMap;
import java.util.HashSet;

import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import yixue.icse.InstrumentStmt;

public class InstrumentationHelper {

	// key is the body that needs to be instrumented
	public final static HashMap<String, HashSet<InstrumentStmt>> instrumentMap = new HashMap<String, HashSet<InstrumentStmt>>();

	public final static HashMap<String, String> jimpleReplaceMap = new HashMap<String, String>();

	final static String HelperClass = "empirical.HelperClass";

	final static String getInputStreamOriginal = "<java.net.URLConnection: java.io.InputStream getInputStream()>";
	final static String newURLOriginal = "<java.net.URL: void <init>(java.lang.String)>";
	
	//	HelperClass Signatures
	final static String printURLInfo = "void printURLInfo(java.lang.String,java.lang.String,java.net.URLConnection)";
	
	/**
	 * if the body is known, then query "instrumentMap"
	 * 
	 * @param original
	 * @return
	 */
	public static SootMethod queryReplaceMethodWithBody(String original) {
		for (String key : instrumentMap.keySet()) {
			for (InstrumentStmt stmt : instrumentMap.get(key)) {
				if (stmt.jimpleOriginal.equals(original)) {
					SootClass ProxyClass = Scene.v().loadClassAndSupport(
							InstrumentationHelper.HelperClass);
					SootMethod replaceMethod = ProxyClass
							.getMethod(stmt.jimpleReplacement);
					return replaceMethod;
				}
			}
		}
		return null;
	}

	/**
	 * if the body is not known, then query "jimpleReplaceMap", which will replace
	 * every method no matter what body it belongs
	 * 
	 * @param original
	 * @return
	 */
	public static SootMethod queryReplaceMethod(String original) {

		if (jimpleReplaceMap.containsKey(original)) {
			SootClass ProxyClass = Scene.v().loadClassAndSupport(
					InstrumentationHelper.HelperClass);
			SootMethod replaceMethod = ProxyClass
					.getMethod(jimpleReplaceMap.get(original));
			return replaceMethod;
		}

		return null;
	}

	public static SootMethod findMethod(String methodName) {
		SootClass ProxyClass = Scene.v().loadClassAndSupport(InstrumentationHelper.HelperClass);
		SootMethod helpMethod = ProxyClass.getMethod(methodName);
		System.out.println("===========helpMethod = "
				+ helpMethod.getSignature());
		return helpMethod;
	}
}
