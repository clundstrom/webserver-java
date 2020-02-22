package assign2;

import java.time.LocalDateTime;

public class HttpResponse {

    String version = "HTTP/1.1 ";
    String statusCode = "200 OK";
    String server = "Server: Webservice";
    String date = "Date: " + LocalDateTime.now();
    String contentLength = "Content-Length: ";
    String connection = "Connection: close";
    String contentType = "";
    String[] extras;

    public HttpResponse(String statusCode, int contentLength, String contentType) {
        this.setStatusCode(statusCode);
        this.setContentLength(contentLength);
        this.setContentType(contentType);
    }

    /**
     * Composes all header fields into a byte array.
     *
     * @return byte Byte array of header.
     */
    byte[] build() {
        StringBuilder sb = new StringBuilder();
        sb.append(version);
        sb.append(statusCode + "\r\n");
        sb.append(server + "\r\n");
        sb.append(date + "\r\n");
        sb.append(contentLength + "\r\n");
        sb.append(connection + "\r\n");
        sb.append(contentType + "\r\n");

        if (extras != null) {
            for (String s : extras) {
                sb.append(s + "\r\n");
            }
        }

        sb.append("\r\n");

        return sb.toString().getBytes();
    }

    void setContentType(String type) {
        contentType = "Content-Type:" + type;
    }

    void setContentLength(int length) {
        if (length <= 0)
            contentLength = "Content-Length:" + 0;
        else
            contentLength = "Content-Length:" + length;
    }

    void setExtras(String[] extras) {
        if (extras != null) {
            this.extras = extras;
        }
    }

    void setStatusCode(String status) {
        this.statusCode = status;
    }
}
