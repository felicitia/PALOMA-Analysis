package empirical;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import soot.jimple.AssignStmt;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.ReturnStmt;
import soot.jimple.ReturnVoidStmt;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
import soot.options.Options;

public class RequestAnalyzer {

	private static int tmpCount = 0;
	private static String apkName; // args3
	private static String appFolder;// args4
	private static String pkgName;// args5
	private static String androidJar;// args6

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		apkName = args[3];
		appFolder = args[4];
		androidJar = args[5];
		pkgName = args[6];

		// prefer Android APK files// -src-prec apk
		Options.v().set_src_prec(Options.src_prec_apk);
		// output as APK, too//-f J
		Options.v().set_output_format(Options.output_format_dex);
		Options.v().set_output_dir(appFolder + "/NewApp"); //folder that contains the new apk (your output)
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

		PackManager.v().getPack("jtp")
				.add(new Transform("jtp.myInstrumenter", new BodyTransformer() {

					@Override
					protected void internalTransform(final Body body,
							String phaseName,
							@SuppressWarnings("rawtypes") Map options) {
						
							printURLConnectionInfoOnSig(body, InstrumentationHelper.getInputStreamOriginal);
					
							body.validate();
					
					
					}

				}));

		String[] sootArgs = { args[0], args[1], args[2] };
		soot.Main.main(sootArgs);
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

}
