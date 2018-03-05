package ccfg;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import soot.Body;
import soot.BodyTransformer;
import soot.PackManager;
import soot.PatchingChain;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Transform;
import soot.Unit;
import soot.Value;
import soot.jimple.Jimple;
import soot.jimple.ReturnStmt;
import soot.jimple.ReturnVoidStmt;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
import soot.options.Options;

public class CallbackInstrumenter {

	private static final String callbackInput = "/Users/felicitia/Documents/Research/PALOMA/Develop/GATOR/gator-3.3/SootAndroid/listeners.xml";
	private static Set<String> handlers = null;
	private static Set<String> lifecycles = null;
	private static String apkName; // args3
	private static String appFolder;// args4
	private static String pkgName;// args5
	private static String androidJar;// args6

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		initHandlerList();
		instrumentCallbacks(args);
	}

	public static void instrumentCallbacks(String[] args) {
		apkName = args[3];
		appFolder = args[4];
		androidJar = args[5];
		pkgName = args[6];

		// prefer Android APK files// -src-prec apk
		Options.v().set_src_prec(Options.src_prec_apk);
		// output as APK, too//-f J
		Options.v().set_output_format(Options.output_format_dex);
		Options.v().set_output_dir(appFolder + "/NewApp(callback)"); // folder
																		// that
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

		// for (SootClass sc : Scene.v().getClasses()) {
		// if (sc.getName().startsWith("android.support")) {
		// sc.setLibraryClass();
		// }
		// }

		PackManager.v().getPack("jtp")
				.add(new Transform("jtp.myInstrumenter", new BodyTransformer() {

					@Override
					protected void internalTransform(final Body body,
							String phaseName,
							@SuppressWarnings("rawtypes") Map options) {
						String callbackType = getApplicationCallbackType(body.getMethod().getSignature());
						if ( callbackType != null) {
							logCallback(body, callbackType);
						}

						body.validate();

					}

				}));

		String[] sootArgs = { args[0], args[1], args[2] };
		soot.Main.main(sootArgs);
	}

	public static void logCallback(Body body, String callbackType) {
		final PatchingChain<Unit> units = body.getUnits();
		for (Iterator<Unit> iter = units.snapshotIterator(); iter.hasNext();) {
			final Stmt stmt = (Stmt) iter.next();
			if (stmt instanceof ReturnStmt || stmt instanceof ReturnVoidStmt) {
				SootMethod logCallbackMethod = InstrumentationHelper
						.findMethod(InstrumentationHelper.logCallback);

				LinkedList<Value> arglist = new LinkedList<Value>();
				arglist.add(StringConstant.v(body.getMethod().getSignature()));
				arglist.add(StringConstant.v(callbackType));

				Stmt logCallbackInvoke = Jimple.v().newInvokeStmt(
						Jimple.v().newStaticInvokeExpr(
								logCallbackMethod.makeRef(), arglist));

				units.insertBefore(logCallbackInvoke, stmt);
			}
		}

	}
	
	/**
	 * return null if not application callback,
	 * if lifecycle callback, return "lifecycle",
	 * if event handler, return "event"
	 * @param bodySig
	 * @return
	 */
	public static String getApplicationCallbackType(String bodySig) {
		if(bodySig.contains("android.support")){
			return null;
		}
		for(String callback: lifecycles){
			if(bodySig.contains(callback)){
				return "lifecycle";
			}
		}
		for (String callback : handlers) {
			if (bodySig.contains(callback)) {
				return "event";
			}
		}
		return null;
	}

	public static void initHandlerList() {
		File xmlInput = new File(callbackInput);
		handlers = new HashSet<String>();
		lifecycles = new HashSet<String>();

		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;
		Document doc = null;
		try {
			dBuilder = dbFactory.newDocumentBuilder();
			doc = dBuilder.parse(xmlInput);
			doc.getDocumentElement().normalize();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		NodeList nList = doc.getElementsByTagName("handler");
		for (int i = 0; i < nList.getLength(); i++) {
			Node node = nList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element element = (Element) node;
				String subsig = element.getAttribute("subsig");
				handlers.add(subsig);
			}
		}
		nList = doc.getElementsByTagName("lifecycle");
		for (int i = 0; i < nList.getLength(); i++) {
			Node node = nList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element element = (Element) node;
				String callback = element.getTextContent();
				lifecycles.add(callback);
			}
		}
	}
}
