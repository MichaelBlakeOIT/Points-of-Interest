package poi.michael.pointsofinterest.models;

/**
 * Created by micha on 3/12/2018.
 */

public class Comment {
    private String m_comment;
    private int m_user_id;
    private int m_poi_id;
    private int m_comment_id;
    private String m_username;

    public Comment(String comment, int user_id, int poi_id, int comment_id, String username) {
        m_comment = comment;
        m_comment_id = comment_id;
        m_user_id = user_id;
        m_username = username;
        m_poi_id = poi_id;
    }

    public String getComment() {
        return m_comment;
    }

    public int getPoiId() {
        return m_poi_id;
    }

    public String getUsername() {
        return m_username;
    }

    public int getCommentId() {
        return m_comment_id;
    }

    public int getUserId() {
        return m_user_id;
    }
}
