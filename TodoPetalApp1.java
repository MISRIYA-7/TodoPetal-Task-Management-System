import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.Window;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Arc2D;
import java.awt.geom.Line2D;
import java.awt.geom.RoundRectangle2D;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;


public class TodoPetalApp {
    public static void main(String[] args) {
        // Initialize the database connection
        DatabaseManager.init();

        // Set custom UI defaults for a modern look
        Style.applyTheme(Theme.AQUA_DREAM);
        UIManager.put("Panel.background", Style.BACKGROUND_COLOR);
        UIManager.put("Frame.background", Style.BACKGROUND_COLOR);
        UIManager.put("Dialog.background", Style.BACKGROUND_COLOR);
        UIManager.put("TextField.border", new EmptyBorder(10, 10, 10, 10));
        UIManager.put("PasswordField.border", new EmptyBorder(10, 10, 10, 10));
        UIManager.put("TextArea.border", new EmptyBorder(10, 10, 10, 10));
        UIManager.put("Button.font", Style.FONT_BUTTON);
        UIManager.put("Label.font", Style.FONT_LABEL);
        UIManager.put("CheckBox.font", Style.FONT_LABEL);
        UIManager.put("ComboBox.font", Style.FONT_FIELD);
        UIManager.put("Table.font", Style.FONT_LABEL);
        UIManager.put("TableHeader.font", Style.FONT_BUTTON);

        SwingUtilities.invokeLater(() -> new LoginPage().setVisible(true));
    }
}


// --- DATABASE AND UTILITIES ---

class DatabaseManager {
    // !!! IMPORTANT: CONFIGURE YOUR DATABASE CONNECTION HERE !!!
    private static final String DB_URL = "jdbc:mysql://localhost:3307/topetal";
    private static final String DB_USER = "misriya"; // Replace with your MySQL username
    private static final String DB_PASS = "root"; // Replace with your MySQL password

    public static void init() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            showError("MySQL JDBC Driver not found. Please add it to your project's classpath.");
            System.exit(1);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }

    public static void showError(String message) {
        JOptionPane.showMessageDialog(null, message, "Database Error", JOptionPane.ERROR_MESSAGE);
    }
}

class PasswordUtils {
    // A simple, static key for AES encryption.
    // WARNING: In a real-world application, this should be stored securely, not hardcoded.
    private static final String SECRET_KEY = "T0d0P3t@lS3cr3t!";
    private static final SecretKeySpec secretKeySpec = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), "AES");


    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] messageDigest = md.digest(password.getBytes());
            BigInteger no = new BigInteger(1, messageDigest);
            String hashtext = no.toString(16);
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            return hashtext;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        return hashPassword(plainPassword).equals(hashedPassword);
    }

    // Method to encrypt a password for the password saver
    public static String encrypt(String strToEncrypt) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            System.out.println("Error while encrypting: " + e.toString());
        }
        return null;
    }

    // Method to decrypt a password from the password saver
    public static String decrypt(String strToDecrypt) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
            return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));
        } catch (Exception e) {
            System.out.println("Error while decrypting: " + e.toString());
        }
        return null;
    }
}

// --- THEME AND STYLES ---
enum Theme {AQUA_DREAM, LAVENDER_BLISS, FOREST_GREEN}

class Style {
    // These are now mutable and changed by applyTheme()
    public static Color PRIMARY_COLOR;
    public static Color ACCENT_COLOR;
    public static Color BACKGROUND_COLOR;
    public static Color CARD_BACKGROUND_COLOR;
    public static Color TEXT_PRIMARY_COLOR;
    public static Color TEXT_SECONDARY_COLOR;
    public static Color DANGER_COLOR;
    public static Color CHART_COLOR_1, CHART_COLOR_2, CHART_COLOR_3, CHART_COLOR_4, CHART_COLOR_5, CHART_COLOR_6; // ADDED MORE CHART COLORS


    // Priority Colors (usually constant)
    public static final Color PRIORITY_HIGH = new Color(255, 77, 77);
    public static final Color PRIORITY_MEDIUM = new Color(255, 191, 0);
    public static final Color PRIORITY_LOW = new Color(60, 179, 113);

    // Status Colors
    public static final Color STATUS_TODO = new Color(0, 150, 255);
    public static final Color STATUS_IN_PROGRESS = new Color(255, 165, 0);
    public static final Color STATUS_DONE = new Color(60, 179, 113);

    // Fonts (constant)
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 42);
    public static final Font FONT_TAGLINE = new Font("Segoe UI Light", Font.ITALIC, 22);
    public static final Font FONT_FORM_HEADER = new Font("Segoe UI Semibold", Font.PLAIN, 32);
    public static final Font FONT_LABEL = new Font("Segoe UI", Font.PLAIN, 16);
    public static final Font FONT_FIELD = new Font("Segoe UI", Font.PLAIN, 18);
    public static final Font FONT_BUTTON = new Font("Segoe UI Semibold", Font.PLAIN, 18);
    public static final Font FONT_TASK_DESC = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_TASK_DETAILS = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_SIDEBAR = new Font("Segoe UI Semibold", Font.PLAIN, 18);
    public static final Font FONT_CARD_TITLE = new Font("Segoe UI Semibold", Font.PLAIN, 20);
    public static final Font FONT_CARD_VALUE = new Font("Segoe UI", Font.BOLD, 36);


    // Initialize with default theme
    static {
        applyTheme(Theme.AQUA_DREAM);
    }

    public static void applyTheme(Theme theme) {
        switch (theme) {
            case AQUA_DREAM:
                PRIMARY_COLOR = new Color(0, 122, 122);
                ACCENT_COLOR = new Color(0, 180, 180);
                BACKGROUND_COLOR = new Color(225, 245, 245);
                CARD_BACKGROUND_COLOR = new Color(248, 252, 252); // A very light off-white
                TEXT_PRIMARY_COLOR = new Color(20, 50, 50);
                TEXT_SECONDARY_COLOR = new Color(100, 130, 130);
                DANGER_COLOR = new Color(200, 50, 50);
                CHART_COLOR_1 = new Color(0, 122, 122);
                CHART_COLOR_2 = new Color(0, 150, 150);
                CHART_COLOR_3 = new Color(60, 180, 180);
                CHART_COLOR_4 = new Color(130, 210, 210);
                CHART_COLOR_5 = new Color(2, 90, 90);
                CHART_COLOR_6 = new Color(0, 210, 210);
                break;
            case FOREST_GREEN:
                PRIMARY_COLOR = new Color(34, 87, 122);
                ACCENT_COLOR = new Color(87, 160, 134);
                BACKGROUND_COLOR = new Color(240, 247, 245);
                CARD_BACKGROUND_COLOR = Color.WHITE;
                TEXT_PRIMARY_COLOR = new Color(40, 40, 40);
                TEXT_SECONDARY_COLOR = new Color(128, 128, 128);
                DANGER_COLOR = new Color(210, 4, 45);
                CHART_COLOR_1 = new Color(56, 102, 65);
                CHART_COLOR_2 = new Color(87, 160, 134);
                CHART_COLOR_3 = new Color(128, 184, 155);
                CHART_COLOR_4 = new Color(199, 228, 216);
                CHART_COLOR_5 = new Color(29, 61, 33);
                CHART_COLOR_6 = new Color(160, 210, 185);
                break;
            case LAVENDER_BLISS:
            default:
                PRIMARY_COLOR = new Color(118, 77, 242);
                ACCENT_COLOR = new Color(230, 80, 150);
                BACKGROUND_COLOR = new Color(244, 241, 255);
                CARD_BACKGROUND_COLOR = Color.WHITE;
                TEXT_PRIMARY_COLOR = new Color(30, 30, 30);
                TEXT_SECONDARY_COLOR = new Color(150, 150, 150);
                DANGER_COLOR = new Color(220, 20, 60);
                CHART_COLOR_1 = new Color(98, 4, 255);
                CHART_COLOR_2 = new Color(158, 123, 255);
                CHART_COLOR_3 = new Color(198, 178, 255);
                CHART_COLOR_4 = new Color(230, 80, 150);
                CHART_COLOR_5 = new Color(138, 90, 255);
                CHART_COLOR_6 = new Color(240, 120, 180);
                break;
        }
    }
}

// --- CUSTOM ROUNDED COMPONENTS ---
class RoundedPanel extends JPanel {
    private final int cornerRadius;
    private Color borderColor = null;

    public RoundedPanel(int r) {
        super();
        this.cornerRadius = r;
        setOpaque(false);
    }

    public void setBorderColor(Color c) {
        this.borderColor = c;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);
        if (borderColor != null) {
            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(2));
            g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, cornerRadius, cornerRadius);
        }
        g2.dispose();
    }
}

class RoundedButton extends JButton {
    public RoundedButton(String text) {
        super(text);
        setOpaque(false);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (getModel().isPressed()) g2.setColor(getBackground().darker());
        else if (getModel().isRollover()) g2.setColor(getBackground().brighter());
        else g2.setColor(getBackground());
        g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 30, 30));
        g2.dispose();
        super.paintComponent(g);
    }
}

// NEW: Custom rounded text field for login page
class RoundedTextField extends JTextField {
    private Shape shape;

    public RoundedTextField(int size) {
        super(size);
        setOpaque(false);
        setBackground(new Color(240, 240, 240)); // A light grey background
        setBorder(new EmptyBorder(5, 10, 5, 10)); // Padding inside the field
    }

    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
        super.paintComponent(g2);
        g2.dispose();
    }

    protected void paintBorder(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(220, 220, 220));
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
        g2.dispose();
    }

    public boolean contains(int x, int y) {
        if (shape == null || !shape.getBounds().equals(getBounds())) {
            shape = new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
        }
        return shape.contains(x, y);
    }
}

// NEW: Custom rounded password field
class RoundedPasswordField extends JPasswordField {
    private Shape shape;

    public RoundedPasswordField(int size) {
        super(size);
        setOpaque(false);
        setBackground(new Color(240, 240, 240));
        setBorder(new EmptyBorder(5, 10, 5, 10));
    }

    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
        super.paintComponent(g2);
        g2.dispose();
    }

    protected void paintBorder(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(220, 220, 220));
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
        g2.dispose();
    }

    public boolean contains(int x, int y) {
        if (shape == null || !shape.getBounds().equals(getBounds())) {
            shape = new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
        }
        return shape.contains(x, y);
    }
}


// --- DATA MODELS ---
enum Priority {HIGH, MEDIUM, LOW}

enum Status {TODO, IN_PROGRESS, DONE}

enum Category {
    IDEA("💡"), FOOD("🍽️"), WORK("💼"), SPORT("🏃"), MUSIC("🎵"), OTHER("📌");
    final String icon;

    Category(String icon) {
        this.icon = icon;
    }
}

enum GoalStatus {IN_PROGRESS, COMPLETED} // ADDED: Status for goals


class Task {
    private int id;
    private int userId;
    private String description;
    private Status status;
    private Priority priority;
    private Category category;
    private LocalDate dueDate;
    private LocalDate creationDate;

    public Task(int id, int userId, String d, Priority p, Category c, LocalDate date, Status s, LocalDate creation) {
        this.id = id;
        this.userId = userId;
        this.description = d;
        this.priority = p;
        this.category = c;
        this.dueDate = date;
        this.status = s;
        this.creationDate = creation;
    }

    public Task(int userId, String d, Priority p, Category c, LocalDate date, Status s) {
        this(0, userId, d, p, c, date, s, LocalDate.now());
    }

    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String d) {
        this.description = d;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status s) {
        this.status = s;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority p) {
        this.priority = p;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate d) {
        this.dueDate = d;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category c) {
        this.category = c;
    }

    public LocalDate getCreationDate() {
        return creationDate;
    }

    @Override
    public String toString() {
        return description;
    }
}

class User {
    private int id;
    private String username;
    private String password;
    private String role;
    private String email;
    private String phone;
    private LocalDateTime lastLoginTime;

    public User(int id, String u, String p, String r, String e, String ph, LocalDateTime lastLogin) {
        this.id = id;
        this.username = u;
        this.password = p;
        this.role = r;
        this.email = e;
        this.phone = ph;
        this.lastLoginTime = lastLogin;
    }

    public User(String u, String p, String r, String e, String ph) {
        this(0, u, p, r, e, ph, null);
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String r) {
        this.role = r;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String e) {
        this.email = e;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String p) {
        this.phone = p;
    }

    public LocalDateTime getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(LocalDateTime t) {
        this.lastLoginTime = t;
    }
}

// ADDED: Goal class
class Goal {
    private int id;
    private int userId;
    private String title;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private GoalStatus status;

    public Goal(int id, int userId, String title, String description, LocalDate startDate, LocalDate endDate, GoalStatus status) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
    }

    // For creating new goals
    public Goal(int userId, String title, String description, LocalDate startDate, LocalDate endDate) {
        this(0, userId, title, description, startDate, endDate, GoalStatus.IN_PROGRESS);
    }

    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public GoalStatus getStatus() {
        return status;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public void setStatus(GoalStatus status) {
        this.status = status;
    }
}

// NEW: PasswordEntry class
class PasswordEntry {
    private int id;
    private int userId;
    private String websiteName;
    private String username;
    private String encryptedPassword;
    private String notes;

    public PasswordEntry(int id, int userId, String websiteName, String username, String encryptedPassword, String notes) {
        this.id = id;
        this.userId = userId;
        this.websiteName = websiteName;
        this.username = username;
        this.encryptedPassword = encryptedPassword;
        this.notes = notes;
    }

    public PasswordEntry(int userId, String websiteName, String username, String password, String notes) {
        this.id = 0;
        this.userId = userId;
        this.websiteName = websiteName;
        this.username = username;
        this.encryptedPassword = PasswordUtils.encrypt(password);
        this.notes = notes;
    }

    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public String getWebsiteName() {
        return websiteName;
    }

    public String getUsername() {
        return username;
    }

    public String getEncryptedPassword() {
        return encryptedPassword;
    }

    public String getDecryptedPassword() {
        return PasswordUtils.decrypt(encryptedPassword);
    }

    public String getNotes() {
        return notes;
    }

    public void setWebsiteName(String websiteName) {
        this.websiteName = websiteName;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEncryptedPassword(String encryptedPassword) {
        this.encryptedPassword = encryptedPassword;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}

// NEW: Contact class
class Contact {
    private int id;
    private int userId;
    private String name;
    private String phoneNumber;
    private String email;
    private String address;

    public Contact(int id, int userId, String name, String phoneNumber, String email, String address) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.address = address;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}


// --- DATA ACCESS OBJECTS (DAO) ---

class UserDAO {
    public Optional<User> getUserByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Timestamp lastLogin = rs.getTimestamp("last_login_time");
                return Optional.of(new User(
                        rs.getInt("id"), rs.getString("username"), rs.getString("password"),
                        rs.getString("role"), rs.getString("email"), rs.getString("phone"),
                        lastLogin == null ? null : lastLogin.toLocalDateTime()
                ));
            }
        } catch (SQLException e) {
            DatabaseManager.showError(e.getMessage());
        }
        return Optional.empty();
    }

    // --- NEW: Method to get user by ID ---
    public Optional<User> getUserById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Timestamp lastLogin = rs.getTimestamp("last_login_time");
                return Optional.of(new User(
                        rs.getInt("id"), rs.getString("username"), rs.getString("password"),
                        rs.getString("role"), rs.getString("email"), rs.getString("phone"),
                        lastLogin == null ? null : lastLogin.toLocalDateTime()
                ));
            }
        } catch (SQLException e) {
            DatabaseManager.showError(e.getMessage());
        }
        return Optional.empty();
    }

    public boolean addUser(User user) {
        String sql = "INSERT INTO users(username, password, role, email, phone) VALUES(?,?,?,?,?)";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, PasswordUtils.hashPassword(user.getPassword()));
            pstmt.setString(3, user.getRole());
            pstmt.setString(4, user.getEmail());
            pstmt.setString(5, user.getPhone());
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            DatabaseManager.showError(e.getMessage());
            return false;
        }
    }

    // --- NEW: Method for admin/user to update user info ---
    public boolean updateUser(User user) {
        String sql = "UPDATE users SET role = ?, email = ?, phone = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getRole());
            pstmt.setString(2, user.getEmail());
            pstmt.setString(3, user.getPhone());
            pstmt.setInt(4, user.getId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            DatabaseManager.showError(e.getMessage());
            return false;
        }
    }

    // --- NEW: Method for user to change their password ---
    public boolean changeUserPassword(int userId, String newPassword) {
        String sql = "UPDATE users SET password = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, PasswordUtils.hashPassword(newPassword));
            pstmt.setInt(2, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            DatabaseManager.showError(e.getMessage());
            return false;
        }
    }


    public void updateLastLogin(User user) {
        String sql = "UPDATE users SET last_login_time = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setInt(2, user.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            DatabaseManager.showError(e.getMessage());
        }
    }

    public List<User> getAllUsers() {
        List<User> userList = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY username";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Timestamp lastLogin = rs.getTimestamp("last_login_time");
                userList.add(new User(
                        rs.getInt("id"), rs.getString("username"), rs.getString("password"),
                        rs.getString("role"), rs.getString("email"), rs.getString("phone"),
                        lastLogin == null ? null : lastLogin.toLocalDateTime()
                ));
            }
        } catch (SQLException e) {
            DatabaseManager.showError(e.getMessage());
        }
        return userList;
    }

    public boolean deleteUser(int userId) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            DatabaseManager.showError(e.getMessage());
            return false;
        }
    }
}

