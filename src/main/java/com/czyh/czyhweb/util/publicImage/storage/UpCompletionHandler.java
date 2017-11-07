package com.czyh.czyhweb.util.publicImage.storage;

import com.czyh.czyhweb.util.publicImage.http.Response;

/**
 * Created by bailong on 15/10/8.
 */
public interface UpCompletionHandler {
    void complete(String key, Response r);
}
