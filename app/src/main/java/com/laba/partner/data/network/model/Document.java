package com.laba.partner.data.network.model;

import android.net.Uri;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Document {

    /*private int id,document_id;
    private String idVal;
    private File imgFile;
    private String name,type,doc_required,url,status,expires_at;
    private boolean expired_status = true;


    public String getIdVal() {
        return idVal;
    }

    public void setIdVal(String idVal) {
        this.idVal = idVal;
    }

    public File getImgFile() {
        return imgFile;
    }

    public void setImgFile(File imgFile) {
        this.imgFile = imgFile;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getDocument_id() {
        return document_id;
    }

    public void setDocument_id(int document_id) {
        this.document_id = document_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDoc_required() {
        return doc_required;
    }

    public void setDoc_required(String doc_required) {
        this.doc_required = doc_required;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getExpires_at() {
        return expires_at;
    }

    public void setExpires_at(String expires_at) {
        this.expires_at = expires_at;
    }

    public boolean isExpired_status() {
        return expired_status;
    }

    public void setExpired_status(boolean expired_status) {
        this.expired_status = expired_status;
    }

    public String getExpiresAtText(){
        if(expires_at == null) return "";
        try{
            SimpleDateFormat smf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

            Date parse = smf.parse(expires_at);

            SimpleDateFormat readableSmf = new SimpleDateFormat("dd/MM/yyyy");
            String format = readableSmf.format(parse);

            return format;
        }
        catch (Exception e){
            return "";
        }
    }*/

    private int id;
    transient private String idVal;
    transient private File imgFile;
    private String name;
    private String type;
    private String doc_required;
    private String status;
    private String expires_at;
    private boolean expired_status;

    private String url;
    private String document_id;
    transient private Uri imgUri;
    private ProviderDocuments providerdocuments;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDocument_id() {
        return document_id;
    }

    public void setDocument_id(String document_id) {
        this.document_id = document_id;
    }

    public boolean isExpired_status() {
        return expired_status;
    }

    public void setExpired_status(boolean expired_status) {
        this.expired_status = expired_status;
    }

    public String getDoc_required() {
        return doc_required;
    }

    public void setDoc_required(String doc_required) {
        this.doc_required = doc_required;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getExpires_at() {
        return expires_at;
    }

    public void setExpires_at(String expires_at) {
        this.expires_at = expires_at;
    }

    public ProviderDocuments getProviderdocuments() {
        return providerdocuments;
    }

    public void setProviderdocuments(ProviderDocuments providerdocuments) {
        this.providerdocuments = providerdocuments;
    }

    public Uri getImgUri() {
        return imgUri;
    }

    public void setImgUri(Uri imgUri) {
        this.imgUri = imgUri;
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ProviderDocuments getProviderDocuments() {
        return this.providerdocuments;
    }

    public void setProviderDocuments(ProviderDocuments providerDocuments) {
        this.providerdocuments = providerDocuments;
    }

    public String getIdVal() {
        return idVal;
    }

    public void setIdVal(String idVal) {
        this.idVal = idVal;
    }

    public File getImgFile() {
        return imgFile;
    }

    public void setImgFile(File imgFile) {
        this.imgFile = imgFile;
    }
}
