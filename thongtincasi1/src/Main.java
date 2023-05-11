import java.util.*;
import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("singer name: ");
        String songName = scanner.nextLine();

        LinkedHashMap<String, String> songInfo = getResponseFromGoogle(songName);
        System.out.println("Title: " + songInfo.get("title"));
        System.out.println("Sub-Title: " + songInfo.get("subTitle"));
        System.out.println("Description: " + songInfo.get("description"));
    }

    private static LinkedHashMap<String, String> getResponseFromGoogle(String data) {
    try {
        String ggSearchUrl = "https://www.google.com/search?q=";
        String fullUrl = ggSearchUrl + data;
        Document document = Jsoup.connect(fullUrl).method(Connection.Method.GET).execute().parse();

        Element rnct = document.getElementById("rcnt");
        Elements yKMVIe = rnct.getElementsByClass("yKMVIe");
        Elements wx62f_pzpZlf_x7XAkb = rnct.getElementsByClass("wx62f PZPZlf x7XAkb");
        Elements desc = rnct.getElementsByClass("UDZeY fAgajc OTFaAf");

        LinkedHashMap<String, String> songInfo = new LinkedHashMap<>();
        songInfo.putIfAbsent("title", yKMVIe.get(0).text());
        String subTitle = wx62f_pzpZlf_x7XAkb.get(0).text();
        if (!subTitle.contains("Ca sĩ")) {
            System.out.println("Lỗi: Không phải ca sĩ");
            return null;
        }
        songInfo.putIfAbsent("subTitle", subTitle);
        songInfo.putIfAbsent("description", desc.get(0).text());

        return songInfo;
    } catch (Exception e) {
        LinkedHashMap<String, String> tmp = new LinkedHashMap<>();
        tmp.putIfAbsent("title", null);
        tmp.putIfAbsent("subTitle", null);
        tmp.putIfAbsent("description", null);
        return tmp;
    }
}

}