class TaskDAO {
    public List<Task> getTasksForUser(int userId) {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT * FROM tasks WHERE user_id = ?";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                tasks.add(new Task(
                        rs.getInt("id"), rs.getInt("user_id"), rs.getString("description"),
                        Priority.valueOf(rs.getString("priority")), Category.valueOf(rs.getString("category")),
                        rs.getDate("due_date").toLocalDate(), Status.valueOf(rs.getString("status")),
                        rs.getDate("creation_date").toLocalDate()
                ));
            }
        } catch (SQLException e) {
            DatabaseManager.showError(e.getMessage());
        }
        return tasks;
    }

    // NEW: Get upcoming tasks for the dashboard
    public List<Task> getUpcomingTasksForUser(int userId, int limit) {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT * FROM tasks WHERE user_id = ? AND status != 'DONE' AND due_date >= CURDATE() ORDER BY due_date ASC LIMIT ?";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, limit);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                tasks.add(new Task(
                        rs.getInt("id"), rs.getInt("user_id"), rs.getString("description"),
                        Priority.valueOf(rs.getString("priority")), Category.valueOf(rs.getString("category")),
                        rs.getDate("due_date").toLocalDate(), Status.valueOf(rs.getString("status")),
                        rs.getDate("creation_date").toLocalDate()
                ));
            }
        } catch (SQLException e) {
            DatabaseManager.showError(e.getMessage());
        }
        return tasks;
    }


    public boolean addTask(Task task) {
        String sql = "INSERT INTO tasks(user_id, description, status, priority, category, due_date, creation_date) VALUES(?,?,?,?,?,?,?)";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, task.getUserId());
            pstmt.setString(2, task.getDescription());
            pstmt.setString(3, task.getStatus().name());
            pstmt.setString(4, task.getPriority().name());
            pstmt.setString(5, task.getCategory().name());
            pstmt.setDate(6, java.sql.Date.valueOf(task.getDueDate()));
            pstmt.setDate(7, java.sql.Date.valueOf(task.getCreationDate()));
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            DatabaseManager.showError(e.getMessage());
            return false;
        }
    }

    public boolean updateTask(Task task) {
        String sql = "UPDATE tasks SET description = ?, status = ?, priority = ?, category = ?, due_date = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, task.getDescription());
            pstmt.setString(2, task.getStatus().name());
            pstmt.setString(3, task.getPriority().name());
            pstmt.setString(4, task.getCategory().name());
            pstmt.setDate(5, java.sql.Date.valueOf(task.getDueDate()));
            pstmt.setInt(6, task.getId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            DatabaseManager.showError(e.getMessage());
            return false;
        }
    }

    // ADDED: Method to get task counts by category for the graph
    public Map<String, Long> getTaskCountByCategory(int userId, Status status) {
        Map<String, Long> counts = new LinkedHashMap<>();
        String sql = "SELECT category, COUNT(*) as count FROM tasks WHERE user_id = ? AND status = ? GROUP BY category";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, status.name());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                counts.put(rs.getString("category"), rs.getLong("count"));
            }
        } catch (SQLException e) {
            DatabaseManager.showError(e.getMessage());
        }
        return counts;
    }

    // ADDED: Method to get dates with tasks for the calendar
    public Set<LocalDate> getTaskDatesInMonth(int userId, int year, int month) {
        Set<LocalDate> dates = new HashSet<>();
        String sql = "SELECT DISTINCT due_date FROM tasks WHERE user_id = ? AND YEAR(due_date) = ? AND MONTH(due_date) = ?";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, year);
            pstmt.setInt(3, month);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                dates.add(rs.getDate("due_date").toLocalDate());
            }
        } catch (SQLException e) {
            DatabaseManager.showError(e.getMessage());
        }
        return dates;
    }

    // ADDED: Method to get tasks for a specific day
    public List<Task> getTasksByDate(int userId, LocalDate date) {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT * FROM tasks WHERE user_id = ? AND due_date = ? ORDER BY priority";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setDate(2, java.sql.Date.valueOf(date));
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                tasks.add(new Task(
                        rs.getInt("id"), rs.getInt("user_id"), rs.getString("description"),
                        Priority.valueOf(rs.getString("priority")), Category.valueOf(rs.getString("category")),
                        rs.getDate("due_date").toLocalDate(), Status.valueOf(rs.getString("status")),
                        rs.getDate("creation_date").toLocalDate()
                ));
            }
        } catch (SQLException e) {
            DatabaseManager.showError(e.getMessage());
        }
        return tasks;
    }


    public Map<String, Long> getTaskCountByStatus(int userId) {
        Map<String, Long> counts = new LinkedHashMap<>();
        counts.put("TODO", 0L);
        counts.put("IN_PROGRESS", 0L);
        counts.put("DONE", 0L);

        String sql = "SELECT status, COUNT(*) as count FROM tasks WHERE user_id = ? GROUP BY status";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                counts.put(rs.getString("status"), rs.getLong("count"));
            }
        } catch (SQLException e) {
            DatabaseManager.showError(e.getMessage());
        }
        return counts;
    }

    public Map<String, Integer> getCompletedTasksByDay(int userId, LocalDate start, LocalDate end) {
        Map<String, Integer> dailyData = new LinkedHashMap<>();
        // Pre-fill the map with all days in the range to ensure the chart shows gaps
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            dailyData.put(date.format(DateTimeFormatter.ofPattern("dd MMM")), 0);
        }

        String sql = "SELECT DATE(due_date) as completed_day, COUNT(*) as count " +
                "FROM tasks WHERE user_id = ? AND status = 'DONE' AND due_date BETWEEN ? AND ? " +
                "GROUP BY completed_day ORDER BY completed_day";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setDate(2, java.sql.Date.valueOf(start));
            pstmt.setDate(3, java.sql.Date.valueOf(end));
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                LocalDate day = rs.getDate("completed_day").toLocalDate();
                dailyData.put(day.format(DateTimeFormatter.ofPattern("dd MMM")), rs.getInt("count"));
            }
        } catch (SQLException e) {
            DatabaseManager.showError(e.getMessage());
        }
        return dailyData;
    }

    // --- NEW: For Admin Stats ---
    public int getSystemTotalTaskCount() {
        String sql = "SELECT COUNT(*) FROM tasks";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            DatabaseManager.showError(e.getMessage());
        }
        return 0;
    }
}

// ADDED: GoalDAO class
class GoalDAO {
    public List<Goal> getGoalsForUser(int userId) {
        List<Goal> goals = new ArrayList<>();
        String sql = "SELECT * FROM goals WHERE user_id = ? ORDER BY end_date";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                goals.add(new Goal(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getDate("start_date").toLocalDate(),
                        rs.getDate("end_date").toLocalDate(),
                        GoalStatus.valueOf(rs.getString("status"))
                ));
            }
        } catch (SQLException e) {
            DatabaseManager.showError(e.getMessage());
        }
        return goals;
    }

    public boolean addGoal(Goal goal) {
        String sql = "INSERT INTO goals(user_id, title, description, start_date, end_date, status) VALUES(?,?,?,?,?,?)";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, goal.getUserId());
            pstmt.setString(2, goal.getTitle());
            pstmt.setString(3, goal.getDescription());
            pstmt.setDate(4, java.sql.Date.valueOf(goal.getStartDate()));
            pstmt.setDate(5, java.sql.Date.valueOf(goal.getEndDate()));
            pstmt.setString(6, goal.getStatus().name());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            DatabaseManager.showError(e.getMessage());
            return false;
        }
    }

    public boolean updateGoal(Goal goal) {
        String sql = "UPDATE goals SET title = ?, description = ?, start_date = ?, end_date = ?, status = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, goal.getTitle());
            pstmt.setString(2, goal.getDescription());
            pstmt.setDate(3, java.sql.Date.valueOf(goal.getStartDate()));
            pstmt.setDate(4, java.sql.Date.valueOf(goal.getEndDate()));
            pstmt.setString(5, goal.getStatus().name());
            pstmt.setInt(6, goal.getId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            DatabaseManager.showError(e.getMessage());
            return false;
        }
    }

    public boolean deleteGoal(int goalId) {
        String sql = "DELETE FROM goals WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, goalId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            DatabaseManager.showError(e.getMessage());
            return false;
        }
    }

    // --- NEW: For Admin Stats ---
    public int getSystemActiveGoalsCount() {
        String sql = "SELECT COUNT(*) FROM goals WHERE status = 'IN_PROGRESS'";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            DatabaseManager.showError(e.getMessage());
        }
        return 0;
    }
}

// NEW: PasswordDAO class for Password Saver
class PasswordDAO {
    public List<PasswordEntry> getPasswordsForUser(int userId) {
        List<PasswordEntry> entries = new ArrayList<>();
        String sql = "SELECT * FROM passwords WHERE user_id = ? ORDER BY website_name";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                entries.add(new PasswordEntry(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getString("website_name"),
                        rs.getString("username"),
                        rs.getString("encrypted_password"),
                        rs.getString("notes")
                ));
            }
        } catch (SQLException e) {
            DatabaseManager.showError(e.getMessage());
        }
        return entries;
    }

    public boolean addPassword(PasswordEntry entry) {
        String sql = "INSERT INTO passwords(user_id, website_name, username, encrypted_password, notes) VALUES(?,?,?,?,?)";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, entry.getUserId());
            pstmt.setString(2, entry.getWebsiteName());
            pstmt.setString(3, entry.getUsername());
            pstmt.setString(4, entry.getEncryptedPassword());
            pstmt.setString(5, entry.getNotes());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            DatabaseManager.showError(e.getMessage());
            return false;
        }
    }

    public boolean updatePassword(PasswordEntry entry) {
        String sql = "UPDATE passwords SET website_name = ?, username = ?, encrypted_password = ?, notes = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, entry.getWebsiteName());
            pstmt.setString(2, entry.getUsername());
            pstmt.setString(3, entry.getEncryptedPassword());
            pstmt.setString(4, entry.getNotes());
            pstmt.setInt(5, entry.getId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            DatabaseManager.showError(e.getMessage());
            return false;
        }
    }

    public boolean deletePassword(int entryId) {
        String sql = "DELETE FROM passwords WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, entryId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            DatabaseManager.showError(e.getMessage());
            return false;
        }
    }
}

// NEW: ContactDAO class for Contacts Saver
class ContactDAO {
    public List<Contact> getContactsForUser(int userId) {
        List<Contact> contacts = new ArrayList<>();
        String sql = "SELECT * FROM contacts WHERE user_id = ? ORDER BY contact_name";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                contacts.add(new Contact(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getString("contact_name"),
                        rs.getString("phone_number"),
                        rs.getString("email"),
                        rs.getString("address")
                ));
            }
        } catch (SQLException e) {
            DatabaseManager.showError(e.getMessage());
        }
        return contacts;
    }

    public boolean addContact(Contact contact) {
        String sql = "INSERT INTO contacts(user_id, contact_name, phone_number, email, address) VALUES(?,?,?,?,?)";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, contact.getUserId());
            pstmt.setString(2, contact.getName());
            pstmt.setString(3, contact.getPhoneNumber());
            pstmt.setString(4, contact.getEmail());
            pstmt.setString(5, contact.getAddress());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            DatabaseManager.showError(e.getMessage());
            return false;
        }
    }

    public boolean updateContact(Contact contact) {
        String sql = "UPDATE contacts SET contact_name = ?, phone_number = ?, email = ?, address = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, contact.getName());
            pstmt.setString(2, contact.getPhoneNumber());
            pstmt.setString(3, contact.getEmail());
            pstmt.setString(4, contact.getAddress());
            pstmt.setInt(5, contact.getId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            DatabaseManager.showError(e.getMessage());
            return false;
        }
    }

    public boolean deleteContact(int contactId) {
        String sql = "DELETE FROM contacts WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, contactId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            DatabaseManager.showError(e.getMessage());
            return false;
        }
    }
}


// --- LOGIN AND SIGN-UP SCREENS ---
class LoginPage extends JFrame {
    private final UserDAO userDAO = new UserDAO();

    public LoginPage() {
        setTitle("Todo Petal - Login");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        JSplitPane sp = new JSplitPane();
        sp.setEnabled(false);
        sp.setDividerSize(0);
        sp.setResizeWeight(0.40);
        add(sp, BorderLayout.CENTER);
        sp.setLeftComponent(createBrandingPanel());
        sp.setRightComponent(createFormPanel());
    }

