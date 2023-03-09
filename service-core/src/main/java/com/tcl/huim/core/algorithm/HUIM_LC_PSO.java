package com.tcl.huim.core.algorithm;

import java.io.*;
import java.util.*;

/**
 * This is an implementation of the "Bio_HUIF_GA Algorithm" for High-Utility Itemsets Mining
 * as described in the paper : <br/><br/>
 * 
 * Wei Song, Chaomin Huang. Mining High Utility Itemsets Using Bio-Inspired Algorithms: 
 * A Diverse Optimal Value Framework. 
 * IEEE Access, 2018, 6(1): 19568-19582. 
 *  
 *
 * @author Wei Song, Chaomin Huang.
 */
public class HUIM_LC_PSO {
	double maxMemory = 0; // the maximum memory usage
	long startTimestamp = 0; // the time the algorithm started
	long endTimestamp = 0; // the time the algorithm terminated
	final int pop_size = 100;//the size of population
	final int max_iter = 6000;// maximum iterations
	int transactionCount=0;//the total num of transactions

	Map<Integer, Integer> mapItemToTWU;
	Map<Integer, Integer> mapItemToTWU0;
	List<Integer> twuPattern;// the items which has twu value more than minUtil

	BufferedWriter writer = null; // writer to write the output file

	//=============Begin==============//
	private int maxLength=Integer.MAX_VALUE;
	private int minLength=1;
	/** the size of a buffer for storing a transaction */
	final int TRANSACTION_BUFFER = 3000;
	/** a buffer for storing utility values */
	private int[] utilitiesBuffer = new int[TRANSACTION_BUFFER];
	/** a buffer for storing pairs of the form (item, utility), in a transaction */
	private boolean flag;
	private boolean flagmax;
	//=============END================//

	// this class represent an item and its utility in a transaction
	class Pair {
		int item = 0;
		int utility = 0;
	}
	// this class represent the particles
	class Particle {
		BitSet X;// the particle
		int fitness;// fitness value of particle

		 Particle() {
			X = new BitSet(twuPattern.size());
			fitness = 0;
		}

		 Particle(int length) {
			X = new BitSet(length);
			fitness = 0;
		}

		 void copyParticle(Particle particle1){
			this.X=(BitSet)particle1.X.clone();
			this.fitness = particle1.fitness;
		}

		 void calculateFitness(int k, List<Integer> templist) {
			if (k == 0)
				return;

			int i, p, q, temp,m;

			int sum, fitness = 0;
			for (m = 0; m < templist.size(); m++) {
				p=templist.get(m).intValue();		//intValue()转为int型，这里就是将
				i = 0;
				q = 0;
				temp = 0;
				sum = 0;
				while (q < database.get(p).size()
						&& i < this.X.length()){
					if(this.X.get(i)){
						if (database.get(p).get(q).item == twuPattern.get(i)){
							sum = sum + database.get(p).get(q).utility;
							++i;
							++q;
							++temp;
						}else{
							++q;
						}
					} else{
						++i;
					}
				}
				if (temp == k){
					fitness = fitness + sum;
				}
			}
			this.fitness = fitness;
		}
	}

	class HUI {
		String itemset;
		int fitness;

		public HUI(String itemset, int fitness) {
			super();
			this.itemset = itemset;
			this.fitness = fitness;
		}

	}

	//use Item to create bitmap
	class Item{
		int item;
		BitSet TIDS;

		public Item(){
			TIDS =  new BitSet(database.size());
		}

		public Item(int item){
			TIDS=new BitSet(database.size());
			this.item=item;
		}
	}

	Particle gBest;// the gBest particle in populations
	List<Particle> pBest = new ArrayList<Particle>();// each pBest particle in populations
	List<Particle> population = new ArrayList<Particle>();// populations
	List<HUI> huiSets = new ArrayList<HUI>();// the set of HUIs
	List<Double> percentage = new ArrayList<Double>();// the portation of twu value of each
														// 1-HTWUIs in sum of twu value
	// Create a list to store database
	List<List<Pair>> database = new ArrayList<List<Pair>>();

