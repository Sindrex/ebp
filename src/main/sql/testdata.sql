START TRANSACTION;

DELETE FROM bike_status;
DELETE FROM repair_return;
DELETE FROM repair;
DELETE FROM bike;
DELETE FROM user;
DELETE FROM docking_status;
DELETE FROM docking_station;
DELETE FROM bike_type;
DELETE FROM admin;

INSERT INTO user (email, password_hash, salt) VALUES
  ("jorgerei@ntnu.com", password(concat("jorgerei", "salt")), "salt");                                                  -- User Jørgen
INSERT INTO user (email, password_hash, salt) VALUES
  ("odderikf@ntnu.com", password(concat("odderikf", "salt")), "salt");                                                  -- User Odd-Erik
INSERT INTO user (email, password_hash, salt) VALUES
  ("sindrhpa@ntnu.com", password(concat("sindrhpa", "salt")), "salt");                                                  -- User Sindre P
INSERT INTO user (email, password_hash, salt) VALUES
  ("aleksjoh@ntnu.com", password(concat("aleksjoh", "salt")), "salt");                                                  -- User Aleksander
INSERT INTO user (email, password_hash, salt) VALUES
  ("sindrtho@ntnu.com", password(concat("sindrtho", "salt")), "salt");                                                  -- User Sindre T

INSERT INTO admin VALUES ("jorgerei@stud.ntnu.no", password(concat("jorgerei", "salt")), "salt");                                      -- Admin Jørgen
INSERT INTO admin VALUES ("odderikf@stud.ntnu.no", password(concat("odderikf", "salt")), "salt");                                      -- Admin Odd-Erik
INSERT INTO admin VALUES ("aleksjoh@stud.ntnu.no", password(concat("aleksjoh", "salt")), "salt");                                    -- Admin Aleksander
INSERT INTO admin VALUES ("sindrhpa@stud.ntnu.no", password(concat("sindrhpa", "salt")), "salt");                                    -- Admin Sindre P
INSERT INTO admin VALUES ("sindrtho@stud.ntnu.no", password(concat("sindrtho", "salt")), "salt");                                    -- Admin Sindre T
INSERT INTO admin VALUES ("admin", password(concat("password", "salt")), "salt");                                       -- Admin Default

INSERT INTO docking_station (position_lat, position_long, max_bikes, active) VALUES (63.43049, 10.39506, 100, 1);       -- Trondheim torg docking
INSERT INTO docking_station (position_lat, position_long, max_bikes, active) VALUES (63.361909, 10.377526, 200, 1);     -- City syd docking
INSERT INTO docking_station (position_lat, position_long, max_bikes, active) VALUES (63.427057, 10.392525, 25, 1);      -- Prinsen kinosenter docking
INSERT INTO docking_station (position_lat, position_long, max_bikes, active) VALUES (63.436700, 10.398820, 50, 1);      -- Trondheim S docking
INSERT INTO docking_station (position_lat, position_long, max_bikes, active) VALUES (63.439828, 10.462645, 200, 1);     -- Lade arena docking
INSERT INTO docking_station (position_lat, position_long, max_bikes, active) VALUES (63.427992, 10.375735, 200, 1);     -- Trondheim spektrum docking
INSERT INTO docking_station (position_lat, position_long, max_bikes, active) VALUES (63.422357, 10.431935, 50, 1);      -- Tyholttårnet docking
INSERT INTO docking_station (position_lat, position_long, max_bikes, active) VALUES (63.440294, 10.400506, 100, 1);     -- Pirbadet docking
INSERT INTO docking_station (position_lat, position_long, max_bikes, active) VALUES (63.419687, 10.401699, 100, 1);     -- Gløshaugen docking
INSERT INTO docking_station (position_lat, position_long, max_bikes, active) VALUES (63.412134, 10.402053, 200, 1);     -- Lerkandal docking
INSERT INTO docking_station (position_lat, position_long, max_bikes, active) VALUES (63.435302, 10.409524, 100, 1);     -- Solsiden docking


COMMIT;
SET AUTOCOMMIT = TRUE;