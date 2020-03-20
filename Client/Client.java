//wang dong
//5988287
import java.util.*;
import java.io.*;
import java.math.*;
import java.net.*;
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
public class Client implements Runnable
{
	String file = "Data.dat";
	Integer pw;
	BigInteger p, g;
	DatagramSocket socket;
	DatagramPacket packet, packetr = new DatagramPacket(new byte[1024],1024);
	Cipher cipher, cipherr;
	SecretKeySpec PW, K;
	MessageDigest hash;
	InetAddress addr = InetAddress.getLoopbackAddress();
	int port = 1500;
	
	public Client() throws Exception
	{
		try
		{
			Scanner s = new Scanner(new File(file));
			
			String buffer [] = s.nextLine().split("\\|");
			
			s.close();
			
			pw = new Integer(buffer[0]);
			
			p = new BigInteger(buffer[1]);
			
			g = new BigInteger(buffer[2]);
			
			socket = new DatagramSocket();
			
			cipher = Cipher.getInstance("RC4");
			
			cipherr = Cipher.getInstance("RC4");
			
			PW = new SecretKeySpec(pw.toString().getBytes(), "RC4");
			
			hash = MessageDigest.getInstance("SHA-1");
		}
		catch(Exception e)
		{
			System.err.println(e.getMessage());
			
			throw e;
		}
	}
	
	public void execute() throws Exception
	{
		byte X[] = new byte[128/8];
		new SecureRandom().nextBytes(X);
		BigInteger x = new BigInteger(X).abs();
		cipher.init(Cipher.ENCRYPT_MODE, PW);
		byte E[] = cipher.doFinal(g.modPow(x, p).toString().getBytes());
		
		packet = new DatagramPacket(E,E.length,addr,port);
		socket.send(packet);
		
		socket.receive(packetr);
		
		cipherr.init(Cipher.DECRYPT_MODE, PW);
		
		BigInteger ey = new BigInteger(new String(cipherr.doFinal(packetr.getData()),0,packetr.getLength()));
		
		K = new SecretKeySpec(hash.digest(ey.modPow(x,p).toString().getBytes()), "RC4");
		
		cipher.init(Cipher.ENCRYPT_MODE, K);
		
		cipherr.init(Cipher.DECRYPT_MODE, K);
		
		new Thread(this).start();
		
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		
		while(true)
		{
			socket.receive(packetr);
		
			byte buffer [] = cipherr.doFinal(packetr.getData(),0,packetr.getLength());
		
			int msgLen = buffer[0] << 24 | (buffer[1] & 0xFF) << 16 | (buffer[2] & 0xFF) << 8 | (buffer[3] & 0xFF); //big indian
		
			byte byteM[] = Arrays.copyOfRange(buffer,4,msgLen + 4);
			buf.write(K.getEncoded());
			buf.write(byteM);
			byte Hprime[] = hash.digest(buf.toByteArray());
			buf.reset();
			byte H[] = Arrays.copyOfRange(buffer,4+msgLen,buffer.length);
			if(Arrays.equals(Hprime, H))
				System.out.println("\rmsg received: " + new String(byteM) + "\t\t");
			else
				System.out.println("\rAuthentication failed! msg discarded!");
			
			System.out.print("message to host: ");
		}
	}
	
	public void run()
	{
		Scanner readin = new Scanner(System.in);
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		while(true)
		{
			try
			{
				System.out.print("message to host: ");
				String M = readin.nextLine();
				if(M.compareToIgnoreCase("exit") == 0)
					System.exit(0);
				byte byteM[] = M.getBytes();
				buf.write(K.getEncoded());
				buf.write(byteM);
				byte H[] = hash.digest(buf.toByteArray());
				buf.reset();
				int arrLen = M.getBytes().length;
				byte msgLen[] = new byte[4];
				msgLen[0] = (byte)(arrLen >> 24);
				msgLen[1] = (byte)(arrLen >> 16);
				msgLen[2] = (byte)(arrLen >> 8);
				msgLen[3] = (byte)arrLen;
				buf.write(msgLen);
				buf.write(byteM);
				buf.write(H);
				byte C[] = cipher.doFinal(buf.toByteArray());
				buf.reset();
				packet = new DatagramPacket(C, C.length,addr,port);
				socket.send(packet);
			}
			catch(Exception e)
			{	
				throw new RuntimeException(e);
			}
		}
	}
	
	public static void main(String args[]) throws Exception
	{
		new Client().execute();
	}
}