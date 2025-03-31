import java.util.ArrayList;
import java.util.List;

public class Main {
    public static int[] pivotArray(int[] nums, int pivot) {
        List<Integer> nums_before = new ArrayList<>();
        List<Integer> nums_after = new ArrayList<>();
        int count_pivot = 0;

        for(int i : nums){
            if(i == pivot){
                count_pivot++;
            }
            if(i < pivot){
                nums_before.add(i);
            }
            if(i > pivot){
                nums_after.add(i);
            }
        }

        int k=0;
        for(int i=0; i<nums_before.size(); i++){
            nums[k] = nums_before.get(i);
            k++;
        }

        for(int i=0; i<count_pivot; i++){
            nums[k] = pivot;
            k++;
        }

        for(int i=0; i<nums_after.size(); i++){
            nums[k] = nums_after.get(i);
            k++;
        }

        return nums;
    }

    public static void main(String[] args) {
        int[] nums = new int[]{9,12,5,10,14,3,10};

        int pivot = 10;

        int[] result = pivotArray(nums, pivot);

        for(int i : result){
            System.out.print(i + ", ");
        }
    }
}