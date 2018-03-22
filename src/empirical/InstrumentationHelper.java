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
	final static String getHttpInputStreamOriginal = "<java.net.HttpURLConnection: java.io.InputStream getInputStream()>";	
	final static String newURLOriginal = "<java.net.URL: void <init>(java.lang.String)>";
	final static String okHttpInterceptor = "<okhttp3.Interceptor$Chain: okhttp3.Response proceed(okhttp3.Request)>";
	final static String okHttpExecute = "<okhttp3.Call: okhttp3.Response execute()>";
	final static String volleyAddRequest = "<com.android.volley.RequestQueue: com.android.volley.Request add(com.android.volley.Request)>";
	//	HelperClass Signatures
	final static String printURLInfo = "void printURLInfo(java.lang.String,java.lang.String,java.net.URLConnection)";
	final static String printeTimeDiff = "void printTimeDiff(java.lang.String,java.lang.String,long)";
	final static String getTimeStamp = "long getTimeStamp()";
	final static String printOkHttpInfo = "void printOkHttpInfo(java.lang.String,java.lang.String,okhttp3.Request,okhttp3.Response)";
	final static String printOkHttpInfoExecute = "void printOkHttpInfoFromCall(java.lang.String,java.lang.String,okhttp3.Call,okhttp3.Response)"; 
	final static String volleyHttpInfo = "void printVolleyInfo(java.lang.String, java.lang.String, com.android.volley.Request)";
	final static String printUrl = "void printUrl(java.lang.String,java.lang.String,java.lang.String)";
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
		ProxyClass.getMethods().stream().forEach(e -> System.out.println(e.getSignature()));
		SootMethod helpMethod = ProxyClass.getMethod(methodName);
		System.out.println("===========helpMethod = "
				+ helpMethod.getSignature());
		return helpMethod;
	}
}
