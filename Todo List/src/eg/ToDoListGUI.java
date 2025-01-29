package eg;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ToDoListGUI extends JFrame {
    // Nested ToDoListLogic class
    public static class ToDoListLogic {
        public static class Task {
            private final String name;
            private final String priority;
            private String status;

            public Task(String name, String priority) {
                this.name = name;
                this.priority = priority;
                this.status = "";
            }

            public String getName() {
                return name;
            }

            public String getPriority() {
                return priority;
            }

            public String getStatus() {
                return status;
            }

            public void setStatus(String status) {
                this.status = status;
            }
        }

        private final List<Task> tasks;

        public ToDoListLogic() {
            tasks = new ArrayList<>();
        }

        public void addTask(String name, String priority) {
            tasks.add(new Task(name, priority));
        }

        public void removeTask(int index) {
            if (index >= 0 && index < tasks.size()) {
                tasks.remove(index);
            }
        }

        public List<Task> getTasks() {
            return tasks;
        }

        public List<Task> getFilteredTasks(String status) {
            List<Task> filtered = new ArrayList<>();
            for (Task task : tasks) {
                if (task.getStatus().equals(status)) {
                    filtered.add(task);
                }
            }
            return filtered;
        }
    }

    private final ToDoListLogic logic;
    private final DefaultTableModel tableModel;

    public ToDoListGUI() {
        super("To-Do List Manager");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        logic = new ToDoListLogic();

        // Main Table
        String[] columns = {"Task Name", "Priority", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make all cells non-editable
            }
        };
        JTable mainTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(mainTable);

        // Buttons Panel
        JPanel buttonPanel = new JPanel();
        JButton addTaskButton = new JButton("Add Task");
        JButton editTaskButton = new JButton("Edit Task");
        JButton executedTasksButton = new JButton("Executed Tasks");
        JButton nonExecutedTasksButton = new JButton("Non-Executed Tasks");

        buttonPanel.add(addTaskButton);
        buttonPanel.add(editTaskButton);
        buttonPanel.add(executedTasksButton);
        buttonPanel.add(nonExecutedTasksButton);

        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Button Actions
        addTaskButton.addActionListener(e -> openAddTaskDialog());
        editTaskButton.addActionListener(e -> openEditTaskDialog());
        executedTasksButton.addActionListener(e -> showFilteredTasks("✔", "Executed Tasks"));
        nonExecutedTasksButton.addActionListener(e -> showFilteredTasks("✖", "Non-Executed Tasks"));

        setSize(600, 400);
        setVisible(true);
    }

    private void openAddTaskDialog() {
        JDialog dialog = new JDialog(this, "Add Task", true);
        dialog.setSize(300, 200);
        dialog.setLayout(new GridLayout(4, 2));

        JTextField taskNameField = new JTextField();
        JComboBox<String> priorityBox = new JComboBox<>(new String[]{"High", "Medium", "Low"});
        JButton addButton = new JButton("Add");

        dialog.add(new JLabel("Task Name:"));
        dialog.add(taskNameField);
        dialog.add(new JLabel("Priority:"));
        dialog.add(priorityBox);
        dialog.add(new JLabel());
        dialog.add(addButton);

        addButton.addActionListener(e -> {
            String taskName = taskNameField.getText();
            if (taskName.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Task name cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String priority = (String) priorityBox.getSelectedItem();
            logic.addTask(taskName, priority);
            updateMainTable();
            dialog.dispose();
        });

        dialog.setVisible(true);
    }

    private void openEditTaskDialog() {
        JDialog dialog = new JDialog(this, "Edit Tasks", true);
        dialog.setSize(600, 400);
        dialog.setLayout(new BorderLayout());

        String[] columns = {"Task Name", "Priority", "Done", "Not Done", "Remove"};
        DefaultTableModel editTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public Class<?> getColumnClass(int column) {
                if (column == 2 || column == 3) return Boolean.class; // Checkboxes
                return String.class;
            }
        };
        JTable editTable = new JTable(editTableModel);

        // Populate Edit Table
        for (ToDoListLogic.Task task : logic.getTasks()) {
            editTableModel.addRow(new Object[]{task.getName(), task.getPriority(), false, false, "Remove"});
        }

        // Save Changes Button
        JButton saveChangesButton = new JButton("Save Changes");
        saveChangesButton.addActionListener(e -> {
            for (int i = 0; i < editTableModel.getRowCount(); i++) {
                boolean isDone = (boolean) editTableModel.getValueAt(i, 2);
                boolean isNotDone = (boolean) editTableModel.getValueAt(i, 3);

                if (isDone && isNotDone) {
                    JOptionPane.showMessageDialog(dialog, "Task cannot be both Done and Not Done!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                ToDoListLogic.Task task = logic.getTasks().get(i);
                if (isDone) task.setStatus("✔");
                else if (isNotDone) task.setStatus("✖");
                else task.setStatus("");
            }
            updateMainTable();
            dialog.dispose();
        });

        dialog.add(new JScrollPane(editTable), BorderLayout.CENTER);
        dialog.add(saveChangesButton, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void showFilteredTasks(String status, String title) {
        JDialog dialog = new JDialog(this, title, true);
        dialog.setSize(600, 300);
        dialog.setLayout(new BorderLayout());

        DefaultTableModel filteredModel = new DefaultTableModel(new String[]{"Task Name", "Priority", "Status"}, 0);
        JTable filteredTable = new JTable(filteredModel);

        for (ToDoListLogic.Task task : logic.getFilteredTasks(status)) {
            filteredModel.addRow(new Object[]{task.getName(), task.getPriority(), task.getStatus()});
        }

        JButton goBackButton = new JButton("Go Back");
        goBackButton.addActionListener(e -> dialog.dispose());

        dialog.add(new JScrollPane(filteredTable), BorderLayout.CENTER);
        dialog.add(goBackButton, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void updateMainTable() {
        tableModel.setRowCount(0);
        for (ToDoListLogic.Task task : logic.getTasks()) {
            tableModel.addRow(new Object[]{task.getName(), task.getPriority(), task.getStatus()});
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ToDoListGUI::new);
    }
}