    JPanel createBrandingPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Style.PRIMARY_COLOR);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(10, 20, 10, 20);
        g.gridwidth = GridBagConstraints.REMAINDER;
        JLabel t = new JLabel("Todo Petal");
        t.setFont(Style.FONT_HEADER);
        t.setForeground(Color.WHITE);
        p.add(t, g);
        JLabel s = new JLabel("Blossom into Productivity.");
        s.setFont(Style.FONT_TAGLINE);
        s.setForeground(new Color(230, 230, 255));
        p.add(s, g);
        return p;
    }

    private JPanel createFormPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Style.CARD_BACKGROUND_COLOR);
        JPanel fc = new JPanel(new GridBagLayout());
        fc.setBackground(Style.CARD_BACKGROUND_COLOR);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(10, 5, 10, 5);
        g.fill = GridBagConstraints.HORIZONTAL;
        JLabel h = new JLabel("Welcome!");
        h.setFont(Style.FONT_FORM_HEADER);
        g.gridwidth = 2;
        g.weighty = 0.5;
        fc.add(h, g);
        g.weighty = 0;
        JTextField uf = addFormField(fc, g, "Username", 1);
        JPasswordField pf = addPasswordField(fc, g, "Password", 2);
        JComboBox<String> rb = addRoleField(fc, g, "Role", 3);
        g.gridy = 7;
        g.insets = new Insets(25, 5, 8, 5);
        JButton lb = new RoundedButton("Login");
        lb.setBackground(Style.ACCENT_COLOR);
        lb.setForeground(Color.WHITE);
        lb.setFont(Style.FONT_BUTTON);
        fc.add(lb, g);
        g.gridy = 8;
        g.insets = new Insets(8, 5, 8, 5);
        JButton sb = new RoundedButton("Create Account");
        sb.setBackground(Style.PRIMARY_COLOR);
        sb.setForeground(Color.WHITE);
        sb.setFont(Style.FONT_BUTTON);
        fc.add(sb, g);

        lb.addActionListener(e -> {
            String u = uf.getText();
            String pass = new String(pf.getPassword());
            String r = (String) rb.getSelectedItem();
            if (u.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields required.", "Login Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Optional<User> userOpt = userDAO.getUserByUsername(u);
            if (userOpt.isPresent() && PasswordUtils.checkPassword(pass, userOpt.get().getPassword()) && userOpt.get().getRole().equals(r)) {
                User user = userOpt.get();
                userDAO.updateLastLogin(user);
                dispose();
                if ("Admin".equals(r)) new AdminDashboard(this, user).setVisible(true);
                else new UserDashboard(this, user).setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Invalid credentials.", "Login Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        sb.addActionListener(e -> {
            dispose();
            new SignUpPage().setVisible(true);
        });
        p.add(fc);
        return p;
    }

    // MODIFIED to use RoundedTextField
    JTextField addFormField(JPanel p, GridBagConstraints g, String l, int y) {
        g.gridy = y * 2 - 1;
        p.add(new JLabel(l), g);
        JTextField f = new RoundedTextField(25);
        f.setFont(Style.FONT_FIELD);
        g.gridy = y * 2;
        p.add(f, g);
        return f;
    }

    // MODIFIED to use RoundedPasswordField
    JPasswordField addPasswordField(JPanel p, GridBagConstraints g, String l, int y) {
        g.gridy = y * 2 - 1;
        p.add(new JLabel(l), g);
        JPasswordField f = new RoundedPasswordField(25);
        f.setFont(Style.FONT_FIELD);
        g.gridy = y * 2;
        p.add(f, g);
        return f;
    }

    JComboBox<String> addRoleField(JPanel p, GridBagConstraints g, String l, int y) {
        g.gridy = y * 2 - 1;
        p.add(new JLabel(l), g);
        JComboBox<String> f = new JComboBox<>(new String[]{"User", "Admin"});
        f.setFont(Style.FONT_FIELD);
        g.gridy = y * 2;
        p.add(f, g);
        return f;
    }
}

class SignUpPage extends JFrame {
    private final UserDAO userDAO = new UserDAO();

    public SignUpPage() {
        setTitle("Todo Petal - Create Account");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        JSplitPane sp = new JSplitPane();
        sp.setEnabled(false);
        sp.setDividerSize(0);
        sp.setResizeWeight(0.40);
        add(sp, BorderLayout.CENTER);
        sp.setLeftComponent(new LoginPage().createBrandingPanel());
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Style.CARD_BACKGROUND_COLOR);
        JPanel fc = new JPanel(new GridBagLayout());
        fc.setBackground(Style.CARD_BACKGROUND_COLOR);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 5, 8, 5);
        g.fill = GridBagConstraints.HORIZONTAL;
        JLabel h = new JLabel("Create an Account");
        h.setFont(Style.FONT_FORM_HEADER);
        g.gridwidth = 2;
        g.weighty = 0.5;
        fc.add(h, g);
        g.weighty = 0;
        LoginPage lp = new LoginPage();
        JTextField uf = lp.addFormField(fc, g, "Username", 1);
        JPasswordField pf = lp.addPasswordField(fc, g, "Password", 2);
        JTextField ef = lp.addFormField(fc, g, "Email", 3);
        JTextField phf = lp.addFormField(fc, g, "Phone Number", 4);
        JComboBox<String> rb = lp.addRoleField(fc, g, "Role", 5);
        g.insets = new Insets(20, 5, 8, 5);
        g.gridy = 11;
        JButton regb = new RoundedButton("Register");
        regb.setBackground(Style.ACCENT_COLOR);
        regb.setForeground(Color.WHITE);
        fc.add(regb, g);
        g.insets = new Insets(8, 5, 8, 5);
        g.gridy = 12;
        JButton backb = new RoundedButton("Back to Login");
        backb.setBackground(Style.PRIMARY_COLOR);
        backb.setForeground(Color.WHITE);
        fc.add(backb, g);

        regb.addActionListener(e -> {
            String u = uf.getText().trim();
            String pass = new String(pf.getPassword());
            String email = ef.getText().trim();
            String phone = phf.getText().trim();
            String role = (String) rb.getSelectedItem();
            if (u.isEmpty() || pass.isEmpty() || email.isEmpty() || phone.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields required.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!Pattern.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$", email)) {
                JOptionPane.showMessageDialog(this, "Invalid email format.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (userDAO.getUserByUsername(u).isPresent()) {
                JOptionPane.showMessageDialog(this, "Username exists.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            User newUser = new User(u, pass, role, email, phone);
            if (userDAO.addUser(newUser)) {
                JOptionPane.showMessageDialog(this, "Account created!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose();
                new LoginPage().setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Could not create account. Check logs.", "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        backb.addActionListener(e -> {
            dispose();
            new LoginPage().setVisible(true);
        });
        p.add(fc);
        sp.setRightComponent(p);
    }
}

// --- NEW DASHBOARD GRAPHICS COMPONENTS ---

class CircularProgressPanel extends JPanel {
    private double progress; // 0.0 to 1.0
    private String text;

    public CircularProgressPanel() {
        this.progress = 0.0;
        this.text = "0%";
        setOpaque(false);
        setPreferredSize(new Dimension(200, 200));
    }

    public void updateProgress(double progress) {
        this.progress = Math.max(0.0, Math.min(1.0, progress));
        this.text = String.format("%d%%", (int) (this.progress * 100));
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int size = Math.min(getWidth(), getHeight()) - 20;
        int x = (getWidth() - size) / 2;
        int y = (getHeight() - size) / 2;
        int thickness = 20;

        g2d.setColor(Style.BACKGROUND_COLOR);
        g2d.setStroke(new BasicStroke(thickness));
        g2d.drawOval(x, y, size, size);

        g2d.setColor(Style.PRIMARY_COLOR);
        g2d.setStroke(new BasicStroke(thickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.draw(new Arc2D.Double(x, y, size, size, 90, -progress * 360, Arc2D.OPEN));

        g2d.setColor(Style.TEXT_PRIMARY_COLOR);
        g2d.setFont(Style.FONT_HEADER.deriveFont(48f));
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        int textHeight = fm.getAscent();
        g2d.drawString(text, getWidth() / 2 - textWidth / 2, getHeight() / 2 + textHeight / 4);

        g2d.setFont(Style.FONT_LABEL);
        fm = g2d.getFontMetrics();
        g2d.drawString("Completed", getWidth() / 2 - fm.stringWidth("Completed") / 2, getHeight() / 2 + textHeight / 4 + 30);

        g2d.dispose();
    }
}

class BarChartPanel extends JPanel {
    private Map<String, Integer> data;
    private final Color[] barColors = {Style.CHART_COLOR_1, Style.CHART_COLOR_2, Style.CHART_COLOR_3, Style.CHART_COLOR_4};

    public BarChartPanel() {
        this.data = new LinkedHashMap<>();
        setOpaque(false);
        setPreferredSize(new Dimension(400, 250));
    }

    public void setData(Map<String, Integer> data) {
        this.data = data;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (data == null || data.isEmpty()) return;

        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int maxVal = data.values().stream().max(Integer::compareTo).orElse(1);
        int width = getWidth();
        int height = getHeight();
        int padding = 25;
        int labelPadding = 20;

        g2d.setColor(Color.LIGHT_GRAY);
        g2d.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        FontMetrics fm = g2d.getFontMetrics();
        for (int i = 0; i <= 4; i++) {
            int y = height - padding - labelPadding - i * (height - padding * 2 - labelPadding) / 4;
            int value = (int) Math.ceil(((double) i / 4.0) * maxVal);
            g2d.drawLine(padding, y, width - padding, y);
            g2d.drawString(String.valueOf(value), padding - fm.stringWidth(String.valueOf(value)) - 5, y + 5);
        }

        int barWidth = (width - 2 * padding) / (data.size() * 2);
        int x = padding + barWidth / 2;
        int colorIndex = 0;
        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            int barHeight = (int) (((double) entry.getValue() / maxVal) * (height - 2 * padding - labelPadding));
            int y = height - padding - labelPadding - barHeight;

            g2d.setColor(barColors[colorIndex % barColors.length]);
            g2d.fillRoundRect(x, y, barWidth, barHeight, 10, 10);

            g2d.setColor(Style.TEXT_SECONDARY_COLOR);
            g2d.drawString(entry.getKey(), x + barWidth / 2 - fm.stringWidth(entry.getKey()) / 2, height - padding);

            x += 2 * barWidth;
            colorIndex++;
        }
        g2d.dispose();
    }
}

// ADDED: PieChartPanel for new graph representation
class PieChartPanel extends JPanel {
    private Map<String, Long> data; // Label -> Value
    private final Color[] sliceColors = {
            Style.CHART_COLOR_1, Style.CHART_COLOR_2, Style.CHART_COLOR_3,
            Style.CHART_COLOR_4, Style.CHART_COLOR_5, Style.CHART_COLOR_6
    };

    public PieChartPanel() {
        this.data = new LinkedHashMap<>();
        setOpaque(false);
    }

    public void setData(Map<String, Long> data) {
        this.data = data;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (data == null || data.isEmpty()) {
            g.setColor(Style.TEXT_SECONDARY_COLOR);
            g.drawString("No data available.", getWidth() / 2 - 50, getHeight() / 2);
            return;
        }

        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        long total = data.values().stream().mapToLong(Long::longValue).sum();
        if (total == 0) return;

        int size = Math.min(getWidth(), getHeight()) - 40;
        int x = (getWidth() - size) / 2;
        int y = (getHeight() - size) / 2;

        double currentAngle = 90.0;
        int colorIndex = 0;

        for (Map.Entry<String, Long> entry : data.entrySet()) {
            double arcAngle = (entry.getValue() / (double) total) * 360.0;
            g2d.setColor(sliceColors[colorIndex % sliceColors.length]);
            g2d.fill(new Arc2D.Double(x, y, size, size, currentAngle, -arcAngle, Arc2D.PIE));
            currentAngle -= arcAngle;
            colorIndex++;
        }
    }
}

// NEW: LineChartPanel for the main dashboard graph
class LineChartPanel extends JPanel {
    private Map<String, Integer> data; // Label (e.g., date) -> Value
    private int padding = 30;
    private int labelPadding = 25;
    private Color lineColor = new Color(0, 100, 200, 180);
    private Color pointColor = new Color(0, 100, 200);
    private Color gridColor = new Color(200, 200, 200, 200);
    private static final Stroke GRAPH_STROKE = new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

    public LineChartPanel() {
        this.data = new LinkedHashMap<>();
        setOpaque(false);
    }

    public void setData(Map<String, Integer> data) {
        this.data = data;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (data == null || data.size() < 2) {
            return; // Not enough data to draw a line
        }

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        double maxScore = data.values().stream().max(Integer::compareTo).orElse(1);

        double xScale = ((double) getWidth() - 2 * padding - labelPadding) / (data.size() - 1);
        double yScale = ((double) getHeight() - 2 * padding - labelPadding) / maxScore;

        List<Point> graphPoints = new ArrayList<>();
        int i = 0;
        for (Integer value : data.values()) {
            int x1 = (int) (i * xScale + padding + labelPadding);
            int y1 = (int) ((maxScore - value) * yScale + padding);
            graphPoints.add(new Point(x1, y1));
            i++;
        }

        // Draw grid lines and labels
        FontMetrics fm = g2.getFontMetrics();
        g2.setColor(gridColor);

        // Y-axis labels and grid lines
        for (int j = 0; j <= 4; j++) {
            int y = getHeight() - padding - labelPadding - j * (getHeight() - 2 * padding - labelPadding) / 4;
            int value = (int) Math.round(j * maxScore / 4.0);
            g2.drawString(String.valueOf(value), padding, y + fm.getAscent() / 2);
            g2.drawLine(padding + labelPadding, y, getWidth() - padding, y);
        }

        // X-axis labels
        i = 0;
        for (String label : data.keySet()) {
            int x = (int) (i * xScale + padding + labelPadding);
            int y = getHeight() - padding;
            g2.drawString(label, x - fm.stringWidth(label) / 2, y);
            i++;
        }

        // Draw the graph line
        g2.setStroke(GRAPH_STROKE);
        g2.setColor(lineColor);
        for (int j = 0; j < graphPoints.size() - 1; j++) {
            int x1 = graphPoints.get(j).x;
            int y1 = graphPoints.get(j).y;
            int x2 = graphPoints.get(j + 1).x;
            int y2 = graphPoints.get(j + 1).y;
            g2.draw(new Line2D.Float(x1, y1, x2, y2));
        }

        // Draw the points on the graph
        g2.setColor(pointColor);
        for (Point point : graphPoints) {
            int x = point.x - 4;
            int y = point.y - 4;
            g2.fillOval(x, y, 8, 8);
        }
        g2.dispose();
    }
}


// --- USER DASHBOARD (Completely redesigned with new pages) ---
class UserDashboard extends JFrame {
    private User user; // --- MODIFIED: Made non-final to allow refresh
    private final LoginPage loginPageInstance;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel mainPanel = new JPanel(cardLayout);
    private DashboardPanel dashboardPanel;
    private TaskListView taskListPanel; // Added reference
    private SettingsPanel settingsPanel; // --- NEW ---
    private Map<String, JLabel> sidebarItems = new HashMap<>(); // To manage active state

    public UserDashboard(LoginPage lp, User u) {
        this.user = u;
        this.loginPageInstance = lp;
        setTitle("Todo Petal | " + u.getUsername());
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel basePanel = new JPanel(new BorderLayout());
        basePanel.setBackground(Style.BACKGROUND_COLOR);

        basePanel.add(createSidebar(), BorderLayout.WEST);

        // Initialize and add all panels to the CardLayout
        // MODIFIED ORDER AND CONSTRUCTOR CALL
        taskListPanel = new TaskListView(this, user); // Initialize TaskListView FIRST
        dashboardPanel = new DashboardPanel(this, user, taskListPanel); // NOW pass it to the DashboardPanel
        CalendarViewPanel calendarPanel = new CalendarViewPanel(this, user);

        GoalsPanel goalsPanel = new GoalsPanel(this, user);
        PasswordSaverPanel passwordPanel = new PasswordSaverPanel(this, user);
        ContactsSaverPanel contactsPanel = new ContactsSaverPanel(this, user);
        settingsPanel = new SettingsPanel(this, user); // --- NEW ---


        mainPanel.setOpaque(false);
        mainPanel.add(dashboardPanel, "DASHBOARD");
        mainPanel.add(taskListPanel, "TASKS");
        mainPanel.add(calendarPanel, "CALENDAR");
        mainPanel.add(goalsPanel, "GOALS");
        mainPanel.add(passwordPanel, "PASSWORDS");
        mainPanel.add(contactsPanel, "CONTACTS");
        mainPanel.add(settingsPanel, "SETTINGS"); // --- NEW ---

        basePanel.add(mainPanel, BorderLayout.CENTER);

        add(basePanel);
        showDashboard();
    }

    // --- NEW: Method to refresh user data after an update ---
    public void refreshUserData() {
        Optional<User> updatedUserOpt = new UserDAO().getUserById(this.user.getId());
        if (updatedUserOpt.isPresent()) {
            this.user = updatedUserOpt.get();
            // You might want to update any components that display user info here
            setTitle("Todo Petal | " + this.user.getUsername());
        }
    }


    private JPanel createSidebar() {
        JPanel sidebar = new RoundedPanel(0); // Use RoundedPanel for consistent look
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(125, 69, 153)); // Purple color from image
        sidebar.setPreferredSize(new Dimension(280, 0));
        sidebar.setBorder(new EmptyBorder(20, 10, 20, 10));

        JLabel appName = new JLabel("✓ TaskFlow");
        appName.setFont(Style.FONT_FORM_HEADER.deriveFont(Font.BOLD, 28f));
        appName.setForeground(Color.WHITE);
        appName.setAlignmentX(Component.CENTER_ALIGNMENT);
        appName.setBorder(new EmptyBorder(10, 0, 30, 0));
        sidebar.add(appName);

        addSidebarItem(sidebar, "🏠 Dashboard", "DASHBOARD");
        addSidebarItem(sidebar, "✔️ Task View", "TASKS");
        addSidebarItem(sidebar, "🎯 Goals", "GOALS");
        addSidebarItem(sidebar, "📅 Calendar", "CALENDAR");
        addSidebarItem(sidebar, "🔑 Password Saver", "PASSWORDS");
        addSidebarItem(sidebar, "👤 Contacts", "CONTACTS");


        sidebar.add(Box.createVerticalGlue());

        // Settings and Logout are handled separately
        addSidebarItem(sidebar, "⚙️ Settings", "SETTINGS"); // --- MODIFIED ---

        JLabel logoutLabel = createSidebarItem("↩️ Logout");
        logoutLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                dispose();
                loginPageInstance.setVisible(true);
            }
        });
        sidebar.add(logoutLabel);
        return sidebar;
    }

    private void addSidebarItem(JPanel sidebar, String text, String cardName) {
        JLabel label = createSidebarItem(text);
        label.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                // Refresh data for relevant panels when navigating to them
                switch(cardName) {
                    case "DASHBOARD": dashboardPanel.refreshData(); break;
                    case "TASKS": taskListPanel.loadUserTasks(); break;
                    case "GOALS": ((GoalsPanel)mainPanel.getComponent(3)).loadUserGoals(); break;
                    case "PASSWORDS": ((PasswordSaverPanel)mainPanel.getComponent(4)).loadPasswords(); break;
                    case "CONTACTS": ((ContactsSaverPanel)mainPanel.getComponent(5)).loadContacts(); break;
                    case "SETTINGS": settingsPanel.loadUserInfo(); break; // --- NEW ---
                }
                setActiveSidebarItem(text);
                cardLayout.show(mainPanel, cardName);
            }
        });
        sidebarItems.put(text, label);
        sidebar.add(label);
    }

    private JLabel createSidebarItem(String text) {
        JLabel label = new JLabel(text);
        label.setFont(Style.FONT_SIDEBAR.deriveFont(20f));
        label.setCursor(new Cursor(Cursor.HAND_CURSOR));
        label.setBorder(new EmptyBorder(15, 25, 15, 25));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setMaximumSize(new Dimension(Integer.MAX_VALUE, label.getPreferredSize().height + 20));
        label.setForeground(new Color(220, 200, 230)); // Inactive color (light purple/white)
        return label;
    }

    private void setActiveSidebarItem(String text) {
        sidebarItems.forEach((itemText, itemLabel) -> {
            JPanel parent = (JPanel) itemLabel.getParent();
            itemLabel.setOpaque(true);
            if (itemText.equals(text)) {
                itemLabel.setBackground(new Color(105, 49, 133)); // Active background color
                itemLabel.setForeground(Color.WHITE); // Active text color
            } else {
                itemLabel.setBackground(parent.getBackground()); // Inactive (transparent to parent)
                itemLabel.setForeground(new Color(220, 200, 230)); // Inactive text color
            }
        });
    }

    // --- Navigation Methods ---
    public void showTaskEditor(Task t) {
        JPanel ev = new TaskEditorPanel(user, t, () -> {
            dashboardPanel.refreshData(); // Refresh all data on return
            taskListPanel.loadUserTasks(); // Also refresh tasks list
            showDashboard();
        });
        mainPanel.add(ev, "EDITOR");
        cardLayout.show(mainPanel, "EDITOR");
    }

    public void showDashboard() {
        setActiveSidebarItem("🏠 Dashboard");
        dashboardPanel.refreshData();
        cardLayout.show(mainPanel, "DASHBOARD");
    }

    // New method to show tasks page
    public void showTasks() {
        setActiveSidebarItem("✔️ Task View");
        taskListPanel.loadUserTasks(); // Ensure tasks are loaded/refreshed
        cardLayout.show(mainPanel, "TASKS");
    }
}

// REDESIGNED DashboardPanel to match the image
 class DashboardPanel extends JPanel {
    private final User user;
    private final UserDashboard parentFrame;
    private final TaskDAO taskDAO = new TaskDAO();
    private JLabel todoCountLabel, inProgressCountLabel, doneCountLabel;
    private LineChartPanel lineChart;
    private MiniCalendarPanel miniCalendar;
    private DefaultTableModel tasksTableModel;
    private TaskListView taskListPanel; // Reference to the actual task list panel

    // MODIFIED CONSTRUCTOR
    public DashboardPanel(UserDashboard parent, User user, TaskListView taskListPanel) {
        this.parentFrame = parent;
        this.user = user;
        this.taskListPanel = taskListPanel; // Assign the panel directly

        setBackground(new Color(236, 239, 241)); // Light grey background like image
//...// Light grey background like image
        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(15, 25, 15, 25));

        add(createHeader(), BorderLayout.NORTH);

        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplit.setResizeWeight(0.7);
        mainSplit.setOpaque(false);
        mainSplit.setBorder(null);

        mainSplit.setLeftComponent(createMainContentArea());
        mainSplit.setRightComponent(createRightSidebar());

        add(mainSplit, BorderLayout.CENTER);

        refreshData(); // Load initial data
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        RoundedTextField searchField = new RoundedTextField(25);
        searchField.setText("Search Tasks...");
        searchField.setForeground(Color.GRAY);
        searchField.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (searchField.getText().equals("Search Tasks...")) {
                    searchField.setText("");
                    searchField.setForeground(Color.BLACK);
                }
            }
            public void focusLost(FocusEvent e) {
                if (searchField.getText().isEmpty()) {
                    searchField.setForeground(Color.GRAY);
                    searchField.setText("Search Tasks...");
                }
            }
        });
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { filterTasks(); }
            @Override public void removeUpdate(DocumentEvent e) { filterTasks(); }
            @Override public void changedUpdate(DocumentEvent e) { filterTasks(); }
            private void filterTasks() {
                String query = searchField.getText();
                if (!query.equals("Search Tasks...") && !query.isEmpty()) {
                    parentFrame.showTasks();
                    if(taskListPanel != null) {
                        taskListPanel.filterTasksBy(query);
                    }
                }
            }
        });


        JLabel profileIcon = new JLabel("👤 " + user.getUsername());
        profileIcon.setFont(Style.FONT_LABEL.deriveFont(Font.BOLD));
        profileIcon.setBorder(new EmptyBorder(0, 0, 0, 15));

        header.add(searchField, BorderLayout.CENTER);
        header.add(profileIcon, BorderLayout.EAST);
        return header;
    }

    private JPanel createMainContentArea() {
        JPanel mainContent = new JPanel(new GridBagLayout());
        mainContent.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(8, 8, 8, 8);

        // Row 1: Statistic Cards
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.33; gbc.weighty = 0.2;
        mainContent.add(createStatCard("To-Do Tasks", todoCountLabel = new JLabel("0"), new Color(66, 133, 244)), gbc);
        gbc.gridx = 1; gbc.gridy = 0;
        mainContent.add(createStatCard("In Progress", inProgressCountLabel = new JLabel("0"), new Color(251, 188, 5)), gbc);
        gbc.gridx = 2; gbc.gridy = 0;
        mainContent.add(createStatCard("Completed", doneCountLabel = new JLabel("0"), new Color(52, 168, 83)), gbc);

        // Row 2: Line Chart
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 3; gbc.weightx = 1.0; gbc.weighty = 0.4;
        mainContent.add(createChartCard("Progress Overview", lineChart = new LineChartPanel()), gbc);

        // Row 3: Tasks Table
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 3; gbc.weightx = 1.0; gbc.weighty = 0.4;
        mainContent.add(createTasksTableCard(), gbc);

        return mainContent;
    }

    private JPanel createRightSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setOpaque(false);

        // Calendar card
        RoundedPanel calendarCard = new RoundedPanel(20);
        calendarCard.setBackground(Style.CARD_BACKGROUND_COLOR);
        calendarCard.setLayout(new BorderLayout());
        miniCalendar = new MiniCalendarPanel();
        calendarCard.add(miniCalendar, BorderLayout.CENTER);
        calendarCard.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Upcoming Deadlines title
        JLabel deadlinesTitle = new JLabel("Upcoming Deadlines");
        deadlinesTitle.setFont(Style.FONT_CARD_TITLE);
        deadlinesTitle.setBorder(new EmptyBorder(15, 10, 5, 10));
        deadlinesTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        sidebar.add(calendarCard);
        sidebar.add(deadlinesTitle);
        // Upcoming deadlines content will be part of the tasks table in main panel

        return sidebar;
    }

    private RoundedPanel createStatCard(String title, JLabel valueLabel, Color bgColor) {
        RoundedPanel card = new RoundedPanel(20);
        card.setBackground(bgColor);
        card.setLayout(new BorderLayout(15, 15));
        card.setBorder(new EmptyBorder(20, 25, 20, 25));

        valueLabel.setFont(Style.FONT_CARD_VALUE.deriveFont(48f));
        valueLabel.setForeground(Color.WHITE);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(Style.FONT_CARD_TITLE.deriveFont(22f));
        titleLabel.setForeground(new Color(240, 240, 240));

        card.add(valueLabel, BorderLayout.CENTER);
        card.add(titleLabel, BorderLayout.SOUTH);
        return card;
    }

    private RoundedPanel createChartCard(String title, JComponent chart) {
        RoundedPanel card = new RoundedPanel(20);
        card.setBackground(Style.CARD_BACKGROUND_COLOR);
        card.setLayout(new BorderLayout(10, 10));
        card.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(Style.FONT_CARD_TITLE);
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(chart, BorderLayout.CENTER);

        return card;
    }

    private RoundedPanel createTasksTableCard() {
        RoundedPanel card = new RoundedPanel(20);
        card.setBackground(Style.CARD_BACKGROUND_COLOR);
        card.setLayout(new BorderLayout(10,10));
        card.setBorder(new EmptyBorder(15,15,15,15));

        JLabel title = new JLabel("My Tasks");
        title.setFont(Style.FONT_CARD_TITLE);
        card.add(title, BorderLayout.NORTH);

        tasksTableModel = new DefaultTableModel(new String[]{"Task", "Project", "Due Date", "Priority"}, 0){
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable tasksTable = new JTable(tasksTableModel);
        tasksTable.setRowHeight(35);
        tasksTable.setGridColor(Style.BACKGROUND_COLOR);
        tasksTable.setSelectionBackground(Style.ACCENT_COLOR);
        tasksTable.getTableHeader().setFont(Style.FONT_BUTTON);
        tasksTable.getTableHeader().setBackground(Style.CARD_BACKGROUND_COLOR);

        JScrollPane sp = new JScrollPane(tasksTable);
        sp.setBorder(BorderFactory.createEmptyBorder());
        card.add(sp, BorderLayout.CENTER);

        return card;
    }

    public void refreshData() {
        // Refresh counts
        Map<String, Long> counts = taskDAO.getTaskCountByStatus(user.getId());
        long todo = counts.getOrDefault("TODO", 0L);
        long inProg = counts.getOrDefault("IN_PROGRESS", 0L);
        long done = counts.getOrDefault("DONE", 0L);

        todoCountLabel.setText(String.valueOf(todo));
        inProgressCountLabel.setText(String.valueOf(inProg));
        doneCountLabel.setText(String.valueOf(done));

        // Refresh line chart (last 7 days of completed tasks)
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(6);
        lineChart.setData(taskDAO.getCompletedTasksByDay(user.getId(), start, end));

        // Refresh mini calendar
        miniCalendar.setTaskDates(taskDAO.getTaskDatesInMonth(user.getId(), LocalDate.now().getYear(), LocalDate.now().getMonthValue()));

        // Refresh tasks table
        tasksTableModel.setRowCount(0); // Clear existing data
        List<Task> upcomingTasks = taskDAO.getUpcomingTasksForUser(user.getId(), 5);
        for(Task task : upcomingTasks) {
            tasksTableModel.addRow(new Object[]{
                    task.getDescription().split("\n")[0],
                    task.getCategory().name(),
                    task.getDueDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy")),
                    task.getPriority().name()
            });
        }
    }
}


