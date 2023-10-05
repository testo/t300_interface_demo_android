package de.testo.demo.t300interface.util;


public class URLBuilder {

    private static String activeUrl = "";

    public static String getActiveUrl() {
        return activeUrl;
    }

    public static void setActiveUrl(String ip, String port) {
        setActiveUrl(ip + ":" + port);
    }

    public static void setActiveUrl(String activeUrl) {
        URLBuilder.activeUrl = activeUrl;
    }

    public static String getTjfUrl() {
        return "http://" + activeUrl + "/data";
    }

    public static String getZivUrl() {
        return "http://" + activeUrl + "/o";
    }

    public static String getDeviceInfoUrl() {
        return "http://" + activeUrl + "/deviceInfo";
    }
}
