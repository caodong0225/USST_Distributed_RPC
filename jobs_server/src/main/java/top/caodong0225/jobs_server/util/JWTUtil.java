package top.caodong0225.jobs_server.util;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author jyzxc
 */
public class JWTUtil {

    // 建议配置在 application.yml
    private static final String SECRET = "super-secret-jwt-key-which-should-be-long-enough-256"; // 至少 256 位（32 字节）

    // Token 过期时间（单位：毫秒） — 2小时
    private static final long EXPIRE_TIME = 2 * 60 * 60 * 1000;

    /**
     * 生成 JWT Token
     */
    public static String generateToken(String userId,String userName,String role,String email) throws JOSEException {
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(userId)  // 主题（通常是用户唯一标识）
                .claim("userId", userId)      // 添加userId
                .claim("role", role)          // 添加role
                .claim("userName", userName)  // 添加userName
                .claim("email", email)        // 添加email
                .issueTime(new Date())
                .expirationTime(new Date(System.currentTimeMillis() + EXPIRE_TIME))
                .build();

        JWSHeader header = new JWSHeader(JWSAlgorithm.HS256);
        SignedJWT signedJWT = new SignedJWT(header, claimsSet);
        JWSSigner signer = new MACSigner(SECRET);
        signedJWT.sign(signer);

        return signedJWT.serialize();
    }

    public static boolean isTokenValid(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            if (!signedJWT.verify(new MACVerifier(SECRET))) {
                return false;
            }

            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
            Date expirationTime = claims.getExpirationTime();
            return !new Date().after(expirationTime);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 验证并解析 Token，返回用户名
     */
    /**
     * 验证并解析Token，返回包含用户信息的Map
     */
    public static Map<String, Object> verifyToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            if (!signedJWT.verify(new MACVerifier(SECRET))) {
                return null;
            }

            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
            Date expirationTime = claims.getExpirationTime();
            if (new Date().after(expirationTime)) {
                return null;
            }

            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("subject", claims.getSubject()); // 原始主题（id）
            userInfo.put("userId", claims.getClaim("userId")); // 自定义字段userId
            userInfo.put("role", claims.getClaim("role"));     // 自定义字段role
            userInfo.put("userName", claims.getClaim("userName")); // 自定义字段userName
            userInfo.put("email", claims.getClaim("email"));     // 自定义字段email
            return userInfo;
        } catch (Exception e) {
            return null;
        }
    }

    public static String renewToken(String token) throws JOSEException {
        Map<String, Object> userInfo = verifyToken(token);
        if (userInfo == null) {
            return null;
        }
        return generateToken((String) userInfo.get("userId"),
                (String) userInfo.get("userName"),
                (String) userInfo.get("role"),
                (String) userInfo.get("email"));
    }

    /**
     * 判断 token 是否过期
     */
    public static boolean isExpired(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            return new Date().after(signedJWT.getJWTClaimsSet().getExpirationTime());
        } catch (Exception e) {
            return true;
        }
    }
}