// --- TASK LIST VIEW ---
class TaskListView extends JPanel {
    private final User user;
    private DefaultListModel<Task> listModel;
    private JList<Task> taskList;
    private JComboBox<String> sortBox, filterBox;
    private int hoveredIndex = -1;
    private final TaskDAO taskDAO = new TaskDAO();
    private final UserDashboard parentFrame;
    private List<Task> allTasks;
    private JTextField searchField;

    public TaskListView(UserDashboard parent, User user) {
        this.parentFrame = parent;
        this.user = user;
        setLayout(new BorderLayout(15, 15));
        setOpaque(false);
        setBorder(new EmptyBorder(15, 25, 15, 25));

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout(15,0));
        headerPanel.setOpaque(false);
        JLabel todayLabel = new JLabel("My Tasks");
        todayLabel.setFont(Style.FONT_HEADER);

        // NEW: Search field in the header
        searchField = new RoundedTextField(30);
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { filterAndSortTasks(); }
            @Override public void removeUpdate(DocumentEvent e) { filterAndSortTasks(); }
            @Override public void changedUpdate(DocumentEvent e) { filterAndSortTasks(); }
        });

        headerPanel.add(todayLabel, BorderLayout.WEST);
        headerPanel.add(searchField, BorderLayout.CENTER);


        // Header controls
        JPanel headerControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        headerControls.setOpaque(false);
        RoundedButton addBtn = new RoundedButton("+ Add New");
        addBtn.setBackground(Style.PRIMARY_COLOR);
        addBtn.setForeground(Color.WHITE);
        addBtn.setPreferredSize(new Dimension(150, 50));
        headerControls.add(addBtn);

        headerPanel.add(headerControls, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Center Panel
        JPanel centerPanel = new JPanel(new BorderLayout(10, 20));
        centerPanel.setOpaque(false);
        JPanel controlsPanel = new JPanel(new BorderLayout());
        controlsPanel.setOpaque(false);

        JPanel sortFilterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        sortFilterPanel.setOpaque(false);
        sortFilterPanel.add(new JLabel("Sort by:"));
        sortBox = new JComboBox<>(new String[]{"Status", "Due Date", "Priority"});
        sortFilterPanel.add(sortBox);
        sortFilterPanel.add(new JLabel("  Filter:"));
        filterBox = new JComboBox<>(new String[]{"All Tasks", "Active Tasks", "Done Tasks"});
        sortFilterPanel.add(filterBox);

        controlsPanel.add(sortFilterPanel, BorderLayout.EAST);
        centerPanel.add(controlsPanel, BorderLayout.NORTH);

        listModel = new DefaultListModel<>();
        taskList = new JList<>(listModel);
        taskList.setCellRenderer(new TaskCardRenderer(() -> hoveredIndex));
        taskList.setBackground(getBackground());
        taskList.setSelectionModel(new DefaultListSelectionModel() {
            @Override
            public void setSelectionInterval(int i, int i1) {
            }
        });

        loadUserTasks();
        JScrollPane sp = new JScrollPane(taskList);
        sp.setBorder(null);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        centerPanel.add(sp, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        addBtn.addActionListener(e -> parentFrame.showTaskEditor(null));
        sortBox.addActionListener(e -> filterAndSortTasks());
        filterBox.addActionListener(e -> filterAndSortTasks());

        taskList.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int i = taskList.locationToIndex(e.getPoint());
                if (i != hoveredIndex) {
                    hoveredIndex = i;
                    taskList.repaint();
                }
            }
        });
        taskList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                hoveredIndex = -1;
                taskList.repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int i = taskList.locationToIndex(e.getPoint());
                    if (i >= 0) parentFrame.showTaskEditor(listModel.getElementAt(i));
                }
            }
        });
    }

    public void loadUserTasks() { // Made public so Dashboard can call it
        allTasks = taskDAO.getTasksForUser(user.getId());
        filterAndSortTasks();
    }

    public void filterTasksBy(String query) {
        searchField.setText(query);
        filterAndSortTasks();
    }

    private void filterAndSortTasks() {
        if (allTasks == null) return;

        listModel.clear();
        String searchQuery = searchField.getText().toLowerCase();

        List<Task> searchedTasks = allTasks.stream()
                .filter(task -> {
                    if(searchQuery.isEmpty()) return true;
                    return task.getDescription().toLowerCase().contains(searchQuery) ||
                            task.getPriority().name().toLowerCase().contains(searchQuery) ||
                            task.getDueDate().format(DateTimeFormatter.ISO_LOCAL_DATE).contains(searchQuery);
                })
                .collect(Collectors.toList());

        String f = (String) filterBox.getSelectedItem();
        List<Task> filtered = searchedTasks.stream()
                .filter(t -> "All Tasks".equals(f) || ("Active Tasks".equals(f) && t.getStatus() != Status.DONE) || ("Done Tasks".equals(f) && t.getStatus() == Status.DONE))
                .collect(Collectors.toList());

        String s = (String) sortBox.getSelectedItem();
        if ("Due Date".equals(s))
            filtered.sort(Comparator.comparing(Task::getDueDate, Comparator.nullsLast(Comparator.naturalOrder())));
        else if ("Priority".equals(s)) filtered.sort(Comparator.comparing(Task::getPriority));
        else if ("Status".equals(s)) filtered.sort(Comparator.comparing(Task::getStatus));

        filtered.forEach(listModel::addElement);
    }
}


// --- DEDICATED TASK EDITOR PANEL ---
class TaskEditorPanel extends JPanel {
    private final User user;
    private Task task;
    private final Runnable onSaveCallback;
    private JTextField titleField;
    private JComboBox<Priority> priorityBox;
    private JComboBox<Category> categoryBox;
    private JComboBox<Status> statusBox;
    private JLabel selectedDateLabel;
    private LocalDate selectedDate;
    private final boolean isNewTask;
    private final TaskDAO taskDAO = new TaskDAO();
    private JPanel subtasksPanel;
    private List<SubtaskComponent> subtaskComponents = new ArrayList<>();

    // A small inner class to hold subtask UI components
    private static class SubtaskComponent {
        JCheckBox checkBox;
        JTextField textField;
        JButton removeButton;
        JPanel panel;

