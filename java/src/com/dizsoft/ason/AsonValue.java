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

import com.dizsoft.ason.utils.Utils;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * AsonValue class, for holding ason data.
 * @author DizSoft Inc. jwang@dizsoft.com
 */
public final class AsonValue implements Iterable<AsonValue> {
    public static final byte TYPE_NULL   = 0x01;
    public static final byte TYPE_TRUE   = 0x02;
    public static final byte TYPE_FALSE  = 0x03;
    public static final byte TYPE_INT8   = 0x10;
    public static final byte TYPE_INT16  = 0x11;
    public static final byte TYPE_INT32  = 0x12;
    public static final byte TYPE_INT64  = 0x13;
    public static final byte TYPE_FLOAT  = 0x1E;
    public static final byte TYPE_DOUBLE = 0x1F;
    public static final byte TYPE_STRING  = (byte) 0xA0;
    public static final byte TYPE_BYTES   = (byte) 0xD0;
    public static final byte TYPE_ARRAY   = (byte) 0xE0;
    public static final byte TYPE_OBJECT  = (byte) 0xF0;
    
    public static final String KEY_NEXT_IDVAL = "_$NEXT_FREE_ID$_";
//    public static final AsonValue NULL_VALUE = new AsonValue(null);

    protected Object value;
    protected Map<String,Integer> keyNameDictMap;

    /**
     * Create AsonValue with value from Root node.
     * @param rootNode Root node, can not be null.
     * @param value init data, can be one of byte[],Byte,Short,Integer,Long,Float,Double,String,Boolean,List,Map.
     */
    public AsonValue(AsonValue rootNode,Object value) {
        if(rootNode==null) throw new IllegalArgumentException("rootNode can not be null.");
        this.keyNameDictMap = rootNode.getDictMap();
        setValue(value);
    }
    
    AsonValue(Map<String,Integer> dictMap, Object value) {
        if(dictMap==null) dictMap = new HashMap<>(16);
        this.keyNameDictMap = dictMap;
        setValue(value);
    }
    
    /**
     * get the dictionary map of this node.
     * @return Dictionary map.
     */
    public Map<String,Integer> getDictMap() {
        return keyNameDictMap;
    }

    /**
     * get the type of this node.
     * @return type. see AsonValue.TYPE_XXX
     */
    public byte getType() {
        if(value==null) return TYPE_NULL;
        else if(value instanceof byte[]) return TYPE_BYTES;
        else if(value instanceof Byte) return TYPE_INT8;
        else if(value instanceof Short) return TYPE_INT16;
        else if(value instanceof Integer) return TYPE_INT32;
        else if(value instanceof Long) return TYPE_INT64;
        else if(value instanceof Float) return TYPE_FLOAT;
        else if(value instanceof Double) return TYPE_DOUBLE;
        else if(value instanceof String) return TYPE_STRING;
        else if(value instanceof Boolean) return (Boolean)value?TYPE_TRUE:TYPE_FALSE;
        else if(value instanceof List) return TYPE_ARRAY;
        else if(value instanceof Map) return TYPE_OBJECT;
//        else if(value instanceof AsonObject) return ((AsonObject)value).isArray()?TYPE_ARRAY:TYPE_OBJECT;
        return -1;
    }

    /**
     * check if this node is an object type.
     * @return 
     */
    public boolean isObject() {
        return value instanceof Map;
//        return (value instanceof AsonObject) && !((AsonObject)value).isArray();
    }
    /**
     * check if this node is an array type.
     * @return 
     */
    public boolean isArray() {
        return value instanceof List;
//        return (value instanceof AsonObject) && ((AsonObject)value).isArray();
    }
    /**
     * check if this node is null type.
     * @return 
     */
    public boolean isNull() {
        return value==null;
    }
 
//    public AsonObject getAsonObject() {
//        return value instanceof AsonObject?(AsonObject)value:null;
//    }
    
