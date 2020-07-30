package retwis.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;
import org.springframework.data.redis.support.collections.DefaultRedisList;
import org.springframework.data.redis.support.collections.RedisList;
import org.springframework.stereotype.Repository;
import retwis.pojo.Post;
import retwis.pojo.Range;
import retwis.pojo.User;
import retwis.util.KeyUtils;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Repository
public class UserDao {

    private static final String mention = "@[\\w]+";

    private final StringRedisTemplate template;
    // 处理键值对操作
    private final ValueOperations<String, String> valueOps;

    // 处理用户id增长的计数器
    private final RedisAtomicLong userIdCounter;
    // 处理帖子id增长的计数器
    private final RedisAtomicLong postIdCounter;

    private RedisList<String> users; // 保存所有用户的名称
    private RedisList<String> posts; // 保存所有帖子的id

    @Autowired
    public UserDao(StringRedisTemplate template) {
        this.template = template;
        valueOps = template.opsForValue();


        // 获取全局uid
        userIdCounter = new RedisAtomicLong(KeyUtils.globalUid(), template.getConnectionFactory());
        postIdCounter = new RedisAtomicLong(KeyUtils.globalPid(), template.getConnectionFactory());
        // 获取用户列表
        users = new DefaultRedisList<>(KeyUtils.users(), template);
        posts = new DefaultRedisList<>(KeyUtils.posts(), template);
    }

    /**
     * 注册用户
     *   步骤:
     *     1. 添加用户信息
     *     2. 把用户id加入全局用户表
     * @param name
     * @param pass
     * @return 用户id
     */
    public String registration(String name, String pass) {
        // 获取用户id
        String uid = String.valueOf(userIdCounter.incrementAndGet());
        // 添加用户信息
        BoundHashOperations<String, String, String> addUser = template.boundHashOps(KeyUtils.user(uid));
        addUser.put("username", name);
        addUser.put("password", pass);
        // 把用户名称和用户id关联起来
        valueOps.set(KeyUtils.userId(name), uid);

        // 加入新注册的用户到用户列表
        users.addFirst(name);

        return uid;
    }

    /**
     * 查询最近注册的用户
     * @param size
     * @return
     */
    public List<User> findUsers(int size) {
        List<User> list = new ArrayList<>(size);
        users.range(0, size - 1).stream().forEach(
                username -> list.add(findUser(username))
        );
        return list;
    }

    /**
     * 全部帖子数
     * @return
     */
    public int postsSize() {
        return posts.size();
    }

    /**
     * 添加帖子
     * @param post
     * @return
     */
    public String savePost(Post post) {
        // 获取帖子id
        String pid = String.valueOf(postIdCounter.incrementAndGet());
        // 添加帖子信息
        BoundHashOperations<String, Object, Object> addPost = template.boundHashOps(KeyUtils.post(pid));
        String uid = String.valueOf(post.getUid());
        addPost.put("uid", uid);
        addPost.put("content", post.getContent());
        addPost.put("publishTime", String.valueOf(post.getPublishTime()));
        addPost.put("username", post.getUsername());

        // 加入帖子到全局帖子列表
        posts.addFirst(pid);

        // 添加到用户帖子列表
        posts(uid).addFirst(pid);

        return pid;
    }

    /**
     * 查询最新帖子
     * @param start
     * @param end
     * @return
     */
    public List<Post> findNewPosts(int start, int end) {
        List<Post> list = new ArrayList<>(end - start + 1);
        posts.range(start, end).stream().forEach(
                pid -> list.add(findPost(pid))
        );
        return list;
    }

    /**
     * 用户名是否存在
     * @param name
     * @return
     */
    public boolean isUserValid(String name) {
        return template.hasKey(KeyUtils.userId(name));
    }

    /**
     * 查询用户id
     * @param name 用户名
     * @return 用户id
     */
    public String findUid(String name) {
        return valueOps.get(KeyUtils.userId(name));
    }

