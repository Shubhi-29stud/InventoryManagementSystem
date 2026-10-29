import java.sql.*;
import java.util.Scanner;

public class InventoryManagementApp {
    // Update your database URL, username, and password accordingly
    private static final String DB_URL = "jdbc:mysql://localhost:3306/retail_db";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "admin"; // Change to your MySQL password

    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Scanner scanner = new Scanner(System.in)) {

            while (true) {
                System.out.println("\n--- Inventory Management System ---");
                System.out.println("1. Insert Product Records");
                System.out.println("2. Conditional Retrieval and Sorting");
                System.out.println("3. Update Product Stock");
                System.out.println("4. Search and Modify Product Details");
                System.out.println("5. Aggregate Query and Conditional Deletion");
                System.out.println("6. Exit");
                System.out.print("Choose an option: ");

                int choice = scanner.nextInt();
                switch (choice) {
                    case 1:
                        insertProducts(conn, scanner);
                        break;
                    case 2:
                        retrieveAndSortProducts(conn, scanner);
                        break;
                    case 3:
                        updateProductStock(conn, scanner);
                        break;
                    case 4:
                        searchAndModifyPrice(conn, scanner);
                        break;
                    case 5:
                        aggregateAndDelete(conn, scanner);
                        break;
                    case 6:
                        System.out.println("Exiting application. Goodbye!");
                        return;
                    default:
                        System.out.println("Invalid choice! Please choose between 1 and 6.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
        }
    }

    // (a) Insert Product Records
    private static void insertProducts(Connection conn, Scanner scanner) {
        String sql = "INSERT INTO Product (product_id, product_name, category, quantity, price) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            System.out.println("Enter details for 3 products:");
            for (int i = 1; i <= 3; i++) {
                System.out.println("\nProduct " + i + ":");
                System.out.print("Enter Product ID: ");
                int id = scanner.nextInt();
                scanner.nextLine(); // consume newline
                System.out.print("Enter Product Name: ");
                String name = scanner.nextLine();
                System.out.print("Enter Category: ");
                String category = scanner.nextLine();
                System.out.print("Enter Quantity: ");
                int qty = scanner.nextInt();
                System.out.print("Enter Price: ");
                double price = scanner.nextDouble();

                pstmt.setInt(1, id);
                pstmt.setString(2, name);
                pstmt.setString(3, category);
                pstmt.setInt(4, qty);
                pstmt.setDouble(5, price);

                try {
                    pstmt.executeUpdate();
                    System.out.println("Product inserted successfully!");
                } catch (SQLIntegrityConstraintViolationException e) {
                    System.out.println("Error: Product with ID " + id + " already exists. Skipping insertion.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error inserting records: " + e.getMessage());
        }
    }

    // (b) Conditional Retrieval and Sorting
    private static void retrieveAndSortProducts(Connection conn, Scanner scanner) {
        scanner.nextLine(); // consume newline
        System.out.print("Enter category to search: ");
        String category = scanner.nextLine();

        String sql = "SELECT * FROM Product WHERE category = ? AND quantity < 20 ORDER BY price DESC";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, category);
            try (ResultSet rs = pstmt.executeQuery()) {
                System.out.println("\n--- Products in Category '" + category + "' with Quantity < 20 (Sorted by Price Desc) ---");
                System.out.printf("%-10s %-20s %-15s %-10s %-10s\n", "ID", "Name", "Category", "Qty", "Price");
                System.out.println("------------------------------------------------------------------");
                boolean found = false;
                while (rs.next()) {
                    found = true;
                    System.out.printf("%-10d %-20s %-15s %-10d %-10.2f\n",
                            rs.getInt("product_id"),
                            rs.getString("product_name"),
                            rs.getString("category"),
                            rs.getInt("quantity"),
                            rs.getDouble("price"));
                }
                if (!found) {
                    System.out.println("No products found matching the criteria.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving records: " + e.getMessage());
        }
    }

    // (c) Update Product Stock
    private static void updateProductStock(Connection conn, Scanner scanner) {
        System.out.print("Enter Product ID to update stock: ");
        int id = scanner.nextInt();
        System.out.print("Enter quantity to be added: ");
        int addQty = scanner.nextInt();

        String checkSql = "SELECT quantity FROM Product WHERE product_id = ?";
        String updateSql = "UPDATE Product SET quantity = quantity + ? WHERE product_id = ?";

        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql);
             PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {

            checkStmt.setInt(1, id);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                updateStmt.setInt(1, addQty);
                updateStmt.setInt(2, id);
                updateStmt.executeUpdate();
                System.out.println("Stock updated successfully for Product ID: " + id);
            } else {
                System.out.println("Product with ID " + id + " does not exist.");
            }
        } catch (SQLException e) {
            System.err.println("Error updating stock: " + e.getMessage());
        }
    }

    // (d) Search and Modify Product Details
    private static void searchAndModifyPrice(Connection conn, Scanner scanner) {
        System.out.print("Enter Product ID: ");
        int id = scanner.nextInt();
        System.out.print("Enter new price: ");
        double newPrice = scanner.nextDouble();

        if (newPrice <= 0) {
            System.out.println("Error: New price must be greater than zero.");
            return;
        }

        String selectSql = "SELECT * FROM Product WHERE product_id = ?";
        String updateSql = "UPDATE Product SET price = ? WHERE product_id = ?";

        try (PreparedStatement selectStmt = conn.prepareStatement(selectSql);
             PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {

            selectStmt.setInt(1, id);
            ResultSet rs = selectStmt.executeQuery();

            if (rs.next()) {
                // Product exists, update price
                updateStmt.setDouble(1, newPrice);
                updateStmt.setInt(2, id);
                updateStmt.executeUpdate();

                System.out.println("Price updated successfully! Updated Product Details:");
                System.out.println("ID: " + rs.getInt("product_id"));
                System.out.println("Name: " + rs.getString("product_name"));
                System.out.println("Category: " + rs.getString("category"));
                System.out.println("Quantity: " + rs.getInt("quantity"));
                System.out.println("Price: " + newPrice);
            } else {
                System.out.println("Product with ID " + id + " does not exist.");
            }
        } catch (SQLException e) {
            System.err.println("Error modifying product: " + e.getMessage());
        }
    }

    // (e) Aggregate Query and Conditional Deletion
    private static void aggregateAndDelete(Connection conn, Scanner scanner) {
        String aggSql = "SELECT SUM(quantity * price) AS total_value FROM Product";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(aggSql)) {
            if (rs.next()) {
                double totalValue = rs.getDouble("total_value");
                System.out.println("\nTotal Inventory Value: " + totalValue);
            }
        } catch (SQLException e) {
            System.err.println("Error calculating inventory value: " + e.getMessage());
        }

        System.out.print("Enter Product ID to delete: ");
        int id = scanner.nextInt();

        String checkSql = "SELECT quantity FROM Product WHERE product_id = ?";
        String deleteSql = "DELETE FROM Product WHERE product_id = ?";

        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql);
             PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {

            checkStmt.setInt(1, id);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                int qty = rs.getInt("quantity");
                if (qty == 0) {
                    deleteStmt.setInt(1, id);
                    deleteStmt.executeUpdate();
                    System.out.println("Product with ID " + id + " has quantity 0 and has been deleted.");
                } else {
                    System.out.println("Cannot delete product. Product ID " + id + " has a non-zero quantity (" + qty + ").");
                }
            } else {
                System.out.println("Product with ID " + id + " does not exist.");
            }
        } catch (SQLException e) {
            System.err.println("Error processing deletion: " + e.getMessage());
        }
    }
}