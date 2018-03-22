package empirical;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import soot.Body;
import soot.BodyTransformer;
import soot.Local;
import soot.LongType;
import soot.PackManager;
import soot.PatchingChain;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Transform;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.InvokeExpr;
import soot.jimple.Jimple;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
import soot.options.Options;

public class RequestAnalyzer {

	private static int tmpCount = 0;
	private static String apkName; // args3
	private static String appFolder;// args4
	private static String pkgName;// args5
	private static String androidJar;// args6
	private static PrintWriter pw;
	private static List<String> sigSubstrings;

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		apkName = args[3];
		appFolder = args[4];
		androidJar = args[5];
		pkgName = args[6];
		try {
			pw = new PrintWriter("output.txt");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// prefer Android APK files// -src-prec apk
		Options.v().set_src_prec(Options.src_prec_apk);
		// output as APK, too//-f J
		Options.v().set_output_format(Options.output_format_dex);
		Options.v().set_output_dir(appFolder + "/NewApp"); // folder that
															// contains the new
															// apk (your output)
		Options.v().set_android_jars(androidJar);
		Options.v().set_whole_program(true);
		Options.v().set_verbose(false);
		Options.v().set_allow_phantom_refs(true);

		// sootClassPath = Scene.v().getSootClassPath() + File.pathSeparator
		// + sootClassPath;
		// Scene.v().setSootClassPath(Scene.v().getSootClassPath());
		Options.v().set_keep_line_number(true);
		Options.v().setPhaseOption("jb", "use-original-names:true");
		// resolve the PrintStream and System soot-classes

		// System.out.println("------------------------java.class.path = "+System.getProperty("java.class.path"));
		Options.v().set_soot_classpath(System.getProperty("java.class.path"));
		Options.v().set_prepend_classpath(true);

		List<String> stringlist = new LinkedList<String>();
		stringlist.add(args[2]);
		Options.v().set_process_dir(stringlist);

		Scene.v().addBasicClass("java.io.PrintStream", SootClass.SIGNATURES);
		Scene.v().addBasicClass("java.lang.System", SootClass.SIGNATURES);

		Scene.v().addBasicClass(InstrumentationHelper.HelperClass);
		SootClass scc = Scene.v().loadClassAndSupport(
				InstrumentationHelper.HelperClass);
		scc.setApplicationClass();

//		for (SootClass sc : Scene.v().getClasses()) {
//			if (sc.getName().startsWith("android.support")) {
//				sc.setLibraryClass();
//			}
//		}

		PackManager.v().getPack("jtp")
				.add(new Transform("jtp.myInstrumenter", new BodyTransformer() {

					@Override
					protected void internalTransform(final Body body,
							String phaseName,
							@SuppressWarnings("rawtypes") Map options) {

//						 print URL info needed
						 printURLConnectionInfoOnSig(body,
						 InstrumentationHelper.getInputStreamOriginal);
						 instrumentURL(body, InstrumentationHelper.newURLOriginal);
						 printURLConnectionInfoOnSig(body,
						 InstrumentationHelper.getHttpInputStreamOriginal);
//						 print time difference of getInputStream() method
//						 instrumentTimestampOnSig(body,
//						 InstrumentationHelper.getInputStreamOriginal);
						 sigSubstrings = new ArrayList<String>();
						 sigSubstrings.add("<java.net.");
						 sigSubstrings.add("<okhttp3.");
						 instrumentTimestampOnSigContains(body, sigSubstrings);
						
						if(body.getMethod().getSignature().contains("ConnectInterceptor") || body.getMethod().getSignature().contains("CallServerInterceptor")){
							pw.println("############");
							pw.println(body.getMethod());
							pw.println("############");
						}
						 // print okhttp3 request info
						 instrumentOkHttpCallExecute(body,
						 InstrumentationHelper.okHttpExecute);
						 instrumentOkHttpCall(body,
						 InstrumentationHelper.okHttpInterceptor);
						//printOkHttpSigs(body);
						
						body.validate();

					}

				}));

		String[] sootArgs = { args[0], args[1], args[2] };
		soot.Main.main(sootArgs);
		pw.close();
	}

