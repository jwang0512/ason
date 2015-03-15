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

import com.dizsoft.ason.Ason;
import com.dizsoft.ason.AsonValue;
import com.dizsoft.ason.utils.Utils;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author DizSoft Inc. jwang@dizsoft.com
 */
public class AsonParser {
    public static final int FLAG_ASON          = 0x80000000;
    public static final int FLAG_BIGID         = 0x40000000;

    public static final byte TYPE_STRING1 = (byte) 0xA1;
    public static final byte TYPE_STRING2 = (byte) 0xA2;
    public static final byte TYPE_STRING4 = (byte) 0xA4;
    public static final byte TYPE_BYTES1  = (byte) 0xD1;
    public static final byte TYPE_BYTES2  = (byte) 0xD2;
    public static final byte TYPE_BYTES4  = (byte) 0xD4;
    public static final byte TYPE_ARRAY1  = (byte) 0xE1;
    public static final byte TYPE_ARRAY2  = (byte) 0xE2;
    public static final byte TYPE_ARRAY4  = (byte) 0xE4;
    public static final byte TYPE_OBJECT1 = (byte) 0xF1;
    public static final byte TYPE_OBJECT2 = (byte) 0xF2;
    public static final byte TYPE_OBJECT4 = (byte) 0xF4;

    protected Map<String,Integer> dictMap = new HashMap<>();
    protected AsonValue rootNode;
    protected boolean flagBigId;
    protected int dictMode;

    public AsonValue parse(InputStream asonInputStream) throws java.io.IOException {
        byte tmp[] = new byte[4];
        DataInputStream dis = new DataInputStream(asonInputStream);

        dis.readFully(tmp,0,4);
        long tas = Utils.Bytes2Uint32(tmp, 0);
        if((tas&FLAG_ASON)==0) return null;
        flagBigId = (tas&FLAG_BIGID)!=0;
        tas &= 0x0FFFFFFF;
//        len-=tas;
        while(tas>0) {
            Integer id;
            if(flagBigId) {
                dis.readFully(tmp, 0, 4);
                tas-=4;
                id = Utils.Bytes2Int32(tmp, 0);
            } else {
                dis.readFully(tmp, 0, 2);
                tas-=2;
                id = Utils.Bytes2Int16(tmp, 0);
            }
            int len = dis.read();
            byte d[] = new byte[len];
            dis.readFully(d);
            tas-=1+len;
            dictMap.put(new String(d,"UTF-8"), id);
        }

        this.rootNode = Ason.CreateRootObject(this.dictMap);
        return parseValue(dis,new Param());
    }