        SubtaskComponent(String text, boolean isCompleted) {
            panel = new JPanel(new BorderLayout(5, 0));
            panel.setOpaque(false);
            checkBox = new JCheckBox();
            checkBox.setSelected(isCompleted);
            textField = new JTextField(text);
            removeButton = new JButton("x");
            removeButton.setForeground(Color.RED);
            panel.add(checkBox, BorderLayout.WEST);
            panel.add(textField, BorderLayout.CENTER);
            panel.add(removeButton, BorderLayout.EAST);
        }
    }


    public TaskEditorPanel(User user, Task task, Runnable onSaveCallback) {
        this.user = user;
        this.isNewTask = (task == null);
        this.task = isNewTask ? new Task(user.getId(), "", Priority.MEDIUM, Category.OTHER, LocalDate.now(), Status.TODO) : task;
        this.onSaveCallback = onSaveCallback;
        this.selectedDate = this.task.getDueDate();

        setBackground(Style.BACKGROUND_COLOR);
        setBorder(new EmptyBorder(20, 100, 20, 100));
        setLayout(new BorderLayout(20, 20));
        String titleText = isNewTask ? "Create New Task" : "Edit Task";
        JLabel title = new JLabel(titleText, SwingConstants.CENTER);
        title.setFont(Style.FONT_HEADER);
        add(title, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 5, 10, 5);
        gbc.weightx = 1.0;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        formPanel.add(new JLabel("Task Title"), gbc);
        gbc.gridy++;
        titleField = new JTextField(this.task.getDescription().split("\n")[0]);
        titleField.setFont(Style.FONT_FIELD);
        formPanel.add(titleField, gbc);

        // --- NEW Subtask Panel ---
        gbc.gridy++;
        formPanel.add(new JLabel("Sub-tasks"), gbc);
        gbc.gridy++;
        gbc.weighty = 1.0; // Allow subtask panel to grow
        gbc.fill = GridBagConstraints.BOTH;
        subtasksPanel = new JPanel();
        subtasksPanel.setLayout(new BoxLayout(subtasksPanel, BoxLayout.Y_AXIS));
        subtasksPanel.setOpaque(false);

        JScrollPane subtaskScrollPane = new JScrollPane(subtasksPanel);
        subtaskScrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        formPanel.add(subtaskScrollPane, gbc);

        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridy++;
        RoundedButton addSubtaskBtn = new RoundedButton("+ Add Sub-task");
        addSubtaskBtn.setBackground(Style.TEXT_SECONDARY_COLOR);
        addSubtaskBtn.setForeground(Color.WHITE);
        addSubtaskBtn.addActionListener(e -> addSubtask("", false));
        formPanel.add(addSubtaskBtn, gbc);


        gbc.gridy++;
        gbc.gridwidth = 1;
        formPanel.add(new JLabel("Priority"), gbc);
        gbc.gridy++;
        priorityBox = new JComboBox<>(Priority.values());
        priorityBox.setSelectedItem(this.task.getPriority());
        formPanel.add(priorityBox, gbc);
        gbc.gridx = 1;
        gbc.gridy--;
        formPanel.add(new JLabel("Category"), gbc);
        gbc.gridy++;
        categoryBox = new JComboBox<>(Category.values());
        categoryBox.setSelectedItem(this.task.getCategory());
        formPanel.add(categoryBox, gbc);
        gbc.gridx = 0;
        gbc.gridy++;
        formPanel.add(new JLabel("Status"), gbc);
        gbc.gridy++;
        statusBox = new JComboBox<>(Status.values());
        statusBox.setSelectedItem(this.task.getStatus());
        formPanel.add(statusBox, gbc);

        gbc.gridx = 1;
        gbc.gridy--;
        JPanel datePanel = new JPanel(new BorderLayout());
        datePanel.setOpaque(false);
        RoundedButton calBtn = new RoundedButton("Select Date");
        calBtn.setBackground(Style.ACCENT_COLOR);
        calBtn.setForeground(Color.WHITE);
        selectedDateLabel = new JLabel();
        updateSelectedDateLabel();
        datePanel.add(calBtn, BorderLayout.NORTH);
        datePanel.add(selectedDateLabel, BorderLayout.SOUTH);
        gbc.gridy++;
        formPanel.add(datePanel, gbc);
        add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setOpaque(false);
        RoundedButton saveBtn = new RoundedButton(isNewTask ? "Create Task" : "Save Changes");
        saveBtn.setBackground(Style.PRIMARY_COLOR);
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setPreferredSize(new Dimension(200, 50));
        RoundedButton cancelBtn = new RoundedButton("Cancel");
        cancelBtn.setBackground(Style.TEXT_SECONDARY_COLOR);
        cancelBtn.setForeground(Color.WHITE);
        cancelBtn.setPreferredSize(new Dimension(150, 50));
        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);
        add(buttonPanel, BorderLayout.SOUTH);

        calBtn.addActionListener(e -> openCalendar());
        saveBtn.addActionListener(e -> saveTask());
        cancelBtn.addActionListener(e -> onSaveCallback.run());

        parseAndLoadSubtasks();
    }

    private void parseAndLoadSubtasks() {
        String[] lines = this.task.getDescription().split("\n");
        if (lines.length > 1) {
            for (int i = 1; i < lines.length; i++) {
                String line = lines[i].trim();
                if (line.startsWith("[x] ")) {
                    addSubtask(line.substring(4), true);
                } else if (line.startsWith("[ ] ")) {
                    addSubtask(line.substring(4), false);
                }
            }
        }
    }

    private void addSubtask(String text, boolean isCompleted) {
        SubtaskComponent stc = new SubtaskComponent(text, isCompleted);
        stc.removeButton.addActionListener(e -> {
            subtasksPanel.remove(stc.panel);
            subtaskComponents.remove(stc);
            subtasksPanel.revalidate();
            subtasksPanel.repaint();
        });
        subtaskComponents.add(stc);
        subtasksPanel.add(stc.panel);
        subtasksPanel.revalidate();
        subtasksPanel.repaint();
    }


    private void openCalendar() {
        JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Select Date", true);
        JCalendarPanel cp = new JCalendarPanel(selectedDate);
        d.add(cp);
        d.pack();
        d.setLocationRelativeTo(this);
        d.setVisible(true);
        if (cp.getSelectedDate() != null) {
            this.selectedDate = cp.getSelectedDate();
            updateSelectedDateLabel();
        }
    }

    private void updateSelectedDateLabel() {
        selectedDateLabel.setText(selectedDate.format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy")));
    }

    private void saveTask() {
        if (titleField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Title cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        StringBuilder fullDesc = new StringBuilder(titleField.getText().trim());
        for (SubtaskComponent stc : subtaskComponents) {
            fullDesc.append("\n")
                    .append(stc.checkBox.isSelected() ? "[x] " : "[ ] ")
                    .append(stc.textField.getText().trim());
        }

        task.setDescription(fullDesc.toString());
        task.setPriority((Priority) priorityBox.getSelectedItem());
        task.setCategory((Category) categoryBox.getSelectedItem());
        task.setStatus((Status) statusBox.getSelectedItem());
        task.setDueDate(selectedDate);

        if (isNewTask) taskDAO.addTask(task);
        else taskDAO.updateTask(task);
        onSaveCallback.run();
    }
}

// --- INTERACTIVE CALENDAR COMPONENT ---
class JCalendarPanel extends JPanel {
    private LocalDate currentDate;
    private LocalDate selectedDate;
    private JLabel monthLabel;
    private JPanel daysPanel;

    public JCalendarPanel(LocalDate initialDate) {
        this.selectedDate = initialDate;
        this.currentDate = initialDate.withDayOfMonth(1);
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));
        JPanel h = new JPanel(new BorderLayout());
        JButton p = new JButton("<");
        JButton n = new JButton(">");
        monthLabel = new JLabel("", SwingConstants.CENTER);
        monthLabel.setFont(Style.FONT_BUTTON);
        h.add(p, BorderLayout.WEST);
        h.add(monthLabel, BorderLayout.CENTER);
        h.add(n, BorderLayout.EAST);
        add(h, BorderLayout.NORTH);
        daysPanel = new JPanel(new GridLayout(0, 7, 5, 5));
        add(daysPanel, BorderLayout.CENTER);
        p.addActionListener(e -> {
            currentDate = currentDate.minusMonths(1);
            drawCalendar();
        });
        n.addActionListener(e -> {
            currentDate = currentDate.plusMonths(1);
            drawCalendar();
        });
        drawCalendar();
    }

    private void drawCalendar() {
        daysPanel.removeAll();
        monthLabel.setText(currentDate.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
        String[] dayNames = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (String name : dayNames) {
            daysPanel.add(new JLabel(name, SwingConstants.CENTER));
        }
        int firstDayOfWeek = currentDate.getDayOfWeek().getValue() % 7;
        for (int i = 0; i < firstDayOfWeek; i++) {
            daysPanel.add(new JLabel(""));
        }
        for (int day = 1; day <= currentDate.lengthOfMonth(); day++) {
            final int d = day;
            JLabel dayLabel = new JLabel(String.valueOf(day), SwingConstants.CENTER);
            dayLabel.setOpaque(true);
            dayLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
            LocalDate thisDate = currentDate.withDayOfMonth(day);
            if (thisDate.equals(selectedDate)) {
                dayLabel.setBackground(Style.PRIMARY_COLOR);
                dayLabel.setForeground(Color.WHITE);
            } else if (thisDate.equals(LocalDate.now())) {
                dayLabel.setBackground(Style.ACCENT_COLOR);
                dayLabel.setForeground(Color.WHITE);
            } else {
                dayLabel.setBackground(Style.CARD_BACKGROUND_COLOR);
            }
            dayLabel.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    selectedDate = currentDate.withDayOfMonth(d);
                    Window w = SwingUtilities.getWindowAncestor(JCalendarPanel.this);
                    if (w instanceof JDialog) ((JDialog) w).dispose();
                }
            });
            daysPanel.add(dayLabel);
        }
        revalidate();
        repaint();
    }

    public LocalDate getSelectedDate() {
        return selectedDate;
    }
}

// --- CUSTOM RENDERERS ---
class TaskCardRenderer implements ListCellRenderer<Task> {
    private final java.util.function.Supplier<Integer> hoveredIndexSupplier;

    public TaskCardRenderer(java.util.function.Supplier<Integer> h) {
        this.hoveredIndexSupplier = h;
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends Task> list, Task task, int index, boolean isSelected, boolean cellHasFocus) {
        RoundedPanel card = new RoundedPanel(25);
        card.setBackground(Style.CARD_BACKGROUND_COLOR);
        card.setLayout(new BorderLayout(15, 15));
        card.setBorder(new EmptyBorder(20, 20, 20, 20));
        if (index == hoveredIndexSupplier.get()) card.setBorderColor(Style.PRIMARY_COLOR);

        JPanel westPanel = new JPanel();
        westPanel.setOpaque(false);
        westPanel.setLayout(new BoxLayout(westPanel, BoxLayout.Y_AXIS));
        westPanel.setAlignmentY(Component.TOP_ALIGNMENT);
        JPanel pi = new JPanel();
        pi.setPreferredSize(new Dimension(8, 8));
        pi.setBackground(task.getPriority() == Priority.HIGH ? Style.PRIORITY_HIGH : task.getPriority() == Priority.MEDIUM ? Style.PRIORITY_MEDIUM : Style.PRIORITY_LOW);
        westPanel.add(pi);
        card.add(westPanel, BorderLayout.WEST);

        JPanel details = new JPanel();
        details.setOpaque(false);
        details.setLayout(new BoxLayout(details, BoxLayout.Y_AXIS));
        String title = task.getDescription().split("\n")[0];
        JLabel dl = new JLabel(title);
        dl.setFont(Style.FONT_TASK_DESC);
        String dt = task.getCategory().icon + " " + task.getCategory().name() + "  |  Due: " + task.getDueDate().format(DateTimeFormatter.ofPattern("dd MMM"));
        JLabel sl = new JLabel(dt);
        sl.setFont(Style.FONT_TASK_DETAILS);
        sl.setForeground(Style.TEXT_SECONDARY_COLOR);
        if (task.getStatus() == Status.DONE) {
            dl.setText("<html><strike>" + title + "</strike></html>");
            dl.setForeground(Style.TEXT_SECONDARY_COLOR);
        } else {
            dl.setForeground(Style.TEXT_PRIMARY_COLOR);
        }
        details.add(dl);
        details.add(Box.createRigidArea(new Dimension(0, 5)));
        details.add(sl);

        String[] lines = task.getDescription().split("\n");
        int totalSubtasks = 0;
        int completedSubtasks = 0;
        for (String line : lines) {
            if (line.trim().startsWith("[ ]")) totalSubtasks++;
            if (line.trim().startsWith("[x]")) {
                totalSubtasks++;
                completedSubtasks++;
            }
        }
        if (totalSubtasks > 0) {
            details.add(Box.createRigidArea(new Dimension(0, 8)));
            JLabel subtaskLabel = new JLabel("Subtasks: " + completedSubtasks + "/" + totalSubtasks + " completed");
            subtaskLabel.setFont(Style.FONT_TASK_DETAILS);
            subtaskLabel.setForeground(Style.TEXT_SECONDARY_COLOR);
            details.add(subtaskLabel);
        }
        card.add(details, BorderLayout.CENTER);

        JLabel statusLabel = new JLabel(task.getStatus().toString().replace("_", " "));
        statusLabel.setFont(Style.FONT_TASK_DETAILS);
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setOpaque(true);
        statusLabel.setBorder(new EmptyBorder(3, 8, 3, 8));
        switch (task.getStatus()) {
            case TODO:
                statusLabel.setBackground(Style.STATUS_TODO);
                break;
            case IN_PROGRESS:
                statusLabel.setBackground(Style.STATUS_IN_PROGRESS);
                break;
            case DONE:
                statusLabel.setBackground(Style.STATUS_DONE);
                break;
        }
        card.add(statusLabel, BorderLayout.EAST);
        return card;
    }
}

class ModernTableCellRenderer extends DefaultTableCellRenderer {
    public Component getTableCellRendererComponent(JTable t, Object v, boolean is, boolean hf, int r, int c) {
        Component co = super.getTableCellRendererComponent(t, v, is, hf, r, c);
        co.setBackground(is ? t.getSelectionBackground() : r % 2 == 0 ? Style.CARD_BACKGROUND_COLOR : Style.BACKGROUND_COLOR);
        setBorder(new EmptyBorder(5, 10, 5, 10));
        return co;
    }
}

// --- ADMIN DASHBOARD ---
class AdminDashboard extends JFrame {
    private final LoginPage loginPageInstance;
    private DefaultTableModel userTableModel;
    private JTable userTable;
    private final UserDAO userDAO = new UserDAO();
    private final TaskDAO taskDAO = new TaskDAO(); // --- NEW ---
    private final GoalDAO goalDAO = new GoalDAO(); // --- NEW ---
    private JLabel totalUsersLabel, totalTasksLabel, activeGoalsLabel; // --- MODIFIED ---

    public AdminDashboard(LoginPage lp, User adminUser) {
        this.loginPageInstance = lp;
        setTitle("Todo Petal - Admin Panel");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(15, 15));
        getContentPane().setBackground(Style.BACKGROUND_COLOR);
        getRootPane().setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel hp = new JPanel(new BorderLayout());
        hp.setOpaque(false);
        JLabel h = new JLabel("Admin Dashboard");
        h.setFont(Style.FONT_HEADER);
        hp.add(h, BorderLayout.WEST);
        RoundedButton lob = new RoundedButton("Logout");
        lob.setBackground(Style.DANGER_COLOR);
        lob.setForeground(Color.WHITE);
        lob.setPreferredSize(new Dimension(140, 45));
        lob.addActionListener(e -> {
            dispose();
            loginPageInstance.setVisible(true);
        });
        hp.add(lob, BorderLayout.EAST);
        add(hp, BorderLayout.NORTH);

        JPanel mc = new JPanel(new BorderLayout(15, 15));
        mc.setOpaque(false);
        RoundedPanel sp = createStatsPanel();
        mc.add(sp, BorderLayout.NORTH);