	// private static void instrumentVolleyCall(Body body, String sig) {
	// final PatchingChain<Unit> units = body.getUnits();
	// for (Iterator<Unit> iter = units.snapshotIterator(); iter.hasNext();) {
	// final Stmt stmt = (Stmt) iter.next();
	// if (stmt.containsInvokeExpr()) {
	// InvokeExpr invoke = stmt.getInvokeExpr();
	// if (invoke.getMethod().getSignature().equals(sig)) {
	//
	// }
	// }
	// }
	//
	//
	// }

	private static void printOkHttpSigs(Body body) {
		final PatchingChain<Unit> units = body.getUnits();
		for (Iterator<Unit> iter = units.snapshotIterator(); iter.hasNext();) {
			final Stmt stmt = (Stmt) iter.next();
			if (stmt.containsInvokeExpr()) {
				// System.out.println("invoke stmt = "+stmt);
				InvokeExpr invoke = stmt.getInvokeExpr();
				// if (invoke.getMethod().getSignature().equals(sig))
				if ((invoke.getMethod().getSignature().contains("OkHttp")
						|| invoke.getMethod().getSignature().contains("URL") || invoke
						.getMethod().getSignature().contains("Request"))) {
					pw.println("method: " + body.getMethod().getName());
					pw.println("stmt: " + stmt);
					pw.println();
				}
			}
		}
	}

	/**
	 * instruments info print out after okhttp3 request is made
	 * 
	 * @param body
	 * @param sig
	 */
	private static void instrumentOkHttpCall(Body body, String sig) {
		final PatchingChain<Unit> units = body.getUnits();
		for (Iterator<Unit> iter = units.snapshotIterator(); iter.hasNext();) {
			final Stmt stmt = (Stmt) iter.next();
			if (stmt.containsInvokeExpr()) {
				InvokeExpr invoke = stmt.getInvokeExpr();
				if (invoke.getMethod().getSignature().equals(sig)) {
					SootMethod printOkHttpRequest = InstrumentationHelper
							.findMethod(InstrumentationHelper.printOkHttpInfo);

					// add arguments to stmt
					LinkedList<Value> arglist = new LinkedList<Value>();

					arglist.add(StringConstant.v(body.getMethod()
							.getSignature()));
					arglist.add(StringConstant.v(stmt.toString()));

					arglist.add(invoke.getUseBoxes().get(0).getValue());
					arglist.add(stmt.getDefBoxes().get(0).getValue());

					Stmt printURLInvoke = Jimple.v().newInvokeStmt(
							Jimple.v().newStaticInvokeExpr(
									printOkHttpRequest.makeRef(), arglist));

					units.insertAfter(printURLInvoke, stmt);
				}

			}
		}
	}

	private static void instrumentOkHttpCallExecute(Body body, String sig) {
		final PatchingChain<Unit> units = body.getUnits();
		for (Iterator<Unit> iter = units.snapshotIterator(); iter.hasNext();) {
			final Stmt stmt = (Stmt) iter.next();
			if (stmt.containsInvokeExpr()) {
				InvokeExpr invoke = stmt.getInvokeExpr();
				if (invoke.getMethod().getSignature().equals(sig)) {
					SootMethod printOkHttpRequest = InstrumentationHelper
							.findMethod(InstrumentationHelper.printOkHttpInfoExecute);

					// add arguments to stmt
					LinkedList<Value> arglist = new LinkedList<Value>();

					arglist.add(StringConstant.v(body.getMethod()
							.getSignature()));
					arglist.add(StringConstant.v(stmt.toString()));
					System.out.println("HEREHERE ###################");
					System.out.println(stmt.toString());
					System.out.println(stmt.getDefBoxes().get(0).getValue().toString());
					for (ValueBox vb : invoke.getUseBoxes()) {
						System.out.println(vb.getValue().toString());
					}

					arglist.add(invoke.getUseBoxes().get(0).getValue());
					arglist.add(stmt.getDefBoxes().get(0).getValue());

					Stmt printURLInvoke = Jimple.v().newInvokeStmt(
							Jimple.v().newStaticInvokeExpr(
									printOkHttpRequest.makeRef(), arglist));

					units.insertAfter(printURLInvoke, stmt);
				}

			}
		}
	}

