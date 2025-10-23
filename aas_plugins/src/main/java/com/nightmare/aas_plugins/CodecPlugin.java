package com.nightmare.aas_plugins;

import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;

import android.media.MediaCodecInfo;
import android.media.MediaCodecList;

import com.nightmare.aas.foundation.AndroidAPIPlugin;
import com.nightmare.aas.helper.L;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import fi.iki.elonen.NanoHTTPD;

public class CodecPlugin extends AndroidAPIPlugin {


    @Override
    public String route() {
        return "/codec";
    }

    @Override
    public NanoHTTPD.Response handle(NanoHTTPD.IHTTPSession session) {
        String action = session.getParms().get("action");
        assert action != null;

        switch (action) {
            case "get_codec_list":
                JSONObject jsonObject = new JSONObject();
                JSONArray jsonArray = new JSONArray();
                // 无界试一下在控制页面同时拿host和target的这个方法
                // ALL_CODECS 会多一些编码器
                // Codec Name: c2.qti.avc.decoder.secure, Supported Type: video/avc
                // Codec Name: OMX.qcom.video.decoder.avc.secure, Supported Type: video/avc
                // Codec Name: c2.qti.hevc.decoder.secure, Supported Type: video/hevc
                // Codec Name: OMX.qcom.video.decoder.hevc.secure, Supported Type: video/hevc
                // Codec Name: c2.qti.vp9.decoder.secure, Supported Type: video/x-vnd.on2.vp9
                // Codec Name: OMX.qcom.video.decoder.vp9.secure, Supported Type: video/x-vnd.on2.vp9
                // Codec Name: c2.qti.av1.decoder.secure, Supported Type: video/av01
                // Codec Name: c2.qti.dv.decoder.secure, Supported Type: video/dolby-vision
                // Codec Name: c2.qti.aac.hw.encoder, Supported Type: audio/mp4a-latm
                // Codec Name: c2.qti.amrnb.hw.encoder, Supported Type: audio/3gpp
                // Codec Name: c2.qti.amrwb.hw.encoder, Supported Type: audio/amr-wb
                try {
                    MediaCodecList codecList = new MediaCodecList(MediaCodecList.REGULAR_CODECS);
                    MediaCodecInfo[] codecInfos = codecList.getCodecInfos();
                    for (MediaCodecInfo codecInfo : codecInfos) {
                        String name = codecInfo.getName();
                        String[] supportedTypes = codecInfo.getSupportedTypes();
                        JSONObject codec = new JSONObject();
                        for (String type : supportedTypes) {
                            codec.put("name", name);
                            codec.put("type", type);
                            codec.put("isEncoder", codecInfo.isEncoder());
                        }
                        jsonArray.put(codec);
                    }
                    jsonObject.put("datas", jsonArray);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", jsonObject.toString());

            default:
                break;
        }
        return null;
    }

}