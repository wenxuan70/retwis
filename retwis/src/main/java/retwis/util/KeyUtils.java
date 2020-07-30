package retwis.util;

/**
 * 处理redis键名的工具类
 */
public class KeyUtils {

    private static final String UID = "uid:";

    /* global */

    public static String globalUid() {
        return "global:uid";
    }

    public static String globalPid() {
        return "global:pid";
    }

    /* user */

    // user:1 --- 用户id为1d的用户信息
    public static String user(String uid) {
        return "user:" + uid;
    }

    // user:tom:uid --- 用户tom的用户id
    public static String userId(String name) {
        return "user:" + name + ":uid";
    }

    // uid:1:auth --- 用户id为1的密钥
   public static String auth(String uid) {
        return UID + uid + ":auth";
    }

    // auth:2b6116cd-4d35-4304-80f6-e9a5dd7e54a6 --- 密钥为2b6116c...的用户id
    public static String authKey(String auth) {
        return "auth:" + auth;
    }

    // users --- 保存所有用户名称的集合
    public static String users() {
        return "users";
    }

    /* post */

    // pid:1 --- 帖子id为1的帖子信息
    public static String post(String pid) {
        return "pid:" + pid;
    }

    // uid:1:posts --- 用户id为1的所有帖子
    public static String posts(String uid) {
        return UID + uid + ":posts";
    }

    // posts --- 保存所有帖子id和帖子发布时间的集合
    public static String posts() {
        return "posts";
    }

    // followers:1:uid --- 用户id为1的粉丝, 谁关注我
    public static String followers(String uid) {
        return "followers:" + uid + ":uid";
    }

    // following:1:uid --- 用户id为1的关注, 我关注谁
    public static String following(String uid) {
        return "following:" + uid + ":uid";
    }

}