    /**
     * return the byte[] representation of this node
     * @return 
     */
    public byte[] asBytes() {
        if(value instanceof byte[]) {
            return (byte[])value;
        } else if(value instanceof Byte) {
            return new byte[]{(Byte)value};
        } else if(value instanceof Short) {
            return Utils.Int162Bytes((Short)value);
        } else if(value instanceof Integer) {
            return Utils.Int322Bytes((Integer)value);
        } else if(value instanceof Long) {
            return Utils.Int642Bytes((Long)value);
        } else if(value instanceof Float) {
            return Utils.Int322Bytes(Float.floatToIntBits((Float)value));
        } else if(value instanceof Double) {
            return Utils.Int642Bytes(Double.doubleToLongBits((Double)value));
        } else if(value instanceof String) {
            try { return ((String)value).getBytes("UTF-8"); }catch(Exception e){}
        }
        return null;
    }
    /**
     * return the Integer representation of this node
     * @return 
     */
    public Integer asInteger() {
        if(value instanceof Number) {
            if(((Number)value).longValue()<=Integer.MAX_VALUE) {
                return ((Number)value).intValue();
            }
        } else if(value instanceof Boolean) {
            return (Boolean)value?1:0;
        } else if(value instanceof String) {
            Number n = parseNumber((String)value);
            if(n!=null && ((Number)value).longValue()<=Integer.MAX_VALUE) return n.intValue();
        }
        return null;
    }
    /**
     * return the Long representation of this node
     * @return 
     */
    public Long asInteger64() {
        if(value instanceof Number) {
            return ((Number)value).longValue();
        } else if(value instanceof Boolean) {
            return (Boolean)value?1L:0L;
        } else if(value instanceof String) {
            Number n = parseNumber((String)value);
            if(n!=null) return n.longValue();
        }
        return null;
    }
    /**
     * return the Float representation of this node
     * @return 
     */
    public Float asFloat() {
        if(value instanceof Number) {
            return ((Number)value).floatValue();
        } else if(value instanceof Boolean) {
            return (Boolean)value?1.0f:0.0f;
        } else if(value instanceof String) {
            Number n = parseNumber((String)value);
            if(n!=null) return n.floatValue();
        }
        return null;
    }
    /**
     * return the Double representation of this node
     * @return 
     */
    public Double asDouble() {
        if(value instanceof Number) {
            return ((Number)value).doubleValue();
        } else if(value instanceof Boolean) {
            return (Boolean)value?1.0:0.0;
        } else if(value instanceof String) {
            Number n = parseNumber((String)value);
            if(n!=null) return n.doubleValue();
        }
        return null;
    }
    /**
     * return the String representation of this node
     * @return 
     */
    public String asString() {
        if(value instanceof String) {
            return (String)value;
        } else if(value instanceof Integer || value instanceof Long || value instanceof Float || value instanceof Double) {
            return value.toString();
        } else if(value instanceof Boolean) {
            return value.toString();
        }
        return null;
    }
    
    private Number parseNumber(String string) {
        char b = string.charAt(0);
        if ((b >= '0' && b <= '9') || b == '-') {
            try {
                if (string.indexOf('.') > -1 || string.indexOf('e') > -1 || string.indexOf('E') > -1) {
                    Double d = Double.valueOf(string);
                    if (!d.isInfinite() && !d.isNaN()) {
                        return d;
                    }
                } else {
                    Long myLong = new Long(string);
                    if (string.equals(myLong.toString())) {
                        if (myLong == myLong.intValue()) {
                            return myLong.intValue();
                        } else {
                            return myLong;
                        }
                    }
                }
            } catch (Exception ignore) {
            }
        }
        return null;
    }
    
