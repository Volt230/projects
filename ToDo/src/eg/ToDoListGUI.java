package eg;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.util.*;
import javax.swing.table.TableColumn;

public class ToDoListGUI extends JFrame {

    private final ToDoListLogic logic;
    private final DefaultTableModel tableModel;

    public ToDoListGUI() {
        super("To-Do List Manager");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        logic = new ToDoListLogic();

        // Load tasks from file when the application starts
        loadTasksFromFile();

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

        // Window listener to save tasks on exit
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                saveTasksToFile(); // Save tasks when window is closing
            }
        });

        // Update the table with tasks immediately
        updateMainTable();

        setSize(600, 400);
        setVisible(true);
    }

    private void saveTasksToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("tasks.txt"))) {
            for (ToDoListLogic.Task task : logic.getTasks()) {
                writer.write(task.getName() + "," + task.getPriority() + "," + task.getStatus());
                writer.newLine();
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving tasks to file.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadTasksFromFile() {
    try (BufferedReader reader = new BufferedReader(new FileReader("tasks.txt"))) {
        String line;
        while ((line = reader.readLine()) != null) {
            String[] taskData = line.split(",");
            if (taskData.length == 3) {
                String name = taskData[0];
                String priority = taskData[1];
                String status = taskData[2].trim();  // Trim to remove any extra spaces
                
                // Create the task with name and priority
                logic.addTask(name, priority);
                ToDoListLogic.Task task = logic.getTasks().getLast();
                
                // Set the status correctly
                if (status.equals("✔") || status.equals("✖")) {
                    task.setStatus(status);  // Only set if status is valid
                } else {
                    task.setStatus("Unknown");  // If status is not valid, set it to "Unknown"
                }
            }
        }
    } catch (IOException e) {
        // Handle file not found or read error, no need to show an error since the file might not exist on first run
    }
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
        if (logic.getTasks().isEmpty()) {
            JOptionPane.showMessageDialog(this, "No tasks available to edit.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog(this, "Edit Tasks", true);
        dialog.setSize(600, 400);
        dialog.setLayout(new BorderLayout());

        String[] columns = {"Task Name", "Priority", "Done", "Not Done", "Remove"};
        DefaultTableModel editTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public Class<?> getColumnClass(int column) {
                if (column == 2 || column == 3) return Boolean.class; // Checkboxes for Done/Not Done
                if (column == 4) return JButton.class; // For the Remove button
                return String.class;
            }
        };
        JTable editTable = new JTable(editTableModel);

        // Populate Edit Table with tasks
        for (ToDoListLogic.Task task : logic.getTasks()) {
            boolean isDone = task.getStatus().equals("✔");
            boolean isNotDone = task.getStatus().equals("✖");
            JButton removeButton = new JButton("Remove");

            // Add ActionListener to the remove button
            TableColumn removeColumn = editTable.getColumnModel().getColumn(4);
            removeColumn.setCellRenderer((table, value, isSelected, hasFocus, row, col) -> new JButton("Remove"));
            removeColumn.setCellEditor(new DefaultCellEditor(new JCheckBox()) {
                private final JButton button = new JButton("Remove");

                {
                    button.addActionListener(e -> {
                        int row = editTable.getSelectedRow();
                        if (row >= 0) {
                            editTableModel.removeRow(row); // Remove row from edit table
                            logic.removeTask(row);        // Remove corresponding task from logic
                            updateMainTable();           // Refresh all tables
                        }
                    });
                }

                @Override
                public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
                    return button;
                }
            });
            editTableModel.addRow(new Object[]{task.getName(), task.getPriority(), isDone, isNotDone, removeButton});
        }

        // Save Changes Button
        JButton saveChangesButton = new JButton("Save Changes");
        saveChangesButton.addActionListener(e -> {
            // Iterate over the rows of the edit table
            for (int i = 0; i < editTableModel.getRowCount(); i++) {
                // Get the 'Done' and 'Not Done' status from the table
                boolean isDone = (boolean) editTableModel.getValueAt(i, 2);
                boolean isNotDone = (boolean) editTableModel.getValueAt(i, 3);

                // Check if both 'Done' and 'Not Done' are selected for the same task
                if (isDone && isNotDone) {
                    JOptionPane.showMessageDialog(dialog, "Task cannot be both Done and Not Done!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Get the corresponding task from the logic's task list
                ToDoListLogic.Task task = logic.getTasks().get(i);

                // Set the status based on the checkboxes
                if (isDone) {
                    task.setStatus("✔"); // Task is done
                } else if (isNotDone) {
                    task.setStatus("✖"); // Task is not done
                } else {
                    task.setStatus("Unknown"); // Task is in progress (no status)
                }
            }

            // Update the main table after saving the changes
            updateMainTable();
            dialog.dispose();
        });

        dialog.add(new JScrollPane(editTable), BorderLayout.CENTER);
        dialog.add(saveChangesButton, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void showFilteredTasks(String status, String title) {
        LinkedList<ToDoListLogic.Task> filteredTasks = logic.getFilteredTasks(status);
        DefaultTableModel filteredTableModel = new DefaultTableModel(new String[]{"Task Name", "Priority", "Status"}, 0);
        for (ToDoListLogic.Task task : filteredTasks) {
            filteredTableModel.addRow(new Object[]{task.getName(), task.getPriority(), task.getStatus()});
        }
        JTable filteredTable = new JTable(filteredTableModel);
        JOptionPane.showMessageDialog(this, new JScrollPane(filteredTable), title, JOptionPane.PLAIN_MESSAGE);
    }

    private void updateMainTable() {
        tableModel.setRowCount(0); // Clear the existing rows
        for (ToDoListLogic.Task task : logic.getTasks()) {
            tableModel.addRow(new Object[]{task.getName(), task.getPriority(), task.getStatus()});
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ToDoListGUI());
    }
}
