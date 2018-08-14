package citi.funcModule.Recommend;

import Jama.Matrix;
import citi.persist.mapper.*;
import citi.vo.Item;
import citi.vo.Merchant;
import citi.vo.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.ServletSecurityElement;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class RecommendService {
    //需要访问到用户的喜好、用户的访问记录、商品的基本信息
    //@Autowired
    //private PrefMapper prefMapper;
    //@Autowired
    //private RecordMapper recordMapper;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private MerchantMapper merchantMapper;
    @Autowired
    private ItemMapper itemMapper;
    @Autowired
    private VisitRecordMapper visitRecordMapper;
    @Autowired
    private UserMapper userMapper;

    /**
     * 初始化用户的偏好列表
     * @param prefList
     * @return true/false
     */
    public boolean initPref(String userID,ArrayList<String> prefList){
        boolean flag = true;
        for(int i=0;i<prefList.size();i++){
            /**
             * 接口一：PrefMapper-----addPref()
             * 参数一：userID,参数二：一个偏好（字符串形式）
             * 返回受影响的行数
             */
            //if(prefMapper.addPref(userID,prefList.get(i))!=1){
             //   flag = false;
             //   break;
            //}
        }
        return flag;
    }

    /**
     * 添加用户浏览记录
     * @param userID
     * @param itemID
     * @param time
     * @return true/false
     */
    public boolean addRecord(String userID, String itemID, Timestamp time){
        /**
         * 接口一：RecordMapper-----addRecord()
         * 参数一：userID,参数二：itemID,参数三：该浏览记录的时间戳
         * 返回受影响的行数
         */
      //if(recordMapper.addRecord(userID,itemID,time)!=1)
        //  return false;
      return true;
    }


    /**
     * 返回用户的推荐商品列表
     * @param userID
     * @param num
     * @return ArrayList<Item>
     */
    public ArrayList<Item> getRecommendedItems(String userID,int num){
        ArrayList<UserItemPoints> userItemPointsArrayList = getUserInterestToItems(userID);
        ArrayList<Item> items = new ArrayList<Item>();
        for(int i =0;i<num;i++){
            String itemID = userItemPointsArrayList.get(i).itemID;
            Item item = itemMapper.getItemByItemID(itemID);
            items.add(item);
        }
        return items;
    }
    /**
     * 返回用户对商品喜好评分
     * @param userID
     * @return ArrayList<Merchant>
     */
    public double getItemPoints(String userID, String itemID){
        /*


        double points = 0;
        // 目前制定的积分策略：购买一次得5分+浏览一次得1分+每符合一个喜好得5分
        // TODO:这里需要数据库写两个vo类还有对应mapper里的方法
        List<Order> orderList = orderMapper.getOrderByUserAndItemID(userID,"+010101010101");
        List<UserPref> prefList = prefMapper.getPrefByUserID(userID);
        List<Record> recordList = recordMapper.getRecordByUserID(userID);
        Item item = itemMapper.getItemByItemID(itemID);
        points += 5*orderList.size();
        points += recordList.size();
        for(int i=0;i<prefList.size();i++){
            if(prefList.get(i).getPref().equals(item.getItemType())){
                points+=5;
            }
        }
        return points;
        */
        return 0;
    }

    class ItemPoints{
        String itemID;
        double[] points;

        public ItemPoints(String itemID, double[] points) {
            this.itemID = itemID;
            this.points = points;
        }
    }
    class UserItemPoints implements Comparable<UserItemPoints>{
        String itemID;
        double points;

        public UserItemPoints(String itemID, double points) {
            this.itemID = itemID;
            this.points = points;
        }

        @Override
        public int compareTo(UserItemPoints o) {
            if((this.points - o.points)>=0){
                return -1;
            }
            else return 1;
        }
    }


    /*
    * 获取用户商品评分数组
    */
    public ArrayList<ItemPoints> getItemPointsArray(){
        ArrayList<ItemPoints> results = new ArrayList<ItemPoints>();
        // TODO:这里缺获取所用用户userID的方法
        List<String> userIDs = userMapper.getAllUserID();
        ArrayList<String> itemIDs = new ArrayList<String>();
        for(String itemID:itemIDs){
            ArrayList<Double> points = new ArrayList<Double>();
            for(String userID: userIDs){
                points.add(getMerchantPoints(userID,itemID));
            }
            double[] pointsArray = new double[points.size()];
            for(int i=0;i<points.size();i++){
                pointsArray[i]=points.get(i);
            }
            ItemPoints itemPoints = new ItemPoints(itemID,pointsArray);
            results.add(itemPoints);
        }
        try {
            ObjectOutputStream os = new ObjectOutputStream(
                    new FileOutputStream("ItemSimilarity.txt"));
            os.writeObject(results);// 将List列表写进文件
            os.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return results;
    }
    public ArrayList<ItemSimilarity> getItemSimilarities(){
        ArrayList<ItemSimilarity> results = new ArrayList<ItemSimilarity>();
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(new FileInputStream("ItemSimilarity.txt"));
            while (true) {
                ItemSimilarity itemSimilarity = (ItemSimilarity) ois.readObject();
                results.add(itemSimilarity);
            }
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
        return results;
    }

    public ArrayList<ItemSimilarity> updateItemSimilarities(){
        ArrayList<ItemSimilarity> results = new ArrayList<ItemSimilarity>();
        ArrayList<ItemPoints> itemPoints = getItemPointsArray();
        for(int i=0;i<itemPoints.size()-1;i++){
            for(int j=i;j<itemPoints.size();j++){
                double similarity = cosineSimilarity(itemPoints.get(i).points,itemPoints.get(j).points);
                String itemID1 = itemPoints.get(i).itemID;
                String itemID2 = itemPoints.get(j).itemID;
                results.add(new ItemSimilarity(itemID1,itemID2, similarity));
            }
        }
        try {
            ObjectOutputStream oos=new ObjectOutputStream(new FileOutputStream("ItemSimilarity.txt"));
            for(ItemSimilarity result:results){
                oos.writeObject(result);
            }
            oos.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return results;
    }
    /*
   * 返回用户对商品的评分
   */
    public ArrayList<UserItemPoints> getUserPointsToItems(String userID){
        ArrayList<UserItemPoints> results = new ArrayList<UserItemPoints>();
        ArrayList<String> itemIDs = new ArrayList<>();
        //TODO:itemMapper中返回所有ItemID

        //itemIDs.addAll(itemMapper,getItemIDs());
        ArrayList<Item> items = new ArrayList<Item>();
        for(String id:itemIDs){
            items.add(itemMapper.getItemByItemID(id));
        }
        for(Item item:items){
            double points = getItemPoints(userID,item.getItemID());
            results.add(new UserItemPoints(item.getItemID(),points));
        }
        return results;
    }
 /*
     * 获得最终用户对商品的喜好评分，并从大到小排序
     */

    public ArrayList<UserItemPoints> getUserInterestToItems(String userID){
        ItemSimilarity itemSimilarity1 = new ItemSimilarity("148055e4-af54-45e7-816e-5e6270b65bb5","148055e4-af54-45e7-816e-5e6270b65bb5",1);
        ItemSimilarity itemSimilarity2 = new ItemSimilarity("148055e4-af54-45e7-816e-5e6270b65bb5","1e739ddb-eabb-4ef1-bb2f-6f6df8d73e0d",0.8);
        ItemSimilarity itemSimilarity3 = new ItemSimilarity("148055e4-af54-45e7-816e-5e6270b65bb5","34bb3c20-43bb-4af2-814c-4a0c5d601af5",0.5);
        ItemSimilarity itemSimilarity4 = new ItemSimilarity("1e739ddb-eabb-4ef1-bb2f-6f6df8d73e0d","34bb3c20-43bb-4af2-814c-4a0c5d601af5",0.2);
        ItemSimilarity itemSimilarity5 = new ItemSimilarity("1e739ddb-eabb-4ef1-bb2f-6f6df8d73e0d","1e739ddb-eabb-4ef1-bb2f-6f6df8d73e0d",1);
        ItemSimilarity itemSimilarity6 = new ItemSimilarity("34bb3c20-43bb-4af2-814c-4a0c5d601af5","34bb3c20-43bb-4af2-814c-4a0c5d601af5",1);
        ArrayList<ItemSimilarity> similarities = new ArrayList<ItemSimilarity>();
        similarities.add(itemSimilarity1);
        similarities.add(itemSimilarity2);
        similarities.add(itemSimilarity3);
        similarities.add(itemSimilarity4);
        similarities.add(itemSimilarity5);
        similarities.add(itemSimilarity6);
        //ArrayList<ItemSimilarity> similarities = getItemSimilarities();
        UserItemPoints userItemPoints1 = new UserItemPoints("148055e4-af54-45e7-816e-5e6270b65bb5",10);
        UserItemPoints userItemPoints2 = new UserItemPoints("1e739ddb-eabb-4ef1-bb2f-6f6df8d73e0d",5);
        UserItemPoints userItemPoints3 = new UserItemPoints("34bb3c20-43bb-4af2-814c-4a0c5d601af5",2);
        ArrayList<UserItemPoints> userItemPointsArrayList = new ArrayList<UserItemPoints>();
        userItemPointsArrayList.add(userItemPoints1);
        userItemPointsArrayList.add(userItemPoints2);
        userItemPointsArrayList.add(userItemPoints3);
        //ArrayList<UserItemPoints> userItemPointsArrayList = getUserPointsToItems(userID);
        ArrayList<UserItemPoints> results = new ArrayList<UserItemPoints>();
        for(UserItemPoints userItemPoints:userItemPointsArrayList){
            double points = 0;
            for(ItemSimilarity similarity:similarities){
                if(similarity.itemID1.equals(userItemPoints.itemID)||similarity.itemID2.equals(userItemPoints.itemID)){
                    points+=userItemPoints.points*similarity.similarity;
                }
            }
            results.add(new UserItemPoints(userItemPoints.itemID,points));
        }
        Collections.sort(results);
        return results;
    }


    /**
     * 返回用户的推荐商户列表
     * @param userID
     * @return ArrayList<Merchant>
     */
    public ArrayList<Merchant> getRecommendedMerchants(String userID){
        ArrayList<UserMerchantPoints> userMerchantPointsArrayList = getUserInterestToMerchants(userID);
        ArrayList<Merchant> merchants = new ArrayList<Merchant>();
        for(UserMerchantPoints userMerchantPoints:userMerchantPointsArrayList){
            merchants.add(merchantMapper.selectByID(userMerchantPoints.merchantID));
        }
        return merchants;
    }

    /**
     * 返回用户对商户喜好评分
     * @param userID
     * @return ArrayList<Merchant>
     */
    public double getMerchantPoints(String userID, String merchantID){
        double points = 0;
        // 目前制定的积分策略：购买一件物品得5分+对应的浏览得1分+消费点数除以10
        // TODO: 这里缺用户浏览和用户积分消费的接口
        // 这里缺获取全部的商品吗？
        List<Item> items = itemMapper.getItemByMerchantID(merchantID,0,1);
        int visitTimes = 0;
        for(Item item:items){
            visitTimes+=visitRecordMapper.getVisitTimes(userID,item.getItemID());
        }
        List<Order> orderList = orderMapper.getOrderByUserID(userID,"+010101010101");
        points = 5*orderList.size()+visitTimes;
        return points;
    }

    class MerchantPoints{
        String merchantID;
        double[] points;

        public MerchantPoints(String merchantID, double[] points) {
            this.merchantID = merchantID;
            this.points = points;
        }
    }

    class UserMerchantPoints implements Comparable<UserMerchantPoints>{
        String merchantID;
        double points;

        public UserMerchantPoints(String merchantID, double points) {
            this.merchantID = merchantID;
            this.points = points;
        }

        @Override
        public int compareTo(UserMerchantPoints o) {
            if((this.points - o.points)>=0){
                return -1;
            }
            else return 1;
        }
    }

    class MerchantSimilarity{
        String merchantID1;
        String merchantID2;
        double similarity;

        public MerchantSimilarity(String merchantID1, String merchantID2, double similarity) {
            this.merchantID1 = merchantID1;
            this.merchantID2 = merchantID2;
            this.similarity = similarity;
        }
    }
    /*
     * 获取用户商户评分数组
     */
    public ArrayList<MerchantPoints> getMerchantPointsArray(){
        ArrayList<MerchantPoints> results = new ArrayList<MerchantPoints>();
        // TODO:这里缺获取所用用户userID的方法
        List<String> userIDs = userMapper.getAllUserID();
        ArrayList<String> merchantIDs = new ArrayList<String>();
        for(String merchantID:merchantIDs){
            ArrayList<Double> points = new ArrayList<Double>();
            for(String userID: userIDs){
                points.add(getMerchantPoints(userID,merchantID));
            }
            double[] pointsArray = new double[points.size()];
            for(int i=0;i<points.size();i++){
                pointsArray[i]=points.get(i);
            }
            MerchantPoints merchantPoints = new MerchantPoints(merchantID,pointsArray);
            results.add(merchantPoints);
        }
        return results;
    }

    
    public static double cosineSimilarity(double[] A, double[] B) {
        if (A.length != B.length) {
            return 2.0000;
        }
        if (A == null || B == null) {
            return 2.0000;
        }
        long fenzi = 0;
        for (int i = 0; i < A.length; i++) {
            fenzi += A[i] * B[i];
        }
        long left = 0;
        long right = 0;
        for (int i = 0; i < A.length; i++) {
            left += A[i] * A[i];
            right += B[i] * B[i];
        }
        if (left * right == 0) {
            return 2.0000;
        }
        double result = fenzi / Math.sqrt(left * right);
        DecimalFormat df = new DecimalFormat("#.####");
        return Double.parseDouble(df.format(result));
    }

    public ArrayList<MerchantSimilarity> getMerchantSimilarities() {
        ArrayList<MerchantSimilarity> results = new ArrayList<MerchantSimilarity>();
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(new FileInputStream("MerchantSimilarity.txt"));
            while (true) {
                    MerchantSimilarity merchantSimilarity = (MerchantSimilarity) ois.readObject();
                    results.add(merchantSimilarity);
            }
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
        return results;
    }
    /*
     * 更新商户相似度
     */
    public ArrayList<MerchantSimilarity> updateMerchantSimilarities(){
        ArrayList<MerchantSimilarity> results = new ArrayList<MerchantSimilarity>();
        ArrayList<MerchantPoints> merchantPoints = getMerchantPointsArray();
        for(int i=0;i<merchantPoints.size()-1;i++){
            for(int j=i;j<merchantPoints.size();j++){
                double similarity = cosineSimilarity(merchantPoints.get(i).points,merchantPoints.get(i).points);
                String merchantID1 = merchantPoints.get(i).merchantID;
                String merchantID2 = merchantPoints.get(j).merchantID;
                results.add(new MerchantSimilarity(merchantID1,merchantID2, similarity));
            }
        }
        try {
            ObjectOutputStream oos=new ObjectOutputStream(new FileOutputStream("MerchantSimilarity.txt"));
            for(MerchantSimilarity result:results){
                oos.writeObject(result);
            }
            oos.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return results;
    }

    /*
     * 返回用户对商户的评分
     */
    public ArrayList<UserMerchantPoints> getUserPointsToMerchants(String userID){
        ArrayList<UserMerchantPoints> results = new ArrayList<UserMerchantPoints>();
        // TODO:这里通过数据库获取所有商户ID
        ArrayList<Merchant> merchants = new ArrayList<Merchant>();
        for(Merchant merchant:merchants){
            double points = getMerchantPoints(userID,merchant.getMerchantID());
            results.add(new UserMerchantPoints(merchant.getMerchantID(),points));
        }
        return results;
    }

    /*
     * 获得最终用户对商品的喜好评分，并从大到小排序
     */

    public ArrayList<UserMerchantPoints> getUserInterestToMerchants(String userID){
        ArrayList<MerchantSimilarity> similarities = getMerchantSimilarities();
        ArrayList<UserMerchantPoints> userMerchantPointsArrayList = getUserPointsToMerchants(userID);
        ArrayList<UserMerchantPoints> results = new ArrayList<UserMerchantPoints>();
        for(UserMerchantPoints userMerchantPoints:userMerchantPointsArrayList){
            double points = 0;
            for(MerchantSimilarity similarity:similarities){
                if(similarity.merchantID1.equals(userMerchantPoints.merchantID)||similarity.merchantID2.equals(userMerchantPoints.merchantID)){
                    points+=userMerchantPoints.points*similarity.similarity;
                }
            }
            results.add(new UserMerchantPoints(userMerchantPoints.merchantID,points));
        }
        Collections.sort(results);
        return results;
    }




}