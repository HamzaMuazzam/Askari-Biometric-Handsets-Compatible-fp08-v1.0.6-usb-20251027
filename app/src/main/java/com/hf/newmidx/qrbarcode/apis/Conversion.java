package com.hf.newmidx.qrbarcode.apis;


import java.io.ByteArrayOutputStream;

/**
 * 文件功能描述：数据转换工具类
 */
public class Conversion {
    /*
     * 16进制数字字符集
     */
    private static final String hexString = "0123456789ABCDEF";

    /*
     * 将字符串编码成16进制数字,适用于所有字符（包括中文）
     */
    public static String encode(String str) {
        //根据默认编码获取字节数组
        byte[] bytes = str.getBytes();
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        //将字节数组中每个字节拆解成2位16进制整数
        for (int i = 0; i < bytes.length; i++) {
            sb.append(hexString.charAt((bytes[i] & 0xf0) >> 4));
            sb.append(hexString.charAt((bytes[i] & 0x0f) >> 0));
        }
        return sb.toString();
    }


    /*
     * 将16进制数字解码成字符串,适用于所有字符（包括中文）
     */
    public static String decode(String bytes) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(bytes.length() / 2);
        //将每2位16进制整数组装成一个字节
        for (int i = 0; i < bytes.length(); i += 2)
            baos.write((hexString.indexOf(bytes.charAt(i)) << 4 | hexString.indexOf(bytes.charAt(i + 1))));
        return new String(baos.toByteArray());
    }

    public static byte uniteBytes(byte src0, byte src1) {
        byte _b0 = Byte.decode("0x" + new String(new byte[]{src0})).byteValue();
        _b0 = (byte) (_b0 << 4);
        byte _b1 = Byte.decode("0x" + new String(new byte[]{src1})).byteValue();
        byte ret = (byte) (_b0 ^ _b1);
        return ret;
    }

    public static byte[] HexString2Bytes(String src) {
        byte[] ret = new byte[src.length() / 2];
        byte[] tmp = src.getBytes();
        for (int i = 0; i < tmp.length / 2; i++) {
            ret[i] = uniteBytes(tmp[i * 2], tmp[i * 2 + 1]);
        }
        return ret;
    }


    public static void printHexString(String hint, byte[] b) {
        System.out.print(hint);
        for (int i = 0; i < b.length; i++) {
            String hex = Integer.toHexString(b[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            System.out.print(hex.toUpperCase() + " ");
        }
        System.out.println();
    }


    public static String Bytes2HexString(byte[] b) {
        String ret = "";
        for (int i = 0; i < b.length; i++) {
            String hex = Integer.toHexString(b[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            ret += hex.toUpperCase();
        }
        return ret;
    }


    public static String oneBytes2HexString(byte b) {
        String ret = "";
        String hex = Integer.toHexString(b & 0xFF);
        if (hex.length() == 1) {
            hex = '0' + hex;
        }
        ret += hex.toUpperCase();
        return ret;
    }


    public static String bcd2Str(byte[] bytes) {
        StringBuffer temp = new StringBuffer(bytes.length * 2);
        for (int i = 0; i < bytes.length; i++) {
            temp.append((byte) ((bytes[i] & 0xf0) >>> 4));
            temp.append((byte) (bytes[i] & 0x0f));
        }
        return temp.toString().substring(0, 1).equalsIgnoreCase("0") ? temp
                .toString().substring(1) : temp.toString();
    }


    // 取高字节
    public static byte GetHighByte(int data) {
        return (byte) (data >> 8);
    }

    // 取低字节
    public static byte GetLowByte(int data) {
        return (byte) (data & 0xff);
    }

    public static int makeWord(byte a, byte b) {
        //UInt16 i = H_bit;
        //i = i << 8;
        //但是你串口一次只能传送8个位，那么就分高字节低字节，一个字节8位，你先把高8位放在一个16位的变量里i = xx，然后执行i<<8；再i |= xx;这就合并了。你的i就是存了对方16位的数据

        return (a & 0xff) | (((b & 0xff) << 8));
    }


    public static byte[] splitInt(int num) {
        byte[] ret = new byte[4];
        for (int i = 0; i < 4; ++i) {
            ret[i] = (byte) (num >> (i * 8));
        }
        return ret;
    }

    public static int combineIntoInt(byte[] bytes) {
        int ret = 0;
        for (int i = 0; i < bytes.length; ++i) {
            ret += ((bytes[i] & 0xFF) << i * 8);
        }
        return ret;
    }

	public static int getIntFromBytes(byte high_h, byte high_l, byte low_h, byte low_l) {
		return (high_h & 0xff) << 24 | (high_l & 0xff) << 16 | (low_h & 0xff) << 8 | low_l & 0xff;
	}

}
