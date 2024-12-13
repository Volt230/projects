package eg;
import java.util.LinkedList;

public class ToDoListLogic {
        // Task class
        public static class Task {
            private final String name;
            private final String priority;
            private String status;

            public Task(String name, String priority) {
                this.name = name;
                this.priority = priority;
                this.status = "Unknown";
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

            // Priority levels as integers for sorting
            public int getPriorityLevel() {
                switch (priority) {
                    case "High":
                        return 1;
                    case "Medium":
                        return 2;
                    case "Low":
                        return 3;
                    default:
                        return Integer.MAX_VALUE;
                }
            }
        }

        private final LinkedList<Task> tasks;

        public ToDoListLogic() {
            tasks = new LinkedList<>();
        }

        public void addTask(String name, String priority) {
            tasks.add(new Task(name, priority));
            mergeSort(tasks); // Sort the tasks after each addition
        }

        public void removeTask(int index) {
            if (index >= 0 && index < tasks.size()) {
                tasks.remove(index);
            }
        }

        public LinkedList<Task> getTasks() {
            return tasks;
        }

        public LinkedList<Task> getFilteredTasks(String status) {
            LinkedList<Task> filtered = new LinkedList<>();
            for (Task task : tasks) {
                if (task.getStatus().equals(status)) {
                    filtered.add(task);
                }
            }
            return filtered;
        }

        // Merge Sort method to sort tasks based on priority level
        public void mergeSort(LinkedList<Task> list) {
            if (list.size() <= 1) {
                return; // Base case: single element or empty list
            }

            // Split the list into two halves
            int middle = list.size() / 2;
            LinkedList<Task> left = new LinkedList<>(list.subList(0, middle));
            LinkedList<Task> right = new LinkedList<>(list.subList(middle, list.size()));

            // Recursively sort both halves
            mergeSort(left);
            mergeSort(right);

            // Merge the sorted halves
            merge(list, left, right);
        }

        // Merge two sorted lists into one sorted list
        private void merge(LinkedList<Task> list, LinkedList<Task> left, LinkedList<Task> right) {
            list.clear();
            int i = 0, j = 0;

            // Merge the two lists while maintaining order
            while (i < left.size() && j < right.size()) {
                if (left.get(i).getPriorityLevel() < right.get(j).getPriorityLevel()) {
                    list.add(left.get(i++));
                } else {
                    list.add(right.get(j++));
                }
            }

            // If there are any remaining elements in left, add them
            while (i < left.size()) {
                list.add(left.get(i++));
            }

            // If there are any remaining elements in right, add them
            while (j < right.size()) {
                list.add(right.get(j++));
            }
        }
    }
