package test;

import main.java.client.connect.Connect;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.ArrayList;


public class Main {
    public static void main(String[] args) throws Exception{

        String url = "https://vi.wikipedia.org/w/api.php?action=query&prop=revisions&rvprop=content&format=json&titles=Phan_M%E1%BA%A1nh_Qu%E1%BB%B3nh";
        String url1 = "https://vi.wikipedia.org/w/api.php?action=query&prop=revisions&rvprop=content&format=json&titles=S%C6%A1n_T%C3%B9ng_M-TP";
        Document doc = Jsoup.connect(url)
                .method(Connection.Method.GET)
                .ignoreContentType(true)
                .execute().parse();


        JSONObject json = new JSONObject(doc.text());
        JSONObject query = json.getJSONObject("query");
        JSONObject pages = query.getJSONObject("pages");

        String pageId = pages.keySet().toString();
//        pageId = pageId.replaceAll("\\[", "");
        pageId = pageId.replaceAll("]|\\[", "");

        JSONObject page = pages.getJSONObject(pageId);
        JSONArray revisions = page.getJSONArray("revisions");
        JSONObject revision1 = (JSONObject) revisions.get(0);
        ArrayList<String> info = getInfo(revision1.get("*").toString());
    }

    private static ArrayList<String> getInfo(String rawInfo) {

        String[] listInfo = rawInfo.split("'''");

        String[] thongTin = listInfo[0].split("Thông tin nhân vật\\n\\|");

        String[] thongTinCaNhan = thongTin[1].split("\n");

        for (String str: thongTinCaNhan) {
            if (str.replaceAll("\\s+", "").split("=").length < 2) continue;
            str = str.replaceAll("\\[|]|\\{|}", "");
            str = str.replaceFirst("\\|", " ");
            str = str.strip();
            if (str.contains("tên") || str.contains("birth_name")) {
                System.out.println(str);
            } else if (str.contains("ngày sinh") || str.contains("birth_date")) {
                System.out.println(str);
            } else if (str.contains("nguyên quán") || str.contains("birth_place")) {
                System.out.println(str);
            } else if (str.contains("nghề nghiệp")) {
                System.out.println(str);
            } else if (str.contains("dân tộc") ) {
                System.out.println(str);
            }
        }
        return null;
    }
}
