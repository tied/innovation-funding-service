-- IFS-7806 - include KTA invite type

ALTER TABLE invite
    MODIFY COLUMN type enum('ROLE',
                            'COMPETITION',
                            'COMPETITION_STAKEHOLDER',
                            'COMPETITION_INNOVATION_LEAD',
                            'ASSESSMENT_PANEL',
                            'INTERVIEW_PANEL',
                            'PROJECT',
                            'PROJECT_PARTNER',
                            'MONITORING_OFFICER',
                            'APPLICATION',
                            'EXTERNAL_FINANCE',
                            'GRANTS_MONITORING_OFFICER',
                            'GRANTS_PROJECT_MANAGER',
                            'GRANTS_PROJECT_FINANCE_CONTACT',
                            'KTA');