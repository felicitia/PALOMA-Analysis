package empirical;

import java.net.URLConnection;

import okhttp3.Headers;
import okhttp3.Request;

public class HelperClass {
    static int reqID = 0;
//    static final String urlTag = "URLInfo";

    public static void printURLInfo(String body, String stmt, URLConnection urlConnection){
        String urlString = urlConnection.getURL().toString();    //check the URL string
        boolean isDoOutput = urlConnection.getDoOutput();   //check whether it's GET or POST
    	
    	//header parameters
        String length = getHeaderField(urlConnection, "Content-Length");
    	String cacheControl = getHeaderField(urlConnection, "Cache-Control");
    	String expires = getHeaderField(urlConnection, "Expires");
    	String age = getHeaderField(urlConnection, "Age");
    	String setCookie = getHeaderField(urlConnection, "Set-Cookie");
    	
    	System.out.println("URLConn;%;" + reqID++ + ";%;" + body + ";%;" + stmt + ";%;" 
    			+ urlString + ";%;" 
    			+ isDoOutput + ";%;"
    			+ length + ";%;"
    			+ cacheControl + ";%;"
    			+ expires + ";%;"
    			+ age + ";%;"
    			+ setCookie);
    }
    
//    public static void printVolleyInfo(String body, String stmt, com.android.volley.Request req) {
//    	
//    }
    
    public static void printOkHttpInfo(String body, String stmt, okhttp3.Request req, okhttp3.Response resp){
    	System.out.print("OkHttpP;%;" + reqID++ + ";%;" + body + ";%;" + stmt + ";%;" + 
    req.url().toString() + ";%;");
    	Headers responseHeaders = resp.headers();
    	System.out.print(responseHeaders.values("Content-Length") + ";%;");
    	System.out.print(responseHeaders.values("Cache-Control") + ";%;");
    	System.out.print(responseHeaders.values("Expires") + ";%;");
    	System.out.print(responseHeaders.values("Age") + ";%;");
    	System.out.println(responseHeaders.values("Set-Cookie") + ";%;");
    	System.out.println("header:" + responseHeaders.toString());
    }

    public static void printOkHttpInfoFromCall(String body, String stmt, okhttp3.Call c, okhttp3.Response resp){
    	Request req = c.request();
    	System.out.print("OkHttpE;%;" + reqID++ + ";%;" + body + ";%;" + stmt + ";%;" + 
    req.url().toString() + ";%;");   	
    	Headers responseHeaders = resp.headers();
        for (int i = 0; i < responseHeaders.size(); i++) {
            System.out.print(responseHeaders.name(i) + ": " + responseHeaders.value(i) + ";%;");
        }
        System.out.println();
    }
    
	public static void printUrl(String body, String sig, String value){
		System.out.println(body+"###"+sig+"###"+value);
	}
    
    private static String getHeaderField(URLConnection conn, String field) {
    	String data = conn.getHeaderField(field);
    	return (data != null) ? data : "Not found";
    }
    
	public static long getTimeStamp(){
		return System.currentTimeMillis();
	}
	
	public static void printTimeDiff(String body, String sig, long timeDiff){
		System.out.println("body:"+body+"\tsig:"+sig+"\ttimediff:"+timeDiff);
	}
}