	private static void printURLConnectionInfoOnSig(Body body, String sig) {
		final PatchingChain<Unit> units = body.getUnits();
		for (Iterator<Unit> iter = units.snapshotIterator(); iter.hasNext();) {
			final Stmt stmt = (Stmt) iter.next();
			if (stmt.containsInvokeExpr()) {
				// System.out.println("invoke stmt = "+stmt);
				InvokeExpr invoke = stmt.getInvokeExpr();
				// if (invoke.getMethod().getSignature().equals(sig))
				if ((invoke.getMethod().getSignature().equals(sig))) {

					SootMethod printURLConnectionInfo = InstrumentationHelper
							.findMethod(InstrumentationHelper.printURLInfo);

					// add arguments to stmt
					LinkedList<Value> arglist = new LinkedList<Value>();

					arglist.add(StringConstant.v(body.getMethod()
							.getSignature()));
					arglist.add(StringConstant.v(stmt.toString()));

					arglist.add(invoke.getUseBoxes().get(0).getValue());

					// new printURL invoke stmt
					Stmt printURLInvoke = Jimple.v().newInvokeStmt(
							Jimple.v().newStaticInvokeExpr(
									printURLConnectionInfo.makeRef(), arglist));

					units.insertAfter(printURLInvoke, stmt);
				}
			}
		}
	}

	private static Local addTmpString2Local(Body body) {
		Local tmpString = Jimple.v().newLocal("tmpString" + (tmpCount++),
				RefType.v("java.lang.String"));
		body.getLocals().add(tmpString);
		return tmpString;
	}

	private static Local addTmpInt2Local(Body body) {
		Local tmpInt = Jimple.v().newLocal("tmpInt" + (tmpCount++),
				RefType.v("java.lang.Integer"));
		body.getLocals().add(tmpInt);
		return tmpInt;
	}

	private static Local addTmpLong2Local(Body body) {
		Local tmpLong = Jimple.v().newLocal("tmpLong" + (tmpCount++),
				LongType.v());
		body.getLocals().add(tmpLong);
		return tmpLong;
	}

	/**
	 * instrument timestamps before "sig" and after "sig"and call printTimeDiff
	 * 
	 * @param body
	 * @param sig
	 */
	private static void instrumentTimestampOnSig(Body body, String sig) {
		final PatchingChain<Unit> units = body.getUnits();
		for (Iterator<Unit> iter = units.snapshotIterator(); iter.hasNext();) {
			final Stmt stmt = (Stmt) iter.next();
			if (stmt.containsInvokeExpr()) {
				// System.out.println("invoke stmt = "+stmt);
				InvokeExpr invoke = stmt.getInvokeExpr();
				// if (invoke.getMethod().getSignature().equals(sig))
				if ((invoke.getMethod().getSignature().equals(sig))) {
					// timestampCounter++;

					// SootMethod printTimeStamp = ProxyHelper
					// .findMethod(ProxyHelper.printTimeStamp);
					SootMethod getTimeStamp = InstrumentationHelper
							.findMethod(InstrumentationHelper.getTimeStamp);
					SootMethod printTimeDiff = InstrumentationHelper
							.findMethod(InstrumentationHelper.printeTimeDiff);

					Stmt getTimeStampInvoke = Jimple.v().newInvokeStmt(
							Jimple.v().newStaticInvokeExpr(
									getTimeStamp.makeRef()));
					Local timeStamp1 = addTmpLong2Local(body);
					Stmt assignTimeStampBefore = Jimple.v().newAssignStmt(
							timeStamp1, getTimeStampInvoke.getInvokeExpr());
					Local timeStamp2 = addTmpLong2Local(body);
					Stmt assignTimeStampAfter = Jimple.v().newAssignStmt(
							timeStamp2, getTimeStampInvoke.getInvokeExpr());
					Local timeDiff = addTmpLong2Local(body);
					Stmt assignTimeDiff = Jimple.v().newAssignStmt(timeDiff,
							Jimple.v().newSubExpr(timeStamp2, timeStamp1));

					LinkedList<Value> arglist = new LinkedList<Value>();
					arglist.add(StringConstant.v(body.getMethod()
							.getSignature()));
					arglist.add(StringConstant.v(stmt.toString()));
					arglist.add(timeDiff);
					Stmt printTimeDiffInvoke = Jimple.v().newInvokeStmt(
							Jimple.v().newStaticInvokeExpr(
									printTimeDiff.makeRef(), arglist));

					units.insertBefore(assignTimeStampBefore, stmt);
					units.insertAfter(printTimeDiffInvoke, stmt);
					units.insertAfter(assignTimeDiff, stmt);
					units.insertAfter(assignTimeStampAfter, stmt);
				}
			}
		}
	}

