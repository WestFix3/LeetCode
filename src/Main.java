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
        while(nums[index] >= pivot){ //Addig megyünk amig az összes nagyobb element végig nem értünk
            //System.out.println("nums[index] >= pivot :" + nums[index] + " >= " + pivot);
            if(nums[index] == pivot){ //Ha az elem pivot elem
                int k = index;
                while(nums[k] >= pivot){ //Végigmegyünk a nagyobb elemeken, ha találunk egyet ami nem a pivot akkor kicseréljük
                    //System.out.println("nums[k] > pivot: " + nums[k] + " >= " + pivot);
                    if(nums[k] != pivot){
                        //System.out.println("CSERE: " + nums[k] + " : " + pivot);
                        int seg = nums[index];
                        nums[index] = nums[k];
                        nums[k] = seg;
                        //System.out.print("nums: ");
//                        for(int s : nums){
//                            System.out.print(s + ", ");
//                        }
//                        System.out.println();
                        break;
                    }
                    k--;
                }
            }
            index--;
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