package ei.scripts;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;



public class GeraSuppliers
{
	//gera valores para quantity
	public static Vector<Integer> geraQuantity()
	{
		return gera(0, 1000000, 1);
	}
	
	//gera valores para deadline
	public static Vector<Integer> geraDT()
	{
		Vector<Integer> n=gera(1, 24, 1);
		return n;
	}
	
	//gera Supplier
	public static String geraSupplier(int n)
	{
		switch(n)
		{
			case 0:
				String s ="\t\t<agent>\n" +
				"\t\t\t<name>" + "Supply1Ont1" + "</name>\n";
				for(int i = 0; i < 20; i++)
				{
					int quant = geraQuantity().get(0);
					String good = geraProduto();
					int dt = geraDT().get(0);
					boolean out = geraOutput(n, quant, dt, good);
					s = s+"\t\t\t<contracts>\n"+
						"\t\t\t\t<context>Contract"+i+"</context>\n"+
						"\t\t\t\t<good>"+good+"</good>\n"+
						"\t\t\t\t<quantity>"+quant+"</quantity>\n"+
						"\t\t\t\t<deadlineCont>"+(dt*86400000)+"</deadlineCont>\n"+
						"\t\t\t\t<obligations>\n"+
							"\t\t\t\t\t<bearer>"+"Supply1Ont1"+"</bearer>\n"+
							"\t\t\t\t\t<counterparty>Request1Ont1</counterparty>\n"+
							"\t\t\t\t\t<fact>Transaction</fact>\n"+
							"\t\t\t\t\t<liveline>1</liveline>\n"+
							"\t\t\t\t\t<deadline>2</deadline>\n"+
						"\t\t\t\t</obligations>\n"+
						"\t\t\t\t<fulfillments>\n"+
							"\t\t\t\t\t<ind>";
					if(out)
						s = s+1+"</ind>\n"+
						"\t\t\t\t</fulfillments>\n"+
						"\t\t\t\t<violations>\n"+
							"\t\t\t\t\t<ind></ind>\n"+
						"\t\t\t\t</violations>\n";
					else
						s = s+"</ind>\n"+
						"\t\t\t\t</fulfillments>\n"+
						"\t\t\t\t<violations>\n"+
							"\t\t\t\t\t<ind>1</ind>\n"+
						"\t\t\t\t</violations>\n";
					s=s+"\t\t\t</contracts>\n";		
				}
				s=s+"\t\t</agent>\n";
				return s;
			case 1:
				String s1 ="\t\t<agent>\n" +
				"\t\t\t<name>" + "Supply2Ont1" + "</name>\n";
				for(int i = 0; i < 20; i++)
				{
					int quant = geraQuantity().get(0);
					String good = geraProduto();
					int dt = geraDT().get(0);
					boolean out = geraOutput(n, quant, dt, good);
					s1 = s1+"\t\t\t<contracts>\n"+
						"\t\t\t\t<context>Contract"+i+"</context>\n"+
						"\t\t\t\t<good>"+good+"</good>\n"+
						"\t\t\t\t<quantity>"+quant+"</quantity>\n"+
						"\t\t\t\t<deadlineCont>"+(dt*86400000)+"</deadlineCont>\n"+
						"\t\t\t\t<obligations>\n"+
							"\t\t\t\t\t<bearer>"+"Supply1Ont1"+"</bearer>\n"+
							"\t\t\t\t\t<counterparty>Request1Ont1</counterparty>\n"+
							"\t\t\t\t\t<fact>Transaction</fact>\n"+
							"\t\t\t\t\t<liveline>1</liveline>\n"+
							"\t\t\t\t\t<deadline>2</deadline>\n"+
							"\t\t\t\t</obligations>\n"+
							"\t\t\t\t<fulfillments>\n"+
								"\t\t\t\t\t<ind>";
					if(out)
						s1 = s1+1+"</ind>\n"+
						"\t\t\t\t</fulfillments>\n"+
						"\t\t\t\t<violations>\n"+
							"\t\t\t\t\t<ind></ind>\n"+
						"\t\t\t\t</violations>\n";
					else
						s1 = s1+"</ind>\n"+
						"\t\t\t\t</fulfillments>\n"+
						"\t\t\t\t<violations>\n"+
							"\t\t\t\t\t<ind>1</ind>\n"+
						"\t\t\t\t</violations>\n";
					s1=s1+"\t\t\t</contracts>\n";				
				}
				s1=s1+"\t\t</agent>\n";
				return s1;
			case 2:
				String s11 ="\t\t<agent>\n" +
				"\t\t\t<name>" + "Supply3Ont1" + "</name>\n";
				for(int i = 0; i < 20; i++)
				{
					int quant = geraQuantity().get(0);
					String good = geraProduto();
					int dt = geraDT().get(0);
					boolean out = geraOutput(n, quant, dt, good);
					s11 = s11+"\t\t\t<contracts>\n"+
						"\t\t\t\t<context>Contract"+i+"</context>\n"+
						"\t\t\t\t<good>"+good+"</good>\n"+
						"\t\t\t\t<quantity>"+quant+"</quantity>\n"+
						"\t\t\t\t<deadlineCont>"+(dt*86400000)+"</deadlineCont>\n"+
						"\t\t\t\t<obligations>\n"+
							"\t\t\t\t\t<bearer>"+"Supply1Ont1"+"</bearer>\n"+
							"\t\t\t\t\t<counterparty>Request1Ont1</counterparty>\n"+
							"\t\t\t\t\t<fact>Transaction</fact>\n"+
							"\t\t\t\t\t<liveline>1</liveline>\n"+
							"\t\t\t\t\t<deadline>2</deadline>\n"+
							"\t\t\t\t</obligations>\n"+
							"\t\t\t\t<fulfillments>\n"+
								"\t\t\t\t\t<ind>";
					if(out)
						s11 = s11+1+"</ind>\n"+
						"\t\t\t\t</fulfillments>\n"+
						"\t\t\t\t<violations>\n"+
							"\t\t\t\t\t<ind></ind>\n"+
						"\t\t\t\t</violations>\n";
					else
						s11 = s11+"</ind>\n"+
						"\t\t\t\t</fulfillments>\n"+
						"\t\t\t\t<violations>\n"+
							"\t\t\t\t\t<ind>1</ind>\n"+
						"\t\t\t\t</violations>\n";
					s11=s11+"\t\t\t</contracts>\n";					
				}
				s11=s11+"\t\t</agent>\n";
				return s11;
			case 3:
				String s111 ="\t\t<agent>\n" +
				"\t\t\t<name>" + "Supply4Ont2" + "</name>\n";
				for(int i = 0; i < 20; i++)
				{
					int quant = geraQuantity().get(0);
					String good = geraProduto();
					int dt = geraDT().get(0);
					boolean out = geraOutput(n, quant, dt, good);
					s111 = s111+"\t\t\t<contracts>\n"+
						"\t\t\t\t<context>Contract"+i+"</context>\n"+
						"\t\t\t\t<good>"+good+"</good>\n"+
						"\t\t\t\t<quantity>"+quant+"</quantity>\n"+
						"\t\t\t\t<deadlineCont>"+(dt*86400000)+"</deadlineCont>\n"+
						"\t\t\t\t<obligations>\n"+
							"\t\t\t\t\t<bearer>"+"Supply1Ont1"+"</bearer>\n"+
							"\t\t\t\t\t<counterparty>Request1Ont1</counterparty>\n"+
							"\t\t\t\t\t<fact>Transaction</fact>\n"+
							"\t\t\t\t\t<liveline>1</liveline>\n"+
							"\t\t\t\t\t<deadline>2</deadline>\n"+
							"\t\t\t\t</obligations>\n"+
							"\t\t\t\t<fulfillments>\n"+
								"\t\t\t\t\t<ind>";
					if(out)
						s111 = s111+1+"</ind>\n"+
						"\t\t\t\t</fulfillments>\n"+
						"\t\t\t\t<violations>\n"+
							"\t\t\t\t\t<ind></ind>\n"+
						"\t\t\t\t</violations>\n";
					else
						s111 = s111+"</ind>\n"+
						"\t\t\t\t</fulfillments>\n"+
						"\t\t\t\t<violations>\n"+
							"\t\t\t\t\t<ind>1</ind>\n"+
						"\t\t\t\t</violations>\n";
					s111=s111+"\t\t\t</contracts>\n";					
				}
				s111=s111+"\t\t</agent>\n";
				return s111;
			case 4:
				String s1111 ="\t\t<agent>\n" +
				"\t\t\t<name>" + "Supply5Ont2" + "</name>\n";
				for(int i = 0; i < 20; i++)
				{
					int quant = geraQuantity().get(0);
					String good = geraProduto();
					int dt = geraDT().get(0);
					boolean out = geraOutput(n, quant, dt, good);
					s1111 = s1111+"\t\t\t<contracts>\n"+
						"\t\t\t\t<context>Contract"+i+"</context>\n"+
						"\t\t\t\t<good>"+good+"</good>\n"+
						"\t\t\t\t<quantity>"+quant+"</quantity>\n"+
						"\t\t\t\t<deadlineCont>"+(dt*86400000)+"</deadlineCont>\n"+
						"\t\t\t\t<obligations>\n"+
							"\t\t\t\t\t<bearer>"+"Supply1Ont1"+"</bearer>\n"+
							"\t\t\t\t\t<counterparty>Request1Ont1</counterparty>\n"+
							"\t\t\t\t\t<fact>Transaction</fact>\n"+
							"\t\t\t\t\t<liveline>1</liveline>\n"+
							"\t\t\t\t\t<deadline>2</deadline>\n"+
							"\t\t\t\t</obligations>\n"+
							"\t\t\t\t<fulfillments>\n"+
								"\t\t\t\t\t<ind>";
					if(out)
						s1111 = s1111+1+"</ind>\n"+
						"\t\t\t\t</fulfillments>\n"+
						"\t\t\t\t<violations>\n"+
							"\t\t\t\t\t<ind></ind>\n"+
						"\t\t\t\t</violations>\n";
					else
						s1111 = s1111+"</ind>\n"+
						"\t\t\t\t</fulfillments>\n"+
						"\t\t\t\t<violations>\n"+
							"\t\t\t\t\t<ind>1</ind>\n"+
						"\t\t\t\t</violations>\n";
					s1111=s1111+"\t\t\t</contracts>\n";					
				}
				s1111=s1111+"\t\t</agent>\n";
				return s1111;
			case 5:
				String s11111 ="\t\t<agent>\n" +
				"\t\t\t<name>" + "Supply6Ont2" + "</name>\n";
				for(int i = 0; i < 20; i++)
				{
					int quant = geraQuantity().get(0);
					String good = geraProduto();
					int dt = geraDT().get(0);
					boolean out = geraOutput(n, quant, dt, good);
					s11111 = s11111+"\t\t\t<contracts>\n"+
						"\t\t\t\t<context>Contract"+i+"</context>\n"+
						"\t\t\t\t<good>"+good+"</good>\n"+
						"\t\t\t\t<quantity>"+quant+"</quantity>\n"+
						"\t\t\t\t<deadlineCont>"+(dt*86400000)+"</deadlineCont>\n"+
						"\t\t\t\t<obligations>\n"+
							"\t\t\t\t\t<bearer>"+"Supply1Ont1"+"</bearer>\n"+
							"\t\t\t\t\t<counterparty>Request1Ont1</counterparty>\n"+
							"\t\t\t\t\t<fact>Transaction</fact>\n"+
							"\t\t\t\t\t<liveline>1</liveline>\n"+
							"\t\t\t\t\t<deadline>2</deadline>\n"+
							"\t\t\t\t</obligations>\n"+
							"\t\t\t\t<fulfillments>\n"+
								"\t\t\t\t\t<ind>";
					if(out)
						s11111 = s11111+1+"</ind>\n"+
						"\t\t\t" +
						"\t</fulfillments>\n"+
						"\t\t\t\t<violations>\n"+
							"\t\t\t\t\t<ind></ind>\n"+
						"\t\t\t\t</violations>\n";
					else
						s11111 = s11111+"</ind>\n"+
						"\t\t\t\t</fulfillments>\n"+
						"\t\t\t\t<violations>\n"+
							"\t\t\t\t\t<ind>1</ind>\n"+
						"\t\t\t\t</violations>\n";
					s11111=s11111+"\t\t\t</contracts>\n";					
				}
				s11111=s11111+"\t\t</agent>\n";
				return s11111;
			case 6:
				String s111111 ="\t\t<agent>\n" +
				"\t\t\t<name>" + "Supply7Ont2" + "</name>\n";
				for(int i = 0; i < 20; i++)
				{
					int quant = geraQuantity().get(0);
					String good = geraProduto();
					int dt = geraDT().get(0);
					boolean out = geraOutput(n, quant, dt, good);
					s111111 = s111111+"\t\t\t<contracts>\n"+
						"\t\t\t\t<context>Contract"+i+"</context>\n"+
						"\t\t\t\t<good>"+good+"</good>\n"+
						"\t\t\t\t<quantity>"+quant+"</quantity>\n"+
						"\t\t\t\t<deadlineCont>"+(dt*86400000)+"</deadlineCont>\n"+
						"\t\t\t\t<obligations>\n"+
							"\t\t\t\t\t<bearer>"+"Supply1Ont1"+"</bearer>\n"+
							"\t\t\t\t\t<counterparty>Request1Ont1</counterparty>\n"+
							"\t\t\t\t\t<fact>Transaction</fact>\n"+
							"\t\t\t\t\t<liveline>1</liveline>\n"+
							"\t\t\t\t\t<deadline>2</deadline>\n"+
							"\t\t\t\t</obligations>\n"+
							"\t\t\t\t<fulfillments>\n"+
								"\t\t\t\t\t<ind>";
					if(out)
						s111111 = s111111+1+"</ind>\n"+
						"\t\t\t\t</fulfillments>\n"+
						"\t\t\t\t<violations>\n"+
							"\t\t\t\t\t<ind></ind>\n"+
						"\t\t\t\t</violations>\n";
					else
						s111111 = s111111+"</ind>\n"+
						"\t\t\t\t</fulfillments>\n"+
						"\t\t\t\t<violations>\n"+
							"\t\t\t\t\t<ind>1</ind>\n"+
						"\t\t\t\t</violations>\n";
					s111111=s111111+"\t\t\t</contracts>\n";					
				}
				s111111=s111111+"\t\t</agent>\n";
				return s111111;
			default:
				return "";
		}
	}
	