	/**
	 * instrument timestamps before "sig" and after "sig"and call printTimeDiff,
	 * as long as "sig" contains the substrings passed to this method
	 * 
	 * @param body
	 * @param sig
	 */
	private static void instrumentTimestampOnSigContains(Body body, List<String> sigSubstrings) {
		final PatchingChain<Unit> units = body.getUnits();
		for (Iterator<Unit> iter = units.snapshotIterator(); iter.hasNext();) {
			final Stmt stmt = (Stmt) iter.next();
			if (stmt.containsInvokeExpr()) {
				// System.out.println("invoke stmt = "+stmt);
				InvokeExpr invoke = stmt.getInvokeExpr();
					if(isContainsSigSubstring(invoke.getMethod().getSignature(), sigSubstrings)){
						// timestampCounter++;
						// SootMethod printTimeStamp = ProxyHelper
						// .findMethod(ProxyHelper.printTimeStamp);
						SootMethod getTimeStamp = InstrumentationHelper
								.findMethod(InstrumentationHelper.getTimeStamp);
						SootMethod printTimeDiff = InstrumentationHelper
								.findMethod(InstrumentationHelper.printeTimeDiff);

						Stmt getTimeStampInvoke = Jimple.v().newInvokeStmt(
								Jimple.v().newStaticInvokeExpr(
										getTimeStamp.makeRef()));
						Local timeStamp1 = addTmpLong2Local(body);
						Stmt assignTimeStampBefore = Jimple.v().newAssignStmt(
								timeStamp1, getTimeStampInvoke.getInvokeExpr());
						Local timeStamp2 = addTmpLong2Local(body);
						Stmt assignTimeStampAfter = Jimple.v().newAssignStmt(
								timeStamp2, getTimeStampInvoke.getInvokeExpr());
						Local timeDiff = addTmpLong2Local(body);
						Stmt assignTimeDiff = Jimple.v().newAssignStmt(timeDiff,
								Jimple.v().newSubExpr(timeStamp2, timeStamp1));

						LinkedList<Value> arglist = new LinkedList<Value>();
						arglist.add(StringConstant.v(body.getMethod()
								.getSignature()));
						arglist.add(StringConstant.v(stmt.toString()));
						arglist.add(timeDiff);
						Stmt printTimeDiffInvoke = Jimple.v().newInvokeStmt(
								Jimple.v().newStaticInvokeExpr(
										printTimeDiff.makeRef(), arglist));

						units.insertBefore(assignTimeStampBefore, stmt);
						units.insertAfter(printTimeDiffInvoke, stmt);
						units.insertAfter(assignTimeDiff, stmt);
						units.insertAfter(assignTimeStampAfter, stmt);
					}
			}
		}
	}
	
	private static boolean isContainsSigSubstring(String sig, List<String> sigSubstrings){
		for(String substring: sigSubstrings){
			if(sig.contains(substring)){
				return true;
			}
		}
		return false;
	}
	/***
	 * instrument after sig, to print out the string value.
	 * 
	 * @param body
	 * @param sig
	 *            is new URL(string)
	 */
	private static void instrumentURL(Body body, String sig) {
		final PatchingChain<Unit> units = body.getUnits();
		for (Iterator<Unit> iter = units.snapshotIterator(); iter.hasNext();) {
			final Stmt stmt = (Stmt) iter.next();
			if (stmt.containsInvokeExpr()) {
				// System.out.println("invoke stmt = "+stmt);
				InvokeExpr invoke = stmt.getInvokeExpr();
				if (invoke.getMethod().getSignature().equals(sig)) {

					SootMethod printUrl = InstrumentationHelper
							.findMethod(InstrumentationHelper.printUrl);

					LinkedList<Value> arglist = new LinkedList<Value>();
					arglist.add(StringConstant.v(body.getMethod()
							.getSignature()));
					arglist.add(StringConstant.v(stmt.toString()));
					arglist.add(invoke.getArg(0));

					Stmt printUrlInvoke = Jimple.v().newInvokeStmt(
							Jimple.v().newStaticInvokeExpr(printUrl.makeRef(),
									arglist));

					units.insertAfter(printUrlInvoke, stmt);
					
				}
			}
		}
	}
}