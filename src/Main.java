public class Main {
    public static int index = 0;
    public static boolean utak(char[][] board, int i, int j, String word){
//        if(index+1 > word.length()){
//            System.out.println(board[i][j] + " " + word.charAt(index-1));
//            System.out.println("Ide se jutunk be");
//            return true;
//        }

        int[][] lehetseges_utak = new int[4][2];
//        System.out.println("COUNT: " + " | i: " + i + " | j: " + j);
        System.out.println(board[i][j] + " " + word.charAt(index-1));
        lehetseges_utak[0][0] = i-1;
        lehetseges_utak[0][1] = j;
        lehetseges_utak[1][0] = i;
        lehetseges_utak[1][1] = j-1;
        lehetseges_utak[2][0] = i;
        lehetseges_utak[2][1] = j+1;
        lehetseges_utak[3][0] = i+1;
        lehetseges_utak[3][1] = j;

//        for(int[] ut : lehetseges_utak){
//            System.out.print("i: " + ut[0] + " | j: " + ut[1]);
//            System.out.println();
//        }

        ut(board, lehetseges_utak, word);

        if(index+1 > word.length()){
            System.out.println(board[i][j] + " " + word.charAt(index-1));
            return true;
        }
        return false;
    }

    public static void ut(char[][] board, int[][] lehetseges_utak, String word){
        if (index+1 > word.length()) {
            return;
        }
        //IDE KELL MÉG, HOGY NE TUDJUNK KIUGRANI AZ INTERVALLUMBÓL
        if(lehetseges_utak[0][0] > 0){
            if (index+1 > word.length()) {
                return;
            }
            if(board[lehetseges_utak[0][0]][lehetseges_utak[0][1]] == word.charAt(index)) {
                index++;
                utak(board, lehetseges_utak[0][0], lehetseges_utak[0][1], word);
            }
        }
        if(lehetseges_utak[1][1] > 0){
            if (index+1 > word.length()) {
                return;
            }
            if(board[lehetseges_utak[1][0]][lehetseges_utak[1][1]] == word.charAt(index)) {
                index++;
                utak(board, lehetseges_utak[1][0], lehetseges_utak[1][1], word);
            }
        }
        if(lehetseges_utak[2][1] < board[0].length){
            if (index+1 > word.length()) {
//                System.out.println("Visszaesunk");
                return;
            }
            if(board[lehetseges_utak[2][0]][lehetseges_utak[2][1]] == word.charAt(index)) {
                //System.out.println("BELÉPEK1");
                index++;
                utak(board, lehetseges_utak[2][0], lehetseges_utak[2][1], word);
            }
        }
        if(lehetseges_utak[3][0] < board.length){
            if (index+1 > word.length()) {
                return;
            }
            if(board[lehetseges_utak[3][0]][lehetseges_utak[3][1]] == word.charAt(index)) {
                index++;
                //System.out.println("BELÉPEK2");
                utak(board, lehetseges_utak[3][0], lehetseges_utak[3][1], word);
            }
        }
    }

    public static boolean exist(char[][] board, String word) {
        for(int i=0; i<board.length; i++){
            for(int j=0; j<board[i].length; j++) {
                if(board[i][j] == word.charAt(0)){
                    index++;
                    if(utak(board, i, j, word)){
//                        System.out.println("HIBA?");
                        return true;
                    }
                    System.out.println("I: " + i + " | J: " + j);
                }else{
                    index = 0;
                }
            }
        }

        return false;
    }

    public static void main(String[] args) {
        char[][] board = new char[][]{{'A','B','C','E'},{'S','F','C','S'},{'A','D','E','E'}};
        String words = "ABCCED";

        System.out.println(exist(board, words));
    }
}