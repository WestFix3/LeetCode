import java.util.Arrays;

public class Main {
    public static class SubrectangleQueries {
        public static int[][] rectangle;
        public SubrectangleQueries(int[][] rectangle) {
            this.rectangle = rectangle;
        }

        public void updateSubrectangle(int row1, int col1, int row2, int col2, int newValue) {
            for(int i=row1; i<=row2; i++){
                for(int j=col1; j<=col2; j++){
                    rectangle[i][j] = newValue;
                }
            }
        }

        public int getValue(int row, int col) {
            return rectangle[row][col];
        }
    }

    public static void main(String[] args) {
        String[] metodusok = new String[]{"SubrectangleQueries","getValue","updateSubrectangle","getValue","getValue","updateSubrectangle","getValue","getValue"};
        int[][][] adatok = {{{1, 2, 1}, {4, 3, 4}, {3, 2, 1}, {1, 1, 1}},
                {{0, 2}},
                {{0, 0, 3, 2, 5}},
                {{0, 2}}, {{3, 1}},
                {{3, 0, 3, 2, 10}},
                {{3, 1}}, {{0, 2}}};
        //System.out.println(metodusok[0] + " : " + Arrays.deepToString(adatok[0]));

        SubrectangleQueries subrectangleQueries = null;
        for(int i=0; i<metodusok.length; i++){
            switch(metodusok[i]){
                case "SubrectangleQueries":
                    subrectangleQueries = new SubrectangleQueries(adatok[i]);
                    break;
                case "updateSubrectangle":
                    subrectangleQueries.updateSubrectangle(adatok[i][0][0], adatok[i][0][1], adatok[i][0][2], adatok[i][0][3], adatok[i][0][4]);
                    break;
                case "getValue":
                    subrectangleQueries.getValue(adatok[i][0][0], adatok[i][0][1]);
                    break;
            }
        }
    }
}