        RoundedPanel up = new RoundedPanel(25);
        up.setBackground(Style.CARD_BACKGROUND_COLOR);
        up.setLayout(new BorderLayout(10, 10));
        up.setBorder(new EmptyBorder(10, 10, 10, 10));
        userTableModel = new DefaultTableModel(new String[]{"ID", "Username", "Role", "Email", "Phone", "Last Active"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        userTable = new JTable(userTableModel);
        setupModernTable(userTable);

        up.add(new JScrollPane(userTable), BorderLayout.CENTER);
        JPanel bp = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bp.setOpaque(false);
        bp.setBackground(Style.CARD_BACKGROUND_COLOR);
        RoundedButton addBtn = new RoundedButton("Add User");
        addBtn.setBackground(Style.PRIMARY_COLOR);
        addBtn.setForeground(Color.WHITE);
        bp.add(addBtn);

        // --- NEW BUTTONS ---
        RoundedButton editBtn = new RoundedButton("Edit User");
        editBtn.setBackground(Style.ACCENT_COLOR);
        editBtn.setForeground(Color.WHITE);
        bp.add(editBtn);
        RoundedButton viewTasksBtn = new RoundedButton("View Tasks");
        viewTasksBtn.setBackground(Style.TEXT_SECONDARY_COLOR);
        viewTasksBtn.setForeground(Color.WHITE);
        bp.add(viewTasksBtn);
        // --- END NEW ---

        RoundedButton deleteBtn = new RoundedButton("Delete User");
        deleteBtn.setBackground(Style.DANGER_COLOR);
        deleteBtn.setForeground(Color.WHITE);
        bp.add(deleteBtn);
        up.add(bp, BorderLayout.SOUTH);

        addBtn.addActionListener(e -> editUser(null)); // MODIFIED
        editBtn.addActionListener(e -> editSelectedUser()); // NEW
        viewTasksBtn.addActionListener(e -> viewSelectedUserTasks()); // NEW
        deleteBtn.addActionListener(e -> deleteSelectedUser());

        mc.add(up, BorderLayout.CENTER);
        add(mc, BorderLayout.CENTER);
        loadData(); // MODIFIED
    }

    private RoundedPanel createStatsPanel() {
        RoundedPanel p = new RoundedPanel(25);
        p.setBackground(Style.CARD_BACKGROUND_COLOR);
        p.setLayout(new FlowLayout(FlowLayout.LEFT, 30, 20));
        totalUsersLabel = new JLabel("Total Users: 0");
        totalUsersLabel.setFont(Style.FONT_FORM_HEADER);
        totalUsersLabel.setForeground(Style.TEXT_PRIMARY_COLOR);
        p.add(totalUsersLabel);
        // --- NEW LABELS ---
        totalTasksLabel = new JLabel("Total Tasks: 0");
        totalTasksLabel.setFont(Style.FONT_FORM_HEADER);
        totalTasksLabel.setForeground(Style.TEXT_PRIMARY_COLOR);
        p.add(totalTasksLabel);
        activeGoalsLabel = new JLabel("Active Goals: 0");
        activeGoalsLabel.setFont(Style.FONT_FORM_HEADER);
        activeGoalsLabel.setForeground(Style.TEXT_PRIMARY_COLOR);
        p.add(activeGoalsLabel);
        return p;
    }

    private void loadData() {
        userTableModel.setRowCount(0);
        List<User> users = userDAO.getAllUsers();
        for (User u : users) {
            userTableModel.addRow(new Object[]{u.getId(), u.getUsername(), u.getRole(), u.getEmail(), u.getPhone(), formatTimeAgo(u.getLastLoginTime())});
        }
        // --- NEW STATS ---
        totalUsersLabel.setText("Total Users: " + users.size());
        totalTasksLabel.setText("Total Tasks: " + taskDAO.getSystemTotalTaskCount());
        activeGoalsLabel.setText("Active Goals: " + goalDAO.getSystemActiveGoalsCount());
    }

    private String formatTimeAgo(LocalDateTime t) {
        if (t == null) return "Never";
        Duration d = Duration.between(t, LocalDateTime.now());
        long s = d.getSeconds();
        if (s < 60) return "Online Now";
        if (s < 3600) return (s / 60) + "m ago";
        if (s < 86400) return (s / 3600) + "h ago";
        if (s < 172800) return "Yesterday";
        return t.format(DateTimeFormatter.ofPattern("dd MMM yyyy"));
    }

    private void editUser(User userToEdit) {
        UserEditorDialog dialog = new UserEditorDialog(this, userToEdit);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            loadData();
        }
    }

    private void editSelectedUser() {
        int row = userTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user to edit.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int userId = (int) userTableModel.getValueAt(row, 0);
        userDAO.getUserById(userId).ifPresent(this::editUser);
    }

    private void viewSelectedUserTasks() {
        int row = userTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user to view their tasks.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int userId = (int) userTableModel.getValueAt(row, 0);
        String username = (String) userTableModel.getValueAt(row, 1);
        List<Task> userTasks = taskDAO.getTasksForUser(userId);
        UserTasksDialog dialog = new UserTasksDialog(this, username, userTasks);
        dialog.setVisible(true);
    }

    private void deleteSelectedUser() {
        int row = userTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user to delete.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int userId = (int) userTableModel.getValueAt(row, 0);
        String username = (String) userTableModel.getValueAt(row, 1);
        if (username.equals("admin")) {
            JOptionPane.showMessageDialog(this, "Cannot delete the primary admin account.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int c = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete user '" + username + "'?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
        if (c == JOptionPane.YES_OPTION) {
            if (userDAO.deleteUser(userId)) loadData();
            else JOptionPane.showMessageDialog(this, "Failed to delete user.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void setupModernTable(JTable t) {
        t.setRowHeight(40);
        t.setGridColor(Style.BACKGROUND_COLOR);
        t.setSelectionBackground(Style.PRIMARY_COLOR);
        t.setSelectionForeground(Color.WHITE);
        t.setDefaultRenderer(Object.class, new ModernTableCellRenderer());
        JTableHeader h = t.getTableHeader();
        h.setBackground(Style.BACKGROUND_COLOR);
        h.setForeground(Style.TEXT_PRIMARY_COLOR);
        h.setPreferredSize(new Dimension(100, 40));
        // Hide ID column
        t.getColumnModel().getColumn(0).setMinWidth(0);
        t.getColumnModel().getColumn(0).setMaxWidth(0);
        t.getColumnModel().getColumn(0).setWidth(0);

        // --- NEW: Double-click to edit ---
        t.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    editSelectedUser();
                }
            }
        });
    }
}

// --- DIALOG FOR ADMIN TO ADD/EDIT USERS (MODIFIED)---
class UserEditorDialog extends JDialog {
    private boolean saved = false;
    private User user;
    private JTextField userField, emailField, phoneField;
    private JPasswordField passField;
    private JComboBox<String> roleBox;
    private UserDAO userDAO = new UserDAO();
    private final boolean isEditMode;

    public UserEditorDialog(Frame owner, User userToEdit) {
        super(owner, (userToEdit == null ? "Add New User" : "Edit User"), true);
        this.isEditMode = userToEdit != null;
        this.user = isEditMode ? userToEdit : new User("", "", "User", "", "");
        setSize(450, 400);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));
        getRootPane().setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.weightx = 1.0;
        userField = new JTextField(user.getUsername());
        if (isEditMode) {
            userField.setEnabled(false);
        }
        passField = new JPasswordField();
        emailField = new JTextField(user.getEmail());
        phoneField = new JTextField(user.getPhone());
        roleBox = new JComboBox<>(new String[]{"User", "Admin"});
        roleBox.setSelectedItem(user.getRole());

        gbc.gridy = 0; form.add(new JLabel("Username:"), gbc);
        gbc.gridy++; form.add(userField, gbc);
        gbc.gridy++; form.add(new JLabel(isEditMode ? "New Password (leave blank to keep current):" : "Password:"), gbc);
        gbc.gridy++; form.add(passField, gbc);
        gbc.gridy++; form.add(new JLabel("Email:"), gbc);
        gbc.gridy++; form.add(emailField, gbc);
        gbc.gridy++; form.add(new JLabel("Phone:"), gbc);
        gbc.gridy++; form.add(phoneField, gbc);
        gbc.gridy++; form.add(new JLabel("Role:"), gbc);
        gbc.gridy++; form.add(roleBox, gbc);
        add(form, BorderLayout.CENTER);

        RoundedButton saveBtn = new RoundedButton("Save");
        saveBtn.setBackground(Style.PRIMARY_COLOR);
        saveBtn.setForeground(Color.WHITE);
        saveBtn.addActionListener(e -> saveUser());
        add(saveBtn, BorderLayout.SOUTH);
    }

    private void saveUser() {
        String u = userField.getText().trim();
        if (u.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username is required.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        user.setEmail(emailField.getText().trim());
        user.setPhone(phoneField.getText().trim());
        user.setRole((String) roleBox.getSelectedItem());

        if (isEditMode) {
            userDAO.updateUser(user);
            String p = new String(passField.getPassword());
            if (!p.isEmpty()) {
                userDAO.changeUserPassword(user.getId(), p);
            }
        } else {
            String p = new String(passField.getPassword());
            if (p.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Password is required for new users.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (userDAO.getUserByUsername(u).isPresent()) {
                JOptionPane.showMessageDialog(this, "Username already exists.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            User newUser = new User(u, p, user.getRole(), user.getEmail(), user.getPhone());
            userDAO.addUser(newUser);
        }

        saved = true;
        dispose();
    }

    public boolean isSaved() {
        return saved;
    }
}

// --- NEW FEATURE: CALENDAR VIEW ---
class CalendarViewPanel extends JPanel {
    private final UserDashboard parentFrame;
    private final User user;
    private final TaskDAO taskDAO = new TaskDAO();
    private LocalDate currentDate;
    private JLabel monthLabel;
    private JPanel calendarGridPanel;
    private JPanel tasksForDayPanel;
    private Set<LocalDate> datesWithTasks = new HashSet<>();

    public CalendarViewPanel(UserDashboard parent, User user) {
        this.parentFrame = parent;
        this.user = user;
        this.currentDate = LocalDate.now().withDayOfMonth(1);

        setOpaque(false);
        setBorder(new EmptyBorder(15, 25, 15, 25));
        setLayout(new BorderLayout(20, 20));

        add(createHeader(), BorderLayout.NORTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.75);
        splitPane.setOpaque(false);
        splitPane.setBorder(null);
        splitPane.setLeftComponent(createCalendarPanel());
        splitPane.setRightComponent(createTasksForDayPanel());

        add(splitPane, BorderLayout.CENTER);

        refreshCalendar();
        displayTasksForDay(LocalDate.now()); // Show today's tasks by default
    }

    private JPanel createHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel titleLabel = new JLabel("My Calendar");
        titleLabel.setFont(Style.FONT_HEADER);

        headerPanel.add(titleLabel, BorderLayout.CENTER);
        return headerPanel;
    }

    private JPanel createCalendarPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Month navigation
        JPanel navPanel = new JPanel(new BorderLayout());
        navPanel.setOpaque(false);
        RoundedButton prevBtn = new RoundedButton("<");
        prevBtn.setBackground(Style.PRIMARY_COLOR);
        prevBtn.setForeground(Color.WHITE);
        prevBtn.addActionListener(e -> {
            currentDate = currentDate.minusMonths(1);
            refreshCalendar();
        });

        RoundedButton nextBtn = new RoundedButton(">");
        nextBtn.setBackground(Style.PRIMARY_COLOR);
        nextBtn.setForeground(Color.WHITE);
        nextBtn.addActionListener(e -> {
            currentDate = currentDate.plusMonths(1);
            refreshCalendar();
        });

        monthLabel = new JLabel("", SwingConstants.CENTER);
        monthLabel.setFont(Style.FONT_FORM_HEADER);
        navPanel.add(prevBtn, BorderLayout.WEST);
        navPanel.add(monthLabel, BorderLayout.CENTER);
        navPanel.add(nextBtn, BorderLayout.EAST);

        panel.add(navPanel, BorderLayout.NORTH);

        calendarGridPanel = new JPanel(new GridLayout(0, 7, 8, 8));
        calendarGridPanel.setOpaque(false);
        panel.add(calendarGridPanel, BorderLayout.CENTER);

        return panel;
    }

    private JScrollPane createTasksForDayPanel() {
        tasksForDayPanel = new JPanel();
        tasksForDayPanel.setLayout(new BoxLayout(tasksForDayPanel, BoxLayout.Y_AXIS));
        tasksForDayPanel.setBackground(Style.CARD_BACKGROUND_COLOR);
        tasksForDayPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JScrollPane scrollPane = new JScrollPane(tasksForDayPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(12);

        return scrollPane;
    }

    private void refreshCalendar() {
        calendarGridPanel.removeAll();
        datesWithTasks = taskDAO.getTaskDatesInMonth(user.getId(), currentDate.getYear(), currentDate.getMonthValue());
        monthLabel.setText(currentDate.format(DateTimeFormatter.ofPattern("MMMM yyyy")));

        // Day name headers
        String[] dayNames = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (String name : dayNames) {
            JLabel dayHeader = new JLabel(name, SwingConstants.CENTER);
            dayHeader.setFont(Style.FONT_BUTTON);
            calendarGridPanel.add(dayHeader);
        }

        int firstDayOfWeek = currentDate.getDayOfWeek().getValue() % 7;
        for (int i = 0; i < firstDayOfWeek; i++) {
            calendarGridPanel.add(new JLabel(""));
        }

        for (int day = 1; day <= currentDate.lengthOfMonth(); day++) {
            final LocalDate thisDate = currentDate.withDayOfMonth(day);
            RoundedPanel dayPanel = new RoundedPanel(15);
            dayPanel.setLayout(new BorderLayout());
            dayPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));

            JLabel dayLabel = new JLabel(String.valueOf(day), SwingConstants.CENTER);
            dayLabel.setFont(Style.FONT_LABEL.deriveFont(Font.BOLD));
            dayPanel.add(dayLabel, BorderLayout.CENTER);

            // --- MODIFIED: More prominent coloring for dates ---
            if (thisDate.equals(LocalDate.now())) {
                dayPanel.setBackground(Style.PRIMARY_COLOR); // Today has the primary color
                dayLabel.setForeground(Color.WHITE);
            } else if (datesWithTasks.contains(thisDate)) {
                dayPanel.setBackground(Style.ACCENT_COLOR.brighter()); // Dates with tasks have accent color
                dayLabel.setForeground(Style.TEXT_PRIMARY_COLOR);
            } else {
                dayPanel.setBackground(Style.CARD_BACKGROUND_COLOR); // Default
                dayLabel.setForeground(Style.TEXT_PRIMARY_COLOR);
            }

            dayPanel.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    displayTasksForDay(thisDate);
                }
            });
            calendarGridPanel.add(dayPanel);
        }

        calendarGridPanel.revalidate();
        calendarGridPanel.repaint();
    }

    private void displayTasksForDay(LocalDate date) {
        tasksForDayPanel.removeAll();

        JLabel header = new JLabel("Tasks for " + date.format(DateTimeFormatter.ofPattern("dd MMM")));
        header.setFont(Style.FONT_CARD_TITLE);
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        tasksForDayPanel.add(header);
        tasksForDayPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        List<Task> tasks = taskDAO.getTasksByDate(user.getId(), date);
        if (tasks.isEmpty()) {
            JLabel noTasksLabel = new JLabel("No tasks scheduled for this day.");
            noTasksLabel.setFont(Style.FONT_LABEL);
            noTasksLabel.setForeground(Style.TEXT_SECONDARY_COLOR);
            tasksForDayPanel.add(noTasksLabel);
        } else {
            for (Task task : tasks) {
                JPanel taskCard = new JPanel(new BorderLayout(10, 0));
                taskCard.setOpaque(false);
                taskCard.setAlignmentX(Component.LEFT_ALIGNMENT);
                taskCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

                JPanel indicator = new JPanel();
                indicator.setPreferredSize(new Dimension(5, 5));
                indicator.setBackground(task.getPriority() == Priority.HIGH ? Style.PRIORITY_HIGH : task.getPriority() == Priority.MEDIUM ? Style.PRIORITY_MEDIUM : Style.PRIORITY_LOW);
                taskCard.add(indicator, BorderLayout.WEST);

                String title = task.getDescription().split("\n")[0];
                JLabel taskLabel = new JLabel(task.getCategory().icon + " " + title);
                taskLabel.setFont(Style.FONT_LABEL);
                if (task.getStatus() == Status.DONE) {
                    taskLabel.setText("<html><strike>" + task.getCategory().icon + " " + title + "</strike></html>");
                    taskLabel.setForeground(Style.TEXT_SECONDARY_COLOR);
                }

                taskCard.add(taskLabel, BorderLayout.CENTER);
                tasksForDayPanel.add(taskCard);
                tasksForDayPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            }
        }

        tasksForDayPanel.revalidate();
        tasksForDayPanel.repaint();
    }
}


