package moe.ywp.misaka.helper;

/**
 * Created by Stalin on 15/03/2017.
 */

import android.content.Context;
import org.mindrot.jbcrypt.BCrypt;

import java.io.*;

public class Password {

    /**
     *
     * @param password plaintext string from the password field
     * @return bcrypt salted hash of password
     */
    public static String hashPassword(String password) {
        String hashed = BCrypt.hashpw(password, BCrypt.gensalt());
        return hashed;
    }

    /**
     * checks password length
     * @param password plaintext string from the password field
     * @return bool, true if long enough false if too short
     */
    public static boolean checkPassword(String password) {
        if (password.length() < 10) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * checks wether the password is in the top 10000 password list
     * @param password plaintext string from the password field
     * @return bool, true if password is uncommon, false if password is common
     * @throws IOException should really not happen as it checks against a hardcoded static file
     */
    public static boolean passwordContainsTop(String password, Context context) throws IOException{
        InputStream instream = context.getAssets().open("10_million_password_list_top_10000.txt");
        try (BufferedReader bReader = new BufferedReader(new InputStreamReader(instream))) {
            String line;
            while ((line = bReader.readLine()) != null) {
                if (line.equals(password)){
                    return false;
                }
            }
            return true;
        }
    }

}
