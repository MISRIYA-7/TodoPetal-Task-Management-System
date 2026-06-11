Import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public class TodoPetalApp {
    public static void main(String[] args) {
        // Set custom UI defaults for a modern look
        Style.applyTheme(Theme.LAVENDER_BLISS); // Apply default theme
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

        SwingUtilities.invokeLater(LoginPage::new);
    }
}

// --- THEME AND STYLES ---
enum Theme { LAVENDER_BLISS, FOREST_GREEN }

class Style {
    // These are now mutable and changed by applyTheme()
    public static Color PRIMARY_COLOR;
    public static Color ACCENT_COLOR;
    public static Color BACKGROUND_COLOR;
    public static Color CARD_BACKGROUND_COLOR;
    public static Color TEXT_PRIMARY_COLOR;
    public static Color TEXT_SECONDARY_COLOR;
    public static Color DANGER_COLOR;

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


    // Initialize with default theme
    static {
        applyTheme(Theme.LAVENDER_BLISS);
    }

    public static void applyTheme(Theme theme) {
        switch (theme) {
            case FOREST_GREEN:
                PRIMARY_COLOR = new Color(34, 87, 122);
                ACCENT_COLOR = new Color(87, 160, 134);
                BACKGROUND_COLOR = new Color(242, 247, 245);
                CARD_BACKGROUND_COLOR = Color.WHITE;
                TEXT_PRIMARY_COLOR = new Color(40, 40, 40);
                TEXT_SECONDARY_COLOR = new Color(128, 128, 128);
                DANGER_COLOR = new Color(210, 4, 45);
                break;
            case LAVENDER_BLISS:
            default:
                PRIMARY_COLOR = new Color(98, 4, 255);
                ACCENT_COLOR = new Color(255, 105, 180);
                BACKGROUND_COLOR = new Color(248, 244, 248);
                CARD_BACKGROUND_COLOR = Color.WHITE;
                TEXT_PRIMARY_COLOR = new Color(30, 30, 30);
                TEXT_SECONDARY_COLOR = new Color(150, 150, 150);
                DANGER_COLOR = new Color(220, 20, 60);
                break;
        }
    }
}

// --- CUSTOM ROUNDED COMPONENTS ---
class RoundedPanel extends JPanel {
    private final int cornerRadius; private Color borderColor = null;
    public RoundedPanel(int r) { super(); this.cornerRadius = r; setOpaque(false); }
    public void setBorderColor(Color c) { this.borderColor = c; repaint(); }
    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g); Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getBackground()); g2.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);
        if (borderColor != null) { g2.setColor(borderColor); g2.setStroke(new BasicStroke(2)); g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, cornerRadius, cornerRadius); }
        g2.dispose();
    }
}
class RoundedButton extends JButton {
    public RoundedButton(String text) { super(text); setOpaque(false); setContentAreaFilled(false); setFocusPainted(false); setBorderPainted(false); setCursor(new Cursor(Cursor.HAND_CURSOR)); }
    @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (getModel().isPressed()) g2.setColor(getBackground().darker());
        else if (getModel().isRollover()) g2.setColor(getBackground().brighter());
        else g2.setColor(getBackground());
        g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 30, 30)); g2.dispose();
        super.paintComponent(g);
    }
}

// --- DATA MODELS ---
enum Priority { HIGH, MEDIUM, LOW }
enum Status { TODO, IN_PROGRESS, DONE }
enum Category { IDEA("💡"), FOOD("🍽️"), WORK("💼"), SPORT("🏃"), MUSIC("🎵"), OTHER("📌");
    final String icon; Category(String icon) { this.icon = icon; }
}
class Task {
    private static final AtomicInteger idCounter = new AtomicInteger(0); private final int id = idCounter.incrementAndGet();
    private String description; private Status status; private Priority priority; private Category category; private LocalDate dueDate;
    private final LocalDate creationDate = LocalDate.now();
    public Task(String d, Priority p, Category c, LocalDate date, Status s) { this.description = d; this.priority = p; this.category = c; this.dueDate = date; this.status = s; }
    public int getId() { return id; } public String getDescription() { return description; } public void setDescription(String d) { this.description = d; }
    public Status getStatus() { return status; } public void setStatus(Status s) { this.status = s; }
    public Priority getPriority() { return priority; } public void setPriority(Priority p) { this.priority = p; }
    public LocalDate getDueDate() { return dueDate; } public void setDueDate(LocalDate d) { this.dueDate = d; }
    public Category getCategory() { return category; } public void setCategory(Category c) { this.category = c; }
    public LocalDate getCreationDate() { return creationDate; } @Override public String toString() { return description; }
}
class User {
    private String username; private String password; private String role; private String email; private String phone;
    private LocalDateTime lastLoginTime;
    public User(String u, String p, String r, String e, String ph) { this.username = u; this.password = p; this.role = r; this.email = e; this.phone = ph; }
    public String getUsername() { return username; } public String getPassword() { return password; }
    public String getRole() { return role; } public void setRole(String r) { this.role = r; }
    public String getEmail() { return email; } public void setEmail(String e) { this.email = e; }
    public String getPhone() { return phone; } public void setPhone(String p) { this.phone = p; }
    public LocalDateTime getLastLoginTime() { return lastLoginTime; } public void setLastLoginTime(LocalDateTime t) { this.lastLoginTime = t; }
}
class Goal {
    private static final AtomicInteger idCounter = new AtomicInteger(0); private final int id = idCounter.incrementAndGet();
    private String title; private String description; private LocalDate targetDate; private boolean isCompleted;
    public Goal(String t, String d, LocalDate date) { this.title = t; this.description = d; this.targetDate = date; this.isCompleted = false; }
    public int getId() { return id; } public String getTitle() { return title; } public void setTitle(String t) { this.title = t; }
    public String getDescription() { return description; } public void setDescription(String d) { this.description = d; }
    public LocalDate getTargetDate() { return targetDate; } public void setTargetDate(LocalDate d) { this.targetDate = d; }
    public boolean isCompleted() { return isCompleted; } public void setCompleted(boolean c) { this.isCompleted = c; }
    @Override public String toString() { return title; }
}
class UserData {
    public static final Map<String, User> users = new HashMap<>();
    static {
        users.put("admin", new User("admin", "admin123", "Admin", "admin@todopetal.com", "9876543210"));
        users.put("user", new User("user", "user123", "User", "user@todopetal.com", "1234567890"));
    }
}
class TaskData {
    public static final Map<String, Vector<Task>> userTasks = new HashMap<>();
    static {
        Vector<Task> sampleTasks = new Vector<>();
        sampleTasks.add(new Task("Finalize project proposal\n[ ] Draft outline\n[ ] Gather resources", Priority.HIGH, Category.WORK, LocalDate.now().plusDays(2), Status.IN_PROGRESS));
        Task completed = new Task("Buy groceries\n[x] Milk\n[x] Bread\n[x] Eggs", Priority.MEDIUM, Category.FOOD, LocalDate.now().minusDays(1), Status.DONE);
        sampleTasks.add(completed);
        sampleTasks.add(new Task("Go for a run", Priority.LOW, Category.SPORT, LocalDate.now(), Status.TODO));
        userTasks.put("user", sampleTasks); userTasks.put("admin", new Vector<>());
    }
    public static void addTask(String u, Task t) { userTasks.computeIfAbsent(u, k -> new Vector<>()).add(t); }
    public static void deleteTask(String u, Task t) { if (userTasks.containsKey(u)) userTasks.get(u).remove(t); }
}
class GoalData {
    public static final Map<String, Vector<Goal>> userGoals = new HashMap<>();
    static {
        Vector<Goal> sampleGoals = new Vector<>();
        sampleGoals.add(new Goal("Learn Java Swing", "Master layouts, components, and event handling.", LocalDate.now().plusMonths(3)));
        userGoals.put("user", sampleGoals); userGoals.put("admin", new Vector<>());
    }
    public static void addGoal(String u, Goal g) { userGoals.computeIfAbsent(u, k -> new Vector<>()).add(g); }
    public static void deleteGoal(String u, Goal g) { if (userGoals.containsKey(u)) userGoals.get(u).remove(g); }
}

