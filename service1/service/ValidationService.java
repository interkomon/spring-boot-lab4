package ru.kovyazin.lab4.service1.service;

import org.springframework.validation.BindingResult;
import ru.kovyazin.lab4.service1.exception.ValidationFailedException;

public interface ValidationService {
    void isValid(BindingResult result) throws ValidationFailedException;
}
