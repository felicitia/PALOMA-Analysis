package yixue.icse;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * stub methods for instrumentation, then Xposed will rewrite the actual method
 * 
 * @author felicitia
 *
 */
public class Proxy {

	/**
	 * nodes: the node ids that should be checked, 
	 * and prefetch if every value is known
	 * @param nodes
	 */
	public static void triggerPrefetch(String body, String sig, String nodes, int Prefetch_Method){
		System.out.println("new prefetch :)");
	}
	
	public static InputStream getInputStream(URLConnection urlconn) throws IOException {
		System.out.println("new getInputStream :)");
		return null;
	}
	
	public static int getResponseCode(HttpURLConnection urlConn) throws IOException{
		System.out.println("new getResponseCode :)");
		return -1;
	}

	public static Object getContent(URLConnection urlconn) throws IOException {
		System.out.println("new getContent :)");
		return null;
	}
	
	public static InputStream openStream(URL url) throws IOException{
		System.out.println("new openStream :)");
		return null;
	}
	
	public static String getURLfromJson(JSONObject obj){
		if(obj.has("dataURL")){
			try {
				return obj.getString("dataURL");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public static String getURLfromJsonParent(JSONObject obj){
		try {
			String itemID = obj.getString("loadScreenWithItemId");
			String url = "https://www.buzztouch.com/applications110313/JA5479375BD29396CCCA51C9D/documents/customHTML_"+itemID+".html";
			return url;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static String returnBigURL(String url){
		if(url.contains("http://www.hablema.com/greyhatmoders/appl/wallpaper/love/thumb/")){
			return url.replace("thumb/", "");
		}
		return null;
	}
	
	public static void sendDef(String body, String sig, String value, String nodeId, int index,
			String pkgName) {
		System.out.println("Proxy: sendDef :)");
		System.out.println("args = "+ value+"\t"+nodeId+"\t"+index+"\t"+pkgName);
	}

	public static String getResult(String urlStr) {
		System.out.println("Proxy: getResult :)");
		return null;
	}

	public static void printTimeStamp(String sig){
		Long time = System.currentTimeMillis(); 
		System.out.println("Yixue: timestamp = "+time.toString());
	}
	
	public static void printTimeDiff(String body, String sig, long timeDiff){
		System.out.println(body+"###"+sig+"###"+timeDiff);
	}
	public static void printUrl(String body, String sig, String value){
		System.out.println(body+"###"+sig+"###"+value);
	}
	public static long getTimeStamp(){
		return System.currentTimeMillis();
	}
	public static void printResponseCode(String body, String sig, int responseCode){
		System.out.println(body+"###"+sig+"###"+responseCode);
	}
}
