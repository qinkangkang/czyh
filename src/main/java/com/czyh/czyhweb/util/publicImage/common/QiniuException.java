package com.czyh.czyhweb.util.publicImage.common;


import java.io.IOException;

import com.czyh.czyhweb.util.publicImage.http.Response;



public class QiniuException extends IOException {
    public final Response response;


    public QiniuException(Response response) {
        this.response = response;
    }

    public QiniuException(Exception e) {
        super(e);
        this.response = null;
    }

    public String url() {
        return response.url();
    }

    public int code() {
        return response == null ? -1 : response.statusCode;
    }
}