    private Number minsizedNum(Number n) {
        if(n instanceof Short) {
            Short s = (Short)n;
            if(s.byteValue()==s) return s.byteValue();
        } else if(n instanceof Integer) {
            Integer i = (Integer)n;
            if(i.byteValue()==i) return i.byteValue();
            else if(i.shortValue()==i) return i.shortValue();
        } else if(n instanceof Long) {
            Long l = (Long)n;
            if(l.byteValue()==l) return l.byteValue();
            else if(l.shortValue()==l) return l.shortValue();
            else if(l.intValue()==l) return l.intValue();
        } else if(n instanceof BigDecimal) {
            BigDecimal b = (BigDecimal)n;
            try { return b.byteValueExact(); }catch(Exception e){}
            try { return b.shortValueExact(); }catch(Exception e){}
            try { return b.intValueExact(); }catch(Exception e){}
            try { return b.longValueExact(); }catch(Exception e){}
            if(b.toString().equals(Float.toString(b.floatValue()))) return b.floatValue();
//            if(b.floatValue()==b.doubleValue()) return b.floatValue();
            return b.doubleValue();
        } else if(n instanceof BigInteger) {
            BigInteger b = (BigInteger)n;
            long bl = b.longValue();
            if(bl==b.byteValue()) return b.byteValue();
            if(bl==b.shortValue()) return b.shortValue();
            if(bl==b.intValue()) return b.intValue();
            return bl;
        } else if(n instanceof Double) {
            Double b = (Double)n;
//            if(b.floatValue()==b.doubleValue()) return new Float(b.floatValue());
            if(b.toString().equals(Float.toString(b.floatValue()))) return b.floatValue();
        }
        return n;
    }
    
    /**
     * get the origin data of this node.
     * @return 
     */
    public Object getValue() {
        return value;
    }
    
    /**
     * set the value of this node.
     * @param value can be one of byte[],Byte,Short,Integer,Long,Float,Double,String,Boolean,List,Map.
     */
    public void setValue(Object value) {
        if(value==null) {
        } else if(value instanceof Number) {
            value = minsizedNum((Number)value);
        } else if((value instanceof AsonArray) || (value instanceof AsonObject) || (value instanceof AsonObjectOrdered)) {
        } else if(value instanceof List) {
            AsonArray nl = new AsonArray(((List)value).size());
            Iterator it = ((List)value).iterator();
            while(it.hasNext()) {
                Object o = it.next();
                if(o instanceof AsonValue) {
                    nl.add((AsonValue)o);
                } else {
                    nl.add(new AsonValue(this, o));
                }
            }
            value = nl;
        } else if(value instanceof Map) {
            Map<Integer,AsonValue> nm;
            if(value instanceof LinkedHashMap) {
                nm = new AsonObject(((Map)value).size());
            } else {
                nm = new AsonObjectOrdered(((Map)value).size());
            }
            Iterator it = ((Map)value).keySet().iterator();
            while(it.hasNext()) {
                Object k = it.next();
                Integer nk;
                if(k instanceof Integer) {
                    nk = (Integer)k;
                } else {
                    String sid = k.toString();
                    nk = keyNameDictMap.get(sid);
                    if(nk==null) {
                        nk = nextFreeId();
                        keyNameDictMap.put(sid, nk);
                    }
                }
                Object v = ((Map)value).get(k);
                if(!(v instanceof AsonValue)) {
                    v = new AsonValue(this, v);
                }
                nm.put(nk, (AsonValue)v);
            }
            value = nm;
        } else if(value instanceof AsonValue) {
            this.value = ((AsonValue)value).getValue();
        } else if(!(value instanceof String) && !(value instanceof byte[]) && !(value instanceof Boolean)) {
            throw new IllegalArgumentException("Invalid value type:"+value.getClass().getName()+", accpeted type: byte[],Boolean,String,Number,Map,List and subclasses.");
        }
        this.value = value;
    }
    
    /**
     * Add data into this node. this node must be object type.
     * @param id key of the added data
     * @param obj data to be added.can be one of byte[],Byte,Short,Integer,Long,Float,Double,String,Boolean,List,Map.
     */
    public void add(String id,Object obj) {
        if(obj instanceof AsonValue) {
            add(id,(AsonValue)obj);
        } else {
            add(id, new AsonValue(this, obj));
        }
    }
    /**
     * Add an AsonValue into this node. this node must be object type.
     * @param id key of the added data
     * @param obj AsnonValue
     */
    public void add(String id,AsonValue obj) {
        Integer iid = keyNameDictMap.get(id);
        if(iid==null) {
            iid = nextFreeId();
            keyNameDictMap.put(id, iid);
        }
        add(iid, obj);
    }
    
