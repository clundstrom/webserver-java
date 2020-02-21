package assign2;

import java.util.Map;

public class ParsedHeader {

    public ParsedHeader(String requestType, String path, String contentType, Map queryParams) {
        this.requestType = requestType;
        this.path = path;
        this.contentType = contentType;
        this.queryParams = queryParams;
    }

    private String requestType;
    private String path;
    private String contentType;
    private Map queryParams;

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Map getQueryParams() {
        return queryParams;
    }

    public void setQueryParams(Map queryParams) {
        this.queryParams = queryParams;
    }
}
