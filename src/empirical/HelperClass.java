package empirical;

import java.net.URLConnection;



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
    	String setCookie = getHeaderField(urlConnection, "Cache-Control");
    	
    	System.out.println(reqID++ + ";" + body + ";" + stmt + ";" 
    			+ urlString + ";" 
    			+ isDoOutput + ";"
    			+ length + ";"
    			+ cacheControl + ";"
    			+ expires + ";"
    			+ age + ";"
    			+ setCookie);
    }

    private static String getHeaderField(URLConnection conn, String field) {
    	String data = conn.getHeaderField(field);
    	return (data != null) ? data : "Not found";
    }
}
