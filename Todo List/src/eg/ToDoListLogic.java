import java.util.ArrayList;
import java.util.List;

public class ToDoListLogic {
    public static class Task {
        private String name;
        private String priority;
        private String status; // "✔", "✖", or ""

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
        this.tasks = new ArrayList<>();
    }

    // Add a new task
    public void addTask(String name, String priority) {
        tasks.add(new Task(name, priority));
    }

    // Remove a task by index
    public void removeTask(int index) {
        tasks.remove(index);
    }

    // Get all tasks
    public List<Task> getTasks() {
        return tasks;
    }

    // Get filtered tasks
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
