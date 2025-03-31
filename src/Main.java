public class Main {
    public static int[] pivotArray(int[] nums, int pivot) {
        for(int i=0; i<nums.length; i++){
            if(nums[i] >= pivot){
                int j1 = 0;
                for(int j=i; j<nums.length; j++){
                    j1 = j;
                    if(nums[j] < pivot){
                        int seg = nums[j];
                        nums[j] = nums[i];
                        nums[i] = seg;
                        break;
                    }
                }
                if(j1 == nums.length-1){
                    break;
                }
            }
        }

        int j=nums.length-1;
        int index = nums.length-1;
        while(index>0){
            if(nums[j] < pivot){
                break;
            }

            if(nums[j] == pivot){
                int k = j;
                while(nums[k] < pivot){
                    if(nums[k] != pivot){
                        int seg = nums[index];
                        nums[index] = nums[k];
                        nums[k] = seg;
                    }
                    k--;
                }
            }

            index--;
        }

        return nums;
    }

    public static void main(String[] args) {
        int[] nums = new int[]{-3,4,3,2};

        int pivot = 2;

        int[] result = pivotArray(nums, pivot);

        for(int i : result){
            System.out.print(i + ", ");
        }
    }
}