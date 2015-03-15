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
package com.dizsoft.ason;

import com.dizsoft.ason.parser.AsonParser;
import com.dizsoft.ason.parser.JsonParser;
import com.dizsoft.ason.serializer.AsonSerializer;
import com.dizsoft.ason.serializer.JsonSerializer;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Ason Helper class for create, parse and serialize.
 * Your will need fastjson-1.2.4.jar if you need json parse,serialize support.
 * @author DizSoft Inc. jwang@dizsoft.com
 */
public class Ason {
    /**
     * output as ason data format. (DEFAULT)
     */
    public static final int FLAG_OUTPUTASON    = 0x00;
    /**
     * output as ason json data format.
     */
    public static final int FLAG_OUTPUTJSON    = 0x80;
    /**
     * output ason with stream mode. (DEFAULT)
     */
    public static final int FLAG_MODESTREAM    = 0x00;
    /**
     * output ason with struct mode.
     */
    public static final int FLAG_MODESTRUCT    = 0x01;
    /**
     * output ason with inline dictionary. (DEFAULT)
     */
    public static final int FLAG_DICTINLINE    = 0x00;
    /**
     * output ason with dictionary headed.
     */
    public static final int FLAG_DICTHEAD      = 0x04;
    /**
     * output ason without dictionary.
     */
    public static final int FLAG_DICTNONE      = 0x08;
    /**
     * output ason with bigid.
     */
    public static final int FLAG_FORCEBIGID    = 0x02;
    
    /**
     * output json with pretty format.
     */
    public static final int FLAG_JSONPRETTYFORMAT = 0x81;

    protected static final int DEFAULT_INITIAL_CAPACITY = 16;

    /**
     * Create a new Root object type AsonValue
     * @return created AsonValue of object type
     */
    public static AsonValue CreateRootObject() {
        return CreateRootObject(null);
    }
    
    /**
     * Create a new unordered Root object type AsonValue with dict map
     * @param dictMap the dict map,can be null
     * @return created AsonValue of object type
     */
    public static AsonValue CreateRootObject(Map<String,Integer> dictMap) {
        return CreateRootObject(dictMap,false);
    }
    /**
     * Create a new ordered/unordered Root object type AsonValue with dict map
     * @param dictMap the dict map,can be null
     * @param ordered ordered/unordered
     * @return created AsonValue of object type
     */
    public static AsonValue CreateRootObject(Map<String,Integer> dictMap,boolean ordered) {
        return CreateRootObject(dictMap,ordered,DEFAULT_INITIAL_CAPACITY);
    }
    /**
     * Create a new ordered/unordered Root object type AsonValue with dict map and init capacity
     * @param dictMap the dict map,can be null
     * @param ordered ordered/unordered
     * @param initCapacity init capacity
     * @return created AsonValue of object type
     */
    public static AsonValue CreateRootObject(Map<String,Integer> dictMap,boolean ordered,int initCapacity) {
        Map<Integer,AsonValue> val;
        if(ordered) {
            val = new LinkedHashMap<>(initCapacity);
        } else {
            val = new HashMap<>(initCapacity);
        }
        return new AsonValue(dictMap,val);
    }
    /**
     * Create a new Root array type AsonValue
     * @return created AsonValue of array type
     */
    public static AsonValue CreateRootArray() {
       return CreateRootArray(null);
    }
    /**
     * Create a new Root array type AsonValue with dict map
     * @param dictMap the dict map,can be null
     * @return created AsonValue of array type
     */
    public static AsonValue CreateRootArray(Map<String,Integer> dictMap) {
        return CreateRootArray(dictMap,DEFAULT_INITIAL_CAPACITY);
    }
    /**
     * Create a new Root array type AsonValue with dict map
     * @param dictMap the dict map,can be null
     * @param initCapacity
     * @return created AsonValue of array type
     */
    public static AsonValue CreateRootArray(Map<String,Integer> dictMap,int initCapacity) {
        return new AsonValue(dictMap, new ArrayList<AsonValue>(initCapacity));
    }

    /**
     * Create AsonValue from inputstream
     * @param inputStream inputstream of ason/json data
     * @return AsonValue created
     * @throws java.io.IOException 
     */
    public static AsonValue CreateFrom(InputStream inputStream) throws java.io.IOException {
        class MIS extends InputStream {
            InputStream is;
            int firstByte;
            boolean firstRead = true;
            MIS(InputStream is) throws java.io.IOException { this.is = is; firstByte=is.read(); }
            @Override
            public int read() throws IOException { if(firstRead){firstRead=false;return firstByte;} return is.read(); }
            public byte getFirstByte() { return (byte)(firstByte&0xFF); }
        }
        MIS is = new MIS(inputStream);
        if((is.getFirstByte()&0x80)!=0) return new AsonParser().parse(is);
        return new JsonParser().parse(is);
    }
    /**
     * Create AsonValue from byte array of ason/json data
     * @param data byte array of ason/json data
     * @return AsonValue created
     * @throws java.io.IOException 
     */
    public static AsonValue CreateFrom(byte data[]) throws java.io.IOException {
        if((data[0]&0x80)!=0) return new AsonParser().parse(new ByteArrayInputStream(data));
        return new JsonParser().parse(new String(data,"UTF-8"));
    }
    /**
     * Serialize AsonValue to outputstream
     * @param obj AsonValue to be serialized
     * @param os outputstream to be write to
     * @param flag output flag. see Ason.FLAG_XXX
     * @throws java.io.IOException 
     */
    public static void Serialize(AsonValue obj,OutputStream os, int flag) throws java.io.IOException {
        new AsonSerializer(flag).serialize(obj, os);
    }
    /**
     * Serialize AsonValue to byte array.
     * @param obj AsonValue to be serialized
     * @param flag output flag. see Ason.FLAG_XXX
     * @return byte array.
     * @throws java.io.IOException 
     */
    public static byte[] ToBytes(AsonValue obj,byte flag) throws java.io.IOException {
        return new AsonSerializer(flag).serialize(obj);
    }
    /**
     * Serialize AsonValue to json string.
     * @param obj AsonValue to be serialized
     * @param format return formated json string or not.
     * @return json string.
     */
    public static String ToJsonString(AsonValue obj,boolean format) {
        return new JsonSerializer().serialize(obj,format);
    }
    
    public static void main(String[] args) throws Exception {
//        AsonValue v = CreateFrom(new java.io.FileInputStream("/Users/jwang/Desktop/ason_test/widget.json"));
//        Serialize(v, new java.io.FileOutputStream("/Users/jwang/Desktop/ason_test/widget.ason"), 0);
        AsonValue v = CreateFrom(new java.io.FileInputStream("/Users/jwang/Desktop/ason_test/widget.ason"));
        System.out.println(ToJsonString(v, true));
    }
}
