package ei.scripts;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Hashtable;
import java.util.Vector;

public class Script {

	/**
	 * @param args
	 */
	static Hashtable<Integer, Vector<Vector<String>>> data;
	static int run = -1;
	static int run_act = 0;
	static int NUMBER_OF_ROUNDS = 10;
	
	@SuppressWarnings("deprecation")
	static void readFile(String fileName)
	{
		File file = new File(fileName);
		FileInputStream fis = null;
		BufferedInputStream bis = null;
		DataInputStream dis = null;
	//	System.out.println(fileName);
			try {
					fis = new FileInputStream(file);
					bis = new BufferedInputStream(fis);
					dis = new DataInputStream(bis);
					int episode = -1;
					boolean nextLine = false;
						boolean average = false;
						while(dis.available() != 0)
						{
							//processa linha
							String line = dis.readLine();
							
							if(line.startsWith("*"))
								continue;
							else if(line.startsWith("Episode"))
							{
								String[] aux = line.split("\t");
								episode = Integer.parseInt(aux[0].substring(8));
							}
							else if(line.contains("numberOfDifferentSuppliers	avgUtility	numberOfSuccessfulContracts	avgUtilitySuccessful") || line.contains("diffSuppliers	negUtility	F	Fd_F	Fd_V	V_F	V_V	utility0	TFFHutility	ITFHutility	TFFButility	IFFButility	TTFRutility	ITFRutility	TTFH	IFFH	TFFB	IFFB	TTFR	ITFR	payedTTFH	payedIFFH	payedTFFB	payedIFFB	payedTTFR	payedITFR	penaltyTTFH	penaltyITFH	penaltyTTFB	penaltyITFB	penaltyTTFR	penaltyITFR	RefSupTTFH	RefSupIFFH	RefSupTFFB	RefSupIFFB	RefSupTTFR	RefSupITFR"))
							{
								nextLine = true;
							}
							else if(line.contains("avgNumberOfDifferentSuppliers	avgAvgUtility	avgNumberOfSuccessfulContracts	avgAvgUtilitySuccessful") || line.contains("avgDiffSuppliers	avgNegUtility	avgF	avgFd_F	avgFd_V	avgV_F	avgV_V	avgUtility0	avgTFFHutility	avgIFFHutility	avgTFFButility	avgIFFButility	avgTTFRutility	avgITFRutility	avgTFFH	avgIFFH	avgTFFB	avgIFFB	avgTTFR	avgITFR	avgPayedTFFH	avgPayedIFFH	avgPayedTFFB	avgPayedIFFB	avgPayedTTFR	avgPayedITFR	avgPenaltyTTFH	avgPenaltyITFH	avgPenaltyTTFB	avgPenaltyITFB	avgPenaltyTTFR	avgPenaltyITFR  	avgRefSupTFFH	avgRefSupIFFH	avgRefSupTFFB	avgRefSupIFFB	avgRefSupTTFR	avgRefSupITFR"))
							{
								average = true;
							}
							else if(nextLine)
							{
							//	System.out.println(line);
								String[] values = line.split("\t");
								Vector<Vector<String>> aux = data.get(run);
								if(aux==null)
									aux = new Vector<Vector<String>>();
								Vector<String> vec = new Vector<String>(39);
								for(int i = 1; i < 39; i++)
								{
									vec.add(values[i]);
								}
									
								aux.add(vec);
								data.put(run, aux);
								nextLine=false;
							}
							else if(average)
							{
								String[] values = line.split("\t");
								Vector<Vector<String>> aux = data.get(run);
								if(aux==null)
									aux = new Vector<Vector<String>>();
								Vector<String> vec = new Vector<String>(39);
								for(int i = 1; i < 39; i++)
								{
									vec.add(values[i]);
								}
								aux.add(0, vec);
								data.put(run, aux);
								average = false;
							}
							
						}
					
					fis.close();
					bis.close();
					dis.close();
					
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
	static void mixValues()
	{ 
		Vector<Vector<String>> vec = new Vector<Vector<String>>();
		for(int i = 0; i < 11; i++)
		{
			float sumDiffSup = 0;
			float sumAvgUtil = 0;
			float sumNumbF = 0;
			float sumNumbFdF = 0;
			float sumNumbFdV = 0;
			float sumNumbVF = 0;
			float sumNumbVV = 0;
			float sumAvgSuccessUtil = 0;
			float sumAvgTFFHutility = 0;
			float sumAvgIFFHutility = 0;
			float sumAvgTFFButility = 0;
			float sumAvgIFFButility = 0;
			float sumAvgITFRutility = 0;
			float sumAvgTTFRutility = 0;
			float avgTFFH = 0;	
			float avgIFFH = 0;
			float avgTFFB = 0;
			float avgIFFB = 0;	
			float avgTTFR = 0;
			float avgITFR = 0;
			float avgPayedTFFH = 0;
			float avgPayedIFFH = 0;
			float avgPayedTFFB = 0;
			float avgPayedIFFB = 0;
			float avgPayedTTFR = 0;
			float avgPayedITFR = 0;
			float avgPenaltyTFFH = 0;
			float avgPenaltyIFFH = 0;
			float avgPenaltyTFFB = 0;
			float avgPenaltyIFFB = 0;
			float avgPenaltyTTFR = 0;
			float avgPenaltyITFR = 0;
			float avgRefSupTFFH = 0;
			float avgRefSupIFFH = 0;
			float avgRefSupTFFB = 0;
			float avgRefSupIFFB = 0;
			float avgRefSupTTFR = 0;
			float avgRefSupITFR = 0;
						
			Vector<String> aux = new Vector<String>();
			for(int j = run_act; j < run+1; j++)
			{
				
				//System.out.println(j+"--"+data.size());
				
				Vector<String> aux2 = data.get(j).get(i);
				
				sumDiffSup += Float.parseFloat(aux2.get(0));
				sumAvgUtil += Float.parseFloat(aux2.get(1));
				sumNumbF += Float.parseFloat(aux2.get(2));
				sumNumbFdF += Float.parseFloat(aux2.get(3));
				sumNumbFdV += Float.parseFloat(aux2.get(4));
				sumNumbVF += Float.parseFloat(aux2.get(5));
				sumNumbVV += Float.parseFloat(aux2.get(6));
				sumAvgSuccessUtil += Float.parseFloat(aux2.get(7));
				sumAvgTFFHutility += Float.parseFloat(aux2.get(8));
				sumAvgIFFHutility += Float.parseFloat(aux2.get(9));
				sumAvgTFFButility += Float.parseFloat(aux2.get(10));
				sumAvgIFFButility += Float.parseFloat(aux2.get(11));
				sumAvgITFRutility += Float.parseFloat(aux2.get(13));
				sumAvgTTFRutility += Float.parseFloat(aux2.get(12));
				avgTFFH += Float.parseFloat(aux2.get(14));	
				avgIFFH += Float.parseFloat(aux2.get(15));
				avgTFFB += Float.parseFloat(aux2.get(16));
				avgIFFB += Float.parseFloat(aux2.get(17));	
				avgTTFR += Float.parseFloat(aux2.get(18));
				avgITFR += Float.parseFloat(aux2.get(19));
				avgPayedTFFH += Float.parseFloat(aux2.get(20));
				avgPayedIFFH += Float.parseFloat(aux2.get(21));
				avgPayedTFFB += Float.parseFloat(aux2.get(22));
				avgPayedIFFB += Float.parseFloat(aux2.get(23));
				avgPayedTTFR += Float.parseFloat(aux2.get(24));
				avgPayedITFR += Float.parseFloat(aux2.get(25));
				avgPenaltyTFFH += Float.parseFloat(aux2.get(26));
				avgPenaltyIFFH += Float.parseFloat(aux2.get(27));
				avgPenaltyTFFB += Float.parseFloat(aux2.get(28));
				avgPenaltyIFFB += Float.parseFloat(aux2.get(29));
				avgPenaltyTTFR += Float.parseFloat(aux2.get(30));
				avgPenaltyITFR += Float.parseFloat(aux2.get(31));
				avgRefSupTFFH += Float.parseFloat(aux2.get(32));
				avgRefSupIFFH += Float.parseFloat(aux2.get(33));
				avgRefSupTFFB += Float.parseFloat(aux2.get(34));
				avgRefSupIFFB += Float.parseFloat(aux2.get(35));
				avgRefSupTTFR += Float.parseFloat(aux2.get(36));
				avgRefSupITFR += Float.parseFloat(aux2.get(37));
			}

			sumDiffSup /= NUMBER_OF_ROUNDS;
			sumAvgUtil /= NUMBER_OF_ROUNDS;
			sumNumbF /= NUMBER_OF_ROUNDS;
			sumNumbFdF /= NUMBER_OF_ROUNDS;
			sumNumbFdV /= NUMBER_OF_ROUNDS;
			sumNumbVF /= NUMBER_OF_ROUNDS;
			sumNumbVV /= NUMBER_OF_ROUNDS;
			sumAvgSuccessUtil /= NUMBER_OF_ROUNDS;

			sumAvgTFFHutility /= NUMBER_OF_ROUNDS;
			sumAvgIFFHutility /= NUMBER_OF_ROUNDS;
			sumAvgTFFButility /= NUMBER_OF_ROUNDS;
			sumAvgIFFButility /= NUMBER_OF_ROUNDS;
			sumAvgITFRutility /= NUMBER_OF_ROUNDS;
			sumAvgTTFRutility /= NUMBER_OF_ROUNDS;
			avgTFFH /= NUMBER_OF_ROUNDS;
			avgIFFH /= NUMBER_OF_ROUNDS;
			avgTFFB /= NUMBER_OF_ROUNDS;
			avgIFFB /= NUMBER_OF_ROUNDS;
			avgTTFR /= NUMBER_OF_ROUNDS;
			avgITFR /= NUMBER_OF_ROUNDS;
			avgPayedTFFH /= NUMBER_OF_ROUNDS;
			avgPayedIFFH /= NUMBER_OF_ROUNDS;
			avgPayedTFFB /= NUMBER_OF_ROUNDS;
			avgPayedIFFB /= NUMBER_OF_ROUNDS;
			avgPayedTTFR /= NUMBER_OF_ROUNDS;
			avgPayedITFR /= NUMBER_OF_ROUNDS;
			avgPenaltyTFFH /= NUMBER_OF_ROUNDS;
			avgPenaltyIFFH /= NUMBER_OF_ROUNDS;
			avgPenaltyTFFB /= NUMBER_OF_ROUNDS;
			avgPenaltyIFFB /= NUMBER_OF_ROUNDS;
			avgPenaltyTTFR /= NUMBER_OF_ROUNDS;
			avgPenaltyITFR /= NUMBER_OF_ROUNDS;
			avgRefSupTFFH /= NUMBER_OF_ROUNDS;
			avgRefSupIFFH /= NUMBER_OF_ROUNDS;
			avgRefSupTFFB /= NUMBER_OF_ROUNDS;
			avgRefSupIFFB /= NUMBER_OF_ROUNDS;
			avgRefSupTTFR /= NUMBER_OF_ROUNDS;
			avgRefSupITFR /= NUMBER_OF_ROUNDS;
			
			DecimalFormatSymbols unusualSymbols = new DecimalFormatSymbols();
			unusualSymbols.setDecimalSeparator(',');
			DecimalFormat df = new DecimalFormat("###.##", unusualSymbols);
			aux.add(df.format(sumDiffSup));
			aux.add(df.format(sumAvgUtil));
			aux.add(df.format(sumNumbF));
			aux.add(df.format(sumNumbFdF));
			aux.add(df.format(sumNumbFdV));
			aux.add(df.format(sumNumbVF));
			aux.add(df.format(sumNumbVV));
			aux.add(df.format(sumAvgSuccessUtil));
			aux.add(df.format(sumAvgTFFHutility));
			aux.add(df.format(sumAvgIFFHutility));
			aux.add(df.format(sumAvgTFFButility));
			aux.add(df.format(sumAvgIFFButility));
			aux.add(df.format(sumAvgTTFRutility));
			aux.add(df.format(sumAvgITFRutility));
			aux.add(df.format(avgTFFH));
			aux.add(df.format(avgIFFH));
			aux.add(df.format(avgTFFB));
			aux.add(df.format(avgIFFB));
			aux.add(df.format(avgTTFR));
			aux.add(df.format(avgITFR));
			aux.add(df.format(avgPayedTFFH));
			aux.add(df.format(avgPayedIFFH));
			aux.add(df.format(avgPayedTFFB));
			aux.add(df.format(avgPayedIFFB));
			aux.add(df.format(avgPayedTTFR));
			aux.add(df.format(avgPayedITFR));
			aux.add(df.format(avgPenaltyTFFH));
			aux.add(df.format(avgPenaltyIFFH));
			aux.add(df.format(avgPenaltyTFFB));
			aux.add(df.format(avgPenaltyIFFB));
			aux.add(df.format(avgPenaltyTTFR));
			aux.add(df.format(avgPenaltyITFR));
			aux.add(df.format(avgRefSupTFFH));
			aux.add(df.format(avgRefSupIFFH));
			aux.add(df.format(avgRefSupTFFB));
			aux.add(df.format(avgRefSupIFFB));
			aux.add(df.format(avgRefSupTTFR));
			aux.add(df.format(avgRefSupITFR));
			
			vec.add(aux);
		}
		data.put(new Integer(run_act-1), vec);
		
	}
	
	static void writeFile(String fileName, int column)
	{
		FileWriter fstream = null;
		try {
			fstream = new FileWriter(fileName);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		BufferedWriter out = new BufferedWriter(fstream);
		
		for(int i = run_act-1; i < run+1; i++)
		{
			//escrever pro ficheiro
			String line = "";
			if(i == run_act-1)
			{
				line="\tEpisode_0\t\t\t"+
				"\tEpisode_1\t\t\t"+
				"\tEpisode_2\t\t\t"+
				"\tEpisode_3\t\t\t"+
				"\tEpisode_4\t\t\t"+
				"\tEpisode_5\t\t\t"+
				"\tEpisode_6\t\t\t"+
				"\tEpisode_7\t\t\t"+
				"\tEpisode_8\t\t\t"+
				"\tEpisode_9\t\t\t"+
				"\tEpisode_10\t\t\t"+
				"\tEpisode_11\t\t\t"+
				"\tEpisode_12\t\t\t"+
				"\tEpisode_13\t\t\t"+
				"\tEpisode_14\n";
				switch(column)
				{
				case 0:
					line+="\tNDiffSupp"+
					"\tNDiffSupp"+
					"\tNDiffSupp"+
					"\tNDiffSupp"+
					"\tNDiffSupp"+
					"\tNDiffSupp"+
					"\tNDiffSupp"+
					"\tNDiffSupp"+
					"\tNDiffSupp"+
					"\tNDiffSupp"+
					"\tNDiffSupp"+
					"\tNDiffSupp"+
					"\tNDiffSupp"+
					"\tNDiffSupp"+
					"\tNDiffSupp\n"+
					"Média\t";
					break;
				case 1:
					line+="\tAvgUtility"+
					"\tAvgUtility"+
					"\tAvgUtility"+
					"\tAvgUtility"+
					"\tAvgUtility"+
					"\tAvgUtility"+
					"\tAvgUtility"+
					"\tAvgUtility"+
					"\tAvgUtility"+
					"\tAvgUtility"+
					"\tAvgUtility"+
					"\tAvgUtility"+
					"\tAvgUtility"+
					"\tAvgUtility"+
					"\tAvgUtility\n"+
					"Média\t";
					break;
				case 2:
					line+="\tNFCont"+
					"\tNFCont"+
					"\tNFCont"+
					"\tNFCont"+
					"\tNFCont"+
					"\tNFCont"+
					"\tNFCont"+
					"\tNFCont"+
					"\tNFCont"+
					"\tNFCont"+
					"\tNFCont"+
					"\tNFCont"+
					"\tNFCont"+
					"\tNFCont"+
					"\tNFCont\n"+
					"Média\t";
					break;
				case 3:
					line+="\tNFdFCont"+
					"\tNFdFCont"+
					"\tNFdFCont"+
					"\tNFdFCont"+
					"\tNFdFCont"+
					"\tNFdFCont"+
					"\tNFdFCont"+
					"\tNFdFCont"+
					"\tNFdFCont"+
					"\tNFdFCont"+
					"\tNFdFCont"+
					"\tNFdFCont"+
					"\tNFdFCont"+
					"\tNFdFCont"+
					"\tNFdFCont\n"+
					"Média\t";
					break;
				case 4:
					line+="\tNFdVCont"+
					"\tNFdVCont"+
					"\tNFdVCont"+
					"\tNFdVCont"+
					"\tNFdVCont"+
					"\tNFdVCont"+
					"\tNFdVCont"+
					"\tNFdVCont"+
					"\tNFdVCont"+
					"\tNFdVCont"+
					"\tNFdVCont"+
					"\tNFdVCont"+
					"\tNFdVCont"+
					"\tNFdVCont"+
					"\tNFdVCont\n"+
					"Média\t";
					break;
				case 5:
					line+="\tNVFCont"+
					"\tNVFCont"+
					"\tNVFCont"+
					"\tNVFCont"+
					"\tNVFCont"+
					"\tNVFCont"+
					"\tNVFCont"+
					"\tNVFCont"+
					"\tNVFCont"+
					"\tNVFCont"+
					"\tNVFCont"+
					"\tNVFCont"+
					"\tNVFCont"+
					"\tNVFCont"+
					"\tNVFCont\n"+
					"Média\t";
					break;
				case 6:
					line+="\tNVVCont"+
					"\tNVVCont"+
					"\tNVVCont"+
					"\tNVVCont"+
					"\tNVVCont"+
					"\tNVVCont"+
					"\tNVVCont"+
					"\tNVVCont"+
					"\tNVVCont"+
					"\tNVVCont"+
					"\tNVVCont"+
					"\tNVVCont"+
					"\tNVVCont"+
					"\tNVVCont"+
					"\tNVVCont\n"+
					"Média\t";
					break;
					
				case 7:
					line+="\tAvgUtilSuc"+
					"\tAvgUtilSuc"+
					"\tAvgUtilSuc"+
					"\tAvgUtilSuc"+
					"\tAvgUtilSuc"+
					"\tAvgUtilSuc"+
					"\tAvgUtilSuc"+
					"\tAvgUtilSuc"+
					"\tAvgUtilSuc"+
					"\tAvgUtilSuc"+
					"\tAvgUtilSuc"+
					"\tAvgUtilSuc"+
					"\tAvgUtilSuc"+
					"\tAvgUtilSuc"+
					"\tAvgUtilSuc\n"+
					"Média\t";
					break;
				case 8:
					line+="\tAvgTFFHutility"+
					"\tAvgTFFHutility"+
					"\tAvgTFFHutility"+
					"\tAvgTFFHutility"+
					"\tAvgTFFHutility"+
					"\tAvgTFFHutility"+
					"\tAvgTFFHutility"+
					"\tAvgTFFHutility"+
					"\tAvgTFFHutility"+
					"\tAvgTFFHutility"+
					"\tAvgTFFHutility"+
					"\tAvgTFFHutility"+
					"\tAvgTFFHutility"+
					"\tAvgTFFHutility"+
					"\tAvgTFFHutility\n"+
					"Média\t";
					break;		
				case 9:
					line+="\tAvgIFFHutility"+
					"\tAvgIFFHutility"+
					"\tAvgIFFHutility"+
					"\tAvgIFFHutility"+
					"\tAvgIFFHutility"+
					"\tAvgIFFHutility"+
					"\tAvgIFFHutility"+
					"\tAvgIFFHutility"+
					"\tAvgIFFHutility"+
					"\tAvgIFFHutility"+
					"\tAvgIFFHutility"+
					"\tAvgIFFHutility"+
					"\tAvgIFFHutility"+
					"\tAvgIFFHutility"+
					"\tAvgIFFHutility\n"+
					"Média\t";
					break;	
				case 10:
					line+="\tAvgTFFButility"+
					"\tAvgTFFButility"+
					"\tAvgTFFButility"+
					"\tAvgTFFButility"+
					"\tAvgTFFButility"+
					"\tAvgTFFButility"+
					"\tAvgTFFButility"+
					"\tAvgTFFButility"+
					"\tAvgTFFButility"+
					"\tAvgTFFButility"+
					"\tAvgTFFButility"+
					"\tAvgTFFButility"+
					"\tAvgTFFButility"+
					"\tAvgTFFButility"+
					"\tAvgTFFButility\n"+
					"Média\t";
					break;	
				case 11:
					line+="\tAvgIFFButility"+
					"\tAvgIFFButility"+
					"\tAvgIFFButility"+
					"\tAvgIFFButility"+
					"\tAvgIFFButility"+
					"\tAvgIFFButility"+
					"\tAvgIFFButility"+
					"\tAvgIFFButility"+
					"\tAvgIFFButility"+
					"\tAvgIFFButility"+
					"\tAvgIFFButility"+
					"\tAvgIFFButility"+
					"\tAvgIFFButility"+
					"\tAvgIFFButility"+
					"\tAvgIFFButility\n"+
					"Média\t";
					break;
				case 12:
					line+="\tAvgTTFRutility"+
					"\tAvgTTFRutility"+
					"\tAvgTTFRutility"+
					"\tAvgTTFRutility"+
					"\tAvgTTFRutility"+
					"\tAvgTTFRutility"+
					"\tAvgTTFRutility"+
					"\tAvgTTFRutility"+
					"\tAvgTTFRutility"+
					"\tAvgTTFRutility"+
					"\tAvgTTFRutility"+
					"\tAvgTTFRutility"+
					"\tAvgTTFRutility"+
					"\tAvgTTFRutility"+
					"\tAvgTTFRutility\n"+
					"Média\t";
					break;
				case 13:
					line+="\tAvgITFRutility"+
					"\tAvgITFRutility"+
					"\tAvgITFRutility"+
					"\tAvgITFRutility"+
					"\tAvgITFRutility"+
					"\tAvgITFRutility"+
					"\tAvgITFRutility"+
					"\tAvgITFRutility"+
					"\tAvgITFRutility"+
					"\tAvgITFRutility"+
					"\tAvgITFRutility"+
					"\tAvgITFRutility"+
					"\tAvgITFRutility"+
					"\tAvgITFRutility"+
					"\tAvgITFRutility\n"+
					"Média\t";
					break;
				default:
					line+="\nMédia\t";
					break;
						
				}
				
			}
			else
				line="Run_"+i+"\t";
			
			Vector<Vector<String>> aux = data.get(i);
			
				for(int j = 1; j < aux.size(); j++)
				{
					if(aux.get(j).size() < 15)
					{	
						if(column == 5)
							line += aux.get(j).get(4)+"\t";
						else if(column == 6)
							line += aux.get(j).get(5)+"\t";
						else if(column == 8)
							line += aux.get(j).get(6)+"\t";
						else if(column == 11)
							line += aux.get(j).get(7)+"\t";
						else if(column == 12)
							line += aux.get(j).get(8)+"\t";
						else if(column == 13)
							line += aux.get(j).get(9)+"\t";
						else if(column == 14)
							line += aux.get(j).get(10)+"\t";
					}
					else
					{
						line += aux.get(j).get(column)+"\t";	
					}
				}
		
			line += "\n";
			try {
				out.write(line);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		try {
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		data =  new Hashtable<Integer, Vector<Vector<String>>>();
		int sim = 0;
			for(int i = 0; i < 10; i++)
			{
				sim = (i/30)+1;
				run = i;
				readFile("teste/sim_"+sim+"_"+i+"_run_2.txt");
			}
			mixValues();
			writeFile("teste/output_Sim_"+sim+"_NDiffSupp",0);
			writeFile("teste/output_Sim_"+sim+"_AvgUtility",1);
			writeFile("teste/output_Sim_"+sim+"_NFCont",2);
			writeFile("teste/output_Sim_"+sim+"_NFdFCont",3);
			writeFile("teste/output_Sim_"+sim+"_NFdVCont",4);
			writeFile("teste/output_Sim_"+sim+"_NVFCont",5);
			writeFile("teste/output_Sim_"+sim+"_NVVCont",6);
			writeFile("teste/output_Sim_"+sim+"_AvgUtilSuc",7);
			writeFile("teste/output_Sim_"+sim+"_AvgTFFHutility",8);
			writeFile("teste/output_Sim_"+sim+"_AvgIFFHutility",9);
			writeFile("teste/output_Sim_"+sim+"_AvgTFFButility",10);
			writeFile("teste/output_Sim_"+sim+"_AvgIFFButility",11);
			writeFile("teste/output_Sim_"+sim+"_AvgTTFRutility",12);
			writeFile("teste/output_Sim_"+sim+"_AvgITFRutility",13);
			writeFile("teste/output_Sim_"+sim+"_AvgTFFH",14);
			writeFile("teste/output_Sim_"+sim+"_AvgIFFH",15);
			writeFile("teste/output_Sim_"+sim+"_AvgTFFB",16);
			writeFile("teste/output_Sim_"+sim+"_AvgIFFB",17);
			writeFile("teste/output_Sim_"+sim+"_AvgTTFR",18);
			writeFile("teste/output_Sim_"+sim+"_AvgITFR",19);
			writeFile("teste/output_Sim_"+sim+"_AvgPayedTTFH",20);
			writeFile("teste/output_Sim_"+sim+"_AvgPayedITFH",21);
			writeFile("teste/output_Sim_"+sim+"_AvgPayedTTFB",22);
			writeFile("teste/output_Sim_"+sim+"_AvgPayedITFB",23);
			writeFile("teste/output_Sim_"+sim+"_AvgPayedTTFR",24);
			writeFile("teste/output_Sim_"+sim+"_AvgPayedITFR",25);
			writeFile("teste/output_Sim_"+sim+"_AvgPenaltyTTFH",26);
			writeFile("teste/output_Sim_"+sim+"_AvgPenaltyITFH",27);
			writeFile("teste/output_Sim_"+sim+"_AvgPenaltyTTFB",28);
			writeFile("teste/output_Sim_"+sim+"_AvgPenaltyITFB",29);
			writeFile("teste/output_Sim_"+sim+"_AvgPenaltyTTFR",30);
			writeFile("teste/output_Sim_"+sim+"_AvgPenaltyITFR",31);
			writeFile("teste/output_Sim_"+sim+"_AvgRefSupTTFH",32);
			writeFile("teste/output_Sim_"+sim+"_AvgRefSupITFH",33);
			writeFile("teste/output_Sim_"+sim+"_AvgRefSupTTFB",34);
			writeFile("teste/output_Sim_"+sim+"_AvgRefSupITFB",35);
			writeFile("teste/output_Sim_"+sim+"_AvgRefSupTTFR",36);
			writeFile("teste/output_Sim_"+sim+"_AvgRefSupITFR",37);

			data =  new Hashtable<Integer, Vector<Vector<String>>>();
			run_act = run+21;
			for(int i = 30; i < 40; i++)
			{
				sim = (i/30)+1;
				run = i;
				readFile("teste/sim_"+sim+"_"+i+"_run_2.txt");
			}
			mixValues();
			writeFile("teste/output_Sim_"+sim+"_NDiffSupp",0);
			writeFile("teste/output_Sim_"+sim+"_AvgUtility",1);
			writeFile("teste/output_Sim_"+sim+"_NFCont",2);
			writeFile("teste/output_Sim_"+sim+"_NFdFCont",3);
			writeFile("teste/output_Sim_"+sim+"_NFdVCont",4);
			writeFile("teste/output_Sim_"+sim+"_NVFCont",5);
			writeFile("teste/output_Sim_"+sim+"_NVVCont",6);
			writeFile("teste/output_Sim_"+sim+"_AvgUtilSuc",7);
			writeFile("teste/output_Sim_"+sim+"_AvgTFFHutility",8);
			writeFile("teste/output_Sim_"+sim+"_AvgIFFHutility",9);
			writeFile("teste/output_Sim_"+sim+"_AvgTFFButility",10);
			writeFile("teste/output_Sim_"+sim+"_AvgIFFButility",11);
			writeFile("teste/output_Sim_"+sim+"_AvgTTFRutility",12);
			writeFile("teste/output_Sim_"+sim+"_AvgITFRutility",13);
			writeFile("teste/output_Sim_"+sim+"_AvgTFFH",14);
			writeFile("teste/output_Sim_"+sim+"_AvgIFFH",15);
			writeFile("teste/output_Sim_"+sim+"_AvgTFFB",16);
			writeFile("teste/output_Sim_"+sim+"_AvgIFFB",17);
			writeFile("teste/output_Sim_"+sim+"_AvgTTFR",18);
			writeFile("teste/output_Sim_"+sim+"_AvgITFR",19);
			writeFile("teste/output_Sim_"+sim+"_AvgPayedTTFH",20);
			writeFile("teste/output_Sim_"+sim+"_AvgPayedITFH",21);
			writeFile("teste/output_Sim_"+sim+"_AvgPayedTTFB",22);
			writeFile("teste/output_Sim_"+sim+"_AvgPayedITFB",23);
			writeFile("teste/output_Sim_"+sim+"_AvgPayedTTFR",24);
			writeFile("teste/output_Sim_"+sim+"_AvgPayedITFR",25);
			writeFile("teste/output_Sim_"+sim+"_AvgPenaltyTTFH",26);
			writeFile("teste/output_Sim_"+sim+"_AvgPenaltyITFH",27);
			writeFile("teste/output_Sim_"+sim+"_AvgPenaltyTTFB",28);
			writeFile("teste/output_Sim_"+sim+"_AvgPenaltyITFB",29);
			writeFile("teste/output_Sim_"+sim+"_AvgPenaltyTTFR",30);
			writeFile("teste/output_Sim_"+sim+"_AvgPenaltyITFR",31);
			writeFile("teste/output_Sim_"+sim+"_AvgRefSupTTFH",32);
			writeFile("teste/output_Sim_"+sim+"_AvgRefSupITFH",33);
			writeFile("teste/output_Sim_"+sim+"_AvgRefSupTTFB",34);
			writeFile("teste/output_Sim_"+sim+"_AvgRefSupITFB",35);
			writeFile("teste/output_Sim_"+sim+"_AvgRefSupTTFR",36);
			writeFile("teste/output_Sim_"+sim+"_AvgRefSupITFR",37);
			data =  new Hashtable<Integer, Vector<Vector<String>>>();
			run_act = run+21;
			for(int i = 60; i < 70; i++)
			{
				sim = (i/30)+1;
				run = i;
				readFile("teste/sim_"+sim+"_"+i+"_run_2.txt");
			}
			mixValues();
			writeFile("teste/output_Sim_"+sim+"_NDiffSupp",0);
			writeFile("teste/output_Sim_"+sim+"_AvgUtility",1);
			writeFile("teste/output_Sim_"+sim+"_NFCont",2);
			writeFile("teste/output_Sim_"+sim+"_NFdFCont",3);
			writeFile("teste/output_Sim_"+sim+"_NFdVCont",4);
			writeFile("teste/output_Sim_"+sim+"_NVFCont",5);
			writeFile("teste/output_Sim_"+sim+"_NVVCont",6);
			writeFile("teste/output_Sim_"+sim+"_AvgUtilSuc",7);
			writeFile("teste/output_Sim_"+sim+"_AvgTFFHutility",8);
			writeFile("teste/output_Sim_"+sim+"_AvgIFFHutility",9);
			writeFile("teste/output_Sim_"+sim+"_AvgTFFButility",10);
			writeFile("teste/output_Sim_"+sim+"_AvgIFFButility",11);
			writeFile("teste/output_Sim_"+sim+"_AvgTTFRutility",12);
			writeFile("teste/output_Sim_"+sim+"_AvgITFRutility",13);
			writeFile("teste/output_Sim_"+sim+"_AvgTFFH",14);
			writeFile("teste/output_Sim_"+sim+"_AvgIFFH",15);
			writeFile("teste/output_Sim_"+sim+"_AvgTFFB",16);
			writeFile("teste/output_Sim_"+sim+"_AvgIFFB",17);
			writeFile("teste/output_Sim_"+sim+"_AvgTTFR",18);
			writeFile("teste/output_Sim_"+sim+"_AvgITFR",19);
			writeFile("teste/output_Sim_"+sim+"_AvgPayedTTFH",20);
			writeFile("teste/output_Sim_"+sim+"_AvgPayedITFH",21);
			writeFile("teste/output_Sim_"+sim+"_AvgPayedTTFB",22);
			writeFile("teste/output_Sim_"+sim+"_AvgPayedITFB",23);
			writeFile("teste/output_Sim_"+sim+"_AvgPayedTTFR",24);
			writeFile("teste/output_Sim_"+sim+"_AvgPayedITFR",25);
			writeFile("teste/output_Sim_"+sim+"_AvgPenaltyTTFH",26);
			writeFile("teste/output_Sim_"+sim+"_AvgPenaltyITFH",27);
			writeFile("teste/output_Sim_"+sim+"_AvgPenaltyTTFB",28);
			writeFile("teste/output_Sim_"+sim+"_AvgPenaltyITFB",29);
			writeFile("teste/output_Sim_"+sim+"_AvgPenaltyTTFR",30);
			writeFile("teste/output_Sim_"+sim+"_AvgPenaltyITFR",31);
			writeFile("teste/output_Sim_"+sim+"_AvgRefSupTTFH",32);
			writeFile("teste/output_Sim_"+sim+"_AvgRefSupITFH",33);
			writeFile("teste/output_Sim_"+sim+"_AvgRefSupTTFB",34);
			writeFile("teste/output_Sim_"+sim+"_AvgRefSupITFB",35);
			writeFile("teste/output_Sim_"+sim+"_AvgRefSupTTFR",36);
			writeFile("teste/output_Sim_"+sim+"_AvgRefSupITFR",37);
			data =  new Hashtable<Integer, Vector<Vector<String>>>();
			run_act = run+21;
			
			for(int i = 90; i < 100; i++)
			{
				sim = (i/30)+1;
				run = i;
				readFile("teste/sim_"+sim+"_"+i+"_run_2.txt");
			}
			mixValues();
			writeFile("teste/output_Sim_"+sim+"_NDiffSupp",0);
			writeFile("teste/output_Sim_"+sim+"_AvgUtility",1);
			writeFile("teste/output_Sim_"+sim+"_NFCont",2);
			writeFile("teste/output_Sim_"+sim+"_NFdFCont",3);
			writeFile("teste/output_Sim_"+sim+"_NFdVCont",4);
			writeFile("teste/output_Sim_"+sim+"_NVFCont",5);
			writeFile("teste/output_Sim_"+sim+"_NVVCont",6);
			writeFile("teste/output_Sim_"+sim+"_AvgUtilSuc",7);
			writeFile("teste/output_Sim_"+sim+"_AvgTFFHutility",8);
			writeFile("teste/output_Sim_"+sim+"_AvgIFFHutility",9);
			writeFile("teste/output_Sim_"+sim+"_AvgTFFButility",10);
			writeFile("teste/output_Sim_"+sim+"_AvgIFFButility",11);
			writeFile("teste/output_Sim_"+sim+"_AvgTTFRutility",12);
			writeFile("teste/output_Sim_"+sim+"_AvgITFRutility",13);
			writeFile("teste/output_Sim_"+sim+"_AvgTFFH",14);
			writeFile("teste/output_Sim_"+sim+"_AvgIFFH",15);
			writeFile("teste/output_Sim_"+sim+"_AvgTFFB",16);
			writeFile("teste/output_Sim_"+sim+"_AvgIFFB",17);
			writeFile("teste/output_Sim_"+sim+"_AvgTTFR",18);
			writeFile("teste/output_Sim_"+sim+"_AvgITFR",19);
			writeFile("teste/output_Sim_"+sim+"_AvgPayedTTFH",20);
			writeFile("teste/output_Sim_"+sim+"_AvgPayedITFH",21);
			writeFile("teste/output_Sim_"+sim+"_AvgPayedTTFB",22);
			writeFile("teste/output_Sim_"+sim+"_AvgPayedITFB",23);
			writeFile("teste/output_Sim_"+sim+"_AvgPayedTTFR",24);
			writeFile("teste/output_Sim_"+sim+"_AvgPayedITFR",25);
			writeFile("teste/output_Sim_"+sim+"_AvgPenaltyTTFH",26);
			writeFile("teste/output_Sim_"+sim+"_AvgPenaltyITFH",27);
			writeFile("teste/output_Sim_"+sim+"_AvgPenaltyTTFB",28);
			writeFile("teste/output_Sim_"+sim+"_AvgPenaltyITFB",29);
			writeFile("teste/output_Sim_"+sim+"_AvgPenaltyTTFR",30);
			writeFile("teste/output_Sim_"+sim+"_AvgPenaltyITFR",31);
			writeFile("teste/output_Sim_"+sim+"_AvgRefSupTTFH",32);
			writeFile("teste/output_Sim_"+sim+"_AvgRefSupITFH",33);
			writeFile("teste/output_Sim_"+sim+"_AvgRefSupTTFB",34);
			writeFile("teste/output_Sim_"+sim+"_AvgRefSupITFB",35);
			writeFile("teste/output_Sim_"+sim+"_AvgRefSupTTFR",36);
			writeFile("teste/output_Sim_"+sim+"_AvgRefSupITFR",37);
			data =  new Hashtable<Integer, Vector<Vector<String>>>();
			run_act = run+21;
			for(int i = 120; i < 130; i++)
			{
				sim = (i/30)+1;
				run = i;
				readFile("teste/sim_"+sim+"_"+i+"_run_2.txt");
			}
			mixValues();
			writeFile("teste/output_Sim_"+sim+"_NDiffSupp",0);
			writeFile("teste/output_Sim_"+sim+"_AvgUtility",1);
			writeFile("teste/output_Sim_"+sim+"_NFCont",2);
			writeFile("teste/output_Sim_"+sim+"_NFdFCont",3);
			writeFile("teste/output_Sim_"+sim+"_NFdVCont",4);
			writeFile("teste/output_Sim_"+sim+"_NVFCont",5);
			writeFile("teste/output_Sim_"+sim+"_NVVCont",6);
			writeFile("teste/output_Sim_"+sim+"_AvgUtilSuc",7);
			writeFile("teste/output_Sim_"+sim+"_AvgTFFHutility",8);
			writeFile("teste/output_Sim_"+sim+"_AvgIFFHutility",9);
			writeFile("teste/output_Sim_"+sim+"_AvgTFFButility",10);
			writeFile("teste/output_Sim_"+sim+"_AvgIFFButility",11);
			writeFile("teste/output_Sim_"+sim+"_AvgTTFRutility",12);
			writeFile("teste/output_Sim_"+sim+"_AvgITFRutility",13);
			writeFile("teste/output_Sim_"+sim+"_AvgTFFH",14);
			writeFile("teste/output_Sim_"+sim+"_AvgIFFH",15);
			writeFile("teste/output_Sim_"+sim+"_AvgTFFB",16);
			writeFile("teste/output_Sim_"+sim+"_AvgIFFB",17);
			writeFile("teste/output_Sim_"+sim+"_AvgTTFR",18);
			writeFile("teste/output_Sim_"+sim+"_AvgITFR",19);
			writeFile("teste/output_Sim_"+sim+"_AvgPayedTTFH",20);
			writeFile("teste/output_Sim_"+sim+"_AvgPayedITFH",21);
			writeFile("teste/output_Sim_"+sim+"_AvgPayedTTFB",22);
			writeFile("teste/output_Sim_"+sim+"_AvgPayedITFB",23);
			writeFile("teste/output_Sim_"+sim+"_AvgPayedTTFR",24);
			writeFile("teste/output_Sim_"+sim+"_AvgPayedITFR",25);
			writeFile("teste/output_Sim_"+sim+"_AvgPenaltyTTFH",26);
			writeFile("teste/output_Sim_"+sim+"_AvgPenaltyITFH",27);
			writeFile("teste/output_Sim_"+sim+"_AvgPenaltyTTFB",28);
			writeFile("teste/output_Sim_"+sim+"_AvgPenaltyITFB",29);
			writeFile("teste/output_Sim_"+sim+"_AvgPenaltyTTFR",30);
			writeFile("teste/output_Sim_"+sim+"_AvgPenaltyITFR",31);
			writeFile("teste/output_Sim_"+sim+"_AvgRefSupTTFH",32);
			writeFile("teste/output_Sim_"+sim+"_AvgRefSupITFH",33);
			writeFile("teste/output_Sim_"+sim+"_AvgRefSupTTFB",34);
			writeFile("teste/output_Sim_"+sim+"_AvgRefSupITFB",35);
			writeFile("teste/output_Sim_"+sim+"_AvgRefSupTTFR",36);
			writeFile("teste/output_Sim_"+sim+"_AvgRefSupITFR",37);
			data =  new Hashtable<Integer, Vector<Vector<String>>>();
			run_act = run+21;
			/*for(int i = 150; i < 180; i++)
			{
				sim = (i/30)+1;
				run = i;
				readFile("teste/sim_"+sim+"_"+i+"_run_2.txt");
			}
			mixValues();
			writeFile("teste/output_Sim_"+sim+"_NDiffSupp",0);
			writeFile("teste/output_Sim_"+sim+"_AvgUtility",1);
			writeFile("teste/output_Sim_"+sim+"_NSucCont",2);
			writeFile("teste/output_Sim_"+sim+"_AvgUtilSuc",3);
			data =  new Hashtable<Integer, Vector<Vector<String>>>();
			run_act = run+1;*/
	}

}
