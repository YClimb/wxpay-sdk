package com.weixin.pay.util;

import com.weixin.pay.constants.WXPayConstants;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.Security;
import java.util.Base64;

/**
 * 微信支付AES加解密工具类
 *
 * @author yclimb
 * @date 2018/6/21
 */
public class AESUtil {

    /**
     * 密钥算法
     */
    private static final String ALGORITHM = "AES";

    /**
     * 加解密算法/工作模式/填充方式
     */
    private static final String ALGORITHM_MODE_PADDING = "AES/ECB/PKCS7Padding";

    /**
     * 生成key
     */
    private static SecretKeySpec KEY;

    static {
        try {
            KEY = new SecretKeySpec(WXPayUtil.MD5(WXPayConstants.API_KEY).toLowerCase().getBytes(), ALGORITHM);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * AES加密
     *
     * @param data d
     * @return str
     * @throws Exception e
     */
    public static String encryptData(String data) throws Exception {
        // 创建密码器
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        Cipher cipher = Cipher.getInstance(ALGORITHM_MODE_PADDING, "BC");
        // 初始化
        cipher.init(Cipher.ENCRYPT_MODE, KEY);
        return base64Encode8859(new String(cipher.doFinal(data.getBytes()), "ISO-8859-1"));

    }

    /**
     * AES解密
     *
     * @param base64Data 64
     * @return str
     * @throws Exception e
     */
    public static String decryptData(String base64Data) throws Exception {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        Cipher cipher = Cipher.getInstance(ALGORITHM_MODE_PADDING, "BC");
        cipher.init(Cipher.DECRYPT_MODE, KEY);
        return new String(cipher.doFinal(base64Decode8859(base64Data).getBytes("ISO-8859-1")), "utf-8");
    }

    /**
     * Base64解码
     * @param source base64 str
     * @return str
     */
    public static String base64Decode8859(final String source) {
        String result = "";
        final Base64.Decoder decoder = Base64.getDecoder();
        try {
            // 此处的字符集是ISO-8859-1
            result = new String(decoder.decode(source), "ISO-8859-1");
        } catch (final UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Base64加密
     * @param source str
     * @return base64 str
     */
    public static String base64Encode8859(final String source) {
        String result = "";
        final Base64.Encoder encoder = Base64.getEncoder();
        byte[] textByte = null;
        try {
            //注意此处的编码是ISO-8859-1
            textByte = source.getBytes("ISO-8859-1");
            result = encoder.encodeToString(textByte);
        } catch (final UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static void main(String[] args) throws Exception {

        String A = "qS/pmvAXYetUObwHm9bAod9G3SVBKQK5CiIgETwHJT4ExUpJnIg87m37KlokIsBZCnQBIO2Ear7Q/IazZ6jDNsnmsITqYt1hPYloGjdjRGlqdSSBVRjk9NIkRRQIlb+5AOHJttfVKMsbMK8FzoysE+rL8yKaOzXvsNCA2g60z3bEw3x891ZwPPiUSkVJGeIHpafWdR94Y/j3hfsrEw5KOTGiPneH5d9zhC73MW/kDWu9+wDkJCtCf5fNc9GIC5x2zKNZozpQ9wT/WLyjSz/En166xbgUt9tApaaQSayFQ0eSokMjYYLKO5KJQ355QtkvZlW96rX9IO6hVHXDgPD7kJOTh/L99ZQtG5umLBfOd9i3xVH4qH+gvi/i0gEpvQOhTvxcrZeKs8Rsliua46u/aBdUy6GlICRxQPmvKBfL9cE2L5MZGqHkCMTmSr1i4L8Ubxoi3Yv6TCTTOo4MVc64igb9HttMVfOiLFrZKAyH64Y5C6+GATUMSzWhXDn089QyrZk+W6GFkQlA6dBlO7v0aucF8t3L6SFtnxm6XkH6eD4/FFxKz+wsqKDX1s+GnPGQdwjxsS3RLGjJuNoSB7N+v4AUbMgLT2sBzew89ow7/vEUMjJMQt3eISwprOaDZqZQBgdLVUwDyWnrWi50Rr2wEuJXv/m6x8f40wN93L8GvGbMsWGXlp9V9W3LR2LZD9CnrWAlhoYoDGMAwCKuPh+dfjXmVGttGxegM+PlUR8nq6Qr1zwHz4dV3PgzWlf3n5qR72tAJ/Y0045n3dT7Iw4UNzBHC6XkUIA884paHbZ3D0V95+WrdyVQ4icsgZIneaAMZfslVsnigUjnXl3m/qZGlW5A6d93VXNe8bQgA6s6lJeEsaZc3sLVPi5Tlr2nfbgdhB4XqYkR4DebEbUzalSOqM+OOeCsYj920+FboIxvShy2ECk6bjebMM3kYw0s1NUWXynKFTvbgZ35H9TNKaeom1qYVmbb/581N8+sO3yDDFzZaaqLqOtaUtgIe2SOS2A6GRnKSanqbsJVU4j2amWEpicl3WchYV9KPeuoqodu+4UCsaY2juUIcbbof/ygkG5NkDz27RA4fBxxAlqvtzEftw==";

        Security.addProvider(new BouncyCastleProvider());

        System.out.println(AESUtil.decryptData(A));

        /*String B = AESUtil.decryptData(A);
        System.out.println(B);*/
    }
}