// --- LOGIN AND SIGN-UP SCREENS ---
class LoginPage extends JFrame {
    public LoginPage() {
        setTitle("Todo Petal - Login"); setExtendedState(JFrame.MAXIMIZED_BOTH); setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); setLayout(new BorderLayout());
        JSplitPane sp = new JSplitPane(); sp.setEnabled(false); sp.setDividerSize(0); sp.setResizeWeight(0.40); add(sp, BorderLayout.CENTER);
        sp.setLeftComponent(createBrandingPanel()); sp.setRightComponent(createFormPanel()); setVisible(true);
    }
    JPanel createBrandingPanel() {
        JPanel p = new JPanel(new GridBagLayout()); p.setBackground(Style.PRIMARY_COLOR); GridBagConstraints g = new GridBagConstraints(); g.insets = new Insets(10, 20, 10, 20);
        g.gridwidth = GridBagConstraints.REMAINDER; JLabel t = new JLabel("Todo Petal"); t.setFont(Style.FONT_HEADER); t.setForeground(Color.WHITE); p.add(t, g);
        JLabel s = new JLabel("Blossom into Productivity."); s.setFont(Style.FONT_TAGLINE); s.setForeground(new Color(230, 200, 255)); p.add(s, g); return p;
    }
    private JPanel createFormPanel() {
        JPanel p = new JPanel(new GridBagLayout()); p.setBackground(Style.BACKGROUND_COLOR);
        JPanel fc = new JPanel(new GridBagLayout()); fc.setBackground(Style.BACKGROUND_COLOR);
        GridBagConstraints g = new GridBagConstraints(); g.insets = new Insets(10, 5, 10, 5); g.fill = GridBagConstraints.HORIZONTAL; JLabel h = new JLabel("Welcome!");
        h.setFont(Style.FONT_FORM_HEADER); g.gridwidth = 2; g.weighty = 0.5; fc.add(h, g); g.weighty = 0; JTextField uf = addFormField(fc, g, "Username", 1);
        JPasswordField pf = addPasswordField(fc, g, "Password", 2); JComboBox<String> rb = addRoleField(fc, g, "Role", 3); g.gridy = 7; g.insets = new Insets(25, 5, 8, 5);
        JButton lb = new RoundedButton("Login"); lb.setBackground(Style.ACCENT_COLOR); lb.setForeground(Color.WHITE); lb.setFont(Style.FONT_BUTTON); fc.add(lb, g);
        g.gridy = 8; g.insets = new Insets(8, 5, 8, 5); JButton sb = new RoundedButton("Create Account"); sb.setBackground(Style.PRIMARY_COLOR);
        sb.setForeground(Color.WHITE); sb.setFont(Style.FONT_BUTTON); fc.add(sb, g);
        lb.addActionListener(e -> {
            // ### BUG FIX IS HERE ###
            // Added .trim() to remove accidental leading/trailing spaces from username input
            String u = uf.getText().trim();
            String pass = new String(pf.getPassword());
            String r = (String) rb.getSelectedItem();
            if (u.isEmpty() || pass.isEmpty()) { JOptionPane.showMessageDialog(this, "All fields required.", "Login Error", JOptionPane.ERROR_MESSAGE); return; }
            User userData = UserData.users.get(u);
            if (userData != null && userData.getPassword().equals(pass) && userData.getRole().equals(r)) {
                userData.setLastLoginTime(LocalDateTime.now()); dispose();
                if ("Admin".equals(r)) new AdminDashboard(this, userData); else new UserDashboard(this, userData);
            } else { JOptionPane.showMessageDialog(this, "Invalid credentials.", "Login Error", JOptionPane.ERROR_MESSAGE); }
        });
        sb.addActionListener(e -> { dispose(); new SignUpPage(); }); p.add(fc); return p;
    }
    JTextField addFormField(JPanel p, GridBagConstraints g, String l, int y) {
        g.gridy = y * 2 - 1; p.add(new JLabel(l), g); JTextField f = new JTextField(25); f.setFont(Style.FONT_FIELD); g.gridy = y * 2; p.add(f, g); return f;
    }
    JPasswordField addPasswordField(JPanel p, GridBagConstraints g, String l, int y) {
        g.gridy = y * 2 - 1; p.add(new JLabel(l), g); JPasswordField f = new JPasswordField(25); f.setFont(Style.FONT_FIELD); g.gridy = y * 2; p.add(f, g); return f;
    }
    JComboBox<String> addRoleField(JPanel p, GridBagConstraints g, String l, int y) {
        g.gridy = y * 2 - 1; p.add(new JLabel(l), g); JComboBox<String> f = new JComboBox<>(new String[]{"User", "Admin"}); f.setFont(Style.FONT_FIELD); g.gridy = y * 2; p.add(f, g); return f;
    }
}
class SignUpPage extends JFrame {
    public SignUpPage() {
        setTitle("Todo Petal - Create Account"); setExtendedState(JFrame.MAXIMIZED_BOTH); setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); setLayout(new BorderLayout());
        JSplitPane sp = new JSplitPane(); sp.setEnabled(false); sp.setDividerSize(0); sp.setResizeWeight(0.40); add(sp, BorderLayout.CENTER);
        sp.setLeftComponent(new LoginPage().createBrandingPanel());
        JPanel p = new JPanel(new GridBagLayout()); p.setBackground(Style.BACKGROUND_COLOR);
        JPanel fc = new JPanel(new GridBagLayout()); fc.setBackground(Style.BACKGROUND_COLOR);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 5, 8, 5); g.fill = GridBagConstraints.HORIZONTAL; JLabel h = new JLabel("Create an Account"); h.setFont(Style.FONT_FORM_HEADER);
        g.gridwidth = 2; g.weighty = 0.5; fc.add(h, g); g.weighty = 0; LoginPage lp = new LoginPage(); JTextField uf = lp.addFormField(fc, g, "Username", 1);
        JPasswordField pf = lp.addPasswordField(fc, g, "Password", 2); JTextField ef = lp.addFormField(fc, g, "Email", 3); JTextField phf = lp.addFormField(fc, g, "Phone Number", 4);
        JComboBox<String> rb = lp.addRoleField(fc, g, "Role", 5); g.insets = new Insets(20, 5, 8, 5); g.gridy = 11; JButton regb = new RoundedButton("Register");
        regb.setBackground(Style.ACCENT_COLOR); regb.setForeground(Color.WHITE); fc.add(regb, g); g.insets = new Insets(8, 5, 8, 5); g.gridy = 12;
        JButton backb = new RoundedButton("Back to Login"); backb.setBackground(Style.PRIMARY_COLOR); backb.setForeground(Color.WHITE); fc.add(backb, g);
        regb.addActionListener(e -> { String u = uf.getText().trim(); String pass = new String(pf.getPassword()); String email = ef.getText().trim(); String phone = phf.getText().trim(); String role = (String) rb.getSelectedItem();
            if (u.isEmpty() || pass.isEmpty() || email.isEmpty() || phone.isEmpty()) { JOptionPane.showMessageDialog(this, "All fields required.", "Error", JOptionPane.ERROR_MESSAGE); return; }
            if (!Pattern.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$", email)) { JOptionPane.showMessageDialog(this, "Invalid email format.", "Error", JOptionPane.ERROR_MESSAGE); return; }
            if (UserData.users.containsKey(u)) { JOptionPane.showMessageDialog(this, "Username exists.", "Error", JOptionPane.ERROR_MESSAGE); return; }
            UserData.users.put(u, new User(u, pass, role, email, phone)); TaskData.userTasks.put(u, new Vector<>());
            GoalData.userGoals.put(u, new Vector<>());
            JOptionPane.showMessageDialog(this, "Account created!", "Success", JOptionPane.INFORMATION_MESSAGE); dispose(); new LoginPage();
        });
        backb.addActionListener(e -> { dispose(); new LoginPage(); }); p.add(fc); sp.setRightComponent(p); setVisible(true);
    }
}

