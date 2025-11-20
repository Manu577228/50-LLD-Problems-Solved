/* ----------------------------------------------------------------------------  */
/*   ( The Authentic JS/JAVA CodeBuff )
 ___ _                      _              _ 
 | _ ) |_  __ _ _ _ __ _ __| |_ __ ____ _ (_)
 | _ \ ' \/ _` | '_/ _` / _` \ V  V / _` || |
 |___/_||_\__,_|_| \__,_\__,_|\_/\_/\__,_|/ |
                                        |__/ 
 */
/* --------------------------------------------------------------------------   */
/*    Youtube: https://youtube.com/@code-with-Bharadwaj                        */
/*    Github : https://github.com/Manu577228                                  */
/*    Portfolio : https://manu-bharadwaj-portfolio.vercel.app/portfolio      */
/* -----------------------------------------------------------------------  */

import java.util.HashMap;

public class URLShortener {
    private HashMap<String, String> urlDb;
    private HashMap<String, String> reverseDb;
    private long counter;
    private final String alphabet;

    public URLShortener() {
        urlDb = new HashMap<>();
        reverseDb = new HashMap<>();
        counter = 1;
        alphabet = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    }

    private String encode(long num) {
        if (num == 0) return "" + alphabet.charAt(0);
        StringBuilder sb = new StringBuilder();
        int base = alphabet.length();
        while (num > 0) {
            sb.append(alphabet.charAt((int)(num % base)));
            num /= base;
        }
        return sb.reverse().toString();
    }

    public String shorten(String longUrl) {
        if (reverseDb.containsKey(longUrl)) {
            return reverseDb.get(longUrl);
        }
        String shortUrl = encode(counter);
        urlDb.put(shortUrl, longUrl);
        reverseDb.put(longUrl, shortUrl);
        counter++;
        return shortUrl;
    }

    public String retrieve(String shortUrl) {
        return urlDb.getOrDefault(shortUrl, null);
    }

    // -------------------------------
    // Demo
    // -------------------------------
    public static void main(String[] args) {
        URLShortener shortener = new URLShortener();

        String url1 = "https://www.youtube.com/@code-with-Bharadwaj";
        String url2 = "https://github.com/Manu577228";

        String s1 = shortener.shorten(url1);
        String s2 = shortener.shorten(url2);

        System.out.println("Short URL for YouTube: " + s1);
        System.out.println("Short URL for GitHub  : " + s2);
        System.out.println("Retrieve YouTube URL  : " + shortener.retrieve(s1));
        System.out.println("Retrieve GitHub URL   : " + shortener.retrieve(s2));
    }
}
