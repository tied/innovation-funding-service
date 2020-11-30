CREATE TABLE `ifs`.`heukar_org_type` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `application_id` BIGINT(20) NULL,
  `organisation_type_id` BIGINT(20) NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `heukar_org_type_application_fk`
    FOREIGN KEY (`application_id`)
    REFERENCES `ifs`.`application` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `heukar_org_type_fk`
    FOREIGN KEY (`organisation_type_id`)
    REFERENCES `ifs`.`organisation_type` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);