// --- USER DASHBOARD ---
class UserDashboard extends JFrame {
    private final User user; private final LoginPage loginPageInstance; private final CardLayout cardLayout = new CardLayout();
    private final JPanel mainPanel = new JPanel(cardLayout); private DefaultListModel<Task> listModel; private JList<Task> taskList;
    private JComboBox<String> sortBox, filterBox; private int hoveredIndex = -1;
    private final Vector<JLabel> sidebarItems = new Vector<>();

    public UserDashboard(LoginPage lp, User u) {
        this.user = u; this.loginPageInstance = lp; setTitle("Todo Petal | " + u.getUsername()); setExtendedState(JFrame.MAXIMIZED_BOTH); setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(0,0));
        add(createSidebar(), BorderLayout.WEST);

        mainPanel.add(createDashboardView(), "TASKS");
        mainPanel.add(new CalendarViewPanel(user), "CALENDAR");
        mainPanel.add(new GoalsViewPanel(user), "GOALS");
        mainPanel.add(new SettingsViewPanel(user, this), "SETTINGS");

        add(mainPanel, BorderLayout.CENTER);
        cardLayout.show(mainPanel, "TASKS");
        setVisible(true);
    }
    private JPanel createDashboardView() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(15, 25, 15, 25));

        JPanel headerPanel = new JPanel(new BorderLayout()); headerPanel.setOpaque(false);
        JLabel todayLabel = new JLabel("My Tasks"); todayLabel.setFont(Style.FONT_HEADER); headerPanel.add(todayLabel, BorderLayout.WEST);
        RoundedButton addBtn = new RoundedButton("+ Add New"); addBtn.setBackground(Style.PRIMARY_COLOR); addBtn.setForeground(Color.WHITE);
        addBtn.setPreferredSize(new Dimension(150, 50));
        JPanel headerControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0)); headerControls.setOpaque(false);
        headerControls.add(addBtn); headerPanel.add(headerControls, BorderLayout.EAST);
        panel.add(headerPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout(10, 20)); centerPanel.setOpaque(false);
        JPanel controlsPanel = new JPanel(new BorderLayout()); controlsPanel.setOpaque(false);

        JPanel sortFilterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT)); sortFilterPanel.setOpaque(false);
        sortFilterPanel.add(new JLabel("Sort by:")); sortBox = new JComboBox<>(new String[]{"Status", "Due Date", "Priority"}); sortFilterPanel.add(sortBox);
        sortFilterPanel.add(new JLabel("  Filter:"));
        filterBox = new JComboBox<>(new String[]{"All Tasks", "Active Tasks", "To Do", "In Progress", "Done Tasks"});
        sortFilterPanel.add(filterBox);
        controlsPanel.add(sortFilterPanel, BorderLayout.EAST); centerPanel.add(controlsPanel, BorderLayout.NORTH);

        listModel = new DefaultListModel<>();
        TaskCardRenderer renderer = new TaskCardRenderer(() -> hoveredIndex);
        taskList = new JList<>(listModel);
        taskList.setCellRenderer(renderer);
        taskList.setBackground(Style.BACKGROUND_COLOR); taskList.setSelectionModel(new DefaultListSelectionModel() { @Override public void setSelectionInterval(int i, int i1) {} });
        loadUserTasks(); JScrollPane sp = new JScrollPane(taskList); sp.setBorder(null); sp.getVerticalScrollBar().setUnitIncrement(16);
        centerPanel.add(sp, BorderLayout.CENTER); panel.add(centerPanel, BorderLayout.CENTER);

        JPanel quickAddPanel = new JPanel(new BorderLayout(10,0)); quickAddPanel.setBorder(new EmptyBorder(10,0,0,0)); quickAddPanel.setOpaque(false);
        JTextField quickAddField = new JTextField("Add a new task and press Enter...");
        quickAddField.setForeground(Style.TEXT_SECONDARY_COLOR);
        RoundedButton quickAddBtn = new RoundedButton("Add"); quickAddBtn.setBackground(Style.ACCENT_COLOR); quickAddBtn.setForeground(Color.WHITE);
        quickAddPanel.add(quickAddField, BorderLayout.CENTER); quickAddPanel.add(quickAddBtn, BorderLayout.EAST);
        panel.add(quickAddPanel, BorderLayout.SOUTH);

        quickAddField.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { if(quickAddField.getText().equals("Add a new task and press Enter...")) { quickAddField.setText(""); quickAddField.setForeground(Style.TEXT_PRIMARY_COLOR); } }
            @Override public void focusLost(FocusEvent e) { if(quickAddField.getText().isEmpty()) { quickAddField.setForeground(Style.TEXT_SECONDARY_COLOR); quickAddField.setText("Add a new task and press Enter..."); } }
        });
        ActionListener quickAddAction = e -> {
            String text = quickAddField.getText().trim();
            if(!text.isEmpty() && !text.equals("Add a new task and press Enter...")) {
                TaskData.addTask(user.getUsername(), new Task(text, Priority.MEDIUM, Category.OTHER, LocalDate.now(), Status.TODO));
                loadUserTasks();
                quickAddField.setText("");
            }
        };
        quickAddField.addActionListener(quickAddAction);
        quickAddBtn.addActionListener(quickAddAction);

        addBtn.addActionListener(e -> showTaskEditor(null));
        sortBox.addActionListener(e -> loadUserTasks()); filterBox.addActionListener(e -> loadUserTasks());
        taskList.addMouseMotionListener(new MouseAdapter() { @Override public void mouseMoved(MouseEvent e) { int i = taskList.locationToIndex(e.getPoint()); if (i != hoveredIndex) { hoveredIndex = i; taskList.repaint(); } } });
        
        taskList.addMouseListener(new MouseAdapter() {
            @Override public void mouseExited(MouseEvent e) { hoveredIndex = -1; taskList.repaint(); }
            @Override public void mouseClicked(MouseEvent e) {
                int index = taskList.locationToIndex(e.getPoint());
                if (index < 0) return;

                if (e.getClickCount() == 2) {
                    showTaskEditor(listModel.getElementAt(index));
                    return;
                }
                
                if (e.getClickCount() == 1) {
                    Rectangle cellBounds = taskList.getCellBounds(index, index);
                    Point relativePoint = new Point(e.getX() - cellBounds.x, e.getY() - cellBounds.y);
                    
                    if (renderer.isDeleteButtonClicked(relativePoint, cellBounds.width)) {
                        Task taskToDelete = listModel.getElementAt(index);
                        int choice = JOptionPane.showConfirmDialog(
                            UserDashboard.this,
                            "Are you sure you want to delete this task?\n\"" + taskToDelete.getDescription().split("\n")[0] + "\"",
                            "Confirm Deletion",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE);
                        
                        if (choice == JOptionPane.YES_OPTION) {
                            TaskData.deleteTask(user.getUsername(), taskToDelete);
                            loadUserTasks();
                        }
                    }
                }
            }
        });
        return panel;
    }
    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(); sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(Style.CARD_BACKGROUND_COLOR); sidebar.setPreferredSize(new Dimension(250, 0)); sidebar.setBorder(new EmptyBorder(20,10,20,10));
        JLabel appName = new JLabel("🌸 Todo Petal"); appName.setFont(Style.FONT_FORM_HEADER); appName.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(appName); sidebar.add(Box.createRigidArea(new Dimension(0, 40)));

        sidebar.add(createSidebarItem("✔️ Tasks", "TASKS", true));
        sidebar.add(createSidebarItem("📅 Calendar", "CALENDAR", false));
        sidebar.add(createSidebarItem("⭐ Goals", "GOALS", false));
        sidebar.add(Box.createVerticalGlue());
        sidebar.add(createSidebarItem("⚙️ Settings", "SETTINGS", false));
        JLabel logoutItem = new JLabel("↩️ Logout");
        configureSidebarLabel(logoutItem);
        logoutItem.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { dispose(); loginPageInstance.setVisible(true); }
        });
        sidebar.add(logoutItem);
        return sidebar;
    }
    private JLabel createSidebarItem(String text, String cardName, boolean isActive) {
        JLabel label = new JLabel(text);
        configureSidebarLabel(label);
        sidebarItems.add(label);
        if (isActive) {
            label.setForeground(Style.PRIMARY_COLOR);
        }
        label.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                cardLayout.show(mainPanel, cardName);
                setActiveSidebarItem(label);
            }
        });
        return label;
    }
    private void configureSidebarLabel(JLabel label){
        label.setFont(Style.FONT_SIDEBAR);
        label.setCursor(new Cursor(Cursor.HAND_CURSOR));
        label.setBorder(new EmptyBorder(15, 20, 15, 20));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        label.setForeground(Style.TEXT_SECONDARY_COLOR);
    }
    private void setActiveSidebarItem(JLabel activeLabel) {
        for (JLabel item : sidebarItems) {
            item.setForeground(item == activeLabel ? Style.PRIMARY_COLOR : Style.TEXT_SECONDARY_COLOR);
        }
    }
    private void showTaskEditor(Task t) {
        JPanel editorView = new TaskEditorPanel(user, t, () -> { loadUserTasks(); cardLayout.show(mainPanel, "TASKS"); });
        mainPanel.add(editorView, "EDITOR");
        cardLayout.show(mainPanel, "EDITOR");
    }
    private void loadUserTasks() {
        listModel.clear(); Vector<Task> ts = TaskData.userTasks.get(user.getUsername()); if (ts != null) { String f = (String) filterBox.getSelectedItem();
            List<Task> filtered = ts.stream().filter(t -> {
                switch (f) {
                    case "Active Tasks": return t.getStatus() != Status.DONE;
                    case "Done Tasks": return t.getStatus() == Status.DONE;
                    case "To Do": return t.getStatus() == Status.TODO;
                    case "In Progress": return t.getStatus() == Status.IN_PROGRESS;
                    default: return true; // All Tasks
                }
            }).collect(Collectors.toList());

            String s = (String) sortBox.getSelectedItem(); if ("Due Date".equals(s)) filtered.sort(Comparator.comparing(Task::getDueDate, Comparator.nullsLast(Comparator.naturalOrder())));
            else if ("Priority".equals(s)) filtered.sort(Comparator.comparing(Task::getPriority)); else if ("Status".equals(s)) filtered.sort(Comparator.comparing(Task::getStatus));
            filtered.forEach(listModel::addElement);
        }
    }
}

