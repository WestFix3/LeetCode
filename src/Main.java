import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class Main {
    public static class ListNode {
       int val;
       ListNode next;
       ListNode() {}
       ListNode(int val) { this.val = val; }
       ListNode(int val, ListNode next) { this.val = val; this.next = next; }
   }

    public static ListNode addTwoNumbers(ListNode l1, ListNode l2) {

    }

    public static ListNode create(int[] tomb){
        ListNode ls = new ListNode(tomb[tomb.length]);
        for(int i=tomb.length-1; i>0; i--){
            ListNode l = new ListNode(tomb[i]);
            ls.next = l;
            ls = l;
        }
    }

    public static void main(String[] args) {
        int[] p1 = new int[]{2, 4, 3};
        int[] p2 = new int[]{5, 6, 4};

        //System.out.println(addTwoNumbers());
    }
}