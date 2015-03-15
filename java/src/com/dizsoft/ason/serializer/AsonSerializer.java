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

import com.dizsoft.ason.Ason;
import com.dizsoft.ason.AsonValue;
import com.dizsoft.ason.utils.Utils;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author DizSoft Inc. jwang@dizsoft.com
 */
public class AsonSerializer {
    private static final byte ZERO[] = {0,0,0,0};
    
    private boolean flagBigId = false; // true:32位ID, false:16位ID
    private final int flag;
    
    private final Map<Integer,String> stringTableRev = new HashMap<>();
    
    public AsonSerializer(int _flag) {
        flag = _flag;
        flagBigId = (flag & Ason.FLAG_FORCEBIGID)!=0;
    }
    
    public byte[] serialize(AsonValue obj) throws java.io.IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        serialize(obj, baos);
        return baos.toByteArray();
    }

    public void serialize(AsonValue obj,OutputStream os) throws java.io.IOException {
        // Write string table data
        int maxId = prepareDictMap(obj);
        if(!flagBigId && maxId>0x7FFF) {
            flagBigId = true;
        }
        int hdr = 0x80000000 | (flagBigId?0x40000000:0);
        if((flag&Ason.FLAG_DICTHEAD)!=0) {
            byte stb[] = serializeStringTable(obj.getDictMap());
            int len = stb.length;
            if(len>0x0FFFFFFF) throw new RuntimeException("string table should be <= 0x0FFFFFFF bytes.(about 256M)");
            os.write(Utils.Int322Bytes(hdr|len));
            os.write(stb);
        } else {
            os.write(Utils.Int322Bytes(hdr));
        }

        // write body data
        if ((flag&Ason.FLAG_MODESTRUCT)!=0) {
            ByteArrayOutputStream data = new ByteArrayOutputStream();
            serializeAsonValueStructed(os, obj);
            writeBytesWithTypeLength(os, obj.isArray()?AsonValue.TYPE_ARRAY:AsonValue.TYPE_OBJECT, data.toByteArray());
        } else {
            serializeAsonValueStreamed(os, obj);
        }
    }
    
    protected void serializeAsonValueStreamed(OutputStream os,AsonValue val) throws IOException {
        if(val.getType()==AsonValue.TYPE_OBJECT) {
            os.write(AsonValue.TYPE_OBJECT);
            java.util.Iterator<Integer> it = val.keyIterator();
            while(it.hasNext()) {
                Integer id = it.next();
                serializeId(os, id);
                serializeAsonValueStreamed(os, val.get(id));
            }
            os.write(ZERO, 0, flagBigId?4:2);
        } else if(val.getType()==AsonValue.TYPE_ARRAY) {
            os.write(AsonValue.TYPE_ARRAY);
            java.util.Iterator<AsonValue> it = val.iterator();
            while(it.hasNext()) {
                AsonValue v = it.next();
                serializeAsonValueStreamed(os, v);
            }
            os.write(0);
        } else if(val.getType()==AsonValue.TYPE_STRING) {
            os.write(AsonValue.TYPE_STRING);
            os.write(((String)val.getValue()).getBytes("UTF-8"));
            os.write(0);
        } else if(val.getType()==AsonValue.TYPE_BYTES) {
            os.write(AsonValue.TYPE_BYTES);
            byte[] b = (byte[])val.getValue();
            int len = b.length;
            int tmp;
            int start = 0;
            do {
                tmp = len>255?255:len;
                os.write(tmp);
                os.write(b,start,tmp);
                start+=tmp;
                len-=tmp;
            } while(len>0);
            os.write(0);
        } else {
            os.write(val.getType());
            if(val.getType()<0x10) return;
            os.write(val.asBytes());
        }
    }
    protected void serializeAsonValueStructed(OutputStream os,AsonValue val) throws IOException {
        if(val.getType()==AsonValue.TYPE_OBJECT) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            java.util.Iterator<Integer> it = val.keyIterator();
            while(it.hasNext()) {
                Integer id = it.next();
                serializeId(baos, id);
                serializeAsonValueStructed(baos, val.get(id));
            }
            writeBytesWithTypeLength(os, AsonValue.TYPE_OBJECT, baos.toByteArray());
        } else if(val.getType()==AsonValue.TYPE_ARRAY) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            java.util.Iterator<AsonValue> it = val.iterator();
            while(it.hasNext()) {
                AsonValue v = it.next();
                serializeAsonValueStructed(baos, v);
            }
            writeBytesWithTypeLength(os, AsonValue.TYPE_ARRAY, baos.toByteArray());
        } else if(val.getType()==AsonValue.TYPE_BYTES) {
            writeBytesWithTypeLength(os, AsonValue.TYPE_BYTES, (byte[])val.getValue());
        } else if(val.getType()==AsonValue.TYPE_STRING) {
            writeBytesWithTypeLength(os, AsonValue.TYPE_STRING, ((String)val.getValue()).getBytes("UTF-8"));
        } else {
            os.write(val.getType());
            if(val.getType()<0x10) return;
            os.write(val.asBytes());
        }
    }
    
    protected int serializeId(OutputStream os, int id) throws IOException {
        int ret = 0;
        String name = null;
        if ((flag&0x0C)==0 && !stringTableRev.isEmpty()) {
            name = stringTableRev.get(id);
        }
        if(flagBigId) {
            os.write(Utils.Int322Bytes(id|(name!=null?0x80000000:0)));
            ret += 4;
        } else {
            os.write(Utils.Int162Bytes(id|(name!=null?0x8000:0)));
            ret += 2;
        }
        if(name!=null) {
            byte d[] = name.getBytes("UTF-8");
            os.write(d.length);
            os.write(d);
            ret += 1+d.length;
            stringTableRev.remove(id);
        }
        return ret;
    }
    
    protected byte[] serializeStringTable(java.util.Map<String,Integer> nameDic) throws java.io.IOException {
        ByteArrayOutputStream data = new ByteArrayOutputStream();
        java.util.Iterator<String> ndk = nameDic.keySet().iterator();
        while(ndk.hasNext()) {
            String  k = ndk.next();
            if(AsonValue.KEY_NEXT_IDVAL.equals(k)) continue;
            Integer v = nameDic.get(k);
            if(flagBigId) {
                data.write(Utils.Int322Bytes(v));
            } else {
                data.write(Utils.Int162Bytes(v));
            }
            byte b[] = k.getBytes("UTF-8");
            if(b.length>255) throw new RuntimeException("Key string length must be <= 255 bytes.");
            data.write(b.length);
            data.write(b);
        }
        return data.toByteArray();
    }

    protected void writeBytesWithTypeLength(OutputStream os,byte type,byte data[]) throws java.io.IOException {
        if(data.length>0xFFFF) { // 4 bytes
            os.write(type+4);
            os.write(Utils.Int322Bytes(data.length));
        } else if(data.length>0xFF) { // 2 bytes
            os.write(type+2);
            os.write(Utils.Int162Bytes(data.length));
        } else { // 1 byte
            os.write(type+1);
            os.write(data.length);
        }
        if(data.length>0) os.write(data);
    }
    
    protected int prepareDictMap(AsonValue v) {
        int max = 0;
        Iterator<String> it = v.getDictMap().keySet().iterator();
        while(it.hasNext()) {
            String k = it.next();
            if(AsonValue.KEY_NEXT_IDVAL.equals(k)) continue;
            Integer id = v.getDictMap().get(k);
            if(id>max) max=id;
            stringTableRev.put(id, k);
        }
        return max;
    }
}
