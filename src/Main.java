
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
 
public class Main {
	
	public List<byte[]> hashes = new ArrayList<>();
	public List<String> commonBins = new ArrayList<>();
	public long start = System.currentTimeMillis();
	public int[] times2 = new int[10];
	
    public static void main(String[] args) throws NoSuchAlgorithmException, IOException {
    	Main m = new Main();
    	for (int i = 0; i < 10; i++) m.times2[i] = i*2;
    	m.readHashesIn("data/hashes.txt");
    	m.getCommonBins("data/pans.txt");
    	m.findPANs();
    }
    
    public void findPANs() throws NoSuchAlgorithmException, UnsupportedEncodingException {
    	int checkDigit;
    	byte[] numDigits = new byte[10];
    	boolean invalid;
    	long pan;
    	for (String bin : commonBins) {
    		pan = Long.parseLong(bin) * 10000000000L;
//    		System.out.println("Starting " + bin);
    		for (int account = 0; account < 1000000000; account++) {
    			// Make sure number contains no more than 2 of each digit
    			String str = String.format("%09d", account);
    			byte[] digits = str.getBytes();
    			Arrays.fill(numDigits, (byte) 0);
    			invalid = false;
    			for (int i = 0; i < 9; i++) {
    				int dig = digits[i]-48;
    				numDigits[dig]++;
    				if (numDigits[dig] > 2) {
    					invalid = true;
    					break;
    				}
    			}
    			if (invalid) continue;
    			
    			long ccn = pan + (account * 10);
//    			System.out.println(ccn);
    			// Check if it is a hashed pan
    			checkDigit = generateCheckDigit(ccn);
    			ccn += checkDigit;
    			byte[] hash = sha1(ccn);
    			validateHash(hash, Long.toString(ccn));
    		}
    	}
    	System.out.println("done");
    }
    
    private void validateHash(byte[] hash, String pan) {
    	for (byte[] known : hashes) {
    		if (Arrays.equals(known, hash)) {
    			System.out.println("Found " + pan + ": " + Base64.encodeBase64String(known));
    			hashes.remove(known);
//    			System.out.println("Milliseconds: " + (System.currentTimeMillis() - start));
//    			System.exit(0);
    			break;
    		}
    	}
    }
    
    private void getCommonBins(String filename) throws NoSuchAlgorithmException, IOException {
    	FileInputStream fis = new FileInputStream(filename);
        BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
    	
        Map<String, Integer> binFrequencies = new HashMap<>();
        
        String line = reader.readLine();
        while (line != null) {
        	String bin = line.substring(0, 6);
        	if (binFrequencies.containsKey(bin)) {
        		binFrequencies.put(bin, binFrequencies.get(bin) + 1);
        	} else {
        		binFrequencies.put(bin, 1);
        	}
        	line = reader.readLine();
        }
        
        reader.close();
        
        // Sort keys by their frequency
        commonBins.addAll(binFrequencies.keySet());
        final Map<String, Integer> map = binFrequencies;
        Collections.sort(commonBins, new Comparator<String>() {
        		public int compare(String a, String b) {
					if (map.get(a) > map.get(b)) return -1;
        			return 1;
        		}
        });
	}

	public void readHashesIn(String filename) throws NoSuchAlgorithmException, IOException {
    	FileInputStream fis = new FileInputStream(filename);
        BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
    	
        String line = reader.readLine();
        while (line != null) {
        	byte[] hash = Base64.decodeBase64(line);
        	hashes.add(hash);
        	line = reader.readLine();
        }
        
        reader.close();
    }
     
	public byte[] sha1(long l) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		String input = Long.toString(l);
	    MessageDigest mDigest = MessageDigest.getInstance("SHA1");
	    return mDigest.digest(input.getBytes("US-ASCII"));
	}
	
	private static int generateCheckDigit(long l) {
		String str = Long.toString(l);
        int[] ints = new int[str.length()];
        
        for (int i = 0; i< str.length(); i++) {
            ints[i] = Integer.parseInt(str.substring(i, i+1));
        }
        
        for (int i = ints.length - 2; i >= 0; i -= 2) {
            int j = ints[i];
            j = j * 2;
            if (j > 9) {
                j = j % 10 + 1;
            }
            ints[i] = j;
        }
        
        int sum = 0;
        for(int i = 0; i < ints.length; i++) {
            sum += ints[i];
        }
        
        if (sum % 10 == 0) {
            return 0;
        } else return 10 - (sum % 10);
    }
}
