package com.bird.yy.wifiproject.utils;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;

import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class StringTextUtils
{

    /**
     * @param text
     * @param color
     * @param dipTextSize
     * @param textStyle   字体（正常、斜体、加粗）in(@link Typeface.NORMAL)
     * @return
     */
    public static SpannableString getSpannableString(String text, int color, int dipTextSize, int textStyle) {
        SpannableString spanText = new SpannableString(text);
        //设置文本颜色
        spanText.setSpan(new ForegroundColorSpan(color), 0, spanText.length(),
                Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        //文本大小
        spanText.setSpan(new AbsoluteSizeSpan(dipTextSize, true), 0, spanText.length(),
                Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        //字体（正常）
        StyleSpan span = new StyleSpan(textStyle);
        spanText.setSpan(span, 0, spanText.length(),
                Spannable.SPAN_INCLUSIVE_EXCLUSIVE);

        return spanText;

    }


    public static SpannableString getSpannableString(String text, int start, int end, int color, int dipTextSize, int textStyle) {
        SpannableString spanText = new SpannableString(text);
        //设置文本颜色
        spanText.setSpan(new ForegroundColorSpan(color), start, end,
                Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        //文本大小
        spanText.setSpan(new AbsoluteSizeSpan(dipTextSize, true), start, end,
                Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        //字体（正常）
        StyleSpan span = new StyleSpan(textStyle);
        spanText.setSpan(span, start, end,
                Spannable.SPAN_INCLUSIVE_EXCLUSIVE);

        return spanText;

    }


    /**
     * 省份添加后缀
     *
     * @param province
     * @return
     */
    public static String addProvinceSuffix(String province) {
        String result = "" + province;
        if (!TextUtils.isEmpty(province) && !province.contains("省") && !province.contains("市") && !province.contains("自治区")) {
            if (province.matches("内蒙古|西藏+")) {
                result = province + "自治区";
            } else if (province.contains("广西")) {
                result = province + "壮族自治区";
            } else if (province.contains("宁夏")) {
                result = province + "回族自治区";
            } else if (province.contains("新疆")) {
                result = province + "维吾尔自治区";
            } else if (province.contains("北京")) {
                result = province + "市";
            } else if (province.contains("天津")) {
                result = province + "市";
            } else if (province.contains("上海")) {
                result = province + "市";
            } else if (province.contains("重庆")) {
                result = province + "市";
            } else if (province.contains("香港")) {
                result = province + "特别行政区";
            } else if (province.contains("澳门")) {
                result = province + "特别行政区";
            } else {
                result = province + "省";

            }
        }

        return result;
    }


    public static String getUTF8StringFromGBKString(String gbkStr) {
        try {
            return new String(getUTF8BytesFromGBKString(gbkStr), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new InternalError();
        }
    }

    public static byte[] getUTF8BytesFromGBKString(String gbkStr) {
        int n = gbkStr.length();
        byte[] utfBytes = new byte[3 * n];
        int k = 0;
        for (int i = 0; i < n; i++) {
            int m = gbkStr.charAt(i);
            if (m < 128 && m >= 0) {
                utfBytes[k++] = (byte) m;
                continue;
            }
            utfBytes[k++] = (byte) (0xe0 | (m >> 12));
            utfBytes[k++] = (byte) (0x80 | ((m >> 6) & 0x3f));
            utfBytes[k++] = (byte) (0x80 | (m & 0x3f));
        }
        if (k < utfBytes.length) {
            byte[] tmp = new byte[k];
            System.arraycopy(utfBytes, 0, tmp, 0, k);
            return tmp;
        }
        return utfBytes;
    }

    public static String getEncoding(String str) {
        String encode = "GB2312";
        try {
            if (str.equals(new String(str.getBytes(encode), encode))) {
                String s = encode;
                return s;
            }
        } catch (Exception exception) {
        }
        encode = "ISO-8859-1";
        try {
            if (str.equals(new String(str.getBytes(encode), encode))) {
                String s1 = encode;
                return s1;
            }
        } catch (Exception exception1) {
        }
        encode = "UTF-8";
        try {
            if (str.equals(new String(str.getBytes(encode), encode))) {
                String s2 = encode;
                return s2;
            }
        } catch (Exception exception2) {
        }
        encode = "GBK";
        try {
            if (str.equals(new String(str.getBytes(encode), encode))) {
                String s3 = encode;
                return s3;
            }
        } catch (Exception exception3) {
        }
        return "";
    }

    /**
     * 是否是GBK编码格式（true：gbk ；false：utf-8）
     *
     * @param strBytes
     * @return
     */
    public static boolean isGBKString(byte[] strBytes) {
        boolean isGBK = false;
        try {
            String str = new String(strBytes, "UTF-8");
//            System.out.println("原始数据--byte-->"+ByteUtil.bytesToHexFun2(strBytes));
//            System.out.println("转码数据--byte-->"+ByteUtil.bytesToHexFun2(str.getBytes("UTF-8")));
            boolean isSameLength = str.getBytes().length == strBytes.length;
            if (isSameLength) {
                for (int i = 0; i < strBytes.length; i++) {
                    if (strBytes[i] != str.getBytes("UTF-8")[i]) {//不等
                        return true;
                    }
                }
                return false;
            } else {//不等，编码不同
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            String str = new String(strBytes, "GBK");
            boolean isSameLength = str.getBytes().length == strBytes.length;
            if (isSameLength) {
                for (int i = 0; i < strBytes.length; i++) {
                    if (strBytes[i] != str.getBytes("GBK")[i]) {//不等
                        return false;
                    }
                }
                return true;
            } else {//不等，编码不同
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            isGBK = false;
        }
//        System.out.println("isGBK--->"+isGBK);
        return isGBK;
    }


    /**
     * 判断字符是否是中文
     *
     * @param c 字符
     * @return 是否是中文
     */
    public static boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
            return true;
        }
        return false;
    }

    /**
     * 判断字符串是否是乱码
     *
     * @param strName 字符串
     * @return 是否是乱码
     */
    public static boolean isMessyCode(String strName) {
        Pattern p = Pattern.compile("\\s*|t*|r*|n*");
        Matcher m = p.matcher(strName);
        String after = m.replaceAll("");
        String temp = after.replaceAll("\\p{P}", "");
        char[] ch = temp.trim().toCharArray();
        float chLength = ch.length;
        float count = 0;
        for (int i = 0; i < ch.length; i++) {
            char c = ch[i];
            if (!Character.isLetterOrDigit(c)) {
                if (!isChinese(c)) {
                    count = count + 1;
                }
            }
        }
        float result = count / chLength;
        if (result > 0.4) {
            return true;
        } else {
            return false;
        }

    }


    //--------------验证密码有效（弱口令认定）--------------------

    /**
     * 验证密码-是否包含用户名字符（密码应与用户名无相关性，密码中不得包含用户名的完整字符串、大小写变位或形似变换的字符串）
     */
    public static boolean verifyPasswordContainAccount(String password, String account) {
        boolean isContain = false;
        if (!TextUtils.isEmpty(password) && !TextUtils.isEmpty(account)) {
            password = password.toLowerCase();
            account = account.toLowerCase();
            if (password.contains(account)) {
                return true;
            }
            String[] likes = {"a", "l", "o"};
            String[] likeSign = {"@", "!", "0"};
            String originalAccount = account + "";
            for (int i = 0; i < likes.length; i++) {
                String tempAccount = originalAccount.replace(likes[i], likeSign[i]);
                account = account.replace(likes[i], likeSign[i]);
                if (password.contains(tempAccount) || password.contains(account)) {
                    return true;
                }
            }

        }
        return isContain;
    }

    public static String getNumberRemoveSpecialCharacter(String text) {
        String ticket = "";
        if (!TextUtils.isEmpty(text)) {
            String REGEX = "[^(0-9)]";
            ticket = Pattern.compile(REGEX).matcher(text).replaceAll("").trim();
        }
        return ticket;
    }

    /**
     * 键盘连续字符统计4个
     *
     * @param str
     * @return
     */
    public static boolean isKeyBoardContinuousChar(String str) {
        boolean result = false;
        char[][] c1 = {
                {'!', '@', '#', '$', '%', '^', '&', '*', '(', ')', '_', '+'},
                {'q', 'w', 'e', 'r', 't', 'y', 'u', 'i', 'o', 'p', '{', '}', '|'},
                {'a', 's', 'd', 'f', 'g', 'h', 'j', 'k', 'l', ':', '"'},
                {'z', 'x', 'c', 'v', 'b', 'n', 'm', '<', '>', '?'}
        };
        char[][] c2 = {
                {'1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '-', '='},
                {'q', 'w', 'e', 'r', 't', 'y', 'u', 'i', 'o', 'p', '{', '}', '\\'},
                {'a', 's', 'd', 'f', 'g', 'h', 'j', 'k', 'l', ';', '\''},
                {'z', 'x', 'c', 'v', 'b', 'n', 'm', ',', '.', '/'}
        };
//        String[] strList = str.split("");
//        //获取坐标位置
//        String[] y ;
//        String[] x ;

        for (char[][] c : new char[][][]{c1, c2}) {
            //横向
            for (int i = 0; i < c.length; i++) {
                for (int j = 0; j < c[i].length - 3; j++) {
                    //创建连续字符
                    StringBuffer sb = new StringBuffer();
                    for (int k = j; k < j + 4; k++) {
                        sb.append(c[i][k]);
                    }
                    String keyStr = sb.toString();
                    if (str.contains(keyStr)) {
                        return true;
                    }
                }
            }

            //纵向
            for (int i = 0; i < c[3].length; i++) {
                //创建连续字符--每列只有4个
                StringBuffer sb = new StringBuffer();
                for (int j = 0; j < 4; j++) {
                    sb.append(c[j][i]);
                }
                String keyStr = sb.toString();
                if (str.contains(keyStr)) {
                    return true;
                }
            }

        }


        return result;
    }


}