// --- DEDICATED TASK EDITOR PANEL ---
class TaskEditorPanel extends JPanel {
    private final User user; private final Task task; private final Runnable onSaveCallback;
    private JTextField titleField; private JTextArea descArea; private JComboBox<Priority> priorityBox; private JComboBox<Category> categoryBox;
    private JComboBox<Status> statusBox; private JLabel selectedDateLabel; private LocalDate selectedDate;
    public TaskEditorPanel(User user, Task task, Runnable onSaveCallback) {
        this.user = user; this.task = (task == null) ? new Task("", Priority.MEDIUM, Category.OTHER, LocalDate.now(), Status.TODO) : task;
        this.onSaveCallback = onSaveCallback; this.selectedDate = this.task.getDueDate();
        setBackground(Style.BACKGROUND_COLOR); setBorder(new EmptyBorder(20, 100, 20, 100)); setLayout(new BorderLayout(20, 20));
        String titleText = (task == null || task.getDescription().isEmpty()) ? "Create New Task" : "Edit Task";
        JLabel title = new JLabel(titleText, SwingConstants.CENTER); title.setFont(Style.FONT_HEADER); add(title, BorderLayout.NORTH);
        JPanel formPanel = new JPanel(new GridBagLayout()); formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints(); gbc.fill = GridBagConstraints.HORIZONTAL; gbc.insets = new Insets(10, 5, 10, 5); gbc.weightx = 1.0;
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; formPanel.add(new JLabel("Task Title"), gbc);
        gbc.gridy++; titleField = new JTextField(this.task.getDescription().split("\n")[0]); titleField.setFont(Style.FONT_FIELD); formPanel.add(titleField, gbc);
        
        JPanel descHeaderPanel = new JPanel(new BorderLayout());
        descHeaderPanel.setOpaque(false);
        descHeaderPanel.add(new JLabel("Detailed Description (add subtasks like [ ] or [x])"), BorderLayout.WEST);
        RoundedButton clearCompletedBtn = new RoundedButton("Clear Completed Subtasks");
        clearCompletedBtn.setFont(Style.FONT_TASK_DETAILS);
        clearCompletedBtn.setBackground(Style.TEXT_SECONDARY_COLOR);
        clearCompletedBtn.setForeground(Color.WHITE);
        descHeaderPanel.add(clearCompletedBtn, BorderLayout.EAST);
        gbc.gridy++; formPanel.add(descHeaderPanel, gbc);

        gbc.gridy++; descArea = new JTextArea(5, 20); descArea.setFont(Style.FONT_FIELD); descArea.setLineWrap(true); descArea.setWrapStyleWord(true);
        String[] descParts = this.task.getDescription().split("\n", 2); if (descParts.length > 1) { descArea.setText(descParts[1]); }
        formPanel.add(new JScrollPane(descArea), gbc);
        gbc.gridy++; gbc.gridwidth = 1; formPanel.add(new JLabel("Priority"), gbc);
        gbc.gridy++; priorityBox = new JComboBox<>(Priority.values()); priorityBox.setSelectedItem(this.task.getPriority()); formPanel.add(priorityBox, gbc);
        gbc.gridx = 1; gbc.gridy = 4; formPanel.add(new JLabel("Category"), gbc);
        gbc.gridy++; categoryBox = new JComboBox<>(Category.values()); categoryBox.setSelectedItem(this.task.getCategory()); formPanel.add(categoryBox, gbc);
        gbc.gridx = 0; gbc.gridy = 6; formPanel.add(new JLabel("Status"), gbc);
        gbc.gridy++; statusBox = new JComboBox<>(Status.values()); statusBox.setSelectedItem(this.task.getStatus()); formPanel.add(statusBox, gbc);
        gbc.gridx = 1; gbc.gridy = 6; JPanel datePanel = new JPanel(new BorderLayout()); datePanel.setOpaque(false);
        RoundedButton calBtn = new RoundedButton("Select Date"); calBtn.setBackground(Style.ACCENT_COLOR); calBtn.setForeground(Color.WHITE);
        selectedDateLabel = new JLabel(); updateSelectedDateLabel(); datePanel.add(calBtn, BorderLayout.NORTH); datePanel.add(selectedDateLabel, BorderLayout.SOUTH);
        gbc.gridy++; formPanel.add(datePanel, gbc); add(formPanel, BorderLayout.CENTER);
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0)); buttonPanel.setOpaque(false);
        RoundedButton saveBtn = new RoundedButton((task == null || task.getDescription().isEmpty()) ? "Create Task" : "Save Changes"); saveBtn.setBackground(Style.PRIMARY_COLOR);
        saveBtn.setForeground(Color.WHITE); saveBtn.setPreferredSize(new Dimension(200, 50));
        RoundedButton cancelBtn = new RoundedButton("Cancel"); cancelBtn.setBackground(Style.TEXT_SECONDARY_COLOR);
        cancelBtn.setForeground(Color.WHITE); cancelBtn.setPreferredSize(new Dimension(150, 50));
        buttonPanel.add(saveBtn); buttonPanel.add(cancelBtn); add(buttonPanel, BorderLayout.SOUTH);
        calBtn.addActionListener(e -> openCalendar()); saveBtn.addActionListener(e -> saveTask()); cancelBtn.addActionListener(e -> onSaveCallback.run());
        
        clearCompletedBtn.addActionListener(e -> {
            String currentDesc = descArea.getText();
            String newDesc = Stream.of(currentDesc.split("\n"))
                                   .filter(line -> !line.trim().startsWith("[x]"))
                                   .collect(Collectors.joining("\n"));
            descArea.setText(newDesc);
        });
    }
    private void openCalendar() { JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Select Date", true); JCalendarPanel cp = new JCalendarPanel(selectedDate); d.add(cp); d.pack(); d.setLocationRelativeTo(this); d.setVisible(true); if(cp.getSelectedDate() != null) { this.selectedDate = cp.getSelectedDate(); updateSelectedDateLabel(); } }
    private void updateSelectedDateLabel() { selectedDateLabel.setText(selectedDate.format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy"))); }
    private void saveTask() { if (titleField.getText().trim().isEmpty()) { JOptionPane.showMessageDialog(this, "Title cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE); return; }
        String fullDesc = titleField.getText().trim() + "\n" + descArea.getText().trim();
        task.setDescription(fullDesc); task.setPriority((Priority) priorityBox.getSelectedItem());
        task.setCategory((Category) categoryBox.getSelectedItem()); task.setStatus((Status) statusBox.getSelectedItem());
        task.setDueDate(selectedDate); if (!TaskData.userTasks.get(user.getUsername()).contains(task)) TaskData.addTask(user.getUsername(), task);
        onSaveCallback.run();
    }
}

// --- INTERACTIVE CALENDAR COMPONENT ---
class JCalendarPanel extends JPanel {
    private LocalDate currentDate; private LocalDate selectedDate; private JLabel monthLabel; private JPanel daysPanel;
    public JCalendarPanel(LocalDate initialDate) {
        this.selectedDate = initialDate; this.currentDate = initialDate.withDayOfMonth(1); setLayout(new BorderLayout(10, 10)); setBorder(new EmptyBorder(10,10,10,10));
        JPanel h = new JPanel(new BorderLayout()); JButton p = new JButton("<"); JButton n = new JButton(">"); monthLabel = new JLabel("", SwingConstants.CENTER);
        monthLabel.setFont(Style.FONT_BUTTON); h.add(p, BorderLayout.WEST); h.add(monthLabel, BorderLayout.CENTER); h.add(n, BorderLayout.EAST); add(h, BorderLayout.NORTH);
        daysPanel = new JPanel(new GridLayout(0, 7, 5, 5)); add(daysPanel, BorderLayout.CENTER);
        p.addActionListener(e -> { currentDate = currentDate.minusMonths(1); drawCalendar(); }); n.addActionListener(e -> { currentDate = currentDate.plusMonths(1); drawCalendar(); });
        drawCalendar();
    }
    private void drawCalendar() {
        daysPanel.removeAll(); monthLabel.setText(currentDate.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
        String[] dayNames = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"}; for(String name : dayNames) { daysPanel.add(new JLabel(name, SwingConstants.CENTER)); }
        int firstDayOfWeek = currentDate.getDayOfWeek().getValue() % 7; for(int i = 0; i < firstDayOfWeek; i++) { daysPanel.add(new JLabel("")); }
        for(int day = 1; day <= currentDate.lengthOfMonth(); day++) { final int d = day; JLabel dayLabel = new JLabel(String.valueOf(day), SwingConstants.CENTER);
            dayLabel.setOpaque(true); dayLabel.setCursor(new Cursor(Cursor.HAND_CURSOR)); LocalDate thisDate = currentDate.withDayOfMonth(day);
            if(thisDate.equals(selectedDate)) { dayLabel.setBackground(Style.PRIMARY_COLOR); dayLabel.setForeground(Color.WHITE); }
            else if (thisDate.equals(LocalDate.now())) { dayLabel.setBackground(Style.ACCENT_COLOR); dayLabel.setForeground(Color.WHITE); }
            else { dayLabel.setBackground(Style.CARD_BACKGROUND_COLOR); }
            dayLabel.addMouseListener(new MouseAdapter() { public void mouseClicked(MouseEvent e) { selectedDate = currentDate.withDayOfMonth(d); Window w = SwingUtilities.getWindowAncestor(JCalendarPanel.this); if (w instanceof JDialog) ((JDialog) w).dispose(); } });
            daysPanel.add(dayLabel);
        } revalidate(); repaint();
    }
    public LocalDate getSelectedDate() { return selectedDate; }
}

// --- CUSTOM RENDERERS ---
class TaskCardRenderer implements ListCellRenderer<Task> {
    private final java.util.function.Supplier<Integer> hoveredIndexSupplier;
    private final RoundedButton deleteButton = new RoundedButton("Delete");
    private final Dimension deleteButtonSize = new Dimension(90, 35);
    
    public TaskCardRenderer(java.util.function.Supplier<Integer> h) {
        this.hoveredIndexSupplier = h;
        deleteButton.setBackground(Style.DANGER_COLOR);
        deleteButton.setForeground(Color.WHITE);
        deleteButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        deleteButton.setPreferredSize(deleteButtonSize);
    }
    
    @Override public Component getListCellRendererComponent(JList<? extends Task> list, Task task, int index, boolean isSelected, boolean cellHasFocus) {
        RoundedPanel card = new RoundedPanel(25); card.setBackground(Style.CARD_BACKGROUND_COLOR); card.setLayout(new BorderLayout(15, 15)); card.setBorder(new EmptyBorder(20, 20, 20, 20));
        if (index == hoveredIndexSupplier.get()) card.setBorderColor(Style.PRIMARY_COLOR);

        JPanel westPanel = new JPanel(); westPanel.setOpaque(false); westPanel.setLayout(new BoxLayout(westPanel, BoxLayout.Y_AXIS)); westPanel.setAlignmentY(Component.TOP_ALIGNMENT);
        JPanel pi = new JPanel(); pi.setPreferredSize(new Dimension(8, 8)); pi.setBackground(task.getPriority() == Priority.HIGH ? Style.PRIORITY_HIGH : task.getPriority() == Priority.MEDIUM ? Style.PRIORITY_MEDIUM : Style.PRIORITY_LOW);
        westPanel.add(pi); card.add(westPanel, BorderLayout.WEST);

        JPanel details = new JPanel(); details.setOpaque(false); details.setLayout(new BoxLayout(details, BoxLayout.Y_AXIS));
        String title = task.getDescription().split("\n")[0]; JLabel dl = new JLabel(title); dl.setFont(Style.FONT_TASK_DESC);
        String dt = task.getCategory().icon + " " + task.getCategory().name() + "  |  Due: " + task.getDueDate().format(DateTimeFormatter.ofPattern("dd MMM")) + "  |  Priority: " + task.getPriority().name();
        JLabel sl = new JLabel(dt); sl.setFont(Style.FONT_TASK_DETAILS); sl.setForeground(Style.TEXT_SECONDARY_COLOR);
        if (task.getStatus() == Status.DONE) { dl.setText("<html><strike>" + title + "</strike></html>"); dl.setForeground(Style.TEXT_SECONDARY_COLOR); }
        else { dl.setForeground(Style.TEXT_PRIMARY_COLOR); }
        details.add(dl); details.add(Box.createRigidArea(new Dimension(0, 5))); details.add(sl);

        String[] lines = task.getDescription().split("\n"); int totalSubtasks = 0; int completedSubtasks = 0;
        for (String line : lines) { if (line.trim().startsWith("[ ]")) totalSubtasks++; if (line.trim().startsWith("[x]")) { totalSubtasks++; completedSubtasks++; } }
        if (totalSubtasks > 0) {
            details.add(Box.createRigidArea(new Dimension(0, 8)));
            JLabel subtaskLabel = new JLabel("Subtasks: " + completedSubtasks + "/" + totalSubtasks + " completed");
            subtaskLabel.setFont(Style.FONT_TASK_DETAILS); subtaskLabel.setForeground(Style.TEXT_SECONDARY_COLOR);
            details.add(subtaskLabel);
        }
        card.add(details, BorderLayout.CENTER);

        JPanel eastPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        eastPanel.setOpaque(false);
        
        JLabel statusLabel = new JLabel(task.getStatus().toString().replace("_", " ")); statusLabel.setFont(Style.FONT_TASK_DETAILS); statusLabel.setForeground(Color.WHITE);
        statusLabel.setOpaque(true); statusLabel.setBorder(new EmptyBorder(3,8,3,8));
        switch (task.getStatus()) { case TODO: statusLabel.setBackground(Style.STATUS_TODO); break; case IN_PROGRESS: statusLabel.setBackground(Style.STATUS_IN_PROGRESS); break; case DONE: statusLabel.setBackground(Style.STATUS_DONE); break; }
        
        eastPanel.add(statusLabel);
        eastPanel.add(deleteButton);
        card.add(eastPanel, BorderLayout.EAST); 
        
        card.doLayout();
        
        return card;
    }

    public boolean isDeleteButtonClicked(Point point, int cellWidth) {
        int buttonX = cellWidth - deleteButtonSize.width - 20;
        int buttonY = 20;
        Rectangle buttonBounds = new Rectangle(buttonX, buttonY, deleteButtonSize.width, deleteButtonSize.height);
        return buttonBounds.contains(point);
    }
}
class ModernTableCellRenderer extends DefaultTableCellRenderer {
    public Component getTableCellRendererComponent(JTable t, Object v, boolean is, boolean hf, int r, int c) {
        Component co = super.getTableCellRendererComponent(t, v, is, hf, r, c);
        co.setBackground(is ? t.getSelectionBackground() : r % 2 == 0 ? Style.CARD_BACKGROUND_COLOR : new Color(252, 252, 252));
        setBorder(new EmptyBorder(5, 10, 5, 10)); return co;
    }
}

// --- ADMIN DASHBOARD ---
class AdminDashboard extends JFrame {
    private final LoginPage loginPageInstance; private DefaultTableModel userTableModel; private JTable userTable;
    public AdminDashboard(LoginPage lp, User adminUser) {
        this.loginPageInstance = lp; setTitle("Todo Petal - Admin Panel"); setExtendedState(JFrame.MAXIMIZED_BOTH); setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(15, 15)); getContentPane().setBackground(Style.BACKGROUND_COLOR); getRootPane().setBorder(new EmptyBorder(15, 15, 15, 15));
        JPanel hp = new JPanel(new BorderLayout()); hp.setOpaque(false); JLabel h = new JLabel("Admin Dashboard"); h.setFont(Style.FONT_HEADER);
        hp.add(h, BorderLayout.WEST); RoundedButton lob = new RoundedButton("Logout"); lob.setBackground(Style.DANGER_COLOR);
        lob.setForeground(Color.WHITE); lob.setPreferredSize(new Dimension(140, 45)); lob.addActionListener(e -> { dispose(); loginPageInstance.setVisible(true); });
        hp.add(lob, BorderLayout.EAST); add(hp, BorderLayout.NORTH); JPanel mc = new JPanel(new BorderLayout(15, 15)); mc.setOpaque(false);
        RoundedPanel sp = createStatsPanel(); mc.add(sp, BorderLayout.NORTH); RoundedPanel up = new RoundedPanel(25); up.setBackground(Style.CARD_BACKGROUND_COLOR);
        up.setLayout(new BorderLayout(10, 10)); up.setBorder(new EmptyBorder(10, 10, 10, 10));
        userTableModel = new DefaultTableModel(new String[]{"Username", "Role", "Email", "Phone", "Last Active"}, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        userTable = new JTable(userTableModel); setupModernTable(userTable); loadUsers(); up.add(new JScrollPane(userTable), BorderLayout.CENTER);
        JPanel bp = new JPanel(new FlowLayout(FlowLayout.RIGHT)); bp.setOpaque(false);
        RoundedButton addBtn = new RoundedButton("Add User"); addBtn.setBackground(Style.PRIMARY_COLOR); addBtn.setForeground(Color.WHITE); bp.add(addBtn);
        RoundedButton deleteBtn = new RoundedButton("Delete User"); deleteBtn.setBackground(Style.DANGER_COLOR); deleteBtn.setForeground(Color.WHITE); bp.add(deleteBtn);
        up.add(bp, BorderLayout.SOUTH); addBtn.addActionListener(e -> addNewUser()); deleteBtn.addActionListener(e -> deleteSelectedUser());
        mc.add(up, BorderLayout.CENTER); add(mc, BorderLayout.CENTER); setVisible(true);
    }
    private RoundedPanel createStatsPanel() { RoundedPanel p = new RoundedPanel(25); p.setBackground(Style.CARD_BACKGROUND_COLOR); p.setLayout(new FlowLayout(FlowLayout.LEFT, 30, 20)); JLabel l = new JLabel("Total Users: " + UserData.users.size()); l.setFont(Style.FONT_FORM_HEADER); l.setForeground(Style.TEXT_PRIMARY_COLOR); p.add(l); return p; }
    private void loadUsers() { userTableModel.setRowCount(0); for (User u : UserData.users.values()) { userTableModel.addRow(new Object[]{u.getUsername(), u.getRole(), u.getEmail(), u.getPhone(), formatTimeAgo(u.getLastLoginTime())}); } }
    private String formatTimeAgo(LocalDateTime t) { if (t == null) return "Never"; Duration d = Duration.between(t, LocalDateTime.now()); long s = d.getSeconds(); if (s < 60) return "Online Now"; if (s < 3600) return (s / 60) + "m ago"; if (s < 86400) return (s / 3600) + "h ago"; if (s < 172800) return "Yesterday"; return t.format(DateTimeFormatter.ofPattern("dd MMM yyyy")); }
    private void addNewUser() { UserEditorDialog dialog = new UserEditorDialog(this, null); dialog.setVisible(true); if(dialog.isSaved()) { User newUser = dialog.getUser(); UserData.users.put(newUser.getUsername(), newUser); TaskData.userTasks.put(newUser.getUsername(), new Vector<>()); GoalData.userGoals.put(newUser.getUsername(), new Vector<>()); loadUsers(); } }
    private void deleteSelectedUser() { int row = userTable.getSelectedRow(); if(row == -1) { JOptionPane.showMessageDialog(this, "Please select a user to delete.", "Error", JOptionPane.WARNING_MESSAGE); return; } String u = (String) userTableModel.getValueAt(row, 0); if(u.equals("admin")) { JOptionPane.showMessageDialog(this, "Cannot delete the primary admin account.", "Error", JOptionPane.ERROR_MESSAGE); return; } int c = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete user '" + u + "'?", "Confirm Deletion", JOptionPane.YES_NO_OPTION); if(c == JOptionPane.YES_OPTION) { UserData.users.remove(u); TaskData.userTasks.remove(u); GoalData.userGoals.remove(u); loadUsers(); } }
    private void setupModernTable(JTable t) { t.setRowHeight(40); t.setGridColor(Style.BACKGROUND_COLOR); t.setSelectionBackground(Style.PRIMARY_COLOR); t.setSelectionForeground(Color.WHITE); t.setDefaultRenderer(Object.class, new ModernTableCellRenderer()); JTableHeader h = t.getTableHeader(); h.setBackground(Style.BACKGROUND_COLOR); h.setForeground(Style.TEXT_PRIMARY_COLOR); h.setPreferredSize(new Dimension(100, 40)); }
}

// --- DIALOG FOR ADMIN TO ADD/EDIT USERS ---
class UserEditorDialog extends JDialog {
    private boolean saved = false; private User user;
    private JTextField userField, emailField, phoneField; private JPasswordField passField; private JComboBox<String> roleBox;
    public UserEditorDialog(Frame owner, User userToEdit) {
        super(owner, (userToEdit == null ? "Add New User" : "Edit User"), true);
        this.user = (userToEdit == null) ? new User("", "", "User", "", "") : userToEdit;
        setSize(450, 400); setLocationRelativeTo(owner); setLayout(new BorderLayout(10, 10)); getRootPane().setBorder(new EmptyBorder(10,10,10,10));
        JPanel form = new JPanel(new GridBagLayout()); GridBagConstraints gbc = new GridBagConstraints(); gbc.fill = GridBagConstraints.HORIZONTAL; gbc.insets = new Insets(5, 5, 5, 5); gbc.weightx = 1.0;
        userField = new JTextField(user.getUsername()); if(userToEdit != null) userField.setEnabled(false);
        passField = new JPasswordField(); emailField = new JTextField(user.getEmail()); phoneField = new JTextField(user.getPhone()); roleBox = new JComboBox<>(new String[]{"User", "Admin"});
        gbc.gridy = 0; form.add(new JLabel("Username:"), gbc); gbc.gridy++; form.add(userField, gbc);
        gbc.gridy++; form.add(new JLabel("Password:"), gbc); gbc.gridy++; form.add(passField, gbc);
        gbc.gridy++; form.add(new JLabel("Email:"), gbc); gbc.gridy++; form.add(emailField, gbc);
        gbc.gridy++; form.add(new JLabel("Phone:"), gbc); gbc.gridy++; form.add(phoneField, gbc);
        gbc.gridy++; form.add(new JLabel("Role:"), gbc); gbc.gridy++; form.add(roleBox, gbc);
        add(form, BorderLayout.CENTER);
        RoundedButton saveBtn = new RoundedButton("Save"); saveBtn.setBackground(Style.PRIMARY_COLOR); saveBtn.setForeground(Color.WHITE);
        saveBtn.addActionListener(e -> saveUser()); add(saveBtn, BorderLayout.SOUTH);
    }
    private void saveUser() {
        String u = userField.getText().trim(); String p = new String(passField.getPassword());
        if(u.isEmpty() || (p.isEmpty() && user.getUsername().isEmpty()) ) { JOptionPane.showMessageDialog(this, "Username and Password are required for new users.", "Error", JOptionPane.ERROR_MESSAGE); return; }
        if(UserData.users.containsKey(u) && user.getUsername().isEmpty()) { JOptionPane.showMessageDialog(this, "Username already exists.", "Error", JOptionPane.ERROR_MESSAGE); return; }
        this.user = new User(u, p, (String)roleBox.getSelectedItem(), emailField.getText().trim(), phoneField.getText().trim());
        saved = true; dispose();
    }
    public boolean isSaved() { return saved; }
    public User getUser() { return user; }
}

// --- FEATURE PANELS (CALENDAR, GOALS, SETTINGS) ---
class CalendarViewPanel extends JPanel {
    private final User user;
    private LocalDate currentDate;
    private final JLabel monthLabel;
    private final JPanel daysPanel;
    private final JList<Task> taskDisplayList;
    private final DefaultListModel<Task> taskListModel;

    public CalendarViewPanel(User user) {
        this.user = user;
        this.currentDate = LocalDate.now().withDayOfMonth(1);
        setOpaque(false);
        setBorder(new EmptyBorder(15, 25, 15, 25));
        setLayout(new BorderLayout(20, 20));

        JLabel titleLabel = new JLabel("Task Calendar");
        titleLabel.setFont(Style.FONT_HEADER);
        add(titleLabel, BorderLayout.NORTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.65);
        splitPane.setBorder(null);
        splitPane.setOpaque(false);
        add(splitPane, BorderLayout.CENTER);

        JPanel calendarContainer = new JPanel(new BorderLayout(10, 10));
        calendarContainer.setOpaque(false);
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JButton prevButton = new JButton("<");
        JButton nextButton = new JButton(">");
        monthLabel = new JLabel("", SwingConstants.CENTER);
        monthLabel.setFont(Style.FONT_BUTTON);
        header.add(prevButton, BorderLayout.WEST);
        header.add(monthLabel, BorderLayout.CENTER);
        header.add(nextButton, BorderLayout.EAST);
        calendarContainer.add(header, BorderLayout.NORTH);

        daysPanel = new JPanel(new GridLayout(0, 7, 8, 8));
        daysPanel.setOpaque(false);
        calendarContainer.add(daysPanel, BorderLayout.CENTER);
        splitPane.setLeftComponent(calendarContainer);

        JPanel taskDisplayPanel = new RoundedPanel(20);
        taskDisplayPanel.setBackground(Style.CARD_BACKGROUND_COLOR);
        taskDisplayPanel.setLayout(new BorderLayout());
        taskDisplayPanel.setBorder(new EmptyBorder(10,10,10,10));
        JLabel tasksForDayLabel = new JLabel("Tasks for Selected Day");
        tasksForDayLabel.setFont(Style.FONT_FORM_HEADER);
        tasksForDayLabel.setBorder(new EmptyBorder(5,5,10,5));
        taskDisplayPanel.add(tasksForDayLabel, BorderLayout.NORTH);

        taskListModel = new DefaultListModel<>();
        taskDisplayList = new JList<>(taskListModel);
        taskDisplayList.setCellRenderer(new TaskCardRenderer(() -> -1));
        taskDisplayPanel.add(new JScrollPane(taskDisplayList), BorderLayout.CENTER);
        splitPane.setRightComponent(taskDisplayPanel);

        prevButton.addActionListener(e -> { currentDate = currentDate.minusMonths(1); drawCalendar(); });
        nextButton.addActionListener(e -> { currentDate = currentDate.plusMonths(1); drawCalendar(); });

        drawCalendar();
        loadTasksForDate(LocalDate.now());
    }

    private void drawCalendar() {
        daysPanel.removeAll();
        monthLabel.setText(currentDate.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
        Vector<Task> userTasksVec = TaskData.userTasks.get(user.getUsername());
        Map<LocalDate, Long> tasksPerDay = userTasksVec.stream()
                .collect(Collectors.groupingBy(Task::getDueDate, Collectors.counting()));

        String[] dayNames = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (String name : dayNames) {
            daysPanel.add(new JLabel(name, SwingConstants.CENTER));
        }

        int firstDayOfWeek = currentDate.getDayOfWeek().getValue() % 7;
        for (int i = 0; i < firstDayOfWeek; i++) {
            daysPanel.add(new JLabel(""));
        }

        for (int day = 1; day <= currentDate.lengthOfMonth(); day++) {
            final LocalDate thisDate = currentDate.withDayOfMonth(day);
            JPanel dayCell = new JPanel(new BorderLayout());
            dayCell.setOpaque(true);
            dayCell.setBackground(Style.CARD_BACKGROUND_COLOR);
            dayCell.setBorder(BorderFactory.createLineBorder(Style.BACKGROUND_COLOR));

            JLabel dayLabel = new JLabel(String.valueOf(day), SwingConstants.LEFT);
            dayLabel.setBorder(new EmptyBorder(3,5,3,5));
            if (thisDate.equals(LocalDate.now())) {
                dayLabel.setFont(new Font(dayLabel.getFont().getName(), Font.BOLD, dayLabel.getFont().getSize()));
                dayLabel.setForeground(Style.ACCENT_COLOR);
            }
            dayCell.add(dayLabel, BorderLayout.NORTH);

            if (tasksPerDay.containsKey(thisDate)) {
                JLabel taskMarker = new JLabel("● " + tasksPerDay.get(thisDate) + " tasks");
                taskMarker.setForeground(Style.PRIMARY_COLOR);
                taskMarker.setHorizontalAlignment(SwingConstants.CENTER);
                dayCell.add(taskMarker, BorderLayout.CENTER);
            }
            dayCell.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            dayCell.addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    loadTasksForDate(thisDate);
                }
            });
            daysPanel.add(dayCell);
        }
        revalidate();
        repaint();
    }

    private void loadTasksForDate(LocalDate date) {
        taskListModel.clear();
        Vector<Task> userTasksVec = TaskData.userTasks.get(user.getUsername());
        if (userTasksVec != null) {
            userTasksVec.stream()
                .filter(task -> task.getDueDate().equals(date))
                .forEach(taskListModel::addElement);
        }
    }
}
class GoalsViewPanel extends JPanel {
    private final User user;
    private final DefaultListModel<Goal> goalListModel;
    private final JList<Goal> goalList;

    public GoalsViewPanel(User user) {
        this.user = user;
        setOpaque(false);
        setBorder(new EmptyBorder(15, 25, 15, 25));
        setLayout(new BorderLayout(15, 15));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel titleLabel = new JLabel("My Goals");
        titleLabel.setFont(Style.FONT_HEADER);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        RoundedButton addBtn = new RoundedButton("+ Add Goal");
        addBtn.setBackground(Style.PRIMARY_COLOR);
        addBtn.setForeground(Color.WHITE);
        headerPanel.add(addBtn, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        goalListModel = new DefaultListModel<>();
        goalList = new JList<>(goalListModel);
        loadUserGoals();
        goalList.setCellRenderer(new GoalCardRenderer());
        add(new JScrollPane(goalList), BorderLayout.CENTER);

        addBtn.addActionListener(e -> showGoalEditor(null));
        goalList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    int index = goalList.locationToIndex(evt.getPoint());
                    showGoalEditor(goalListModel.getElementAt(index));
                }
            }
        });
    }

    private void loadUserGoals() {
        goalListModel.clear();
        Vector<Goal> goals = GoalData.userGoals.get(user.getUsername());
        if (goals != null) {
            goals.forEach(goalListModel::addElement);
        }
    }

    private void showGoalEditor(Goal goal) {
        GoalEditorDialog dialog = new GoalEditorDialog((Frame) SwingUtilities.getWindowAncestor(this), goal);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            Goal savedGoal = dialog.getGoal();
            if (!GoalData.userGoals.get(user.getUsername()).contains(savedGoal)) {
                GoalData.addGoal(user.getUsername(), savedGoal);
            }
            loadUserGoals();
        }
    }

    class GoalCardRenderer extends JPanel implements ListCellRenderer<Goal> {
        private final JLabel title = new JLabel();
        private final JLabel desc = new JLabel();
        private final JLabel date = new JLabel();
        private final JCheckBox completed = new JCheckBox("Completed");

        public GoalCardRenderer() {
            setLayout(new BorderLayout(10, 5));
            setBorder(new EmptyBorder(10, 15, 10, 15));
            setBackground(Color.WHITE);

            title.setFont(Style.FONT_TASK_DESC);
            desc.setFont(Style.FONT_TASK_DETAILS);
            date.setFont(Style.FONT_TASK_DETAILS);
            date.setForeground(Style.TEXT_SECONDARY_COLOR);

            JPanel textPanel = new JPanel();
            textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
            textPanel.add(title);
            textPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            textPanel.add(desc);
            textPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            textPanel.add(date);

            add(textPanel, BorderLayout.CENTER);
            add(completed, BorderLayout.EAST);

            completed.addActionListener(e -> {
                int index = goalList.getSelectedIndex();
                if(index != -1) {
                    Goal selectedGoal = goalListModel.getElementAt(index);
                    selectedGoal.setCompleted(completed.isSelected());
                    goalList.repaint();
                }
            });
        }
        @Override
        public Component getListCellRendererComponent(JList<? extends Goal> list, Goal goal, int index, boolean isSelected, boolean cellHasFocus) {
            title.setText(goal.getTitle());
            desc.setText("<html>" + goal.getDescription().replaceAll("\n", "<br>") + "</html>");
            date.setText("Target: " + goal.getTargetDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
            completed.setSelected(goal.isCompleted());

            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(Color.WHITE);
                setForeground(list.getForeground());
            }
            return this;
        }
    }

    class GoalEditorDialog extends JDialog {
        private boolean saved = false;
        private Goal goal;
        private final JTextField titleField;
        private final JTextArea descArea;

        public GoalEditorDialog(Frame owner, Goal goalToEdit) {
            super(owner, (goalToEdit == null ? "Add New Goal" : "Edit Goal"), true);
            this.goal = (goalToEdit == null) ? new Goal("", "", LocalDate.now().plusMonths(1)) : goalToEdit;

            setSize(450, 400); setLocationRelativeTo(owner);
            setLayout(new BorderLayout(10, 10)); getRootPane().setBorder(new EmptyBorder(10,10,10,10));
            JPanel form = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.HORIZONTAL; gbc.insets = new Insets(5,5,5,5); gbc.weightx = 1.0;

            titleField = new JTextField(goal.getTitle());
            descArea = new JTextArea(goal.getDescription(), 5, 20);

            gbc.gridy = 0; form.add(new JLabel("Goal Title:"), gbc);
            gbc.gridy++; form.add(titleField, gbc);
            gbc.gridy++; form.add(new JLabel("Description:"), gbc);
            gbc.gridy++; form.add(new JScrollPane(descArea), gbc);
            add(form, BorderLayout.CENTER);

            RoundedButton saveBtn = new RoundedButton("Save");
            saveBtn.setBackground(Style.PRIMARY_COLOR); saveBtn.setForeground(Color.WHITE);
            saveBtn.addActionListener(e -> saveGoal());
            add(saveBtn, BorderLayout.SOUTH);
        }
        private void saveGoal() {
            if (titleField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Title cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE); return;
            }
            goal.setTitle(titleField.getText().trim());
            goal.setDescription(descArea.getText().trim());
            saved = true; dispose();
        }
        public boolean isSaved() { return saved; }
        public Goal getGoal() { return goal; }
    }
}
class SettingsViewPanel extends JPanel {
    private final User user;
    private final JTextField emailField, phoneField;

