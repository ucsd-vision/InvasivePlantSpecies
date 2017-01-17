-- MySQL Script generated by MySQL Workbench
-- 01/16/17 13:05:15
-- Model: New Model    Version: 1.0
-- MySQL Workbench Forward Engineering

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';

-- -----------------------------------------------------
-- Schema invasivespecies
-- -----------------------------------------------------
DROP SCHEMA IF EXISTS `invasivespecies` ;

-- -----------------------------------------------------
-- Schema invasivespecies
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `invasivespecies` DEFAULT CHARACTER SET utf8 ;
USE `invasivespecies` ;

-- -----------------------------------------------------
-- Table `invasivespecies`.`gsv_pano`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `invasivespecies`.`gsv_pano` ;

CREATE TABLE IF NOT EXISTS `invasivespecies`.`gsv_pano` (
  `panoid` VARCHAR(64) NOT NULL,
  `lat` DOUBLE NULL,
  `lng` DOUBLE NULL,
  `image` VARCHAR(64) NULL,
  PRIMARY KEY (`panoid`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `invasivespecies`.`species`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `invasivespecies`.`species` ;

CREATE TABLE IF NOT EXISTS `invasivespecies`.`species` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `description` VARCHAR(45) NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `spid_UNIQUE` (`id` ASC))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `invasivespecies`.`bounding_box`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `invasivespecies`.`bounding_box` ;

CREATE TABLE IF NOT EXISTS `invasivespecies`.`bounding_box` (
  `bbid` INT NOT NULL AUTO_INCREMENT,
  `topleftX` INT NULL,
  `topleftY` INT NULL,
  `bottomrightX` INT NULL,
  `bottomrightY` INT NULL,
  `gsv_pano_panoid` VARCHAR(64) NOT NULL,
  `species_id` INT NOT NULL,
  PRIMARY KEY (`bbid`, `gsv_pano_panoid`, `species_id`),
  UNIQUE INDEX `bbid_UNIQUE` (`bbid` ASC),
  INDEX `fk_bounding_box_gsv_pano_idx` (`gsv_pano_panoid` ASC),
  INDEX `fk_bounding_box_species1_idx` (`species_id` ASC),
  CONSTRAINT `fk_bounding_box_gsv_pano`
    FOREIGN KEY (`gsv_pano_panoid`)
    REFERENCES `invasivespecies`.`gsv_pano` (`panoid`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_bounding_box_species1`
    FOREIGN KEY (`species_id`)
    REFERENCES `invasivespecies`.`species` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;


-- insert some data to species table

insert into species ( description ) values ('Phragmite');

insert into species ( description ) values ('Japanese Knotweed');

insert into species ( description ) values ('Common Reed');

insert into species ( description ) values ('Wild Parsnip');

insert into species ( description ) values ('Purple Loostrife');