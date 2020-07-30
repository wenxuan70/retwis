package retwis.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import retwis.dao.UserDao;
import retwis.pojo.Post;
import retwis.pojo.Range;
import retwis.pojo.User;
import retwis.util.MD5Utils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class UserService {

    private UserDao userDao;

    private Pattern mention = Pattern.compile("@[\\w]+");

    @Autowired
    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    public User getUser(String name) {
        return userDao.findUser(name);
    }

    /**
     * 获取用户帖子
     * @param name
     * @param range
     * @return
     */
    public List<Post> getPosts(String name, Range range) {
        return userDao.findPosts(name, range.getStart(), range.getEnd());
    }

    public boolean hasMorePosts(String name, Range range) {
        boolean b = userDao.hasMorePosts(name, range);
        range.checkValid();
        return b;
    }

    /**
     * 注册用户
     * @param username
     * @param password
     */
    public void regUser(String username, String password) {
        userDao.registration(username, MD5Utils.saltAndMD5(password));
    }

    /**
     * 检查用户名是否存在
     * @param username
     * @return
     */
    public boolean checkValid(String username) {
        boolean userValid = userDao.isUserValid(username);
        return userValid;
    }

    /**
     * 添加帖子
     * @param post
     */
    public void addPost(Post post) {
        post.setContent(handleContent(post.getContent()));
        post.setId(Long.valueOf(userDao.savePost(post)));
    }

    /**
     * 查找用户id
     * @param username
     * @return
     */
    public Long findUid(String username) {
        return Long.valueOf(userDao.findUid(username));
    }

    /**
     * 关注
     * @param uName 用户
     * @param fName 粉丝
     * @return
     */
    public boolean follow(String uName, String fName) {
        userDao.follow(uName, fName);
        return true;
    }

    /**
     * 取消关注
     * @param uName 用户
     * @param fName 粉丝
     * @return
     */
    public boolean stopFollowing(String uName, String fName) {
        userDao.stopFollowing(uName, fName);
        return true;
    }

    /**
     * 查询帖子,分页
     * @param range
     * @return
     */
    public List<Post> findPosts(Range range) {
        range.setTotal(userDao.postsSize());
        range.checkValid();
        return userDao.findNewPosts(range.getStart(), range.getEnd());
    }

    /**
     * 查询最近注册的用户
     * @param size
     * @return
     */
    public List<User> findUsers(int size) {
        return userDao.findUsers(size);
    }

    /**
     * 是否关注过
     * @param uName
     * @param fName
     * @return
     */
    public boolean isFollowed(String uName, String fName) {
        if (fName == null)
            return false;
        return userDao.isFollowed(uName, fName);
    }

    /**
     * 验证用户名和密码
     * @param username
     * @param password
     * @return
     */
    public boolean auth(String username, String password) {
        if (!userDao.isUserValid(username)) {
            // 不存在该用户
            return false;
        }
        return MD5Utils.saltverifyMD5(password, userDao.getPass(username));
    }

    /**
     * 处理@
     * @param content
     * @return
     */
    private String handleContent(String content) {
        String s = content.replaceAll("<", "&lt;").replaceAll(">", "&gt;");
        Matcher matcher = mention.matcher(s);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            // 获取@的人
            String p = matcher.group().substring(1);
            // 检查是否存在该用户
            if (userDao.isUserValid(p)) {
                matcher.appendReplacement(sb, "<a href='/profile/"+ p +"'>@" + p + "</a>");
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}
