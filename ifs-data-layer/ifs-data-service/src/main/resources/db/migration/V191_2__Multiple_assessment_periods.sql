-- IFS-8846, multiple assessment periods changes

ALTER TABLE milestone
        DROP INDEX milestone_unique_competition_type;

ALTER TABLE milestone
        ADD COLUMN parent_id BIGINT(20) DEFAULT NULL,
        ADD CONSTRAINT `FK_milestone_parent_id` FOREIGN KEY (`parent_id`) REFERENCES `milestone` (`id`);

ALTER TABLE application
        ADD COLUMN milestone BIGINT(20) DEFAULT NULL,
        ADD CONSTRAINT `FK_milestone_id` FOREIGN KEY (`milestone`) REFERENCES `milestone` (`id`);
