package ist.cnv.loadBalancer;


public class AvailableWebServer {
    private String url = null;

    public AvailableWebServer(String url) {
        this.url = "http://" + url + "/r.html?";
    }

    public String getUrl() {
        return url;
    }
}