	List<Item> Items;//bitmap database representation

	List<Particle> huiBA = new ArrayList<Particle>();//store hui Particles

	List<Double> percentHUIBA;
	/**
	 * Default constructor
	 */
	public HUIM_LC_PSO() {
	}
	/**
	 * Run the algorithm
	 * 
	 * @param input
	 *            the input file path
	 * @param output
	 *            the output file path
	 * @param minUtility
	 *            the minimum utility threshold
	 * @throws IOException
	 *             exception if error while writing the file
	 */
	public void runAlgorithm(String input, String output, int minUtility,int minLength,int maxLength)
			throws IOException {
		// reset maximum
		maxMemory = 0;

		startTimestamp = System.currentTimeMillis();

		this.minLength=minLength;
		this.maxLength=maxLength;

		writer = new BufferedWriter(new FileWriter(output));

		// We create a map to store the TWU of each item
		mapItemToTWU = new HashMap<Integer, Integer>();
		mapItemToTWU0 = new HashMap<Integer, Integer>();

		// We scan the database a first time to calculate the TWU of each item.
		BufferedReader myInput = null;
		String thisLine;
		try {
			// prepare the object for reading the file
			myInput = new BufferedReader(new InputStreamReader(
					new FileInputStream(new File(input))));
			// for each line (transaction) until the end of file
			while ((thisLine = myInput.readLine()) != null) {
				// if the line is a comment, is empty or is a
				// kind of metadata
				if (thisLine.isEmpty() == true || thisLine.charAt(0) == '#'
						|| thisLine.charAt(0) == '%'
						|| thisLine.charAt(0) == '@') {
					continue;
				}
//				这里事务数量++，有点不一样
				++transactionCount;//�����������ݿ���������

				// split the transaction according to the : separator
				String split[] = thisLine.split(":");
				// the first part is the list of items
				String items[] = split[0].split(" ");
				// the second part is the transaction utility

				/// =================== BEGIN RTWU ===================
				//用于计算maxLength长度的TWU
				//  COPY THE UTILITIES OF ITEMS IN A TEMPORARY BUFFER
				int z =0;
				// for each utility value
				for(String utilityValue : split[2].split(" ")){
					// convert it to int
					utilitiesBuffer[z++] = Integer.parseInt(utilityValue);
				}
				// SORT THE UTILITIES BY ASCENDING ORDER
				Arrays.sort(utilitiesBuffer,0, items.length);

				// Calculate the revised TWU by making the sum of the first items in  the sorted
				// transaction
				int reducedTWU = 0;  //这一行maxLength的TWU
				int startIndex = (items.length - 1) - maxLength;
				if(startIndex < 0){
					startIndex = 0;
				}
				// calculate the sum
				for(int i = items.length - 1; i >= startIndex; i--){
					reducedTWU += utilitiesBuffer[i];
				}
				/// =================== END RTWU ==================)
				// for each item, we add the transaction utility to its TWU
				for (int i = 0; i < items.length; i++) {
					// convert item to integer
					Integer item = Integer.parseInt(items[i]);
					// get the current TWU of that item
					Integer twu = mapItemToTWU.get(item);
					Integer twu0 = mapItemToTWU0.get(item);
					// add the utility of the item in the current transaction to
					// its twu
					twu = (twu == null) ? reducedTWU : twu + reducedTWU;
					twu0 = (twu0 == null) ? reducedTWU : twu0 + reducedTWU;
					mapItemToTWU.put(item, twu);
					mapItemToTWU0.put(item, twu0);
				}
			}
		} catch (Exception e) {
			// catches exception if error while reading the input file
			e.printStackTrace();
		} finally {
			if (myInput != null) {
				myInput.close();
			}
		}
		// SECOND DATABASE SCAN TO CONSTRUCT THE DATABASE
		// OF 1-ITEMSETS HAVING TWU >= minutil (promising items)
		try {
			// prepare object for reading the file
			myInput = new BufferedReader(new InputStreamReader(
					new FileInputStream(new File(input))));
			// variable to count the number of transaction
			// for each line (transaction) until the end of file
			while ((thisLine = myInput.readLine()) != null) {
				// if the line is a comment, is empty or is a
				// kind of metadata
				if (thisLine.isEmpty() == true || thisLine.charAt(0) == '#'
						|| thisLine.charAt(0) == '%'
						|| thisLine.charAt(0) == '@') {
					continue;
				}

				// split the line according to the separator
				String split[] = thisLine.split(":");
				// get the list of items
				String items[] = split[0].split(" ");
				// get the list of utility values corresponding to each item
				// for that transaction
				String utilityValues[] = split[2].split(" ");

				// Create a list to store items and its utility
				List<Pair> revisedTransaction = new ArrayList<Pair>();
				// Create a list to store items
				List<Integer> pattern = new ArrayList<Integer>();
				// for each item
				for (int i = 0; i < items.length; i++) {
					// / convert values to integers
					Pair pair = new Pair();
					pair.item = Integer.parseInt(items[i]);
					pair.utility = Integer.parseInt(utilityValues[i]);
					// if the item has enough utility
					if (mapItemToTWU.get(pair.item) >= minUtility) {
						// add it
						revisedTransaction.add(pair);
						pattern.add(pair.item);
					}else{
						mapItemToTWU0.remove(pair.item); //记录最后修改的pair
					}
				}
				// Copy the transaction into database but
				// without items with TWU < minutility
				database.add(revisedTransaction);    //记录满足条件的pair
			}
		} catch (Exception e) {
			// to catch error while reading the input file
			e.printStackTrace();
		} finally {
			if (myInput != null) {
				myInput.close();
			}
		}

		twuPattern = new ArrayList<Integer>(mapItemToTWU0.keySet());  //keySet()是获取键名
		Collections.sort(twuPattern);

		System.out.println("twuPattern:"+twuPattern.size());    //输出总的满足TWU模式数量
		System.out.println(twuPattern);                         //keySet()获取键名，所以输出[1, 2, 3, 4, 5]
		
		Items = new ArrayList<Item>();
		
		for(Integer tempitem:twuPattern){
			Items.add(new Item(tempitem.intValue()));      //.intValue()转为int
		}
		////scan database to create bitmap
		for(int i=0;i<database.size();++i){
			for(int j=0;j<Items.size();++j){
				for(int k=0;k<database.get(i).size();++k){
					if(Items.get(j).item==database.get(i).get(k).item){
						Items.get(j).TIDS.set(i);
					}
				}
			}
		}
		//init pBest
		for(int i=0;i<pop_size;++i){
			pBest.add(new Particle(twuPattern.size()));
		}
		//global Best
		gBest = new Particle(twuPattern.size());
		
		// check the memory usage
		checkMemory();
		// Mine the database recursively
		if (twuPattern.size() > 0) {
			// initial population
			pop_Init(minUtility,minLength,maxLength);
			for (int i = 0; i < max_iter; i++) {
				
				// update population and HUIset
				next_Gen_PA(minUtility);
				if(huiBA.size() != 0){
					percentHUIBA = roulettePercentHUIBA();
					int num = rouletteSelectHUIBA(percentHUIBA);
					gBest.copyParticle(huiBA.get(num));
				}
				if(i%500==0){
//					System.out.println(i + "-update end. HUIs No. is " + huiSets.size());
					System.out.print(huiSets.size()+",");
				}
			}
		}

		writeOut();
		// check the memory usage again and close the file.
		checkMemory();
		// close output file
		writer.close();
		// record end time
		endTimestamp = System.currentTimeMillis();
	}
	/**
	 * This is the method to initial population
	 * 
	 * @param minUtility
	 *            minimum utility threshold
	 */
	private void pop_Init(int minUtility,int minLength,int maxLength)//
	{
		int i, j, k, temp;
		
		List<Integer> transList;//��������itemset���ڵ�����ļ���
		// initial percentage according to the twu value of 1-HTWUIs
		percentage = roulettePercent();
		
//		System.out.println(percentage);   //输出初始化种群百分比

		int lur=twuPattern.size()-minLength+1;

		for (i = 0; i < pop_size; i++) {
			// initial particles
			Particle tempParticle = new Particle(twuPattern.size());
			j = 0;
			// k is the count of 1 in particle
			Random r = new Random();
			k = (int) (r.nextInt(lur)+minLength);    //最小长度到最大长度

			while (j < k) {
				// roulette select the position of 1 in population
				temp = rouletteSelect(percentage);
				if (!tempParticle.X.get(temp)) {   //如果没有改为1，则执行；防止产生重复的1
					j++;
					tempParticle.X.set(temp);
				}
			}

			//������Ӧ��
			transList=new ArrayList<Integer>();//�洢itemset���ڵ�����ļ���
			pev_Check(tempParticle,transList);     //剪枝
			tempParticle.calculateFitness(k, transList);
			
			// insert particle into population
			population.add(i, tempParticle);
			// initial pBest
			//pBest.add(i, population.get(i));
			pBest.get(i).copyParticle(tempParticle);
			// update huiSets
			if (population.get(i).fitness >= minUtility
					&& population.get(i).X.cardinality()<=maxLength && population.get(i).X.cardinality()>=minLength) {

				insert(population.get(i));
				addHuiBA(population.get(i));

			}
			// update gBest
			if (i == 0) {
				//gBest = pBest.get(i);
				gBest.copyParticle(pBest.get(i));
			} else {
				if (pBest.get(i).fitness > gBest.fitness) {
					gBest.copyParticle(pBest.get(i));
				}
			}
		}
	}
	/**
	 * Methos to update particle, pBest and gBest
	 * 
	 * @param minUtility
	 */
	private void next_Gen_PA(int minUtility) {
		int i, k,num,changeBit;
		
		List<Integer> disList;
		List<Integer> transList;

		for (i = 0; i < pop_size; i++) {
			//��i������������ʷ���Ž������������������λ���б��Ա������������Լ��õķ�������ƶ�
			disList = bitDiff(pBest.get(i),population.get(i));
			
			//ʹ�������������ӿ�£
//			改变位数，0-1，1-0，如何限制1的个数在范围内
//
			num = (int)(disList.size()*Math.random()) + 1;
			if(disList.size()>0){
				for(int m = 0; m < num; ++m){
					changeBit = (int)(disList.size()*Math.random());
					//System.out.println(changeBit);
					if(population.get(i).X.get(disList.get(changeBit))){
						population.get(i).X.clear(disList.get(changeBit));
					}else{
						population.get(i).X.set(disList.get(changeBit));
					}
				}
			}
			//��i��������ȫ�����Ž������������������λ���б��Ա���������ȫ�����Ž����ƶ�
			disList = bitDiff(gBest,population.get(i));

			//ʹ������������ʷ���ſ�£
			num = (int)(disList.size()*Math.random()) + 1;
			if(disList.size()>0){
				for(int m = 0; m < num; ++m){
					changeBit = (int)(disList.size()*Math.random());
					if(population.get(i).X.get(disList.get(changeBit))){
						population.get(i).X.clear(disList.get(changeBit));
					}else{
						population.get(i).X.set(disList.get(changeBit));
					}
				}
			}
			//�����еĲ��������ƶ��������ٶ�
			for(int m = 0; m < 1;++m){
				changeBit = (int)(twuPattern.size()*Math.random());
				if(population.get(i).X.get(changeBit)){
					population.get(i).X.clear(changeBit);
				}else{
					population.get(i).X.set(changeBit);
				}
			}

			k=population.get(i).X.cardinality();
			transList=new ArrayList<Integer>();
			pev_Check(population.get(i),transList);
			
			population.get(i).calculateFitness(k, transList);

			if(flag && population.get(i).fitness>=minUtility){
				System.out.println();
				System.out.println("增加 "+"X="+population.get(i).X+" util="+population.get(i).fitness);
			}
			if(flagmax && population.get(i).fitness>=minUtility){
				System.out.println();
				System.out.println("减少 "+"X="+population.get(i).X+" util="+population.get(i).fitness);
			}
			// update pBest & gBest
			if (population.get(i).fitness > pBest.get(i).fitness) {
				//pBest.set(i, population.get(i));
				pBest.get(i).copyParticle(population.get(i));
				if (pBest.get(i).fitness > gBest.fitness) {
					gBest.copyParticle(pBest.get(i));
				}
			}
			// update huiSets
			if (population.get(i).fitness >= minUtility
					&& population.get(i).X.cardinality()<=maxLength && population.get(i).X.cardinality()>=minLength) {

				insert(population.get(i));
				addHuiBA(population.get(i));
			}
		}
	}