    public SettingsViewPanel(User user, JFrame parentFrame) {
        this.user = user;
        setOpaque(false);
        setBorder(new EmptyBorder(15, 25, 15, 25));
        setLayout(new BorderLayout(15, 15));

        JLabel titleLabel = new JLabel("Settings");
        titleLabel.setFont(Style.FONT_HEADER);
        add(titleLabel, BorderLayout.NORTH);

        JPanel settingsContent = new JPanel();
        settingsContent.setOpaque(false);
        settingsContent.setLayout(new BoxLayout(settingsContent, BoxLayout.Y_AXIS));

        // Profile Panel
        JPanel profilePanel = createTitledPanel("Profile Information");
        profilePanel.setLayout(new GridLayout(0, 2, 10, 10));
        profilePanel.add(new JLabel("Email:"));
        emailField = new JTextField(user.getEmail());
        profilePanel.add(emailField);
        profilePanel.add(new JLabel("Phone:"));
        phoneField = new JTextField(user.getPhone());
        profilePanel.add(phoneField);
        profilePanel.add(new JLabel()); // Spacer
        RoundedButton saveProfileBtn = new RoundedButton("Save Profile");
        saveProfileBtn.setBackground(Style.PRIMARY_COLOR);
        saveProfileBtn.setForeground(Color.WHITE);
        profilePanel.add(saveProfileBtn);
        settingsContent.add(profilePanel);

        // Theme Panel
        JPanel themePanel = createTitledPanel("Appearance");
        themePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        JRadioButton lavenderRadio = new JRadioButton("Lavender Bliss");
        lavenderRadio.setSelected(true);
        JRadioButton forestRadio = new JRadioButton("Forest Green");
        ButtonGroup themeGroup = new ButtonGroup();
        themeGroup.add(lavenderRadio);
        themeGroup.add(forestRadio);
        themePanel.add(lavenderRadio);
        themePanel.add(forestRadio);
        settingsContent.add(themePanel);

        settingsContent.add(Box.createVerticalGlue()); // Push content to the top
        add(settingsContent, BorderLayout.CENTER);

        // Action Listeners
        saveProfileBtn.addActionListener(e -> {
            user.setEmail(emailField.getText());
            user.setPhone(phoneField.getText());
            JOptionPane.showMessageDialog(this, "Profile updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        });
        ActionListener themeListener = e -> {
            if (lavenderRadio.isSelected()) Style.applyTheme(Theme.LAVENDER_BLISS);
            else Style.applyTheme(Theme.FOREST_GREEN);
            SwingUtilities.updateComponentTreeUI(parentFrame);
        };
        lavenderRadio.addActionListener(themeListener);
        forestRadio.addActionListener(themeListener);
    }
    private JPanel createTitledPanel(String title) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), title, 0, 0, Style.FONT_BUTTON, Style.TEXT_PRIMARY_COLOR
        ));
        return panel;
    }
}
