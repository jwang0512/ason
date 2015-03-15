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
package com.dizsoft.ason.utils;

/**
 *
 * @author DizSoft Inc. jwang@dizsoft.com
 */
public class Utils {

    /**
     * 将整数数值转换为双字节输出，低位在后（Little endian），Array2Int16的逆方法
     * @param sour 要转换的数值
     * @return 转化后的字节流
     */
    public static byte[] Int162Bytes(int sour) {
        byte dest[] = {(byte) ((sour & 0x0000FF00) >> 8), (byte) (sour & 0x000000FF)};
        return dest;
    }
    
    /**
     * 将双字节转换为无符号整数数值输出，低位在后，Int162Array的逆方法
     * @param sour 要转换的数值
     * @param offset 数据在sour中的偏移量
     * @return 转化后的值
     */
    public static int Bytes2Uint16(byte[] sour, int offset) {
        return (((sour[offset] << 8) & 0xFF00) | (sour[offset + 1] & 0x00FF));
    }
    
    /**
     * 将双字节转换为有符号整数数值输出，低位在后，Int162Array的逆方法
     * @param sour 要转换的数值
     * @param offset 数据在sour中的偏移量
     * @return 转化后的值
     */
    public static int Bytes2Int16(byte[] sour, int offset) {
        int ret = Bytes2Uint16(sour,offset);
        return ((ret&0x8000)==0x8000)?0xFFFF0000|ret:ret;
    }

    /**
     * 将整数数值转换为四字节输出，低位在后（Little endian）,Bytes2Int32的逆方法
     * @param sour 要转换的数值
     * @return 转化后的字节流
     */
    public static byte[] Int322Bytes(int sour) {
        byte dest[] = {(byte) ((sour & 0xFF000000) >> 24), (byte) ((sour & 0x00FF0000) >> 16), (byte) ((sour & 0x0000FF00) >> 8), (byte) (sour & 0x000000FF)};
        return dest;
    }

    /**
     * 将64位整数数值转换为八字节输出，低位在后（Little endian）,Bytes2Int64的逆方法
     * @param sour 要转换的数值
     * @return 转化后的字节流
     */
    public static byte[] Int642Bytes(long sour) {
        byte dest[] = {(byte) ((sour & 0xFF00000000000000L) >> 56), (byte) ((sour & 0x00FF000000000000L) >> 48), (byte) ((sour & 0x0000FF0000000000L) >> 40), (byte) ((sour & 0x000000FF00000000L) >> 32),
                       (byte) ((sour & 0x00000000FF000000L) >> 24), (byte) ((sour & 0x0000000000FF0000L) >> 16), (byte) ((sour & 0x000000000000FF00L) >> 8 ), (byte) (sour & 0x00000000000000FF)};
        return dest;
    }

    /**
     * 将四字节转换为32位整数数值输出，低位在后（Little endian），Int322Bytes的逆方法
     * @param sour 要转换的数值
     * @param offset 数据在sour中的偏移量
     * @return 转化后的值
     */
    public static int Bytes2Int32(byte[] sour, int offset) {
        return (((sour[offset] << 24) & 0xFF000000) | ((sour[offset + 1] << 16) & 0x00FF0000) | ((sour[offset + 2] << 8) & 0x0000FF00) | (sour[offset + 3] & 0x000000FF));
    }
    
    /**
     * 将八字节转换为64位整数数值输出，低位在后（Little endian），Int642Array的逆方法
     * @param sour 要转换的数值
     * @param offset 数据在sour中的偏移量
     * @return 转化后的值
     */
    public static long Bytes2Int64(byte[] sour, int offset) {
        return ((((long)sour[offset] << 56) & 0xFF00000000000000L) | (((long)sour[offset + 1] << 48) & 0x00FF000000000000L) | (((long)sour[offset + 2] << 40) & 0x0000FF0000000000L) | (((long)sour[offset + 3] << 32) & 0x000000FF00000000L) |
                ((sour[offset + 4] << 24) & 0x00000000FF000000L) | ((sour[offset + 5] << 16) & 0x0000000000FF0000L) | ((sour[offset + 6] << 8) & 0x000000000000FF00L) | (sour[offset + 7] & 0x00000000000000FFL));
    }

    /**
     * 将四字节转换为无符号整数数值输出，低位在后（Little endian），Int322Bytes的逆方法
     * @param sour 要转换的数值
     * @param offset 数据在sour中的偏移量
     * @return 转化后的值
     */
    public static long Bytes2Uint32(byte[] sour, int offset) {
        return Bytes2Int32(sour,offset)&0xFFFFFFFFL;
    }

}
