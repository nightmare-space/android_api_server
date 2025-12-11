package com.nightmare.aas_plugins;

import com.nightmare.aas.foundation.AndroidAPIPlugin;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import fi.iki.elonen.NanoHTTPD;

import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;

public class DeviceInfoPlugin extends AndroidAPIPlugin {
    @Override
    public String route() {
        return "/device_info";
    }

    private String readFileContent(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                StringBuilder content = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    content.append(line);
                }
                return content.toString();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public NanoHTTPD.Response handle(NanoHTTPD.IHTTPSession session) {
        String action = session.getParms().get("action");
        switch (action) {
            case "proc_stat":
                // get /proc/stat
                String result = readFileContent("/proc/stat");
                return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "text/plain", result);
        }
        // path /sys/devices/system/cpu/cpu[0-7]/cpufreq/scaling_cur_freq
        JSONObject result = new JSONObject();
        JSONArray cpu = new JSONArray();
        // /proc/cpuinfo
        try {
            for (int i = 0; i < 8; i++) {
                JSONObject cpuInfo = new JSONObject();
                String content = readFileContent("/sys/devices/system/cpu/cpu" + i + "/cpufreq/scaling_cur_freq");
                if (content != null) {
                    cpuInfo.put("scaling_cur_freq", Integer.parseInt(content));
                }
                content = readFileContent("/sys/devices/system/cpu/cpu" + i + "/cpufreq/scaling_min_freq");
                if (content != null) {
                    cpuInfo.put("scaling_min_freq", Integer.parseInt(content));
                }
                // /sys/devices/system/cpu/cpu[0-7]/cpufreq/cpuinfo_max_freq
                content = readFileContent("/sys/devices/system/cpu/cpu" + i + "/cpufreq/scaling_max_freq");
                if (content != null) {
                    cpuInfo.put("scaling_max_freq", Integer.parseInt(content));
                }
                cpu.put(cpuInfo);
            }

            result.put("cpu", cpu);
            // get gpu radio from /sys/class/kgsl/kgsl-3d0/gpubusy
            String gpuContent = readFileContent("/sys/class/kgsl/kgsl-3d0/gpubusy");
            JSONArray gpu = new JSONArray();
            if (gpuContent != null) {
                String[] split = gpuContent.trim().split("\\s+");
                gpu.put(Integer.parseInt(split[0]));
                gpu.put(Integer.parseInt(split[1]));
            }
            result.put("gpu", gpu);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", result.toString());
    }
}
