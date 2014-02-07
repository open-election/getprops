package to.netparty.getprops;

public class PropsApp 
{
    public static void main( String[] args )
    {
        CrawlTweets ct = new CrawlTweets(
                "ieiri.seisaku@groups.facebook.com"
                , "新規政策ツィート "
        );
        
        ct.checkTweets("fukushi", "#ぼくらの福祉政策");
        ct.checkTweets("toshi", "#ぼくらの都市計画・成長戦略");
        ct.checkTweets("gyosei", "#ぼくらの行政改革");
        ct.checkTweets("bosai", "#ぼくらの防災・危機管理計画");
        ct.checkTweets("2020", "#ぼくらの2020年計画");
        ct.checkTweets("props", "#ぼくらの政策");
        ct.checkTweets("ibasho", "#どんな人でも生きやすい居場所");
        ct.checkTweets("lifestyle", "#新しい技術と新しいライフスタイル");
        ct.checkTweets("idea", "#政治家じゃ思いつかないアイデア");
        ct.checkTweets("jyosei", "#ぼくらが愛する女性のために");
    }    
}
