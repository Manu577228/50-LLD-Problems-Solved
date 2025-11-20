# Design a Form Validator Utility in Python that allows adding multiple input fields,
# each with customizable validation rules (like required, min/max length, email format, and
# age check). The utility should validate all fields and return descriptive error messages
# for invalid inputs.

import re


class Field:
    def __init__(self, name, value, validators=None):
        self.name = name
        self.value = value
        self.validators = validators or []

    def validate(self):
        errors = []
        for v in self.validators:
            result = v(self.value)
            if result is not True:
                errors.append(result)
        return errors


def required(value):
    if value is None or str(value).strip() == "":
        return "Fiels is required"
    return True


def min_length(n):
    def inner(value):
        if len(str(value)) < n:
            return f"Minimum length should be {n}"
        return True
    return inner


def max_length(n):
    def inner(value):
        if len(str(value)) > n:
            return f"Maximum length should be {n}"
        return True
    return inner


def email_validator(value):
    pattern = r"^[\w\.-]+@[\w\.-]+\.\w+$"
    if not re.match(pattern, str(value)):
        return "Invalid email format"
    return True


def age_validator(value):
    if not isinstance(value, int) or value <= 0:
        return "Age must be a positive Integer"
    return True


class FormValidator:
    def __init__(self):
        self.fields = []

    def add_field(self, field):
        self.fields.append(field)

    def validate(self):
        errors = {}
        for f in self.fields:
            result = f.validate()
            if result:
                errors[f.name] = result
        return errors


if __name__ == "__main__":
    # Create an instance of FormValidator
    form = FormValidator()

    # Add several fields with their respective validators
    form.add_field(Field("username", "Manu", [required, min_length(3)]))
    form.add_field(Field("email", "manu@yt", [required, email_validator]))
    form.add_field(Field("age", 31, [required, age_validator]))
    form.add_field(
        Field("password", "pw", [required, min_length(6), max_length(12)]))

    # Run validation across all fields
    result = form.validate()

    # Display results neatly
    if not result:
        print("✅ All validations passed!")
    else:
        print("❌ Validation Errors Found:")
        for field, errs in result.items():
            for e in errs:
                print(f"- {field}: {e}")
