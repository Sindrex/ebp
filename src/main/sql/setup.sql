START TRANSACTION;
SET AUTOCOMMIT = FALSE;

DROP TABLE IF EXISTS bike_status;
DROP TABLE IF EXISTS repair_return;
DROP TABLE IF EXISTS repair;
DROP TABLE IF EXISTS bike;
DROP TABLE IF EXISTS user;
DROP TABLE IF EXISTS docking_status;
DROP TABLE IF EXISTS docking_station;
DROP TABLE IF EXISTS bike_type;
DROP TABLE IF EXISTS admin;

CREATE TABLE user(
  user_id INT PRIMARY KEY AUTO_INCREMENT,
  email VARCHAR(50) UNIQUE NOT NULL,
  password_hash VARCHAR(128) NOT NULL,
  salt VARCHAR(100) NOT NULL
);

CREATE TABLE admin(
  username VARCHAR(30) PRIMARY KEY,
  password_hash VARCHAR(128) NOT NULL,
  salt VARCHAR(100) NOT NULL
);

CREATE TABLE bike_type(
  name VARCHAR(30) PRIMARY KEY,
  timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  active ENUM("ACTIVE", "DELETED") DEFAULT "ACTIVE"
);

CREATE TABLE bike(
  bike_id INT PRIMARY KEY AUTO_INCREMENT,
  type VARCHAR(30) NOT NULL,
  make VARCHAR(30) NOT NULL,
  price DOUBLE NOT NULL,
  date_purchased DATE NOT NULL,
  active_status ENUM("RENTED", "DOCKED", "REPAIR", "DELETED") DEFAULT "DOCKED",
  user_id INT,      -- If active = rented, user_id not null, station_id null
  station_id INT,    -- If active = docked, station_id not null, user_id null
  -- If active = repair/deleted, user_id and station_id null
  -- ,keeper_id int foreign key keeper_id references(user.user_id, docking_station.station_id)
  status_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  charging_level DOUBLE NOT NULL DEFAULT -1,
  total_km DOUBLE DEFAULT 0. NOT NULL,
  total_trips INT DEFAULT 0,
  position_long DOUBLE DEFAULT NULL ,
  position_lat DOUBLE DEFAULT NULL
);

CREATE TABLE bike_status(
  status_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  bike_id INT NOT NULL ,
  charging_level DOUBLE NOT NULL DEFAULT 100,
  total_km DOUBLE DEFAULT 0. NOT NULL,
  total_trips INT DEFAULT 0,
  position_long DOUBLE DEFAULT NULL ,
  position_lat DOUBLE DEFAULT NULL ,
  active_status ENUM("RENTED", "DOCKED", "REPAIR", "DELETED") DEFAULT NULL ,
  user_id INT DEFAULT NULL ,
  station_id INT DEFAULT NULL,
  bike_status_id INT AUTO_INCREMENT PRIMARY KEY -- temporary, for simulation only.
  -- PRIMARY KEY (status_timestamp, bike_id)
);

CREATE TABLE repair(
  repair_id INT PRIMARY KEY AUTO_INCREMENT,
  request_date DATE NOT NULL,
  description VARCHAR(200),
  bike_id INT NOT NULL
);

CREATE TABLE repair_return(
  repair_id INT,
  return_date DATE NOT NULL,
  price DOUBLE NOT NULL,
  description VARCHAR(200) NOT NULL,
  PRIMARY KEY (repair_id)
);