	private static boolean geraOutput(int n, int quant, int dt, String good) {
		int out = gera(0,100,1).get(0);
		
		// term LOW
		double belongToLow = 0;
		if (dt >= 0 && dt <= 14) {
			belongToLow = (double)(14-dt)/14;
		}
		
		// term MEDIUM
		double belongToMedium = 0;
		if (7 <= dt && dt < 21) {
			belongToMedium = (double)(dt-7)/14;
		}
		if (dt >= 21 && dt <= 35) {
			belongToMedium = (double)(35 - dt)/14;
		}
		
		// term HIGH
		double belongToBig = 0;
		if (dt < 28) {
			belongToBig = 0.0;
		}
		if (dt >= 28 && dt <= 35) {
			belongToBig = (double)(dt - 28) / 7;
		}
		
		if (belongToLow > belongToMedium && belongToLow > belongToBig)
			dt=0;
		else
			dt=1;
		
		
		// term LOW
		double belongToLow1 = 0;
		if (quant >= 0 && quant <= 270000) {
			belongToLow1 = (double)(270000-quant)/270000;
		}
		
		// term MEDIUM
		double belongToMedium1 = 0;
		if (180000 <= quant && quant < 540000) {
			belongToMedium1 = (double)(quant-180000)/360000;
		}
		if (quant >= 540000 && quant <= 900000) {
			belongToMedium1 = (double)(900000 - quant)/360000;
		}
		
		// term HIGH
		double belongToHigh = 0;
		if (quant < 810000) {
			belongToHigh = 0.0;
		}
		if (quant >= 810000 && quant <= 1080000) {
			belongToHigh = (double)(quant - 810000) / 270000;
		}
		
		if (belongToLow1 > belongToMedium1 && belongToLow1 > belongToHigh)
			quant=1;
		else if (belongToMedium1 > belongToLow1 && belongToMedium1 > belongToHigh)
			quant=1;
		else quant=0;
		
		//0 - S1O1 = HDT
		//1 - S2O1 = HQT
		//2 - S3O1 = HFab(Switch)
		//3 - S4O2 = HQTDT
		//4 - S5O2 = HFabQT
		//5 - S6O2 = HFabDT
		//6 - S7O2 = Sem Handicap
		switch(n)
		{
		case 0:
			if(dt==0)
			{
				if(out>94)
					return true;
				else
					return false;
			}
			else
				return true;
		case 1:
			if(quant == 0)
				if(out>94)
					return true;
				else
					return false;
			else
				return true;
		case 2:
			if(good.equalsIgnoreCase("Switch"))
				if(out>94)
					return true;
				else
					return false;
			else
				return true;
		case 3:
			if(dt==0 || quant==0)
			{
				if(out>94)
					return true;
				else
					return false;
			}
			else
				return true;
		case 4:
			if(quant==0 || good.equalsIgnoreCase("Switch"))
			{
				if(out>94)
					return true;
				else
					return false;
			}
			else
				return true;
		case 5:
			if(dt==0 || good.equalsIgnoreCase("Switch"))
			{
				if(out>94)
					return true;
				else
					return false;
			}
			else
				return true;
		case 6:
			if(out>49)
				return true;
			else
				return false;
		default:
			return true;
		}
	}