// --- NEW FEATURE: GOALS PAGE ---
class GoalsPanel extends JPanel {
    private final UserDashboard parentFrame;
    private final User user;
    private final GoalDAO goalDAO = new GoalDAO();
    private DefaultListModel<Goal> listModel;
    private JList<Goal> goalList;
    private int hoveredIndex = -1;

    public GoalsPanel(UserDashboard parent, User user) {
        this.parentFrame = parent;
        this.user = user;
        setOpaque(false);
        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(15, 25, 15, 25));

        add(createHeader(), BorderLayout.NORTH);

        listModel = new DefaultListModel<>();
        goalList = new JList<>(listModel);
        goalList.setCellRenderer(new GoalCardRenderer(() -> hoveredIndex));
        goalList.setBackground(getBackground());
        goalList.setSelectionModel(new DefaultListSelectionModel() {
            @Override
            public void setSelectionInterval(int i, int i1) {
            }
        });

        JScrollPane sp = new JScrollPane(goalList);
        sp.setBorder(null);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        add(sp, BorderLayout.CENTER);

        loadUserGoals();

        goalList.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int i = goalList.locationToIndex(e.getPoint());
                if (i != hoveredIndex) {
                    hoveredIndex = i;
                    goalList.repaint();
                }
            }
        });
        goalList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                hoveredIndex = -1;
                goalList.repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int i = goalList.locationToIndex(e.getPoint());
                    if (i >= 0) openGoalEditor(listModel.getElementAt(i));
                }
            }
        });
    }

    private JPanel createHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel titleLabel = new JLabel("Long-Term Goals");
        titleLabel.setFont(Style.FONT_HEADER);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        controls.setOpaque(false);

        RoundedButton addBtn = new RoundedButton("+ Add New Goal");
        addBtn.setBackground(Style.PRIMARY_COLOR);
        addBtn.setForeground(Color.WHITE);
        addBtn.addActionListener(e -> openGoalEditor(null));
        controls.add(addBtn);

        headerPanel.add(titleLabel, BorderLayout.CENTER);
        headerPanel.add(controls, BorderLayout.EAST);
        return headerPanel;
    }

    public void loadUserGoals() {
        listModel.clear();
        List<Goal> goals = goalDAO.getGoalsForUser(user.getId());
        goals.forEach(listModel::addElement);
    }

    private void openGoalEditor(Goal goal) {
        GoalEditorDialog dialog = new GoalEditorDialog((Frame) SwingUtilities.getWindowAncestor(this), user.getId(), goal);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            loadUserGoals(); // Refresh the list after saving
        }
    }

    // Custom renderer for goals
    class GoalCardRenderer implements ListCellRenderer<Goal> {
        private final java.util.function.Supplier<Integer> hoveredIndexSupplier;

        public GoalCardRenderer(java.util.function.Supplier<Integer> h) {
            this.hoveredIndexSupplier = h;
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends Goal> list, Goal goal, int index, boolean isSelected, boolean cellHasFocus) {
            RoundedPanel card = new RoundedPanel(25);
            card.setBackground(Style.CARD_BACKGROUND_COLOR);
            card.setLayout(new BorderLayout(15, 15));
            card.setBorder(new EmptyBorder(20, 20, 20, 20));
            if (index == hoveredIndexSupplier.get()) card.setBorderColor(Style.PRIMARY_COLOR);

            JLabel titleLabel = new JLabel("🎯 " + goal.getTitle());
            titleLabel.setFont(Style.FONT_TASK_DESC);

            JLabel dateLabel = new JLabel("Due by: " + goal.getEndDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
            dateLabel.setFont(Style.FONT_TASK_DETAILS);
            dateLabel.setForeground(Style.TEXT_SECONDARY_COLOR);

            JPanel textPanel = new JPanel();
            textPanel.setOpaque(false);
            textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
            textPanel.add(titleLabel);
            textPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            textPanel.add(dateLabel);

            card.add(textPanel, BorderLayout.CENTER);

            // Progress Bar
            long totalDays = ChronoUnit.DAYS.between(goal.getStartDate(), goal.getEndDate());
            long daysPassed = ChronoUnit.DAYS.between(goal.getStartDate(), LocalDate.now());
            int progress = 0;
            if (totalDays > 0) {
                progress = (int) (100 * Math.max(0, Math.min(totalDays, daysPassed)) / totalDays);
            }
            if (goal.getStatus() == GoalStatus.COMPLETED || LocalDate.now().isAfter(goal.getEndDate())) {
                progress = 100;
            }

            JProgressBar progressBar = new JProgressBar(0, 100);
            progressBar.setValue(progress);
            progressBar.setStringPainted(true);
            progressBar.setString(progress + "%");
            if (goal.getStatus() == GoalStatus.COMPLETED) {
                progressBar.setForeground(Style.STATUS_DONE);
                titleLabel.setForeground(Style.TEXT_SECONDARY_COLOR);
            } else {
                progressBar.setForeground(Style.PRIMARY_COLOR);
            }

            card.add(progressBar, BorderLayout.SOUTH);
            return card;
        }
    }
}

// Dialog to Add/Edit Goals
class GoalEditorDialog extends JDialog {
    private boolean saved = false;
    private final int userId;
    private Goal goal;
    private JTextField titleField;
    private JTextArea descArea;
    private JLabel startDateLabel, endDateLabel;
    private LocalDate startDate, endDate;
    private JComboBox<GoalStatus> statusBox;
    private final boolean isNewGoal;
    private final GoalDAO goalDAO = new GoalDAO();

    public GoalEditorDialog(Frame owner, int userId, Goal goalToEdit) {
        super(owner, (goalToEdit == null ? "Add New Goal" : "Edit Goal"), true);
        this.userId = userId;
        this.isNewGoal = (goalToEdit == null);
        this.goal = isNewGoal ? new Goal(userId, "", "", LocalDate.now(), LocalDate.now().plusMonths(1)) : goalToEdit;
        this.startDate = this.goal.getStartDate();
        this.endDate = this.goal.getEndDate();

        setSize(500, 550);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));
        getRootPane().setBorder(new EmptyBorder(20, 20, 20, 20));

        // --- Form Panel ---
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 5, 8, 5);
        gbc.weightx = 1.0;

        gbc.gridy = 0;
        form.add(new JLabel("Goal Title:"), gbc);
        gbc.gridy++;
        titleField = new JTextField(goal.getTitle());
        titleField.setFont(Style.FONT_FIELD);
        form.add(titleField, gbc);

        gbc.gridy++;
        form.add(new JLabel("Description:"), gbc);
        gbc.gridy++;
        descArea = new JTextArea(goal.getDescription(), 4, 20);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        form.add(new JScrollPane(descArea), gbc);

        // --- Date Selection ---
        JPanel datePanel = new JPanel(new GridLayout(1, 2, 10, 0));
        datePanel.setOpaque(false);
        JPanel startPanel = new JPanel(new BorderLayout());
        startPanel.setOpaque(false);
        startDateLabel = new JLabel();
        RoundedButton startBtn = new RoundedButton("Start Date");
        startBtn.addActionListener(e -> selectDate(true));
        startPanel.add(startBtn, BorderLayout.NORTH);
        startPanel.add(startDateLabel, BorderLayout.SOUTH);

        JPanel endPanel = new JPanel(new BorderLayout());
        endPanel.setOpaque(false);
        endDateLabel = new JLabel();
        RoundedButton endBtn = new RoundedButton("End Date");
        endBtn.addActionListener(e -> selectDate(false));
        endPanel.add(endBtn, BorderLayout.NORTH);
        endPanel.add(endDateLabel, BorderLayout.SOUTH);

        datePanel.add(startPanel);
        datePanel.add(endPanel);
        gbc.gridy++;
        form.add(datePanel, gbc);
        updateDateLabels();

        gbc.gridy++;
        form.add(new JLabel("Status:"), gbc);
        gbc.gridy++;
        statusBox = new JComboBox<>(GoalStatus.values());
        statusBox.setSelectedItem(goal.getStatus());
        form.add(statusBox, gbc);

        add(form, BorderLayout.CENTER);

        // --- Button Panel ---
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        RoundedButton saveBtn = new RoundedButton("Save Goal");
        saveBtn.setBackground(Style.PRIMARY_COLOR);
        saveBtn.setForeground(Color.WHITE);
        saveBtn.addActionListener(e -> saveGoal());

        RoundedButton cancelBtn = new RoundedButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());

        buttons.add(cancelBtn);
        buttons.add(saveBtn);

        if (!isNewGoal) {
            RoundedButton deleteBtn = new RoundedButton("Delete");
            deleteBtn.setBackground(Style.DANGER_COLOR);
            deleteBtn.setForeground(Color.WHITE);
            deleteBtn.addActionListener(e -> deleteGoal());
            buttons.add(deleteBtn, 0);
        }

        add(buttons, BorderLayout.SOUTH);
    }

    private void selectDate(boolean isStart) {
        LocalDate initial = isStart ? startDate : endDate;
        JDialog d = new JDialog(this, "Select Date", true);
        JCalendarPanel cp = new JCalendarPanel(initial);
        d.add(cp);
        d.pack();
        d.setLocationRelativeTo(this);
        d.setVisible(true);
        if (cp.getSelectedDate() != null) {
            if (isStart) startDate = cp.getSelectedDate();
            else endDate = cp.getSelectedDate();
            updateDateLabels();
        }
    }

    private void updateDateLabels() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
        startDateLabel.setText(startDate.format(formatter));
        endDateLabel.setText(endDate.format(formatter));
    }

    private void saveGoal() {
        if (titleField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Title is required.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (endDate.isBefore(startDate)) {
            JOptionPane.showMessageDialog(this, "End date cannot be before start date.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        goal.setTitle(titleField.getText().trim());
        goal.setDescription(descArea.getText().trim());
        goal.setStartDate(startDate);
        goal.setEndDate(endDate);
        goal.setStatus((GoalStatus) statusBox.getSelectedItem());

        if (isNewGoal) {
            goalDAO.addGoal(goal);
        } else {
            goalDAO.updateGoal(goal);
        }
        saved = true;
        dispose();
    }

    private void deleteGoal() {
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this goal?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            goalDAO.deleteGoal(goal.getId());
            saved = true;
            dispose();
        }
    }

    public boolean isSaved() {
        return saved;
    }
}


// --- NEW FEATURE: PASSWORD SAVER ---
class PasswordSaverPanel extends JPanel {
    private final UserDashboard parentFrame;
    private final User user;
    private final PasswordDAO passwordDAO = new PasswordDAO();
    private DefaultTableModel tableModel;
    private JTable passwordTable;
    private TableRowSorter<DefaultTableModel> sorter;


    public PasswordSaverPanel(UserDashboard parent, User user) {
        this.parentFrame = parent;
        this.user = user;
        setOpaque(false);
        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(15, 25, 15, 25));

        add(createHeader(), BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new String[]{"ID", "Website", "Username", "Password"}, 0){
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        passwordTable = new JTable(tableModel);
        setupTable();

        JScrollPane sp = new JScrollPane(passwordTable);
        add(sp, BorderLayout.CENTER);

        loadPasswords();
    }

    private void setupTable() {
        passwordTable.setRowHeight(35);
        passwordTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        // Hide ID column
        passwordTable.getColumnModel().getColumn(0).setMinWidth(0);
        passwordTable.getColumnModel().getColumn(0).setMaxWidth(0);
        passwordTable.getColumnModel().getColumn(0).setWidth(0);

        sorter = new TableRowSorter<>(tableModel);
        passwordTable.setRowSorter(sorter);

        passwordTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    editSelectedPassword();
                }
            }
        });
    }

    private JPanel createHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel titleLabel = new JLabel("Password Saver");
        titleLabel.setFont(Style.FONT_HEADER);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        controls.setOpaque(false);

        JTextField searchField = new RoundedTextField(25);
        searchField.getDocument().addDocumentListener(new DocumentListener(){
            @Override public void insertUpdate(DocumentEvent e) { filterTable(); }
            @Override public void removeUpdate(DocumentEvent e) { filterTable(); }
            @Override public void changedUpdate(DocumentEvent e) { filterTable(); }
            private void filterTable() {
                String text = searchField.getText();
                if (text.trim().length() == 0) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                }
            }
        });
        controls.add(searchField);

        RoundedButton addBtn = new RoundedButton("+ Add New");
        addBtn.setBackground(Style.PRIMARY_COLOR);
        addBtn.setForeground(Color.WHITE);
        addBtn.addActionListener(e -> openPasswordEditor(null));
        controls.add(addBtn);

        RoundedButton deleteBtn = new RoundedButton("Delete");
        deleteBtn.setBackground(Style.DANGER_COLOR);
        deleteBtn.setForeground(Color.WHITE);
        deleteBtn.addActionListener(e -> deleteSelectedPassword());
        controls.add(deleteBtn);

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(controls, BorderLayout.EAST);
        return headerPanel;
    }

    public void loadPasswords() {
        tableModel.setRowCount(0);
        List<PasswordEntry> entries = passwordDAO.getPasswordsForUser(user.getId());
        for (PasswordEntry entry : entries) {
            tableModel.addRow(new Object[]{entry.getId(), entry.getWebsiteName(), entry.getUsername(), "••••••••"});
        }
    }

    private void editSelectedPassword() {
        int viewRow = passwordTable.getSelectedRow();
        if (viewRow >= 0) {
            int modelRow = passwordTable.convertRowIndexToModel(viewRow);
            int entryId = (int) tableModel.getValueAt(modelRow, 0);
            Optional<PasswordEntry> entryOpt = passwordDAO.getPasswordsForUser(user.getId()).stream()
                    .filter(p -> p.getId() == entryId).findFirst();
            entryOpt.ifPresent(this::openPasswordEditor);
        }
    }

    private void openPasswordEditor(PasswordEntry entry) {
        PasswordEditorDialog dialog = new PasswordEditorDialog((Frame)SwingUtilities.getWindowAncestor(this), user.getId(), entry);
        dialog.setVisible(true);
        if(dialog.isSaved()){
            loadPasswords();
        }
    }

    private void deleteSelectedPassword() {
        int viewRow = passwordTable.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a password to delete.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = passwordTable.convertRowIndexToModel(viewRow);
        int entryId = (int) tableModel.getValueAt(modelRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this password entry?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if(passwordDAO.deletePassword(entryId)) {
                loadPasswords();
            }
        }
    }
}

// --- NEW FEATURE: CONTACTS SAVER ---
class ContactsSaverPanel extends JPanel {
    private final UserDashboard parentFrame;
    private final User user;
    private final ContactDAO contactDAO = new ContactDAO();
    private DefaultTableModel tableModel;
    private JTable contactsTable;
    private TableRowSorter<DefaultTableModel> sorter;

    public ContactsSaverPanel(UserDashboard parent, User user) {
        this.parentFrame = parent;
        this.user = user;
        setOpaque(false);
        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(15, 25, 15, 25));

        add(createHeader(), BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new String[]{"ID", "Name", "Phone Number", "Email"}, 0){
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        contactsTable = new JTable(tableModel);
        setupTable();

        JScrollPane sp = new JScrollPane(contactsTable);
        add(sp, BorderLayout.CENTER);

        loadContacts();
    }

