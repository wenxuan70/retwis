package retwis.pojo;

import lombok.Data;

@Data
public class User {
    private Long id;
    private String username;
    private String password;
    private Integer follower;
    private Integer following;

    public User() {
    }

    public User(Long id, String username, String password) {
        this.id = id;
        this.username = username;
        this.password = password;
    }
}
