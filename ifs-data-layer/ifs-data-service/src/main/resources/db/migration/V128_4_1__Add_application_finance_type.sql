-- IFS-3284 Begin migration of competition.full_application_finances to new column competition.application_finance_type
ALTER TABLE competition ADD application_finance_type ENUM('STANDARD', 'STANDARD_WITH_VAT', 'NO_FINANCES') NULL;

UPDATE competition SET application_finance_type = 'STANDARD' WHERE full_application_finance = 1;
UPDATE competition SET application_finance_type = 'NO_FINANCES' WHERE full_application_finance = 0;
UPDATE competition SET application_finance_type = 'NO_FINANCES' WHERE full_application_finance IS NULL;