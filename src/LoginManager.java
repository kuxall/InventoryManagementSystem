import javax.swing.*;
import java.sql.Connection;

public class LoginManager {
    private final Connection conn;

    public LoginManager(Connection conn) {
        this.conn = conn;
    }

    public String showLoginDialog() {
        JTextField usernameField = new JTextField(20);
        JPasswordField passwordField = new JPasswordField(20);
        Object[] message = {
            "Username:", usernameField,
            "Password:", passwordField
        };

        int option = JOptionPane.showConfirmDialog(null, message, "Login", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (option == JOptionPane.OK_OPTION) {
            return validateCredentials(usernameField.getText(), new String(passwordField.getPassword()));
        }
        return null;
    }

    private String validateCredentials(String username, String password) {
        if ("admin".equals(username) && "admin".equals(password)) {
            return "admin";
        } else if ("user".equals(username) && "user".equals(password)) {
            return "user";
        } else {
            JOptionPane.showMessageDialog(null, "Invalid username or password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }
}
