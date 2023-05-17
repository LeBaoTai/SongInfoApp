package test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Scanner;
import org.json.JSONObject;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class WikipediaContentFetcher {

    public static void main(String[] args) throws IOException {

        // Nhập từ khóa tìm kiếm từ bàn phím
        Scanner scanner = new Scanner(System.in);
        System.out.print("Nhập từ khóa tìm kiếm trên Google: ");
        String keyword = scanner.nextLine();
        scanner.close();

        // URL trang tìm kiếm Google
        String googleUrl = "https://www.google.com/search?q=" + keyword;

        // Kết nối và lấy trang web dưới dạng HTML
        Document doc = Jsoup.connect(googleUrl).get();

        // Tìm thẻ HTML có class "ruhjFe NJLBac fl" (đường link của kết quả tìm kiếm đầu tiên)
        Elements links = doc.select("a.ruhjFe.NJLBac.fl");
        if (links.size() == 0) {
            System.out.println("Không có thông tin cho ca sĩ này");
            return;
        }
        Elements searchResults = doc.select("div.wx62f.PZPZlf.x7XAkb");

        String subtitle = searchResults.first().text();
        if (!(subtitle.contains("Ca sĩ") || subtitle.contains("Rapper") || subtitle.contains("Nhạc sĩbi"))) {
            System.out.println("đây không phải ca sĩ, rapper hoặc nhạc sĩ");
            return;
        }



        // Lấy đường link của kết quả tìm kiếm đầu tiên
        String href = links.first().attr("href");

        // Lấy tên trang Wikipedia từ đường link
        String pageTitle = href.substring(href.indexOf("/wiki/") + 6);

        // Tạo URL cho trang Wikipedia
        String wikiUrl = "https://vi.wikipedia.org/w/api.php?action=query&prop=revisions&rvprop=content&format=json&titles=" + pageTitle;

        // Kết nối và lấy nội dung trang Wikipedia
        URL url = new URL(wikiUrl);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder content = new StringBuilder();
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();

        JSONObject data = new JSONObject(content.toString());

        // Lấy thông tin của trang
        JSONObject pages = data.getJSONObject("query").getJSONObject("pages");
        JSONObject page = pages.getJSONObject(pages.keys().next());

        // Tìm và lấy thông tin ca sĩ
        String contentText = page.getJSONArray("revisions").getJSONObject(0).getString("*");
        String[] lines = contentText.split("\\r?\\n");

        String birthName = null;
        String birthDate = null;
        String birthPlace = null;
        String namhoatdong = null;
        String dongnhac = null;
        String hangdia = null;
        String nhaccu = null;
        String theloai = null;
        String job = null;
        for (String line : lines) {
            //    tên
            if (line.startsWith("| tên khai sinh =")) {
                birthName = line.substring("| tên khai sinh =".length()).trim();
            } else if (line.startsWith("| birth_name =")) {
                birthName = line.substring("| birth_name =".length()).trim();
            } else if (line.startsWith("|Tên=")) {
                birthName = line.substring("|Tên=".length()).trim();
            } else if (line.startsWith("| Tên khai sinh =")) {
                birthName = line.substring("| Tên khai sinh =".length()).trim();
            }else if (line.startsWith("| tên thật =")) {
                birthName = line.substring("| tên thật =".length()).trim();
            }
            //    ngày sinh
            else if (line.startsWith("| ngày sinh =")) {
                birthDate = line.substring("| ngày sinh =".length()).trim();
            }else if (line.startsWith("| birth_date")) {
                birthDate = line.substring("| birth_date".length()).trim();
            }
            else if (line.startsWith("|Sinh ngày=")) {
                birthDate = line.substring("|Sinh ngày=".length()).trim();
            }else if (line.startsWith("| Ngày sinh =")) {
                birthDate = line.substring("| Ngày sinh =".length()).trim();
            }

            //    nơi sinh
            else if (line.startsWith("| nơi sinh =")) {
                birthPlace = line.substring("| nơi sinh =".length()).trim();
            }else if (line.startsWith("| birth_place")) {
                birthPlace = line.substring("| birth_place".length()).trim();
            }
            else if (line.startsWith("|Nơi sinh=")) {
                birthPlace = line.substring("|Nơi sinh=".length()).trim();
            }else if (line.startsWith("| Nơi sinh =")) {
                birthPlace = line.substring("| Nơi sinh =".length()).trim();
            }
            //    năm hoạt động
            else if (line.startsWith("| năm hoạt động")) {
                namhoatdong = line.substring("|".length()).trim();
            }else if (line.startsWith("|years_active =")) {
                namhoatdong = line.substring("|".length()).trim();
            }
            else if (line.startsWith("| years_active =")) {
                namhoatdong = line.substring("|".length()).trim();
            }else if (line.startsWith("|Năm hoạt động=")) {
                namhoatdong = line.substring("|".length()).trim();
            }else if (line.startsWith("| years_active    =")) {
                namhoatdong = line.substring("|".length()).trim();
            }
            //    dòng nhạc
            else if (line.startsWith("| dòng nhạc ")) {
                dongnhac = line.substring("|".length()).trim();
            }else if (line.startsWith("|label=")) {
                dongnhac = line.substring("|".length()).trim();
            }
            //    hãng đĩa
            if (line.startsWith("| hãng đĩa = ")) {
                hangdia = line.substring("|".length()).trim();
            }else if (line.startsWith("| Hãng đĩa =*")) {
                hangdia = line.substring("|".length()).trim();
            }else if (line.startsWith("|hãng đĩa=")) {
                hangdia = line.substring("|".length()).trim();
            }
            //    nhac cu
            if (line.startsWith("| nhạc cụ = ")) {
                nhaccu = line.substring("|".length()).trim();
            }else if (line.startsWith("| instrument = ")) {
                nhaccu = line.substring("|".length()).trim();
            }else if (line.startsWith("|instrument=")) {
                nhaccu = line.substring("|".length()).trim();
            }

            //thể loại
            if (line.startsWith("| thể loại =")) {
                theloai = line.substring("|".length()).trim();
            }else if (line.startsWith("| dòng nhạc")) {
                theloai = line.substring("|".length()).trim();
            }else if (line.startsWith("| genre = {{flat list|")) {
                theloai = line.substring("| genre = {{flat list|".length());
                // tiếp tục xử lý với chuỗi genre ở đây
            }else if (line.startsWith("|genre=ameee")) {
                theloai = line.substring("|".length()).trim();
            }else if (line.startsWith("|Thể loại=")) {
                theloai = line.substring("|".length()).trim();
            }else if (line.startsWith("|Genre = ")) {
                theloai = line.substring("|".length()).trim();
            }

            //nghề nghiệp
            if (line.startsWith("| nghề nghiệp = ")) {
                job = line.substring("|".length()).trim();
            }else if (line.startsWith("| occupation = ")) {
                job = line.substring("|".length()).trim();
            }else if (line.startsWith("|Nghề nghiệp=")) {
                job = line.substring("|".length()).trim();
            }
        }

        // In ra các thông tin về ca sĩ
        System.out.println("Tên khai sinh: " + birthName );
        System.out.println("Ngày sinh: " + birthDate);
        System.out.println("Nơi sinh: " + birthPlace);
        System.out.println("thong tin su nghiep: " +"\n"+ hangdia + " \n " + namhoatdong + " \n " +dongnhac + "\n"+nhaccu+" \n "+theloai+" \n "+"\n" +job);
    }
}