/* ----------------------------------------------------------------------------  */
/*   ( The Authentic JS/JAVA CodeBuff )
 ___ _                      _              _ 
 | _ ) |_  __ _ _ _ __ _ __| |_ __ ____ _ (_)
 | _ \ ' \/ _` | '_/ _` / _` \ V  V / _` || |
 |___/_||_\__,_|_| \__,_\__,_|\_/\_/\__,_|/ |
                                        |__/ 
 */
/* --------------------------------------------------------------------------   */
/*    Youtube: https://youtube.com/@code-with-Bharadwaj                        */
/*    Github : https://github.com/Manu577228                                  */
/*    Portfolio : https://manu-bharadwaj-portfolio.vercel.app/portfolio        */
/* -----------------------------------------------------------------------  */

import java.util.*;
import java.util.regex.*;

// Step 1: Functional interface for Validators (Strategy Pattern)
@FunctionalInterface
interface Validator {
    String validate(Object value);
}

// Step 2: Field class representing each input field
class Field {
    private String name;
    private Object value;
    private List<Validator> validators;

    // Constructor to initialize field details
    public Field(String name, Object value, List<Validator> validators) {
        this.name = name;
        this.value = value;
        this.validators = validators != null ? validators : new ArrayList<>();
    }

    // Step 3: validate() executes all validator rules for the current field
    public List<String> validate() {
        List<String> errors = new ArrayList<>();
        for (Validator v : validators) {
            String result = v.validate(value);
            if (result != null) errors.add(result);
        }
        return errors;
    }

    // Getters for field name
    public String getName() { return name; }
}

// Step 4: Predefined Validator Implementations

class Validators {
    // Required field validator
    public static Validator required() {
        return value -> (value == null || value.toString().trim().isEmpty())
                ? "Field is required"
                : null;
    }

    // Minimum length validator
    public static Validator minLength(int n) {
        return value -> value.toString().length() < n
                ? "Minimum length should be " + n
                : null;
    }

    // Maximum length validator
    public static Validator maxLength(int n) {
        return value -> value.toString().length() > n
                ? "Maximum length should be " + n
                : null;
    }

    // Email pattern validator
    public static Validator email() {
        return value -> {
            String regex = "^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$";
            return Pattern.matches(regex, value.toString())
                    ? null
                    : "Invalid email format";
        };
    }

    // Age numeric validation
    public static Validator agePositive() {
        return value -> {
            if (!(value instanceof Integer)) return "Age must be a number";
            int age = (Integer) value;
            return (age > 0) ? null : "Age must be positive";
        };
    }
}

// Step 5: Main Form Validator Class
class FormValidator {
    private List<Field> fields = new ArrayList<>();

    // Add a new field to the form
    public void addField(Field f) {
        fields.add(f);
    }

    // Run validation across all fields
    public Map<String, List<String>> validate() {
        Map<String, List<String>> result = new HashMap<>();

        for (Field f : fields) {
            List<String> errors = f.validate();
            if (!errors.isEmpty()) {
                result.put(f.getName(), errors);
            }
        }
        return result;
    }
}

// Step 6: Main Runner Class (Demo Execution)
public class FormValidatorDemo {

    public static void main(String[] args) {

        // Create a FormValidator instance
        FormValidator form = new FormValidator();

        // Add form fields with validation rules
        form.addField(new Field("username", "Manu",
                Arrays.asList(Validators.required(), Validators.minLength(3))));
        form.addField(new Field("email", "manu@yt",
                Arrays.asList(Validators.required(), Validators.email())));
        form.addField(new Field("age", 0,
                Arrays.asList(Validators.required(), Validators.agePositive())));
        form.addField(new Field("password", "pw",
                Arrays.asList(Validators.required(), Validators.minLength(6), Validators.maxLength(12))));

        // Validate the entire form
        Map<String, List<String>> result = form.validate();

        // Display results
        if (result.isEmpty()) {
            System.out.println("✅ All validations passed!");
        } else {
            System.out.println("❌ Validation Errors Found:");
            for (Map.Entry<String, List<String>> entry : result.entrySet()) {
                for (String e : entry.getValue()) {
                    System.out.println("- " + entry.getKey() + ": " + e);
                }
            }
        }
    }
}
