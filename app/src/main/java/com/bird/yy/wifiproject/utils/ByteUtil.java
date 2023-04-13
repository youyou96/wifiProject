package com.bird.yy.wifiproject.utils;

/**
 * Created by lixiao on 2017/10/14.
 */

public class ByteUtil
{


    private static final char[] HEX_CHAR = {'0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    /**
     * 格式化mac
     *
     * @param mac
     * @return
     */
    public static String formatMacByByte(byte[] mac) {
        StringBuffer sb = new StringBuffer();
        String strMac = "00:00:00:00:00:00";
        if (mac != null) {
            for (int i = 0; i < mac.length; i++) {
                sb.append(byteToHex(mac[i]));
                if (i != mac.length - 1) {
                    sb.append(":");
                }
            }
            strMac = sb.toString().trim();
        }
        return strMac;
    }


    /**
     * byte转int
     * （因为在int强制转换为byte型数据时，会产生一个-128~127的有符号字节，而不是read方法返回的0~255的无符号字节。）
     *
     * @param b
     * @return
     */
    public static int parseByte(byte b) {
        int intValue = 0;
        if (b >= 0) {
            intValue = b;
        } else {
            intValue = 256 + b;
        }
        return intValue;
    }


    //byte转换16进制字符串
    public static String byteToHex(byte b) {
        return numToHex8(parseByte(b)).toUpperCase();
    }

    //int转换16进制字符串：1字节表示b
    public static String numToHex8(int b) {
        return String.format("%02x", b);
    }

    //转换16进制字符串：2字节表示b
    public static String numToHex16(int b) {
        return String.format("%04x", b);
    }

    //转换16进制字符串：4字节表示b
    public static String numToHex32(int b) {
        return String.format("%08x", b);
    }

    //byte转int
    int ByteArrayToInt(byte[] bytes) {
        int addr = bytes[0] & 0xFF;
        addr |= ((bytes[1] << 8) & 0xFF00);
        addr |= ((bytes[2] << 16) & 0xFF0000);
        addr |= ((bytes[3] << 24) & 0xFF000000);
        return addr;
    }


    /**
     * 将int数值转换为占四个字节的byte数组，本方法适用于(低位在前，高位在后)的顺序。 和bytesToInt（）配套使用
     *
     * @param value 要转换的int值
     * @return byte数组
     */
    public static byte[] intToBytes(int value) {
        byte[] src = new byte[4];
        src[3] = (byte) ((value >> 24) & 0xFF);
        src[2] = (byte) ((value >> 16) & 0xFF);
        src[1] = (byte) ((value >> 8) & 0xFF);
        src[0] = (byte) (value & 0xFF);
        return src;
    }

    /**
     * 将int数值转换为占四个字节的byte数组，本方法适用于(高位在前，低位在后)的顺序。  和bytesToInt2（）配套使用
     */
    public static byte[] intToBytes2(int value) {
        byte[] src = new byte[4];
        src[0] = (byte) ((value >> 24) & 0xFF);
        src[1] = (byte) ((value >> 16) & 0xFF);
        src[2] = (byte) ((value >> 8) & 0xFF);
        src[3] = (byte) (value & 0xFF);
        return src;
    }

    /**
     * byte[]转String
     *
     * @param b
     * @param size
     * @return
     */
    public static String Bytes2HexString(byte[] b, int size) {
        String ret = "";
        for (int i = 0; i < size; i++) {
            String hex = Integer.toHexString(b[i] & 0xFF);
            if (hex.length() == 1) {
                hex = "0" + hex;
            }
            ret += hex.toUpperCase();
        }
        return ret;
    }

    /**
     * 方法二：
     * byte[] to hex string
     *
     * @param bytes
     * @return
     */
    public static String bytesToHexFun2(byte[] bytes) {
        char[] buf = new char[bytes.length * 2];
        int index = 0;
        for (byte b : bytes) { // 利用位运算进行转换，可以看作方法一的变种
            buf[index++] = HEX_CHAR[b >>> 4 & 0xf];
            buf[index++] = HEX_CHAR[b & 0xf];
        }

        return new String(buf);
    }

    /**
     * 方法三：
     * byte[] to hex string
     *
     * @param bytes
     * @return
     */
    public static String bytesToHexFun3(byte[] bytes) {
        StringBuilder buf = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) { // 使用String的format方法进行转换
            buf.append(String.format("%02x", new Integer(b & 0xff)));
        }

        return buf.toString();
    }


}
