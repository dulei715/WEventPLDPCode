package hnu.dll.utils.run;

public class RunUtils {
    public static void main(String[] args) {
        Runtime runtime = Runtime.getRuntime();

        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;

        System.out.println("最大内存: " + maxMemory*1.0/1024/1024 + " M");
        System.out.println("总内存: " + totalMemory*1.0/1024/1024 + " M");
        System.out.println("可用内存: " + freeMemory*1.0/1024/1024 + " M");
        System.out.println("已使用内存: " + usedMemory*1.0/1024/1024 + " M");
//        System.out.println("最大内存: " + maxMemory + " bytes");
//        System.out.println("总内存: " + totalMemory + " bytes");
//        System.out.println("可用内存: " + freeMemory + " bytes");
//        System.out.println("已使用内存: " + usedMemory + " bytes");
    }
}
