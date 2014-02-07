/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package to.netparty.getprops;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

/**
 *
 * @author antonio
 */
public class PostersApp {
    public static void main( String[] args )
    {
        CrawlTweets ct = new CrawlTweets(
                "posterdone@gmail.com"
                , "ポスター貼付けツィート "
        );
        
        // get search items
        try {
            InputStream fis = new FileInputStream("getprops.txt");
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(fis, Charset.forName("UTF-8"))
            );
            
            String line;
            Integer i = 1;
            while ((line = br.readLine()) != null) {
                // Deal with the line
                ct.checkTweets("poster." + i.toString(), line);
                i ++;
            }

            br.close();
        } catch (FileNotFoundException e) {
            System.out.println("warning: getprops.txt not found: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("warning: getprops.txt read error: " + e.getMessage());
        }
    }
}
