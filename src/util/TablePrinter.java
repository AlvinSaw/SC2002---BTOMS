package util;

import java.util.List;
import java.util.Arrays;

/**
 * Utility class for printing formatted ASCII tables to the console
 */
public class TablePrinter {
    
    /**
     * Prints a formatted ASCII table with the provided headers and data
     * @param headers An array of column headers
     * @param data A 2D array of data where each row is an array of strings
     */
    public static void printTable(String[] headers, String[][] data) {
        // Calculate column widths
        int[] columnWidths = new int[headers.length];
        for (int i = 0; i < headers.length; i++) {
            columnWidths[i] = headers[i].length();
        }
        
        for (String[] row : data) {
            for (int i = 0; i < row.length && i < columnWidths.length; i++) {
                if (row[i] != null) {
                    columnWidths[i] = Math.max(columnWidths[i], row[i].length());
                }
            }
        }
        
        // Add padding to column widths
        for (int i = 0; i < columnWidths.length; i++) {
            columnWidths[i] += 2; // 1 space on each side for padding
        }
        
        // Calculate total width
        int totalWidth = Arrays.stream(columnWidths).sum() + columnWidths.length + 1;
        
        // Print top border
        printBorder(totalWidth);
        
        // Print headers
        printRow(headers, columnWidths);
        
        // Print header-data separator
        printSeparator(columnWidths);
        
        // Print data rows
        for (String[] row : data) {
            printRow(row, columnWidths);
        }
        
        // Print bottom border
        printBorder(totalWidth);
    }
    
    /**
     * Prints a single row of the table
     * @param columns The columns of a row to print
     * @param columnWidths The width of each column
     */
    private static void printRow(String[] columns, int[] columnWidths) {
        StringBuilder sb = new StringBuilder("|");
        
        for (int i = 0; i < columnWidths.length; i++) {
            String value = (i < columns.length && columns[i] != null) ? columns[i] : "";
            sb.append(" ");
            sb.append(value);
            // Right padding to column width
            int padding = columnWidths[i] - value.length() - 1;
            sb.append(" ".repeat(padding));
            sb.append("|");
        }
        
        System.out.println(sb.toString());
    }
    
    /**
     * Prints a horizontal border
     * @param width The width of the border
     */
    private static void printBorder(int width) {
        System.out.println("-".repeat(width));
    }
    
    /**
     * Prints a horizontal separator between header and data
     * @param columnWidths The width of each column
     */
    private static void printSeparator(int[] columnWidths) {
        StringBuilder sb = new StringBuilder("|");
        
        for (int width : columnWidths) {
            sb.append("-".repeat(width));
            sb.append("|");
        }
        
        System.out.println(sb.toString());
    }
    
    /**
     * Example usage of the TablePrinter
     */
    public static void main(String[] args) {
        // Example usage
        String[] headers = {"ID", "Name", "Age", "Department"};
        String[][] data = {
            {"1", "John Doe", "28", "Engineering"},
            {"2", "Jane Smith", "32", "Marketing"},
            {"3", "Bob Johnson", "45", "Finance"},
            {"4", "Alice Williams", "26", "Human Resources"}
        };
        
        printTable(headers, data);
    }
}