	/**
	 *check itemset is promising or unpromising
	 * @param tempBAIndividual
	 * @param list
	 * @return
	 */
	 boolean pev_Check(Particle tempBAIndividual,List<Integer> list){
		List<Integer> templist=new ArrayList<Integer>();		//记录1的位置
		 List<Integer> templist0=new ArrayList<Integer>();		//记录1的位置，后续改动时用
		 List<Integer> zerolist = new ArrayList<Integer>();		//记录0的位置，
		 flag=false;
		 flagmax=false;
		//int temp=0;
		for(int i=0;i<tempBAIndividual.X.length();++i){
			if(tempBAIndividual.X.get(i)){
				templist.add(i);
				templist0.add(i);
			}else {
				zerolist.add(i);
			}
		}
		if(templist.size()==0){
			return false;
		}
		BitSet tempBitSet = new BitSet(transactionCount);
		BitSet midBitSet = new BitSet(transactionCount);
		tempBitSet = (BitSet)Items.get(templist.get(0).intValue()).TIDS.clone();
		midBitSet = (BitSet)tempBitSet.clone();//��¼�м���
//		 System.out.println();
//		 System.out.println("***************************Begin Pruning*********************************");
//		 System.out.println("待Pruning："+tempBAIndividual.X);
		//item��λͼ���������������ʹ��itemset�������item��������itemset��ȥ����item
		for(int i=1;i<templist.size();++i){
			tempBitSet.and(Items.get(templist.get(i).intValue()).TIDS);
			if(tempBitSet.cardinality() != 0){
				midBitSet = (BitSet)tempBitSet.clone();
			}else{
				tempBitSet = (BitSet)midBitSet.clone();
				tempBAIndividual.X.clear(templist.get(i).intValue());
				//使用遍历删除
				for (int j = 0; j <templist0.size() ; j++) {
					if((templist.get(i)).equals(templist0.get(j))){
						templist0.remove(templist.get(i));
						j--;
					}

				}
//				zerolist.add(templist.get(i));    //不用记录这个0，删除的0，没必要记录
			}
		}

//		 System.out.println("简单Pruning="+tempBAIndividual.X);
//		 System.out.println("======开始长度检查========");
//
//		 System.out.println("Pruning后所在行:"+tempBitSet);

		//========================BEGIN PEV_Check==============//
		 int num,changeBit=0;
		 if (tempBAIndividual.X.cardinality()>maxLength) {
			 /**
			  * 主要思想：去掉1的个数，使其满足条件，并且不用做判断
			  */
			 num = (int) (((maxLength - minLength + 1) * Math.random()) + (tempBAIndividual.X.cardinality() - maxLength));
			 for (int m = 0; m < num; m++) {
				 changeBit = (int) (templist0.size() * Math.random());

				 //此时，真正的1的位置是templist0，所以，这里也不用做判断
				 tempBAIndividual.X.clear(templist0.get(changeBit));
				 templist0.remove(templist0.get(changeBit));        //同时删除这个位置的数，防止再次访问到该位置，使得0-->1，不符合题意
			 }

			 //注意：这里要更新RV，以便重新定义TIDs，用于f(x)的计算
			 tempBitSet = (BitSet) Items.get(templist0.get(0).intValue()).TIDS.clone();  //tempBitSet克隆第一个，get(0),相当于第一个1的位置其对应的item
			 for (int i = 1; i < templist0.size(); ++i) {   //这里size()指的是1的个数
				 tempBitSet.and(Items.get(templist0.get(i).intValue()).TIDS);    //与后面存在的1依次相与
				 //因为这个肯定时存在的，故不用判断是否为0
			 }
			 flagmax=true;
//		 }

		 }else if(tempBAIndividual.X.cardinality()<minLength){
			 /**
			  * 写一个递归函数，依次增加1的位置，直到满足最小条件
			  * 	可继续探索，要是没有解，则返回上一次满足条件的解
			  *
			  */
			//编写一个方法，添加num个1
//			 System.out.println("Length小了："+tempBAIndividual.X);
//			 System.out.println("templist0="+templist0);
//			 flag=false;


			 num=(int)((maxLength-minLength+1)*Math.random()+(minLength-tempBAIndividual.X.cardinality()));
// 			 System.out.println("*****开始递归*****");
//			 System.out.println("增加前的粒子："+tempBAIndividual.X);
//			 System.out.println("num"+num);

//			 outParticle=new Particle();//这一部很重要。
//			 templist0Copy=new ArrayList<Integer>();
//第一种递归BEGIN------------------------------------------
//			 System.out.println("-----------递归开始---------------------------------------------------");
//			addNum1(0,num,tempBAIndividual,zerolist,templist0,outParticle,templist0Copy);//这里不会输出想要的解
//			 System.out.println("-----------递归结束--------");
//
//			 tempBAIndividual.copyParticle(outParticle);
//			 templist0=new ArrayList<Integer>();
//			 templist0.addAll(templist0Copy);
//			 Collections.sort(templist0);
//			 System.out.println("增加后的粒子："+tempBAIndividual.X);
//			 System.out.println("templisto="+templist0);
//			 System.out.println("====================");
//第一种递归END------------------------------------------
/*******************************************************************************/
//第二种递归BEGIN-----------------------------------------

//			tempBAIndividual.copyParticle(addNum2(0,num,tempBAIndividual,zerolist,templist0));
			 addNum2(0,num,tempBAIndividual,zerolist,templist0);
			Collections.sort(templist0);
//第而种递归END------------------------------------------
//			 System.out.println();
//			 System.out.println("由小变大--->结果Particle="+tempBAIndividual.X);
//			 System.out.println("结果templist="+templist0);
//			 System.out.println("*****结束递归*****");

			 //同样更新RV
			 tempBitSet = new BitSet();
			 tempBitSet = (BitSet)Items.get(templist0.get(0).intValue()).TIDS.clone();
//			 System.out.println("之前:"+tempBitSet.cardinality());
			 for(int i=1;i<templist0.size();i++){
				 tempBitSet.and(Items.get(templist0.get(i).intValue()).TIDS);
				 if(tempBitSet.cardinality()==0){
					 System.out.println("递归异常");
					 System.exit(0);
				 }
			 }
			 flag=true;
//			 System.out.println("之后:"+tempBitSet.cardinality());
//			 System.out.println("Bitset"+tempBitSet);
		 }
 		//========================END PEV_Check===============//
//		 System.out.println("======结束长度检查========");
		if(tempBitSet.cardinality()==0){
			return false;
		}else{
			for(int m=0;m<tempBitSet.length();++m){
				if(tempBitSet.get(m)){
					list.add(m);
				}	
			}
			return true;	
		}
	}
	/**
	 * xor(itemset1,itemset2)
	 * @param gBest
	 * @param tempBAIndividual
	 * @return
	 */
	private List<Integer> bitDiff(Particle gBest,Particle tempBAIndividual){
		List<Integer> list = new ArrayList<Integer>();
		BitSet tmpBitSet = (BitSet)gBest.X.clone();
		tmpBitSet.xor(tempBAIndividual.X);
		for(int i = 0; i < tmpBitSet.length(); ++i){
			if(tmpBitSet.get(i)){
				list.add(i);
			}
		}
		return list;	
	}
	/**
	 * add hui Particles to HuiBA
	 * @param tempBAIndividual
	 */
	private void addHuiBA(Particle tempBAIndividual){
		Particle tmpBAIndividual = new Particle();
		tmpBAIndividual.copyParticle(tempBAIndividual);
		BitSet tmpBitSet;
		if(huiBA.size() != 0){
			for(int i = 0; i < huiBA.size(); ++i){
				tmpBitSet = (BitSet)(tmpBAIndividual.X.clone());
				tmpBitSet.xor(huiBA.get(i).X);
				if(tmpBitSet.cardinality() == 0){
					return ;
				}
			}
		}	
		huiBA.add(tmpBAIndividual);
	}
	//Method to initial percentHUIBA
	private List<Double> roulettePercentHUIBA() {
		double sum = 0;
		double tempsum = 0;
		double percent = 0.0;
		List<Double> percentHUIBA = new ArrayList<Double>();
		for(int i = 0; i < huiBA.size(); ++i){
			sum += huiBA.get(i).fitness;
		}
		for(int i = 0; i < huiBA.size();++i){
			tempsum += huiBA.get(i).fitness;
			percent = tempsum/sum;
			percentHUIBA.add(percent);
		}
		return percentHUIBA;
		
	}
	//Method to roulette select Particles to replace Particle of population
	private int rouletteSelectHUIBA(List<Double> percentage) {
		int i,temp=0;
		double randNum;
		randNum = Math.random();
		for (i = 0; i < percentage.size(); i++) {
			if (i == 0) {
				if ((randNum >= 0) && (randNum <= percentage.get(0))) {
					temp = 0;
					break;
				}
			} else if ((randNum > percentage.get(i - 1))
					&& (randNum <= percentage.get(i))) {
				temp = i;
				break;
			}
		}
		return temp;
	}
	/**
	 * Method to inseret tempParticle to huiSets
	 * 
	 * @param tempParticle
	 *            the particle to be inserted
	 */
	private void insert(Particle tempParticle) {
		int i;
		StringBuilder temp = new StringBuilder();
		for (i = 0; i < twuPattern.size(); i++) {
			if (tempParticle.X.get(i)) {
				temp.append(twuPattern.get(i));
				temp.append(' ');
			}
		}
		// ========== ADDED BY PHILIPPE 2019-01 otherwise some empty itemsets may be output
		if(temp.length() == 0){
			return;
		}
		//========================================================================
		
		// huiSets is null
		if (huiSets.size() == 0) {
			huiSets.add(new HUI(temp.toString(), tempParticle.fitness));
		} else {
			// huiSets is not null, judge whether exist an itemset in huiSets
			// same with tempParticle
			for (i = 0; i < huiSets.size(); i++) {
				if (temp.toString().equals(huiSets.get(i).itemset)) {
					break;
				}
			}
			// if not exist same itemset in huiSets with tempParticle,insert it
			// into huiSets
			if (i == huiSets.size())
				huiSets.add(new HUI(temp.toString(), tempParticle.fitness));
		}
	}

