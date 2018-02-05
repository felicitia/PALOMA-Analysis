package empirical;

import java.net.URLConnection;



public class HelperClass {
    static int reqID = 0;
//    static final String urlTag = "URLInfo";

    public static void printURLInfo(String body, String stmt, URLConnection urlConnection){
        String urlString = urlConnection.getURL().toString();    //check the URL string
        boolean isDoOutput = urlConnection.getDoOutput();   //check whether it's GET or POST
        System.out.println(reqID++ + "," + body + "," + stmt + "," + urlString + "," + isDoOutput);
    }

    
    
}
