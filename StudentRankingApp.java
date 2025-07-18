import java.sql.*;
import java.util.*;

public class StudentRankingApp {

    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        try {
           
            Class.forName("com.mysql.cj.jdbc.Driver");

            
            Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/studentdb", "root", "Bulbul@28"
            );

            System.out.print("Enter Student Name (max 30): ");
            String studentName = sc.nextLine();

            System.out.print("Enter College Name (max 50): ");
            String collegeName = sc.nextLine();

            float round1 = getValidatedFloat("Enter Round 1 (0-10): ", 0, 10);
            float round2 = getValidatedFloat("Enter Round 2 (0-10): ", 0, 10);
            float round3 = getValidatedFloat("Enter Round 3 (0-10): ", 0, 10);
            float techRound = getValidatedFloat("Enter Technical Round (0-20): ", 0, 20);

          
            float total = round1 + round2 + round3 + techRound;
            String result = total >= 35 ? "Selected" : "Rejected";

        
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO student_results (student_name, college_name, round1, round2, round3, tech_round, total_marks, result, `rank`) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)"
            );

            ps.setString(1, studentName);
            ps.setString(2, collegeName);
            ps.setFloat(3, round1);
            ps.setFloat(4, round2);
            ps.setFloat(5, round3);
            ps.setFloat(6, techRound);
            ps.setFloat(7, total);
            ps.setString(8, result);
            ps.setNull(9, java.sql.Types.INTEGER); 

            ps.executeUpdate();
            System.out.println(" Student data inserted!");

           
            updateRanks(conn);

           
            displayAll(conn);

            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static float getValidatedFloat(String prompt, float min, float max) {
        float value;
        while (true) {
            System.out.print(prompt);
            value = sc.nextFloat();
            if (value >= min && value <= max) break;
            System.out.println(" Please enter a value between " + min + " and " + max);
        }
        return value;
    }

    public static void updateRanks(Connection conn) throws SQLException {
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery("SELECT id, total_marks FROM student_results ORDER BY total_marks DESC");

        Map<Float, Integer> marksToRank = new LinkedHashMap<>();
        int rank = 1;

      
        while (rs.next()) {
            float total = rs.getFloat("total_marks");
            if (!marksToRank.containsKey(total)) {
                marksToRank.put(total, rank);
            }
            rank++;
        }

        
        for (Map.Entry<Float, Integer> entry : marksToRank.entrySet()) {
            float score = entry.getKey();
            int r = entry.getValue();

            PreparedStatement ps = conn.prepareStatement("UPDATE student_results SET `rank` = ? WHERE total_marks = ?");
            ps.setInt(1, r);
            ps.setFloat(2, score);
            ps.executeUpdate();
        }

        System.out.println(" Ranks updated!");
    }

    public static void displayAll(Connection conn) throws SQLException {
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery("SELECT * FROM student_results ORDER BY `rank` ASC");

        System.out.println("\n Final Results:");
        System.out.println("-----");
        while (rs.next()) {
            System.out.printf(
                "ID: %d | Name: %s | Total: %.2f | Result: %s | Rank: %d\n",
                rs.getInt("id"),
                rs.getString("student_name"),
                rs.getFloat("total_marks"),
                rs.getString("result"),
                rs.getInt("rank")
            );
        }
        System.out.println("----");
    }
}