    /**
     * 查询帖子
     * @param pid
     * @return
     */
    public Post findPost(String pid) {
        BoundHashOperations<String, String, String> postOps = template.boundHashOps(KeyUtils.post(pid));
        Post post = new Post();
        String uid = postOps.get("uid");
        String name = postOps.get("username");
        String content = postOps.get("content");
        String publishTime = postOps.get("publishTime");
        post.setId(Long.valueOf(pid));
        post.setUid(Long.valueOf(uid));
        post.setContent(content);
        post.setPublishTime(Long.valueOf(publishTime));
        post.setUsername(name);
        return post;
    }

    /**
     * 根据用户名查询用户
     * @param name 用户名
     * @return 用户
     */
    public User findUser(String name) {
        User user = new User();
        String uid = findUid(name);
        BoundHashOperations<String, String, String> userOps =
                template.boundHashOps(KeyUtils.user(uid));
        user.setId(Long.valueOf(uid));
        user.setUsername(userOps.get("username"));
        user.setPassword(userOps.get("password"));
        // 粉丝数量
        user.setFollower(followersSize(name));
        // 关注数量
        user.setFollowing(watchListSize(name));
        return user;
    }

    /**
     * 获取帖子
     * @param name
     * @param start
     * @param end
     * @return
     */
    public List<Post> findPosts(String name, int start, int end) {
        List<Post> list = new ArrayList<>(end - start + 1);
        posts(findUid(name)).range(start, end).stream().forEach(
                pid -> list.add(findPost(pid))
        );
        return list;
    }

    /**
     * 是否还有更多帖子
     * @param name
     * @param range
     * @return
     */
    public boolean hasMorePosts(String name, Range range) {
        int size = posts(findUid(name)).size();
        range.setTotal(size);
        return range.getEnd() < size - 1;
    }

    /**
     * 获取用户密码
     * @param name
     * @return
     */
    public String getPass(String name) {
        BoundHashOperations<String, String, String> userOps =
                template.boundHashOps(KeyUtils.user(findUid(name)));
        return userOps.get("password");
    }

    /**
     * 关注
     * @param uName 用户
     * @param fName 粉丝
     * @return
     */
    public void follow(String uName, String fName) {
        String uid = valueOps.get(KeyUtils.userId(uName));
        String fid = valueOps.get(KeyUtils.userId(fName)); // tom
        followers(uid).addFirst(fid);
        following(fid).addFirst(uid);
    }

    /**
     * 取消关注
     * @param uName 用户
     * @param fName 粉丝
     * @return
     */
    public void stopFollowing(String uName, String fName) {
        String uid = findUid(uName);
        String fid = findUid(fName);
        followers(uid).remove(fid);
        following(fid).remove(uid);
    }

    /**
     * 粉丝数
     * @param name
     * @return
     */
    public int followersSize(String name) {
        return followers(findUid(name)).size();
    }

    /**
     * 关注数
     * @param name
     * @return
     */
    public int watchListSize(String name) {
        return following(findUid(name)).size();
    }

    /**
     * 是否关注过
     * @param uName 用户
     * @param fName 粉丝
     * @return
     */
    public boolean isFollowed(String uName, String fName) {
        String uid = findUid(uName);
        String fid = findUid(fName);
        boolean b = following(fid).contains(uid);
        return b;
    }

    /**
     * 粉丝列表
     * @param uid
     * @return
     */
    private RedisList<String> followers(String uid) {
        return new DefaultRedisList<>(KeyUtils.followers(uid), template);
    }

    /**
     * 关注列表
     * @param uid
     * @return
     */
    private RedisList<String> following(String uid) {
        return new DefaultRedisList<>(KeyUtils.following(uid), template);
    }

    /**
     * 帖子列表
     * @param uid
     * @return
     */
    private RedisList<String> posts(String uid) {
        return new DefaultRedisList<>(KeyUtils.posts(uid), template);
    }
}