    private void setupTable() {
        contactsTable.setRowHeight(35);
        contactsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        contactsTable.getColumnModel().getColumn(0).setMinWidth(0);
        contactsTable.getColumnModel().getColumn(0).setMaxWidth(0);
        contactsTable.getColumnModel().getColumn(0).setWidth(0);

        sorter = new TableRowSorter<>(tableModel);
        contactsTable.setRowSorter(sorter);

        contactsTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    editSelectedContact();
                }
            }
        });
    }

    private JPanel createHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel titleLabel = new JLabel("Contacts");
        titleLabel.setFont(Style.FONT_HEADER);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        controls.setOpaque(false);

        JTextField searchField = new RoundedTextField(25);
        searchField.getDocument().addDocumentListener(new DocumentListener(){
            @Override public void insertUpdate(DocumentEvent e) { filterTable(); }
            @Override public void removeUpdate(DocumentEvent e) { filterTable(); }
            @Override public void changedUpdate(DocumentEvent e) { filterTable(); }
            private void filterTable() {
                sorter.setRowFilter(RowFilter.regexFilter("(?i)" + searchField.getText()));
            }
        });
        controls.add(searchField);

        RoundedButton addBtn = new RoundedButton("+ Add New");
        addBtn.setBackground(Style.PRIMARY_COLOR);
        addBtn.setForeground(Color.WHITE);
        addBtn.addActionListener(e -> openContactEditor(null));
        controls.add(addBtn);

        RoundedButton deleteBtn = new RoundedButton("Delete");
        deleteBtn.setBackground(Style.DANGER_COLOR);
        deleteBtn.setForeground(Color.WHITE);
        deleteBtn.addActionListener(e -> deleteSelectedContact());
        controls.add(deleteBtn);

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(controls, BorderLayout.EAST);
        return headerPanel;
    }

    public void loadContacts() {
        tableModel.setRowCount(0);
        List<Contact> contacts = contactDAO.getContactsForUser(user.getId());
        for (Contact contact : contacts) {
            tableModel.addRow(new Object[]{contact.getId(), contact.getName(), contact.getPhoneNumber(), contact.getEmail()});
        }
    }

    private void editSelectedContact() {
        int viewRow = contactsTable.getSelectedRow();
        if (viewRow >= 0) {
            int modelRow = contactsTable.convertRowIndexToModel(viewRow);
            int contactId = (int) tableModel.getValueAt(modelRow, 0);
            Optional<Contact> contactOpt = contactDAO.getContactsForUser(user.getId()).stream()
                    .filter(c -> c.getId() == contactId).findFirst();
            contactOpt.ifPresent(this::openContactEditor);
        }
    }

    private void openContactEditor(Contact contact) {
        ContactEditorDialog dialog = new ContactEditorDialog((Frame)SwingUtilities.getWindowAncestor(this), user.getId(), contact);
        dialog.setVisible(true);
        if(dialog.isSaved()){
            loadContacts();
        }
    }

    private void deleteSelectedContact() {
        int viewRow = contactsTable.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a contact to delete.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = contactsTable.convertRowIndexToModel(viewRow);
        int contactId = (int) tableModel.getValueAt(modelRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this contact?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if(contactDAO.deleteContact(contactId)) {
                loadContacts();
            }
        }
    }
}

// Dialog for Password Saver
class PasswordEditorDialog extends JDialog {
    private boolean saved = false;
    private final int userId;
    private PasswordEntry entry;
    private final boolean isNewEntry;
    private final PasswordDAO passwordDAO = new PasswordDAO();
    private JTextField websiteField, usernameField;
    private JPasswordField passwordField;
    private JTextArea notesArea;

    public PasswordEditorDialog(Frame owner, int userId, PasswordEntry entry) {
        super(owner, entry == null ? "Add New Password" : "Edit Password", true);
        this.userId = userId;
        this.entry = entry;
        this.isNewEntry = (entry == null);

        setSize(450, 450);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));
        getRootPane().setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.weightx = 1.0;

        gbc.gridy = 0; form.add(new JLabel("Website:"), gbc);
        gbc.gridy++; websiteField = new JTextField(isNewEntry ? "" : entry.getWebsiteName()); form.add(websiteField, gbc);

        gbc.gridy++; form.add(new JLabel("Username:"), gbc);
        gbc.gridy++; usernameField = new JTextField(isNewEntry ? "" : entry.getUsername()); form.add(usernameField, gbc);

        gbc.gridy++; form.add(new JLabel("Password:"), gbc);
        gbc.gridy++; passwordField = new JPasswordField(isNewEntry ? "" : entry.getDecryptedPassword()); form.add(passwordField, gbc);

        gbc.gridy++; form.add(new JLabel("Notes:"), gbc);
        gbc.gridy++; notesArea = new JTextArea(isNewEntry ? "" : entry.getNotes(), 3, 20);
        form.add(new JScrollPane(notesArea), gbc);

        add(form, BorderLayout.CENTER);

        RoundedButton saveBtn = new RoundedButton("Save");
        saveBtn.addActionListener(e -> save());
        add(saveBtn, BorderLayout.SOUTH);
    }

    private void save() {
        String website = websiteField.getText().trim();
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (website.isEmpty() || username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Website, Username, and Password cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (isNewEntry) {
            PasswordEntry newEntry = new PasswordEntry(userId, website, username, password, notesArea.getText().trim());
            passwordDAO.addPassword(newEntry);
        } else {
            entry.setWebsiteName(website);
            entry.setUsername(username);
            entry.setEncryptedPassword(PasswordUtils.encrypt(password));
            entry.setNotes(notesArea.getText().trim());
            passwordDAO.updatePassword(entry);
        }
        saved = true;
        dispose();
    }

    public boolean isSaved() { return saved; }
}

// Dialog for Contacts Saver
class ContactEditorDialog extends JDialog {
    private boolean saved = false;
    private final int userId;
    private Contact contact;
    private final boolean isNewContact;
    private final ContactDAO contactDAO = new ContactDAO();
    private JTextField nameField, phoneField, emailField;
    private JTextArea addressArea;

    public ContactEditorDialog(Frame owner, int userId, Contact contact) {
        super(owner, contact == null ? "Add New Contact" : "Edit Contact", true);
        this.userId = userId;
        this.contact = contact;
        this.isNewContact = (contact == null);

        setSize(450, 450);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));
        getRootPane().setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.weightx = 1.0;

        gbc.gridy = 0; form.add(new JLabel("Name:"), gbc);
        gbc.gridy++; nameField = new JTextField(isNewContact ? "" : contact.getName()); form.add(nameField, gbc);

        gbc.gridy++; form.add(new JLabel("Phone Number:"), gbc);
        gbc.gridy++; phoneField = new JTextField(isNewContact ? "" : contact.getPhoneNumber()); form.add(phoneField, gbc);

        gbc.gridy++; form.add(new JLabel("Email Address:"), gbc);
        gbc.gridy++; emailField = new JTextField(isNewContact ? "" : contact.getEmail()); form.add(emailField, gbc);

        gbc.gridy++; form.add(new JLabel("Address:"), gbc);
        gbc.gridy++; addressArea = new JTextArea(isNewContact ? "" : contact.getAddress(), 3, 20);
        form.add(new JScrollPane(addressArea), gbc);

        add(form, BorderLayout.CENTER);

        RoundedButton saveBtn = new RoundedButton("Save");
        saveBtn.addActionListener(e -> save());
        add(saveBtn, BorderLayout.SOUTH);
    }

    private void save() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (isNewContact) {
            Contact newContact = new Contact(0, userId, name, phoneField.getText().trim(), emailField.getText().trim(), addressArea.getText().trim());
            contactDAO.addContact(newContact);
        } else {
            contact.setName(name);
            contact.setPhoneNumber(phoneField.getText().trim());
            contact.setEmail(emailField.getText().trim());
            contact.setAddress(addressArea.getText().trim());
            contactDAO.updateContact(contact);
        }
        saved = true;
        dispose();
    }

    public boolean isSaved() { return saved; }
}

// NEW: MiniCalendarPanel for the dashboard sidebar
class MiniCalendarPanel extends JPanel {
    private LocalDate currentDate;
    private Set<LocalDate> taskDates = new HashSet<>();

    public MiniCalendarPanel() {
        this.currentDate = LocalDate.now();
        setOpaque(false);
        setLayout(new BorderLayout(5, 5));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        add(createHeader(), BorderLayout.NORTH);
        add(createGrid(), BorderLayout.CENTER);
    }

    public void setTaskDates(Set<LocalDate> dates) {
        this.taskDates = dates;
        // Redraw grid
        remove(1);
        add(createGrid(), BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel monthLabel = new JLabel(currentDate.format(DateTimeFormatter.ofPattern("MMMM yyyy")), JLabel.CENTER);
        monthLabel.setFont(Style.FONT_LABEL.deriveFont(Font.BOLD));
        header.add(monthLabel, BorderLayout.CENTER);
        return header;
    }

    private JPanel createGrid() {
        JPanel gridPanel = new JPanel(new GridLayout(0, 7, 3, 3));
        gridPanel.setOpaque(false);

        // Day name headers
        String[] dayNames = {"S", "M", "T", "W", "T", "F", "S"};
        for (String name : dayNames) {
            JLabel dayHeader = new JLabel(name, SwingConstants.CENTER);
            dayHeader.setFont(Style.FONT_LABEL.deriveFont(10f));
            gridPanel.add(dayHeader);
        }

        LocalDate firstDayOfMonth = currentDate.withDayOfMonth(1);
        int firstDayOfWeek = firstDayOfMonth.getDayOfWeek().getValue() % 7;

        for (int i = 0; i < firstDayOfWeek; i++) {
            gridPanel.add(new JLabel(""));
        }

        for (int day = 1; day <= currentDate.lengthOfMonth(); day++) {
            LocalDate thisDate = currentDate.withDayOfMonth(day);
            JLabel dayLabel = new JLabel(String.valueOf(day), SwingConstants.CENTER);
            dayLabel.setOpaque(true);

            if (thisDate.equals(LocalDate.now())) {
                dayLabel.setBackground(Style.PRIMARY_COLOR);
                dayLabel.setForeground(Color.WHITE);
            } else if (taskDates.contains(thisDate)) {
                dayLabel.setBackground(Style.ACCENT_COLOR.brighter());
                dayLabel.setForeground(Color.WHITE);
            } else {
                dayLabel.setBackground(new Color(0,0,0,0)); // Transparent
            }
            gridPanel.add(dayLabel);
        }
        return gridPanel;
    }
}

// --- NEW CLASS: User's Settings Panel ---
class SettingsPanel extends JPanel {
    private final UserDashboard parentFrame;
    private User user;
    private JTextField usernameField, emailField, phoneField;
    private final UserDAO userDAO = new UserDAO();

    public SettingsPanel(UserDashboard parent, User user) {
        this.parentFrame = parent;
        this.user = user;
        setOpaque(false);
        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(15, 25, 15, 25));

        JLabel titleLabel = new JLabel("Settings");
        titleLabel.setFont(Style.FONT_HEADER);
        add(titleLabel, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(Style.FONT_BUTTON);
        tabbedPane.addTab("My Profile", createProfilePanel());
        tabbedPane.addTab("Theme", createThemePanel());

        add(tabbedPane, BorderLayout.CENTER);

        loadUserInfo();
    }

    private JPanel createProfilePanel() {
        JPanel profilePanel = new JPanel(new GridBagLayout());
        profilePanel.setOpaque(false);
        profilePanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.weightx = 1.0;

        // Username
        gbc.gridy = 0; profilePanel.add(new JLabel("Username:"), gbc);
        gbc.gridy++;
        usernameField = new JTextField();
        usernameField.setFont(Style.FONT_FIELD);
        usernameField.setEditable(false);
        usernameField.setForeground(Style.TEXT_SECONDARY_COLOR);
        profilePanel.add(usernameField, gbc);

        // Email
        gbc.gridy++; profilePanel.add(new JLabel("Email Address:"), gbc);
        gbc.gridy++;
        emailField = new JTextField();
        emailField.setFont(Style.FONT_FIELD);
        profilePanel.add(emailField, gbc);

        // Phone
        gbc.gridy++; profilePanel.add(new JLabel("Phone Number:"), gbc);
        gbc.gridy++;
        phoneField = new JTextField();
        phoneField.setFont(Style.FONT_FIELD);
        profilePanel.add(phoneField, gbc);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        buttonPanel.setOpaque(false);
        RoundedButton updateInfoBtn = new RoundedButton("Update Info");
        updateInfoBtn.setBackground(Style.PRIMARY_COLOR);
        updateInfoBtn.setForeground(Color.WHITE);
        updateInfoBtn.addActionListener(e -> updateUserInfo());
        buttonPanel.add(updateInfoBtn);

        RoundedButton changePassBtn = new RoundedButton("Change Password");
        changePassBtn.setBackground(Style.ACCENT_COLOR);
        changePassBtn.setForeground(Color.WHITE);
        changePassBtn.addActionListener(e -> changePassword());
        buttonPanel.add(changePassBtn);

        gbc.gridy++;
        gbc.insets = new Insets(20, 8, 8, 8);
        profilePanel.add(buttonPanel, gbc);

        return profilePanel;
    }

    private JPanel createThemePanel() {
        JPanel themePanel = new JPanel(new GridLayout(0, 1, 10, 10));
        themePanel.setOpaque(false);
        themePanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel themeLabel = new JLabel("Select a Theme:");
        themeLabel.setFont(Style.FONT_CARD_TITLE);
        themePanel.add(themeLabel);

        RoundedButton aquaBtn = new RoundedButton("Aqua Dream");
        aquaBtn.addActionListener(e -> applyAndRefreshTheme(Theme.AQUA_DREAM));
        themePanel.add(aquaBtn);

        RoundedButton lavenderBtn = new RoundedButton("Lavender Bliss");
        lavenderBtn.addActionListener(e -> applyAndRefreshTheme(Theme.LAVENDER_BLISS));
        themePanel.add(lavenderBtn);

        RoundedButton forestBtn = new RoundedButton("Forest Green");
        forestBtn.addActionListener(e -> applyAndRefreshTheme(Theme.FOREST_GREEN));
        themePanel.add(forestBtn);

        return themePanel;
    }

    public void loadUserInfo() {
        this.user = userDAO.getUserById(user.getId()).orElse(this.user); // Refresh user data
        usernameField.setText(user.getUsername());
        emailField.setText(user.getEmail());
        phoneField.setText(user.getPhone());
    }

    private void updateUserInfo() {
        user.setEmail(emailField.getText().trim());
        user.setPhone(phoneField.getText().trim());
        if (userDAO.updateUser(user)) {
            JOptionPane.showMessageDialog(this, "Profile updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            parentFrame.refreshUserData(); // Refresh user object in parent frame
        } else {
            JOptionPane.showMessageDialog(this, "Failed to update profile.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void changePassword() {
        ChangePasswordDialog dialog = new ChangePasswordDialog((Frame) SwingUtilities.getWindowAncestor(this), user);
        dialog.setVisible(true);
    }

    private void applyAndRefreshTheme(Theme theme) {
        Style.applyTheme(theme);
        SwingUtilities.updateComponentTreeUI(parentFrame);
    }
}

// --- NEW CLASS: Dialog for changing password ---
class ChangePasswordDialog extends JDialog {
    private final User user;
    private final UserDAO userDAO = new UserDAO();
    private JPasswordField currentPassField, newPassField, confirmPassField;

    public ChangePasswordDialog(Frame owner, User user) {
        super(owner, "Change Password", true);
        this.user = user;
        setSize(400, 300);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));
        getRootPane().setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel form = new JPanel(new GridLayout(0, 1, 5, 5));
        currentPassField = new JPasswordField();
        newPassField = new JPasswordField();
        confirmPassField = new JPasswordField();

        form.add(new JLabel("Current Password:"));
        form.add(currentPassField);
        form.add(new JLabel("New Password:"));
        form.add(newPassField);
        form.add(new JLabel("Confirm New Password:"));
        form.add(confirmPassField);

        add(form, BorderLayout.CENTER);

        RoundedButton saveBtn = new RoundedButton("Save Changes");
        saveBtn.setBackground(Style.PRIMARY_COLOR);
        saveBtn.setForeground(Color.WHITE);
        saveBtn.addActionListener(e -> savePassword());
        add(saveBtn, BorderLayout.SOUTH);
    }

    private void savePassword() {
        String currentPass = new String(currentPassField.getPassword());
        String newPass = new String(newPassField.getPassword());
        String confirmPass = new String(confirmPassField.getPassword());

        if (!PasswordUtils.checkPassword(currentPass, user.getPassword())) {
            JOptionPane.showMessageDialog(this, "Incorrect current password.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (newPass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "New password cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!newPass.equals(confirmPass)) {
            JOptionPane.showMessageDialog(this, "New passwords do not match.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (userDAO.changeUserPassword(user.getId(), newPass)) {
            JOptionPane.showMessageDialog(this, "Password changed successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to change password.", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

// --- NEW CLASS: Dialog for Admins to view a user's tasks ---
class UserTasksDialog extends JDialog {
    public UserTasksDialog(Frame owner, String username, List<Task> tasks) {
        super(owner, "Tasks for " + username, true);
        setSize(800, 600);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        DefaultTableModel tableModel = new DefaultTableModel(new String[]{"Description", "Status", "Priority", "Due Date"}, 0);
        JTable taskTable = new JTable(tableModel);
        taskTable.setRowHeight(30);

        for (Task task : tasks) {
            tableModel.addRow(new Object[]{
                    task.getDescription().split("\n")[0], // Only show main title
                    task.getStatus(),
                    task.getPriority(),
                    task.getDueDate().format(DateTimeFormatter.ISO_LOCAL_DATE)
            });
        }

        add(new JScrollPane(taskTable), BorderLayout.CENTER);
    }
}
