import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {
    private Connection conn;

    public UserDAO(Connection conn) {
        this.conn = conn;
    }

    public boolean addUser(User user) {
        String sql = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, user.getUsername());
            pst.setString(2, user.getPassword());
            pst.setString(3, user.getRole());
            int affectedRows = pst.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateUser(User user) {
        String sql = "UPDATE users SET password = ?, role = ? WHERE username = ?";
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, user.getPassword());
            pst.setString(2, user.getRole());
            pst.setString(3, user.getUsername());
            int affectedRows = pst.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public User getUser(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, username);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    String password = rs.getString("password");
                    String role = rs.getString("role");
                    return new User(username, password, role);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
