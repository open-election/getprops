package to.netparty.getprops;

public class App 
{
    public static void main( String[] args )
    {
        CrawlTweets ct = new CrawlTweets();
        
        ct.checkTweets("fukushi", "#ぼくらの福祉政策");
        ct.checkTweets("toshi", "#ぼくらの都市計画・成長戦略");
        ct.checkTweets("gyosei", "#ぼくらの行政改革");
        ct.checkTweets("bosai", "#ぼくらの防災・危機管理計画");
        ct.checkTweets("2020", "#ぼくらの2020年計画");
        ct.checkTweets("props", "#ぼくらの政策");
    }    
}