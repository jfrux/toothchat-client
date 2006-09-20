package org.jivesoftware.spark.plugin;

import org.jdesktop.jdic.browser.WebBrowser;
import org.jivesoftware.spark.util.StringUtils;
import org.jivesoftware.spark.util.log.Log;
import org.w3c.dom.Element;

import java.awt.BorderLayout;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPanel;


/**
 * The GoogleSearchResult is one instance of a found item from a Google Search.
 *
 * @author Derek DeMoro
 */
public class GoogleSearchResult {
    private String searchBase;
    private String searchUrl;
    private String query;
    private int relevance;
    String id;
    String title;
    String url;
    String time;
    String snippet;
    String icon;
    String cacheUrl;
    String from;

    public GoogleSearchResult(String searchBase, String query, int relevance, Element element) {
        this.searchBase = searchBase;
        this.query = query;
        this.relevance = relevance;
        this.id = getContent("id", element);
        this.title = getContent("title", element);
        this.url = getContent("url", element);
        this.time = getContent("time", element);
        this.snippet = getContent("snippet", element);
        this.icon = getContent("icon", element);
        this.cacheUrl = getContent("cache_url", element);
        this.from = getContent("from", element);
    }

    private String getContent(String field, Element element) {
        try {
            return ((Element)element.getElementsByTagName(field).item(0)).getChildNodes().item(0).getNodeValue();
        }
        catch (Exception e) {
            return null;
        }
    }

    public String getCachedURL() {
        return cacheUrl;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getQuery() {
        return query;
    }

    public String getUniqueID() {
        return id;
    }

    public String getSubject() {
        title = StringUtils.stripTags(title);

        return title.replaceAll("<[^>]+>", "");
    }

    public String getAuthor() {
        return from != null ? from : "unknown";
    }

    public String getToolTip() {
        return snippet;
    }

    /**
     * Return the relevance of this document pertaining to the query.
     *
     * @return the relevance of this document.
     */
    public int getRelevance() {
        return relevance;
    }

    public Date getPostedDate() {
        return new Date(Long.parseLong(time));
    }

    /**
     * Return the icon to be used to identify the type of search result.
     *
     * @return the icon to be use.
     */
    public Icon getIcon() {
        try {
            return new ImageIcon(new URL(searchBase + icon));
        }
        catch (MalformedURLException e) {
            return null;
        }
    }

    /**
     * Return the component to use to display this document.
     *
     * @return the component to use to display this document.
     */
    public JComponent getDocumentViewer() {
        WebBrowser viewer = new WebBrowser();
        File file = new File(url);
        if (file.exists()) {
            try {
                URL curl = new URL(cacheUrl);
                viewer.setURL(curl);
            }
            catch (Exception e) {
                Log.error(e);
            }
        }
        else {
            try {
                viewer.setURL(new URL(url));
            }
            catch (MalformedURLException e) {
                try {
                    URL curl = new URL(cacheUrl);
                    viewer.setURL(curl);
                }
                catch (MalformedURLException e1) {
                    Log.error(e1);
                }
            }
        }

        JPanel p = new JPanel();
        p.setLayout(new BorderLayout());
        p.add(viewer, BorderLayout.CENTER);

        return p;
    }

    public String getURL() {
        return url;
    }

    /**
     * Return the values to populate the table with. Please see
     * GoogleSearchPlugin#getFieldHeaders() for the type of data to return.
     *
     * @return the values to populate the table with.
     */
    public List getFieldValues() {
        final List returnList = new ArrayList();
        returnList.add(StringUtils.stripTags(snippet));
        returnList.add(getSubject());
        return returnList;
    }

    /**
     * Value returned if item is dragged.
     *
     * @return the value returned if this item is dragged.
     */
    public String getDraggableValue() {
        return "";
    }

    /**
     * Returns the date when this object was created.
     *
     * @return the date when this object was created.
     */
    public Date getCreationDate() {
        return null;
    }

    /**
     * Returns a summary of the search result item. This will be displayed in the "All" tab of a search result.
     *
     * @return the summary of the search result.
     */
    public String getSummary() {
        return getSubject();
    }

    private String getGoogle() {
        return "" +
            "<html>" +
            "<head>" +
            "<style>\n" +
            "BODY,TD,DIV,.P,A {\tFONT-FAMILY: arial,sans-serif}\n" +
            "DIV,TD { COLOR: #000}\n" +
            ".f { COLOR: #6f6f6f}\n" +
            ".fl:link { COLOR: #6f6f6f}\n" +
            "A:link, .w, A.w:link, .w A:link { COLOR: #00c}\n" +
            "A:visited {\tCOLOR: #551a8b}\n" +
            ".fl:visited { COLOR: #551a8b}\n" +
            "A:active, .fl:active  {\tCOLOR: #f00}\n" +
            ".h { COLOR: #3399CC}\n" +
            ".i { COLOR: #a90a08}\n" +
            ".i:link { COLOR: #a90a08}\n" +
            ".a, .a:link, .a:visited { COLOR: #008000}\n" +
            "DIV.n {\tMARGIN-TOP: 1ex}\n" +
            ".n A { FONT-SIZE: 10pt; COLOR: #000}\n" +
            ".n .i {\tFONT-WEIGHT: bold; FONT-SIZE: 10pt}\n" +
            ".q A:visited { COLOR: #00c}\n" +
            ".q A:link {\tCOLOR: #00c}\n" +
            ".q A:active { COLOR: #00c}\n" +
            ".q { COLOR: #00c}\n" +
            ".b { FONT-WEIGHT: bold; FONT-SIZE: 12pt; COLOR: #00c}\n" +
            ".ch { CURSOR: hand}\n" +
            ".e { MARGIN-TOP: 0.75em; MARGIN-BOTTOM: 0.75em}\n" +
            ".g { MARGIN-TOP: 1em; MARGIN-BOTTOM: 1em}\n" +
            ".f { MARGIN-TOP: 0.5em; MARGIN-BOTTOM: 0.25em}\n" +
            ".s { HEIGHT: 10px }\n" +
            ".c:active {\tCOLOR: #ff0000}\n" +
            ".c:visited { COLOR: #551a8b}\n" +
            ".c:link { COLOR: #7777cc}\n" +
            ".c { COLOR: #7777cc }\n" +
            "</style>" +
            "</head>" +
            "<body>" +
            "<table>" +
            "<tr>" +
            "<td rowspan=\"2\" valign=\"top\">&nbsp;<img style=\"vertical-align: middle;\" src=\"" + searchBase + "/read.gif\" border=\"0\" height=\"16\" width=\"16\">&nbsp;&nbsp;</td>" +
            "<td width=\"98%\"><a href=\"" + cacheUrl + "\">" + title + "</a></td>" +
            "</tr>" +
            "<tr>" +
            "<td><font size=\"-1\">" + snippet + "</font><br>" +
            "<font color=\"#008000\" size=\"-1\">" + from + " - DATE </font>" +
            "</td>" +
            "</tr>" +
            "</table>" +
            "</body>" +
            "</html>" +
            "";
    }
}