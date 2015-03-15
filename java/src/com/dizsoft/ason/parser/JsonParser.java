/*
 * Copyright (c) 2015, DizSoft Inc. (jwang@dizsoft.com)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.dizsoft.ason.parser;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dizsoft.ason.Ason;
import com.dizsoft.ason.AsonValue;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Iterator;

/**
 *
 * @author DizSoft Inc. jwang@dizsoft.com
 */
public class JsonParser {
    protected AsonValue rootNode;
    
    public AsonValue parse(InputStream jsonInputStream) throws java.io.IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte data[] = new byte[1024];
        int readed;
        do {
            readed = jsonInputStream.read(data);
            if(readed>0) baos.write(data, 0, readed);
        } while(readed>=0);
        return parse(new String(baos.toByteArray(),"UTF-8"));
    }
    public AsonValue parse(String jsonStr) {
        rootNode = Ason.CreateRootArray(null);
        return parse(JSON.parse(jsonStr));
    }
    
    protected AsonValue parse(Object json) {
        if(json instanceof JSONObject) {
            AsonValue.AsonObjectOrdered m = new AsonValue.AsonObjectOrdered();
            AsonValue ret = new AsonValue(rootNode, m);
            Iterator<String> it = ((JSONObject)json).keySet().iterator();
            while(it.hasNext()) {
                String k = it.next();
                Object v = ((JSONObject)json).get(k);
                ret.add(k, parse(v));
            }
            return ret;
        } else if(json instanceof JSONArray) {
            AsonValue.AsonArray l = new AsonValue.AsonArray();
            AsonValue ret = new AsonValue(rootNode, l);
            for (Object v : (JSONArray)json) {
                ret.add(parse(v));
            }
            return ret;
        } else {
            return new AsonValue(rootNode, json);
        }
    }
}
