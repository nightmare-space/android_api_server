package com.nightmare.aas;

import android.util.Log;

import com.nightmare.aas.foundation.AndroidAPIPlugin;
import com.nightmare.aas.helper.L;

import org.json.JSONObject;

import fi.iki.elonen.NanoHTTPD;

public class AndroidAPIServerHTTPD extends NanoHTTPD {
    public AndroidAPIServerHTTPD(String address, int port) {
        super(address, port);
    }


    AndroidAPIServer aas;
    String key;

    void setAndroidAPIServer(AndroidAPIServer aas) {
        this.aas = aas;
    }

    @Override
    public Response serve(IHTTPSession session) {
        try {
            String url = session.getUri();
            Log.d("AndroidAPIServerHTTPD", "url -> " + url);
            if (url.startsWith("/check")) {
                return newFixedLengthResponse(Response.Status.OK, "text/plain", "ok");
            }
            // extract key from header
            String key = session.getHeaders().get("key");
            if (key == null) {
                key = session.getParms().get("key");
            }
            if (key == null) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("result", "You need set key to request header");
                return newFixedLengthResponse(Response.Status.BAD_REQUEST, "application/json", jsonObject.toString());
            }
            if (this.key == null) {
                this.key = key;
            }
            if (!key.equals(this.key)) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("result", "You need use the same key what you first request");
                return newFixedLengthResponse(Response.Status.BAD_REQUEST, "application/json", jsonObject.toString());
            }
            for (AndroidAPIPlugin plugin : aas.plugins) {
                if (!plugin.route().isEmpty() && url.startsWith(plugin.route())) {
                    // log the request
                    L.d("request url: " + url + " param: " + session.getParms());
                    return plugin.handle(session);
                }
            }
            return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "route not found");
        } catch (Exception e) {
            // noinspection CallToPrintStackTrace
            e.printStackTrace();
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", e.toString());
        }
    }
}
