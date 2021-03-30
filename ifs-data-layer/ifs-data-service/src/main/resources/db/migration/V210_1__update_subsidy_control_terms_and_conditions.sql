-- IFS-9467-update-subsidy-control-ati-terms-conditions

SET @system_maintenance_user_id = (
    SELECT id
    FROM user
    WHERE email = 'ifs_system_maintenance_user@innovateuk.org');

INSERT INTO terms_and_conditions (name, template, version, type, created_by, created_on, modified_on, modified_by)
VALUES ('Aerospace Technology Institute (ATI) - Subsidy control', 'ati-subsidy-control-terms-and-conditions-v3', 3, 'GRANT', @system_maintenance_user_id, NOW(), NOW(), @system_maintenance_user_id);