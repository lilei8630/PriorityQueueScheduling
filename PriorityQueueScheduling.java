import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

/**
 * Created by Lilei on 16/3/17.
 */

class Task {

    private int pid ;             //进程id
    private int servetime ;       //服务时间
    private float finishtime ;    //完成时间
    private int priority;         //优先级[0-5]

    public Task(){
    }

    public Task(int pid, int priority,int servetime){//构造方法只传入已知量
        this.pid=pid ;
        this.priority=priority ;
        this.servetime=servetime ;
    }
    /*
    当该任务被调用时,输出进程基本信息:进程id,进程优先级priority,服务时间servetime
    * */
    public void run(){
        System.out.println("[execute]--------"+"pid:"+pid+"---------"+"priority:"+priority+"--------"+"Need Service Time:"+servetime);
    }
    public int getServetime() {
        return servetime;
    }
    public void setServetime(int servetime) {
        this.servetime = servetime;
    }
    public void setPriority(int p){
        this.priority = p;
    }
    public int getPriority(){
        return priority;
    }
    public int getPid() {
        return pid;
    }
    public void setPid(int pid) {
        this.pid = pid;
    }

}
/*
* 6级优先级队列的实现
* */

class PriorityQueue {
    //从哪个优先级队列取出的任务,记录下这个队列号
    int curPriority = -1;
    //声明6级优先级队列
    Queue<Task>[]  priorityQueue = new Queue[6];
    //初始化优先级队列
    public PriorityQueue(){
        for(int i = 0;i<6;i++){
            priorityQueue[i] = new LinkedList<Task>();
        }
    }
    /*
    将作业按照其优先级加入到优先级队列
    * */
    public synchronized void add(Task task){
        int pri = task.getPriority();
        priorityQueue[pri].offer(task);
    }

    /*
    从优先级高到低遍历优先级队列,找到需要执行的Task
    * */
    public synchronized Task first(){
        Task newTask = null;
        for(int i =0;i < 6;i++){
            if(!priorityQueue[i].isEmpty()) {
                newTask = priorityQueue[i].peek();/*这里并没有将此Task pop出队列*/
                curPriority = i;
                break;
            }
        }
        return newTask;
    }
    /*
    如果任务需要服务的时间小于一个时间片的时间,那么任务执行完毕,从优先级队列中取出.
    * */
    public synchronized void remove(){
        priorityQueue[curPriority].poll();
    }
}

/*
PriorityQueueScheduling类,用来模拟CPU执行调度.
* */

public class PriorityQueueScheduling implements Runnable {

    private static PriorityQueue priorityQueue = new PriorityQueue();
    private static int timeSlot = 200;            //CPU时间片为200ms
    private int maxTaskTime = 1000;               //一个任务最大待服务时间为1000ms,用于随机生成任务服务时间
    private int priorityLeavel = 6;               //优先级队列层数,用于随机生成任务的优先级
    Random random = new Random();                 //生成随机数
    int pid = 0;                                  //初始进程号为0

    @Override
    /*
    此线程用来模拟向多级优先级队列提交任务(Task)
    * */
    public void run() {
        //测试样例提交10个任务
        int count = 0;
        while(count<10){

            int priority = random.nextInt(priorityLeavel); //随机生成0-5的优先级
            int serviceTime = random.nextInt(maxTaskTime); //随机生成0-1000的任务时间

            System.out.println("[submit]---------"+"pid:"+pid+"--------"+"priority:"+priority + "----------"+ " Need Service Time:"+serviceTime);
            //将task提交给优先级队列调度
            priorityQueue.add(new Task(pid, priority, serviceTime));
            pid++;
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            count++;
        }
    }

    public static  void main(String args[]) throws InterruptedException {
        PriorityQueueScheduling priorityQueueScheduling = new PriorityQueueScheduling();
        Thread taskProducer = new Thread(priorityQueueScheduling);
        //模拟生产者,用来生成任务并提交给多级优先级队列进行调度
        taskProducer.start();
        //模拟调度
        Task curTask = null;
        while(true){
            curTask = priorityQueue.first();
            if(curTask!=null){
                curTask.run();
                //如果需要服务时间大于一个时间片,则将需要服务时间减去一个时间片的时间,并放回优先级队列中
                if(curTask.getServetime()>timeSlot){
                    curTask.setServetime(curTask.getServetime()-timeSlot);
                }else{
                    //如果需要服务时间小于一个时间片的时间,那么将该任务执行完毕,并将该任务从优先级队列删除
                    priorityQueue.remove();
                }
                Thread.sleep(timeSlot);
            }
        }
    }
}