    protected int nextFreeId() {
        Integer nid = keyNameDictMap.get(KEY_NEXT_IDVAL);
        if(nid==null) {
            nid = 0;
            Iterator<Integer> it = keyNameDictMap.values().iterator();
            while(it.hasNext()) {
                int id = it.next();
                if(id>nid) nid=id;
            }
            ++nid;
        }
        keyNameDictMap.put(KEY_NEXT_IDVAL, nid+1);
        return nid;
    }
    /**
     * Add data into this node. this node must be object or array type.
     * @param id int key when this node is object type or index if this node is array type.
     * @param obj data to be added.can be one of byte[],Byte,Short,Integer,Long,Float,Double,String,Boolean,List,Map.
     */
    public void add(int id,Object obj) {
        if(value instanceof AsonValue) {
            add(id,(AsonValue)obj);
        } else {
            add(id,new AsonValue(this, obj));
        }
    }
    /**
     * Add AsonValue into this node. this node must be object or array type.
     * @param id int key when this node is object type or index if this node is array type.
     * @param obj AsonValue to be added.
     */
    public void add(int id,AsonValue obj) {
        if(value instanceof Map) {
            ((Map<Integer,AsonValue>)value).put(id, obj);
        } else if(value instanceof List) {
            ((List<AsonValue>)value).add(id, obj);
        }
    }
    /**
     * Add data to the end of this array type node. this node must be array type.
     * @param obj data to be added.can be one of byte[],Byte,Short,Integer,Long,Float,Double,String,Boolean,List,Map.
     */
    public void add(Object obj) {
        if(obj instanceof AsonValue) {
            add((AsonValue)obj);
        } else {
            add(new AsonValue(this, obj));
        }
    }
    /**
     * Add AsonValue to the end of this array type node. this node must be array type.
     * @param obj AsonValue to be added.
     */
    public void add(AsonValue obj) {
        if(value instanceof List) {
            ((List<AsonValue>)value).add(obj);
        }
    }
    /**
     * Remove the data with key id if this node is object or index if this node is array from this node.
     * @param id key id if this nod is object or index if this node is array.
     */
    public void remove(int id) {
        if(value instanceof Map) {
            ((Map<Integer,AsonValue>)value).remove(id);
        } else if(value instanceof List) {
            ((List<AsonValue>)value).remove(id);
        }
    }
    /**
     * Get AsonValue from this node. this node must be array or object type.
     * @param id key id if this nod is object or index if this node is array.
     * @return AsonValue if found. or null.
     */
    public AsonValue get(int id) {
        if(value instanceof Map) {
            return ((Map<Integer,AsonValue>)value).get(id);
        } else if(value instanceof List) {
            return ((List<AsonValue>)value).get(id);
        }
        return null;
    }
    /**
     * Get the key iterator of this node. this node must be object type.
     * @return 
     */
    public Iterator<Integer> keyIterator() {
        if(value instanceof Map) {
            return ((Map<Integer,AsonValue>)value).keySet().iterator();
        }
        return null;
    }

    /**
     * Get the value iterator of this node. this node must be object or array type.
     * @return 
     */
    @Override
    public Iterator<AsonValue> iterator() {
        if(value instanceof Map) {
            return ((Map<Integer,AsonValue>)value).values().iterator();
        } else if(value instanceof List) {
            return ((List<AsonValue>)value).iterator();
        }
        return null;
    }
    /**
     * Get thet size of this node.
     * @return child count if this node is array or object type. length if this node is byte[] or String.
     */
    public int size() {
        if(value instanceof Map) {
            return ((Map<Integer,AsonValue>)value).size();
        } else if(value instanceof List) {
            return ((List<AsonValue>)value).size();
        } else if(value instanceof byte[]) {
            return ((byte[])value).length;
        } else if(value instanceof String) {
            return ((String)value).length();
        }
        return 0;
    }


    public static class AsonArray extends ArrayList<AsonValue> {
        public AsonArray() { super(); }
        public AsonArray(int initialCapacity) { super(initialCapacity); }
    }
    public static class AsonObject extends HashMap<Integer, AsonValue> {
        public AsonObject() { super(); }
        public AsonObject(int initialCapacity) { super(initialCapacity); }
    }
    public static class AsonObjectOrdered extends LinkedHashMap<Integer, AsonValue> {
        public AsonObjectOrdered() { super(); }
        public AsonObjectOrdered(int initialCapacity) { super(initialCapacity); }
    }
}