	/**
	 * Method to initial percentage
	 * 
	 * @return percentage
	 */
	private List<Double> roulettePercent() {
		int i;
		double sum = 0, tempSum = 0;
		double tempPercent;

		// calculate the sum of twu value of each 1-HTWUIs
		for (i = 0; i < twuPattern.size(); i++) {
			sum = sum + mapItemToTWU.get(twuPattern.get(i));
		}
		// calculate the portation of twu value of each item in sum
		for (i = 0; i < twuPattern.size(); i++) {
			tempSum = tempSum + mapItemToTWU.get(twuPattern.get(i));
			tempPercent = tempSum / (sum + 0.0);
			percentage.add(tempPercent);
		}
		return percentage;
	}

	/**
	 * Method to ensure the posotion of 1 in particle use roulette selection
	 * 
	 * @param percentage
	 *            the portation of twu value of each 1-HTWUIs in sum of twu
	 *            value
	 * @return the position of 1
	 */
	private int rouletteSelect(List<Double> percentage) {
		int i, temp = 0;
		double randNum;
		randNum = Math.random();
		for (i = 0; i < percentage.size(); i++) {
			if (i == 0) {
				if ((randNum >= 0) && (randNum <= percentage.get(0))) {
					temp = 0;
					break;
				}
			} else if ((randNum > percentage.get(i - 1))
					&& (randNum <= percentage.get(i))) {
				temp = i;
				break;
			}
		}
		return temp;
	}
//========================BEGIN_minLength=============//

