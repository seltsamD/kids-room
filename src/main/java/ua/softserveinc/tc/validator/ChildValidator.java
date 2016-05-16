package ua.softserveinc.tc.validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import ua.softserveinc.tc.constants.ValidationConst;
import ua.softserveinc.tc.entity.Child;
import ua.softserveinc.tc.service.ChildService;
import ua.softserveinc.tc.service.ChildServiceImpl;

import java.util.Date;
import java.util.regex.Pattern;

/**
 * Created by Nestor on 12.05.2016.
 */

@Component
public class ChildValidator implements Validator{

    @Override
    public boolean supports(Class<?> aClass) {
        return Child.class.equals(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        Child kidToValidate = (Child) o;

        ValidationUtils.rejectIfEmpty(errors, "firstName", "registration.empty");
        ValidationUtils.rejectIfEmpty(errors, "lastName", "registration.empty");
        ValidationUtils.rejectIfEmpty(errors, "dateOfBirth", "registration.empty");

        if(!Pattern.compile(ValidationConst.NAME_REGEX)
                .matcher(kidToValidate.getFirstName())
                .matches()){
            errors.rejectValue("firstName",  "registration.kid.name");
        }

        if(!Pattern.compile(ValidationConst.NAME_REGEX)
                .matcher(kidToValidate.getLastName())
                .matches()){
            errors.rejectValue("lastName",  "registration.kid.name");
        }

        int age = kidToValidate.getAge();

        if(age < ChildServiceImpl.getMinAge() || age> ChildServiceImpl.getMaxAge()){
            errors.rejectValue("dateOfBirth",  "registration.kid.date");
        }
    }
}