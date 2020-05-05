import java.util.*;
import java.util.HashSet;

/**
 * @author vitalchuan
 * @create 2020-04-25 21:54
 */
public class Main {
    private static class Node {
        /**
         * 0:空地
         * 1:仓库
         * 2:普通小区
         * 3:捐献小区
         */
        public int sortId;
        public int num;
        public int x;
        public int y;

        public Node() {

        }

        public Node(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public Node(int num, int x, int y) {
            this.num = num;
            this.x = x;
            this.y = y;
        }

        public int getSortId() {
            return sortId;
        }

        public void setSortId(int sortId) {
            this.sortId = sortId;
        }

        public int getNum() {
            return num;
        }

        public void setNum(int num) {
            this.num = num;
        }
    }

    private static class DeliveryMan {
        public int x = 0;
        public int y = 0;
        public int carryNum = 0;
        public boolean isDone = false;

        public DeliveryMan(int x, int y, int carryNum) {
            this.x = x;
            this.y = y;
            this.carryNum = carryNum;
        }

        public DeliveryMan() {
        }
    }

    private static Set<Node> lackNodes = new HashSet<>();
    private static Set<Node> donateNodes = new HashSet<>();

    private static Node repository;
    private static DeliveryMan deliveryMan = new DeliveryMan();
    private static int maxCarryNum = 100;

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        while (in.hasNextLine()) {
            String input = in.nextLine();

            String[] inputs = input.split(" ");
            if (inputs[0].equalsIgnoreCase("s")) {
                //初始化仓库位置
                int x = Integer.parseInt(inputs[1]);
                int y = Integer.parseInt(inputs[2]);
                repository = new Node(x, y);
                //初始化 配送员位置
                deliveryMan.x = x;
                deliveryMan.y = y;
                deliveryMan.carryNum = maxCarryNum;

            } else if (inputs[0].equalsIgnoreCase("r")) {
                int x = Integer.parseInt(inputs[1]);
                int y = Integer.parseInt(inputs[2]);
                int num = Integer.parseInt(inputs[3]);

                if (num > 0)
                    donateNodes.add(new Node(num, x, y));

                if (num < 0)
                    lackNodes.add(new Node(num, x, y));
            } else if (inputs[0].equalsIgnoreCase("G")) {
                process();
                String res = getStep(deliveryMan);
                process();
                System.out.println(res);
                if (lackNodes.isEmpty()) {
                    break;
                }
            }
        }
    }

    private static String getStep(DeliveryMan man) {
//        Node dest = findMinLackNumNode();
        Node dest = findNearestLackNode(man);
        Node nearestDonateNode = findNearestDonateNode(man);

        if (man.carryNum < - dest.num) {
            //携带货物不够，则回仓库或者去最近的捐赠小区或配送
            if (man.carryNum == maxCarryNum) {
                //已携带满，直接配送
                return gotoPoint(dest, man);
            } else {
                //没携带满，需要补货
                if (nearestDonateNode == null)
                    //没有捐赠小区，直接返回仓库
                    return gotoPoint(repository, man);
                else {
                    //有捐赠的小区，判断是否需要去小区取货
                    if (man.carryNum + nearestDonateNode.num >= dest.num) {
                        //最近捐赠小区的数量 足够
                        //比较路程
                        if ((dis(repository, man) + dis(repository, dest)) >
                                (dis(nearestDonateNode, man) + dis(nearestDonateNode, dest))) {
                            return gotoPoint(nearestDonateNode, man);
                        } else
                            return gotoPoint(repository, man);
                    } else return gotoPoint(repository, man);
                }
            }
        } else {
            //携带货物数量足够
            return gotoPoint(dest, man);
        }
    }

    private static Node findNearestDonateNode(DeliveryMan deliveryMan) {
        Node res = null;
        int min = 100;
        for (Node node : donateNodes)
            if (dis(node, deliveryMan) < min) {
                res = node;
                min = dis(node, deliveryMan);
            }
        return res;
    }

    public static void process() {
        //在仓库，则填满
        if (deliveryMan.x == repository.x && deliveryMan.y == repository.y) {
            deliveryMan.carryNum = maxCarryNum;
        }

        //在需求小区，则卸货
        Iterator<Node> it1 = lackNodes.iterator();
        while (it1.hasNext()) {
            Node currentNode = it1.next();
            if (deliveryMan.x == currentNode.x && deliveryMan.y == currentNode.y) {
                if (deliveryMan.carryNum + currentNode.num >= 0) {
                    deliveryMan.carryNum = deliveryMan.carryNum + currentNode.num;
                    currentNode.num = 0;
                    it1.remove();
                } else {
                    currentNode.num = deliveryMan.carryNum + currentNode.num;
                    deliveryMan.carryNum = 0;
                }
            }
        }

        //在捐献小区，则收货
        Iterator<Node> it2 = donateNodes.iterator();
        while (it2.hasNext()) {
            Node currentNode = it2.next();
            if (deliveryMan.x == currentNode.x && deliveryMan.y == currentNode.y) {
                if (deliveryMan.carryNum + currentNode.num <= maxCarryNum) {
                    deliveryMan.carryNum = deliveryMan.carryNum + currentNode.num;
                    currentNode.num = 0;
                    currentNode.sortId = 0;
                    it2.remove();
                } else {
                    currentNode.num = currentNode.num - maxCarryNum + deliveryMan.carryNum;
                    deliveryMan.carryNum = maxCarryNum;
                }
            }
        }
    }

    /**
     * 向目标点移动；若已到，则返回""
     *
     * @param dest
     * @param deliveryMan
     * @return
     */
    private static String gotoPoint(Node dest, DeliveryMan deliveryMan) {
        //先走水平，再走竖直
        if (dest.x > deliveryMan.x) {
            deliveryMan.x++;
            return "S";
        } else if (dest.x < deliveryMan.x) {
            deliveryMan.x--;
            return "N";
        } else if (dest.y > deliveryMan.y) {
            deliveryMan.y++;
            return "E";
        } else if (dest.y < deliveryMan.y) {
            deliveryMan.y--;
            return "W";
        } else return "";
    }

    /**
     * 寻找最小需求量的小区
     *
     * @return
     */
    private static Node findMinLackNumNode() {
        int max = - 200;
        Node res = new Node();
        for (Node node : lackNodes) {
            if (node.num >= max) {
                max = node.num;
                res = node;
            }
        }
        return res;
    }

    private static Node findNearestLackNode(DeliveryMan man) {
        Node res = null;
        int min = 100;
        Iterator<Node> it = lackNodes.iterator();
        while (it.hasNext()) {
            Node currentNode = it.next();
            if (dis(currentNode, man) <= min) {
                res = currentNode;
                min = dis(currentNode, man);
            }
        }
        return res;
    }

    private static int dis(Node node1, Node node2) {
        return Math.abs(node1.x - node2.x) + Math.abs(node1.y - node2.y);
    }

    private static int dis(Node node, DeliveryMan deliveryMan) {
        return Math.abs(node.x - deliveryMan.x) + Math.abs(node.y - deliveryMan.y);
    }
}