	//gera o produto
	public static String geraProduto()
	{
		int n = gera(0, 10, 1).get(0);
		switch(n)
		{
			case 0:
				return "Switch";
			case 1:
				return "Alarm";
			case 2:
				return "Command";
			case 3:
				return "Camera";
			default:
				return "O"+n;
		}
	}
	
	//Gera q valores aleatórios entre m e n
	public static Vector<Integer> gera(int m, int n, int q)
	{
		Vector<Integer> vec = new Vector<Integer>();
		for(int i = 0; i < q; i++)
		{
			double d = Math.random();
			int value = (int)((d*(m-n-1))+(n+1));
			vec.add(value);
		}
		return vec;
	}
	
	public static void main(String args[])
	{
		String s = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"+
		"<!DOCTYPE reputation SYSTEM \"reputation.dtd\">\n"+
		"<reputation>\n"+
			"\t<parameters>\n"+
				"\t\t<flexibility>1</flexibility>\n"+
				"\t\t<premium>1</premium>\n"+
				"\t\t<penalty>1</penalty>\n"+
			"\t</parameters>\n"+
			"\t<values>\n";

		for(int i = 0; i < 7; i++)
			s=s+geraSupplier(i);
		
		s=s+"</values>\n</reputation>";
		FileWriter fstream;
		try {
			fstream = new FileWriter("reputation.xml");
		
		BufferedWriter out = new BufferedWriter(fstream);
		out.write(s);
		out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
}