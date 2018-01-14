package yixue.icse;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import soot.Body;
import soot.PatchingChain;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.ValueBox;
import soot.jimple.Stmt;
import soot.options.Options;

public class DefFinder {
	static List<DefSpot> defSpotList = null;
	static List<TargetField> targetFieldList = null;
	static List<DefSpot> manualDefSpotList = null;

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println(getDefSpotListFromTargetField("/Users/felicitia/Documents/Research/Prefetch/ICSE2018/MBMRoot/MBMApp", "app-release.apk", "/Users/felicitia/Documents/Research/Prefetch/Develop/AndroidJar", "edu.usc.mbm"));
	}

	public static List<DefSpot> createDefSpots(TargetField field){
		List<DefSpot> defSpotsOfField = new ArrayList<DefSpot>();
		SootClass sootClass = Scene.v().loadClassAndSupport(field.classBelonged);
		Scene.v().loadNecessaryClasses();
//		SootField  field = sootClass.getFieldByName("favCityId");
		for(SootMethod method: sootClass.getMethods()){
			try{
				Body body = method.retrieveActiveBody();
				final PatchingChain<Unit> units = body.getUnits();
				for (Iterator<Unit> iter = units.snapshotIterator(); iter.hasNext();) {
					final Stmt stmt = (Stmt) iter.next();
//					System.out.println("stmt = "+stmt);
					for(ValueBox defBox: stmt.getDefBoxes()){
//					System.out.println(defBox.getValue().getClass().getName());
						if(defBox.getValue().getClass().getName().contains("Field") ){
							// ">" is to make sure it's not the other fields that start with the same name
							// example of defBox.getValue().toString is:  $r0.<edu.usc.yixue.weatherapp.MainActivity: java.lang.String cityName>
							if(defBox.getValue().toString().contains(field.fieldName+">")){
								if(!stmt.toString().endsWith(" = null")){
									DefSpot defSpot = new DefSpot();
									defSpot.jimple = stmt.toString();
									defSpot.body = method.getSignature();
									defSpot.nodeId = field.nodeId;
									defSpot.pkgName = field.pkgName;
									defSpot.subStrPos = field.substrPos;
									defSpotsOfField.add(defSpot);
								}
							}
						}
					}
				}
			}catch(Exception e){
				e.printStackTrace();
				continue;
			}
		}
		return defSpotsOfField;
	}
	
	// read from targetField.csv
	public static List<TargetField> getTargetFieldList(String appFolder, String pkgName){
		if(targetFieldList == null){
			targetFieldList = new ArrayList<TargetField>();
			String csvFilePath = appFolder+"/Input/targetField.csv";
			try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
				String sCurrentLine;
				br.readLine(); //skip the header
				while ((sCurrentLine = br.readLine()) != null) {
					System.out.println(sCurrentLine);
					String[] values = sCurrentLine.split(",");
					TargetField targetFiled = new TargetField();
					targetFiled.classBelonged = values[0];
					targetFiled.fieldName = values[1];
					targetFiled.nodeId = values[2];
					targetFiled.pkgName = pkgName;
					targetFiled.substrPos = Integer.parseInt(values[3]);
					targetFieldList.add(targetFiled);
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return targetFieldList;
	}

	public static void sootSettingAndroid(String apkPath, String androidJar){
		// prefer Android APK files// -src-prec apk
				Options.v().set_src_prec(Options.src_prec_apk);
				// output as APK, too//-f J
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
				stringlist.add(apkPath);
				Options.v().set_process_dir(stringlist);
	}
	
	public static List<DefSpot> getDefSpotListFromTargetField(String appFolder, String apkName, String androidJar, String pkgName){
		if(defSpotList == null){
			defSpotList = new ArrayList<DefSpot>();
			sootSettingAndroid(appFolder+"/"+apkName, androidJar);
			 
			for(TargetField field: getTargetFieldList(appFolder, pkgName)){
				defSpotList.addAll(createDefSpots(field));
			}
			for(DefSpot defSpot: defSpotList){
				System.out.println("body = " + defSpot.body);
				System.out.println("jimple = " + defSpot.jimple);
				System.out.println("node Id = " + defSpot.nodeId);
				System.out.println("pkg name = " + defSpot.pkgName);
				System.out.println("substr pos = " + defSpot.subStrPos);
				System.out.println();
			}
		}
		return defSpotList;
	}
	
	public static List<DefSpot> getDefSpotListFromDefSpots(String appFolder, String pkgName){
		if(manualDefSpotList == null){
			manualDefSpotList = new ArrayList<DefSpot>();
			String csvFilePath = appFolder+"/Input/DefSpots.csv";
			try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
				String sCurrentLine;
				br.readLine(); //skip the header
				while ((sCurrentLine = br.readLine()) != null) {
					String[] values = sCurrentLine.split(",");
					DefSpot defSpot = new DefSpot();
					defSpot.jimple = values[0];
					defSpot.body = values[1];
					defSpot.nodeId = values[2];
					defSpot.subStrPos = Integer.parseInt(values[3]);
					defSpot.pkgName = values[4];
					manualDefSpotList.add(defSpot);
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return manualDefSpotList;
	}
}