CREATE TABLE docking_station(
  station_id INT PRIMARY KEY AUTO_INCREMENT,
  position_long DOUBLE NOT NULL,
  position_lat DOUBLE NOT NULL,
  max_bikes INT NOT NULL,
  active BIT DEFAULT 1 NOT NULL,
  power_usage DOUBLE DEFAULT 0 NOT NULL,
  status_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE docking_status(
  status_timestamp TIMESTAMP,
  station_id INT,
  power_usage DOUBLE DEFAULT 0 NOT NULL,
  PRIMARY KEY (status_timestamp, station_id)
);




  -- Legger på foreign keys.

ALTER TABLE bike
  ADD CONSTRAINT type FOREIGN KEY (type) REFERENCES bike_type(name);

ALTER TABLE bike
  ADD CONSTRAINT user_id FOREIGN KEY (user_id) REFERENCES user(user_id);

ALTER TABLE bike
  ADD CONSTRAINT station_id FOREIGN KEY (station_id) REFERENCES docking_station(station_id);

ALTER TABLE bike_status
  ADD CONSTRAINT status1_fk FOREIGN KEY(bike_id) REFERENCES bike(bike_id);

ALTER TABLE bike_status
  ADD CONSTRAINT status2_fk FOREIGN KEY(user_id) REFERENCES user(user_id);

ALTER TABLE bike_status
  ADD CONSTRAINT status3_fk FOREIGN KEY(station_id) REFERENCES docking_station(station_id);

ALTER TABLE repair
  ADD CONSTRAINT bike_id FOREIGN KEY(bike_id) REFERENCES bike(bike_id);

ALTER TABLE repair_return
  ADD CONSTRAINT return_fk FOREIGN KEY(repair_id) REFERENCES repair(repair_id);

ALTER TABLE docking_status
  ADD CONSTRAINT status4_fk FOREIGN KEY(station_id) REFERENCES docking_station(station_id);

-- constraints
delimiter $$

CREATE TRIGGER owner_status_correspondence_insert BEFORE INSERT ON bike
  FOR EACH ROW
  BEGIN
    IF(
      (NEW.active_status = "DOCKED" AND (NEW.user_id IS NOT NULL OR NEW.station_id IS NULL ))
      OR (NEW.active_status = "RENTED" AND (NEW.user_id IS NULL OR NEW.station_id IS NOT NULL ))
      OR (NEW.active_status = "REPAIR" OR NEW.active_status="DELETED") AND (NEW.user_id IS NOT NULL OR NEW.station_id IS NOT NULL )
    )
    THEN
      SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'Invalid state of active and owner';
    END IF;
  END$$

CREATE TRIGGER owner_status_correspondence_update BEFORE UPDATE ON bike
  FOR EACH ROW
  BEGIN
    IF(
      (NEW.active_status = "DOCKED" AND (NEW.user_id IS NOT NULL OR NEW.station_id IS NULL ))
      OR (NEW.active_status = "RENTED" AND (NEW.user_id IS NULL OR NEW.station_id IS NOT NULL ))
      OR (NEW.active_status = "REPAIR" OR NEW.active_status="DELETED") AND (NEW.user_id IS NOT NULL OR NEW.station_id IS NOT NULL )
    )
    THEN
      SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'Invalid state of active and owner';
    END IF;
  END$$

CREATE TRIGGER lat_long_status_correspondence_update BEFORE UPDATE ON bike_status
  FOR EACH ROW
  BEGIN
    IF(
      (NEW.active_status != "REPAIR" AND (NEW.position_long is NULL OR NEW.position_lat IS NULL ))
      OR (NEW.active_status = "REPAIR" AND (NEW.position_long is NOT NULL OR NEW.position_lat IS NOT NULL ))
    )
    THEN
      SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'Invalid state of lat_long and active_status';
    END IF;
  END$$

CREATE TRIGGER lat_long_status_correspondence_insert BEFORE INSERT ON bike_status
  FOR EACH ROW
  BEGIN
    IF(
      (NEW.active_status != "REPAIR" AND (NEW.position_long is NULL OR NEW.position_lat IS NULL ))
      OR (NEW.active_status = "REPAIR" AND (NEW.position_long is NOT NULL OR NEW.position_lat IS NOT NULL ))
    )
    THEN
      SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'Invalid state of lat_long and active_status';
    END IF;
  END$$


CREATE TRIGGER docking_station_max_bikecount BEFORE INSERT ON bike
  FOR EACH ROW
  BEGIN
    IF NEW.active_status="DOCKED" AND TRUE IN (
      SELECT
        (SELECT count(bike_id)+1
         FROM bike
         WHERE bike.station_id = NEW.station_id
        ) > max_bikes
      FROM docking_station
      WHERE docking_station.station_id=NEW.station_id)
    THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'too many bikes in a docking_station';
    END IF;
  END $$

CREATE TRIGGER docking_station_max_bikecount_update BEFORE UPDATE ON bike
  FOR EACH ROW
  BEGIN
    IF NEW.active_status="DOCKED"
       AND NEW.station_id != OLD.station_id
       AND TRUE IN (
      SELECT
        (SELECT count(bike_id)+1
         FROM bike
         WHERE bike.station_id = NEW.station_id
        ) > max_bikes
      FROM docking_station
      WHERE docking_station.station_id=NEW.station_id)
    THEN
      SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'too many bikes in a docking_station';
    END IF;
  END $$


CREATE TRIGGER gen_docking_status AFTER UPDATE ON docking_station
  FOR EACH ROW
  BEGIN
    INSERT INTO docking_status VALUES (NEW.status_timestamp, NEW.station_id, NEW.power_usage);
  END$$

CREATE TRIGGER gen_docking_status_insert AFTER INSERT ON docking_station
  FOR EACH ROW
  BEGIN
    INSERT INTO docking_status VALUES (NEW.status_timestamp, NEW.station_id, NEW.power_usage);
  END $$

CREATE TRIGGER gen_bike_status AFTER UPDATE ON bike
  FOR EACH ROW
  BEGIN
    INSERT INTO
      bike_status (status_timestamp   ,   bike_id,      charging_level,     total_km,     total_trips,      position_long,      position_lat,     active_status,    user_id,      station_id)
      VALUES      (NEW.status_timestamp,  NEW.bike_id,  NEW.charging_level, NEW.total_km, NEW.total_trips,  NEW.position_long,  NEW.position_lat, NEW.active_status,  NEW.user_id,  NEW.station_id);
  END $$

CREATE TRIGGER gen_bike_status_insert AFTER INSERT ON bike
  FOR EACH ROW
  BEGIN
    INSERT INTO
      bike_status (status_timestamp   ,   bike_id,      charging_level,     total_km,     total_trips,      position_long,      position_lat,     active_status,    user_id,      station_id)
    VALUES      (NEW.status_timestamp,  NEW.bike_id,  NEW.charging_level, NEW.total_km, NEW.total_trips,  NEW.position_long,  NEW.position_lat, NEW.active_status,  NEW.user_id,  NEW.station_id);
  END $$

CREATE TRIGGER test_set_bike_random_charging_level BEFORE INSERT ON bike
  FOR EACH ROW
  BEGIN
    IF NEW.charging_level = -1
      THEN
      SET NEW.charging_level = RAND()*100;
    END IF ;
  END $$
delimiter ;

COMMIT;
SET AUTOCOMMIT = TRUE;
