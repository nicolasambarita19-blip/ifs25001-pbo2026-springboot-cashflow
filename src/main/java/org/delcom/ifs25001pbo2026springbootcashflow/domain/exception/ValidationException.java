package org.delcom.ifs25001pbo2026springbootcashflow.domain.exception;

/** Dilempar saat validasi aturan bisnis gagal. */
public class ValidationException extends DomainException {
    public ValidationException(String message) { super(message); }
}
