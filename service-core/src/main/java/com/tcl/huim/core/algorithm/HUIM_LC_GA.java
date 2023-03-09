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

public class HUIM_LC_GA {
	double maxMemory = 0; // the maximum memory usage
	long startTimestamp = 0; // the time the algorithm started
	long endTimestamp = 0; // the time the algorithm terminated
	int transactionCount=0;//the total num of transactions
	final int pop_size = 100;//the size of population
	final int max_iter = 6000;// maximum iterations

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
	//=============END================//
	// this class represent an item and its utility in a transaction
	class Pair {
		int item = 0;
		int utility = 0;
	}

	// this class represent the chromosome
	class ChroNode implements Comparable {
		BitSet chromosome;// the chromosome
		int fitness;// fitness value of chromosome
		double rfitness;//select chromosomes to crossover
		int rank;// the rank of chromosome's fitness in population

		public ChroNode() {
			chromosome = new BitSet();
		}

		public ChroNode(int length) {
			chromosome = new BitSet(length);
		}
		//deepcopy ChroNode
		public void deepcopy(ChroNode tempChroNode){
			chromosome = (BitSet)tempChroNode.chromosome.clone();
			fitness = tempChroNode.fitness;
			rfitness = tempChroNode.rfitness;
			rank = tempChroNode.rank;
		}