	//判断粒子是否符合条件
	private boolean judge(Particle temparticle,List<Integer> templist,int temp){
		boolean falg =true;

		//利用中间变量来确定
		Particle judepaticle = new Particle();
		List<Integer> judelist = new ArrayList<Integer>();
		judepaticle.copyParticle(temparticle);
		judelist.addAll(templist);

		//中间变量赋值
		judepaticle.X.set(temp);
		judelist.add(temp);


		BitSet tempBitSet = new BitSet(transactionCount);
		tempBitSet = (BitSet)Items.get(judelist.get(0).intValue()).TIDS.clone();

		for(int i=1;i<judelist.size();i++){
			tempBitSet.and(Items.get(judelist.get(i).intValue()).TIDS);
			if(tempBitSet.cardinality() == 0){
				falg=false;
				break;
			}
		}
		return falg;
	}
	//编写一个方法，增加num个1
	/**
	 *
	 * @param n             递归参数，增加1的个数
	 * @param num           改变的元素个数
	 * @param tempartilce   输入粒子
	 * @param zerolist      输入0的列表
	 * @param templist      输入1的列表
	 */


	private void addNum2(int n,int num,Particle tempartilce,List<Integer> zerolist,List<Integer> templist) {  //代n=0,从0开始
		if (n == num || zerolist.size() == 0) {    //n=num时，表示1的位置已经ok
//			System.out.println("temparticle=" + tempartilce.X + "------------------------------------");
//			System.out.println("templist=" + templist);
			return ;
		}

		//依次加入1，并判断是否合理
		int changeBit;
		for (int i = 0; i < zerolist.size(); i++) {
			//随机改变一位0
			changeBit = (int) (zerolist.size() * Math.random());
			Integer temp = zerolist.get(changeBit);//记录中间变量，防止删去了找不到

			//现在外面删除,
			// 删除使用正序遍历,对当前位置清0,防止出现脏数据
			for (int zero = 0; zero < zerolist.size(); zero++) {
				if (temp.equals(zerolist.get(zero))) {
					zerolist.remove(zero);
					zero--;
				}
			}
			//判断这个1是否合理
			if (judge(tempartilce, templist,temp)) {
				//如果添加的这个1，满足条件就继续递归
				tempartilce.X.set(temp);                //把这个位置设为1
				templist.add(temp);

				addNum2(n + 1, num, tempartilce, zerolist, templist);
				return ;
			}

		}
//		return tempartilce;
	}



//========================END_minLength===============//

