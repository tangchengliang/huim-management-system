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
				p=templist.get(m).intValue();		//intValue()??????int?????????????????????
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
//				??????????????????++??????????????????
				++transactionCount;//??????????????????????????????????????????????????????????????

				// split the transaction according to the : separator
				String split[] = thisLine.split(":");
				// the first part is the list of items
				String items[] = split[0].split(" ");
				// the second part is the transaction utility

				/// =================== BEGIN RTWU ===================
				//????????????maxLength?????????TWU
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
				int reducedTWU = 0;  //?????????maxLength???TWU
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
						mapItemToTWU0.remove(pair.item); //?????????????????????pair
					}
				}
				// Copy the transaction into database but
				// without items with TWU < minutility
				database.add(revisedTransaction);    //?????????????????????pair
			}
		} catch (Exception e) {
			// to catch error while reading the input file
			e.printStackTrace();
		} finally {
			if (myInput != null) {
				myInput.close();
			}
		}

		twuPattern = new ArrayList<Integer>(mapItemToTWU0.keySet());  //keySet()???????????????
		Collections.sort(twuPattern);

		System.out.println("twuPattern:"+twuPattern.size());    //??????????????????TWU????????????
		System.out.println(twuPattern);                         //keySet()???????????????????????????[1, 2, 3, 4, 5]
		
		Items = new ArrayList<Item>();
		
		for(Integer tempitem:twuPattern){
			Items.add(new Item(tempitem.intValue()));      //.intValue()??????int
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
		
		List<Integer> transList;//????????????????????????itemset?????????????????????????????????????
		// initial percentage according to the twu value of 1-HTWUIs
		percentage = roulettePercent();
		
//		System.out.println(percentage);   //??????????????????????????????

		int lur=twuPattern.size()-minLength+1;

		for (i = 0; i < pop_size; i++) {
			// initial particles
			Particle tempParticle = new Particle(twuPattern.size());
			j = 0;
			// k is the count of 1 in particle
			Random r = new Random();
			k = (int) (r.nextInt(lur)+minLength);    //???????????????????????????

			while (j < k) {
				// roulette select the position of 1 in population
				temp = rouletteSelect(percentage);
				if (!tempParticle.X.get(temp)) {   //??????????????????1????????????????????????????????????1
					j++;
					tempParticle.X.set(temp);
				}
			}

			//??????????????????????????
			transList=new ArrayList<Integer>();//??????itemset?????????????????????????????????????
			pev_Check(tempParticle,transList);     //??????
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
			//??????i??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
			disList = bitDiff(pBest.get(i),population.get(i));
			
			//????????????????????????????????????????????????
//			???????????????0-1???1-0???????????????1?????????????????????
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
			//??????i?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
			disList = bitDiff(gBest,population.get(i));

			//????????????????????????????????????????????????????????
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
			//??????????????????????????????????????????????????????????????????????????
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
				System.out.println("?????? "+"X="+population.get(i).X+" util="+population.get(i).fitness);
			}
			if(flagmax && population.get(i).fitness>=minUtility){
				System.out.println();
				System.out.println("?????? "+"X="+population.get(i).X+" util="+population.get(i).fitness);
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
		List<Integer> templist=new ArrayList<Integer>();		//??????1?????????
		 List<Integer> templist0=new ArrayList<Integer>();		//??????1??????????????????????????????
		 List<Integer> zerolist = new ArrayList<Integer>();		//??????0????????????
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
		midBitSet = (BitSet)tempBitSet.clone();//??????????????????????
//		 System.out.println();
//		 System.out.println("***************************Begin Pruning*********************************");
//		 System.out.println("???Pruning???"+tempBAIndividual.X);
		//item???????????????????????????????????????????????????????????????itemset?????????????????????item????????????????????????itemset????????????????????item
		for(int i=1;i<templist.size();++i){
			tempBitSet.and(Items.get(templist.get(i).intValue()).TIDS);
			if(tempBitSet.cardinality() != 0){
				midBitSet = (BitSet)tempBitSet.clone();
			}else{
				tempBitSet = (BitSet)midBitSet.clone();
				tempBAIndividual.X.clear(templist.get(i).intValue());
				//??????????????????
				for (int j = 0; j <templist0.size() ; j++) {
					if((templist.get(i)).equals(templist0.get(j))){
						templist0.remove(templist.get(i));
						j--;
					}

				}
//				zerolist.add(templist.get(i));    //??????????????????0????????????0??????????????????
			}
		}

//		 System.out.println("??????Pruning="+tempBAIndividual.X);
//		 System.out.println("======??????????????????========");
//
//		 System.out.println("Pruning????????????:"+tempBitSet);

		//========================BEGIN PEV_Check==============//
		 int num,changeBit=0;
		 if (tempBAIndividual.X.cardinality()>maxLength) {
			 /**
			  * ?????????????????????1??????????????????????????????????????????????????????
			  */
			 num = (int) (((maxLength - minLength + 1) * Math.random()) + (tempBAIndividual.X.cardinality() - maxLength));
			 for (int m = 0; m < num; m++) {
				 changeBit = (int) (templist0.size() * Math.random());

				 //??????????????????1????????????templist0????????????????????????????????????
				 tempBAIndividual.X.clear(templist0.get(changeBit));
				 templist0.remove(templist0.get(changeBit));        //????????????????????????????????????????????????????????????????????????0-->1??????????????????
			 }

			 //????????????????????????RV?????????????????????TIDs?????????f(x)?????????
			 tempBitSet = (BitSet) Items.get(templist0.get(0).intValue()).TIDS.clone();  //tempBitSet??????????????????get(0),??????????????????1?????????????????????item
			 for (int i = 1; i < templist0.size(); ++i) {   //??????size()?????????1?????????
				 tempBitSet.and(Items.get(templist0.get(i).intValue()).TIDS);    //??????????????????1????????????
				 //?????????????????????????????????????????????????????????0
			 }
			 flagmax=true;
//		 }

		 }else if(tempBAIndividual.X.cardinality()<minLength){
			 /**
			  * ????????????????????????????????????1????????????????????????????????????
			  * 	????????????????????????????????????????????????????????????????????????
			  *
			  */
			//???????????????????????????num???1
//			 System.out.println("Length?????????"+tempBAIndividual.X);
//			 System.out.println("templist0="+templist0);
//			 flag=false;


			 num=(int)((maxLength-minLength+1)*Math.random()+(minLength-tempBAIndividual.X.cardinality()));
// 			 System.out.println("*****????????????*****");
//			 System.out.println("?????????????????????"+tempBAIndividual.X);
//			 System.out.println("num"+num);

//			 outParticle=new Particle();//?????????????????????
//			 templist0Copy=new ArrayList<Integer>();
//???????????????BEGIN------------------------------------------
//			 System.out.println("-----------????????????---------------------------------------------------");
//			addNum1(0,num,tempBAIndividual,zerolist,templist0,outParticle,templist0Copy);//??????????????????????????????
//			 System.out.println("-----------????????????--------");
//
//			 tempBAIndividual.copyParticle(outParticle);
//			 templist0=new ArrayList<Integer>();
//			 templist0.addAll(templist0Copy);
//			 Collections.sort(templist0);
//			 System.out.println("?????????????????????"+tempBAIndividual.X);
//			 System.out.println("templisto="+templist0);
//			 System.out.println("====================");
//???????????????END------------------------------------------
/*******************************************************************************/
//???????????????BEGIN-----------------------------------------

//			tempBAIndividual.copyParticle(addNum2(0,num,tempBAIndividual,zerolist,templist0));
			 addNum2(0,num,tempBAIndividual,zerolist,templist0);
			Collections.sort(templist0);
//???????????????END------------------------------------------
//			 System.out.println();
//			 System.out.println("????????????--->??????Particle="+tempBAIndividual.X);
//			 System.out.println("??????templist="+templist0);
//			 System.out.println("*****????????????*****");

			 //????????????RV
			 tempBitSet = new BitSet();
			 tempBitSet = (BitSet)Items.get(templist0.get(0).intValue()).TIDS.clone();
//			 System.out.println("??????:"+tempBitSet.cardinality());
			 for(int i=1;i<templist0.size();i++){
				 tempBitSet.and(Items.get(templist0.get(i).intValue()).TIDS);
				 if(tempBitSet.cardinality()==0){
					 System.out.println("????????????");
					 System.exit(0);
				 }
			 }
			 flag=true;
//			 System.out.println("??????:"+tempBitSet.cardinality());
//			 System.out.println("Bitset"+tempBitSet);
		 }
 		//========================END PEV_Check===============//
//		 System.out.println("======??????????????????========");
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

	//??????????????????????????????
	private boolean judge(Particle temparticle,List<Integer> templist,int temp){
		boolean falg =true;

		//???????????????????????????
		Particle judepaticle = new Particle();
		List<Integer> judelist = new ArrayList<Integer>();
		judepaticle.copyParticle(temparticle);
		judelist.addAll(templist);

		//??????????????????
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
	//???????????????????????????num???1
	/**
	 *
	 * @param n             ?????????????????????1?????????
	 * @param num           ?????????????????????
	 * @param tempartilce   ????????????
	 * @param zerolist      ??????0?????????
	 * @param templist      ??????1?????????
	 */


	private void addNum2(int n,int num,Particle tempartilce,List<Integer> zerolist,List<Integer> templist) {  //???n=0,???0??????
		if (n == num || zerolist.size() == 0) {    //n=num????????????1???????????????ok
//			System.out.println("temparticle=" + tempartilce.X + "------------------------------------");
//			System.out.println("templist=" + templist);
			return ;
		}

		//????????????1????????????????????????
		int changeBit;
		for (int i = 0; i < zerolist.size(); i++) {
			//??????????????????0
			changeBit = (int) (zerolist.size() * Math.random());
			Integer temp = zerolist.get(changeBit);//?????????????????????????????????????????????

			//??????????????????,
			// ????????????????????????,??????????????????0,?????????????????????
			for (int zero = 0; zero < zerolist.size(); zero++) {
				if (temp.equals(zerolist.get(zero))) {
					zerolist.remove(zero);
					zero--;
				}
			}
			//????????????1????????????
			if (judge(tempartilce, templist,temp)) {
				//?????????????????????1??????????????????????????????
				tempartilce.X.set(temp);                //?????????????????????1
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
