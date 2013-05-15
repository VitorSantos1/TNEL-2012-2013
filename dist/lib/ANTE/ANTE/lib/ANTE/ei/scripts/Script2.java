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
import java.util.Vector;

public class Script2 {

	/**
	 * @param args
	 */
	static Vector<Vector<String>> ficheiro = null;
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		for(int i = 0; i < 90; i++)
		{
			ficheiro = new Vector<Vector<String>>();
			int sim = ((int)(i/30))+1; 
			leFicheiro("scriptFiles/sim_"+sim+"_"+i+"_run_0.txt");
			criaColunas();
			escreveFicheiro("scriptFiles/sim_"+sim+"_"+i+"_run_t.txt");
		}
		
	}
	private static void escreveFicheiro(String string) {
		// TODO Auto-generated method stub
		FileWriter fstream = null;
		try {
			fstream = new FileWriter(string);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		BufferedWriter out = new BufferedWriter(fstream);
		
		for(int i = 0; i < ficheiro.size(); i++)
		{
			//escrever pro ficheiro
			String line = "";
			Vector<String> vec = ficheiro.get(i);
			
			for(int j = 0; j < vec.size(); j++)
				line += vec.get(j)+"\t";
			
			line+="\n";
			
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
	private static void criaColunas() {
		// TODO Auto-generated method stub
		double sum10 = 0.0, sum40= 0.0, sum20=0.0, sum50=0.0, sum30=0.0;
		double sumEpisode10=0.0, sumEpisode40=0.0, sumEpisode20=0.0, sumEpisode50=0.0, sumEpisode30=0.0;
		boolean nextLine = false, finalLine = false;
		for(int i = 0; i < ficheiro.size();i++)
		{
			if(ficheiro.get(i).get(0).startsWith("*"))
				continue;
			else if(ficheiro.get(i).get(0).startsWith("Episode"))
			{
				sum10 = 0.0; 
				sum40= 0.0; 
				sum20=0.0;
				sum50=0.0;
				sum30=0.0;
			}
			else if(ficheiro.get(i).get(0).startsWith("Client"))
			{
				double aux = Double.parseDouble(ficheiro.get(i).get(4));
				double aux2 = Double.parseDouble(ficheiro.get(i).get(2));
				if(aux == 0.0)
				{
					DecimalFormat df = new DecimalFormat("###.##");
					
					double val = aux2*0.1*(-1);
					sum10 += val;
					ficheiro.get(i).add(df.format(val));
					System.out.println("-->"+aux+"..."+val);

					val = aux2*0.2*(-1);
					sum20 += val;
					ficheiro.get(i).add(df.format(val));
					System.out.println("-->"+aux+"..."+val);
					
					val = aux2*0.3*(-1);
					sum30 += val;
					ficheiro.get(i).add(df.format(val));
					System.out.println("-->"+aux+"..."+val);
					
					val = aux2*0.40*(-1);
					sum40 += val;
					ficheiro.get(i).add(df.format(val));
					System.out.println("-->"+aux+"..."+val);
					
					val = aux2*0.50*(-1);
					sum50 += val;
					ficheiro.get(i).add(df.format(val));
					System.out.println("-->"+aux+"..."+val);
				}
				else
				{
					DecimalFormat df = new DecimalFormat("###.##");
					double val = aux2;
					sum10 += val;
					ficheiro.get(i).add(df.format(val));
					System.out.println("-->"+aux+"..."+val);
					
					sum20 += val;
					ficheiro.get(i).add(df.format(val));
					System.out.println("-->"+aux+"..."+val);
					
					sum30 += val;
					ficheiro.get(i).add(df.format(val));
					System.out.println("-->"+aux+"..."+val);
					
					sum40 += val;
					ficheiro.get(i).add(df.format(val));
					System.out.println("-->"+aux+"..."+val);

					sum50 += val;
					ficheiro.get(i).add(df.format(val));
					System.out.println("-->"+aux+"..."+val);
				}
			}
			else if(ficheiro.get(i).size()>1 && ficheiro.get(i).get(1).startsWith("numberOf"))
				nextLine = true;
			else if(ficheiro.get(i).size()>1 && ficheiro.get(i).get(1).startsWith("avgNumberOf"))
				finalLine = true;
			else if(nextLine)
			{
				DecimalFormat df = new DecimalFormat("###.##");
				nextLine=false;
				sum10 /= 10; 
				sum40 /= 10; 
				sum20 /= 10;
				sum50 /= 10;
				sum30 /= 10;
				sumEpisode10 += sum10;
				sumEpisode40 += sum40;
				sumEpisode20 += sum20;
				sumEpisode50 += sum50;
				sumEpisode30 += sum30;
				ficheiro.get(i).add(df.format(sum10));
				ficheiro.get(i).add(df.format(sum40));
				ficheiro.get(i).add(df.format(sum20));
				ficheiro.get(i).add(df.format(sum50));
				ficheiro.get(i).add(df.format(sum30));
			}
			else if(finalLine)
			{
				DecimalFormat df = new DecimalFormat("###.##");
				finalLine=false;
				sumEpisode10 /= 15;
				sumEpisode40 /= 15;
				sumEpisode20 /= 15;
				sumEpisode50 /= 15;
				sumEpisode30 /= 15;
				ficheiro.get(i).add(df.format(sumEpisode10));
				ficheiro.get(i).add(df.format(sumEpisode20));
				ficheiro.get(i).add(df.format(sumEpisode30));
				ficheiro.get(i).add(df.format(sumEpisode40));
				ficheiro.get(i).add(df.format(sumEpisode50));
			}
		}
		
	}
	private static void leFicheiro(String string) {
		// TODO Auto-generated method stub
		File file = new File(string);
		FileInputStream fis = null;
		BufferedInputStream bis = null;
		DataInputStream dis = null;
		
			try {
					fis = new FileInputStream(file);
					bis = new BufferedInputStream(fis);
					dis = new DataInputStream(bis);
						while(dis.available() != 0)
						{
							//processa linha
							String line = dis.readLine();
							Vector<String> vec  = new Vector<String>();
							String[] values = line.split("\t");
							for(int k = 0; k < values.length; k++)
								vec.add(k, values[k]);
							ficheiro.add(vec);
						}
				}catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	}

}
