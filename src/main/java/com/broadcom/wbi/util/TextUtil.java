package com.broadcom.wbi.util;

import org.apache.commons.lang.WordUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.Arrays;
import java.util.List;
import java.util.regex.PatternSyntaxException;

public class TextUtil {

    public static String formatName(String d) {
//		List<String> firstCapital = Arrays.asList("max", "for", "pre", "rom", "ngo", "die", "key", "lob", "zed", "lee", "kou", "kuo", "old",
//				"cao", "ver", "pin", "and", "red");
        List<String> allCapital = Arrays.asList("ngsdk", "xldk", "umpte", "pcie", "pra", "aka", "npv", "p&r", "htol", "lob", "cmicm");

        d = d.replaceAll("\\+", "plus");
        if (d.trim().length() > 0) {
            if (d.matches(".*(\\s+|\\_|\\-|\\/).*")) {
                String[] darr = d.trim().split("\\s+|\\_|\\-|\\/|\\(|\\)");
                for (String ds : darr) {
                    String l = ds.replaceAll("\\d", "").replaceAll("\\.", "").replaceAll("[()]", "").replaceAll("(?i)x", "").trim();
                    int length = l.replaceAll("plus", "").length();
                    if (length > 2) {
                        if (allCapital.contains(l))
                            l = ds.toUpperCase();
                        else
                            l = WordUtils.capitalizeFully(ds);
                    } else {
                        l = ds.toUpperCase();
                    }
                    try {
                        d = d.replaceFirst(ds, l);
                    } catch (PatternSyntaxException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                String l = d.trim().replaceAll("[0-9]", "");
                int length = l.replaceAll("plus", "").length();
                String newl = l;
                if (length > 3) {
                    if (allCapital.contains(l))
                        newl = l.toUpperCase();
                    else
                        newl = WordUtils.capitalizeFully(l);
                } else
                    newl = l.toUpperCase();
                d = d.replace(l, newl);
            }
        }
        d = d.replaceAll("(?i)plus", "\\+");
        return d;
    }

    public static String cleanRemark(String remark) {
        remark = remark.replaceAll("^(\\[)?[0-1]?\\d\\/[0-3]?\\d\\/(18|19|20|21)?\\d{2}(\\])?(\\s)*:?", "").trim();
        remark = remark.replaceAll("^\\<p\\>", "");
        remark = remark.replaceAll("\\[(\\s)+\\]", "");
        return remark;
    }

    public static String cleanHeadline(String hlstring) {
        hlstring = hlstring.replaceAll("(<hr>)+", "<hr>")
                .replaceAll("<hr>\\s*$", "")
                .replaceAll("\\[\\d{2}\\/\\d{2}\\/\\d{2,4}\\]", "")
                .replaceAll("<[a-zA-Z0-9]*>\\s*</[a-zA-Z0-9]*>", "")
                .replaceAll("^\\s*<hr>", "")
                .replaceAll("\'\'", "\'")
                .replaceAll("(<hr>)*?$", "")
                .replaceAll("<([^>]*)></\\1>", "");
        if (hlstring.length() > 0) {
            Document doc = Jsoup.parse(hlstring);
            for (Element element : doc.select("*")) {
                if (!element.hasText() && element.isBlock() && !element.tagName().equalsIgnoreCase("hr")) {
                    element.remove();
                }
            }
            return doc.body().html();
        }
        return "";
    }

}