		//calculate fitness of itemset
		public void calculateFitness(int k,List<Integer> templist) {
			if (k == 0)
				return;
			int i, j, p, q, temp,m;

			int sum, fitness = 0;
			for (m = 0; m < templist.size(); m++) { //m�������񼯺�
				p=templist.get(m).intValue();	// ��p����database�е�transaction
				i = 0;
				j = 0;
				q = 0;
				temp = 0;
				sum = 0;
				// use j to scan bit=1 in tempGroup.X, use q to scan every transaction,
				//use i to scan transaction.X
				while (q < database.get(p).size()
						&& i < this.chromosome.length()){
					if(this.chromosome.get(i)){
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

		@Override
		public int compareTo(Object o) {
			// TODO Auto-generated method stub
			return -(fitness-((ChroNode)o).fitness);
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

	List<Double> percentHUIChroNode;
	List<ChroNode> huiBA = new ArrayList<ChroNode>();
	List<ChroNode> population = new ArrayList<ChroNode>();// populations
	List<ChroNode> subPopulation = new ArrayList<ChroNode>();// son of populations
	List<HUI> huiSets = new ArrayList<HUI>();// the set of HUIs chromosome
	// Create a list to store database
	List<List<Pair>> database = new ArrayList<List<Pair>>();
	List<Double> percentage = new ArrayList<Double>();// the portation of twu value of each
														//1-HTWUIs in sum of twu value
	List<Item> Items;//bitmap database representation

	/**
	 * Default constructor
	 */
	public HUIM_LC_GA() {
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

		writer = new BufferedWriter(new FileWriter(output));

		this.minLength=minLength;
		this.maxLength=maxLength;

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

				// split the transaction according to the : separator
				String split[] = thisLine.split(":");
				// the first part is the list of items
				String items[] = split[0].split(" ");
				// the second part is the RTWU transaction utility
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

				for (int i = 0; i < items.length; i++) {
					// convert item to integer
					Integer item = Integer.parseInt(items[i]);
					// get the current TWU of that item
					Integer twu = mapItemToTWU.get(item);
					Integer twu0 = mapItemToTWU0.get(item);
					// add the utility of the item in the current transaction to
					// its twu
					twu = (twu == null) ? reducedTWU : twu
							+ reducedTWU;
					twu0 = (twu0 == null) ? reducedTWU : twu0
							+ reducedTWU;
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
		// SECOND DATABASE PASS TO CONSTRUCT THE DATABASE
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
						mapItemToTWU0.remove(pair.item);
					}
				}
				// Copy the transaction into database but
				// without items with TWU < minutility
				database.add(revisedTransaction);
			}
		} catch (Exception e) {
			// to catch error while reading the input file
			e.printStackTrace();
		} finally {
			if (myInput != null) {
				myInput.close();
			}
		}
		
		twuPattern = new ArrayList<Integer>(mapItemToTWU0.keySet());
		Collections.sort(twuPattern);

		System.out.println("twuPattern:"+twuPattern.size());
		Items = new ArrayList<Item>();
		
		
		for(Integer tempitem:twuPattern){
			Items.add(new Item(tempitem.intValue()));
		}
	
		//scan database to create bitmap
		for(int i=0;i<database.size();++i){
			for(int j=0;j<Items.size();++j){
				for(int k=0;k<database.get(i).size();++k){
					if(Items.get(j).item==database.get(i).get(k).item){
						Items.get(j).TIDS.set(i);
					}
				}
			}
		}
		// check the memory usage
		checkMemory();

		// Mine the database recursively
		if (twuPattern.size() > 0) {
			double pMax, pMin;
			int num1,num2,tempA,tempB;
			int m = database.size();
			int n = twuPattern.size();
			int temp1 = 0, temp2 = 0;
			// initial population
			pop_Init(minUtility);
			// calculate the mutation rate
			if (m > n) {
				pMin = 1 / (m + 0.0);
				pMax = 1 / (n + 0.0);
			} else {
				pMin = 1 / (n + 0.0);
				pMax = 1 / (m + 0.0);
			}

			for (int i = 0; i < max_iter; i++) {
				if(huiBA.size() > 2){
					percentHUIChroNode = roulettePercentHUIBA();
					num1 = rouletteSelectHUIBA(percentHUIChroNode);
					num2 = rouletteSelectHUIBA(percentHUIChroNode);
					tempA = (int)(Math.random()*pop_size);
					tempB = (int)(Math.random()*pop_size);
					//利用轮盘赌注算法，选取2条染色体以备杂交
					population.get(tempA).deepcopy(huiBA.get(num1));
					population.get(tempB).deepcopy(huiBA.get(num2));
				}
				calculateRfitness();
				// update subPopulation and HUIset
				while (subPopulation.size() < pop_size) {
					
					// 
					temp1 = selectChromosome();
					temp2 = selectChromosome();
					
					while (temp1 == temp2) {
						temp2=(temp2+(int)(Math.random()*1000))%pop_size;
					}
					
					// crossover
					crossover(temp1, temp2, minUtility);
				}
				// mutation
				subPopulation = rankedMutation(pMax, pMin, i, minUtility);
				// get next population
				subPopulation.addAll(population);
				rankData(subPopulation);
				for (int j = 0; j < population.size(); j++) {
					population.set(j, subPopulation.get(j));
				}
				subPopulation.clear();
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
	private void pop_Init(int minUtility)//
	{
		int i = 0, j, k, temp;
		List<Integer> transList;
		// initial percentage according to the twu value of 1-HTWUIs
		percentage = roulettePercent();

		int lur=twuPattern.size()-minLength+1;
		while (i < pop_size) {
			// generate a chromosome
			ChroNode tempNode = new ChroNode(twuPattern.size());
			// initial chromosome
			j = 0;
			// k is the count of 1 in particle
			Random r = new Random();
			k = (int) (r.nextInt(lur)+minLength);    //最小长度到最大长度

			while (j < k) {
				// roulette select the position of 1 in population
				temp = select(percentage);
				if (!tempNode.chromosome.get(temp)) {
					j++;
					tempNode.chromosome.set(temp);
				}

			}
			// calculate the fitenss of chromosome
			transList=new ArrayList<Integer>();
			pev_Check(tempNode,transList);
			tempNode.calculateFitness(k, transList);
			

			tempNode.rank = 0;
			population.add(tempNode);
			if (tempNode.fitness >= minUtility
					&& tempNode.chromosome.cardinality()>=minLength && tempNode.chromosome.cardinality()<=maxLength) {
				insert(tempNode);
				addHuiBA(tempNode);
			}
			i++;
		}
	}
	
	/**
	 * check itemset is promising or unpromising
	 * @param tempBAIndividual
	 * @param list
	 * @return
	 */
	public boolean pev_Check(ChroNode tempBAIndividual,List<Integer> list){
		List<Integer> templist=new ArrayList<Integer>();//�惦�����0��λ��
		List<Integer> templist0=new ArrayList<Integer>();
		List<Integer> zerolist = new ArrayList<Integer>();
		for(int i=0;i<tempBAIndividual.chromosome.length();++i){
			if(tempBAIndividual.chromosome.get(i)){
				templist.add(i);
				templist0.add(i);
			}else {
				zerolist.add(i);
			}
		}
		if(templist.size()==0){
			return false;
		}
		BitSet tempBitSet = new BitSet(database.size());
		BitSet midBitSet = new BitSet(database.size());
		tempBitSet = (BitSet)Items.get(templist.get(0).intValue()).TIDS.clone();
		midBitSet = (BitSet)tempBitSet.clone();//��¼�м���
		
		//item��λͼ���������������ʹ��itemset�������item��������itemset��ȥ����item
		for(int i=1;i<templist.size();++i) {
			tempBitSet.and(Items.get(templist.get(i).intValue()).TIDS);
			if (tempBitSet.cardinality() != 0) {
				midBitSet = (BitSet) tempBitSet.clone();
			} else {
				tempBitSet = (BitSet) midBitSet.clone();
				tempBAIndividual.chromosome.clear(templist.get(i).intValue());
				//使用遍历删除
				for (int j = 0; j < templist0.size(); j++) {
					if ((templist.get(i)).equals(templist0.get(j))) {
						templist0.remove(templist.get(i));
						j--;
					}
				}
			}
		}
	//***************************开始剪枝********************************************//
		int num,changeBit=0;
		if(tempBAIndividual.chromosome.cardinality()>maxLength){
			/**
			 * 主要思想：去掉1的个数，使其满足条件，并且不用做判断
			 */
			num = (int) (((maxLength - minLength + 1) * Math.random()) + (tempBAIndividual.chromosome.cardinality() - maxLength));
			for (int m = 0; m < num; m++) {
				changeBit = (int) (templist0.size() * Math.random());

				//此时，真正的1的位置是templist0，所以，这里也不用做判断
				tempBAIndividual.chromosome.clear(templist0.get(changeBit));
				templist0.remove(templist0.get(changeBit));        //同时删除这个位置的数，防止再次访问到该位置，使得0-->1，不符合题意
			}

			//注意：这里要更新RV，以便重新定义TIDs，用于f(x)的计算
			tempBitSet = (BitSet) Items.get(templist0.get(0).intValue()).TIDS.clone();  //tempBitSet克隆第一个，get(0),相当于第一个1的位置其对应的item
			for (int i = 1; i < templist0.size(); ++i) {   //这里size()指的是1的个数
				tempBitSet.and(Items.get(templist0.get(i).intValue()).TIDS);    //与后面存在的1依次相与
				//因为这个肯定时存在的，故不用判断是否为0
			}
		}else if(tempBAIndividual.chromosome.cardinality()<minLength){
			num=(int)((maxLength-minLength+1)*Math.random()+(minLength-tempBAIndividual.chromosome.cardinality()));

			addNum2(0,num,tempBAIndividual,zerolist,templist0);
			Collections.sort(templist0);

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
		}
	//***********************************************END PEV_CHECK*********************************************//
		
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
	 * calculate rfitness of every ChroNode
	 */
	public void calculateRfitness(){
		int sum=0;
		int temp=0;
		//�ϼ���Ӧֵ
		for(int i=0; i< population.size();++i){
			sum =sum+population.get(i).fitness;
		}
		//��������
		for(int i=0; i< population.size();++i){
			temp =temp+population.get(i).fitness;
			population.get(i).rfitness= temp/(sum+0.0);
		}
	}
	
	/**
	 * select Chromosome to Crossover
	 * @return
	 */
	private int selectChromosome() {
		int i, temp = 0;
		double randNum;
		randNum = Math.random();
		for (i = 0; i < population.size(); i++) {
			if (i == 0) {
				if ((randNum >= 0) && (randNum <= population.get(0).rfitness)) {
					temp = 0;
					break;
				}
			} else if ((randNum > population.get(i - 1).rfitness)
					&& (randNum <= population.get(i).rfitness)) {
				temp = i;
				break;
			}
		}
		return temp;
	}
	
	/**
	 * Method to crossover population[temp1] and population[temp2]
	 * 
	 * @param temp1
	 *            the number of chromosome to crossover
	 * @param temp2
	 *            the number of chromosome to crossover
	 * @param minUtility
	 */
	private void crossover(int temp1, int temp2, int minUtility) {
		int num1,num2,changeBit;
		int tempA = 0, tempB = 0;// record the number of 1 in chromosomes
		List<Integer> transList;//��¼itemset���ڵ�����ļ���
		
		ChroNode temp1Node = new ChroNode();
		ChroNode temp2Node = new ChroNode();
		temp1Node.deepcopy(population.get(temp1));
		temp2Node.deepcopy(population.get(temp2));
		//������Ⱦɫ��֮��ľ���
		List<Integer> disList = bitDiff(temp1Node,temp2Node);
		
		//���н�������
		
		//ʹ�������������ӿ�£
		num1 = (int)(disList.size()*Math.random()) + 1;
		num2 = (int)(disList.size()*Math.random()) + 1;
		
		if(disList.size()>0){
			for(int m = 0; m < num1; ++m){
				changeBit = (int)(disList.size()*Math.random());
				//System.out.println(changeBit);
				if(temp1Node.chromosome.get(disList.get(changeBit))){
					temp1Node.chromosome.clear(disList.get(changeBit));
				}else{
					temp1Node.chromosome.set(disList.get(changeBit));
				}
			}
			for(int m = 0; m < num2; ++m){
				changeBit = (int)(disList.size()*Math.random());
				//System.out.println(changeBit);
				if(temp2Node.chromosome.get(disList.get(changeBit))){
					temp2Node.chromosome.clear(disList.get(changeBit));
				}else{
					temp2Node.chromosome.set(disList.get(changeBit));
				}
			}
		}
		// get the chromosome after crossover
		
		transList=new ArrayList<Integer>();
		pev_Check(temp1Node,transList);
		tempA = temp1Node.chromosome.cardinality();
		temp1Node.calculateFitness(tempA, transList);
		
		temp1Node.rank = 0;
		temp1Node.rfitness = 0;
		subPopulation.add(temp1Node);
		if (temp1Node.fitness >= minUtility
				&&temp1Node.chromosome.cardinality()>=minLength && temp1Node.chromosome.cardinality()<=maxLength) {
			insert(temp1Node);
			addHuiBA(temp1Node);
		}
		
		
		// calculate the fitenss of chromosome
		transList=new ArrayList<Integer>();
		pev_Check(temp2Node,transList);
		tempB = temp2Node.chromosome.cardinality();
		temp2Node.calculateFitness(tempB, transList);
		
		temp2Node.rank = 0;
		temp2Node.rfitness = 0;
		subPopulation.add(temp2Node);
		if (temp2Node.fitness >= minUtility
				&&temp2Node.chromosome.cardinality()>=minLength && temp2Node.chromosome.cardinality()<=maxLength) {
			insert(temp2Node);
			addHuiBA(temp2Node);
		}
	}
	/**
	 * 
	 * @param pMax
	 *            maximum mutation rate
	 * @param pMin
	 *            minimum mutation rate
	 * @param currentIteration
	 *            current iteration
	 * @param minUtility
	 *            minimum utility threshold
	 * @return
	 */
	private List<ChroNode> rankedMutation(double pMax, double pMin,
			int currentIteration, int minUtility) {
		double pm, rankNum;// pm is ranked mutation rate
		List<Integer> transList;
		List<Integer> record = getRank(subPopulation);
		for (int i = 0; i < pop_size; i++) {
			// calculate pm
			pm = (pMax - (pMax - pMin) * currentIteration / max_iter)
					* record.get(i) / subPopulation.size();
			rankNum = Math.random();
			// compare pm an random
			if (/*rankNum < pm*/true) {// single point mutation
				int temp = (int) (Math.random() * twuPattern.size());
				if (subPopulation.get(i).chromosome.get(temp)) {
					subPopulation.get(i).chromosome.clear(temp);
				} else {
					subPopulation.get(i).chromosome.set(temp);
				}
				// get the number of 1 in chromosome
				int k = subPopulation.get(i).chromosome.cardinality();
				// calculate the fitness of chromosome
				transList=new ArrayList<Integer>();
				pev_Check(subPopulation.get(i),transList);
				subPopulation.get(i).calculateFitness(k, transList);
				// insert chromosome has higher utility into huiSets
				if (subPopulation.get(i).fitness >= minUtility
						&&subPopulation.get(i).chromosome.cardinality()>=minLength && subPopulation.get(i).chromosome.cardinality()<=maxLength) {
					insert(subPopulation.get(i));
					addHuiBA(subPopulation.get(i));
				}
			}

		}
		return subPopulation;
	}
	/**
	 * Method to rank chromosomes in population according to fitness
	 * 
	 * @param tempPop
	 * @return
	 */
	private void rankData(List<ChroNode> tempPop) {
		int i;
		Collections.sort(tempPop);

		for (i = 0; i < tempPop.size() - 1; i++) {
			tempPop.get(i).rank = i + 1;
		}
	}

	/**
	 * Method to get the rank of subPopulation in the set of subPopulation and
	 * population
	 * 
	 * @return
	 */
	private List<Integer> getRank(List<ChroNode> tempPop) {
		int i;
		Collections.sort(tempPop);
		List<Integer> rank = new ArrayList<Integer>();
		for (i = 0; i < subPopulation.size(); i++) {
			rank.add(i + 1);
		}
		return rank;
	}

	/**
	 * xor(itemset1,itemset2)
	 * @param gBest
	 * @param tempBAIndividual
	 * @return
	 */
	private List<Integer> bitDiff(ChroNode gBest,ChroNode tempBAIndividual){
		List<Integer> list = new ArrayList<Integer>();
		BitSet tmpBitSet = (BitSet)gBest.chromosome.clone();
		tmpBitSet.xor(tempBAIndividual.chromosome);
		for(int i = 0; i < tmpBitSet.length(); ++i){
			if(tmpBitSet.get(i)){
				list.add(i);
			}
		}
		return list;
	}

	/**
	 * Method to initial percentHUIChroNode
	 * @return
	 */
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
	/**
	 * Method to roulette select chromosome to replace 
	 * the two ChroNode of population
	 * @param percentage
	 * @return
	 */
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
	 * Method to roulette select chromosome
	 * 
	 * @param percentage
	 *            the portation of twu value of each 1-HTWUIs in sum of twu
	 *            value
	 * @return the position of 1
	 */
	private int select(List<Double> percentage) {
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
	/**
	 * Method to inseret tempChroNode to huiSets
	 * 
	 * @param tempChroNode
	 *            the chromosome to be inserted
	 */
	private void insert(ChroNode tempChroNode) {
		int i;
		StringBuilder temp = new StringBuilder();
		for (i = 0; i < twuPattern.size(); i++) {
			if (tempChroNode.chromosome.get(i)) {
				temp.append(twuPattern.get(i));
				temp.append(' ');
			}
		}
		// huiSets is null
		if (huiSets.size() == 0) {
			huiSets.add(new HUI(temp.toString(), tempChroNode.fitness));
		} else {
			// huiSets is not null, judge whether exist an itemset in huiSets
			// same with tempChroNode
			for (i = 0; i < huiSets.size(); i++) {
				if (temp.toString().equals(huiSets.get(i).itemset)) {
					break;
				}
			}
			// if not exist same itemset in huiSets with tempChroNode,insert it
			// into huiSets
			if (i == huiSets.size())
				huiSets.add(new HUI(temp.toString(), tempChroNode.fitness));
		}
	}
	/**
	 * add hui ChroNode to huiBA
	 * @param tempBAIndividual
	 */
	private void addHuiBA(ChroNode tempBAIndividual){
		ChroNode tmpBAIndividual = new ChroNode();
		tmpBAIndividual.deepcopy(tempBAIndividual);
		BitSet tmpBitSet;
		if(huiBA.size() != 0){
			for(int i = 0; i < huiBA.size(); ++i){
				tmpBitSet = (BitSet)(tmpBAIndividual.chromosome.clone());
				tmpBitSet.xor(huiBA.get(i).chromosome);
				if(tmpBitSet.cardinality() == 0){
					return ;
				}
			}
		}	
		huiBA.add(tmpBAIndividual);
	}

//========================BEGIN_minLength=============//
	//编写一个方法，增加num个1
	/**
	 *
	 * @param n             递归参数，增加1的个数
	 * @param num           改变的元素个数
	 * @param tempchromosome   输入染色体
	 * @param zerolist      输入0的列表
	 * @param templist      输入1的列表
	 */
	private void addNum2(int n, int num, ChroNode tempchromosome, List<Integer> zerolist, List<Integer> templist) {  //代n=0,从0开始
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
			if (judge(tempchromosome, templist,temp)) {
				//如果添加的这个1，满足条件就继续递归
				tempchromosome.chromosome.set(temp);                //把这个位置设为1
				templist.add(temp);

				addNum2(n + 1, num, tempchromosome, zerolist, templist);
				return ;
			}

		}
	}


	//判断粒子是否符合条件
	private boolean judge(ChroNode tempchromosome, List<Integer> templist, int temp){
		boolean falg =true;

		//利用中间变量来确定
		ChroNode judgechromosome =new ChroNode();
		List<Integer> judelist = new ArrayList<Integer>();
		judgechromosome.deepcopy(tempchromosome);
		judelist.addAll(templist);

		//中间变量赋值
		judgechromosome.chromosome.set(temp);
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
				.println("=============  HUIF-GA ALGORITHM v.2.36 - STATS =============");
		System.out.println(" Total time ~ " + (endTimestamp - startTimestamp)
				+ " ms");
		System.out.println(" Memory ~ " + maxMemory + " MB");
		System.out.println(" High-utility itemsets count : " + huiSets.size());
		System.out
				.println("===================================================");
	}

	
}
