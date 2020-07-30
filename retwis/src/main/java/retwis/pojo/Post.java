package retwis.pojo;

import lombok.Data;
import retwis.util.TimeUtils;

@Data
public class Post {
    private Long id;
    private String content;
    private Long uid;
    private String username;
    private Long publishTime;
}
