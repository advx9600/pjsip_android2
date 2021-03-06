package gen;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT. Enable "keep" sections if you want to edit. 
/**
 * Entity mapped to table "TB_USER".
 */
public class TbUser {

    private Long id;
    /** Not-null value. */
    private String username;
    /** Not-null value. */
    private String pwd;
    /** Not-null value. */
    private String domain="192.168.0.166";

    public TbUser() {
    }

    public TbUser(Long id) {
        this.id = id;
    }

    public TbUser(Long id, String username, String pwd, String domain) {
        this.id = id;
        this.username = username;
        this.pwd = pwd;
        this.domain = domain;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /** Not-null value. */
    public String getUsername() {
        return username;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setUsername(String username) {
        this.username = username;
    }

    /** Not-null value. */
    public String getPwd() {
        return pwd;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    /** Not-null value. */
    public String getDomain() {
        return domain;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setDomain(String domain) {
        this.domain = domain;
    }

}
