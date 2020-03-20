//wang dong
//5988287
import java.math.*;
import java.security.*;
//import java.util.*;
import java.io.*;
public class Gen
{
	static String file1 = "host\\Data.dat";
	static String file2 = "client\\Data.dat";
	static BigInteger g, p, k;
	
	public static void main(String args[])
	{
		SecureRandom r = new SecureRandom();
		boolean isPrime = false, isPrimitive = false;
		System.out.println("generating parameters...");
		while(!isPrime)
		{
			p = BigInteger.probablePrime(128, r);
			//System.out.println("num = "+ p + " bitcount = " + p.bitCount() + " bitLength = " + p.bitLength()); 
			isPrime = testPrime(); 
		}
		int pw = r.nextInt(1000000 - 100000) + 100000;
		//System.out.println(pw);
		
		byte G[] = new byte[128/8];
		
		while(!isPrimitive)
		{
			r.nextBytes(G);
		
			g = new BigInteger(G).abs();
		
		//	System.out.println("num = "+ g + " bitcount = " + g.bitCount() + " bitLength = " + g.bitLength()); 
			
			isPrimitive = testPrimitive();
		}
		
		String output = pw+"|"+p+"|"+g;
		try
		{
			PrintWriter w = new PrintWriter(file1);
			
			w.println(output);
			
			w.close();
			
			w = new PrintWriter(file2);
			
			w.println(output);
			
			w.close();
		}
		catch(Exception e)
		{
			e.getMessage();
		}
		System.out.println("done"); 
	}
	
	static boolean testPrimitive()
	{
		if(g.compareTo(p) != -1)
			return false;
		if(g.mod(p).equals(BigInteger.ONE) || g.mod(p).equals(BigInteger.ONE.negate()) || g.modPow(k,p).equals(BigInteger.ONE))
			g = p.subtract(g);		
		return true;
	}
	
	static boolean testPrime()
	{	
		
		if (!p.isProbablePrime(Integer.MAX_VALUE))
			return false;

		BigInteger two = new BigInteger("2");
		if (BigInteger.ZERO.equals(p.mod(two)))
			return false;	
		
		BigInteger a = p.subtract(BigInteger.ONE);
		k = a.divide(new BigInteger("2"));
		
		if(!k.isProbablePrime(Integer.MAX_VALUE))
			return false;
		
		if (BigInteger.ZERO.equals(k.mod(two)))
			return false;
		
   		return true;
	}
}