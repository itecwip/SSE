package com.paranoiaworks.unicus.android.sse.dao;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.paranoiaworks.unicus.android.sse.R;

/**
 * Support password related atributes
 * in version 1.0.0 mainly "Password Strength"
 * 
 * @author Unicus (unicus<atmark>paranoiaworks.com) for Paranoia Works
 * @version 1.0.0
 */
public class PasswordAttributes {
	
	int passwordStrength;
	int passwordStrengthWeight;
	
	public  PasswordAttributes(String password) {
		this.passwordStrength = checkPasswordStrength(password);
		this.passwordStrengthWeight = checkPasswordStrengthWeight(password);
	}
	
	public int getDrawableID()
	{
		return getDrawableID(this.passwordStrengthWeight);
	}
	
	public static int getDrawableID(int psw)
	{
        int drawableID = -1;
        switch (psw) {
            case 1:  drawableID = R.drawable.d_button_password_1; break;
            case 2:  drawableID = R.drawable.d_button_password_2; break;
            case 3:  drawableID = R.drawable.d_button_password_3; break;
            case 4:  drawableID = R.drawable.d_button_password_4; break;
            case 5:  drawableID = R.drawable.d_button_password_5; break;
            default: drawableID = R.drawable.d_button_password_0; break;
        }
        return drawableID;
	}
	
	public int getCommentID()
	{
		return getCommentID(this.passwordStrengthWeight);
	}
	
	public static int getCommentID(int psw)
	{
        int commentID = -1;
        switch (psw) {
            case 1:  commentID = R.string.passwordDialog_passwordWeak; break;
            case 2:  commentID = R.string.passwordDialog_passwordWeak; break;
            case 3:  commentID = R.string.passwordDialog_passwordFair; break;
            case 4:  commentID = R.string.passwordDialog_passwordStrong; break;
            case 5:  commentID = R.string.passwordDialog_passwordStrong; break;
            default: commentID = R.string.passwordDialog_passwordShort; break;
        }
        return commentID;
	}
	
	public int getSMImageID()
	{
		return getSMImageID(this.passwordStrengthWeight);
	}
	
	public static int getSMImageID(int psw)
	{
        int smiid = -1;
        switch (psw) {
	        case 1:  smiid = R.drawable.strength_metter_1; break;
	        case 2:  smiid = R.drawable.strength_metter_2; break;
	        case 3:  smiid = R.drawable.strength_metter_3; break;
	        case 4:  smiid = R.drawable.strength_metter_4; break;
	        case 5:  smiid = R.drawable.strength_metter_5; break;
	        default: smiid = R.drawable.strength_metter_0; break;
        }
        return smiid;
	}
	
	public static int checkPasswordStrengthWeight(String password)
	{
        int strenght = checkPasswordStrength(password);
        if(strenght == 0) return 0;
        if(strenght < 25) return 1;
        if(strenght < 50) return 2;
        if(strenght < 70) return 3;
        if(strenght < 90) return 4;
        return 5;
	}
	
	public static int checkPasswordStrength(String password)
	{
		if(password.length() < 8) return 0;
		String noDup = removeDuplicatesChar(password);
		if(noDup.length() < 4) return 1;
		
		double strenght = 0;
		double multip = 1;
		
		final int upperCase = matches(password, "[A-Z]");
		double upperCaseWeight = 2.0;
		final int lowerCase = matches(password, "[a-z]");
		double lowerCaseWeight = 2.0;
		final int numbers = matches(password, "[0-9]");
		double numbersWeight = 1.6;
		final int specialCharacters = password.length() - upperCase - lowerCase - numbers;
		double specialCharactersWeight = 2.5;
		
		if (upperCase > 0) multip += 0.5;
		if (lowerCase > 0) multip += 0.5;
		if (numbers > 0) multip += 0.5;
		if (specialCharacters > 0) multip += 0.5;
		
		strenght =  (upperCase * upperCaseWeight) +
					(lowerCase * lowerCaseWeight) +
					(numbers * numbersWeight) +
					(specialCharacters * specialCharactersWeight);
		
		double entropy = (double)password.length() - (double)noDup.length();
		
		strenght -= entropy;
		
		return (int) (strenght * multip);
	}
	
	private static int matches(final String string, final String regexPattern) {
		int matches = 0;
		final Pattern pattern = Pattern.compile(regexPattern);
		final Matcher matcher = pattern.matcher(string);

		while (matcher.find()) {
			++matches;
		}

		return matches;
	}
	
	private static String removeDuplicatesChar(String s) {
	    StringBuilder noDupes = new StringBuilder();
	    for (int i = 0; i < s.length(); i++) {
	        String si = s.substring(i, i + 1);
	        if (noDupes.indexOf(si) == -1) noDupes.append(si);
	    }
	    return noDupes.toString();
	}
}
