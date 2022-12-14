package com.laba.partner.data.network.model;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ProviderDocuments {

    private String url;
    private String status;
    private String document_id;
    private String expires_at;

    public String getExpires_at() {
        return expires_at;
    }

    public void setExpires_at(String expires_at) {
        this.expires_at = expires_at;
    }

    public String getExpiresAtText() {
        try {
            SimpleDateFormat smf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

            Date parse = smf.parse(expires_at);

            SimpleDateFormat readableSmf = new SimpleDateFormat("dd/MM/yyyy");
            String format = readableSmf.format(parse);

            return format;
        } catch (Exception e) {
            return "";
        }
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDocumentId() {
        return this.document_id;
    }

    public void setDocumentId(String document_id) {
        this.document_id = document_id;
    }
}
