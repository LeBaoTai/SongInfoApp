package main.java.server;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.swing.plaf.IconUIResource;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class test {
    private static String getLinkLyricFromBHH(String link) {
        try {
            String apiLink = "https://vi.wikipedia.org/w/api.php?action=query&prop=revisions&rvprop=content&format=json&titles=";
            String url = apiLink + link;

            Document doc = Jsoup.connect(url)
                    .method(Connection.Method.GET)
                    .followRedirects(false)
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
            JSONObject revision = (JSONObject) revisions.get(0);
            String content = revision.get("*").toString();

            String[] subContent = content.split("'''");
            String name = subContent[1];

            String fullName = "";
            String dateOfBirth = "";
            String placeOfBirth = "";
            String genre = "";
            String occupation = "";
            String[] infoContent = subContent[0].replaceAll("\n\\*", "").split("\n\\| ");

            System.out.println(name);
            for (String str: infoContent) {
                if(!str.contains(" = ")) continue;
                System.out.println(str);
                String key = str.split("=")[0].strip();
                String value = str.split("=")[1].strip();
                if (key.equalsIgnoreCase("birth_name")
                        || key.equalsIgnoreCase("tên khai sinh")) {
                    fullName = value;
                }
                if (key.equalsIgnoreCase("birth_date")
                        || key.equalsIgnoreCase("ngày sinh")) {
                    value = value.replaceAll("\\{", "")
                            .replaceAll("}", "");
                    String[] subValue = value.split("\\|");
                    dateOfBirth = subValue[3] + "/" + subValue [2] + "/" + subValue[1];
                }
                if (key.equalsIgnoreCase("birth_place")
                        || key.equalsIgnoreCase("nơi sinh")) {
                    placeOfBirth = value.replaceAll("\\[", "")
                            .replaceAll("]", "");

                }
                if (key.equalsIgnoreCase("genre")
                || key.equalsIgnoreCase("thể loại âm nhạc")) {
                    if (value.contains("|")) {
                        value = value.replaceAll("\\[", "")
                                .replaceAll("]", "")
                                .replaceAll("\\{", "")
                                .replaceAll("}", "");
                        String[] subStr = value.split("\\|");
                        for (int i = 1; i < subStr.length; i++) {
                            genre += subStr[i] + " ";
                        }
                    } else {
                        value = value.replaceAll("\\[", "")
                                .replaceAll("]", "")
                                .replaceAll("\\{", "")
                                .replaceAll("}", "");
                        genre = value;
                    }
                }
                if (key.equalsIgnoreCase("occupation")
                || key.equalsIgnoreCase("nghề nghiệp")) {
                    if (value.contains("|")) {
                        value = value.replaceAll("\\[", "")
                                .replaceAll("]", "")
                                .replaceAll("\\{", "")
                                .replaceAll("}", "");
                        String[] subStr = value.split("\\|");
                        for (int i = 1; i < subStr.length; i++) {
                            occupation += subStr[i] + " ";
                        }
                    }
                    else {
                        value = value.replaceAll("\\[", "")
                                .replaceAll("]", "")
                                .replaceAll("\\{", "")
                                .replaceAll("}", "");
                        genre = value;
                    }
                }
            }

            System.out.println(fullName);
            System.out.println(dateOfBirth);
            System.out.println(placeOfBirth);
            System.out.println(occupation.strip());
            System.out.println(genre.strip());

            LinkedHashMap<String, String> returnHashMap = new LinkedHashMap<>();
            returnHashMap.putIfAbsent("fullName", fullName);
            returnHashMap.putIfAbsent("dateOfBirth", dateOfBirth);
            returnHashMap.putIfAbsent("placeOfBirth", placeOfBirth);

            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        String data = "Hùng quân";
        try {
//            System.out.println("data ham tik gg" + data);
            String ggSearchUrl = "https://www.google.com/search?q=";

            String fullUrl = ggSearchUrl + data;

            Document document = Jsoup.connect(fullUrl)
                    .followRedirects(false)
                    .method(Connection.Method.GET)
                    .execute().parse();

            // tach html tu goole search
            Element rnct = document.getElementById("rcnt");
            Element yKMVIe = rnct.getElementsByClass("yKMVIe").get(0);

            LinkedHashMap<String, String> songInfo = new LinkedHashMap<>();

            String title = yKMVIe.text();
//            System.out.println("title " + title);

            title = title.split("\\(")[0].strip();
            if (title.contains("|")) {
                String[] sub = title.split("\\|");
                title = sub[1].strip();
            }
            songInfo.putIfAbsent("title", title);

            Elements wx62f_pzpZlf_x7XAkb = rnct.getElementsByClass("wx62f PZPZlf x7XAkb");
            String subTitle = wx62f_pzpZlf_x7XAkb.first().text();

            songInfo.putIfAbsent("subTitle", subTitle);

            try {
                Element H1u2de = rnct.getElementsByClass("H1u2de").first();
                Element tagA = H1u2de.getElementsByTag("a").first();
                songInfo.putIfAbsent("linkVideo", tagA.attr("href"));
            } catch (Exception e ) {
                songInfo.putIfAbsent("linkVideo", null);
            }


            System.out.println(subTitle);
            System.out.println(subTitle.toLowerCase().endsWith("sĩ"));
            System.out.println(title);

        } catch (Exception e) {
            System.out.println("Can't connect to Google!!!");
            LinkedHashMap<String, String> tmp = new LinkedHashMap<>();
            tmp.putIfAbsent("title", null);
            tmp.putIfAbsent("subTitle", null);
        }
    }
}