	/**
	 * Method to write a high utility itemset to the output file.
	 * 
	 * @throws IOException
	 */
	private void writeOut() throws IOException {
		// Create a string buffer
		StringBuilder buffer = new StringBuilder();
		// append the prefix
		for (int i = 0; i < huiSets.size(); i++) {
			buffer.append(huiSets.get(i).itemset);
			// append the utility value
			buffer.append("#UTIL: ");
			buffer.append(huiSets.get(i).fitness);
			if(i != huiSets.size() -1){
				buffer.append(System.lineSeparator());
			}
		}
		// write to file
		writer.write(buffer.toString());
	}
	

	/**
	 * Method to check the memory usage and keep the maximum memory usage.
	 */
	private void checkMemory() {
		// get the current memory usage
		double currentMemory = (Runtime.getRuntime().totalMemory() - Runtime
				.getRuntime().freeMemory()) / 1024d / 1024d;
		// if higher than the maximum until now
		if (currentMemory > maxMemory) {
			// replace the maximum with the current memory usage
			maxMemory = currentMemory;
		}
	}

	/**
	 * Print statistics about the latest execution to System.out.
	 */
	public void printStats() {
		System.out.println();
		System.out
				.println("=============  HUIF-PSO-PEV_C ALGORITHM v.2.11 - STATS =============");
		System.out.println(" Total time ~ " + (endTimestamp - startTimestamp)
				+ " ms");
		System.out.println(" Memory ~ " + maxMemory + " MB");
		System.out.println(" High-utility itemsets count : " + huiSets.size());
		System.out
				.println("===================================================");
	}
}
