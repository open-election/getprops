package to.netparty.getprops;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.User;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;

import javax.mail.Session;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class App 
{
    @SuppressWarnings("empty-statement")
    public static void main( String[] args )
    {
        checkTweets("fukushi", "#ぼくらの福祉政策");
        checkTweets("toshi", "#ぼくらの都市計画・成長戦略");
        checkTweets("gyosei", "#ぼくらの行政改革");
        checkTweets("bosai", "#ぼくらの防災・危機管理計画");
        checkTweets("2020", "#ぼくらの2020年計画");
        checkTweets("props", "#ぼくらの政策");
    }
    
    public static void checkTweets(String prop, String hashTag)
    {
        System.out.println("start: " + hashTag);
        
        long min_id = -1;
        long end_id = -1;
        long start_id = -1;
        
        // load state
        Properties props = new Properties();
        try {
            File f = new File("getprops.properties");
            InputStream is = new FileInputStream(f);
            props.load(is);

            min_id = Long.parseLong(props.getProperty(prop + ".min_id", "-1"));
            start_id = Long.parseLong(props.getProperty(prop + ".start_id", "-1"));
            end_id = Long.parseLong(props.getProperty(prop + ".end_id", "-1"));
        }
        catch (FileNotFoundException e) {
            System.out.println("warning: getprops.properties file not found: " + e.getMessage());
        }
        catch (IOException e) { 
            System.out.println("warning: getprops.properties error: " + e.getMessage());
        }
                
        // prepare output file        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");
        String FileName = prop + "." + sdf.format(new Date()) + ".csv";
        
        File csv = new File(FileName);
        int count = 0;
        try {                    
            BufferedWriter bw = new BufferedWriter(new FileWriter(csv, true));
            
            // read tweets
            try {
                // prepare twitter api
                Twitter twitter = new TwitterFactory().getInstance();
                Query query = new Query();

                // loop
                boolean first = true;
                for (int loop = 0; loop < 450; loop ++) {
                    System.out.println("info: loop " + ((Integer) loop).toString()
                            + " for " + hashTag
                    );
                    
                    // 検索条件
                    query.setQuery(hashTag);
                    query.setCount(100);
                    if (min_id > 0) {
                        query.maxId(min_id - 1);
                    }
                    if (end_id > 0) {
                        query.sinceId(end_id);
                    }

                    // 検索実行
                    QueryResult result = twitter.search(query);
                    List<Status> tweets = result.getTweets();

                    // check if we are at last page
                    if (tweets.isEmpty()) {
                        // if there is nothing to get exit
                        if (first) {
                            break;
                        }

                        // restart search
                        end_id = start_id;
                        start_id = -1;
                        min_id = -1;
                        first = true;
                        continue;
                    }
                    first = false;

                    // 検索結果を見てみる
                    for (Status tweet : tweets) {
                        // skip RTs
                        long id = tweet.getId();
                        if (!tweet.isRetweet()) {
                            String text = tweet.getText().replaceAll(",", "、");
                            User user = tweet.getUser();
                            String name = user.getName().replaceAll(",", "、");
                            String screen_name = user.getScreenName().replaceAll(",", "、");

                            bw.write(
                                ((Long) id).toString()
                                + "," + text
                                + "," + name
                                + "," + screen_name
                                + "," + tweet.getCreatedAt()
                                + "," + ((Integer) tweet.getRetweetCount()).toString()
                            );
                            bw.newLine(); 
                            count ++;
                        }

                        if (min_id < 0 || min_id > id) {
                            min_id = id;
                        }

                        if (start_id < 0) {
                            start_id = id;
                        }
                    }                
                }
            }
            catch (TwitterException e) {
                System.out.println("warning: twitter exception: " + e.getErrorMessage());
            }

            bw.close();
        }
        catch (IOException e) {
            System.out.println("warning: general I/O error: " + e.getMessage());
        }

        // save state
        try {
            props.setProperty(prop + ".min_id", ((Long) min_id).toString());
            props.setProperty(prop + ".start_id", ((Long) start_id).toString());
            props.setProperty(prop + ".end_id", ((Long) end_id).toString());

            File f = new File("getprops.properties");
            OutputStream out = new FileOutputStream( f );
            props.store(out, "");            
        }
        catch (IOException e) {
            System.out.println("warning: general I/O error: " + e.getMessage());
        }
        
        if (count == 0) {
            csv.delete();
            return;
        }
        
        // send email
        final String username = props.getProperty("mail_user", "user@some.where");
        final String password = props.getProperty("mail_pass", "password");

        props.put("mail.smtp.auth", true);
        props.put("mail.smtp.starttls.enable", true);
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props,
            new javax.mail.Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            }
        );

        // send tweets as attachment
        try {
            // prepare attachment
            Multipart multipart = new MimeMultipart();
            MimeBodyPart messageBodyPart = new MimeBodyPart();
            DataSource source = new FileDataSource(FileName);
            messageBodyPart.setDataHandler(new DataHandler(source));
            messageBodyPart.setFileName(FileName);
            multipart.addBodyPart(messageBodyPart);

            // prepare message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO,
                InternetAddress.parse("ieiri.seisaku@groups.facebook.com"));
            message.setSubject("新規政策ツィート " + hashTag);
            message.setText("件数： " + ((Integer) count).toString());
            message.setContent(multipart);

            // send
            Transport.send(message);
        }
        catch (MessagingException e) {
            System.out.println("warning: error sending mail: " + e.getMessage());
        }
    }
}