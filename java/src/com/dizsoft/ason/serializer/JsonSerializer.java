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
package com.dizsoft.ason.serializer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dizsoft.ason.AsonValue;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author DizSoft Inc. jwang@dizsoft.com
 */
public class JsonSerializer {
    private Map<Integer,String> st;

    public String serialize(AsonValue ason,boolean prettyFormat) {
        Map<String,Integer> tst = ason.getDictMap();
        if(tst!=null) {
            st = new HashMap<>();
            Iterator<String> it = tst.keySet().iterator();
            while(it.hasNext()) {
                String k = it.next();
                Integer v = tst.get(k);
                st.put(v, k);
            }
        }

        Object json = serializeValue(ason);
        return JSON.toJSONString(json, prettyFormat);
    }
    
    private Object serializeValue(AsonValue ason) {
        if(ason.getType()==AsonValue.TYPE_ARRAY) {
            JSONArray ret = new JSONArray(ason.size());
            for(AsonValue v : ason) {
                ret.add(serializeValue(v));
            }
            return ret;
        } else if(ason.getType()==AsonValue.TYPE_OBJECT) {
            JSONObject ret = new JSONObject(ason.size());
            Iterator<Integer> it = ason.keyIterator();
            while(it.hasNext()) {
                Integer k = it.next();
                String  sk = st==null?k.toString():st.get(k);
                if(sk==null) sk = k.toString();
                AsonValue v = ason.get(k);
                ret.put(sk, serializeValue(v));
            }
            return ret;
        } else {
            return ason.getValue();
        }
    }
}