    private AsonValue parseValue(DataInputStream dis, Param p) throws IOException {
        byte type = dis.readByte();
        if(p!=null) p.i = 1;
        if(type==AsonValue.TYPE_NULL) {
            return new AsonValue(rootNode,null);
        } else if(type==AsonValue.TYPE_TRUE) {
            return new AsonValue(rootNode, true);
        } else if(type==AsonValue.TYPE_FALSE) {
            return new AsonValue(rootNode, false);
        } else if(type==AsonValue.TYPE_INT8) {
            if(p!=null) p.i += 1;
            return new AsonValue(rootNode, dis.readByte());
        } else if(type==AsonValue.TYPE_INT16) {
            byte tmp[] = new byte[2];
            dis.readFully(tmp,0,2);
            if(p!=null) p.i += 2;
            return new AsonValue(rootNode, Utils.Bytes2Int16(tmp, 0));
        } else if(type==AsonValue.TYPE_INT32) {
            byte tmp[] = new byte[4];
            dis.readFully(tmp,0,4);
            if(p!=null) p.i += 4;
            return new AsonValue(rootNode, Utils.Bytes2Int32(tmp, 0));
        } else if(type==AsonValue.TYPE_INT64) {
            byte tmp[] = new byte[8];
            dis.readFully(tmp,0,8);
            if(p!=null) p.i += 8;
            return new AsonValue(rootNode, Utils.Bytes2Int64(tmp, 0));
        } else if(type==AsonValue.TYPE_FLOAT) {
            byte tmp[] = new byte[4];
            dis.readFully(tmp, 0, 4);
            if(p!=null) p.i += 4;
            return new AsonValue(rootNode, Float.intBitsToFloat(Utils.Bytes2Int32(tmp, 0)));
        } else if(type==AsonValue.TYPE_DOUBLE) {
            byte tmp[] = new byte[8];
            dis.readFully(tmp, 0, 8);
            if(p!=null) p.i += 8;
            return new AsonValue(rootNode, Double.longBitsToDouble(Utils.Bytes2Int64(tmp, 0)));
        } else if(type==AsonValue.TYPE_BYTES) {
            byte tmp[] = new byte[255];
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int n;
            do {
                n = dis.read();
                if(p!=null) p.i += 1;
                if(n>0) {
                    dis.readFully(tmp, 0, n);
                    baos.write(tmp, 0, n);
                    if(p!=null) p.i += n;
                }
            } while(n!=0);
            return new AsonValue(rootNode, baos.toByteArray());
        } else if(type==TYPE_BYTES1 || type==TYPE_BYTES2 || type==TYPE_BYTES4) {
            int len = readLen(dis, type-AsonValue.TYPE_BYTES);
            if(p!=null) p.i += type-AsonValue.TYPE_BYTES + len;
            byte tmp[] = new byte[len];
            dis.readFully(tmp);
            return new AsonValue(rootNode, tmp);
        } else if(type==AsonValue.TYPE_STRING) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int v;
            do {
                v = dis.read();
                if(p!=null) ++p.i;
                if(v!=0) baos.write(v);
            } while(v!=0);
            return new AsonValue(rootNode, new String(baos.toByteArray(),"UTF-8"));
        } else if(type==TYPE_STRING1 || type==TYPE_STRING2 || type==TYPE_STRING4) {
            int len = readLen(dis, type-AsonValue.TYPE_STRING);
            if(p!=null) p.i += type-AsonValue.TYPE_STRING + len;
            byte tmp[] = new byte[len];
            dis.readFully(tmp);
            return new AsonValue(rootNode,new String(tmp,"UTF-8"));
        } else if(type==AsonValue.TYPE_ARRAY) {
            AsonValue.AsonArray l = new AsonValue.AsonArray();
            AsonValue v;
            Param lp = new Param();
            do {
                v = parseValue(dis,lp);
                if(p!=null) p.i += lp.i;
                if(v!=null) l.add(v);
            } while(v!=null);
            return new AsonValue(rootNode, l);
        } else if(type==TYPE_ARRAY1 || type==TYPE_ARRAY2 || type==TYPE_ARRAY4) {
            int len = readLen(dis, type-AsonValue.TYPE_ARRAY);
            if(p!=null) p.i += type-AsonValue.TYPE_ARRAY + len;
            AsonValue.AsonArray l = new AsonValue.AsonArray();
            Param lp = new Param();
            while(len>0) {
                l.add(parseValue(dis,lp));
                len -= lp.i;
            }
            return new AsonValue(rootNode, l);
        } else if(type==AsonValue.TYPE_OBJECT) {
            AsonValue.AsonObjectOrdered m = new AsonValue.AsonObjectOrdered();
            Param lp = new Param();
            int id;
            do {
                id = parseId(dis , lp);
                if(p!=null) p.i += lp.i;
                if(id!=0) {
                    m.put(id, parseValue(dis,lp));
                    if(p!=null) p.i += lp.i;
                }
            } while(id!=0);
            return new AsonValue(rootNode, m);
        } else if(type==TYPE_OBJECT1 || type==TYPE_OBJECT2 || type==TYPE_OBJECT4) {
            int len = readLen(dis, type-AsonValue.TYPE_OBJECT);
            if(p!=null) p.i += type-AsonValue.TYPE_OBJECT + len;
            AsonValue.AsonObjectOrdered m = new AsonValue.AsonObjectOrdered();
            Param lp = new Param();
            while(len>0) {
                int id = parseId(dis, lp);
                len -= lp.i;
                AsonValue v = parseValue(dis, lp);
                len -= lp.i;
                m.put(id, v);
            }
            return new AsonValue(rootNode, m);
        }
        return null;
    }
    
    protected int parseId(DataInputStream dis, Param p) throws IOException {
        int ret;
        if(flagBigId) {
            byte tmp[] = new byte[4];
            dis.readFully(tmp);
            ret = Utils.Bytes2Int32(tmp, 0);
            if(p!=null) p.i = 4;
        } else {
            byte tmp[] = new byte[2];
            dis.readFully(tmp);
            ret = Utils.Bytes2Int16(tmp, 0);
            if(p!=null) p.i = 2;
        }
        if((ret&(flagBigId?0x80000000:0x8000))!=0) {
            ret &= flagBigId?0x7FFFFFFF:0x7FFF;
            int len = dis.read();
            byte nb[] = new byte[len];
            dis.readFully(nb);
            dictMap.put(new String(nb,"UTF-8"),ret);
            if(p!=null) p.i = 1+len;
        }
        return ret;
    }
    
    protected int readLen(DataInputStream dis,int sl) throws IOException {
        if(sl==1) {
            return dis.read();
        } else if(sl==2) {
            byte b[] = new byte[2];
            dis.readFully(b);
            return Utils.Bytes2Int16(b, 0);
        } else if(sl==4) {
            byte b[] = new byte[4];
            dis.readFully(b);
            return Utils.Bytes2Int32(b, 0);
        }
        return 0;
    }
    
    protected class Param {
        int i;
        Param() { i = 0; }
    }
}
